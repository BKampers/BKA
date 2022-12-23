package bka.awt.clock;

import java.awt.*;
import java.util.*;


public class PolygonNeedle extends Needle {

    public PolygonNeedle(Point turningPoint, Scale scale, int length) {
        this(turningPoint, scale, new Polygon(new int[]{ 0, 0 }, new int[]{ 0, -length, }, 2));
    }

    public PolygonNeedle(Point turningPoint, Scale scale, Polygon polygon) {
        super(turningPoint, scale);
        setPolygon(polygon);
    }

    public final void setPolygon(Polygon polygon) {
        this.polygon = Objects.requireNonNull(polygon);
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    @Override
    public void paintNeedle(Graphics2D graphics) {
        Stroke restoreStroke = null;
        if (stroke != null) {
            restoreStroke = graphics.getStroke();
        }
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (paint != null) {
            graphics.setPaint(paint);
        }
        if (stroke != null) {
            graphics.setStroke(stroke);
        }
        Point turningPoint = getTurningPoint();
        graphics.translate(turningPoint.x, turningPoint.y);
        graphics.drawPolygon(polygon);
        graphics.translate(-turningPoint.x, -turningPoint.y);
        if (restoreStroke != null) {
            graphics.setStroke(restoreStroke);
        }
    }

    private Polygon polygon;
    private Paint paint;
    private Stroke stroke;

}
