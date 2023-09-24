/*
** © Bart Kampers
*/

package bka.demo.graphs;


import bka.demo.graphs.Label;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.*;


public class DrawHistory {

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
            addToHistory(new ElementRelocation(
                elements.stream().filter(element -> element instanceof VertexRenderer).map(element -> (VertexRenderer) element).collect(Collectors.toList()),
                elements.stream().filter(element -> element instanceof EdgeRenderer).map(element -> (EdgeRenderer) element).collect(Collectors.toList()),
                vector,
                new HashMap<>(affectedEdges)));
        }
    }

    public void addVertexSizeChange(VertexRenderer vertexRenderer, Dimension dimension) {
        addToHistory(new SizeChange(vertexRenderer, dimension));
    }

    public void addElementDeletion(Collection<VertexRenderer> vertexRenderers, Collection<EdgeRenderer> edgeRenderers) {
        addToHistory(new ElementDeletion(new ArrayList<>(vertexRenderers), new ArrayList<>(edgeRenderers)));
    }

    public void addEdgeInsertion(EdgeRenderer edgeRenderer) {
        addToHistory(new ElementInsertion(edgeRenderer));
    }

    public void addEdgeTransformation(EdgeRenderer element, List<Point> originalPoints) {
        addToHistory(new EdgeTransformation(element, originalPoints.stream().map(point -> new Point(point)).collect(Collectors.toList())));
    }

    public void addLabelInsertion(Element element, Label label) {
        addToHistory(new LabelInsertion(element, label));
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

    private GraphCanvas getCanvas() {
        return graphCanvas;
    }

    private static <T> Collection<T> unmodifiable(Collection<T> collection) {
        if (collection.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(collection);
    }

    private static <K, V> Map<K, V> unmodifiable(Map<K, V> map) {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }


    private class ElementInsertion implements Mutation {

        public ElementInsertion(VertexRenderer vertex) {
            vertices = List.of(vertex);
            edges = Collections.emptyList();
        }

        public ElementInsertion(EdgeRenderer edge) {
            vertices = Collections.emptyList();
            edges = List.of(edge);
        }

        @Override
        public Mutation.Type getType() {
            return Mutation.Type.INSERTION;
        }

        @Override
        public void undo() {
            getCanvas().removeRenderers(vertices, edges);
        }

        @Override
        public void redo() {
            getCanvas().insertRenderers(vertices, edges);
        }

        @Override
        public Collection<VertexRenderer> getVertices() {
            return vertices;
        }

        @Override
        public Collection<EdgeRenderer> getEdges() {
            return edges;
        }

        private final Collection<VertexRenderer> vertices;
        private final Collection<EdgeRenderer> edges;

    }


    private class ElementDeletion implements Mutation {

        public ElementDeletion(Collection<VertexRenderer> vertices, Collection<EdgeRenderer> edges) {
            this.vertexRenderers = unmodifiable(vertices);
            this.edgeRenderers = unmodifiable(edges);
        }

        @Override
        public Mutation.Type getType() {
            return Mutation.Type.DELETION;
        }

        @Override
        public void undo() {
            getCanvas().insertRenderers(vertexRenderers, edgeRenderers);
        }

        @Override
        public void redo() {
            getCanvas().removeRenderers(vertexRenderers, edgeRenderers);
        }

        @Override
        public Collection<VertexRenderer> getVertices() {
            return vertexRenderers;
        }

        @Override
        public Collection<EdgeRenderer> getEdges() {
            return edgeRenderers;
        }

        private final Collection<VertexRenderer> vertexRenderers;
        private final Collection<EdgeRenderer> edgeRenderers;

    }


    private class ElementRelocation extends Mutation.Symmetrical {

        public ElementRelocation(Collection<VertexRenderer> vertices, Collection<EdgeRenderer> edges, Point vector, Map<EdgeRenderer, List<Point>> affectedEdges) {
            this.vertices = unmodifiable(vertices);
            this.edges = unmodifiable(edges);
            this.vector = vector;
            this.affectedEdges = unmodifiable(affectedEdges);
        }

        @Override
        public Mutation.Type getType() {
            return Mutation.Type.RELOCATION;
        }

        @Override
        public void revert() {
            vector.move(-vector.x, -vector.y);
            vertices.forEach(element -> element.move(vector));
            edges.forEach(element -> element.move(vector));
            affectedEdges.forEach((edge, points) -> {
                List<Point> edgePoints = new ArrayList<>(edge.getPoints());
                edge.setPoints(points);
                points.clear();
                points.addAll(edgePoints);
            });
        }

        @Override
        public Collection<VertexRenderer> getVertices() {
            return vertices;
        }

        @Override
        public Collection<EdgeRenderer> getEdges() {
            return edges;
        }

        private final Collection<VertexRenderer> vertices;
        private final Collection<EdgeRenderer> edges;
        private final Point vector;
        private final Map<EdgeRenderer, List<Point>> affectedEdges;

    }


    private class SizeChange extends Mutation.Symmetrical {

        public SizeChange(VertexRenderer vertex, Dimension dimension) {
            this.vertex = vertex;
            this.dimension = dimension;
        }

        @Override
        public Mutation.Type getType() {
            return Mutation.Type.SHAPE_CHANGE;
        }

        @Override
        protected void revert() {
            Dimension currentDimension = vertex.getDimension();
            vertex.setDimension(dimension);
            dimension = currentDimension;
        }

        @Override
        public Collection<VertexRenderer> getVertices() {
            return List.of(vertex);
        }

        private final VertexRenderer vertex;
        private Dimension dimension;

    }


    private class EdgeTransformation extends Mutation.Symmetrical {

        public EdgeTransformation(EdgeRenderer edge, List<Point> originalPoints) {
            this.edge = edge;
            this.originalPoints = originalPoints;
        }

        @Override
        public Mutation.Type getType() {
            return Mutation.Type.SHAPE_CHANGE;
        }

        @Override
        protected void revert() {
            List<Point> currentPoints = new ArrayList<>(edge.getPoints());
            edge.setPoints(originalPoints);
            originalPoints = currentPoints;
        }

        @Override
        public Collection<EdgeRenderer> getEdges() {
            return List.of(edge);
        }

        private final EdgeRenderer edge;
        private List<Point> originalPoints;

    }


    private class LabelInsertion implements Mutation {

        public LabelInsertion(Element element, Label label) {
            this.element = element;
            this.label = label;
        }

        @Override
        public Type getType() {
            return Type.LABEL_INSERTION;
        }

        @Override
        public void undo() {
            element.removeLabel(label);
        }

        @Override
        public void redo() {
            element.addLabel(label);
        }

        private final Element element;
        private final Label label;
    }


    private final GraphCanvas graphCanvas;
    private final LinkedList<Mutation> history = new LinkedList<>();
    private final Collection<Listener> listeners = new ArrayList<>();

    private int index;

}
