package bka.theworks.persistence;

import java.math.BigInteger;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;


public final class Database {

    private Database() {
        // Util class must not be instantiated
    }

    public static synchronized Connection getConnection() throws DatabaseException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = createConnection();
                initializeTables();
            }
            return connection;
        }
        catch (SQLException ex) {
            throw new DatabaseException("Failed to get database connection", ex);
        }
    }

    private static Connection createConnection() throws DatabaseException {
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(true);
            return conn;
        }
        catch (ClassNotFoundException | SQLException ex) {
            throw new DatabaseException("Failed to create database connection", ex);
        }
    }

    public static void shutdown() throws DatabaseException {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            }
            catch (SQLException ex) {
                throw new DatabaseException("Failed to close database connection", ex);
            }
        }
    }

    private static void initializeTables() throws DatabaseException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS albums (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(255),
                    artist VARCHAR(255))
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS tracks (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(255) NOT NULL,
                    artist VARCHAR(255),
                    "year" INTEGER,
                    duration_seconds BIGINT NOT NULL,
                    play_count INTEGER NOT NULL,
                    play_date TIMESTAMP,
                    album_id BIGINT,
                    disc_number INTEGER,
                    track_number INTEGER,
                FOREIGN KEY (album_id) REFERENCES albums(id)
                ON DELETE CASCADE)
            """);
        }
        catch (SQLException ex) {
            throw new DatabaseException("Failed to initialize database tables", ex);
        }
    }

    public static void storeAlbum(Map<String, Object> album) throws DatabaseException {
        Long albumId;
        try (Connection batchConnection = getConnection()) {
            batchConnection.setAutoCommit(false);
            try (PreparedStatement statement = batchConnection.prepareStatement(INSERT_ALBUM_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, (String) album.get("Name"));
                statement.setString(2, (String) album.get("Artist"));
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new DatabaseException("Creating album failed, no rows affected.");
                }
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        albumId = generatedKeys.getLong(1);
                    }
                    else {
                        throw new DatabaseException("Creating album failed, no ID obtained.");
                    }
                }
            }
            try (PreparedStatement statement = getConnection().prepareStatement(INSERT_TRACK_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                for (Map<String, Object> track : (List<Map<String, Object>>) album.get("tracks")) {
                    Map<String, Object> t = new HashMap<>(track);
                    t.put("Album Id", albumId);
                    setTrack(statement, t);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            batchConnection.commit();
        }
        catch (SQLException ex) {
            throw new DatabaseException("Failed to store album", ex);
        }
    }

    public static void storeTrack(Map<String, Object> track) throws DatabaseException {
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_TRACK_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            setTrack(statement, track);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            throw new DatabaseException("Failed to store track", ex);
        }
    }

    public static void setTrack(PreparedStatement statement, Map<String, Object> track) throws SQLException {
        statement.setString(1, (String) Objects.requireNonNull(track.get("Name"), "Missing 'Name' in track"));
        statement.setString(2, (String) track.get("Artist"));
        statement.setObject(3, (track.containsKey("Year")) ? ((BigInteger) track.get("Year")).intValue() : null);
        statement.setLong(4, ((BigInteger) Objects.requireNonNull(track.get("Total Time"), "Missing 'Total Time' in track")).longValue() / 1000);
        statement.setInt(5, (track.containsKey("Play Count") ? ((BigInteger) track.get("Play Count")).intValue() : 0));
        statement.setObject(6, (track.containsKey("Play Date UTC")) ? Timestamp.from(((ZonedDateTime) track.get("Play Date UTC")).toInstant()) : null);
        statement.setObject(7, (Long) track.get("Album Id"));
        statement.setObject(8, (track.containsKey("Disc Number")) ? ((BigInteger) track.get("Disc Number")).intValue() : null);
        statement.setObject(9, (track.containsKey("Track Number")) ? ((BigInteger) track.get("Track Number")).intValue() : null);
    }

    public static List<Map<String, Object>> query(String sql) throws DatabaseException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (ResultSet result = getConnection().createStatement().executeQuery(sql)) {
            ResultSetMetaData metadata = result.getMetaData();
            while (result.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= metadata.getColumnCount(); i++) {
                    row.put(metadata.getTableName(i) + "." + metadata.getColumnName(i), result.getObject(i));
                }
                rows.add(row);
            }
        }
        catch (SQLException ex) {
            throw new DatabaseException(String.format("Failed to execute:%n```%s```", sql), ex);
        }
        return rows;
    }

    private static final String DB_URL = "jdbc:h2:mem:theworks;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static Connection connection;

    private static final String INSERT_ALBUM_QUERY = """
        INSERT INTO albums
            (title, artist)
            VALUES (?, ?)
    """;
    public static final String INSERT_TRACK_QUERY = """
        INSERT INTO tracks
           (title, artist, "year", duration_seconds, play_count, play_date, album_id, disc_number, track_number)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;
}
