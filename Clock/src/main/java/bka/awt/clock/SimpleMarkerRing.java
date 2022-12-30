package bka.awt.clock;

import java.awt.*;

public class SimpleMarkerRing extends MarkerRing {

    public SimpleMarkerRing(Point center, int radius, Scale scale) {
        this(center, radius, scale, 1.0);
    }

    public SimpleMarkerRing(Point center, int radius, Scale scale, double interval) {
        this(center, radius, scale, interval, true);
    }

    public SimpleMarkerRing(Point center, int radius, Scale scale, double interval, boolean itemsRotated) {
        super(center, radius, scale, interval, itemsRotated);
    }

    public void setMajorMarkerLength(int length) {
        majorMarkerLength = length;
    }
    
    public void setMinorMarkerLength(int length) {
        minorMarkerLength = length;
    }

    @Override
    public void paintMajorMarker(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.BLACK);
        graphics.drawLine(0, 0, 0, majorMarkerLength);
    }

    @Override
    public void paintMinorMarker(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.BLACK);
        graphics.drawLine(0, 0, 0, minorMarkerLength);
    }
    
    
    private int majorMarkerLength = 4;
    private int minorMarkerLength = 2;

}
