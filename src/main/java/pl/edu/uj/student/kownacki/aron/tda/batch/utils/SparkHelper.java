package pl.edu.uj.student.kownacki.aron.tda.batch.utils;

import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Aron Kownacki on 20.06.2018.
 */
@Slf4j
public class SparkHelper {

    private static final RestTemplate restTemplate = new RestTemplate();

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
