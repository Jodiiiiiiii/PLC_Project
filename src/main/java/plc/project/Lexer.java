package plc.project;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The lexer works through three main functions:
 * *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which does lexing on the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 * *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid.
 * *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are * helpers you need to use, they will make the implementation a lot easier. */
public final class Lexer {

    public static final Pattern
            NONZERO_DIGIT = Pattern.compile("([1-9])"),
            DIGIT = Pattern.compile("([0-9])"),
            IDENTIFIER_START = Pattern.compile("([A-Za-z@])"),
            IDENTIFIER_PART = Pattern.compile("([A-Za-z0-9_-])"),
            ESCAPE_CHAR = Pattern.compile("([bnrt'\"\\\\])");

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexing the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();

        // continue lexing until no more input
        while(chars.index < chars.input.length()){
            // skip whitespace characters
            if(peek("[ \b\n\r\t]")) // set of whitespace characters in our grammar
            {
                chars.advance();
                chars.skip();
            }
            else // lex new token starting from current non-whitespace character
                tokens.add(lexToken());
        }
        // return finalized list of tokens
        return tokens;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     * *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {
        // Identifier: a-z,A-Z,@
        if(peek(IDENTIFIER_START.pattern()))
            return lexIdentifier();
        // Number: 0-9 | '-' AND 1-9 | '-' AND 0 AND '.'
        else if(peek(DIGIT.pattern()) || peek("-", NONZERO_DIGIT.pattern()) || peek("-", "0", "."))
            return lexNumber();
        // Character: '
        else if(peek("'"))
            return lexCharacter();
        // String: "
        else if(peek("\""))
            return lexString();
        // Operator: [any other character]
        else
            return lexOperator();
    }

    public Token lexIdentifier() {
        // read first character - already confirmed earlier
        match(IDENTIFIER_START.pattern());
        // iterate through remaining matches
        while(peek(IDENTIFIER_PART.pattern()))
            match(IDENTIFIER_PART.pattern());
        // return final string of matched characters
        return chars.emit(Token.Type.IDENTIFIER);
    }

    public Token lexNumber() {
        // match negative (-) sign if applicable
        if(peek("-"))
            match("-");
        // Zero
        if(peek("0")) {
            match("0");
        }
        // Non-zero number
        else
        {
            // Integer | Decimal (before decimal point)
            while(peek(DIGIT.pattern()))
                match(DIGIT.pattern());
        }
        // Decimal?
        if(peek("\\.", DIGIT.pattern())) {
            match("\\.", DIGIT.pattern());
            // iterate through valid subsequent digits (after decimal point)
            while(peek(DIGIT.pattern()))
                match(DIGIT.pattern());
            // return final DECIMAL token
            return chars.emit(Token.Type.DECIMAL);
        }
        // not a decimal - return final INTEGER token
        return chars.emit(Token.Type.INTEGER);
    }

    public Token lexCharacter() {
        // Opening '
        match("'");

        // Single character
        if(peek("\\\\"))  // escape characters
            lexEscape();
        else if(peek("'")) // invalid ' as character
            throw new ParseException("missing/invalid single quotation in character literal", chars.index);
        else if(peek("[^\n\r]")) // valid character (no spanning multiple lines)
            match("[^\n\r]"); // match any next character
        else
            throw new ParseException("character literal cannot span multiple lines", chars.index);

        // Closing '
        if(!peek("'")) // no closing ' character
            throw new ParseException("missing/invalid single quotation in character literal", chars.index);
        else
            match("'");

        // return final character token
        return chars.emit(Token.Type.CHARACTER);
    }

    public Token lexString() {
        // Opening "
        match("\"");

        // Repeat until Closing "
        while(!peek("\"")) {
            if(peek("\\\\"))  // escape characters
                lexEscape();
            else if(peek("[^\n\r]")) // valid character (does not span multiple lines)
                match("[^\n\r]"); // match any next character (does not span multiple lines)
            else
                throw new ParseException("string literal cannot span multiple lines", chars.index);
        }

        // Closing "
        match("\"");

        // return final character token
        return chars.emit(Token.Type.STRING);
    }

    public void lexEscape() {
        match("\\\\"); // already known
        // check for valid escape character
        if(!peek(ESCAPE_CHAR.pattern()))
            throw new ParseException("invalid escape character", chars.index);
        else
            match(ESCAPE_CHAR.pattern());
    }

    public Token lexOperator() {
        // !=
        if(peek("!", "="))
            match("!","=");
        // ==
        else if(peek("=", "="))
            match("=", "=");
        // &&
        else if(peek("&", "&"))
            match("&", "&");
        // ||
        else if(peek("\\|", "\\|"))
            match("\\|", "\\|");
        else if(peek(".")) // should NOT include whitespace characters (parsed out earlier - don't worry about it here)
            match("."); // match whatever other character there is

        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) { // Note: allows for peaking several characters at once!! (useful for looking ahead)
        // check for issues (not enough characters or non-matching characters)
        for(int i = 0; i < patterns.length; i++)
            if(!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i]) )
                return false;
        // no issues found -> return true
        return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        boolean peek = peek(patterns);
        if(peek) {
            // advance for each character
            for(int i = 0; i < patterns.length; i++){
                chars.advance();
            }
        }
        // return same result from peek
        return peek;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     * *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }

}