package pl.edu.uj.student.kownacki.aron.tda.batch.web.test;

import static java.util.stream.Collectors.toList;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.edu.uj.student.kownacki.aron.tda.batch.dao.ReportDataRepository;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.StreamOutput;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;
import pl.edu.uj.student.kownacki.aron.tda.batch.mongo.TweetRepository;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.api.TweetsResources;

@Deprecated
@RestController
@RequestMapping("/test/deprecated")
public class OldTestController {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private JavaSparkContext sc;

    @Autowired
    private SparkSession ss;

    @Autowired
    private ReportDataRepository reportDataRepository;

    @Autowired
    private TweetRepository tweetRepository;

    @RequestMapping("/sparkContext")
    public String sparkContext() {
        return "result count is: " + sc.textFile("c:/tmp/tds_poc/tweets/*/*").count();
    }

    @RequestMapping("/test")
    public String test() {
        return "data count: " + reportDataRepository.count();
    }

    @RequestMapping("/webSocket")
    public void webSocket() {
        template.convertAndSend("/stream/output", new StreamOutput("test msg: "));
    }

    @RequestMapping("/mongo")
    public String mongo() {
        return "mongo" + tweetRepository.count();
//        Dataset<SimplePojo> ds = MongoSpark.load(sc).toDS(SimplePojo.class);
//            ds.createOrReplaceTempView("toupdate");

//        Dataset<Row> toUpdate = ss.sql("SELECT id from toupdate where id>0");
//        MongoSpark.save(ds.map(p-> {p.setUser("aron"); return p;}).toDF().write().option("collection", "collNameToUpdate").mode("append"));
//        return "mongo: " + MongoSpark.load(sc).toDS(SimplePojo.class).selectExpr()
//                return "mongo: " + MongoSpark.load(sc).toDS(SimplePojo.class).collectAsList().stream().map(SimplePojo::getId).filter(integer -> integer != null && integer > 0).collect(Collectors.toList());
    }

    @RequestMapping("/favorite")
    public String favorite() {
        int twitterApiWindowInMinytes = 15;
        int twitterApiLookupIdLimitPerRequest = 100;
        int twitterApiLookupRequestLimitPerWindow = 300;
        int lookupStartTimeShiftInHours = 12;
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime lookupShift = now.minusHours(lookupStartTimeShiftInHours);
        int page = 0;
        int size = twitterApiLookupIdLimitPerRequest;
        Sort sort = new Sort(Sort.Direction.ASC, "receivedAt");
        Pageable pageable = new PageRequest(page, size, sort);
        List<Tweet> tweets = tweetRepository.findByReceivedAtGreaterThan(lookupShift.toInstant().toEpochMilli(), pageable).getContent();
        String before = tweets.stream().map(Tweet::getFavoriteCountLambda).map(Long::toString).collect(Collectors.joining(", "));

        long[] ids = tweets.stream().map(Tweet::getStatusId).filter(i -> i != null).mapToLong(Long::longValue).toArray();

        ResponseList<Status> lookup = null;
        TweetsResources twitterApi = TwitterFactory.getSingleton().tweets();

        for (int i = 0; i < twitterApiLookupRequestLimitPerWindow; i++) {
            try {
                //todo if processing in loop exceeds time limit for window return break
                lookup = twitterApi.lookup(ids);
                System.out.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + i);

            } catch (TwitterException e) {
                return e.getMessage() + ", iteration: " + i;
            }
        }
        if (lookup.size() > 0) {
            return "from lookup: " +
                lookup.stream().map(Status::getFavoriteCount).map(Object::toString).collect(Collectors.joining(", ")) + "/from: "
                    + twitterApiLookupRequestLimitPerWindow + "/"
                    + now.toEpochSecond() + "/to:" + ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond();
        }


        tweetRepository.save(tweets.stream().map(t -> {
            t.setFavoriteCountLambda(1 + t.getFavoriteCountLambda());
            return t;
        }).collect(toList()));
        tweets = tweetRepository.findByReceivedAtGreaterThan(lookupShift.toInstant().toEpochMilli(), pageable).getContent();
        String after = tweets.stream().map(Tweet::getFavoriteCountLambda).map(Long::toString).collect(Collectors.joining(", "));


        return "favoriteTest before: [" + before + "] after: [" + after + "]";
    }
}