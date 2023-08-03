/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import java.awt.*;


public interface VertexRenderer extends Renderer {

    Point getLocation();

    default Point getConnectorPoint(Point edgePoint) {
        return getLocation();
    }

    default long squareDistance(Point point) {
        throw new UnsupportedOperationException();
    }

    public void setLocation(Point point);

}
