/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;

import bka.awt.chart.custom.*;
import bka.awt.chart.geometry.*;
import java.awt.*;


public class DefaultGridRenderer extends GridRenderer {

    
    public DefaultGridRenderer(GridStyle gridStyle) {
        this.gridStyle = gridStyle;
    }
    
    
    @Override
    public void draw(Graphics2D g2d) {
        if (getPaintBox() != null) {
            paintGridAreas(g2d);
        }
        drawHorizontalLines(g2d);
        drawVerticalLines(g2d);
    }


    private void drawVerticalLines(Graphics2D g2d) {
        Stroke stroke = gridStyle.getYStroke();
        Paint paint = gridStyle.getYPaint();
        if (stroke != null && paint != null) {
            g2d.setPaint(paint);
            g2d.setStroke(stroke);
            int areaTop = getChartRenderer().areaTop();
            int areaHeight = getChartRenderer().areaHeight();
            int areaLeft = getChartRenderer().areaLeft();
            int areaRight = getChartRenderer().areaRight();
            ChartGeometry geometry = getChartRenderer().getChartGeometry();
            for (Number gridValue : geometry.getXGrid().getValues()) {
                int x = geometry.xPixel(gridValue);
                if (areaLeft <= x && x <= areaRight) {
                    g2d.drawLine(x, areaTop, x, areaTop + areaHeight);
                }
            }
        }
    }


    private void drawHorizontalLines(Graphics2D g2d) {
        Stroke stroke = gridStyle.getXStroke();
        Paint paint = gridStyle.getXPaint();
        if (stroke != null && paint != null) {
            g2d.setPaint(paint);
            g2d.setStroke(stroke);
            int areaTop = getChartRenderer().areaTop();
            int areaLeft = getChartRenderer().areaLeft();
            int areaRight = getChartRenderer().areaRight();
            ChartGeometry geometry = getChartRenderer().getChartGeometry();
            for (Number gridValue : geometry.getDefaultYGrid().getValues()) {
                int y = geometry.yPixel(gridValue);
                if (y >= areaTop) {
                    g2d.drawLine(areaLeft, y, areaRight, y);
                }
            }
        }
    }


    private void paintGridAreas(Graphics2D g2d) {
        switch (getChartRenderer().getGridMode()) {
            case X:
                drawVerticalGridAreas(g2d);
                break;
            case Y:
                drawHorizontalGridAreas(g2d);
                break;
            case NONE:
                break;
        }
    }


    private void drawVerticalGridAreas(Graphics2D g2d) {
        int areaTop = getChartRenderer().areaTop();
        int areaHeight = getChartRenderer().areaHeight();
        int areaLeft = getChartRenderer().areaLeft();
        int areaRight = getChartRenderer().areaRight();
        ChartGeometry geometry = getChartRenderer().getChartGeometry();
        java.util.List<Number> values = geometry.getXGrid().getValues();
        int count = values.size();
        for (int i = 1; i < count; ++i) {
            int paintLeft = geometry.xPixel(values.get(i - 1));
            int paintRight = geometry.xPixel(values.get(i));
            int left = Math.max(paintLeft, areaLeft);
            int right = Math.min(paintRight, areaRight);
            int width = right - left;
            if (width > 0) {
                Rectangle area = new Rectangle(paintLeft, areaTop, paintRight - paintLeft, areaHeight);
                g2d.setPaint(getPaintBox().getVerticalPaint(area, i));
                area = new Rectangle(left, areaTop, right - left, areaHeight);
                g2d.fill(area);
            }
        }
    }


    private void drawHorizontalGridAreas(Graphics2D g2d) {
        int areaTop = getChartRenderer().areaTop();
        int areaBottom = getChartRenderer().areaBottom();
        int areaLeft = getChartRenderer().areaLeft();
        int areaWidth = getChartRenderer().areaWidth();
        ChartGeometry geometry = getChartRenderer().getChartGeometry();
        java.util.List<Number> values = geometry.getYGrid(null).getValues();
        int count = values.size();
        for (int i = 1; i < count; ++i) {
            int paintBottom = geometry.yPixel(values.get(i - 1));
            int paintTop = geometry.yPixel(values.get(i));
            int bottom = Math.min(paintBottom, areaBottom);
            int top = Math.max(paintTop, areaTop);
            int height = bottom - top;
            if (height > 0) {
                Rectangle area = new Rectangle(areaLeft, paintTop, areaWidth, paintBottom - paintTop);
                g2d.setPaint(getPaintBox().getHorizontalPaint(area, i));
                area = new Rectangle(areaLeft, top, areaWidth, height);
                g2d.fill(area);
            }
        }
    }


    private GridStyle.PaintBox getPaintBox() {
        return gridStyle.getPaintBox();
    }


    private final GridStyle gridStyle;
    
}
