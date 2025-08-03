/*
** © Bart Kampers
*/

package run;

import java.util.*;


public class Factory {

    protected Factory() {
        // Utility class base should not be instantiated
    }

    protected static <T> Set<T> unmodifiable(Set<T> set) {
        if (set.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(set);
    }

}
