/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas.history;

import bka.awt.graphcanvas.*;
import java.util.*;


public class LabelInsertion implements Mutation {

    public LabelInsertion(GraphComponent element, Label label) {
        this.element = Objects.requireNonNull(element);
        this.label = Objects.requireNonNull(label);
    }

    @Override
    public Mutation.Type getType() {
        return Mutation.Type.LABEL_INSERTION;
    }

    @Override
    public void undo() {
        element.removeLabel(label);
    }

    @Override
    public void redo() {
        element.addLabel(label);
    }

    private final GraphComponent element;
    private final Label label;
}
