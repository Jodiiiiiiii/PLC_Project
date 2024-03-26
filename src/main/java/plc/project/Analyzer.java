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
                int val = ((BigInteger) literal).intValueExact();
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
        // check that it is binary expression
        if(!(ast.getExpression() instanceof Ast.Expression.Binary))
            throw new RuntimeException("Expected Binary Expression within grouping. No other expression types permitted");

        // analyze expression
        visit(ast.getExpression());
        // set type of group to analyzed result type
        ast.setType(ast.getExpression().getType());

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        // visit lhs and rhs expressions
        visit(ast.getLeft());
        Ast.Expression lhs = ast.getLeft();
        visit(ast.getRight());
        Ast.Expression rhs = ast.getRight();

        // set result types
        String op = ast.getOperator();
        switch(op) {
            case "&&":
            case "||":
                requireAssignable(Environment.Type.BOOLEAN, lhs.getType());
                requireAssignable(Environment.Type.BOOLEAN, rhs.getType());
                ast.setType(Environment.Type.BOOLEAN);
                break;
            case ">":
            case "<":
            case "==":
            case "!=":
                requireAssignable(Environment.Type.COMPARABLE, lhs.getType());
                requireAssignable(lhs.getType(), rhs.getType()); // must be same Comparable type
                ast.setType(Environment.Type.BOOLEAN);
                break;
            case "+":
                if(lhs.getType().equals(Environment.Type.STRING)) // lhs = STRING
                {
                    requireAssignable(Environment.Type.ANY, rhs.getType());
                    ast.setType(Environment.Type.STRING);
                }
                else if(rhs.getType().equals(Environment.Type.STRING)) // rhs = STRING
                {
                    requireAssignable(Environment.Type.ANY, lhs.getType());
                    ast.setType(Environment.Type.STRING);
                }
                else if(lhs.getType().equals(Environment.Type.INTEGER) || lhs.getType().equals(Environment.Type.DECIMAL)) // lhs = DECIMAL/INTEGER
                {
                    requireAssignable(lhs.getType(), rhs.getType()); // rhs/return must be same DECIMAL/INTEGER
                    ast.setType(lhs.getType());
                }
                else
                    throw new RuntimeException("Expected String (concatenation), Decimals, or Integers for for lhs/rhs of '+' operator.");
                break;
            case "-":
            case "*":
            case "/":
                if(lhs.getType().equals(Environment.Type.INTEGER) || lhs.getType().equals(Environment.Type.DECIMAL)) // lhs = DECIMAL/INTEGER
                {
                    requireAssignable(lhs.getType(), rhs.getType()); // rhs/return must be same DECIMAL/INTEGER
                    ast.setType(lhs.getType());
                }
                else
                    throw new RuntimeException("Expected Decimals, or Integers for for lhs/rhs of arithmetic operator.");
                break;
            case "^":
                requireAssignable(Environment.Type.INTEGER, lhs.getType());
                requireAssignable(Environment.Type.INTEGER, rhs.getType());
                ast.setType(Environment.Type.INTEGER);
                break;
            default:
                throw new RuntimeException("Expected valid operator in Binary ast"); // should never be reached - indicates issue in parser
        }

        return null;
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
