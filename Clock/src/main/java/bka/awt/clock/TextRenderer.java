/*
** © Bart Kampers
*/

package bka.awt.clock;

import bka.awt.*;
import java.awt.*;
import java.util.*;


public class TextRenderer implements Renderer {

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
        this.font = Objects.requireNonNull(font);
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
        graphics.setFont(font);
        graphics.setPaint(paint);
        centerText(graphics, point.x, point.y, text);
    }

    /**
     * Draw text centered around the origin of graphics
     *
     * @param graphics
     * @param text
     */
    public static void centerText(Graphics2D graphics, String text) {
        centerText(graphics, 0, 0, text);
    }

    /**
     * Draw text centered around point x,y
     *
     * @param graphics
     * @param x
     * @param y
     * @param text
     */
    public static void centerText(Graphics2D graphics, int x, int y, String text) {
        Point offset = centerPoint(graphics, text);
        graphics.drawString(text, x - offset.x, y + offset.y);
    }

    /**
     * Computes coordinate to draw given texts at in order to center it
     *
     * @param graphics to draw text on
     * @param text to draw
     * @return offset point
     */
    public static Point centerPoint(Graphics2D graphics, String text) {
        FontMetrics fontMetrics = graphics.getFontMetrics();
        return new Point(fontMetrics.stringWidth(text) / 2, fontMetrics.getHeight() / 2 - fontMetrics.getDescent());
    }

    private Point point;
    private String text;
    private Font font;
    private Paint paint;

}
