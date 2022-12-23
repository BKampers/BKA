/*
 * © Bart Kampers
 */
package bka.text;

import java.util.regex.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.*;

public class RegexUtilTest {


    @Test
    public void testNamedCharacter() {
        Pattern pattern = Pattern.compile(RegexUtil.namedCharacter(NAME, "abc"));
        assertFind(pattern, "a");
        assertFind(pattern, "b");
        assertFind(pattern, "c");
        assertNotFind(pattern, "d");
    }

    @Test
    public void testNamedWord() {
        Pattern pattern = Pattern.compile(RegexUtil.namedWord(NAME, 3));
        assertFind(pattern, "abc");
        assertNotFind(pattern, "ab");
        assertNotMatches(pattern, "abcd");
    }

    @Test
    public void testNamedOption() {
        Pattern pattern = Pattern.compile(RegexUtil.namedOption(NAME, "abc", "def", "ghi"));
        assertFind(pattern, "abc");
        assertFind(pattern, "def");
        assertFind(pattern, "ghi");
        assertNotFind(pattern, "jkl");
        assertNotFind(pattern, "a");
        assertNotFind(pattern, "ab");
    }

    @Test
    public void testNamedDecimalGroup() {
        Pattern pattern = Pattern.compile(RegexUtil.namedDecimalGroup(NAME));
        assertFind(pattern.matcher("abc456ghi"), "456");
        assertNotFind(pattern, "abcdefghi");
    }

    @Test
    public void testNamedDecimalGroupPrefix() {
        Pattern pattern = Pattern.compile(RegexUtil.namedDecimalGroup(":", NAME));
        assertMatches(pattern.matcher(":34"), "34");
        assertNotMatches(pattern.matcher(",34"));
    }

    @Test
    public void testOptionalNamedDecimalGroup() {
        Pattern pattern = Pattern.compile("\\d*" + RegexUtil.optionalNamedDecimalGroup(":", NAME));
        assertMatches(pattern.matcher("12:34"), "34");
        assertNotMatches(pattern.matcher("12,34"));
    }

    @Test
    public void testOptionalNamedDecimalGroupPostfix() {
        Pattern pattern = Pattern.compile("\\d*" + RegexUtil.optionalNamedDecimalGroup(":", NAME, "'"));
        assertMatches(pattern.matcher("12:34'"), "34");
        assertNotMatches(pattern.matcher("12:34"));
        assertNotMatches(pattern.matcher("12,34'"));
    }

    private static void assertFind(Pattern pattern, String string) {
        assertFind(pattern.matcher(string), string);
    }

    private static void assertNotFind(Pattern pattern, String string) {
        assertNotFind(pattern.matcher(string), string);
    }

    private static void assertFind(Matcher matcher, String string) {
        assertTrue(matcher.find());
        assertEquals(string, matcher.group(NAME));
    }

    private static void assertNotFind(Matcher matcher, String string) {
        assertFalse(matcher.find());
    }

    private static void assertMatches(Matcher matcher, String string) {
        assertTrue(matcher.matches());
        assertEquals(string, matcher.group(NAME));
    }

    private static void assertNotMatches(Pattern pattern, String string) {
        assertNotMatches(pattern.matcher(string));
    }

    private static void assertNotMatches(Matcher matcher) {
        assertFalse(matcher.matches());
    }

    private static final String NAME = "name";
}
