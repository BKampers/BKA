/*
** © Bart Kampers
*/

package run;


public interface Memory {

    Object load(String name) throws StateMachineException;

    void store(String name, Object value);

}
