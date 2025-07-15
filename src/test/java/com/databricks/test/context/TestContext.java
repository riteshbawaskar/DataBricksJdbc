package com.databricks.test.context;

import com.databricks.test.config.TestConfiguration;
import com.databricks.test.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestContext {

    private final Map<String, Object> context = new HashMap<>();

    public void set(String key, Object value) {
        context.put(key, value);
    }

    public Object get(String key) {
        return context.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) context.get(key);
    }

    public boolean containsKey(String key) {
        return context.containsKey(key);
    }

    public void clear() {
        context.clear();
    }

    public void remove(String key) {
        context.remove(key);
    }

    // Convenience methods for common objects
    public void setTestConfiguration(TestConfiguration config) {
        set("testConfiguration", config);
    }

    public TestConfiguration getTestConfiguration() {
        return get("testConfiguration", TestConfiguration.class);
    }

    public void setCsvRecords(List<Map<String, Object>> csvRecords) {
        set("csvRecords", csvRecords);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCsvRecords() {
        return (List<Map<String, Object>>) get("csvRecords");
    }

    public void setBronzeRecords(List<Map<String, Object>> bronzeRecords) {
        set("bronzeRecords", bronzeRecords);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getBronzeRecords() {
        return (List<Map<String, Object>>) get("bronzeRecords");
    }

    public void setSilverRecords(List<Map<String, Object>> silverRecords) {
        set("silverRecords", silverRecords);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getSilverRecords() {
        return (List<Map<String, Object>>) get("silverRecords");
    }

    public void setValidationResult(ValidationResult validationResult) {
        set("validationResult", validationResult);
    }

    public ValidationResult getValidationResult() {
        return get("validationResult", ValidationResult.class);
    }

    public void setConfigFileName(String configFileName) {
        set("configFileName", configFileName);
    }

    public String getConfigFileName() {
        return get("configFileName", String.class);
    }

    public void setCurrentTest(String currentTest) {
        set("currentTest", currentTest);
    }

    public String getCurrentTest() {
        return get("currentTest", String.class);
    }
}