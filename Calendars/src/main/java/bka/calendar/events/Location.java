/*
** © Bart Kampers
*/

package bka.calendar.events;

import java.math.*;

/**
 * Simple VO class to store latitude/longitude information.
 */
public class Location {

    /**
     * Creates a new instance of <code>Location</code> with the given parameters.
     *
     * @param latitude the latitude, in degrees, of this location. North latitude is positive, south negative.
     * @param longitude the longitude, in degrees, of this location. East longitude is positive, east negative.
     */
    public Location(double latitude, double longitude) {
        if (latitude < -90.0 || 90.0 < latitude) {
            throw new IllegalArgumentException("Invalid latitude: " + latitude);
        }
        if (longitude < -180.0 || 180.0 < longitude) {
            throw new IllegalArgumentException("Invalid longitude: " + longitude);
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Creates a new instance of <code>Location</code> with the given parameters.
     *
     * @param latitude the latitude, in degrees, of this location. North latitude is positive, south negative.
     * @param longitude the longitude, in degrees of this location. East longitude is positive, west negative.
     */
    public Location(BigDecimal latitude, BigDecimal longitude) {
        this(latitude.doubleValue(), longitude.doubleValue());
    }

    /**
     * Creates a new instance of <code>Location</code> with the given parameters.
     *
     * @param latitude the latitude, in degrees, of this location. North latitude is positive, south negative.
     * @param longitude the longitude, in degrees of this location. East longitude is positive, west negative.
     */
    public Location(String latitude, String longitude) {
        this(Double.parseDouble(latitude), Double.parseDouble(longitude));
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    private final double latitude;
    private final double longitude;

}
