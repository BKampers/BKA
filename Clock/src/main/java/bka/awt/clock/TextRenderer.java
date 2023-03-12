/*
** © Bart Kampers
*/

package bka.awt.clock;

import bka.awt.*;
import java.awt.*;
import java.util.*;


public class TextRenderer implements Renderer {

    public TextRenderer(Point point, String text, Paint paint) {
        this(point, text, null, paint);
    }

    public TextRenderer(Point point, String text, Font font, Paint paint) {
        setPoint(point);
        setText(text);
        setFont(font);
        setPaint(paint);
    }

    public final Point getPoint() {
        return new Point(point);
    }

    public final void setPoint(Point point) {
        this.point = new Point(point);
    }

    public final String getText() {
        return text;
    }

    public final void setText(String text) {
        this.text = Objects.requireNonNull(text);
    }

    public final Font getFont() {
        return font;
    }

    public final void setFont(Font font) {
        this.font = font;
    }

    public final Paint getPaint() {
        return paint;
    }

    public final void setPaint(Paint paint) {
        this.paint = Objects.requireNonNull(paint);
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (font != null) {
            graphics.setFont(font);
        }
        graphics.setPaint(paint);
        centerText(graphics, point.x, point.y, text);
    }

    public static void centerText(Graphics2D graphics, String text) {
        centerText(graphics, 0, 0, text);
    }

    public static void centerText(Graphics2D graphics, int x, int y, String text) {
        FontMetrics fontMetrics = graphics.getFontMetrics();
        graphics.drawString(text, x - fontMetrics.stringWidth(text) / 2, y + fontMetrics.getHeight() / 2 - fontMetrics.getDescent());
    }

    private Point point;
    private String text;
    private Font font;
    private Paint paint;

}
