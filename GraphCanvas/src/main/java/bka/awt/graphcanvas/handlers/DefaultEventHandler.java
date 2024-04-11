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
    public ComponentUpdate mouseMoved(MouseEvent event) {
        Point cursor = event.getPoint();
        VertexRenderer nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex != null) {
            return handleVertexHovered(nearestVertex, event);
        }
        EdgeRenderer nearestEdge = getCanvas().findNearestEdge(cursor);
        if (nearestEdge != null && MouseButton.MAIN.matchesModifier(event)) {
            return handleEdgeHovered(nearestEdge, cursor);
        }
        boolean needRepaint = setEdgePoint(null);
        needRepaint |= setConnectorPoint(null);
        needRepaint |= setHoveredLabel(labelAt(cursor));
        Element nearestElement = getCanvas().findNearestElement(cursor);
        if (nearestElement != null && MouseButton.EDIT.matchesModifier(event)) {
            needRepaint |= setHighlightedElement(nearestElement);
            return new ComponentUpdate(Cursor.TEXT_CURSOR, needRepaint);
        }
        needRepaint |= setHighlightedElement(null);
        if (hoveredLabel != null) {
            if (MouseButton.EDIT.matchesModifier(event)) {
                return new ComponentUpdate(Cursor.TEXT_CURSOR, needRepaint);
            }
            return new ComponentUpdate(Cursor.HAND_CURSOR, needRepaint);
        }
        return new ComponentUpdate(Cursor.DEFAULT_CURSOR, needRepaint);
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

    private ComponentUpdate handleVertexHovered(VertexRenderer vertex, MouseEvent event) {
        boolean needRepaint = setHoveredLabel(null);
        needRepaint |= setEdgePoint(null);
        if (MouseButton.EDIT.matchesModifier(event)) {
            needRepaint |= setConnectorPoint(null);
            return new ComponentUpdate(Cursor.TEXT_CURSOR, needRepaint);
        }
        if (MouseButton.MAIN.matchesModifier(event)) {
            return handleVertexHovered(vertex, event.getPoint(), needRepaint);
        }
        return new ComponentUpdate(Cursor.DEFAULT_CURSOR, needRepaint);
    }

    private ComponentUpdate handleVertexHovered(VertexRenderer vertex, Point cursor, boolean needRepaint) {
        long distance = vertex.squareDistance(cursor);
        if (CanvasUtil.isInside(distance)) {
            needRepaint |= setConnectorPoint(null);
            return new ComponentUpdate(Cursor.HAND_CURSOR, needRepaint);
        }
        if (CanvasUtil.isOnBorder(distance)) {
            needRepaint |= setConnectorPoint(null);
            return new ComponentUpdate(getResizeCursorType(cursor, vertex.getLocation()), needRepaint);
        }
        if (CanvasUtil.isNear(distance) && getCanvas().getVertices().size() > 1) {
            needRepaint |= setConnectorPoint(cursor);
            return new ComponentUpdate(Cursor.DEFAULT_CURSOR, needRepaint);
        }
        return (needRepaint) ? ComponentUpdate.REPAINT : ComponentUpdate.NO_OPERATION;
    }

    private static int getResizeCursorType(Point cursor, Point vertexLocation) {
        int dx = cursor.x - vertexLocation.x;
        int dy = cursor.y - vertexLocation.y;
        double angle = Math.abs(Math.atan((double) dx / dy));
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

    private ComponentUpdate handleEdgeHovered(EdgeRenderer nearestEdge, Point cursor) {
        boolean needRepaint = setHoveredLabel(null);
        needRepaint |= setConnectorPoint(null);
        needRepaint |= setEdgePoint(cursor);
        edgeBendSelected = nearestEdge.getPoints().stream().anyMatch(point -> CanvasUtil.isNear(cursor, point));
        return new ComponentUpdate(Cursor.HAND_CURSOR, needRepaint);
    }

    @Override
    public ComponentUpdate mousePressed(MouseEvent event) {
        button = MouseButton.get(event);
        if (button != MouseButton.MAIN) {
            return ComponentUpdate.NO_OPERATION;
        }
        if (hoveredLabel != null) {
            getCanvas().setEventHandler(new DragLabelHandler(getCanvas(), hoveredLabel));
            return ComponentUpdate.NO_OPERATION;
        }
        Point cursor = event.getPoint();
        VertexRenderer nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex != null) {
            handleVertexPressed(cursor, nearestVertex);
        }
        else {
            EdgeRenderer nearestEdge = getCanvas().findNearestEdge(cursor);
            if (nearestEdge != null) {
                handleEdgePressed(cursor, nearestEdge);
            }
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private void handleVertexPressed(Point cursor, VertexRenderer vertex) {
        if (connectorPoint != null) {
            getCanvas().setEventHandler(new CreateEdgeHandler(getCanvas(), vertex));
        }
        else {
            long distance = vertex.squareDistance(cursor);
            if (CanvasUtil.isOnBorder(distance)) {
                getCanvas().setEventHandler(new VertexResizeHandler(getCanvas(), vertex));
            }
            else {
                if (!getCanvas().getSelection().contains(vertex)) {
                    getCanvas().selectSingleVertex(vertex);
                }
                getCanvas().setEventHandler(new SelectionMoveHandler(getCanvas(), cursor));
            }
        }
    }

    private void handleEdgePressed(Point cursor, EdgeRenderer nearestEdge) {
        EdgeRenderer.Excerpt originalShape = nearestEdge.getExcerpt();
        getCanvas().setEventHandler(new EdgePointMoveHandler(getCanvas(), dragPoint(nearestEdge, cursor), nearestEdge, originalShape));
    }

    private static Point dragPoint(EdgeRenderer edge, Point point) {
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

    @Override
    public ComponentUpdate mouseClicked(MouseEvent event) {
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
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate mainButtonClicked(Point cursor) {
        getCanvas().clearSelection();
        Element nearest = getCanvas().findNearestEdge(cursor);
        if (nearest == null) {
            nearest = getCanvas().findNearestVertex(cursor);
        }
        if (nearest != null) {
            getCanvas().select(nearest);
            return ComponentUpdate.REPAINT;
        }
        return addNewVertex(cursor);
    }

    private ComponentUpdate addNewVertex(Point cursor) {
        VertexRenderer vertex = getCanvas().getContext().createVertexRenderer(cursor);
        if (vertex == null) {
            return ComponentUpdate.NO_OPERATION;
        }
        getCanvas().addVertex(vertex);
        getCanvas().addHistory(new ElementInsertion(vertex, getCanvas()));
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate toggleSelection(Point cursor) {
        VertexRenderer nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex != null) {
            if (getCanvas().getSelection().contains(nearestVertex)) {
                getCanvas().removeSelected(nearestVertex);
            }
            else {
                getCanvas().select(nearestVertex);
            }
            return ComponentUpdate.REPAINT;
        }
        EdgeRenderer nearestEdge = getCanvas().findNearestEdge(cursor);
        if (nearestEdge != null) {
            getCanvas().toggleSelected(nearestEdge);
            return ComponentUpdate.REPAINT;
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate contextButtonClicked(MouseEvent event) {
        Point cursor = event.getPoint();
        EdgeRenderer nearestEdge = getCanvas().findNearestEdge(cursor);
        if (nearestEdge != null) {
            getCanvas().getContext().showEdgeMenu(nearestEdge, cursor);
            getCanvas().resetEventHandler();
            return ComponentUpdate.repaint(java.awt.Cursor.DEFAULT_CURSOR);
        }
        VertexRenderer nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex != null) {
            getCanvas().getContext().showVertexMenu(nearestVertex, cursor);
            getCanvas().resetEventHandler();
            return ComponentUpdate.repaint(java.awt.Cursor.DEFAULT_CURSOR);
        }
        getCanvas().clearSelection();
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate editLabel(Point cursor) {
        connectorPoint = null;
        Element element = hoveredLabel == null ? highlightedElement : hoveredLabel.getElement();
        if (element != null) {
            getCanvas().getContext().editString(
                labelText(hoveredLabel),
                cursor,
                applyLabelText(element, hoveredLabel, cursor));
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private String labelText(Label label) {
        return (label == null) ? "" : label.getText();
    }

    private Consumer<String> applyLabelText(Element element, Label label, Point position) {
        return input -> {
            if (!input.isBlank()) {
                if (label == null) {
                    addLabel(element, position, input);
                }
                else {
                    modifyLabel(label, input);
                }
                getCanvas().getContext().requestUpdate(ComponentUpdate.REPAINT);
            }
            else if (label != null) {
                removeLabel(element, label);
                getCanvas().getContext().requestUpdate(ComponentUpdate.REPAINT);
            }
            getCanvas().getContext().requestUpdate(ComponentUpdate.noOperation(Cursor.DEFAULT_CURSOR));
        };
    }

    private void addLabel(Element element, Point cursor, String text) {
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

    private void removeLabel(Element element, Label label) {
        element.removeLabel(label);
        hoveredLabel = null;
        getCanvas().addHistory(new LabelDeletion(element, label));
    }

    @Override
    public ComponentUpdate mouseDragged(MouseEvent event) {
        getCanvas().setEventHandler(new SelectAreaHandler(getCanvas(), event.getPoint()));
        return ComponentUpdate.NO_OPERATION;
    }

    @Override
    public ComponentUpdate mouseReleased(MouseEvent event) {
        return ComponentUpdate.NO_OPERATION;
    }

    @Override
    public ComponentUpdate keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ALT && hoveredLabel != null || connectorPoint != null) {
            editButtonDown = true;
            return new ComponentUpdate(Cursor.TEXT_CURSOR, connectorPoint != null);
        }
        return ComponentUpdate.NO_OPERATION;
    }

    @Override
    public ComponentUpdate keyReleased(KeyEvent event) {
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
            return new ComponentUpdate(Cursor.DEFAULT_CURSOR, connectorPoint != null);
        }
        return ComponentUpdate.NO_OPERATION;
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

    private boolean setHighlightedElement(Element element) {
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
    private Element highlightedElement;

    private static final double HORIZONTAL_MARGIN = 0.4 * Math.PI;
    private static final double VERTICAL_MARGIN = 0.1 * Math.PI;

}
