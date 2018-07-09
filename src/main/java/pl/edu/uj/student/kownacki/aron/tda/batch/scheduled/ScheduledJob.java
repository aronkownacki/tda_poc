package pl.edu.uj.student.kownacki.aron.tda.batch.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Granularity;
import pl.edu.uj.student.kownacki.aron.tda.batch.service.ReportDataService;
import pl.edu.uj.student.kownacki.aron.tda.batch.service.TwitterService;

/**
 * Created by Aron Kownacki on 23.07.2017.
 */
@Slf4j
@Component
public class ScheduledJob {

    @Autowired
    private TwitterService twitterService;

    @Autowired
    private ReportDataService reportDataService;

//    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void runFavoriteCounting() {
        twitterService.favoriteCounting();
    }

    @Scheduled(cron = "0 0 * ? * *")
    public void runHourlyReportAggragation() {
        reportDataService.aggregate(Granularity.HOUR);
    }

    @Scheduled(cron = "0 0 0 ? * *")
    public void runDailyReportAggragation() {
        reportDataService.aggregate(Granularity.DAY);
    }
}
