/*
** © Bart Kampers
*/

package run;

import java.util.*;
import java.util.stream.*;
import uml.*;
import uml.annotation.*;
import uml.statechart.*;


public class UmlTransitionFactory extends Factory {

    private UmlTransitionFactory() {
        // Utility class should not be instantiated
    }

    public static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target) {
        return createTransition(Objects.requireNonNull(source), Objects.requireNonNull(target), Optional.empty(), Optional.empty(), Optional.empty(), Collections.emptySet());
    }

    public static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, Set<Stereotype> stereotypes) {
        return createTransition(Objects.requireNonNull(source), Objects.requireNonNull(target), Optional.empty(), Optional.empty(), Optional.empty(), unmodifiable(stereotypes));
    }

    public static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, GuardCondition guardCondition, Set<Stereotype> stereotypes) {
        return createTransition(Objects.requireNonNull(source), Objects.requireNonNull(target), Optional.empty(), Optional.of(guardCondition), Optional.empty(), unmodifiable(stereotypes));
    }

    public static Transition<Event, GuardCondition, Action> copyTransition(Transition<Event, GuardCondition, Action> transition, Optional<GuardCondition> guardCondition, Set<Stereotype> stereotypes) {
        return createTransition(transition.getSource(), transition.getTarget(), transition.getEvent(), guardCondition, transition.getAction(), unmodifiable(stereotypes));
    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, Optional<Event> event, Optional<GuardCondition> guardCondition, Optional<Action> action, Set<Stereotype> stereotypes) {
        return new Transition<>() {

            @Override
            public TransitionSource getSource() {
                return source;
            }

            @Override
            public TransitionTarget getTarget() {
                return target;
            }

            @Override
            public Optional<Event> getEvent() {
                return event;
            }

            @Override
            public Optional<GuardCondition> getGuardCondition() {
                return guardCondition;
            }

            @Override
            public Optional<Action> getAction() {
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
