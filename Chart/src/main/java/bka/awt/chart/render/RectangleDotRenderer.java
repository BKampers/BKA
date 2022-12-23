/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;


import bka.awt.chart.custom.*;
import java.awt.*;


public class RectangleDotRenderer extends PointRenderer<Rectangle> {
    
    
    public RectangleDotRenderer(int width, int height, AreaDrawStyle areaDrawStyle) {
        super(width, height, areaDrawStyle);
    }

    
    public RectangleDotRenderer(int size, AreaDrawStyle areaDrawStyle) {
        super(size, size, areaDrawStyle);
    }


    @Override
    protected Rectangle createShape(int x, int y) {
        return new Rectangle(x - getWidth() / 2, y - getHeight() / 2, getWidth(), getHeight());
    }
    
}
