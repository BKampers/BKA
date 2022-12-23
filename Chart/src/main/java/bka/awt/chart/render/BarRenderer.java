/*
** Copyright © Bart Kampers
*/


package bka.awt.chart.render;

import bka.awt.chart.*;
import bka.awt.chart.custom.*;
import bka.awt.chart.geometry.*;
import java.awt.*;


public class BarRenderer extends CoordinateAreaRenderer<Rectangle> {


    public BarRenderer(int width, AreaDrawStyle drawStyle) {
        super(drawStyle);
        this.width = width;
    }


    public BarRenderer(int width, AreaDrawStyle drawStyle, Number base) {
        this(width, drawStyle);
        this.base = base;
    }


    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * Set horizontal shift of the bar w.r.t. the x value.
     * Useful when plotting multiple bar graphs into one chart.
     * @param shift
     */
    public void setShift(int shift) {
        this.shift = shift;
    }


    @Override
    public void addPointsInWindow(Object key, ChartPoints chartData) throws ChartDataException {
        ChartPoints graphPointsInWindow = new ChartPoints();
        for (ChartPoint element : chartData) {
            Number x = element.getX();
            if (getWindow().inXWindowRange(x)) {
                Number y = getY(element);
                graphPointsInWindow.add(x, y);
                getWindow().adjustXBounds(x);
                if (getWindow().inYWindowRange(y)) {
                    getWindow().adjustYBounds(y);
                }
            }
        }
        getWindow().putPoints(key, graphPointsInWindow);
    }


    @Override
    protected AreaGeometry<Rectangle> createSymbolGeometry(int x, int y) {
        return new AreaGeometry<>(null, null, createSymbolArea(x, y));
    }


    @Override
    protected Rectangle createArea(Number x, Number y) throws ChartDataException {
        int yPixel = getWindow().yPixel(y);
        int yBasePixel = (base == null) ? getWindow().getYPixelBottom() : getWindow().yPixel(base);
        if (yBasePixel < yPixel) {
            return createDownwardArea(x, yPixel, yBasePixel);
        }
        int barHeight = (getStackBase() != null) ? getWindow().yPixel(getStackBase().getY(x)) - yPixel : yBasePixel - yPixel;
        return createArea(getWindow().xPixel(x), yPixel, barHeight);
    }


    private Rectangle createDownwardArea(Number x, int yPixel, final int yBasePixel) throws ChartDataException {
        if (getStackBase() != null) {
            int barHeight = yPixel - getWindow().yPixel(getStackBase().getY(x));
            return createArea(getWindow().xPixel(x), getWindow().yPixel(getStackBase().getY(x)), barHeight);
        }
        return createArea(getWindow().xPixel(x), yBasePixel, yPixel - yBasePixel);
    }


    private Rectangle createArea(int x, int y, int barHeight) {
        int left = x - width / 2 + shift;
        return new Rectangle(left, y, width, barHeight);

    }


    @Override
    protected Rectangle createSymbolArea(int x, int y) {
        int symbolWidth = 10;
        int symbolHeight = 16;
        int left = x - symbolWidth / 2;
        int top = y - symbolHeight / 2;
        return new Rectangle(left, top, symbolWidth, symbolHeight);
    }


    private int width;
    private int shift;

    private Number base;
    
}
