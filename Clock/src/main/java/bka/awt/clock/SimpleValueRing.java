package bka.awt.clock;

import java.awt.*;
import java.util.*;


public class SimpleValueRing extends ValueRing {

    public SimpleValueRing(Point center, int radius, Scale scale) {
        this(center, radius, scale, 1.0);
    }

    public SimpleValueRing(Point center, int radius, Scale scale, double interval) {
        this(center, radius, scale, interval, false);
    }

    public SimpleValueRing(Point center, int radius, Scale scale, double interval, boolean itemsRotated) {
        this(center, radius, scale, interval, itemsRotated, new Font(Font.SANS_SERIF, Font.BOLD, 50), Color.BLACK);
    }

    public SimpleValueRing(Point center, int radius, Scale scale, Font font, Paint paint) {
        this(center, radius, scale, 1.0, font, paint);
    }

    public SimpleValueRing(Point center, int radius, Scale scale, double interval, Font font, Paint paint) {
        this(center, radius, scale, interval, false, font, paint);
    }

    public SimpleValueRing(Point center, int radius, Scale scale, double interval, boolean itemsRotated, Font font, Paint paint) {
        super(center, radius, scale, interval, itemsRotated);
        setFont(font);
        setPaint(paint);
    }

    public final void setFont(Font font) {
        this.font = Objects.requireNonNull(font);
    }

    public final void setPaint(Paint paint) {
        this.paint = Objects.requireNonNull(paint);
    }

    @Override
    public void paintValue(Graphics2D graphics, String value) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(font);
        graphics.setPaint(paint);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int width = fontMetrics.stringWidth(value);
        int height = fontMetrics.getHeight();
        int descent = fontMetrics.getDescent();
        graphics.drawString(value, width / -2, height / 2 - descent);
    }

    private Font font;
    private Paint paint;

}
