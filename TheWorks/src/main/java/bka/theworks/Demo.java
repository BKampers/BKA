package bka.theworks;

import bka.theworks.persistence.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;


/**
 */
public final class Demo {

    public static void main(String[] args) throws DatabaseException, IOException, ParserConfigurationException, SAXException {
        System.out.println(args.length);
        for (int i = 0; i < args.length; ++i) {
            System.out.printf("arg %d: ```%s```%n", i, args[i]);
        }
        populateDatabase();
        String sql = getQuery(args);
        Files.writeString(Paths.get("latest.sql"), sql);
        List<Map<String, Object>> records = Database.query(sql);
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
        Map<String, Map<String, AlbumId>> albumEntities = new HashMap<>();
        System.out.println("Loading tracks... ");
        Collection<Map<String, Object>> tracks = libraryLoader.getTracks();
        Map<AlbumId, List<Map<String, Object>>> albumTracks = new HashMap<>();
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
                AlbumId albumId = albumEntities.computeIfAbsent(
                    albumArtist,
                    artist -> new HashMap<>()).computeIfAbsent(albumTitle, title -> new AlbumId(albumTitle, albumArtist));
                albumTracks.computeIfAbsent(albumId, id -> new ArrayList<>()).add(track);

            }
            else {
                Database.storeTrack(track);
            }
        }
        for (Map<String, AlbumId> albumMap : albumEntities.values()) {
            for (AlbumId entity : albumMap.values()) {
                Map<String, Object> album = new HashMap<>();
                album.put("Name", entity.title());
                album.put("Artist", entity.artist());
                album.put("tracks", albumTracks.get(entity));
                Database.storeAlbum(album);
            }
        }
    }

    private static String getQuery(String[] args) throws IOException {
        if (args.length == 1 && args[0].contains("=")) {
            int index = args[0].indexOf('=');
            String argument = args[0].substring(0, index).trim();
            if ("sql".equals(argument)) {
                return args[0].substring(index + 1).replace('\\', '\'');
            }
            if ("file".equals(argument)) {
                return new String(Files.readAllBytes(Paths.get(args[0].substring(index + 1))));
            }
        }
        return DEFAULT_QUERY;
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
            Duration duration = Duration.ofMillis(((Number) object).longValue());
            long hours = duration.toHours();
            if (hours != 0) {
                return String.format("%d:%02d'%02d\"", hours, duration.toMinutesPart(), duration.toSecondsPart());
            }
            return String.format("%4d'%02d\"", duration.toMinutes(), duration.toSecondsPart());
        }
        return displayValue(object);
    }

    private static String displayValue(Object object) {
        if (object instanceof java.sql.Timestamp timestamp) {
            return DISPLAY_DATE_FORMAT.format(timestamp);
        }
        return (object == null) ? "NULL" : object.toString();
    }

    private record AlbumId(String title, String artist) {

    }

    private static final String DEFAULT_QUERY = """
        SELECT albums.artist AS "Artist", albums.title AS "Title", MAX(tracks.release_year) AS "year", MAX(tracks.play_count) AS "played", MAX(tracks.play_date) AS latest_play_date, SUM(tracks.duration_millis)
        FROM tracks
        JOIN albums ON tracks.album_id = albums.id
        GROUP BY tracks.album_id
        ORDER BY latest_play_date
    """;
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
