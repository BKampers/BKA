/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.geometry;


import java.awt.*;


/**
 * Holds the x-value, y-value and computed pixel of a point in a chart.
 * @param <S> Area (Shape) this date ocupies on the chart canvas
 */
public class PixelAreaGeometry<S extends Shape> extends AreaGeometry<S> {

    
    public PixelAreaGeometry(Number x, Number y, S area, Point pixel) {
        super(x, y, area);
        this.pixel = pixel;
    }
    
    
    public java.awt.Point getPixel() {
        return pixel;
    }


    private final Point pixel;
    
}
