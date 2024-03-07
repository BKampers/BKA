/*
** © Bart Kampers
*/

package bka.awt.graphcanvas;

import java.awt.*;
import java.util.function.*;


public interface ApplicationContext {

    void showEdgeMenu(EdgeRenderer edge, Point location);
    void editString(String input, Point location, Consumer<String> onApply);
    void requestUpdate(ComponentUpdate update);

    EdgeRenderer createEdgeRenderer(VertexRenderer origin);

}
