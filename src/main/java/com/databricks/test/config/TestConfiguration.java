package com.databricks.test.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class TestConfiguration {

    @JsonProperty("processName")
    private String processName;

    @JsonProperty("testDate")
    private String testDate;

    @JsonProperty("csvToBronzeValidation")
    private CsvToBronzeValidation csvToBronzeValidation;

    @JsonProperty("bronzeToSilverValidation")
    private BronzeToSilverValidation bronzeToSilverValidation;

    @JsonProperty("validationRules")
    private ValidationRules validationRules;

    // Getters and Setters
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }

    public String getTestDate() { return testDate; }
    public void setTestDate(String testDate) { this.testDate = testDate; }

    public CsvToBronzeValidation getCsvToBronzeValidation() { return csvToBronzeValidation; }
    public void setCsvToBronzeValidation(CsvToBronzeValidation csvToBronzeValidation) {
        this.csvToBronzeValidation = csvToBronzeValidation;
    }

    public BronzeToSilverValidation getBronzeToSilverValidation() { return bronzeToSilverValidation; }
    public void setBronzeToSilverValidation(BronzeToSilverValidation bronzeToSilverValidation) {
        this.bronzeToSilverValidation = bronzeToSilverValidation;
    }

    public ValidationRules getValidationRules() { return validationRules; }
    public void setValidationRules(ValidationRules validationRules) { this.validationRules = validationRules; }

    public static class CsvToBronzeValidation {
        @JsonProperty("csvFilePath")
        private String csvFilePath;

        @JsonProperty("bronzeTableName")
        private String bronzeTableName;

        @JsonProperty("columnMappings")
        private Map<String, String> columnMappings;

        @JsonProperty("bronzeQuery")
        private String bronzeQuery;

        // Getters and Setters
        public String getCsvFilePath() { return csvFilePath; }
        public void setCsvFilePath(String csvFilePath) { this.csvFilePath = csvFilePath; }

        public String getBronzeTableName() { return bronzeTableName; }
        public void setBronzeTableName(String bronzeTableName) { this.bronzeTableName = bronzeTableName; }

        public Map<String, String> getColumnMappings() { return columnMappings; }
        public void setColumnMappings(Map<String, String> columnMappings) { this.columnMappings = columnMappings; }

        public String getBronzeQuery() { return bronzeQuery; }
        public void setBronzeQuery(String bronzeQuery) { this.bronzeQuery = bronzeQuery; }
    }

    public static class BronzeToSilverValidation {
        @JsonProperty("bronzeTableName")
        private String bronzeTableName;

        @JsonProperty("silverTableName")
        private String silverTableName;

        @JsonProperty("columnMappings")
        private Map<String, ColumnMapping> columnMappings;

        @JsonProperty("bronzeQuery")
        private String bronzeQuery;

        @JsonProperty("silverQuery")
        private String silverQuery;

        // Getters and Setters
        public String getBronzeTableName() { return bronzeTableName; }
        public void setBronzeTableName(String bronzeTableName) { this.bronzeTableName = bronzeTableName; }

        public String getSilverTableName() { return silverTableName; }
        public void setSilverTableName(String silverTableName) { this.silverTableName = silverTableName; }

        public Map<String, ColumnMapping> getColumnMappings() { return columnMappings; }
        public void setColumnMappings(Map<String, ColumnMapping> columnMappings) { this.columnMappings = columnMappings; }

        public String getBronzeQuery() { return bronzeQuery; }
        public void setBronzeQuery(String bronzeQuery) { this.bronzeQuery = bronzeQuery; }

        public String getSilverQuery() { return silverQuery; }
        public void setSilverQuery(String silverQuery) { this.silverQuery = silverQuery; }
    }

    public static class ColumnMapping {
        @JsonProperty("silverColumn")
        private String silverColumn;

        @JsonProperty("transformationRule")
        private String transformationRule;

        // Getters and Setters
        public String getSilverColumn() { return silverColumn; }
        public void setSilverColumn(String silverColumn) { this.silverColumn = silverColumn; }

        public String getTransformationRule() { return transformationRule; }
        public void setTransformationRule(String transformationRule) { this.transformationRule = transformationRule; }
    }

    public static class ValidationRules {
        @JsonProperty("numericTolerance")
        private double numericTolerance;

        @JsonProperty("dateFormats")
        private DateFormats dateFormats;

        @JsonProperty("paddingConfig")
        private PaddingConfig paddingConfig;

        // Getters and Setters
        public double getNumericTolerance() { return numericTolerance; }
        public void setNumericTolerance(double numericTolerance) { this.numericTolerance = numericTolerance; }

        public DateFormats getDateFormats() { return dateFormats; }
        public void setDateFormats(DateFormats dateFormats) { this.dateFormats = dateFormats; }

        public PaddingConfig getPaddingConfig() { return paddingConfig; }
        public void setPaddingConfig(PaddingConfig paddingConfig) { this.paddingConfig = paddingConfig; }
    }

    public static class DateFormats {
        @JsonProperty("input")
        private String input;

        @JsonProperty("output")
        private String output;

        // Getters and Setters
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }

        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
    }

    public static class PaddingConfig {
        @JsonProperty("padChar")
        private String padChar;

        @JsonProperty("padDirection")
        private String padDirection;

        @JsonProperty("targetLength")
        private int targetLength;

        // Getters and Setters
        public String getPadChar() { return padChar; }
        public void setPadChar(String padChar) { this.padChar = padChar; }

        public String getPadDirection() { return padDirection; }
        public void setPadDirection(String padDirection) { this.padDirection = padDirection; }

        public int getTargetLength() { return targetLength; }
        public void setTargetLength(int targetLength) { this.targetLength = targetLength; }
    }
}