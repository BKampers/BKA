/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
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

    public static Point getPoint(Point origin, double distance, double slope) {
        double angle = angle(slope);
        double x = Math.sin(angle) * distance;
        double y = Math.cos(angle) * distance;
        return getPoint(x, y);
    }

    public static double angle(double slope) {
        if (slope == Double.NEGATIVE_INFINITY) {
            return 0;
        }
        if (slope == Double.POSITIVE_INFINITY) {
            return Math.PI;
        }
        return (0.5 - slope * 0.25) * Math.PI;
    }

    public static double slope(Point point1, Point point2) {
        return (double) (point2.y - point1.y) / (point2.x - point1.x);
    }

    public static Point getPoint(double x, double y) {
        return new Point(Math.round((float) x), Math.round((float) y));
    }

    public static double distance(Point point1, Point point2) {
        return Math.sqrt(squareDistance(point1, point2));
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
        return squareDistance(point, intersectionPoint(point, linePoint1, linePoint2));
    }

    private static boolean inRange(int i, int limit1, int limit2) {
        return Math.min(limit1, limit2) <= i && i <= Math.max(limit1, limit2);
    }

    public static Point intersectionPoint(Point point, Point linePoint1, Point linePoint2) {
        if (linePoint1.x == linePoint2.x) {
            if (linePoint1.y == linePoint2.y) {
                return linePoint1;
            }
            return new Point(linePoint1.x, point.y);
        }
        if (linePoint1.y == linePoint2.y) {
            return new Point(point.x, linePoint1.y);
        }
        float slope = (float) (linePoint1.y - linePoint2.y) / (linePoint1.x - linePoint2.x);
        float offset = -slope * linePoint1.x + linePoint1.y;
        float perpendicularSlope = -1 / slope;
        float perpendicularOffset = point.y - perpendicularSlope * point.x;
        float xIntersection = (offset - perpendicularOffset) / (perpendicularSlope - slope);
        float yIntersection = slope * xIntersection + offset;
        return new Point(Math.round(xIntersection), Math.round(yIntersection));
    }

    public static java.util.List<Point> deepCopy(List<Point> list) {
        return list.stream().map(point -> new Point(point)).collect(Collectors.toList());
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
