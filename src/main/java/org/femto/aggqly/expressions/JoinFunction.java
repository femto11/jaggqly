package org.femto.aggqly.expressions;

import java.util.Map;

@FunctionalInterface
public interface JoinFunction {
    String get(ExecutableAggqlyType l, ExecutableAggqlyType r, MapWithAncestor<String, String> args,
            Map<String, String> ctx);

    default WhereFunction reduce(ExecutableAggqlyType l) {
        return (r, args, ctx) -> this.get(l, r, args, ctx);
    }

    public static JoinFunction fromExpression(String s) throws ParserException {
        final var tokens = new Lexer().tokenize(s);
        final var nodes = new Parser(ParserMode.JOIN_EXPRESSION).parse(new TokenStream(tokens));
        return new JoinEmitter().emit(nodes);
    }
}