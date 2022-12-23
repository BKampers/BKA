/*
** Copyright © Bart Kampers
*/

package bka.awt.chart.geometry;


import java.awt.geom.*;


public class ArcAreaGeometry extends AreaGeometry<Arc2D.Float> {

    public ArcAreaGeometry(Number x, Number y, Arc2D.Float area, int index) {
        super(x, y, area);
        this.index = index;
    }


    public int getIndex() {
        return index;
    }


    private final int index;
    
}
