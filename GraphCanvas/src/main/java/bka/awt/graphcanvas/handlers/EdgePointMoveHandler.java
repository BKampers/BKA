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


public class EdgePointMoveHandler extends CanvasEventHandler {

    public EdgePointMoveHandler(GraphCanvas canvas, Point dragStartPoint, EdgeComponent draggingEdgeRenderer, EdgeComponent.Excerpt originalShape) {
        super(canvas);
        this.dragPoint = Objects.requireNonNull(dragStartPoint);
        this.draggingEdgeRenderer = draggingEdgeRenderer;
        this.originalShape = originalShape;
        edgeBendSelected = originalShape.getPoints().size() == draggingEdgeRenderer.getPoints().size();
    }

    @Override
    public CanvasUpdate mouseDragged(MouseEvent event) {
        dragPoint.setLocation(event.getX(), event.getY());
        return CanvasUpdate.REPAINT;
    }

    @Override
    public CanvasUpdate mouseReleased(MouseEvent event) {
        CanvasUtil.cleanup(draggingEdgeRenderer);
        if (!originalShape.equals(draggingEdgeRenderer.getExcerpt())) {
            getCanvas().addHistory(new PropertyMutation<>(
                Mutation.Type.EDGE_TRANSFORMATION,
                () -> draggingEdgeRenderer.getExcerpt(),
                draggingEdgeRenderer::set,
                originalShape));
        }
        getCanvas().resetEventHandler();
        return CanvasUpdate.REPAINT;
    }

    @Override
    public void paint(Graphics2D graphics) {
        if (edgeBendSelected) {
            PaintUtil.paintEdgePoint(graphics, dragPoint);
        }
        else {
            PaintUtil.paintNewEdgePoint(graphics, dragPoint);
        }
    }

    private final boolean edgeBendSelected;
    private final Point dragPoint;
    private final EdgeComponent draggingEdgeRenderer;
    private final EdgeComponent.Excerpt originalShape;

}
