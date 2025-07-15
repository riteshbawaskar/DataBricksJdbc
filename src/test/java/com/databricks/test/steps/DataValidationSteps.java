package com.databricks.test.steps;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.databricks.test.config.TestConfiguration;
import com.databricks.test.context.TestContext;
import com.databricks.test.model.ValidationResult;
import com.databricks.test.reporting.ExtentReportManager;
import com.databricks.test.service.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DataValidationSteps {

    private static final Logger logger = LoggerFactory.getLogger(DataValidationSteps.class);

    @Autowired
    private ConfigService configService;

    @Autowired
    private CsvService csvService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private ExtentReportManager reportManager;

    @Autowired
    private TestContext testContext;

    private ExtentTest currentTest;

    @Given("I have a test configuration file {string}")
    public void i_have_a_test_configuration_file(String configFileName) {
        try {
            currentTest = reportManager.createTest("Load Configuration",
                    "Loading test configuration from " + configFileName);

            TestConfiguration config = configService.loadConfiguration(configFileName);
            testContext.setTestConfiguration(config);
            testContext.setConfigFileName(configFileName);

            reportManager.addTestInfo(currentTest, "Process Name", config.getProcessName());
            reportManager.addTestInfo(currentTest, "Test Date", config.getTestDate());
            reportManager.markTestPassed(currentTest, "Configuration loaded successfully");

            logger.info("Loaded configuration for process: {}", config.getProcessName());

        } catch (Exception e) {
            reportManager.markTestFailed(currentTest, "Failed to load configuration: " + e.getMessage());
            logger.error("Failed to load configuration: {}", configFileName, e);
            fail("Failed to load configuration: " + e.getMessage());
        }
    }

    @Given("I can connect to the Databricks database")
    public void i_can_connect_to_the_databricks_database() {
        currentTest = reportManager.createTest("Database Connection Test",
                "Testing connection to Databricks database");

        try {
            boolean connected = databaseService.testConnection();
            assertTrue(connected, "Failed to connect to Databricks database");

            reportManager.markTestPassed(currentTest, "Successfully connected to Databricks");
            logger.info("Database connection test passed");

        } catch (Exception e) {
            reportManager.markTestFailed(currentTest, "Database connection failed: " + e.getMessage());
            logger.error("Database connection test failed", e);
            fail("Database connection failed: " + e.getMessage());
        }
    }

    @When("I load the CSV file data")
    public void i_load_the_csv_file_data() {
        currentTest = reportManager.createTest("Load CSV Data",
                "Loading data from CSV file");

        try {
            TestConfiguration config = testContext.getTestConfiguration();
            String csvFilePath = config.getCsvToBronzeValidation().getCsvFilePath();

            List<Map<String, Object>> csvRecords = csvService.readCsvFile(csvFilePath);
            testContext.setCsvRecords(csvRecords);

            reportManager.addTestInfo(currentTest, "CSV File Path", csvFilePath);
            reportManager.addTestInfo(currentTest, "Records Loaded", String.valueOf(csvRecords.size()));
            reportManager.markTestPassed(currentTest, "CSV data loaded successfully");

            logger.info("Loaded {} records from CSV file: {}", csvRecords.size(), csvFilePath);

        } catch (Exception e) {
            reportManager.markTestFailed(currentTest, "Failed to load CSV data: " + e.getMessage());
            logger.error("Failed to load CSV data", e);
            fail("Failed to load CSV data: " + e.getMessage());
        }
    }

    @When("I query the bronze table data")
    public void i_query_the_bronze_table_data() {
        currentTest = reportManager.createTest("Query Bronze Table",
                "Querying data from bronze table");

        try {
            TestConfiguration config = testContext.getTestConfiguration();
            String bronzeQuery = config.getCsvToBronzeValidation().getBronzeQuery();

            List<Map<String, Object>> bronzeRecords = databaseService.executeQuery(bronzeQuery);
            testContext.setBronzeRecords(bronzeRecords);

            reportManager.addTestInfo(currentTest, "Bronze Table",
                    config.getCsvToBronzeValidation().getBronzeTableName());
            reportManager.addTestInfo(currentTest, "Records Retrieved", String.valueOf(bronzeRecords.size()));
            reportManager.addTestStep(currentTest, "Query executed", Status.INFO, bronzeQuery);
            reportManager.markTestPassed(currentTest, "Bronze table data queried successfully");

            logger.info("Retrieved {} records from bronze table", bronzeRecords.size());

        } catch (Exception e) {
            reportManager.markTestFailed(currentTest, "Failed to query bronze table: " + e.getMessage());
            logger.error("Failed to query bronze table", e);
            fail("Failed to query bronze table: " + e.getMessage());
        }
    }

    @When("I query the silver table data")
    public void i_query_the_silver_table_data() {
        currentTest = reportManager.createTest("Query Silver Table",
                "Querying data from silver table");

        try {
            TestConfiguration config = testContext.getTestConfiguration();
            String silverQuery = config.getBronzeToSilverValidation().getSilverQuery();

            List<Map<String, Object>> silverRecords = databaseService.executeQuery(silverQuery);
            testContext.setSilverRecords(silverRecords);

            reportManager.addTestInfo(currentTest, "Silver Table",
                    config.getBronzeToSilverValidation().getSilverTableName());
            reportManager.addTestInfo(currentTest, "Records Retrieved", String.valueOf(silverRecords.size()));
            reportManager.addTestStep(currentTest, "Query executed", Status.INFO, silverQuery);
            reportManager.markTestPassed(currentTest, "Silver table data queried successfully");

            logger.info("Retrieved {} records from silver table", silverRecords.size());

        } catch (Exception e) {
            reportManager.markTestFailed(currentTest, "Failed to query silver table: " + e.getMessage());
            logger.error("Failed to query silver table", e);
            fail("Failed to query silver table: " + e.getMessage());
        }
    }

    @Then("I validate CSV data against bronze table")
    public void i_validate_csv_data_against_bronze_table() {
        currentTest = reportManager.createTest("CSV to Bronze Validation",
                "Validating CSV data against bronze table");

        try {
            TestConfiguration config = testContext.getTestConfiguration();
            List<Map<String, Object>> csvRecords = testContext.getCsvRecords();
            List<Map<String, Object>> bronzeRecords = testContext.getBronzeRecords();

            // Map CSV records to bronze table column names
            List<Map<String, Object>> mappedCsvRecords = csvService.mapCsvToTableColumns(
                    csvRecords, config.getCsvToBronzeValidation().getColumnMappings());

            ValidationResult result = validationService.validateCsvToBronze(
                    mappedCsvRecords, bronzeRecords, config.getCsvToBronzeValidation());

            testContext.setValidationResult(result);

            reportManager.logValidationResult(currentTest, result);

            if (result.isSuccess()) {
                reportManager.markTestPassed(currentTest, "CSV to Bronze validation passed");
                logger.info("CSV to Bronze validation passed: {}", result);
            } else {
                reportManager.markTestFailed(currentTest, "CSV to Bronze validation failed");
                logger.error("CSV to Bronze validation failed: {}", result);
                fail("CSV to Bronze validation failed. Check the report for details.");
            }

        } catch (Exception e) {
            reportManager.markTestFailed(currentTest, "Validation error: " + e.getMessage());
            logger.error("CSV to Bronze validation error", e);
            fail("CSV to Bronze validation error: " + e.getMessage());
        }
    }

    @Then("I validate bronze data against silver table")
    public void i_validate_bronze_data_against_silver_table() {
        currentTest = reportManager.createTest("Bronze to Silver Validation",
                "Validating bronze data against silver table with transformations");

        try {
            TestConfiguration config = testContext.getTestConfiguration();
            List<Map<String, Object>> bronzeRecords = testContext.getBronzeRecords();
            List<Map<String, Object>> silverRecords = testContext.getSilverRecords();

            ValidationResult result = validationService.validateBronzeToSilver(
                    bronzeRecords, silverRecords,
                    config.getBronzeToSilverValidation(),
                    config.getValidationRules());

            testContext.setValidationResult(result);

            reportManager.logValidationResult(currentTest, result);

            if (result.isSuccess()) {
                reportManager.markTestPassed(currentTest, "Bronze to Silver validation passed");
                logger.info("Bronze to Silver validation passed: {}", result);
            } else {
                reportManager.markTestFailed(currentTest, "Bronze to Silver validation failed");
                logger.error("Bronze to Silver validation failed: {}", result);
                fail("Bronze to Silver validation failed. Check the report for details.");
            }

        } catch (Exception e) {
            reportManager.markTestFailed(currentTest, "Validation error: " + e.getMessage());
            logger.error("Bronze to Silver validation error", e);
            fail("Bronze to Silver validation error: " + e.getMessage());
        }
    }

    @Then("the validation should be successful")
    public void the_validation_should_be_successful() {
        ValidationResult result = testContext.getValidationResult();
        assertNotNull(result, "Validation result should not be null");
        assertTrue(result.isSuccess(), "Validation should be successful");

        logger.info("Validation completed successfully: {}", result);
    }

    @Then("the validation should fail with errors")
    public void the_validation_should_fail_with_errors() {
        ValidationResult result = testContext.getValidationResult();
        assertNotNull(result, "Validation result should not be null");
        assertFalse(result.isSuccess(), "Validation should fail");
        assertNotNull(result.getErrors(), "Errors should not be null");
        assertFalse(result.getErrors().isEmpty(), "Errors list should not be empty");

        logger.info("Validation failed as expected: {}", result);
    }
}