package uml.structure;

import java.util.*;


public interface Operation extends Member {

    boolean isAbstract();

    List<Parameter> getParameters();

}
