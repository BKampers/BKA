/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.custom;


import java.awt.*;
import java.awt.geom.*;
import java.util.*;


public class GridStyle {


    public interface PaintBox {
        Paint getHorizontalPaint(RectangularShape area, int index);
        Paint getVerticalPaint(RectangularShape area, int index);
    }


    private GridStyle(Stroke xStroke, Paint xPaint, Stroke yStroke, Paint yPaint, PaintBox paintBox) {
        this.xStroke = xStroke;
        this.xPaint = xPaint;
        this.yStroke = yStroke;
        this.yPaint = yPaint;
        this.paintBox = paintBox;
    }


    public static GridStyle create(Stroke stroke, Paint paint) {
        return create(stroke, paint, null);
    }


    public static GridStyle create(Stroke stroke, Paint paint, PaintBox paintBox) {
        return create(stroke, paint, stroke, paint, paintBox);
    }


    public static GridStyle create(Stroke xStroke, Paint xPaint, Stroke yStroke, Paint yPaint) {
        return create(xStroke, xPaint, yStroke, yPaint, null);
    }


    public static GridStyle create(PaintBox paintBox) {
        return create(null, null, null, null, paintBox);
    }


    public static GridStyle create(Stroke xStroke, Paint xPaint, Stroke yStroke, Paint yPaint, PaintBox paintBox) {
        return new GridStyle(xStroke, xPaint, yStroke, yPaint, paintBox);
    }


    public static GridStyle createSolid(Color color) {
        return create(createSolidPaintBox(new Color[] { color }));
    }


    public static GridStyle createSolid(Color[] colors) {
        return create(createSolidPaintBox(colors));
    }


    public static GridStyle createGradient(Color[] colors) {
        return createGradient(colors, darker(colors));
    }


    public static GridStyle createGradient(Color[] colors1, Color[] colors2) {
        return create(createGradientPaintBox(colors1, colors2));
    }


    public static PaintBox createSolidPaintBox(Color[] colors) {
        return new SolidPaintBox(colors);
    }


    public static PaintBox createGradientPaintBox(Color[] colors1, Color[] colors2) {
        return new GradientPaintBox(colors1, colors2);
    }


    public Stroke getXStroke() {
        return xStroke;
    }


    public Paint getXPaint() {
        return xPaint;
    }


    public Stroke getYStroke() {
        return yStroke;
    }


    public Paint getYPaint() {
        return yPaint;
    }


    public PaintBox getPaintBox() {
        return paintBox;
    }
    
    
    private static Color[] darker(Color[] colors) {
        Color[] darker = new Color[colors.length];
        for (int i = 0; i < colors.length; ++i) {
            darker[i] = colors[i].darker();
        }
        return darker;
    }


    public Paint getHorizontalPaint(RectangularShape area, int index) {
        return null;
    }


    public Paint getVerticalPaint(RectangularShape area, int index) {
        return null;
    }



    private static class SolidPaintBox implements PaintBox {

        SolidPaintBox(Color[] colors) {
            this.colors = Objects.requireNonNull(colors);
        }

        @Override
        public Paint getHorizontalPaint(RectangularShape area, int index) {
            return colors[index % colors.length];
        }

        @Override
        public Paint getVerticalPaint(RectangularShape area, int index) {
            return colors[index % colors.length];
        }

        private final Color[] colors;

    }
    
    
    private static class GradientPaintBox implements PaintBox {

        private GradientPaintBox(Color[] colors1, Color[] colors2) {
            this.colors1 = Objects.requireNonNull(colors1);
            this.colors2 = Objects.requireNonNull(colors2);
        }

        @Override
        public Paint getHorizontalPaint(RectangularShape area, int index) {
            Color color1 = colors1[index % colors1.length];
            Color color2 = colors2[index % colors2.length];
            return new GradientPaint((float) area.getCenterX(), (float) area.getMinY(), color1, (float) area.getCenterX(), (float) area.getMaxY(), color2);
        }

        @Override
        public Paint getVerticalPaint(RectangularShape area, int index) {
            Color color1 = colors1[index % colors1.length];
            Color color2 = colors2[index % colors2.length];
            return new GradientPaint((float) area.getMaxX(), (float) area.getCenterY(), color1, (float) area.getMinX(), (float) area.getCenterY(), color2);
        }

        private final Color[] colors1;
        private final Color[] colors2;

    }


    private final Stroke xStroke;
    private final Paint xPaint;
    private final Stroke yStroke;
    private final Paint yPaint;
    private final PaintBox paintBox;

}
