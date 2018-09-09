package pl.edu.uj.student.kownacki.aron.tda.batch.spark.task;

import java.util.List;

public interface QueryTask {
    List<String> runQuery(String query);
}
