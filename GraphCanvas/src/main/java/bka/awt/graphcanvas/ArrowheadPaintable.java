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

public class ArrowheadPaintable extends Paintable {
    
    public ArrowheadPaintable(Supplier<Point> startPoint, Supplier<Point> endPoint) {
        this.startPoint = Objects.requireNonNull(startPoint);
        this.endPoint = Objects.requireNonNull(endPoint);
    }

    @Override
    public void paint(Graphics2D graphics) {
        paint(graphics, getPaint(EdgeComponent.ARROWHEAD_PAINT_KEY), getStroke(EdgeComponent.ARROWHEAD_STROKE_KEY));
    }

    @Override
    public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
        Point lineStart = startPoint.get();
        Point lineEnd = endPoint.get();
        double angle = arrowheadRotation(lineStart, lineEnd);
        Point location = arrowheadLocation(lineStart, lineEnd);
        graphics.translate(location.x, location.y);
        graphics.rotate(angle);
        graphics.setPaint(paint);
        graphics.setStroke(stroke);
        graphics.fillPolygon(ARROWHEAD_X_COORDINATES, ARROWHEAD_Y_COORDINATES, ARROWHEAD_X_COORDINATES.length);
        graphics.rotate(-angle);
        graphics.translate(-location.x, -location.y);
    }

    /**
     * @param start point
     * @param end point
     * @return angle to rotate an arrowhead that is pointing from left to right, 
     *         so that it points from the start point to the end point
     */
    private double arrowheadRotation(Point start, Point end) {
        double angle = Math.atan(CanvasUtil.slope(start, end));
        if (end.x < start.x) {
            return angle + Math.PI;
        }
        return angle;
    }

    private Point arrowheadLocation(Point start, Point end) {
        return coordinateOnLine(start, end, 0.5f);
    }

    private Point coordinateOnLine(Point start, Point end, float position) {
        return new Point(
            Math.round(start.x + (end.x - start.x) * position),
            Math.round(start.y + (end.y - start.y) * position));
    }

    private final Supplier<Point> startPoint;
    private final Supplier<Point> endPoint;

    private static final int[] ARROWHEAD_X_COORDINATES = { -5, 5, -5 };
    private static final int[] ARROWHEAD_Y_COORDINATES = { -5, 0, 5 };

}