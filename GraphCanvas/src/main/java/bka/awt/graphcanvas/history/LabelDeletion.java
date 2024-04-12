/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas.history;

import bka.awt.graphcanvas.*;
import java.util.*;


public class LabelDeletion implements Mutation {

    public LabelDeletion(GraphComponent element, Label label) {
        this.element = Objects.requireNonNull(element);
        this.label = Objects.requireNonNull(label);
    }

    @Override
    public Mutation.Type getType() {
        return Mutation.Type.LABEL_DELETION;
    }

    @Override
    public void undo() {
        element.addLabel(label);
    }

    @Override
    public void redo() {
        element.removeLabel(label);
    }

    private final GraphComponent element;
    private final Label label;
}
