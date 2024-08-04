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
import java.util.stream.*;


public class PolygonPaintable extends Paintable {

    public static final Object LINE_PAINT_KEY = "LinePaint";
    public static final Object LINE_STROKE_KEY = "LineStroke";
    
    public interface Factory {
        PolygonPaintable create(IntFunction<Point> pointAt, IntSupplier pointCount);
    }

    public PolygonPaintable(Supplier<int[]> xPoints, Supplier<int[]> yPoints, IntSupplier nPoints) {
        this.xPoints = Objects.requireNonNull(xPoints);
        this.yPoints = Objects.requireNonNull(yPoints);
        this.nPoints = Objects.requireNonNull(nPoints);
    }
    
    public static PolygonPaintable create(IntFunction<Point> pointAt, IntSupplier pointCount) {
        return new PolygonPaintable(
            () -> xPoints(pointAt, pointCount), 
            () -> yPoints(pointAt, pointCount), 
            pointCount);
    }
    
    public static PolygonPaintable create(List<Point> points) {
        return create(points::get, points::size);
    }
    
    private static int[] xPoints(IntFunction<Point> pointAt, IntSupplier pointCount) {
        return IntStream.range(0, pointCount.getAsInt())
            .map(i -> pointAt.apply(i).x)
            .toArray();
    }
    
    private static int[] yPoints(IntFunction<Point> pointAt, IntSupplier pointCount) {
        return IntStream.range(0, pointCount.getAsInt())
            .map(i -> pointAt.apply(i).y)
            .toArray();
    }
    
    @Override
    public void paint(Graphics2D graphics) {
        paint(graphics, getPaint(LINE_PAINT_KEY), getStroke(LINE_STROKE_KEY));
    }

    @Override
    public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
        graphics.setPaint(paint);
        graphics.setStroke(stroke);
        graphics.drawPolyline(xPoints.get(), yPoints.get(), nPoints.getAsInt());
    }
    
    private final Supplier<int[]> xPoints;
    private final Supplier<int[]> yPoints;
    private final IntSupplier nPoints;
    
}
