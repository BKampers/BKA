/*
** © Bart Kampers
*/

package bka.demo.graphs;


import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.*;


class DrawHistory {

    public interface Listener {

        void historyChanged(DrawHistory history);
    }

    public DrawHistory(GraphCanvas graphCanvas) {
        this.graphCanvas = graphCanvas;
    }

    public void addListener(Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public List<Mutation> getMutattions() {
        return Collections.unmodifiableList(history);
    }

    public int getIndex() {
        return index;
    }

    public void addVertexInsertion(VertexRenderer vertexRenderer) {
        addToHistory(new ElementInsertion(vertexRenderer));
    }

    public void addElementRelocation(Collection<Element> elements, Point vector, Map<EdgeRenderer, List<Point>> affectedEdges) {
        if (!elements.isEmpty()) {
            addToHistory(new ElementRelocation(elements, vector, affectedEdges));
        }
    }

    public void addVertexSizeChange(VertexRenderer vertexRenderer, Dimension dimension) {
        addToHistory(new SizeChange(vertexRenderer, dimension));
    }

//    public void addVertexSizeChange(Map<VertexRenderer, Dimension> resizements) {
//        if (!resizements.isEmpty()) {
//            addToHistory(new SizeChange(resizements));
//        }
//    }

    public void addElementDeletion(Collection<VertexRenderer> vertexRenderers, Collection<EdgeRenderer> edgeRenderers) {
        addToHistory(new ElementDeletion(vertexRenderers, edgeRenderers));
    }

    public void addEdgeInsertion(EdgeRenderer edgeRenderer) {
        addToHistory(new ElementInsertion(edgeRenderer));
    }

    public void addEdgeTransformation(EdgeRenderer element, List<Point> originalPoints) {
        addToHistory(new EdgeTransformation(element, originalPoints.stream().map(point -> new Point(point)).collect(Collectors.toList())));
    }

    public boolean canUndo() {
        return index > 0;
    }

    public Mutation getUndo() {
        if (!canUndo()) {
            throw new NoSuchElementException("Cannot undo");
        }
        index--;
        notifyListeners();
        return history.get(index);
    }

    public void undo() {
        if (canUndo()) {
            getUndo().undo();
        }
    }

    public boolean canRedo() {
        return index < history.size();
    }

    public Mutation getRedo() {
        if (!canRedo()) {
            throw new NoSuchElementException("Cannot redo");
        }
        Mutation mutation = history.get(index);
        index++;
        notifyListeners();
        return mutation;
    }

    public void redo() {
        if (canRedo()) {
            getRedo().redo();
        }
    }

    private void addToHistory(Mutation mutation) {
        while (index < history.size()) {
            history.removeLast();
        }
        history.add(mutation);
        index++;
        notifyListeners();
    }

    private void notifyListeners() {
        synchronized (listeners) {
            for (Listener listener : listeners) {
                listener.historyChanged(DrawHistory.this);
            }
        }
    }


    private abstract class AbstractMutation implements Mutation {

        @Override
        public void undo() {
            revert();
        }

        @Override
        public void redo() {
            revert();
        }

        abstract protected void revert();
    }


    private class ElementInsertion implements Mutation {

        ElementInsertion(VertexRenderer element) {
            vertexRenderers.add(element);
        }

        ElementInsertion(EdgeRenderer element) {
            edgeRenderers.add(element);
        }

        @Override
        public void undo() {
            graphCanvas.removeRenderers(vertexRenderers, edgeRenderers);
        }

        @Override
        public void redo() {
            graphCanvas.insertRenderers(vertexRenderers, edgeRenderers);
        }

        private final Collection<VertexRenderer> vertexRenderers = new ArrayList<>();
        private final Collection<EdgeRenderer> edgeRenderers = new ArrayList<>();

    }


    private class ElementDeletion implements Mutation {

        ElementDeletion(EdgeRenderer edgeRenderer) {
            this(Collections.emptyList(), List.of(edgeRenderer));
        }

        ElementDeletion(VertexRenderer vertexRenderer, Collection<EdgeRenderer> edgeRenderers) {
            this(List.of(vertexRenderer), edgeRenderers);
        }

        ElementDeletion(Collection<VertexRenderer> vertexRenderers, Collection<EdgeRenderer> edgeRenderers) {
            this.vertexRenderers = new ArrayList<>(vertexRenderers);
            this.edgeRenderers = new ArrayList<>(edgeRenderers);
        }

        @Override
        public void undo() {
            graphCanvas.insertRenderers(vertexRenderers, edgeRenderers);
        }

        @Override
        public void redo() {
            graphCanvas.removeRenderers(vertexRenderers, edgeRenderers);
        }

        private final Collection<VertexRenderer> vertexRenderers;
        private final Collection<EdgeRenderer> edgeRenderers;

    }

    private class ElementRelocation extends AbstractMutation {

        public ElementRelocation(Collection<Element> elements, Point vector, Map<EdgeRenderer, List<Point>> affectedEdges) {
            this.elements = new ArrayList(elements);
            this.vector = vector;
            this.affectedEdges = (affectedEdges.isEmpty()) ? Collections.emptyMap() : affectedEdges;
        }

        @Override
        public void revert() {
            vector.move(-vector.x, -vector.y);
            graphCanvas.revertElementRelocation(elements, vector);
            affectedEdges.forEach((edge, points) -> {
                List<Point> edgePoints = new ArrayList<>(edge.getPoints());
                edge.setPoints(points);
                points.clear();
                points.addAll(edgePoints);
            });
        }

        private final Collection<Element> elements;
        private final Point vector;
        private final Map<EdgeRenderer, List<Point>> affectedEdges;

    }


    private class SizeChange extends AbstractMutation {

        public SizeChange(VertexRenderer vertexRenderer, Dimension dimension) {
            this.vertexRenderer = vertexRenderer;
            this.dimension = dimension;
        }

        @Override
        protected void revert() {
//            for (Map.Entry<VertexRenderer, Dimension> entry : mutations.entrySet()) {
//                VertexRenderer element = entry.getKey();
//                Dimension originalDimension = entry.getValue();
            Dimension currentDimension = vertexRenderer.getDimension();
            graphCanvas.revertVertexMutation(vertexRenderer, vertexRenderer.getLocation(), dimension);
            dimension = currentDimension;
//            }
        }

        private final VertexRenderer vertexRenderer;
        private Dimension dimension;

    }


    private class EdgeTransformation extends AbstractMutation {

        public EdgeTransformation(EdgeRenderer element, List<Point> originalPoints) {
            this.element = element;
            this.originalPoints = originalPoints;
        }

        @Override
        protected void revert() {
            List<Point> currentPoints = new ArrayList<>(element.getPoints());
            element.setPoints(originalPoints);
            originalPoints = currentPoints;
        }

        private final EdgeRenderer element;
        private List<Point> originalPoints;

    }


    private final GraphCanvas graphCanvas;
    private final LinkedList<Mutation> history = new LinkedList<>();
    private final Collection<Listener> listeners = new ArrayList<>();

    private int index;

}
