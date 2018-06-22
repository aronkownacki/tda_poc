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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.edu.uj.student.kownacki.aron.tda.batch.dao.ReportDataRepository;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.StreamOutput;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;
import pl.edu.uj.student.kownacki.aron.tda.batch.mongo.TweetRepository;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@RestController
@RequestMapping("/test")
public class TestViewController {

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

    @Autowired
    Twitter twitterApi;

    @RequestMapping("/sparkContext")
    public String sparkContext() {
        return "result count is: " + sc.textFile("c:/tmp/tds_poc/tweets/*/*").count();
    }

    @RequestMapping("/h2")
    public String test() {
        return "H2 stores " + reportDataRepository.count() + " report entries";
    }

    @RequestMapping("/webSocket")
    public String webSocket() {
        template.convertAndSend("/stream/output", new StreamOutput("test msg: "));
        return "OK";
    }

    @RequestMapping("/mongo")
    public String mongo() {
        Tweet lastStroredStatus = tweetRepository.findAll(new PageRequest(0, 1, Sort.Direction.DESC, "receivedAt")).getContent().get(0);
        return "Mongo stores " + tweetRepository.count() + " tweets, last stored: <a target='_blank' href='https://twitter.com/i/web/status/" + lastStroredStatus.getStatusId() + "'>" + lastStroredStatus + "</>";
    }

    @RequestMapping("/lookup/{statusId}")
    public String lookupForId(@PathVariable("statusId") String statusId) throws TwitterException {
        ResponseList<Status> statuses = twitterApi.lookup(Long.parseLong(statusId));
        if (statuses.isEmpty()) {
            return "Status with id: " + statusId + " does not exist..";
        }
        if (statuses.size() > 1) {
            throw new RuntimeException("it looks like there is more than one status with id: " + statusId);
        }
        Gson gson = new GsonBuilder().create();
//        System.out.print(gson.toJson(statuses.get(0)));
        return gson.toJson(statuses.get(0));
//        return statuses.get(0).toString();
    }

    @RequestMapping("/lookup")
    public String lookupSmaple() {
        int twitterApiWindowInMinytes = 15;
        int twitterApiLookupIdLimitPerRequest = 100;
        int twitterApiLookupRequestLimitPerWindow = 10; //todo should be 300;
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

        for (int i = 0; i < twitterApiLookupRequestLimitPerWindow; i++) {
            try {
                //todo if processing in loop exceeds time limit for window return break
                lookup = twitterApi.lookup(ids);
                System.out.print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + i);

            } catch (TwitterException e) {
                return e.getMessage() + ", iteratio: " + i;
            }
        }
        if (lookup.size() > 0) {
            return "from lookup: " + lookup.stream().map(Status::getFavoriteCount).map(Object::toString).collect(Collectors.joining(", ")) + "/from: " + twitterApiLookupRequestLimitPerWindow + "/" + now.toEpochSecond() + "/to:" + ZonedDateTime
                    .now(ZoneOffset.UTC).toEpochSecond();
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