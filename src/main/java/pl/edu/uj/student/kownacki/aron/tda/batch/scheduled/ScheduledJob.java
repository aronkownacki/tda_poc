package pl.edu.uj.student.kownacki.aron.tda.batch.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Aron Kownacki on 23.07.2017.
 */
@Component
public class ScheduledJob {

    @Autowired
    private SimpMessagingTemplate template;

    @Scheduled(fixedDelay=1000)
    public void run() {
//        template.convertAndSend("/stream/output", new StreamOutput("dupa: "));
    }
}
