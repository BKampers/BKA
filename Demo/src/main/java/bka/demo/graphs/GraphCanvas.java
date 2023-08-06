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
    }

    public boolean handleMouseClicked(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                handleMouseClicked(event.getPoint(), event.getButton());
            case CREATING_EDGE ->
                clickedDraggingEdge(event.getButton());
            case MOVING_VERTEX ->
                false;
            case MOVING_EDGE ->
                false;
            case MOVING_EDGE_POINT ->
                false;
            case SELECTING_AREA ->
                false;
            case MOVING_AREA ->
                false;
        };
    }

    private boolean handleMouseClicked(Point point, int button) {
        if (button == MouseEvent.BUTTON1) {
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
        if (!selection.isEmpty()) {
            selection.clear();
            return true;
        }
        return false;
    }

    private boolean clickedDraggingEdge(int button) {
        if (button == MouseEvent.BUTTON1) {
            return false;
        }
        draggingEdgeRenderer = null;
        state = State.IDLE;
        return true;
    }

    public boolean handleMousePressed(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                handleMousePressed(event.getPoint(), event.getButton());
            case CREATING_EDGE ->
                false;
            case MOVING_VERTEX ->
                false;
            case MOVING_EDGE ->
                false;
            case MOVING_EDGE_POINT ->
                false;
            case SELECTING_AREA ->
                false;
            case MOVING_AREA ->
                false;
        };
    }

    private boolean handleMousePressed(Point point, int button) {
        if (button == MouseEvent.BUTTON1) {
            VertexRenderer nearestVertex = findNearestVertex(point);
            if (nearestVertex != null) {
                if (connectorPoint != null) {
                    draggingEdgeRenderer = new EdgeRenderer(nearestVertex);
                    state = State.CREATING_EDGE;
                }
                else {
                    movingVertex = nearestVertex;
                    state = State.MOVING_VERTEX;
                }
                return false;
            }
            EdgeRenderer nearestEdge = findNearestEdge(point);
            if (nearestEdge != null) {
                edgePoint = nearestEdgePoint(nearestEdge, point);
                draggingEdgeRenderer = nearestEdge;
                state = State.MOVING_EDGE_POINT;
                return false;
            }
        }
        return false;
    }

    private Point nearestEdgePoint(EdgeRenderer edge, Point cursor) {
        Point p0 = edge.getStartConnectorPoint();
        if (isNear(cursor, p0)) {
            edge.addPoint(0, cursor);
            return cursor;
        }
        int index = 0;
        for (Point p1 : edge.getPoints()) {
            if (isNear(cursor, p1)) {
                return p1;
            }
            if (isNear(cursor, p0, p1)) {
                edge.addPoint(index, cursor);
                return cursor;
            }
            p0 = p1;
            ++index;
        }
        edge.addPoint(cursor);
        return cursor;
    }

    public boolean handleMouseReleased(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                false;
            case CREATING_EDGE ->
                updateDraggingEdge(event.getPoint());
            case MOVING_VERTEX ->
                finishVertexMove(event.getPoint());
            case MOVING_EDGE ->
                false;
            case MOVING_EDGE_POINT ->
                finishEdgePointMove();
            case SELECTING_AREA ->
                false;
            case MOVING_AREA ->
                false;
        };
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
        movingVertex.setLocation(point);
        movingVertex = null;
        state = State.IDLE;
        return true;
    }

    private boolean finishEdgePointMove() {
        draggingEdgeRenderer = null;
        state = State.IDLE;
        return false;
    }

    public boolean handleMouseMoved(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                handleMouseMoved(event.getPoint());
            case CREATING_EDGE ->
                handleMouseMoved(event.getPoint());
            case MOVING_VERTEX ->
                false;
            case MOVING_EDGE ->
                false;
            case MOVING_EDGE_POINT ->
                false;
            case SELECTING_AREA ->
                false;
            case MOVING_AREA ->
                false;
        };
    }

    public boolean handleMouseDragged(MouseEvent event) {
        return switch (state) {
            case IDLE ->
                false;
            case CREATING_EDGE ->
                handleMouseMoved(event.getPoint()) | addEdgePoint(event.getPoint());
            case MOVING_VERTEX ->
                setVertexLocation(event.getPoint());
            case MOVING_EDGE ->
                false;
            case MOVING_EDGE_POINT ->
                moveEdgePoint(event.getPoint());
            case SELECTING_AREA ->
                false;
            case MOVING_AREA ->
                false;
        };
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

    private boolean setVertexLocation(Point point) {
        movingVertex.setLocation(point);
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
        Map.Entry<Long, VertexRenderer> nearest = distances.floorEntry(10L);
        if (nearest == null) {
            return null;
        }
        return nearest.getValue();
    }

    private EdgeRenderer findNearestEdge(Point point) {
        TreeMap<Long, EdgeRenderer> distances = new TreeMap<>();
        edges.forEach(edgeRenderer -> distances.put(edgeRenderer.squareDistance(point), edgeRenderer));
        Map.Entry<Long, EdgeRenderer> nearest = distances.floorEntry(10L);
        if (nearest == null) {
            return null;
        }
        return nearest.getValue();
    }

    private static boolean isNear(Point cursor, Point linePoint1, Point linePoint2) {
        return isNear(CanvasUtil.squareDistance(cursor, linePoint1, linePoint2));
    }

    private static boolean isNear(Point cursor, Point p0) {
        return isNear(CanvasUtil.squareDistance(cursor, p0));
    }

    private static boolean isNear(long distance) {
        return distance < EDGE_POINT_SELECTION_RANGE;
    }

    private enum State {
        IDLE, CREATING_EDGE, MOVING_VERTEX, MOVING_EDGE, MOVING_EDGE_POINT, SELECTING_AREA, MOVING_AREA
    }

    private State state = State.IDLE;
    private final Set<Renderer> selection = new HashSet<>();
    private final Set<Renderer> hovered = new HashSet<>();

    private Point connectorPoint;
    private Point edgePoint;
    private VertexRenderer movingVertex;
    private EdgeRenderer draggingEdgeRenderer;
    private boolean edgeBendSelected;

    private final Collection<VertexRenderer> vertices = new LinkedList<>();
    private final Collection<EdgeRenderer> edges = new LinkedList<>();

    private static final long EDGE_POINT_SELECTION_RANGE = 10;

}
