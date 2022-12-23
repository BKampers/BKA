/*
** Copyright © Bart Kampers
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


    public boolean isDelete(KeyEvent evt) {
        return evt.getKeyCode() == KeyEvent.VK_DELETE;
    }

    
    public boolean isUndo(KeyEvent evt) {
        return evt.getKeyCode() == undoKeyCode && evt.getModifiersEx() == undoModifiers;
    }


    public boolean isRedo(KeyEvent evt) {
        return evt.getKeyCode() == redoKeyCode && evt.getModifiersEx() == redoModifiers;
    }


    private Keyboard() {
        String osName = System.getProperty("os.name");
        if (osName.contains("Mac")) {
            undoKeyCode = KeyEvent.VK_Z;
            undoModifiers = KeyEvent.META_DOWN_MASK;
            redoKeyCode = KeyEvent.VK_Z;
            redoModifiers = KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK;
        }
        else if (osName.contains("Windows")) {
            undoKeyCode = KeyEvent.VK_Z;
            undoModifiers = KeyEvent.CTRL_DOWN_MASK;
            redoKeyCode = KeyEvent.VK_Y;
            redoModifiers = KeyEvent.CTRL_DOWN_MASK;
        }
        else {
            undoKeyCode = KeyEvent.VK_UNDO;
            undoModifiers = 0;
            redoKeyCode = KeyEvent.VK_UNDO;
            redoModifiers = KeyEvent.SHIFT_DOWN_MASK;
        }
    }


    private static Keyboard instance;

    private final int undoKeyCode;
    private final int undoModifiers;
    private final int redoKeyCode;
    private final int redoModifiers;

}
