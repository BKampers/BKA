/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;


import bka.awt.chart.custom.*;
import java.awt.*;


public abstract class PointRenderer<S extends Shape> extends CoordinateAreaRenderer<S> {


    protected PointRenderer(int width, int height, AreaDrawStyle drawStyle) {
        super(drawStyle);
        this.width = width;
        this.height = height;
    }


    protected int getWidth() {
        return width;
    }

    
    protected int getHeight() {
        return height;
    }


    @Override
    protected S createArea(Number x, Number y) {
        return createShape(getWindow().xPixel(x), getWindow().yPixel(y));
    }


    @Override
    protected S createSymbolArea(int x, int y) {
        return createShape(x, y);
    }

    
    protected abstract S createShape(int x, int y);


    private final int width;
    private final int height;

}
