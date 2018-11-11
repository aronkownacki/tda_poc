package pl.edu.uj.student.kownacki.aron.tda.batch.dao.mongo;


import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;

public interface TweetRepository extends MongoRepository<Tweet, String> {

    Page<Tweet> findByFavoriteCountLambdaGreaterThan(Integer favoriteCountLambda, Pageable pageable);

    Page<Tweet> findByReceivedAtGreaterThan(Long receivedAt, Pageable pageable);

    Page<Tweet> findByReceivedAtLessThan(Long ReceivedAt, Pageable pageable);

    Page<Tweet> findByReceivedAtBetween(Long from, Long to, Pageable pageable);

    Page<Tweet> findByCountriesIsNullAndReceivedAtGreaterThan(Long rceeivedAt, Pageable pageable);

    Page<Tweet> findByCountries(Set<Country> countries, Pageable pageable);

    Tweet findByStatusId(Long statusId);
}