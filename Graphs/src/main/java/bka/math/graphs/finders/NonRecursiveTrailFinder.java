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


public class NonRecursiveTrailFinder<V, E extends Edge<V>> extends AbstractTrailFinder<V, E> {

    @Override
    public void setRestriction(Predicate<List<E>> restriction) {
        this.restriction = restriction;
    }

    @Override
    public void setFilter(Predicate<List<E>> filter) {
        this.filter = (filter == null) ? allPass() : filter;
    }

    public void setLimiter(Predicate<Collection<List<E>>> limiter) {
        this.limiter = (limiter == null) ? nonePass() : limiter;
    }

    @Override
    public Collection<List<E>> find(Collection<E> graph, V start, V end, boolean revisitVertices) {
        Collection<List<E>> foundTrails = new ArrayList<>();
        Deque<SearchStage> stack = new LinkedList<>();
        SearchStage stage = new SearchStage(relevantEdges(revisitVertices, graph, start), start);
        for (;;) {
            if (stage.selectNextEdge()) {
                V nextVertex = stage.getAdjacentVertex();
                TrailBuilder currentTrail = new TrailBuilder(stack, stage);
                if (restriction.test(currentTrail.get())) {
                    if ((end == null || nextVertex.equals(end)) && filter.test(currentTrail.get())) {
                        foundTrails.add(currentTrail.get());
                        if (limiter.test(foundTrails)) {
                            return foundTrails;
                        }
                    }
                    if (revisitVertices || !nextVertex.equals(end)) {
                        stack.push(stage);
                        stage = stage.createComplement(revisitVertices || isFirstInCircuit(stack, start, end), nextVertex);
                    }
                }
            }
            else if (!stack.isEmpty()) {
                stage = stack.pop();
            }
            else {
                return foundTrails;
            }
        }
    }

    private Collection<E> relevantEdges(boolean revisitVertices, Collection<E> graph, V start) {
        return (revisitVertices) ? graph : graph.stream().filter(relevant(start)).collect(Collectors.toList());
    }

    private Predicate<E> relevant(V start) {
        return edge -> !EdgeUtil.isLoop(edge) || edge.getVertices().contains(start);
    }
    
    private boolean isFirstInCircuit(Deque stack, V start, V end) {
        return stack.size() == 1 && start.equals(end);
    }

    private class TrailBuilder {

        public TrailBuilder(Deque<SearchStage> stack, SearchStage stage) {
            this.stack = stack;
            this.stage = stage;
        }

        public List<E> get() {
            if (trail == null) {
                trail = new ArrayList<>(stack.size() + 1);
                for (Iterator<SearchStage> it = stack.descendingIterator(); it.hasNext();) {
                    trail.add(it.next().getCurrent());
                }
                trail.add(stage.getCurrent());
            }
            return trail;
        }

        private final Deque<SearchStage> stack;
        private final SearchStage stage;
        private List<E> trail;

    }

    private class SearchStage {

        public SearchStage(Collection<E> subgraph, V from) {
            this.subgraph = subgraph;
            this.from = from;
            iterator = subgraph.stream().filter(EdgeUtil.isIncidentWith(from)).iterator();
        }

        public SearchStage createComplement(boolean revisitVertices, V nextVertex) {
            Collection<E> remainingEdges = (revisitVertices) ? complement(subgraph, current) : complement(subgraph, from);
            return new SearchStage(remainingEdges, nextVertex);
        }

        public boolean selectNextEdge() {
            if (! iterator.hasNext()) {
                return false;
            }
            current = iterator.next();
            return true;
        }

        public V getAdjacentVertex() {
            return EdgeUtil.getAdjacentVertex(current, from);
        }

        public E getCurrent() {
            return current;
        }

        private final Collection<E> subgraph;
        private final V from;
        private final Iterator<E> iterator;
        private E current;
    }

    private Predicate<List<E>> restriction = allPass();
    private Predicate<List<E>> filter = allPass();
    private Predicate<Collection<List<E>>> limiter = nonePass();

}
