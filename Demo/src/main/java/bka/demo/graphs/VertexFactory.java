/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.graphcanvas.*;
import java.awt.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;


public class VertexFactory implements Factory {

    public VertexFactory(Function<Dimension, VertexPaintable> newInstance, Map<Object, Stroke> defaultStrokes, Map<Object, Paint> defaultPaints) {
        this.newInstance = newInstance;
        defaultInstance = newInstance.apply(VERTEX_ICON_DIMENSION);
        defaultStrokes.forEach(defaultInstance::setStroke);
        defaultPaints.forEach(defaultInstance::setPaint);
    }

    public VertexFactory(Function<Dimension, VertexPaintable> newInstance, Paintable paintable) {
        this(newInstance, toMap(paintable.getStrokeKeys(), paintable::getStroke), toMap(paintable.getPaintKeys(), paintable::getPaint));
    }

    private static <K, V> Map<K, V> toMap(Collection<K> keys, Function<K, V> getter) {
        return keys.stream().collect(Collectors.toMap(Function.identity(), getter::apply));
    }

    @Override
    public VertexPaintable getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public VertexPaintable getCopyInstance() {
        VertexPaintable copyInstance = newInstance.apply(VERTEX_ICON_DIMENSION);
        copy(defaultInstance, copyInstance);
        return copyInstance;
    }

    private static void copy(Paintable source, Paintable target) {
        copy(source.getPaintKeys(), target::setPaint, source::getPaint);
        copy(source.getStrokeKeys(), target::setStroke, source::getStroke);
    }

    private static <T> void copy(Collection<Object> keys, BiConsumer<Object, T> setter, Function<Object, T> getter) {
        keys.forEach(key -> setter.accept(key, getter.apply(key)));
    }

    private final Function<Dimension, VertexPaintable> newInstance;
    private final VertexPaintable defaultInstance;

    private static final Dimension VERTEX_ICON_DIMENSION = new Dimension(12, 12);

}
