package pl.edu.uj.student.kownacki.aron.tda.batch;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import pl.edu.uj.student.kownacki.aron.tda.batch.web.Greeting;
import pl.edu.uj.student.kownacki.aron.tda.batch.web.HelloMessage;

/**
 * Created by Aron Kownacki on 10.07.2017.
 */
@Controller
public class GreetingController {

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + message.getName() + "!");
    }

}
