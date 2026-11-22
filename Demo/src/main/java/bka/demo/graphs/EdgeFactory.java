/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.graphcanvas.*;
import java.awt.*;
import java.util.*;
import java.util.function.*;


public class EdgeFactory implements Factory {

    public enum Decoration {
        NONE(ArrowheadDecorationPaintable::new, EdgeFactory::initializeArrowhead),
        ARROWHEAD(ArrowheadDecorationPaintable::new, EdgeFactory::initializeArrowhead),
        DIAMOND(DiamondDecorationPaintable::new, EdgeFactory::initializeDiamond);

        private Decoration(BiFunction<Supplier<Point>, Supplier<Point>, EdgeDecorationPaintable> constructor, Consumer<EdgePaintable> initialize) {
            this.constructor = constructor;
            this.initializer = initialize;
        }

        public EdgePaintable createEdgePaintable() {
            EdgePaintable paintable = new EdgePaintable(ICON_LEFT, ICON_RIGHT, createEdgeDecorationPaintable(), this != NONE);
            initializer.accept(paintable);
            return paintable;
        }

        public EdgeDecorationPaintable createEdgeDecorationPaintable() {
            EdgeDecorationPaintable paintable = constructor.apply(ICON_LEFT, ICON_RIGHT);
            paintable.setCentered(true);
            return paintable;
        }

        private final BiFunction<Supplier<Point>, Supplier<Point>, EdgeDecorationPaintable> constructor;
        private final Consumer<EdgePaintable> initializer;
    };

    public EdgeFactory(Decoration decoration) {
        this.decoration = decoration;
        defaultInstance = decoration.createEdgePaintable();
    }

    public EdgeFactory(EdgeComponent component) {
        this(getDecoration(component));
        copy(component.getPaintable(), defaultInstance);
        copy(component.getDecorationPaintable(), defaultInstance.getDecorationPaintable());
    }

    private static Decoration getDecoration(EdgeComponent component) {
        if (!component.isDirected()) {
            return Decoration.NONE;
        }
        if (component.getDecorationPaintable() instanceof ArrowheadDecorationPaintable) {
            return Decoration.ARROWHEAD;
        }
        if (component.getDecorationPaintable() instanceof DiamondDecorationPaintable) {
            return Decoration.DIAMOND;
        }
        throw new IllegalStateException("Unsupported decoration: " + component.getDecorationPaintable().getClass());
    }

    private static void initializeArrowhead(EdgePaintable edgePainatble) {
        edgePainatble.getPolygonPaintable().setPaint(PolygonPaintable.LINE_PAINT_KEY, Color.BLACK);
        edgePainatble.getPolygonPaintable().setStroke(PolygonPaintable.LINE_STROKE_KEY, SOLID_STROKE);
        edgePainatble.getDecorationPaintable().setPaint(ArrowheadDecorationPaintable.ARROWHEAD_PAINT_KEY, Color.BLACK);
        edgePainatble.getDecorationPaintable().setStroke(ArrowheadDecorationPaintable.ARROWHEAD_STROKE_KEY, SOLID_STROKE);
    }

    private static void initializeDiamond(EdgePaintable edgePaintable) {
        edgePaintable.getPolygonPaintable().setPaint(PolygonPaintable.LINE_PAINT_KEY, Color.BLACK);
        edgePaintable.getPolygonPaintable().setStroke(PolygonPaintable.LINE_STROKE_KEY, SOLID_STROKE);
        edgePaintable.getDecorationPaintable().setStroke(DiamondDecorationPaintable.DIAMOND_BORDER_STROKE_KEY, SOLID_STROKE);
        edgePaintable.getDecorationPaintable().setPaint(DiamondDecorationPaintable.DIAMOND_BORDER_PAINT_KEY, Color.BLACK);
        edgePaintable.getDecorationPaintable().setPaint(DiamondDecorationPaintable.DIAMOND_FILL_PAINT_KEY, Color.WHITE);
    }

    @Override
    public EdgePaintable getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public EdgePaintable getCopyInstance() {
        EdgeDecorationPaintable decorationPaintable = decoration.createEdgeDecorationPaintable();
        EdgePaintable copyInstance = new EdgePaintable(ICON_LEFT, ICON_RIGHT, decorationPaintable, defaultInstance.isDirected());
        copy(defaultInstance.getPolygonPaintable(), copyInstance.getPolygonPaintable());
        copy(defaultInstance.getDecorationPaintable(), copyInstance.getDecorationPaintable());
        return copyInstance;
    }

    public EdgeDecorationPaintable.Factory createDecorationPaintable() {
        EdgePaintable paintable = getCopyInstance();
        return (left, right) -> {
            EdgeDecorationPaintable decorationPaintable = (paintable.getDecorationPaintable() instanceof DiamondDecorationPaintable)
                ? new DiamondDecorationPaintable(left, right)
                : new ArrowheadDecorationPaintable(left, right);
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

    private final Decoration decoration;
    private final EdgePaintable defaultInstance;

    private static final Point ICON_MID_LEFT = new Point(-8, 0);
    private static final Point ICON_MID_RIGHT = new Point(8, 0);
    private static final Supplier<Point> ICON_LEFT = () -> ICON_MID_LEFT;
    private static final Supplier<Point> ICON_RIGHT = () -> ICON_MID_RIGHT;

    private static final BasicStroke SOLID_STROKE = new BasicStroke();
}
