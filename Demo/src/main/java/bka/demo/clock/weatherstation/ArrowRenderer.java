/*
** © Bart Kampers
*/

package bka.demo.clock.weatherstation;

import bka.awt.*;
import java.awt.*;
import java.util.*;


public class ArrowRenderer implements Renderer {

    private ArrowRenderer(int radius, Paint initialPaint, Renderer renderer) {
        stroke = new BasicStroke(radius * 0.03f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        setPaint(initialPaint);
        this.renderer = renderer;
    }

    public static ArrowRenderer defaultArrowRenderer(int radius, Paint initialPaint) {
        return new ArrowRenderer(radius, initialPaint, graphics -> {
            int ratio = radius / 10;
            int offset = ratio / -2;
            int length = ratio * -7;
            graphics.fillOval(offset, offset, ratio, ratio);
            graphics.drawLine(0, ratio, 0, length);
            drawChevron(graphics, length, ratio / 10 * 4, ratio * -2);
        });
    }

    public static ArrowRenderer cardinalArrowRenderer(int radius, Paint initialPaint) {
        return new ArrowRenderer(radius, initialPaint, graphics -> {
            int ratio = radius / 10;
            int tailRatio = ratio / 2;
            graphics.fillPolygon(triangle(ratio));
            graphics.drawLine(0, 0, 0, ratio * -7);
            drawChevron(graphics, tailRatio * -12, tailRatio, ratio);
            drawChevron(graphics, tailRatio * -10, tailRatio, ratio);
        });
    }

    private static void drawChevron(Graphics2D graphics, int base, int width, int height) {
        graphics.drawLine(width, base - height, 0, base);
        graphics.drawLine(-width, base - height, 0, base);
    }

    private static Polygon triangle(int size) {
        return new Polygon(
            new int[]{ 0, -size, size },
            new int[]{ size, -size, -size },
            3);
    }

    public final void setPaint(Paint paint) {
        this.paint = Objects.requireNonNull(paint);
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.setPaint(paint);
        graphics.setStroke(stroke);
        renderer.paint(graphics);
    }

    private final Stroke stroke;
    private Paint paint;
    Renderer renderer;

}
