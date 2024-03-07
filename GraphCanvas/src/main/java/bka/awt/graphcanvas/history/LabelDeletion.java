/*
** © Bart Kampers
*/

package bka.awt.graphcanvas.history;

import bka.awt.graphcanvas.*;
import java.util.*;


public class LabelDeletion implements Mutation {

    public LabelDeletion(Element element, Label label) {
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

    private final Element element;
    private final Label label;
}
