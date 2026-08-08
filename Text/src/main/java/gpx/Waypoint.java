/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.time.*;
import java.util.*;


/**
 * Waypoint / route point / track point ({@code wpt}, {@code rtept}, {@code trkpt} / wptType).
 */
public final class Waypoint {

    public Waypoint(
        double latitude,
        double longitude,
        OptionalDouble elevation,
        Optional<Instant> time,
        OptionalDouble magneticVariation,
        OptionalDouble geoidHeight,
        Optional<String> name,
        Optional<String> comment,
        Optional<String> description,
        Optional<String> source,
        List<Link> links,
        Optional<String> symbol,
        Optional<String> type,
        Optional<Fix> fix,
        OptionalInt satellites,
        OptionalDouble hdop,
        OptionalDouble vdop,
        OptionalDouble pdop,
        OptionalDouble ageOfDgpsData,
        OptionalInt dgpsId,
        List<Extension> extensions
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = Objects.requireNonNull(elevation);
        this.time = Objects.requireNonNull(time);
        this.magneticVariation = Objects.requireNonNull(magneticVariation);
        this.geoidHeight = Objects.requireNonNull(geoidHeight);
        this.name = Objects.requireNonNull(name);
        this.comment = Objects.requireNonNull(comment);
        this.description = Objects.requireNonNull(description);
        this.source = Objects.requireNonNull(source);
        this.links = List.copyOf(links);
        this.symbol = Objects.requireNonNull(symbol);
        this.type = Objects.requireNonNull(type);
        this.fix = Objects.requireNonNull(fix);
        this.satellites = Objects.requireNonNull(satellites);
        this.hdop = Objects.requireNonNull(hdop);
        this.vdop = Objects.requireNonNull(vdop);
        this.pdop = Objects.requireNonNull(pdop);
        this.ageOfDgpsData = Objects.requireNonNull(ageOfDgpsData);
        this.dgpsId = Objects.requireNonNull(dgpsId);
        this.extensions = List.copyOf(extensions);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public OptionalDouble getElevation() {
        return elevation;
    }

    public Optional<Instant> getTime() {
        return time;
    }

    public OptionalDouble getMagneticVariation() {
        return magneticVariation;
    }

    public OptionalDouble getGeoidHeight() {
        return geoidHeight;
    }

    public Optional<String> getName() {
        return name;
    }

    public Optional<String> getComment() {
        return comment;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<String> getSource() {
        return source;
    }

    public List<Link> getLinks() {
        return links;
    }

    public Optional<String> getSymbol() {
        return symbol;
    }

    public Optional<String> getType() {
        return type;
    }

    public Optional<Fix> getFix() {
        return fix;
    }

    public OptionalInt getSatellites() {
        return satellites;
    }

    public OptionalDouble getHdop() {
        return hdop;
    }

    public OptionalDouble getVdop() {
        return vdop;
    }

    public OptionalDouble getPdop() {
        return pdop;
    }

    public OptionalDouble getAgeOfDgpsData() {
        return ageOfDgpsData;
    }

    public OptionalInt getDgpsId() {
        return dgpsId;
    }

    public List<Extension> getExtensions() {
        return extensions;
    }

    private final double latitude;
    private final double longitude;
    private final OptionalDouble elevation;
    private final Optional<Instant> time;
    private final OptionalDouble magneticVariation;
    private final OptionalDouble geoidHeight;
    private final Optional<String> name;
    private final Optional<String> comment;
    private final Optional<String> description;
    private final Optional<String> source;
    private final List<Link> links;
    private final Optional<String> symbol;
    private final Optional<String> type;
    private final Optional<Fix> fix;
    private final OptionalInt satellites;
    private final OptionalDouble hdop;
    private final OptionalDouble vdop;
    private final OptionalDouble pdop;
    private final OptionalDouble ageOfDgpsData;
    private final OptionalInt dgpsId;
    private final List<Extension> extensions;

}
