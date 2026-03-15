/*
** © Bart Kampers
*/

package run;


public interface Action {

    void perform(Memory memory) throws StateMachineException;

}
