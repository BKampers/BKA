/*
** © Bart Kampers
*/

package bka.awt;

import java.awt.*;
import java.util.*;


public class TiltedRenderer implements Renderer {

    public TiltedRenderer(Renderer renderer, double angle) {
        this.renderer = Objects.requireNonNull(renderer);
        this.angle = angle;
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.rotate(angle);
        renderer.paint(graphics);
        graphics.rotate(-angle);
    }

    private final Renderer renderer;
    private final double angle; // radians

}
