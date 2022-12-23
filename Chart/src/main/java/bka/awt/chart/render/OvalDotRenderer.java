/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.render;


import bka.awt.chart.custom.*;
import java.awt.geom.*;


public class OvalDotRenderer extends PointRenderer<Ellipse2D.Float> {
    
    
    public OvalDotRenderer(int width, int height, AreaDrawStyle drawStyle) {
        super(width, height, drawStyle);
    }
    
    
    public OvalDotRenderer(int size, AreaDrawStyle drawStyle) {
        super(size, size, drawStyle);
    }
    

    @Override
    protected Ellipse2D.Float createShape(int x, int y) {
        return new Ellipse2D.Float(x - getWidth() / 2.0f, y - getHeight() / 2.0f, getWidth(), getHeight());
    }

}
