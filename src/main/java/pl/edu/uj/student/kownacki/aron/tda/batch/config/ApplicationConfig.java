package pl.edu.uj.student.kownacki.aron.tda.batch.config;

import java.util.concurrent.Executor;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
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
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.conf.ConfigurationBuilder;

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
    public TwitterTask twitterTask(JavaStreamingContext javaStreamingContext, Authorization authorization) throws Exception {
        TwitterTask twitterTask = new TwitterTask(javaStreamingContext, authorization);
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
    public SparkSession sparkSession(JavaSparkContext javaSparkContext) {

        return new SparkSession(javaSparkContext.sc());
    }

    @Bean
    public JavaStreamingContext javaStreamingContext() {
        SparkConf conf = new SparkConf().setAppName("tda_poc")
            .setMaster("local[*]");

        conf.set("spark.cores.max", "4");
        conf.set("spark.executor.instances", "2");
        conf.set("spark.dynamicAllocation.enabled", "false");

        conf.set("spark.mongodb.input.uri", "mongodb://127.0.0.1/test.tweets");
        conf.set("spark.mongodb.output.uri", "mongodb://127.0.0.1/test.tweets");

        return new JavaStreamingContext(conf, Duration.apply(10000));
    }

    @Bean
    public twitter4j.conf.Configuration configuration() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .setOAuthConsumerKey("kH4RSvKyqz9mH9P3RDf1xyBwr")
                .setOAuthConsumerSecret("EYa00u0wpdLY9Z21a5yd2jX1BQMALPFmF1GgjQA7eDZQyiosjR")
                .setOAuthAccessToken("871418083407265792-IqNyZeV72b692P81LRWdoFzHJsxIDk2")
                .setOAuthAccessTokenSecret("MAlFDhMfoPwTINB1gZ4a95Dc8CsBpkPKYnjSmLZpUIxQZ")
                .setTweetModeExtended(true);

        return configurationBuilder.build();
    }

    @Bean
    public Authorization authorization(twitter4j.conf.Configuration configuration) {
        return AuthorizationFactory.getInstance(configuration);
    }

    @Bean
    public Twitter twitterApi(twitter4j.conf.Configuration configuration) {
        return new TwitterFactory(configuration).getInstance();
    }

}
