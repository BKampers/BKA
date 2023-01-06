package bka.awt.clock;

import java.awt.geom.*;
import java.util.*;

public abstract class Ring implements Renderer {

    public Ring(Point2D center, int radius, Scale scale) {
        setCenter(center);
        setRadius(radius);
        setScale(scale);
    }

    public final void setCenter(Point2D center) {
        this.center = new Point2D.Double(center.getX(), center.getY());
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

    private Point2D.Double center;
    private double radius;
    private Scale scale;

}
