package com.example.airquality;

import com.example.airquality.controller.ReportRestController;
import com.example.airquality.entity.*;
import com.example.airquality.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportRestController.class)
public class ReportControllerIT {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReportService reportService;

    @BeforeEach
    public void setUp() throws Exception {
    }


    @Test
    public void givenSupportedLocationName_whenGetReportForThatLocation_thenReturnJson() throws Exception {
        Report aveiroReport = setUpAveiroReport();
        String location_input = "Aveiro";

        given(reportService.getReportForInput(location_input)).willReturn(aveiroReport);

        mvc.perform(get("/api/location/"+location_input).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location.countryCode", is(aveiroReport.getLocation().getCountryCode())))
                .andExpect(jsonPath("$.location.address", is(aveiroReport.getLocation().getAddress())))
                .andExpect(jsonPath("$.location.coordinates.latitude", is(aveiroReport.getLocation().getCoordinates().getLatitude())))
                .andExpect(jsonPath("$.location.coordinates.longitude", is(aveiroReport.getLocation().getCoordinates().getLongitude())))
                .andExpect(jsonPath("$.errorCode", is(aveiroReport.getErrorCode())))
                .andExpect(jsonPath("$.errorTitle", is(aveiroReport.getErrorTitle())));
        verify(reportService, VerificationModeFactory.times(1)).getReportForInput(location_input);
        reset(reportService);
    }

    @Test
    public void givenUnsupportedLocationName_whenGetReportForThatLocation_thenReturnJson() throws Exception {
        Report antarcticaReport = setUpAntarcticaReport();
        String location_input = "Antarctica";

        given(reportService.getReportForInput(location_input)).willReturn(antarcticaReport);

        mvc.perform(get("/api/location/"+location_input).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location.countryCode", is(antarcticaReport.getLocation().getCountryCode())))
                .andExpect(jsonPath("$.location.address", is(antarcticaReport.getLocation().getAddress())))
                .andExpect(jsonPath("$.location.coordinates.latitude", is(antarcticaReport.getLocation().getCoordinates().getLatitude())))
                .andExpect(jsonPath("$.location.coordinates.longitude", is(antarcticaReport.getLocation().getCoordinates().getLongitude())))
                .andExpect(jsonPath("$.errorCode", is(antarcticaReport.getErrorCode())))
                .andExpect(jsonPath("$.errorTitle", is(antarcticaReport.getErrorTitle())));
        verify(reportService, VerificationModeFactory.times(1)).getReportForInput(location_input);
        reset(reportService);
    }

    // TODO: verficar o que se passa
    @Test
    public void givenInvalidLocationName_whenGetReportForThatLocation_thenReturnJson() throws Exception {
        String location_input = " ";

        given(reportService.getReportForInput(location_input)).willReturn(null);

        mvc.perform(get("/api/location/"+location_input).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(reportService, VerificationModeFactory.times(1)).getReportForInput(location_input);
        reset(reportService);
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

        return aveiroReport;
    }


    public Report setUpAntarcticaReport(){
        Location antarctica = new Location(new Coordinates(-82.108182, 34.37824), "AQ",", ");

        Report antarcticaReport = new Report();
        antarcticaReport.setLocation(antarctica);
        antarcticaReport.setDataAvailable(false);

        antarcticaReport.removeError();
        antarcticaReport.setErrorCode("location_unsupported");
        antarcticaReport.setErrorTitle("Location Specified Is Unsupported");

        return antarcticaReport;
    }
}
