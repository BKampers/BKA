/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.text.parser.gpx;

import bka.text.parser.sax.*;
import gpx.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import org.xml.sax.*;


/**
 * Converts GPX 1.1 {@link XmlElement}s into immutable domain objects.
 *
 * <p>Designed for use with {@link SaxStackHandler}. Elements in the GPX namespace are mapped to the
 * corresponding types in {@link gpx}; foreign-namespace content becomes {@link Extension}.
 */
public final class GpxConverter implements Function<XmlElement, Object> {

    public static final String NAMESPACE = "http://www.topografix.com/GPX/1/1";

    @Override
    public Object apply(XmlElement element) {
        if (NAMESPACE.equals(element.getUri())) {
            return convertGpxElement(element);
        }
        return convertExtension(element);
    }

    private static Object convertGpxElement(XmlElement element) {
        return switch (element.getLocalName()) {
            case "gpx" -> createGpx(element);
            case "metadata" -> createMetadata(element);
            case "wpt", "rtept", "trkpt" -> createWaypoint(element);
            case "rte" -> createRoute(element);
            case "trk" -> createTrack(element);
            case "trkseg" -> createTrackSegment(element);
            case "link" -> createLink(element);
            case "email" -> createEmail(element);
            case "author" -> createPerson(element);
            case "copyright" -> createCopyright(element);
            case "bounds" -> createBounds(element);
            case "extensions" -> extensionElements(element.getChildren());
            case "pt" -> createPoint(element);
            case "ptseg" -> createPointSegment(element);
            case "name", "desc", "cmt", "src", "type", "sym", "keywords", "text", "license" -> element.getCharacters().strip();
            case "year" -> Year.parse(element.getCharacters().strip());
            case "time" -> parseTime(element.getCharacters());
            case "ele", "magvar", "geoidheight", "hdop", "vdop", "pdop", "ageofdgpsdata" -> Double.valueOf(element.getCharacters().strip());
            case "sat", "number", "dgpsid" -> Integer.valueOf(element.getCharacters().strip());
            case "fix" -> Fix.fromValue(element.getCharacters().strip());
            default -> throw new IllegalArgumentException("Unsupported GPX element: " + element.getLocalName());
        };
    }

    private static Extension convertExtension(XmlElement element) {
        return new Extension(
            element.getUri(),
            element.getLocalName(),
            element.getCharacters(),
            extensionElements(element.getChildren())
        );
    }

    private static Gpx createGpx(XmlElement element) {
        return new Gpx(
            requiredAttribute(element, "version"),
            requiredAttribute(element, "creator"),
            optionalChild(element, "metadata"),
            children(element, "wpt"),
            children(element, "rte"),
            children(element, "trk"),
            extensions(element)
        );
    }

    private static Metadata createMetadata(XmlElement element) {
        return new Metadata(
            optionalChild(element, "name"),
            optionalChild(element, "desc"),
            optionalChild(element, "author"),
            optionalChild(element, "copyright"),
            children(element, "link"),
            optionalChild(element, "time"),
            optionalChild(element, "keywords"),
            optionalChild(element, "bounds"),
            extensions(element)
        );
    }

    private static Waypoint createWaypoint(XmlElement element) {
        return new Waypoint(
            Double.parseDouble(requiredAttribute(element, "lat")),
            Double.parseDouble(requiredAttribute(element, "lon")),
            optionalDouble(element, "ele"),
            optionalChild(element, "time"),
            optionalDouble(element, "magvar"),
            optionalDouble(element, "geoidheight"),
            optionalChild(element, "name"),
            optionalChild(element, "cmt"),
            optionalChild(element, "desc"),
            optionalChild(element, "src"),
            children(element, "link"),
            optionalChild(element, "sym"),
            optionalChild(element, "type"),
            optionalChild(element, "fix"),
            optionalInt(element, "sat"),
            optionalDouble(element, "hdop"),
            optionalDouble(element, "vdop"),
            optionalDouble(element, "pdop"),
            optionalDouble(element, "ageofdgpsdata"),
            optionalInt(element, "dgpsid"),
            extensions(element)
        );
    }

