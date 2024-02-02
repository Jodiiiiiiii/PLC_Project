package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LexerTests {

    @ParameterizedTest
    @MethodSource
    void testIdentifier(String test, String input, boolean success) {
        test(input, Token.Type.IDENTIFIER, success);
    }

    private static Stream<Arguments> testIdentifier() {
        return Stream.of(
                Arguments.of("Alphabetic", "getName", true),
                Arguments.of("Alphanumeric", "thelegend27", true),
                Arguments.of("Leading Hyphen", "-five", false),
                Arguments.of("Leading Digit", "1fish2fish3fishbluefish", false),
                Arguments.of("Leading @", "@getName", true),
                Arguments.of("Only symbols - but valid", "@__--__-", true),
                Arguments.of("@ Within", "get@Name", false),
                Arguments.of("Double @", "@@getName", false),
                Arguments.of("Leading Underscore", "_getName", false), // not permitted in grammar (strange)
                Arguments.of("Underscores", "___", false),
                Arguments.of("Single Character & Underscores", "g___", true),
                Arguments.of("Single Character", "g", true),
                Arguments.of("Single Character - Underscore", "_", false),
                Arguments.of("Single Character - @", "@", true),
                Arguments.of("Hyphenated", "a-b-c", true),
                Arguments.of("Trailing Hyphen", "five-", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testInteger(String test, String input, boolean success) {
        test(input, Token.Type.INTEGER, success);
    }

    private static Stream<Arguments> testInteger() {
        return Stream.of(
                Arguments.of("Single Digit", "1", true),
                Arguments.of("Multiple Digits", "12345", true),
                Arguments.of("Signed", "-1", true),
                Arguments.of("Leading Zero", "01", false),
                Arguments.of("Zero", "0", true),
                Arguments.of("Negative Zero", "-0", false),
                Arguments.of("Multiple Zeroes", "000", false),
                Arguments.of("Trailing Zeros", "100", true),
                Arguments.of("Decimal", "123.456", false),
                Arguments.of("Comma Separated", "1,234", false),
                Arguments.of("Leading Zeros", "007", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDecimal(String test, String input, boolean success) {
        test(input, Token.Type.DECIMAL, success);
    }

    private static Stream<Arguments> testDecimal() {
        return Stream.of(
                Arguments.of("Multiple Digits", "123.456", true),
                Arguments.of("Negative Decimal", "-1.0", true),
                Arguments.of("Trailing Decimal", "1.", false),
                Arguments.of("Leading Decimal", ".5", false),
                Arguments.of("Negative Leading Decimal", "-.5", false),
                Arguments.of("Negative Trailing Decimal", "-1.", false),
                Arguments.of("Multiple Leading", "123.0", true),
                Arguments.of("Multiple Trailing", "1.234", true),
                Arguments.of("Integer (No Decimal Point)", "1234", false),
                Arguments.of("Single Digit", "1", false),
                Arguments.of("Double Decimal", "1..0", false),
                // Zeros
                Arguments.of("Zero Left Side", "0.5", true),
                Arguments.of("Double Zero Left Side", "00.5", false),
                Arguments.of("Leading Zero Multiple Digits", "01.5", false),
                Arguments.of("Positive Zero", "0.0", true),
                Arguments.of("Negative Zero", "-0.0", true),
                Arguments.of("Extra Trailing Zeros", "0.0000000", true),
                Arguments.of("Extra Leading Zeros", "00000000.0", false),
                Arguments.of("Valid Zeros", "10000000.00000000", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testCharacter(String test, String input, boolean success) {
        test(input, Token.Type.CHARACTER, success);
    }

    private static Stream<Arguments> testCharacter() {
        return Stream.of(
                // Valid - Standard
                Arguments.of("Alphabetic", "'c'", true),
                Arguments.of("Numeric", "'0'", true),
                // Formatting Errors
                Arguments.of("No Quotes", "c", false),
                Arguments.of("No Leading Quote", "c'", false),
                Arguments.of("No Trailing Quote", "'c", false),
                Arguments.of("Only One Quote", "'", false),
                Arguments.of("Empty", "''", false),
                Arguments.of("Multiple", "'abc'", false),
                // Escapes
                Arguments.of("Escape - \\b", "'\\b'", true),
                Arguments.of("Escape - \\n", "'\\n'", true),
                Arguments.of("Escape - \\r", "'\\r'", true),
                Arguments.of("Escape - \\t", "'\\t'", true),
                Arguments.of("Invalid Escape Char", "'\\p'", false),
                Arguments.of("Escape - Single Quote", "'\\''", true),
                Arguments.of("Non-Escaped Single Quote", "'''", false),
                Arguments.of("Escape - Double Quote", "'\\\"'", true), // redundant, but valid
                Arguments.of("Non-Escaped Double Quote", "'\"'", true), // also valid
                Arguments.of("Escape - Backslash", "'\\\\'", true),
                Arguments.of("Non-Escaped Backslash", "'\\'", false),
                // Whitespace characters
                Arguments.of("Newline", "'\n'", false), // not permitted in grammar
                Arguments.of("Return", "'\r'", false), // not permitted in grammar
                Arguments.of("Backspace", "'\b'", true), // FINE
                Arguments.of("Tab", "'\t'", true), // FINE
                Arguments.of("Space", "' '", true), // FINE
                // Form Feed // Vertical Tab
                Arguments.of("Form feed", "'\f'", true), // FINE
                Arguments.of("Vertical Tab", "'\u000B'", true) // FINE
        );
    }

    @ParameterizedTest
    @MethodSource
    void testString(String test, String input, boolean success) {
        test(input, Token.Type.STRING, success);
    }

    private static Stream<Arguments> testString() {
        return Stream.of(
                // Various Valid Strings
                Arguments.of("Alphabetic", "\"abc\"", true),
                Arguments.of("Numeric", "\"123\"", true),
                Arguments.of("Single Character", "\"a\"", true),
                Arguments.of("Single Number", "\"1\"", true),
                Arguments.of("Mixed Characters and Numbers", "\"a1b2c3d4\"", true),
                Arguments.of("Symbols", "\"#@%$!*()(*#%@&\"", true),
                Arguments.of("Alphanumeric + Symbols", "\"abc123^&$\"", true),
                Arguments.of("Empty", "\"\"", true),
                // Formatting Errors
                Arguments.of("No Quotes", "abc", false),
                Arguments.of("No Leading Quote", "abc\"", false),
                Arguments.of("No Trailing Quote", "\"abc", false),
                Arguments.of("Only One Quote", "\"", false),
                Arguments.of("Alphabetic", "\"abc\"", true),
                // Escapes
                Arguments.of("Escape - \\b", "\"ab\\bcd\"", true),
                Arguments.of("Escape - \\n", "\"ab\\ncd\"", true),
                Arguments.of("Escape - \\r", "\"ab\\rcd\"", true),
                Arguments.of("Escape - \\t", "\"ab\\tcd\"", true),
                Arguments.of("Invalid Escape Char", "\"ab\\zcd\"", false),
                Arguments.of("Escape - Single Quote", "\"ab\\'cd\"", true), // Redundant, but valid
                Arguments.of("Non-Escaped Single Quote", "\"ab'cd\"", true), // also valid
                Arguments.of("Escape - Double Quote", "\"ab\\\"cd\"", true),
                Arguments.of("Non-Escaped Double Quote", "\"ab\"cd\"", false),
                Arguments.of("Escape - Backslash", "\"ab\\\\cd\"", true),
                Arguments.of("Non-escaped Backslash", "\"ab\\cd\"", false),
                // Whitespace Characters
                Arguments.of("Newline", "\"ab\ncd\"", false), // not permitted in grammar
                Arguments.of("Return", "\"ab\rcd\"", false), // not permitted in grammar
                Arguments.of("Backspace", "\"ab\bcd\"", true), // FINE
                Arguments.of("Tab", "\"ab\tcd\"", true), // FINE
                Arguments.of("Space", "\"abcd\"", true),
                Arguments.of("Many Spaces", "\"ab       cd\"", true),
                Arguments.of("Only Space", "\" \"", true),
                Arguments.of("Only Many Spaces", "\"          \"", true),
                // Provided
                Arguments.of("Newline Escape", "\"Hello,\\nWorld\"", true),
                Arguments.of("Newline in Java", "\"Hello,\nWorld\"", false),
                Arguments.of("Unterminated", "\"unterminated", false),
                Arguments.of("Invalid Escape", "\"invalid\\escape\"", false),
                // Form Feed // Vertical Tab
                Arguments.of("Form feed", "\"Hello\fWorld\"", true), // FINE
                Arguments.of("Vertical Tab", "\"Hello\u000BWorld\"", true) // FINE

        );
    }

    @ParameterizedTest
    @MethodSource
    void testOperator(String test, String input, boolean success) {
        //this test requires our lex() method, since that's where whitespace is handled.
        test(input, List.of(new Token(Token.Type.OPERATOR, input, 0)), success);
        System.out.println("Return1123123 \r Return2 |");
    }

    private static Stream<Arguments> testOperator() {
        return Stream.of(
                // Conjunction Operators
                Arguments.of("Not Equals", "!=", true),
                Arguments.of("Equals", "==", true),
                Arguments.of("And", "&&", true),
                Arguments.of("Or", "||", true),
                // Invalid Conjunctions
                Arguments.of("Not Equals and Equals", "!==", false),
                Arguments.of("Many Equals", "===", false),
                Arguments.of("Or And", "|&", false),
                Arguments.of("Random", "*#", false),
                // Non-operators
                Arguments.of("Alphabetic", "a", false), // identifier
                Arguments.of("Numerical", "1", false), // integer
                Arguments.of("Character", "'", false), // start of (invalid) character
                Arguments.of("String", "\"", false), // start of (invalid) string
                Arguments.of("@ Symbol", "@", false), // this is technically an identifier
                // Valid single character
                Arguments.of("Plus", "+", true),
                Arguments.of("Open Parentheses", "(", true),
                Arguments.of("Close Parentheses", ")", true),
                Arguments.of("Semicolon", ";", true),
                Arguments.of("Dollar Sign", "$", true),
                Arguments.of("Single Equals", "=", true),
                Arguments.of("Exclamation Point", "!", true),
                Arguments.of("Single Or", "|", true),
                Arguments.of("Single And", "&", true),
                // Whitespace characters (should be ignored)
                Arguments.of("Space", " ", false),
                Arguments.of("Backspace", "\b", false),
                Arguments.of("Newline", "\n", false),
                Arguments.of("Return", "\r", false),
                Arguments.of("Tab", "\t", false)

        );
    }

    @ParameterizedTest
    @MethodSource
    void testExamples(String test, String input, List<Token> expected) {
        test(input, expected, true);
    }

    private static Stream<Arguments> testExamples() {
        return Stream.of(
                Arguments.of("Example 1 - given", "LET x = 5;", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "LET", 0),
                        new Token(Token.Type.IDENTIFIER, "x", 4),
                        new Token(Token.Type.OPERATOR, "=", 6),
                        new Token(Token.Type.INTEGER, "5", 8),
                        new Token(Token.Type.OPERATOR, ";", 9)
                )),
                Arguments.of("Example 2 - given", "print(\"Hello, World!\");", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "print", 0),
                        new Token(Token.Type.OPERATOR, "(", 5),
                        new Token(Token.Type.STRING, "\"Hello, World!\"", 6),
                        new Token(Token.Type.OPERATOR, ")", 21),
                        new Token(Token.Type.OPERATOR, ";", 22)
                )),
                Arguments.of("Example 3 - int operator identifier", "1||one", Arrays.asList(
                        new Token(Token.Type.INTEGER, "1", 0),
                        new Token(Token.Type.OPERATOR, "||", 1),
                        new Token(Token.Type.IDENTIFIER, "one", 3)
                )),
                Arguments.of("Example 4 - spaces", "spaces   123", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "spaces", 0),
                        new Token(Token.Type.INTEGER, "123", 9)
                )),
                Arguments.of("Example 5 - whitespaces", "whitespaces \b\n\r\tafter", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "whitespaces", 0),
                        new Token(Token.Type.IDENTIFIER, "after", 16)
                )),
                Arguments.of("Example 6 - Trailing Newline", "1.23 token\n", Arrays.asList(
                        new Token(Token.Type.DECIMAL, "1.23", 0),
                        new Token(Token.Type.IDENTIFIER, "token", 5)
                )),
                Arguments.of("Example 7 - Multiple Decimals", "1.2.3", Arrays.asList(
                        new Token(Token.Type.DECIMAL, "1.2", 0),
                        new Token(Token.Type.OPERATOR, ".", 3),
                        new Token(Token.Type.INTEGER, "3", 4)
                )),
                Arguments.of("Example 8 - Equals Combination", "!====", Arrays.asList(
                        new Token(Token.Type.OPERATOR, "!=", 0),
                        new Token(Token.Type.OPERATOR, "==", 2),
                        new Token(Token.Type.OPERATOR, "=", 4)
                )),
                Arguments.of("Example 9 - Weird Quotes", "'\"'string\"'\"", Arrays.asList(
                        new Token(Token.Type.CHARACTER, "'\"'", 0),
                        new Token(Token.Type.IDENTIFIER, "string", 3),
                        new Token(Token.Type.STRING, "\"'\"", 9)
                )),
                Arguments.of("Example 10 - Both Negative Sign Meanings", "-1.0--2", Arrays.asList(
                        new Token(Token.Type.DECIMAL, "-1.0", 0),
                        new Token(Token.Type.OPERATOR, "-", 4),
                        new Token(Token.Type.INTEGER, "-2", 5)
                )),
                Arguments.of("Example 11 - All Types", "float x = 0.123 + 1; string str = 'a'\b|\"bc\"   &&", Arrays.asList(
                        new Token(Token.Type.IDENTIFIER, "float", 0),
                        new Token(Token.Type.IDENTIFIER, "x", 6),
                        new Token(Token.Type.OPERATOR, "=", 8),
                        new Token(Token.Type.DECIMAL, "0.123", 10),
                        new Token(Token.Type.OPERATOR, "+", 16),
                        new Token(Token.Type.INTEGER, "1", 18),
                        new Token(Token.Type.OPERATOR, ";", 19),
                        new Token(Token.Type.IDENTIFIER, "string", 21),
                        new Token(Token.Type.IDENTIFIER, "str", 28),
                        new Token(Token.Type.OPERATOR, "=", 32),
                        new Token(Token.Type.CHARACTER, "'a'", 34),
                        new Token(Token.Type.OPERATOR, "|", 38),
                        new Token(Token.Type.STRING, "\"bc\"", 39),
                        new Token(Token.Type.OPERATOR, "&&", 46)

                )),
                Arguments.of("Example 11 - -.0foo", "-.0foo", Arrays.asList(
                        new Token(Token.Type.OPERATOR, "-", 0),
                        new Token(Token.Type.OPERATOR, ".", 1),
                        new Token(Token.Type.INTEGER, "0", 2),
                        new Token(Token.Type.IDENTIFIER, "foo", 3)
                )),
                Arguments.of("Example 12 - Form Feed and Vertical Tab", "\"HelloWorld\" \r\b\f\nHello\fWorld \u000B", Arrays.asList(
                        new Token(Token.Type.STRING, "\"HelloWorld\"", 0),
                        new Token(Token.Type.OPERATOR, "\f", 15),
                        new Token(Token.Type.IDENTIFIER, "Hello", 17),
                        new Token(Token.Type.OPERATOR, "\f", 22),
                        new Token(Token.Type.IDENTIFIER, "World", 23),
                        new Token(Token.Type.OPERATOR, "\u000B", 29)
                ))
        );
    }

    // unterminated string
    @Test
    void testException1() {
        ParseException exception = Assertions.assertThrows(ParseException.class,
                () -> new Lexer("\"unterminated").lex());
        Assertions.assertEquals(13, exception.getIndex());
    }

    // unterminated character
    @Test
    void testException2() {
        ParseException exception = Assertions.assertThrows(ParseException.class,
                () -> new Lexer("'u").lex());
        Assertions.assertEquals(2, exception.getIndex());
    }

    // invalid escape - string
    @Test
    void testException3() {
        ParseException exception = Assertions.assertThrows(ParseException.class,
                () -> new Lexer("\"invalid\\escape\"").lex());
        Assertions.assertEquals(9, exception.getIndex());
    }

    // invalid escape - character
    @Test
    void testException4() {
        ParseException exception = Assertions.assertThrows(ParseException.class,
                () -> new Lexer("'\\e'").lex());
        Assertions.assertEquals(2, exception.getIndex());
    }

    // string spanning multiple lines
    @Test
    void testException5() {
        ParseException exception = Assertions.assertThrows(ParseException.class,
                () -> new Lexer("\"string spanning \n multiple lines\"").lex());
        Assertions.assertEquals(17, exception.getIndex());
    }

    // character spanning multiple lines
    @Test
    void testException6() {
        ParseException exception = Assertions.assertThrows(ParseException.class,
                () -> new Lexer("'\n'").lex()); // was \r - but this is actually FINE
        Assertions.assertEquals(1, exception.getIndex());
    }

    /**
     * Tests that lexing the input through {@link Lexer#lexToken()} produces a
     * single token with the expected type and literal matching the input.
     */
    private static void test(String input, Token.Type expected, boolean success) {
        try {
            if (success) {
                Assertions.assertEquals(new Token(expected, input, 0), new Lexer(input).lexToken());
            } else {
                Assertions.assertNotEquals(new Token(expected, input, 0), new Lexer(input).lexToken());
            }
        } catch (ParseException e) {
            Assertions.assertFalse(success, e.getMessage());
        }
    }

    /**
     * Tests that lexing the input through {@link Lexer#lex()} matches the
     * expected token list.
     */
    private static void test(String input, List<Token> expected, boolean success) {
        try {
            if (success) {
                Assertions.assertEquals(expected, new Lexer(input).lex());
            } else {
                Assertions.assertNotEquals(expected, new Lexer(input).lex());
            }
        } catch (ParseException e) {
            Assertions.assertFalse(success, e.getMessage());
        }
    }

}
