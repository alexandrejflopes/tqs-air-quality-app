package com.example.airquality.service;

import com.example.airquality.client.ReportHttpClient;
import com.example.airquality.entity.*;
import com.example.airquality.entity.Error;
import com.example.airquality.repository.ReportRepository;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

@Service
@Transactional
public class ReportService {

    private URIBuilder uriBuilder;
    private ReportHttpClient httpClient;

    public void setHttpClient(ReportHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Autowired
    private ReportRepository reportRepository;


    public Report getReportByLocation(Location location){
        Optional<Report> result = reportRepository.findById(location);

        if(result.isPresent()){
            return result.get();
        }

        throw new NoSuchElementException("Could not find report with the provided location.");

    }


    public Report getReportByCoordiantes(Coordinates coordinates){

        List<Report> allReports = reportRepository.findAll();

        for(Report r : allReports){
            if(r.getLocation().getCoordinates().equals(coordinates)){
                return r;
            }
        }

        return null;
    }

    public boolean existsReportWithLocation(Location location){
        return reportRepository.existsById(location);
    }

    public boolean existsReportWithCoordinates(Coordinates coordinates){
        List<Report> allReports = reportRepository.findAll();

        boolean exists = false;

        for(Report r : allReports){
            if(r.getLocation().getCoordinates().equals(coordinates)){
                exists = true;
                break;
            }
        }

        return exists;
    }

    public Report saveReport(Report report){
        return reportRepository.save(report);
    }

    public Report getReportForInput(String userInput) throws IOException, URISyntaxException, ParseException {

        Location location = requestLocationDataForInput(userInput);

        /*
        * if there's already a cached report for the location, return that
        * report instead of making an unnecessary API request
        * */
        if(existsReportWithLocation(location)){
            return getReportByLocation(location);
        }
        else{
            Report report = requestNewReportForLocation(location);
            saveReport(report);
            return report;
        }

    }


    public Report requestNewReportForLocation(Location location) throws URISyntaxException, IOException, ParseException {


        uriBuilder = new URIBuilder("https://api.breezometer.com/air-quality/v2/current-conditions?key=09e19031c4764f0097225dd225826731");
        uriBuilder.addParameter("lat", (new Formatter()).format("%.6f", location.getCoordinates().getLatitude()).toString() );
        uriBuilder.addParameter("lon", (new Formatter()).format("%.6f", location.getCoordinates().getLongitude()).toString() );
        uriBuilder.addParameter("features", "breezometer_aqi,local_aqi,health_recommendations,sources_and_effects,dominant_pollutant_concentrations,pollutants_concentrations,pollutants_aqi_information");
        uriBuilder.addParameter("metadata", "true");

        System.err.println(" url is --> " + uriBuilder.build().toString());

        String response = this.httpClient.get(uriBuilder.build().toString());
        JSONObject obj = (JSONObject) new JSONParser().parse(response);


        JSONObject dataObject = (JSONObject) obj.get("data");


        Report resultReport = new Report();

        if(dataObject.isEmpty()){
            /* in case of error (location unsupported, etc) */

            JSONObject errorObject = (JSONObject) obj.get("error");
            String code = (String) errorObject.get("code");
            String title = (String) errorObject.get("title");

            Error error = new Error(code, title);

            resultReport.setLocation(location);
            resultReport.setDataAvailable(false);
        }

        else {
            // request timestamp
            String timestampString = (String) ((JSONObject) obj.get("metadata")).get("timestamp");
            LocalDateTime requestTimestamp = LocalDateTime.parse(timestampString);


            // data timestamp
            LocalDateTime lastUpdate = LocalDateTime.parse((String) dataObject.get("datetime"));
            boolean dataAvailable = (boolean) dataObject.get("data_available");


            // INDEX
            JSONObject indexObject = (JSONObject) ((JSONObject) dataObject.get("indexes")).get("baqi");
            String dominantPollutantName = (String) indexObject.get("dominant_pollutant");
            String displayName = (String) indexObject.get("display_name");
            String valueDisplay = (String) indexObject.get("aqi_display");
            String category = (String) indexObject.get("category");
            Index index = new Index(displayName, valueDisplay, category);


            // POLLUTANTS
            JSONObject pollutantsObject = (JSONObject) dataObject.get("pollutants");

            Set keys = pollutantsObject.keySet();

            List<Pollutant> pollutants = new ArrayList<>();
            for (Object key : keys){
                if(pollutantsObject.get(key) instanceof JSONObject){
                    JSONObject pollutantData = (JSONObject) pollutantsObject.get(key);
                    String symbol = (String) pollutantData.get("display_name");
                    String fullName = (String) pollutantData.get("full_name");
                    Pollutant pollutant = new Pollutant(symbol, fullName);

                    JSONObject concentrationObj = (JSONObject) pollutantData.get("concentration");
                    double value = (double) concentrationObj.get("value");
                    String units = (String) concentrationObj.get("units");
                    Concentration concentration = new Concentration(value,units);
                    pollutant.setConcentration(concentration);

                    pollutants.add(pollutant);

                    // check if is this the dominant pollutant
                    if(symbol.equalsIgnoreCase(dominantPollutantName)){
                        index.setDominantPollutant(pollutant);
                    }
                }
            }

            Error error = new Error(); // no error

            // setup report
            resultReport.setDataAvailable(dataAvailable);
            resultReport.setLocation(location);
            resultReport.setError(error);
            resultReport.setIndex(index);
            resultReport.setLastUpdatedAt(lastUpdate);
            resultReport.setPollutants(pollutants);
            resultReport.setRequestTimeStamp(requestTimestamp);

        }

        return resultReport;

    }

    public Location requestLocationDataForInput(String locationInput) throws URISyntaxException, IOException, ParseException {

        uriBuilder = new URIBuilder("http://open.mapquestapi.com/geocoding/v1/address?key=zB8zqDRHU5QIZxabKtiTQGSoHeOMXNeK");
        uriBuilder.addParameter("location", locationInput );

        System.err.println(" url is --> " + uriBuilder.build().toString());

        String response = this.httpClient.get(uriBuilder.build().toString());


        // get parts from response till reaching the address
        JSONObject obj = (JSONObject) new JSONParser().parse(response);
        obj =(JSONObject)((JSONArray) obj.get("results")).get(0);
        JSONObject addressObj =(JSONObject)((JSONArray) obj.get("locations")).get(0);
        JSONObject coordinates = (JSONObject) addressObj.get("latLng");

        double latitude = Double.parseDouble((String.valueOf((double) coordinates.get("lat"))).replace(",", "."));
        double longitude = Double.parseDouble((String.valueOf((double) coordinates.get("lng"))).replace(",", "."));

        String city = (String) addressObj.get("adminArea5");
        String county = (String) addressObj.get("adminArea4");
        String address = city + ", " + county;
        String countryCode = (String) addressObj.get("adminArea1");

        Location location = new Location(new Coordinates(latitude,longitude), countryCode, address);

        System.err.println(" location is --> " + location.toString());

        return location;


    }


    // deprecated
    public Coordinates getCoordinatesFromResponse(String response) throws ParseException {

        // get parts from response till reaching the address
        JSONObject obj = (JSONObject) new JSONParser().parse(response);
        obj =(JSONObject)((JSONArray) obj.get("results")).get(0);
        JSONObject address =(JSONObject)((JSONArray) obj.get("locations")).get(0);
        JSONObject coordinates = (JSONObject) address.get("latLng");

        double latitude = (double) coordinates.get("lat");
        double longitude = (double) coordinates.get("lng");

        return new Coordinates(latitude, longitude);

    }

    // deprecated
    public String getAddressFromResponse(String response) throws ParseException {

        // get parts from response till reaching the address
        JSONObject obj = (JSONObject) new JSONParser().parse(response);
        obj =(JSONObject)((JSONArray) obj.get("results")).get(0);
        JSONObject address =(JSONObject)((JSONArray) obj.get("locations")).get(0);

        String city = (String) address.get("adminArea5");
        String county = (String) address.get("adminArea4");

        return city + ", " + county;

    }

}
