package bka.awt.clock;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class MarkerRingRenderer extends IntervalRing {

    public MarkerRingRenderer(Point2D center, double radius, Scale scale, double interval, boolean itemsRotated, Renderer renderer) {
        this(center, radius, scale, interval, itemsRotated, (graphics, value) -> renderer.paint(graphics));
    }

    public MarkerRingRenderer(Point2D center, double radius, Scale scale, double interval, boolean itemsRotated, MarkerRenderer valueRenderer) {
        super(center, radius, scale, interval, itemsRotated);
        this.valueRenderer = Objects.requireNonNull(valueRenderer);
    }

    @Override
    protected void paintMarker(Graphics2D graphics, double value) {
        valueRenderer.paint(graphics, value);
    }

    private final MarkerRenderer valueRenderer;

}
