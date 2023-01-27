/*
** © Bart Kampers
*/

package bka.awt.clock;

import bka.awt.*;
import java.awt.*;
import java.util.*;


public class ShapeRenderer implements Renderer {

    public ShapeRenderer(Shape shape) {
        this(shape, Color.BLACK);
    }

    public ShapeRenderer(Shape shape, Paint paint) {
        this(shape, paint, new BasicStroke(1f));
    }

    public ShapeRenderer(Shape shape, Paint paint, Stroke stroke) {
        this.shape = Objects.requireNonNull(shape);
        this.paint = Objects.requireNonNull(paint);
        this.stroke = Objects.requireNonNull(stroke);
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.setPaint(paint);
        graphics.setStroke(stroke);
        graphics.draw(shape);
    }

    private final Shape shape;
    private final Paint paint;
    private final Stroke stroke;

}
