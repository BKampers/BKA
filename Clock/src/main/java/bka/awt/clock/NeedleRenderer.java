/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;


public class NeedleRenderer extends Needle {

    public NeedleRenderer(Point rotationPoint, Scale scale, Renderer renderer) {
        super(rotationPoint, scale);
        this.renderer = Objects.requireNonNull(renderer);
    }

    public static NeedleRenderer of(Point rotationPoint, Scale scale, int length) {
        return new NeedleRenderer(rotationPoint, scale, new ShapeRenderer(new Polygon(new int[]{ 0, 0 }, new int[]{ 0, -length }, 2)));
    }

    @Override
    protected void paintNeedle(Graphics2D graphics) {
        Point2D rotationPoint = getRotationPoint();
        graphics.translate(rotationPoint.getX(), rotationPoint.getY());
        renderer.paint(graphics);
        graphics.translate(-rotationPoint.getX(), -rotationPoint.getY());
    }

    private final Renderer renderer;

}
