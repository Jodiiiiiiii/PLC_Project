package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GeneratorTests {

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testSource(String test, Ast.Source ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testSource() {
        return Stream.of(
                Arguments.of("Global - one",
                        // FUN main(): Integer DO
                        //     print("Hello, World!");
                        //     RETURN 0;
                        // END
                        new Ast.Source(
                                Arrays.asList(
                                        init(new Ast.Global("x", "Integer", true, Optional.of(
                                                        init(new Ast.Expression.Literal(new BigInteger("1")), ast -> ast.setType(Environment.Type.INTEGER)))),
                                                ast -> ast.setVariable(new Environment.Variable("x", "x", Environment.Type.INTEGER, true, Environment.NIL)))
                                ),
                                Arrays.asList(init(
                                        new Ast.Function("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                                new Ast.Statement.Expression(init(new Ast.Expression.Function("print", Arrays.asList(
                                                        init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                                )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                                new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                        )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))))
                        ),
                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    int x = 1;",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "}"
                        )
                ),
                Arguments.of("Global - ALL",
                        // FUN main(): Integer DO
                        //     print("Hello, World!");
                        //     RETURN 0;
                        // END
                        new Ast.Source(
                                Arrays.asList(
                                        init(new Ast.Global("x", "Integer", true, Optional.of(
                                                        init(new Ast.Expression.Literal(new BigInteger("1")), ast -> ast.setType(Environment.Type.INTEGER)))),
                                                ast -> ast.setVariable(new Environment.Variable("x", "x", Environment.Type.INTEGER, true, Environment.NIL))),
                                        init(new Ast.Global("xyz", "String", true, Optional.of(
                                                        init(new Ast.Expression.PlcList(List.of(
                                                                init(new Ast.Expression.Literal("The Letter 'x'"), ast -> ast.setType(Environment.Type.STRING)),
                                                                init(new Ast.Expression.Literal("The Letter 'y'"), ast -> ast.setType(Environment.Type.STRING)),
                                                                init(new Ast.Expression.Literal("The Letter 'z'"), ast -> ast.setType(Environment.Type.STRING))
                                                        )), ast -> ast.setType(Environment.Type.STRING)))),
                                                ast -> ast.setVariable(new Environment.Variable("x", "x", Environment.Type.STRING, true, Environment.NIL))),
                                        init(new Ast.Global("y", "Decimal", true, Optional.empty()),
                                                ast -> ast.setVariable(new Environment.Variable("x", "x", Environment.Type.DECIMAL, true, Environment.NIL))),
                                        init(new Ast.Global("z", "Boolean", false, Optional.of(
                                                        init(new Ast.Expression.Literal(Boolean.TRUE), ast -> ast.setType(Environment.Type.BOOLEAN)))),
                                                ast -> ast.setVariable(new Environment.Variable("x", "x", Environment.Type.BOOLEAN, true, Environment.NIL)))
                                ),
                                Arrays.asList(init(
                                        new Ast.Function("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                                new Ast.Statement.Expression(init(new Ast.Expression.Function("print", Arrays.asList(
                                                        init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                                )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                                new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                        )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))))
                        ),
                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    int x = 1;",
                                "    String[] xyz = {\"The Letter 'x'\", \"The Letter 'y'\", \"The Letter 'z'\"};",
                                "    double y;",
                                "    final boolean z = true;",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "}"
                        )
                ),
                Arguments.of("Main only",
                        // FUN main(): Integer DO
                        //     print("Hello, World!");
                        //     RETURN 0;
                        // END
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(init(
                                            new Ast.Function("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                                    new Ast.Statement.Expression(init(new Ast.Expression.Function("print", Arrays.asList(
                                                            init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                                    )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                                    new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                        )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))))
                        ),
                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "}"
                        )
                ),
                Arguments.of("Multiple Functions - one param",
                        // FUN main(): Integer DO
                        //     print("Hello, World!");
                        //     RETURN 0;
                        // END
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(init(
                                                new Ast.Function("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                                        new Ast.Statement.Expression(init(new Ast.Expression.Function("print", Arrays.asList(
                                                                init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                                        new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                                )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))),
                                        init(
                                                new Ast.Function("otherFunction", Arrays.asList("param1"), Arrays.asList("Integer"), Optional.of("Boolean"), Arrays.asList(
                                                        new Ast.Statement.Expression(init(new Ast.Expression.Function("print", Arrays.asList(
                                                                init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                                        new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                                )), ast -> ast.setFunction(new Environment.Function("otherFunction", "otherFunction", Arrays.asList(Environment.Type.INTEGER), Environment.Type.BOOLEAN, args -> Environment.NIL))))
                        ),
                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "    boolean otherFunction(int param1) {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "}"
                        )
                ),
                Arguments.of("Multiple Functions - several params",
                        // FUN main(): Integer DO
                        //     print("Hello, World!");
                        //     RETURN 0;
                        // END
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(init(
                                                new Ast.Function("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                                        new Ast.Statement.Expression(init(new Ast.Expression.Function("print", Arrays.asList(
                                                                init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                                        new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                                )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))),
                                        init(
                                                new Ast.Function("otherFunction", Arrays.asList("param1", "param2", "param3"), Arrays.asList("Integer", "String", "Character"), Optional.of("Boolean"), Arrays.asList(
                                                        new Ast.Statement.Expression(init(new Ast.Expression.Function("print", Arrays.asList(
                                                                init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                                        new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                                )), ast -> ast.setFunction(new Environment.Function("otherFunction", "otherFunction", Arrays.asList(Environment.Type.INTEGER, Environment.Type.STRING, Environment.Type.CHARACTER), Environment.Type.BOOLEAN, args -> Environment.NIL))))
                        ),
                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "    boolean otherFunction(int param1, String param2, char param3) {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "}"
                        )
                ),
                // TODO: ensure there should not be a space between braces of empty function block (question asked in Microsoft Teams)
                Arguments.of("Multiple Functions - empty function block",
                        // FUN main(): Integer DO
                        //     print("Hello, World!");
                        //     RETURN 0;
                        // END
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(init(
                                                new Ast.Function("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                                        new Ast.Statement.Expression(init(new Ast.Expression.Function("print", Arrays.asList(
                                                                init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                                        new Ast.Statement.Return(init(new Ast.Expression.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                                )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))),
                                        init(
                                                new Ast.Function("otherFunction", Arrays.asList("param1", "param2", "param3"), Arrays.asList("Integer", "String", "Character"), Optional.of("Boolean"), List.of(
                                                )), ast -> ast.setFunction(new Environment.Function("otherFunction", "otherFunction", Arrays.asList(Environment.Type.INTEGER, Environment.Type.STRING, Environment.Type.CHARACTER), Environment.Type.BOOLEAN, args -> Environment.NIL))))
                        ),
                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "    boolean otherFunction(int param1, String param2, char param3) {}",
                                "",
                                "}"
                        )
                )
        );
    }

    @Test
    void testList() {
        // LIST name : Decimal = [1.0, 1.5, 2.0];
        Ast.Expression.Literal expr1 = new Ast.Expression.Literal(new BigDecimal("1.0"));
        Ast.Expression.Literal expr2 = new Ast.Expression.Literal(new BigDecimal("1.5"));
        Ast.Expression.Literal expr3 = new Ast.Expression.Literal(new BigDecimal("2.0"));
        expr1.setType(Environment.Type.DECIMAL);
        expr2.setType(Environment.Type.DECIMAL);
        expr3.setType(Environment.Type.DECIMAL);

        Ast.Global global = new Ast.Global("list", "Decimal", true, Optional.of(new Ast.Expression.PlcList(Arrays.asList(expr1, expr2, expr3))));
        Ast.Global astList = init(global, ast -> ast.setVariable(new Environment.Variable("list", "list", Environment.Type.DECIMAL, true, Environment.create(Arrays.asList(1.0, 1.5, 2.0)))));

        String expected = "double[] list = {1.0, 1.5, 2.0};";
        test(astList, expected);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testExpressionStatement(String test, Ast.Statement.Expression ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testExpressionStatement() {
        return Stream.of(
                Arguments.of("Function Expression",
                        new Ast.Statement.Expression(init(
                                new Ast.Expression.Function("log",
                                    List.of(init(new Ast.Expression.Literal("Hello World"), ast -> ast.setType(Environment.Type.STRING)))),
                                    ast -> ast.setFunction(new Environment.Function("log", "log", List.of(Environment.Type.STRING), Environment.Type.NIL, args -> Environment.NIL)))),
                        "log(\"Hello World\");"
                ),
                Arguments.of("Literal Expression",
                        new Ast.Statement.Expression(
                                init(new Ast.Expression.Literal(new BigInteger("1")), ast -> ast.setType(Environment.Type.INTEGER))),
                        "1;"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testDeclarationStatement(String test, Ast.Statement.Declaration ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testDeclarationStatement() {
        return Stream.of(
                Arguments.of("Declaration - int",
                        // LET name: Integer;
                        init(new Ast.Statement.Declaration("name", Optional.of("Integer"), Optional.empty()), ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.INTEGER, true, Environment.NIL))),
                        "int name;"
                ),
                Arguments.of("Initialization - int",
                        // LET name = 1.0;
                        init(new Ast.Statement.Declaration("name", Optional.empty(), Optional.of(
                                init(new Ast.Expression.Literal(new BigInteger("1")),ast -> ast.setType(Environment.Type.INTEGER))
                        )), ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.INTEGER, true, Environment.NIL))),
                        "int name = 1;"
                ),
                Arguments.of("declaration - double",
                        // LET name = 1.0;
                        init(new Ast.Statement.Declaration("name", Optional.of("Decimal"), Optional.empty()),
                                ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.DECIMAL, true, Environment.NIL))),
                        "double name;"
                ),
                Arguments.of("Initialization - double",
                        // LET name = 1.0;
                        init(new Ast.Statement.Declaration("name", Optional.empty(), Optional.of(
                                init(new Ast.Expression.Literal(new BigDecimal("1.0")),ast -> ast.setType(Environment.Type.DECIMAL))
                        )), ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.DECIMAL, true, Environment.NIL))),
                        "double name = 1.0;"
                ),
                Arguments.of("declaration - String",
                        // LET name = 1.0;
                        init(new Ast.Statement.Declaration("name", Optional.of("String"), Optional.empty()),
                                ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.STRING, true, Environment.NIL))),
                        "String name;"
                ),
                Arguments.of("Initialization - String",
                        // LET name = 1.0;
                        init(new Ast.Statement.Declaration("name", Optional.empty(), Optional.of(
                                init(new Ast.Expression.Literal("1.0"),ast -> ast.setType(Environment.Type.STRING))
                        )), ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.STRING, true, Environment.NIL))),
                        "String name = \"1.0\";"
                ),
                Arguments.of("declaration - char",
                        // LET name = 1.0;
                        init(new Ast.Statement.Declaration("name", Optional.of("char"), Optional.empty()),
                                ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.CHARACTER, true, Environment.NIL))),
                        "char name;"
                ),
                Arguments.of("Initialization - char",
                        // LET name = 1.0;
                        init(new Ast.Statement.Declaration("name", Optional.empty(), Optional.of(
                                init(new Ast.Expression.Literal('1'),ast -> ast.setType(Environment.Type.CHARACTER))
                        )), ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.CHARACTER, true, Environment.NIL))),
                        "char name = '1';"
                ),
                Arguments.of("declaration - bool",
                        // LET name = 1.0;
                        init(new Ast.Statement.Declaration("name", Optional.of("Boolean"), Optional.empty()),
                                ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.BOOLEAN, true, Environment.NIL))),
                        "boolean name;"
                ),
                Arguments.of("Initialization - bool",
                        // LET name = 1.0;
                        init(new Ast.Statement.Declaration("name", Optional.empty(), Optional.of(
                                init(new Ast.Expression.Literal(Boolean.TRUE),ast -> ast.setType(Environment.Type.BOOLEAN))
                        )), ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.BOOLEAN, true, Environment.NIL))),
                        "boolean name = true;"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testAssignmentStatement(String test, Ast.Statement.Assignment ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testAssignmentStatement() {
        return Stream.of(
                Arguments.of("Initialization - String",
                        // LET name = 1.0;
                        new Ast.Statement.Assignment(
                                init(new Ast.Expression.Access(Optional.empty(), "variable"),
                                        ast -> ast.setVariable(new Environment.Variable("variable", "variable", Environment.Type.STRING, true, Environment.NIL))),
                                init(new Ast.Expression.Literal("Hello World"), ast -> ast.setType(Environment.Type.STRING))
                                ),
                        "variable = \"Hello World\";"
                ),
                Arguments.of("Initialization - char",
                        // LET name = 1.0;
                        new Ast.Statement.Assignment(
                                init(new Ast.Expression.Access(Optional.empty(), "variable"),
                                        ast -> ast.setVariable(new Environment.Variable("variable", "variable", Environment.Type.CHARACTER, true, Environment.NIL))),
                                init(new Ast.Expression.Literal('c'), ast -> ast.setType(Environment.Type.CHARACTER))
                        ),
                        "variable = 'c';"
                ),
                Arguments.of("Initialization - int",
                        // LET name = 1.0;
                        new Ast.Statement.Assignment(
                                init(new Ast.Expression.Access(Optional.empty(), "variable"),
                                        ast -> ast.setVariable(new Environment.Variable("variable", "variable", Environment.Type.INTEGER, true, Environment.NIL))),
                                init(new Ast.Expression.Literal(new BigInteger("1")), ast -> ast.setType(Environment.Type.INTEGER))
                        ),
                        "variable = 1;"
                ),
                Arguments.of("Initialization - double",
                        // LET name = 1.0;
                        new Ast.Statement.Assignment(
                                init(new Ast.Expression.Access(Optional.empty(), "variable"),
                                        ast -> ast.setVariable(new Environment.Variable("variable", "variable", Environment.Type.DECIMAL, true, Environment.NIL))),
                                init(new Ast.Expression.Literal(new BigDecimal("1.02020")), ast -> ast.setType(Environment.Type.DECIMAL))
                        ),
                        "variable = 1.02020;"
                ),
                Arguments.of("Initialization - bool",
                        // LET name = 1.0;
                        new Ast.Statement.Assignment(
                                init(new Ast.Expression.Access(Optional.empty(), "variable"),
                                        ast -> ast.setVariable(new Environment.Variable("variable", "variable", Environment.Type.BOOLEAN, true, Environment.NIL))),
                                init(new Ast.Expression.Literal(Boolean.TRUE), ast -> ast.setType(Environment.Type.BOOLEAN))
                        ),
                        "variable = true;"
                ),
                Arguments.of("List index assignment",
                        // LET name = 1.0;
                        new Ast.Statement.Assignment(
                                init(new Ast.Expression.Access(
                                        Optional.of(init(new Ast.Expression.Literal(new BigInteger("2")), ast -> ast.setType(Environment.Type.INTEGER))),
                                        "variable"),
                                        ast -> ast.setVariable(new Environment.Variable("variable", "variable", Environment.Type.BOOLEAN, true, Environment.NIL))),
                                init(new Ast.Expression.Literal(Boolean.TRUE), ast -> ast.setType(Environment.Type.BOOLEAN))
                        ),
                        "variable[2] = true;"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testIfStatement(String test, Ast.Statement.If ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testIfStatement() {
        return Stream.of(
                Arguments.of("If",
                        // IF expr DO
                        //     stmt;
                        // END
                        new Ast.Statement.If(
                                init(new Ast.Expression.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, true, Environment.NIL))),
                                Arrays.asList(new Ast.Statement.Expression(init(new Ast.Expression.Access(Optional.empty(), "stmt"), ast -> ast.setVariable(new Environment.Variable("stmt", "stmt", Environment.Type.NIL, true, Environment.NIL))))),
                                Arrays.asList()
                        ),
                        String.join(System.lineSeparator(),
                                "if (expr) {",
                                "    stmt;",
                                "}"
                        )
                ),
                Arguments.of("Else",
                        // IF expr DO
                        //     stmt1;
                        // ELSE
                        //     stmt2;
                        // END
                        new Ast.Statement.If(
                                init(new Ast.Expression.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, true, Environment.NIL))),
                                Arrays.asList(new Ast.Statement.Expression(init(new Ast.Expression.Access(Optional.empty(), "stmt1"), ast -> ast.setVariable(new Environment.Variable("stmt1", "stmt1", Environment.Type.NIL, true, Environment.NIL))))),
                                Arrays.asList(new Ast.Statement.Expression(init(new Ast.Expression.Access(Optional.empty(), "stmt2"), ast -> ast.setVariable(new Environment.Variable("stmt2", "stmt2", Environment.Type.NIL, true, Environment.NIL)))))
                        ),
                        String.join(System.lineSeparator(),
                                "if (expr) {",
                                "    stmt1;",
                                "} else {",
                                "    stmt2;",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testSwitchStatement(String test, Ast.Statement.Switch ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testSwitchStatement() {
        return Stream.of(
                Arguments.of("Switch",
                        // SWITCH letter
                        //     CASE 'y':
                        //         print("yes");
                        //         letter = 'n';
                        //         break;
                        //     DEFAULT
                        //         print("no");
                        // END
                        new Ast.Statement.Switch(
                                init(new Ast.Expression.Access(Optional.empty(), "letter"), ast -> ast.setVariable(new Environment.Variable("letter", "letter", Environment.Type.CHARACTER, true, Environment.create('y')))),
                                Arrays.asList(
                                        new Ast.Statement.Case(
                                                Optional.of(init(new Ast.Expression.Literal('y'), ast -> ast.setType(Environment.Type.CHARACTER))),
                                                Arrays.asList(
                                                        new Ast.Statement.Expression(
                                                                init(new Ast.Expression.Function("print", Arrays.asList(init(new Ast.Expression.Literal("yes"), ast -> ast.setType(Environment.Type.STRING)))),
                                                                        ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))
                                                                )
                                                        ),
                                                        new Ast.Statement.Assignment(
                                                                init(new Ast.Expression.Access(Optional.empty(), "letter"), ast -> ast.setVariable(new Environment.Variable("letter", "letter", Environment.Type.CHARACTER, true, Environment.create('y')))),
                                                                init(new Ast.Expression.Literal('n'), ast -> ast.setType(Environment.Type.CHARACTER))
                                                        )
                                                )
                                        ),
                                        new Ast.Statement.Case(
                                                Optional.empty(),
                                                Arrays.asList(
                                                        new Ast.Statement.Expression(
                                                                init(new Ast.Expression.Function("print", Arrays.asList(init(new Ast.Expression.Literal("no"), ast -> ast.setType(Environment.Type.STRING)))),
                                                                        ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "switch (letter) {",
                                "    case 'y':",
                                "        System.out.println(\"yes\");",
                                "        letter = 'n';",
                                "        break;",
                                "    default:",
                                "        System.out.println(\"no\");",
                                "}"
                        )
                ),
                Arguments.of("Switch - default only",
                        // SWITCH letter
                        //     DEFAULT
                        //         print("no");
                        // END
                        new Ast.Statement.Switch(
                                init(new Ast.Expression.Access(Optional.empty(), "letter"), ast -> ast.setVariable(new Environment.Variable("letter", "letter", Environment.Type.CHARACTER, true, Environment.create('y')))),
                                Arrays.asList(
                                        new Ast.Statement.Case(
                                                Optional.empty(),
                                                Arrays.asList(
                                                        new Ast.Statement.Expression(
                                                                init(new Ast.Expression.Function("print", Arrays.asList(init(new Ast.Expression.Literal("no"), ast -> ast.setType(Environment.Type.STRING)))),
                                                                        ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "switch (letter) {",
                                "    default:",
                                "        System.out.println(\"no\");",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testWhileStatement(String test, Ast.Statement.While ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testWhileStatement() {
        return Stream.of(
                Arguments.of("While - standard",
                        new Ast.Statement.While(
                                init(new Ast.Expression.Access(Optional.empty(), "condition"),
                                        ast -> ast.setVariable(new Environment.Variable("condition", "condition", Environment.Type.BOOLEAN, true, Environment.NIL))),
                                Arrays.asList(
                                        new Ast.Statement.Expression(
                                                init(new Ast.Expression.Function("print", Arrays.asList(init(new Ast.Expression.Literal("yes"), ast -> ast.setType(Environment.Type.STRING)))),
                                                        ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))
                                                )
                                        ),
                                        new Ast.Statement.Assignment(
                                                init(new Ast.Expression.Access(Optional.empty(), "letter"), ast -> ast.setVariable(new Environment.Variable("letter", "letter", Environment.Type.CHARACTER, true, Environment.create('y')))),
                                                init(new Ast.Expression.Literal('n'), ast -> ast.setType(Environment.Type.CHARACTER))
                                        )
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "while (condition) {",
                                "    System.out.println(\"yes\");",
                                "    letter = 'n';",
                                "}"
                        )
                ),
                // TODO: verify there should not be a space between braces of empty block (asked in Microsoft Teams)
                Arguments.of("While - empty body",
                        new Ast.Statement.While(
                                init(new Ast.Expression.Access(Optional.empty(), "condition"),
                                        ast -> ast.setVariable(new Environment.Variable("condition", "condition", Environment.Type.BOOLEAN, true, Environment.NIL))),
                                Arrays.asList()
                        ),
                        String.join(System.lineSeparator(),
                                "while (condition) {}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testReturnStatement(String test, Ast.Statement.Return ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testReturnStatement() {
        return Stream.of(
                Arguments.of("Return - standard",
                        new Ast.Statement.Return(init(new Ast.Expression.Literal(Boolean.FALSE), ast -> ast.setType(Environment.Type.BOOLEAN))),
                        "return false;"
                ),
                Arguments.of("Return - binary",
                        new Ast.Statement.Return(
                                init(new Ast.Expression.Binary("*",
                                        init(new Ast.Expression.Literal(new BigInteger("5")), ast -> ast.setType(Environment.Type.INTEGER)),
                                        init(new Ast.Expression.Literal(new BigInteger("10")), ast -> ast.setType(Environment.Type.INTEGER))),
                                        ast -> ast.setType(Environment.Type.INTEGER))
                        ),
                        "return 5 * 10;"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testLiteralExpression(String test, Ast.Expression.Literal ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testLiteralExpression() {
        return Stream.of(
                Arguments.of("String",
                        init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING)),
                        "\"Hello, World!\""
                ),
                Arguments.of("Char",
                        init(new Ast.Expression.Literal('c'), ast -> ast.setType(Environment.Type.CHARACTER)),
                        "'c'"
                ),
                Arguments.of("Integer",
                        init(new Ast.Expression.Literal(new BigInteger("1")), ast -> ast.setType(Environment.Type.INTEGER)),
                        "1"
                ),
                Arguments.of("Decimal",
                        init(new Ast.Expression.Literal(new BigDecimal("1.20")), ast -> ast.setType(Environment.Type.DECIMAL)),
                        "1.20"
                ),
                Arguments.of("Boolean",
                        init(new Ast.Expression.Literal(Boolean.TRUE), ast -> ast.setType(Environment.Type.BOOLEAN)),
                        "true"
                ),
                Arguments.of("Null/NIL",
                        init(new Ast.Expression.Literal(null), ast -> ast.setType(Environment.Type.NIL)),
                        "null"
                )


        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testGroupExpression(String test, Ast.Expression.Group ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testGroupExpression() {
        return Stream.of(
                Arguments.of("String",
                        init(new Ast.Expression.Group(
                                init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))),
                                ast -> ast.setType(Environment.Type.STRING)),
                        "(\"Hello, World!\")"
                ),
                Arguments.of("Integer",
                        init(new Ast.Expression.Group(
                                init(new Ast.Expression.Literal(new BigInteger("1")), ast -> ast.setType(Environment.Type.INTEGER))),
                                ast -> ast.setType(Environment.Type.INTEGER)),
                        "(1)"
                ),
                Arguments.of("Binary",
                        init(new Ast.Expression.Group(
                                // 1 + 10
                                init(new Ast.Expression.Binary("+",
                                        init(new Ast.Expression.Literal(BigInteger.ONE), ast -> ast.setType(Environment.Type.INTEGER)),
                                        init(new Ast.Expression.Literal(BigInteger.TEN), ast -> ast.setType(Environment.Type.INTEGER))),
                                        ast -> ast.setType(Environment.Type.STRING))),
                                ast -> ast.setType(Environment.Type.INTEGER)),
                        "(1 + 10)"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testBinaryExpression(String test, Ast.Expression.Binary ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("And",
                        // TRUE && FALSE
                        init(new Ast.Expression.Binary("&&",
                                init(new Ast.Expression.Literal(true), ast -> ast.setType(Environment.Type.BOOLEAN)),
                                init(new Ast.Expression.Literal(false), ast -> ast.setType(Environment.Type.BOOLEAN))
                        ), ast -> ast.setType(Environment.Type.BOOLEAN)),
                        "true && false"
                ),
                Arguments.of("Concatenation",
                        // "Ben" + 10
                        init(new Ast.Expression.Binary("+",
                                init(new Ast.Expression.Literal("Ben"), ast -> ast.setType(Environment.Type.STRING)),
                                init(new Ast.Expression.Literal(BigInteger.TEN), ast -> ast.setType(Environment.Type.INTEGER))
                        ), ast -> ast.setType(Environment.Type.STRING)),
                        "\"Ben\" + 10"
                ),
                Arguments.of("Numeric Addition",
                        // 1 + 10
                        init(new Ast.Expression.Binary("+",
                                init(new Ast.Expression.Literal(BigInteger.ONE), ast -> ast.setType(Environment.Type.INTEGER)),
                                init(new Ast.Expression.Literal(BigInteger.TEN), ast -> ast.setType(Environment.Type.INTEGER))
                        ), ast -> ast.setType(Environment.Type.STRING)),
                        "1 + 10"
                ),
                Arguments.of("Power Operator",
                        // 1 + 10
                        init(new Ast.Expression.Binary("^",
                                init(new Ast.Expression.Literal(BigInteger.ONE), ast -> ast.setType(Environment.Type.INTEGER)),
                                init(new Ast.Expression.Literal(BigInteger.TEN), ast -> ast.setType(Environment.Type.INTEGER))
                        ), ast -> ast.setType(Environment.Type.STRING)),
                        "Math.pow(1, 10)"
                ),
                Arguments.of("Nested Binary Expressions",
                        // 2 ^ 3 + 1
                        init(new Ast.Expression.Binary("+",
                            init(new Ast.Expression.Binary("^",
                                init(new Ast.Expression.Literal(new BigInteger("2")), ast -> ast.setType(Environment.Type.INTEGER)),
                                init(new Ast.Expression.Literal(new BigInteger("3")), ast -> ast.setType(Environment.Type.INTEGER)))
                                , ast -> ast.setType(Environment.Type.STRING)),
                            init(new Ast.Expression.Literal(BigInteger.ONE), ast -> ast.setType(Environment.Type.INTEGER))
                        ), ast -> ast.setType(Environment.Type.INTEGER)),
                        "Math.pow(2, 3) + 1"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testAccessExpression(String test, Ast.Expression.Access ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testAccessExpression() {
        return Stream.of(
                Arguments.of("Variable",
                        // variable
                        init(new Ast.Expression.Access(
                                Optional.empty(),
                                "variable"),
                                ast -> ast.setVariable(new Environment.Variable("variable", "variable", Environment.Type.INTEGER, true, Environment.NIL))),
                        "variable"
                ),
                Arguments.of("List Access",
                        // variable
                        init(new Ast.Expression.Access(
                            Optional.of(init(new Ast.Expression.Access(
                                Optional.empty(),
                                "expr"),
                                ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.INTEGER, true, Environment.NIL)))),
                            "list"),
                            ast -> ast.setVariable(new Environment.Variable("list", "list", Environment.Type.INTEGER, true, Environment.NIL))),
                        "list[expr]"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testFunctionExpression(String test, Ast.Expression.Function ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                Arguments.of("Print",
                        // print("Hello, World!")
                        init(new Ast.Expression.Function("print", Arrays.asList(
                                init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))),
                        "System.out.println(\"Hello, World!\")"
                ),
                Arguments.of("No Params",
                        // print("Hello, World!")
                        init(new Ast.Expression.Function("func", Arrays.asList()),
                                ast -> ast.setFunction(new Environment.Function("func", "func", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))),
                        "func()"
                ),
                Arguments.of("One Param",
                        // print("Hello, World!")
                        init(new Ast.Expression.Function("func", Arrays.asList(
                                init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING)))),
                                ast -> ast.setFunction(new Environment.Function("func", "func", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))),
                        "func(\"Hello, World!\")"
                ),
                // TODO: Are comma-separated arguments separated with a space (question in Microsoft Teams)
                Arguments.of("Multiple Params",
                        // print("Hello, World!")
                        init(new Ast.Expression.Function("func", Arrays.asList(
                                        init(new Ast.Expression.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING)),
                                        init(new Ast.Expression.Literal(new BigInteger("1")), ast -> ast.setType(Environment.Type.INTEGER)),
                                        init(new Ast.Expression.Literal(Boolean.FALSE), ast -> ast.setType(Environment.Type.BOOLEAN)))),
                                ast -> ast.setFunction(new Environment.Function("func", "func", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))),
                        "func(\"Hello, World!\", 1, false)"
                )
        );
    }

    /**
     * Helper function for tests, using a StringWriter as the output stream.
     */
    private static void test(Ast ast, String expected) {
        StringWriter writer = new StringWriter();
        new Generator(new PrintWriter(writer)).visit(ast);
        Assertions.assertEquals(expected, writer.toString());
    }

    /**
     * Runs a callback on the given value, used for inline initialization.
     */
    private static <T> T init(T value, Consumer<T> initializer) {
        initializer.accept(value);
        return value;
    }

}
