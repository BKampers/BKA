/*
** © Bart Kampers
*/
package bka.demo.graphs;

import bka.demo.graphs.history.*;
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
    public ComponentUpdate mouseDragged(MouseEvent event) {
        return moveSelection(event.getPoint());
    }

    @Override
    public ComponentUpdate mouseReleased(MouseEvent event) {
        if (dragStartPoint.equals(event.getPoint())) {
            getCanvas().setEventHandler(new DefaultEventHandler(getCanvas(), MouseButton.MAIN));
            return ComponentUpdate.REPAINT;
        }
        return finishSelectionMove(event.getPoint());
    }

    private ComponentUpdate moveSelection(Point cursor) {
        int deltaX = cursor.x - dragPoint.x;
        int deltaY = cursor.y - dragPoint.y;
        Point vector = new Point(deltaX, deltaY);
        Set<Element> selection = getCanvas().getSelection();
        selection.forEach(element -> element.move(vector));
        getCanvas().getEdges().stream()
            .filter(edge -> !selection.contains(edge))
            .filter(edge -> selection.contains(edge.getStart()))
            .filter(edge -> selection.contains(edge.getEnd()))
            .forEach(edge -> edge.move(vector));
        dragPoint = cursor;
        return ComponentUpdate.REPAINT;
    }

    private ComponentUpdate finishSelectionMove(Point cursor) {
        Set<Element> selection = getCanvas().getSelection();
        Map<EdgeRenderer, EdgeRenderer.Excerpt> affectedEdges = getCanvas().getEdges().stream()
            .filter(edge -> selection.contains(edge.getStart()) != selection.contains(edge.getEnd()))
            .collect(Collectors.toMap(
                Function.identity(),
                edge -> edge.getExcerpt()));
        cleanup(affectedEdges);
        getCanvas().addHistory(new ElementRelocation(selection, new Point(cursor.x - dragStartPoint.x, cursor.y - dragStartPoint.y), affectedEdges));
        getCanvas().resetEventHandler();
        return ComponentUpdate.REPAINT;
    }

    private void cleanup(Map<EdgeRenderer, EdgeRenderer.Excerpt> affectedEdges) {
        Iterator<EdgeRenderer> it = affectedEdges.keySet().iterator();
        while (it.hasNext()) {
            if (!CanvasUtil.cleanup(it.next())) {
                it.remove();
            }
        }
    }

    private final Point dragStartPoint;
    private Point dragPoint;

}
