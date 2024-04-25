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

    public VertexComponent(VertexPaintable vertexPaintable, Point location) {
        this.vertexPaintable = Objects.requireNonNull(vertexPaintable);
        this.location = new Point(location);
    }

    public Point getLocation() {
        return new Point(location);
    }

    public void setLocation(Point location) {
        this.location.move(location.x, location.y);
    }

    public Dimension getDimension() {
        return vertexPaintable.getSize();
    }

    public void setDimension(Dimension dimension) {
        vertexPaintable.setSize(dimension);
    }

    @Override
    public void move(Point vector) {
        location.move(location.x + vector.x, location.y + vector.y);
    }

    public Point getConnectorPoint(Point edgePoint) {
        return vertexPaintable.getConnectorPoint(location, edgePoint);
    }

    public long squareDistance(Point point) {
        double distance = distance(point);
        long squareDistance = Math.round(distance * distance);
        return (distance < 0) ? -squareDistance : squareDistance;
    }

    private double distance(Point point) {
        return vertexPaintable.distance(location, point);
    }

    @Override
    public Supplier<Point> distancePositioner(Point point) {
        return vertexPaintable.distancePositioner(location, point);
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.translate(location.x, location.y);
        vertexPaintable.paint(graphics);
        graphics.translate(-location.x, -location.y);
        getLabels().forEach(label -> label.paint(graphics));
    }

    @Override
    public void paintHighlight(Graphics2D graphics, Color color, Stroke stroke) {
        graphics.translate(location.x, location.y);
        vertexPaintable.paint(graphics, color, stroke);
        graphics.translate(-location.x, -location.y);
    }

    public void resize(Point target, ResizeDirection direction) {
        vertexPaintable.resize(location, target, direction);
    }

    @Override
    public Collection<Paintable> getCustomizablePaintables() {
        return List.of(vertexPaintable);
    }

    private final VertexPaintable vertexPaintable;
    private final Point location;

}
