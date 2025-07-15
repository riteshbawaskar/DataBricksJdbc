Feature: Data Pipeline Validation
  As a data engineer
  I want to validate data between different pipeline stages
  So that I can ensure data quality and transformation accuracy

  Background:
    Given I can connect to the Databricks database

  @csv-to-bronze @database
  Scenario: Validate CSV data against Bronze table
    Given I have a test configuration file "sample-process-config.yaml"
    When I load the CSV file data
    And I query the bronze table data
    Then I validate CSV data against bronze table
    And the validation should be successful

  @bronze-to-silver @database
  Scenario: Validate Bronze data against Silver table with transformations
    Given I have a test configuration file "sample-process-config.yaml"
    When I query the bronze table data
    And I query the silver table data
    Then I validate bronze data against silver table
    And the validation should be successful

  @end-to-end @database
  Scenario: Complete pipeline validation from CSV to Silver
    Given I have a test configuration file "sample-process-config.yaml"
    When I load the CSV file data
    And I query the bronze table data
    And I query the silver table data
    Then I validate CSV data against bronze table
    And I validate bronze data against silver table
    And the validation should be successful

  @negative-test @database
  Scenario: Validate with mismatched data
    Given I have a test configuration file "invalid-data-config.yaml"
    When I load the CSV file data
    And I query the bronze table data
    Then I validate CSV data against bronze table
    And the validation should fail with errors

  @configuration-test
  Scenario Outline: Validate multiple processes
    Given I have a test configuration file "<config_file>"
    When I load the CSV file data
    And I query the bronze table data
    Then I validate CSV data against bronze table
    And the validation should be successful

    Examples:
      | config_file                    |
      | customer-data-config.yaml      |
      | transaction-data-config.yaml   |
      | product-data-config.yaml       |

  @transformation-test @database
  Scenario: Validate specific transformations
    Given I have a test configuration file "transformation-test-config.yaml"
    When I query the bronze table data
    And I query the silver table data
    Then I validate bronze data against silver table
    And the validation should be successful

  @data-quality @database
  Scenario: Data quality checks
    Given I have a test configuration file "data-quality-config.yaml"
    When I load the CSV file data
    And I query the bronze table data
    Then I validate CSV data against bronze table
    And the validation should be successful