/*
** © Bart Kampers
*/

package bka.demo.graphs.history;

import bka.demo.graphs.*;
import java.util.*;

public interface Mutation {

    public enum Type {
        INSERTION, DELETION, RELOCATION, VERTEX_RESIZE, EDGE_TRANSFORMATION, LABEL_INSERTION, LABEL_DELETION, LABEL_MUTATION
    }


    public abstract class Symmetrical implements Mutation {

        @Override
        public void undo() {
            revert();
        }

        @Override
        public void redo() {
            revert();
        }

        abstract protected void revert();
    }


    Type getType();

    void undo();

    void redo();

    default Collection<VertexRenderer> getVertices() {
        return Collections.emptyList();
    }

    default Collection<EdgeRenderer> getEdges() {
        return Collections.emptyList();
    }

}
