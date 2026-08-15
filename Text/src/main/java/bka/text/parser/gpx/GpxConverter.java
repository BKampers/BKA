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
import org.xml.sax.*;


/**
 * Converts GPX 1.1 {@link XmlElement}s into immutable domain objects.
 *
 * <p>Designed for use with {@link SaxStackHandler}. Elements in the GPX namespace are mapped to the
 * corresponding types in {@link gpx}; foreign-namespace content becomes {@link Extension}.
 */
public final class GpxConverter implements SaxElementConverter {

    public static final String NAMESPACE = "http://www.topografix.com/GPX/1/1";

    @Override
    public Object convert(Element element) throws SAXException {
        if (!NAMESPACE.equals(element.getUri())) {
            return convertExtension(element);
        }
        return convertGpxElement(element);
    }

    private static Object convertGpxElement(XmlElement element) throws SAXException {
        return switch (element.getLocalName()) {
            case GPX_ROOT -> createGpx(element);
            case METADATA -> createMetadata(element);
            case WAYPOINT, ROUTE_POINT, TRACK_POINT -> createWaypoint(element);
            case ROUTE -> createRoute(element);
            case TRACK -> createTrack(element);
            case TRACK_SEGMENT -> createTrackSegment(element);
            case LINK -> createLink(element);
            case EMAIL -> createEmail(element);
            case AUTHOR -> createPerson(element);
            case COPYRIGHT -> createCopyright(element);
            case BOUNDS -> createBounds(element);
            case EXTENSIONS -> extensionElements(element);
            case POINT -> createPoint(element);
            case POINT_SEGMENT -> List.copyOf(children(element, POINT));
            case NAME, DESCRIPTION, COMMENT, SOURCE, TYPE, SYMBOL, KEYWORDS, TEXT, LICENSE -> element.getCharacters().strip();
            case YEAR -> Year.parse(element.getCharacters().strip());
            case TIME -> parseTime(element);
            case ELEVATION, MAGNETIC_VARIATION, GEOID_HEIGHT, HDOP, VDOP, PDOP, AGE_OF_DGPS_DATA -> parseDouble(element);
            case SATELLITES, NUMBER, DGPS_ID -> parseInteger(element);
            case FIX -> Fix.fromValue(element.getCharacters().strip());
            default -> throw new SAXException("Unsupported GPX element: " + element.getLocalName());
        };
    }

    private static Extension convertExtension(XmlElement element) throws SAXException {
        return new Extension(
            element.getUri(),
            element.getLocalName(),
            element.getCharacters(),
            extensionElements(element)
        );
    }

    private static Gpx createGpx(XmlElement element) throws SAXException {
        return new Gpx(
            requiredAttribute(element, VERSION),
            requiredAttribute(element, CREATOR),
            optionalChild(element, METADATA),
            children(element, WAYPOINT),
            children(element, ROUTE),
            children(element, TRACK),
            extensions(element)
        );
    }

    private static Metadata createMetadata(XmlElement element) {
        return new Metadata(
            optionalChild(element, NAME),
            optionalChild(element, DESCRIPTION),
            optionalChild(element, AUTHOR),
            optionalChild(element, COPYRIGHT),
            children(element, LINK),
            optionalChild(element, TIME),
            optionalChild(element, KEYWORDS),
            optionalChild(element, BOUNDS),
            extensions(element)
        );
    }

    private static Waypoint createWaypoint(XmlElement element) throws SAXException {
        return new Waypoint(
            parseDouble(requiredAttribute(element, LATITUDE)),
            parseDouble(requiredAttribute(element, LONGITUDE)),
            optionalDouble(element, ELEVATION),
            optionalChild(element, TIME),
            optionalDouble(element, MAGNETIC_VARIATION),
            optionalDouble(element, GEOID_HEIGHT),
            optionalChild(element, NAME),
            optionalChild(element, COMMENT),
            optionalChild(element, DESCRIPTION),
            optionalChild(element, SOURCE),
            children(element, LINK),
            optionalChild(element, SYMBOL),
            optionalChild(element, TYPE),
            optionalChild(element, FIX),
            optionalInt(element, SATELLITES),
            optionalDouble(element, HDOP),
            optionalDouble(element, VDOP),
            optionalDouble(element, PDOP),
            optionalDouble(element, AGE_OF_DGPS_DATA),
            optionalInt(element, DGPS_ID),
            extensions(element)
        );
    }

    private static Route createRoute(XmlElement element) {
        return new Route(
            optionalChild(element, NAME),
            optionalChild(element, COMMENT),
            optionalChild(element, DESCRIPTION),
            optionalChild(element, SOURCE),
            children(element, LINK),
            optionalInt(element, NUMBER),
            optionalChild(element, TYPE),
            extensions(element),
            children(element, ROUTE_POINT)
        );
    }

    private static Track createTrack(XmlElement element) {
        return new Track(
            optionalChild(element, NAME),
            optionalChild(element, COMMENT),
            optionalChild(element, DESCRIPTION),
            optionalChild(element, SOURCE),
            children(element, LINK),
            optionalInt(element, NUMBER),
            optionalChild(element, TYPE),
            extensions(element),
            children(element, TRACK_SEGMENT)
        );
    }

    private static TrackSegment createTrackSegment(XmlElement element) {
        return new TrackSegment(
            children(element, TRACK_POINT),
            extensions(element)
        );
    }

    private static Link createLink(XmlElement element) throws SAXException {
        return new Link(
            requiredAttribute(element, HYPERTEXT_REFERENCE),
            optionalChild(element, TEXT),
            optionalChild(element, TYPE)
        );
    }

