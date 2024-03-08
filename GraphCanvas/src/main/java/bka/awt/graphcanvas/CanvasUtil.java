/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
 */
package bka.awt.graphcanvas;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.stream.*;


public class CanvasUtil {

    public static boolean isNear(Point cursor, Point linePoint1, Point linePoint2) {
        return isNear(squareDistance(cursor, linePoint1, linePoint2));
    }

    public static boolean isNear(Point cursor, Point point) {
        return isNear(squareDistance(cursor, point));
    }

    public static boolean isNear(long distance) {
        return distance < NEAR_DISTANCE;
    }

    public static boolean isOnBorder(long distance) {
        return INSIDE_BORDER_MARGIN < distance && distance < OUTSIDE_BORDER_MARGIN;
    }

    public static boolean isInside(long distance) {
        return distance <= INSIDE_BORDER_MARGIN;
    }

    public static Point getPoint(double x, double y) {
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    public static Point round(Point2D point2d) {
        return getPoint(point2d.getX(), point2d.getY());
    }

    public static long squareDistance(Point point1, Point point2) {
        long deltaX = point1.x - point2.x;
        long deltaY = point1.y - point2.y;
        return deltaX * deltaX + deltaY * deltaY;
    }

    public static long squareDistance(Point point, Point linePoint1, Point linePoint2) {
        if (!inRange(point.x, linePoint1.x, linePoint2.x) && !inRange(point.y, linePoint1.y, linePoint2.y)) {
            return Math.min(squareDistance(point, linePoint1), squareDistance(point, linePoint2));
        }
        return squareDistance(point, round(intersectionPoint(point, linePoint1, linePoint2)));
    }

    private static boolean inRange(int i, int limit1, int limit2) {
        return Math.min(limit1, limit2) <= i && i <= Math.max(limit1, limit2);
    }

    public static Point2D intersectionPoint(Point point, Point linePoint1, Point linePoint2) {
        if (linePoint1.x == linePoint2.x) {
            if (linePoint1.y == linePoint2.y) {
                return linePoint1;
            }
            return new Point(linePoint1.x, point.y);
        }
        if (linePoint1.y == linePoint2.y) {
            return new Point(point.x, linePoint1.y);
        }
        double slope = slope(linePoint1, linePoint2);
        double offset = -slope * linePoint1.x + linePoint1.y;
        double perpendicularSlope = -1 / slope;
        double perpendicularOffset = point.y - perpendicularSlope * point.x;
        double x = (offset - perpendicularOffset) / (perpendicularSlope - slope);
        return new Point2D.Double(x, slope * x + offset);
    }

    public static double slope(Point2D point1, Point2D point2) {
        return (point2.getY() - point1.getY()) / (point2.getX() - point1.getX());
    }

    public static List<Point> deepCopy(List<Point> list) {
        return list.stream().map(Point::new).collect(Collectors.toList());
    }

    public static boolean cleanup(EdgeRenderer edge) {
        return edge.cleanup(TWIN_TOLERANCE, ACUTE_COSINE_LIMIT, OBTUSE_COSINE_LIMIT);
    }

    private CanvasUtil() {
    }

    public static final long NEAR_DISTANCE = 5 * 5;
    private static final long INSIDE_BORDER_MARGIN = -4 * 4;
    private static final long OUTSIDE_BORDER_MARGIN = 4 * 4;

    private static final double ACUTE_COSINE_LIMIT = -0.99;
    private static final double OBTUSE_COSINE_LIMIT = 0.99;
    private static final long TWIN_TOLERANCE = 3 * 3;


}
