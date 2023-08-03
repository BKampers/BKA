/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
import java.util.*;


public class DefaultEdgeRenderer implements EdgeRenderer {

    public DefaultEdgeRenderer(VertexRenderer start, VertexRenderer end) {
        this(start);
        this.end = end;
    }

    public DefaultEdgeRenderer(VertexRenderer start) {
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

    public Point selectEdgePoint(Point point) {
        Point selected = points.stream().filter(edgePoint -> squareDistance(edgePoint, point) < 10).findAny().orElse(null);
        if (selected != null) {
            return selected;
        }
        return null;
    }

    @Override
    public void paint(Graphics2D graphics) {
        int count = points.size() + 1;
        if (end != null) {
            count++;
        }
        int[] x = new int[count];
        int[] y = new int[count];
        x[0] = start.getLocation().x;
        y[0] = start.getLocation().y;
        for (int i = 0; i < points.size(); ++i) {
            x[i + 1] = points.get(i).x;
            y[i + 1] = points.get(i).y;
        }
        if (end != null) {
            x[count - 1] = end.getLocation().x;
            y[count - 1] = end.getLocation().y;
        }
        graphics.drawPolyline(x, y, count);
    }

    @Override
    public long squareDistance(Point point) {
        Collection<Double> distances = new ArrayList<>(points.size() + 1);
        Point startPoint = start.getLocation();
        for (Point endPoint : points) {
            distances.add(squareDistance(point, startPoint, endPoint));
            startPoint = endPoint;
        }
        distances.add(squareDistance(point, startPoint, end.getLocation()));
//        System.out.printf("sqDist (%d,%d) : %f\n", point.x, point.y, distances.stream().sorted().findFirst().orElseThrow());
        return Math.round(distances.stream().sorted().findFirst().orElseThrow());
    }

    @Override
    public Point getStartConnectorPoint() {
        if (points.isEmpty()) {
            return start.getConnectorPoint(end.getLocation());
        }
        return start.getConnectorPoint(points.get(0));
    }

    @Override
    public Point getEndConnectorPoint() {
        if (points.isEmpty()) {
            return end.getConnectorPoint(start.getLocation());
        }
        return end.getConnectorPoint(points.get(points.size() - 1));
    }

    private static long squareDistance(Point point1, Point point2) {
        long dx = point1.x - point2.x;
        long dy = point1.y - point2.y;
        return dx * dx + dy * dy;
    }

    private static double squareDistance(Point point, Point linePoint1, Point linePoint2) {
        int xMin = Math.min(linePoint1.x, linePoint2.x);
        int xMax = Math.max(linePoint1.x, linePoint2.x);
        int yMin = Math.min(linePoint1.y, linePoint2.y);
        int yMax = Math.max(linePoint1.y, linePoint2.y);
        if (point.x < xMin || xMax < point.x || point.y < yMin || yMax < point.y) {
            return Math.min(squareDistance(point, linePoint1), squareDistance(point, linePoint2));
        }
        float dx = linePoint1.x - linePoint2.x;
        float dy = linePoint1.y - linePoint2.y;
        float a = dy / dx;
        float b = -a * linePoint1.x + linePoint1.y;
        float ap = -1 / a;
        float bp = point.y - ap * point.x;
        float xi = (b - bp) / (ap - a);
        float yi = a * xi + b;
        Point intersection = new Point(Math.round(xi), Math.round(yi));
        return squareDistance(point, intersection);
    }

    private int indexNear(Point point) {
        int count = points.size() - 1;
        int index = 0;
        while (index < count) {
            int x1 = points.get(index).x;
            int y1 = points.get(index).y;
            int x2 = points.get(index + 1).x;
            int y2 = points.get(index + 1).y;
            int xRangeMin = Math.min(x1, x2) - NEAR_TOLERANCE;
            int xRangeMax = Math.max(x1, x2) + NEAR_TOLERANCE;
            int yRangeMin = Math.min(y1, y2) - NEAR_TOLERANCE;
            int yRangeMax = Math.max(y1, y2) + NEAR_TOLERANCE;
            if (xRangeMin < point.x && point.x < xRangeMax && yRangeMin < point.y && point.y < yRangeMax) {
                int dx = deltaX(index);
                int dy = deltaY(index);
                if (-NEAR_TOLERANCE < dx && dx < NEAR_TOLERANCE || -NEAR_TOLERANCE < dy && dy < NEAR_TOLERANCE) {
                    return index;
                }
                else {
                    float a = (float) dy / dx;
                    float b = -a * x1 + y1;
                    if (dy < dx) {
                        float y = a * point.x + b;
                        if (y - NEAR_TOLERANCE < point.y && point.y < y + NEAR_TOLERANCE) {
                            return index;
                        }
                    }
                    else {
                        float x = (b - point.y) / -a;
                        if (x - NEAR_TOLERANCE < point.x && point.x < x + NEAR_TOLERANCE) {
                            return index;
                        }
                    }
                }
            }
            index++;
        }
        return NO_INDEX;
    }

    private int deltaX(int index) {
        return points.get(index + 1).x - points.get(index).x;
    }


    private int deltaY(int index) {
        return points.get(index + 1).y - points.get(index).y;
    }

    private final ArrayList<Point> points = new ArrayList<>();
    private final VertexRenderer start;
    private VertexRenderer end;

    private static final int NEAR_TOLERANCE = 7;
    private static final int NO_INDEX = -1;

}
