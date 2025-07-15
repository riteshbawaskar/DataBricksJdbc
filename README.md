# Databricks Data Validation Framework

A comprehensive Java 11 Spring Boot Cucumber framework for validating data pipelines in Databricks, supporting CSV to Bronze table validation and Bronze to Silver table validation with configurable transformations.

## Features

- **CSV to Bronze Table Validation**: Compare CSV files against Bronze tables with configurable column mappings
- **Bronze to Silver Table Validation**: Validate Bronze to Silver transformations with predefined rules
- **Configurable Transformations**: Support for date format conversions, padding, and direct comparisons
- **Databricks JDBC Integration**: Native connection to Databricks clusters
- **Cucumber BDD Testing**: Behavior-driven development with Gherkin syntax
- **Extent Reports**: Detailed HTML reports with test results and metrics
- **Spring Boot Framework**: Enterprise-grade configuration and dependency injection
- **Multiple Configuration Files**: Support for different processes with separate config files

## Project Structure

```
src/
├── main/java/com/databricks/test/
│   ├── config/
│   │   └── TestConfiguration.java
│   ├── service/
│   │   ├── ConfigService.java
│   │   ├── CsvService.java
│   │   ├── DatabaseService.java
│   │   ├── TransformationService.java
│   │   └── ValidationService.java
│   ├── reporting/
│   │   └── ExtentReportManager.java
│   ├── model/
│   │   └── ValidationResult.java
│   └── DatabricksValidationApplication.java
├── test/java/com/databricks/test/
│   ├── context/
│   │   └── TestContext.java
│   ├── steps/
│   │   └── DataValidationSteps.java
│   ├── hooks/
│   │   └── CucumberHooks.java
│   └── runner/
│       └── CucumberTestRunner.java
└── test/resources/
    ├── configs/
    │   └── sample-process-config.yaml
    ├── data/
    │   └── customer_data.csv
    └── features/
        └── data-validation.feature
```

## Configuration

### Database Configuration (application.properties)

```properties
# Databricks JDBC Configuration
databricks.jdbc.url=jdbc:databricks://your-cluster:443/default
databricks.jdbc.username=token
databricks.jdbc.password=your-access-token
```

### Test Configuration (YAML)

Each process has its own configuration file defining:
- Column mappings between CSV and Bronze tables
- Column mappings and transformations between Bronze and Silver tables
- SQL queries for data extraction
- Validation rules and transformation parameters

## Supported Transformations

1. **DIRECT_COMPARE**: Direct value comparison without transformation
2. **PAD_LEFT_ZERO_8**: Left pad with zeros to 8 characters
3. **DATE_FORMAT_DD_MON_YY_TO_DD_MM_YY**: Convert date format from dd-MMM-yy to dd/mm/yy

## Running Tests

### Prerequisites

1. Java 11 or higher
2. Maven 3.6+
3. Databricks cluster access with JDBC enabled
4. Valid Databricks access token

### Execution

```bash
# Run all tests
mvn clean test

# Run specific tags
mvn clean test -Dcucumber.filter.tags="@csv-to-bronze"
mvn clean test -Dcucumber.filter.tags="@bronze-to-silver"

# Run with Spring profile
mvn clean test -Dspring.profiles.active=dev
```

### Test Scenarios

1. **CSV to Bronze Validation**: Validates CSV file data against Bronze table
2. **Bronze to Silver Validation**: Validates Bronze data against Silver table with transformations
3. **End-to-End Validation**: Complete pipeline validation from CSV to Silver
4. **Negative Testing**: Tests with mismatched data
5. **Multiple Process Testing**: Tests different configuration files

## Reports

### Extent Reports
- Location: `target/reports/extent-reports_[timestamp].html`
- Contains detailed test results, execution times, and failure details
- Includes validation statistics and match percentages

### Cucumber Reports
- HTML: `target/cucumber-reports/index.html`
- JSON: `target/cucumber-reports/Cucumber.json`
- XML: `target/cucumber-reports/Cucumber.xml`

## Sample Configuration

```yaml
processName: "customer_data_pipeline"
testDate: "2024-01-15"

csvToBronzeValidation:
  csvFilePath: "src/test/resources/data/customer_data.csv"
  bronzeTableName: "bronze.customer_data"
  columnMappings:
    "Control ID": "control_id"
    "Customer Name": "customer_name"
    "Email Address": "email_address"
  bronzeQuery: |
    SELECT control_id, customer_name, email_address
    FROM bronze.customer_data 
    WHERE process_date = '{testDate}'
    ORDER BY control_id

bronzeToSilverValidation:
  bronzeTableName: "bronze.customer_data"
  silverTableName: "silver.customer_data"
  columnMappings:
    "control_id": 
      silverColumn: "control_id"
      transformationRule: "PAD_LEFT_ZERO_8"
    "registration_date":
      silverColumn: "registration_date"
      transformationRule: "DATE_FORMAT_DD_MON_YY_TO_DD_MM_YY"
```

## Key Features

### Configurable Column Mappings
- Map CSV columns to database table columns
- Support for different column names between layers
- Flexible transformation rules per column

### Transformation Rules
- Date format conversions with configurable patterns
- String padding with customizable characters and lengths
- Numeric comparisons with tolerance settings

### Comprehensive Validation
- Row count validation
- Cell-by-cell data comparison
- Transformation accuracy verification
- Detailed error and warning reporting

### Robust Error Handling
- Database connection management
- File I/O error handling
- Validation result aggregation
- Comprehensive logging

## Best Practices

1. **One Config Per Process**: Create separate configuration files for different data processes
2. **Descriptive Test Names**: Use clear, descriptive names for test scenarios
3. **Data Isolation**: Use date-based filters to isolate test data
4. **Regular Cleanup**: Clean up test data after validation runs
5. **Version Control**: Store configuration files in version control

## Troubleshooting

### Common Issues

1. **Database Connection Failures**
    - Verify Databricks cluster is running
    - Check JDBC URL format
    - Validate access token permissions

2. **Configuration Loading Errors**
    - Ensure YAML syntax is correct
    - Verify file paths exist
    - Check column mapping accuracy

3. **Validation Failures**
    - Review Extent