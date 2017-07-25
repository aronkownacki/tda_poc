package pl.edu.uj.student.kownacki.aron.tda.batch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pl.edu.uj.student.kownacki.aron.tda.batch.web.Greeting;

/**
 * Created by Aron Kownacki on 20.06.2017.
 */
@RestController
public class HelloController {

    @Autowired
    private SimpMessagingTemplate template;



    @Autowired
    private TwitterTask task;

    @RequestMapping("/start")
    public void start() {
        task.start();
    }

    @RequestMapping("/stop")
    public void stop() {
        task.stop();
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public void newPost(@RequestBody String newPost) {
        template.convertAndSend("/topic/greetings", new Greeting("new: " + newPost));
    }

}
