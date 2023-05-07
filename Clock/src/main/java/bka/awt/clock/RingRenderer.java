/*
** © Bart Kampers
 */
package bka.awt.clock;

import bka.awt.*;
import java.awt.geom.*;
import java.util.*;

public abstract class RingRenderer implements Renderer {

    public RingRenderer(Point2D center, double radius, Scale scale) {
        setCenter(center);
        setRadius(radius);
        setScale(scale);
    }

    public final void setCenter(Point2D center) {
        this.center = Objects.requireNonNull(center);
    }

    public final void setRadius(double radius) {
        this.radius = radius;
    }

    public final void setScale(Scale scale) {
        this.scale = Objects.requireNonNull(scale);
    }
    
    protected Point2D getCenter() {
        return center;
    }

    protected Scale getScale() {
        return scale;
    }

    protected double getRadius() {
        return radius;
    }

    protected double getDiameter() {
        return radius * 2.0;
    }

    private Point2D center;
    private double radius;
    private Scale scale;

}
