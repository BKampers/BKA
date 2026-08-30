/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run.statemachine;


public interface Action {

    void perform(Memory memory) throws StateMachineException;

}
