/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas.handlers;

import bka.awt.graphcanvas.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public abstract class CanvasEventHandler {

    public CanvasEventHandler(GraphCanvas canvas) {
        this.canvas = Objects.requireNonNull(canvas);
    }

    public void paint(Graphics2D graphics) {
    }

    public CanvasUpdate mouseMoved(MouseEvent event) {
        throw unexpectedEventException(event);
    }

    public CanvasUpdate mousePressed(MouseEvent event) {
        throw unexpectedEventException(event);
    }

    public CanvasUpdate mouseDragged(MouseEvent event) {
        throw unexpectedEventException(event);
    }

    public CanvasUpdate mouseReleased(MouseEvent event) {
        throw unexpectedEventException(event);
    }

    public CanvasUpdate mouseClicked(MouseEvent event) {
        throw unexpectedEventException(event);
    }

    private IllegalStateException unexpectedEventException(MouseEvent event) {
        return new IllegalStateException(getClass().getSimpleName() + ": " + event.paramString());
    }

    public CanvasUpdate keyPressed(KeyEvent event) {
        return CanvasUpdate.NO_OPERATION;
    }

    public CanvasUpdate keyReleased(KeyEvent event) {
        return CanvasUpdate.NO_OPERATION;
    }

    protected final GraphCanvas getCanvas() {
        return canvas;
    }

    private final GraphCanvas canvas;

}
