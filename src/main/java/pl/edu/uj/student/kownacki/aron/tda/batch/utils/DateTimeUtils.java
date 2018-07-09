package pl.edu.uj.student.kownacki.aron.tda.batch.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;

public class DateTimeUtils {

    private static final long MILLISESONDS_IN_SECOND = 1000L;

    public static long getTimestampAtStartOfUnit(long timestampInMillis, TemporalUnit unit){
        LocalDateTime localDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestampInMillis), ZoneId.systemDefault()).toLocalDateTime();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return zonedDateTime.truncatedTo(unit).toEpochSecond() * MILLISESONDS_IN_SECOND;
    }

    public static long getNowInMillis() {
        return ZonedDateTime.now().toEpochSecond() * MILLISESONDS_IN_SECOND;
    }
}
