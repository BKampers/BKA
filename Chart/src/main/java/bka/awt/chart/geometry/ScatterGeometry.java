/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.geometry;

import java.awt.*;


public class ScatterGeometry<S extends Shape> extends AreaGeometry<S> {


    public ScatterGeometry(Number x, Number y, S area, int count) {
        super(x, y, area);
        this.count = count;
    }

    
    public ScatterGeometry(Number x, Number y, S area) {
        this(x, y, area, 1);
    }


    public int getCount() {
        return count;
    }


    private final int count;


}
