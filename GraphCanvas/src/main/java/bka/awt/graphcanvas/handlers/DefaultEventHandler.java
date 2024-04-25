/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas.handlers;

import bka.awt.*;
import bka.awt.graphcanvas.Label;
import bka.awt.graphcanvas.*;
import bka.awt.graphcanvas.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;


public class DefaultEventHandler extends CanvasEventHandler {

    public DefaultEventHandler(GraphCanvas canvas) {
        this(canvas, null);
    }

    public DefaultEventHandler(GraphCanvas canvas, MouseButton button) {
        super(canvas);
        this.button = button;
    }

    @Override
    public CanvasUpdate mouseMoved(MouseEvent event) {
        Point cursor = event.getPoint();
        VertexComponent nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex != null) {
            return handleVertexHovered(nearestVertex, event);
        }
        EdgeComponent nearestEdge = getCanvas().findNearestEdge(cursor);
        if (nearestEdge != null && MouseButton.MAIN.matchesModifier(event)) {
            return handleEdgeHovered(nearestEdge, cursor);
        }
        boolean needRepaint = setEdgePoint(null);
        needRepaint |= setConnectorPoint(null);
        needRepaint |= setHoveredLabel(labelAt(cursor));
        GraphComponent nearestElement = getCanvas().findNearestElement(cursor);
        if (nearestElement != null && MouseButton.EDIT.matchesModifier(event)) {
            needRepaint |= setHighlightedElement(nearestElement);
            return new CanvasUpdate(Cursor.TEXT_CURSOR, needRepaint);
        }
        needRepaint |= setHighlightedElement(null);
        if (hoveredLabel != null) {
            if (MouseButton.EDIT.matchesModifier(event)) {
                return new CanvasUpdate(Cursor.TEXT_CURSOR, needRepaint);
            }
            return new CanvasUpdate(Cursor.HAND_CURSOR, needRepaint);
        }
        return new CanvasUpdate(Cursor.DEFAULT_CURSOR, needRepaint);
    }

    private Label labelAt(Point point) {
        return Stream.concat(getCanvas().getVertices().stream(), getCanvas().getEdges().stream())
            .flatMap(element -> element.getLabels().stream().filter(boundsContain(point)))
            .findAny().orElse(null);
    }

    private Predicate<Label> boundsContain(Point point) {
        return label -> {
            Rectangle bounds = label.getBounds();
            return bounds != null && bounds.contains(point);
        };
    }

    private CanvasUpdate handleVertexHovered(VertexComponent vertex, MouseEvent event) {
        boolean needRepaint = setHoveredLabel(null);
        needRepaint |= setEdgePoint(null);
        if (MouseButton.EDIT.matchesModifier(event)) {
            needRepaint |= setConnectorPoint(null);
            return new CanvasUpdate(Cursor.TEXT_CURSOR, needRepaint);
        }
        if (MouseButton.MAIN.matchesModifier(event)) {
            return handleVertexHovered(vertex, event.getPoint(), needRepaint);
        }
        return new CanvasUpdate(Cursor.DEFAULT_CURSOR, needRepaint);
    }

    private CanvasUpdate handleVertexHovered(VertexComponent vertex, Point cursor, boolean needRepaint) {
        long distance = vertex.squareDistance(cursor);
        if (CanvasUtil.isInside(distance)) {
            needRepaint |= setConnectorPoint(null);
            return new CanvasUpdate(Cursor.HAND_CURSOR, needRepaint);
        }
        if (CanvasUtil.isOnBorder(distance)) {
            needRepaint |= setConnectorPoint(null);
            return new CanvasUpdate(getResizeDirection(cursor, vertex.getLocation()).getCursorType(), needRepaint);
        }
        if (CanvasUtil.isNear(distance) && getCanvas().getVertices().size() > 1) {
            needRepaint |= setConnectorPoint(cursor);
            return new CanvasUpdate(Cursor.DEFAULT_CURSOR, needRepaint);
        }
        return (needRepaint) ? CanvasUpdate.REPAINT : CanvasUpdate.NO_OPERATION;
    }

    private CanvasUpdate handleEdgeHovered(EdgeComponent nearestEdge, Point cursor) {
        boolean needRepaint = setHoveredLabel(null);
        needRepaint |= setConnectorPoint(null);
        needRepaint |= setEdgePoint(cursor);
        edgeBendSelected = nearestEdge.getPoints().stream().anyMatch(point -> CanvasUtil.isNear(cursor, point));
        return new CanvasUpdate(Cursor.HAND_CURSOR, needRepaint);
    }

    @Override
    public CanvasUpdate mousePressed(MouseEvent event) {
        button = MouseButton.get(event);
        if (button != MouseButton.MAIN) {
            return CanvasUpdate.NO_OPERATION;
        }
        if (hoveredLabel != null) {
            getCanvas().setEventHandler(new DragLabelHandler(getCanvas(), hoveredLabel));
            return CanvasUpdate.NO_OPERATION;
        }
        Point cursor = event.getPoint();
        VertexComponent nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex != null) {
            handleVertexPressed(cursor, nearestVertex);
        }
        else {
            EdgeComponent nearestEdge = getCanvas().findNearestEdge(cursor);
            if (nearestEdge != null) {
                handleEdgePressed(cursor, nearestEdge);
            }
        }
        return CanvasUpdate.NO_OPERATION;
    }

    private void handleVertexPressed(Point cursor, VertexComponent vertex) {
        if (connectorPoint != null) {
            getCanvas().setEventHandler(new CreateEdgeHandler(getCanvas(), vertex, cursor));
        }
        else {
            long distance = vertex.squareDistance(cursor);
            if (CanvasUtil.isOnBorder(distance)) {
                getCanvas().setEventHandler(new VertexResizeHandler(getCanvas(), vertex, getResizeDirection(cursor, vertex.getLocation())));
            }
            else {
                if (!getCanvas().getSelection().contains(vertex)) {
                    getCanvas().selectSingleVertex(vertex);
                }
                getCanvas().setEventHandler(new SelectionMoveHandler(getCanvas(), cursor));
            }
        }
    }

    private void handleEdgePressed(Point cursor, EdgeComponent nearestEdge) {
        EdgeComponent.Excerpt originalShape = nearestEdge.getExcerpt();
        getCanvas().setEventHandler(new EdgePointMoveHandler(getCanvas(), dragPoint(nearestEdge, cursor), nearestEdge, originalShape));
    }

    private static Point dragPoint(EdgeComponent edge, Point point) {
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
            index++;
        }
        edge.addPoint(point);
        return point;
    }

    private static ResizeDirection getResizeDirection(Point cursor, Point vertexLocation) {
        int dx = cursor.x - vertexLocation.x;
        int dy = cursor.y - vertexLocation.y;
        double angle = Math.abs(Math.atan((double) dx / dy));
        if (angle < VERTICAL_MARGIN) {
            return (dy < 0) ? ResizeDirection.NORTH : ResizeDirection.SOUTH;
        }
        if (angle > HORIZONTAL_MARGIN) {
            return (dx < 0) ? ResizeDirection.WEST : ResizeDirection.EAST;
        }
        if (dx < 0) {
            return (dy < 0) ? ResizeDirection.NORTH_WEST : ResizeDirection.SOUTH_WEST;
        }
        return (dy < 0) ? ResizeDirection.NORTH_EAST : ResizeDirection.SOUTH_EAST;
    }

    @Override
    public CanvasUpdate mouseClicked(MouseEvent event) {
        if (button == MouseButton.MAIN) {
            return mainButtonClicked(event.getPoint());
        }
        if (button == MouseButton.TOGGLE_SELECT) {
            return toggleSelection(event.getPoint());
        }
        if (button == MouseButton.CONTEXT) {
            return contextButtonClicked(event);
        }
        if (button == MouseButton.EDIT) {
            return editLabel(event.getPoint());
        }
        return CanvasUpdate.NO_OPERATION;
    }

    private CanvasUpdate mainButtonClicked(Point cursor) {
        getCanvas().clearSelection();
        GraphComponent nearest = getCanvas().findNearestEdge(cursor);
        if (nearest == null) {
            nearest = getCanvas().findNearestVertex(cursor);
        }
        if (nearest != null) {
            getCanvas().select(nearest);
            return CanvasUpdate.REPAINT;
        }
        return addNewVertex(cursor);
    }

    private CanvasUpdate addNewVertex(Point cursor) {
        VertexComponent vertex = getCanvas().getContext().createVertexRenderer(cursor);
        if (vertex == null) {
            return CanvasUpdate.NO_OPERATION;
        }
        getCanvas().addVertex(vertex);
        getCanvas().addHistory(new ElementInsertion(vertex, getCanvas()));
        return CanvasUpdate.REPAINT;
    }

    private CanvasUpdate toggleSelection(Point cursor) {
        VertexComponent nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex != null) {
            if (getCanvas().getSelection().contains(nearestVertex)) {
                getCanvas().removeSelected(nearestVertex);
            }
            else {
                getCanvas().select(nearestVertex);
            }
            return CanvasUpdate.REPAINT;
        }
        EdgeComponent nearestEdge = getCanvas().findNearestEdge(cursor);
        if (nearestEdge != null) {
            getCanvas().toggleSelected(nearestEdge);
            return CanvasUpdate.REPAINT;
        }
        return CanvasUpdate.NO_OPERATION;
    }

    private CanvasUpdate contextButtonClicked(MouseEvent event) {
        Point cursor = event.getPoint();
        EdgeComponent nearestEdge = getCanvas().findNearestEdge(cursor);
        if (nearestEdge != null) {
            getCanvas().getContext().showEdgeMenu(nearestEdge, cursor);
            getCanvas().resetEventHandler();
            return CanvasUpdate.repaint(java.awt.Cursor.DEFAULT_CURSOR);
        }
        VertexComponent nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex != null) {
            getCanvas().getContext().showVertexMenu(nearestVertex, cursor);
            getCanvas().resetEventHandler();
            return CanvasUpdate.repaint(java.awt.Cursor.DEFAULT_CURSOR);
        }
        getCanvas().clearSelection();
        return CanvasUpdate.REPAINT;
    }

    private CanvasUpdate editLabel(Point cursor) {
        connectorPoint = null;
        GraphComponent element = hoveredLabel == null ? highlightedElement : hoveredLabel.getElement();
        if (element != null) {
            getCanvas().getContext().editString(
                labelText(hoveredLabel),
                cursor,
                applyLabelText(element, hoveredLabel, cursor));
        }
        return CanvasUpdate.NO_OPERATION;
    }

    private String labelText(Label label) {
        return (label == null) ? "" : label.getText();
    }

    private Consumer<String> applyLabelText(GraphComponent element, Label label, Point position) {
        return input -> {
            if (!input.isBlank()) {
                if (label == null) {
                    addLabel(element, position, input);
                }
                else {
                    modifyLabel(label, input);
                }
                getCanvas().getContext().requestUpdate(CanvasUpdate.REPAINT);
            }
            else if (label != null) {
                removeLabel(element, label);
                getCanvas().getContext().requestUpdate(CanvasUpdate.REPAINT);
            }
            getCanvas().getContext().requestUpdate(CanvasUpdate.noOperation(Cursor.DEFAULT_CURSOR));
        };
    }

    private void addLabel(GraphComponent element, Point cursor, String text) {
        Label newLabel = new Label(element, element.distancePositioner(cursor), text);
        element.addLabel(newLabel);
        getCanvas().addHistory(new LabelInsertion(element, newLabel));
    }

    private void modifyLabel(Label label, String text) {
        if (!text.equals(label.getText())) {
            getCanvas().addHistory(new PropertyMutation<>(Mutation.Type.LABEL_MUTATION, label::getText, label::setText));
            label.setText(text);
        }
    }

    private void removeLabel(GraphComponent element, Label label) {
        element.removeLabel(label);
        hoveredLabel = null;
        getCanvas().addHistory(new LabelDeletion(element, label));
    }

    @Override
    public CanvasUpdate mouseDragged(MouseEvent event) {
        getCanvas().setEventHandler(new SelectAreaHandler(getCanvas(), event.getPoint()));
        return CanvasUpdate.NO_OPERATION;
    }

    @Override
    public CanvasUpdate mouseReleased(MouseEvent event) {
        return CanvasUpdate.NO_OPERATION;
    }

    @Override
    public CanvasUpdate keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ALT && hoveredLabel != null || connectorPoint != null) {
            editButtonDown = true;
            return new CanvasUpdate(Cursor.TEXT_CURSOR, connectorPoint != null);
        }
        return CanvasUpdate.NO_OPERATION;
    }

    @Override
    public CanvasUpdate keyReleased(KeyEvent event) {
        if (Keyboard.getInstance().isDelete(event)) {
            return getCanvas().deleteSelection();
        }
        if (Keyboard.getInstance().isUndo(event)) {
            return getCanvas().historyUndo();
        }
        if (Keyboard.getInstance().isRedo(event)) {
            return getCanvas().historyRedo();
        }
        if (event.getKeyCode() == KeyEvent.VK_ALT) {
            editButtonDown = false;
            return new CanvasUpdate(Cursor.DEFAULT_CURSOR, connectorPoint != null);
        }
        return CanvasUpdate.NO_OPERATION;
    }

    private boolean setConnectorPoint(Point point) {
        if (Objects.equals(connectorPoint, point)) {
            return false;
        }
        connectorPoint = point;
        return true;
    }

    private boolean setEdgePoint(Point point) {
        if (Objects.equals(edgePoint, point)) {
            return false;
        }
        edgePoint = point;
        return true;
    }

    private boolean setHoveredLabel(Label label) {
        if (Objects.equals(hoveredLabel, label)) {
            return false;
        }
        hoveredLabel = label;
        return true;
    }

    private boolean setHighlightedElement(GraphComponent element) {
        if (Objects.equals(highlightedElement, element)) {
            return false;
        }
        highlightedElement = element;
        return true;
    }

    @Override
    public void paint(Graphics2D graphics) {
        if (edgePoint != null) {
            if (edgeBendSelected) {
                PaintUtil.paintEdgePoint(graphics, edgePoint);
            }
            else {
                PaintUtil.paintNewEdgePoint(graphics, edgePoint);
            }
        }
        if (connectorPoint != null && !editButtonDown) {
            PaintUtil.paintNewConnectorPoint(graphics, connectorPoint);
        }
        if (hoveredLabel != null) {
            graphics.setColor(Color.GRAY);
            Rectangle hoveredLabelBounds = hoveredLabel.getBounds();
            graphics.drawRect(hoveredLabelBounds.x, hoveredLabelBounds.y, hoveredLabelBounds.width, hoveredLabelBounds.height);
        }
        if (highlightedElement != null) {
            highlightedElement.paintHighlight(graphics, GraphCanvas.getLabelHighlightColor(), GraphCanvas.getHighlightStroke());
        }
    }

    private MouseButton button;
    private boolean editButtonDown;
    private Point connectorPoint;
    private Point edgePoint;
    private boolean edgeBendSelected;
    private Label hoveredLabel;
    private GraphComponent highlightedElement;

    private static final double HORIZONTAL_MARGIN = 0.4 * Math.PI;
    private static final double VERTICAL_MARGIN = 0.1 * Math.PI;

}
