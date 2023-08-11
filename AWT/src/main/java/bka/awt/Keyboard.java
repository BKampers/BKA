/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt;

import java.awt.event.*;


public class Keyboard {

    public static Keyboard getInstance() {
        if (instance == null) {
            instance = new Keyboard();
        }
        return instance;
    }

    public int getToggleSelectionModifiers() {
        return toggleSelectionModifiers;
    }

    public boolean isDelete(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.VK_DELETE;
    }

    public boolean isUndo(KeyEvent event) {
        return event.getKeyCode() == undoKeyCode && event.getModifiersEx() == undoModifiers;
    }

    public boolean isRedo(KeyEvent event) {
        return event.getKeyCode() == redoKeyCode && event.getModifiersEx() == redoModifiers;
    }

    private Keyboard() {
        String osName = System.getProperty("os.name");
        if (osName.contains("Mac")) {
            toggleSelectionModifiers = InputEvent.META_DOWN_MASK;
            undoKeyCode = KeyEvent.VK_Z;
            undoModifiers = KeyEvent.META_DOWN_MASK;
            redoKeyCode = KeyEvent.VK_Z;
            redoModifiers = InputEvent.SHIFT_DOWN_MASK | InputEvent.META_DOWN_MASK;
        }
        else if (osName.contains("Windows")) {
            toggleSelectionModifiers = InputEvent.CTRL_DOWN_MASK;
            undoKeyCode = KeyEvent.VK_Z;
            undoModifiers = InputEvent.CTRL_DOWN_MASK;
            redoKeyCode = KeyEvent.VK_Y;
            redoModifiers = KeyEvent.CTRL_DOWN_MASK;
        }
        else {
            toggleSelectionModifiers = InputEvent.CTRL_DOWN_MASK;
            undoKeyCode = KeyEvent.VK_UNDO;
            undoModifiers = 0;
            redoKeyCode = KeyEvent.VK_UNDO;
            redoModifiers = InputEvent.SHIFT_DOWN_MASK;
        }
    }

    private static Keyboard instance;

    private final int toggleSelectionModifiers;
    private final int undoKeyCode;
    private final int undoModifiers;
    private final int redoKeyCode;
    private final int redoModifiers;

}
