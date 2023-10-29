/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import bka.demo.graphs.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.*;


public class GraphCanvas extends CompositeRenderer {

    public GraphCanvas(ApplicationContext context) {
        this.context = context;
        resetEventHandler();
    }

    final void resetEventHandler() {
        mouseHandler = new DefaultEventHandler(GraphCanvas.this);
    }

    void setEventHandler(CanvasEventHandler handler) {
        mouseHandler = handler;
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(graphics);
        graphics.setPaint(Color.BLACK);
        vertices.forEach(renderer -> renderer.paint(graphics));
        edges.forEach(edge -> {
            graphics.setPaint(Color.BLACK);
            edge.paint(graphics);
            PaintUtil.paintConnectorPoint(graphics, edge.getStartConnectorPoint());
            PaintUtil.paintConnectorPoint(graphics, edge.getEndConnectorPoint());
        });
        graphics.setPaint(Color.BLUE);
        selection.forEach(renderer -> renderer.paint(graphics));
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

    ComponentUpdate historyUndo() {
        if (!history.canUndo()) {
            return ComponentUpdate.NO_OPERATION;
        }
        history.undo();
        return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    ComponentUpdate historyRedo() {
        if (!history.canRedo()) {
            return ComponentUpdate.NO_OPERATION;
        }
        history.redo();
        return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    ComponentUpdate deleteSelection() {
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

    public static Point nearestEdgePoint(EdgeRenderer edge, Point point) {
        Point p0 = edge.getStartConnectorPoint();
        if (CanvasUtil.isNear(point, p0)) {
            edge.addPoint(0, point);
            return point;
        }
        int index = 0;
        for (Point p1 : edge.getPoints()) {
            if (CanvasUtil.isNear(point, p1)) {
                return p1;
            }
            if (CanvasUtil.isNear(point, p0, p1)) {
                edge.addPoint(index, point);
                return point;
            }
            p0 = p1;
            ++index;
        }
        edge.addPoint(point);
        return point;
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

    void addVertex(VertexRenderer vertex) {
        vertices.add(vertex);
    }

    public Collection<EdgeRenderer> getEdges() {
        return Collections.unmodifiableCollection(edges);
    }

    void addEdge(EdgeRenderer edge) {
        edges.add(edge);
    }

    public Set<Element> getSelection() {
        return Collections.unmodifiableSet(selection);
    }

    void select(Element element) {
        selection.add(element);
    }

    void selectSingleVertex(VertexRenderer vertex) {
        selection.clear();
        selection.add(vertex);
    }

    void removeSelected(Element element) {
        selection.remove(element);
    }

    void toggleSelected(Element element) {
        if (selection.contains(element)) {
            selection.remove(element);
        }
        else {
            selection.add(element);
        }
    }

    void clearSelection() {
        selection.clear();
    }

    ApplicationContext getContext() {
        return context;
    }


    private final ApplicationContext context;

    private final Collection<VertexRenderer> vertices = new ArrayList<>();
    private final Collection<EdgeRenderer> edges = new ArrayList<>();
    private final Set<Element> selection = new HashSet<>();

    private final DrawHistory history = new DrawHistory();

    private CanvasEventHandler mouseHandler;

}
