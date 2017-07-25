package pl.edu.uj.student.kownacki.aron.tda.batch;

import org.springframework.context.annotation.Configuration;

/**
 * Created by Aron Kownacki on 20.06.2017.
 */
@Configuration
public class MainConfig {

//    @Bean
    public TwitterTask twitterTask() throws Exception {
        return new TwitterTask();
    }
}
