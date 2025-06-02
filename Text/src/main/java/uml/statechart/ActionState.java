package uml.statechart;

import java.util.*;

/**
 * @param <A> action
 */
public interface ActionState<A> extends TransitionSource<A>, TransitionTarget<A> {

    public Optional<A> getAction();
    
}
