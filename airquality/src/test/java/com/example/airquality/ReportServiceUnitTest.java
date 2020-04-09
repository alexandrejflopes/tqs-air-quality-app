package com.example.airquality;

import com.example.airquality.client.ReportHttpClient;
import com.example.airquality.entity.*;
import com.example.airquality.repository.ReportRepository;
import com.example.airquality.service.ReportService;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ReportServiceUnitTest {

    @Mock( lenient = true)
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    public void setUp() {
        reportService.setHttpClient(new ReportHttpClient());

        Report aveiroReport = setUpAveiroReport();
        //Location aveiro = aveiroReport.getLocation();

        Report antarcticaReport = setUpAntarcticaReport();
        //Location antarctica = antarcticaReport.getLocation();

        // non requested location yet
        Location castanheiraDePera = new Location(new Coordinates(40.00405, -8.202775),"PT","Castanheira de Pera e Coentral, Castanheira de Pera");

        Mockito.when(reportRepository.existsById(aveiroReport.getLocation())).thenReturn(true);
        Mockito.when(reportRepository.findById(aveiroReport.getLocation())).thenReturn(Optional.of(aveiroReport));
        Mockito.when(reportRepository.findById(antarcticaReport.getLocation())).thenReturn(Optional.of(antarcticaReport));
        Mockito.when(reportRepository.findById(castanheiraDePera)).thenReturn(Optional.empty());
    }

    @Test
    public void whenPreviouslyRequestedLocation_thenReportShouldBeFound() {
        Location aveiro = new Location(new Coordinates(40.640496, -8.653784), "PT","Aveiro, Aveiro");
        Report fromDB = reportService.getReportByLocation(aveiro);
        verifyFindByIdIsCalledOnce();
        assertThat(fromDB.getLocation()).isEqualTo(aveiro);
    }

    @Test
    public void whenNeverRequestedLocation_thenReportShouldNotBeFound() {
        Location castanheiraDePera = new Location(new Coordinates(40.00405, -8.202775),"PT","Castanheira de Pera e Coentral, Castanheira de Pera");
        Report fromDB = reportService.getReportByLocation(castanheiraDePera);
        verifyFindByIdIsCalledOnce();
        assertThat(fromDB).isNull();
    }

    @Test
    public void whenPreviouslyRequestedLocation_thenReportShouldExist() {
        Location aveiro = new Location(new Coordinates(40.640496, -8.653784), "PT","Aveiro, Aveiro");
        boolean reportExists = reportService.existsReportWithLocation(aveiro);
        assertThat(reportExists).isEqualTo(true);

        verifyExistsByIdIsCalledOnce();

    }

    @Test
    public void whenNeverRequestedLocation_thenReportShouldNotExist() {
        Location castanheiraDePera = new Location(new Coordinates(40.00405, -8.202775),"PT","Castanheira de Pera e Coentral, Castanheira de Pera");
        boolean reportExists = reportService.existsReportWithLocation(castanheiraDePera);
        assertThat(reportExists).isEqualTo(false);

        verifyExistsByIdIsCalledOnce();
    }

    @Test
    public void whenValidInput_thenLocationShouldBeFetched() throws ParseException, IOException, URISyntaxException {
        String location_input = "Aveiro";
        Location fetched = reportService.requestLocationDataForInput(location_input);

        assertThat(fetched.getCoordinates().getLatitude()).isEqualTo(40.640496);
        assertThat(fetched.getCoordinates().getLongitude()).isEqualTo(-8.653784);
        assertThat(fetched.getCountryCode()).isEqualTo("PT");
        assertThat(fetched.getAddress().contains("Aveiro")).isEqualTo(true);

    }

    @Test
    public void whenInvalidInput_thenLocationShouldNotBeFetched() throws ParseException, IOException, URISyntaxException {
        String location_input = " ";
        Location fetched = reportService.requestLocationDataForInput(location_input);

        assertThat(fetched).isNull();
    }

    @Test
    public void whenValidInput_thenReportShouldBeFetched() throws ParseException, IOException, URISyntaxException {
        String location_input = "Aveiro";
        Report fetched = reportService.getReportForInput(location_input);

        assertThat(fetched).isNotNull();
        verifyFindByIdIsCalledZeroOrMoreTimes();

    }

    @Test
    public void whenInvalidInput_thenReportShouldNotBeFetched() throws ParseException, IOException, URISyntaxException {
        String location_input = " ";
        Report fetched = reportService.getReportForInput(location_input);

        assertThat(fetched).isNull();
    }

    @Test
    public void whenValidLocation_thenReportShouldBeFetched() throws ParseException, IOException, URISyntaxException {
        Location antarctica = new Location(new Coordinates(-82.108182, 34.37824), "AQ",", ");
        Report fetched = reportService.requestNewReportForLocation(antarctica);

        assertThat(fetched).isNotNull();
    }

    @Test
    public void whenInValidLocation_thenReportShouldBeFetched() throws ParseException, IOException, URISyntaxException {
        Location emptyLocation = new Location();
        Report fetched = reportService.requestNewReportForLocation(emptyLocation);

        assertThat(fetched).isNull();
    }


    private void verifyFindByIdIsCalledOnce() {
        Mockito.verify(reportRepository, VerificationModeFactory.times(1)).findById(Mockito.any(Location.class));
        Mockito.reset(reportRepository);
    }

    private void verifyFindByIdIsCalledZeroOrMoreTimes() {
        Mockito.verify(reportRepository, VerificationModeFactory.atMost(1)).findById(Mockito.any(Location.class));
        Mockito.reset(reportRepository);
    }

    private void verifyExistsByIdIsCalledOnce() {
        Mockito.verify(reportRepository, VerificationModeFactory.times(1)).existsById(Mockito.any(Location.class));
        Mockito.reset(reportRepository);
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
