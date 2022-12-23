/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.custom;


import bka.awt.chart.geometry.*;
import java.awt.*;


public class BarDrawStyle implements AreaDrawStyle<AreaGeometry<Rectangle>> {


    private BarDrawStyle(float[] pattern, Color[] colors) {
        this.pattern = pattern;
        this.colors = colors;
    }


    public static BarDrawStyle create(Color color) {
        return new BarDrawStyle(null, new Color[] { color });
    }


    public static BarDrawStyle create(float[] pattern, Color[] colors) {
        return new BarDrawStyle(pattern, colors);
    }


    public static BarDrawStyle createAlternative(Color color1, Color color2) {
        return new BarDrawStyle(null, new Color[] { color1, color2 });
    }


    public static BarDrawStyle create(Color centerColor, Color edgeColor) {
        return new BarDrawStyle(
            new float[] { 0.0f, 0.5f, 1.0f },
            new Color[] { edgeColor, centerColor, edgeColor });
    }


    public void setImage(Image image) {
        this.image = image;
    }


    public void setBorder(Paint borderPaint, Stroke borderStroke) {
        this.borderPaint = borderPaint;
        this.borderStroke = borderStroke;
    }


    public void setBorder(Paint borderPaint) {
        setBorder(borderPaint, new BasicStroke());
    }


    @Override
    public Image getImage() {
        return image;
    }


    @Override
    public Paint getPaint(AreaGeometry<Rectangle> geometry) {
        if (pattern != null) {
            Rectangle area = geometry.getArea();
            return new LinearGradientPaint(area.x, area.y, area.x + area.width, area.y, pattern, colors);
        }
        if (colors.length == 1) {
            return colors[0];
        }
        return getAlternativePaint(geometry);
    }


    @Override
    public Paint getBorderPaint(AreaGeometry<Rectangle> geometry) {
        return borderPaint;
    }


    @Override
    public Stroke getBorderStroke(AreaGeometry<Rectangle> geometry) {
        return borderStroke;
    }


    @Override
    public Paint getLabelPaint(AreaGeometry<Rectangle> geometry) {
        return Color.BLACK;
    }


    @Override
    public Font getLabelFont(AreaGeometry<Rectangle> geometry) {
        return null;
    }


    private Paint getAlternativePaint(AreaGeometry<Rectangle> geometry) {
        return new GradientPaint(0, 0, colors[0], geometry.getArea().width, geometry.getArea().y + geometry.getArea().height, colors[1]);
    }


    private final float[] pattern;
    private final Color[] colors;

    private Paint borderPaint;
    private Stroke borderStroke;
    private Image image;

}
