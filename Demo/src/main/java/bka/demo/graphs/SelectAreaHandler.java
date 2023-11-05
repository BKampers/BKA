/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class SelectAreaHandler extends CanvasEventHandler {

    public SelectAreaHandler(GraphCanvas canvas, Point dragStartPoint) {
        super(canvas);
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
        getCanvas().clearSelection();
        getCanvas().getVertices().stream()
            .filter(vertex -> selectionRectangle.contains(vertex.getLocation()))
            .forEach(vertex -> getCanvas().select(vertex));
        Set<Element> selection = getCanvas().getSelection();
        getCanvas().getEdges().stream()
            .filter(edge -> selection.contains(edge.getStart()))
            .filter(edge -> selection.contains(edge.getEnd()))
            .forEach(edge -> getCanvas().select(edge));
        getCanvas().resetEventHandler();
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
