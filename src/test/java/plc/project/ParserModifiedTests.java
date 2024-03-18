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
 *
 * Tests the TYPED Parser grammar.
 */
final class ParserModifiedTests {

    @ParameterizedTest
    @MethodSource
    void testSource(String test, List<Token> tokens, Ast.Source expected) {
        test(tokens, expected, Parser::parseSource);
    }

    // TODO: test globals
    private static Stream<Arguments> testSource() {
        return Stream.of(
                Arguments.of("Zero Statements",
                        Arrays.asList(),
                        new Ast.Source(Arrays.asList(), Arrays.asList())
                ),
                Arguments.of("Global - Immutable",
                        Arrays.asList(
                                //VAL name: Type = expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 9),
                                new Token(Token.Type.IDENTIFIER, "Type", 11),
                                new Token(Token.Type.OPERATOR, "=", 15),
                                new Token(Token.Type.IDENTIFIER, "expr", 17),
                                new Token(Token.Type.OPERATOR, ";", 21)
                        ),
                        new Ast.Source(
                                Arrays.asList(new Ast.Global("name", "Type", false, Optional.of(new Ast.Expression.Access(Optional.empty(), "expr")))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("Function",
                        Arrays.asList(
                                //FUN name(): Type DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.OPERATOR, ":", 10),
                                new Token(Token.Type.IDENTIFIER, "Type", 12),
                                new Token(Token.Type.IDENTIFIER, "DO", 17),
                                new Token(Token.Type.IDENTIFIER, "stmt", 20),
                                new Token(Token.Type.OPERATOR, ";", 24),
                                new Token(Token.Type.IDENTIFIER, "END", 26)
                        ),
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(new Ast.Function("name", Arrays.asList(), Arrays.asList(), Optional.of("Type"), Arrays.asList(
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt"))
                                )))
                        )
                ),
                Arguments.of("Global - Mutable (Declaration)",
                        Arrays.asList(
                                //VAR name : Type;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ";", 21)
                        ),
                        new Ast.Source(
                                Arrays.asList(new Ast.Global("name", "Type", true, Optional.empty())),
                                Arrays.asList()
                        )
                ),
                Arguments.of("Global - Mutable (Initialization)",
                        Arrays.asList(
                                //VAR name : Type = expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new Ast.Source(
                                Arrays.asList(new Ast.Global("name", "Type", true, Optional.of(new Ast.Expression.Access(Optional.empty(), "expr")))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("List (One Element)",
                        Arrays.asList(
                                //VAL name : Type = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                                "Type",
                                                true,
                                                Optional.of(new Ast.Expression.PlcList(Arrays.asList(
                                                        new Ast.Expression.Access(Optional.empty(), "expr")))))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("List (Two Elements)",
                        Arrays.asList(
                                //VAL name : Type = [expr1,expr2];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                                "Type",
                                                true,
                                                Optional.of(new Ast.Expression.PlcList(Arrays.asList(
                                                        new Ast.Expression.Access(Optional.empty(), "expr1"),
                                                        new Ast.Expression.Access(Optional.empty(), "expr2")))))),
                                Arrays.asList()
                        )
                ),
                Arguments.of("List (Multiple Elements)",
                        Arrays.asList(
                                //VAL name : Type = [expr1,expr2,expr3];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                                "Type",
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
                                Arrays.asList(new Ast.Function("name", Arrays.asList(), List.of(), Optional.empty(), Arrays.asList(
                                        new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt"))
                                )))
                        )
                ),
                Arguments.of("Function (1 Parameter)",
                        Arrays.asList(
                                //FUN name(param1 : Type) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                        List.of("Type"),
                                        Optional.empty(),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt"))
                                        )))
                        )
                ),
                Arguments.of("Function (Multiple Parameters)",
                        Arrays.asList(
                                //FUN name(param1 : Type1, param2 : Type2, param3 : Type3) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type1", 17),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type2", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type3", 17),
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
                                        List.of("Type1", "Type2", "Type3"),
                                        Optional.empty(),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt"))
                                        )))
                        )
                ),
                Arguments.of("Function (Multiple Parameters) with return type",
                        Arrays.asList(
                                //FUN name(param1 : Type1, param2 : Type2, param3 : Type3) : ReturnType DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type1", 17),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type2", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type3", 17),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "ReturnType", 17),
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
                                        List.of("Type1", "Type2", "Type3"),
                                        Optional.of("ReturnType"),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Access(Optional.empty(), "stmt"))
                                        )))
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
                Arguments.of("List - Missing :",
                        Arrays.asList(
                                //VAL name Type = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected ':' : invalid list definition. index: 17", 17)
                ),
                Arguments.of("List - Invalid Type",
                        Arrays.asList(
                                //VAL name : ; = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.OPERATOR, ";", 17),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected (type) Identifier : invalid list definition. index: 17", 17)
                ),
                Arguments.of("Mutable (Declaration) - missing :",
                        Arrays.asList(
                                //VAR name Type;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ";", 21)
                        ),
                        new ParseException("Expected ':' : invalid list definition. index: 17", 17)
                ),
                Arguments.of("Mutable (Declaration) - invalid type",
                        Arrays.asList(
                                //VAR name : ;;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 16),
                                new Token(Token.Type.OPERATOR, ";", 201),
                                new Token(Token.Type.OPERATOR, ";", 21)
                        ),
                        new ParseException("Expected (type) Identifier : invalid list definition. index: 201", 201)
                ),
                Arguments.of("Mutable (Initialization) - missing :",
                        Arrays.asList(
                                //VAR name Type = expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected ':' : invalid list definition. index: 17", 17)
                ),
                Arguments.of("Mutable (Initialization) - Invalid Type",
                        Arrays.asList(
                                //VAR name : Type = expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.OPERATOR, ";", 201),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected (type) Identifier : invalid list definition. index: 201", 201)
                ),
                Arguments.of("Function - invalid return type",
                        Arrays.asList(
                                //FUN name(param1 : Type) : ; DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ")", 15),
                                new Token(Token.Type.OPERATOR, ":", 16),
                                new Token(Token.Type.OPERATOR, ";", 200),
                                new Token(Token.Type.IDENTIFIER, "DO", 16),
                                new Token(Token.Type.IDENTIFIER, "stmt", 19),
                                new Token(Token.Type.OPERATOR, ";", 23),
                                new Token(Token.Type.IDENTIFIER, "END", 25)
                        ),
                        new ParseException("Expected return type Identifier : invalid function definition. index: 200", 200)
                ),
                Arguments.of("Function (one param) - missing :",
                        Arrays.asList(
                                //FUN name(param1 Type) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ")", 15),
                                new Token(Token.Type.IDENTIFIER, "DO", 16),
                                new Token(Token.Type.IDENTIFIER, "stmt", 19),
                                new Token(Token.Type.OPERATOR, ";", 23),
                                new Token(Token.Type.IDENTIFIER, "END", 25)
                        ),
                        new ParseException("Expected ':' : invalid function definition at first parameter. index: 17", 17)
                ),
                Arguments.of("Function (one param) - invalid identifier",
                        Arrays.asList(
                                //FUN name(param1 : ;) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.OPERATOR, ";", 17),
                                new Token(Token.Type.OPERATOR, ")", 15),
                                new Token(Token.Type.IDENTIFIER, "DO", 16),
                                new Token(Token.Type.IDENTIFIER, "stmt", 19),
                                new Token(Token.Type.OPERATOR, ";", 23),
                                new Token(Token.Type.IDENTIFIER, "END", 25)
                        ),
                        new ParseException("Expected (type) Identifier : invalid function definition at first parameter. index: 17", 17)
                ),
                Arguments.of("Function (one param) - missing :",
                        Arrays.asList(
                                //FUN name(param1 : Type1, param2 Type2, param3 : Type3) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type1", 17),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.IDENTIFIER, "Type2", 201),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type3", 17),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.IDENTIFIER, "DO", 31),
                                new Token(Token.Type.IDENTIFIER, "stmt", 34),
                                new Token(Token.Type.OPERATOR, ";", 38),
                                new Token(Token.Type.IDENTIFIER, "END", 40)
                        ),
                        new ParseException("Expected ':' : invalid function definition. index: 201", 201)
                ),
                Arguments.of("Function (one param) - invalid identifier",
                        Arrays.asList(
                                //FUN name(param1 : Type1, param2 : ;, param3 : Type3) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type1", 17),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.OPERATOR, ";", 201),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type3", 17),
                                new Token(Token.Type.OPERATOR, ")", 29),
                                new Token(Token.Type.IDENTIFIER, "DO", 31),
                                new Token(Token.Type.IDENTIFIER, "stmt", 34),
                                new Token(Token.Type.OPERATOR, ";", 38),
                                new Token(Token.Type.IDENTIFIER, "END", 40)
                        ),
                        new ParseException("Expected (type) Identifier : invalid function definition. index: 201", 201)
                ),
                Arguments.of("Global - Immutable - Missing :",
                        Arrays.asList(
                                //VAL name Type = expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.IDENTIFIER, "Type", 11),
                                new Token(Token.Type.OPERATOR, "=", 15),
                                new Token(Token.Type.IDENTIFIER, "expr", 17),
                                new Token(Token.Type.OPERATOR, ";", 21)
                        ),
                        new ParseException("Expected ':' : invalid list definition. index: 11", 11)
                ),
                Arguments.of("Global - Immutable - Invalid Type",
                        Arrays.asList(
                                //VAL name : ; = expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 9),
                                new Token(Token.Type.OPERATOR, ";", 11),
                                new Token(Token.Type.OPERATOR, "=", 15),
                                new Token(Token.Type.IDENTIFIER, "expr", 17),
                                new Token(Token.Type.OPERATOR, ";", 21)
                        ),
                        new ParseException("Expected (type) Identifier : invalid list definition. index: 11", 11)
                ),
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
                                //VAL name : Type expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected '=' : invalid immutable definition. index: 11", 11)
                ),
                Arguments.of("Global (Immutable): Missing Expression",
                        Arrays.asList(
                                //VAL name : Type = ;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 15", 15)
                ),
                Arguments.of("Global (Immutable): Invalid Expression",
                        Arrays.asList(
                                //VAL name : Type = ; ;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.OPERATOR, ";", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 11", 11)
                ),
                Arguments.of("Global (Immutable): Missing Semicolon",
                        Arrays.asList(
                                //VAL name : Type = expr
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //VAR name : Type
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17)
                        ),
                        new ParseException("Expected ';' : invalid mutable definition. index: 21", 21)
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
                                //VAR name : Type expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected ';' : invalid mutable definition. index: 11", 11)
                ),
                Arguments.of("Global (Mutable/Initialization): Missing Expression",
                        Arrays.asList(
                                //VAR name : Type = ;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 15", 15)
                ),
                Arguments.of("Global (Mutable/Initialization): Invalid Expression",
                        Arrays.asList(
                                //VAR name : Type = =;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.OPERATOR, "=", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 11", 11)
                ),
                Arguments.of("Global (Mutable/Initialization): Missing Semicolon",
                        Arrays.asList(
                                //VAR name : Type = expr
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                        new ParseException("Expected (name) Identifier : invalid list definition. index: 5", 5)
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
                        new ParseException("Expected (name) Identifier : invalid list definition. index: 10", 10)
                ),
                Arguments.of("List (One Element): Missing =",
                        Arrays.asList(
                                //VAL name : Type [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected '=' : invalid list definition. index: 11", 11)
                ),
                Arguments.of("List (One Element): Missing [",
                        Arrays.asList(
                                //VAL name : Type = expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected '[' : invalid list definition. index: 12", 12)
                ),
                Arguments.of("List (One Element): Missing Expression",
                        Arrays.asList(
                                //VAL name : Type = [];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected valid primary expression : no literal, group, function, or access found. index: 17", 17)
                ),
                Arguments.of("List (One Element): Invalid Expression",
                        Arrays.asList(
                                //VAL name : Type = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //VAL name : Type = [expr;
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        new ParseException("Expected ']' : invalid list definition. index: 18", 18)
                ),
                Arguments.of("List (One Element): Missing Semicolon",
                        Arrays.asList(
                                //VAL name : Type = [expr]
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17)
                        ),
                        new ParseException("Expected ';' : invalid list definition. index: 18", 18)
                ),
                Arguments.of("List (One Element): Leading Comma",
                        Arrays.asList(
                                //VAL name : Type = [,expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //VAL name : Type = [expr,];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //VAL name : Type = [expr1 expr2 ,expr3 ];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //VAL name : Type = [expr1,expr2  expr3];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //VAL name : Type = [,expr1,expr2,expr3];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //VAL name : Type = [expr1,expr2,expr3,];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //FUN name( DO : Type stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //FUN name(param1 : Type,) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //FUN name(param1 : Type, param2 : Type, param3: Type) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.OPERATOR, ";", 23),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //FUN name(param1 : Type param2: Type, param3 : Type) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //FUN name(param1 : Type, param2 : Type param3 : Type) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //FUN name(param1 : Type, param2 : Type, param3 : Type) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ",", 9),
                                new Token(Token.Type.IDENTIFIER, "param1", 10),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //FUN name(param1 : Type, param2 : Type, param3 : Type,) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //VAL name : Type = expr;
                                new Token(Token.Type.IDENTIFIER, "VAL", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                //VAR name : Type;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                //VAR name : Type = expr;
                                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                //VAL name : Type = [expr];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr", 12),
                                new Token(Token.Type.OPERATOR, "]", 17),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                //VAL name : Type = [expr1,expr2];
                                new Token(Token.Type.IDENTIFIER, "LIST", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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
                                //VAL name : Type = [expr1,expr2,expr3];
                                new Token(Token.Type.IDENTIFIER, "LIST", 250),
                                new Token(Token.Type.IDENTIFIER, "name", 5),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, "=", 10),
                                new Token(Token.Type.OPERATOR, "[", 11),
                                new Token(Token.Type.IDENTIFIER, "expr1", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr2", 18),
                                new Token(Token.Type.OPERATOR, ",", 24),
                                new Token(Token.Type.IDENTIFIER, "expr3", 25),
                                new Token(Token.Type.OPERATOR, "]", 30),
                                new Token(Token.Type.OPERATOR, ";", 31),
                                //FUN name(param1 : Type) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ")", 15),
                                new Token(Token.Type.IDENTIFIER, "DO", 16),
                                new Token(Token.Type.IDENTIFIER, "stmt", 19),
                                new Token(Token.Type.OPERATOR, ";", 23),
                                new Token(Token.Type.IDENTIFIER, "END", 25),
                                //FUN name(param1 : Type, param2 : Type, param3 : Type) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FUN", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "param1", 9),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ",", 15),
                                new Token(Token.Type.IDENTIFIER, "param2", 17),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
                                new Token(Token.Type.OPERATOR, ",", 22),
                                new Token(Token.Type.IDENTIFIER, "param3", 23),
                                new Token(Token.Type.OPERATOR, ":", 15),
                                new Token(Token.Type.IDENTIFIER, "Type", 17),
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

    // unchanged
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

    // done + tested
    @ParameterizedTest
    @MethodSource
    void testDeclarationStatement(String test, List<Token> tokens, Ast.Statement.Declaration expected) {
        test(tokens, expected, Parser::parseStatement);
    }

    private static Stream<Arguments> testDeclarationStatement() {
        return Stream.of(
                Arguments.of("Definition",
                        Arrays.asList(
                                //LET name: Type;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 8),
                                new Token(Token.Type.IDENTIFIER, "Type", 10),
                                new Token(Token.Type.OPERATOR, ";", 14)
                        ),
                        new Ast.Statement.Declaration("name", Optional.of("Type"), Optional.empty())
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
                        new Ast.Statement.Declaration("name", Optional.empty(), Optional.of(new Ast.Expression.Access(Optional.empty(), "expr")))
                ),
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
                ),
                Arguments.of("No Type/Value",
                        Arrays.asList(
                                // Let name;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ";", 9)
                        ),
                        new Ast.Statement.Declaration("name", Optional.empty(), Optional.empty()))
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
                ),
                Arguments.of("Invalid type",
                        Arrays.asList(
                                // Let name : 1 = expr;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, ":", 9),
                                new Token(Token.Type.INTEGER, "1", 11),
                                new Token(Token.Type.OPERATOR, "=", 13),
                                new Token(Token.Type.IDENTIFIER, "expr", 15),
                                new Token(Token.Type.OPERATOR, ";", 19)
                        ),
                        new ParseException("Expected Identifier : invalid declaration type specification. index: 11", 11))
        );
    }

    // unchanged
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

    // unchanged
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

    // unchanged
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

    // unchanged
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
                )
        );
    }

    // unchanged
    @ParameterizedTest
    @MethodSource
    void testLiteralExpression(String test, List<Token> tokens, Ast.Expression.Literal expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    private static Stream<Arguments> testLiteralExpression() {
        return Stream.of(
                Arguments.of("Boolean Literal",
                        Arrays.asList(new Token(Token.Type.IDENTIFIER, "TRUE", 0)),
                        new Ast.Expression.Literal(Boolean.TRUE)
                ),
                Arguments.of("Integer Literal",
                        Arrays.asList(new Token(Token.Type.INTEGER, "1", 0)),
                        new Ast.Expression.Literal(new BigInteger("1"))
                ),
                Arguments.of("Decimal Literal",
                        Arrays.asList(new Token(Token.Type.DECIMAL, "2.0", 0)),
                        new Ast.Expression.Literal(new BigDecimal("2.0"))
                ),
                Arguments.of("Character Literal",
                        Arrays.asList(new Token(Token.Type.CHARACTER, "'c'", 0)),
                        new Ast.Expression.Literal('c')
                ),
                Arguments.of("String Literal",
                        Arrays.asList(new Token(Token.Type.STRING, "\"string\"", 0)),
                        new Ast.Expression.Literal("string")
                ),
                Arguments.of("Escape Character",
                        Arrays.asList(new Token(Token.Type.STRING, "\"Hello,\\nWorld!\"", 0)),
                        new Ast.Expression.Literal("Hello,\nWorld!")
                )
        );
    }

    // unchanged
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
                                new Ast.Expression.Access(Optional.empty(), "expr2")
                        ))
                )
        );
    }

    // unchanged
    @ParameterizedTest
    @MethodSource
    void testBinaryExpression(String test, List<Token> tokens, Ast.Expression.Binary expected) {
        test(tokens, expected, Parser::parseExpression);
    }

    // unchanged
    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("Binary And",
                        Arrays.asList(
                                //expr1 && expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "&&", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
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

    // unchanged
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
                Arguments.of("List Index Access",
                        Arrays.asList(
                                //list[expr]
                                new Token(Token.Type.IDENTIFIER, "list", 0),
                                new Token(Token.Type.OPERATOR, "[", 4),
                                new Token(Token.Type.IDENTIFIER, "expr", 5),
                                new Token(Token.Type.OPERATOR, "]", 9)
                        ),
                        new Ast.Expression.Access(Optional.of(new Ast.Expression.Access(Optional.empty(), "expr")), "list")
                )
        );
    }

    // unchanged
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
                )
        );
    }

    @Test
    void testExample1() {
        List<Token> input = Arrays.asList(
                /* VAR first: Integer = 1;
                 * FUN main(): Integer DO
                 *     WHILE first != 10 DO
                 *         print(first);
                 *         first = first + 1;
                 *     END
                 * END
                 */
                //VAR first: Integer = 1;
                new Token(Token.Type.IDENTIFIER, "VAR", 0),
                new Token(Token.Type.IDENTIFIER, "first", 4),
                new Token(Token.Type.OPERATOR, ":", 10),
                new Token(Token.Type.IDENTIFIER, "Integer", 11),
                new Token(Token.Type.OPERATOR, "=", 19),
                new Token(Token.Type.INTEGER, "1", 21),
                new Token(Token.Type.OPERATOR, ";", 22),
                //FUN main(): Integer DO
                new Token(Token.Type.IDENTIFIER, "FUN", 24),
                new Token(Token.Type.IDENTIFIER, "main", 28),
                new Token(Token.Type.OPERATOR, "(", 32),
                new Token(Token.Type.OPERATOR, ")", 33),
                new Token(Token.Type.OPERATOR, ":", 34),
                new Token(Token.Type.IDENTIFIER, "Integer", 36),
                new Token(Token.Type.IDENTIFIER, "DO", 44),
                //    WHILE first != 10 DO
                new Token(Token.Type.IDENTIFIER, "WHILE", 51),
                new Token(Token.Type.IDENTIFIER, "first", 57),
                new Token(Token.Type.OPERATOR, "!=", 63),
                new Token(Token.Type.INTEGER, "10", 66),
                new Token(Token.Type.IDENTIFIER, "DO", 69),
                //        print(first);
                new Token(Token.Type.IDENTIFIER, "print", 80),
                new Token(Token.Type.OPERATOR, "(", 85),
                new Token(Token.Type.IDENTIFIER, "first", 86),
                new Token(Token.Type.OPERATOR, ")", 91),
                new Token(Token.Type.OPERATOR, ";", 92),
                //        first = first + 1;
                new Token(Token.Type.IDENTIFIER, "first", 102),
                new Token(Token.Type.OPERATOR, "=", 108),
                new Token(Token.Type.IDENTIFIER, "first", 110),
                new Token(Token.Type.OPERATOR, "+", 116),
                new Token(Token.Type.INTEGER, "1", 118),
                new Token(Token.Type.OPERATOR, ";", 119),
                //    END
                new Token(Token.Type.IDENTIFIER, "END", 125),
                //END
                new Token(Token.Type.IDENTIFIER, "END", 129)
        );
        Ast.Source expected = new Ast.Source(
                Arrays.asList(new Ast.Global("first", "Integer", true, Optional.of(new Ast.Expression.Literal(BigInteger.ONE)))),
                Arrays.asList(new Ast.Function("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
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

    private static <T extends Ast> void testParseException(List<Token> tokens, Exception exception, Function<Parser, T> function) {
        Parser parser = new Parser(tokens);
        ParseException pe = Assertions.assertThrows(ParseException.class, () -> function.apply(parser));
        Assertions.assertEquals(exception, pe);
    }
}
