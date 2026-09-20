/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas.handlers;

import bka.awt.graphcanvas.*;
import bka.awt.graphcanvas.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;


public final class CreateEdgeHandler extends CanvasEventHandler {

    public CreateEdgeHandler(GraphCanvas canvas, VertexComponent vertex, Point cursor) {
        super(canvas);
        draggingEdgeRenderer = getCanvas().getContext().createEdgeComponent(vertex, new VertexComponent(VERTEX_PLACEHOLDER, cursor)).get();
    }

    @Override
    public CanvasUpdate mouseMoved(MouseEvent event) {
        if (!MouseButton.MAIN.matchesModifier(event)) {
            return CanvasUpdate.NO_OPERATION;
        }
        Point cursor = event.getPoint();
        Point newConnectorPoint = null;
        Optional<VertexComponent> nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex.isPresent()) {
            newConnectorPoint = nearestVertex.get().getConnectorPoint(cursor);
        }
        if (Objects.equals(newConnectorPoint, connectorPoint)) {
            return CanvasUpdate.NO_OPERATION;
        }
        connectorPoint = newConnectorPoint;
        return CanvasUpdate.REPAINT;
    }

    @Override
    public CanvasUpdate mousePressed(MouseEvent event) {
        button = MouseButton.get(event);
        return CanvasUpdate.NO_OPERATION;
    }

    @Override
    public CanvasUpdate mouseClicked(MouseEvent event) {
        if (button == MouseButton.CONTEXT) {
            getCanvas().resetEventHandler();
            return CanvasUpdate.REPAINT;
        }
        button = null;
        return CanvasUpdate.NO_OPERATION;
    }

    @Override
    public CanvasUpdate mouseDragged(MouseEvent event) {
        if (!MouseButton.MAIN.matchesModifier(event)) {
            return CanvasUpdate.NO_OPERATION;
        }
        Point cursor = event.getPoint();
        Optional<VertexComponent> nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex.isEmpty()) {
            connectorPoint = null;
        }
        else if (!cursor.equals(nearestVertex.get().getLocation())) {
            connectorPoint = nearestVertex.get().getConnectorPoint(cursor);
        }
        draggingEdgeRenderer.getEnd().setLocation(cursor);
        return CanvasUpdate.REPAINT;
    }

    @Override
    public CanvasUpdate mouseReleased(MouseEvent event) {
        if (!MouseButton.MAIN.matchesModifier(event)) {
            return CanvasUpdate.NO_OPERATION;
        }
        Point cursor = event.getPoint();
        Optional<VertexComponent> end = getCanvas().findNearestVertex(cursor);
        if (end.isPresent()) {
            draggingEdgeRenderer.setEnd(end.get());
            CanvasUtil.cleanup(draggingEdgeRenderer);
            if (!end.equals(draggingEdgeRenderer.getStart()) || !draggingEdgeRenderer.getPoints().isEmpty()) {
                getCanvas().addEdge(draggingEdgeRenderer);
                getCanvas().addHistory(new ElementInsertion(draggingEdgeRenderer, getCanvas()));
            }
            getCanvas().resetEventHandler();
        }
        else {
            draggingEdgeRenderer.addPoint(cursor);
            draggingEdgeRenderer.getEnd().setLocation(cursor);
        }
        return CanvasUpdate.REPAINT;
    }

    @Override
    public void paint(Graphics2D graphics) {
        graphics.setPaint(Color.GRAY);
        draggingEdgeRenderer.paint(graphics);
        PaintUtil.paintNewConnectorPoint(graphics, draggingEdgeRenderer.getStartConnectorPoint());
        if (connectorPoint != null) {
            PaintUtil.paintNewConnectorPoint(graphics, connectorPoint);
        }
    }

    private static final VertexPaintable VERTEX_PLACEHOLDER = new VertexPaintable(new Dimension()) {
        @Override
        public Point getConnectorPoint(Point location, Point point) {
            return location;
        }

        @Override
        public void paint(Graphics2D graphics) {
            throw new IllegalStateException();
        }

        @Override
        public void paint(Graphics2D graphics, Paint paint, Stroke stroke) {
            throw new IllegalStateException();
        }

        @Override
        public void resize(Point location, Point target, ResizeDirection direction) {
            throw new IllegalStateException();
        }

        @Override
        public Supplier<Point> distancePositioner(Point location, Point point) {
            throw new IllegalStateException();
        }
    };

    private final EdgeComponent draggingEdgeRenderer;
    private Point connectorPoint;
    private MouseButton button;

}
