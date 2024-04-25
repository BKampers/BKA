/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.function.*;


public abstract class VertexPaintable extends Paintable {

    public final static Object BORDER_PAINT_KEY = "BorderPaint";
    public final static Object BORDER_STROKE_KEY = "BorderStroke";
    public final static Object FILL_PAINT_KEY = "FillPaint";

    public VertexPaintable(Dimension size) {
        super();
        setSize(size);
    }

    public VertexPaintable(VertexPaintable vertexPaintable) {
        super(vertexPaintable);
        setSize(vertexPaintable.size);
    }

    public final void setSize(Dimension size) {
        setSize(size.width, size.height);
    }

    public final void setSize(int width, int height) {
        size = new Dimension(width, height);
    }

    protected Dimension getSize() {
        return new Dimension(size);
    }

    public abstract void resize(Point location, Point traget, ResizeDirection direction);

    public double distance(Point location, Point point) {
        return location.distance(point) - getSize().width / 2d;
    }

    public abstract Supplier<Point> distancePositioner(Point location, Point point);

    public abstract Point getConnectorPoint(Point location, Point point);

    private Dimension size;
}
