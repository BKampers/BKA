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


public class DragLabelHandler implements GraphCanvas.MouseHandler {

    public DragLabelHandler(GraphCanvas canvas, Label label) {
        this.canvas = Objects.requireNonNull(canvas);
        this.label = Objects.requireNonNull(label);
        originalPositioner = label.getPositioner();
    }

    @Override
    public ComponentUpdate mouseDragged(MouseEvent event) {
        label.setPositioner(label.getVertex().distancePositioner(event.getPoint()));
        return ComponentUpdate.REPAINT;
    }

    @Override
    public ComponentUpdate mouseReleased(MouseEvent event) {
        canvas.resetMouseHandler();
        if (!Objects.equals(originalPositioner.get(), label.getPositioner().get())) {
            canvas.addHistory(new PositionerMutation());
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

    private final GraphCanvas canvas;
    private final Label label;
    private final Supplier<Point> originalPositioner;

}
