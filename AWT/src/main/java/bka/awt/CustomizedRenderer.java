/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt;

import java.awt.*;
import java.util.*;
import java.util.function.*;

/**
 * Component that can be painted on a <code>Graphics2D</code> canvas with customized paint attributes
 */
public class CustomizedRenderer implements Renderer {
    
    public CustomizedRenderer(PaintAttributes attributes, BiConsumer<PaintAttributes, Graphics2D> painter) {
        this.attributes = new PaintAttributes(attributes);
        this.painter = Objects.requireNonNull(painter);
    }

    public CustomizedRenderer(PaintAttributes attributes, CustomizedRenderer renderer) {
        this(attributes, renderer.painter);
    }

    @Override
    public void paint(Graphics2D graphics) {
        painter.accept(attributes, graphics);
    }
    
    public PaintAttributes getAttributes() {
        return new PaintAttributes(attributes);
    }
    
    private final PaintAttributes attributes;
    private final BiConsumer<PaintAttributes, Graphics2D> painter;
    
}
