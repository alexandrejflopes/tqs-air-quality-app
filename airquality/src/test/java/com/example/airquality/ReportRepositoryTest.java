package com.example.airquality;

import com.example.airquality.entity.*;
import com.example.airquality.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ReportRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReportRepository reportRepository;

    @Test
    public void whenFindByPreviouslyRequestedLocation_thenReturnReport() {
        Report aveiroReport = setUpAveiroReport();
        entityManager.persistAndFlush(aveiroReport); // after being "requested", is saved

        Report found = reportRepository.findById(aveiroReport.getLocation()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getLocation()).isEqualTo(aveiroReport.getLocation());
    }

    @Test
    public void whenFindByNeverRequestedLocation_thenReturnNull() {
        Location castanheiraDePera = new Location(new Coordinates(40.00405, -8.202775),"PT","Castanheira de Pera e Coentral, Castanheira de Pera");
        Report result = reportRepository.findById(castanheiraDePera).orElse(null);
        assertThat(result).isNull();
    }

    @Test
    public void whenCheckExistenceWithPreviouslyRequestedLocation_thenReportShouldExist() {
        Report antarcticaReport = setUpAntarcticaReport();
        entityManager.persistAndFlush(antarcticaReport);

        boolean reportExists = reportRepository.existsById(antarcticaReport.getLocation());
        assertThat(reportExists).isEqualTo(true);
    }

    @Test
    public void whenCheckExistenceWithNeverRequestedLocation_thenReportShouldNotExist() {
        Location castanheiraDePera = new Location(new Coordinates(40.00405, -8.202775),"PT","Castanheira de Pera e Coentral, Castanheira de Pera");

        boolean reportExists = reportRepository.existsById(castanheiraDePera);
        assertThat(reportExists).isEqualTo(false);
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
