package bka.text.parser.gpx;

import gpx.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import javax.xml.parsers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.xml.sax.*;


class GpxParserTest {

    @Test
    void parseSampleGpx() throws IOException, SAXException, ParserConfigurationException {
        Gpx gpx = new GpxParser().parse(Path.of("src/test/resources/gpx/sample.gpx"));

        assertEquals("1.1", gpx.getVersion());
        assertEquals("GPS app", gpx.getCreator());

        Metadata metadata = gpx.getMetadata().orElseThrow();
        assertOptional("Morning Walk", metadata.getName());
        assertOptional(Instant.parse("2026-07-17T08:00:00Z"), metadata.getTime());
        assertOptional("walk, icloud", metadata.getKeywords());

        Person author = metadata.getAuthor().orElseThrow();
        assertOptional("Walker", author.getName());
        assertEquals("user@example.com", author.getEmail().orElseThrow().getAddress());
        assertEquals("https://example.com/home", author.getLink().orElseThrow().getHref());

        Copyright copyright = metadata.getCopyright().orElseThrow();
        assertEquals("Owner", copyright.getAuthor());
        assertOptional(Year.of(2026), copyright.getYear());

        Bounds bounds = metadata.getBounds().orElseThrow();
        assertEquals(52.0900, bounds.getMinLatitude(), PRECISION);
        assertEquals(5.1300, bounds.getMaxLongitude(), PRECISION);

        assertEquals(1, gpx.getWaypoints().size());
        Waypoint start = gpx.getWaypoints().getFirst();
        assertEquals(52.0907, start.getLatitude(), PRECISION);
        assertEquals(5.1214, start.getLongitude(), PRECISION);
        assertOptional(4.5, start.getElevation());
        assertOptional("Start", start.getName());
        assertOptional(Fix.THREE_D, start.getFix());
        assertOptional(12, start.getSatellites());

        assertEquals(1, gpx.getRoutes().size());
        Route route = gpx.getRoutes().get(0);
        assertOptional("Short route", route.getName());
        assertOptional(1, route.getNumber());
        assertEquals(2, route.getPoints().size());

        assertEquals(1, gpx.getTracks().size());
        Track track = gpx.getTracks().get(0);
        assertOptional("Morning Walk", track.getName());
        assertEquals(1, track.getSegments().size());

        List<Waypoint> points = track.getSegments().get(0).getPoints();
        assertEquals(3, points.size());
        assertOptional(Instant.parse("2026-07-17T08:00:00Z"), points.get(0).getTime());
        assertOptional(Instant.parse("2026-07-17T06:01:00Z"), points.get(1).getTime());

        List<Extension> extensions = points.get(0).getExtensions();
        assertEquals(1, extensions.size());
        Extension trackPointExtension = extensions.get(0);
        assertEquals("http://www.garmin.com/xmlschemas/TrackPointExtension/v1", trackPointExtension.getUri());
        assertEquals("TrackPointExtension", trackPointExtension.getLocalName());
        Extension heartRate = trackPointExtension.getChildren().getFirst();
        assertEquals("hr", heartRate.getLocalName());
        assertEquals("98", heartRate.getCharacters().strip());
    }

    @Test
    void parseMinimalGpxWithEmptyOptionals() throws IOException, SAXException, ParserConfigurationException {
        Gpx gpx = new GpxParser().parse(Path.of("src/test/resources/gpx/minimal.gpx"));

        assertEquals("1.1", gpx.getVersion());
        assertEquals("minimal", gpx.getCreator());
        assertEmpty(gpx.getMetadata());
        assertTrue(gpx.getWaypoints().isEmpty());
        assertTrue(gpx.getRoutes().isEmpty());
        assertTrue(gpx.getExtensions().isEmpty());

        assertEquals(1, gpx.getTracks().size());
        Track track = gpx.getTracks().getFirst();
        assertEmpty(track.getName());
        assertEmpty(track.getComment());
        assertEmpty(track.getDescription());
        assertEmpty(track.getSource());
        assertTrue(track.getLinks().isEmpty());
        assertEmpty(track.getNumber());
        assertEmpty(track.getType());
        assertTrue(track.getExtensions().isEmpty());

        assertEquals(1, track.getSegments().size());
        TrackSegment segment = track.getSegments().getFirst();
        assertTrue(segment.getExtensions().isEmpty());

        assertEquals(1, segment.getPoints().size());
        Waypoint point = segment.getPoints().getFirst();
        assertEquals(52.0, point.getLatitude(), PRECISION);
        assertEquals(5.0, point.getLongitude(), PRECISION);
        assertEmpty(point.getElevation());
        assertEmpty(point.getTime());
        assertEmpty(point.getMagneticVariation());
        assertEmpty(point.getGeoidHeight());
        assertEmpty(point.getName());
        assertEmpty(point.getComment());
        assertEmpty(point.getDescription());
        assertEmpty(point.getSource());
        assertTrue(point.getLinks().isEmpty());
        assertEmpty(point.getSymbol());
        assertEmpty(point.getType());
        assertEmpty(point.getFix());
        assertEmpty(point.getSatellites());
        assertEmpty(point.getHdop());
        assertEmpty(point.getVdop());
        assertEmpty(point.getPdop());
        assertEmpty(point.getAgeOfDgpsData());
        assertEmpty(point.getDgpsId());
        assertTrue(point.getExtensions().isEmpty());
    }

    private static <T> void assertOptional(T expected, Optional<T> actual) {
        assertEquals(expected, actual.orElseThrow());
    }

    private static void assertOptional(int expected, OptionalInt actual) {
        assertEquals(expected, actual.getAsInt());
    }

    private static void assertOptional(double expected, OptionalDouble actual) {
        assertEquals(expected, actual.getAsDouble(), PRECISION);
    }

    private static void assertEmpty(Optional<?> actual) {
        assertTrue(actual.isEmpty());
    }

    private static void assertEmpty(OptionalInt actual) {
        assertTrue(actual.isEmpty());
    }

    private static void assertEmpty(OptionalDouble actual) {
        assertTrue(actual.isEmpty());
    }

    private static final double PRECISION = 1e-9;
}
