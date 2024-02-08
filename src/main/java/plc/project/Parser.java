package plc.project;

// new import statements
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code global} rule. This method should only be called if the
     * next tokens start a global, aka {@code LIST|VAL|VAR}.
     */
    public Ast.Global parseGlobal() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code list} rule. This method should only be called if the
     * next token declares a list, aka {@code LIST}.
     */
    public Ast.Global parseList() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code mutable} rule. This method should only be called if the
     * next token declares a mutable global variable, aka {@code VAR}.
     */
    public Ast.Global parseMutable() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code immutable} rule. This method should only be called if the
     * next token declares an immutable global variable, aka {@code VAL}.
     */
    public Ast.Global parseImmutable() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code function} rule. This method should only be called if the
     * next tokens start a method, aka {@code FUN}.
     */
    public Ast.Function parseFunction() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code block} rule. This method should only be called if the
     * preceding token indicates the opening a block of statements.
     */
    public List<Ast.Statement> parseBlock() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Statement parseStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a switch statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a switch statement, aka
     * {@code SWITCH}.
     */
    public Ast.Statement.Switch parseSwitchStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a case or default statement block from the {@code switch} rule. 
     * This method should only be called if the next tokens start the case or 
     * default block of a switch statement, aka {@code CASE} or {@code DEFAULT}.
     */
    public Ast.Statement.Case parseCaseStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Statement.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() throws ParseException {
        //throw new UnsupportedOperationException(); //TODO

        // temp
        return parsePrimaryExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expression parseLogicalExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code comparison-expression} rule.
     */
    public Ast.Expression parseComparisonExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expression parsePrimaryExpression() throws ParseException {
        // LITERALS
        if(peek("NIL")) // NIL - null
        {
            match("NIL");

            return new Ast.Expression.Literal(null);
        }
        else if(peek("TRUE")) // TRUE - boolean
        {
            match("TRUE");

            return new Ast.Expression.Literal(true);
        }
        else if(peek("FALSE")) // FALSE - boolean
        {
            match("FALSE");

            return new Ast.Expression.Literal(false);
        }
        else if(peek(Token.Type.INTEGER)) { // integer
            BigInteger literal = new BigInteger(tokens.get(0).getLiteral());
            match(Token.Type.INTEGER);

            return new Ast.Expression.Literal(literal);
        }
        else if(peek(Token.Type.DECIMAL)) { // decimal
            BigDecimal literal = new BigDecimal(tokens.get(0).getLiteral());
            match(Token.Type.DECIMAL);

            return new Ast.Expression.Literal(literal);
        }
        else if(peek(Token.Type.CHARACTER)) {
            String literal = tokens.get(0).getLiteral().substring(1, 2); // ignore single quotes
            // handle escape characters
            literal = handleEscapes(literal);

            match(Token.Type.CHARACTER);

            return new Ast.Expression.Literal(literal.charAt(0)); // convert string to char
        }
        else if(peek(Token.Type.STRING)) {
            String literal = tokens.get(0).getLiteral().substring(1, tokens.get(0).getLiteral().length()-1);
            literal = handleEscapes(literal);

            match(Token.Type.STRING);

            return new Ast.Expression.Literal(literal);
        }
        else if(peek("(")) // expression grouping
        {
            // consume matched open parentheses
            match("(");

            // parse grouped expression
            Ast.Expression.Group group = new Ast.Expression.Group(parseExpression()); // recursive call to expression

            // check for matched closing parentheses
            if(!peek(")")) // invalid grouping
                throw new ParseException("invalid expression grouping - closing parentheses expected", tokens.get(tokens.index).getIndex());
            match(")");
        }
        else if(peek(Token.Type.IDENTIFIER, "(")) // function call
        {
            // function name (identifier)
            String funName = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER, "(");

            // arguments
            List<Ast.Expression> arguments = new ArrayList<>();
            if(!peek(")")) // check for first argument
                arguments.add(parseExpression());
            while(!peek(")")) // additional arguments
            {
                // comma to separate arguments
                if(!peek(",")) // invalid argument separation
                    throw new ParseException("invalid function call - comma expected", tokens.get(tokens.index).getIndex());
                match(",");

                arguments.add(parseExpression());
            }

            return new Ast.Expression.Function(funName, arguments);
        }
        else if(peek(Token.Type.IDENTIFIER, "[")) // access list
        {
            // name (identifier)
            String name = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER, "[");

            // offset (expression)
            Ast.Expression offset = parseExpression();

            // closing square bracket
            if(!match("]"))
                throw new ParseException("invalid list access - expected closing square bracket", tokens.get(tokens.index).getIndex());
            match("]");

            return new Ast.Expression.Access(Optional.of(offset), name); // generate access object for list
        }
        else if(peek(Token.Type.IDENTIFIER)) // access identifier
        {
            // name (identifier)
            String name = tokens.get(0).getLiteral();
            match(Token.Type.IDENTIFIER);

            return new Ast.Expression.Access(Optional.empty(), name);
        }

        throw new ParseException("invalid primary expression - no literal, group, function, or access found", 1);
    }

    private String handleEscapes(String literal) {
        // handle escape characters
        literal = literal.replace("\\\\", "\\");
        literal = literal.replace("\\b", "\b");
        literal = literal.replace("\\n", "\n");
        literal = literal.replace("\\r", "\r");
        literal = literal.replace("\\t", "\t");
        literal = literal.replace("\\\"", "\"");
        literal = literal.replace("\\'", "'");
        return literal;
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; i++)
        {
            if (!tokens.has(i)){
                return false; // fail if that many tokens do not remain
            } else if (patterns[i] instanceof Token.Type) {
                if (patterns[i] != tokens.get(i).getType()) {
                    return false; // fail if token type does not match expected
                }
            } else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral())) {
                    return false; // fail if literal does not match expected
                }
            } else {
                // invalid object class type input (i.e. not Token.Type or String - should never occur)
                throw new AssertionError("Invalid pattern object: " +  patterns[i].getClass());
            }
        }
        return true; // true if did not fail
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        // advance token stream for each individual match
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                tokens.advance();
            }
        }
        return peek; // return same return as peek
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
