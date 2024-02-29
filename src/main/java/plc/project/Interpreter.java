package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Global ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Function ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Expression ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Declaration ast) {
        throw new UnsupportedOperationException(); //TODO (in lecture)
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Assignment ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.If ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Switch ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Case ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.While ast) {
        throw new UnsupportedOperationException(); //TODO (in lecture)
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Return ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Literal ast) {
        // check for null literal
        if(ast.getLiteral() == null) return Environment.NIL;

        // else return new PLC object constructed from literal
        return Environment.create(ast.getLiteral());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Group ast) {
        return visit(ast.getExpression());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Binary ast) {

        // only visit rhs for now (in case of short circuit)
        Environment.PlcObject lhs = visit(ast.getLeft());

        // logical cases must wait to visit rhs for short circuit cases
        switch(ast.getOperator()) {
            // Logical: &&
            case "&&":
                // short circuiting: false + &&
                if (!requireType(Boolean.class, lhs))
                    return Environment.create(false);
                else // return processed (ANDed) result (lhs already known to be true, so only rhs matters here)
                    return Environment.create(requireType(Boolean.class, visit(ast.getRight())));
                // Logical: ||
            case "||":
                // short circuiting: true + ||
                if (requireType(Boolean.class, lhs))
                    return Environment.create(true);
                else // return processed (ORed) result (lhs already known to be false, so only rhs matters here)
                    return Environment.create(requireType(Boolean.class, visit(ast.getRight())));
        }

        // all remaining cases allow visiting of rhs
        Environment.PlcObject rhs = visit(ast.getRight());

        switch(ast.getOperator())
        {
            // Comparable: > OR <
            case ">":
                // ensure both lhs and rhs are comparable and the same class, then compare
                return Environment.create(requireType(Comparable.class, lhs).compareTo(requireType(lhs.getValue().getClass(), rhs)) > 0);
            case "<":
                // ensure both lhs and rhs are comparable and the same class, then compare
                return Environment.create(requireType(Comparable.class, lhs).compareTo(requireType(lhs.getValue().getClass(), rhs)) < 0);
            // Equality
            case "==":
                // check for equality using null-safe Objects.equals
                return Environment.create(Objects.equals(lhs.getValue(), rhs.getValue()));
            case "!=":
                // check for equality using null-safe Objects.equals
                return Environment.create(!Objects.equals(lhs.getValue(), rhs.getValue()));
            // Addition (string concatenation or numeric addition)
            case "+":
                // string concatenation
                if(lhs.getValue().getClass() == String.class || rhs.getValue().getClass() == String.class)
                {
                    return Environment.create(lhs.getValue() + "" + rhs.getValue());
                }
                // Numerical addition
                else
                {
                    // standard calculations + type checking
                    if(lhs.getValue().getClass() == BigDecimal.class) // decimals
                        return Environment.create(((BigDecimal) lhs.getValue()).add(requireType(BigDecimal.class, rhs)));
                    else // integers
                        return Environment.create(requireType(BigInteger.class, lhs).add(requireType(BigInteger.class, rhs)));
                }
            // Subtraction/Multiplication
            case "-":
                // standard calculations + type checking
                if(lhs.getValue().getClass() == BigDecimal.class) // decimals
                    return Environment.create(((BigDecimal) lhs.getValue()).subtract(requireType(BigDecimal.class, rhs)));
                else // integers
                    return Environment.create(requireType(BigInteger.class, lhs).subtract(requireType(BigInteger.class, rhs)));
            case "*":
                // standard calculations + type checking
                if(lhs.getValue().getClass() == BigDecimal.class) // decimals
                    return Environment.create(((BigDecimal) lhs.getValue()).multiply(requireType(BigDecimal.class, rhs)));
                else // integers
                    return Environment.create(requireType(BigInteger.class, lhs).multiply(requireType(BigInteger.class, rhs)));
            // Division
            case "/":
                // check first for divide by zero evaluation failure
                if(rhs.getValue().equals(BigDecimal.valueOf(0.0)) || rhs.getValue().equals(BigInteger.valueOf(0)))
                    throw new RuntimeException("Cannot divide by zero.");
                // standard calculations + type checking
                if(lhs.getValue().getClass() == BigDecimal.class) // decimals
                    return Environment.create(((BigDecimal) lhs.getValue()).divide(requireType(BigDecimal.class, rhs), RoundingMode.HALF_EVEN));
                else // integers
                    return Environment.create(requireType(BigInteger.class, lhs).divide(requireType(BigInteger.class, rhs)));
            // Exponents
            case "^":
                // manually calculate exponent because built-in functions only allow integer exponent,
                // but value input might actually be larger than the range of an integer
                BigInteger base = requireType(BigInteger.class, lhs);
                BigInteger exp = requireType(BigInteger.class, rhs);
                BigInteger result = BigInteger.ONE;
                for(BigInteger i = BigInteger.ZERO; i.compareTo(exp.abs()) < 0; i = i.add(BigInteger.ONE))
                {
                    if(exp.compareTo(BigInteger.ZERO) > 0)
                        result = result.multiply(base);
                    else
                        result = result.divide(base);
                }

                return Environment.create(result);
            default:
                throw new RuntimeException("Unable to interpret unknown binary operator"); // should not be possible to reach this statement
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Access ast) {
        if(ast.getOffset().isEmpty()) // variable
            return scope.lookupVariable(ast.getName()).getValue();
        else // list
        {
            // retrieve data
            BigInteger offset = requireType(BigInteger.class, visit(ast.getOffset().get()));
            List<Object> varList = requireType(List.class, scope.lookupVariable(ast.getName()).getValue());
            // check for index out of bounds
            if(offset.intValue() > varList.size() - 1)
                throw new RuntimeException("Invalid List Access: Index Out of Bounds");

            return Environment.create(varList.get(offset.intValue())); // TODO: confirm I can just convert BigInteger to int like this
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Function ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.PlcList ast) {
        // iterate through expressions to store values
        List<Ast.Expression> expressions = ast.getValues();
        List<Object> objects = new ArrayList<>();
        for (Ast.Expression expr : expressions)
        {
            objects.add(visit(expr).getValue());
        }
        // return list of values (objects)
        return Environment.create(objects);
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }

    }

}
