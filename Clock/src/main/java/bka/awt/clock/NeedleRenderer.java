/*
** © Bart Kampers
*/

package bka.awt.clock;

import bka.awt.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;


public class NeedleRenderer implements Renderer {

    public NeedleRenderer(Point2D rotationPoint, Scale scale, Renderer renderer) {
        this.rotationPoint = rotationPoint;
        this.scale = scale;
        this.renderer = Objects.requireNonNull(renderer);
    }

    public static NeedleRenderer of(Point rotationPoint, Scale scale, int length) {
        return new NeedleRenderer(rotationPoint, scale, new ShapeRenderer(new Polygon(new int[]{ 0, 0 }, new int[]{ 0, -length }, 2)));
    }

    private void paintNeedle(Graphics2D graphics) {
        graphics.translate(rotationPoint.getX(), rotationPoint.getY());
        renderer.paint(graphics);
        graphics.translate(-rotationPoint.getX(), -rotationPoint.getY());
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public final void setScale(Scale scale) {
        this.scale = Objects.requireNonNull(scale);
    }

    public Scale getScale() {
        return scale;
    }

    protected final Point2D getRotationPoint() {
        return rotationPoint;
    }

    @Override
    public void paint(Graphics2D graphics) {
        double angle = scale.radians(value);
        graphics.rotate(angle, rotationPoint.getX(), rotationPoint.getY());
        paintNeedle(graphics);
        graphics.rotate(-angle, rotationPoint.getX(), rotationPoint.getY());
        graphics.translate(rotationPoint.getX(), rotationPoint.getY());
        graphics.translate(-rotationPoint.getX(), -rotationPoint.getY());
    }

    private final Point2D rotationPoint;
    private Scale scale;
    private double value;
    private final Renderer renderer;

}
