package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
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
        // visit globals
        for(Ast.Global global : ast.getGlobals())
            visit(global);
        // visit functions
        for(Ast.Function function: ast.getFunctions())
            visit(function);

        // checks if main/0 exists
        Environment.Function main = scope.lookupFunction("main", 0);
        if(main.getReturnType() != Environment.Type.INTEGER)
            throw new RuntimeException("Expected Integer return type for main/0");

        return null;
    }

    @Override
    public Void visit(Ast.Global ast) {

        // determine type of newly created variable
        Environment.Type type = Environment.getType(ast.getTypeName());

        // value checking/visiting
        if(ast.getValue().isPresent())
        {
            // send type to PlcList if of type LIST (needed to check each item for type congruence)
            if(ast.getValue().get() instanceof Ast.Expression.PlcList)
                ((Ast.Expression.PlcList) ast.getValue().get()).setType(type);

            // visit rhs expression
            visit(ast.getValue().get());
            // ensure rhs can be assigned to explicit type
            requireAssignable(type, ast.getValue().get().getType());
        }

        // create/set new variable
        scope.defineVariable(ast.getName(), ast.getName(), type, ast.getMutable(), Environment.NIL);
        ast.setVariable(scope.lookupVariable(ast.getName()));

        return null;
    }

    @Override
    public Void visit(Ast.Function ast) {

        // determine return type
        Environment.Type returnType;
        if(ast.getReturnTypeName().isPresent())
            returnType = Environment.getType(ast.getReturnTypeName().get());
        else
            returnType = Environment.Type.NIL;

        // determine parameter types
        List<Environment.Type> parameterTypes = new ArrayList<>();
        for(String paramTypeName : ast.getParameterTypeNames())
            parameterTypes.add(Environment.getType(paramTypeName));

        // create/set new function
        scope.defineFunction(ast.getName(), ast.getName(), parameterTypes, returnType, args -> Environment.NIL);
        ast.setFunction(scope.lookupFunction(ast.getName(), ast.getParameters().size()));

        // visit function statements in new scope
        try{
            scope = new Scope(scope);

            // create variables for each parameter
            for(int i = 0; i < ast.getParameters().size(); i++)
            {
                String name = ast.getParameters().get(i);
                Environment.Type type = Environment.getType(ast.getParameterTypeNames().get(i));
                scope.defineVariable(name, type.getJvmName(), type, true, Environment.NIL);
            }

            // execute list of statements within this scope
            for(Ast.Statement stmt : ast.getStatements())
            {
                if(stmt instanceof Ast.Statement.Return)
                {
                    // ensure return statements are correct type
                    visit(((Ast.Statement.Return) stmt).getValue());
                    requireAssignable(returnType, ((Ast.Statement.Return) stmt).getValue().getType());
                }
                else
                    visit(stmt);
            }
        } finally {
            // return to parent scope when exiting/looping, regardless of error state
            scope = scope.getParent();
        }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        if(!(ast.getExpression() instanceof Ast.Expression.Function))
            throw new RuntimeException("Expected Function Expression. This is the only permitted expression type within statement expression.");

        // visit function expression
        visit(ast.getExpression());

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {

        // determine type of new declared variable
        Environment.Type type;
        if(ast.getTypeName().isPresent()) { // explicitly defined
            // explicit type
            type = Environment.getType(ast.getTypeName().get());

            // value checks/visiting
            if(ast.getValue().isPresent())
            {
                // visit rhs expression
                visit(ast.getValue().get());
                // ensure rhs can be assigned to explicit type (if present)
                requireAssignable(type, ast.getValue().get().getType());
            }
        }
        else { // not explicitly defined
            if(ast.getValue().isPresent())
            {
                // visit rhs expression
                visit(ast.getValue().get());
                // determine type from visited rhs
                type = ast.getValue().get().getType();
            }
            else
                throw new RuntimeException("Expected either explicit or implicit type specification of variable " + ast.getName());
        }

        // create/set new variable
        scope.defineVariable(ast.getName(), ast.getName(), type, true, Environment.NIL);
        ast.setVariable(scope.lookupVariable(ast.getName()));

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        // verify/visit receiver
        if(!(ast.getReceiver() instanceof Ast.Expression.Access))
            throw new RuntimeException("Expected access expression as lhs of assignment statement.");
        visit(ast.getReceiver());

        // visit value and verify type matches receiver
        visit(ast.getValue());
        requireAssignable(ast.getReceiver().getType(), ast.getValue().getType());

        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        // verify condition type (BOOLEAN)
        visit(ast.getCondition());
        requireAssignable(Environment.Type.BOOLEAN, ast.getCondition().getType());

        // verify thenStatements is not empty
        if(ast.getThenStatements().isEmpty())
            throw new RuntimeException("Expected statements in then block of if statement. Empty if blocks not permitted.");

        // visit then statements in new scope
        VisitNewScope(ast.getThenStatements());

        // visit else statements in new scope
        VisitNewScope(ast.getElseStatements());

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        // determine type from condition
        visit(ast.getCondition());
        Environment.Type type = ast.getCondition().getType();

        // looks at all cases but default
        for (int i = 0; i < ast.getCases().size() - 1; i++)
        {
            Ast.Statement.Case currCase = ast.getCases().get(i);

            // check for default-case error
            if(currCase.getValue().isEmpty())
                throw new RuntimeException("Only the final case of switch-case can be default (no value).");

            // ensure case value type matches condition type
            visit(currCase.getValue().get());
            requireAssignable(type, currCase.getValue().get().getType());

            // visit statements in case
            visit(currCase);
        }

        // default case
        // check for default-case error
        Ast.Statement.Case defaultCase = ast.getCases().getLast();
        if(defaultCase.getValue().isPresent())
            throw new RuntimeException("Switch-case must end with default case.");
        // visit statements in default case
        visit(defaultCase);

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        VisitNewScope(ast.getStatements());

        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        // verify condition type (BOOLEAN)
        visit(ast.getCondition());
        requireAssignable(Environment.Type.BOOLEAN, ast.getCondition().getType());

        // visit while statements in new scope
        VisitNewScope(ast.getStatements());

        return null;
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
        }
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        // type checking is handled in visit(Ast.Function) to ensure congruency with function type - hopefully not actually using this function is fine
        return null;
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
        if(ast.getOffset().isPresent()) // with offset - list index
        {
            // visit offset expression
            visit(ast.getOffset().get());
            // require offsets type to be integer
            requireAssignable(Environment.Type.INTEGER, ast.getOffset().get().getType());

            ast.setVariable(scope.lookupVariable(ast.getName()));
        }
        else // no offset - variable
        {
            ast.setVariable(scope.lookupVariable(ast.getName()));
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {

        // lookup function
        Environment.Function fun = scope.lookupFunction(ast.getName(), ast.getArguments().size());
        ast.setFunction(fun);

        // verify parameter types
        for (int i = 0; i < ast.getArguments().size(); i++)
        {
            // visit (determine type of) parameter
            visit(ast.getArguments().get(i));

            // ensure argument is assignable to parameter type
            requireAssignable(fun.getParameterTypes().get(i), ast.getArguments().get(i).getType());
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {
        // ensure all expressions are assignable to list type
        for(Ast.Expression expr : ast.getValues())
        {
            visit(expr);
            requireAssignable(ast.getType(), expr.getType());
        }

        return null;
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
