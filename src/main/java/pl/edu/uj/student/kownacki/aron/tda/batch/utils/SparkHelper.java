package pl.edu.uj.student.kownacki.aron.tda.batch.utils;

import java.io.Serializable;
import java.util.List;

import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function2;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.TrafficData;

/**
 * Created by Aron Kownacki on 20.06.2018.
 */
@Slf4j
public class SparkHelper implements Serializable {

    private static final RestTemplate restTemplate = new RestTemplate();

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public static Function2<List<Integer>, Optional<Object>, Optional<Object>> calculateTrafficData() {
        return (integers, state) -> {
            TrafficData trafficData = (TrafficData) state.or(new TrafficData());
            int count = integers.size();
            if(count > trafficData.getMax()){
                trafficData.setMax(count);
            }
            trafficData.setAvg(((trafficData.getCount() * trafficData.getAvg()) + count) / (trafficData.getCount() + 1));

            trafficData.setCount(trafficData.getCount() + 1);
            return Optional.of(trafficData);

        };
    }
}
