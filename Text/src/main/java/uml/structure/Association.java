package uml.structure;


public interface Association {

    
    public enum Kind {
        PLAIN,
        AGGREGATION,
        COMPOSITION
    }


    Structural getDependent();

    Structural getIndependent();

    Kind getKind();

    boolean isDirected();

}
