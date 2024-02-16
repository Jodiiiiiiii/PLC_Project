package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Standard JUnit5 parameterized tests. See the RegexTests file from Homework 1
 * or the LexerTests file from the last project part for more information.
 */
final class ParserExpressionTests {

    @ParameterizedTest
    @MethodSource
    void testExpressionStatement(String test, List<Token> tokens, Ast.Statement.Expression expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testExpressionStatement() {
        return Stream.of(
                Arguments.of("Function Expression",
                        Arrays.asList(
                                //name();
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.OPERATOR, ")", 5),
                                new Token(Token.Type.OPERATOR, ";", 6)
                        ),
                        new Ast.Statement.Expression(new Ast.Expression.Function("name", Arrays.asList()))
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAssignmentStatement(String test, List<Token> tokens, Ast.Statement.Assignment expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testAssignmentStatement() {
        return Stream.of(
                Arguments.of("Assignment",
                        Arrays.asList(
                                //name = value;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.IDENTIFIER, "value", 7),
                                new Token(Token.Type.OPERATOR, ";", 12)
                        ),
                        new Ast.Statement.Assignment(
                                new Ast.Expression.Access(Optional.empty(), "name"),
                                new Ast.Expression.Access(Optional.empty(), "value")
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testLiteralExpression(String test, List<Token> tokens, Ast.Expression.Literal expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testLiteralExpression() {
        return Stream.of(
                Arguments.of("Null: NIL",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "NIL", 0)),
                        new Ast.Expression.Literal(null)
                ),
                Arguments.of("Boolean Literal: TRUE",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "TRUE", 0)),
                        new Ast.Expression.Literal(Boolean.TRUE)
                ),
                Arguments.of("Boolean Literal: FALSE",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "FALSE", 0)),
                        new Ast.Expression.Literal(Boolean.FALSE)
                ),
                Arguments.of("Integer Literal: Small",
                        Arrays.asList(new Token(Token.Type.INTEGER, "1", 0)),
                        new Ast.Expression.Literal(new BigInteger("1"))
                ),
                Arguments.of("Integer Literal: Big",
                        Arrays.asList(new Token(Token.Type.INTEGER, "123456789111", 0)),
                        new Ast.Expression.Literal(new BigInteger("123456789111"))
                ),
                Arguments.of("Integer Literal: Negative",
                        Arrays.asList(new Token(Token.Type.INTEGER, "-1", 0)),
                        new Ast.Expression.Literal(new BigInteger("-1"))
                ),
                Arguments.of("Decimal Literal: Less Precision",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "2.0", 0)),
                        new Ast.Expression.Literal(new BigDecimal("2.0"))
                ),
                Arguments.of("Decimal Literal: More Precision",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "123456789.123456789", 0)),
                        new Ast.Expression.Literal(new BigDecimal("123456789.123456789"))
                ),
                Arguments.of("Decimal Literal: Negative",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "-2.0", 0)),
                        new Ast.Expression.Literal(new BigDecimal("-2.0"))
                ),
                Arguments.of("Character Literal: Standard",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'c'", 0)),
                        new Ast.Expression.Literal('c')
                ),
                Arguments.of("Character Literal: Escape \\n",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\n'", 0)),
                        new Ast.Expression.Literal('\n')
                ),
                Arguments.of("Character Literal: Escape \\b",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\b'", 0)),
                        new Ast.Expression.Literal('\b')
                ),
                Arguments.of("Character Literal: Escape \\r",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\r'", 0)),
                        new Ast.Expression.Literal('\r')
                ),
                Arguments.of("Character Literal: Escape \\t",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\t'", 0)),
                        new Ast.Expression.Literal('\t')
                ),
                Arguments.of("Character Literal: Escape \\",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\\\'", 0)),
                        new Ast.Expression.Literal('\\')
                ),
                Arguments.of("Character Literal: Escape \"",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\\"'", 0)),
                        new Ast.Expression.Literal('\"')
                ),
                Arguments.of("Character Literal: Escape '",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'\\''", 0)),
                        new Ast.Expression.Literal('\'')
                ),
                Arguments.of("String Literal: Standard",
                        Arrays.asList(new Token(Token.Type.STRING, "\"string\"", 0)),
                        new Ast.Expression.Literal("string")
                ),
                Arguments.of("String Literal: Escape \\b",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\bWorld!\"", 0)),
                        new Ast.Expression.Literal("Hello,\bWorld!")
                ),
                Arguments.of("String Literal: Escape \\n",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\nWorld!\"", 0)),
                        new Ast.Expression.Literal("Hello,\nWorld!")
                ),
                Arguments.of("String Literal: Escape \\r",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\rWorld!\"", 0)),
                        new Ast.Expression.Literal("Hello,\rWorld!")
                ),
                Arguments.of("String Literal: Escape \\t",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\tWorld!\"", 0)),
                        new Ast.Expression.Literal("Hello,\tWorld!")
                ),
                Arguments.of("String Literal: Escape \\",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\\\World!\"", 0)),
                        new Ast.Expression.Literal("Hello,\\World!")
                ),
                Arguments.of("String Literal: Escape \"",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\\"World!\"", 0)),
                        new Ast.Expression.Literal("Hello,\"World!")
                ),
                Arguments.of("String Literal: Escape '",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\'World!\"", 0)),
                        new Ast.Expression.Literal("Hello,'World!")
                ),
                Arguments.of("String Literal: Escape Ends",
                        Arrays.asList(new Token(Token.Type.STRING, "\"\\nHello,World!\\n\"", 0)),
                        new Ast.Expression.Literal("\nHello,World!\n")
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGroupExpression(String test, List<Token> tokens, Ast.Expression.Group expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testGroupExpression() {
        return Stream.of(
                Arguments.of("Grouped Variable",
                        Arrays.asList(
                                //(expr)
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 1),
                                new Token(Token.Type.OPERATOR, ")", 5)
                        ),
                        new Ast.Expression.Group(new Ast.Expression.Access(Optional.empty(), "expr"))
                ),
                Arguments.of("Grouped Binary",
                        Arrays.asList(
                                //(expr1 + expr2)
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr1", 1),
                                new Token(Token.Type.OPERATOR, "+", 7),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, ")", 14)
                        ),
                        new Ast.Expression.Group(new Ast.Expression.Binary("+",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")))
                ),
                Arguments.of("Grouped Group",
                        Arrays.asList(
                                //(expr1 + expr2)
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.OPERATOR, "(", 1),
                                new Token(Token.Type.IDENTIFIER, "expr", 2),
                                new Token(Token.Type.OPERATOR, ")", 6),
                                new Token(Token.Type.OPERATOR, ")", 7)
                        ),
                        new Ast.Expression.Group( new Ast.Expression.Group(new Ast.Expression.Access(Optional.empty(), "expr"))))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGroupParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseExpression);
    }
    private static Stream<Arguments> testGroupParseException() {
        return Stream.of(
                Arguments.of("Missing Closing Parenthesis",
                        Arrays.asList(
                                //(expr
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 1)
                        ),
                        new ParseException("Expected ')' : invalid expression grouping. index: 5", 5)
                ),
                Arguments.of("Missing Expression in Parentheses",
                        Arrays.asList(
                                //()
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.OPERATOR, ")", 1)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 1", 1)
                ),
                Arguments.of("Invalid Closing Parenthesis",
                        Arrays.asList(
                                //()
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 1),
                                new Token(Token.Type.IDENTIFIER, "]", 5)
                        ),
                        new ParseException("Expected ')' : invalid expression grouping. index: 5", 5)
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testBinaryExpression(String test, List<Token> tokens, Ast.Expression.Binary expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("Binary And",
                        Arrays.asList(
                                //expr1 && expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "&&", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 10)
                        ),
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Equality",
                        Arrays.asList(
                                //expr1 == expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "==", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Addition",
                        Arrays.asList(
                                //expr1 + expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Multiplication",
                        Arrays.asList(
                                //expr1 * expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "*", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expression.Binary("*",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAccessExpression(String test, List<Token> tokens, Ast.Expression.Access expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testAccessExpression() {
        return Stream.of(
                Arguments.of("Variable",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "name", 0)),
                        new Ast.Expression.Access(Optional.empty(), "name")
                ),
                Arguments.of("List Access: identifier",
                        Arrays.asList(
                                //list[expr]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.IDENTIFIER, "expr", 5),
                                new Token(Token.Type.OPERATOR, "]", 9)
                        ),
                        new Ast.Expression.Access(Optional.of(new Ast.Expression.Access(Optional.empty(), "expr")), "list")
                ),
                Arguments.of("List Access: Integer",
                        Arrays.asList(
                                //list[1]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.INTEGER, "1", 5),
                                new Token(Token.Type.OPERATOR, "]", 6)
                        ),
                        new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(new BigInteger("1"))), "list")
                ),
                Arguments.of("List Access: Decimal",
                        Arrays.asList(
                                //list[1.0]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.DECIMAL, "1.0", 5),
                                new Token(Token.Type.OPERATOR, "]", 8)
                        ),
                        new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(new BigDecimal("1.0"))), "list")
                ),
                Arguments.of("List Access: Character",
                        Arrays.asList(
                                //list['a']
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.CHARACTER, "'a'", 5),
                                new Token(Token.Type.OPERATOR, "]", 8)
                        ),
                        new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal('a')), "list")
                ),
                Arguments.of("List Access: String",
                        Arrays.asList(
                                //list["hello"]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.STRING, "\"hello\"", 5),
                                new Token(Token.Type.OPERATOR, "]", 12)
                        ),
                        new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal("hello")), "list")
                ),
                Arguments.of("List Access: TRUE",
                        Arrays.asList(
                                //list[TRUE]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 5),
                                new Token(Token.Type.OPERATOR, "]", 8)
                        ),
                        new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(Boolean.TRUE)), "list")
                ),
                Arguments.of("List Access: FALSE",
                        Arrays.asList(
                                //list[FALSE]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.IDENTIFIER, "FALSE", 5),
                                new Token(Token.Type.OPERATOR, "]", 8)
                        ),
                        new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(Boolean.FALSE)), "list")
                ),
                Arguments.of("List Access: NIL",
                        Arrays.asList(
                                //list[NIL]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.IDENTIFIER, "NIL", 5),
                                new Token(Token.Type.OPERATOR, "]", 8)
                        ),
                        new Ast.Expression.Access(
                                Optional.of(new Ast.Expression.Literal(null)), "list")
                ),
                Arguments.of("List Access: Group",
                        Arrays.asList(
                                //list[(expr)]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.OPERATOR, "(", 5),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.OPERATOR, ")", 10),
                                new Token(Token.Type.OPERATOR, "]", 11)
                        ),
                        new Ast.Expression.Access(Optional.of(new Ast.Expression.Group(
                                new Ast.Expression.Access(Optional.empty(), "expr"))), "list")
                ),
                Arguments.of("List Access: List Access",
                        Arrays.asList(
                                //list[list2[expr]]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.IDENTIFIER, "list2", 5),
                                new Token(Token.Type.OPERATOR, "[", 10),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, "]", 15),
                                new Token(Token.Type.OPERATOR, "]", 16)
                        ),
                        new Ast.Expression.Access(
                                Optional.of(new Ast.Expression.Access(
                                        Optional.of(new Ast.Expression.Access(Optional.empty(), "expr")),
                                        "list2")),
                                "list")
                ),
                Arguments.of("List Access: Function",
                        Arrays.asList(
                                //list[list2[expr]]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.IDENTIFIER, "func", 5),
                                new Token(Token.Type.OPERATOR, "(", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 10),
                                new Token(Token.Type.OPERATOR, ")", 14),
                                new Token(Token.Type.OPERATOR, "]", 15)
                        ),
                        new Ast.Expression.Access(
                                Optional.of(new Ast.Expression.Function(
                                        "func",
                                        Arrays.asList(new Ast.Expression.Access(Optional.empty(), "expr")))),
                                "list")
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAccessParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseExpression);
    }
    private static Stream<Arguments> testAccessParseException() {
        return Stream.of(
                Arguments.of("Missing Closing Bracket",
                        Arrays.asList(
                                //list[1
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.INTEGER, "1", 5)
                        ),
                        new ParseException("Expected ']' : invalid list access. index: 6", 6)
                ),
                Arguments.of("Missing Expression",
                        Arrays.asList(
                                //list[]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.OPERATOR, "]", 5)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 5", 5)
                ),
                Arguments.of("Invalid Expression",
                        Arrays.asList(
                                //[
                                new Token(Token.Type.OPERATOR, "[", 0)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 0", 0)
                )
                );
    }

    @ParameterizedTest
    @MethodSource
    void testFunctionExpression(String test, List<Token> tokens, Ast.Expression.Function expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                Arguments.of("Zero Arguments",
                        Arrays.asList(
                                //name()
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.OPERATOR, ")", 5)
                        ),
                        new Ast.Expression.Function("name", Arrays.asList())
                ),
                Arguments.of("Multiple Arguments",
                        Arrays.asList(
                                //name(expr1, expr2, expr3)
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "expr1", 5),
                                new Token(Token.Type.OPERATOR, ",", 10),
                                new Token(Token.Type.IDENTIFIER, "expr2", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr3", 19),
                                new Token(Token.Type.OPERATOR, ")", 24)
                        ),
                        new Ast.Expression.Function("name", Arrays.asList(
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Access(Optional.empty(), "expr2"),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        ))
                ),
                Arguments.of("Different Argument Types",
                        Arrays.asList(
                                //name(12345, (expr2), expr3[NIL])
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.INTEGER, "12345", 5),
                                new Token(Token.Type.OPERATOR, ",", 10),
                                new Token(Token.Type.OPERATOR, "(", 11),
                                new Token(Token.Type.IDENTIFIER, "expr2", 12),
                                new Token(Token.Type.OPERATOR, ")", 17),
                                new Token(Token.Type.OPERATOR, ",", 18),
                                new Token(Token.Type.IDENTIFIER, "expr3", 19),
                                new Token(Token.Type.OPERATOR, "[", 24),
                                new Token(Token.Type.IDENTIFIER, "NIL", 25),
                                new Token(Token.Type.OPERATOR, "]", 28),
                                new Token(Token.Type.OPERATOR, ")", 29)
                        ),
                        new Ast.Expression.Function("name", Arrays.asList(
                                new Ast.Expression.Literal(new BigInteger("12345")),
                                new Ast.Expression.Group(new Ast.Expression.Access(Optional.empty(), "expr2")),
                                new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(null)), "expr3")
                        ))
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testFunctionParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseExpression);
    }
    private static Stream<Arguments> testFunctionParseException() {
        return Stream.of(
                Arguments.of("Missing Closing Parenthesis",
                        Arrays.asList(
                                //name(
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4)
                        ),
                        // unable to determine missing parentheses because it is searching for a parameter due to no following parenthesis
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 5", 5)
                ),
                Arguments.of("Missing Second Parameter",
                        Arrays.asList(
                                //name(param,)
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "param", 5),
                                new Token(Token.Type.OPERATOR, ",", 10),
                                new Token(Token.Type.OPERATOR, ")", 11)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 11", 11)
                ),
                Arguments.of("Missing Comma v1",
                        Arrays.asList(
                                //name(param1 param2)
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "param1", 5),
                                new Token(Token.Type.IDENTIFIER, "param2", 11),
                                new Token(Token.Type.OPERATOR, ")", 17)
                        ),
                        new ParseException("Expected ',' or ')' : invalid function parameters. index: 11", 11)
                ),
                Arguments.of("Missing Closing Parenthesis w/ Parameter",
                        Arrays.asList(
                                //name(param1
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "param1", 5)
                        ),
                        new ParseException("Expected ',' or ')' : invalid function parameters. index: 11", 11)
                )
        );
    }

    /**
     * Standard test function. If expected is null, a ParseException is expected
     * to be thrown (not used in the provided tests).
     */
    private static <T extends Ast> void test(List<Token> tokens, T expected, Function<Parser, T> function) {
        Parser parser = new Parser(tokens);
        if (expected != null) {
            Assertions.assertEquals(expected, function.apply(parser));
        } else {
            Assertions.assertThrows(ParseException.class, () -> function.apply(parser));
        }
    }

    @ParameterizedTest
    @MethodSource
    void testScenarioParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseExpression);
    }
    private static Stream<Arguments> testScenarioParseException() {
        return Stream.of(
                Arguments.of("Missing Closing Parenthesis",
                        Arrays.asList(
                                //012345
                                //(expr
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 1)
                        ),
                        new ParseException("Expected ')' : invalid expression grouping. index: 5", 5)
                )
        );
    }

    private static <T extends Ast> void testParseException(List<Token> tokens, Exception exception, Function<Parser, T> function) {
        Parser parser = new Parser(tokens);
        ParseException pe = Assertions.assertThrows(ParseException.class, () -> function.apply(parser));
        Assertions.assertEquals(exception, pe);
    }

}
