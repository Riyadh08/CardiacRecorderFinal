package com.example.cardiacrecorder.classes;


import java.util.List;

public class DataProcessor {

    public boolean processData(List<EachData> dataList) {
        // Processing logic for EachData
        for (EachData data : dataList) {
            // Example processing: Check if data is unusual
            if (data.isSysUnusual() || data.isDysUnusual() || data.isHeartRateUnusual()) {
                return false;
            }
        }
        return true;
    }
}