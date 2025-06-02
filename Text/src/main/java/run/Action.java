/*
** © Bart Kampers
*/

package run;


public interface Action {

    public void perform(Memory memory) throws StateMachineException;

}
