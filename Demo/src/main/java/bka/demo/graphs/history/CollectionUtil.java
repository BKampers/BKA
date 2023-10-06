/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
import java.util.*;
import java.util.stream.*;


public class CollectionUtil {

    public static <T> Collection<T> unmodifiableCollection(Collection<T> collection) {
        if (collection.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(collection);
    }

    public static <T> List<T> unmodifiableList(List<T> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    public static <K, V> Map<K, V> unmodifiableMap(Map<K, V> map) {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }

    public static Collection<VertexRenderer> getVertices(Collection<Element> elements) {
        return unmodifiableCollection(elements.stream()
            .filter(element -> element instanceof VertexRenderer)
            .map(element -> (VertexRenderer) element)
            .collect(Collectors.toList()));
    }

    public static Collection<EdgeRenderer> getEdges(Collection<Element> elements) {
        return unmodifiableCollection(elements.stream()
            .filter(element -> element instanceof EdgeRenderer)
            .map(element -> (EdgeRenderer) element)
            .collect(Collectors.toList()));
    }

}
