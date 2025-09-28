/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
 */
package bka.text.parser.pascal;

import java.util.*;
import java.util.function.*;
import run.*;
import uml.annotation.*;
import uml.statechart.*;

/**
 */
public final class ActivityDiagramBuilder {

    public ActivityDiagramBuilder() {
        leaves.add(UmlStateFactory.getInitialState());
    }

    public void add(Function<TransitionSource, Transition<Event, GuardCondition, Action>> consumer, Decision decision) {
        requireNotFinished();
        leaves.forEach(leave -> transitions.add(consumer.apply(leave)));
        leaves.clear();
        leaves.add(decision);
    }

    public void add(Decision decision, Set<Stereotype> stereotypes) {
        requireNotFinished();
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision, stereotypes)));
        leaves.clear();
        leaves.add(decision);
    }

    public void add(ActionState<Action> loopInitialization, Decision decision) {
        requireNotFinished();
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, loopInitialization)));
        leaves.clear();
        transitions.add(UmlTransitionFactory.createTransition(loopInitialization, decision));
        leaves.add(decision);
    }

    public void add(ActionState<Action> incrementActionState, Decision decision, String strerotype) {
        requireNotFinished();
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, incrementActionState)));
        addGuardCondition(transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "for");
        transitions.add(UmlTransitionFactory.createTransition(incrementActionState, decision));
        leaves.clear();
        leaves.add(decision);
    }

    public void add(ActionState<Action> assignment) {
        requireNotFinished();
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, assignment)));
        leaves.clear();
        leaves.add(assignment);
    }

    public void addLeaf(Decision decision) {
        requireNotFinished();
        leaves.add(decision);
    }

    public void addLeaves(Collection<TransitionSource> leavesToAdd) {
        requireNotFinished();
        leaves.addAll(leavesToAdd);
    }

    public Collection<TransitionSource> replaceLeaves(Collection<TransitionSource> replacement) {
        requireNotFinished();
        Collection<TransitionSource> oldLeaves = new ArrayList<>(leaves);
        leaves.clear();
        leaves.addAll(replacement);
        return oldLeaves;
    }

    public void addTransition(Decision decision, TransitionTarget loopTarget, GuardCondition guardCondition, String stereotype) {
        requireNotFinished();
        transitions.add(UmlTransitionFactory.createTransition(decision, loopTarget, UmlGuardConditionFactory.fail(decision), UmlStereotypeFactory.createStereotypes(stereotype)));
        leaves.clear();
        leaves.add(decision);
    }

    public void addGuardCondition(Predicate<Transition<Event, GuardCondition, Action>> predicate, GuardCondition guardCondition, String stereotype) {
        requireNotFinished();
        Transition<Event, GuardCondition, Action> transition = transitions.stream().filter(predicate).findAny().get();
        transitions.remove(transition);
        transitions.add(UmlTransitionFactory.copyTransition(transition, Optional.of(guardCondition), UmlStereotypeFactory.createStereotypes(stereotype)));
    }

    public void addStereotype(Predicate<Transition<Event, GuardCondition, Action>> predicate, String stereotype) {
        Transition<Event, GuardCondition, Action> transition = transitions.stream().filter(predicate).findAny().get();
        transitions.remove(transition);
        transitions.add(UmlTransitionFactory.copyTransition(transition, transition.getGuardCondition(), UmlStereotypeFactory.createStereotypes(stereotype)));
    }

    public void addFinalState() {
        requireNotFinished();
        leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, UmlStateFactory.getFinalState())));
        leaves.clear();
    }

    private void requireNotFinished() throws IllegalStateException {
        if (leaves.isEmpty()) {
            throw new IllegalStateException("Diagram finished");
        }
    }

    public TransitionSource anyLeaf() {
        return leaves.stream().findAny().get();
    }

    public TransitionTarget targetOf(TransitionSource loopRoot) {
        return transitions.stream().filter(transition -> loopRoot.equals(transition.getSource())).findAny().get().getTarget();
    }

    public Collection<Transition<Event, GuardCondition, Action>> getTransitions() {
        if (!leaves.isEmpty()) {
            throw new IllegalStateException("Diagram not finished");
        }
        return Collections.unmodifiableCollection(transitions);
    }

    private final Collection<Transition<Event, GuardCondition, Action>> transitions = new ArrayList<>();
    private final Collection<TransitionSource> leaves = new ArrayList<>();

}
