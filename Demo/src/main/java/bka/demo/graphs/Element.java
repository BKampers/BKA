/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import java.awt.*;


public interface Element extends Renderer {

    Point getLocation();

    Dimension getDimension();

    void move(Point vector);

}
