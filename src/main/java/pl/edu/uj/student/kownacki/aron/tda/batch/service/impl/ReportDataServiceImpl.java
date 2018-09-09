package pl.edu.uj.student.kownacki.aron.tda.batch.service.impl;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static pl.edu.uj.student.kownacki.aron.tda.batch.utils.DateTimeUtils.getNowInMillis;
import static pl.edu.uj.student.kownacki.aron.tda.batch.utils.DateTimeUtils.getTimestampAtStartOfUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public List<List<Double>> getFullReport(Country country, Granularity granularity) {
        return reportDataRepository.findByCountryNameAndGranularity(country, granularity).stream().map(rd -> asList((double) rd.getTimestamp(), (double) rd.getCount())).sorted((o1, o2) -> o1.get(0).compareTo(o2.get(0))).collect(toList());
    }

    @Override
    public List<List<Double>> get24Report(Country country, Granularity granularity) {

        Long minus24hours = getNowInMillis() - (24 * 3600 * 1000);
        return reportDataRepository.findByCountryNameAndGranularity(country, granularity, minus24hours).stream().map(rd -> asList((double) rd.getTimestamp(), (double) rd.getCount())).sorted((o1, o2) -> o1.get(0).compareTo(o2.get(0))).collect(toList());
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
}
