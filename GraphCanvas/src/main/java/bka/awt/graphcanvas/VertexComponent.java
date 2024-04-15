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
import java.util.*;
import java.util.function.*;


public class VertexComponent extends GraphComponent {

    public VertexComponent(VertexPaintable shapePaintable, Point location) {
        this.shapePaintable = Objects.requireNonNull(shapePaintable);
        this.location = new Point(location);
    }

    public Point getLocation() {
        return new Point(location);
    }

    public void setLocation(Point location) {
        this.location.move(location.x, location.y);
    }

    public Dimension getDimension() {
        return shapePaintable.getSize();
    }

    public void setDimension(Dimension dimension) {
        shapePaintable.setSize(dimension);
    }

    @Override
    public void move(Point vector) {
        location.move(location.x + vector.x, location.y + vector.y);
    }

    public Point getConnectorPoint(Point edgePoint) {
        return shapePaintable.getConnectorPoint(location, edgePoint);
    }

    public long squareDistance(Point point) {
        double distance = distance(point);
        long squareDistance = Math.round(distance * distance);
        return (distance < 0) ? -squareDistance : squareDistance;
    }

    private double distance(Point point) {
        return shapePaintable.distance(location, point);
    }

    @Override
    public Supplier<Point> distancePositioner(Point point) {
        if (points() == null) {
            return shapePaintable.distancePositioner(location, point);
        }
        return lineDistancePositioner(point);
    }

    public Supplier<Point> lineDistancePositioner(Point point) {
        int index = nearestLineIndex(point);
        Point linePoint1 = getPoint(index);
        Point linePoint2 = getPoint(index + 1);
        Point2D anchor = CanvasUtil.intersectionPoint(point, linePoint1, linePoint2);
        double yDistance = directedDistance(point, anchor, CanvasUtil.slope(linePoint1, linePoint2));
        double xDistance = (linePoint1.x > linePoint2.x) ? -yDistance : yDistance;
        return new DistanceToLinePositioner(index, xDistance, yDistance, directedRatio(linePoint1, linePoint2, anchor), this::getPoint);
    }

    private int nearestLineIndex(Point point) {
        int index = -1;
        long shortestDistance = Long.MAX_VALUE;
        List<Point> points = points();
        for (int i = 0; i < points.size(); ++i) {
            Point startPoint = getPoint(i);
            Point endPoint = getPoint(i + 1);
            long distance = CanvasUtil.squareDistance(point, startPoint, endPoint);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                index = i;
            }
        }
        return index;
    }


    private Point getPoint(int index) {
        if (index < points().size()) {
            return points().get(index);
        }
        return points().get(0);
    }

    private List<Point> points() {
        return shapePaintable.getContour(location);
    }

    private static double directedDistance(Point2D distantPoint, Point2D anchor, double slope) {
        double distance = distantPoint.distance(anchor);
        if (slope <= -1) {
            return (distantPoint.getX() < anchor.getX()) ? -distance : distance;
        }
        if (1 <= slope) {
            return (distantPoint.getX() > anchor.getX()) ? -distance : distance;
        }
        return (distantPoint.getY() < anchor.getY()) ? -distance : distance;
    }

    private static double directedRatio(Point2D linePoint1, Point2D linePoint2, Point2D anchor) {
        double ratio = anchor.distance(linePoint1) / linePoint1.distance(linePoint2);
        double x1 = linePoint1.getX();
        double x2 = linePoint2.getX();
        double y1 = linePoint1.getY();
        double y2 = linePoint2.getY();
        if (Math.abs(CanvasUtil.slope(linePoint1, linePoint2)) < 1) {
            if (anchor.getX() < x1 && x1 < x2 || x2 < x1 && x1 < anchor.getX()) {
                return -ratio;
            }
        }
        else {
            if (anchor.getY() < y1 && y1 < y2 || y2 < y1 && y1 < anchor.getY()) {
                return -ratio;
            }
        }
        return ratio;
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.translate(location.x, location.y);
        shapePaintable.paint(graphics);
        graphics.translate(-location.x, -location.y);
        getLabels().forEach(label -> label.paint(graphics));
    }

    @Override
    public void paintHighlight(Graphics2D graphics, Color color, Stroke stroke) {
        graphics.translate(location.x, location.y);
        shapePaintable.paint(graphics, color, stroke);
        graphics.translate(-location.x, -location.y);
    }

    public void resize(Point target) {
        int size = Math.abs((int) Math.round(location.distance(target) * 2));
        shapePaintable.setSize(new Dimension(size, size));
    }

    @Override
    public Collection<Paintable> getCustomizablePaintables() {
        return List.of(shapePaintable);
    }

    private final VertexPaintable shapePaintable;
    private final Point location;

}
