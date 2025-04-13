/*
** © Bart Kampers
*/

package bka.calendar.events;

/**
 * Value object to store latitude and longitude.
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
