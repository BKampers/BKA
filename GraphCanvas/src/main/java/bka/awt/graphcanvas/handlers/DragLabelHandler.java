/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
 */
package bka.awt.graphcanvas.handlers;

import bka.awt.graphcanvas.*;
import bka.awt.graphcanvas.Label;
import bka.awt.graphcanvas.history.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;


public class DragLabelHandler extends CanvasEventHandler {

    public DragLabelHandler(GraphCanvas canvas, Label label) {
        super(canvas);
        this.label = Objects.requireNonNull(label);
        this.originalElement = label.getElement();
        nearestElement = originalElement;
        originalPositioner = label.getPositioner();
    }

    @Override
    public ComponentUpdate mouseDragged(MouseEvent event) {
        Point cursor = event.getPoint();
        label.setPositioner(label.getElement().distancePositioner(cursor));
        Element nearest = getCanvas().findNearestElement(cursor);
        if (!nearest.equals(nearestElement)) {
            nearestElement = nearest;
        }
        nearestIndex = (nearestElement instanceof EdgeRenderer) ? ((EdgeRenderer) nearestElement).nearestLineIndex(cursor) : -1;
        return ComponentUpdate.REPAINT;
    }

    @Override
    public ComponentUpdate mouseReleased(MouseEvent event) {
        getCanvas().resetEventHandler();
        if (!Objects.equals(originalPositioner.get(), label.getPositioner().get())) {
            if (label.getElement().equals(nearestElement)) {
                getCanvas().addHistory(new PositionerMutation());
            }
            else {
                label.moveTo(nearestElement, nearestElement.distancePositioner(event.getPoint()));
                getCanvas().addHistory(new LabelReallocationMutation());
            }
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
                ((EdgeRenderer) nearestElement).paintHighlight(graphics, GraphCanvas.getLabelHighlightColor(), GraphCanvas.getHighlightStroke(), nearestIndex);
            }
            else {
                nearestElement.paintHighlight(graphics, GraphCanvas.getLabelHighlightColor(), GraphCanvas.getHighlightStroke());
            }
        }
    }


    private class PositionerMutation extends PropertyMutation<Supplier<Point>> {

        public PositionerMutation() {
            super(Mutation.Type.RELOCATION, getLabel()::getPositioner, getLabel()::setPositioner, getOriginalPositioner());
        }

        @Override
        public String getBundleKey() {
            return "Label" + super.getBundleKey();
        }
    }


    private class LabelReallocationMutation extends Mutation.Symmetrical {

        public LabelReallocationMutation() {
            this.reallocatedLabel = label;
            historyElement = originalElement;
            historyPositioner = originalPositioner;
        }

        @Override
        public void revert() {
            Element oldElement = reallocatedLabel.getElement();
            Supplier<Point> oldPositioner = reallocatedLabel.getPositioner();
            reallocatedLabel.moveTo(historyElement, historyElement.distancePositioner(historyPositioner.get()));
            historyElement = oldElement;
            historyPositioner = oldPositioner;
        }

        @Override
        public Mutation.Type getType() {
            return Mutation.Type.RELOCATION;
        }

        @Override
        public String getBundleKey() {
            return "Label" + getType().getBundleKey();
        }

        private final Label reallocatedLabel;
        private Element historyElement;
        private Supplier<Point> historyPositioner;
    }


    private final Label label;
    private final Supplier<Point> originalPositioner;
    private final Element originalElement;

    private Element nearestElement;
    private int nearestIndex = -1;

}
