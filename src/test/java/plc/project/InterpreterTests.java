package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

final class InterpreterTests {

    @ParameterizedTest
    @MethodSource
    void testSource(String test, Ast.Source ast, Object expected) {
        test(ast, expected, new Scope(null));
    }

    private static Stream<Arguments> testSource() {
        return Stream.of(
                // FUN main() DO RETURN 0; END
                Arguments.of("Main", new Ast.Source(
                        Arrays.asList(),
                        Arrays.asList(new Ast.Function("main", Arrays.asList(), Arrays.asList(
                                new Ast.Statement.Return(new Ast.Expression.Literal(BigInteger.ZERO)))
                        ))
                ), BigInteger.ZERO),
                // VAR x = 1; VAR y = 10; FUN main() DO x + y; END
                Arguments.of("Globals & No Return", new Ast.Source(
                        Arrays.asList(
                                new Ast.Global("x", true, Optional.of(new Ast.Expression.Literal(BigInteger.ONE))),
                                new Ast.Global("y", true, Optional.of(new Ast.Expression.Literal(BigInteger.TEN)))
                        ),
                        Arrays.asList(new Ast.Function("main", Arrays.asList(), Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Binary("+",
                                        new Ast.Expression.Access(Optional.empty(), "x"),
                                        new Ast.Expression.Access(Optional.empty(), "y")                                ))
                        )))
                ), Environment.NIL.getValue()),
                // missing main
                // VAR x = 1; VAR y = 10; FUN main() DO x + y; END
                Arguments.of("Globals & No Return", new Ast.Source(
                        Arrays.asList(
                                new Ast.Global("x", true, Optional.of(new Ast.Expression.Literal(BigInteger.ONE))),
                                new Ast.Global("y", true, Optional.of(new Ast.Expression.Literal(BigInteger.TEN)))
                        ),
                        Arrays.asList(new Ast.Function("notMain", Arrays.asList(), Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Binary("+",
                                        new Ast.Expression.Access(Optional.empty(), "x"),
                                        new Ast.Expression.Access(Optional.empty(), "y")                                ))
                        )))
                ), null),
                // only globals (missing main)
                // VAR x = 1; VAR y = 10; FUN main() DO x + y; END
                Arguments.of("Globals & No Return", new Ast.Source(
                        Arrays.asList(
                                new Ast.Global("x", true, Optional.of(new Ast.Expression.Literal(BigInteger.ONE))),
                                new Ast.Global("y", true, Optional.of(new Ast.Expression.Literal(BigInteger.TEN)))
                        ),
                        Arrays.asList()
                ), null),
                // Function Scope (Given)
                //    VAR x = 1;
                //    VAR y = 2;
                //    VAR z = 3;
                //    FUN f(z) DO
                //        RETURN x + y + z;
                //    END
                //    FUN main() DO
                //        LET y = 4;
                //        RETURN f(5);
                //    END
                Arguments.of("Globals & No Return", new Ast.Source(
                        Arrays.asList(
                                new Ast.Global("x", true, Optional.of(new Ast.Expression.Literal(new BigInteger("1")))),
                                new Ast.Global("y", true, Optional.of(new Ast.Expression.Literal(new BigInteger("2")))),
                                new Ast.Global("z", true, Optional.of(new Ast.Expression.Literal(new BigInteger("3"))))
                        ),
                        Arrays.asList(
                                new Ast.Function("f", List.of("z"), List.of(new Ast.Statement.Return(
                                        new Ast.Expression.Binary("+",
                                                new Ast.Expression.Binary("+",
                                                        new Ast.Expression.Access(Optional.empty(), "x"),
                                                        new Ast.Expression.Access(Optional.empty(), "y")),
                                                new Ast.Expression.Access(Optional.empty(), "z"))
                                ))),
                                new Ast.Function("main", List.of(), List.of(
                                        new Ast.Statement.Declaration("y", Optional.of(new Ast.Expression.Literal(new BigInteger("4")))),
                                        new Ast.Statement.Return(new Ast.Expression.Function("f", List.of(new Ast.Expression.Literal(new BigInteger("5")))))
                                ))
                        )
                ), new BigInteger("8"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGlobal(String test, Ast.Global ast, Object expected) {
        Scope scope = test(ast, Environment.NIL.getValue(), new Scope(null)); // confirms Interpreter returns NIL
        Assertions.assertEquals(expected, scope.lookupVariable(ast.getName()).getValue().getValue()); // checks values of globals
    }

    private static Stream<Arguments> testGlobal() {
        return Stream.of(
                // VAR name;
                Arguments.of("Mutable", new Ast.Global("name", true, Optional.empty()), Environment.NIL.getValue()),
                // VAL name = 1;
                Arguments.of("Immutable", new Ast.Global("name", false, Optional.of(new Ast.Expression.Literal(BigInteger.ONE))), BigInteger.ONE)
        );
    }

    @Test
    void testList() {
        // LIST list = [1, 5, 10];
        List<Object> expected = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.TEN);

        List<Ast.Expression> values = Arrays.asList(new Ast.Expression.Literal(BigInteger.ONE),
                                                    new Ast.Expression.Literal(BigInteger.valueOf(5)),
                                                    new Ast.Expression.Literal(BigInteger.TEN));

        Optional<Ast.Expression> value = Optional.of(new Ast.Expression.PlcList(values));
        Ast.Global ast = new Ast.Global("list", true, value);

        Scope scope = test(ast, Environment.NIL.getValue(), new Scope(null)); // confirms interpreter returns NIL
        Assertions.assertEquals(expected, scope.lookupVariable(ast.getName()).getValue().getValue()); // checks values in (global) list
    }

    @ParameterizedTest
    @MethodSource
    void testFunction(String test, Ast.Function ast, List<Environment.PlcObject> args, Object expected) {
        Scope scope = test(ast, Environment.NIL.getValue(), new Scope(null)); // confirms that visiting the Function itself returns NIL
        Assertions.assertEquals(expected, scope.lookupFunction(ast.getName(), args.size()).invoke(args).getValue()); // confirms that function returns proper value (or NIL if none)
    }

    private static Stream<Arguments> testFunction() {
        return Stream.of(
                // no arguments
                // FUN main() DO RETURN 0; END
                Arguments.of("Main",
                        new Ast.Function("main", Arrays.asList(), Arrays.asList(
                                new Ast.Statement.Return(new Ast.Expression.Literal(BigInteger.ZERO)))
                        ),
                        Arrays.asList(),
                        BigInteger.ZERO
                ),
                // one argument
                // FUN square(x) DO RETURN x * x; END
                Arguments.of("Arguments",
                        new Ast.Function("square", Arrays.asList("x"), Arrays.asList(
                                new Ast.Statement.Return(new Ast.Expression.Binary("*",
                                        new Ast.Expression.Access(Optional.empty(), "x"),
                                        new Ast.Expression.Access(Optional.empty(), "x")
                                ))
                        )),
                        Arrays.asList(Environment.create(BigInteger.TEN)),
                        BigInteger.valueOf(100)
                ),
                // multiple arguments
                // FUN square2(x, y) DO RETURN x * y; END
                Arguments.of("Arguments",
                        new Ast.Function("square2", Arrays.asList("x", "y"), Arrays.asList(
                                new Ast.Statement.Return(new Ast.Expression.Binary("*",
                                        new Ast.Expression.Access(Optional.empty(), "x"),
                                        new Ast.Expression.Access(Optional.empty(), "y")
                                ))
                        )),
                        Arrays.asList(Environment.create(BigInteger.TEN), Environment.create(BigInteger.TWO)),
                        BigInteger.valueOf(20)
                )
        );
    }

    @Test
    void testExpressionStatement() {

        PrintStream sysout = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Scope scope = new Scope(null);
        scope.defineFunction("logarithm", 1, args -> {

            // Alternate Type Checking: using instanceof operator
            Environment.PlcObject bd = Environment.create(args.getFirst().getValue());
            if(!(bd.getValue() instanceof BigDecimal))
                throw new RuntimeException("expected BigDecimal value");

            // it is a BigDecimal!
            BigDecimal result = BigDecimal.valueOf(Math.log(((BigDecimal)bd.getValue()).doubleValue()));

            return Environment.create(result);
        });

        // print("Hello, World!");
        System.setOut(new PrintStream(out));
        try {
            test(new Ast.Statement.Expression(
                    new Ast.Expression.Function("print", Arrays.asList(new Ast.Expression.Literal("Hello, World!")))
            ), Environment.NIL.getValue(), scope); // ensures statement expression returns NIL
            Assertions.assertEquals("Hello, World!" + System.lineSeparator(), out.toString()); // ensures print statement (expression call) actually happens
        } finally {
            System.setOut(sysout); // resets System's output stream even if exception is thrown
        }

        // nested function expression statement
        // print(logarithm(2
        System.setOut(new PrintStream(out));
        try {
            test(new Ast.Statement.Expression(
                    new Ast.Expression.Function("print", Arrays.asList(new Ast.Expression.Function(
                            "logarithm", Arrays.asList(new Ast.Expression.Literal(BigDecimal.TWO)))
                    ))
            ), Environment.NIL.getValue(), scope); // ensures statement expression returns NIL
            Assertions.assertEquals("Hello, World!" + System.lineSeparator() + BigDecimal.valueOf(Math.log(BigDecimal.TWO.doubleValue())) + System.lineSeparator(), out.toString()); // ensures print statement (expression call) actually happens
        } finally {
            System.setOut(sysout); // resets System's output stream even if exception is thrown
        }
    }

    @ParameterizedTest
    @MethodSource
    void testDeclarationStatement(String test, Ast.Statement.Declaration ast, Object expected) {
        Scope scope = test(ast, Environment.NIL.getValue(), new Scope(null)); // confirms Interpreter returns NIL
        Assertions.assertEquals(expected, scope.lookupVariable(ast.getName()).getValue().getValue()); // confirms variable declared to proper value (or NIL)
    }

    private static Stream<Arguments> testDeclarationStatement() {
        return Stream.of(
                // LET name;
                Arguments.of("Declaration",
                        new Ast.Statement.Declaration("name", Optional.empty()),
                        Environment.NIL.getValue()
                ),
                // LET name = 1;
                Arguments.of("Initialization",
                        new Ast.Statement.Declaration("name", Optional.of(new Ast.Expression.Literal(BigInteger.ONE))),
                        BigInteger.ONE
                )
        );
    }

    @Test
    void testAssignmentStatement() {
        // variable = 1;
        Scope scope = new Scope(null);
        scope.defineVariable("variable", true, Environment.create("variable"));
        test(new Ast.Statement.Assignment(
                new Ast.Expression.Access(Optional.empty(),"variable"),
                new Ast.Expression.Literal(BigInteger.ONE)
        ), Environment.NIL.getValue(), scope); // ensures Interpreter returns NIL
        Assertions.assertEquals(BigInteger.ONE, scope.lookupVariable("variable").getValue().getValue()); // ensures variable is assigned to value

        // immutable var access
        // variable = 1;
        Scope scope2 = new Scope(null);
        scope2.defineVariable("variable", false, Environment.create("variable"));
        test(new Ast.Statement.Assignment(
                new Ast.Expression.Access(Optional.empty(),"variable"),
                new Ast.Expression.Literal(BigInteger.ONE)
        ), null, scope2); // ensures Interpreter returns NIL

        // non-access receiver
        // 3 = 1;
        Scope scope3 = new Scope(null);
        test(new Ast.Statement.Assignment(
                new Ast.Expression.Literal(BigInteger.valueOf(3)),
                new Ast.Expression.Literal(BigInteger.ONE)
        ), null, scope3); // ensures Interpreter returns NIL
    }

    @Test
    void testListAssignmentStatement() {
        // list[2] = 3;
        List<Object> expected = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.valueOf(3));
        List<Object> list = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.TEN);

        Scope scope = new Scope(null);
        scope.defineVariable("list", true, Environment.create(list));
        test(new Ast.Statement.Assignment(
                new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(2))), "list"),
                new Ast.Expression.Literal(BigInteger.valueOf(3))
        ), Environment.NIL.getValue(), scope); // ensures Interpreter returns NIL

        Assertions.assertEquals(expected, scope.lookupVariable("list").getValue().getValue()); // ensures list index is assigned to value

        // list[0] = 3;
        List<Object> expected2 = Arrays.asList(BigInteger.valueOf(3), BigInteger.valueOf(5), BigInteger.valueOf(3));
        List<Object> list2 = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.valueOf(3));

        Scope scope2 = new Scope(null);
        scope2.defineVariable("list", true, Environment.create(list2));
        test(new Ast.Statement.Assignment(
                new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(0))), "list"),
                new Ast.Expression.Literal(BigInteger.valueOf(3))
        ), Environment.NIL.getValue(), scope2); // ensures Interpreter returns NIL

        Assertions.assertEquals(expected2, scope2.lookupVariable("list").getValue().getValue()); // ensures list index is assigned to value

        // out of bounds
        // list[-1] = 3;
        List<Object> list3 = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.valueOf(3));

        Scope scope3 = new Scope(null);
        scope3.defineVariable("list", true, Environment.create(list3));
        test(new Ast.Statement.Assignment(
                new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(-1))), "list"),
                new Ast.Expression.Literal(BigInteger.valueOf(3))
        ), null, scope2); // ensures Interpreter returns NIL

        // out of bounds
        // list[3] = 3;
        List<Object> list4 = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.valueOf(3));

        Scope scope4 = new Scope(null);
        scope4.defineVariable("list", true, Environment.create(list4));
        test(new Ast.Statement.Assignment(
                new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(3))), "list"),
                new Ast.Expression.Literal(BigInteger.valueOf(3))
        ), null, scope2); // ensures Interpreter returns NIL

        // non-access receiver
        // (list[2]) = 3;
        List<Object> list5 = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.valueOf(3));

        Scope scope5 = new Scope(null);
        scope5.defineVariable("list", true, Environment.create(list5));
        test(new Ast.Statement.Assignment(
                new Ast.Expression.Group(new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(2))), "list")),
                new Ast.Expression.Literal(BigInteger.valueOf(3))
        ), null, scope2); // ensures Interpreter returns NIL
    }

    @ParameterizedTest
    @MethodSource
    void testIfStatement(String test, Ast.Statement.If ast, Object expected) {
        Scope scope = new Scope(null);
        scope.defineVariable("num", true, Environment.NIL);
        test(ast, Environment.NIL.getValue(), scope); // ensures Interpreter returns NIL
        Assertions.assertEquals(expected, scope.lookupVariable("num").getValue().getValue()); // ensures variable num is assigned according to if
    }

    private static Stream<Arguments> testIfStatement() {
        return Stream.of(
                // IF TRUE DO num = 1; END
                Arguments.of("True Condition",
                        new Ast.Statement.If(
                                new Ast.Expression.Literal(true),
                                Arrays.asList(new Ast.Statement.Assignment(new Ast.Expression.Access(Optional.empty(),"num"), new Ast.Expression.Literal(BigInteger.ONE))),
                                Arrays.asList()
                        ),
                        BigInteger.ONE
                ),
                // IF FALSE DO ELSE num = 10; END
                Arguments.of("False Condition",
                        new Ast.Statement.If(
                                new Ast.Expression.Literal(false),
                                Arrays.asList(),
                                Arrays.asList(new Ast.Statement.Assignment(new Ast.Expression.Access(Optional.empty(),"num"), new Ast.Expression.Literal(BigInteger.TEN)))
                        ),
                        BigInteger.TEN
                )
        );
    }

    @Test
    void additionalIfTests() {
        // non-boolean condition
        // IF 12 DO ELSE num = 10; END
        Scope scope = new Scope(null);
        scope.defineVariable("num", true, Environment.NIL);
        Ast ast2 = new Ast.Statement.If(
                new Ast.Expression.Literal(BigInteger.valueOf(12)),
                Arrays.asList(),
                Arrays.asList(new Ast.Statement.Assignment(new Ast.Expression.Access(Optional.empty(),"num"), new Ast.Expression.Literal(BigInteger.TEN)))
        );
        test(ast2, null, scope);
    }

    @Test
    void testSwitchStatement() {
        // CASE 1: GIVEN
        // SWITCH letter CASE 'y': print("yes"); letter = 'n'; DEFAULT: print("no"); END
        Scope scope = new Scope(null);
        scope.defineVariable("letter", true, Environment.create('y'));

        List<Ast.Statement> statements = Arrays.asList(
                new Ast.Statement.Expression(new Ast.Expression.Function("print", Arrays.asList(new Ast.Expression.Literal("yes")))),
                new Ast.Statement.Assignment(new Ast.Expression.Access(Optional.empty(), "letter"),
                                             new Ast.Expression.Literal('n'))
        );

        List<Ast.Statement.Case> cases = Arrays.asList(
                new Ast.Statement.Case(Optional.of(new Ast.Expression.Literal('y')), statements),
                new Ast.Statement.Case(Optional.empty(), Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Function("print", Arrays.asList(new Ast.Expression.Literal("no"))))))
        );

        Ast.Statement.Switch ast = new Ast.Statement.Switch(new Ast.Expression.Access(Optional.empty(), "letter"), cases);

        PrintStream sysout = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try {
            test(ast, Environment.NIL.getValue(), scope); // ensures Interpreter returns NIL
            Assertions.assertEquals("yes" + System.lineSeparator(), out.toString()); // ensures "yes" was printed
        } finally {
            System.setOut(sysout);
        }

        Assertions.assertEquals('n', scope.lookupVariable("letter").getValue().getValue()); // ensures letter variable was assigned to 'n'

        // CASE 2: DEFAULT
        // SWITCH letter CASE 'y': print("yes"); letter = 'n'; DEFAULT: print("no"); END
        Scope scope2 = new Scope(null);
        scope2.defineVariable("letter", true, Environment.create('x'));

        List<Ast.Statement> statements2 = Arrays.asList(
                new Ast.Statement.Expression(new Ast.Expression.Function("print", Arrays.asList(new Ast.Expression.Literal("yes")))),
                new Ast.Statement.Assignment(new Ast.Expression.Access(Optional.empty(), "letter"),
                        new Ast.Expression.Literal('n'))
        );

        List<Ast.Statement.Case> cases2 = Arrays.asList(
                new Ast.Statement.Case(Optional.of(new Ast.Expression.Literal('y')), statements2),
                new Ast.Statement.Case(Optional.empty(), Arrays.asList(new Ast.Statement.Expression(new Ast.Expression.Function("print", Arrays.asList(new Ast.Expression.Literal("no"))))))
        );

        Ast.Statement.Switch ast2 = new Ast.Statement.Switch(new Ast.Expression.Access(Optional.empty(), "letter"), cases2);

        PrintStream sysout2 = System.out;
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out2));
        try {
            test(ast, Environment.NIL.getValue(), scope2); // ensures Interpreter returns NIL
            Assertions.assertEquals("no" + System.lineSeparator(), out2.toString()); // ensures "yes" was printed
        } finally {
            System.setOut(sysout2);
        }

        Assertions.assertEquals('x', scope2.lookupVariable("letter").getValue().getValue()); // ensures letter variable was assigned to 'n'
    }

    @Test
    void testWhileStatement() {
        // WHILE num < 10 DO num = num + 1; END
        Scope scope = new Scope(null);
        scope.defineVariable("num", true, Environment.create(BigInteger.ZERO));
        test(new Ast.Statement.While(
                new Ast.Expression.Binary("<",
                        new Ast.Expression.Access(Optional.empty(),"num"),
                        new Ast.Expression.Literal(BigInteger.TEN)
                ),
                Arrays.asList(new Ast.Statement.Assignment(
                        new Ast.Expression.Access(Optional.empty(),"num"),
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Access(Optional.empty(),"num"),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        )
                ))
        ),Environment.NIL.getValue(), scope); // ensures Interpreter returns NIL
        Assertions.assertEquals(BigInteger.TEN, scope.lookupVariable("num").getValue().getValue()); // ensures variable "num" has reached 10 by end of loop

        // non-boolean condition
        // WHILE num DO num = num + 1; END
        Scope scope2 = new Scope(null);
        scope2.defineVariable("num", true, Environment.create(BigInteger.ZERO));
        test(new Ast.Statement.While(
                        new Ast.Expression.Access(Optional.empty(),"num"),
                Arrays.asList(new Ast.Statement.Assignment(
                        new Ast.Expression.Access(Optional.empty(),"num"),
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Access(Optional.empty(),"num"),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        )
                ))
        ),null, scope2); // ensures Interpreter returns NIL
    }

    @ParameterizedTest
    @MethodSource
    void testLiteralExpression(String test, Ast ast, Object expected) {
        test(ast, expected, new Scope(null)); // ensures Interpreter returns literal value
    }

    private static Stream<Arguments> testLiteralExpression() {
        return Stream.of(
                // NIL
                Arguments.of("Nil", new Ast.Expression.Literal(null), Environment.NIL.getValue()), //remember, special case
                // TRUE
                Arguments.of("Boolean: true", new Ast.Expression.Literal(true), true),
                // FALSE
                Arguments.of("Boolean: false", new Ast.Expression.Literal(false), false),
                // 1
                Arguments.of("Integer", new Ast.Expression.Literal(BigInteger.ONE), BigInteger.ONE),
                // 1.0
                Arguments.of("Decimal", new Ast.Expression.Literal(BigDecimal.ONE), BigDecimal.ONE),
                // 'c'
                Arguments.of("Character", new Ast.Expression.Literal('c'), 'c'),
                // "string"
                Arguments.of("String", new Ast.Expression.Literal("string"), "string")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGroupExpression(String test, Ast ast, Object expected) {
        test(ast, expected, new Scope(null)); // ensures Interpreter returns evaluated value
    }

    private static Stream<Arguments> testGroupExpression() {
        return Stream.of(
                // (1)
                Arguments.of("Literal", new Ast.Expression.Group(new Ast.Expression.Literal(BigInteger.ONE)), BigInteger.ONE),
                // (1 + 10)
                Arguments.of("Binary",
                        new Ast.Expression.Group(new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        )),
                        BigInteger.valueOf(11)
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testBinaryExpression(String test, Ast ast, Object expected) {
        test(ast, expected, new Scope(null)); // ensures Interpreter returns evaluated value
    }

    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                // null && FALSE
                Arguments.of("Null",
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Literal(null),
                                new Ast.Expression.Literal(false)
                        ),
                        null
                ),
                // TRUE && FALSE
                Arguments.of("And: False",
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Literal(true),
                                new Ast.Expression.Literal(false)
                        ),
                        false
                ),
                // TRUE && TRUE
                Arguments.of("And: True",
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Literal(true),
                                new Ast.Expression.Literal(true)
                        ),
                        true
                ),
                // FALSE && undefined
                Arguments.of("And (Short Circuit)",
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Literal(false),
                                new Ast.Expression.Access(Optional.empty(), "undefined")
                        ),
                        false
                ),
                // 1 && TRUE
                Arguments.of("And (First Not Boolean)",
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(true)
                        ),
                        null
                ),
                // TRUE && 1
                Arguments.of("And (Second Not Boolean)",
                        new Ast.Expression.Binary("&&",
                                new Ast.Expression.Literal(true),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ),
                        null
                ),
                // FALSE || TRUE
                Arguments.of("Or: True",
                        new Ast.Expression.Binary("||",
                                new Ast.Expression.Literal(false),
                                new Ast.Expression.Literal(true)
                        ),
                        true
                ),
                // FALSE || FALSE
                Arguments.of("Or: False",
                        new Ast.Expression.Binary("||",
                                new Ast.Expression.Literal(false),
                                new Ast.Expression.Literal(false)
                        ),
                        false
                ),
                // TRUE || undefined
                Arguments.of("Or (Short Circuit)",
                        new Ast.Expression.Binary("||",
                                new Ast.Expression.Literal(true),
                                new Ast.Expression.Access(Optional.empty(), "undefined")
                        ),
                        true
                ),
                // 1 || TRUE
                Arguments.of("Or (Not Boolean First)",
                        new Ast.Expression.Binary("||",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(true)
                        ),
                        null
                ),
                // FALSE || 1
                Arguments.of("Or (Not Boolean Second)",
                        new Ast.Expression.Binary("||",
                                new Ast.Expression.Literal(false),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ),
                        null
                ),
                // 1 < 10
                Arguments.of("Less Than: True",
                        new Ast.Expression.Binary("<",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        true
                ),
                // 10 < 1
                Arguments.of("Less Than: False",
                        new Ast.Expression.Binary("<",
                                new Ast.Expression.Literal(BigInteger.TEN),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ),
                        false
                ),
                // Optional.of(1) < 10
                Arguments.of("Less Than: non-comparable",
                        new Ast.Expression.Binary("<",
                                new Ast.Expression.Literal(Optional.of(BigInteger.ONE)),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        null
                ),
                // 1.0 < 10
                Arguments.of("Less Than: Different Types",
                        new Ast.Expression.Binary("<",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        null
                ),
                // 1.0 < 10.0
                Arguments.of("Less Than: Decimal",
                        new Ast.Expression.Binary("<",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigDecimal.TEN)
                        ),
                        true
                ),
                // 'a' < 'b'
                Arguments.of("Less Than: Character",
                        new Ast.Expression.Binary("<",
                                new Ast.Expression.Literal('a'),
                                new Ast.Expression.Literal('b')
                        ),
                        true
                ),
                // "abc" < "def"
                Arguments.of("Less Than: String",
                        new Ast.Expression.Binary("<",
                                new Ast.Expression.Literal("abc"),
                                new Ast.Expression.Literal("def")
                        ),
                        true
                ),
                // 1 > 10
                Arguments.of("Greater Than: False",
                        new Ast.Expression.Binary(">",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        false
                ),
                // 10 > 1
                Arguments.of("Greater Than: True",
                        new Ast.Expression.Binary(">",
                                new Ast.Expression.Literal(BigInteger.TEN),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ),
                        true
                ),
                // Optional.of(1) > 10
                Arguments.of("Greater Than: non-comparable",
                        new Ast.Expression.Binary(">",
                                new Ast.Expression.Literal(Optional.of(BigInteger.ONE)),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        null
                ),
                // 1.0 > 10
                Arguments.of("Greater Than: Different Types",
                        new Ast.Expression.Binary(">",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        null
                ),
                // 1.0 > 10.0
                Arguments.of("Greater Than: Decimal",
                        new Ast.Expression.Binary(">",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigDecimal.TEN)
                        ),
                        false
                ),
                // 'a' > 'b'
                Arguments.of("Greater Than: Character",
                        new Ast.Expression.Binary(">",
                                new Ast.Expression.Literal('a'),
                                new Ast.Expression.Literal('b')
                        ),
                        false
                ),
                // "abc" > "def"
                Arguments.of("Greater Than: String",
                        new Ast.Expression.Binary(">",
                                new Ast.Expression.Literal("abc"),
                                new Ast.Expression.Literal("def")
                        ),
                        false
                ),
                // 1 == 10
                Arguments.of("Equal: False",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        false
                ),
                // 1 == 1
                Arguments.of("Equal: True",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ),
                        true
                ),
                // null == 1
                Arguments.of("Equal: null (false)",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal(null),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ),
                        false
                ),
                // null == null
                Arguments.of("Equal: null (true)",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal(null),
                                new Ast.Expression.Literal(null)
                        ),
                        true
                ),
                // 1.0 == 1.0
                Arguments.of("Equal: decimal (true)",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigDecimal.ONE)
                        ),
                        true
                ),
                // 1.0 == 10.0
                Arguments.of("Equal: decimal (false)",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigDecimal.TEN)
                        ),
                        false
                ),
                // 'a' == 'a'
                Arguments.of("Equal: char (true)",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal('a'),
                                new Ast.Expression.Literal('a')
                        ),
                        true
                ),
                // 'a' == 'b'
                Arguments.of("Equal: char (false)",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal('a'),
                                new Ast.Expression.Literal('b')
                        ),
                        false
                ),
                // "abc" == "abc"
                Arguments.of("Equal: char (true)",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal("abc"),
                                new Ast.Expression.Literal("abc")
                        ),
                        true
                ),
                // "abc == "def"
                Arguments.of("Equal: char (false)",
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Literal("abc"),
                                new Ast.Expression.Literal("def")
                        ),
                        false
                ),
                // 1 != 10
                Arguments.of("Not Equal: True",
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        true
                ),
                // 1 != 1
                Arguments.of("Not Equal: False",
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ),
                        false
                ),
                // null != 1
                Arguments.of("Not Equal: null (true)",
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Literal(null),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ),
                        true
                ),
                // null != null
                Arguments.of("Not Equal: null (false)",
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Literal(null),
                                new Ast.Expression.Literal(null)
                        ),
                        false
                ),
                // 1.0 != 1.0
                Arguments.of("Not Equal: decimal (false)",
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigDecimal.ONE)
                        ),
                        false
                ),
                // 1.0 != 10.0
                Arguments.of("Not Equal: decimal (true)",
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigDecimal.TEN)
                        ),
                        true
                ),
                // 'a' != 'a'
                Arguments.of("Not Equal: char (false)",
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Literal('a'),
                                new Ast.Expression.Literal('a')
                        ),
                        false
                ),
                // 'a' != 'b'
                Arguments.of("Not Equal: char (true)",
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Literal('a'),
                                new Ast.Expression.Literal('b')
                        ),
                        true
                ),
                // "abc" != "abc"
                Arguments.of("Not Equal: char (false)",
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Literal("abc"),
                                new Ast.Expression.Literal("abc")
                        ),
                        false
                ),
                // "abc != "def"
                Arguments.of("Not Equal: char (true)",
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Literal("abc"),
                                new Ast.Expression.Literal("def")
                        ),
                        true
                ),
                // "a" + "b"
                Arguments.of("Concatenation",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal("a"),
                                new Ast.Expression.Literal("b")
                        ),
                        "ab"
                ),
                // "abc " + "def"
                Arguments.of("Concatenation: multiple letters",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal("abc "),
                                new Ast.Expression.Literal("def")
                        ),
                        "abc def"
                ),
                // "a" + 1.0 ("1.0" defined)
                Arguments.of("Concatenation: plus decimal (weird)",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal("a"),
                                new Ast.Expression.Literal(new BigDecimal(1.0))
                        ),
                        "a1" // objects passed in test cases generate results in consistent manner, no matter objects passed (in other words, its fine)
                        // new BigDecimal(1.0) -> "a1"
                        // BigDecimal.ONE -> "a1"
                        // new BigDecimal("1.0") -> "a1.0"
                        // BigDecimal.valueOf(1.0) -> "a1.0"
                ),
                // "a" + 1.0 (BidDecimal.ONE defined)
                Arguments.of("Concatenation: plus decimal (weird)",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal("a"),
                                new Ast.Expression.Literal(BigDecimal.ONE)
                        ),
                        "a1" // see above for explanation
                ),
                // "a" + 1.1
                Arguments.of("Concatenation: plus decimal",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal("a"),
                                new Ast.Expression.Literal(new BigDecimal("1.1"))
                        ),
                        "a1.1"
                ),
                // "a" + 2
                Arguments.of("Concatenation: plus integer",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal("a"),
                                new Ast.Expression.Literal(BigInteger.TWO)
                        ),
                        "a2"
                ),
                // "a" + 'b'
                Arguments.of("Concatenation: plus char",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal("a"),
                                new Ast.Expression.Literal('b')
                        ),
                        "ab"
                ),
                // 2 + "a"
                Arguments.of("Concatenation: second string only",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal(BigInteger.TWO),
                                new Ast.Expression.Literal("a")
                        ),
                        "2a"
                ),
                // 'a' + 'b'
                Arguments.of("Concatenation: two chars",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal('a'),
                                new Ast.Expression.Literal('b')
                        ),
                        null
                ),
                // 'a' + 1
                Arguments.of("Concatenation: char with number",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal('a'),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ),
                        null
                ),
                // 1 + 10
                Arguments.of("Addition: Integer",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        BigInteger.valueOf(11)
                ),
                // 1.0 + 10.0
                Arguments.of("Addition: Decimal",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigDecimal.TEN)
                        ),
                        BigDecimal.valueOf(11)
                ),
                // 1.0 + 10
                Arguments.of("Addition: Mixed Types",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        null
                ),
                // 1.0 + null
                Arguments.of("Addition: null",
                        new Ast.Expression.Binary("+",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(null)
                        ),
                        null
                ),
                // 1 - 10
                Arguments.of("Subtraction: Integer",
                        new Ast.Expression.Binary("-",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        BigInteger.valueOf(-9)
                ),
                // 1.0 + 10.0
                Arguments.of("Subtraction: Decimal",
                        new Ast.Expression.Binary("-",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigDecimal.TEN)
                        ),
                        BigDecimal.valueOf(-9)
                ),
                // 1.0 - 10
                Arguments.of("Subtraction: Mixed Types",
                        new Ast.Expression.Binary("-",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        null
                ),
                // 1.0 - null
                Arguments.of("Subtraction: null",
                        new Ast.Expression.Binary("-",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(null)
                        ),
                        null
                ),
                // 1.0 - "abc"
                Arguments.of("Subtraction: String",
                        new Ast.Expression.Binary("-",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal("abc")
                        ),
                        null
                ),
                // 1 * 10
                Arguments.of("Multiple: Integer",
                        new Ast.Expression.Binary("*",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        BigInteger.valueOf(10)
                ),
                // 1.0 * 10.0
                Arguments.of("Multiply: Decimal",
                        new Ast.Expression.Binary("*",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigDecimal.TEN)
                        ),
                        BigDecimal.valueOf(10)
                ),
                // 1.0 * 10
                Arguments.of("Multiply: Mixed Types",
                        new Ast.Expression.Binary("*",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        null
                ),
                // 1.0 * null
                Arguments.of("Multiply: null",
                        new Ast.Expression.Binary("*",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(null)
                        ),
                        null
                ),
                // 1.0 * "abc"
                Arguments.of("Multiply: String",
                        new Ast.Expression.Binary("*",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal("abc")
                        ),
                        null
                ),
                // 1.2 / 3.4
                Arguments.of("Division: Given",
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Literal(new BigDecimal("1.2")),
                                new Ast.Expression.Literal(new BigDecimal("3.4"))
                        ),
                        new BigDecimal("0.4")
                ),
                // 1 / 10
                Arguments.of("Division: Integer (0)",
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Literal(BigInteger.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        BigInteger.valueOf(0)
                ),
                // 20 / 2
                Arguments.of("Division: Integer (non-0)",
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Literal(BigInteger.valueOf(20)),
                                new Ast.Expression.Literal(BigInteger.TWO)
                        ),
                        BigInteger.valueOf(10)
                ),
                // 1.0 / 10.0
                Arguments.of("Division: Decimal",
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigDecimal.TEN)
                        ),
                        BigDecimal.valueOf(0)
                ),
                // 10.0 / 3.0
                Arguments.of("Division: Decimal (another)",
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Literal(new BigDecimal(10.0)),
                                new Ast.Expression.Literal(new BigDecimal(3.0))
                        ),
                        BigDecimal.valueOf(3)
                        // behavior depends on initialization of BigDecimal in test case, this is fine because it behaves in a consistent manner for any particular one method
                ),
                // 25.0 / 10.0
                Arguments.of("Division: Decimal (another)",
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Literal(BigDecimal.valueOf(25)),
                                new Ast.Expression.Literal(BigDecimal.valueOf(10))
                        ),
                        BigDecimal.valueOf(2)
                        // see above for explanation
                ),
                // 25.0 / 10.0
                Arguments.of("Division: Given",
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Literal(new BigDecimal("1.2")),
                                new Ast.Expression.Literal(new BigDecimal("3.4"))
                        ),
                        new BigDecimal("0.4")
                        // see above for explanation
                ),
                // 1.0 / 10
                Arguments.of("Division: Mixed Types",
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        null
                ),
                // 1.0 / null
                Arguments.of("Division: null",
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal(null)
                        ),
                        null
                ),
                // 1.0 / "abc"
                Arguments.of("Division: String",
                        new Ast.Expression.Binary("/",
                                new Ast.Expression.Literal(BigDecimal.ONE),
                                new Ast.Expression.Literal("abc")
                        ),
                        null
                ),
                // 2 ^ 2
                Arguments.of("Exponent: Standard",
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Literal(BigInteger.valueOf(2)),
                                new Ast.Expression.Literal(BigInteger.valueOf(2))
                        ),
                        BigInteger.valueOf(4)
                ),
                // 2 ^ 0
                Arguments.of("Exponent: Zero Exponent",
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Literal(BigInteger.valueOf(2)),
                                new Ast.Expression.Literal(BigInteger.valueOf(0))
                        ),
                        BigInteger.valueOf(1)
                ),
                // 2 ^ -1
                Arguments.of("Exponent: Negative Exponent",
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Literal(BigInteger.valueOf(2)),
                                new Ast.Expression.Literal(BigInteger.valueOf(-1))
                        ),
                        new BigDecimal("0")
                ),
                // -2 ^ 2
                Arguments.of("Exponent: Negative Base (even)",
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Literal(BigInteger.valueOf(-2)),
                                new Ast.Expression.Literal(BigInteger.valueOf(2))
                        ),
                        BigInteger.valueOf(4)
                ),
                // -2 ^ 3
                Arguments.of("Exponent: Negative Base (odd)",
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Literal(BigInteger.valueOf(-2)),
                                new Ast.Expression.Literal(BigInteger.valueOf(3))
                        ),
                        BigInteger.valueOf(-8)
                ),
                // 2 ^ 2.0
                Arguments.of("Exponent: One Decimal",
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Literal(BigInteger.valueOf(2)),
                                new Ast.Expression.Literal(BigDecimal.valueOf(2))
                        ),
                        null
                ),
                // 2.0 ^ 2.0
                Arguments.of("Exponent: Two Decimals",
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Literal(BigDecimal.valueOf(2)),
                                new Ast.Expression.Literal(BigDecimal.valueOf(2))
                        ),
                        null
                ),
                // 2.0 ^ 'a'
                Arguments.of("Exponent: Char",
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Literal(BigDecimal.valueOf(2)),
                                new Ast.Expression.Literal('a')
                        ),
                        null
                ),
                // 2.0 ^ "abc"
                Arguments.of("Exponent: String",
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Literal(BigDecimal.valueOf(2)),
                                new Ast.Expression.Literal("abc")
                        ),
                        null
                ),
                // 2 ^ 1000000
                Arguments.of("Exponent: Huge RHS",
                        new Ast.Expression.Binary("^",
                                new Ast.Expression.Literal(new BigInteger("2")),
                                new Ast.Expression.Literal(new BigInteger("1000000"))
                        ),
                        new BigInteger("2").pow(1000000)
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAccessExpression(String test, Ast ast, Object expected) {
        Scope scope = new Scope(null);
        scope.defineVariable("variable", true, Environment.create("variable"));
        test(ast, expected, scope); // ensures Interpreter returns accessed value from variable
    }

    private static Stream<Arguments> testAccessExpression() {
        return Stream.of(
                // variable
                Arguments.of("Variable",
                        new Ast.Expression.Access(Optional.empty(), "variable"),
                        "variable"
                ),
                // variable2
                Arguments.of("Variable Missing",
                        new Ast.Expression.Access(Optional.empty(), "variable2"),
                        null
                )
        );
    }

    @Test
    void testListAccessExpression() {
        // list[1]
        List<Object> list = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.TEN);

        Scope scope = new Scope(null);
        scope.defineVariable("list", true, Environment.create(list));
        // ensures Interpreter returns proper value from list (at proper index)
        test(new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(1))), "list"), BigInteger.valueOf(5), scope);
    }

    @Test
    void testListAccessExpressionMissing() {
        // list[1]
        List<Object> list = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.TEN);

        Scope scope = new Scope(null);
        //scope.defineVariable("list", true, Environment.create(list)); // removed from scope for this test
        // ensures Interpreter returns proper value from list (at proper index)
        test(new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(1))), "list"), null, scope);
    }

    @Test
    void testListAccessExpressionIndexOutOfBounds() {
        // list[1]
        List<Object> list = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.TEN);

        Scope scope = new Scope(null);
        scope.defineVariable("list", true, Environment.create(list)); // removed from scope for this test
        // ensures Interpreter returns proper value from list (at proper index)
        test(new Ast.Expression.Access(Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(-1))), "list"), null, scope);
    }

    @ParameterizedTest
    @MethodSource
    void testFunctionExpression(String test, Ast ast, Object expected) {
        Scope scope = new Scope(null);
        scope.defineFunction("function", 0, args -> Environment.create("function45"));
        test(ast, expected, scope); // ensures Interpreter returns evaluated function value
    }

    @Test
    void testLogarithmExpressionStatement() {
        Scope scope = new Scope(null);

        scope.defineFunction("logarithm", 1, args -> {

            // Alternate Type Checking: using instanceof operator
            Environment.PlcObject bd = Environment.create(args.getFirst().getValue());
            if(!(bd.getValue() instanceof BigDecimal))
                throw new RuntimeException("expected BigDecimal value");

            // it is a BigDecimal!
            BigDecimal result = BigDecimal.valueOf(Math.log(((BigDecimal)bd.getValue()).doubleValue()));

            return Environment.create(result);
        });

        test(   new Ast.Expression.Function(
                    "logarithm",
                    List.of(new Ast.Expression.Literal(BigDecimal.valueOf(Math.E)))),
                BigDecimal.valueOf(1.0),
                scope
        );
    }

    private static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                // function()
                Arguments.of("Function",
                        new Ast.Expression.Function("function", Arrays.asList()),
                        "function45"
                ),
                // print("Hello, World!")
                Arguments.of("Print",
                        new Ast.Expression.Function("print", Arrays.asList(new Ast.Expression.Literal("Hello, World!"))),
                        Environment.NIL.getValue() // nothing returned from print function, so NIL here
                ),
                // printf
                Arguments.of("Invalid lookup",
                        new Ast.Expression.Function("printf", Arrays.asList(new Ast.Expression.Literal("Hello, World!"))),
                        null
                )
        );
    }

    @Test
    void testPlcList() {
        // [1, 5, 10]
        List<Object> expected = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(5), BigInteger.TEN);

        List<Ast.Expression> values = Arrays.asList(new Ast.Expression.Literal(BigInteger.ONE),
                new Ast.Expression.Literal(BigInteger.valueOf(5)),
                new Ast.Expression.Literal(BigInteger.TEN));

        Ast ast = new Ast.Expression.PlcList(values);

        test(ast, expected, new Scope(null)); // ensures Interpreter returns list of values
    }

    @Test
    void testPlcListNesting() {
        // [1, 5+2, 10]
        List<Object> expected = Arrays.asList(BigInteger.ONE, BigInteger.valueOf(7), BigInteger.TEN);

        List<Ast.Expression> values = Arrays.asList(new Ast.Expression.Literal(BigInteger.ONE),
                new Ast.Expression.Binary("+", new Ast.Expression.Literal(BigInteger.valueOf(5)), new Ast.Expression.Literal(BigInteger.valueOf(2))),
                new Ast.Expression.Literal(BigInteger.TEN));

        Ast ast = new Ast.Expression.PlcList(values);

        test(ast, expected, new Scope(null)); // ensures Interpreter returns list of values
    }

    private static Scope test(Ast ast, Object expected, Scope scope) {
        Interpreter interpreter = new Interpreter(scope);
        if (expected != null) {
            Assertions.assertEquals(expected, interpreter.visit(ast).getValue());
        } else {
            Assertions.assertThrows(RuntimeException.class, () -> interpreter.visit(ast));
        }
        return interpreter.getScope();
    }
}
