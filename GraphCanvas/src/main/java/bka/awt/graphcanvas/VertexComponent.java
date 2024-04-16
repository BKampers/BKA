/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
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
        return DistanceToLinePositioner.create(point, nearestLineIndex(point), this::getPoint);
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
