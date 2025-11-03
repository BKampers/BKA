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
        leavesStack.push(leavesOf(UmlStateFactory.getInitialState()));
    }

    public void add(Function<TransitionSource, Transition<Event, GuardCondition, Action>> consumer, Decision decision) {
        requireNotFinished();
        leaves().forEach(leave -> transitions.add(consumer.apply(leave)));
        setLeaf(decision);
    }

    public void add(Decision decision, Set<Stereotype> stereotypes) {
        requireNotFinished();
        leaves().forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision, stereotypes)));
        setLeaf(decision);
    }

    public void add(ActionState<Action> initializer, Decision decision) {
        requireNotFinished();
        leaves().forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, initializer)));
        transitions.add(UmlTransitionFactory.createTransition(initializer, decision));
        setLeaf(decision);
    }

    public void add(ActionState<Action> incrementActionState, Decision decision, String stereotype) {
        requireNotFinished();
        leaves().forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, incrementActionState)));
        addGuardCondition(transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), stereotype);
        transitions.add(UmlTransitionFactory.createTransition(incrementActionState, decision));
        setLeaf(decision);
    }

    public void add(Decision decision, ActionState<Action> incrementActionState, TransitionTarget loopStart, String stereotype) {
        requireNotFinished();
        leaves().forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision)));
        transitions.add(UmlTransitionFactory.createTransition(decision, incrementActionState));
        transitions.add(UmlTransitionFactory.createTransition(incrementActionState, loopStart));
        addGuardCondition(transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), stereotype);
        setLeaf(decision);
    }

    public void add(ActionState<Action> assignment) {
        requireNotFinished();
        leaves().forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, assignment)));
        setLeaf(assignment);
    }

    public void addLeaf(Decision decision) {
        requireNotFinished();
        leaves().add(decision);
    }

    public void fork(TransitionSource leaf) {
        requireNotFinished();
        leavesStack.push(leavesOf(Objects.requireNonNull(leaf)));
    }

    public void join() {
        requireNotFinished();
        if (leavesStack.size() == 1) {
            throw new IllegalStateException("Diagram is not forked");
        }
        Collection<TransitionSource> branchLeaves = leavesStack.pop();
        leaves().addAll(branchLeaves);
    }

    public void addTransition(Decision decision, TransitionTarget loopTarget, GuardCondition guardCondition, String stereotype) {
        requireNotFinished();
        transitions.add(UmlTransitionFactory.createTransition(decision, loopTarget, UmlGuardConditionFactory.fail(decision), UmlStereotypeFactory.createStereotypes(stereotype)));
        setLeaf(decision);
    }

    public void addGuardCondition(Predicate<Transition<Event, GuardCondition, Action>> predicate, GuardCondition guardCondition, String stereotype) {
        requireNotFinished();
        Transition<Event, GuardCondition, Action> transition = transitions.stream().filter(predicate).findAny().get();
        transitions.remove(transition);
        transitions.add(UmlTransitionFactory.copyTransition(transition, Optional.of(guardCondition), UmlStereotypeFactory.createStereotypes(stereotype)));
    }

    public void addGuardCondition(TransitionSource source, GuardCondition guardCondition, String stereotype) {
        requireNotFinished();
        Transition<Event, GuardCondition, Action> transition = transitions.stream()
            .filter(t -> source.equals(t.getSource()))
            .findAny().get();
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
        if (leavesStack.size() > 1) {
            throw new IllegalStateException("Forked diagram cannot be finished");
        }
        leavesStack.pop().forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, UmlStateFactory.getFinalState())));
    }

    private void requireNotFinished() {
        if (leavesStack.isEmpty()) {
            throw new IllegalStateException("Diagram finished");
        }
    }

    public TransitionSource anyLeaf() {
        return leaves().stream().findAny().get();
    }

    public TransitionTarget targetOf(TransitionSource source) {
        return transitions.stream().filter(transition -> source.equals(transition.getSource())).findAny().get().getTarget();
    }

    public Collection<Transition<Event, GuardCondition, Action>> getTransitions() {
        if (!leavesStack.isEmpty()) {
            throw new IllegalStateException("Diagram not finished");
        }
        return Collections.unmodifiableCollection(transitions);
    }

    private void setLeaf(TransitionSource decision) {
        leaves().clear();
        leaves().add(decision);
    }

    private Collection<TransitionSource> leaves() {
        return leavesStack.peek();
    }

    private static Collection<TransitionSource> leavesOf(TransitionSource leaf) {
        Collection<TransitionSource> leaves = new ArrayList<>();
        leaves.add(leaf);
        return leaves;
    }

    private final Collection<Transition<Event, GuardCondition, Action>> transitions = new ArrayList<>();
    private final Deque<Collection<TransitionSource>> leavesStack = new LinkedList<>();

}
