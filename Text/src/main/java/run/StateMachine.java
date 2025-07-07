/*
** © Bart Kampers
*/

package run;


import java.util.*;
import uml.statechart.*;


public class StateMachine {


    public interface Listener {

        public void stateEntered(TransitionNode<Action> state);

        public void stateLeft(TransitionNode<Action> state);
    }


    public enum Mode {
        RUN, STEP
    }


    public StateMachine(Collection<Transition<Event, GuardCondition, Action>> diagram) {
        long initialStates = diagram.stream().filter(transition -> transition.getSource() instanceof InitialState).count();
        if (initialStates != 1) {
            throw new IllegalArgumentException("Illegal number of initial states: " + initialStates);
        }
        this.diagram = new ArrayList<>(diagram);
    }

    public synchronized void start() throws StateMachineException {
        currentState = diagram.stream()
            .map(Transition::getSource)
            .filter(source -> source instanceof InitialState)
            .findAny().get();
        trigger(null);
    }

    public void resume() {
        mode = Mode.RUN;
        synchronized (semaphore) {
            semaphore.notify();
        }
    }

    public void step() {
        synchronized (semaphore) {
            semaphore.notify();
        }
    }

    public StateMachine.Mode getMode() {
        return mode;
    }

    public synchronized void trigger(Event event) throws StateMachineException {
        currentTransition = findTransition(currentState, Optional.ofNullable(event));
        while (currentTransition != null) {
            leave(currentState);
            if (currentTransition.getAction().isPresent()) {
                execute(currentTransition.getAction().get());
            }
            TransitionNode<Action> next = currentTransition.getTarget();
            if (!(next instanceof FinalState)) {
                enter(next);
                currentTransition = findTransition(currentState, Optional.empty());
            }
            else {
                currentTransition = null;
            }
        }
    }

    private Transition<Event, GuardCondition, Action> findTransition(TransitionNode<Action> state, Optional<Event> event) throws StateMachineException {
        List<Transition<Event, GuardCondition, Action>> guardedTransitions = new ArrayList<>();
        List<Transition<Event, GuardCondition, Action>> unguardedTransitions = new ArrayList<>();
        for (Transition<Event, GuardCondition, Action> transition : diagram) {
            if (state.equals(transition.getSource()) && event.equals(transition.getEvent())) {
                if (transition.getGuardCondition().isEmpty()) {
                    unguardedTransitions.add(transition);
                }
                else if (transition.getGuardCondition().get().applies(memory)) {
                    guardedTransitions.add(transition);
                }
            }
        }
        List<Transition<Event, GuardCondition, Action>> transitions = guardedTransitions;
        if (transitions.isEmpty()) {
            transitions = unguardedTransitions;
        }
        if (transitions.size() > 1) {
            throw new StateMachineException("Too many outgoing transitions for state " + state + " and event" + event);
        }
        if (transitions.isEmpty()) {
            return null;
        }
        return transitions.getFirst();
    }

    private void enter(TransitionNode<Action> state) throws StateMachineException {
        currentState = state;
        listeners.forEach(listener -> listener.stateEntered(state));
        if (state.getEntryAction().isPresent()) {
            execute(state.getEntryAction().get());
        }
        if (state.getAction().isPresent()) {
            execute(state.getAction().get());
        }
    }

    private void leave(TransitionNode<Action> state) throws StateMachineException {
        if (state.getExitAction().isPresent()) {
            execute(state.getExitAction().get());
        }
        currentState = null;
        listeners.forEach(listener -> listener.stateLeft(state));
    }

    private void execute(Action action) throws StateMachineException {
        if (mode == Mode.STEP) {
            waitForSemaphore();
        }
        action.perform(memory);
    }

    private void waitForSemaphore() throws StateMachineException {
        synchronized (semaphore) {
            try {
                semaphore.wait();
            }
            catch (InterruptedException ex) {
                throw new StateMachineException(ex);
            }
        }
    }

    public Object getMemoryObject(String identifier) throws StateMachineException {
        return memory.load(identifier);
    }

    private final Memory memory = new Memory() {

        @Override
        public Object load(String identifier) throws StateMachineException {
            Object value = map.get(identifier);
            if (value == null) {
                throw new StateMachineException("Memory does not contain value for identifier '" + identifier + "'");
            }
            return value;
        }

        @Override
        public void store(String identifier, Object value) {
            map.put(identifier, value);
        }

        private final Map<String, Object> map = new HashMap<>();

    };


    private final Collection<Transition<Event, GuardCondition, Action>> diagram;
    private Mode mode = Mode.RUN;

    private TransitionNode<Action> currentState;
    private Transition<Event, GuardCondition, Action> currentTransition;

    private final Collection<Listener> listeners = new ArrayList<>();
    private final java.lang.Object semaphore = new java.lang.Object();
}
