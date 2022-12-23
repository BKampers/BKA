package bka.awt.clock;

import java.awt.*;
import java.util.*;

public abstract class Ring {

    public Ring(Point center, int radius, Scale scale) {
        setCenter(center);
        setRadius(radius);
        setScale(scale);
    }

    public abstract void paint(Graphics2D graphics);

    public final void setCenter(Point center) {
        this.center = Objects.requireNonNull(center);
    }

    public final void setRadius(int radius) {
        this.radius = radius;
    }

    public final void setScale(Scale scale) {
        this.scale = Objects.requireNonNull(scale);
    }
    
    protected Point getCenter() {
        return center;
    }

    protected Scale getScale() {
        return scale;
    }

    protected int getRadius() {
        return radius;
    }

    private int radius;
    private Scale scale;
    private Point center;
    
}
