package bka.awt.clock;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public abstract class Ring {

    public Ring(Point center, int radius, Scale scale) {
        setCenter(center);
        setRadius(radius);
        setScale(scale);
    }

    public abstract void paint(Graphics2D graphics);

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

    private double radius;
    private Scale scale;
    private Point2D.Double center;
    
}
