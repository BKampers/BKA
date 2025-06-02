package uml.structure;


public interface Member extends Typed {

    public enum Visibility {

        PUBLIC('+'),
        PROTECTED('#'),
        PACKAGE('~'),
        PRIVATE('-');

        Visibility(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol()  {
            return symbol;
        }

        private final char symbol;
    }

    Type getOwner();

    Visibility getVisibility();

    boolean isClassScoped();
    
}
