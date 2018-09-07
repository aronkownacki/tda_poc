package pl.edu.uj.student.kownacki.aron.tda.batch.spark.task;

import java.util.List;

public interface TaskQuery {
    List<String> runQuery(String query);
}
