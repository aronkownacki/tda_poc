package pl.edu.uj.student.kownacki.aron.tda.batch.spark.task.impl;

import java.io.Serializable;
import java.util.List;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.spark.task.TaskQuery;

/**
 * Created by Aron Kownacki on 05.06.2017.
 */
@Slf4j
@Service
public class TwitterTaskQueryImpl implements TaskQuery, Serializable {

    @Autowired
    private SparkSession sparkSession;

    @Override
    public List<String> runQuery(String query) {
        return sparkSession.sql(query).toJSON().collectAsList();
    }
}
