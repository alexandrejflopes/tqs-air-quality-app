package com.example.airquality.service;

import com.example.airquality.client.ReportHttpClient;
import com.example.airquality.entity.*;
import com.example.airquality.entity.LocationCacheStats;
import com.example.airquality.repository.ReportRepository;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

@Service
@Transactional
public class ReportService {

    private static final int TTL = 1;

    private GlobalCacheStats globalCacheStats = new GlobalCacheStats();

    private Map<Location, LocationCacheStats> cacheStatsMap = new LinkedHashMap<>();

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


    public boolean statsMapConatainsLocation(Location location){
        for(Location l : cacheStatsMap.keySet()){
            if(location.equals(l))
                return true;
        }

        return false;
    }

    public Report getReportForInput(String userInput) throws IOException, URISyntaxException, ParseException {

        System.err.println("-------------------------------------------------------------");
        System.err.println("-------------------------------------------------------------");

        System.err.println("location stats map");
        System.err.println(cacheStatsMap.toString());
        System.err.println();

        Location location = requestLocationDataForInput(userInput);

        LocationCacheStats locationCacheStats;

        // a new request to be recorded in global stats
        GlobalCacheStats localGlobalCacheStats = globalCacheStats;

        localGlobalCacheStats.addRequest();

        // get cache data for this location, if exists and count request
        if(cacheStatsMap.containsKey(location)){
            System.err.println("already got a location stats for that place");
            locationCacheStats = cacheStatsMap.get(location);
        }
        else{
            System.err.println("initialize new location stats for that place");
            locationCacheStats = new LocationCacheStats();
        }

        locationCacheStats.addRequest();

        /*
        * if there's already a cached report for the location, return that
        * report instead of making an unnecessary external API request
        * */
        if(existsReportWithLocation(location)){
            System.err.println("There's already a report in cache for " + location.toString());
            Report report = getReportByLocation(location);


            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            LocalDateTime lastRequestTimeStamp = report.getRequestTimeStamp();

            //System.err.println("lastRequestTimeStamp LDT: " + lastRequestTimeStamp.toString());
            //System.err.println("lastRequestTimeStamp ZT: " + ZonedDateTime.of(lastRequestTimeStamp, ZoneId.of("UTC")).toString());

            boolean differentHourOrDay = (now.getHour() != lastRequestTimeStamp.getHour()) ||
                                        (   now.getDayOfMonth()!=lastRequestTimeStamp.getDayOfMonth() ||
                                            now.getMonthValue()!=lastRequestTimeStamp.getMonthValue() ||
                                            now.getYear()!=lastRequestTimeStamp.getYear()
                                        );


            if(differentHourOrDay){
                /*
                * new data is available each hour in the external source (API),
                * so we must request new data if the user current hour is different
                * from the hour the data was last fetched externally or if the hour
                * is the same, but the user current day "is already in the future"
                * */

                // a new request means a miss in the cache
                locationCacheStats.addMiss();
                cacheStatsMap.put(location,locationCacheStats); // update location stats
                localGlobalCacheStats.addMiss();
                globalCacheStats = localGlobalCacheStats; // update global

                System.err.println("Old data. Requesting updated data...");

                Report new_report = requestNewReportForLocation(location);

                new_report.setLocationCacheStats(locationCacheStats);
                new_report.setGlobalCacheStats(localGlobalCacheStats);
                //System.err.println("Old reqTS: " + report.getRequestTimeStamp());
                //System.err.println("New reqTS: " + new_report.getRequestTimeStamp());
                //System.err.println("Old lastUpdated: " + report.getLastUpdatedAt());
                //System.err.println("New lastUpdated: " + new_report.getLastUpdatedAt());

                System.err.println("Miss!");
                System.err.println("locationCacheStats -> " + locationCacheStats.toString());
                System.err.println("globalCacheStats -> " + globalCacheStats.toString());
                saveReport(new_report);
                return new_report;
            }

            Duration duration = Duration.between(lastRequestTimeStamp, now);

            //System.err.println("now hour -> " + now.getHour());
            //System.err.println("reqTS hour -> " + lastRequestTimeStamp.getHour());

            //System.err.println("duration minutes -> " + duration.toMinutes());

            // implementation with a fixed TTL
            /*
            if(duration.toMinutes() >= TTL){
                // if the cached data was fetched TTL minutes ago, request new data

                System.err.println("Reached TTL. Requesting updated data...");

                Report new_report = requestNewReportForLocation(location);
                System.err.println("Old reqTS: " + report.getRequestTimeStamp());
                System.err.println("New reqTS: " + new_report.getRequestTimeStamp());
                System.err.println("Old lastUpdated: " + report.getLastUpdatedAt());
                System.err.println("New lastUpdated: " + new_report.getLastUpdatedAt());
                saveReport(new_report);
                return new_report;
            }
            */

            System.err.println("Found the report in cache");
            // if a new request is not made, add a hit in the cache
            locationCacheStats.addHit();
            cacheStatsMap.put(location,locationCacheStats); // update location stats
            localGlobalCacheStats.addHit();
            globalCacheStats = localGlobalCacheStats; // update global
            report.setLocationCacheStats(locationCacheStats);
            report.setGlobalCacheStats(localGlobalCacheStats);

            System.err.println("Hit!");
            System.err.println("locationCacheStats -> " + locationCacheStats.toString());
            System.err.println("globalCacheStats -> " + globalCacheStats.toString());
            saveReport(report); // update report with new cache stats
            return report;
        }
        else{
            /*
            * if a locations has never been searched:
            * - new entry in cacheStatsMap (for this new location)
            * - a miss in the cache for this location
            * */
            Report report = requestNewReportForLocation(location);
            locationCacheStats.addMiss();
            localGlobalCacheStats.addMiss();
            globalCacheStats = localGlobalCacheStats; // update global
            cacheStatsMap.put(location, locationCacheStats);

            System.err.println("Miss!");
            System.err.println("locationCacheStats -> " + locationCacheStats.toString());
            System.err.println("globalCacheStats -> " + globalCacheStats.toString());

            report.setLocationCacheStats(locationCacheStats);
            report.setGlobalCacheStats(localGlobalCacheStats);
            saveReport(report);
            return report;
        }

    }


