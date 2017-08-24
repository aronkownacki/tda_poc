package pl.edu.uj.student.kownacki.aron.tda.batch.service;

import java.util.List;
import java.util.Map;

import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;

/**
 * Created by Aron Kownacki on 14.08.2017.
 */
public interface ReportService {

    List<List<Double>> getRandomReport(Country country);

    List<List<Double>> getMonthlyReport(Country country);

    void update(Map<Country, Long> updateMap);

}
