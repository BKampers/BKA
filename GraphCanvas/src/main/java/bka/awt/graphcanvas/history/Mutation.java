/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
 */
package bka.awt.graphcanvas.history;

public interface Mutation {

    public enum Type {
        INSERTION("Added"),
        DELETION("Deleted"),
        RELOCATION("Relocated"),
        VERTEX_RESIZE("VertexResized"),
        EDGE_TRANSFORMATION("EdgeTransformed"),
        EDGE_DIRECTED_TOGGLE("EdgeDirectedToggle"),
        EDGE_REVERT("EdgeReverted"),
        LABEL_INSERTION("LabelAdded"),
        LABEL_DELETION("LabelRemoved"),
        LABEL_MUTATION("LabelChanged");

        private Type(String bundleKey) {
            this.bundleKey = bundleKey;
        }

        public String getBundleKey() {
            return bundleKey;
        }

        private final String bundleKey;
    }


    public abstract class Symmetrical implements Mutation {

        @Override
        final public void undo() {
            revert();
        }

        @Override
        final public void redo() {
            revert();
        }

        abstract protected void revert();
    }


    void undo();

    void redo();

    Type getType();

    default String getBundleKey() {
        return getType().getBundleKey();
    }

}
