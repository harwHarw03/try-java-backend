package com.aeris.controller;

import com.aeris.model.SensorData;
import com.aeris.service.SensorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/data")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PostMapping
    public String addData(@RequestBody SensorData data) {
        sensorService.addData(data);
        return "Data added";
    }

    @GetMapping
    public List<SensorData> getAllData() {
        return sensorService.getAllData();
    }

    @GetMapping("/average")
    public double getAverage() {
        return sensorService.getAverageTemperature();
    }
}
