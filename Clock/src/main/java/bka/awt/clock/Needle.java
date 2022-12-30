package bka.awt.clock;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;


public abstract class Needle {

    protected Needle(Point2D rotationPoint, Scale scale) {
        setRotationPoint(rotationPoint);
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

    public final void setRotationPoint(Point2D point) {
        rotationPoint = Objects.requireNonNull(point);
    }

    protected final Point2D getRotationPoint() {
        return rotationPoint;
    }
    
    public void paint(Graphics2D graphics) {
        graphics.translate(rotationPoint.getX(), rotationPoint.getY());
        graphics.drawLine(0, -5, 0, 5);
        graphics.drawLine(-5, 0, 5, 0);
        graphics.translate(-rotationPoint.getX(), -rotationPoint.getY());
        double angle = scale.radians(value);
        graphics.rotate(angle, rotationPoint.getX(), rotationPoint.getY());
        paintNeedle(graphics);
        graphics.rotate(-angle, rotationPoint.getX(), rotationPoint.getY());
    }

    protected abstract void paintNeedle(Graphics2D graphics);
    
    private Point2D rotationPoint;
    private Scale scale;
    private double value;

}
