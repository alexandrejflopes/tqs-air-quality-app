package com.example.airquality.service;

import com.example.airquality.client.ReportHttpClient;
import com.example.airquality.entity.*;
import com.example.airquality.exception.InvalidLocationException;
import com.example.airquality.repository.ReportRepository;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URISyntaxException;
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

    private static final String BREEZOMETER_API_KEY = setUpBreezometerApiKey();
    private static final String MAPQUEST_API_KEY = setUpMapQuestApiKey();


    public static final String GEOCODING_BASE_URL = "http://open.mapquestapi.com/geocoding/v1/address?key="+MAPQUEST_API_KEY;

    public static final String BREEZOMETER_BASE_URL = "https://api.breezometer.com/air-quality/v2/current-conditions?key="+BREEZOMETER_API_KEY;


    private CacheStats globalCacheStats = new CacheStats();

    private Map<Location, CacheStats> cacheStatsMap = new LinkedHashMap<>();

    private URIBuilder uriBuilder;
    private ReportHttpClient httpClient;

    public void setHttpClient(ReportHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private static String setUpBreezometerApiKey() {

        String breezometerKeyFile = "src/main/java/com/example/airquality/breezometerKey.txt";

        try(BufferedReader reader = new BufferedReader(new FileReader(breezometerKeyFile))){
            return reader.readLine();
        }
        catch (Exception e){
            // default key
            return "09e19031c4764f0097225dd225826731";
        }

    }

    private static String setUpMapQuestApiKey() {

        String mapQuestKeyFile = "src/main/java/com/example/airquality/mapquestKey.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(mapQuestKeyFile))) {
            return reader.readLine();
        }
        catch (Exception e){
            // default key
            return "zB8zqDRHU5QIZxabKtiTQGSoHeOMXNeK";
        }

    }

    @Autowired
    private ReportRepository reportRepository;


    public Report getReportByLocation(Location location){
        Optional<Report> result = reportRepository.findById(location);

        return result.orElse(null);
    }

    public boolean existsReportWithLocation(Location location){
        return reportRepository.existsById(location);
    }

    public Report saveReport(Report report){
        return reportRepository.save(report);
    }

    public Report getReportForInput(String userInput) throws IOException, URISyntaxException, ParseException, InvalidLocationException {

        Location location = requestLocationDataForInput(userInput);

        boolean invalidLocation = location == null || location.getCoordinates()==null || location.getAddress() == null || location.getCountryCode() == null;

        if(invalidLocation){
            throw new InvalidLocationException();
        }

        CacheStats locationCacheStats;

        // a new request to be recorded in global stats
        globalCacheStats.addRequest();

        // get cache data for this location, if exists and count request
        if(cacheStatsMap.containsKey(location)){
            locationCacheStats = cacheStatsMap.get(location);
        }
        else{
            locationCacheStats = new CacheStats();
        }

        locationCacheStats.addRequest();



        /*
        * if there's already a cached report for the location, return that
        * report instead of making an unnecessary external API request
        * */
        if(existsReportWithLocation(location)){
            Report report = getReportByLocation(location);

            if(report.hasError()){
                // if it's a report with error already, do not process it
                locationCacheStats.addHit();
                cacheStatsMap.put(location,locationCacheStats); // update location stats
                globalCacheStats.addHit();
                report.setLocationCacheStats(locationCacheStats);
                report.setGlobalCacheStats(globalCacheStats);

                saveReport(report); // update report with new cache stats
                return report;
            }


            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            LocalDateTime lastRequestTimeStamp = report.getRequestTimeStamp();

            boolean differentHour = (now.getHour() != lastRequestTimeStamp.getHour()) &&
                                    (now.getMinute()>=2); // give some time to external API to update

            boolean differentHourOrDay = differentHour ||
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
                globalCacheStats.addMiss();

                Report newReport = requestNewReportForLocation(location);
                newReport.setLocationCacheStats(locationCacheStats);
                newReport.setGlobalCacheStats(globalCacheStats);

                saveReport(newReport);
                return newReport;
            }


            // if a new request is not made, add a hit in the cache
            locationCacheStats.addHit();
            cacheStatsMap.put(location,locationCacheStats); // update location stats
            globalCacheStats.addHit();
            report.setLocationCacheStats(locationCacheStats);
            report.setGlobalCacheStats(globalCacheStats);

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
            globalCacheStats.addMiss();
            cacheStatsMap.put(location, locationCacheStats);

            report.setLocationCacheStats(locationCacheStats);
            report.setGlobalCacheStats(globalCacheStats);
            saveReport(report);
            return report;
        }

    }

    public Report requestNewReportForLocation(Location location) throws URISyntaxException, IOException, ParseException, InvalidLocationException {

        boolean invalidLocation = location == null || location.getCoordinates()==null || location.getAddress() == null || location.getCountryCode() == null;

        if(invalidLocation){
            throw new InvalidLocationException();
        }

        String lat = String.valueOf(location.getCoordinates().getLatitude());
        String lon = String.valueOf(location.getCoordinates().getLongitude());

        uriBuilder = new URIBuilder(BREEZOMETER_BASE_URL);
        uriBuilder.addParameter("lat", lat );
        uriBuilder.addParameter("lon", lon );
        uriBuilder.addParameter("features", "breezometer_aqi,local_aqi,health_recommendations,sources_and_effects,dominant_pollutant_concentrations,pollutants_concentrations,pollutants_aqi_information");
        uriBuilder.addParameter("metadata", "true");

        String response = this.httpClient.get(uriBuilder.build().toString());
        JSONObject obj = (JSONObject) new JSONParser().parse(response);

        JSONObject dataObject;

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
                boolean keyIsJsonObject = pollutantsObject.get(key) instanceof JSONObject;

                if(keyIsJsonObject){
                    JSONObject pollutantData = (JSONObject) pollutantsObject.get(key);
                    String symbol = (String) pollutantData.get("display_name");
                    String fullName = (String) pollutantData.get("full_name");
                    Pollutant pollutant = new Pollutant(symbol, fullName);

                    JSONObject concentrationObj = (JSONObject) pollutantData.get("concentration");
                    double value = extractConcentrationValue(concentrationObj);
                    String units = (String) concentrationObj.get("units");
                    Concentration concentration = new Concentration(value,units);
                    pollutant.setConcentration(concentration);

                    pollutants.add(pollutant);

                    // check if is this the dominant pollutant
                    // remove special characters from symbol in order to compare it to its pollutant name
                    if(symbol.replace(".","").equalsIgnoreCase(dominantPollutantName)){
                        index.setDominantPollutant(pollutant);
                    }
                }
            }

            resultReport.removeError();
            resultReport.setErrorCode("NA");
            resultReport.setErrorTitle("NA");

            // setup report
            resultReport.setDataAvailable(dataAvailable);
            resultReport.setLocation(location);
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

            resultReport.putError();
            resultReport.setErrorCode(code);
            resultReport.setErrorTitle(title);
            resultReport.setLocation(location);
            resultReport.setDataAvailable(false);
        }

        return resultReport;

    }

    public Location requestLocationDataForInput(String locationInput) throws URISyntaxException, IOException {

        uriBuilder = new URIBuilder(GEOCODING_BASE_URL);
        uriBuilder.addParameter("location", locationInput );

        String response = this.httpClient.get(uriBuilder.build().toString());

        try {
            JSONObject obj = (JSONObject) new JSONParser().parse(response);
            obj =(JSONObject)((JSONArray) obj.get("results")).get(0);
            JSONObject addressObj =(JSONObject)((JSONArray) obj.get("locations")).get(0);
            JSONObject coordinates = (JSONObject) addressObj.get("latLng");

            double latitude = (double) coordinates.get("lat");
            double longitude = (double) coordinates.get("lng");

            String city = (String) addressObj.get("adminArea5");
            String county = (String) addressObj.get("adminArea4");
            String address = city.isEmpty() ? county : city + ", " + county;
            String countryCode = (String) addressObj.get("adminArea1");

            return new Location(new Coordinates(latitude,longitude), countryCode, address);
        }
        catch (Exception e){
            // invalid location
            return null;
        }


    }

    public double extractConcentrationValue(JSONObject concentrationObject){
        double value;

        /*
         * try to convert to double or long if it's not possible to cast;
         * if there's error, then the value is -1
         * */
        try {
            value = (double) concentrationObject.get("value");
        }
        catch (Exception e){
            value = convertConcentrationValue(concentrationObject);
        }

        return value;

    }

    public double convertConcentrationValue(JSONObject concentrationObject){
        double v;

        try {
            Long longValue = (long) concentrationObject.get("value");
            v = longValue.doubleValue();
        }
        catch (Exception ex){
            v = -1;
        }

        return v;
    }

    public CacheStats getGlobalCacheStats() {
        return globalCacheStats;
    }

    public CacheStats getLocationCacheStats(Location location) {
        if(cacheStatsMap.containsKey(location))
            return cacheStatsMap.get(location);
        return new CacheStats();
    }
}
