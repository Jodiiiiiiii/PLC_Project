package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);

    public Interpreter(Scope parent) {
        scope = new Scope(parent);

        scope.defineFunction("print", 1, args -> {
            System.out.println(args.getFirst().getValue());
            return Environment.NIL;
        });

        scope.defineFunction("logarithm", 1, args -> {

            // Alternate Type Checking: using instanceof operator

            BigDecimal bd2 = requireType(BigDecimal.class, Environment.create(args.getFirst().getValue()));
            BigDecimal result = BigDecimal.valueOf(Math.log(bd2.doubleValue()));

            return Environment.create(result);
        });

        // converts from one base to another (???)
        scope.defineFunction("converter", 2, args -> {

            //BigInteger decimal = requireType(BigInteger.class, Environment.create(args.get(0)));
            //BigInteger base = requireType(BigInteger.class, Environment.create(args.get(1)));

            return Environment.NIL; // temp
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
        // simply visit (evaluate) the expression
        visit(ast.getExpression());

        // always returns NIL on successful execution
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Declaration ast) {
        if(ast.getValue().isPresent())
            scope.defineVariable(ast.getName(), true, visit(ast.getValue().get()));
        else
            scope.defineVariable(ast.getName(), true, Environment.NIL);

        // always returns NIL on successful execution
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Assignment ast) {
        // ensure receiver (lhs) is a variable or list index
        if(!(ast.getReceiver() instanceof Ast.Expression.Access receiver))
            throw new RuntimeException("Expected Access Expression. Only a variable or list index can be assigned with assignment statement.");

        // check for mutability
        if(!scope.lookupVariable(receiver.getName()).getMutable())
            throw new RuntimeException("Expected Mutable Variable. " + receiver.getName() + " is immutable");

        if(receiver.getOffset().isPresent()) // list index receiver
        {
            // ensure offset is BigInteger
            BigInteger offset = requireType(BigInteger.class, visit(receiver.getOffset().get()));
            // ensure var is List
            List<Object> varList = requireType(List.class, scope.lookupVariable(receiver.getName()).getValue());

            // check for index out of bounds
            if(offset.intValue() > varList.size() - 1)
                throw new RuntimeException("Invalid List Access: Index Out of Bounds");

            // modify temporary retrieved list
            varList.set(offset.intValue(), visit(ast.getValue()).getValue());

            // set list index to new list
            scope.lookupVariable(receiver.getName()).setValue(Environment.create(varList));
        }
        else // variable receiver
        {
            // set variable to value
            scope.lookupVariable(receiver.getName()).setValue(visit(ast.getValue()));
        }

        // always returns NIL on successful execution
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.If ast) {
        if(requireType(Boolean.class, visit(ast.getCondition()))) // true
            VisitNewScope(ast.getThenStatements());
        else // false
            VisitNewScope(ast.getElseStatements());

        // always returns NIL on successful execution
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Switch ast) {
        // get condition/cases
        Object conditionVal = visit(ast.getCondition()).getValue();
        List<Ast.Statement.Case> cases = ast.getCases();

        // check for right case to visit
        for(Ast.Statement.Case caseToCheck : cases)
            if(caseToCheck.getValue().isPresent() && visit(caseToCheck.getValue().get()).getValue().equals(conditionVal))
                return visit(caseToCheck);

        return visit(cases.getLast()); // visit default case if none visited already
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Case ast) {
        // visit case statements
        VisitNewScope(ast.getStatements());

        // always returns NIL on successful execution
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.While ast) {
        while(requireType(Boolean.class, visit(ast.getCondition())))
            VisitNewScope(ast.getStatements());

        // always returns NIL on successful execution
        return Environment.NIL;
    }

    private void VisitNewScope(List<Ast.Statement> statements) {
        try{
            scope = new Scope(scope);
            // execute list of statements within this scope
            for(Ast.Statement stmt : statements)
                visit(stmt);
        } finally {
            // return to parent scope when exiting/looping, regardless of error state
            scope = scope.getParent();
            // scope is re-created ever loop
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Return ast) {
        // simply return the visited evaluation
        throw new Return(visit(ast.getValue()));

        // TODO: figure out and revisit when I know what is meant by "implementation of Ast.Function will catch and Return exceptions and complete behavior"
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
        // simply visits whatever expression is within the group
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
        // lookup function
        Environment.Function function = scope.lookupFunction(ast.getName(), ast.getArguments().size());
        // gather arguments into List
        List<Ast.Expression> expressions = ast.getArguments();
        List<Environment.PlcObject> arguments = new ArrayList<>();
        for(Ast.Expression expr : expressions)
            arguments.add(visit(expr));
        // invoke/return function call
        return function.invoke(arguments);
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