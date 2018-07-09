package pl.edu.uj.student.kownacki.aron.tda.batch.model;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;

/**
 * Created by Aron Kownacki on 14.08.2017.
 */
public enum Granularity implements Serializable {
    MILLISECOND(ChronoUnit.MILLIS),
    HOUR(ChronoUnit.HOURS),
    DAY(ChronoUnit.DAYS),
    ;

    private final ChronoUnit unit;

    Granularity(ChronoUnit unit) {
        this.unit = unit;
    }

    public ChronoUnit getUnit() {
        return unit;
    }
}
