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


public abstract class CoordinateAreaRenderer<S extends Shape> extends AbstractDataAreaRendererBase<AreaGeometry<S>> {


    CoordinateAreaRenderer(AreaDrawStyle drawStyle) {
        super(drawStyle);
    }


    protected abstract S createArea(Number x, Number y) throws ChartDataException;
    protected abstract S createSymbolArea(int x, int y);


    @Override
    public List<AreaGeometry<S>> createGraphGeomerty(ChartPoints chart) throws ChartDataException {
        List<AreaGeometry<S>> graphGeometry = new ArrayList<>();
        for (ChartPoint element : chart) {
            if (! element.isOutsideWindow()) {
                Number x = element.getX();
                Number y = getY(element);
                graphGeometry.add(new AreaGeometry<>(x, y, createArea(x, y)));
            }
        }
        return graphGeometry;
    }


    @Override
    protected AreaGeometry<S> createSymbolGeometry(int x, int y) {
        return new AreaGeometry<>(null, null, createSymbolArea(x, y));
    }


}
