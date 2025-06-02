/*
** Copyright © Bart Kampers
*/

package uml.structure;

import java.util.*;


public interface Typed extends Named {
    
    Optional<Type> getType();

}
