package bka.theworks.persistence;

import java.math.BigInteger;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.*;


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
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(true);
            return conn;
        }
        catch (SQLException ex) {
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
            for (Map.Entry<String, ColumnDefinition[]> tableEntry : TABLE_DEFINITIONS.entrySet()) {
                statement.execute(createTableStatement(tableEntry.getKey(), tableEntry.getValue()));
            }
        }
        catch (SQLException ex) {
            throw new DatabaseException("Failed to initialize database tables", ex);
        }
    }

    private static String createTableStatement(String tableName, ColumnDefinition[] columns) {
        return String.format("""
            CREATE TABLE IF NOT EXISTS %s(
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                %s
            %s)""",
            tableName,
            Arrays.stream(columns)
                .map(Object::toString)
                .collect(Collectors.joining(",\n    ")),
            Arrays.stream(columns)
                .filter(column -> column.foreignKey().isPresent())
                .map(column -> column.foreignKey().get().toString(column.name()))
                .collect(Collectors.joining(",\n")));
    }

    public static void storeAlbum(Map<String, Object> album) throws DatabaseException {
        Long albumId;
        try (Connection batchConnection = getConnection()) {
            batchConnection.setAutoCommit(false);
            try (PreparedStatement statement = batchConnection.prepareStatement(insertStatement(ALBUMS), Statement.RETURN_GENERATED_KEYS)) {
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
            try (PreparedStatement statement = getConnection().prepareStatement(insertStatement(TRACKS), Statement.RETURN_GENERATED_KEYS)) {
                for (Map<String, Object> track : (List<Map<String, Object>>) album.get(TRACKS)) {
                    Map<String, Object> t = new HashMap<>(track);
                    t.put("Album Id", BigInteger.valueOf(albumId));
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
        try (PreparedStatement statement = getConnection().prepareStatement(insertStatement(TRACKS), Statement.RETURN_GENERATED_KEYS)) {
            setTrack(statement, track);
            statement.executeUpdate();
        }
        catch (SQLException ex) {
            throw new DatabaseException("Failed to store track", ex);
        }
    }

    public static void setTrack(PreparedStatement statement, Map<String, Object> track) throws SQLException {
        int columnIndex = 1;
        for (ColumnDefinition column : TABLE_DEFINITIONS.get(TRACKS)) {
            Object value = track.get(column.sourceName());
            if (value == null && column.constraint() == ColumnConstraint.NOT_NULL) {
                throw new SQLException("Missing '" + column.sourceName() + "' in track");
            }
            switch (column.type().primitive()) {
                case VARCHAR ->
                    statement.setString(columnIndex, (String) value);
                case TIMESTAMP ->
                    statement.setTimestamp(columnIndex, (value == null) ? null : Timestamp.from(((ZonedDateTime) value).toInstant()));
                case BIGINT ->
                    statement.setObject(columnIndex, (value == null) ? null : ((BigInteger) value).longValue());
                case INTEGER ->
                    statement.setObject(columnIndex, (value == null) ? null : ((BigInteger) value).intValue());
                default ->
                    throw new IllegalStateException(column.type().primitive().name());
            }
            columnIndex++;
        }
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

    private static String insertStatement(String tableName) {
        ColumnDefinition[] columns = TABLE_DEFINITIONS.get(tableName);
        return String.format("""
            INSERT INTO %s
            %s
            VALUES %s
        """,
            tableName,
            Arrays.stream(columns).map(ColumnDefinition::name).collect(Collectors.joining(", ", "(", ")")),
            Arrays.stream(columns).map(column -> "?").collect(Collectors.joining(", ", "(", ")")));
    }


    private enum Primitive {
        INTEGER,
        BIGINT,
        VARCHAR,
        TIMESTAMP
    }

    private record Type(Primitive primitive, OptionalInt width) {

        public Type(Primitive primitive) {
            this(primitive, OptionalInt.empty());
        }

        public Type(Primitive primitive, int width) {
            this(primitive, OptionalInt.of(width));
        }

        @Override
        public String toString() {
            if (width.isPresent()) {
                return primitive.toString() + '(' + width.getAsInt() + ')';
            }
            return primitive.toString();
        }

    }


    private enum ColumnConstraint {
        NULLABLE("NULL"),
        NOT_NULL("NOT NULL");

        private ColumnConstraint(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }

        private final String string;

    }

    private record ForeignKey(String tableName, String columnName) {

        public String toString(String localColumnName) {
            return String.format(",\nFOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE", localColumnName, tableName, columnName);
        }
    }

    private record ColumnDefinition(String name, Type type, ColumnConstraint constraint, Optional<ForeignKey> foreignKey, String sourceName) {

        public ColumnDefinition(String name, Type type, ColumnConstraint constraint, String sourceName) {
            this(name.equals("year") ? "\"year\"" : name, type, constraint, Optional.empty(), sourceName);
        }

        public ColumnDefinition(String name, Type type, ColumnConstraint constraint, ForeignKey foreignKey, String sourceName) {
            this(name, type, constraint, Optional.of(foreignKey), sourceName);
        }


        @Override
        public String toString() {
            return name + ' ' + type.toString() + ' ' + constraint.toString();
        }

    }

    private static final String TRACKS = "tracks";
    private static final String ALBUMS = "albums";

    private static final Map<String, ColumnDefinition[]> TABLE_DEFINITIONS = new LinkedHashMap() {
        {
            put(ALBUMS, new ColumnDefinition[]{
                new ColumnDefinition("title", new Type(Primitive.VARCHAR, 255), ColumnConstraint.NOT_NULL, "Name"),
                new ColumnDefinition("artist", new Type(Primitive.VARCHAR, 255), ColumnConstraint.NULLABLE, "Artist")
        });
            put(TRACKS, new ColumnDefinition[]{
                new ColumnDefinition("title", new Type(Primitive.VARCHAR, 255), ColumnConstraint.NOT_NULL, "Name"),
                new ColumnDefinition("artist", new Type(Primitive.VARCHAR, 255), ColumnConstraint.NULLABLE, "Artist"),
                new ColumnDefinition("release_year", new Type(Primitive.INTEGER), ColumnConstraint.NULLABLE, "Year"),
                new ColumnDefinition("duration_millis", new Type(Primitive.INTEGER), ColumnConstraint.NOT_NULL, "Total Time"),
                new ColumnDefinition("play_count", new Type(Primitive.BIGINT), ColumnConstraint.NOT_NULL, "Play Count"),
                new ColumnDefinition("play_date", new Type(Primitive.TIMESTAMP), ColumnConstraint.NULLABLE, "Play Date UTC"),
                new ColumnDefinition("album_id", new Type(Primitive.BIGINT), ColumnConstraint.NULLABLE, new ForeignKey(ALBUMS, "id"), "Album Id"),
                new ColumnDefinition("disc_number", new Type(Primitive.INTEGER), ColumnConstraint.NULLABLE, "Disc Number"),
                new ColumnDefinition("track_number", new Type(Primitive.INTEGER), ColumnConstraint.NULLABLE, "Track Number")
            });
        }
    };
}
