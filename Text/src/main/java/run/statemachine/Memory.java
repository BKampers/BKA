/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run.statemachine;


public interface Memory {

    Object UNINITIALIZED = new Object() {
        @Override
        public String toString() {
            return "@uninitialized";
        }
    };

    Object load(String name) throws MemoryException;

    void store(String name, Object value) throws MemoryException;

}
