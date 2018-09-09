package pl.edu.uj.student.kownacki.aron.tda.batch.spark.task;

import twitter4j.auth.Authorization;

public interface StreamingTask {

    void start();

    void stop();

    void processStream(Authorization twitterAuth);
}
