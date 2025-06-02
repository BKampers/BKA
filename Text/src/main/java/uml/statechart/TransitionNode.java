/*
** © Bart Kampers
*/

package uml.statechart;

import java.util.*;


public interface TransitionNode<A> {

    default Optional<A> getAction() {
        return Optional.empty();
    }

    default Optional<A> getEntryAction() {
        return Optional.empty();
    }

    default Optional<A> getExitAction() {
        return Optional.empty();
    }

    default Optional<A> getActivity() {
        return Optional.empty();
    }

}
