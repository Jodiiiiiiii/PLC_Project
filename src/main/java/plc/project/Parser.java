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
        if(peek("LET"))
            return parseDeclarationStatement();
        else if(peek("SWITCH"))
            return parseSwitchStatement();
        else if(peek("CASE") || peek("DEFAULT"))
            return parseCaseStatement();
        else if(peek("IF"))
            return parseIfStatement();
        else if(peek("WHILE"))
            return parseWhileStatement();
        else if(peek("RETURN"))
            return parseReturnStatement();
        else // expression/assignment statement
        {
            // left side expression always present for expression/assignment statement
            Ast.Expression left = parseExpression();

            if(peek("="))
            {
                match("=");
                Ast.Expression right = parseExpression();

                // check for missing semicolon
                if(!peek(";"))
                    throw new ParseException("Expected ; - invalid assignment statement", getErrorIndex());
                match(";");

                return new Ast.Statement.Assignment(left, right);
            }

            // check for missing semicolon
            if(!peek(";"))
                throw new ParseException("Expected ; - invalid expression statement", getErrorIndex());
            match(";");

            // no assignment, so it is an expression
            return new Ast.Statement.Expression(left);
        }
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        match("LET");

        // identifier
        if(!peek(Token.Type.IDENTIFIER))
            throw new ParseException("Expected identifier - invalid declaration statement", getErrorIndex());
        String name = tokens.get(0).getLiteral();
        match(Token.Type.IDENTIFIER);

        // Assignment - optional
        if(peek("="))
        {
            match("=");

            Ast.Expression value = parseExpression();

            // check for semicolon - required
            if(!peek(";"))
                throw new ParseException("Expected ; - invalid declaration statement", getErrorIndex());
            match(";");

            // return declaration with initialization
            return new Ast.Statement.Declaration(name, Optional.of(value));
        }

        // check for semicolon - required
        if(!peek(";"))
            throw new ParseException("Expected ; - invalid declaration statement", getErrorIndex());
        match(";");

        // return declaration without initialization
        return new Ast.Statement.Declaration(name, Optional.empty());
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        match("IF");

        // if expression
        Ast.Expression condition = parseExpression();

        // DO - required
        if(!peek("DO"))
            throw new ParseException("Expected DO - invalid if statement", getErrorIndex());
        match("DO");

        // if block
        List<Ast.Statement> ifBlock = parseBlock();

        // else block - optional
        if(peek("ELSE"))
        {
            match("ELSE");
            List<Ast.Statement> elseBlock = parseBlock();

            // END - required
            if(!peek("END"))
                throw new ParseException("Expected END - invalid if-else statement", getErrorIndex());
            match("END");

            return new Ast.Statement.If(condition, ifBlock, elseBlock);
        }

        // END - required
        if(!peek("END"))
            throw new ParseException("Expected END - invalid if statement", getErrorIndex());
        match("END");

        return new Ast.Statement.If(condition, ifBlock, new ArrayList<>()); // empty block for else
    }

    /**
     * Parses a switch statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a switch statement, aka
     * {@code SWITCH}.
     */
    public Ast.Statement.Switch parseSwitchStatement() throws ParseException {
        match("SWITCH");

        Ast.Expression condition = parseExpression();

        // iterate through cases
        List<Ast.Statement.Case> cases = new ArrayList<>();
        while(peek("CASE"))
        {
            cases.add(parseCaseStatement());
        }

        // DEFAULT case - required
        if(!peek("DEFAULT"))
            throw new ParseException("Expected DEFAULT - missing default case in switch statement", getErrorIndex());
        match("DEFAULT");
        cases.add(new Ast.Statement.Case(Optional.empty(), parseBlock()));

        // END - required
        if(!peek("END"))
            throw new ParseException("Missing END - invalid switch statement", getErrorIndex());
        match("END");

        return new Ast.Statement.Switch(condition, cases);
    }

    /**
     * Parses a case or default statement block from the {@code switch} rule. 
     * This method should only be called if the next tokens start the case or 
     * default block of a switch statement, aka {@code CASE} or {@code DEFAULT}.
     */
    public Ast.Statement.Case parseCaseStatement() throws ParseException {
        match("CASE");

        // expression value - required
        Ast.Expression value = parseExpression();

        // Colon - required
        if(!peek(":"))
            throw new ParseException("Expected : in case statement", getErrorIndex());
        match(":");

        return new Ast.Statement.Case(Optional.of(value), parseBlock());
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        match("WHILE");

        // condition - required
        Ast.Expression condition = parseExpression();

        // DO - required
        if(!peek("DO"))
            throw new ParseException("Expected DO - invalid while loop", getErrorIndex());
        match("DO");

        // block
        List<Ast.Statement> block = parseBlock();

        // END - required
        if(!peek("END"))
            throw new ParseException("Expected END - invalid while loop", getErrorIndex());
        match("END");

        return new Ast.Statement.While(condition, block);
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Statement.Return parseReturnStatement() throws ParseException {
        match("RETURN");

        // expression - required
        Ast.Expression expr = parseExpression();

        // semicolon - required
        if(!peek(";"))
            throw new ParseException("Expected ; - invalid return statement", getErrorIndex());
        match(";");

        return new Ast.Statement.Return(expr);
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() throws ParseException {
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expression parseLogicalExpression() throws ParseException {
        Ast.Expression left = parseComparisonExpression(); // gather left expression if not passed as parameter already

        // check for and loop through multiplicative operators
        if(peek("&&") || peek("||"))
        {
            String operator = tokens.get(0).getLiteral(); // gather operator for AST construction
            match(Token.Type.OPERATOR); // consume operator token

            // start new recursive call for right hand side
            Ast.Expression right = parseComparisonExpression();
            // return final binary
            return new Ast.Expression.Binary(operator, left, right);
        }

        // else just return left hand expression (non-binary)
        return left;
    }

    /**
     * Parses the {@code comparison-expression} rule.
     */
    public Ast.Expression parseComparisonExpression() throws ParseException {
        Ast.Expression left = parseAdditiveExpression(); // gather left expression if not passed as parameter already

        // check for and loop through multiplicative operators
        if(peek(">") || peek("<") || peek("==") || peek("!="))
        {
            String operator = tokens.get(0).getLiteral(); // gather operator for AST construction
            match(Token.Type.OPERATOR); // consume operator token

            // start new recursive call for right hand side
            Ast.Expression right = parseAdditiveExpression();
            // return final binary
            return new Ast.Expression.Binary(operator, left, right);
        }

        // else just return left hand expression (non-binary)
        return left;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() throws ParseException {
        Ast.Expression left = parseMultiplicativeExpression(); // gather left expression if not passed as parameter already

        // check for and loop through multiplicative operators
        if(peek("+") || peek("-"))
        {
            String operator = tokens.get(0).getLiteral(); // gather operator for AST construction
            match(Token.Type.OPERATOR); // consume operator token

            // start new recursive call for right hand side
            Ast.Expression right = parseMultiplicativeExpression();
            // return final binary
            return new Ast.Expression.Binary(operator, left, right);
        }

        // else just return left hand expression (non-binary)
        return left;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression() throws ParseException {
        Ast.Expression left = parsePrimaryExpression(); // gather left expression if not passed as parameter already

        // check for and loop through multiplicative operators
        if(peek("*") || peek("/") || peek("^"))
        {
            String operator = tokens.get(0).getLiteral(); // gather operator for AST construction
            match(Token.Type.OPERATOR); // consume operator token

            // start new recursive call for right hand side
            Ast.Expression right = parsePrimaryExpression();
            // return final binary
            return new Ast.Expression.Binary(operator, left, right);
        }

        // else just return left hand expression (non-binary)
        return left;
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
                throw new ParseException("Expected ) - invalid expression grouping", getErrorIndex());
            match(")");

            return group;
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
                    throw new ParseException("Expected , - invalid function parameters", getErrorIndex());
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
                throw new ParseException("Expected ] - invalid list access", getErrorIndex());
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
        else
        {
            throw new ParseException("Expected valid primary expression - no literal, group, function, or access found", getErrorIndex());
        }
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

    private int getErrorIndex(){
        if (tokens.has(0)) {
            return tokens.get(0).getIndex(); // valid next token, just invalid parsing
        }

        if(tokens.has(-1))
        {
            Token prevToken = tokens.get(-1);
            return prevToken.getIndex() + prevToken.getLiteral().length(); // no subsequent token, but yes previous term
        }

        return 0; // no tokens (previous or subsequent)

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
