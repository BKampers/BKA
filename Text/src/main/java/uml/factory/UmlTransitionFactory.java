/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package uml.factory;

import java.util.*;
import java.util.stream.*;
import uml.*;
import uml.annotation.*;
import uml.statechart.*;


public class UmlTransitionFactory {

    private UmlTransitionFactory() {
        // Utility class should not be instantiated
    }

    public static <E, G, A> Transition<E, G, A> createTransition(TransitionSource source, TransitionTarget target) {
        return createTransition(Objects.requireNonNull(source), Objects.requireNonNull(target), Optional.empty(), Optional.empty(), Optional.empty(), Collections.emptySet());
    }

    public static <E, G, A> Transition<E, G, A> createTransition(TransitionSource source, TransitionTarget target, Set<Stereotype> stereotypes) {
        return createTransition(Objects.requireNonNull(source), Objects.requireNonNull(target), Optional.empty(), Optional.empty(), Optional.empty(), Set.copyOf(stereotypes));
    }

    public static <E, G, A> Transition<E, G, A> createTransition(TransitionSource source, TransitionTarget target, G guardCondition, Set<Stereotype> stereotypes) {
        return createTransition(Objects.requireNonNull(source), Objects.requireNonNull(target), Optional.empty(), Optional.of(guardCondition), Optional.empty(), Set.copyOf(stereotypes));
    }

    public static <E, G, A> Transition<E, G, A> copyTransition(Transition<E, G, A> transition, Optional<G> guardCondition, Set<Stereotype> stereotypes) {
        return createTransition(transition.getSource(), transition.getTarget(), transition.getEvent(), guardCondition, transition.getAction(), Set.copyOf(stereotypes));
    }

    private static <E, G, A> Transition<E, G, A> createTransition(TransitionSource source, TransitionTarget target, Optional<E> event, Optional<G> guardCondition, Optional<A> action, Set<Stereotype> stereotypes) {
        return new Transition<E,G,A>() {

            @Override
            public TransitionSource getSource() {
                return source;
            }

            @Override
            public TransitionTarget getTarget() {
                return target;
            }

            @Override
            public Optional<E> getEvent() {
                return event;
            }

            @Override
            public Optional<G> getGuardCondition() {
                return guardCondition;
            }

            @Override
            public Optional<A> getAction() {
                return action;
            }

            @Override
            public Set<Stereotype> getStereotypes() {
                return stereotypes;
            }

            @Override
            public String toString() {
                StringBuilder string = new StringBuilder();
                guardCondition.ifPresent(condition -> string.append('[').append(condition).append("] "));
                string.append(getSource()).append(" \u279D ").append(getTarget());
                string.append(stereotypes.stream().map(Util::display).collect(Collectors.joining()));
                return string.toString();
            }

        };
    }


}
