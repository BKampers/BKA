/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;


public class NeedleImageRenderer extends ImageRenderer {

    public NeedleImageRenderer(Image image) {
        this(image, new Point(0, 0));
    }

    public NeedleImageRenderer(Image image, Point2D rotationPoint) {
        super(image);
        this.rotationPoint = Objects.requireNonNull(rotationPoint);
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.translate(-rotationPoint.getX(), -rotationPoint.getY());
        super.paint(graphics);
        graphics.translate(rotationPoint.getX(), rotationPoint.getY());
    }

    private final Point2D rotationPoint;

}
