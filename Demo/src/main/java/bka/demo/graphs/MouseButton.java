/*
** © Bart Kampers
*/
package bka.demo.graphs;

import bka.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 ** @author bartkampers
 */
public enum MouseButton {

    MAIN(MouseEvent.BUTTON1),
    TOGGLE_SELECT(MouseEvent.BUTTON1, Keyboard.getInstance().getToggleSelectionModifiers()),
    RESET(MouseEvent.BUTTON3),
    EDIT(MouseEvent.BUTTON1, MouseEvent.ALT_DOWN_MASK),
    UNSUPPORTED(0, 0, 0);

    private MouseButton(int buttonId, int clickCount, int modifiers) {
        this.buttonId = buttonId;
        this.clickCount = clickCount;
        this.modifiers = modifiers;
    }

    private MouseButton(int buttonId, int modifiers) {
        this(buttonId, 1, modifiers);
    }

    private MouseButton(int buttonId) {
        this(buttonId, 1, 0);
    }

    public static MouseButton get(MouseEvent event) {
        return Arrays.asList(values()).stream()
            .filter(button -> event.getButton() == button.buttonId)
            .filter(button -> event.getClickCount() == button.clickCount)
            .filter(button -> button.modifierMatch(event))
            .findAny()
            .orElse(UNSUPPORTED);
    }

    public boolean modifierMatch(MouseEvent event) {
        return (event.getModifiersEx() & MASK) == modifiers;
    }

    private final int buttonId;
    private final int clickCount;
    private final int modifiers;

    private static final int MASK = InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;

}
