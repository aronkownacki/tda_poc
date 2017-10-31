package pl.edu.uj.student.kownacki.aron.tda.batch.scheduled;

import static java.util.stream.Collectors.toList;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;
import pl.edu.uj.student.kownacki.aron.tda.batch.mongo.TweetRepository;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.api.TweetsResources;

/**
 * Created by Aron Kownacki on 23.07.2017.
 */
@Slf4j
@Component
public class ScheduledJob {

    //todo move to properties
    public static final int twitterApiWindowInMinytes = 15;
    public static final int twitterApiLookupIdLimitPerRequest = 100;
    public static final int twitterApiLookupRequestLimitPerWindow = 300;
    public static final int lookupStartTimeShiftInHours = 12;

    @Autowired
    private TweetRepository tweetRepository;

    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void runFavoriteCounting() {
        favoriteCounting();
    }

    public String favoriteCounting() {

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime lookupShift = now.minusHours(lookupStartTimeShiftInHours);
        Sort sort = new Sort(Sort.Direction.ASC, "receivedAt");

        for (int page = 0; page < twitterApiLookupRequestLimitPerWindow; page++) {

            Pageable pageable = new PageRequest(page, twitterApiLookupIdLimitPerRequest, sort);

            List<Tweet> tweets = tweetRepository.findByReceivedAtGreaterThan(lookupShift.toInstant().toEpochMilli(), pageable).getContent();

            long[] ids = tweets.stream().map(Tweet::getStatusId).filter(i -> i != null).mapToLong(Long::longValue).toArray();

            ResponseList<Status> statuses = null;

            try {
                //todo if processing in loop exceeds time limit for window return break
               statuses = TwitterFactory.getSingleton().tweets().lookup(ids);

            } catch (TwitterException e) {
                log.warn("exception occurred {}", e);
                break;
            }

            statuses.stream().filter(status -> status.getFavoriteCount() > 0).forEach(status -> {

            });

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
