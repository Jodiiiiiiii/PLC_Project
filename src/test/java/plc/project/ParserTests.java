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
                Arguments.of("Global - Mutable (Declaration)",
                        Arrays.asList(
                                //VAR name;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new Ast.Source(
                                Arrays.asList(new Ast.Global("name", true, Optional.empty())),
                                Arrays.asList()
                        )
                ),
                Arguments.of("Global - Mutable (Initialization)",
                        Arrays.asList(
                                //VAR name = expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new Ast.Source(
                                Arrays.asList(new Ast.Global("name", true, Optional.of(new Ast.Expression.Access(Optional.empty(), "expr")))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("List (One Element)",
                        Arrays.asList(
                                //VAL name = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new Ast.Source(
                                Arrays.asList(
                                        new Ast.Global(
                                                "name",
                                                true,
                                                Optional.of(new Ast.Expression.PlcList(Arrays.asList(
                                                        new Ast.Expression.Access(Optional.empty(), "expr")))))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("List (Two Elements)",
                        Arrays.asList(
                                //VAL name = [expr1,expr2];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr1", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.OPERATOR, "]", 23),
                                new Token(Token.Type.OPERATOR, ";", 24)
                        ),
                        new Ast.Source(
                                Arrays.asList(
                                        new Ast.Global(
                                                "name",
                                                true,
                                                Optional.of(new Ast.Expression.PlcList(Arrays.asList(
                                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                                        new Ast.Expression.Access(Optional.empty(), "expr2")))))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("List (Multiple Elements)",
                        Arrays.asList(
                                //VAL name = [expr1,expr2,expr3];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr1", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.OPERATOR, ",", 24),
                                new Token(Token.Type.IDENTIFIER, "expr3", 25),
                                new Token(Token.Type.OPERATOR, "]", 30),
                                new Token(Token.Type.OPERATOR, ";", 31)
                        ),
                        new Ast.Source(
                                Arrays.asList(
                                        new Ast.Global(
                                                "name",
                                                true,
                                                Optional.of(new Ast.Expression.PlcList(Arrays.asList(
                                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                                        new Ast.Expression.Access(Optional.empty(), "expr3")))))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("Function (0 Parameters)",
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
                ),
                Arguments.of("Function (1 Parameter)",
                        Arrays.asList(
                                //FUN name(param1) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ")", 15),
                                new Token(Token.Type.IDENTIFIER, "DO", 16),
                                new Token(Token.Type.IDENTIFIER, "stmt", 19),
                                new Token(Token.Type.OPERATOR, ";", 23),
                                new Token(Token.Type.IDENTIFIER, "END", 25)
                        ),
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(new Ast.Function("name",
                                        Arrays.asList(
                                                "param1"
                                        ),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt"))
                                )))
                        )
                ),
                Arguments.of("Function (Multiple Parameters)",
                        Arrays.asList(
                                //FUN name(param1, param2, param3) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.IDENTIFIER, "DO", 31),
                                new Token(Token.Type.IDENTIFIER, "stmt", 34),
                                new Token(Token.Type.OPERATOR, ";", 38),
                                new Token(Token.Type.IDENTIFIER, "END", 40)
                        ),
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(new Ast.Function("name",
                                        Arrays.asList(
                                                "param1",
                                                "param2",
                                                "param3"
                                        ),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt"))
                                        )))
                        )
                ),
                Arguments.of("Globals and Function",
                        Arrays.asList(
                                //VAL name = expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                //VAR name;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                //VAR name = expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                //VAL name = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                //VAL name = [expr1,expr2];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr1", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.OPERATOR, "]", 23),
                                new Token(Token.Type.OPERATOR, ";", 24),
                                //VAL name = [expr1,expr2,expr3];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr1", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.OPERATOR, ",", 24),
                                new Token(Token.Type.IDENTIFIER, "expr3", 25),
                                new Token(Token.Type.OPERATOR, "]", 30),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                //FUN name() DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20),
                                //FUN name(param1) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ")", 15),
                                new Token(Token.Type.IDENTIFIER, "DO", 16),
                                new Token(Token.Type.IDENTIFIER, "stmt", 19),
                                new Token(Token.Type.OPERATOR, ";", 23),
                                new Token(Token.Type.IDENTIFIER, "END", 25),
                                //FUN name(param1, param2, param3) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.IDENTIFIER, "DO", 31),
                                new Token(Token.Type.IDENTIFIER, "stmt", 34),
                                new Token(Token.Type.OPERATOR, ";", 38),
                                new Token(Token.Type.IDENTIFIER, "END", 40)
                        ),
                        new Ast.Source(
                                Arrays.asList(
                                        new Ast.Global("name", false, Optional.of(new Ast.Expression.Access(Optional.empty(), "expr"))),
                                        new Ast.Global("name", true, Optional.empty()),
                                        new Ast.Global("name", true, Optional.of(new Ast.Expression.Access(Optional.empty(), "expr"))),
                                        new Ast.Global("name", true, Optional.of(new Ast.Expression.PlcList(Arrays.asList(
                                                new Ast.Expression.Access(Optional.empty(), "expr"))))),
                                        new Ast.Global(
                                                "name",
                                                true,
                                                Optional.of(new Ast.Expression.PlcList(Arrays.asList(
                                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                                        new Ast.Expression.Access(Optional.empty(), "expr2"))))),
                                        new Ast.Global(
                                                "name",
                                                true,
                                                Optional.of(new Ast.Expression.PlcList(Arrays.asList(
                                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                                        new Ast.Expression.Access(Optional.empty(), "expr2"),
                                                        new Ast.Expression.Access(Optional.empty(), "expr3")))))
                                ),
                                Arrays.asList(
                                        new Ast.Function("name", Arrays.asList(), Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt")))),
                                        new Ast.Function("name", Arrays.asList("param1"), Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt")))),
                                        new Ast.Function("name", Arrays.asList("param1", "param2", "param3"), Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt")))))
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testSourceParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseSource);
    }
    private static Stream<Arguments> testSourceParseException() {
        return Stream.of(
                Arguments.of("Global (Immutable): Missing Identifier",
                        Arrays.asList(
                                //VAL = expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected Identifier : invalid immutable definition. index: 9", 9)
                ),
                Arguments.of("Global (Immutable): Invalid Identifier",
                        Arrays.asList(
                                //VAL ; = expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.OPERATOR, ";", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected Identifier : invalid immutable definition. index: 4", 4)
                ),
                Arguments.of("Global (Immutable): Missing =",
                        Arrays.asList(
                                //VAL name expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected '=' : invalid immutable definition. index: 11", 11)
                ),
                Arguments.of("Global (Immutable): Missing Expression",
                        Arrays.asList(
                                //VAL name = ;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 15", 15)
                ),
                Arguments.of("Global (Immutable): Invalid Expression",
                        Arrays.asList(
                                //VAL name = ; ;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.OPERATOR, ";", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 11", 11)
                ),
                Arguments.of("Global (Immutable): Missing Semicolon",
                        Arrays.asList(
                                //VAL name = expr
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11)
                        ),
                        new ParseException("Expected ';' : invalid immutable definition. index: 15", 15)
                ),
                Arguments.of("Global (Mutable/Declaration): Missing Identifier",
                        Arrays.asList(
                                //VAR ;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected Identifier : invalid mutable definition. index: 15", 15)
                ),
                Arguments.of("Global (Mutable/Declaration): Invalid Identifier",
                        Arrays.asList(
                                //VAR ; ;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.OPERATOR, ";", 4),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected Identifier : invalid mutable definition. index: 4", 4)
                ),
                Arguments.of("Global (Mutable/Declaration): Missing Semicolon",
                        Arrays.asList(
                                //VAR name
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4)
                        ),
                        new ParseException("Expected ';' : invalid mutable definition. index: 8", 8)
                ),
                Arguments.of("Global (Mutable/Initialization): Missing Identifier",
                        Arrays.asList(
                                //VAR = expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected Identifier : invalid mutable definition. index: 9", 9)
                ),
                Arguments.of("Global (Mutable/Initialization): Invalid Identifier",
                        Arrays.asList(
                                //VAR ; = expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.OPERATOR, ";", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected Identifier : invalid mutable definition. index: 4", 4)
                ),
                Arguments.of("Global (Mutable/Initialization): Missing =",
                        Arrays.asList(
                                //VAR name expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected ';' : invalid mutable definition. index: 11", 11)
                ),
                Arguments.of("Global (Mutable/Initialization): Missing Expression",
                        Arrays.asList(
                                //VAR name = ;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 15", 15)
                ),
                Arguments.of("Global (Mutable/Initialization): Invalid Expression",
                        Arrays.asList(
                                //VAR name = =;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.OPERATOR, "=", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 11", 11)
                ),
                Arguments.of("Global (Mutable/Initialization): Missing Semicolon",
                        Arrays.asList(
                                //VAR name = expr
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11)
                        ),
                        new ParseException("Expected ';' : invalid mutable definition/initialization. index: 15", 15)
                ),
                Arguments.of("List (One Element): Invalid Identifier",
                        Arrays.asList(
                                //VAL name = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.OPERATOR, ";", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected Identifier : invalid list definition. index: 5", 5)
                ),
                Arguments.of("List (One Element): Missing Identifier",
                        Arrays.asList(
                                //VAL = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected Identifier : invalid list definition. index: 10", 10)
                ),
                Arguments.of("List (One Element): Missing =",
                        Arrays.asList(
                                //VAL name [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected '=' : invalid list definition. index: 11", 11)
                ),
                Arguments.of("List (One Element): Missing [",
                        Arrays.asList(
                                //VAL name = expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected '[' : invalid list definition. index: 12", 12)
                ),
                Arguments.of("List (One Element): Missing Expression",
                        Arrays.asList(
                                //VAL name = [];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 17", 17)
                ),
                Arguments.of("List (One Element): Invalid Expression",
                        Arrays.asList(
                                //VAL name = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.OPERATOR, "!=", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 12", 12)
                ),
                Arguments.of("List (One Element): Missing ]",
                        Arrays.asList(
                                //VAL name = [expr;
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected ']' : invalid list definition. index: 18", 18)
                ),
                Arguments.of("List (One Element): Missing Semicolon",
                        Arrays.asList(
                                //VAL name = [expr]
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17)
                        ),
                        new ParseException("Expected ';' : invalid list definition. index: 18", 18)
                ),
                Arguments.of("List (One Element): Leading Comma",
                        Arrays.asList(
                                //VAL name = [,expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.OPERATOR, ",", 12),
                                new Token(Token.Type.IDENTIFIER, "expr", 13),
                                new Token(Token.Type.OPERATOR, "]", 18),
                                new Token(Token.Type.OPERATOR, ";", 19)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 12", 12)
                ),
                Arguments.of("List (One Element): Trailing Comma",
                        Arrays.asList(
                                //VAL name = [expr,];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, ",", 13),
                                new Token(Token.Type.OPERATOR, "]", 18),
                                new Token(Token.Type.OPERATOR, ";", 19)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 18", 18)
                ),
                Arguments.of("List (Multiple Elements): Missing Separating Comma 1",
                        Arrays.asList(
                                //VAL name = [expr1 expr2,expr3];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr1", 12),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.OPERATOR, ",", 24),
                                new Token(Token.Type.IDENTIFIER, "expr3", 25),
                                new Token(Token.Type.OPERATOR, "]", 30),
                                new Token(Token.Type.OPERATOR, ";", 31)
                        ),
                        new ParseException("Expected ']' : invalid list definition. index: 18", 18)
                ),
                Arguments.of("List (Multiple Elements): Missing Separating Comma 2",
                        Arrays.asList(
                                //VAL name = [expr1,expr2 expr3];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr1", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.IDENTIFIER, "expr3", 25),
                                new Token(Token.Type.OPERATOR, "]", 30),
                                new Token(Token.Type.OPERATOR, ";", 31)
                        ),
                        new ParseException("Expected ']' : invalid list definition. index: 25", 25)
                ),
                Arguments.of("List (Multiple Elements): Leading Comma",
                        Arrays.asList(
                                //VAL name = [,expr1,expr2,expr3];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.OPERATOR, ",", 12),
                                new Token(Token.Type.IDENTIFIER, "expr1", 13),
                                new Token(Token.Type.OPERATOR, ",", 18),
                                new Token(Token.Type.IDENTIFIER, "expr2", 19),
                                new Token(Token.Type.OPERATOR, ",", 25),
                                new Token(Token.Type.IDENTIFIER, "expr3", 26),
                                new Token(Token.Type.OPERATOR, "]", 31),
                                new Token(Token.Type.OPERATOR, ";", 32)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 12", 12)
                ),
                Arguments.of("List (Multiple Elements): Trailing Comma",
                        Arrays.asList(
                                //VAL name = [expr1,expr2,expr3,];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr1", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.OPERATOR, ",", 24),
                                new Token(Token.Type.IDENTIFIER, "expr3", 25),
                                new Token(Token.Type.OPERATOR, ",", 26),
                                new Token(Token.Type.OPERATOR, "]", 31),
                                new Token(Token.Type.OPERATOR, ";", 32)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 31", 31)
                ),
                Arguments.of("Function (No Parameters): Invalid Identifier",
                        Arrays.asList(
                                //FUN name() DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.OPERATOR, ";", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new ParseException("Expected Function Name : invalid function definition. index: 4", 4)
                ),
                Arguments.of("Function (No Parameters): Missing Identifier",
                        Arrays.asList(
                                //FUN () DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new ParseException("Expected Function Name : invalid function definition. index: 8", 8)
                ),
                Arguments.of("Function (No Parameters): Missing (",
                        Arrays.asList(
                                //FUN name( DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new ParseException("Missing '(' : invalid function definition. index: 9", 9)
                ),
                Arguments.of("Function (No Parameters): Missing )",
                        Arrays.asList(
                                //FUN name) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new ParseException("Expected ')' : invalid function definition. index: 14", 14)
                ),
                Arguments.of("Function (No Parameters): Missing DO",
                        Arrays.asList(
                                //FUN name() stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new ParseException("Expected \"DO\" : invalid function definition. index: 14", 14)
                ),
                Arguments.of("Function (No Parameters): Invalid Statement/Block",
                        Arrays.asList(
                                //FUN name() DO ; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 18", 18)
                ),
                Arguments.of("Function (No Parameters): Missing END",
                        Arrays.asList(
                                //FUN name() DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        // still trying to search for another statement within block
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 19", 19)
                ),
                Arguments.of("Function (No Parameters): INVALID Terminator (Not END)",
                        Arrays.asList(
                                //FUN name() DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 20)
                        ),
                        new ParseException("Expected \"END\" : invalid function definition. index: 20", 20)
                ),
                Arguments.of("Function (One Parameter): Invalid Parameter",
                        Arrays.asList(
                                //FUN name(;) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ";", 9),
                                new Token(Token.Type.OPERATOR, ")", 15),
                                new Token(Token.Type.IDENTIFIER, "DO", 16),
                                new Token(Token.Type.IDENTIFIER, "stmt", 19),
                                new Token(Token.Type.OPERATOR, ";", 23),
                                new Token(Token.Type.IDENTIFIER, "END", 25)
                        ),
                        new ParseException("Expected ')' or Identifier : invalid function definition. index: 9", 9)
                ),
                Arguments.of("Function (One Parameter): Leading Comma",
                        Arrays.asList(
                                //FUN name(,param1) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ",", 9),
                                new Token(Token.Type.IDENTIFIER, "param1", 10),
                                new Token(Token.Type.OPERATOR, ")", 16),
                                new Token(Token.Type.IDENTIFIER, "DO", 17),
                                new Token(Token.Type.IDENTIFIER, "stmt", 20),
                                new Token(Token.Type.OPERATOR, ";", 24),
                                new Token(Token.Type.IDENTIFIER, "END", 26)
                        ),
                        new ParseException("Expected ')' or Identifier : invalid function definition. index: 9", 9)
                ),
                Arguments.of("Function (One Parameter): Trailing COmma",
                        Arrays.asList(
                                //FUN name(param1,) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.OPERATOR, ")", 16),
                                new Token(Token.Type.IDENTIFIER, "DO", 17),
                                new Token(Token.Type.IDENTIFIER, "stmt", 18),
                                new Token(Token.Type.OPERATOR, ";", 24),
                                new Token(Token.Type.IDENTIFIER, "END", 26)
                        ),
                        new ParseException("Expected Identifier after ',' : invalid function definition. index: 16", 16)
                ),
                Arguments.of("Function (Multiple Parameters): Invalid Parameter",
                        Arrays.asList(
                                //FUN name(param1, param2, param3) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.OPERATOR, ";", 23),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.IDENTIFIER, "DO", 31),
                                new Token(Token.Type.IDENTIFIER, "stmt", 34),
                                new Token(Token.Type.OPERATOR, ";", 38),
                                new Token(Token.Type.IDENTIFIER, "END", 40)
                        ),
                        new ParseException("Expected Identifier after ',' : invalid function definition. index: 23", 23)
                ),
                Arguments.of("Function (Multiple Parameters): Missing Comma 1",
                        Arrays.asList(
                                //FUN name(param1 param2, param3) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.IDENTIFIER, "DO", 31),
                                new Token(Token.Type.IDENTIFIER, "stmt", 34),
                                new Token(Token.Type.OPERATOR, ";", 38),
                                new Token(Token.Type.IDENTIFIER, "END", 40)
                        ),
                        new ParseException("Expected ')' : invalid function definition. index: 17", 17)
                ),
                Arguments.of("Function (Multiple Parameters): Missing Comma 2",
                        Arrays.asList(
                                //FUN name(param1, param2 param3) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.IDENTIFIER, "DO", 31),
                                new Token(Token.Type.IDENTIFIER, "stmt", 34),
                                new Token(Token.Type.OPERATOR, ";", 38),
                                new Token(Token.Type.IDENTIFIER, "END", 40)
                        ),
                        new ParseException("Expected ')' : invalid function definition. index: 23", 23)
                ),
                Arguments.of("Function (Multiple Parameters): Leading Comma",
                        Arrays.asList(
                                //FUN name(param1, param2, param3) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ",", 9),
                                new Token(Token.Type.IDENTIFIER, "param1", 10),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.IDENTIFIER, "DO", 31),
                                new Token(Token.Type.IDENTIFIER, "stmt", 34),
                                new Token(Token.Type.OPERATOR, ";", 38),
                                new Token(Token.Type.IDENTIFIER, "END", 40)
                        ),
                        new ParseException("Expected ')' or Identifier : invalid function definition. index: 9", 9)
                ),
                Arguments.of("Function (Multiple Parameters): Trailing Comma",
                        Arrays.asList(
                                //FUN name(param1, param2, param3,) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ",", 24),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.IDENTIFIER, "DO", 31),
                                new Token(Token.Type.IDENTIFIER, "stmt", 34),
                                new Token(Token.Type.OPERATOR, ";", 38),
                                new Token(Token.Type.IDENTIFIER, "END", 40)
                        ),
                        new ParseException("Expected Identifier after ',' : invalid function definition. index: 29", 29)
                ),
                Arguments.of("Function Before Global",
                        Arrays.asList(
                                //VAL name = expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                //VAR name;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                //VAR name = expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                //VAL name = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                //VAL name = [expr1,expr2];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr1", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.OPERATOR, "]", 23),
                                new Token(Token.Type.OPERATOR, ";", 249),
                                //FUN name() DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20),
                                //VAL name = [expr1,expr2,expr3];
                                new Token(Token.Type.IDENTIFIER, "LIST", 250),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr1", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.OPERATOR, ",", 24),
                                new Token(Token.Type.IDENTIFIER, "expr3", 25),
                                new Token(Token.Type.OPERATOR, "]", 30),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                //FUN name(param1) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ")", 15),
                                new Token(Token.Type.IDENTIFIER, "DO", 16),
                                new Token(Token.Type.IDENTIFIER, "stmt", 19),
                                new Token(Token.Type.OPERATOR, ";", 23),
                                new Token(Token.Type.IDENTIFIER, "END", 25),
                                //FUN name(param1, param2, param3) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.IDENTIFIER, "DO", 31),
                                new Token(Token.Type.IDENTIFIER, "stmt", 34),
                                new Token(Token.Type.OPERATOR, ";", 38),
                                new Token(Token.Type.IDENTIFIER, "END", 40)
                        ),
                        new ParseException("Expected end of file : source file must be composed of all globals followed by all functions. index: 250", 250)
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
                        new Ast.Statement.Expression(new Ast.Expression.Function("name", Arrays.asList())
                        )
                ),
                Arguments.of("Variable Expression",
                        Arrays.asList(
                                //name;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, ";", 6)
                        ),
                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "name")
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testExpressionStatementParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseStatement);
    }
    private static Stream<Arguments> testExpressionStatementParseException() {
        return Stream.of(
                Arguments.of("Invalid Expression",
                        Arrays.asList(
                                //;     ;
                                new Token(Token.Type.OPERATOR, ";", 0),
                                new Token(Token.Type.OPERATOR, ";", 6)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 0", 0)
                ),
                Arguments.of("Missing Semicolon",
                        Arrays.asList(
                                //name()
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.OPERATOR, ")", 5)
                        ),
                        new ParseException("Expected ';' : invalid expression statement. index: 6", 6)
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
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ";", 6)
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
    void testDeclarationParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseStatement);
    }
    private static Stream<Arguments> testDeclarationParseException() {
        return Stream.of(
                Arguments.of("Definition: missing identifier",
                        Arrays.asList(
                                //LET ;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.OPERATOR, ";", 4)
                        ),
                        new ParseException("Expected identifier : invalid declaration statement. index: 4", 4)
                ),
                Arguments.of("Definition: Invalid identifier",
                        Arrays.asList(
                                //LET ; ;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.OPERATOR, ";", 4),
                                new Token(Token.Type.OPERATOR, ";", 6)
                        ),
                        new ParseException("Expected identifier : invalid declaration statement. index: 4", 4)
                ),
                Arguments.of("Definition: Missing Semicolon",
                        Arrays.asList(
                                //LET name
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4)
                        ),
                        new ParseException("Expected ';' : invalid declaration statement. index: 8", 8)
                ),
                Arguments.of("Initialization: missing identifier",
                        Arrays.asList(
                                //LET = expr;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected identifier : invalid declaration statement. index: 9", 9)
                ),
                Arguments.of("Initialization: invalid identifier",
                        Arrays.asList(
                                //LET = expr;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.OPERATOR, ";", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected identifier : invalid declaration statement. index: 4", 4)
                ),
                Arguments.of("Initialization: missing equal sign",
                        Arrays.asList(
                                //LET name expr;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected ';' : invalid declaration statement. index: 11", 11)
                ),
                Arguments.of("Initialization: missing expression",
                        Arrays.asList(
                                //LET name = ;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 15", 15)
                ),
                Arguments.of("Initialization: invalid expression",
                        Arrays.asList(
                                //LET name = expr;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.OPERATOR, ":", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 11", 11)
                ),
                Arguments.of("Initialization: missing semicolon",
                        Arrays.asList(
                                //LET name = expr;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11)
                        ),
                        new ParseException("Expected ';' : invalid initialization statement. index: 15", 15)
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
                Arguments.of("Assignment: Standard",
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
                ),
                Arguments.of("Assignment: Different Types",
                        Arrays.asList(
                                //TRUE = list[1.2];
                                new Token(Token.Type.IDENTIFIER, "TRUE", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.IDENTIFIER, "list", 7),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.DECIMAL, "1.2", 12),
                                new Token(Token.Type.OPERATOR, "]", 15),
                                new Token(Token.Type.OPERATOR, ";", 16)
                        ),
                        new Ast.Statement.Assignment(
                                new Ast.Expression.Literal(Boolean.TRUE),
                                new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(new BigDecimal("1.2"))), "list")
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAssignmentParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseStatement);
    }
    private static Stream<Arguments> testAssignmentParseException() {
        return Stream.of(
                Arguments.of("Missing Value",
                        Arrays.asList(
                                //name = ;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.OPERATOR, ";", 12)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 12", 12)
                ),
                Arguments.of("Missing Semicolon",
                        Arrays.asList(
                                //name = value;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.IDENTIFIER, "value", 7)
                        ),
                        new ParseException("Expected ';' : invalid assignment statement. index: 12", 12)
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
                Arguments.of("If - empty block",
                        Arrays.asList(
                                //IF expr DO END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "END", 17)
                        ),
                        new Ast.Statement.If(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList(),
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
                ),
                Arguments.of("Else - Empty blocks",
                        Arrays.asList(
                                //IF expr DO stmt1; ELSE stmt2; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "ELSE", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 30)
                        ),
                        new Ast.Statement.If(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList(),
                                Arrays.asList()
                        )
                ),
                Arguments.of("If-Else: Literals",
                        Arrays.asList(
                                //IF expr DO stmt1; ELSE stmt2; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 11),
                                new Token(Token.Type.OPERATOR, ";", 16),
                                new Token(Token.Type.IDENTIFIER, "ELSE", 18),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 23),
                                new Token(Token.Type.OPERATOR, ";", 28),
                                new Token(Token.Type.IDENTIFIER, "END", 30)
                        ),
                        new Ast.Statement.If(
                                new Ast.Expression.Literal(Boolean.TRUE),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt1"))),
                                Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt2")))
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testIfParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseStatement);
    }
    private static Stream<Arguments> testIfParseException() {
        return Stream.of(
                Arguments.of("Missing expression",
                        Arrays.asList(
                                //IF DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                new Token(Token.Type.IDENTIFIER, "END", 17)
                        ),
                        // improperly processed existing DO as expression
                        new ParseException("Expected \"DO\" : invalid if statement. index: 11", 11)
                ),
                Arguments.of("Missing DO",
                        Arrays.asList(
                                //IF expr stmt; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "stmt", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                new Token(Token.Type.IDENTIFIER, "END", 17)
                        ),
                        new ParseException("Expected \"DO\" : invalid if statement. index: 11", 11)
                ),
                Arguments.of("Invalid statement (in if)",
                        Arrays.asList(
                                //IF expr DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                new Token(Token.Type.IDENTIFIER, "END", 17)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 15", 15)
                ),
                Arguments.of("Missing END",
                        Arrays.asList(
                                //IF expr DO stmt;
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        // could not find END so kept trying to parse block
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 16", 16)
                ),
                Arguments.of("Improper termination (instead of END)",
                        Arrays.asList(
                                //IF expr DO stmt; DEFAULT
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 17)
                        ),
                        new ParseException("Expected \"END\" : invalid if statement. index: 17", 17)
                ),
                Arguments.of("Missing END (if-else)",
                        Arrays.asList(
                                //IF expr DO stmt1; ELSE stmt2;
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 11),
                                new Token(Token.Type.OPERATOR, ";", 16),
                                new Token(Token.Type.IDENTIFIER, "ELSE", 18),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 23),
                                new Token(Token.Type.OPERATOR, ";", 28)
                        ),
                        // could not find END so kept trying to parse block
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 29", 29)
                ),
                Arguments.of("Invalid statement (else)",
                        Arrays.asList(
                                //IF expr DO stmt1; ELSE stmt2; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 11),
                                new Token(Token.Type.OPERATOR, ";", 16),
                                new Token(Token.Type.IDENTIFIER, "ELSE", 18),
                                new Token(Token.Type.OPERATOR, ";", 28),
                                new Token(Token.Type.IDENTIFIER, "END", 30)
                        ),
                        // could not find END so kept trying to parse block
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 28", 28)
                ),
                Arguments.of("Invalid termination (other than END) - if-else",
                        Arrays.asList(
                                //IF expr DO stmt1; ELSE stmt2; DEFAULT
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 11),
                                new Token(Token.Type.OPERATOR, ";", 16),
                                new Token(Token.Type.IDENTIFIER, "ELSE", 18),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 23),
                                new Token(Token.Type.OPERATOR, ";", 28),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 30)
                        ),
                        // could not find END so kept trying to parse block
                        new ParseException("Expected \"END\" : invalid if-else statement. index: 30", 30)
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testSwitchStatement(String test, List<Token> tokens, Ast.Statement.Switch expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testSwitchStatement() {
        return Stream.of(
                Arguments.of("Switch: Default Case Only",
                        Arrays.asList(
                                //SWITCH expr DEFAULT line; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 12),
                                new Token(Token.Type.IDENTIFIER, "line", 20),
                                new Token(Token.Type.OPERATOR, ";", 24),
                                new Token(Token.Type.IDENTIFIER, "END", 26)
                        ),
                        new Ast.Statement.Switch(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList(
                                        new Ast.Statement.Case(Optional.empty(), Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "line")))))
                        )
                ),
                Arguments.of("Switch: One Extra Case",
                        Arrays.asList(
                                //SWITCH expr CASE TRUE : stmt1; DEFAULT stmt2; stmt3; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "CASE", 12),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 20),
                                new Token(Token.Type.OPERATOR, ":", 24),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 26),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 33),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 41),
                                new Token(Token.Type.OPERATOR, ";", 46),
                                new Token(Token.Type.IDENTIFIER, "stmt3", 48),
                                new Token(Token.Type.OPERATOR, ";", 53),
                                new Token(Token.Type.IDENTIFIER, "END", 55)
                        ),
                        new Ast.Statement.Switch(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList(
                                        new Ast.Statement.Case(Optional.of(new Ast.Expression.Literal(Boolean.TRUE)), Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt1"))
                                        )),
                                        new Ast.Statement.Case(Optional.empty(), Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt2")),
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt3")))))
                        )
                ),
                Arguments.of("Switch: Two Extra Case",
                        Arrays.asList(
                                //SWITCH expr CASE TRUE : stmt1; CASE FALSE : stmt4; DEFAULT stmt2; stmt3; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "CASE", 12),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 20),
                                new Token(Token.Type.OPERATOR, ":", 24),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 26),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                new Token(Token.Type.IDENTIFIER, "CASE", 33),
                                new Token(Token.Type.IDENTIFIER, "FALSE", 38),
                                new Token(Token.Type.OPERATOR, ":", 44),
                                new Token(Token.Type.IDENTIFIER, "stmt4", 46),
                                new Token(Token.Type.OPERATOR, ";", 51),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 53),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 61),
                                new Token(Token.Type.OPERATOR, ";", 66),
                                new Token(Token.Type.IDENTIFIER, "stmt3", 68),
                                new Token(Token.Type.OPERATOR, ";", 73),
                                new Token(Token.Type.IDENTIFIER, "END", 75)
                        ),
                        new Ast.Statement.Switch(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList(
                                        new Ast.Statement.Case(Optional.of(new Ast.Expression.Literal(Boolean.TRUE)), Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt1"))
                                        )),
                                        new Ast.Statement.Case(Optional.of(new Ast.Expression.Literal(Boolean.FALSE)), Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt4"))
                                        )),
                                        new Ast.Statement.Case(Optional.empty(), Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt2")),
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt3")))))
                        )
                ),
                Arguments.of("Switch: Literal Expression",
                        Arrays.asList(
                                //SWITCH expr DEFAULT line; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 7),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 12),
                                new Token(Token.Type.IDENTIFIER, "line", 20),
                                new Token(Token.Type.OPERATOR, ";", 24),
                                new Token(Token.Type.IDENTIFIER, "END", 26)
                        ),
                        new Ast.Statement.Switch(
                                new Ast.Expression.Literal(Boolean.TRUE),
                                Arrays.asList(
                                        new Ast.Statement.Case(Optional.empty(), Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "line")))))
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testSwitchParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseStatement);
    }
    private static Stream<Arguments> testSwitchParseException() {
        return Stream.of(
                Arguments.of("Missing Expression",
                        Arrays.asList(
                                //SWITCH DEFAULT line; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 7),
                                new Token(Token.Type.IDENTIFIER, "line", 15),
                                new Token(Token.Type.OPERATOR, ";", 19),
                                new Token(Token.Type.IDENTIFIER, "END", 21)
                        ),
                        // improperly parsed DEFAULT as expression, so it expects another DEFAULT
                        new ParseException("Expected \"DEFAULT\" : missing default case in switch statement. index: 15", 15)
                ),
                Arguments.of("Missing DEFAULT",
                        Arrays.asList(
                                //SWITCH expr line; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "line", 12),
                                new Token(Token.Type.OPERATOR, ";", 16),
                                new Token(Token.Type.IDENTIFIER, "END", 18)
                        ),
                        new ParseException("Expected \"DEFAULT\" : missing default case in switch statement. index: 12", 12)
                ),
                Arguments.of("Invalid Expression: DEFAULT case",
                        Arrays.asList(
                                //SWITCH expr DEFAULT ; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 12),
                                new Token(Token.Type.OPERATOR, ";", 24),
                                new Token(Token.Type.IDENTIFIER, "END", 26)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 24", 24)
                ),
                Arguments.of("Missing END",
                        Arrays.asList(
                                //SWITCH expr DEFAULT line;
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 12),
                                new Token(Token.Type.IDENTIFIER, "line", 20),
                                new Token(Token.Type.OPERATOR, ";", 24)
                        ),
                        // no termination to block (so it says invalid identifier, not Expected "END")
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 25", 25)
                ),
                Arguments.of("Improper Termination (not END)",
                        Arrays.asList(
                                //SWITCH expr DEFAULT line;
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 12),
                                new Token(Token.Type.IDENTIFIER, "line", 20),
                                new Token(Token.Type.OPERATOR, ";", 24),
                                new Token(Token.Type.IDENTIFIER, "CASE", 26)
                        ),
                        new ParseException("Missing \"END\" : invalid switch statement. index: 26", 26)
                ),
                Arguments.of("Missing Expression (with cases)",
                        Arrays.asList(
                                //SWITCH CASE TRUE : stmt1; CASE FALSE : stmt4; DEFAULT stmt2; stmt3; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "CASE", 12),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 20),
                                new Token(Token.Type.OPERATOR, ":", 24),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 26),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                new Token(Token.Type.IDENTIFIER, "CASE", 33),
                                new Token(Token.Type.IDENTIFIER, "FALSE", 38),
                                new Token(Token.Type.OPERATOR, ":", 44),
                                new Token(Token.Type.IDENTIFIER, "stmt4", 46),
                                new Token(Token.Type.OPERATOR, ";", 51),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 53),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 61),
                                new Token(Token.Type.OPERATOR, ";", 66),
                                new Token(Token.Type.IDENTIFIER, "stmt3", 68),
                                new Token(Token.Type.OPERATOR, ";", 73),
                                new Token(Token.Type.IDENTIFIER, "END", 75)
                        ),
                        // improperly parsed CASE as expression, so it expects CASE/DEFAULT next
                        new ParseException("Expected \"DEFAULT\" : missing default case in switch statement. index: 20", 20)
                ),
                Arguments.of("Missing CASE",
                        Arrays.asList(
                                //SWITCH expr TRUE : stmt1; CASE FALSE : stmt4; DEFAULT stmt2; stmt3; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 20),
                                new Token(Token.Type.OPERATOR, ":", 24),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 26),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                new Token(Token.Type.IDENTIFIER, "CASE", 33),
                                new Token(Token.Type.IDENTIFIER, "FALSE", 38),
                                new Token(Token.Type.OPERATOR, ":", 44),
                                new Token(Token.Type.IDENTIFIER, "stmt4", 46),
                                new Token(Token.Type.OPERATOR, ";", 51),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 53),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 61),
                                new Token(Token.Type.OPERATOR, ";", 66),
                                new Token(Token.Type.IDENTIFIER, "stmt3", 68),
                                new Token(Token.Type.OPERATOR, ";", 73),
                                new Token(Token.Type.IDENTIFIER, "END", 75)
                        ),
                        // improperly parsed CASE as expression, so it expects CASE/DEFAULT next
                        new ParseException("Expected \"DEFAULT\" : missing default case in switch statement. index: 20", 20)
                ),
                Arguments.of("Missing expression in case",
                        Arrays.asList(
                                //SWITCH expr CASE : stmt1; CASE FALSE : stmt4; DEFAULT stmt2; stmt3; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "CASE", 12),
                                new Token(Token.Type.OPERATOR, ":", 24),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 26),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                new Token(Token.Type.IDENTIFIER, "CASE", 33),
                                new Token(Token.Type.IDENTIFIER, "FALSE", 38),
                                new Token(Token.Type.OPERATOR, ":", 44),
                                new Token(Token.Type.IDENTIFIER, "stmt4", 46),
                                new Token(Token.Type.OPERATOR, ";", 51),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 53),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 61),
                                new Token(Token.Type.OPERATOR, ";", 66),
                                new Token(Token.Type.IDENTIFIER, "stmt3", 68),
                                new Token(Token.Type.OPERATOR, ";", 73),
                                new Token(Token.Type.IDENTIFIER, "END", 75)
                        ),
                        // improperly parsed CASE as expression, so it expects CASE/DEFAULT next
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 24", 24)
                ),
                Arguments.of("Missing : in case",
                        Arrays.asList(
                                //SWITCH expr CASE TRUE : stmt1; CASE FALSE   stmt4; DEFAULT stmt2; stmt3; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "CASE", 12),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 20),
                                new Token(Token.Type.OPERATOR, ":", 24),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 26),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                new Token(Token.Type.IDENTIFIER, "CASE", 33),
                                new Token(Token.Type.IDENTIFIER, "FALSE", 38),
                                new Token(Token.Type.IDENTIFIER, "stmt4", 46),
                                new Token(Token.Type.OPERATOR, ";", 51),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 53),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 61),
                                new Token(Token.Type.OPERATOR, ";", 66),
                                new Token(Token.Type.IDENTIFIER, "stmt3", 68),
                                new Token(Token.Type.OPERATOR, ";", 73),
                                new Token(Token.Type.IDENTIFIER, "END", 75)
                        ),
                        // improperly parsed CASE as expression, so it expects CASE/DEFAULT next
                        new ParseException("Expected ':' : invalid case statement. index: 46", 46)
                ),
                Arguments.of("Missing DEFAULT (with cases)",
                        Arrays.asList(
                                //SWITCH expr CASE TRUE : stmt1; CASE FALSE : stmt4; DEFAULT stmt2; stmt3; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "CASE", 12),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 20),
                                new Token(Token.Type.OPERATOR, ":", 24),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 26),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                new Token(Token.Type.IDENTIFIER, "CASE", 33),
                                new Token(Token.Type.IDENTIFIER, "FALSE", 38),
                                new Token(Token.Type.OPERATOR, ":", 44),
                                new Token(Token.Type.IDENTIFIER, "stmt4", 46),
                                new Token(Token.Type.OPERATOR, ";", 51),
                                new Token(Token.Type.IDENTIFIER, "END", 75)
                        ),
                        // improperly parsed CASE as expression, so it expects CASE/DEFAULT next
                        new ParseException("Expected \"DEFAULT\" : missing default case in switch statement. index: 75", 75)
                ),
                Arguments.of("Invalid statement (with cases)",
                        Arrays.asList(
                                //SWITCH expr CASE TRUE : stmt1; CASE FALSE : stmt4; DEFAULT ; stmt3; END
                                new Token(Token.Type.IDENTIFIER, "SWITCH", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.IDENTIFIER, "CASE", 12),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 20),
                                new Token(Token.Type.OPERATOR, ":", 24),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 26),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                new Token(Token.Type.IDENTIFIER, "CASE", 33),
                                new Token(Token.Type.IDENTIFIER, "FALSE", 38),
                                new Token(Token.Type.OPERATOR, ":", 44),
                                new Token(Token.Type.IDENTIFIER, "stmt4", 46),
                                new Token(Token.Type.OPERATOR, ";", 51),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 53),
                                new Token(Token.Type.OPERATOR, ";", 66),
                                new Token(Token.Type.IDENTIFIER, "stmt3", 68),
                                new Token(Token.Type.OPERATOR, ";", 73),
                                new Token(Token.Type.IDENTIFIER, "END", 75)
                        ),
                        // improperly parsed CASE as expression, so it expects CASE/DEFAULT next
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 66", 66)
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testWhileStatement(String test, List<Token> tokens, Ast.Statement.While expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testWhileStatement() {
        return Stream.of(
                Arguments.of("While: Standard",
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
                ),
                Arguments.of("While: Empty Body",
                        Arrays.asList(
                                //WHILE expr DO END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "END", 14)
                        ),
                        new Ast.Statement.While(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList()
                        )
                ),
                Arguments.of("While: Multiple-line block",
                        Arrays.asList(
                                //WHILE expr DO stmt; another; END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "another", 20),
                                new Token(Token.Type.OPERATOR, ";", 27),
                                new Token(Token.Type.IDENTIFIER, "END", 29)
                        ),
                        new Ast.Statement.While(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList(
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt")),
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "another")))
                        )
                ),
                Arguments.of("While: Triple-line block",
                        Arrays.asList(
                                //WHILE expr DO stmt; another; again; END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "another", 20),
                                new Token(Token.Type.OPERATOR, ";", 27),
                                new Token(Token.Type.IDENTIFIER, "again", 29),
                                new Token(Token.Type.OPERATOR, ";", 34),
                                new Token(Token.Type.IDENTIFIER, "END", 36)
                        ),
                        new Ast.Statement.While(
                                new Ast.Expression.Access(Optional.empty(), "expr"),
                                Arrays.asList(
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt")),
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "another")),
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "again")))
                        )
                ),
                Arguments.of("While: literal expression",
                        Arrays.asList(
                                //WHILE TRUE DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "TRUE", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "another", 20),
                                new Token(Token.Type.OPERATOR, ";", 27),
                                new Token(Token.Type.IDENTIFIER, "again", 29),
                                new Token(Token.Type.OPERATOR, ";", 34),
                                new Token(Token.Type.IDENTIFIER, "END", 36)
                        ),
                        new Ast.Statement.While(
                                new Ast.Expression.Literal(Boolean.TRUE),
                                Arrays.asList(
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt")),
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "another")),
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "again")))
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testWhileParseException(String test, List<Token> tokens, ParseException exception) {
        testParseException(tokens, exception, Parser::parseStatement);
    }
    private static Stream<Arguments> testWhileParseException() {
        return Stream.of(
                Arguments.of("Missing DO",
                        Arrays.asList(
                                //WHILE expr stmt; END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "stmt", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                new Token(Token.Type.IDENTIFIER, "END", 17)
                        ),
                        new ParseException("Expected \"DO\" : invalid while loop. index: 11", 11)
                ),
                Arguments.of("Missing END",
                        Arrays.asList(
                                //WHILE expr DO stmt;
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 19", 19)
                ),
                Arguments.of("While: Missing Expression",
                        Arrays.asList(
                                //WHILE DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "DO", 6),
                                new Token(Token.Type.IDENTIFIER, "stmt", 9),
                                new Token(Token.Type.OPERATOR, ";", 13),
                                new Token(Token.Type.IDENTIFIER, "END", 15)
                        ),
                        new ParseException("Expected \"DO\" : invalid while loop. index: 9", 9) // existing DO was processed as the expression
                ),
                Arguments.of("While: Invalid Expression in Block",
                        Arrays.asList(
                                //WHILE expr DO ; END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.OPERATOR, ";", 14),
                                new Token(Token.Type.IDENTIFIER, "END", 16)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 14", 14) // existing DO was processed as the expression
                ),
                Arguments.of("While: Improper Termination of Loop",
                        Arrays.asList(
                                //WHILE expr DO stmt; DEFAULT
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                new Token(Token.Type.IDENTIFIER, "DEFAULT", 17)
                        ),
                        new ParseException("Expected \"END\" : invalid while loop. index: 17", 17) // existing DO was processed as the expression
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

    @Test
    void parserTestCaseFoo() {
        List<Token> input = Arrays.asList(
                //VAR i = -1;
                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                new Token(Token.Type.IDENTIFIER, "i", 4),
                new Token(Token.Type.OPERATOR, "=", 6),
                new Token(Token.Type.INTEGER, "-1", 8),
                new Token(Token.Type.OPERATOR, ";", 10),

                //VAL inc = 2;
                new Token(Token.Type.IDENTIFIER, "VAL", 12),
                new Token(Token.Type.IDENTIFIER, "inc", 16),
                new Token(Token.Type.OPERATOR, "=", 20),
                new Token(Token.Type.INTEGER, "2", 22),
                new Token(Token.Type.OPERATOR, ";", 23),

                //FUN foo() DO
                new Token(Token.Type.IDENTIFIER, "FUN", 25),
                new Token(Token.Type.IDENTIFIER, "foo", 29),
                new Token(Token.Type.OPERATOR, "(", 32),
                new Token(Token.Type.OPERATOR, ")", 33),
                new Token(Token.Type.IDENTIFIER, "DO", 35),

                //    WHILE i != 1 DO
                new Token(Token.Type.IDENTIFIER, "WHILE", 42),
                new Token(Token.Type.IDENTIFIER, "i", 48),
                new Token(Token.Type.OPERATOR, "!=", 50),
                new Token(Token.Type.INTEGER, "1", 53),
                new Token(Token.Type.IDENTIFIER, "DO", 55),

                //        IF i > 0 DO
                new Token(Token.Type.IDENTIFIER, "IF", 66),
                new Token(Token.Type.IDENTIFIER, "i", 69),
                new Token(Token.Type.OPERATOR, ">", 71),
                new Token(Token.Type.INTEGER, "0", 73),
                new Token(Token.Type.IDENTIFIER, "DO", 75),

                //            print(\"bar\");
                new Token(Token.Type.IDENTIFIER, "print", 90),
                new Token(Token.Type.OPERATOR, "(", 95),
                new Token(Token.Type.STRING, "\"bar\"", 96),
                new Token(Token.Type.OPERATOR, ")", 101),
                new Token(Token.Type.OPERATOR, ";", 102),

                //        END
                new Token(Token.Type.IDENTIFIER, "END", 112),

                //        i = i + inc;
                new Token(Token.Type.IDENTIFIER, "i",124),
                new Token(Token.Type.OPERATOR, "=", 126),
                new Token(Token.Type.IDENTIFIER, "i", 128),
                new Token(Token.Type.OPERATOR, "+", 130),
                new Token(Token.Type.IDENTIFIER, "inc", 132),
                new Token(Token.Type.OPERATOR, ";", 135),

                //    END
                new Token(Token.Type.IDENTIFIER, "END", 141),

                //END
                new Token(Token.Type.IDENTIFIER, "END", 145)
        );
        Ast.Source expected = new Ast.Source(
                Arrays.asList(
                        new Ast.Global("i", true, Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(-1)))),
                        new Ast.Global("inc", false, Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(2))))
                ),
                Arrays.asList(
                        new Ast.Function(
                                "foo",
                                Arrays.asList(),
                                Arrays.asList(
                                        new Ast.Statement.While(
                                                new Ast.Expression.Binary(
                                                        "!=",
                                                        new Ast.Expression.Access(Optional.empty(), "i"),
                                                        new Ast.Expression.Literal(BigInteger.ONE)
                                                ),
                                                Arrays.asList(
                                                        new Ast.Statement.If(
                                                                new Ast.Expression.Binary(
                                                                        ">",
                                                                        new Ast.Expression.Access(Optional.empty(), "i"),
                                                                        new Ast.Expression.Literal(BigInteger.ZERO)
                                                                ),
                                                                Arrays.asList(
                                                                        new Ast.Statement.Expression(
                                                                                new Ast.Expression.Function(
                                                                                        "print",
                                                                                        Arrays.asList(
                                                                                                new Ast.Expression.Literal("bar")
                                                                                        )
                                                                                )
                                                                        )
                                                                ),
                                                                Arrays.asList()
                                                        ),
                                                        new Ast.Statement.Assignment(
                                                                new Ast.Expression.Access(
                                                                        Optional.empty(),
                                                                        "i"),
                                                                new Ast.Expression.Binary(
                                                                        "+",
                                                                        new Ast.Expression.Access(Optional.empty(), "i"),
                                                                        new Ast.Expression.Access(Optional.empty(), "inc")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
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

    private static <T extends Ast> void testParseException(List<Token> tokens, Exception exception, Function<Parser, T> function) {
        Parser parser = new Parser(tokens);
        ParseException pe = Assertions.assertThrows(ParseException.class, () -> function.apply(parser));
        Assertions.assertEquals(exception, pe);
    }
}
