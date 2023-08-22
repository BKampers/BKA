/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;


public class CanvasUtil {

    public static double distance(Point point1, Point point2) {
        return Math.sqrt(squareDistance(point1, point2));
    }

    public static long squareDistance(Point point1, Point point2) {
        long deltaX = point1.x - point2.x;
        long deltaY = point1.y - point2.y;
        return deltaX * deltaX + deltaY * deltaY;
    }

    public static long squareDistance(Point point, Point linePoint1, Point linePoint2) {
        if (!pointInSquare(point, linePoint1, linePoint2)) {
            return Math.min(squareDistance(point, linePoint1), squareDistance(point, linePoint2));
        }
        return squareDistance(point, intersectionPoint(point, linePoint1, linePoint2));
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

    public static boolean pointInSquare(Point point, Point corner1, Point corner2) {
        return Math.min(corner1.x, corner2.x) <= point.x && point.x <= Math.max(corner1.x, corner2.x)
            && Math.min(corner1.y, corner2.y) <= point.y && point.y <= Math.max(corner1.y, corner2.y);
    }

    private CanvasUtil() {
    }

}
