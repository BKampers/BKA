package bka.text.roman;

/*
 * © Bart Kampers
 */

import java.text.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.*;

public class RomanFormatTest {

    @Test
    public void test1() {
        RomanNumberFormat formatter = new RomanNumberFormat();
        String one = formatter.format(1);
        assertEquals("I", one);
    }

    @Test
    public void testFieldPosition() {
        RomanNumberFormat formatter = new RomanNumberFormat();
        StringBuffer pattern = new StringBuffer("one: ");
        StringBuffer one = formatter.format(1, pattern, new FieldPosition(5));
        assertEquals("one: I", one.toString());
    } 

}