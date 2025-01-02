package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

enum TokenKind {
    EOS,
    WSP,
    OPENCURLY,
    CLOSECURLY,
    QUESTIONMARK,
    EXCLAMATIONMARK,
    OPENPAREN,
    CLOSEPAREN,
    DOT,
    DOLLAR,
    T,
    L,
    M,
    R,
    ARG,
    CTX,
    IDENTIFIER,
    TEXT
}

enum TokenClass {
    NOTAPPLICABLE, COLLECTION
}

public class Lexer {
    private StringStream stream;

    public record Token(TokenKind kind, TokenClass clazz, CharSequence text) {
    };

    enum LexerContext {
        Body, Directive, Identifier
    }

    private static final Map<TokenKind, LexerContext> tokenContext = Map.ofEntries(
            Map.entry(TokenKind.TEXT, LexerContext.Body),
            Map.entry(TokenKind.OPENCURLY, LexerContext.Directive),
            Map.entry(TokenKind.CLOSECURLY, LexerContext.Body),
            Map.entry(TokenKind.QUESTIONMARK, LexerContext.Directive),
            Map.entry(TokenKind.EXCLAMATIONMARK, LexerContext.Directive),
            Map.entry(TokenKind.OPENPAREN, LexerContext.Identifier),
            Map.entry(TokenKind.CLOSEPAREN, LexerContext.Body),
            Map.entry(TokenKind.T, LexerContext.Directive),
            Map.entry(TokenKind.L, LexerContext.Directive),
            Map.entry(TokenKind.M, LexerContext.Directive),
            Map.entry(TokenKind.R, LexerContext.Directive),
            Map.entry(TokenKind.ARG, LexerContext.Directive),
            Map.entry(TokenKind.CTX, LexerContext.Directive),
            Map.entry(TokenKind.DOT, LexerContext.Identifier),
            Map.entry(TokenKind.DOLLAR, LexerContext.Identifier),
            Map.entry(TokenKind.IDENTIFIER, LexerContext.Directive));

    public List<Token> tokenize(String expression) {
        this.stream = new StringStream(expression);

        var tokens = new ArrayList<Token>();
        var token = nextToken(LexerContext.Body);
        tokens.add(token);
        while (token.kind != TokenKind.EOS) {
            token = nextToken(tokenContext.get(token.kind));
            tokens.add(token);
        }
        return tokens;
    }

    public Token nextToken(LexerContext context) {
        if (stream.peek() == 0) {
            return new Token(TokenKind.EOS, TokenClass.NOTAPPLICABLE, null);
        }

        return switch (context) {
            case Body -> scanBodyContext();
            case Directive -> scanDirectiveContext();
            case Identifier -> scanIdentifierContext();
        };
    }

    private Token scanBodyContext() {
        int start = stream.at();
        while (true) {
            switch (stream.peek()) {
                case 0:
                    if (stream.at() > start) {
                        return new Token(TokenKind.TEXT, TokenClass.NOTAPPLICABLE, stream.extract(start, stream.at()));
                    }
                    stream.eat();
                    return new Token(TokenKind.EOS, TokenClass.NOTAPPLICABLE, null);
                case '{':
                    if (stream.at() > start) {
                        return new Token(TokenKind.TEXT, TokenClass.NOTAPPLICABLE, stream.extract(start, stream.at()));
                    }
                    stream.eat();
                    return new Token(TokenKind.OPENCURLY, TokenClass.NOTAPPLICABLE, null);
                case '}':
                    if (stream.at() > start) {
                        return new Token(TokenKind.TEXT, TokenClass.NOTAPPLICABLE, stream.extract(start, stream.at()));
                    }
                    stream.eat();
                    return new Token(TokenKind.CLOSECURLY, TokenClass.NOTAPPLICABLE, null);
            }
            stream.eat();
        }
    }

    private Token scanIdentifierContext() {
        int start = stream.at();

        while (Character.isWhitespace(stream.peek())) {
            stream.eat();
        }

        if (stream.peek() == '$') {
            stream.eat();
            return new Token(TokenKind.DOLLAR, TokenClass.NOTAPPLICABLE, null);
        }

        if (stream.peek() == '.') {
            stream.eat();
            return new Token(TokenKind.DOT, TokenClass.NOTAPPLICABLE, null);
        }

        if (!Character.isJavaIdentifierStart(stream.peek())) {
            return null;
        }

        stream.eat();

        while (Character.isJavaIdentifierPart(stream.peek())) {
            stream.eat();
        }

        return new Token(TokenKind.IDENTIFIER, TokenClass.NOTAPPLICABLE, stream.extract(start, stream.at()));
    }

    private Token scanDirectiveContext() {
        while (Character.isWhitespace(stream.peek())) {
            stream.eat();
        }

        switch (stream.peek()) {
            case '{':
                stream.eat();
                return new Token(TokenKind.OPENCURLY, TokenClass.NOTAPPLICABLE, null);
            case '}':
                stream.eat();
                return new Token(TokenKind.CLOSECURLY, TokenClass.NOTAPPLICABLE, null);
            case '?': // ?l ?m ?r ?arg ?ctx
                stream.eat();
                return new Token(TokenKind.QUESTIONMARK, TokenClass.NOTAPPLICABLE, null);
            case '!': // !l !m !r !arg !ctx
                stream.eat();
                return new Token(TokenKind.EXCLAMATIONMARK, TokenClass.NOTAPPLICABLE, null);
            case '(':
                stream.eat();
                return new Token(TokenKind.OPENPAREN, TokenClass.NOTAPPLICABLE, null);
            case ')':
                stream.eat();
                return new Token(TokenKind.CLOSEPAREN, TokenClass.NOTAPPLICABLE, null);
            case 'a':
                if (this.stream.peek(1) == 'r'
                        && this.stream.peek(2) == 'g'
                        && !Character.isLetterOrDigit(stream.peek(3))) {
                    stream.eat(3);
                    return new Token(TokenKind.ARG, TokenClass.COLLECTION, null);
                }
                break;
            case 'c':
                if (this.stream.peek(1) == 't'
                        && this.stream.peek(2) == 'x'
                        && !Character.isLetterOrDigit(stream.peek(3))) {
                    stream.eat(3);
                    return new Token(TokenKind.CTX, TokenClass.COLLECTION, null);
                }
                break;
            case 't':
                if (!Character.isLetterOrDigit(stream.peek(1))) {
                    stream.eat();
                    return new Token(TokenKind.L, TokenClass.COLLECTION, null);
                }
                break;
            case 'l':
                if (!Character.isLetterOrDigit(stream.peek(1))) {
                    stream.eat();
                    return new Token(TokenKind.L, TokenClass.COLLECTION, null);
                }
                break;
            case 'm':
                if (!Character.isLetterOrDigit(stream.peek(1))) {
                    stream.eat();
                    return new Token(TokenKind.M, TokenClass.COLLECTION, null);
                }
                break;
            case 'r':
                if (!Character.isLetterOrDigit(stream.peek(1))) {
                    stream.eat();
                    return new Token(TokenKind.R, TokenClass.COLLECTION, null);
                }
                break;
        }

        return null;
    }

}
