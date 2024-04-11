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


public class VertexRenderer extends Element {

    public VertexRenderer(VertexPaintable shapePaintable, Point location) {
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
        return shapePaintable.distancePositioner(location, point);
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
