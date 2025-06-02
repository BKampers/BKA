/*
** © Bart Kampers
*/

package run;


public interface GuardCondition {

    boolean applies(Memory memory) throws StateMachineException;

}
