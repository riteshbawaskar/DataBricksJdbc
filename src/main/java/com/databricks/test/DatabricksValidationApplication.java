package com.databricks.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.databricks.test")
public class DatabricksValidationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatabricksValidationApplication.class, args);
    }
}