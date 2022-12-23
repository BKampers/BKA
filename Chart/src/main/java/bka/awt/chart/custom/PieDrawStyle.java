package bka.awt.chart.custom;


import bka.awt.chart.geometry.*;
import java.awt.*;
import java.awt.geom.*;



public class PieDrawStyle implements AreaDrawStyle<ArcAreaGeometry> {


    private PieDrawStyle(Image image, Color[][] colors) {
        this.image = image;
        this.colors = colors;
    }


    public static PieDrawStyle create(Color[][] colors) {
        return new PieDrawStyle(null, colors);
    }


    public static PieDrawStyle create(Color[] baseColors) {
        Color[][] colors = new Color[baseColors.length][];
        for (int i = 0; i < baseColors.length; ++i) {
            colors[i] = new Color[] { baseColors[i], baseColors[i].darker() };
        }
        return new PieDrawStyle(null, colors);
    }


    @Override
    public Image getImage() {
        return image;
    }


    public void setRotatedLabels(boolean rotatedLabels) {
        this.rotatedLabels = rotatedLabels;
    }


    public boolean getRotatedLabels() {
        return rotatedLabels;
    }


    @Override
    public Paint getPaint(ArcAreaGeometry geometry) {
        Arc2D.Float area = geometry.getArea();
        Point2D.Float center = new Point2D.Float((float) area.getCenterX(), (float) area.getCenterY());
        float diameter = (float) Math.max(area.getWidth(), area.getHeight());
        return new RadialGradientPaint(center, diameter / 2.0f, fractions, colors[geometry.getIndex() % colors.length]);
    }


    @Override
    public Paint getBorderPaint(ArcAreaGeometry geometry) {
        return colors[geometry.getIndex() % colors.length][0].brighter();
    }


    @Override
    public Stroke getBorderStroke(ArcAreaGeometry geometry) {
        return new BasicStroke(1.0f);
    }


     @Override
    public Paint getLabelPaint(ArcAreaGeometry geometry) {
        return Color.BLACK;
    }


    @Override
    public Font getLabelFont(ArcAreaGeometry geometry) {
        return null;
    }


    private final Image image;
    private final float[] fractions = new float[] { 0.0f, 1.0f };
    private final Color[][] colors;
    private boolean rotatedLabels;

}
