package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.Map;

@FunctionalInterface
public interface WhereFunction {
    String get(String t, Map<String, String> args, Map<String, String> ctx);

    static WhereFunction fromExpression(String s) throws ParserException {
        final var tokens = new Lexer().tokenize(s);
        final var nodes = new Parser(ParserMode.WHERE_EXPRESSION).parse(new TokenStream(tokens));
        return new WhereEmitter().emit(nodes);
    }
}