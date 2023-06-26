/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/

package bka.math.graphs.finders;

import bka.math.graphs.*;
import bka.math.graphs.utils.*;
import java.util.function.*;


public class RecursiveDirectedTrailFinder<V, E extends DirectedEdge<V>> extends RecursiveTrailFinder<V, E> {

    @Override
    protected Predicate<E> from(V start) {
        return EdgeUtil.from(start);
    }

    @Override
    protected V next(E edge, V start) {
        return edge.getTerminus();
    }

}
