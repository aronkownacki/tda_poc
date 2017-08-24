package pl.edu.uj.student.kownacki.aron.tda.batch.sandbox;

import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.edu.uj.student.kownacki.aron.tda.batch.dao.ReportDataRepository;

@RestController
public class TestController {

    @Autowired
    private JavaSparkContext sc;

    @Autowired
    private ReportDataRepository reportDataRepository;

    @RequestMapping("/testSparkContext")
    public String testSparkContext() {
        return "result count is: " + sc.textFile("c:/tmp/tds_poc/tweets/*/*").count();
    }

    @RequestMapping("/test")
    public String test() {
        return "data count: " + reportDataRepository.count();
    }
}