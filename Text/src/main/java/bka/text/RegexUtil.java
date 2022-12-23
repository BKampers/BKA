/*
** © Bart Kampers
*/

package bka.text;


public class RegexUtil {

    public static String namedString(String name) {
        return named(name, ".+");
    }


    public static String namedCharacter(String name, String characters) {
        return "(" + regexName(name) + "[" + characters + "])";
    }

    public static String namedOption(String name, String... options) {
        StringBuilder builder = new StringBuilder();
        for (String option : options) {
            if (builder.length() > 0) {
                builder.append('|');
            }
            builder.append(option);
        }
        builder.insert(0, "(" + regexName(name)).append(")");
        return builder.toString();
    }

    public static String namedWord(String name, int length) {
        return "(" + regexName(name) + "\\w{" + length + "})";
    }

    public static String optionalNamedDecimalGroup(String separator, String name) {
        return optionalNamedDecimalGroup(separator, name, "");
    }

    public static String optionalNamedDecimalGroup(String separator, String name, String post) {
        return "(" + separator + namedDecimalGroup(name) + post + ")?";
    }


    public static String namedDecimalGroup(String separator, String name) {
        return separator + "(" + regexName(name) + "\\d+)";
    }

    public static String namedDecimalGroup(String name) {
        return "(" + regexName(name) + "\\d+)";
    }

    public static String named(String name, String pattern) {
        return "(" + regexName(name) + pattern + ")";
    }

    private static String regexName(String name) {
        return "?<" + name + ">";
    }

}
