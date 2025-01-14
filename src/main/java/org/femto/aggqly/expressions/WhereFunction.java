package org.femto.aggqly.expressions;

import java.util.Map;

@FunctionalInterface
public interface WhereFunction {
    String get(ExecutableAggqlyType t, MapWithAncestor<String, String> args, Map<String, String> ctx);

    static WhereFunction fromExpression(String s) throws ParserException {
        final var tokens = new Lexer().tokenize(s);
        final var nodes = new Parser(ParserMode.WHERE_EXPRESSION).parse(new TokenStream(tokens));
        return new WhereEmitter().emit(nodes);
    }
}