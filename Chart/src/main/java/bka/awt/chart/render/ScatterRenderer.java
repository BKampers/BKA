/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;

import bka.awt.chart.*;
import bka.awt.chart.custom.*;
import bka.awt.chart.geometry.*;
import java.awt.*;
import java.util.List;
import java.util.*;


public class ScatterRenderer<S extends Shape> extends AbstractDataAreaRendererBase<ScatterGeometry<S>> {


    public ScatterRenderer(AreaDrawStyle drawStyle, float sizeFactor) {
        super(drawStyle);
        this.sizeFactor = sizeFactor;
    }


    @Override
    public List<ScatterGeometry<S>> createGraphGeomerty(ChartPoints chart) {
        Map<ChartPoint, ScatterGeometry<S>> map = new HashMap<>();
        for (ChartPoint element : chart) {
            Number x = element.getX();
            Number y = element.getY();
            ScatterGeometry<S> scaterGeometry = map.get(element);
            int count = (scaterGeometry == null) ? 1 : scaterGeometry.getCount() + 1;
            Shape area = createShape(getWindow().xPixel(x), getWindow().yPixel(y), count * sizeFactor);
            scaterGeometry = new ScatterGeometry(x, y, area, count);
            map.put(element, scaterGeometry);
        }
        return new ArrayList<>(map.values());
    }


    @Override
    protected ScatterGeometry<S> createSymbolGeometry(int x, int y) {
        Shape shape = createShape(x, y, SYMBOL_SIZE);
        return new ScatterGeometry(x, y, shape);
    }


    private Shape createShape(float x, float y, float diameter) {
        float radius = diameter / 2.0f;
        return new java.awt.geom.Ellipse2D.Float(x - radius, y - radius, diameter, diameter);
    }


    @Override
    public boolean supportStack() {
        return false;
    }

    
    private final float sizeFactor;
    private static final float SYMBOL_SIZE = 10.0f;

}
