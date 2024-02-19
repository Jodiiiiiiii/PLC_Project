package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Standard JUnit5 parameterized tests. See the RegexTests file from Homework 1
 * or the LexerTests file from the last project part for more information.
 */
final class ParserTests {

    @ParameterizedTest
    @MethodSource
    void testSource(String test, List<Token> tokens, Ast.Source expected) {
        test(tokens, expected, Parser::parseSource);
    }

    private static Stream<Arguments> testSource() {
        return Stream.of(
                Arguments.of("Zero Statements",
                        Arrays.asList(),
                        new Ast.Source(Arrays.asList(), Arrays.asList())
                ),
                Arguments.of("Global - Immutable",
                        Arrays.asList(
                                //VAL name = expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new Ast.Source(
                                Arrays.asList(new Ast.Global("name", false, Optional.of(new Ast.Expression.Access(Optional.empty(), "expr")))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("Function",
                        Arrays.asList(
                                //FUN name() DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(new Ast.Function("name", Arrays.asList(), Arrays.asList(
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt"))
                                )))
                        )
                )
        );
    }

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
    void testDeclarationStatement(String test, List<Token> tokens, Ast.Statement.Declaration expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testDeclarationStatement() {
        return Stream.of(
                Arguments.of("Definition",
                        Arrays.asList(
                                //LET name;
                                new Token(Token.Type.IDENTIFIER, "LET", -1),
                                new Token(Token.Type.IDENTIFIER, "name", -1),
                                new Token(Token.Type.OPERATOR, ";", -1)
                        ),
                        new Ast.Statement.Declaration("name", Optional.empty())
                ),
                Arguments.of("Initialization",
                        Arrays.asList(
                                //LET name = expr;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new Ast.Statement.Declaration("name", Optional.of(new Ast.Expression.Access(Optional.empty(), "expr")))
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
    void testIfStatement(String test, List<Token> tokens, Ast.Statement.If expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testIfStatement() {
        return Stream.of(
                Arguments.of("If",
                        Arrays.asList(
                                //IF expr DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                new Token(Token.Type.IDENTIFIER, "END", 17)
                        ),
                        new Ast.Statement.If(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt"))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("Else",
                        Arrays.asList(
                                //IF expr DO stmt1; ELSE stmt2; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 11),
                                new Token(Token.Type.OPERATOR, ";", 16),
                                new Token(Token.Type.IDENTIFIER, "ELSE", 18),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 23),
                                new Token(Token.Type.OPERATOR, ";", 28),
                                new Token(Token.Type.IDENTIFIER, "END", 30)
                        ),
                        new Ast.Statement.If(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt1"))),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt2")))
                        )
                )
        );
    }

    /*

    @ParameterizedTest
    @MethodSource
    void testForStatement(String test, List<Token> tokens, Ast.Statement.For expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testForStatement() {
        return Stream.of(
                Arguments.of("For",
                        Arrays.asList(
                                //FOR elem IN list DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FOR", 0),
                                new Token(Token.Type.IDENTIFIER, "elem", 6),
                                new Token(Token.Type.IDENTIFIER, "IN", 9),
                                new Token(Token.Type.IDENTIFIER, "list", 12),
                                new Token(Token.Type.IDENTIFIER, "DO", 17),
                                new Token(Token.Type.IDENTIFIER, "stmt", 20),
                                new Token(Token.Type.OPERATOR, ";", 24),
                                new Token(Token.Type.IDENTIFIER, "END", 26)
                        ),
                        new Ast.Statement.For(
                                "elem",
                                new Ast.Expression.Access(Optional.empty(), "list"),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt")))
                        )
                )
        );
    }
*/
    @ParameterizedTest
    @MethodSource
    void testWhileStatement(String test, List<Token> tokens, Ast.Statement.While expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testWhileStatement() {
        return Stream.of(
                Arguments.of("While",
                        Arrays.asList(
                                //WHILE expr DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new Ast.Statement.While(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt")))
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testReturnStatement(String test, List<Token> tokens, Ast.Statement.Return expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testReturnStatement() {
        return Stream.of(
                Arguments.of("Return Statement",
                        Arrays.asList(
                                //RETURN expr;
                                new Token(Token.Type.IDENTIFIER, "RETURN", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.OPERATOR, ";", 11)
                        ),
                        new Ast.Statement.Return(new Ast.Expression.Access(Optional.empty(), "expr"))
                ),
                Arguments.of("Return Statement: literal",
                        Arrays.asList(
                                //RETURN expr;
                                new Token(Token.Type.IDENTIFIER, "RETURN", 0),
                                new Token(Token.Type.DECIMAL, "1.0", 7),
                                new Token(Token.Type.OPERATOR, ";", 10)
                        ),
                        new Ast.Statement.Return(new Ast.Expression.Literal(new BigDecimal("1.0")))
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testReturnParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseStatement);
    }
    private static Stream<Arguments> testReturnParseException() {
        return Stream.of(
                Arguments.of("Missing Semicolon",
                        Arrays.asList(
                                //RETURN expr
                                new Token(Token.Type.IDENTIFIER, "RETURN", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7)
                        ),
                        new ParseException("Expected ';' : invalid return statement. index: 11", 11)
                ),
                Arguments.of("Missing Expression",
                        Arrays.asList(
                                //RETURN expr
                                new Token(Token.Type.IDENTIFIER, "RETURN", 0),
                                new Token(Token.Type.OPERATOR, ";", 7)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 7", 7)
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
                Arguments.of("Binary: Logical",
                        Arrays.asList(
                                //expr1 && expr2 || expr3
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "&&", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 10),
                                new Token(Token.Type.OPERATOR, "||", 17),
                                new Token(Token.Type.IDENTIFIER, "expr3", 20)
                        ),
                        new Ast.Expression.Binary("||",
                                new Ast.Expression.Binary("&&",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")),
                                new Ast.Expression.Access(Optional.empty(), "expr3"))
                ),
                Arguments.of("Binary: Comparison",
                        Arrays.asList(
                                //expr1 == expr2 > expr3 != expr4 < expr5
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "==", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, ">", 15),
                                new Token(Token.Type.IDENTIFIER, "expr3", 17),
                                new Token(Token.Type.OPERATOR, "!=", 23),
                                new Token(Token.Type.IDENTIFIER, "expr4", 26),
                                new Token(Token.Type.OPERATOR, "<", 32),
                                new Token(Token.Type.IDENTIFIER, "expr5", 34)
                        ),
                        new Ast.Expression.Binary(
                                "<",
                                new Ast.Expression.Binary("!=",
                                        new Ast.Expression.Binary(">",
                                                new Ast.Expression.Binary("==",
                                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                                        new Ast.Expression.Access(Optional.empty(), "expr2")
                                                ),
                                                new Ast.Expression.Access(Optional.empty(), "expr3")
                                        ),
                                        new Ast.Expression.Access(Optional.empty(), "expr4")
                                ),
                                new Ast.Expression.Access(Optional.empty(), "expr5")
                        )
                ),
                Arguments.of("Binary: Additive",
                        Arrays.asList(
                                //expr1 + expr2 - expr3
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, "-", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 16)
                        ),
                        new Ast.Expression.Binary("-",
                                new Ast.Expression.Binary("+",
                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                        new Ast.Expression.Access(Optional.empty(), "expr2")),
                                new Ast.Expression.Access(Optional.empty(), "expr3")
                        )
                ),
                Arguments.of("Binary: Multiplicative",
                        Arrays.asList(
                                //expr1 * expr2 / expr3 ^ expr4
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "*", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, "/", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 16),
                                new Token(Token.Type.OPERATOR, "^", 22),
                                new Token(Token.Type.IDENTIFIER, "expr4", 24)
                        ),
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Binary("/",
                                        new Ast.Expression.Binary("*",
                                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                                new Ast.Expression.Access(Optional.empty(), "expr2")),
                                        new Ast.Expression.Access(Optional.empty(), "expr3")),
                                new Ast.Expression.Access(Optional.empty(), "expr4")
                        )
                ),
                Arguments.of("Priority: Given",
                        Arrays.asList(
                                //expr1 + expr2 * expr3
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, "*", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 16)
                        ),
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Binary("*",
                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                        new Ast.Expression.Access(Optional.empty(), "expr3"))
                        )
                ),
                Arguments.of("Priority: All",
                        Arrays.asList(
                                // expr1 && expr2 > expr3 + expr4 * expr5
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "&&", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8),
                                new Token(Token.Type.OPERATOR, ">", 14),
                                new Token(Token.Type.IDENTIFIER, "expr3", 16),
                                new Token(Token.Type.OPERATOR, "+", 22),
                                new Token(Token.Type.IDENTIFIER, "expr4", 24),
                                new Token(Token.Type.OPERATOR, "*", 30),
                                new Token(Token.Type.IDENTIFIER, "expr5", 32)
                        ),
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Access(Optional.empty(), "expr1"),
                                new Ast.Expression.Binary(">",
                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                        new Ast.Expression.Binary("+",
                                                new Ast.Expression.Access(Optional.empty(), "expr3"),
                                                new Ast.Expression.Binary("*",
                                                        new Ast.Expression.Access(Optional.empty(), "expr4"),
                                                        new Ast.Expression.Access(Optional.empty(), "expr5")
                                                )
                                        )
                                )
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

    @Test
    void testExample1() {
        List<Token> input = Arrays.asList(
                /* VAR first = 1;
                 * FUN main() DO
                 *     WHILE first != 10 DO
                 *         print(first);
                 *         first = first + 1;
                 *     END
                 * END
                 */
                //VAR first = 1;
                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                new Token(Token.Type.IDENTIFIER, "first", 4),
                new Token(Token.Type.OPERATOR, "=", 10),
                new Token(Token.Type.INTEGER, "1", 12),
                new Token(Token.Type.OPERATOR, ";", 13),
                //FUN main() DO
                new Token(Token.Type.IDENTIFIER, "FUN", 15),
                new Token(Token.Type.IDENTIFIER, "main", 19),
                new Token(Token.Type.OPERATOR, "(", 23),
                new Token(Token.Type.OPERATOR, ")", 24),
                new Token(Token.Type.IDENTIFIER, "DO", 26),
                //    WHILE first != 10 DO
                new Token(Token.Type.IDENTIFIER, "WHILE", 33),
                new Token(Token.Type.IDENTIFIER, "first", 39),
                new Token(Token.Type.OPERATOR, "!=", 45),
                new Token(Token.Type.INTEGER, "10", 48),
                new Token(Token.Type.IDENTIFIER, "DO", 51),
                //        print(first);
                new Token(Token.Type.IDENTIFIER, "print", 62),
                new Token(Token.Type.OPERATOR, "(", 67),
                new Token(Token.Type.IDENTIFIER, "first", 68),
                new Token(Token.Type.OPERATOR, ")", 73),
                new Token(Token.Type.OPERATOR, ";", 74),
                //        first = first + 1;
                new Token(Token.Type.IDENTIFIER, "first", 84),
                new Token(Token.Type.OPERATOR, "=", 90),
                new Token(Token.Type.IDENTIFIER, "first", 92),
                new Token(Token.Type.OPERATOR, "+", 98),
                new Token(Token.Type.INTEGER, "1", 100),
                new Token(Token.Type.OPERATOR, ";", 101),
                //    END
                new Token(Token.Type.IDENTIFIER, "END", 107),
                //END
                new Token(Token.Type.IDENTIFIER, "END", 111)
        );
        Ast.Source expected = new Ast.Source(
                Arrays.asList(new Ast.Global("first", true, Optional.of(new Ast.Expression.Literal(BigInteger.ONE)))),
                Arrays.asList(new Ast.Function("main", Arrays.asList(), Arrays.asList(
                        new Ast.Statement.While(
                                new Ast.Expression.Binary("!=",
                                        new Ast.Expression.Access(Optional.empty(), "first"),
                                        new Ast.Expression.Literal(BigInteger.TEN)
                                ),
                                Arrays.asList(
                                        new Ast.Statement.Expression(
                                                new Ast.Expression.Function("print", Arrays.asList(
                                                        new Ast.Expression.Access(Optional.empty(), "first"))
                                                )
                                        ),
                                        new Ast.Statement.Assignment(
                                                new Ast.Expression.Access(Optional.empty(), "first"),
                                                new Ast.Expression.Binary("+",
                                                        new Ast.Expression.Access(Optional.empty(), "first"),
                                                        new Ast.Expression.Literal(BigInteger.ONE)
                                                )
                                        )
                                )
                        )
                ))
        ));
        test(input, expected, Parser::parseSource);
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
