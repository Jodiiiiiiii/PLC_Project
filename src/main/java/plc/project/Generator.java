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
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Void visit(Ast.Global ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Void visit(Ast.Function ast) {
        throw new UnsupportedOperationException(); //TODO
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
