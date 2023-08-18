/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


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
            graphics.setPaint(Color.MAGENTA);
            Point connector = edge.getStartConnectorPoint();
            graphics.fillOval(connector.x - 3, connector.y - 3, 7, 7);
            connector = edge.getEndConnectorPoint();
            graphics.fillOval(connector.x - 3, connector.y - 3, 7, 7);
        });
        if (draggingEdgeRenderer != null) {
            graphics.setPaint(Color.DARK_GRAY);
            draggingEdgeRenderer.paint(graphics);
            Point connector = draggingEdgeRenderer.getStartConnectorPoint();
            graphics.setPaint(Color.MAGENTA);
            graphics.drawOval(connector.x - 3, connector.y - 3, 7, 7);
        }
        graphics.setPaint(Color.BLUE);
        selection.forEach(renderer -> renderer.paint(graphics));
        if (connectorPoint != null) {
            if (draggingEdgeRenderer == null) {
                graphics.setPaint(Color.RED);
                graphics.drawOval(connectorPoint.x - 4, connectorPoint.y - 4, 9, 9);
            }
            else {
                graphics.setPaint(Color.MAGENTA);
//                Point connector = draggingEdgeRenderer.getStartConnectorPoint();
                graphics.drawOval(connectorPoint.x - 3, connectorPoint.y - 3, 7, 7);
            }
        }
        if (edgePoint != null) {
            graphics.setPaint(Color.RED);
            if (edgeBendSelected) {
                graphics.fillOval(edgePoint.x - 3, edgePoint.y - 3, 7, 7);
            }
            else {
                graphics.drawOval(edgePoint.x - 3, edgePoint.y - 3, 7, 7);
            }
        }
        if (selectionRectangle != null) {
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.drawRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width, selectionRectangle.height);
        }
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
                finishVertexMove(event.getPoint());
            case MOVING_EDGE_POINT ->
                finishEdgePointMove();
            case RESIZING_VERTEX ->
                finishResizingVertex();
            case SELECTING_AREA ->
                finishSelectionRectangle();
        };
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

    private ComponentUpdate handleVertexHovered(VertexRenderer nearestVertex, Point point, boolean needRepaint) {
        long distance = nearestVertex.squareDistance(point);
        System.out.println(distance);
        if (isInside(distance)) {
            hovered = nearestVertex;
            return new ComponentUpdate(Cursor.HAND_CURSOR, needRepaint);
        }
        if (isOnBorder(distance)) {
            return new ComponentUpdate(getPositionCursorType(point, nearestVertex.getLocation()), needRepaint);
        }
        if (isNear(distance)) {
            connectorPoint = point;
            return new ComponentUpdate(Cursor.DEFAULT_CURSOR, true);
        }
        return (needRepaint) ? ComponentUpdate.REPAINT : ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate handleEdgeHovered(EdgeRenderer nearestEdge, Point point) {
        edgeBendSelected = nearestEdge.getPoints().stream().anyMatch(p -> isNear(point, p));
        hovered = nearestEdge;
        edgePoint = point;
        return new ComponentUpdate(Cursor.HAND_CURSOR, true);
    }

    private static int getPositionCursorType(Point cursor, Point location) {
        final double SQUARE_ANGLE = 0.5 * Math.PI;
        final double HORIZONTAL_MARGIN = 0.4 * Math.PI;
        final double VERTICAL_MARGIN = 0.1 * Math.PI;
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
                if (!selection.contains(vertex)) {
                    selection.clear();
                    selection.add(vertex);
                }
                state = State.MOVING_SELECTION;
            }
        }
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
            selection.clear();
            VertexRenderer nearest = findNearestVertex(point);
            if (nearest == null) {
                vertices.add(new DefaultVertexRenderer(point));
            }
            else {
                selection.add(nearest);
            }
            return ComponentUpdate.REPAINT;
        }
        if (button == Button.TOGGLE_SELECT) {
            VertexRenderer nearest = findNearestVertex(point);
            if (nearest != null) {
                if (selection.contains(nearest)) {
                    selection.remove(nearest);
                }
                else {
                    selection.add(nearest);
                }
                return ComponentUpdate.REPAINT;
            }
            return ComponentUpdate.NO_OPERATION;
        }
        if (button == Button.RESET) {
            resetSelection();
            return ComponentUpdate.REPAINT;
        }
        return ComponentUpdate.NO_OPERATION;
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
        selection.clear();
        vertices.stream()
            .filter(vertex -> selectionRectangle.contains(vertex.getLocation()))
            .forEach(vertex -> selection.add(vertex));
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

    private void resetSelection() {
        selection.clear();
        state = State.IDLE;
    }

    private ComponentUpdate updateDraggingEdge(Point point) {
        VertexRenderer nearest = findNearestVertex(point);
        if (nearest != null) {
            draggingEdgeRenderer.setEnd(nearest);
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

    private ComponentUpdate finishVertexMove(Point point) {
        state = State.IDLE;
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate finishEdgePointMove() {
        draggingEdgeRenderer = null;
        state = State.IDLE;
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate resizeVertex(Point target) {
        draggingVertex.resize(target);
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate finishResizingVertex() {
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
        selection.forEach(renderer -> {
            Point location = renderer.getLocation();
            location.move(location.x + deltaX, location.y + deltaY);
        });
        edges.stream()
            .filter(edge -> selection.contains(edge.getStart()))
            .filter(edge -> selection.contains(edge.getEnd()))
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
    private final Set<VertexRenderer> selection = new HashSet<>();

    private Point cursor;
    private Point connectorPoint;
    private Point edgePoint;
    private VertexRenderer draggingVertex;
    private EdgeRenderer draggingEdgeRenderer;
    private Renderer hovered;
    private Rectangle selectionRectangle;
    private boolean edgeBendSelected;

    private int eventModifiers; // Because of a bug in modifiers from the mouseClicked event, modifiers from the emousePressed event need to be remembered.

    private static final long INSIDE_BORDER_MARGIN = -25;
    private static final long OUTSIDE_BORDER_MARGIN = 25;
    private static final long NEAR_DISTANCE = 40;

}
