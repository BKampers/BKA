/*
** © Bart Kampers
*/
package bka.demo.graphs;

import bka.demo.graphs.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class CreateEdgeHandler extends CanvasEventHandler {

    public CreateEdgeHandler(GraphCanvas canvas, VertexRenderer vertex) {
        super(canvas);
        draggingEdgeRenderer = new EdgeRenderer(vertex);
    }

    @Override
    public ComponentUpdate mouseMoved(MouseEvent event) {
        if (!MouseButton.MAIN.matchesModifier(event)) {
            return ComponentUpdate.NO_OPERATION;
        }
        Point cursor = event.getPoint();
        Point newConnectorPoint = null;
        VertexRenderer nearestVertex = getCanvas().findNearestVertex(cursor);
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
        button = MouseButton.get(event);
        return ComponentUpdate.NO_OPERATION;
    }

    @Override
    public ComponentUpdate mouseClicked(MouseEvent event) {
        if (button == MouseButton.RESET) {
            getCanvas().resetEventHandler();
            return ComponentUpdate.REPAINT;
        }
        button = null;
        return ComponentUpdate.NO_OPERATION;
    }

    @Override
    public ComponentUpdate mouseDragged(MouseEvent event) {
        if (!MouseButton.MAIN.matchesModifier(event)) {
            return ComponentUpdate.NO_OPERATION;
        }
        Point cursor = event.getPoint();
        VertexRenderer nearestVertex = getCanvas().findNearestVertex(cursor);
        if (nearestVertex == null) {
            connectorPoint = null;
        }
        else if (!cursor.equals(nearestVertex.getLocation())) {
            connectorPoint = nearestVertex.getConnectorPoint(cursor);
        }
        draggingEdgeRenderer.setEnd(new VertexRenderer(cursor));
        return ComponentUpdate.REPAINT;
    }

    @Override
    public ComponentUpdate mouseReleased(MouseEvent event) {
        if (!MouseButton.MAIN.matchesModifier(event)) {
            return ComponentUpdate.NO_OPERATION;
        }
        Point cursor = event.getPoint();
        VertexRenderer end = getCanvas().findNearestVertex(cursor);
        if (end != null) {
            draggingEdgeRenderer.setEnd(end);
            CanvasUtil.cleanup(draggingEdgeRenderer);
            if (!end.equals(draggingEdgeRenderer.getStart()) || !draggingEdgeRenderer.getPoints().isEmpty()) {
                getCanvas().addEdge(draggingEdgeRenderer);
                getCanvas().addHistory(new ElementInsertion(draggingEdgeRenderer, getCanvas()));
            }
            getCanvas().resetEventHandler();
        }
        else {
            draggingEdgeRenderer.addPoint(cursor);
            draggingEdgeRenderer.setEnd(new VertexRenderer(cursor));
        }
        return ComponentUpdate.REPAINT;
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

    private final EdgeRenderer draggingEdgeRenderer;
    private Point connectorPoint;
    private MouseButton button;

}
