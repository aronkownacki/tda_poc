package pl.edu.uj.student.kownacki.aron.tda.batch.config;

import static pl.edu.uj.student.kownacki.aron.tda.batch.config.Profile.DRY_RUN;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import pl.edu.uj.student.kownacki.aron.tda.batch.config.properties.TwitterAccountConfigProperties;
import pl.edu.uj.student.kownacki.aron.tda.batch.spark.task.StreamingTask;
import pl.edu.uj.student.kownacki.aron.tda.batch.spark.task.impl.TwitterStreamingTaskImpl;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Aron Kownacki on 20.06.2017.
 */
@EnableConfigurationProperties(TwitterAccountConfigProperties.class)
@Configuration
public class SparkConfig {

    @Autowired
    private TwitterAccountConfigProperties twitterAccountConfigProperties;

    @Bean
    public StreamingTask twitterTask(twitter4j.conf.Configuration configuration, JavaStreamingContext javaStreamingContext, SparkSession sparkSession) {

        Authorization authorization = AuthorizationFactory.getInstance(configuration);
        return new TwitterStreamingTaskImpl(javaStreamingContext, authorization, sparkSession);
    }

    @Bean
    @Profile("!" + DRY_RUN)
    public Object startTwitterTask(StreamingTask twitterTask) {
        twitterTask.start();
        return new Object();
    }

    @Bean
    public SparkSession sparkSession(JavaStreamingContext javaStreamingContext) {
        return new SparkSession(javaStreamingContext.sparkContext().sc());
    }

    @Bean
    public JavaStreamingContext javaStreamingContext() {
        SparkConf conf = new SparkConf().setAppName("tda_poc").setMaster("local[*]");

        conf.set("spark.cores.max", "4");
        conf.set("spark.executor.instances", "2");
        conf.set("spark.dynamicAllocation.enabled", "false");

        conf.set("spark.mongodb.input.uri", "mongodb://127.0.0.1/test.tweets");
        conf.set("spark.mongodb.output.uri", "mongodb://127.0.0.1/test.tweets");

        return  new JavaStreamingContext(conf, Duration.apply(10000));
    }

    @Bean
    public twitter4j.conf.Configuration configuration() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .setOAuthConsumerKey(twitterAccountConfigProperties.getConsumerKey())
                .setOAuthConsumerSecret(twitterAccountConfigProperties.getConsumerSecret())
                .setOAuthAccessToken(twitterAccountConfigProperties.getAccessToken())
                .setOAuthAccessTokenSecret(twitterAccountConfigProperties.getAccessTokenSecret())
                .setTweetModeExtended(true);

        return configurationBuilder.build();
    }

    @Bean
    public Twitter twitterApi(twitter4j.conf.Configuration configuration) {
        return new TwitterFactory(configuration).getInstance();
    }

}

