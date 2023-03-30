/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.*;
import java.text.*;
import java.util.*;


public class FormattedValueRenderer implements MarkerRenderer {


    public FormattedValueRenderer(Paint paint, Font font) {
        this(paint, font, defaultFormat());
    }

    public FormattedValueRenderer(Paint paint, Font font, NumberFormat format) {
        setPaint(paint);
        this.font = Objects.requireNonNull(font);
        this.format = Objects.requireNonNull(format);
    }

    public final void setPaint(Paint paint) {
        this.paint = Objects.requireNonNull(paint);
    }

    @Override
    public void paint(Graphics2D graphics, double value) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        String text = format.format(value);
        graphics.setPaint(paint);
        graphics.setFont(font);
        TextRenderer.centerText(graphics, text);
    }

    private static NumberFormat defaultFormat() {
        NumberFormat defaultFormat = new DecimalFormat();
        defaultFormat.setGroupingUsed(false);
        return defaultFormat;
    }

    private Paint paint;
    private final Font font;
    private final NumberFormat format;

}
