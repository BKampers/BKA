/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.function.*;


public class RoundVertexPaintable extends VertexPaintable {

    public RoundVertexPaintable(Dimension size) {
        super(size);
    }

    public RoundVertexPaintable(VertexPaintable vertexPaintable) {
        super(vertexPaintable);
    }

    @Override
    public void paint(Graphics2D graphics) {
        fill(graphics);
        paint(graphics, getPaint(BORDER_PAINT_KEY), getStroke(BORDER_STROKE_KEY));
    }

    @Override
    public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
        graphics.setPaint(paint);
        graphics.setStroke(stroke);
        graphics.drawOval(-widthRadius(), -heightRadius(), getSize().width, getSize().height);
    }

    private void fill(Graphics2D graphics) {
        graphics.setPaint(getPaint(FILL_PAINT_KEY));
        graphics.fillOval(-widthRadius(), -heightRadius(), getSize().width, getSize().height);
    }

    private int widthRadius() {
        return getSize().width / 2;
    }

    private int heightRadius() {
        return getSize().height / 2;
    }

    @Override
    public void resize(Point location, Point target, ResizeDirection direction) {
        int size = Math.abs((int) Math.round(location.distance(target) * 2));
        setSize(new Dimension(size, size));
    }

    @Override
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

    @Override
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



}
