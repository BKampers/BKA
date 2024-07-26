/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import bka.awt.*;
import bka.awt.graphcanvas.handlers.*;
import bka.awt.graphcanvas.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.*;


public class GraphCanvas extends CompositeRenderer {

    public GraphCanvas(ApplicationContext context) {
        this.context = context;
        resetEventHandler();
    }

    public final void resetEventHandler() {
        mouseHandler = new DefaultEventHandler(GraphCanvas.this);
    }

    public void setEventHandler(CanvasEventHandler handler) {
        mouseHandler = handler;
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(graphics);
        vertices.forEach(renderer -> renderer.paint(graphics));
        edges.forEach(edge -> {
            edge.paint(graphics);
            PaintUtil.paintConnectorPoint(graphics, edge.getStartConnectorPoint());
            PaintUtil.paintConnectorPoint(graphics, edge.getEndConnectorPoint());
        });
        selection.forEach(renderer -> renderer.paintHighlight(graphics, SELECTION_HIGHLIGHT_COLOR, new BasicStroke(3f)));
        mouseHandler.paint(graphics);
    }

    public void addHistory(Mutation mutation) {
        history.add(mutation);
    }

    public CanvasUpdate handleMouseMoved(MouseEvent event) {
        return mouseHandler.mouseMoved(event);
    }

    public CanvasUpdate handleMousePressed(MouseEvent event) {
        return mouseHandler.mousePressed(event);
    }

    public CanvasUpdate handleMouseClicked(MouseEvent event) {
        return mouseHandler.mouseClicked(event);
    }

    public CanvasUpdate handleMouseDragged(MouseEvent event) {
        return mouseHandler.mouseDragged(event);
    }

    public CanvasUpdate handleMouseReleased(MouseEvent event) {
        return mouseHandler.mouseReleased(event);
    }

    public CanvasUpdate handleKeyPressed(KeyEvent event) {
        return mouseHandler.keyPressed(event);
    }

    public CanvasUpdate handleKeyReleased(KeyEvent event) {
        return mouseHandler.keyReleased(event);
    }

