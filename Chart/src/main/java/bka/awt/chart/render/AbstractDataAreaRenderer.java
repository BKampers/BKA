/*
** Copyright © Bart Kampers
*/


package bka.awt.chart.render;


import bka.awt.chart.*;
import bka.awt.chart.custom.*;
import bka.awt.chart.geometry.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public abstract class AbstractDataAreaRenderer<G extends AreaGeometry> {

    
    public enum Layer { BACKGROUND, FOREGROUND }


    AbstractDataAreaRenderer(AreaDrawStyle drawStyle) {
        this.areaDrawStyle = drawStyle;
    }


    public void setStackBase(AbstractDataAreaRenderer stackBase) {
        if (stackBase != null && ! stackBase.supportStack()) {
            throw new IllegalArgumentException(stackNotSupportedMessage(stackBase.getClass()));
        }
        if (! supportStack()) {
            throw new IllegalStateException(stackNotSupportedMessage(getClass()));
        }
        this.stackBase = stackBase;
    }


    private static String stackNotSupportedMessage(Class rendererClass) {
        return String.format("Renderer '%s' does not support stacking", rendererClass.getName());
    }


    public AbstractDataAreaRenderer getStackBase() {
        return stackBase;
    }


    public abstract List<G> createGraphGeomerty(ChartPoints chart) throws ChartDataException;
    public abstract void drawLegend(Graphics2D g2d, Object key, LegendGeometry geometry);


    public void setChartRenderer(ChartRenderer renderer) {
        chartRenderer = renderer;
    }


    /**
     * Set the ChartGeometry.Window object that will hold the computed coordinates needed to
     * draw graphs.
     * @param window
     */
    public void setWindow(ChartGeometry.Window window) {
        this.window = window;
    }


    /**
     * Collect all points that are contained in this renderer's window and adjust
     * data bounds of the window.
     * 
     * @param key
     * @param chartData
     */
    public void addPointsInWindow(Object key, ChartPoints chartData) throws ChartDataException {
        ChartPoints graphPointsInWindow = new ChartPoints();
        for (ChartPoint element : chartData) {
            Number x = element.getX();
            Number y = getY(element);
            if (window.inXWindowRange(x) && window.inYWindowRange(y)) {
                graphPointsInWindow.add(x, y);
                window.adjustBounds(x, y);
            }
        }
        window.putPoints(key, graphPointsInWindow);
    }


    protected Number getY(ChartPoint element) throws ChartDataException {
        if (supportStack()) {
            return getY(element.getX());
        }
        return element.getY();
    }


    protected Number getY(Number x) throws ChartDataException {
        ChartPoints chartData = chartRenderer.getChartGeometry().getChartData(this);
        double y = getY(chartData, x).doubleValue();
        if (stackBase != null) {
            y += stackBase.getY(x).doubleValue();
        }
        return y;
    }


    private Number getY(ChartPoints chartData, Number x) throws ChartDataException {
        Iterator<ChartPoint> it = chartData.iterator();
        while (it.hasNext()) {
            ChartPoint next = it.next();
            if (x.equals(next.getX())) {
                return next.getY();
            }
        }
        throw new ChartDataException("No y value for " + x);
    }


    public void draw(Graphics2D g2d, Layer layer, List<G> graphGeometry) {
        if (layer == Layer.FOREGROUND) {
            graphGeometry.forEach(areaGeometry -> draw(g2d, areaGeometry));
        }
    }


    protected void draw(Graphics2D g2d, G areaGeometry) {
        Shape area = areaGeometry.getArea();
        if (areaDrawStyle != null && area != null) {
            Paint paint = areaDrawStyle.getPaint(areaGeometry);
            if (paint != null) {
                g2d.setPaint(paint);
                g2d.fill(area);
            }
            Image image = areaDrawStyle.getImage();
            if (image != null) {
                Shape clipToRestore = g2d.getClip();
                g2d.setClip(area);
                g2d.drawImage(image, area.getBounds().x, area.getBounds().y, area.getBounds().width, area.getBounds().height, null);
                g2d.setClip(clipToRestore);
            }
            Paint borderPaint = areaDrawStyle.getBorderPaint(areaGeometry);
            Stroke borderStroke = areaDrawStyle.getBorderStroke(areaGeometry);
            if (borderPaint != null && borderStroke != null) {
                g2d.setPaint(borderPaint);
                g2d.setStroke(borderStroke);
                g2d.draw(area);
            }
        }
    }


    protected AreaDrawStyle<G> getAreaDrawStyle() {
        return areaDrawStyle;
    }


    protected ChartRenderer getChartRenderer() {
        return chartRenderer;
    }


    protected ChartGeometry.Window getWindow() {
        return window;
    }


    public boolean supportStack() {
        return true;
    }


    private ChartRenderer chartRenderer;
    private ChartGeometry.Window window;

    private final AreaDrawStyle<G> areaDrawStyle;
    private AbstractDataAreaRenderer stackBase;

}
