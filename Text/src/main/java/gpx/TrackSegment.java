/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.util.*;


/**
 * Track segment ({@code trkseg} / trksegType).
 */
public final class TrackSegment {

    public TrackSegment(List<Waypoint> points, List<Extension> extensions) {
        this.points = List.copyOf(points);
        this.extensions = List.copyOf(extensions);
    }

    public List<Waypoint> getPoints() {
        return points;
    }

    public List<Extension> getExtensions() {
        return extensions;
    }

    private final List<Waypoint> points;
    private final List<Extension> extensions;

}
