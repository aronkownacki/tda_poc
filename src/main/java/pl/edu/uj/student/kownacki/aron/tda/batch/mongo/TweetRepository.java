package pl.edu.uj.student.kownacki.aron.tda.batch.mongo;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import pl.edu.uj.student.kownacki.aron.tda.batch.model.Tweet;

/**
 * Created by bartoszjedrzejewski on 31/10/2016.
 */
public interface TweetRepository extends MongoRepository<Tweet, String> {

    Page<Tweet> findByFavoriteCountLambdaGreaterThan(Integer favoriteCountLambda, Pageable pageable);

    Page<Tweet> findByReceivedAtGreaterThan(Long ReceivedAt, Pageable pageable);

    Page<Tweet> findByReceivedAtLessThan(Long ReceivedAt, Pageable pageable);
}