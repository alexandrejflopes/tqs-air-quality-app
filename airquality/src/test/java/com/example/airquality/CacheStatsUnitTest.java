package com.example.airquality;


import com.example.airquality.entity.CacheStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CacheStatsUnitTest {


    @Test
    public void whenAddRequest_thenNumberOfRequestsShouldIncrementByOne() {
        CacheStats cacheStats = new CacheStats();

        double before = cacheStats.getNumRequests();
        cacheStats.addRequest();
        double after = cacheStats.getNumRequests();

        assertThat(after).isEqualTo(before+1);
    }

    @Test
    public void whenAddMiss_thenNumberOfMissesShouldIncrementByOne() {
        CacheStats cacheStats = new CacheStats();

        double before = cacheStats.getMisses();
        cacheStats.addMiss();
        double after = cacheStats.getMisses();

        assertThat(after).isEqualTo(before+1);
    }

    @Test
    public void whenAddHit_thenNumberOfHitsShouldIncrementByOne() {
        CacheStats cacheStats = new CacheStats();

        double before = cacheStats.getHits();
        cacheStats.addHit();
        double after = cacheStats.getHits();

        assertThat(after).isEqualTo(before+1);
    }
}
