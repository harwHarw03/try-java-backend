package com.aeris.service;

import com.aeris.model.SensorData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SensorService {

    private final List<SensorData> dataList = new ArrayList<>();

    public void addData(SensorData data) {
        dataList.add(data);
    }

    public List<SensorData> getAllData() {
        return dataList;
    }

    public double getAverageTemperature() {
        if (dataList.isEmpty()) return 0;

        double sum = 0;
        for (SensorData d : dataList) {
            sum += d.getTemperature();
        }
        return sum / dataList.size();
    }
}
