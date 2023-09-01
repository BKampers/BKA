/*
** © Bart Kampers
*/

package bka.demo.graphs;

import java.util.*;

public interface Mutation {

    void undo();

    void redo();

    Collection<VertexRenderer> getVertices();

    Collection<EdgeRenderer> getEdges();

}
