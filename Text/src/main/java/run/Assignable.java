package run;


/**
 * {@link Evaluable} that can appear on the left-hand side of an assignment.
 */
public interface Assignable extends Evaluable {

    void assign(Execution execution, java.lang.Object value, ObjectScope scope);

}
