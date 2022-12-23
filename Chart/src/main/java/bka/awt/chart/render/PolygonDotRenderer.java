/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;


import bka.awt.chart.custom.*;
import java.awt.*;


public class PolygonDotRenderer extends CoordinateAreaRenderer<Polygon> {
    
    
    public PolygonDotRenderer(Polygon polygon, AreaDrawStyle drawStyle) {
        super(drawStyle);
        this.polygon = polygon;
    }
    

    @Override
    protected Polygon createArea(Number x, Number y) {
        return createPolygon(getWindow().xPixel(x), getWindow().yPixel(y));
    }


    @Override
    protected Polygon createSymbolArea(int x, int y) {
        return createPolygon(x, y);
    }


    private Polygon createPolygon(int x, int y) {
        Polygon area = new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
        area.translate(x, y);
        return area;
    }


    private final Polygon polygon;
    
}
