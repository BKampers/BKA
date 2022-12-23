/*
** © Bart Kampers
*/

package bka.text.parser;


public class Symbol {

    public Symbol(String expression) {
        this(expression, false);
    }

    public Symbol(String expression, boolean isRegex) {
        this.identifier = expression;
        this.isRegex = isRegex;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isRegex() {
        return isRegex;
    }

    @Override
    public String toString() {
        return identifier;
    }

    private final String identifier;
    private final boolean isRegex;
}
