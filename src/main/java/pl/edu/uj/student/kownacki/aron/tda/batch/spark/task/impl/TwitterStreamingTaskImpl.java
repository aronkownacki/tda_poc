package pl.edu.uj.student.kownacki.aron.tda.batch.spark.task.impl;

import static java.lang.String.valueOf;
import static org.apache.spark.streaming.Durations.minutes;
import static org.apache.spark.streaming.Durations.seconds;
import static com.google.common.collect.Iterables.size;
import static pl.edu.uj.student.kownacki.aron.tda.batch.utils.TwitterHelper.extractCountries;
import static pl.edu.uj.student.kownacki.aron.tda.batch.utils.TwitterHelper.getPopularity;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;

import com.mongodb.spark.MongoSpark;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.spark.task.StreamingTask;
import pl.edu.uj.student.kownacki.aron.tda.batch.utils.SparkHelper;
import pl.edu.uj.student.kownacki.aron.tda.batch.utils.TwitterHelper;
import scala.Tuple2;
import twitter4j.Status;
import twitter4j.auth.Authorization;

/**
 * Created by Aron Kownacki on 05.06.2017.
 */
@Slf4j
@Getter
@Setter
public class TwitterStreamingTaskImpl implements StreamingTask, Serializable {

    private SparkSession sparkSession;
    private transient JavaStreamingContext sc;

    private transient Thread thread;

    public TwitterStreamingTaskImpl(JavaStreamingContext sc, Authorization twitterAuth, SparkSession sparkSession) {
        this.sc = sc;
        this.sparkSession = sparkSession;
        processStream(twitterAuth);
    }

    @Override
    public void processStream(Authorization twitterAuth) {

        String[] filter = Country.getAllHashtags();

        JavaReceiverInputDStream<Status> inputStatusStream = TwitterUtils.createStream(sc, twitterAuth, filter);

        inputStatusStream.mapToPair(status -> new Tuple2<>(valueOf(status.getId()), 1)).count().print();

        JavaDStream<Status> originalStatusStream =  inputStatusStream.map(status -> status.isRetweet() ? status.getRetweetedStatus() : status);

        JavaPairDStream<String, Integer> statusPair = originalStatusStream.mapToPair(status -> new Tuple2<>(valueOf(status.getId()), getPopularity(status)));

        statusPair.reduceByKeyAndWindow((i1, i2) -> i1 > i2 ? i1 : i2, minutes(60), seconds(10)).foreachRDD(rdd -> {

            Dataset<Row> rowDataset = sparkSession.createDataset(JavaPairRDD.toRDD(rdd), Encoders.tuple(Encoders.STRING(), Encoders.INT())).toDF("tweet_id", "popularity");

            rowDataset.createOrReplaceTempView("tweets");
        });

        JavaPairDStream<String, Integer> userPair = originalStatusStream.mapToPair(status -> new Tuple2<>(status.getUser().getName(), 1));

        userPair.reduceByKeyAndWindow((i1, i2) -> i1 + i2, minutes(60), seconds(10)).foreachRDD(rdd -> {
            Dataset<Row> rowDataset = sparkSession.createDataset(JavaPairRDD.toRDD(rdd), Encoders.tuple(Encoders.STRING(), Encoders.INT())).toDF("user", "tweet_count");

            rowDataset.createOrReplaceTempView("users");

            sparkSession.sql("select count(*) as all_users from users").show();
            sparkSession.sql("select * from users order by tweet_count desc").show();
        });

        inputStatusStream.filter(TwitterHelper::acceptedStatus).foreachRDD(rdd -> {
            rdd.foreachPartition(partition -> {

                Iterable<Status> statuses = () -> partition;
                Map<Country, Long> result = StreamSupport.stream(statuses.spliterator(), false).flatMap(s -> extractCountries(s).stream()).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                result.put(Country.EU, result.getOrDefault(Country.EU, 0L) + size(statuses));
                SparkHelper.getRestTemplate().postForEntity("http://localhost:10400/report/update", result, Map.class);
            });

            MongoSpark.save(
                    rdd.map(TwitterHelper::buildTweet).map(TwitterHelper::serialize));
        });
    }

    @Override
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

    @Override
    public void stop() {
        sc.stop();
        thread.interrupt();
    }
}
