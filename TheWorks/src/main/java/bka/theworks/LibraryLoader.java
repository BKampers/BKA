/*
** © Bart Kampers
*/

package bka.theworks;

import bka.text.parser.sax.*;
import java.io.*;
import java.math.*;
import java.time.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

public class LibraryLoader {

    public LibraryLoader(String uri) {
        this.uri = Objects.requireNonNull(uri);
    }

    public Collection<Map<String, Object>> getTracks() throws ParserConfigurationException, SAXException, IOException {
        PListSaxHandler handler = new PListSaxHandler();
        SAXParserFactory.newInstance().newSAXParser().parse("/Users/bartkampers/iCloud Drive/Bibliotheek.xml", handler);
        return (Collection<Map<String, Object>>) ((Map) ((Map) handler.getContent().get(0)).get("Tracks")).values();
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        LibraryLoader loader = new LibraryLoader("/Users/bartkampers/iCloud Drive/Bibliotheek.xml");
        loader.getTracks().stream().sorted(trackSorter()).forEach(track -> System.out.printf("%s: %s (%2d) %s\n", albumArtistOf(track), albumOf(track), trackNumberOf(track), track.get("Name")));
    }

    private static Comparator<Map<String, Object>> trackSorter() {
        return (track1, track2) -> {
            int sort = compare(uppercase(albumOf(track1)), uppercase(albumOf(track2)));
            if (sort != 0) {
                return sort;
            }
            sort = compare(discNumberOf(track1), discNumberOf(track2));
            if (sort != 0) {
                return sort;
            }
            return compare(trackNumberOf(track1), trackNumberOf(track2));
        };
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

    private static String albumArtistOf(Map<String, Object> track) {
        return (String) track.getOrDefault("Sort Album Artist", track.getOrDefault("Album Artist", track.getOrDefault("Sort Artist", track.getOrDefault("Artist", null))));
    }

    private static String albumOf(Map<String, Object> track) {
        return (String) track.getOrDefault("Sort Album", track.getOrDefault("Album", null));
    }

    private static ZonedDateTime playDateOf(Map<String, Object> track) {
        return (ZonedDateTime) track.get("Paly Date UTC");
    }

    private static BigInteger trackNumberOf(Map<String, Object> track) {
        return (BigInteger) track.get("Track Number");
    }

    private static Integer discNumberOf(Map<String, Object> track) {
        BigInteger discNumber = (BigInteger) track.get("Disc Number");
        return (discNumber == null) ? null : discNumber.intValue();
    }

    private final String uri;
}
