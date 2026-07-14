package run;


/**
 * Expression that can be evaluated at runtime.
 */
public interface RuntimeExpression extends uml.structure.Expression {

    java.lang.Object evaluate() throws MemoryException;

}
