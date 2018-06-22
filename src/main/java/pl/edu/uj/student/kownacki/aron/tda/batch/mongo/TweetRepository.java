package pl.edu.uj.student.kownacki.aron.tda.batch.mongo;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;

public interface TweetRepository extends MongoRepository<Tweet, String> {

    Page<Tweet> findByFavoriteCountLambdaGreaterThan(Integer favoriteCountLambda, Pageable pageable);

    Page<Tweet> findByReceivedAtGreaterThan(Long ReceivedAt, Pageable pageable);

    Page<Tweet> findByReceivedAtLessThan(Long ReceivedAt, Pageable pageable);
}