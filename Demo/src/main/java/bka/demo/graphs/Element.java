/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.*;
import bka.demo.graphs.Label;
import java.awt.*;


public interface Element extends Renderer {

    Point getLocation();

    Dimension getDimension();

    void move(Point vector);

    void addLabel(Label label);

    void removeLabel(Label label);

}
