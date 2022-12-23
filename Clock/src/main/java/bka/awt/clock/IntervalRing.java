/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.*;


public abstract class IntervalRing extends Ring {

    protected IntervalRing(Point center, int radius, Scale scale, double interval, boolean markersRotated) {
        super(center, radius, scale);
        this.interval = interval;
        this.markersRotated = markersRotated;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    protected double getInterval() {
        return interval;
    }

    public void setMarkersRotated(boolean rotated) {
        markersRotated = rotated;
    }

    @Override
    public void paint(Graphics2D graphics) {
        if (interval <= 0.0) {
            return;
        }
        Scale scale = getScale();
        Point center = getCenter();
        double minValue = Math.min(scale.getMinValue(), scale.getMaxValue());
        double maxValue = Math.max(scale.getMinValue(), scale.getMaxValue());
        for (double value = minValue; value <= maxValue; value += interval) {
            if (markersRotated) {
                paintRotatedMarker(graphics, center, scale, value);
            }
            else {
                paintUprightMarker(graphics, value);
            }
        }
    }

    private void paintRotatedMarker(Graphics2D graphics, Point center, Scale scale, double value) {
        double angle = scale.radians(value);
        graphics.rotate(angle, center.x, center.y);
        graphics.translate(center.x, center.y - getRadius());
        paintMarker(graphics, value);
        graphics.translate(-center.x, -(center.y - getRadius()));
        graphics.rotate(-angle, center.x, center.y);
    }

    private void paintUprightMarker(Graphics2D graphics, double value) {
        Point.Double translation = markerPoint(value);
        graphics.translate(translation.x, translation.y);
        paintMarker(graphics, value);
        graphics.translate(-translation.x, -translation.y);
    }

    protected abstract void paintMarker(Graphics2D graphics, double value);

    private Point.Double markerPoint(double value) {
        double angle = getScale().radians(value);
        return new Point.Double(
            getCenter().x + Math.sin(angle) * getRadius(),
            getCenter().y - Math.cos(angle) * getRadius());
    }

    private double interval;
    private boolean markersRotated;

}
