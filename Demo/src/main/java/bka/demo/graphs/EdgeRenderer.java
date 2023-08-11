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

    public VertexRenderer getStart() {
        return start;
    }

    public void setEnd(VertexRenderer end) {
        this.end = end;
    }

    public VertexRenderer getEnd() {
        return end;
    }

    public java.util.List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public void addPoint(int index, Point point) {
        points.add(index, point);
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
        int i = 1;
        for (Point point : points) {
            x[i] = point.x;
            y[i] = point.y;
            ++i;
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
            distance = Math.min(distance, CanvasUtil.squareDistance(point, startPoint, endPoint));
            startPoint = endPoint;
        }
        if (end == null) {
            if (distance == Long.MAX_VALUE) {
                return CanvasUtil.squareDistance(point, startPoint);
            }
            return distance;
        }
        return Math.min(distance, CanvasUtil.squareDistance(point, startPoint, getEndConnectorPoint()));
    }

    public Point getStartConnectorPoint() {
        if (points.isEmpty()) {
            return start.getConnectorPoint(end.getLocation());
        }
        return start.getConnectorPoint(points.getFirst());
    }

    public Point getEndConnectorPoint() {
        if (points.isEmpty()) {
            return end.getConnectorPoint(start.getLocation());
        }
        return end.getConnectorPoint(points.getLast());
    }

    private final LinkedList<Point> points = new LinkedList<>();
    private final VertexRenderer start;
    private VertexRenderer end;


}
