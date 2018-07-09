package pl.edu.uj.student.kownacki.aron.tda.batch.utils;

import static org.junit.Assert.assertEquals;

import java.time.temporal.ChronoUnit;

import org.junit.Test;

public class DateTimeUtilsTest {
    @Test
    public void getTimestampAtStartOfUnitTest() {
        assertEquals(1514775600000L, DateTimeUtils.getTimestampAtStartOfUnit(1514775845L * 1000, ChronoUnit.HOURS));
        assertEquals(1514761200000L, DateTimeUtils.getTimestampAtStartOfUnit(1514775845L * 1000, ChronoUnit.DAYS));
    }
}