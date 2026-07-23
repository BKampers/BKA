package bka.theworks;

import bka.theworks.persistence.*;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.text.*;
import java.time.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;


/**
 */
public final class Demo {

    public static void main(String[] args) throws DatabaseException, IOException, ParserConfigurationException, SAXException {
        System.out.println(args.length);
        for (int i = 0; i < args.length; ++i) {
            System.out.printf("arg %d: ```%s```%n", i, args[i]);
        }
        populateDatabase();
        List<Map<String, Object>> records;
        String sql = getQuery(args);
        Files.writeString(Paths.get("latest.sql"), sql);
        records = Database.query(sql);
        Map<String, Integer> widths = computeColumnsWidths(records);
        widths.forEach((key, value) -> System.out.printf(columnFormat(value), key));
        System.out.println();
        printHeader(widths);
        printRecords(records, widths);
        System.out.printf("Total of %d rows listed%n", records.size());
        Database.shutdown();
    }

    private static void populateDatabase() throws DatabaseException, SAXException, IOException, ParserConfigurationException {
        LibraryLoader libraryLoader = new LibraryLoader("/Users/bartkampers/iCloud Drive/Bibliotheek.xml");
        Map<String, Map<String, AlbumEntity>> albumEntities = new HashMap<>();
        System.out.println("Loading tracks... ");
        Collection<Map<String, Object>> tracks = libraryLoader.getTracks();
        Map<AlbumEntity, List<Map<String, Object>>> albumTracks = new HashMap<>();
        for (Map<String, Object> track : tracks) {
            if (!track.containsKey("Play Count")) {
                track = new HashMap<>(track);
                track.put("PlayCount", BigInteger.ZERO);
            }
            String albumTitle = (String) track.get("Album");
            if (albumTitle != null) {
                String albumArtist = (Boolean.TRUE.equals(track.get("Compilation")))
                    ? null
                    : (String) (track.containsKey("Album Artist") ? track.get("Album Artist") : track.get("Artist"));
                AlbumEntity albumEntity = albumEntities.computeIfAbsent(
                    albumArtist,
                    artist -> new HashMap<>()).computeIfAbsent(albumTitle, title -> new AlbumEntity(albumTitle, albumArtist));
                albumTracks.computeIfAbsent(albumEntity, entity -> new ArrayList<>()).add(track);

            }
            else {
                Database.storeTrack(track);
            }
        }
        for (Map<String, AlbumEntity> albumMap : albumEntities.values()) {
            for (AlbumEntity entity : albumMap.values()) {
                Map<String, Object> album = new HashMap<>();
                album.put("Name", entity.title());
                album.put("Artist", entity.artist());
                album.put("tracks", albumTracks.get(entity));
                Database.storeAlbum(album);
            }
        }
    }

    private static String getQuery(String[] arguments) throws IOException {
        if (arguments.length == 0) {
            return DEFAULT_QUERY;
        }
        StringBuilder query = new StringBuilder(statement(arguments[0]));
        Arrays.stream(arguments).skip(1).forEach(argument -> substitureArgument(query, argument));
        return query.toString();
    }

    private static String statement(String nameValuePair) throws IOException {
        Argument argument = Argument.of(nameValuePair);
        return switch (argument.name()) {
            case "sql" ->
                fixQuotes(argument.value());
            case "file" ->
                new String(Files.readAllBytes(Paths.get(argument.value())));
            default ->
                throw new IllegalArgumentException("Unsupported source: " + argument.name());
        };
    }

    private static void substitureArgument(StringBuilder query, String argument) throws IllegalArgumentException {
        if (argument.contains("=")) {
            substituteNamedArgument(query, argument);
        }
        else {
            substituteUnnamedArgument(query, argument);
        }
    }

    private static void substituteNamedArgument(StringBuilder query, String nameValuePair) throws IllegalArgumentException {
        Argument argument = Argument.of(nameValuePair);
        switch (argument.name()) {
            case "where" ->
                replace(query, target(argument), "WHERE", fixQuotes(argument.value()));
            case "order" ->
                replace(query, target(argument), "ORDER BY", argument.value());
            default ->
                throw new IllegalArgumentException("Unsupported argument: " + argument.name());
        }
    }

    private static String target(Argument argument) {
        return "$" + argument.name();
    }

