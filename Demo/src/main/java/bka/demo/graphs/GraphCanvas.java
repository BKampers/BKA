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
            graphics.setPaint(Color.GREEN.darker());
            draggingEdgeRenderer.paint(graphics);
            draggingEdgeRenderer.getEnd().paint(graphics);
        }
        graphics.setPaint(Color.BLUE);
        selection.forEach(renderer -> renderer.paint(graphics));
        graphics.setPaint(Color.ORANGE);
        hovered.forEach(renderer -> renderer.paint(graphics));
        if (connectorPoint != null) {
            graphics.setPaint(Color.RED);
            graphics.drawOval(connectorPoint.x - 4, connectorPoint.y - 4, 9, 9);
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
            graphics.drawRect(
                (selectionRectangle.width >= 0) ? selectionRectangle.x : selectionRectangle.x + selectionRectangle.width,
                (selectionRectangle.height >= 0) ? selectionRectangle.y : selectionRectangle.y + selectionRectangle.height,
                Math.abs(selectionRectangle.width),
                Math.abs(selectionRectangle.height));
        }
    }

    public boolean handleMousePressed(MouseEvent event) {
        cursor = event.getPoint();
        eventModifiers = event.getModifiersEx();
        return switch (state) {
            case IDLE ->
                handleMousePressed(Button.get(event, eventModifiers));
            case CREATING_EDGE ->
                false;
            case MOVING_SELECTION ->
                false;
            case MOVING_EDGE_POINT ->
                false;
            case SELECTING_AREA ->
                false;
        };
    }

    public boolean handleMouseClicked(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                handleMouseClicked(event.getPoint(), Button.get(event, eventModifiers));
            case CREATING_EDGE ->
                clickedDraggingEdge(Button.get(event, eventModifiers));
            case MOVING_SELECTION ->
                false;
            case MOVING_EDGE_POINT ->
                false;
            case SELECTING_AREA ->
                false;
        };
    }

    public boolean handleMouseDragged(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                initializeSelectionRectangle(event.getPoint());
            case CREATING_EDGE ->
                handleMouseMoved(event.getPoint()) | addEdgePoint(event.getPoint());
            case MOVING_SELECTION ->
                moveSelection(event.getPoint());
            case MOVING_EDGE_POINT ->
                moveEdgePoint(event.getPoint());
            case SELECTING_AREA ->
                resizeSelectionRectangle(event.getPoint());
        };
    }

    public boolean handleMouseReleased(MouseEvent event) {
        cursor = null;
        return switch (state) {
            case IDLE ->
                false;
            case CREATING_EDGE ->
                updateDraggingEdge(event.getPoint());
            case MOVING_SELECTION ->
                finishVertexMove(event.getPoint());
            case MOVING_EDGE_POINT ->
                finishEdgePointMove();
            case SELECTING_AREA -> {
                selection.clear();
                vertices.stream()
                    .filter(vertex -> selectionRectangle.contains(vertex.getLocation()))
                    .forEach(vertex -> selection.add(vertex));
                selectionRectangle = null;
                state = State.IDLE;
                yield true;
            }
        };
    }

    public boolean handleMouseMoved(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                handleMouseMoved(event.getPoint());
            case CREATING_EDGE ->
                handleMouseMoved(event.getPoint());
            case MOVING_SELECTION ->
                false;
            case MOVING_EDGE_POINT ->
                false;
            case SELECTING_AREA ->
                false;
        };
    }

    private boolean handleMouseClicked(Point point, Button button) {
        if (button == Button.MAIN) {
            selection.clear();
            VertexRenderer nearest = findNearestVertex(point);
            if (nearest != null) {
                selection.add(nearest);
            }
            else {
                vertices.add(new DefaultVertexRenderer(point));
            }
            return true;
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
            }
            return true;
        }
        if (button == Button.RESET) {
            resetSelection();
            return true;
        }
        return false;
    }

    private boolean clickedDraggingEdge(Button button) {
        if (button == Button.MAIN) {
            return false;
        }
        draggingEdgeRenderer = null;
        state = State.IDLE;
        return true;
    }

    private boolean handleMousePressed(Button button) {
        if (button == Button.MAIN) {
            VertexRenderer nearestVertex = findNearestVertex(cursor);
            if (nearestVertex != null) {
                if (connectorPoint != null) {
                    draggingEdgeRenderer = new EdgeRenderer(nearestVertex);
                    state = State.CREATING_EDGE;
                }
                else if (selection.contains(nearestVertex)) {
                    state = State.MOVING_SELECTION;
                }
                return false;
            }
            EdgeRenderer nearestEdge = findNearestEdge(cursor);
            if (nearestEdge != null) {
                edgePoint = nearestEdgePoint(nearestEdge, cursor);
                draggingEdgeRenderer = nearestEdge;
                state = State.MOVING_EDGE_POINT;
                return false;
            }
        }
        return false;
    }

    private boolean initializeSelectionRectangle(Point point) {
        selectionRectangle = new Rectangle(cursor.x, cursor.y, xDistanceToCursor(point), yDistanceToCursor(point));
        state = State.SELECTING_AREA;
        return false;
    }

    private boolean resizeSelectionRectangle(Point point) {
        selectionRectangle.width = xDistanceToCursor(point);
        selectionRectangle.height = yDistanceToCursor(point);
        return true;
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

    private boolean updateDraggingEdge(Point point) {
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
        return true;
    }

    private boolean finishVertexMove(Point point) {
        state = State.IDLE;
        return false;
    }

    private boolean finishEdgePointMove() {
        draggingEdgeRenderer = null;
        state = State.IDLE;
        return false;
    }

    private boolean moveEdgePoint(Point point) {
        edgePoint.setLocation(point);
        return true;
    }

    private boolean handleMouseMoved(Point point) {
        boolean needRepaint = connectorPoint != null || edgePoint != null || !hovered.isEmpty();
        connectorPoint = null;
        edgePoint = null;
        hovered.clear();
        EdgeRenderer nearestEdge = findNearestEdge(point);
        VertexRenderer nearestVertex = findNearestVertex(point);
        if (nearestVertex != null) {
            long distance = nearestVertex.squareDistance(point);
            if (nearestEdge == null || distance < nearestEdge.squareDistance(point)) {
                if (distance < 0) {
                    hovered.add(nearestVertex);
                }
                else if (isNear(distance)) {
                    connectorPoint = point;
                }
                return true;
            }
        }
        else if (nearestEdge != null) {
            edgeBendSelected = nearestEdge.getPoints().stream().anyMatch(p -> isNear(point, p));
            hovered.add(nearestEdge);
            edgePoint = point;
            return true;
        }
        return needRepaint;
    }

    private boolean moveSelection(Point target) {
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
        return true;
    }

    private boolean addEdgePoint(Point point) {
        draggingEdgeRenderer.setEnd(dragEndPoint(point));
        return true;
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
        Map.Entry<Long, VertexRenderer> nearest = distances.floorEntry(EDGE_POINT_SELECTION_RANGE);
        if (nearest == null) {
            return null;
        }
        return nearest.getValue();
    }

    private EdgeRenderer findNearestEdge(Point point) {
        TreeMap<Long, EdgeRenderer> distances = new TreeMap<>();
        edges.forEach(edgeRenderer -> distances.put(edgeRenderer.squareDistance(point), edgeRenderer));
        Map.Entry<Long, EdgeRenderer> nearest = distances.floorEntry(EDGE_POINT_SELECTION_RANGE);
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
        return distance < EDGE_POINT_SELECTION_RANGE;
    }

    private int xDistanceToCursor(Point point) {
        return point.x - cursor.x;
    }

    private int yDistanceToCursor(Point point) {
        return point.y - cursor.y;
    }

    private enum State {
        IDLE, CREATING_EDGE, MOVING_SELECTION, MOVING_EDGE_POINT, SELECTING_AREA
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
    private final Set<Renderer> hovered = new HashSet<>();

    private Point cursor;
    private Point connectorPoint;
    private Point edgePoint;
    private EdgeRenderer draggingEdgeRenderer;
    private boolean edgeBendSelected;
    private Rectangle selectionRectangle;

    private int eventModifiers; // Because of a bug in modifiers from the mouseClicked event, modifiers from the emousePressed event need to be remembered.

    private static final long EDGE_POINT_SELECTION_RANGE = 10;

}
