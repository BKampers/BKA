/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans (including
** exploitation and discrimination), humanity, the environment or the
** universe.
*/
package bka.text.roman;

import java.text.*;


public class RomanNumberFormat extends NumberFormat {

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(Math.round(number), toAppendTo, pos);
    }
    
    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        if (number < Integer.MIN_VALUE || Integer.MAX_VALUE < number) {
            throw new IllegalArgumentException(Long.toString(number) + " out of int range");
        }
        String result = converter.standard((int) number);
        return toAppendTo.append(result);
    }

    @Override
    public Number parse(String source, ParsePosition parsePosition) {
        return converter.parseInt(source);
    }
    
    
    Converter converter = new Converter();

}
