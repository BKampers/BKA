package run;


import java.util.*;
import java.util.stream.*;


public enum Operator {

    ADDITION("\\+"),
    SUBTRACTION("\\-"),
    MULTIPLICATION("\\*"),
    REAL_DIVISION("\\/"),
    INTEGER_DIVISION("DIV\\b"),
    MODULUS("MOD\\b"),
    AND("AND\\b"),
    OR("OR\\b"),
    XOR("XOR\\b"),
    EQUALS("\\="),
    UNEQUALS("\\<\\>"),
    LESS_THAN("\\<"),
    LESS_EQUAL("\\<\\="),
    GREATER_THAN("\\>"),
    GREATER_EQUAL("\\>\\=");

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public static Operator lookup(String symbol) {
        return Arrays.stream(values())
            .filter(operator -> symbol.equals(operator.symbol))
            .findAny()
            .orElseThrow(() -> new NoSuchElementException(symbol));
    }

    private final String symbol;

}
