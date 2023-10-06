/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
import java.util.*;


public class LabelInsertion implements Mutation {

    public LabelInsertion(Element element, Label label) {
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

    private final Element element;
    private final Label label;
}
