/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import java.awt.*;


public interface Element extends Renderer {

    Point getLocation();

    void move(Point vector);

}
