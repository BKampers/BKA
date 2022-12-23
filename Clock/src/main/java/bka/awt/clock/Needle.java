package bka.awt.clock;

import java.awt.*;
import java.util.*;


public abstract class Needle {

    protected Needle(Point turningPoint, Scale scale) {
        setTurningPoint(turningPoint);
        setScale(scale);
    }

    public void setValue(double value) {
        this.value = value;
    } 

    public final void setScale(Scale scale) {
        this.scale = Objects.requireNonNull(scale);
    }
    
    protected Scale getScale() {
        return scale;
    }

    public final void setTurningPoint(Point point) {
        turningPoint = Objects.requireNonNull(point);
    }

    protected final Point getTurningPoint() {
        return turningPoint;
    }
    
    public void paint(Graphics2D graphics) {
        double angle = scale.radians(value);
        graphics.rotate(angle, turningPoint.x, turningPoint.y);
        paintNeedle(graphics);
        graphics.rotate(-angle, turningPoint.x, turningPoint.y);
    }

    protected abstract void paintNeedle(Graphics2D g2d);
    
    private Point turningPoint;
    private Scale scale;
    private double value;

}
