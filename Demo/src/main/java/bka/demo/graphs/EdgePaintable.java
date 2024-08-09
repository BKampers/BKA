/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.graphcanvas.*;
import java.awt.*;
import java.util.*;
import java.util.function.*;


public class EdgePaintable extends Paintable {

    public EdgePaintable(Supplier<Point> start, Supplier<Point> end, boolean directed) {
        this(start, end, new ArrowheadPaintable(start, end), directed);
    }

    public EdgePaintable(Supplier<Point> start, Supplier<Point> end, EdgeDecorationPaintable decorationPaintable, boolean directed) {
        this.directed = directed;
        polygonPaintable = PolygonPaintable.create(java.util.List.of(start.get(), end.get()));
        this.decorationPaintable = (decorationPaintable != null)
            ? decorationPaintable
            : new ArrowheadPaintable(start, end);
    }

    @Override
    public void paint(Graphics2D graphics) {
        polygonPaintable.paint(graphics);
        if (directed) {
            decorationPaintable.paint(graphics);
        }
    }

    @Override
    public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
        polygonPaintable.paint(graphics, paint, stroke);
        if (directed) {
            decorationPaintable.paint(graphics, paint, stroke);
        }
    }

    @Override
    public Collection<Object> getPaintKeys() {
        Collection<Object> keys = new ArrayList<>(polygonPaintable.getPaintKeys());
        if (directed) {
            keys.addAll(decorationPaintable.getPaintKeys());
        }
        return keys;
    }

    @Override
    public Collection<Object> getStrokeKeys() {
        return java.util.List.of(PolygonPaintable.LINE_STROKE_KEY);
    }

    @Override
    public final void setPaint(Object key, Paint paint) {
        if (polygonPaintable.getPaintKeys().contains(key)) {
            polygonPaintable.setPaint(key, paint);
        }
        else if (decorationPaintable.getPaintKeys().contains(key)) {
            decorationPaintable.setPaint(key, paint);
        }
        else {
            throw new IllegalArgumentException(key.toString());
        }
    }

    @Override
    public final void setStroke(Object key, Stroke stroke) {
        if (polygonPaintable.getStrokeKeys().contains(key)) {
            polygonPaintable.setStroke(key, stroke);
        }
        else if (decorationPaintable.getStrokeKeys().contains(key)) {
            decorationPaintable.setStroke(key, stroke);
        }
        else {
            throw new IllegalArgumentException(key.toString());
        }
    }

    public PolygonPaintable getPolygonPaintable() {
        return polygonPaintable;
    }

    public EdgeDecorationPaintable getDecorationPaintable() {
        return decorationPaintable;
    }

    public boolean isDirected() {
        return directed;
    }

    private final PolygonPaintable polygonPaintable;
    private final EdgeDecorationPaintable decorationPaintable;
    private final boolean directed;

}
