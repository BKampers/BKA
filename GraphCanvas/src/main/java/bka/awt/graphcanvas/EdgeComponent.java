/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import bka.awt.graphcanvas.Label;
import bka.awt.graphcanvas.Vector;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;


public class EdgeComponent extends GraphComponent {

    public static final Object LINE_PAINT_KEY = "LinePaint";
    public static final Object LINE_STROKE_KEY = "LineStroke";
    public static final Object ARROWHEAD_PAINT_KEY = "ArrowheadPaint";
    public static final Object ARROWHEAD_STROKE_KEY = "ArrowheadStroke";

    public class Excerpt {

        private Excerpt() {
            edgePoints = Collections.unmodifiableList(CanvasUtil.deepCopy(points));
            labelIndices = collectLabelIndices();
        }

        private Map<Label, Integer> collectLabelIndices() {
            if (getLabels().isEmpty()) {
                return Collections.emptyMap();
            }
            return Collections.unmodifiableMap(getLabels().stream().collect(Collectors.toMap(
                Function.identity(),
                label -> ((DistanceToLinePositioner) label.getPositioner()).getIndex())));
        }

        public void set(Excerpt excerpt) {
            edgePoints = excerpt.edgePoints;
            labelIndices = excerpt.labelIndices;
        }

        public List<Point> getPoints() {
            return edgePoints;
        }

        public Map<Label, Integer> getLabelIndices() {
            return labelIndices;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Excerpt)) {
                return false;
            }
            Excerpt excerpt = (Excerpt) object;
            return edgePoints.equals(excerpt.edgePoints) && labelIndices.equals(excerpt.labelIndices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(edgePoints, labelIndices);
        }

