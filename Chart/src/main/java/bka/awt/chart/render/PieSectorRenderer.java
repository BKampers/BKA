/*
** Copyright © Bart Kampers
*/


package bka.awt.chart.render;


import bka.awt.chart.*;
import bka.awt.chart.custom.*;
import bka.awt.chart.geometry.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;


public abstract class PieSectorRenderer extends AbstractDataAreaRenderer<ArcAreaGeometry> {


    PieSectorRenderer(PieDrawStyle drawStyle) {
        super(drawStyle);
    }


    @Override
    public List<ArcAreaGeometry> createGraphGeomerty(ChartPoints chart) {
        graphGeometry = new ArrayList<>();
        ChartRenderer RendererPanel = getChartRenderer();
        diameter = Math.min(RendererPanel.areaWidth(), RendererPanel.areaHeight()) - DIAMETER_MARGIN;
        float pieLeft = RendererPanel.areaLeft() + (RendererPanel.areaWidth() - diameter) / 2.0f;
        float pieTop = RendererPanel.areaTop() + (RendererPanel.areaHeight() - diameter) / 2.0f;
        float radius = diameter / 2.0f;
        center = new Point(Math.round(pieLeft + radius), Math.round(pieTop + radius));
        double previous = 0.0;
        double total = total(chart);
        int index = 0;
        for (ChartPoint element : chart) {
            double value = element.getY().doubleValue();
            double startAngle = previous / total * 360;
            double angularExtent = value / total * 360;
            Arc2D.Float arc = new Arc2D.Float(pieLeft, pieTop, diameter, diameter, (float) startAngle, (float) angularExtent, Arc2D.PIE);
            ArcAreaGeometry arcGeometry = new ArcAreaGeometry(element.getX(), element.getY(), arc, index);
            graphGeometry.add(arcGeometry);
            previous += value;
            index++;
        }
        return graphGeometry;
    }


    @Override
    public void addPointsInWindow(Object key, ChartPoints chartData) {
        getWindow().putPoints(key, chartData);
    }

    protected List<ArcAreaGeometry> getGraphGeometry() {
        return graphGeometry;
    }


    protected Point getCenter() {
        return new Point(center);
    }


    protected int getDiameter() {
        return diameter;
    }


    private double total(ChartPoints chart) {
        double total = 0.0;
        for (ChartPoint element : chart) {
            total += element.getY().doubleValue();
        }
        return total;
    }


    @Override
    public boolean supportStack() {
        return false;
    }


    private List<ArcAreaGeometry> graphGeometry;


    private Point center;
    private int diameter;

    
    private static final int DIAMETER_MARGIN = 50; // pixels

}
