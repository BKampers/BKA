package run;


/**
 * Executable {@link Expression}.
 */
public interface Evaluable extends Expression {

    java.lang.Object evaluate(Execution execution, ObjectScope scope);

}
