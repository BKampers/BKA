/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.demo.graphs.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class EdgePointMoveHandler extends CanvasEventHandler {

    public EdgePointMoveHandler(GraphCanvas canvas, Point dragStartPoint, EdgeRenderer draggingEdgeRenderer, EdgeRenderer.Excerpt excerpt) {
        super(canvas);
        this.dragPoint = Objects.requireNonNull(dragStartPoint);
        this.draggingEdgeRenderer = draggingEdgeRenderer;
        this.excerpt = excerpt;
        edgeBendSelected = excerpt.getPoints().size() == draggingEdgeRenderer.getPoints().size();
    }

    @Override
    public ComponentUpdate mouseDragged(MouseEvent event) {
        dragPoint.setLocation(event.getX(), event.getY());
        return ComponentUpdate.REPAINT;
    }

    @Override
    public ComponentUpdate mouseReleased(MouseEvent event) {
        CanvasUtil.cleanup(draggingEdgeRenderer);
        if (!excerpt.equals(draggingEdgeRenderer.getExcerpt())) {
            getCanvas().addHistory(new PropertyMutation<>(
                Mutation.Type.EDGE_TRANSFORMATION,
                () -> draggingEdgeRenderer.getExcerpt(),
                draggingEdgeRenderer::set,
                excerpt));
        }
        getCanvas().resetEventHandler();
        return ComponentUpdate.REPAINT;
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
    private final EdgeRenderer draggingEdgeRenderer;
    private final EdgeRenderer.Excerpt excerpt;

}
