package pl.edu.uj.student.kownacki.aron.tda.batch.service;

import java.util.List;
import java.util.Map;

import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Granularity;

/**
 * Created by Aron Kownacki on 14.08.2017.
 */
public interface ReportDataService {

    List<List<Double>> getRandomReport(Country country);

    List<List<Double>> getFullReport(Country country, Granularity granularity);

    List<List<Double>> get24Report(Country country, Granularity granularity);

    void update(Map<Country, Long> updateMap);

    void aggregate(Granularity granularity);

}
