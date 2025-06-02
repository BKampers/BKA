package uml.statechart;

import java.util.*;


/**
 * @param <A> action or activity
 */
public interface State<A> extends TransitionSource<A>, TransitionTarget<A> {

    Optional<String> getName();

    Optional<A> getEntryAction();

    Optional<A> getExitAction();

    Optional<A> getActivity();
     
}
