package uml.statechart;

import java.util.*;
import uml.structure.*;

/**
 * @param <E> event
 * @param <G> guard condition
 * @param <A> action
 */
public interface Transition<E, G, A> extends Stereotyped {

    TransitionSource getSource();

    TransitionTarget getTarget();

    Optional<E> getEvent();

    Optional<G> getGuardCondition();

    Optional<A> getAction();
    
}