    public CanvasUpdate historyUndo() {
        if (!history.canUndo()) {
            return CanvasUpdate.NO_OPERATION;
        }
        history.undo();
        return CanvasUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    public CanvasUpdate historyRedo() {
        if (!history.canRedo()) {
            return CanvasUpdate.NO_OPERATION;
        }
        history.redo();
        return CanvasUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    public CanvasUpdate deleteSelection() {
        if (selection.isEmpty()) {
            return CanvasUpdate.NO_OPERATION;
        }
        Collection<EdgeComponent> edgesToRemove = new HashSet<>();
        Collection<VertexComponent> verticesToRemove = new ArrayList<>();
        selection.forEach(element -> {
            if (element instanceof EdgeComponent) {
                edgesToRemove.add((EdgeComponent) element);
            }
            else {
                VertexComponent vertex = (VertexComponent) element;
                edgesToRemove.addAll(incidentEdges(vertex));
                verticesToRemove.add(vertex);
            }
        });
        edges.removeAll(edgesToRemove);
        vertices.removeAll(verticesToRemove);
        history.add(new ElementDeletion(verticesToRemove, edgesToRemove, GraphCanvas.this));
        selection.clear();
        return CanvasUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    public void setDirected(EdgeComponent edge, boolean directed) {
        if (edge.isDirected() != directed) {
            history.add(new EdgeDirectedMutation(edge));
            edge.setDirected(directed);
            getContext().requestUpdate(CanvasUpdate.REPAINT);
        }
    }

    public void revert(EdgeComponent edge) {
        history.add(new RevertEdgeMutation(edge));
        edge.revert();
        getContext().requestUpdate(CanvasUpdate.REPAINT);
    }

    public void changeStroke(Paintable paintable, Object key, Stroke newStroke) {
        if (!Objects.equals(paintable.getStroke(key), newStroke)) {
            addHistory(new StrokeMutation(paintable, key));
            paintable.setStroke(key, newStroke);
            getContext().requestUpdate(CanvasUpdate.REPAINT);
        }
    }

    public void changePaint(Paintable paintable, Object key, Paint newPaint) {
        if (!Objects.equals(paintable.getPaint(key), newPaint)) {
            addHistory(new PaintMutation(paintable, key));
            paintable.setPaint(key, newPaint);
            getContext().requestUpdate(CanvasUpdate.REPAINT);
        }
    }

    public GraphComponent findNearestElement(Point point) {
        if (vertices.isEmpty()) {
            return null;
        }
        TreeMap<Long, GraphComponent> distances = new TreeMap<>();
        vertices.forEach(vertexRenderer -> distances.put(vertexRenderer.squareDistance(point), vertexRenderer));
        edges.forEach(edgeRenderer -> distances.put(edgeRenderer.squareDistance(point), edgeRenderer));
        return distances.firstEntry().getValue();
    }

    public VertexComponent findNearestVertex(Point point) {
        TreeMap<Long, VertexComponent> distances = new TreeMap<>();
        vertices.forEach(vertexRenderer -> distances.put(vertexRenderer.squareDistance(point), vertexRenderer));
        Map.Entry<Long, VertexComponent> nearest = distances.floorEntry(CanvasUtil.NEAR_DISTANCE);
        if (nearest == null) {
            return null;
        }
        return nearest.getValue();
    }

    public EdgeComponent findNearestEdge(Point point) {
        TreeMap<Long, EdgeComponent> distances = new TreeMap<>();
        edges.forEach(edgeRenderer -> distances.put(edgeRenderer.squareDistance(point), edgeRenderer));
        Map.Entry<Long, EdgeComponent> nearest = distances.floorEntry(CanvasUtil.NEAR_DISTANCE);
        if (nearest == null) {
            return null;
        }
        return nearest.getValue();
    }

    private Collection<EdgeComponent> incidentEdges(VertexComponent vertex) {
        return edges.stream()
            .filter(edge -> vertex.equals(edge.getStart()) || vertex.equals(edge.getEnd()))
            .collect(Collectors.toList());
    }

    public void removeRenderers(Collection<VertexComponent> vertexRenderers, Collection<EdgeComponent> edgeRenderers) {
        vertices.removeAll(vertexRenderers);
        edges.removeAll(edgeRenderers);
        selection.removeAll(edgeRenderers);
        selection.removeAll(vertexRenderers);
    }

    public void insertRenderers(Collection<VertexComponent> vertexRenderers, Collection<EdgeComponent> edgeRenderers) {
        vertices.addAll(vertexRenderers);
        edges.addAll(edgeRenderers);
    }

    public DrawHistory getDrawHistory() {
        return history;
    }

    public Collection<VertexComponent> getVertices() {
        return Collections.unmodifiableCollection(vertices);
    }

    public void addVertex(VertexComponent vertex) {
        vertices.add(vertex);
    }

    public Collection<EdgeComponent> getEdges() {
        return Collections.unmodifiableCollection(edges);
    }

    public void addEdge(EdgeComponent edge) {
        edges.add(edge);
    }

    public Set<GraphComponent> getSelection() {
        return Collections.unmodifiableSet(selection);
    }

    public void select(GraphComponent element) {
        selection.add(element);
    }

    public void selectSingleVertex(VertexComponent vertex) {
        selection.clear();
        selection.add(vertex);
    }

    public void removeSelected(GraphComponent element) {
        selection.remove(element);
    }

    public void toggleSelected(GraphComponent element) {
        if (selection.contains(element)) {
            selection.remove(element);
        }
        else {
            selection.add(element);
        }
    }

    public void clearSelection() {
        selection.clear();
    }

    public ApplicationContext getContext() {
        return context;
    }

    public static Color getLabelHighlightColor() {
        return LABEL_HIGHLIGHT_COLOR;
    }

    public static Stroke getHighlightStroke() {
        return HIGHLIGHT_STROKE;
    }

    private class EdgeDirectedMutation extends PropertyMutation<Boolean> {

        public EdgeDirectedMutation(EdgeComponent edge) {
            super(Mutation.Type.EDGE_DIRECTED_TOGGLE, edge::isDirected, edge::setDirected);
            bundleKey = edge.isDirected() ? "EdgeUndirected" : "EdgeDirected";
        }

        @Override
        public String getBundleKey() {
            return bundleKey;
        }

        private final String bundleKey;
    }


    private class RevertEdgeMutation extends Mutation.Symmetrical {

        private RevertEdgeMutation(EdgeComponent edge) {
            this.edge = edge;
        }

        @Override
        protected void revert() {
            edge.revert();
        }

        @Override
        public Type getType() {
            return Mutation.Type.EDGE_REVERT;
        }

        private final EdgeComponent edge;

    }


    private class StrokeMutation extends PropertyMutation<Stroke> {

        private StrokeMutation(Paintable paintable, Object key) {
            super(Mutation.Type.STROKE_MUTATION, () -> paintable.getStroke(key), stroke -> paintable.setStroke(key, stroke));
            bundleKey = key.toString() + "Changed";
        }

        @Override
        public String getBundleKey() {
            return bundleKey;
        }

        private final String bundleKey;
    }


    private class PaintMutation extends PropertyMutation<Paint> {

        private PaintMutation(Paintable paintable, Object key) {
            super(Mutation.Type.PAINT_MUTATION, () -> paintable.getPaint(key), paint -> paintable.setPaint(key, paint));
            bundleKey = key.toString() + "Changed";
        }

        @Override
        public String getBundleKey() {
            return bundleKey;
        }

        private final String bundleKey;
    }


    private final ApplicationContext context;

    private final Collection<VertexComponent> vertices = new ArrayList<>();
    private final Collection<EdgeComponent> edges = new ArrayList<>();
    private final Set<GraphComponent> selection = new HashSet<>();

    private final DrawHistory history = new DrawHistory();

    private CanvasEventHandler mouseHandler;

    private static final Color SELECTION_HIGHLIGHT_COLOR = new Color(0x7fffff00, true);
    private static final Color LABEL_HIGHLIGHT_COLOR = new Color(0x7f6699ff, true);
    private static final Stroke HIGHLIGHT_STROKE = new BasicStroke(4f);

}
