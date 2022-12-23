/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.geometry;

import java.awt.*;


/**
 * Information needed for rendering and highlighting data in a graph.
 * @param <S> Area (Shape) this data ocupies on the chart canvas
 */
public class AreaGeometry<S extends Shape> {


    public AreaGeometry(Number x, Number y, S area) {
        this.x = x;
        this.y = y;
        this.area = area;
    }


    public Number getX() {
        return x;
    }


    public Number getY() {
        return y;
    }


    public S getArea() {
        return area;
    }


    private final Number x;
    private final Number y;
    private final S area;
    
}
