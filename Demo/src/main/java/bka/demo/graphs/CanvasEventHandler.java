/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;


public abstract class CanvasEventHandler {

    public CanvasEventHandler(GraphCanvas canvas) {
        this.canvas = Objects.requireNonNull(canvas);
    }

    public void paint(Graphics2D graphics) {
    }

    public ComponentUpdate mouseMoved(MouseEvent event) {
        throw unexpectedEventException(event);
    }

    public ComponentUpdate mousePressed(MouseEvent event) {
        throw unexpectedEventException(event);
    }

    public ComponentUpdate mouseDragged(MouseEvent event) {
        throw unexpectedEventException(event);
    }

    public ComponentUpdate mouseReleased(MouseEvent event) {
        throw unexpectedEventException(event);
    }

    public ComponentUpdate mouseClicked(MouseEvent event) {
        throw unexpectedEventException(event);
    }

    private IllegalStateException unexpectedEventException(MouseEvent event) {
        return new IllegalStateException(getClass().getSimpleName() + ": " + event.paramString());
    }

    public ComponentUpdate keyPressed(KeyEvent event) {
        return ComponentUpdate.NO_OPERATION;
    }

    public ComponentUpdate keyReleased(KeyEvent event) {
        return ComponentUpdate.NO_OPERATION;
    }

    protected final GraphCanvas getCanvas() {
        return canvas;
    }

    private final GraphCanvas canvas;

}
