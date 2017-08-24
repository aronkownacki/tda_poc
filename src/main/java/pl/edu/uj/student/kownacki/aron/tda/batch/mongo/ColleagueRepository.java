package pl.edu.uj.student.kownacki.aron.tda.batch.mongo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by bartoszjedrzejewski on 31/10/2016.
 */
public interface ColleagueRepository extends MongoRepository<Colleague, String> {

    public List<Colleague> findByName(String name);

}