/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.*;
import java.text.*;
import java.util.*;


public class FormattedValueRenderer implements MarkerRenderer {

    public FormattedValueRenderer(Paint paint) {
        this(paint, null);
    }

    public FormattedValueRenderer(Paint paint, Font font) {
        this(paint, font, defaultFormat());
    }

    public FormattedValueRenderer(Paint paint, Font font, NumberFormat format) {
        this.paint = Objects.requireNonNull(paint);
        this.font = font;
        this.format = Objects.requireNonNull(format);
    }

    private static NumberFormat defaultFormat() {
        NumberFormat defaultFormat = new DecimalFormat();
        defaultFormat.setGroupingUsed(false);
        return defaultFormat;
    }

    public void setPaint(Paint paint) {
        this.paint = Objects.requireNonNull(paint);
    }

    @Override
    public void paint(Graphics2D graphics, double value) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        String text = format.format(value);
        graphics.setPaint(paint);
        if (font != null) {
            graphics.setFont(font);
        }
        TextRenderer.centerText(graphics, text);
    }

    private Paint paint;
    private final Font font;
    private final NumberFormat format;

}