        private List<Point> edgePoints;
        private Map<Label, Integer> labelIndices;
    }

    public EdgeComponent(VertexComponent start, VertexComponent end) {
        polygonPaintable.setPaint(LINE_PAINT_KEY, Color.BLACK);
        polygonPaintable.setStroke(LINE_STROKE_KEY, new BasicStroke());
        arrowheadPaintable.setPaint(ARROWHEAD_PAINT_KEY, Color.BLACK);
        arrowheadPaintable.setStroke(ARROWHEAD_STROKE_KEY, new BasicStroke());
        this.start = Objects.requireNonNull(start);
        setEnd(end);
    }

    public final void setEnd(VertexComponent end) {
        this.end = Objects.requireNonNull(end);
    }

    public Excerpt getExcerpt() {
        return new Excerpt();
    }

    public void set(Excerpt excerpt) {
        points.clear();
        points.addAll(excerpt.getPoints());
        excerpt.getLabelIndices().forEach((label, index) -> ((DistanceToLinePositioner) label.getPositioner()).setIndex(index));
    }

    public VertexComponent getStart() {
        return start;
    }

    public VertexComponent getEnd() {
        return end;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public boolean isDirected() {
        return directed;
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
            ((DistanceToLinePositioner) label.getPositioner()).pointInserted(index, point);
        });
    }

    @Override
    public void move(Point vector) {
        points.forEach(point -> point.move(point.x + vector.x, point.y + vector.y));
    }

    @Override
    public void addLabel(Label label) {
        super.addLabel(label);
    }

    public void revert() {
        Map<Label, Point> labelPoints = getLabels().stream().collect(Collectors.toMap(Function.identity(), label -> label.getPositioner().get()));
        VertexComponent newEnd = start;
        start = end;
        end = newEnd;
        Collections.reverse(points);
        labelPoints.forEach((label, point) -> label.setPositioner(distancePositioner(point)));
    }

    @Override
    public Supplier<Point> distancePositioner(Point point) {
        return DistanceToLinePositioner.create(point, nearestLineIndex(point), this::getPoint);
    }

    @Override
    public void paint(Graphics2D graphics) {
        polygonPaintable.paint(graphics);
        if (directed) {
            arrowheadPaintable.paint(graphics);
        }
        getLabels().forEach(label -> label.paint(graphics));
    }

    private int arrowHeadLineIndex() {
        return points.size() / 2;
    }

    /**
     *
     * @param start point
     * @param end point
     * @return angle to rotate an arrowhead pointing from left to right so that it points from the start point to the end point
     */
    private double arrowheadRotation(Point start, Point end) {
        double angle = Math.atan(CanvasUtil.slope(start, end));
        if (end.x < start.x) {
            return angle + Math.PI;
        }
        return angle;
    }

    private Point arrowheadLocation(Point start, Point end) {
        return coordinateOnLine(start, end, 0.5f);
    }

    private Point coordinateOnLine(Point start, Point end, float position) {
        return new Point(
            Math.round(start.x + (end.x - start.x) * position),
            Math.round(start.y + (end.y - start.y) * position));
    }

    @Override
    public void paintHighlight(Graphics2D graphics, Color color, Stroke stroke) {
        polygonPaintable.paint(graphics, color, stroke);
        if (directed) {
            arrowheadPaintable.paint(graphics, color, stroke);
        }
    }

    public void paintHighlight(Graphics2D graphics, Color color, Stroke stroke, int index) {
        graphics.setPaint(color);
        graphics.setStroke(stroke);
        Point p1 = getPoint(index);
        Point p2 = getPoint(index + 1);
        graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    public long squareDistance(Point point) {
        long distance = Long.MAX_VALUE;
        Point startPoint = getStartConnectorPoint();
        for (Point endPoint : points) {
            distance = Math.min(distance, CanvasUtil.squareDistance(point, startPoint, endPoint));
            startPoint = endPoint;
        }
        return Math.min(distance, CanvasUtil.squareDistance(point, startPoint, getEndConnectorPoint()));
    }

    public int nearestLineIndex(Point point) {
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

    public Point getPoint(int index) {
        if (index == 0) {
            return getStartConnectorPoint();
        }
        if (index == points.size() + 1) {
            return getEndConnectorPoint();
        }
        return points.get(index - 1);
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
        Point point = getStartConnectorPoint();
        int index = 0;
        while (index <= points.size()) {
            Point nextPoint = (index < points.size()) ? points.get(index) : getEndConnectorPoint();
            if (CanvasUtil.squareDistance(point, nextPoint) < twinTolerance) {
                removePoint((index < points.size()) ? index : index - 1);
                removed = true;
            }
            else {
                point = nextPoint;
                index++;
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
        Vector line = point.subtract(vector(getStartConnectorPoint()));
        int index = 0;
        while (index < points.size()) {
            int nextIndex = index + 1;
            Vector nextPoint = vector((nextIndex < points.size()) ? points.get(nextIndex) : getEndConnectorPoint());
            Vector nextLine = nextPoint.subtract(point);
            double cosine = Vector.cosine(line, nextLine);
            if (cosine < acuteCosineLimit || obtuseCosineLimit < cosine) {
                removePoint(index);
                removed = true;
            }
            else {
                index = nextIndex;
                point = nextPoint;
                line = nextLine;
            }
        }
        return removed;
    }

    private void removePoint(int index) {
        points.remove(index);
        getLabels().forEach(label -> ((DistanceToLinePositioner) label.getPositioner()).pointRemoved(index));
    }

    private static Vector vector(Point point) {
        return new Vector(point.getX(), point.getY());
    }

    @Override
    public Collection<Paintable> getCustomizablePaintables() {
        if (directed) {
            return List.of(polygonPaintable, arrowheadPaintable);
        }
        return List.of(polygonPaintable);
    }


    private final Paintable polygonPaintable = new Paintable() {

        @Override
        public void paint(Graphics2D graphics) {
            paint(graphics, getPaint(LINE_PAINT_KEY), getStroke(LINE_STROKE_KEY));
        }

        @Override
        public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
            int count = points.size() + ENDPOINT_COUNT;
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
            Point endPoint = getEndConnectorPoint();
            x[count - 1] = endPoint.x;
            y[count - 1] = endPoint.y;
            graphics.setPaint(paint);
            graphics.setStroke(stroke);
            graphics.drawPolyline(x, y, count);
        }

    };


    private final Paintable arrowheadPaintable = new Paintable() {

        @Override
        public void paint(Graphics2D graphics) {
            paint(graphics, getPaint(ARROWHEAD_PAINT_KEY), getStroke(ARROWHEAD_STROKE_KEY));
        }

        @Override
        public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
            int index = arrowHeadLineIndex();
            Point lineStart = getPoint(index);
            Point lineEnd = getPoint(index + 1);
            double angle = arrowheadRotation(lineStart, lineEnd);
            Point location = arrowheadLocation(lineStart, lineEnd);
            graphics.translate(location.x, location.y);
            graphics.rotate(angle);
            graphics.setPaint(paint);
            graphics.setStroke(stroke);
            graphics.fillPolygon(ARROWHEAD_X_COORDINATES, ARROWHEAD_Y_COORDINATES, ARROWHEAD_X_COORDINATES.length);
            graphics.rotate(-angle);
            graphics.translate(-location.x, -location.y);
        }

    };

    private final List<Point> points = new ArrayList<>();
    private VertexComponent start;
    private VertexComponent end;
    private boolean directed;

    private static final int[] ARROWHEAD_X_COORDINATES = { -5, 5, -5 };
    private static final int[] ARROWHEAD_Y_COORDINATES = { -5, 0, 5 };

    private static final int ENDPOINT_COUNT = 2;

}
