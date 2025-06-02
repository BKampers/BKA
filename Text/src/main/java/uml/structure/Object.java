package uml.structure;

import java.util.*;


public interface Object extends Structural, Typed {

    Map<Attribute, Expression> getAttributeValues();

}
