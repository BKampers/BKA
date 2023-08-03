/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import java.awt.*;


public interface EdgeRenderer extends Renderer {

    public long squareDistance(Point point);

    public Point getStartConnectorPoint();

    public Point getEndConnectorPoint();

}
