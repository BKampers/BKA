/*
** © Bart Kampers
*/

package bka.theworks;

import bka.text.parser.sax.*;
import java.io.*;
import java.math.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public final class LibraryLoader {

    public LibraryLoader(String uri) {
        this.uri = Objects.requireNonNull(uri);
    }

    public Collection<Map<String, Object>> getTracks() throws ParserConfigurationException, SAXException, IOException {
        PListSaxHandler handler = new PListSaxHandler();
        SAXParserFactory.newInstance().newSAXParser().parse(uri, handler);
        return (Collection<Map<String, Object>>) ((Map) ((Map) handler.getContent().get(0)).get("Tracks")).values();
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        LibraryLoader loader = new LibraryLoader("/Users/bartkampers/iCloud Drive/Bibliotheek.xml");
        if (args.length == 1 && args[0].contains("=")) {
            final String[] split = args[0].split("=");
            loader.dump(split[0], split[1]);
            return;
        }
        loader.listTop2000();
        loader.listCounts();
        loader.listYears();
        loader.listLatest();
        loader.listLatestOfPlayCount();
        loader.filterTime();
        loader.listShortAlbums();
//        loader.getTracks().stream().filter(track -> nameOf(track).contains("Blood Meridian")).forEach(track -> System.out.println(albumTitleOf(track) + " | " + nameOf(track)));
    }

    private void dump(String field, String value) throws ParserConfigurationException, SAXException, IOException {
        final int dotIndex = field.indexOf(".");
        String table = (dotIndex >= 0) ? field.substring(0, dotIndex) : "track";
        String column = (dotIndex >= 0) ? field.substring(dotIndex + 1) : field;
        System.out.println("Select from " + table + " where " + column + " = " + value);
        switch (table.toLowerCase()) {
            case "track":
                System.out.println("Tracks where " + column + " = " + value);
                getTracks().stream()
                    .filter(track -> value.equals(Objects.toString(track.get(column), "")))
                    .sorted((track1, track2) -> playDateOf(track1).compareTo(playDateOf(track2)))
                    .forEach(LibraryLoader::printTrack);
                break;
            case "album":
                Map<Map<String, Object>, Collection<Map<String, Object>>> albums = albums(getTracks());
                albums.entrySet().stream()
                    .filter(album -> value.equals(Objects.toString(album.getKey().get(column), "")) || Objects.equals(album.getKey().get(column), Optional.of(value)))
                    .sorted((album1, album2) -> album1.getKey().get("Title").toString().compareTo(album2.getKey().get("Title").toString()))
                    .forEach(album -> System.out.printf("%s%n", album.getKey().get("Title")));
                break;
            default:
                throw new IllegalArgumentException("Unknown table: '" + table + '\'');
        }
    }

    private void filterTime() throws ParserConfigurationException, SAXException, IOException {
        final LocalTime ONE_PM = LocalTime.of(13, 0);
        final LocalTime TEN_PM = LocalTime.of(22, 0);
        System.out.println("late / early");
        getTracks().stream()
            .filter(track -> localTime(track).isBefore(ONE_PM) || localTime(track).isAfter(TEN_PM))
            .forEach(LibraryLoader::printTrack);
    }

    public void listTop2000() throws ParserConfigurationException, SAXException, IOException {
        getTracks().stream()
            .filter(track -> playDateOf(track) != null)
            .sorted(LibraryLoader::favoriteCompare)
            .limit(2000)
            .collect(Collectors.groupingBy(track -> albumArtistOf(track) + " | " + albumOf(track), LinkedHashMap::new, Collectors.toList())).values()
            //            .stream().findAny().get()
            .forEach(//track -> System.out.println(albumOrTrackString(track))
                tracks -> System.out.println(albumOrTrackString(tracks.stream().findFirst().get()))
            );
    }

    public void listLatest() throws ParserConfigurationException, SAXException, IOException {
        getTracks().stream()
            .filter(track -> playDateOf(track) != null)
            .map(track -> localDate(playDateOf(track)))
            .distinct()
            .sorted((date1, date2) -> -date1.compareTo(date2))
            .limit(40)
            .forEach(date -> System.out.println(date));
    }

    public void listLatestOfPlayCount() throws ParserConfigurationException, SAXException, IOException {
        getTracks().stream()
            .filter(track -> playDateOf(track) != null)
            .collect(Collectors.groupingBy(track -> playCountOf(track))).values().stream()
            .map(entry -> youngest(entry))
            .sorted((track1, track2) -> playCountOf(track1).compareTo(playCountOf(track2)))
            .forEach(LibraryLoader::printAlbumOrElseTrack);
    }

    private static Map<String, Object> youngest(List<Map<String, Object>> tracks) {
        return tracks.stream().reduce(
            Map.of("Play Date UTC", epoch()),
            (youngest, current) -> (playDateOf(youngest).compareTo(playDateOf(current)) < 0) ? current : youngest);
    }

    public void listShortAlbums() throws ParserConfigurationException, SAXException, IOException {
        albums(getTracks()).forEach((albumId, tracks) -> {
            Duration albumDuration = tracks.stream().map(track -> totalTimeOf(track)).reduce(Duration.ZERO, (total, duration) -> {
                return total.plus(duration);
            });
            if (albumDuration.toMinutes() < 30) {
                System.out.printf("%2d:%02d | %s | %s\n", albumDuration.toMinutesPart(), albumDuration.toSecondsPart(), albumId.get("Title"), ifPresent(albumId.get("Artist")));
            }
        });
    }

    private Object ifPresent(Object object) {
        return ((object instanceof Optional) && (!((Optional) object).isEmpty())) ? ((Optional) object).get() : "-";
    }

    private static ZonedDateTime epoch() {
        return ZonedDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    }

    private static void printTrack(Map<String, Object> track) {
        System.out.printf("(%2d %s) %s | %s | %s\n",
            playCountOf(track),
            local(playDateOf(track)),
            track.get("Artist"),
            track.getOrDefault("Album", "-"),
            track.get("Name"));
    }

    private static void printAlbumOrElseTrack(Map<String, Object> track) {
        System.out.println(albumOrTrackString(track));
    }

    private static String albumOrTrackString(Map<String, Object> track) {
        return String.format("(%2d %s) %s | %s",
            playCountOf(track),
            local(playDateOf(track)),
            albumArtistOf(track),
            track.getOrDefault("Album", "- | " + track.get("Name")));
    }

    private static String local(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return "---";
        }
        return localDateTime(dateTime).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static LocalTime localTime(Map<String, Object> track) {
        return localDateTime(playDateOf(track)).toLocalTime();
    }

    private static LocalDateTime localDateTime(ZonedDateTime dateTime) {
        return LocalDateTime.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
    }

    private static LocalDate localDate(ZonedDateTime dateTime) {
        return LocalDate.ofInstant(dateTime.toInstant(), ZoneId.systemDefault());
    }

    public void listCounts() throws ParserConfigurationException, SAXException, IOException {
        Map<Integer, Integer> map = new TreeMap<>();
        List<Map<String, Object>> list = new ArrayList<>(getTracks());
        list.forEach(track -> {
            int playCount = playCountOf(track).intValue();
            fillKeys(map, playCount);
            increase(map, playCount);
        });
        map.forEach((playCount, count) -> System.out.printf("%2d: %d\n", playCount, count));
    }

    private void listYears() throws ParserConfigurationException, SAXException, IOException {
        Map<Integer, Integer> map = new TreeMap<>();
        Collection<Map<String, Object>> tracks = getTracks();
        tracks.stream()
            .filter(track -> yearOf(track) != null)
            .forEach(track -> increase(map, yearOf(track).intValue() / 10 * 10));
        map.forEach((year, count) -> System.out.printf("%d: %d\n", year, count));
    }

    private static Map<Map<String, Object>, Collection<Map<String, Object>>> albums(Collection<Map<String, Object>> tracks) {
        Map<Map<String, Object>, Collection<Map<String, Object>>> albums = new HashMap<>();
        tracks.stream().filter(track -> albumTitleOf(track) != null).forEach((track) -> {
            Map<String, Object> albumId = Map.of(
                "Title", albumTitleOf(track),
                "Artist", Optional.ofNullable(albumArtistOf(track)),
                "Compilation", isOnCompilation(track));
            albums.computeIfAbsent(albumId, id -> new ArrayList<>()).add(track);
        });
        return albums;
    }

    private static void fillKeys(Map<Integer, Integer> map, int maxKey) {
        if (!map.containsKey(maxKey)) {
            map.put(maxKey, 0);
            if (maxKey > 0) {
                fillKeys(map, maxKey - 1);
            }
        }
    }

    private static <K> void increase(Map<K, Integer> map, K key) {
        map.put(key, map.getOrDefault(key, 0) + 1);
    }

    private static int favoriteCompare(Map<String, Object> track1, Map<String, Object> track2) {
        int sort = -compare(playCountOf(track1), playCountOf(track2));
        if (sort != 0) {
            return sort;
        }
        return -compare(playDateOf(track1), playDateOf(track2));
    }

    private static int trackCompare(Map<String, Object> track1, Map<String, Object> track2) {
        int sort = compare(uppercase(albumOf(track1)), uppercase(albumOf(track2)));
        if (sort != 0) {
            return sort;
        }
        return albumCompare(track1, track2);
    }

    private static int albumCompare(Map<String, Object> track1, Map<String, Object> track2) {
        int sort = compare(discNumberOf(track1), discNumberOf(track2));
        if (sort != 0) {
            return sort;
        }
        return compare(trackNumberOf(track1), trackNumberOf(track2));
    }

    private static String uppercase(String string) {
        return (string == null) ? null : string.toUpperCase();
    }


    private static int compare(Comparable value1, Comparable value2) {
        if (value1 == null) {
            return (value2 == null) ? 0 : -1;
        }
        if (value2 == null) {
            return 1;
        }
        return value1.compareTo(value2);
    }

    private static String nameOf(Map<String, Object> track) {
        return (String) track.get("Name");
    }

    private static String albumTitleOf(Map<String, Object> track) {
        return (String) track.get("Album");
    }

    private static String albumArtistOf(Map<String, Object> track) {
        if (isOnCompilation(track)) {
            return "-";
        }
        return (String) track.getOrDefault("Album Artist", track.getOrDefault("Artist", null));
    }

    private static boolean isOnCompilation(Map<String, Object> track) {
        return Boolean.TRUE.equals(track.get("Compilation"));
    }

    private static String albumOf(Map<String, Object> track) {
        return (String) track.getOrDefault("Sort Album", track.getOrDefault("Album", null));
    }

    private static BigInteger playCountOf(Map<String, Object> track) {
        return (BigInteger) track.getOrDefault("Play Count", BigInteger.ZERO);
    }

    private static ZonedDateTime playDateOf(Map<String, Object> track) {
        return (ZonedDateTime) track.get("Play Date UTC");
    }

    private static BigInteger discNumberOf(Map<String, Object> track) {
        return (BigInteger) track.get("Disc Number");
    }

    private static BigInteger trackNumberOf(Map<String, Object> track) {
        return (BigInteger) track.get("Track Number");
    }

    private static BigInteger yearOf(Map<String, Object> track) {
        return (BigInteger) track.get("Year");
    }

    private static Duration totalTimeOf(Map<String, Object> track) {
        return Duration.of(((BigInteger) track.get("Total Time")).longValue(), ChronoUnit.MILLIS);
    }

    private final String uri;
}
