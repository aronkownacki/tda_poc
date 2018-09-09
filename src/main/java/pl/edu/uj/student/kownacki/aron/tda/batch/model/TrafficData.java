package pl.edu.uj.student.kownacki.aron.tda.batch.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TrafficData implements Serializable {
    long count;
    double avg;
    int max;
}
