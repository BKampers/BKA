/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;

public enum ResizeDirection {

    NORTH(Cursor.N_RESIZE_CURSOR),
    NORTH_EAST(Cursor.NE_RESIZE_CURSOR),
    EAST(Cursor.E_RESIZE_CURSOR),
    SOUTH_EAST(Cursor.SE_RESIZE_CURSOR),
    SOUTH(Cursor.S_RESIZE_CURSOR),
    SOUTH_WEST(Cursor.SW_RESIZE_CURSOR),
    WEST(Cursor.W_RESIZE_CURSOR),
    NORTH_WEST(Cursor.NW_RESIZE_CURSOR);

    private ResizeDirection(int cursorType) {
        this.cursorType = cursorType;
    }

    public int getCursorType() {
        return cursorType;
    }

    private final int cursorType;

}