    private static Email createEmail(XmlElement element) throws SAXException {
        return new Email(
            requiredAttribute(element, ID),
            requiredAttribute(element, DOMAIN)
        );
    }

    private static Person createPerson(XmlElement element) {
        return new Person(
            optionalChild(element, NAME),
            optionalChild(element, EMAIL),
            optionalChild(element, LINK)
        );
    }

    private static Copyright createCopyright(XmlElement element) throws SAXException {
        return new Copyright(
            requiredAttribute(element, AUTHOR),
            optionalChild(element, YEAR),
            optionalChild(element, LICENSE)
        );
    }

    private static Bounds createBounds(XmlElement element) throws SAXException {
        return new Bounds(
            parseDouble(requiredAttribute(element, MIN_LATITUDE)),
            parseDouble(requiredAttribute(element, MIN_LONGITUDE)),
            parseDouble(requiredAttribute(element, MAX_LATITUDE)),
            parseDouble(requiredAttribute(element, MAX_LONGITUDE))
        );
    }

    private static Point createPoint(XmlElement element) throws SAXException {
        return new Point(
            parseDouble(requiredAttribute(element, LATITUDE)),
            parseDouble(requiredAttribute(element, LONGITUDE)),
            optionalDouble(element, ELEVATION),
            optionalChild(element, TIME)
        );
    }

    private static int parseInteger(XmlElement element) throws SAXException {
        try {
            return Integer.parseInt(element.getCharacters().strip());
        }
        catch (NumberFormatException ex) {
            throw new SAXException("Invalid integer", ex);
        }
    }

    private static double parseDouble(XmlElement element) throws SAXException {
        return parseDouble(element.getCharacters());
    }

    private static double parseDouble(String string) throws SAXException {
        try {
            return Double.parseDouble(string.strip());
        }
        catch (NumberFormatException ex) {
            throw new SAXException("Invalid number", ex);
        }
    }

    private static Instant parseTime(XmlElement element) throws SAXException {
        try {
            return OffsetDateTime.parse(element.getCharacters().strip()).toInstant();
        }
        catch (DateTimeException ex) {
            throw new SAXException("Invalid time", ex);
        }
    }

    private static String requiredAttribute(XmlElement element, String name) throws SAXException {
        Attributes attributes = element.getAttributes();
        String value = attributes.getValue(name);
        if (value != null) {
        }
        value = attributes.getValue("", name);
        if (value != null) {
            return value;
        }
        throw new SAXException("Missing attribute '" + name + "' on " + element.getLocalName());
    }

    private static <T> Optional<T> optionalChild(XmlElement element, String localName) {
        return element.<T>getChildren(NAMESPACE, localName).stream().findFirst();
    }

    private static <T> List<T> children(XmlElement element, String localName) {
        return element.getChildren(NAMESPACE, localName);
    }

    private static List<Extension> extensions(XmlElement element) {
        Optional<List<Extension>> extensions = optionalChild(element, EXTENSIONS);
        return extensions.orElseGet(Collections::emptyList);
    }

    private static List<Extension> extensionElements(XmlElement element) throws SAXException {
        List<Extension> extensions = new ArrayList<>();
        for (Object child : element.getChildren()) {
            if (child instanceof Extension extension) {
                extensions.add(extension);
            }
            else {
                throw new SAXException("Extensions may only contain foreign-namespace elements, got: " + (child == null ? "null" : child.getClass().getName()));
            }
        }
        return extensions;
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

    private static final String GPX_ROOT = "gpx";
    private static final String METADATA = "metadata";
    private static final String WAYPOINT = "wpt";
    private static final String ROUTE_POINT = "rtept";
    private static final String TRACK_POINT = "trkpt";
    private static final String ROUTE = "rte";
    private static final String TRACK = "trk";
    private static final String TRACK_SEGMENT = "trkseg";
    private static final String LINK = "link";
    private static final String EMAIL = "email";
    private static final String AUTHOR = "author";
    private static final String COPYRIGHT = "copyright";
    private static final String BOUNDS = "bounds";
    private static final String EXTENSIONS = "extensions";
    private static final String POINT = "pt";
    private static final String POINT_SEGMENT = "ptseg";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "desc";
    private static final String COMMENT = "cmt";
    private static final String SOURCE = "src";
    private static final String TYPE = "type";
    private static final String SYMBOL = "sym";
    private static final String KEYWORDS = "keywords";
    private static final String TEXT = "text";
    private static final String LICENSE = "license";
    private static final String YEAR = "year";
    private static final String TIME = "time";
    private static final String ELEVATION = "ele";
    private static final String MAGNETIC_VARIATION = "magvar";
    private static final String GEOID_HEIGHT = "geoidheight";
    private static final String HDOP = "hdop";
    private static final String VDOP = "vdop";
    private static final String PDOP = "pdop";
    private static final String AGE_OF_DGPS_DATA = "ageofdgpsdata";
    private static final String SATELLITES = "sat";
    private static final String NUMBER = "number";
    private static final String DGPS_ID = "dgpsid";
    private static final String FIX = "fix";
    private static final String VERSION = "version";
    private static final String CREATOR = "creator";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lon";
    private static final String HYPERTEXT_REFERENCE = "href";
    private static final String ID = "id";
    private static final String DOMAIN = "domain";
    private static final String MIN_LATITUDE = "minlat";
    private static final String MIN_LONGITUDE = "minlon";
    private static final String MAX_LATITUDE = "maxlat";
    private static final String MAX_LONGITUDE = "maxlon";

}