    private static Route createRoute(XmlElement element) {
        return new Route(
            optionalChild(element, "name"),
            optionalChild(element, "cmt"),
            optionalChild(element, "desc"),
            optionalChild(element, "src"),
            children(element, "link"),
            optionalInt(element, "number"),
            optionalChild(element, "type"),
            extensions(element),
            children(element, "rtept")
        );
    }

    private static Track createTrack(XmlElement element) {
        return new Track(
            optionalChild(element, "name"),
            optionalChild(element, "cmt"),
            optionalChild(element, "desc"),
            optionalChild(element, "src"),
            children(element, "link"),
            optionalInt(element, "number"),
            optionalChild(element, "type"),
            extensions(element),
            children(element, "trkseg")
        );
    }

    private static TrackSegment createTrackSegment(XmlElement element) {
        return new TrackSegment(
            children(element, "trkpt"),
            extensions(element)
        );
    }

    private static Link createLink(XmlElement element) {
        return new Link(
            requiredAttribute(element, "href"),
            optionalChild(element, "text"),
            optionalChild(element, "type")
        );
    }

    private static Email createEmail(XmlElement element) {
        return new Email(
            requiredAttribute(element, "id"),
            requiredAttribute(element, "domain")
        );
    }

    private static Person createPerson(XmlElement element) {
        return new Person(
            optionalChild(element, "name"),
            optionalChild(element, "email"),
            optionalChild(element, "link")
        );
    }

    private static Copyright createCopyright(XmlElement element) {
        return new Copyright(
            requiredAttribute(element, "author"),
            optionalChild(element, "year"),
            optionalChild(element, "license")
        );
    }

    private static Bounds createBounds(XmlElement element) {
        return new Bounds(
            Double.parseDouble(requiredAttribute(element, "minlat")),
            Double.parseDouble(requiredAttribute(element, "minlon")),
            Double.parseDouble(requiredAttribute(element, "maxlat")),
            Double.parseDouble(requiredAttribute(element, "maxlon"))
        );
    }

    private static Point createPoint(XmlElement element) {
        return new Point(
            Double.parseDouble(requiredAttribute(element, "lat")),
            Double.parseDouble(requiredAttribute(element, "lon")),
            optionalDouble(element, "ele"),
            optionalChild(element, "time")
        );
    }

    private static PointSegment createPointSegment(XmlElement element) {
        return new PointSegment(children(element, "pt"));
    }

    private static Instant parseTime(String characters) {
        String text = characters.strip();
        try {
            return Instant.parse(text);
        }
        catch (DateTimeException exception) {
            return OffsetDateTime.parse(text).toInstant();
        }
    }

    private static String requiredAttribute(XmlElement element, String name) {
        Attributes attributes = element.getAttributes();
        String value = attributes.getValue(name);
        if (value == null) {
            value = attributes.getValue("", name);
        }
        if (value == null) {
            throw new IllegalArgumentException("Missing attribute '" + name + "' on " + element.getLocalName());
        }
        return value;
    }

    private static <T> Optional<T> optionalChild(XmlElement element, String localName) {
        return element.<T>getChildren(NAMESPACE, localName).stream().findFirst();
    }

    private static <T> List<T> children(XmlElement element, String localName) {
        return element.getChildren(NAMESPACE, localName);
    }

    private static List<Extension> extensions(XmlElement element) {
        Optional<List<Extension>> extensions = optionalChild(element, "extensions");
        return extensions.orElseGet(List::of);
    }

    private static List<Extension> extensionElements(List<Object> children) {
        return children.stream().map(GpxConverter::requireExtension).toList();
    }

    private static Extension requireExtension(Object child) {
        if (child instanceof Extension extension) {
            return extension;
        }
        throw new IllegalArgumentException(
            "Extensions may only contain foreign-namespace elements, got: "
                + (child == null ? "null" : child.getClass().getName())
        );
    }

    private static OptionalInt optionalInt(XmlElement element, String localName) {
        return element.<Integer>getChildren(NAMESPACE, localName).stream()
            .findFirst()
            .map(OptionalInt::of)
            .orElseGet(OptionalInt::empty);
    }

    private static OptionalDouble optionalDouble(XmlElement element, String localName) {
        return element.<Double>getChildren(NAMESPACE, localName).stream()
            .findFirst()
            .map(OptionalDouble::of)
            .orElseGet(OptionalDouble::empty);
    }

}
