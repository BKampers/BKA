/*
** © Bart Kampers
*/

package bka.demo.graphs;


public class ComponentUpdate {

    private static final int NO_CURSOR_CHANGE = 0;
    public static final ComponentUpdate NO_OPERATION = new ComponentUpdate(NO_CURSOR_CHANGE, false);
    public static final ComponentUpdate REPAINT = new ComponentUpdate(NO_CURSOR_CHANGE, true);

    public ComponentUpdate(int cursorType, boolean needRepeaint) {
        this.cursorType = cursorType;
        this.needRepeaint = needRepeaint;
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
