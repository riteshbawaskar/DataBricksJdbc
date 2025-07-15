package com.databricks.test.service;

import com.databricks.test.config.TestConfiguration;
import com.databricks.test.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    @Autowired
    private TransformationService transformationService;

    public ValidationResult validateCsvToBronze(
            List<Map<String, Object>> csvRecords,
            List<Map<String, Object>> bronzeRecords,
            TestConfiguration.CsvToBronzeValidation config) {

        logger.info("Starting CSV to Bronze validation");

        ValidationResult result = new ValidationResult();
        result.setValidationType("CSV_TO_BRONZE");
        result.setSourceName("CSV File");
        result.setTargetName(config.getBronzeTableName());
        result.setExpectedRowCount(csvRecords.size());
        result.setActualRowCount(bronzeRecords.size());

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check row count
        if (csvRecords.size() != bronzeRecords.size()) {
            errors.add(String.format("Row count mismatch: CSV has %d rows, Bronze table has %d rows",
                    csvRecords.size(), bronzeRecords.size()));
        }

        // Validate each record
        int minRecords = Math.min(csvRecords.size(), bronzeRecords.size());
        int matchedRecords = 0;

        for (int i = 0; i < minRecords; i++) {
            Map<String, Object> csvRecord = csvRecords.get(i);
            Map<String, Object> bronzeRecord = bronzeRecords.get(i);

            boolean recordMatches = validateRecord(csvRecord, bronzeRecord,
                    config.getColumnMappings(),
                    errors, warnings, i + 1);

            if (recordMatches) {
                matchedRecords++;
            }
        }

        result.setMatchedRecords(matchedRecords);
        result.setErrors(errors);
        result.setWarnings(warnings);
        result.setSuccess(errors.isEmpty());

        logger.info("CSV to Bronze validation completed. Success: {}, Matched records: {}/{}",
                result.isSuccess(), matchedRecords, minRecords);

        return result;
    }

    public ValidationResult validateBronzeToSilver(
            List<Map<String, Object>> bronzeRecords,
            List<Map<String, Object>> silverRecords,
            TestConfiguration.BronzeToSilverValidation config,
            TestConfiguration.ValidationRules validationRules) {

        logger.info("Starting Bronze to Silver validation");

        ValidationResult result = new ValidationResult();
        result.setValidationType("BRONZE_TO_SILVER");
        result.setSourceName(config.getBronzeTableName());
        result.setTargetName(config.getSilverTableName());
        result.setExpectedRowCount(bronzeRecords.size());
        result.setActualRowCount(silverRecords.size());

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check row count
        if (bronzeRecords.size() != silverRecords.size()) {
            errors.add(String.format("Row count mismatch: Bronze has %d rows, Silver has %d rows",
                    bronzeRecords.size(), silverRecords.size()));
        }

        // Validate each record with transformations
        int minRecords = Math.min(bronzeRecords.size(), silverRecords.size());
        int matchedRecords = 0;

        for (int i = 0; i < minRecords; i++) {
            Map<String, Object> bronzeRecord = bronzeRecords.get(i);
            Map<String, Object> silverRecord = silverRecords.get(i);

            boolean recordMatches = validateRecordWithTransformation(
                    bronzeRecord, silverRecord, config.getColumnMappings(),
                    validationRules, errors, warnings, i + 1);

            if (recordMatches) {
                matchedRecords++;
            }
        }

        result.setMatchedRecords(matchedRecords);
        result.setErrors(errors);
        result.setWarnings(warnings);
        result.setSuccess(errors.isEmpty());

        logger.info("Bronze to Silver validation completed. Success: {}, Matched records: {}/{}",
                result.isSuccess(), matchedRecords, minRecords);

        return result;
    }

    private boolean validateRecord(Map<String, Object> sourceRecord,
                                   Map<String, Object> targetRecord,
                                   Map<String, String> columnMappings,
                                   List<String> errors,
                                   List<String> warnings,
                                   int recordNumber) {

        boolean recordMatches = true;

        for (Map.Entry<String, String> mapping : columnMappings.entrySet()) {
            String sourceColumn = mapping.getKey();
            String targetColumn = mapping.getValue();

            Object sourceValue = sourceRecord.get(sourceColumn);
            Object targetValue = targetRecord.get(targetColumn);

            if (!compareValues(sourceValue, targetValue)) {
                errors.add(String.format("Record %d: Column '%s' -> '%s' mismatch. Expected: '%s', Actual: '%s'",
                        recordNumber, sourceColumn, targetColumn, sourceValue, targetValue));
                recordMatches = false;
            }
        }

        return recordMatches;
    }

    private boolean validateRecordWithTransformation(
            Map<String, Object> bronzeRecord,
            Map<String, Object> silverRecord,
            Map<String, TestConfiguration.ColumnMapping> columnMappings,
            TestConfiguration.ValidationRules validationRules,
            List<String> errors,
            List<String> warnings,
            int recordNumber) {

        boolean recordMatches = true;

        for (Map.Entry<String, TestConfiguration.ColumnMapping> mapping : columnMappings.entrySet()) {
            String bronzeColumn = mapping.getKey();
            TestConfiguration.ColumnMapping columnMapping = mapping.getValue();
            String silverColumn = columnMapping.getSilverColumn();
            String transformationRule = columnMapping.getTransformationRule();

            Object bronzeValue = bronzeRecord.get(bronzeColumn);
            Object silverValue = silverRecord.get(silverColumn);

            if (!transformationService.compareValues(bronzeValue, silverValue, transformationRule, validationRules)) {
                Object transformedValue = transformationService.applyTransformation(
                        bronzeValue, transformationRule, validationRules);

                errors.add(String.format(
                        "Record %d: Column '%s' -> '%s' mismatch after transformation '%s'. " +
                                "Original: '%s', Transformed: '%s', Actual: '%s'",
                        recordNumber, bronzeColumn, silverColumn, transformationRule,
                        bronzeValue, transformedValue, silverValue));
                recordMatches = false;
            }
        }

        return recordMatches;
    }

    private boolean compareValues(Object expected, Object actual) {
        if (expected == null && actual == null) {
            return true;
        }

        if (expected == null || actual == null) {
            return false;
        }

        return String.valueOf(expected).equals(String.valueOf(actual));
    }
}