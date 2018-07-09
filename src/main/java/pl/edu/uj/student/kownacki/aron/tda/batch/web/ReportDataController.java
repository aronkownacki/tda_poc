package pl.edu.uj.student.kownacki.aron.tda.batch.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Sets;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Granularity;
import pl.edu.uj.student.kownacki.aron.tda.batch.service.ReportDataService;

/**
 * Created by Aron Kownacki on 16.08.2017.
 */
@RestController
@RequestMapping("/report")
public class ReportDataController {

    @Autowired
    private ReportDataService reportService;

    @RequestMapping(value = "/{country}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<List<Double>> getHourlyReport(@PathVariable String country) {
        return reportService.getReport(Country.valueOf(country.toUpperCase()), Granularity.HOUR);
    }

    @RequestMapping(value = "/metadata/countries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Country> getSupportedCoutries() {
        Set<Country> countries = Sets.newHashSet(Country.values());
        countries.remove(Country.EU);
        return new ArrayList<>(countries);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateReport(@RequestBody Map<Country, Long> updateMap) {
        reportService.update(updateMap);
    }
}
