/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.stream.*;


public class GraphCanvas extends CompositeRenderer {

    @Override
    public void paint(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(graphics);
        graphics.setPaint(Color.BLACK);
        vertices.forEach(renderer -> renderer.paint(graphics));
        edges.forEach(edge -> {
            graphics.setPaint(Color.BLACK);
            edge.paint(graphics);
            paintDot(graphics, edge.getStartConnectorPoint(), 3, Color.MAGENTA);
            paintDot(graphics, edge.getEndConnectorPoint(), 3, Color.MAGENTA);
        });
        if (draggingEdgeRenderer != null) {
            graphics.setPaint(Color.GRAY);
            draggingEdgeRenderer.paint(graphics);
            paintCircle(graphics, draggingEdgeRenderer.getStartConnectorPoint(), 3, Color.MAGENTA);
        }
        graphics.setPaint(Color.BLUE);
        selection.forEach(renderer -> renderer.paint(graphics));
        if (connectorPoint != null) {
            if (draggingEdgeRenderer == null) {
                paintCircle(graphics, connectorPoint, 4, Color.RED);
            }
            else {
                paintCircle(graphics, connectorPoint, 3, Color.MAGENTA);
            }
        }
        if (edgePoint != null) {
            graphics.setPaint(Color.RED);
            if (edgeBendSelected) {
                paintDot(graphics, edgePoint, 3, Color.RED);
            }
            else {
                paintCircle(graphics, edgePoint, 3, Color.RED);
            }
        }
        if (selectionRectangle != null) {
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.drawRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width, selectionRectangle.height);
        }
    }

    private static void paintDot(Graphics2D graphics, Point point, int radius, Paint paint) {
        int size = radius * 2 + 1;
        graphics.setPaint(paint);
        graphics.fillOval(point.x - radius, point.y - radius, size, size);
    }

    private static void paintCircle(Graphics2D graphics, Point point, int radius, Paint paint) {
        int size = radius * 2 + 1;
        graphics.setPaint(paint);
        graphics.drawOval(point.x - radius, point.y - radius, size, size);
    }

    public ComponentUpdate handleMouseMoved(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                handleMouseMoved(event.getPoint());
            case CREATING_EDGE ->
                handleMouseMoved(event.getPoint());
            case MOVING_SELECTION ->
                ComponentUpdate.NO_OPERATION;
            case MOVING_EDGE_POINT ->
                ComponentUpdate.NO_OPERATION;
            case RESIZING_VERTEX ->
                ComponentUpdate.NO_OPERATION;
            case SELECTING_AREA ->
                ComponentUpdate.NO_OPERATION;
        };
    }

    public ComponentUpdate handleMousePressed(MouseEvent event) {
        eventModifiers = event.getModifiersEx();
        return switch (state) {
            case IDLE ->
                handleMousePressed(Button.get(event, eventModifiers), event.getPoint());
            case CREATING_EDGE ->
                ComponentUpdate.NO_OPERATION;
            case MOVING_SELECTION ->
                ComponentUpdate.NO_OPERATION;
            case MOVING_EDGE_POINT ->
                ComponentUpdate.NO_OPERATION;
            case RESIZING_VERTEX ->
                ComponentUpdate.NO_OPERATION;
            case SELECTING_AREA ->
                ComponentUpdate.NO_OPERATION;
        };
    }

    public ComponentUpdate handleMouseClicked(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                handleMouseClicked(event.getPoint(), Button.get(event, eventModifiers));
            case CREATING_EDGE ->
                clickedDraggingEdge(Button.get(event, eventModifiers));
            case MOVING_SELECTION ->
                ComponentUpdate.NO_OPERATION;
            case MOVING_EDGE_POINT ->
                ComponentUpdate.NO_OPERATION;
            case RESIZING_VERTEX ->
                ComponentUpdate.NO_OPERATION;
            case SELECTING_AREA ->
                ComponentUpdate.NO_OPERATION;
        };
    }

    public ComponentUpdate handleMouseDragged(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                initializeSelectionRectangle(event.getPoint());
            case CREATING_EDGE ->
                dragNewEdge(event.getPoint());
            case MOVING_SELECTION ->
                moveSelection(event.getPoint());
            case MOVING_EDGE_POINT ->
                moveEdgePoint(event.getPoint());
            case RESIZING_VERTEX ->
                resizeVertex(event.getPoint());
            case SELECTING_AREA ->
                resizeSelectionRectangle(event.getPoint());
        };
    }

    public ComponentUpdate handleMouseReleased(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                ComponentUpdate.NO_OPERATION;
            case CREATING_EDGE ->
                updateDraggingEdge(event.getPoint());
            case MOVING_SELECTION ->
                finishSelectionMove(event.getPoint());
            case MOVING_EDGE_POINT ->
                finishEdgePointMove();
            case RESIZING_VERTEX ->
                finishResizingVertex();
            case SELECTING_AREA ->
                finishSelectionRectangle();
        };
    }

    public ComponentUpdate handleKeyReleased(KeyEvent event) {
        if (Keyboard.getInstance().isDelete(event)) {
            return deleteSelection();
        }
        if (Keyboard.getInstance().isUndo(event)) {
            return historyUndo();
        }
        if (Keyboard.getInstance().isRedo(event)) {
            return historyRedo();
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate deleteSelection() {
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
        history.addElementDeletion(verticesToRemove, edgesToRemove);
        selection.clear();
        return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    private ComponentUpdate historyUndo() {
        if (!history.canUndo()) {
            return ComponentUpdate.NO_OPERATION;
        }
        history.undo();
        return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    private ComponentUpdate historyRedo() {
        if (!history.canRedo()) {
            return ComponentUpdate.NO_OPERATION;
        }
        history.redo();
        return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    private ComponentUpdate handleMouseMoved(Point cursor) {
        boolean needRepaint = connectorPoint != null || edgePoint != null || hovered != null;
        connectorPoint = null;
        edgePoint = null;
        hovered = null;
        VertexRenderer nearestVertex = findNearestVertex(cursor);
        if (nearestVertex != null) {
            return handleVertexHovered(nearestVertex, cursor, needRepaint);
        }
        EdgeRenderer nearestEdge = findNearestEdge(cursor);
        if (nearestEdge != null) {
            return handleEdgeHovered(nearestEdge, cursor);
        }
        return new ComponentUpdate(Cursor.DEFAULT_CURSOR, needRepaint);
    }

    private ComponentUpdate handleVertexHovered(VertexRenderer vertex, Point cursor, boolean needRepaint) {
        long distance = vertex.squareDistance(cursor);
        if (isInside(distance)) {
            hovered = vertex;
            return new ComponentUpdate(Cursor.HAND_CURSOR, needRepaint);
        }
        if (isOnBorder(distance)) {
            return new ComponentUpdate(getResizeCursorType(cursor, vertex.getLocation()), needRepaint);
        }
        if (isNear(distance)) {
            connectorPoint = cursor;
            return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
        }
        return (needRepaint) ? ComponentUpdate.REPAINT : ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate handleEdgeHovered(EdgeRenderer nearestEdge, Point cursor) {
        edgeBendSelected = nearestEdge.getPoints().stream().anyMatch(p -> isNear(cursor, p));
        hovered = nearestEdge;
        edgePoint = cursor;
        return ComponentUpdate.repaint(Cursor.HAND_CURSOR);
    }

    private static int getResizeCursorType(Point cursor, Point vertexLocation) {
        int dx = cursor.x - vertexLocation.x;
        int dy = cursor.y - vertexLocation.y;
        double angle = (dy != 0) ? Math.abs(Math.atan((double) dx / dy)) : SQUARE_ANGLE;
        if (angle < VERTICAL_MARGIN) {
            return (dy < 0) ? Cursor.N_RESIZE_CURSOR : Cursor.S_RESIZE_CURSOR;
        }
        if (angle > HORIZONTAL_MARGIN) {
            return (dx < 0) ? Cursor.W_RESIZE_CURSOR : Cursor.E_RESIZE_CURSOR;
        }
        if (dx < 0) {
            return (dy < 0) ? Cursor.NW_RESIZE_CURSOR : Cursor.SW_RESIZE_CURSOR;
        }
        return (dy < 0) ? Cursor.NE_RESIZE_CURSOR : Cursor.SE_RESIZE_CURSOR;
    }

    private ComponentUpdate handleMousePressed(Button button, Point cursor) {
        if (button == Button.MAIN) {
            dragStartPoint = cursor;
            dragPoint = cursor;
            VertexRenderer nearestVertex = findNearestVertex(dragStartPoint);
            if (nearestVertex != null) {
                handleVertexPressed(nearestVertex);
            }
            else {
                EdgeRenderer nearestEdge = findNearestEdge(dragStartPoint);
                if (nearestEdge != null) {
                    handleEdgePressed(nearestEdge);
                }
            }
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private void handleVertexPressed(VertexRenderer vertex) {
        if (connectorPoint != null) {
            draggingEdgeRenderer = new EdgeRenderer(vertex);
            state = State.CREATING_EDGE;
        }
        else {
            long distance = vertex.squareDistance(dragStartPoint);
            if (isOnBorder(distance)) {
                draggingVertex = vertex;
                originalDimension = vertex.getDimension();
                state = State.RESIZING_VERTEX;
            }
            else {
                if (!selection.contains(vertex)) {
                    selectSingleVertex(vertex);
                }
                state = State.MOVING_SELECTION;
            }
        }
    }

    private void selectSingleVertex(VertexRenderer vertex) {
        selection.clear();
        selection.add(vertex);
    }

    private void handleEdgePressed(EdgeRenderer nearestEdge) {
        originalEdgePoints = deepCopy(nearestEdge.getPoints());
        edgePoint = nearestEdgePoint(nearestEdge, dragStartPoint);
        draggingEdgeRenderer = nearestEdge;
        state = State.MOVING_EDGE_POINT;
    }

    private ComponentUpdate dragNewEdge(Point cursor) {
        VertexRenderer nearestVertex = findNearestVertex(cursor);
        if (nearestVertex == null) {
            connectorPoint = null;
        }
        else {
            if (!cursor.equals(nearestVertex.getLocation())) {
                connectorPoint = nearestVertex.getConnectorPoint(cursor);
            }
        }
        return addEdgePoint(cursor);
    }

    private ComponentUpdate handleMouseClicked(Point cursor, Button button) {
        if (button == Button.MAIN) {
            return mainButtonClicked(cursor);
        }
        if (button == Button.TOGGLE_SELECT) {
            return toggleSelection(cursor);
        }
        if (button == Button.RESET) {
            return clearSelection();
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate mainButtonClicked(Point cursor) {
        selection.clear();
        EdgeRenderer nearestEdge = findNearestEdge(cursor);
        if (nearestEdge != null) {
            selection.add(nearestEdge);
            return ComponentUpdate.REPAINT;
        }
        VertexRenderer nearest = findNearestVertex(cursor);
        if (nearest == null) {
            VertexRenderer vertex = new VertexRenderer(cursor);
            vertices.add(vertex);
            history.addVertexInsertion(vertex);
        }
        else {
            selection.add(nearest);
        }
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate toggleSelection(Point cursor) {
        VertexRenderer nearestVertex = findNearestVertex(cursor);
        if (nearestVertex != null) {
            if (selection.contains(nearestVertex)) {
                selection.remove(nearestVertex);
            }
            else {
                selection.add(nearestVertex);
            }
            return ComponentUpdate.REPAINT;
        }
        EdgeRenderer nearestEdge = findNearestEdge(cursor);
        if (nearestEdge != null) {
            if (selection.contains(nearestEdge)) {
                selection.remove(nearestEdge);
            }
            else {
                selection.add(nearestEdge);
            }
            return ComponentUpdate.REPAINT;
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate clearSelection() {
        selection.clear();
        state = State.IDLE;
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate clickedDraggingEdge(Button button) {
        if (button == Button.RESET) {
            draggingEdgeRenderer = null;
            state = State.IDLE;
            return ComponentUpdate.REPAINT;
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate initializeSelectionRectangle(Point cursor) {
        createSelectionRectangle(cursor);
        state = State.SELECTING_AREA;
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate resizeSelectionRectangle(Point cursor) {
        createSelectionRectangle(cursor);
        return ComponentUpdate.REPAINT;
    }

    private void createSelectionRectangle(Point cursor) {
        selectionRectangle = new Rectangle(dragStartPoint.x, dragStartPoint.y, xDistanceToDragStart(cursor), yDistanceToDragStart(cursor));
        if (selectionRectangle.width < 0) {
            selectionRectangle.x += selectionRectangle.width;
            selectionRectangle.width = -selectionRectangle.width;
        }
        if (selectionRectangle.height < 0) {
            selectionRectangle.y += selectionRectangle.height;
            selectionRectangle.height = -selectionRectangle.height;
        }
    }

    private ComponentUpdate finishSelectionRectangle() {
        selection.clear();
        vertices.stream()
            .filter(vertex -> selectionRectangle.contains(vertex.getLocation()))
            .forEach(vertex -> selection.add(vertex));
        edges.stream()
            .filter(edge -> selection.contains(edge.getStart()))
            .filter(edge -> selection.contains(edge.getEnd()))
            .forEach(edge -> selection.add(edge));
        selectionRectangle = null;
        state = State.IDLE;
        return ComponentUpdate.REPAINT;
    }

    private Point nearestEdgePoint(EdgeRenderer edge, Point point) {
        Point p0 = edge.getStartConnectorPoint();
        if (isNear(point, p0)) {
            edge.addPoint(0, point);
            return point;
        }
        int index = 0;
        for (Point p1 : edge.getPoints()) {
            if (isNear(point, p1)) {
                return p1;
            }
            if (isNear(point, p0, p1)) {
                edge.addPoint(index, point);
                return point;
            }
            p0 = p1;
            ++index;
        }
        edge.addPoint(point);
        return point;
    }

    private ComponentUpdate updateDraggingEdge(Point cursor) {
        VertexRenderer vertex = findNearestVertex(cursor);
        if (vertex != null) {
            draggingEdgeRenderer.setEnd(vertex);
            cleanup(draggingEdgeRenderer);
            edges.add(draggingEdgeRenderer);
            history.addEdgeInsertion(draggingEdgeRenderer);
            draggingEdgeRenderer = null;
            state = State.IDLE;
        }
        else {
            draggingEdgeRenderer.addPoint(cursor);
            draggingEdgeRenderer.setEnd(dragEndPoint(cursor));
        }
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate resizeVertex(Point target) {
        draggingVertex.resize(target);
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate finishResizingVertex() {
        edges.stream()
            .filter(edge -> draggingVertex.equals(edge.getStart()) || draggingVertex.equals(edge.getEnd()))
            .forEach(edge -> cleanup(edge));
        history.addVertexSizeChange(draggingVertex, originalDimension);
        originalDimension = null;
        draggingVertex = null;
        state = State.IDLE;
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate moveEdgePoint(Point cursor) {
        edgePoint.setLocation(cursor);
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate finishEdgePointMove() {
        cleanup(draggingEdgeRenderer);
        history.addEdgeTransformation(draggingEdgeRenderer, originalEdgePoints);
        draggingEdgeRenderer = null;
        originalEdgePoints = null;
        edgePoint = null;
        state = State.IDLE;
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate moveSelection(Point cursor) {
        int deltaX = cursor.x - dragPoint.x;
        int deltaY = cursor.y - dragPoint.y;
        Point vector = new Point(deltaX, deltaY);
        selection.forEach(element -> element.move(vector));
        dragPoint = cursor;
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate finishSelectionMove(Point cursor) {
        Map<EdgeRenderer, List<Point>> affectedEdges = edges.stream()
            .filter(edge -> selection.contains(edge.getStart()) != selection.contains(edge.getEnd()))
            .collect(Collectors.toMap(
                edge -> edge,
                edge -> deepCopy(edge.getPoints())));
        Iterator<EdgeRenderer> it = affectedEdges.keySet().iterator();
        while (it.hasNext()) {
            if (!cleanup(it.next())) {
                it.remove();
            }
        }
        history.addElementRelocation(selection, new Point(xDistanceToDragStart(cursor), yDistanceToDragStart(cursor)), affectedEdges);
        state = State.IDLE;
        return ComponentUpdate.REPAINT;
    }

    private static List<Point> deepCopy(List<Point> list) {
        return list.stream().map(point -> new Point(point)).collect(Collectors.toList());
    }

    private ComponentUpdate addEdgePoint(Point cursor) {
        draggingEdgeRenderer.setEnd(dragEndPoint(cursor));
        return ComponentUpdate.REPAINT;
    }

    private static VertexRenderer dragEndPoint(Point location) {
        return new VertexRenderer(location);
    }

    private VertexRenderer findNearestVertex(Point point) {
        TreeMap<Long, VertexRenderer> distances = new TreeMap<>();
        vertices.forEach(vertexRenderer -> distances.put(vertexRenderer.squareDistance(point), vertexRenderer));
        Map.Entry<Long, VertexRenderer> nearest = distances.floorEntry(NEAR_DISTANCE);
        if (nearest == null) {
            return null;
        }
        return nearest.getValue();
    }

    private EdgeRenderer findNearestEdge(Point point) {
        TreeMap<Long, EdgeRenderer> distances = new TreeMap<>();
        edges.forEach(edgeRenderer -> distances.put(edgeRenderer.squareDistance(point), edgeRenderer));
        Map.Entry<Long, EdgeRenderer> nearest = distances.floorEntry(NEAR_DISTANCE);
        if (nearest == null) {
            return null;
        }
        return nearest.getValue();
    }

    private static boolean cleanup(EdgeRenderer edge) {
        return edge.cleanup(TWIN_TOLERANCE, ACUTE_COSINE_LIMIT, OBTUSE_COSINE_LIMIT);
    }

    private Collection<EdgeRenderer> incidentEdges(VertexRenderer vertex) {
        return edges.stream()
            .filter(edge -> vertex.equals(edge.getStart()) || vertex.equals(edge.getEnd()))
            .collect(Collectors.toList());
    }

    private static boolean isNear(Point cursor, Point linePoint1, Point linePoint2) {
        return isNear(CanvasUtil.squareDistance(cursor, linePoint1, linePoint2));
    }

    private static boolean isNear(Point cursor, Point point) {
        return isNear(CanvasUtil.squareDistance(cursor, point));
    }

    private static boolean isNear(long distance) {
        return distance < NEAR_DISTANCE;
    }

    private static boolean isOnBorder(long distance) {
        return INSIDE_BORDER_MARGIN < distance && distance < OUTSIDE_BORDER_MARGIN;
    }

    private static boolean isInside(long distance) {
        return distance <= INSIDE_BORDER_MARGIN;
    }

    private int xDistanceToDragStart(Point point) {
        return point.x - dragStartPoint.x;
    }

    private int yDistanceToDragStart(Point point) {
        return point.y - dragStartPoint.y;
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

    public void revertVertexMutation(VertexRenderer vertex, Point originalLocation, Dimension originalDimension) {
        vertex.setLocation(originalLocation);
        vertex.setDimension(originalDimension);
    }

    public void revertElementRelocation(Collection<Element> elements, Point vector) {
        elements.forEach(element -> element.move(vector));
    }


    public DrawHistory getDrawHistory() {
        return history;
    }

    private enum State {
        IDLE, CREATING_EDGE, MOVING_SELECTION, MOVING_EDGE_POINT, RESIZING_VERTEX, SELECTING_AREA
    }


    private enum Button {
        MAIN(MouseEvent.BUTTON1),
        TOGGLE_SELECT(MouseEvent.BUTTON1, Keyboard.getInstance().getToggleSelectionModifiers()),
        RESET(MouseEvent.BUTTON3),
        UNSUPPORTED(0);

        private Button(int buttonId, int modifiers) {
            this.buttonId = buttonId;
            this.modifiers = modifiers;
        }

        private Button(int buttonId) {
            this(buttonId, 0);
        }

        public static Button get(MouseEvent event, int eventModifiers) {
            if (event.getClickCount() != 1) {
                return UNSUPPORTED;
            }
            return Arrays.asList(values()).stream()
                .filter(button -> event.getButton() == button.buttonId)
                .filter(button -> button.modifiersMatch(eventModifiers))
                .findAny()
                .orElse(UNSUPPORTED);
        }

        private boolean modifiersMatch(int eventModifiers) {
            final int MASK = InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
            return (eventModifiers & MASK) == modifiers;
        }

        private final int buttonId;
        private final int modifiers;
    }

    private final DrawHistory history = new DrawHistory(this);

    private final Collection<VertexRenderer> vertices = new LinkedList<>();
    private final Collection<EdgeRenderer> edges = new LinkedList<>();

    private State state = State.IDLE;
    private final Set<Element> selection = new HashSet<>();

    private Point dragStartPoint;
    private Point dragPoint;
    private Dimension originalDimension;
    private List<Point> originalEdgePoints;
    private Point connectorPoint;
    private Point edgePoint;
    private VertexRenderer draggingVertex;
    private EdgeRenderer draggingEdgeRenderer;
    private Renderer hovered;
    private Rectangle selectionRectangle;
    private boolean edgeBendSelected;

    private int eventModifiers; // Because of a bug in modifiers from the mouseClicked event, modifiers from the emousePressed event need to be remembered.

    private static final long INSIDE_BORDER_MARGIN = -4 * 4;
    private static final long OUTSIDE_BORDER_MARGIN = 4 * 4;
    private static final long NEAR_DISTANCE = 5 * 5;
    private static final double ACUTE_COSINE_LIMIT = -0.99;
    private static final double OBTUSE_COSINE_LIMIT = 0.99;
    private static final long TWIN_TOLERANCE = 3 * 3;
    private static final double SQUARE_ANGLE = 0.5 * Math.PI;
    private static final double HORIZONTAL_MARGIN = 0.4 * Math.PI;
    private static final double VERTICAL_MARGIN = 0.1 * Math.PI;

}
