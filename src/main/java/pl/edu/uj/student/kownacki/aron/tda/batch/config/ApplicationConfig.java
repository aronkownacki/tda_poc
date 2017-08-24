package pl.edu.uj.student.kownacki.aron.tda.batch.config;

import java.util.concurrent.Executor;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import pl.edu.uj.student.kownacki.aron.tda.batch.spark.task.TwitterTask;

/**
 * Created by Aron Kownacki on 20.06.2017.
 */
@EnableAsync
@EnableScheduling
@EnableJpaRepositories(basePackages = "pl.edu.uj.student.kownacki.aron.tda.batch.dao")
@EnableMongoRepositories(basePackages = "pl.edu.uj.student.kownacki.aron.tda.batch.mongo")
@Configuration
public class ApplicationConfig {

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("asyncExecutor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public TwitterTask twitterTask(JavaStreamingContext javaStreamingContext) throws Exception {
        TwitterTask twitterTask = new TwitterTask(javaStreamingContext);
        twitterTask.start();
        return twitterTask;
    }

    @Bean
    public JavaSparkContext javaSparkContext(JavaStreamingContext javaStreamingContext) {
        JavaSparkContext sc = javaStreamingContext.sparkContext();
        sc.setLogLevel("ERROR");
        return sc;
    }

    @Bean
    public JavaStreamingContext javaStreamingContext() {
        SparkConf conf = new SparkConf().setAppName("tda_poc")
            .setMaster("local[*]");

        conf.set("spark.cores.max", "4");
        conf.set("spark.executor.instances", "2");
        conf.set("spark.dynamicAllocation.enabled", "false");

        return new JavaStreamingContext(conf, Duration.apply(10000));
    }
}
