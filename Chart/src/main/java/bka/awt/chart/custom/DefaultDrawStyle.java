/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.custom;


import bka.awt.chart.geometry.*;
import java.awt.*;


public class DefaultDrawStyle implements AreaDrawStyle<AreaGeometry> {


    private DefaultDrawStyle(Image image, Color color, Color borderColor, Stroke borderStroke) {
        this.image = image;
        this.color = color;
        this.borderColor = borderColor;
        this.borderStroke = borderStroke;
    }


    public static DefaultDrawStyle create(Color color, Color borderColor, Stroke borderStroke) {
        return new DefaultDrawStyle(null, color, borderColor, borderStroke);
    }


    public static DefaultDrawStyle create(Color color, Color borderColor) {
        return new DefaultDrawStyle(null, color, borderColor, new BasicStroke());
    }


    public static DefaultDrawStyle createSolid(Color color) {
        return new DefaultDrawStyle(null, color, null, null);
    }


    public static DefaultDrawStyle createBorder(Color borderColor, Stroke borderStroke) {
        return new DefaultDrawStyle(null, null, borderColor, borderStroke);
    }


    public static DefaultDrawStyle createBorder(Color borderColor) {
        return new DefaultDrawStyle(null, null, borderColor, new BasicStroke());
    }


    public static DefaultDrawStyle createImage(Image image) {
        return new DefaultDrawStyle(image, null, null, null);
    }


    @Override
    public Image getImage() {
        return image;
    }


    @Override
    public Paint getPaint(AreaGeometry geometry) {
        return color;
    }


    @Override
    public Paint getBorderPaint(AreaGeometry geometry) {
        return borderColor;
    }


    @Override
    public Stroke getBorderStroke(AreaGeometry geometry) {
        return borderStroke;
    }


    @Override
    public Paint getLabelPaint(AreaGeometry geometry) {
        return labelColor;
    }


    @Override
    public Font getLabelFont(AreaGeometry geometry) {
        return labelFont;
    }


    private final Image image;
    private final Color color;
    private final Color borderColor;
    private final Stroke borderStroke;
    private final Color labelColor = Color.BLACK;
    private Font labelFont;

}
