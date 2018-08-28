package pl.edu.uj.student.kownacki.aron.tda.batch.service.impl;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static pl.edu.uj.student.kownacki.aron.tda.batch.utils.DateTimeUtils.getNowInMillis;
import static pl.edu.uj.student.kownacki.aron.tda.batch.utils.DateTimeUtils.getTimestampAtStartOfUnit;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import pl.edu.uj.student.kownacki.aron.tda.batch.dao.jpa.ReportDataRepository;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Country;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.Granularity;
import pl.edu.uj.student.kownacki.aron.tda.batch.model.ReportData;
import pl.edu.uj.student.kownacki.aron.tda.batch.service.ReportDataService;

/**
 * Created by Aron Kownacki on 14.08.2017.
 */
@Service
public class ReportDataServiceImpl implements ReportDataService {

    @Autowired
    private ReportDataRepository reportDataRepository;

    private Map<Country, List<List<Double>>> inMemoryReportData = null;

    public ReportDataServiceImpl() {
        this.inMemoryReportData = buildFullReport();
    }

    //todo this is mock version
    private Map<Country, List<List<Double>>> buildFullReport() {
        LocalDateTime today = ZonedDateTime.now().toLocalDate().atStartOfDay();
        return Arrays.stream(Country.values()).collect(toMap(country -> country, country -> {
            List<List<Double>> list = IntStream.range(0, 10).boxed().map(i -> asList((double) today.atZone(ZoneId.systemDefault()).minusDays(i).toEpochSecond() * 1000, (double) Math.round(Math.random() * 100))).collect(toList());
            list.sort((o1, o2) -> o1.get(0).compareTo(o2.get(0)));
            return list;
        }));
    }

    @Override
    public List<List<Double>> getRandomReport(Country country) {
        return inMemoryReportData.get(country);
    }

    @Override
    public List<List<Double>> getReport(Country country, Granularity granularity) {
        return reportDataRepository.findByCountryNameAndGranularity(country, granularity).stream().map(rd -> asList((double) rd.getTimestamp(), (double) rd.getCount())).sorted((o1, o2) -> o1.get(0).compareTo(o2.get(0))).collect(toList());
    }

    @Override
    @Async
    public void update(Map<Country, Long> updateMap) {
        Long nowInMillis = getNowInMillis();

        reportDataRepository.save(updateMap.entrySet().stream().map(entry -> {
                    ReportData reportData = new ReportData();
                    reportData.setCountryName(entry.getKey());
                    reportData.setCount(entry.getValue());
                    reportData.setTimestamp(nowInMillis);
                    reportData.setGranularity(Granularity.MILLISECOND);
                    return reportData;
                }).collect(toList())

        );

        updateInMemoryReportData(updateMap, nowInMillis);
    }

    @Override
    public void aggregate(Granularity granularity) {
        stream(Country.values()).forEach(country -> aggregate(granularity, country));
    }

    private void aggregate(Granularity granularity, Country country) {

        Pageable pageRequest = new PageRequest(0, 100);
        while (true) {
            Page<ReportData> latest = reportDataRepository.findByCountryNameAndGranularity(country, granularity, new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "id")));
            Long beginTimestamp = latest.getContent().isEmpty() ? 0 : latest.getContent().get(0).getTimestamp();
            Long endTimestamp = getNowInMillis();

            Page<ReportData> toAggregate = reportDataRepository.findByCountryNameAndGranularity(country, Granularity.MILLISECOND, beginTimestamp, endTimestamp, pageRequest);

            Map<Long, Long> aggregatedMap = new HashMap<>();

            toAggregate.forEach(reportData -> {
                long timestampAtStartOfUnit = getTimestampAtStartOfUnit(reportData.getTimestamp(), granularity.getUnit());
                long currentCount = aggregatedMap.getOrDefault(timestampAtStartOfUnit, 0L);
                aggregatedMap.put(timestampAtStartOfUnit, currentCount + reportData.getCount());
            });

            if(aggregatedMap.get(beginTimestamp) != null){
                ReportData latestReportData = latest.getContent().get(0);
                latestReportData.setCount(latestReportData.getCount() + aggregatedMap.get(beginTimestamp));
                reportDataRepository.save(latestReportData);
                aggregatedMap.remove(beginTimestamp);
            }

            List<ReportData> newReportEntries = aggregatedMap.entrySet().stream().map(entry -> {
                ReportData reportData = new ReportData();
                reportData.setCountryName(country);
                reportData.setCount(entry.getValue());
                reportData.setTimestamp(entry.getKey());
                reportData.setGranularity(granularity);
                return reportData;
            }).collect(Collectors.toList());

            if (!newReportEntries.isEmpty()) {
                reportDataRepository.save(newReportEntries);
            }

            if (!toAggregate.hasNext()) {
                break;
            }
            pageRequest = toAggregate.nextPageable();
        }

    }

    private void updateInMemoryReportData(Map<Country, Long> updateMap, double nowInMillis) {
        updateMap.forEach((country, count) -> {
            inMemoryReportData.get(country).add(asList(nowInMillis, (double) count));
        });

        stream(Country.values()).filter(c -> !updateMap.keySet().contains(c)).forEach(country -> inMemoryReportData.get(country).add(asList(nowInMillis, 0.0)));
    }
}
