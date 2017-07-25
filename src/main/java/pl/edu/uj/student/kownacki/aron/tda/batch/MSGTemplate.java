package pl.edu.uj.student.kownacki.aron.tda.batch;

import java.io.Serializable;

import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Created by Aron Kownacki on 24.07.2017.
 */

public class MSGTemplate implements Serializable {

    public MSGTemplate(Object template) {
        this.template = template;
    }

    private Object template;

    public void convertAndSend(String s) {
        ((SimpMessagingTemplate) template).convertAndSend("/topic/greetings", s);
        System.out.print("-----------------------------------> msg sent");
    }
}
