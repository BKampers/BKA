/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;


public class NeedleImageRenderer extends ImageRenderer {

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

    private static Image transportedInstance(Image image, Point rotationPoint) {
        BufferedImage transported = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = transported.createGraphics();
        graphics.drawImage(image, -rotationPoint.x, -rotationPoint.y, null);
        graphics.dispose();
        return transported;
    }

    private final Point2D rotationPoint;

}
