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

    @Override
    protected Point location(Point start, Point end) {
        if (isCentered()) {
            return super.location(start, end);
        }
        return coordinateOnLine(start, end, 0f);
    }

    @Override
    protected Polygon getPolygon() {
        return DIAMOND;
    }
    
    private static final Polygon DIAMOND = new Polygon(
        new int[] {  0, 5, 0, -5 },
        new int[] { -5, 0, 5 , 0},
        4);

}