/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run;

import uml.factory.UmlTypeFactory;
import uml.structure.*;


public final class PascalTypes {

    public static final Type STRING = UmlTypeFactory.create("string");
    public static final Type CHAR = UmlTypeFactory.create("char");
    public static final Type REAL = UmlTypeFactory.create("real");
    public static final Type INTEGER = UmlTypeFactory.create("integer");
    public static final Type BOOLEAN = UmlTypeFactory.create("boolean");

    private PascalTypes() {
    }

}
