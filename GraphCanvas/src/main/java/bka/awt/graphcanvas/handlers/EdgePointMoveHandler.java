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


public final class EdgePointMoveHandler extends CanvasEventHandler {

    /**
     * Handles moving of a point on an edge. The coordinates of given {@code dragPoint} are updated while the mouse is dragged. Pass the live
     * {@link Point} that belongs to {@code draggingEdgeRenderer} (an existing bend, or a newly inserted one).
     *
     * @param canvas canvas that owns this handler
     * @param dragPoint live edge point that will be moved
     * @param draggingEdgeRenderer edge whose geometry is being edited
     * @param originalShape excerpt of the edge before this drag, used for undo
     */
    public EdgePointMoveHandler(GraphCanvas canvas, Point dragPoint, EdgeComponent draggingEdgeRenderer, EdgeComponent.Excerpt originalShape) {
        super(canvas);
        this.dragPoint = Objects.requireNonNull(dragPoint);
        this.draggingEdgeRenderer = draggingEdgeRenderer;
        this.originalShape = originalShape;
        renderer = (originalShape.getPoints().size() == draggingEdgeRenderer.getPoints().size())
            ? PaintUtil::paintEdgePoint
            : PaintUtil::paintNewEdgePoint;
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
                draggingEdgeRenderer::getExcerpt,
                draggingEdgeRenderer::applyExcerpt,
                originalShape));
        }
        if (dragPoint.equals(event.getPoint())) {
            getCanvas().setEventHandler(DefaultEventHandler.create(getCanvas(), MouseButton.get(event)));
        }
        else {
            getCanvas().resetEventHandler();
        }
        return CanvasUpdate.REPAINT;
    }

    @Override
    public void paint(Graphics2D graphics) {
        renderer.accept(graphics, dragPoint);
    }

    private final Point dragPoint;
    private final EdgeComponent draggingEdgeRenderer;
    private final EdgeComponent.Excerpt originalShape;
    private final BiConsumer<Graphics2D, Point> renderer;

}
