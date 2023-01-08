/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.*;
import java.text.*;
import java.util.*;


public class FormattedValueRenderer implements ValueRenderer {

    public FormattedValueRenderer(Paint paint, Font font) {
        this(paint, font, defaultFormat());
    }

    public FormattedValueRenderer(Paint paint, Font font, NumberFormat format) {
        this.paint = Objects.requireNonNull(paint);
        this.font = Objects.requireNonNull(font);
        this.format = Objects.requireNonNull(format);
    }

    @Override
    public void paint(Graphics2D graphics, double value) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        String text = format.format(value);
        graphics.setPaint(Color.BLACK);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        FontMetrics fontMetrics = graphics.getFontMetrics();
        graphics.drawString(text, fontMetrics.stringWidth(text) / -2, fontMetrics.getHeight() / 2 - fontMetrics.getDescent());
    }

    private static NumberFormat defaultFormat() {
        NumberFormat defaultFormat = new DecimalFormat();
        defaultFormat.setGroupingUsed(false);
        return defaultFormat;
    }

    private final Paint paint;
    private final Font font;
    private final NumberFormat format;

}
