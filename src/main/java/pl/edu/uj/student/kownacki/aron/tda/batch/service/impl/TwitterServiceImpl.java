package pl.edu.uj.student.kownacki.aron.tda.batch.service.impl;

import static java.util.stream.Collectors.toMap;
import static pl.edu.uj.student.kownacki.aron.tda.batch.utils.DateTimeUtils.getZone;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.dao.mongo.TweetRepository;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;
import pl.edu.uj.student.kownacki.aron.tda.batch.service.ReportDataService;
import pl.edu.uj.student.kownacki.aron.tda.batch.service.TwitterService;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@Slf4j
@Service
public class TwitterServiceImpl implements TwitterService {

    //todo move to properties
    public static final int twitterApiWindowInMinytes = 15;
    public static final int twitterApiLookupIdLimitPerRequest = 100;
    public static final int twitterApiLookupRequestLimitPerWindow = 300;
    public static final int lookupStartTimeShiftInHours = 12;

    @Autowired
    private Twitter twitterApi;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private ReportDataService reportDataService;

    @Override
    public void favoriteCounting() {

        ZonedDateTime now = ZonedDateTime.now(getZone());
        ZonedDateTime lookupShift = now.minusHours(lookupStartTimeShiftInHours);
        Sort sort = new Sort(Sort.Direction.ASC, "receivedAt");

        Map<Country, Long> updateMap = Arrays.stream(Country.values()).collect(toMap(country -> country, country -> 0L));
        Pageable pageable = new PageRequest(0, twitterApiLookupIdLimitPerRequest, sort);

        while (true) {

            Page<Tweet> tweets = tweetRepository.findByReceivedAtGreaterThan(lookupShift.toInstant().toEpochMilli(), pageable);

            if (tweets.getContent().isEmpty()) {
                break;
            }

            Map<Long, Tweet> idsToTweets = tweets.getContent().stream().filter(t -> t.getCountries() != null).filter(t -> t.getStatusId() != null).collect(Collectors.toMap(Tweet::getStatusId, t -> t, (t1, t2) -> t1));

            ResponseList<Status> statuses = null;

            try {
                //todo if processing in loop exceeds time limit for window return break
                statuses = twitterApi.lookup(ArrayUtils.toPrimitive(idsToTweets.keySet().toArray(new Long[0])));

            } catch (TwitterException e) {
                log.warn("exception occurred {}", e);
                break;
            }

            tweetRepository.save(statuses.stream().filter(status -> status.getFavoriteCount() > 0).map(status -> {
                Tweet tweet = idsToTweets.get(status.getId());
                Integer oldFavoriteCount = tweet.getFavoriteCount();
                Integer newFavoriteCount = status.getFavoriteCount();
                if (oldFavoriteCount < newFavoriteCount) {
                    long delta = newFavoriteCount - oldFavoriteCount;
                    tweet.getCountries().forEach(country -> {
                        updateMap.compute(country, (c, count) -> count + delta);
                    });

                    tweet.setFavoriteCount(newFavoriteCount); //todo save delata too
                    return tweet;
                }

                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList()));

            if (!tweets.hasNext() || pageable.next().getPageNumber() == twitterApiLookupRequestLimitPerWindow) {
                break;
            }
            pageable = tweets.nextPageable();
        }
    
        reportDataService.update(updateMap);
    }
}
