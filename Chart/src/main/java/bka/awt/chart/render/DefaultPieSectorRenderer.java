/*
** Copyright © Bart Kampers
*/


package bka.awt.chart.render;


import bka.awt.chart.custom.*;
import bka.awt.chart.geometry.*;
import java.awt.*;
import java.awt.geom.*;


public class DefaultPieSectorRenderer extends PieSectorRenderer {


    public DefaultPieSectorRenderer(PieDrawStyle drawStyle) {
        super(drawStyle);
    }


    @Override
    public void drawLegend(Graphics2D g2d, Object key, LegendGeometry geometry) {
        int x = geometry.getX();
        int y = geometry.getY();
        g2d.setFont(geometry.getFont());
        FontMetrics fontMetrics = g2d.getFontMetrics();
        for (AreaGeometry areaGeometry : getGraphGeometry()) {
            drawSymbol(g2d, x, y, (ArcAreaGeometry) areaGeometry);
            g2d.setColor(geometry.getColor());
            g2d.drawString(areaGeometry.getX().toString(), x + geometry.getSpace(), y + fontMetrics.getDescent());
            y += geometry.getFeed() + fontMetrics.getHeight();
        }
        geometry.setY(y);
    }


    @Override
    protected void draw(Graphics2D g2d, ArcAreaGeometry geometry) {
        super.draw(g2d, geometry);
        drawLabel(g2d, geometry);
    }


    private ArcAreaGeometry createSymbolGeometry(int x, int y, ArcAreaGeometry geometry) {
        Arc2D.Float area = createSymbolArea(x, y);
        return new ArcAreaGeometry(null, null, area, geometry.getIndex());
    }


    private Arc2D.Float createSymbolArea(int x, int y) {
        return new Arc2D.Float(x - 10, y - 10, 20, 20, -40, 80, Arc2D.PIE);
    }


    private void drawSymbol(Graphics2D g2d, int x, int y, ArcAreaGeometry geometry) {
        super.draw(g2d, createSymbolGeometry(x, y, geometry));
    }


    private void drawLabel(Graphics2D g2d, ArcAreaGeometry geometry) {
        PieDrawStyle drawStyle = (PieDrawStyle) getAreaDrawStyle();
        g2d.setPaint(drawStyle.getLabelPaint(geometry));
        g2d.setFont(drawStyle.getLabelFont(geometry));
        if (drawStyle.getRotatedLabels()) {
            drawRotatedLabel(g2d, geometry);
        }
        else {
            drawHorizontalLabel(g2d, geometry);
        }
    }


    private void drawHorizontalLabel(Graphics2D g2d, ArcAreaGeometry geometry) {
        String label = geometry.getX().toString();
        FontMetrics fontMetrics = g2d.getFontMetrics();
        double angle = angle(geometry.getArea());
        Point2D labelPoint = labelPoint(angle);
        double x = labelPoint.getX();
        double y = labelPoint.getY() + fontMetrics.getHeight() / 2.0;
        if (LEFT_SIDE_MIN < angle && angle < LEFT_SIDE_MAX) {
            x -= fontMetrics.stringWidth(label);
        }
        drawString(g2d, label, x, y);
    }

    
    private void drawRotatedLabel(Graphics2D g2d, ArcAreaGeometry geometry) {
        String label = geometry.getX().toString();
        double angle = angle(geometry.getArea());
        Point2D labelPoint = labelPoint(angle);
        double x = labelPoint.getX();
        if (LEFT_SIDE_MIN < angle && angle < LEFT_SIDE_MAX) {
            angle -= Math.PI;
            x -= g2d.getFontMetrics().stringWidth(label);
        }
        g2d.rotate(-angle, labelPoint.getX(), labelPoint.getY());
        drawString(g2d, label, x, labelPoint.getY());
        g2d.rotate(angle, labelPoint.getX(), labelPoint.getY());
    }


    private void drawString(Graphics2D g2d, String label, double labelX, double labelY) {
        g2d.drawString(label, Math.round((float) labelX), Math.round((float) labelY));
    }


    private static double angle(Arc2D.Float arc) {
        return Math.toRadians(arc.start + arc.extent / 2.0);
    }


    private double labelRadius() {
        return getDiameter() / 2.0 + TEXT_RADIUS_EXTENT;
    }


    Point2D.Double labelPoint(double angle) {
        Point center = getCenter();
        double radius = labelRadius();
        return new Point2D.Double(
            center.x + Math.cos(angle) * radius,
            center.y - Math.sin(angle) * radius);
    }


    private static final double LEFT_SIDE_MIN = 0.5 * Math.PI; // radians
    private static final double LEFT_SIDE_MAX = 1.5 * Math.PI; // radians

    private static final int TEXT_RADIUS_EXTENT = 15; // pixels

}
