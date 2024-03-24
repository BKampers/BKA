/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
 */
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;


public class VertexRenderer extends Element {

    public static final Object FILL_PAINT_KEY = "FillPaint";
    public static final Object FILL_STROKE_KEY = "FillStroke";

    public VertexRenderer(Point point) {
        this.location = new Point(point);
        shapePaintable.setPaint(FILL_PAINT_KEY, Color.BLACK);
        shapePaintable.setStroke(FILL_STROKE_KEY, new BasicStroke());
    }

    public Point getLocation() {
        return new Point(location);
    }

    public void setLocation(Point location) {
        this.location.move(location.x, location.y);
    }

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
        return location.distance(point) - size / 2d;
    }

    @Override
    public Supplier<Point> distancePositioner(Point point) {
        double dy = point.y - location.y;
        double angle = Math.atan((point.x - location.x) / dy);
        float sin = (float) ((dy < 0) ? -Math.sin(angle) : Math.sin(angle));
        float cos = (float) ((dy < 0) ? -Math.cos(angle) : Math.cos(angle));
        float distanceToBorder = (float) Math.sqrt(squareDistance(point));
        return () -> {
            float distanceToLocation = size / 2f + distanceToBorder;
            return new Point(location.x + Math.round(sin * distanceToLocation), location.y + Math.round(cos * distanceToLocation));
        };
    }

    @Override
    public void paint(Graphics2D graphics) {
        shapePaintable.paint(graphics);
        getLabels().forEach(label -> label.paint(graphics));
    }

    @Override
    public void paintHighlight(Graphics2D graphics, Color color, Stroke stroke) {
        shapePaintable.paint(graphics, color, stroke);
    }

    public void resize(Point target) {
        size = Math.abs((int) Math.round(location.distance(target) * 2));
    }

    @Override
    public Collection<Paintable> getCustomizablePaintables() {
        return List.of(shapePaintable);
    }

    private final Paintable shapePaintable = new Paintable() {
        @Override
        public void paint(Graphics2D graphics) {
            paint(graphics, getPaint(FILL_PAINT_KEY), getStroke(FILL_STROKE_KEY));
        }

        @Override
        public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
            graphics.setPaint(paint);
            graphics.setStroke(stroke);
            int radius = size / 2;
            graphics.fillOval(location.x - radius, location.y - radius, size, size);
        }

    };

    private final Point location;
    private int size = 12;

}
