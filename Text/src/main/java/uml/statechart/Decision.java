package uml.statechart;

/**
 * @param <E> expression
 */
public interface Decision<E> extends TransitionSource<E>, TransitionTarget<E>, uml.structure.Typed {

    E getExpression();

}
