/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.demo.graphs.Label;
import bka.demo.graphs.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;


public class DragLabelHandler extends CanvasEventHandler {

    public DragLabelHandler(GraphCanvas canvas, Label label) {
        super(canvas);
        this.label = Objects.requireNonNull(label);
        originalPositioner = label.getPositioner();
    }

    @Override
    public ComponentUpdate mouseDragged(MouseEvent event) {
        Point cursor = event.getPoint();
        label.setPositioner(label.getElement().distancePositioner(cursor));
        nearestElement = getCanvas().findNearestElement(cursor);
        nearestIndex = (nearestElement instanceof EdgeRenderer) ? ((EdgeRenderer) nearestElement).nearestLineIndex(cursor) : -1;
        return ComponentUpdate.REPAINT;
    }

    @Override
    public ComponentUpdate mouseReleased(MouseEvent event) {
        getCanvas().resetEventHandler();
        if (!Objects.equals(originalPositioner.get(), label.getPositioner().get())) {
            getCanvas().addHistory(new PositionerMutation());
        }
        return ComponentUpdate.REPAINT;
    }

    private Label getLabel() {
        return label;
    }

    private Supplier<Point> getOriginalPositioner() {
        return originalPositioner;
    }

    @Override
    public void paint(Graphics2D graphics) {
        if (nearestElement != null) {
            if (nearestIndex >= 0) {
                graphics.setPaint(new Color(Color.YELLOW.getRGB() | 0x7F000000, true));
                graphics.setStroke(new BasicStroke(2));
                Point p1 = ((EdgeRenderer) nearestElement).getPoint(nearestIndex);
                Point p2 = ((EdgeRenderer) nearestElement).getPoint(nearestIndex + 1);
                graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            else {
                nearestElement.paintHighlight(graphics, new Color(Color.YELLOW.getRGB() | 0x7F000000, true), new BasicStroke(2));
            }
        }
    }

    private class PositionerMutation extends PropertyMutation<Supplier<Point>> {

        public PositionerMutation() {
            super(Mutation.Type.RELOCATION, getLabel()::getPositioner, getLabel()::setPositioner, getOriginalPositioner());
        }

        @Override
        public String getBundleKey() {
            return "Label" + getType().getBundleKey();
        }
    }

    private final Label label;
    private final Supplier<Point> originalPositioner;
    private Element nearestElement;
    private int nearestIndex = -1;

}
