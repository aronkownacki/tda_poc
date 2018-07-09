package pl.edu.uj.student.kownacki.aron.tda.batch.spark.task;

import static pl.edu.uj.student.kownacki.aron.tda.batch.utils.TwitterHelper.extractCountries;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;
import org.bson.Document;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.mongodb.spark.MongoSpark;
import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;
import pl.edu.uj.student.kownacki.aron.tda.batch.utils.TwitterHelper;
import twitter4j.auth.Authorization;

/**
 * Created by Aron Kownacki on 05.06.2017.
 */
@Slf4j
public class TwitterTask implements Serializable {

    private JavaStreamingContext sc;

    private Thread thread;

    public TwitterTask(JavaStreamingContext sc, Authorization twitterAuth) throws InterruptedException {
        this.sc = sc;
        processStream(twitterAuth);
    }

    private void processStream(Authorization twitterAuth) {
        RestTemplate restTemplate = new RestTemplate();
        String[] filter = Country.getAllHashtags();
        TwitterUtils.createStream(sc, twitterAuth, filter).foreachRDD(rdd -> {
            if (!rdd.isEmpty()) {

                long totalCount = rdd.filter(TwitterHelper::acceptedStatus).count();

                Map<Country, Long> result = rdd.filter(TwitterHelper::acceptedStatus).flatMap(s -> extractCountries(s).iterator()).collect().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                result.put(Country.EU, result.getOrDefault(Country.EU, 0L) + totalCount);

                restTemplate.postForEntity("http://localhost:10400/report/update", result, Map.class);

                //todo should be processed once with Map<Country, Long> result extraction
                JavaRDD<Document> tweetsImplic = rdd.filter(TwitterHelper::acceptedStatus)
                        .map(status -> Tweet.builder().countries(extractCountries(status)).favoriteCountLambda(0).favoriteCount(status.isFavorited() || status.isRetweeted() ? status.getFavoriteCount() : 0).statusId(status.getId())
                                .receivedAt(status.getCreatedAt().getTime()).build()).map(tweet -> {
                            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + tweet.getCountries());
                            Document document = new Document();
                            document.put("countries", tweet.getCountries().isEmpty() ? null : new Gson().toJson(tweet.getCountries()));
                            document.put("favoriteCountLambda", tweet.getFavoriteCountLambda());//todo count fav and retweets
                            document.put("favoriteCount", tweet.getFavoriteCount());
                            document.put("statusId", tweet.getStatusId());
                            document.put("receivedAt", tweet.getReceivedAt());
                            return document;
                        });

                //                MongoSpark.save(tweetsExpl, Tweet.class);
                //                Dataset<Row> dataFrame = sqlContext.createDataFrame(rdd, Status.class);
                MongoSpark.save(tweetsImplic);
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
}
