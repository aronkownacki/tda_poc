package pl.edu.uj.student.kownacki.aron.tda.batch;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;


/**
 * Created by Aron Kownacki on 05.06.2017.
 */
@Slf4j
@Component
public class TwitterTask implements Serializable {

    private JavaStreamingContext sc;
    private Thread thread;


    private Consumer<String> printToTempplate = (status) -> {
        new RestTemplate().postForEntity("http://localhost:10400/new", status, String.class);
    };

    public TwitterTask() throws InterruptedException {
//        SparkContext sc = new SparkContext(config);
//        sc.setLogLevel("WARN");

        System.setProperty("twitter4j.oauth.consumerKey", "kH4RSvKyqz9mH9P3RDf1xyBwr");
        System.setProperty("twitter4j.oauth.consumerSecret", "EYa00u0wpdLY9Z21a5yd2jX1BQMALPFmF1GgjQA7eDZQyiosjR");
        System.setProperty("twitter4j.oauth.accessToken", "871418083407265792-IqNyZeV72b692P81LRWdoFzHJsxIDk2");
        System.setProperty("twitter4j.oauth.accessTokenSecret", "MAlFDhMfoPwTINB1gZ4a95Dc8CsBpkPKYnjSmLZpUIxQZ");

        SparkConf config = new SparkConf().setAppName("tda_poc")
            .setMaster("local[*]");
//                .setMaster("spark://localhost:7077");

        config.set("spark.cores.max", "4");
        config.set("spark.dynamicAllocation.enabled", "false");

        String jarFile = "E:\\uj\\mgr\\repo\\tda_poc\\target\\tda-poc-1.0-SNAPSHOT.jar";
        sc = new JavaStreamingContext(config, Duration.apply(1000));
//        sc = new JavaStreamingContext("spark://localhost:7077", "Tutorial", new Duration(1000), "/root/spark", new String[]{jarFile});

        Configuration twitterConf = ConfigurationContext.getInstance();
        Authorization twitterAuth = AuthorizationFactory.getInstance(twitterConf);

        FlatMapFunction<Status, HashtagEntity> fun = (FlatMapFunction<Status, HashtagEntity>) status -> Arrays.asList(status.getHashtagEntities()).iterator();
        String[] filters = {"#Trump"};


        TwitterUtils.createStream(sc, twitterAuth, filters)
//                .map(s -> {
//                    log.info(s.getUser().toString());
//                    print(s.getUser().toString());
//                    return s;
//                })
//            .map(print)
            .flatMap(fun)
            .map(h -> h.getText().toLowerCase()).filter( (s1) -> s1 != null).foreachRDD(
            rdd -> {
                if(!rdd.isEmpty()) {
                    String s = rdd.map(h -> "#" + h + " ").reduce(String::concat);
                    printToTempplate.accept(s);
                }
            });
//                .flatMap(s -> Arrays.asList(s.getHashtagEntities()))
//                .map(h -> h.getText().toLowerCase()).filter(h -> !h.equals("android")).countByValue().print();

//        String checkpointDir = TutorialHelper.getHdfsUrl() + "/checkpoint/";
//        sc.checkpoint(checkpointDir);

//        startAsync();
    }

    public void start() {
        Runnable task2 = () -> {
            sc.start();
            try {
                sc.awaitTermination();
            } catch (InterruptedException e) {
            }
        };
        thread = new Thread(task2);
        thread.start();
    }

    public void stop() {
        sc.stop();
        thread.interrupt();
    }

    /*
    val config = new SparkConf().setAppName("twitter-stream-sentiment")
    val sc = new SparkContext(config) sc.setLogLevel("WARN")
    val ssc = new StreamingContext(sc, Seconds(5))
    System.setProperty("twitter4j.oauth.consumerKey", "consumerKey")
     System.setProperty("twitter4j.oauth.consumerSecret", "consumerSecret")
     System.setProperty("twitter4j.oauth.accessToken", accessToken)
      System.setProperty("twitter4j.oauth.accessTokenSecret", "accessTokenSecret")
       val stream = TwitterUtils.createStream(ssc, None)
     */


//    @Scheduled(fixedRate = 1000L)
//    public void execute() {
//
//        log.info("execution started"
//        );
//    }

}
