/*
** Copyright © Bart Kampers
*/

package bka.awt.chart;

import java.util.*;


public class ChartPoint {


    public ChartPoint(Number x, Number y, boolean outsideWindow) {
        this.x = Objects.requireNonNull(x);
        this.y = Objects.requireNonNull(y);
        this.outsideWindow = outsideWindow;
    }


    public ChartPoint(Number x, Number y) {
        this(x, y, false);
    }

    
    public Number getX() {
        return x;
    }


    public Number getY() {
        return y;
    }


    public boolean isOutsideWindow() {
        return outsideWindow;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || object.getClass() != ChartPoint.class) {
            return false;
        }
        ChartPoint other = (ChartPoint) object;
        return x.equals(other.x) && y.equals(other.y);
    }


    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }


    private final Number x;
    private final Number y;
    private final boolean outsideWindow;

}
