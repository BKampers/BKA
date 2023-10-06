/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
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