    public Report requestNewReportForLocation(Location location) throws URISyntaxException, IOException, ParseException {

        String lat = String.valueOf(location.getCoordinates().getLatitude());
        String lon = String.valueOf(location.getCoordinates().getLongitude());

        //System.err.println(" lat is --> " + lat);
        //System.err.println(" lon is --> " + lon);

        uriBuilder = new URIBuilder("https://api.breezometer.com/air-quality/v2/current-conditions?key=09e19031c4764f0097225dd225826731");
        uriBuilder.addParameter("lat", lat );
        uriBuilder.addParameter("lon", lon );
        uriBuilder.addParameter("features", "breezometer_aqi,local_aqi,health_recommendations,sources_and_effects,dominant_pollutant_concentrations,pollutants_concentrations,pollutants_aqi_information");
        uriBuilder.addParameter("metadata", "true");

        //System.err.println(" url is --> " + uriBuilder.build().toString());

        String response = this.httpClient.get(uriBuilder.build().toString());
        JSONObject obj = (JSONObject) new JSONParser().parse(response);

        //System.err.println("response obj -> " + obj.toJSONString());


        JSONObject dataObject;

        //System.err.println("dataObject null ? -> " + dataObject==null);
        //System.err.println("dataObject -> " + dataObject.toJSONString());
        //System.err.println("dataObject empty ? -> " + dataObject.isEmpty());



        Report resultReport = new Report();

        try {
            dataObject = (JSONObject) obj.get("data");

            // request timestamp
            String timestampString = (String) ((JSONObject) obj.get("metadata")).get("timestamp");
            LocalDateTime requestTimestamp = LocalDateTime.parse(timestampString.replace("Z", ""));


            // data timestamp
            LocalDateTime lastUpdate = LocalDateTime.parse(((String) dataObject.get("datetime")).replace("Z", ""));
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
                    System.err.println("con. value -> " + concentrationObj.get("value"));
                    double value;
                    // tentar converter para double ou para long, caso não seja possível o cast
                    // em caso de erro, o value fica -1
                    try {
                        value = (double) concentrationObj.get("value");
                    }
                    catch (Exception e){
                        try {
                            Long longValue = (long) concentrationObj.get("value");
                            value = longValue.doubleValue();
                        }
                        catch (Exception ex){
                            value = -1;
                        }
                    }
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

            // Error error = new Error(); // no error
            resultReport.removeError();
            resultReport.setErrorCode("NA");
            resultReport.setErrorTitle("NA");

            // setup report
            resultReport.setDataAvailable(dataAvailable);
            resultReport.setLocation(location);
            //resultReport.setError(error);
            resultReport.setIndex(index);
            resultReport.setLastUpdatedAt(lastUpdate);
            resultReport.setPollutants(pollutants);
            resultReport.setRequestTimeStamp(requestTimestamp);
        }

        catch (Exception e){
            /* in case of error (location unsupported, etc) */

            JSONObject errorObject = (JSONObject) obj.get("error");
            String code = (String) errorObject.get("code");
            String title = (String) errorObject.get("title");

            //Error error = new Error(code, title);

            //resultReport.setError(error);
            resultReport.putError();
            resultReport.setErrorCode(code);
            resultReport.setErrorTitle(title);
            resultReport.setLocation(location);
            resultReport.setDataAvailable(false);
        }

        //System.err.println(" report a devolver --> " + resultReport.toString());

        return resultReport;

    }

    public Location requestLocationDataForInput(String locationInput) throws URISyntaxException, IOException, ParseException {

        uriBuilder = new URIBuilder("http://open.mapquestapi.com/geocoding/v1/address?key=zB8zqDRHU5QIZxabKtiTQGSoHeOMXNeK");
        uriBuilder.addParameter("location", locationInput );

        //System.err.println(" url is --> " + uriBuilder.build().toString());

        String response = this.httpClient.get(uriBuilder.build().toString());


        // get parts from response till reaching the address
        JSONObject obj = (JSONObject) new JSONParser().parse(response);
        obj =(JSONObject)((JSONArray) obj.get("results")).get(0);
        JSONObject addressObj =(JSONObject)((JSONArray) obj.get("locations")).get(0);
        JSONObject coordinates = (JSONObject) addressObj.get("latLng");

        double latitude = (double) coordinates.get("lat");
        double longitude = (double) coordinates.get("lng");

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
