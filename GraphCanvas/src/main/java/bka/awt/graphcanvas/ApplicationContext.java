/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.awt.graphcanvas;

import java.awt.*;
import java.util.*;
import java.util.function.*;


public interface ApplicationContext {

    void showVertexMenu(VertexComponent vertex, Point location);
    void showEdgeMenu(EdgeComponent edge, Point location);
    void editString(String input, Point location, Consumer<String> onApply);
    void requestUpdate(CanvasUpdate update);

    boolean createEdgeAllowed();

    Optional<VertexComponent> createVertexComponent(Point location);
    Optional<EdgeComponent> createEdgeComponent(VertexComponent origin, VertexComponent terminus);

}
