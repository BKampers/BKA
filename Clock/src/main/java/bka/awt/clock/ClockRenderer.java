package bka.awt.clock;

import java.awt.*;
import java.util.*;
import java.util.function.*;

public class ClockRenderer {

    public ClockRenderer(Point center, Scale scale) {
        this.center = Objects.requireNonNull(center);
        this.scale = Objects.requireNonNull(scale);
    }

    public Scale getScale() {
        return scale;
    }

    public void add(Renderer renderer) {
        components.add(renderer);
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

    public MarkerRingRenderer addMarkerRingRenderer(double radius, double interval, int length) {
        return addMarkerRingRenderer(radius, interval, graphics -> graphics.drawLine(0, 0, 0, length));
    }

    public MarkerRingRenderer addMarkerRingRenderer(double radius, double interval, int majorLength, int minorLength, int majorInterval) {
        return addMarkerRingRenderer(radius, interval, majorLength, minorLength, value -> Math.round(value) % majorInterval == 0);
    }

    public MarkerRingRenderer addMarkerRingRenderer(double radius, double interval, int majorLength, int minorLength, Predicate<Double> isMajor) {
        return addMarkerRingRenderer(radius, interval, (graphics, value) -> {
            if (isMajor.test(value)) {
                graphics.drawLine(0, 0, 0, majorLength);
            }
            graphics.drawLine(0, 0, 0, minorLength);
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

    public MarkerRingRenderer addNonTiltedMarkerRingRenderer(double radius, double interval, MarkerRenderer markerRenderer) {
        MarkerRingRenderer markerRingRenderer = new MarkerRingRenderer(center, radius, scale, interval, false, markerRenderer);
        add(markerRingRenderer);
        return markerRingRenderer;
    }

    public void addArcRingRenderer(int radius, Collection<ArcRing.Arc> arcs) {
        add(new ArcRing(center, radius, scale, arcs));
    }

    public NeedleRenderer addNeedleRenderer(int length, Paint paint, float width) {
        return addNeedleRenderer(length, 0, paint, width);
    }

    public NeedleRenderer addNeedleRenderer(int length, int tailLength, Paint paint, float width) {
        return addNeedleRenderer(length, tailLength, paint, new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
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

    public void paint(Graphics2D graphics) {
        components.forEach(renderer -> renderer.paint(graphics));
    }

    private final Point center;
    private final Scale scale;

    private final ArrayList<Renderer> components = new ArrayList<>();
    
}
