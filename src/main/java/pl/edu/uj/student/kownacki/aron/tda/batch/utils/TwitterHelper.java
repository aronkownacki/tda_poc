package pl.edu.uj.student.kownacki.aron.tda.batch.utils;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import twitter4j.HashtagEntity;
import twitter4j.Status;

/**
 * Created by Aron Kownacki on 20.06.2018.
 */
@Slf4j
public class TwitterHelper {

        public static Set<Country> extractCountries(Status status) {
            String statusHashtags = Arrays.stream(status.getHashtagEntities()).map(HashtagEntity::getText).collect(Collectors.joining(","));
            return Arrays.stream(Country.values()).filter(country -> country.getHashtags().stream().anyMatch(countryHashtag -> {
                boolean contains = containsIgnoreCase(statusHashtags, countryHashtag) || containsIgnoreCase(status.getText(), countryHashtag);
                if (contains) {
                    log.info(">>>>>>>>>>>>>>>>>contains>>>>>>>>>>>>>>>>>>" + countryHashtag);
                }
                return contains;
            })).collect(toSet());
        }

        public static int getPopularity(Status status) {
            return status.getFavoriteCount() + status.getRetweetCount();
        }

        public static Boolean acceptedStatus(Status status){
            return !status.isRetweet();
        }
}
