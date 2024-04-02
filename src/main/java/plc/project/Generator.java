package plc.project;

import java.io.PrintWriter;
import java.util.List;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        // create Main
        print("public class Main {");
        newline(0); // extra spacing line
        indent++;

        // globals
        for(Ast.Global global : ast.getGlobals()) {
            newline(indent);
            print(global);
        }
        if(!ast.getGlobals().isEmpty())
            newline(0); // another spacing line

        // Java main method
        newline(indent);
        print("public static void main(String[] args) {");
        newline(indent + 1);
        print("System.exit(new Main().main());");
        newline(indent);
        print("}");
        newline(0); // extra spacing line

        // functions
        for(Ast.Function function : ast.getFunctions()) {
            newline(indent);
            print(function);
            newline(0); // spacing after each function
        }

        // close Main
        indent--;
        newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Global ast) {
        if(ast.getMutable()) // mutable
        {
            if(ast.getValue().isPresent() && ast.getValue().get() instanceof Ast.Expression.PlcList) // list
            {
                print(ast.getVariable().getType().getJvmName(), "[] ", ast.getName(), " = ", ast.getValue().get(), ";");
            }
            else // mutable variable
            {
                print(ast.getVariable().getType().getJvmName(), " ", ast.getName());

                if(ast.getValue().isPresent())
                    print(" = ", ast.getValue().get());
                print(";");
            }
        }
        else { // immutable
            print("final ", ast.getVariable().getType().getJvmName(), " ", ast.getName(), " = ", ast.getValue().get(), ";");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Function ast) {
        // function definition
        print(ast.getFunction().getReturnType().getJvmName(), " ", ast.getName(), "(");
        // parameters
        for (int i = 0; i < ast.getParameters().size(); i++)
        {
            if(i == ast.getParameters().size()-1) // last parameter (no comma)
                print(Environment.getType(ast.getParameterTypeNames().get(i)).getJvmName(), " ", ast.getParameters().get(i));
            else
                print(Environment.getType(ast.getParameterTypeNames().get(i)).getJvmName(), " ", ast.getParameters().get(i), ", ");
        }
        print(") {");

        // statements
        printIndentedBlock(ast.getStatements());

        // closing brace of function
        if(!ast.getStatements().isEmpty())
            newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        print(ast.getExpression(), ";");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        print(ast.getVariable().getType().getJvmName(), " ", ast.getName());

        if(ast.getValue().isPresent())
            print(" = ", ast.getValue().get());

        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        print(ast.getReceiver(), " = ", ast.getValue(), ";");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        // if block (always)
        print("if (", ast.getCondition(), ") {");

        printIndentedBlock(ast.getThenStatements());

        newline(indent);
        print("}");

        // else block - if non-empty
        if(!ast.getElseStatements().isEmpty())
        {
            print(" else {");

            printIndentedBlock(ast.getElseStatements());

            newline(indent);
            print("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        print("switch (", ast.getCondition(), ") {");

        indent++;
        for(Ast.Statement.Case currCase : ast.getCases()) {
            newline(indent);
            print(currCase);
        }
        indent--;

        newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        if(ast.getValue().isPresent()) // case
        {
            print("case ", ast.getValue().get(), ":");

            printIndentedBlock(ast.getStatements());

            // must end cases with break
            indent++;
            newline(indent);
            print("break;");
            indent--;
        }
        else // default
        {
            print("default:");

            printIndentedBlock(ast.getStatements());
        }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        print("while (", ast.getCondition(), ") {");

        printIndentedBlock(ast.getStatements());

        // } on same line if no statements in body
        if(!ast.getStatements().isEmpty())
            newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        print("return ", ast.getValue(), ";");

        return null;
    }

    public void printIndentedBlock(List<Ast.Statement> statements)
    {
        indent++;
        for(Ast.Statement stmt : statements)
        {
            newline(indent);
            print(stmt);
        }
        indent--;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        if(ast.getType() == Environment.Type.STRING)
            print("\"", ast.getLiteral(), "\"");
        else if(ast.getType() == Environment.Type.CHARACTER)
            print("'", ast.getLiteral(), "'");
        else
            print(ast.getLiteral());

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        print("(", ast.getExpression(), ")");

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        if(ast.getOperator().equals("^"))
            print("Math.pow(", ast.getLeft(), ", ", ast.getRight(), ")");
        else
            print(ast.getLeft(), " " + ast.getOperator(), " ", ast.getRight());

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        print(ast.getVariable().getJvmName());

        // check for offset
        if(ast.getOffset().isPresent())
            print("[", ast.getOffset().get(), "]");

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        print(ast.getFunction().getJvmName(), "(");

        // parameters
        for(Ast.Expression arg : ast.getArguments())
        {
            if(arg.equals(ast.getArguments().getLast()))
                print(arg);
            else
                print(arg, ", ");
        }

        print(")");

        return null;
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {
        print("{");

        // values
        for(Ast.Expression val : ast.getValues())
        {
            if(val.equals(ast.getValues().getLast()))
                print(val);
            else
                print(val, ", ");
        }

        print("}");

        return null;
    }

}
