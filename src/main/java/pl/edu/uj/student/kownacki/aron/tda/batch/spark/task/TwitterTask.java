package pl.edu.uj.student.kownacki.aron.tda.batch.spark.task;

import static java.lang.String.valueOf;
import static org.apache.spark.streaming.Durations.minutes;
import static org.apache.spark.streaming.Durations.seconds;
import static pl.edu.uj.student.kownacki.aron.tda.batch.utils.TwitterHelper.extractCountries;
import static pl.edu.uj.student.kownacki.aron.tda.batch.utils.TwitterHelper.getPopularity;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;
import org.bson.Document;
import org.springframework.web.client.RestTemplate;

import com.mongodb.spark.MongoSpark;
import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;
import pl.edu.uj.student.kownacki.aron.tda.batch.utils.TwitterHelper;
import scala.Tuple2;
import twitter4j.Status;
import twitter4j.auth.Authorization;

/**
 * Created by Aron Kownacki on 05.06.2017.
 */
@Slf4j
public class TwitterTask implements Serializable {

    private final SparkSession sparkSession;
    private JavaStreamingContext sc;

    private Thread thread;

    public TwitterTask(JavaStreamingContext sc, Authorization twitterAuth, SparkSession sparkSession) throws InterruptedException {
        this.sc = sc;
        this.sparkSession = sparkSession;
        processStream(twitterAuth);
    }

    private void processStream(Authorization twitterAuth) {
        RestTemplate restTemplate = new RestTemplate();
        String[] filter = Country.getAllHashtags();

        JavaReceiverInputDStream<Status> inputStatusStream = TwitterUtils.createStream(sc, twitterAuth, filter);

        JavaDStream<Status> originalStatusStream =  inputStatusStream.map(status -> status.isRetweet() ? status.getRetweetedStatus() : status);

        JavaPairDStream<String, Integer> statusPair = originalStatusStream.mapToPair(status -> new Tuple2<>(valueOf(status.getId()), getPopularity(status)));
        statusPair.reduceByKeyAndWindow((i1, i2) -> i1 > i2 ? i1 : i2, minutes(60), seconds(10))
                .foreachRDD(rdd -> {

                    Dataset<Row> rowDataset = sparkSession.createDataset(JavaPairRDD.toRDD(rdd), Encoders.tuple(Encoders.STRING(), Encoders.INT())).toDF("tweet_id", "popularity");

                    rowDataset.createOrReplaceTempView("tweets");
                });

        JavaPairDStream<String, Integer> userPair = originalStatusStream.mapToPair(status -> new Tuple2<>(status.getUser().getName(), 1));
        JavaPairDStream<String, Integer> reduced = userPair.reduceByKeyAndWindow((i1, i2) -> i1 + i2, minutes(60), seconds(10));
        //        reduced.count().print();
        //        reduced.print();
        reduced.foreachRDD(rdd -> {
            //            SparkSession spark = SparkSession.builder().config(rdd.rdd().sparkContext().getConf()).getOrCreate();

            Dataset<Row> rowDataset = sparkSession.createDataset(JavaPairRDD.toRDD(rdd), Encoders.tuple(Encoders.STRING(), Encoders.INT())).toDF("user", "tweet_count");

            // Creates a temporary view using the DataFrame
            rowDataset.createOrReplaceTempView("users");

            sparkSession.sql("select count(*) as all_users from users").show();
            sparkSession.sql("select * from users order by tweet_count desc").show();
        });

        inputStatusStream.foreachRDD(rdd -> {
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
                            document.put("countries", tweet.getCountries().isEmpty() ? null : tweet.getCountries().stream().map(Enum::toString).collect(Collectors.toSet()));
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
