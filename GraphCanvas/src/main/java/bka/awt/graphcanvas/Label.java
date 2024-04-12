/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.*;
import java.util.function.*;


public class Label {

    public Label(GraphComponent element, Supplier<Point> positioner, String text) {
        this.element = Objects.requireNonNull(element);
        setPositioner(positioner);
        setText(text);
    }

    public void moveTo(GraphComponent element, Supplier<Point> positioner) {
        this.element.removeLabel(this);
        this.element = Objects.requireNonNull(element);
        this.element.addLabel(this);
        setPositioner(positioner);
    }

    public GraphComponent getElement() {
        return element;
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

    public Supplier<Point> getPositioner() {
        return positioner;
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
        graphics.setPaint(Color.BLACK);
        graphics.drawString(text, bounds.x, bounds.y + bounds.height - metrics.getDescent());
    }

    private Rectangle computeBounds(FontMetrics metrics) {
        Point position = positioner.get();
        int width = metrics.stringWidth(text);
        int height = metrics.getHeight();
        return new Rectangle(position.x - width / 2, position.y - height / 2, width, height);
    }

    private GraphComponent element;

    private Supplier<Point> positioner;
    private String text;

    private Rectangle bounds;

}
