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
 * Simple point ({@code pt} / ptType).
 */
public final class Point {

    public Point(double latitude, double longitude, OptionalDouble elevation, Optional<Instant> time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = Objects.requireNonNull(elevation);
        this.time = Objects.requireNonNull(time);
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

    private final double latitude;
    private final double longitude;
    private final OptionalDouble elevation;
    private final Optional<Instant> time;

}
