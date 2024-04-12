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
import java.util.stream.*;


public class SelectionMoveHandler extends CanvasEventHandler {

    public SelectionMoveHandler(GraphCanvas canvas, Point dragStartPoint) {
        super(canvas);
        this.dragStartPoint = Objects.requireNonNull(dragStartPoint);
        this.dragPoint = dragStartPoint;
    }

    @Override
    public CanvasUpdate mouseDragged(MouseEvent event) {
        return moveSelection(event.getPoint());
    }

    @Override
    public CanvasUpdate mouseReleased(MouseEvent event) {
        if (dragStartPoint.equals(event.getPoint())) {
            getCanvas().setEventHandler(new DefaultEventHandler(getCanvas(), MouseButton.MAIN));
            return CanvasUpdate.REPAINT;
        }
        return finishSelectionMove(event.getPoint());
    }

    private CanvasUpdate moveSelection(Point cursor) {
        int deltaX = cursor.x - dragPoint.x;
        int deltaY = cursor.y - dragPoint.y;
        Point vector = new Point(deltaX, deltaY);
        Set<GraphComponent> selection = getCanvas().getSelection();
        selection.forEach(element -> element.move(vector));
        getCanvas().getEdges().stream()
            .filter(edge -> !selection.contains(edge))
            .filter(edge -> selection.contains(edge.getStart()))
            .filter(edge -> selection.contains(edge.getEnd()))
            .forEach(edge -> edge.move(vector));
        dragPoint = cursor;
        return CanvasUpdate.REPAINT;
    }

    private CanvasUpdate finishSelectionMove(Point cursor) {
        Set<GraphComponent> selection = getCanvas().getSelection();
        Map<EdgeComponent, EdgeComponent.Excerpt> affectedEdges = getCanvas().getEdges().stream()
            .filter(edge -> selection.contains(edge.getStart()) != selection.contains(edge.getEnd()))
            .collect(Collectors.toMap(
                Function.identity(),
                edge -> edge.getExcerpt()));
        cleanup(affectedEdges);
        getCanvas().addHistory(new ElementRelocation(selection, new Point(cursor.x - dragStartPoint.x, cursor.y - dragStartPoint.y), affectedEdges));
        getCanvas().resetEventHandler();
        return CanvasUpdate.REPAINT;
    }

    private void cleanup(Map<EdgeComponent, EdgeComponent.Excerpt> affectedEdges) {
        Iterator<EdgeComponent> it = affectedEdges.keySet().iterator();
        while (it.hasNext()) {
            if (!CanvasUtil.cleanup(it.next())) {
                it.remove();
            }
        }
    }

    private final Point dragStartPoint;
    private Point dragPoint;

}
