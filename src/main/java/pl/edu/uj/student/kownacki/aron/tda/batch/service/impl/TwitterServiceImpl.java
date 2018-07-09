package pl.edu.uj.student.kownacki.aron.tda.batch.service.impl;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.dao.mongo.TweetRepository;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;
import pl.edu.uj.student.kownacki.aron.tda.batch.service.TwitterService;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

@Slf4j
@Service
public class TwitterServiceImpl implements TwitterService {

    //todo move to properties
    public static final int twitterApiWindowInMinytes = 15;
    public static final int twitterApiLookupIdLimitPerRequest = 100;
    public static final int twitterApiLookupRequestLimitPerWindow = 300;
    public static final int lookupStartTimeShiftInHours = 12;

    @Autowired
    private TweetRepository tweetRepository;

    @Override
    public void favoriteCounting() {

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime lookupShift = now.minusHours(lookupStartTimeShiftInHours);
        Sort sort = new Sort(Sort.Direction.ASC, "receivedAt");

        for (int page = 0; page < twitterApiLookupRequestLimitPerWindow; page++) {

            Pageable pageable = new PageRequest(page, twitterApiLookupIdLimitPerRequest, sort);

            List<Tweet> tweets = tweetRepository.findByReceivedAtGreaterThan(lookupShift.toInstant().toEpochMilli(), pageable).getContent();

            long[] ids = tweets.stream().map(Tweet::getStatusId).filter(Objects::nonNull).mapToLong(Long::longValue).toArray();

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
    }
}
