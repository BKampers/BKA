/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.*;
import java.util.function.*;

public abstract class EdgeDecorationPaintable extends Paintable {
    
    protected EdgeDecorationPaintable(Supplier<Point> startPoint, Supplier<Point> endPoint) {
        this.startPoint = Objects.requireNonNull(startPoint);
        this.endPoint = Objects.requireNonNull(endPoint);
    }

    public Supplier<Point> getStartPoint() {
        return startPoint;
    }

    public Supplier<Point> getEndPoint() {
        return endPoint;
    }
    
    private final Supplier<Point> startPoint;
    private final Supplier<Point> endPoint;


    
}
