/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run.statemachine;


public interface GuardCondition {

    boolean applies(Memory memory) throws StateMachineException;

}
