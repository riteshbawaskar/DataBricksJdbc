package com.databricks.test.hooks;

import com.databricks.test.context.TestContext;
import com.databricks.test.reporting.ExtentReportManager;
import com.databricks.test.service.DatabaseService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CucumberHooks {

    private static final Logger logger = LoggerFactory.getLogger(CucumberHooks.class);

    @Autowired
    private ExtentReportManager reportManager;

    @Autowired
    private TestContext testContext;

    @Autowired
    private DatabaseService databaseService;

    @Before
    public void setUp(Scenario scenario) {
        logger.info("Starting scenario: {}", scenario.getName());
        testContext.clear();
        testContext.setCurrentTest(scenario.getName());
    }

    @After
    public void tearDown(Scenario scenario) {
        try {
            logger.info("Completed scenario: {} - Status: {}",
                    scenario.getName(), scenario.getStatus());

            if (scenario.isFailed()) {
                logger.error("Scenario failed: {}", scenario.getName());
            }

            // Clean up test context
            testContext.clear();

        } catch (Exception e) {
            logger.error("Error in tearDown", e);
        }
    }

    @After("@database")
    public void cleanupDatabase() {
        try {
            databaseService.closeConnection();
            logger.info("Database connection closed");
        } catch (Exception e) {
            logger.error("Error closing database connection", e);
        }
    }
}