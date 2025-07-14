import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabricksJDBCExample {
    
    // Databricks connection parameters
    private static final String DATABRICKS_HOST = "your-databricks-host.cloud.databricks.com";
    private static final String HTTP_PATH = "/sql/1.0/warehouses/your-warehouse-id";
    private static final String ACCESS_TOKEN = "your-access-token";
    
    // JDBC URL format for Databricks
    private static final String JDBC_URL = String.format(
        "jdbc:databricks://%s:443;httpPath=%s;AuthMech=3;UID=token;PWD=%s",
        DATABRICKS_HOST, HTTP_PATH, ACCESS_TOKEN
    );
    
    public static void main(String[] args) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            // Load the Databricks JDBC driver
            Class.forName("com.databricks.client.jdbc.Driver");
            
            // Establish connection
            System.out.println("Connecting to Databricks...");
            connection = DriverManager.getConnection(JDBC_URL);
            System.out.println("Connected successfully!");
            
            // Create statement
            statement = connection.createStatement();
            
            // Execute query - replace with your actual table name
            String query = "SELECT * FROM your_database.your_table LIMIT 10";
            System.out.println("Executing query: " + query);
            
            resultSet = statement.executeQuery(query);
            
            // Process results
            int columnCount = resultSet.getMetaData().getColumnCount();
            
            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(resultSet.getMetaData().getColumnName(i) + "\t");
            }
            System.out.println();
            
            // Print data rows
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(resultSet.getString(i) + "\t");
                }
                System.out.println();
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
                System.out.println("Connection closed.");
            } catch (Exception e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}