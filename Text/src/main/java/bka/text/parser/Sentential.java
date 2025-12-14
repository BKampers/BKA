package bka.text.parser;

import java.util.*;

/**
 * Right hand side of a production rule. The symbols replace the left hand side when the ruleis applied.
 */
public class Sentential {

    /**
     * @param symbols
     * @return a sentential of given symbols
     */
    public static Sentential of(String... symbols) {
        return new Sentential(Arrays.asList(symbols));
    }

    /**
     * Constructs a sentential of given symbols.
     *
     * @param symbols
     * @throws IllegalArgumentException exception if one of the symbols is empty
     */
    public Sentential(List<String> symbols) {
        this.symbols = symbols.stream().map(Sentential::requireNonEmpty).toList();
    }

    private static String requireNonEmpty(String symbol) {
        if (symbol.isEmpty()) {
            throw new IllegalArgumentException("Empty symbol in sentential");
        }
        return symbol;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    /**
     * @return number of symbols in this sentential
     */
    public int length() {
        return symbols.size();
    }

    public boolean isErasing() {
        return symbols.isEmpty();
    }

    private final List<String> symbols;

}
