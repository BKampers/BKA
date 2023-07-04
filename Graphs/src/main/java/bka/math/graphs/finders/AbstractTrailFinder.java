/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs.finders;

import bka.math.graphs.*;
import bka.math.graphs.utils.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;


public abstract class AbstractTrailFinder<V, E extends Edge<V>> implements TrailFinder<V, E> {

    protected Collection<E> complement(Collection<E> edges, E edge) {
        return edges.stream().filter(Predicate.not(Predicate.isEqual(edge))).collect(Collectors.toList());
    }

    protected Collection<E> complement(Collection<E> edges, V vertex) {
        return edges.stream().filter(Predicate.not(EdgeUtil.isIncidentWith(vertex))).collect(Collectors.toList());
    }

    protected <T> Predicate<T> allPass() {
        return (T any) -> true;
    }

    protected <T> Predicate<T> nonePass() {
        return (T any) -> false;
    }

}
