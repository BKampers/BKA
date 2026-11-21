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

    public final void setSize(Dimension size) {
        setSize(size.width, size.height);
    }

    public final void setSize(int width, int height) {
        size = new Dimension(Math.max(MINIMUM_SIZE, width), Math.max(MINIMUM_SIZE, height));
    }

    protected Dimension getSize() {
        return new Dimension(size);
    }

    protected int getWidth() {
        return size.width;
    }

    protected int getHeight() {
        return size.height;
    }

    public abstract void resize(Point location, Point traget, ResizeDirection direction);

    public ResizeDirection resizeDirection(Point location, Point cursor) {
        int dx = cursor.x - location.x;
        int dy = cursor.y - location.y;
        double angle = Math.abs(Math.atan((double) dx / dy));
        if (angle < VERTICAL_MARGIN) {
            return (dy < 0) ? ResizeDirection.NORTH : ResizeDirection.SOUTH;
        }
        if (HORIZONTAL_MARGIN < angle) {
            return (dx < 0) ? ResizeDirection.WEST : ResizeDirection.EAST;
        }
        if (dx < 0) {
            return (dy < 0) ? ResizeDirection.NORTH_WEST : ResizeDirection.SOUTH_WEST;
        }
        return (dy < 0) ? ResizeDirection.NORTH_EAST : ResizeDirection.SOUTH_EAST;
    }

    public double distance(Point location, Point point) {
        return location.distance(point) - getWidth() / 2d;
    }

    public abstract Supplier<Point> distancePositioner(Point location, Point point);

    public abstract Point getConnectorPoint(Point location, Point point);

    private Dimension size;

    private static final int MINIMUM_SIZE = 2;

    private static final double HORIZONTAL_MARGIN = 0.4 * Math.PI;
    private static final double VERTICAL_MARGIN = 0.1 * Math.PI;

}
