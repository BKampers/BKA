/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.graphcanvas.*;
import bka.demo.graphs.EdgePaintable;
import java.awt.*;
import java.util.*;
import java.util.function.*;


public class EdgeFactory implements Factory {

    public EdgeFactory(EdgeDecorationPaintable decorationPaintable) {
        defaultInstance = new EdgePaintable(() -> ICON_MID_LEFT, () -> ICON_MID_RIGHT, decorationPaintable, true);
        defaultInstance.getPolygonPaintable().setPaint(PolygonPaintable.LINE_PAINT_KEY, Color.BLACK);
        defaultInstance.getPolygonPaintable().setStroke(PolygonPaintable.LINE_STROKE_KEY, SOLID_STROKE);
    }

    public EdgeFactory(boolean directed) {
        defaultInstance = new EdgePaintable(() -> ICON_MID_LEFT, () -> ICON_MID_RIGHT, directed);
        defaultInstance.getPolygonPaintable().setPaint(PolygonPaintable.LINE_PAINT_KEY, Color.BLACK);
        defaultInstance.getPolygonPaintable().setStroke(PolygonPaintable.LINE_STROKE_KEY, SOLID_STROKE);
        defaultInstance.getDecorationPaintable().setPaint(ArrowheadPaintable.ARROWHEAD_PAINT_KEY, Color.BLACK);
        defaultInstance.getDecorationPaintable().setStroke(ArrowheadPaintable.ARROWHEAD_STROKE_KEY, SOLID_STROKE);
    }

    public EdgeFactory(EdgeComponent component) {
        this(component.isDirected());
        copy(component.getPaintable(), defaultInstance);
        copy(component.getDecorationPaintable(), defaultInstance.getDecorationPaintable());
    }

    @Override
    public EdgePaintable getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public EdgePaintable getCopyInstance() {
        EdgeDecorationPaintable decorationPaintable = (defaultInstance.getDecorationPaintable() instanceof ArrowheadPaintable)
            ? new ArrowheadPaintable(defaultInstance.getDecorationPaintable().getStartPoint(), defaultInstance.getDecorationPaintable().getEndPoint())
            : new DiamondPaintable(defaultInstance.getDecorationPaintable().getStartPoint(), defaultInstance.getDecorationPaintable().getEndPoint());
        EdgePaintable copyInstance = new EdgePaintable(() -> ICON_MID_LEFT, () -> ICON_MID_RIGHT, decorationPaintable, defaultInstance.isDirected());
        copy(defaultInstance.getPolygonPaintable(), copyInstance.getPolygonPaintable());
        copy(defaultInstance.getDecorationPaintable(), copyInstance.getDecorationPaintable());
        return copyInstance;
    }

    public EdgeDecorationPaintable.Factory createDecorationPaintable() {
        EdgePaintable paintable = getCopyInstance();
        return (left, right) -> {
            EdgeDecorationPaintable decorationPaintable = (paintable.getDecorationPaintable() instanceof DiamondPaintable)
                ? new DiamondPaintable(left, right)
                : new ArrowheadPaintable(left, right);
            copy(paintable.getDecorationPaintable(), decorationPaintable);
            return decorationPaintable;
        };
    }

    private static void copy(Paintable source, Paintable target) {
        copy(source.getPaintKeys(), target::setPaint, source::getPaint);
        copy(source.getStrokeKeys(), target::setStroke, source::getStroke);
    }

    private static <T> void copy(Collection<Object> keys, BiConsumer<Object, T> setter, Function<Object, T> getter) {
        keys.forEach(key -> setter.accept(key, getter.apply(key)));
    }


    public boolean isDirected() {
        return defaultInstance.isDirected();
    }

    private final EdgePaintable defaultInstance;

    private static final Point ICON_MID_LEFT = new Point(-8, 0);
    private static final Point ICON_MID_RIGHT = new Point(8, 0);

    private static final BasicStroke SOLID_STROKE = new BasicStroke();
}
