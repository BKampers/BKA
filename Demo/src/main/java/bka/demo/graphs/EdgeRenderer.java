/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import java.awt.*;
import java.util.*;


public class EdgeRenderer implements Renderer {

    public EdgeRenderer(VertexRenderer start, VertexRenderer end) {
        this(start);
        this.end = end;
    }

    public EdgeRenderer(VertexRenderer start) {
        this.start = start;
    }

    public void setEnd(VertexRenderer end) {
        this.end = end;
    }

    public VertexRenderer getEnd() {
        return end;
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    @Override
    public void paint(Graphics2D graphics) {
        int count = points.size() + 1;
        if (end != null) {
            count++;
        }
        int[] x = new int[count];
        int[] y = new int[count];
        Point startPoint = getStartConnectorPoint();
        x[0] = startPoint.x;
        y[0] = startPoint.y;
        for (int i = 0; i < points.size(); ++i) {
            x[i + 1] = points.get(i).x;
            y[i + 1] = points.get(i).y;
        }
        if (end != null) {
            Point endPoint = getEndConnectorPoint();
            x[count - 1] = endPoint.x;
            y[count - 1] = endPoint.y;
        }
        graphics.drawPolyline(x, y, count);
    }

    public long squareDistance(Point point) {
        long distance = Long.MAX_VALUE;
        Point startPoint = getStartConnectorPoint();
        for (Point endPoint : points) {
            distance = Math.min(distance, squareDistance(point, startPoint, endPoint));
            startPoint = endPoint;
        }
        if (end == null) {
            if (distance == Long.MAX_VALUE) {
                return squareDistance(point, startPoint);
            }
            return distance;
        }
        System.out.printf("sq-dist: %d\n", Math.min(distance, squareDistance(point, startPoint, getEndConnectorPoint())));
        return Math.min(distance, squareDistance(point, startPoint, getEndConnectorPoint()));
    }

    public Point getStartConnectorPoint() {
        if (points.isEmpty()) {
            return start.getConnectorPoint(end.getLocation());
        }
        return start.getConnectorPoint(points.get(0));
    }

    public Point getEndConnectorPoint() {
        if (points.isEmpty()) {
            return end.getConnectorPoint(start.getLocation());
        }
        return end.getConnectorPoint(points.get(points.size() - 1));
    }

    private static long squareDistance(Point point1, Point point2) {
        long deltaX = point1.x - point2.x;
        long deltaY = point1.y - point2.y;
        return deltaX * deltaX + deltaY * deltaY;
    }

    private static long squareDistance(Point point, Point linePoint1, Point linePoint2) {
        if (!pointInSquare(point, linePoint1, linePoint2)) {
            return Math.min(squareDistance(point, linePoint1), squareDistance(point, linePoint2));
        }
        return squareDistance(point, intersectionPoint(point, linePoint1, linePoint2));
    }

    private static boolean pointInSquare(Point point, Point linePoint1, Point linePoint2) {
        return Math.min(linePoint1.x, linePoint2.x) <= point.x && point.x <= Math.max(linePoint1.x, linePoint2.x)
            && Math.min(linePoint1.y, linePoint2.y) <= point.y && point.y <= Math.max(linePoint1.y, linePoint2.y);
    }

    private static Point intersectionPoint(Point point, Point linePoint1, Point linePoint2) {
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

    private final java.util.List<Point> points = new ArrayList<>();
    private final VertexRenderer start;
    private VertexRenderer end;


}
