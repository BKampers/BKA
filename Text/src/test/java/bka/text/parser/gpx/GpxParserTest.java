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
        assertEquals("Apple iOS", gpx.getCreator());

        Metadata metadata = gpx.getMetadata().orElseThrow();
        assertEquals(Optional.of("Morning Walk"), metadata.getName());
        assertEquals(Instant.parse("2026-07-17T08:00:00Z"), metadata.getTime().orElseThrow());
        assertEquals(Optional.of("walk, icloud"), metadata.getKeywords());

        Person author = metadata.getAuthor().orElseThrow();
        assertEquals(Optional.of("Bart"), author.getName());
        assertEquals("bart@example.com", author.getEmail().orElseThrow().getAddress());
        assertEquals("https://example.com/bart", author.getLink().orElseThrow().getHref());

        Copyright copyright = metadata.getCopyright().orElseThrow();
        assertEquals("Bart Kampers", copyright.getAuthor());
        assertEquals(Year.of(2026), copyright.getYear().orElseThrow());

        Bounds bounds = metadata.getBounds().orElseThrow();
        assertEquals(52.0900, bounds.getMinLatitude(), 1e-9);
        assertEquals(5.1300, bounds.getMaxLongitude(), 1e-9);

        assertEquals(1, gpx.getWaypoints().size());
        Waypoint start = gpx.getWaypoints().get(0);
        assertEquals(52.0907, start.getLatitude(), 1e-9);
        assertEquals(5.1214, start.getLongitude(), 1e-9);
        assertEquals(4.5, start.getElevation().orElseThrow(), 1e-9);
        assertEquals(Optional.of("Start"), start.getName());
        assertEquals(Fix.THREE_D, start.getFix().orElseThrow());
        assertEquals(12, start.getSatellites().orElseThrow());

        assertEquals(1, gpx.getRoutes().size());
        Route route = gpx.getRoutes().get(0);
        assertEquals(Optional.of("Short route"), route.getName());
        assertEquals(1, route.getNumber().orElseThrow());
        assertEquals(2, route.getPoints().size());

        assertEquals(1, gpx.getTracks().size());
        Track track = gpx.getTracks().get(0);
        assertEquals(Optional.of("Morning Walk"), track.getName());
        assertEquals(1, track.getSegments().size());

        List<Waypoint> points = track.getSegments().get(0).getPoints();
        assertEquals(3, points.size());
        assertEquals(Instant.parse("2026-07-17T08:00:00Z"), points.get(0).getTime().orElseThrow());
        assertEquals(Instant.parse("2026-07-17T06:01:00Z"), points.get(1).getTime().orElseThrow());

        List<Extension> extensions = points.get(0).getExtensions();
        assertEquals(1, extensions.size());
        Extension trackPointExtension = extensions.get(0);
        assertEquals("http://www.garmin.com/xmlschemas/TrackPointExtension/v1", trackPointExtension.getUri());
        assertEquals("TrackPointExtension", trackPointExtension.getLocalName());
        Extension heartRate = trackPointExtension.getChildren().get(0);
        assertEquals("hr", heartRate.getLocalName());
        assertEquals("98", heartRate.getCharacters().strip());
    }

}
