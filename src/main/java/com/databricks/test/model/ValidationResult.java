package com.databricks.test.model;

import java.util.List;

public class ValidationResult {

    private String validationType;
    private String sourceName;
    private String targetName;
    private int expectedRowCount;
    private int actualRowCount;
    private int matchedRecords;
    private boolean success;
    private List<String> errors;
    private List<String> warnings;
    private long executionTimeMs;

    public ValidationResult() {
        this.executionTimeMs = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getValidationType() { return validationType; }
    public void setValidationType(String validationType) { this.validationType = validationType; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public int getExpectedRowCount() { return expectedRowCount; }
    public void setExpectedRowCount(int expectedRowCount) { this.expectedRowCount = expectedRowCount; }

    public int getActualRowCount() { return actualRowCount; }
    public void setActualRowCount(int actualRowCount) { this.actualRowCount = actualRowCount; }

    public int getMatchedRecords() { return matchedRecords; }
    public void setMatchedRecords(int matchedRecords) { this.matchedRecords = matchedRecords; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public void completeExecution() {
        this.executionTimeMs = System.currentTimeMillis() - this.executionTimeMs;
    }

    public double getMatchPercentage() {
        if (expectedRowCount == 0) return 0.0;
        return (double) matchedRecords / expectedRowCount * 100.0;
    }

    @Override
    public String toString() {
        return String.format("ValidationResult{type='%s', source='%s', target='%s', " +
                        "expected=%d, actual=%d, matched=%d, success=%s, errors=%d, warnings=%d, " +
                        "executionTime=%dms, matchPercentage=%.2f%%}",
                validationType, sourceName, targetName, expectedRowCount, actualRowCount,
                matchedRecords, success, errors != null ? errors.size() : 0,
                warnings != null ? warnings.size() : 0, executionTimeMs, getMatchPercentage());
    }
}