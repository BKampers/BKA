/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
import java.util.*;
import java.util.function.*;


public class Label {

    public Label(Supplier<Point> positioner, String text) {
        setPositioner(positioner);
        setText(text);
    }

    public final void setText(String text) {
        this.text = Objects.requireNonNull(text);
    }

    public String getText() {
        return text;
    }

    public final void setPositioner(Supplier<Point> positioner) {
        this.positioner = Objects.requireNonNull(positioner);
    }

    public Rectangle getBounds() {
        if (bounds == null) {
            return null;
        }
        return new Rectangle(bounds);
    }

    public void paint(Graphics2D graphics) {
        FontMetrics metrics = graphics.getFontMetrics();
        bounds = computeBounds(metrics);
        graphics.drawString(text, bounds.x, bounds.y + bounds.height - metrics.getDescent());
    }

    private Rectangle computeBounds(FontMetrics metrics) {
        Point position = positioner.get();
        int width = metrics.stringWidth(text);
        int height = metrics.getHeight();
        return new Rectangle(position.x - width / 2, position.y - height / 2, width, height);
    }

    private Supplier<Point> positioner;
    private String text;

    private Rectangle bounds;

}
