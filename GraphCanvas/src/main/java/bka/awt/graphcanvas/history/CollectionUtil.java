/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas.history;

import bka.awt.graphcanvas.*;
import java.util.*;
import java.util.stream.*;


public class CollectionUtil {

    public static <T> Collection<T> unmodifiableCollection(Collection<T> collection) {
        if (collection.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(collection);
    }

    public static <K, V> Map<K, V> unmodifiableMap(Map<K, V> map) {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }

    public static Collection<VertexComponent> getVertices(Collection<GraphComponent> elements) {
        return unmodifiableCollection(elements.stream()
            .filter(element -> element instanceof VertexComponent)
            .map(element -> (VertexComponent) element)
            .collect(Collectors.toList()));
    }

    public static Collection<EdgeComponent> getEdges(Collection<GraphComponent> elements) {
        return unmodifiableCollection(elements.stream()
            .filter(element -> element instanceof EdgeComponent)
            .map(element -> (EdgeComponent) element)
            .collect(Collectors.toList()));
    }

}
