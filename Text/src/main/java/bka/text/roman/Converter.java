/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.text.roman;

import java.util.*;
import java.util.regex.*;

/**
 * Converts between integers and Roman number strings
 */
public class Converter {

    /**
     * Converts a number in the range [1, 3999] to a standardized roman number.
     * @param number integer to convert
     * @return roman number
     * @throws IllegalArgumentException if digital out of range
     */
    public String standard(int number) {
        if ((1 <= number) && (number <= 3999)) {
            StringBuilder roman = new StringBuilder();
            int exponent = 0;
            while (number > 0) {
                int digit = number % 10;
                roman = convertDigit(digit, exponent).append(roman);
                exponent++;
                number /= 10;
            }
            return roman.toString();
        }
        else {
            throw new IllegalArgumentException(Integer.toString(number) + " cannot be converted to regular Roman");
        }
    }
    
    /**
     * Convert any positive number to a roman number divided in particals of standard roman numbers
     * representing the thousands groups.
     * @param  number to convert
     * @return array of particals with least significant at index 0
     * @throws IlleagalArgumentException if number is 0 or negative
     */
    public String[] large(int number) {
        if (number >= 1) {
            List<String> particals = new ArrayList<>();
            while (number > 0) {
                int partical = number % 1000;
                if (partical > 0) {
                    particals.add(standard(partical));
                }
                else {
                    particals.add(new String());
                }
                number /= 1000;
            }
            String[] roman = new String[particals.size()];
            return particals.toArray(roman);
        }
        else {
            throw new IllegalArgumentException(Integer.toString(number) + " cannot be converted to Roman");
        }    
    }
    
    /**
     * Convert a roman number to a long value. 
     * Numbers Brackets can be used to construct large numbers.
     * Accepts standardized roman numers as well as non standardized numbers.
     * Examples: I       =     1
     *           II      =     2
     *           IIII    =     4
     *           IV      =     4
     *           XLIX    =    49 (standardized)
     *           IL      =    49 (non standardized)
     *           D       =   500
     *           IƆ      =   500
     *           M       =  1000
     *           CIƆ     =  1000
     *           CIƆI    =  1001
     *           CIƆƆ    =  1500
     *           CIƆIƆ   =  1500
     *           ICIƆCIƆ =  1999
     *           CIƆCIƆ  =  2000
     *           CCIƆƆ   = 10000
     *           CCIƆƆƆ  = 10500
     *           CCIƆƆƆƆ = 15000
     * @param roman number to convert, numbers betwee
     * @return long value of roman
     * @throws NumberFormatException if roman is not a valid Roman number
     */
    public long parseLong(String roman) {
        if (roman.isEmpty()) {
            throw new NumberFormatException("Roman number must not be empty");
        }
        List<Long> values = createValueList(roman);
        long value = 0; 
        for (long v : values) {
            value += v;
        }
        return value;
    }
    
    /**
     * @see parseLong
     * @param roman number to convert
     * @return integer value of roman
     * @throws NumberFormatException if roman is not a valid Roman number
     * @throws IllegalArgumentException if roman is too large for an int
     */
    public int parseInt(String roman) {
        long value = parseLong(roman);
        if (value > Integer.MAX_VALUE) {
            throw new java.lang.IllegalArgumentException(roman + " is too large for int");
        }
        return (int) value;
    }
    
    private List<Long> createValueList(String roman) {
        List<Long> values = new ArrayList<>();
        Scanner scanner = new Scanner(roman);
        Symbol previousSymbol = null;
        while (scanner.hasNext()) { 
            Symbol symbol = scanner.next();
            if (previousSymbol == null || symbol.exponent < previousSymbol.exponent) {
                values.add(symbol.value);
            }
            else if (previousSymbol.value < symbol.value) {
                int valueIndex = values.size() - 1;
                values.set(valueIndex, symbol.value - values.get(valueIndex));
            }
            else {
                int valueIndex = values.size() - 1;
                values.set(valueIndex, values.get(valueIndex) + symbol.value);
            }
            previousSymbol = symbol;
        }
        return values;
    }

    private class Symbol {
        
        Symbol(long value, int exponent) {
            this.value = value;
            this.exponent = exponent;
        }
        
        final long value;
        final int exponent;
        
    }
    
    
    private class Scanner {
        
        public Scanner(String source) {
            Pattern pattern = Pattern.compile("C*IƆ+");
            matcher = pattern.matcher(source);
            this.source = source;
            matchIndex = (matcher.find()) ? matcher.start() : -1;
        }
        
        public boolean hasNext() {
            return index < source.length();
        }
        
        public Symbol next() {
            if (index == matchIndex) {
                return groupSymbol();
            }
            return characterSymbol();
        }

        private Symbol characterSymbol() {
            Symbol symbol = symbol(source.charAt(index));
            index++;
            return symbol;
        }
        
        private Symbol symbol(char ch) {
            int factor = 1;
            for (int exponent = 0; exponent < SYMBOLS.length; ++exponent) {
                char[] symbols = SYMBOLS[exponent];
                for (int i = 0; i < symbols.length; ++i) {
                    if (symbols[i] == ch) {
                        long value = ((i == 0) ? 1 : 5) * factor;
                        return new Symbol(value, exponent);
                    }
                }
                factor *= 10;
            }
            throw new NumberFormatException("Invalid character '" + ch + "'");
        }
        
        private Symbol groupSymbol() {
            Symbol symbol = symbol(matcher.group());
            index = matcher.end();
            matchIndex = matcher.find() ? matcher.start() : -1;
            return symbol;
        }
        
        private Symbol symbol(String group) {
            long value = 0;
            int openCount = group.indexOf('I');
            int closeCount = group.length() - openCount - 1;
            if (closeCount < openCount) {
                throw new NumberFormatException("Invalid number of closing brackets");
            }
            if (openCount > 0) {
                value += 1000 * (long) Math.pow(10, openCount - 1);
            }
            if (closeCount - openCount > 0) {
                value += 500 * (long) Math.pow(10, closeCount - openCount - 1);
            }
            return new Symbol(value, openCount + 2);
        }
        
        private final Matcher matcher;
        private final String source;
        private int matchIndex;
        private int index;
        
    }

    /**
     * Convert one digit to a roman number accounting given exponent
     * @param digit [0, 9] to convert
     * @param exponent of the digit 0, 1 or 2 
     * @return roman number representing the digit
     */
    private StringBuilder convertDigit(int digit, int exponent) {
        StringBuilder roman = new StringBuilder();
        if (digit >= 4) {
            if (digit == 4 || digit == 9) {
                // start with 1-symbol
                roman.append(SYMBOLS[exponent][INDEX_1]);
            }
            if (digit != 9) {
                // append 5-symbol 
                roman.append(SYMBOLS[exponent][INDEX_5]);
                digit -= 5;
            }
            else {
                // append 10-symbol, that is the 1-symbol of the next exponent
                roman.append(SYMBOLS[exponent+1][INDEX_1]);
                digit = 0;
            }
        }
        while (digit > 0) {
            // append up to three 1-symbols
            roman.append(SYMBOLS[exponent][INDEX_1]);
            digit--;
        }
        return roman;
    }
    
    
    private static final char[][] SYMBOLS = {{'I', 'V'}, {'X', 'L'}, {'C', 'D'} , {'M'}};
    
    private static final int INDEX_1 = 0;
    private static final int INDEX_5 = 1;
    
}
