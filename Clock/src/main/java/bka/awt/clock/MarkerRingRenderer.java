package bka.awt.clock;

import java.awt.*;
import java.util.*;

public class MarkerRingRenderer extends IntervalRing {

    public MarkerRingRenderer(Point center, int radius, Scale scale, double interval, boolean itemsRotated, Renderer renderer) {
        this(center, radius, scale, interval, itemsRotated, (graphics, value) -> renderer.paint(graphics));
    }

    public MarkerRingRenderer(Point center, int radius, Scale scale, double interval, boolean itemsRotated, ValueRenderer valueRenderer) {
        super(center, radius, scale, interval, itemsRotated);
        this.valueRenderer = Objects.requireNonNull(valueRenderer);
    }

    @Override
    protected void paintMarker(Graphics2D graphics, double value) {
        valueRenderer.paint(graphics, value);
    }

    private final ValueRenderer valueRenderer;

}
