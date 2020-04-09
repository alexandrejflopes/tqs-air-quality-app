package com.example.airquality;


import com.example.airquality.entity.*;
import com.example.airquality.repository.ReportRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AirQualityWebAppApplication.class)
@AutoConfigureMockMvc
// @TestPropertySource(locations = "application-integrationtest.properties")
@AutoConfigureTestDatabase
public class ReportRestControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ReportRepository reportRepository;

    @AfterEach
    public void resetDb() {
        reportRepository.deleteAll();
    }

    @Test
    public void whenValidLocationName_whenGetReportForThatLocation_thenCacheReport() throws Exception {
        Report aveiroReport = setUpAveiroReport();
        String location_input = "Aveiro";

        mvc.perform(get("/api/location/"+location_input)
                .contentType(MediaType.APPLICATION_JSON));

        Optional<Report> fromDB = reportRepository.findById(aveiroReport.getLocation());

        assertThat(fromDB.isPresent()).isEqualTo(true);

        /*
         * as ids, timestamps and index and pollutants metrics change
         * depending on the moment of request, we need to sync
         * those in the testing report with the one obtained from DB
         * in order to have a 'fair' comparison;
         * if all went right, all the rest should be the same
         * */
        aveiroReport = updateMetrics(aveiroReport,fromDB.get());
        assertThat(fromDB.get()).isEqualTo(aveiroReport);

    }

    @Test
    public void whenValidLocationName_whenGetReportForThatLocation_thenStatus200() throws Exception {
        Report aveiroReport = setUpAveiroReport();
        saveTestReport(aveiroReport);
        String location_input = "Aveiro";

        mvc.perform(get("/api/location/"+location_input).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.location.countryCode", is(aveiroReport.getLocation().getCountryCode())))
                .andExpect(jsonPath("$.location.address", is(aveiroReport.getLocation().getAddress())))
                .andExpect(jsonPath("$.location.coordinates.latitude", is(aveiroReport.getLocation().getCoordinates().getLatitude())))
                .andExpect(jsonPath("$.location.coordinates.longitude", is(aveiroReport.getLocation().getCoordinates().getLongitude())))
                .andExpect(jsonPath("$.errorCode", is(aveiroReport.getErrorCode())))
                .andExpect(jsonPath("$.errorTitle", is(aveiroReport.getErrorTitle())));
    }

    @Test
    public void whenInvalidLocationName_whenGetReportForThatLocation_thenStatus400() throws Exception {
        String location_input = "";


        mvc.perform(get("/api/location/"+location_input).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }



    public Report setUpAveiroReport(){
        Location aveiro = new Location(new Coordinates(40.640496, -8.653784), "PT","Aveiro, Aveiro");

        Report aveiroReport = new Report();
        aveiroReport.setRequestTimeStamp(LocalDateTime.parse("2020-04-08T13:29:15"));
        aveiroReport.setLastUpdatedAt(LocalDateTime.parse("2020-04-08T13:00"));
        aveiroReport.setLocation(aveiro);
        aveiroReport.setDataAvailable(true);
        Pollutant dominantPollutant = new Pollutant("03","Ozone", new Concentration(29.75,"ppb"));
        aveiroReport.setIndex(new Index("BreezoMeter AQI", "77", "Good air quality", dominantPollutant));

        Pollutant no2 = new Pollutant("NO2","Nitrogen dioxide", new Concentration(7.05,"ppb"));
        Pollutant pm25 = new Pollutant("PM2.5","Fine particulate matter (<2.5µm)", new Concentration(2.05,"ug/m3"));
        Pollutant so2 = new Pollutant("SO2","Sulfur dioxide", new Concentration(0.09,"ppb"));
        Pollutant pm10 = new Pollutant("PM10","Inhalable particulate matter (<10µm)", new Concentration(8.23,"ug/m3"));
        Pollutant co = new Pollutant("CO","Carbon monoxide", new Concentration(135.98,"ppb"));

        List<Pollutant> pollutantList = Arrays.asList(no2, dominantPollutant, pm25, so2, pm10, co);
        aveiroReport.setPollutants(pollutantList);
        aveiroReport.removeError();
        aveiroReport.setErrorCode("NA");
        aveiroReport.setErrorTitle("NA");

        // in the first request the report is not cached, so it's a miss
        aveiroReport.setLocationCacheStats(new CacheStats(0,1,1));
        aveiroReport.setGlobalCacheStats(new CacheStats(0,1,1));
        return aveiroReport;
    }

    private Report updateMetrics(Report toUpdate, Report withNewMetrics){
        toUpdate.setIndex(withNewMetrics.getIndex());
        toUpdate.setPollutants(withNewMetrics.getPollutants());
        toUpdate.setLastUpdatedAt(withNewMetrics.getLastUpdatedAt());
        toUpdate.setRequestTimeStamp(withNewMetrics.getRequestTimeStamp());

        // granting that cache stats ids are the same
        toUpdate.getGlobalCacheStats().setId(withNewMetrics.getGlobalCacheStats().getId());
        toUpdate.getLocationCacheStats().setId(withNewMetrics.getLocationCacheStats().getId());
        return toUpdate;
    }


    private void saveTestReport(Report report) {
        reportRepository.saveAndFlush(report);
    }

}
