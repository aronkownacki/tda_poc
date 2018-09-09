package pl.edu.uj.student.kownacki.aron.tda.batch.utils;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;

import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;
import twitter4j.HashtagEntity;
import twitter4j.Status;

/**
 * Created by Aron Kownacki on 20.06.2018.
 */
@Slf4j
public class TwitterHelper {

    public static Set<Country> extractCountries(Status status) {
        String statusHashtags = Arrays.stream(status.getHashtagEntities()).map(HashtagEntity::getText).collect(Collectors.joining(","));
        return Arrays.stream(Country.values())
                .filter(country ->
                        country.getHashtags().stream().anyMatch(countryHashtag ->
                                containsIgnoreCase(statusHashtags, countryHashtag) || containsIgnoreCase(status.getText(), countryHashtag)))
                .collect(toSet());
    }

    public static int getPopularity(Status status) {
        return status.getFavoriteCount() + status.getRetweetCount();
    }

    public static Boolean acceptedStatus(Status status) {
        return !status.isRetweet();
    }

    public static Tweet buildTweet(Status status) {
        return Tweet.builder()
                .countries(extractCountries(status))
                .favoriteCountLambda(0)
                .favoriteCount(status.isFavorited() || status.isRetweeted() ? status.getFavoriteCount() : 0)
                .statusId(status.getId())
                .receivedAt(status.getCreatedAt().getTime())
                .build();
    }

    public static Document serialize(Tweet tweet) {
        Document document = new Document();
        document.put("countries", tweet.getCountries().isEmpty() ? null : tweet.getCountries().stream().map(Enum::toString).collect(Collectors.toSet()));
        document.put("favoriteCountLambda", tweet.getFavoriteCountLambda());
        document.put("favoriteCount", tweet.getFavoriteCount());
        document.put("statusId", tweet.getStatusId());
        document.put("receivedAt", tweet.getReceivedAt());
        return document;
    }
}
