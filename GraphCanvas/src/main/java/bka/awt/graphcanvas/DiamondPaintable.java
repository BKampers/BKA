/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.function.*;

public class DiamondPaintable extends EdgeDecorationPaintable {
    
    public static final Object DIAMOND_FILL_PAINT_KEY = "DiamondFillPaint";
    public static final Object DIAMOND_BORDER_PAINT_KEY = "DiamondBorderPaint";
    public static final Object DIAMOND_BORDER_STROKE_KEY = "DiamondBorderStroke";

    
    public DiamondPaintable(Supplier<Point> startPoint, Supplier<Point> endPoint) {
        super(startPoint, endPoint);
    }

    @Override
    public void paint(Graphics2D graphics) {
        paint(graphics, () -> {
            fill(graphics, getPaint(DIAMOND_FILL_PAINT_KEY));
            drawBorder(graphics, getPaint(DIAMOND_BORDER_PAINT_KEY), getStroke(DIAMOND_BORDER_STROKE_KEY));
        });
    }
    
    @Override
    public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
        paint(graphics, () -> drawBorder(graphics, paint, stroke));
    }
    
    private void paint(Graphics2D graphics, Runnable painter) {
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

    private void drawBorder(Graphics2D graphics, Paint paint, Stroke stroke) {
        graphics.setPaint(paint);
        graphics.setStroke(stroke);
        graphics.drawPolygon(DIAMOND_X_COORDINATES, DIAMOND_Y_COORDINATES, DIAMOND_X_COORDINATES.length);
    }

    private void fill(Graphics2D graphics, Paint paint) {
        graphics.setPaint(paint);
        graphics.fillPolygon(DIAMOND_X_COORDINATES, DIAMOND_Y_COORDINATES, DIAMOND_X_COORDINATES.length);
    }
    
    /**
     * @param start point
     * @param end point
     * @return angle to rotate an upright diamod that is pointing from left to right, 
     *         so that it tilts to the slope of the line from start point to end point.
     */
    private double rotation(Point start, Point end) {
        double angle = Math.atan(CanvasUtil.slope(start, end));
        if (end.x < start.x) {
            return angle + Math.PI;
        }
        return angle;
    }

    private Point location(Point start, Point end) {
        return coordinateOnLine(start, end, 0.5f);
    }

    private Point coordinateOnLine(Point start, Point end, float position) {
        return new Point(
            Math.round(start.x + (end.x - start.x) * position),
            Math.round(start.y + (end.y - start.y) * position));
    }

    private static final int[] DIAMOND_X_COORDINATES = {  0, 5, 0, -5 };
    private static final int[] DIAMOND_Y_COORDINATES = { -5, 0, 5 , 0};

}