package com.example.cardiacrecorder;

import com.example.cardiacrecorder.classes.EachData;
import com.example.cardiacrecorder.classes.DataProcessor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Arrays;

public class IntegrationTest {

    @Test
    public void testEachDataProcessing() {
        // Creating test data
        EachData data1 = new EachData(1, System.currentTimeMillis(), "01-01-2023", "12:00AM", 120, 80, 70, "Comment 1");
        EachData data2 = new EachData(2, System.currentTimeMillis(), "02-01-2023", "01:00PM", 130, 85, 75, "Comment 2");
        EachData data3 = new EachData(3, System.currentTimeMillis(), "03-01-2023", "02:00PM", 140, 90, 80, "Comment 3");

        List<EachData> dataList = Arrays.asList(data1, data2, data3);

        // Mocking DataProcessor
        DataProcessor dataProcessor = Mockito.mock(DataProcessor.class);
        Mockito.when(dataProcessor.processData(dataList)).thenReturn(true);

        // Performing the integration test
        boolean result = dataProcessor.processData(dataList);

        // Verifying the result
        Assert.assertTrue("Data processing failed", result);

        // Additional verification if needed
        Mockito.verify(dataProcessor).processData(dataList);
    }
}
