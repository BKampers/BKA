/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import bka.demo.graphs.Label;
import bka.demo.graphs.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;


public class GraphCanvas extends CompositeRenderer {

    public interface Context {

        void editString(String input, Point location, Consumer<String> onApply);

        void requestRepaint();
    }

    public GraphCanvas(Context context) {
        this.context = context;
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
            paintDot(graphics, edge.getStartConnectorPoint(), 3, Color.MAGENTA);
            paintDot(graphics, edge.getEndConnectorPoint(), 3, Color.MAGENTA);
        });
        graphics.setPaint(Color.BLUE);
        selection.forEach(renderer -> renderer.paint(graphics));
        mouseHandler.paint(graphics);
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
        history.addToHistory(new ElementDeletion(verticesToRemove, edgesToRemove, GraphCanvas.this));
        selection.clear();
        return ComponentUpdate.repaint(Cursor.DEFAULT_CURSOR);
    }

    private void selectSingleVertex(VertexRenderer vertex) {
        selection.clear();
        selection.add(vertex);
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

    private static List<Point> deepCopy(List<Point> list) {
        return list.stream().map(point -> new Point(point)).collect(Collectors.toList());
    }

    private static VertexRenderer dragEndPoint(Point location) {
        return new VertexRenderer(location);
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


    private interface MouseHandler {

        default void paint(Graphics2D graphics) {
        }

        default ComponentUpdate mouseMoved(MouseEvent event) {
            throw new IllegalStateException(getClass().getSimpleName() + ": " + event.paramString());
        }

        default ComponentUpdate mousePressed(MouseEvent event) {
            throw new IllegalStateException(getClass().getSimpleName() + ": " + event.paramString());
        }

        default ComponentUpdate mouseDragged(MouseEvent event) {
            throw new IllegalStateException(getClass().getSimpleName() + ": " + event.paramString());
        }

        default ComponentUpdate mouseReleased(MouseEvent event) {
            throw new IllegalStateException(getClass().getSimpleName() + ": " + event.paramString());
        }

        default ComponentUpdate mouseClicked(MouseEvent event) {
            throw new IllegalStateException(getClass().getSimpleName() + ": " + event.paramString());
        }
    }


    private class DefaultMouseHandler implements MouseHandler {

        public DefaultMouseHandler() {
            this(null);
        }

        public DefaultMouseHandler(Button button) {
            this.button = button;
        }

        @Override
        public ComponentUpdate mouseMoved(MouseEvent event) {
            boolean needRepaint = connectorPoint != null || edgePoint != null;
            connectorPoint = null;
            edgePoint = null;
            Point cursor = event.getPoint();
            Label labelAtCursor = labelAt(cursor);
            needRepaint |= Objects.equals(labelAtCursor, hoveredLabel);
            hoveredLabel = labelAtCursor;
            if (labelAtCursor != null) {
                return new ComponentUpdate(Cursor.HAND_CURSOR, needRepaint);
            }
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

        private Label labelAt(Point point) {
            return vertices.stream()
                .flatMap(vertex -> vertex.getLabels().stream().filter(boundsContain(point)))
                .findAny().orElse(null);
        }

        private Predicate<Label> boundsContain(Point point) {
            return label -> {
                Rectangle bounds = label.getBounds();
                return bounds != null && bounds.contains(point);
            };
        }

        private ComponentUpdate handleVertexHovered(VertexRenderer vertex, Point cursor, boolean needRepaint) {
            long distance = vertex.squareDistance(cursor);
            if (isInside(distance)) {
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
            edgePoint = cursor;
            return ComponentUpdate.repaint(Cursor.HAND_CURSOR);
        }

        @Override
        public ComponentUpdate mousePressed(MouseEvent event) {
            button = Button.get(event);
            Point cursor = event.getPoint();
            if (button == Button.MAIN) {
                VertexRenderer nearestVertex = findNearestVertex(cursor);
                if (nearestVertex != null) {
                    handleVertexPressed(cursor, nearestVertex);
                }
                else {
                    EdgeRenderer nearestEdge = findNearestEdge(cursor);
                    if (nearestEdge != null) {
                        handleEdgePressed(cursor, nearestEdge);
                    }
                }
            }
            return ComponentUpdate.NO_OPERATION;
        }

        private void handleVertexPressed(Point cursor, VertexRenderer vertex) {
            if (connectorPoint != null) {
                mouseHandler = new CreateEdgeMouseHandler(vertex);
            }
            else {
                long distance = vertex.squareDistance(cursor);
                if (isOnBorder(distance)) {
                    mouseHandler = new VertexResizeMouseHandler(vertex);
                }
                else {
                    if (!selection.contains(vertex)) {
                        selectSingleVertex(vertex);
                    }
                    mouseHandler = new SelectionMoveMouseHandler(cursor);
                }
            }
        }

        private void handleEdgePressed(Point cursor, EdgeRenderer nearestEdge) {
            List<Point> originalEdgePoints = deepCopy(nearestEdge.getPoints());
            mouseHandler = new EdgePointMoveMouseHandler(nearestEdgePoint(nearestEdge, cursor), nearestEdge, originalEdgePoints, edgeBendSelected);
        }

        @Override
        public ComponentUpdate mouseClicked(MouseEvent event) {
            if (button == Button.MAIN) {
                return mainButtonClicked(event.getPoint());
            }
            if (button == Button.TOGGLE_SELECT) {
                return toggleSelection(event.getPoint());
            }
            if (button == Button.RESET) {
                return clearSelection();
            }
            if (button == Button.EDIT) {
                return editLabel(event.getPoint());
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
                history.addToHistory(new ElementInsertion(vertex, GraphCanvas.this));
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
            return ComponentUpdate.REPAINT;
        }

        private ComponentUpdate editLabel(Point cursor) {
            connectorPoint = null;
            VertexRenderer vertex = findNearestVertex(cursor);
            if (vertex != null) {
                context.editString(labelText(hoveredLabel), cursor, applyLabelText(vertex, hoveredLabel, cursor));
            }
            return ComponentUpdate.NO_OPERATION;
        }

        private String labelText(Label label) {
            return (label == null) ? "" : label.getText();
        }

        private Consumer<String> applyLabelText(VertexRenderer vertex, Label label, Point position) {
            return text -> {
                if (!text.isBlank()) {
                    if (label == null) {
                        addLabel(vertex, position, text);
                    }
                    else {
                        modifyLabel(label, text);
                    }
                    context.requestRepaint();
                }
                else if (label != null) {
                    removeLabel(vertex, label);
                    context.requestRepaint();
                }
            };
        }

        private void addLabel(VertexRenderer vertex, Point cursor, String text) {
            Label newLabel = new Label(vertex.distancePositioner(cursor), text);
            vertex.addLabel(newLabel);
            history.addToHistory(new LabelInsertion(vertex, newLabel));
        }

        private void modifyLabel(Label label, String text) {
            String oldText = label.getText();
            label.setText(text);
            history.addToHistory(new LabelMutation(label, oldText));
        }

        private void removeLabel(VertexRenderer vertex, Label label) {
            vertex.removeLabel(label);
            hoveredLabel = null;
            history.addToHistory(new LabelDeletion(vertex, label));
        }

        @Override
        public ComponentUpdate mouseDragged(MouseEvent event) {
            mouseHandler = new SelectAreaMouseHandler(event.getPoint());
            return ComponentUpdate.NO_OPERATION;
        }

        @Override
        public ComponentUpdate mouseReleased(MouseEvent event) {
            return ComponentUpdate.NO_OPERATION;
        }

        @Override
        public void paint(Graphics2D graphics) {
            if (edgePoint != null) {
                graphics.setPaint(Color.RED);
                if (edgeBendSelected) {
                    paintDot(graphics, edgePoint, 3, Color.RED);
                }
                else {
                    paintCircle(graphics, edgePoint, 3, Color.RED);
                }
            }
            if (connectorPoint != null) {
                paintCircle(graphics, connectorPoint, 4, Color.RED);
            }
            if (hoveredLabel != null) {
                graphics.setColor(Color.GRAY);
                Rectangle hoveredLabelBounds = hoveredLabel.getBounds();
                graphics.drawRect(hoveredLabelBounds.x, hoveredLabelBounds.y, hoveredLabelBounds.width, hoveredLabelBounds.height);
            }
        }

        private Button button;
        private Point connectorPoint;
        private Point edgePoint;
        private boolean edgeBendSelected;
        private Label hoveredLabel;
    }


    private class CreateEdgeMouseHandler implements MouseHandler {

        public CreateEdgeMouseHandler(VertexRenderer vertex) {
            draggingEdgeRenderer = new EdgeRenderer(vertex);
        }

        @Override
        public ComponentUpdate mouseMoved(MouseEvent event) {
            Point cursor = event.getPoint();
            Point newConnectorPoint = null;
            VertexRenderer nearestVertex = findNearestVertex(cursor);
            if (nearestVertex != null) {
                newConnectorPoint = nearestVertex.getConnectorPoint(cursor);
            }
            if (Objects.equals(newConnectorPoint, connectorPoint)) {
                return ComponentUpdate.NO_OPERATION;
            }
            connectorPoint = newConnectorPoint;
            return ComponentUpdate.REPAINT;
        }

        @Override
        public ComponentUpdate mousePressed(MouseEvent event) {
            button = Button.get(event);
            return ComponentUpdate.NO_OPERATION;
        }

        @Override
        public ComponentUpdate mouseClicked(MouseEvent event) {
            if (button == Button.RESET) {
                mouseHandler = new DefaultMouseHandler();
                return ComponentUpdate.REPAINT;
            }
            button = null;
            return ComponentUpdate.NO_OPERATION;
        }

        @Override
        public ComponentUpdate mouseDragged(MouseEvent event) {
            Point cursor = event.getPoint();
            VertexRenderer nearestVertex = findNearestVertex(cursor);
            if (nearestVertex == null) {
                connectorPoint = null;
            }
            else if (!cursor.equals(nearestVertex.getLocation())) {
                connectorPoint = nearestVertex.getConnectorPoint(cursor);
            }
            draggingEdgeRenderer.setEnd(dragEndPoint(cursor));
            return ComponentUpdate.REPAINT;
        }

        @Override
        public ComponentUpdate mouseReleased(MouseEvent event) {
            Point cursor = event.getPoint();
            VertexRenderer end = findNearestVertex(cursor);
            if (end != null) {
                draggingEdgeRenderer.setEnd(end);
                cleanup(draggingEdgeRenderer);
                if (!end.equals(draggingEdgeRenderer.getStart()) || !draggingEdgeRenderer.getPoints().isEmpty()) {
                    edges.add(draggingEdgeRenderer);
                    history.addToHistory(new ElementInsertion(draggingEdgeRenderer, GraphCanvas.this));
                }
                mouseHandler = new DefaultMouseHandler();
            }
            else {
                draggingEdgeRenderer.addPoint(cursor);
                draggingEdgeRenderer.setEnd(dragEndPoint(cursor));
            }
            return ComponentUpdate.REPAINT;
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(Color.GRAY);
            draggingEdgeRenderer.paint(graphics);
            paintCircle(graphics, draggingEdgeRenderer.getStartConnectorPoint(), 3, Color.MAGENTA);
            if (connectorPoint != null) {
                paintCircle(graphics, connectorPoint, 3, Color.MAGENTA);
            }
        }

        private final EdgeRenderer draggingEdgeRenderer;
        private Point connectorPoint;
        private Button button;

    }


    private class SelectionMoveMouseHandler implements MouseHandler {

        public SelectionMoveMouseHandler(Point dragStartPoint) {
            this.dragStartPoint = dragStartPoint;
            this.dragPoint = dragStartPoint;
        }

        @Override
        public ComponentUpdate mouseDragged(MouseEvent event) {
            return moveSelection(event.getPoint());
        }

        @Override
        public ComponentUpdate mouseReleased(MouseEvent event) {
            if (dragStartPoint.equals(event.getPoint())) {
                mouseHandler = new DefaultMouseHandler(Button.MAIN);
                return ComponentUpdate.REPAINT;
            }
            return finishSelectionMove(event.getPoint());
        }

        private ComponentUpdate moveSelection(Point cursor) {
            int deltaX = cursor.x - dragPoint.x;
            int deltaY = cursor.y - dragPoint.y;
            Point vector = new Point(deltaX, deltaY);
            selection.forEach(element -> element.move(vector));
            edges.stream()
                .filter(edge -> !selection.contains(edge))
                .filter(edge -> selection.contains(edge.getStart()))
                .filter(edge -> selection.contains(edge.getEnd()))
                .forEach(edge -> edge.move(vector));
            dragPoint = cursor;
            return ComponentUpdate.REPAINT;
        }

        private ComponentUpdate finishSelectionMove(Point cursor) {
            Map<EdgeRenderer, List<Point>> affectedEdges = edges.stream()
                .filter(edge -> selection.contains(edge.getStart()) != selection.contains(edge.getEnd()))
                .collect(Collectors.toMap(
                    edge -> edge,
                    edge -> deepCopy(edge.getPoints())));
            keepCleanedEdges(affectedEdges);
            history.addToHistory(new ElementRelocation(selection, new Point(cursor.x - dragStartPoint.x, cursor.y - dragStartPoint.y), affectedEdges));
            mouseHandler = new DefaultMouseHandler();
            return ComponentUpdate.REPAINT;
        }

        private void keepCleanedEdges(Map<EdgeRenderer, List<Point>> affectedEdges) {
            Iterator<EdgeRenderer> it = affectedEdges.keySet().iterator();
            while (it.hasNext()) {
                if (!cleanup(it.next())) {
                    it.remove();
                }
            }
        }

        private final Point dragStartPoint;
        private Point dragPoint;
    }


    private class EdgePointMoveMouseHandler implements MouseHandler {

        public EdgePointMoveMouseHandler(Point dragStartPoint, EdgeRenderer draggingEdgeRenderer, List<Point> originalEdgePoints, boolean edgeBendSelected) {
            this.dragPoint = dragStartPoint;
            this.draggingEdgeRenderer = draggingEdgeRenderer;
            this.originalEdgePoints = originalEdgePoints;
            this.edgeBendSelected = edgeBendSelected;
        }

        @Override
        public ComponentUpdate mouseDragged(MouseEvent event) {
            dragPoint.setLocation(event.getX(), event.getY());
            return ComponentUpdate.REPAINT;
        }

        @Override
        public ComponentUpdate mouseReleased(MouseEvent event) {
            cleanup(draggingEdgeRenderer);
            if (!originalEdgePoints.equals(draggingEdgeRenderer.getPoints())) {
                history.addToHistory(new EdgeTransformation(draggingEdgeRenderer, originalEdgePoints));
            }
            mouseHandler = new DefaultMouseHandler();
            return ComponentUpdate.REPAINT;
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(Color.RED);
            if (edgeBendSelected) {
                paintDot(graphics, dragPoint, 3, Color.RED);
            }
            else {
                paintCircle(graphics, dragPoint, 3, Color.RED);
            }
        }

        private final boolean edgeBendSelected;
        private final Point dragPoint;
        private final EdgeRenderer draggingEdgeRenderer;
        private final List<Point> originalEdgePoints;
    }


    private class VertexResizeMouseHandler implements MouseHandler {

        public VertexResizeMouseHandler(VertexRenderer draggingVertex) {
            this.draggingVertex = draggingVertex;
            this.originalDimension = draggingVertex.getDimension();
        }

        @Override
        public ComponentUpdate mouseDragged(MouseEvent event) {
            draggingVertex.resize(event.getPoint());
            return ComponentUpdate.REPAINT;
        }

        @Override
        public ComponentUpdate mouseReleased(MouseEvent event) {
            if (!originalDimension.equals(draggingVertex.getDimension())) {
                edges.stream()
                    .filter(edge -> draggingVertex.equals(edge.getStart()) || draggingVertex.equals(edge.getEnd()))
                    .forEach(edge -> cleanup(edge));
                history.addToHistory(new SizeChange(draggingVertex, originalDimension));
            }
            mouseHandler = new DefaultMouseHandler();
            return ComponentUpdate.NO_OPERATION;
        }

        private final VertexRenderer draggingVertex;
        private final Dimension originalDimension;
    }


    private class SelectAreaMouseHandler implements MouseHandler {

        public SelectAreaMouseHandler(Point dragStartPoint) {
            this.dragStartPoint = dragStartPoint;
            selectionRectangle = new Rectangle(dragStartPoint.x, dragStartPoint.y, 0, 0);
        }

        @Override
        public ComponentUpdate mouseDragged(MouseEvent event) {
            selectionRectangle = new Rectangle(dragStartPoint.x, dragStartPoint.y, event.getX() - dragStartPoint.x, event.getY() - dragStartPoint.y);
            if (selectionRectangle.width < 0) {
                selectionRectangle.x += selectionRectangle.width;
                selectionRectangle.width = -selectionRectangle.width;
            }
            if (selectionRectangle.height < 0) {
                selectionRectangle.y += selectionRectangle.height;
                selectionRectangle.height = -selectionRectangle.height;
            }
            return ComponentUpdate.REPAINT;
        }

        @Override
        public ComponentUpdate mouseReleased(MouseEvent event) {
            selection.clear();
            vertices.stream()
                .filter(vertex -> selectionRectangle.contains(vertex.getLocation()))
                .forEach(vertex -> selection.add(vertex));
            edges.stream()
                .filter(edge -> selection.contains(edge.getStart()))
                .filter(edge -> selection.contains(edge.getEnd()))
                .forEach(edge -> selection.add(edge));
            mouseHandler = new DefaultMouseHandler();
            return ComponentUpdate.REPAINT;
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.drawRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width, selectionRectangle.height);
        }

        private final Point dragStartPoint;
        private Rectangle selectionRectangle;
    }


    private enum Button {
        MAIN(MouseEvent.BUTTON1),
        TOGGLE_SELECT(MouseEvent.BUTTON1, Keyboard.getInstance().getToggleSelectionModifiers()),
        RESET(MouseEvent.BUTTON3),
        EDIT(MouseEvent.BUTTON1, MouseEvent.ALT_DOWN_MASK),
        UNSUPPORTED(0, 0, 0);

        private Button(int buttonId, int clickCount, int modifiers) {
            this.buttonId = buttonId;
            this.clickCount = clickCount;
            this.modifiers = modifiers;
        }

        private Button(int buttonId, int modifiers) {
            this(buttonId, 1, modifiers);
        }

        private Button(int buttonId) {
            this(buttonId, 1, 0);
        }

        public static Button get(MouseEvent event) {
            return Arrays.asList(values()).stream()
                .filter(button -> event.getButton() == button.buttonId)
                .filter(button -> event.getClickCount() == button.clickCount)
                .filter(button -> (event.getModifiersEx() & MASK) == button.modifiers)
                .findAny()
                .orElse(UNSUPPORTED);
        }

        private final int buttonId;
        private final int clickCount;
        private final int modifiers;

        private static final int MASK = InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
    }

    private final Context context;

    private final Collection<VertexRenderer> vertices = new LinkedList<>();
    private final Collection<EdgeRenderer> edges = new LinkedList<>();
    private final Set<Element> selection = new HashSet<>();

    private final DrawHistory history = new DrawHistory();

    private MouseHandler mouseHandler = new DefaultMouseHandler();

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
