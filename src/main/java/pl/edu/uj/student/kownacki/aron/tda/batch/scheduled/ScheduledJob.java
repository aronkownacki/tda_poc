package pl.edu.uj.student.kownacki.aron.tda.batch.scheduled;

import static pl.edu.uj.student.kownacki.aron.tda.batch.config.Profile.DRY_RUN;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
@Profile("!" + DRY_RUN)
public class ScheduledJob {

    @Autowired
    private TwitterService twitterService;

    @Autowired
    private ReportDataService reportDataService;

    @Scheduled(cron = "0 15 * ? * *")
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
