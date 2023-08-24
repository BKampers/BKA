/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import java.awt.*;
import java.awt.event.*;
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
            graphics.setPaint(Color.DARK_GRAY);
            draggingEdgeRenderer.paint(graphics);
            paintCircle(graphics, draggingEdgeRenderer.getStartConnectorPoint(), 3, Color.MAGENTA);
        }
        graphics.setPaint(Color.BLUE);
        vertexSelection.forEach(renderer -> renderer.paint(graphics));
        edgeSelection.forEach(renderer -> renderer.paint(graphics));
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
        cursor = event.getPoint();
        eventModifiers = event.getModifiersEx();
        return switch (state) {
            case IDLE ->
                handleMousePressed(Button.get(event, eventModifiers));
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
        cursor = null;
        return switch (state) {
            case IDLE ->
                ComponentUpdate.NO_OPERATION;
            case CREATING_EDGE ->
                updateDraggingEdge(event.getPoint());
            case MOVING_SELECTION ->
                finishSelectionMove();
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
            edgeSelection.forEach(edge -> edges.remove(edge));
            edgeSelection.clear();
            vertexSelection.forEach(vertex -> removeVertex(vertex));
            vertexSelection.clear();
            return ComponentUpdate.REPAINT;
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private void removeVertex(VertexRenderer vertex) {
        edges.removeAll(
            edges.stream()
                .filter(edge -> vertex.equals(edge.getStart()) || vertex.equals(edge.getEnd()))
                .collect(Collectors.toList()));
        vertices.remove(vertex);
    }

    private ComponentUpdate handleMouseMoved(Point point) {
        boolean needRepaint = connectorPoint != null || edgePoint != null || hovered != null;
        connectorPoint = null;
        edgePoint = null;
        hovered = null;
        VertexRenderer nearestVertex = findNearestVertex(point);
        if (nearestVertex != null) {
            return handleVertexHovered(nearestVertex, point, needRepaint);
        }
        EdgeRenderer nearestEdge = findNearestEdge(point);
        if (nearestEdge != null) {
            return handleEdgeHovered(nearestEdge, point);
        }
        return new ComponentUpdate(Cursor.DEFAULT_CURSOR, needRepaint);
    }

    private ComponentUpdate handleVertexHovered(VertexRenderer vertex, Point point, boolean needRepaint) {
        long distance = vertex.squareDistance(point);
        if (isInside(distance)) {
            hovered = vertex;
            return new ComponentUpdate(Cursor.HAND_CURSOR, needRepaint);
        }
        if (isOnBorder(distance)) {
            return new ComponentUpdate(getResizeCursorType(point, vertex.getLocation()), needRepaint);
        }
        if (isNear(distance)) {
            connectorPoint = point;
            return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
        }
        return (needRepaint) ? ComponentUpdate.REPAINT : ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate handleEdgeHovered(EdgeRenderer nearestEdge, Point point) {
        edgeBendSelected = nearestEdge.getPoints().stream().anyMatch(p -> isNear(point, p));
        hovered = nearestEdge;
        edgePoint = point;
        return ComponentUpdate.repaint(Cursor.HAND_CURSOR);
    }

    private static int getResizeCursorType(Point cursor, Point location) {
        int dx = cursor.x - location.x;
        int dy = cursor.y - location.y;
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

    private ComponentUpdate handleMousePressed(Button button) {
        if (button == Button.MAIN) {
            VertexRenderer nearestVertex = findNearestVertex(cursor);
            if (nearestVertex != null) {
                handleVertexPressed(nearestVertex);
            }
            else {
                EdgeRenderer nearestEdge = findNearestEdge(cursor);
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
            long distance = vertex.squareDistance(cursor);
            if (isOnBorder(distance)) {
                draggingVertex = vertex;
                state = State.RESIZING_VERTEX;
            }
            else {
                if (!vertexSelection.contains(vertex)) {
                    selectSingleVertex(vertex);
                }
                state = State.MOVING_SELECTION;
            }
        }
    }

    private void selectSingleVertex(VertexRenderer vertex) {
        edgeSelection.clear();
        vertexSelection.clear();
        vertexSelection.add(vertex);
    }

    private void handleEdgePressed(EdgeRenderer nearestEdge) {
        edgePoint = nearestEdgePoint(nearestEdge, cursor);
        draggingEdgeRenderer = nearestEdge;
        state = State.MOVING_EDGE_POINT;
    }

    private ComponentUpdate dragNewEdge(Point cursor) {
        VertexRenderer nearestVertex = findNearestVertex(cursor);
        connectorPoint = (nearestVertex != null) ? nearestVertex.getConnectorPoint(cursor) : null;
        return addEdgePoint(cursor);
    }

    private ComponentUpdate handleMouseClicked(Point point, Button button) {
        if (button == Button.MAIN) {
            return mainButtonClicked(point);
        }
        if (button == Button.TOGGLE_SELECT) {
            return toggleSelection(point);
        }
        if (button == Button.RESET) {
            return resetSelection();
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate mainButtonClicked(Point point) {
        vertexSelection.clear();
        edgeSelection.clear();
        EdgeRenderer nearestEdge = findNearestEdge(point);
        if (nearestEdge != null) {
            edgeSelection.add(nearestEdge);
            return ComponentUpdate.REPAINT;
        }
        VertexRenderer nearest = findNearestVertex(point);
        if (nearest == null) {
            vertices.add(new DefaultVertexRenderer(point));
        }
        else {
            vertexSelection.clear();
            edgeSelection.clear();
            vertexSelection.add(nearest);
        }
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate toggleSelection(Point point) {
        VertexRenderer nearestVertex = findNearestVertex(point);
        if (nearestVertex != null) {
            if (vertexSelection.contains(nearestVertex)) {
                vertexSelection.remove(nearestVertex);
            }
            else {
                vertexSelection.add(nearestVertex);
            }
            return ComponentUpdate.REPAINT;
        }
        EdgeRenderer nearestEdge = findNearestEdge(point);
        if (nearestEdge != null) {
            if (edgeSelection.contains(nearestEdge)) {
                edgeSelection.remove(nearestEdge);
            }
            else {
                edgeSelection.add(nearestEdge);
            }
            return ComponentUpdate.REPAINT;
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate resetSelection() {
        edgeSelection.clear();
        vertexSelection.clear();
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

    private ComponentUpdate initializeSelectionRectangle(Point point) {
        createSelectionRectangle(point);
        state = State.SELECTING_AREA;
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate resizeSelectionRectangle(Point point) {
        createSelectionRectangle(point);
        return ComponentUpdate.REPAINT;
    }

    private void createSelectionRectangle(Point corner) {
        selectionRectangle = new Rectangle(cursor.x, cursor.y, xDistanceToCursor(corner), yDistanceToCursor(corner));
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
        edgeSelection.clear();
        vertexSelection.clear();
        vertices.stream()
            .filter(vertex -> selectionRectangle.contains(vertex.getLocation()))
            .forEach(vertex -> vertexSelection.add(vertex));
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

    private ComponentUpdate updateDraggingEdge(Point point) {
        VertexRenderer vertex = findNearestVertex(point);
        if (vertex != null) {
            draggingEdgeRenderer.setEnd(vertex);
            cleanup(draggingEdgeRenderer);
            edges.add(draggingEdgeRenderer);
            draggingEdgeRenderer = null;
            state = State.IDLE;
        }
        else {
            draggingEdgeRenderer.addPoint(point);
            draggingEdgeRenderer.setEnd(dragEndPoint(point));
        }
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate finishSelectionMove() {
        edges.stream().filter(edge -> vertexSelection.contains(edge.getStart()) != vertexSelection.contains(edge.getEnd())).forEach(edge -> cleanup(edge));
        state = State.IDLE;
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate finishEdgePointMove() {
        cleanup(draggingEdgeRenderer);
        draggingEdgeRenderer = null;
        state = State.IDLE;
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate resizeVertex(Point target) {
        draggingVertex.resize(target);
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate finishResizingVertex() {
        edges.stream().filter(edge -> draggingVertex.equals(edge.getStart()) || draggingVertex.equals(edge.getEnd())).forEach(edge -> cleanup(edge));
        draggingVertex = null;
        state = State.IDLE;
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate moveEdgePoint(Point point) {
        edgePoint.setLocation(point);
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate moveSelection(Point target) {
        int deltaX = xDistanceToCursor(target);
        int deltaY = yDistanceToCursor(target);
        vertexSelection.forEach(renderer -> {
            Point location = renderer.getLocation();
            location.move(location.x + deltaX, location.y + deltaY);
        });
        edges.stream()
            .filter(edge -> vertexSelection.contains(edge.getStart()))
            .filter(edge -> vertexSelection.contains(edge.getEnd()))
            .forEach(edge -> edge.getPoints().forEach(point -> point.move(point.x + deltaX, point.y + deltaY)));
        cursor = target;
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate addEdgePoint(Point point) {
        draggingEdgeRenderer.setEnd(dragEndPoint(point));
        return ComponentUpdate.REPAINT;
    }

    private static VertexRenderer dragEndPoint(Point point) {
        return new VertexRenderer() {
            @Override
            public Point getLocation() {
                return point;
            }

            @Override
            public void paint(Graphics2D graphics) {
                graphics.drawOval(point.x - 2, point.y - 2, 5, 5);
            }
        };
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

    private static void cleanup(EdgeRenderer edge) {
        edge.cleanup(TWIN_TOLERANCE, ACUTE_COSINE_LIMIT, OBTUSE_COSINE_LIMIT);
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

    private int xDistanceToCursor(Point point) {
        return point.x - cursor.x;
    }

    private int yDistanceToCursor(Point point) {
        return point.y - cursor.y;
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
                .filter(button -> modifiersMatch(eventModifiers, button))
                .findAny()
                .orElse(UNSUPPORTED);
        }

        private static boolean modifiersMatch(int eventModifiers, Button button) {
            final int MASK = InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
            return (eventModifiers & MASK) == button.modifiers;
        }

        private final int buttonId;
        private final int modifiers;
    }


    private final Collection<VertexRenderer> vertices = new LinkedList<>();
    private final Collection<EdgeRenderer> edges = new LinkedList<>();

    private State state = State.IDLE;
    private final Set<VertexRenderer> vertexSelection = new HashSet<>();
    private final Set<EdgeRenderer> edgeSelection = new HashSet<>();

    private Point cursor;
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
