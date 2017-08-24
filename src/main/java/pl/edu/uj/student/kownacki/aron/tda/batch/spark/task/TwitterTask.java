package pl.edu.uj.student.kownacki.aron.tda.batch.spark.task;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
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
public class TwitterTask implements Serializable {

    private JavaStreamingContext sc;

    private Thread thread;

    private RestTemplate restTemplate = new RestTemplate();

//    private Consumer<String> postStatus = (status) -> {
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        restTemplate.postForEntity("http://localhost:10400/stream/new", status, String.class);
//    };

    public TwitterTask(JavaStreamingContext sc) throws InterruptedException {

        System.setProperty("twitter4j.oauth.consumerKey", "kH4RSvKyqz9mH9P3RDf1xyBwr");
        System.setProperty("twitter4j.oauth.consumerSecret", "EYa00u0wpdLY9Z21a5yd2jX1BQMALPFmF1GgjQA7eDZQyiosjR");
        System.setProperty("twitter4j.oauth.accessToken", "871418083407265792-IqNyZeV72b692P81LRWdoFzHJsxIDk2");
        System.setProperty("twitter4j.oauth.accessTokenSecret", "MAlFDhMfoPwTINB1gZ4a95Dc8CsBpkPKYnjSmLZpUIxQZ");


        Configuration twitterConf = ConfigurationContext.getInstance();
        Authorization twitterAuth = AuthorizationFactory.getInstance(twitterConf);

        this.sc = sc;
        processStream(twitterAuth);
    }

    private void hashtagStream(Authorization twitterAuth) {
        FlatMapFunction<Status, HashtagEntity> toHashtag = (FlatMapFunction<Status, HashtagEntity>) status -> Arrays.asList(status.getHashtagEntities()).iterator();

        TwitterUtils.createStream(sc, twitterAuth)
            .flatMap(toHashtag)
            .map(hashtag -> hashtag.getText().toLowerCase()).filter((hashtagString) -> hashtagString != null).foreachRDD(
            rdd -> {
                if (!rdd.isEmpty()) {
                    String hashtags = rdd.map(hashtag -> "#" + hashtag + " ").reduce(String::concat);
                    Thread.sleep(500);
//                    postStatus.accept(hashtags);
                }
            });
    }

    private void polishStatusStream(Authorization twitterAuth) {
//        String[] filter = {"trump"};
        String[] filter = {"polexit", "brexit", "plexit"};
        TwitterUtils.createStream(sc, twitterAuth, filter)
            .map(Status::getText).foreachRDD(rdd -> {
            if (!rdd.isEmpty()) {
                List<String> statuses = rdd.collect();
//                statuses.forEach(postStatus);
            }
        });
    }

    private void saveStream(Authorization twitterAuth) {

        String[] filter = Country.getAllHashtags();
        TwitterUtils.createStream(sc, twitterAuth, filter)
            .map(Status::getText).filter(StringUtils::isNoneBlank).dstream().saveAsTextFiles("c:/tmp/tds_poc/tweets/", "json");
    }

    //todo add statuses persistance
    private void processStream(Authorization twitterAuth) {
        RestTemplate restTemplate = new RestTemplate();
        String[] filter = Country.getAllHashtags();
        TwitterUtils.createStream(sc, twitterAuth, filter).foreachRDD(rdd -> {
            if (!rdd.isEmpty()) {

                long totalCount = rdd.count();
                Map<Country, Long> result = rdd.flatMap(s -> extractCountries(s).iterator()).collect().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                result.put(Country.EU, result.getOrDefault(Country.EU, 0L) + totalCount);

                restTemplate.postForEntity("http://localhost:10400/report/update", result, Map.class);
            }
        });
    }

    public void start() {
        thread = new Thread(() -> {
            sc.start();
            try {
                sc.awaitTermination();
            } catch (InterruptedException e) {
            }
        });
        thread.start();
    }

    public void stop() {
        sc.stop();
        thread.interrupt();
    }

    private static Set<Country> extractCountries(Status status) {
        String statusHashtags = Arrays.stream(status.getHashtagEntities()).map(HashtagEntity::getText).collect(Collectors.joining(","));
        return Arrays.stream(Country.values()).filter(
            country -> country.getHashtags().stream().anyMatch(countryHashtag ->
                statusHashtags.contains(countryHashtag) || status.getText().contains(countryHashtag))).collect(toSet());

    }
}
