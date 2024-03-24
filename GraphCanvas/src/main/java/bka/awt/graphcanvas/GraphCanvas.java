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

    public ComponentUpdate handleMouseMoved(MouseEvent event) {
        return mouseHandler.mouseMoved(event);
    }

    public ComponentUpdate handleMousePressed(MouseEvent event) {
        return mouseHandler.mousePressed(event);
    }

    public ComponentUpdate handleMouseClicked(MouseEvent event) {
        return mouseHandler.mouseClicked(event);
    }

    public ComponentUpdate handleMouseDragged(MouseEvent event) {
        return mouseHandler.mouseDragged(event);
    }

    public ComponentUpdate handleMouseReleased(MouseEvent event) {
        return mouseHandler.mouseReleased(event);
    }

    public ComponentUpdate handleKeyPressed(KeyEvent event) {
        return mouseHandler.keyPressed(event);
    }

    public ComponentUpdate handleKeyReleased(KeyEvent event) {
        return mouseHandler.keyReleased(event);
    }

    public ComponentUpdate historyUndo() {
        if (!history.canUndo()) {
            return ComponentUpdate.NO_OPERATION;
        }
        history.undo();
        return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    public ComponentUpdate historyRedo() {
        if (!history.canRedo()) {
            return ComponentUpdate.NO_OPERATION;
        }
        history.redo();
        return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    public ComponentUpdate deleteSelection() {
        if (selection.isEmpty()) {
            return ComponentUpdate.NO_OPERATION;
        }
        Collection<EdgeRenderer> edgesToRemove = new HashSet<>();
        Collection<VertexRenderer> verticesToRemove = new ArrayList<>();
        selection.forEach(element -> {
            if (element instanceof EdgeRenderer) {
                edgesToRemove.add((EdgeRenderer) element);
            }
            else {
                VertexRenderer vertex = (VertexRenderer) element;
                edgesToRemove.addAll(incidentEdges(vertex));
                verticesToRemove.add(vertex);
            }
        });
        edges.removeAll(edgesToRemove);
        vertices.removeAll(verticesToRemove);
        history.add(new ElementDeletion(verticesToRemove, edgesToRemove, GraphCanvas.this));
        selection.clear();
        return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    public void setDirected(EdgeRenderer edge, boolean directed) {
        if (edge.isDirected() != directed) {
            history.add(new EdgeDirectedMutation(edge));
            edge.setDirected(directed);
            getContext().requestUpdate(ComponentUpdate.REPAINT);
        }
    }

    public void revert(EdgeRenderer edge) {
        history.add(new RevertEdgeMutation(edge));
        edge.revert();
        getContext().requestUpdate(ComponentUpdate.REPAINT);
    }

    public void changePaint(Paintable paintable, Object key, Paint newPaint) {
        if (!Objects.equals(paintable.getPaint(key), newPaint)) {
            addHistory(new PaintMutation(paintable, key));
            paintable.setPaint(key, newPaint);
            getContext().requestUpdate(ComponentUpdate.REPAINT);
        }
    }

    public Element findNearestElement(Point point) {
        if (vertices.isEmpty()) {
            return null;
        }
        TreeMap<Long, Element> distances = new TreeMap<>();
        vertices.forEach(vertexRenderer -> distances.put(vertexRenderer.squareDistance(point), vertexRenderer));
        edges.forEach(edgeRenderer -> distances.put(edgeRenderer.squareDistance(point), edgeRenderer));
        return distances.firstEntry().getValue();
    }

    public VertexRenderer findNearestVertex(Point point) {
        TreeMap<Long, VertexRenderer> distances = new TreeMap<>();
        vertices.forEach(vertexRenderer -> distances.put(vertexRenderer.squareDistance(point), vertexRenderer));
        Map.Entry<Long, VertexRenderer> nearest = distances.floorEntry(CanvasUtil.NEAR_DISTANCE);
        if (nearest == null) {
            return null;
        }
        return nearest.getValue();
    }

    public EdgeRenderer findNearestEdge(Point point) {
        TreeMap<Long, EdgeRenderer> distances = new TreeMap<>();
        edges.forEach(edgeRenderer -> distances.put(edgeRenderer.squareDistance(point), edgeRenderer));
        Map.Entry<Long, EdgeRenderer> nearest = distances.floorEntry(CanvasUtil.NEAR_DISTANCE);
        if (nearest == null) {
            return null;
        }
        return nearest.getValue();
    }

    private Collection<EdgeRenderer> incidentEdges(VertexRenderer vertex) {
        return edges.stream()
            .filter(edge -> vertex.equals(edge.getStart()) || vertex.equals(edge.getEnd()))
            .collect(Collectors.toList());
    }

    public void removeRenderers(Collection<VertexRenderer> vertexRenderers, Collection<EdgeRenderer> edgeRenderers) {
        vertices.removeAll(vertexRenderers);
        edges.removeAll(edgeRenderers);
        selection.removeAll(edgeRenderers);
        selection.removeAll(vertexRenderers);
    }

    public void insertRenderers(Collection<VertexRenderer> vertexRenderers, Collection<EdgeRenderer> edgeRenderers) {
        vertices.addAll(vertexRenderers);
        edges.addAll(edgeRenderers);
    }

    public DrawHistory getDrawHistory() {
        return history;
    }

    public Collection<VertexRenderer> getVertices() {
        return Collections.unmodifiableCollection(vertices);
    }

    public void addVertex(VertexRenderer vertex) {
        vertices.add(vertex);
    }

    public Collection<EdgeRenderer> getEdges() {
        return Collections.unmodifiableCollection(edges);
    }

    public void addEdge(EdgeRenderer edge) {
        edges.add(edge);
    }

    public Set<Element> getSelection() {
        return Collections.unmodifiableSet(selection);
    }

    public void select(Element element) {
        selection.add(element);
    }

    public void selectSingleVertex(VertexRenderer vertex) {
        selection.clear();
        selection.add(vertex);
    }

    public void removeSelected(Element element) {
        selection.remove(element);
    }

    public void toggleSelected(Element element) {
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

        public EdgeDirectedMutation(EdgeRenderer edge) {
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

        private RevertEdgeMutation(EdgeRenderer edge) {
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

        private final EdgeRenderer edge;

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

    private final Collection<VertexRenderer> vertices = new ArrayList<>();
    private final Collection<EdgeRenderer> edges = new ArrayList<>();
    private final Set<Element> selection = new HashSet<>();

    private final DrawHistory history = new DrawHistory();

    private CanvasEventHandler mouseHandler;

    private static final Color SELECTION_HIGHLIGHT_COLOR = new Color(0x7fffff00, true);
    private static final Color LABEL_HIGHLIGHT_COLOR = new Color(0x7f00ffff, true);
    private static final BasicStroke HIGHLIGHT_STROKE = new BasicStroke(2f);


}
