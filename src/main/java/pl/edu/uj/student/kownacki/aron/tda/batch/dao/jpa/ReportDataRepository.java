package pl.edu.uj.student.kownacki.aron.tda.batch.dao.jpa;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Granularity;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.ReportData;

/**
 * Created by Aron Kownacki on 22.08.2017.
 */
@Repository
public interface ReportDataRepository extends CrudRepository<ReportData, Long> {

    @Query("select rd from ReportData rd where rd.countryName = :countryName and rd.granularity = :granularity and rd.timestamp >= :beginTimestamp")
    List<ReportData> findByCountryNameAndGranularity(@Param("countryName") Country countryName, @Param("granularity") Granularity granularity, @Param("beginTimestamp") Long beginTimestamp);

    List<ReportData> findByCountryNameAndGranularity(Country countryName, Granularity granularity);

    Page<ReportData> findByCountryNameAndGranularity(Country countryName, Granularity granularity, Pageable pageable);

    @Query("select rd from ReportData rd where rd.countryName = :countryName and rd.granularity = :granularity and rd.timestamp between :beginTimestamp and :endTimestamp")
    Page<ReportData> findByCountryNameAndGranularity(@Param("countryName") Country countryName, @Param("granularity") Granularity granularity, @Param("beginTimestamp") Long beginTimestamp, @Param("endTimestamp") Long endTimestamp, Pageable pageable);
}
