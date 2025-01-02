package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.List;

public class TokenStream {
    private final List<Lexer.Token> tokens;
    private int pos;

    public TokenStream(final List<Lexer.Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public Lexer.Token eat() {
        return this.pos >= this.tokens.size() ? null : this.tokens.get(pos++);
    }

    public boolean eat(int n) {
        this.pos += n;
        return this.pos >= this.tokens.size();
    }

    public Lexer.Token peek() {
        return this.pos >= this.tokens.size() ? null : this.tokens.get(pos);
    }

    public Lexer.Token peek(int n) {
        return this.pos + n >= this.tokens.size() ? null : this.tokens.get(pos + n);
    }

    public boolean eos() {
        return this.pos >= this.tokens.size();
    }

    public int at() {
        return this.pos;
    }
}