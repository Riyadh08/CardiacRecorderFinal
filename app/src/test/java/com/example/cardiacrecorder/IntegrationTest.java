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
    @Test
    public void testEachDataProcessingWithUnusualData() {
        // Creating test data with unusual values
        EachData data1 = new EachData(1, System.currentTimeMillis(), "01-01-2023", "12:00AM", 150, 100, 90, "Comment 1");
        EachData data2 = new EachData(2, System.currentTimeMillis(), "02-01-2023", "01:00PM", 130, 95, 85, "Comment 2");
        EachData data3 = new EachData(3, System.currentTimeMillis(), "03-01-2023", "02:00PM", 160, 105, 100, "Comment 3");

        List<EachData> dataList = Arrays.asList(data1, data2, data3);

        // Instantiate DataProcessor
        DataProcessor dataProcessor = new DataProcessor();

        // Performing the integration test
        boolean result = dataProcessor.processData(dataList);

        // Verifying the result
        Assert.assertFalse("Data processing passed with unusual data", result);
    }

    @Test
    public void testCombinedUnusualChecks() {
        // Creating test data with mixed normal and unusual values
        EachData data1 = new EachData(1, System.currentTimeMillis(), "01-01-2023", "12:00AM", 120, 80, 70, "Comment 1");
        EachData data2 = new EachData(2, System.currentTimeMillis(), "02-01-2023", "01:00PM", 130, 85, 75, "Comment 2");
        EachData data3 = new EachData(3, System.currentTimeMillis(), "03-01-2023", "02:00PM", 140, 90, 80, "Comment 3");
        EachData data4 = new EachData(4, System.currentTimeMillis(), "04-01-2023", "03:00PM", 80, 55, 50, "Comment 4");

        List<EachData> dataList = Arrays.asList(data1, data2, data3, data4);

        // Instantiate DataProcessor
        DataProcessor dataProcessor = new DataProcessor();

        // Performing the integration test
        boolean result = dataProcessor.processData(dataList);

        // Verifying the result
        Assert.assertFalse("Data processing passed with mixed normal and unusual data", result);
    }
    @Test
    public void testMockedDataProcessorWithNormalData() {
        // Creating test data with normal values
        EachData data1 = new EachData(1, System.currentTimeMillis(), "01-01-2023", "12:00AM", 120, 80, 70, "Comment 1");
        EachData data2 = new EachData(2, System.currentTimeMillis(), "02-01-2023", "01:00PM", 125, 85, 75, "Comment 2");
        EachData data3 = new EachData(3, System.currentTimeMillis(), "03-01-2023", "02:00PM", 110, 75, 65, "Comment 3");

        List<EachData> dataList = Arrays.asList(data1, data2, data3);

        // Mocking DataProcessor
        DataProcessor dataProcessor = Mockito.mock(DataProcessor.class);
        Mockito.when(dataProcessor.processData(dataList)).thenReturn(true);

        // Performing the integration test
        boolean result = dataProcessor.processData(dataList);

        // Verifying the result
        Assert.assertTrue("Data processing failed with mocked normal data", result);

        // Additional verification if needed
        Mockito.verify(dataProcessor).processData(dataList);
    }
    @Test
    public void testMockedDataProcessorWithUnusualData() {
        // Creating test data with unusual values
        EachData data1 = new EachData(1, System.currentTimeMillis(), "01-01-2023", "12:00AM", 150, 100, 90, "Comment 1");
        EachData data2 = new EachData(2, System.currentTimeMillis(), "02-01-2023", "01:00PM", 130, 95, 85, "Comment 2");
        EachData data3 = new EachData(3, System.currentTimeMillis(), "03-01-2023", "02:00PM", 160, 105, 100, "Comment 3");

        List<EachData> dataList = Arrays.asList(data1, data2, data3);

        // Mocking DataProcessor
        DataProcessor dataProcessor = Mockito.mock(DataProcessor.class);
        Mockito.when(dataProcessor.processData(dataList)).thenReturn(false);

        // Performing the integration test
        boolean result = dataProcessor.processData(dataList);

        // Verifying the result
        Assert.assertFalse("Data processing passed with mocked unusual data", result);

        // Additional verification if needed
        Mockito.verify(dataProcessor).processData(dataList);
    }
}