    private static void substituteUnnamedArgument(StringBuilder query, String argument) throws IllegalArgumentException {
        replace(query, "?", fixQuotes(argument));
    }

    private static void replace(StringBuilder builder, String target, String keyword, String argument) {
        if (argument.isBlank()) {
            replace(builder, target, "");
        }
        else {
            replace(builder, target, keyword + ' ' + argument);
        }
    }

    private static void replace(StringBuilder builder, String target, String replacement) {
        int index = builder.indexOf(target);
        if (index < 0) {
            throw new IllegalArgumentException(String.format("'%s' not found in '%s'", target, builder));
        }
        builder.replace(index, index + target.length(), replacement);
    }

    private static String fixQuotes(String string) {
        return string.replace('\\', '\'');
    }

    private static Map<String, Integer> computeColumnsWidths(List<Map<String, Object>> records) {
        Map<String, Integer> widths = new LinkedHashMap<>();
        records.forEach((Map<String, Object> record) -> {
            record.entrySet().forEach((Map.Entry<String, Object> entry) -> {
                int length = displayValue(entry.getKey(), entry.getValue()).length();
                Integer max = widths.get(entry.getKey());
                widths.put(entry.getKey(), (max == null) ? Math.max(length, entry.getKey().length()) : Math.max(length, max));
            });
        });
        return widths;
    }

    private static void printHeader(Map<String, Integer> widths) {
        int totalWidth = widths.values().stream().mapToInt(i -> i).sum() + widths.size() * 3 - 1;
        if (totalWidth > 0) {
            System.out.println("-".repeat(totalWidth));
        }
    }

    private static void printRecords(List<Map<String, Object>> records, Map<String, Integer> widths) {
        records.forEach((Map<String, Object> record) -> {
            StringBuilder line = new StringBuilder();
            record.entrySet().forEach((Map.Entry<String, Object> entry) -> {
                line.append(String.format(columnFormat(widths.get(entry.getKey())), displayValue(entry.getKey(), entry.getValue())));
            });
            System.out.println(line.toString());
        });
    }

    private static String columnFormat(int width) {
        return "%-" + width + "s | ";
    }

    private static String displayValue(String key, Object object) {
        if (key.contains("DURATION_MILLIS")) {
            return durationDisplayValue(object);
        }
        Optional<String> widthFormat = findWidthFormat(key);
        if (widthFormat.isPresent()) {
            return String.format(widthFormat.get(), (Number) object);
        }
        return displayValue(object);
    }

    private static String durationDisplayValue(Object object) {
        Duration duration = Duration.ofMillis(((Number) object).longValue());
        long hours = duration.toHours();
        if (hours != 0) {
            return String.format("%d:%02d'%02d\"", hours, duration.toMinutesPart(), duration.toSecondsPart());
        }
        return String.format("%4d'%02d\"", duration.toMinutes(), duration.toSecondsPart());
    }

    private static Optional<String> findWidthFormat(String key) {
        String lowerCaseKey = key.toLowerCase();
        return COLUMN_WIDTHS.entrySet().stream()
            .filter(entry -> lowerCaseKey.contains(entry.getKey()))
            .map(entry -> String.format("%%%dd", entry.getValue()))
            .findAny();
    }

    private static String displayValue(Object object) {
        if (object instanceof java.sql.Timestamp timestamp) {
            return DISPLAY_DATE_FORMAT.format(timestamp);
        }
        return Objects.toString(object);
    }

    private record AlbumEntity(String title, String artist) {

    }

    private record Argument(String name, String value) {

        static Argument of(String argument) {
            String[] array = argument.split("=", 2);
            return new Argument(array[0], array[1]);
        }

    }

    private static final Map<String, Integer> COLUMN_WIDTHS = Map.of(
        "count", 4,
        "id", 5,
        "number", 4);

    private static final String DEFAULT_QUERY = """
        SELECT albums.artist AS "Artist", albums.title AS "Title", MAX(tracks.release_year) AS "year", MAX(tracks.play_count) AS "played", MAX(tracks.play_date) AS latest_play_date, SUM(tracks.duration_millis)
        FROM tracks
        JOIN albums ON tracks.album_id = albums.id
        GROUP BY tracks.album_id
        ORDER BY latest_play_date
    """;

    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
