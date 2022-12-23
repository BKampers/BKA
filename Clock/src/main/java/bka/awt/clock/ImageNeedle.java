package bka.awt.clock;

import java.awt.*;
import java.util.*;


public class ImageNeedle extends Needle {
    
    public ImageNeedle(Point turningPoint, Scale scale, Image image, Point turningOffset) {
        super(turningPoint, scale);
        setImage(image);
        setTurningOffset(turningOffset);
    }

    public final void setImage(Image image) {
        this.image = Objects.requireNonNull(image);
    }

    public final void setTurningOffset(Point point) {
        turningOffset = Objects.requireNonNull(point);
    }
    
    @Override
    public void paintNeedle(Graphics2D graphics) {
        Point turningPoint = getTurningPoint();
        graphics.drawImage(image, turningPoint.x - turningOffset.x, turningPoint.y - turningOffset.y, null);
    }
    
    private Image image;
    private Point turningOffset;

}
