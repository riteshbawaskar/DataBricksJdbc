package com.databricks.test.runner;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "html:target/cucumber-reports")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "json:target/cucumber-reports/Cucumber.json")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "junit:target/cucumber-reports/Cucumber.xml")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.databricks.test.steps")
@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")
@ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, value = "false")
public class CucumberTestRunner {
}