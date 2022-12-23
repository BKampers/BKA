/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;


import bka.awt.chart.*;
import bka.awt.chart.geometry.*;
import bka.awt.chart.grid.*;
import java.util.*;


public abstract class AxisRenderer {
    
    
    public abstract void drawXAxis(java.awt.Graphics2D g2d, Locale locale);
    public abstract void drawYAxis(java.awt.Graphics2D g2d, Locale locale);


    public AxisRenderer(ChartRenderer.AxisPosition position) {
        this(position, null);
    }


    public AxisRenderer(ChartRenderer.AxisPosition position, Object key) {
        this.position = Objects.requireNonNull(position);
        this.key = key;
    }


    public final Object getKey() {
        return key;
    }


    public final void setChartRenderer(ChartRenderer renderer) {
        chartRenderer = renderer;
        chartGeometry = chartRenderer.getChartGeometry();
    }


    public final void setTitle(String title) {
        this.title = title;
    }


    public final void setUnit(String unit) {
        this.unit = unit;
    }


    protected final String getTitle() {
        return title;
    }


    protected final String getUnit() {
        return unit;
    }


    protected final java.util.List<Grid.MarkerList> xMarkerLists() {
        return getMarketLists(chartGeometry.getXGrid());
    }


    protected final java.util.List<Grid.MarkerList> yMarkerLists() {
        Grid grid = chartGeometry.getYGrid(key);
        if (grid == null) {
            grid = chartGeometry.getDefaultYGrid();
        }
        return getMarketLists(grid);
    }


    private List<Grid.MarkerList> getMarketLists(Grid grid) {
        if (grid == null) {
            return Collections.emptyList();
        }
        return grid.getMarkerLists();
    }


    protected final int xPixel(Number value) {
        return chartGeometry.xPixel(value);
    }
    
    
    protected final int xMin() {
        return chartRenderer.areaLeft();
    }

    
    protected final int xMax() {
        return chartRenderer.areaRight();
    }

    
    protected final int x0() {
        int x = xPixel(0);
        if (chartRenderer.areaLeft() <= x && x <= chartRenderer.areaRight()) {
            return x;
        }
        return chartRenderer.areaLeft();
    }

    
    protected final int yPixel(Number value) {
        return chartGeometry.yPixel(key, value);
    }


    protected final int yMin() {
        return chartRenderer.areaBottom();
    }

    
    protected final int yMax() {
        return chartRenderer.areaTop();
    }

    
    protected final int y0() {
        int y = yPixel(0);
        if (chartRenderer.areaTop() <= y && y <= chartRenderer.areaBottom()) {
            return y;
        }
        return chartRenderer.areaBottom();
    }


    protected Range getXRange() {
        return chartGeometry.getXDataRange();
    }


    protected Range getYRange() {
        return chartGeometry.getYDataRanges().getDefault();
    }


    protected ChartRenderer.AxisPosition getPosition() {
        return position;
    }


    private final ChartRenderer.AxisPosition position;
    private final Object key;

    private String title;
    private String unit;

    private ChartRenderer chartRenderer;
    private ChartGeometry chartGeometry;

}
