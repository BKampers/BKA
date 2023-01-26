/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.geom.*;
import java.util.*;


public interface MarkerRingProperties {

    public static MarkerRingProperties of(Point2D center, double radius, Scale scale) {
        Objects.requireNonNull(center);
        Objects.requireNonNull(scale);
        return new MarkerRingProperties() {
            @Override
            public Point2D getCenter() {
                return center;
            }

            @Override
            public double getRadius() {
                return radius;
            }

            @Override
            public Scale getScale() {
                return scale;
            }
        };
    }

    Point2D getCenter();

    double getRadius();

    Scale getScale();
}
