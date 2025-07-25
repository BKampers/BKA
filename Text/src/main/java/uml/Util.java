/*
** © Bart Kampers
*/

package uml;

import uml.annotation.*;
import uml.structure.*;


public class Util {
    
    
    public static String display(Stereotype stereotype) {
        return '«' + stereotype.getName() + '»';
    }

    public static boolean isConstructor(Operation operation) {
        return operation.getStereotypes().contains(Stereotype.CONSTRUCTOR);
    }

}
