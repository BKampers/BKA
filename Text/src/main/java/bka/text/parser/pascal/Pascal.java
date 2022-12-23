/*
** © Bart Kampers
*/

package bka.text.parser.pascal;


public class Pascal {

    public static final String IDENTIFIER_REGEX = "[A-Za-z_]+\\w*";
    public static final String INTEGER_LITERAL_REGEX = "[-+]?\\d+";
    public static final String REAL_LITERAL_REGEX = "[-+]?((((\\d*\\.\\d+)|(\\d+\\.\\d*))([eE][-+]?\\d+)?)|(\\d+[eE][-+]?\\d+))";
    public static final String STRING_LITERAL_REGEX = "'.*'";
    public static final String OPERATOR_REGEX = "=|\\^|div|mod|\\*|\\/|\\+|\\-|and|or|xor";

}
