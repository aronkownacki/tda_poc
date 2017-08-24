package pl.edu.uj.student.kownacki.aron.tda.batch.service.impl;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import pl.edu.uj.student.kownacki.aron.tda.batch.dao.ReportDataRepository;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.ReportData;
import pl.edu.uj.student.kownacki.aron.tda.batch.service.ReportService;

/**
 * Created by Aron Kownacki on 14.08.2017.
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportDataRepository reportDataRepository;

    private Map<Country, List<List<Double>>> fullReport = null;


    public ReportServiceImpl() {
        this.fullReport = buildFullReport();
    }

    //todo this is mock version
    private Map<Country, List<List<Double>>> buildFullReport() {
        LocalDateTime today = ZonedDateTime.now().toLocalDate().atStartOfDay();
        return Arrays.stream(Country.values()).collect(toMap(c -> c, c -> {
            List<List<Double>> list = IntStream.range(0, 10).boxed().map(i -> asList((double) today.atZone(ZoneId.systemDefault()).minusDays(i).toEpochSecond() * 1000, (double) Math.round(Math.random() * 100))).collect(toList());
            list.sort((o1, o2) -> o1.get(0).compareTo(o2.get(0)));
            return list;
        }));
    }

    @Override
    public List<List<Double>> getRandomReport(Country country) {
        return fullReport.get(country);
    }

    @Override
    public List<List<Double>> getMonthlyReport(Country country) {
         return reportDataRepository.findByCountryName(country).stream().map(rd -> asList((double) rd.getTimestamp(), (double) rd.getCount())).sorted((o1, o2) -> o1.get(0).compareTo(o2.get(0))).collect(toList());
    }

    @Override
    @Async
    public void update(Map<Country, Long> updateMap) {
        Long now = ZonedDateTime.now().toEpochSecond() * 1000;

        reportDataRepository.save(
            updateMap.entrySet().stream().map(entry -> {
                ReportData reportData = new ReportData();
                reportData.setCountryName(entry.getKey());
                reportData.setCount(entry.getValue());
                reportData.setTimestamp(now);
                return reportData;
            }).collect(toList())

        );

        updateMap.forEach((country, count) -> {

            fullReport.get(country).add(asList((double) now, (double) count));
        });

        Arrays.stream(Country.values()).filter(c -> !updateMap.keySet().contains(c)).forEach(country ->
            fullReport.get(country).add(asList((double) now, 0.0))
        );
    }
}
