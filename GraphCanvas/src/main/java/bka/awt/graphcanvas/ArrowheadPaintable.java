/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.function.*;

public class ArrowheadPaintable extends EdgeDecorationPaintable {
    
    public static final Object ARROWHEAD_PAINT_KEY = "ArrowheadPaint";
    public static final Object ARROWHEAD_STROKE_KEY = "ArrowheadStroke";

    
    public ArrowheadPaintable(Supplier<Point> startPoint, Supplier<Point> endPoint) {
        super(startPoint, endPoint);
    }

    @Override
    public void paint(Graphics2D graphics) {
        paint(graphics, () -> fill(graphics, getPaint(ARROWHEAD_PAINT_KEY)));
    }

    @Override
    public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
        paint(graphics, () -> drawBorder(graphics, paint, stroke));
    }

    @Override
    protected Polygon getPolygon() {
        return ARROWHEAD;
    }
    
    private static final Polygon ARROWHEAD = new Polygon(
        new int[] { -5, 5, -5 }, 
        new int[] { -5, 0, 5 },
        3);

}