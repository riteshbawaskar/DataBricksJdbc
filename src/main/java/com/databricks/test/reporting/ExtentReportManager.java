package com.databricks.test.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.databricks.test.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ExtentReportManager {

    private static final Logger logger = LoggerFactory.getLogger(ExtentReportManager.class);

    @Value("${extent.reports.path}")
    private String reportsPath;

    @Value("${extent.reports.title}")
    private String reportsTitle;

    @Value("${extent.reports.document.title}")
    private String documentTitle;

    private ExtentReports extent;
    private ExtentSparkReporter sparkReporter;

    @PostConstruct
    public void initializeReport() {
        try {
            // Create reports directory if it doesn't exist
            File reportsDir = new File(reportsPath).getParentFile();
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            // Generate timestamped report filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String reportFileName = reportsPath.replace(".html", "_" + timestamp + ".html");

            sparkReporter = new ExtentSparkReporter(reportFileName);
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setDocumentTitle(documentTitle);
            sparkReporter.config().setReportName(reportsTitle);
            sparkReporter.config().setTimeStampFormat("dd/MM/yyyy HH:mm:ss");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);

            // System information
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
            extent.setSystemInfo("Test Environment", "Databricks");
            extent.setSystemInfo("Report Generated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            logger.info("Extent Report initialized: {}", reportFileName);

        } catch (Exception e) {
            logger.error("Error initializing Extent Report", e);
        }
    }

    public ExtentTest createTest(String testName, String description) {
        return extent.createTest(testName, description);
    }

    public ExtentTest createTest(String testName) {
        return extent.createTest(testName);
    }

    public void logValidationResult(ExtentTest test, ValidationResult result) {
        if (result.isSuccess()) {
            test.log(Status.PASS, "Validation completed successfully");
        } else {
            test.log(Status.FAIL, "Validation failed");
        }

        // Add validation details
        test.info("Validation Type: " + result.getValidationType());
        test.info("Source: " + result.getSourceName());
        test.info("Target: " + result.getTargetName());
        test.info("Expected Row Count: " + result.getExpectedRowCount());
        test.info("Actual Row Count: " + result.getActualRowCount());
        test.info("Matched Records: " + result.getMatchedRecords());
        test.info("Match Percentage: " + String.format("%.2f%%", result.getMatchPercentage()));
        test.info("Execution Time: " + result.getExecutionTimeMs() + "ms");

        // Add errors if any
        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
            test.log(Status.ERROR, "Errors Found:");
            for (String error : result.getErrors()) {
                test.log(Status.ERROR, error);
            }
        }

        // Add warnings if any
        if (result.getWarnings() != null && !result.getWarnings().isEmpty()) {
            test.log(Status.WARNING, "Warnings Found:");
            for (String warning : result.getWarnings()) {
                test.log(Status.WARNING, warning);
            }
        }
    }

    public void addTestInfo(ExtentTest test, String key, String value) {
        test.info(key + ": " + value);
    }

    public void addTestStep(ExtentTest test, String step, Status status) {
        test.log(status, step);
    }

    public void addTestStep(ExtentTest test, String step, Status status, String details) {
        test.log(status, step + " - " + details);
    }

    public void addScreenshot(ExtentTest test, String screenshotPath) {
        try {
            test.addScreenCaptureFromPath(screenshotPath);
        } catch (Exception e) {
            logger.error("Error adding screenshot to report", e);
        }
    }

    public void markTestPassed(ExtentTest test, String message) {
        test.log(Status.PASS, message);
    }

    public void markTestFailed(ExtentTest test, String message) {
        test.log(Status.FAIL, message);
    }

    public void markTestSkipped(ExtentTest test, String message) {
        test.log(Status.SKIP, message);
    }

    @PreDestroy
    public void flushReport() {
        if (extent != null) {
            extent.flush();
            logger.info("Extent Report flushed and saved");
        }
    }

    public void generateReport() {
        if (extent != null) {
            extent.flush();
            logger.info("Extent Report generated successfully");
        }
    }
}