package com.databricks.test.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    @Value("${databricks.jdbc.url}")
    private String jdbcUrl;

    @Value("${databricks.jdbc.username}")
    private String username;

    @Value("${databricks.jdbc.password}")
    private String password;

    @Value("${databricks.jdbc.driver}")
    private String driverClass;

    private Connection connection;

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName(driverClass);
                connection = DriverManager.getConnection(jdbcUrl, username, password);
                logger.info("Successfully connected to Databricks");
            } catch (ClassNotFoundException e) {
                logger.error("Databricks JDBC driver not found", e);
                throw new SQLException("Driver not found", e);
            }
        }
        return connection;
    }

    public List<Map<String, Object>> executeQuery(String query) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }

            logger.info("Executed query successfully. Retrieved {} rows", results.size());
            logger.debug("Query: {}", query);
        }

        return results;
    }

    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            logger.error("Connection test failed", e);
        }
        return false;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }

    public int getRowCount(String tableName, String whereClause) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM " + tableName;
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            query += " WHERE " + whereClause;
        }

        List<Map<String, Object>> results = executeQuery(query);
        if (!results.isEmpty()) {
            return ((Number) results.get(0).get("count")).intValue();
        }
        return 0;
    }

    public List<String> getTableColumns(String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();

        try (PreparedStatement stmt = getConnection().prepareStatement(
                "DESCRIBE TABLE " + tableName)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                columns.add(rs.getString("col_name"));
            }
        }

        return columns;
    }
}