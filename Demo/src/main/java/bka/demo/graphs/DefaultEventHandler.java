/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import bka.demo.graphs.Label;
import bka.demo.graphs.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;


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
        if (nearestEdge != null && MouseButton.MAIN.modifierMatch(event)) {
            return handleEdgeHovered(nearestEdge, cursor);
        }
        boolean needRepaint = setEdgePoint(null);
        needRepaint |= setConnectorPoint(null);
        needRepaint |= setHoveredLabel(labelAt(cursor));
        if (hoveredLabel != null) {
            if (MouseButton.EDIT.modifierMatch(event)) {
                return new ComponentUpdate(Cursor.TEXT_CURSOR, needRepaint);
            }
            return new ComponentUpdate(Cursor.HAND_CURSOR, needRepaint);
        }
        return new ComponentUpdate(Cursor.DEFAULT_CURSOR, needRepaint);
    }

    private Label labelAt(Point point) {
        return getCanvas().getVertices().stream()
            .flatMap(vertex -> vertex.getLabels().stream().filter(boundsContain(point)))
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
        if (MouseButton.EDIT.modifierMatch(event)) {
            needRepaint |= setConnectorPoint(null);
            return new ComponentUpdate(Cursor.TEXT_CURSOR, needRepaint);
        }
        if (MouseButton.MAIN.modifierMatch(event)) {
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
        needRepaint |= setEdgePoint(cursor);
        edgeBendSelected = nearestEdge.getPoints().stream().anyMatch(point -> CanvasUtil.isNear(cursor, point));
        return new ComponentUpdate(Cursor.HAND_CURSOR, needRepaint);
    }

    @Override
    public ComponentUpdate mousePressed(MouseEvent event) {
        button = MouseButton.get(event);
        if (button == MouseButton.MAIN) {
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
        java.util.List<Point> originalEdgePoints = CanvasUtil.deepCopy(nearestEdge.getPoints());
        getCanvas().setEventHandler(new EdgePointMoveHandler(getCanvas(), getCanvas().nearestEdgePoint(nearestEdge, cursor), nearestEdge, originalEdgePoints));
    }

    @Override
    public ComponentUpdate mouseClicked(MouseEvent event) {
        if (button == MouseButton.MAIN) {
            return mainButtonClicked(event.getPoint());
        }
        if (button == MouseButton.TOGGLE_SELECT) {
            return toggleSelection(event.getPoint());
        }
        if (button == MouseButton.RESET) {
            return clearSelection();
        }
        if (button == MouseButton.EDIT) {
            return editLabel(event.getPoint());
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private ComponentUpdate mainButtonClicked(Point cursor) {
        getCanvas().clearSelection();
        EdgeRenderer nearestEdge = getCanvas().findNearestEdge(cursor);
        if (nearestEdge != null) {
            getCanvas().select(nearestEdge);
            return ComponentUpdate.REPAINT;
        }
        VertexRenderer nearest = getCanvas().findNearestVertex(cursor);
        if (nearest == null) {
            VertexRenderer vertex = new VertexRenderer(cursor);
            getCanvas().addVertex(vertex);
            getCanvas().addHistory(new ElementInsertion(vertex, getCanvas()));
        }
        else {
            getCanvas().select(nearest);
        }
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

    private ComponentUpdate clearSelection() {
        getCanvas().clearSelection();
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate editLabel(Point cursor) {
        connectorPoint = null;
        VertexRenderer vertex = hoveredLabel == null ? getCanvas().findNearestVertex(cursor) : hoveredLabel.getVertex();
        if (vertex != null) {
            getCanvas().getContext().editString(
                labelText(hoveredLabel),
                cursor,
                applyLabelText(vertex, hoveredLabel, cursor));
        }
        return ComponentUpdate.NO_OPERATION;
    }

    private String labelText(Label label) {
        return (label == null) ? "" : label.getText();
    }

    private Consumer<String> applyLabelText(VertexRenderer vertex, Label label, Point position) {
        return input -> {
            if (!input.isBlank()) {
                if (label == null) {
                    addLabel(vertex, position, input);
                }
                else {
                    modifyLabel(label, input);
                }
                getCanvas().getContext().requestUpdate(ComponentUpdate.REPAINT);
            }
            else if (label != null) {
                removeLabel(vertex, label);
                getCanvas().getContext().requestUpdate(ComponentUpdate.REPAINT);
            }
            getCanvas().getContext().requestUpdate(ComponentUpdate.noOperation(Cursor.DEFAULT_CURSOR));
        };
    }

    private void addLabel(VertexRenderer vertex, Point cursor, String text) {
        Label newLabel = new Label(vertex, vertex.distancePositioner(cursor), text);
        vertex.addLabel(newLabel);
        getCanvas().addHistory(new LabelInsertion(vertex, newLabel));
    }

    private void modifyLabel(Label label, String text) {
        if (!text.equals(label.getText())) {
            getCanvas().addHistory(new PropertyMutation<>(Mutation.Type.LABEL_MUTATION, label::getText, label::setText));
            label.setText(text);
        }
    }

    private void removeLabel(VertexRenderer vertex, Label label) {
        vertex.removeLabel(label);
        hoveredLabel = null;
        getCanvas().addHistory(new LabelDeletion(vertex, label));
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
    }

    private MouseButton button;
    private boolean editButtonDown;
    private Point connectorPoint;
    private Point edgePoint;
    private boolean edgeBendSelected;
    private Label hoveredLabel;

    private static final double HORIZONTAL_MARGIN = 0.4 * Math.PI;
    private static final double VERTICAL_MARGIN = 0.1 * Math.PI;

}
