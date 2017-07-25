package pl.edu.uj.student.kownacki.aron.tda.batch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import pl.edu.uj.student.kownacki.aron.tda.batch.web.Greeting;

/**
 * Created by Aron Kownacki on 23.07.2017.
 */
@Component
public class ScheduledUpdatesOnTopic{

    private static int cntr = 1;

    @Autowired
    private SimpMessagingTemplate template;

//    @Scheduled(fixedDelay=300)
    public void publishUpdates(){
        template.convertAndSend("/topic/greetings", new Greeting("dupa_jasiu_" + cntr++));
    }
}
