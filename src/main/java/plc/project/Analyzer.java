package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void> {

    public Scope scope;
    private Ast.Function function;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Global ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Function ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        Object literal = ast.getLiteral();
        if(literal == null) // NIL
            ast.setType(Environment.Type.NIL);
        else if(literal instanceof Boolean) // BOOLEAN
            ast.setType(Environment.Type.BOOLEAN);
        else if(literal instanceof Character) // CHARACTER
            ast.setType(Environment.Type.CHARACTER);
        else if(literal instanceof String) // STRING
            ast.setType(Environment.Type.STRING);
        else if(literal instanceof BigInteger) // INTEGER
        {
            ast.setType(Environment.Type.INTEGER);

            // check for out of Java int range
            try {
                ((BigInteger) literal).intValueExact();
            } catch(ArithmeticException e) {
                throw new RuntimeException("Integer out of range of Java int (32-bit signed int). BigInteger value: " + literal + " too large.");
            }
        }
        else if(literal instanceof BigDecimal) // DECIMAL
        {
            ast.setType(Environment.Type.DECIMAL);

            // check for out of Java double range
            double val = ((BigDecimal) literal).doubleValue();
            if(val == Double.NEGATIVE_INFINITY || val == Double.POSITIVE_INFINITY)
                throw new RuntimeException("Double out of range of Java double (64-bit signed float). BigDecimal value: " + literal + " too large.");
        }
        else
            // should never be able to reach here - indicates an issue in the Parser
            throw new RuntimeException("Expected valid literal type (null, boolean, character, String, BigInteger, or BigDecimal");

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        // same type or target is any
        if(target.equals(type) || target.equals(Environment.Type.ANY))
            return;

        // Comparable
        if(target.equals(Environment.Type.COMPARABLE) &&
                (type.equals(Environment.Type.INTEGER) || type.equals(Environment.Type.DECIMAL)
                || type.equals(Environment.Type.CHARACTER) || type.equals(Environment.Type.STRING)))
            return;

        throw new RuntimeException("Expected type " + target.getName() + ". Received type " + type.getName());
    }

}
