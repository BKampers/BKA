/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.List;
import java.util.function.*;


public abstract class VertexPaintable extends Paintable {

    public VertexPaintable(Dimension size) {
        super();
        setSize(size);
    }

    public VertexPaintable(VertexPaintable vertexPaintable) {
        super(vertexPaintable);
        setSize(vertexPaintable.size);
    }

    public final void setSize(Dimension size) {
        this.size = new Dimension(size);
    }

    protected Dimension getSize() {
        return new Dimension(size);
    }

    public double distance(Point location, Point point) {
        return location.distance(point) - getSize().width / 2d;
    }

    public Supplier<Point> distancePositioner(Point location, Point point) {
        double dy = point.y - location.y;
        double angle = Math.atan((point.x - location.x) / dy);
        float sin = (float) ((dy < 0) ? -Math.sin(angle) : Math.sin(angle));
        float cos = (float) ((dy < 0) ? -Math.cos(angle) : Math.cos(angle));
        float distanceToBorder = (float) distance(location, point);
        return () -> {
            float distanceToLocation = getSize().width / 2f + distanceToBorder;
            return new Point(location.x + Math.round(sin * distanceToLocation), location.y + Math.round(cos * distanceToLocation));
        };
    }

    public Point getConnectorPoint(Point location, Point point) {
        if (location.equals(point)) {
            return point;
        }
        double dx = point.x - location.x;
        double dy = point.y - location.y;
        double angle = Math.atan(dx / dy);
        float radius = getSize().width / ((dy < 0) ? -2f : 2f);
        float cx = (float) Math.sin(angle) * radius;
        float cy = (float) Math.cos(angle) * radius;
        return new Point(Math.round(location.x + cx), Math.round(location.y + cy));
    }

    public List<Point> getContour(Point location) {
        return null;
    }

    private Dimension size;
}
