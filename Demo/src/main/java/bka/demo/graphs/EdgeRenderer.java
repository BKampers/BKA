/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.demo.graphs.Label;
import bka.demo.graphs.Vector;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.*;
import java.util.function.*;


public class EdgeRenderer extends Element {


    public class Excerpt {

        private Excerpt() {
            this.points = CanvasUtil.deepCopy(EdgeRenderer.this.points);
            this.labelIndices = new HashMap<>(EdgeRenderer.this.labelIndices);
        }

        public void set(Excerpt excerpt) {
            this.points.clear();
            this.labelIndices.clear();
            this.points.addAll(CanvasUtil.deepCopy(excerpt.points));
            this.labelIndices.putAll(excerpt.labelIndices);
        }

        public List<Point> getPoints() {
            return CanvasUtil.deepCopy(this.points);
        }

        public Map<Label, Integer> getLabelIndices() {
            return Collections.unmodifiableMap(this.labelIndices);
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Excerpt)) {
                return false;
            }
            return points.equals(((Excerpt) object).points) && labelIndices.equals(((Excerpt) object).labelIndices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(points, labelIndices);
        }

        private final List<Point> points;
        private final Map<Label, Integer> labelIndices;
    }


    public EdgeRenderer(VertexRenderer start, VertexRenderer end) {
        this(start);
        this.end = Objects.requireNonNull(end);
    }

    public EdgeRenderer(VertexRenderer start) {
        this.start = Objects.requireNonNull(start);
    }

    public Excerpt getExcerpt() {
        return new Excerpt();
    }

    public void set(Excerpt excerpt) {
        points.clear();
        labelIndices.clear();
        points.addAll(excerpt.getPoints());
        labelIndices.putAll(excerpt.getLabelIndices());
    }

    @Override
    public Point getLocation() {
        return start.getLocation();
    }

    @Override
    public void move(Point vector) {
        points.forEach(point -> point.move(point.x + vector.x, point.y + vector.y));
    }

    @Override
    public void addLabel(Label label) {
        super.addLabel(label);
        labelIndices.put(label, ((DistancePositioner) label.getPositioner()).getIndex());
    }

    @Override
    public Supplier<Point> distancePositioner(Point point) {
        int index = nearestLineIndex(point);
        Point linePoint1 = getPoint(index);
        Point linePoint2 = getPoint(index + 1);
        double lineLength = linePoint1.distance(linePoint2);
        Point2D intersection = CanvasUtil.intersectionPoint(point, linePoint1, linePoint2);
        double distanceToLine = point.distance(intersection);
        double ratio = intersection.distance(linePoint1) / lineLength;
        return new DistancePositioner(index, distanceToLine, ratio);
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

    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public void addPoint(Point point) {
        addPoint(points.size(), point);
    }

    public void addPoint(int index, Point point) {
        points.add(index, point);
        getLabels().forEach(label -> {
            ((DistancePositioner) label.getPositioner()).pointInserted(index, point);
            labelIndices.put(label, ((DistancePositioner) label.getPositioner()).getIndex());
        });
    }

    @Override
    public void paint(Graphics2D graphics) {
        int count = points.size() + ((end == null) ? 1 : 2);
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
        graphics.setStroke(new BasicStroke());
        graphics.drawPolyline(x, y, count);
        getLabels().forEach(label -> label.paint(graphics));
    }

    @Override
    public void paintHighlight(Graphics2D graphics, Color color, Stroke stroke) {
        graphics.setPaint(color);
        graphics.setStroke(stroke);
        paint(graphics);
    }

    public long squareDistance(Point point) {
        long distance = Long.MAX_VALUE;
        Point startPoint = getStartConnectorPoint();
        for (Point endPoint : points) {
            distance = Math.min(distance, CanvasUtil.squareDistance(point, startPoint, endPoint));
            startPoint = endPoint;
        }
        if (end != null) {
            return Math.min(distance, CanvasUtil.squareDistance(point, startPoint, getEndConnectorPoint()));
        }
        if (distance == Long.MAX_VALUE) {
            return CanvasUtil.squareDistance(point, startPoint);
        }
        return distance;
    }

    private int nearestLineIndex(Point point) {
        int index = -1;
        long shortestDistance = Long.MAX_VALUE;
        Point startPoint = getStartConnectorPoint();
        for (int i = 0; i < points.size(); ++i) {
            Point endPoint = points.get(i);
            long distance = CanvasUtil.squareDistance(point, startPoint, endPoint);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                index = i;
            }
            startPoint = endPoint;
        }
        if (CanvasUtil.squareDistance(point, startPoint, getEndConnectorPoint()) < shortestDistance) {
            return points.size();
        }
        return index;
    }

    private Point getPoint(int index) {
        if (index == 0) {
            return getStartConnectorPoint();
        }
        if (index <= points.size()) {
            return points.get(index - 1);
        }
        return getEndConnectorPoint();
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
                final int j = i;
                getLabels().forEach(label -> {
                    ((DistancePositioner) label.getPositioner()).pointRemoved(j);
                    labelIndices.put(label, ((DistancePositioner) label.getPositioner()).getIndex());
                });
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
        Vector point = vector(points.get(0));
        Vector segment1 = point.subtract(vector(getStartConnectorPoint()));
        int i = 0;
        while (i < points.size()) {
            Vector nextPoint = vector((i + 1 < points.size()) ? points.get(i + 1) : getEndConnectorPoint());
            Vector segment2 = nextPoint.subtract(point);
            double cosine = Vector.cosine(segment1, segment2);
            if (cosine < acuteCosineLimit || obtuseCosineLimit < cosine) {
                points.remove(i);
                final int j = i;
                getLabels().forEach(label -> {
                    ((DistancePositioner) label.getPositioner()).pointRemoved(j);
                    labelIndices.put(label, ((DistancePositioner) label.getPositioner()).getIndex());
                });
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


    private class DistancePositioner implements Supplier<Point> {

        public DistancePositioner(int index, double distance, double ratio) {
            this.distance = distance;
            this.ratio = ratio;
            this.index = index;
        }

        @Override
        public Point get() {
            Point linePoint1 = getPoint(index);
            Point linePoint2 = getPoint(index + 1);
            double xi = linePoint1.x + (linePoint2.x - linePoint1.x) * ratio;
            double yi = linePoint1.y + (linePoint2.y - linePoint1.y) * ratio;
            double angle = Math.acos((linePoint1.y - linePoint2.y) / linePoint1.distance(linePoint2));
            double x = Math.cos(angle) * distance + xi;
            double y = Math.sin(angle) * distance + yi;
            return CanvasUtil.getPoint(x, y);
        }

        int getIndex() {
            return index;
        }

        void pointInserted(int insertIndex, Point point) {
            if (insertIndex < index) {
                index++;
            }
            else if (insertIndex == index) {
                if (CanvasUtil.squareDistance(getPoint(index), point) < CanvasUtil.squareDistance(getPoint(index + 2), point)) {
                    index++;
                }
            }
        }

        void pointRemoved(int removeIndex) {
            if (removeIndex < index) {
                index--;
            }
        }

        private final double distance;
        private final double ratio;
        private int index;

    }


    private final List<Point> points = new ArrayList<>();
    private final Map<Label, Integer> labelIndices = new HashMap<>();
    private final VertexRenderer start;
    private VertexRenderer end;

}
