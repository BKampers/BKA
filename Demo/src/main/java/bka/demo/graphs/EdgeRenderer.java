/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.demo.graphs.Vector;
import java.awt.*;
import java.util.*;


public class EdgeRenderer implements Element {

    public EdgeRenderer(VertexRenderer start, VertexRenderer end) {
        this(start);
        this.end = end;
    }

    public EdgeRenderer(VertexRenderer start) {
        this.start = start;
    }

    @Override
    public Point getLocation() {
        return start.getLocation();
    }

    @Override
    public Dimension getDimension() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void move(Point vector) {
        points.forEach(point -> point.move(point.x + vector.x, point.y + vector.y));
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

    public void setPoints(java.util.List<Point> points) {
        this.points.clear();
        this.points.addAll(points);
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

    public boolean cleanup(long twinTolerance, double acuteCosineLimit, double obtuseCosineLimit) {
        boolean removed = false;
        if (twinTolerance > 0) {
            removed = removeTwins(twinTolerance);
        }
        if (acuteCosineLimit > -1 || obtuseCosineLimit < 1) {
            removed |= removeExtremeAngles(acuteCosineLimit, obtuseCosineLimit);
        }
        return removed;
    }

    private boolean removeTwins(long twinTolerance) {
        if (points.isEmpty()) {
            return false;
        }
        boolean removed = false;
        Point p0 = getStartConnectorPoint();
        int i = 0;
        while (i <= points.size()) {
            Point pi = (i < points.size()) ? points.get(i) : getEndConnectorPoint();
            if (CanvasUtil.squareDistance(p0, pi) < twinTolerance) {
                points.remove(i);
                removed = true;
            }
            else {
                p0 = pi;
                i++;
            }
        }
        return removed;
    }

    private boolean removeExtremeAngles(double acuteCosineLimit, double obtuseCosineLimit) {
        if (points.isEmpty()) {
            return false;
        }
        boolean removed = false;
        Vector point = vector(points.getFirst());
        Vector segment1 = point.subtract(vector(getStartConnectorPoint()));
        int i = 0;
        while (i < points.size()) {
            Vector nextPoint = vector((i + 1 < points.size()) ? points.get(i + 1) : getEndConnectorPoint());
            Vector segment2 = nextPoint.subtract(point);
            double cosine = Vector.cosine(segment1, segment2);
            if (cosine < acuteCosineLimit || obtuseCosineLimit < cosine) {
                points.remove(i);
                removed = true;
            }
            else {
                point = nextPoint;
                segment1 = segment2;
                i++;
            }
        }
        return removed;
    }

    private static Vector vector(Point point) {
        return new Vector(point.getX(), point.getY());
    }

    @Override
    public void addLabel(bka.demo.graphs.Label label) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeLabel(bka.demo.graphs.Label label) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private final LinkedList<Point> points = new LinkedList<>();
    private final VertexRenderer start;
    private VertexRenderer end;


}
