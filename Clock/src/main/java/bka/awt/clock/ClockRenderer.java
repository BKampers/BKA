package bka.awt.clock;

import bka.awt.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.function.*;

public class ClockRenderer extends CompositeRenderer {

    public ClockRenderer(Point center, Scale scale) {
        this.center = Objects.requireNonNull(center);
        this.scale = Objects.requireNonNull(scale);
    }

    public void addClockFace(int radius, Paint paint, Paint borderPaint, float borderWidth) {
        addClockFace(radius, paint);
        addClockFace(radius, borderPaint, borderWidth);
    }

    public void addClockFace(int radius, Paint paint) {
        Objects.requireNonNull(paint);
        add(graphics -> {
            graphics.setPaint(paint);
            graphics.fillOval(center.x - radius, center.y - radius, radius * 2, radius * 2);
        });
    }

    public void addClockFace(int radius, Paint borderPaint, float borderWidth) {
        Objects.requireNonNull(borderPaint);
        add(graphics -> {
            graphics.setPaint(borderPaint);
            graphics.setStroke(new BasicStroke(borderWidth));
            graphics.drawOval(center.x - radius, center.y - radius, radius * 2, radius * 2);
        });
    }

    public MarkerRingRenderer addMarkerRingRenderer(double radius, double interval, int length, Paint paint, float width) {
        return addMarkerRingRenderer(radius, interval, graphics -> {
            graphics.setPaint(paint);
            graphics.setStroke(roundedStroke(width));
            graphics.drawLine(0, 0, 0, length);
        });
    }

    public MarkerRingRenderer addMarkerRingRenderer(double radius, double minorInterval, int minorLength, int majorInterval, int majorLength, Paint paint, float width) {
        return addMarkerRingRenderer(radius, minorInterval, majorLength, minorLength, matches(majorInterval), paint, width);
    }

    private static Predicate<Double> matches(int majorInterval) {
        return value -> Math.round(value) % majorInterval == 0;
    }

    public MarkerRingRenderer addMarkerRingRenderer(double radius, double interval, int majorLength, int minorLength, Predicate<Double> isMajor, Paint paint, float width) {
        return addMarkerRingRenderer(radius, interval, (graphics, value) -> {
            graphics.setPaint(paint);
            graphics.setStroke(roundedStroke(width));
            if (isMajor.test(value)) {
                graphics.drawLine(0, 0, 0, majorLength);
            }
            else {
                graphics.drawLine(0, 0, 0, minorLength);
            }
        });
    }

    public MarkerRingRenderer addMarkerRingRenderer(double radius, double interval, Renderer renderer) {
        return addMarkerRingRenderer(radius, interval, (graphics, value) -> renderer.paint(graphics));
    }

    public MarkerRingRenderer addMarkerRingRenderer(double radius, double interval, MarkerRenderer markerRenderer) {
        MarkerRingRenderer markerRingRenderer = new MarkerRingRenderer(center, radius, scale, interval, true, markerRenderer);
        add(markerRingRenderer);
        return markerRingRenderer;
    }

    public MarkerRingRenderer addNumberRingRenderer(double radius, double interval, Paint paint) {
        return addNumberRingRenderer(radius, interval, paint, null);
    }

    public MarkerRingRenderer addNumberRingRenderer(double radius, double interval, Paint paint, Font font) {
        return addNonTiltedMarkerRingRenderer(radius, interval, new FormattedValueRenderer(paint, font));
    }

    public MarkerRingRenderer addNonTiltedMarkerRingRenderer(double radius, double interval, MarkerRenderer markerRenderer) {
        MarkerRingRenderer markerRingRenderer = new MarkerRingRenderer(center, radius, scale, interval, false, markerRenderer);
        add(markerRingRenderer);
        return markerRingRenderer;
    }

    public void addArc(double radius, double start, double end, Paint paint, float width) {
        addArc(radius, start, end, paint, roundedStroke(width));
    }

    public void addArc(double radius, double start, double end, Paint paint, Stroke stroke) {
        double diameter = radius * 2d;
        double startDegrees = scale.degrees(start);
        double endDegrees = scale.degrees(end);
        Arc2D arc = new Arc2D.Double(center.getX() - radius, center.getY() - radius, diameter, diameter, angleStart(startDegrees), angleExtent(startDegrees, endDegrees), Arc2D.OPEN);
        add(new ShapeRenderer(arc, paint, stroke));
    }

    private static double angleStart(double start) {
        return 90d - start;
    }

    private static double angleExtent(double start, double end) {
        double arc = start - end;
        return (arc <= 0.0) ? arc : -360d + arc;
    }

    public NeedleRenderer addNeedleRenderer(int length, Paint paint, float width) {
        return addNeedleRenderer(length, 0, paint, width);
    }

    public NeedleRenderer addNeedleRenderer(int length, int tailLength, Paint paint, float width) {
        return addNeedleRenderer(length, tailLength, paint, roundedStroke(width));
    }

    public NeedleRenderer addNeedleRenderer(int length, int tailLength, Paint paint, Stroke stroke) {
        return addNeedleRenderer(graphics -> {
            graphics.setPaint(paint);
            graphics.setStroke(stroke);
            graphics.drawLine(0, tailLength, 0, -length);
        });
    }

    public NeedleRenderer addNeedleRenderer(Renderer renderer) {
        NeedleRenderer needleRenderer = new NeedleRenderer(center, scale, renderer);
        add(needleRenderer);
        return needleRenderer;
    }

    private static Stroke roundedStroke(float width) {
        return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(graphics);
    }

    private final Point center;
    private final Scale scale;
    
}
