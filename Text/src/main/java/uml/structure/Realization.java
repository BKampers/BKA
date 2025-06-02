package uml.structure;

public interface Realization extends Dependency {

    Type getAncestor();

    Type getDescendant();

}
