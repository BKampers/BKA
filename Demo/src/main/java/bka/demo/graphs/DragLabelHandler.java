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
        label.setPositioner(label.getElement().distancePositioner(event.getPoint()));
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

}
