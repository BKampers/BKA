/*
** © Bart Kampers
*/

package bka.demo.graphs;

import bka.awt.graphcanvas.*;


public interface Factory {

    Paintable getDefaultInstance();

    Paintable getCopyInstance();

}
