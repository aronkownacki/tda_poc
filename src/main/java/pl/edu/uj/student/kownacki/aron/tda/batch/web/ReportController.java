package pl.edu.uj.student.kownacki.aron.tda.batch.web;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.service.ReportService;

/**
 * Created by Aron Kownacki on 16.08.2017.
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @RequestMapping(value = "/{country}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<List<Double>> getCountryReport(@PathVariable String country) {
        return reportService.getMonthlyReport(Country.valueOf(country.toUpperCase()));
    }

    @RequestMapping(value = "/metadata/countries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Country> getSupportedCoutries() {
        return Arrays.asList(Country.values());
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateReport(@RequestBody Map<Country, Long> updateMap) {
        reportService.update(updateMap);
    }
}
