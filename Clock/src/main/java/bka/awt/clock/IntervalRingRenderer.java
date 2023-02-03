/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.*;
import java.awt.geom.*;


public abstract class IntervalRingRenderer extends RingRenderer {

    protected IntervalRingRenderer(Point2D center, double radius, Scale scale, double interval, boolean markersRotated) {
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
        Point2D center = getCenter();
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

    private void paintRotatedMarker(Graphics2D graphics, Point2D center, Scale scale, double value) {
        double angle = scale.radians(value);
        graphics.rotate(angle, center.getX(), center.getY());
        graphics.translate(center.getX(), center.getY() - getRadius());
        paintMarker(graphics, value);
        graphics.translate(-center.getX(), -(center.getY() - getRadius()));
        graphics.rotate(-angle, center.getX(), center.getY());
    }

    private void paintUprightMarker(Graphics2D graphics, double value) {
        Point.Double translation = markerPoint(value);
        graphics.translate(translation.getX(), translation.getY());
        paintMarker(graphics, value);
        graphics.translate(-translation.getX(), -translation.getY());
    }

    protected abstract void paintMarker(Graphics2D graphics, double value);

    private Point.Double markerPoint(double value) {
        double angle = getScale().radians(value);
        return new Point.Double(
            getCenter().getX() + Math.sin(angle) * getRadius(),
            getCenter().getY() - Math.cos(angle) * getRadius());
    }

    private double interval;
    private boolean markersRotated;

}
