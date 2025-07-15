package com.databricks.test.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvService {

    private static final Logger logger = LoggerFactory.getLogger(CsvService.class);

    public List<Map<String, Object>> readCsvFile(String filePath) throws IOException, CsvException {
        List<Map<String, Object>> records = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> allRecords = reader.readAll();

            if (allRecords.isEmpty()) {
                logger.warn("CSV file is empty: {}", filePath);
                return records;
            }

            // First row contains headers
            String[] headers = allRecords.get(0);

            // Process data rows
            for (int i = 1; i < allRecords.size(); i++) {
                String[] values = allRecords.get(i);
                Map<String, Object> record = new HashMap<>();

                for (int j = 0; j < headers.length && j < values.length; j++) {
                    String header = headers[j].trim();
                    String value = values[j].trim();

                    // Convert to appropriate data type
                    Object convertedValue = convertValue(value);
                    record.put(header, convertedValue);
                }

                records.add(record);
            }

            logger.info("Successfully read {} records from CSV file: {}", records.size(), filePath);
        }

        return records;
    }

    private Object convertValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // Try to convert to number
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            // Return as string if not a number
            return value;
        }
    }

    public List<String> getCsvHeaders(String filePath) throws IOException, CsvException {
        List<String> headers = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] headerArray = reader.readNext();

            if (headerArray != null) {
                for (String header : headerArray) {
                    headers.add(header.trim());
                }
            }
        }

        return headers;
    }

    public int getCsvRecordCount(String filePath) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> allRecords = reader.readAll();
            // Subtract 1 for header row
            return Math.max(0, allRecords.size() - 1);
        }
    }

    public List<Map<String, Object>> mapCsvToTableColumns(
            List<Map<String, Object>> csvRecords,
            Map<String, String> columnMappings) {

        List<Map<String, Object>> mappedRecords = new ArrayList<>();

        for (Map<String, Object> csvRecord : csvRecords) {
            Map<String, Object> mappedRecord = new HashMap<>();

            for (Map.Entry<String, String> mapping : columnMappings.entrySet()) {
                String csvColumn = mapping.getKey();
                String tableColumn = mapping.getValue();

                if (csvRecord.containsKey(csvColumn)) {
                    mappedRecord.put(tableColumn, csvRecord.get(csvColumn));
                } else {
                    logger.warn("CSV column '{}' not found in record", csvColumn);
                }
            }

            mappedRecords.add(mappedRecord);
        }

        logger.info("Mapped {} CSV records to table column format", mappedRecords.size());
        return mappedRecords;
    }
}