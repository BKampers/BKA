/*
** © Bart Kampers
*/

package run;


public interface Memory {

    Object load(String name) throws MemoryException;

    void store(String name, Object value) throws MemoryException;

}
