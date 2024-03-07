/*
** © Bart Kampers
*/

package bka.awt.graphcanvas;


public class ComponentUpdate {

    private static final int NO_CURSOR_CHANGE = -1;
    public static final ComponentUpdate NO_OPERATION = ComponentUpdate.noOperation(NO_CURSOR_CHANGE);
    public static final ComponentUpdate REPAINT = ComponentUpdate.repaint(NO_CURSOR_CHANGE);

    public ComponentUpdate(int cursorType, boolean needRepeaint) {
        this.cursorType = cursorType;
        this.needRepeaint = needRepeaint;
    }

    public static ComponentUpdate noOperation(int cursorType) {
        return new ComponentUpdate(cursorType, false);
    }

    public static ComponentUpdate repaint(int cursorType) {
        return new ComponentUpdate(cursorType, true);
    }

    public int getCursorType() {
        return cursorType;
    }

    public boolean needsRepeaint() {
        return needRepeaint;
    }

    private final int cursorType;
    private final boolean needRepeaint;

}
