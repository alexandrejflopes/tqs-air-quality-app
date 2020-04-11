package com.example.airquality;

import com.example.airquality.client.ReportHttpClient;
import com.example.airquality.entity.*;
import com.example.airquality.exception.InvalidLocationException;
import com.example.airquality.repository.ReportRepository;
import com.example.airquality.service.ReportService;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ReportServiceUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock(lenient = true)
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    public void setUp() {
        reportService.setHttpClient(new ReportHttpClient());
        Location aveiro = new Location(new Coordinates(40.640496, -8.653784), "PT","Aveiro, Aveiro");

        Report aveiroReport = setUpAveiroReport();
        Report cachedAveiroReport = setUpCachedReportFrom(aveiroReport);


        Report antarcticaReport = setUpAntarcticaReport();

        // non requested location yet
        Location castanheiraDePera = new Location(new Coordinates(40.00405, -8.202775),"PT","Castanheira de Pera e Coentral, Castanheira de Pera");

        Mockito.when(reportRepository.existsById(aveiro)).thenReturn(false).thenReturn(true);
        Mockito.when(reportRepository.save(aveiroReport)).thenReturn(cachedAveiroReport);
        Mockito.when(reportRepository.findById(aveiro)).thenReturn(Optional.of(cachedAveiroReport));
        Mockito.when(reportRepository.findById(antarcticaReport.getLocation())).thenReturn(Optional.of(antarcticaReport));
        Mockito.when(reportRepository.findById(castanheiraDePera)).thenReturn(Optional.empty());
    }



    @Test
    public void whenPreviouslyRequestedLocation_thenReportShouldBeFound() throws ParseException, IOException, URISyntaxException, InvalidLocationException {
        // perform a request
        Report aveiro_report = reportService.getReportForInput("Aveiro");

        Report fromDB = reportService.getReportByLocation(aveiro_report.getLocation());
        verifyFindByIdIsCalledOnce();
        assertThat(fromDB).isNotNull();
        assertThat(fromDB.getLocation()).isEqualTo(aveiro_report.getLocation());
    }

    @Test
    public void whenNeverRequestedLocation_thenReportShouldNotBeFound() {
        Location castanheiraDePera = new Location(new Coordinates(40.00405, -8.202775),"PT","Castanheira de Pera e Coentral, Castanheira de Pera");
        Report fromDB = reportService.getReportByLocation(castanheiraDePera);
        verifyFindByIdIsCalledOnce();
        assertThat(fromDB).isNull();
    }

    @Test
    public void whenPreviouslyRequestedLocation_thenReportShouldExist() throws ParseException, IOException, URISyntaxException, InvalidLocationException {
        // perform a request
        Report aveiro_report = reportService.getReportForInput("Aveiro");

        boolean reportExists = reportService.existsReportWithLocation(aveiro_report.getLocation());
        assertThat(reportExists).isEqualTo(true);

        verifyExistsByIdIsCalledTwice(); // one in the request, another for checking its existence

    }

    @Test
    public void whenNeverRequestedLocation_thenReportShouldNotExist() {
        Location castanheiraDePera = new Location(new Coordinates(40.00405, -8.202775),"PT","Castanheira de Pera e Coentral, Castanheira de Pera");
        boolean reportExists = reportService.existsReportWithLocation(castanheiraDePera);
        assertThat(reportExists).isEqualTo(false);

        verifyExistsByIdIsCalledOnce();
    }

    @Test
    public void whenValidInput_thenLocationShouldBeFetched() throws IOException, URISyntaxException {
        String location_input = "Aveiro";
        Location fetched = reportService.requestLocationDataForInput(location_input);

        assertThat(fetched.getCoordinates().getLatitude()).isEqualTo(40.640496);
        assertThat(fetched.getCoordinates().getLongitude()).isEqualTo(-8.653784);
        assertThat(fetched.getCountryCode()).isEqualTo("PT");
        assertThat(fetched.getAddress().contains("Aveiro")).isEqualTo(true);

    }

    @Test
    public void whenInvalidInput_thenLocationShouldNotBeFetched() throws IOException, URISyntaxException {
        String location_input = " ";
        Location fetched = reportService.requestLocationDataForInput(location_input);

        assertThat(fetched).isNull();
    }

    @Test
    public void whenValidInput_thenReportShouldBeFetched() throws ParseException, IOException, URISyntaxException, InvalidLocationException {
        String location_input = "Aveiro";
        Report fetched = reportService.getReportForInput(location_input);

        assertThat(fetched).isNotNull();
        verifyFindByIdIsCalledZeroOrMoreTimes();

    }

    @Test
    public void whenInvalidInput_thenInvalidLocationExceptionShouldBeThrown() {
        String location_input = " ";
        assertThrows(InvalidLocationException.class, () -> reportService.getReportForInput(location_input));
    }

    @Test
    public void whenValidLocation_thenReportShouldBeFetched() throws ParseException, IOException, URISyntaxException, InvalidLocationException {
        Location antarctica = new Location(new Coordinates(-82.108182, 34.37824), "AQ",", ");
        Report fetched = reportService.requestNewReportForLocation(antarctica);

        assertThat(fetched).isNotNull();
    }

    @Test
    public void whenInValidLocation_thenInvalidLocationExceptionShouldBeThrown() {
        Location emptyLocation = new Location();
        assertThrows(InvalidLocationException.class, () -> reportService.requestNewReportForLocation(emptyLocation));
    }


    // cache statistics tests

    @Test
    public void whenNewRequest_thenNumberOfRequestsShouldIncrementByOne() throws ParseException, IOException, URISyntaxException, InvalidLocationException {
        String location_input = "Aveiro";
        Location aveiro = new Location(new Coordinates(40.640496, -8.653784), "PT","Aveiro, Aveiro");

        // number of requests before the next request
        int numGlobalRequestsBefore = reportService.getGlobalCacheStats().getNumRequests();
        int numLocalRequestsBefore = reportService.getLocationCacheStats(aveiro).getNumRequests();

        // perform one request
        Report fromDB1 = reportService.getReportForInput(location_input);
        assertThat(fromDB1.getLocation()).isEqualTo(aveiro);

        int numGlobalRequestsAfter = reportService.getGlobalCacheStats().getNumRequests();
        int numLocalRequestsAfter = reportService.getLocationCacheStats(fromDB1.getLocation()).getNumRequests();
        verifyExistsByIdIsCalledOnce(); // to check if the report is cached

        assertThat(numGlobalRequestsAfter).isEqualTo(numGlobalRequestsBefore+1);
        assertThat(numLocalRequestsAfter).isEqualTo(numLocalRequestsBefore+1);
    }

    @Test
    public void whenNeverRequestedLocation_thenNumberOfMissesShouldIncrementByOne() throws ParseException, IOException, URISyntaxException, InvalidLocationException {
        String location_input = "Aveiro";
        Location aveiro = new Location(new Coordinates(40.640496, -8.653784), "PT","Aveiro, Aveiro");

        // number of misses before the request
        int numGlobalMissesBefore = reportService.getGlobalCacheStats().getMisses();
        int numLocalMissesBefore = reportService.getLocationCacheStats(aveiro).getMisses();

        // perform one request
        Report fromDB1 = reportService.getReportForInput(location_input);
        assertThat(fromDB1.getLocation()).isEqualTo(aveiro);

        int numGlobalMissesAfter = reportService.getGlobalCacheStats().getMisses();
        int numLocalMissesAfter = reportService.getLocationCacheStats(fromDB1.getLocation()).getMisses();
        verifyExistsByIdIsCalledOnce(); // to check if the report is cached

        assertThat(numGlobalMissesAfter).isEqualTo(numGlobalMissesBefore+1);
        assertThat(numLocalMissesAfter).isEqualTo(numLocalMissesBefore+1);
    }


    @Test
    public void whenPrevouslyRequestedLocation_thenNumberOfHitsShouldIncrementByOne() throws ParseException, IOException, URISyntaxException, InvalidLocationException {
        String location_input = "Aveiro";
        Location aveiro = new Location(new Coordinates(40.640496, -8.653784), "PT","Aveiro, Aveiro");

        // number of hits before the request
        int numGlobalHitsBefore = reportService.getGlobalCacheStats().getHits();
        int numLocalHitsBefore = reportService.getLocationCacheStats(aveiro).getHits();

        // perform two requests
        Report fromDB1 = reportService.getReportForInput(location_input);
        Report fromDB2 = reportService.getReportForInput(location_input);
        assertThat(fromDB1.getLocation()).isEqualTo(fromDB2.getLocation()).isEqualTo(aveiro);

        /*
         * as timestamps change slightly between requests
         * we need to guarantee that the testing reports
         * have the same timestamps in order to have a 'fair' comparison;
         * if all went right, all the rest should be the same
         * */
        fromDB2 = updateMetrics(fromDB2,fromDB1);

        // fromDB2 should be equal to fromDB1, as fromDB2
        // should be fromDB1 retrieved from cache
        assertThat(fromDB1).isEqualTo(fromDB2);

        int numGlobalHitsAfter = reportService.getGlobalCacheStats().getHits();
        int numLocalHitsAfter = reportService.getLocationCacheStats(aveiro).getHits();
        verifyExistsByIdIsCalledTwice(); // to check if the report is cached in each request

        assertThat(numGlobalHitsAfter).isEqualTo(numGlobalHitsBefore+1);
        assertThat(numLocalHitsAfter).isEqualTo(numLocalHitsBefore+1);
    }


    private void verifyFindByIdIsCalledOnce() {
        Mockito.verify(reportRepository, VerificationModeFactory.times(1)).findById(Mockito.any(Location.class));
        Mockito.reset(reportRepository);
    }

    private void verifyFindByIdIsCalledTwice() {
        Mockito.verify(reportRepository, VerificationModeFactory.times(2)).findById(Mockito.any(Location.class));
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

    private void verifyExistsByIdIsCalledTwice() {
        Mockito.verify(reportRepository, VerificationModeFactory.times(2)).existsById(Mockito.any(Location.class));
        Mockito.reset(reportRepository);
    }

    private void verifySaveIsCalledTwice() {
        Mockito.verify(reportRepository, VerificationModeFactory.times(2)).save(Mockito.any(Report.class));
        Mockito.reset(reportRepository);
    }

    public Report setUpAveiroReport(){
        Location aveiro = new Location(new Coordinates(40.640496, -8.653784), "PT","Aveiro, Aveiro");

        Report aveiroReport = new Report();
        aveiroReport.setRequestTimeStamp((ZonedDateTime.now(ZoneId.of("UTC"))).toLocalDateTime());
        int thisMonth = aveiroReport.getRequestTimeStamp().getMonthValue();
        String monthValue = thisMonth < 10 ? "0"+thisMonth : String.valueOf(thisMonth);
        String lastUpd =    aveiroReport.getRequestTimeStamp().getYear() + "-" +
                            monthValue + "-" +
                            aveiroReport.getRequestTimeStamp().getDayOfMonth() + "T" +
                            aveiroReport.getRequestTimeStamp().getHour() + ":00";

        aveiroReport.setLastUpdatedAt(LocalDateTime.parse(lastUpd));
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

    public Report setUpCachedReportFrom(Report report){
        Report cachedReport = report;

        // set expected stats when the report for same location is requested again
        // hit: +1 ; miss: +0 ; numRequests: +1
        cachedReport.getGlobalCacheStats().addHit();
        cachedReport.getGlobalCacheStats().addRequest();
        cachedReport.getLocationCacheStats().addHit();
        cachedReport.getLocationCacheStats().addRequest();

        // granting that timestamps are equal (just for testing comparisons)
        cachedReport.setRequestTimeStamp(report.getRequestTimeStamp());

        return cachedReport;
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
}
