/*
 * © Bart Kampers
 */
package bka.text;

import java.math.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.*;

public class FormatterUtilTest {


    @Test
    public void testIntFormat() {
        assertEquals("0", FormatterUtil.format("%d", new TestNumber(0)));
        assertEquals(" 0", FormatterUtil.format("%2d", 0));
        assertEquals("a", FormatterUtil.format("%h", new TestNumber(10)));
        assertEquals("A", FormatterUtil.format("%H", new TestNumber(10)));
    }


    @Test
    public void testDecoratedFormat() {
        assertEquals("1%", FormatterUtil.format("%d%%", 1));
        assertEquals("€ 2,50", FormatterUtil.format("€ %.2f", new Locale("nl"), 2.5));
        assertEquals("'0'", FormatterUtil.format("'%d'", 0));
    }

    @Test
    public void testFloatFormat() {
        assertEquals("123", FormatterUtil.format("%.0f", Locale.ENGLISH, 123.456));
        assertEquals("123.5", FormatterUtil.format("%5.1f", Locale.ENGLISH, 123.456));
        assertTrue(FormatterUtil.format("%f", Locale.ENGLISH, 123.456).contains("123.456"));
        assertTrue(FormatterUtil.format("%7f", Locale.ENGLISH, 123.456).contains("123.456"));
        assertEquals("1e-05", FormatterUtil.format("%.0g", new BigDecimal("1e-05")));
        assertEquals("1E-05", FormatterUtil.format("%.0G", new BigDecimal("1e-05")));
        assertEquals("0e+00", FormatterUtil.format("%.0e", 0));
        assertEquals("0E+00", FormatterUtil.format("%.0E", 0));
    }


    @Test
    public void testFloatToIntFormat() {
        assertEquals("0", FormatterUtil.format("%d", BigDecimal.ZERO));
        assertEquals("a", FormatterUtil.format("%x", 10.0));
        assertEquals("A", FormatterUtil.format("%X", 10.0));
        assertEquals("10", FormatterUtil.format("%o", 8.0f));
    }


    @Test
    public void testIntToFloatFromat() {
        assertEquals("1", FormatterUtil.format("%.0f", Locale.ENGLISH, (byte) 1));
        assertEquals("1.0", FormatterUtil.format("%.1f", Locale.ENGLISH, (short) 1));
        assertEquals("1.00", FormatterUtil.format("%.2f", Locale.ENGLISH, 1));
        assertEquals("1.000", FormatterUtil.format("%.3f", Locale.ENGLISH, 1L));
        assertEquals("1.0000", FormatterUtil.format("%.4f", Locale.ENGLISH, BigInteger.ONE));
        assertEquals("1e+05", FormatterUtil.format("%.0g", 100000));
        assertEquals("1E+05", FormatterUtil.format("%.0G", 100000));
        assertEquals("0x0.0p0", FormatterUtil.format("%a", 0));
        assertEquals("0X0.0P0", FormatterUtil.format("%A", 0));
    }


    @Test
    public void testBooleanFormat() {
        assertEquals("false", FormatterUtil.format("%b", null));
        assertEquals("FALSE", FormatterUtil.format("%B", null));
        assertEquals("true", FormatterUtil.format("%b", new TestNumber(0)));
        assertEquals("TRUE", FormatterUtil.format("%B", new TestNumber(0)));
    }


    @Test
    public void testTextFormat() {
        assertEquals("TestNumber: 0", FormatterUtil.format("%s", new TestNumber(0)));
        assertEquals("TESTNUMBER: 0", FormatterUtil.format("%S", new TestNumber(0)));
        assertEquals("\0", FormatterUtil.format("%c", 0.0f));
        assertEquals("A", FormatterUtil.format("%C", (double) 'a'));
    }


    @Test
    public void testDateFormat() {
        assertEquals("1970-01-01", FormatterUtil.format("YYYY-MM-dd", 0));
        assertEquals("Jan 1", FormatterUtil.format("MMM d", Locale.ENGLISH, 0));
        assertEquals("1 jan", FormatterUtil.format("d MMM", new Locale("nl"), 0));
    }


    @Test
    public void testNullPointers() {
        assertEquals("null", FormatterUtil.format(null, null));
        assertEquals("null", FormatterUtil.format("%s", null));
        assertEquals("NULL", FormatterUtil.format("%C", null));
        assertEquals("TestNumber: 0", FormatterUtil.format(null, new TestNumber(0)));
    }


    private class TestNumber extends Number {

        TestNumber(int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }

        @Override
        public long longValue() {
            return intValue();
        }

        @Override
        public float floatValue() {
            return intValue();
        }

        @Override
        public double doubleValue() {
            return intValue();
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof TestNumber && value == ((TestNumber) obj).value);
        }

        @Override
        public String toString() {
            return "TestNumber: " + value;
        }

        private final int value;

    }
}
