/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;


import bka.awt.chart.custom.*;
import bka.awt.chart.geometry.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;


public class DefaultLineRenderer extends LineRenderer {


    public DefaultLineRenderer(LineDrawStyle lineDrawStyle) {
        this(lineDrawStyle, null);
    }



    public DefaultLineRenderer(LineDrawStyle lineDrawStyle, CoordinateAreaRenderer markerRenderer) {
        super(lineDrawStyle, markerRenderer);
    }


    @Override
    public void draw(Graphics2D g2d, Layer layer, List<PixelAreaGeometry> graphGeometry) {
        switch (layer) {
            case BACKGROUND:
                drawBackground(g2d, graphGeometry);
                break;
            case FOREGROUND:
                drawForeground(g2d, graphGeometry);
                break;
        }
    }


    private void drawForeground(Graphics2D g2d, List<PixelAreaGeometry> graphGeometry) {
        Polygon polyline = createPolyline(graphGeometry);
        if (polyline.npoints > 0) {
            if (lineDrawStyle.getLinePaint() != null && lineDrawStyle.getLineStroke() != null) {
                Shape clipToRestore = g2d.getClip();
                g2d.clip(getWindow().getChartArea());
                g2d.setPaint(lineDrawStyle.getLinePaint());
                g2d.setStroke(lineDrawStyle.getLineStroke());
                g2d.drawPolyline(polyline.xpoints, polyline.ypoints, polyline.npoints);
                g2d.setClip(clipToRestore);
            }
            if (lineDrawStyle.getMarkerDrawStyle() != null) {
                super.draw(g2d, Layer.FOREGROUND, graphGeometry);
            }
        }
    }


    private void drawBackground(Graphics2D g2d, List<PixelAreaGeometry> graphGeometry) {
        Polygon polyline = createPolyline(graphGeometry);
        if (polyline.npoints > 0 && (lineDrawStyle.getBottomAreaPaint() != null || lineDrawStyle.getTopAreaPaint() != null)) {
            Shape clipToRestore = g2d.getClip();
            g2d.clip(getWindow().getChartArea());
            if (lineDrawStyle.getBottomAreaPaint() != null) {
                fillBottomArea(g2d, polyline);
            }
            if (lineDrawStyle.getTopAreaPaint() != null) {
                fillTopArea(g2d, polyline);
            }
            g2d.setClip(clipToRestore);
        }
    }


    @Override
    protected PixelAreaGeometry createSymbolGeometry(int x, int y) {
        Shape area = createSymbolArea(x, y);
        return new PixelAreaGeometry<>(null, null, area, new Point(x, y));
    }


    @Override
    protected void drawSymbol(Graphics2D g2d, int x, int y) {
        Shape area = createSymbolArea(x, y);
        drawLine(g2d, x - SYMBOL_WIDTH / 2, y, x + SYMBOL_WIDTH / 2, y);
        super.draw(g2d, new PixelAreaGeometry<>(null, null, area, new Point(x, y)));
    }


    private Polygon createPolyline(List<PixelAreaGeometry> graphGeometry) {
        Polygon polyline = new Polygon();
        for (PixelAreaGeometry<RectangularShape> dataAreaGeometry : graphGeometry) {
            Point pixel = dataAreaGeometry.getPixel();
            polyline.addPoint(pixel.x, pixel.y);
        }
        return polyline;
    }


    private void fillBottomArea(Graphics2D g2d, Polygon polyline) {
        fillArea(g2d, lineDrawStyle.getBottomAreaPaint(), polyline, getWindow().getYPixelBottom());
    }


    private void fillTopArea(Graphics2D g2d, Polygon polyline) {
        fillArea(g2d, lineDrawStyle.getTopAreaPaint(), polyline, getWindow().getYPixelTop());
    }


    private void fillArea(Graphics2D g2d, Paint paint, Polygon polyline, int base) {
        Polygon area = new Polygon(polyline.xpoints, polyline.ypoints, polyline.npoints);
        area.addPoint(polyline.xpoints[polyline.npoints - 1], base);
        area.addPoint(polyline.xpoints[0], base);
        g2d.setPaint(paint);
        g2d.fillPolygon(area);
    }


    private void drawLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        Paint paint = lineDrawStyle.getLinePaint();
        Stroke stroke = lineDrawStyle.getLineStroke();
        if (paint != null && stroke != null) {
            g2d.setPaint(paint);
            g2d.setStroke(stroke);
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    
    private static final int SYMBOL_WIDTH = 14;

}
