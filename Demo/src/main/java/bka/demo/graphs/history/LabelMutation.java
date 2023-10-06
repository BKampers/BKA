/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
import java.util.*;


public class LabelMutation extends Mutation.Symmetrical {

    public LabelMutation(Label label, String text) {
        this.label = Objects.requireNonNull(label);
        this.text = text;
    }

    @Override
    public Mutation.Type getType() {
        return Mutation.Type.LABEL_MUTATION;
    }

    @Override
    protected void revert() {
        String currentText = label.getText();
        label.setText(text);
        text = currentText;
    }

    private final Label label;
    private String text;

}
