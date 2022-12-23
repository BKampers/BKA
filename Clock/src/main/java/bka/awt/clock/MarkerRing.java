package bka.awt.clock;

import java.awt.*;

public abstract class MarkerRing extends IntervalRing {

    protected MarkerRing(Point center, int radius, Scale scale, double interval, boolean itemsRotated) {
        super(center, radius, scale, interval, itemsRotated);
    }

    @Override
    public void setInterval(double interval) {
        super.setInterval(interval);
        tolerance = interval / 2.0;
    }

    public void setMajorInterval(double interval) {
        majorInterval = interval;
    }

    @Override
    protected void paintMarker(Graphics2D graphics, double value) {
        long majorValue = Math.round(value / majorInterval);
        double distance = value - majorValue * majorInterval;
        if (-tolerance < distance && distance < tolerance) {
            paintMajorMarker(graphics);
        }
        else {
            paintMinorMarker(graphics);
        }
    }
    
    public abstract void paintMajorMarker(Graphics2D graphics);

    public abstract void paintMinorMarker(Graphics2D graphics);

    private double majorInterval = 1.0;
    private double tolerance = 0.5;

}
