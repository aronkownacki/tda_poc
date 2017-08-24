package pl.edu.uj.student.kownacki.aron.tda.batch.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Aron Kownacki on 22.08.2017.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "report_data")
public class ReportData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "country_name", nullable = false)
    private Country countryName;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "count", nullable = false)
    private Long count;
}
