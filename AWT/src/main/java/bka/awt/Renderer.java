/*
** © Bart Kampers
*/

package bka.awt;

import java.awt.*;


public interface Renderer {

    void paint(Graphics2D graphics);

    public static Renderer rotated(Renderer renderer, double radians) {
        return graphics -> {
            graphics.rotate(radians);
            renderer.paint(graphics);
            graphics.rotate(-radians);
        };
    }

}
