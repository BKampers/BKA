/*
** © Bart Kampers
*/

package bka.awt;

import java.awt.*;
import java.util.*;

/**
 * Renders <type>Renderer</type> layers on a Graphics2D canvas.
 */
public class CompositeRenderer {

    /**
     * Paints all layers on given (@link Graphics2D) canvas. Layers are painted in order of addiditon, later added layers are painted o top of the
     * previous added layers.
     *
     * @param graphics
     */
    public void paint(Graphics2D graphics) {
        layers.forEach(renderer -> renderer.paint(graphics));
    }

    /**
     */
    public void add(Renderer renderer) {
        layers.add(renderer);
    }

    private final ArrayList<Renderer> layers = new ArrayList<>();

}
