/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package gpx;

import java.util.*;


/**
 * Ordered list of points ({@code ptseg} / ptsegType).
 */
public final class PointSegment {

    public PointSegment(List<Point> points) {
        this.points = List.copyOf(points);
    }

    public List<Point> getPoints() {
        return points;
    }

    private final List<Point> points;

}
