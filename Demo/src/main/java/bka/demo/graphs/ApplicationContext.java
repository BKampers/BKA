/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.awt.*;
import java.util.function.*;


public interface ApplicationContext {

    void editString(String input, Point location, Consumer<String> onApply);
    void requestUpdate(ComponentUpdate update);

    EdgeRenderer createEdgeRenderer(VertexRenderer origin);

}
