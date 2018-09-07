package pl.edu.uj.student.kownacki.aron.tda.batch.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pl.edu.uj.student.kownacki.aron.tda.batch.model.StreamOutput;
import pl.edu.uj.student.kownacki.aron.tda.batch.spark.task.TwitterTask;

/**
 * Created by Aron Kownacki on 20.06.2017.
 */
@RestController
@RequestMapping("/stream")
public class StreamController {

    @Autowired
    private TwitterTask task;

    @Autowired
    private SimpMessagingTemplate template;

    @RequestMapping("/start")
    public void start() {
        task.start();
    }

    @RequestMapping("/stop")
    public void stop() {
        task.stop();
    }

    @RequestMapping(value = "/new/{msg}", method = RequestMethod.GET)
    public void newPost(@PathVariable("msg") String newPost) {
        template.convertAndSend("/stream/output", new StreamOutput("new: " + newPost));
    }

}
