/*
** © Bart Kampers
*/

package bka.awt.clock;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;


public class MappedRingRenderer extends RingRenderer {

    public MappedRingRenderer(Point2D center, double radius, Scale scale, Map<Double, MarkerRenderer> map) {
        super(center, radius, scale);
        this.map = Objects.requireNonNull(map);
    }

    @Override
    public void paint(Graphics2D graphics) {
        map.forEach((value, renderer) -> {
            renderer.paint(graphics, value);
        });
    }

    private final Map<Double, MarkerRenderer> map;

}
