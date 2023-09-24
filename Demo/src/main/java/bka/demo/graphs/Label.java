/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
import java.util.function.*;


public class Label {

    public Label(Supplier<Point> positioner, String text) {
        this.positioner = positioner;
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void paint(Graphics2D graphics) {
        Point position = positioner.get();
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, position.x - metrics.stringWidth(text) / 2, position.y + metrics.getHeight() / 2);
    }

    private final Supplier<Point> positioner;
    private String text;
}
