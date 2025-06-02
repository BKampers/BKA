package uml.structure;

import java.util.*;


public interface Parameter extends Typed {

    public enum Direction { 

        IN, OUT, INOUT;
        
        @Override 
        public String toString() {
            return '[' + super.toString() + ']';
        }

    }

    Direction getDirection();

    Optional<Expression> getDefaultValue();

}
