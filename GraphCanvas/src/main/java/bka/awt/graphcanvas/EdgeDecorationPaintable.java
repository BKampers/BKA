/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.*;
import java.util.function.*;

public abstract class EdgeDecorationPaintable extends Paintable {
    
    public interface Factory {
        EdgeDecorationPaintable create(Supplier<Point> startPoint, Supplier<Point> endPoint);
    }
    
    protected abstract Polygon getPolygon();
    
    protected EdgeDecorationPaintable(Supplier<Point> startPoint, Supplier<Point> endPoint) {
        this.startPoint = Objects.requireNonNull(startPoint);
        this.endPoint = Objects.requireNonNull(endPoint);
    }

    protected void paint(Graphics2D graphics, Runnable painter) {
        Point lineStart = getStartPoint().get();
        Point lineEnd = getEndPoint().get();
        double angle = rotation(lineStart, lineEnd);
        Point location = location(lineStart, lineEnd);
        graphics.translate(location.x, location.y);
        graphics.rotate(angle);
        painter.run();
        graphics.rotate(-angle);
        graphics.translate(-location.x, -location.y);
    }

    /**
     * @param start point
     * @param end point
     * @return angle to rotate a decoration, so that it tilts to the slope of the line from start point to end point.
     */
    protected double rotation(Point start, Point end) {
        double angle = Math.atan(CanvasUtil.slope(start, end));
        if (end.x < start.x) {
            return angle + Math.PI;
        }
        return angle;
    }

    /**
     * @param start
     * @param end
     * @return location between start and end where this decoration is to be painted
     */
    protected Point location(Point start, Point end) {
        return coordinateOnLine(start, end, 0.5f);
    }

    private Point coordinateOnLine(Point start, Point end, float position) {
        return new Point(
            Math.round(start.x + (end.x - start.x) * position),
            Math.round(start.y + (end.y - start.y) * position));
    }

    protected void drawBorder(Graphics2D graphics, Paint paint, Stroke stroke) {
        graphics.setPaint(paint);
        graphics.setStroke(stroke);
        graphics.drawPolygon(getPolygon());
    }

    protected void fill(Graphics2D graphics, Paint paint) {
        graphics.setPaint(paint);
        graphics.fillPolygon(getPolygon());
    }
    
    public Supplier<Point> getStartPoint() {
        return startPoint;
    }

    public Supplier<Point> getEndPoint() {
        return endPoint;
    }
    
    private final Supplier<Point> startPoint;
    private final Supplier<Point> endPoint;


    
}
