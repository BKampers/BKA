/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.demo.graphs.Label;
import java.awt.*;
import java.util.*;
import java.util.function.*;


public class VertexRenderer implements Element {

    public VertexRenderer(Point point) {
        this.location = new Point(point);
    }

    @Override
    public Point getLocation() {
        return new Point(location);
    }

    public void setLocation(Point location) {
        this.location.move(location.x, location.y);
    }

    @Override
    public Dimension getDimension() {
        return new Dimension(size, size);
    }

    public void setDimension(Dimension dimension) {
        size = dimension.height;
    }

    @Override
    public void move(Point vector) {
        location.move(location.x + vector.x, location.y + vector.y);
    }

    public Point getConnectorPoint(Point edgePoint) {
        if (location.equals(edgePoint)) {
            return edgePoint;
        }
        float dx = edgePoint.x - location.x;
        float dy = edgePoint.y - location.y;
        double angle = Math.atan(dx / dy);
        float radius = size / ((dy < 0) ? -2f : 2f);
        float cx = (float) Math.sin(angle) * radius;
        float cy = (float) Math.cos(angle) * radius;
        return new Point(Math.round(location.x + cx), Math.round(location.y + cy));
    }

    public long squareDistance(Point point) {
        double distance = distance(point);
        long squareDistance = Math.round(distance * distance);
        return (distance < 0) ? -squareDistance : squareDistance;
    }

    private double distance(Point point) {
        return CanvasUtil.distance(point, location) - size / 2d;
    }

    @Override
    public void addLabel(Label label) {
        labels.add(label);
    }

    @Override
    public void removeLabel(Label label) {
        labels.remove(label);
    }

    public Supplier<Point> distancePositioner(Point point) {
        double distanceToBorder = Math.sqrt(squareDistance(point));
        double dx = point.x - location.x;
        double dy = point.y - location.y;
        double angle = Math.atan(dx / dy);
        float sin = (float) ((dx < 0) ? -Math.sin(angle) : Math.sin(angle));
        float cos = (float) ((dy < 0) ? -Math.cos(angle) : Math.cos(angle));
        return () -> {
            double radius = size / 2d;
            float distanceToLocation = (float) (Math.sqrt(radius * radius) + distanceToBorder);
            return new Point(location.x + Math.round(sin * distanceToLocation), location.y + Math.round(cos * distanceToLocation));
        };
    }

    @Override
    public Collection<Label> getLabels() {
        return Collections.unmodifiableCollection(labels);
    }

    @Override
    public void paint(Graphics2D graphics) {
        int radius = size / 2;
        graphics.fillOval(location.x - radius, location.y - radius, size, size);
        labels.forEach(label -> label.paint(graphics));
    }

    public void resize(Point target) {
        size = Math.abs((int) Math.round(CanvasUtil.distance(location, target) * 2));
    }

    private final Collection<Label> labels = new ArrayList<>();
    private final Point location;
    private int size = 12;


}
