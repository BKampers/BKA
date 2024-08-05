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

    public boolean isNavigateEnd(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.VK_END && event.getModifiersEx() == 0;
    }

    public boolean isNavigateHome(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.VK_HOME && event.getModifiersEx() == 0;
    }
    
    public boolean isNavigatePageDown(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.VK_PAGE_DOWN && event.getModifiersEx() == 0;
    }

    public boolean isNavigatePageUp(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.VK_PAGE_UP && event.getModifiersEx() == 0;
    }
    

    public boolean isNavigateLeft(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.VK_LEFT && event.getModifiersEx() == 0;
    }

    public boolean isNavigateRight(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.VK_RIGHT && event.getModifiersEx() == 0;
    }

    public boolean isNavigateUp(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.VK_UP && event.getModifiersEx() == 0;
    }

    public boolean isNavigateDown(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.VK_DOWN && event.getModifiersEx() == 0;
    }

    public boolean isRedo(KeyEvent event) {
        return event.getKeyCode() == redoKeyCode && event.getModifiersEx() == redoModifiers;
    }

    public boolean isUndo(KeyEvent event) {
        return event.getKeyCode() == undoKeyCode && event.getModifiersEx() == undoModifiers;
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
