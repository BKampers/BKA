package bka.theworks.persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class DatabaseConfig {
    private static final String DB_URL = "jdbc:h2:mem:theworks";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static Connection connection;

    public static synchronized Connection getConnection() throws PersistenceException {
        if (connection == null || isClosed(connection)) {
            connection = createConnection();
            initializeTables();
        }
        return connection;
    }

    private static Connection createConnection() throws PersistenceException {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException ex) {
            throw new PersistenceException("Failed to create database connection", ex);
        }
    }

    private static boolean isClosed(Connection conn) {
        try {
            return conn.isClosed();
        } catch (SQLException ex) {
            return true;
        }
    }

    private static void initializeTables() throws PersistenceException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS albums (id BIGINT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(255), artist VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS tracks (id BIGINT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(255) NOT NULL, artist VARCHAR(255), year INTEGER, duration_seconds BIGINT NOT NULL, play_date TIMESTAMP, album_id BIGINT, disc_number INTEGER, track_number INTEGER, FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE)");
        } catch (SQLException ex) {
            throw new PersistenceException("Failed to initialize database tables", ex);
        }
    }

    public static void shutdown() throws PersistenceException {
        if (connection != null && !isClosed(connection)) {
            try {
                connection.close();
            } catch (SQLException ex) {
                throw new PersistenceException("Failed to close database connection", ex);
            }
        }
    }
} 
