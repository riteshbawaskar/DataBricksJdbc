package com.databricks.test.service;

import com.databricks.test.config.TestConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    @Value("${test.config.directory}")
    private String configDirectory;

    private final ObjectMapper yamlMapper;
    private final Map<String, TestConfiguration> configCache;

    public ConfigService() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.configCache = new HashMap<>();
    }

    public TestConfiguration loadConfiguration(String configFileName) throws IOException {
        if (configCache.containsKey(configFileName)) {
            logger.debug("Returning cached configuration for: {}", configFileName);
            return configCache.get(configFileName);
        }

        String configPath = configDirectory + File.separator + configFileName;
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configPath);
        }

        logger.info("Loading configuration from: {}", configPath);

        try {
            TestConfiguration config = yamlMapper.readValue(configFile, TestConfiguration.class);

            // Replace placeholders in SQL queries
            replacePlaceholders(config);

            configCache.put(configFileName, config);

            logger.info("Successfully loaded configuration for process: {}", config.getProcessName());
            return config;

        } catch (Exception e) {
            logger.error("Error loading configuration from: {}", configPath, e);
            throw new IOException("Failed to load configuration", e);
        }
    }

    private void replacePlaceholders(TestConfiguration config) {
        String testDate = config.getTestDate();

        if (config.getCsvToBronzeValidation() != null) {
            String bronzeQuery = config.getCsvToBronzeValidation().getBronzeQuery();
            if (bronzeQuery != null) {
                bronzeQuery = bronzeQuery.replace("{testDate}", testDate);
                config.getCsvToBronzeValidation().setBronzeQuery(bronzeQuery);
            }
        }

        if (config.getBronzeToSilverValidation() != null) {
            String bronzeQuery = config.getBronzeToSilverValidation().getBronzeQuery();
            if (bronzeQuery != null) {
                bronzeQuery = bronzeQuery.replace("{testDate}", testDate);
                config.getBronzeToSilverValidation().setBronzeQuery(bronzeQuery);
            }

            String silverQuery = config.getBronzeToSilverValidation().getSilverQuery();
            if (silverQuery != null) {
                silverQuery = silverQuery.replace("{testDate}", testDate);
                config.getBronzeToSilverValidation().setSilverQuery(silverQuery);
            }
        }

        logger.debug("Replaced placeholders in SQL queries with testDate: {}", testDate);
    }

    public void clearCache() {
        configCache.clear();
        logger.info("Configuration cache cleared");
    }

    public void clearCache(String configFileName) {
        configCache.remove(configFileName);
        logger.info("Configuration cache cleared for: {}", configFileName);
    }

    public boolean isConfigurationCached(String configFileName) {
        return configCache.containsKey(configFileName);
    }

    public Map<String, TestConfiguration> getAllCachedConfigurations() {
        return new HashMap<>(configCache);
    }
}