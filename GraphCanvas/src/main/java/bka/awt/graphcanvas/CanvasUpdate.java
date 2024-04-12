/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;


public class CanvasUpdate {

    private static final int NO_CURSOR_CHANGE = -1;
    public static final CanvasUpdate NO_OPERATION = CanvasUpdate.noOperation(NO_CURSOR_CHANGE);
    public static final CanvasUpdate REPAINT = CanvasUpdate.repaint(NO_CURSOR_CHANGE);

    public CanvasUpdate(int cursorType, boolean needRepeaint) {
        this.cursorType = cursorType;
        this.needRepeaint = needRepeaint;
    }

    public static CanvasUpdate noOperation(int cursorType) {
        return new CanvasUpdate(cursorType, false);
    }

    public static CanvasUpdate repaint(int cursorType) {
        return new CanvasUpdate(cursorType, true);
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
