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


public abstract class LineRenderer extends AbstractDataAreaRendererBase<PixelAreaGeometry> {


    LineRenderer(LineDrawStyle drawStyle, CoordinateAreaRenderer markerRenderer) {
        super(drawStyle.getMarkerDrawStyle());
        this.lineDrawStyle = drawStyle;
        this.markerRenderer = markerRenderer;
    }

    
    @Override
    public List<PixelAreaGeometry> createGraphGeomerty(ChartPoints chart) throws ChartDataException {
        List<PixelAreaGeometry> graphGeometry = new ArrayList<>();
        for (ChartPoint element : chart) {
            add(element, graphGeometry);
        }
        return graphGeometry;
    }


    @Override
    public void addPointsInWindow(Object key, ChartPoints chartData) throws ChartDataException {
        ChartGeometry.Window window = getWindow();
        ChartPoint[] elements = new ChartPoint[chartData.size()];
        int i = 0;
        for (ChartPoint element : chartData) {
            Number x = element.getX();
            Number y = getY(element);
            boolean inRange = window.inXWindowRange(x) && window.inYWindowRange(y);
            elements[i] = new ChartPoint(x, y,  ! inRange);
            i++;
        }
        ChartPoints graphPointsInWindow = new ChartPoints();
        int start = xRangeStartIndex(elements, window);
        if (start >= 0) {
            int end = xRangeEndIndex(elements, window, start);
            if (start > 0) {
                graphPointsInWindow.add(elements[start - 1]);
            }
            for (i = start; i <= end; ++i) {
                ChartPoint element = elements[i];
                if (i == start || i == end || ! element.isOutsideWindow() || ! elements[i - 1].isOutsideWindow() || ! elements[i + 1].isOutsideWindow()) {
                    graphPointsInWindow.add(element);
                    if (! element.isOutsideWindow()) {
                        window.adjustBounds(element.getX(), getY(element));
                    }
                }
            }
            if (end < elements.length - 1) {
                graphPointsInWindow.add(elements[end + 1]);
            }
        }
        window.putPoints(key, graphPointsInWindow);
    }


    private static int xRangeStartIndex(ChartPoint[] elements, ChartGeometry.Window window) {
        for (int i = 0; i < elements.length; ++i) {
            if (window.inXWindowRange(elements[i].getX())) {
                return i;
            }
        }
        return -1;
    }


    private static int xRangeEndIndex(ChartPoint[] elements, ChartGeometry.Window window, int startIndex) {
        for (int i = startIndex; i < elements.length; ++i) {
            if (! window.inXWindowRange(elements[i].getX())) {
                return i - 1;
            }
        }
        return elements.length - 1;
    }


    private void add(ChartPoint element, List<PixelAreaGeometry> dataGeometry) throws ChartDataException {
        add(element.getX(), getY(element), dataGeometry, ! element.isOutsideWindow());
    }


    private void add(Number x, Number y, List<PixelAreaGeometry> dataGeometry, boolean createArea) {
        int xPixel = getWindow().xPixel(x);
        int yPixel = getWindow().yPixel(y);
        Shape area = (createArea) ? createArea(xPixel, yPixel) : null;
        dataGeometry.add(new PixelAreaGeometry(x, y, area, new Point(xPixel, yPixel)));
    }

    
    protected Shape createSymbolArea(int x, int y) {
        return createArea(x, y);
    }


    private Shape createArea(int x, int y) {
        if (markerRenderer == null) {
            return null;
        }
        return markerRenderer.createSymbolArea(x, y);
    }



    protected final LineDrawStyle lineDrawStyle;
    private final CoordinateAreaRenderer markerRenderer;
    
}
