package pl.edu.uj.student.kownacki.aron.tda.batch.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.ReportData;

/**
 * Created by Aron Kownacki on 22.08.2017.
 */
@Repository
public interface ReportDataRepository extends CrudRepository<ReportData, Long> {

    List<ReportData> findByCountryName(Country countryName);
}
