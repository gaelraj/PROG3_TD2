import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public Connection getDBConnection() {
        Connection connection = null;

        try {
            String jdbcURL = System.getenv("JDBC_URL");
            String user = System.getenv("USER");
            String password = System.getenv("PASSWORD");
            connection = DriverManager.getConnection(jdbcURL, user, password);
            System.out.println("Connection established!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }
}
