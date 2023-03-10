/*
** © Bart Kampers
*/

package bka.awt.clock;

import bka.awt.*;
import java.awt.*;
import java.util.*;


public class TextRenderer implements Renderer {

    public TextRenderer(Point point, String text, Font font, Paint paint) {
        this.point = Objects.requireNonNull(point);
        this.text = Objects.requireNonNull(text);
        this.font = font;
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

    private final Point point;
    private final String text;
    private final Font font;
    private final Paint paint;

}
