/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import java.util.regex.*;
import org.junit.*;
import static org.junit.Assert.*;

public class PascalTest {

    @Test
    public void identifierTest() {
        Pattern pattern = Pattern.compile(Pascal.IDENTIFIER_REGEX);
        assertTrue(pattern.matcher("A_123").matches());
        assertTrue(pattern.matcher("a").matches());
        assertTrue(pattern.matcher("_").matches());
        assertTrue(pattern.matcher("_1").matches());
        assertFalse(pattern.matcher("12A").matches());
    }

    @Test
    public void realLiteralTest() {
        Pattern pattern = Pattern.compile(Pascal.REAL_LITERAL_REGEX);
        assertTrue(pattern.matcher("0.1").matches());
        assertTrue(pattern.matcher("-23.45e+6").matches());
        assertTrue(pattern.matcher("+23.45e-6").matches());
        assertTrue(pattern.matcher(".7E8").matches());
        assertTrue(pattern.matcher("7.E8").matches());
        assertTrue(pattern.matcher("9e6").matches());
        assertTrue(pattern.matcher("-87E+6").matches());
        assertTrue(pattern.matcher("+87E-6").matches());
        assertFalse(pattern.matcher("1e1.1").matches());
        assertFalse(pattern.matcher("e1").matches());
        assertFalse(pattern.matcher("1E").matches());
        assertFalse(pattern.matcher("0").matches());
        assertFalse(pattern.matcher("1234567890").matches());
    }

    @Test
    public void stringLiteralTest() {
        Pattern pattern = Pattern.compile(Pascal.STRING_LITERAL_REGEX);
        assertTrue(pattern.matcher("'literal'").matches());
        assertFalse(pattern.matcher("literal").matches());
        assertFalse(pattern.matcher("'literal").matches());
        assertFalse(pattern.matcher("literal'").matches());
    }

}