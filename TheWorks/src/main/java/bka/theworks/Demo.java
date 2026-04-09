package bka.theworks;

import bka.theworks.persistence.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
        LibraryLoader libraryLoader = new LibraryLoader("/Users/bartkampers/iCloud Drive/Bibliotheek.xml");
        Map<Optional<String>, Map<String, AlbumId>> albumEntities = new HashMap<>();
            System.out.println("Loading tracks... ");
            Collection<Map<String, Object>> tracks = libraryLoader.getTracks();
        Map<AlbumId, List<Map<String, Object>>> albumTracks = new HashMap<>();
            for (Map<String, Object> track : tracks) {
                String albumTitle = (String) track.get("Album");
                if (albumTitle != null) {
                    Optional<String> albumArtist = (Boolean.TRUE.equals(track.get("Compilation")))
                        ? Optional.empty()
                        : Optional.ofNullable((String) (track.containsKey("Album Artist") ? track.get("Album Artist") : track.get("Artist")));
                    AlbumId albumId = albumEntities.computeIfAbsent(albumArtist, artist -> new HashMap<>()).computeIfAbsent(albumTitle, title -> new AlbumId(albumTitle, albumArtist));
                    albumTracks.computeIfAbsent(albumId, e -> new ArrayList<>()).add(track);

                }
                else {
                    Database.storeTrack(track);
                }
            }
        for (Map<String, AlbumId> albumMap : albumEntities.values()) {
            for (AlbumId entity : albumMap.values()) {
                    Map<String, Object> album = new HashMap<>();
                    album.put("Name", entity.title());
                    album.put("Artist", entity.artist().orElse(null));
                    album.put("tracks", albumTracks.get(entity));
                    Database.storeAlbum(album);
                }
            }
        String sql = getQuery(args);
            System.out.println("sql = " + sql);
            List<Map<String, Object>> queryTracks = Database.query(sql);
            Map<String, Integer> widths = new LinkedHashMap<>();
            queryTracks.forEach((Map<String, Object> record) -> {
                record.entrySet().forEach((Map.Entry<String, Object> entry) -> {
                    int length = displayValue(entry.getKey(), entry.getValue()).length();
                    Integer max = widths.get(entry.getKey());
                    widths.put(entry.getKey(), (max == null) ? Math.max(length, entry.getKey().length()) : Math.max(length, max));
                });
            });
            widths.forEach((key, value) -> System.out.printf(columnFormat(value), key));
            System.out.println();
            int totalWidth = widths.values().stream().mapToInt(i -> i).sum() + widths.size() * 3 - 1;
            if (totalWidth > 0) {
                System.out.println("-".repeat(totalWidth));
            }
            queryTracks.forEach((Map<String, Object> record) -> {
                StringBuilder line = new StringBuilder();
                record.entrySet().forEach((Map.Entry<String, Object> entry) -> {
                    line.append(String.format(columnFormat(widths.get(entry.getKey())), displayValue(entry.getKey(), entry.getValue())));
                });
                System.out.println(line.toString());
            });
            System.out.printf("Total of %d rows listed%n", queryTracks.size());
//        }
        Database.shutdown();
    }

    public static String getQuery(String[] args) {
        if (args.length == 1 && args[0].contains("=")) {
            int index = args[0].indexOf('=');
            if ("sql".equals(args[0].substring(0, index).trim())) {
                return args[0].substring(index + 1).replaceAll("(?i)year", "\"year\"").replace('\\', '\'');
            }
        }
        return """
            SELECT albums.artist, albums.title, MAX(tracks.play_date) AS latest_play_date, SUM(tracks.duration_seconds)
            FROM tracks
            JOIN albums ON tracks.album_id = albums.id
            GROUP BY tracks.album_id
            ORDER BY latest_play_date
        """;
    }

    public static String columnFormat(int width) {
        return "%-" + width + "s | ";
    }

    private static String displayValue(String key, Object object) {
        if (key.contains("DURATION_SECONDS")) {
            int seconds = ((Number) object).intValue();
            if (seconds >= 3600) {
                return String.format("%d:%02d'%02d\"", seconds / 3600, seconds % 3600 / 60, seconds % 60);
            }
            return String.format("%4d'%02d\"", seconds / 60, seconds % 60);
        }
        return displayValue(object);
    }

    private static String displayValue(Object object) {
        if (object instanceof java.sql.Timestamp timestamp) {
            return DISPLAY_DATE_FORMAT.format(timestamp);
        }
        return (object == null) ? "NULL" : object.toString();
    }

    private record AlbumId(String title, Optional<String> artist) {

    }

    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
