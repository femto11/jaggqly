package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.List;
import java.util.stream.Collectors;

public class JoinEmitter implements NodeVisitor<JoinFunction> {
    public JoinFunction emit(List<TopLevelAstNode> nodes) {
        var innerGetters = nodes
                .stream()
                .map(n -> n.accept(this))
                .toList();

        return (l, r, args, ctx) -> {
            return innerGetters
                    .stream()
                    .map(g -> g.get(l, r, args, ctx))
                    .collect(Collectors.joining());
        };
    }

    @Override
    @SuppressWarnings("unused")
    public JoinFunction visit(FragmentNode node) {
        return (l, r, args, ctx) -> node.text().toString();
    }

    @Override
    public JoinFunction visit(ConditionalNode node) {
        var accessor = node.accessNode().accept(this);

        var innerGetters = node.innerNodes()
                .stream()
                .map(n -> n.accept(this))
                .toList();

        JoinFunction inner = (l, r, args, ctx) -> {
            return innerGetters
                    .stream()
                    .map(g -> g.get(l, r, args, ctx))
                    .collect(Collectors.joining());
        };

        return (l, r, args, ctx) -> accessor.get(l, r, args, ctx) != null ? inner.get(l, r, args, ctx) : "";
    }

    @Override
    public JoinFunction visit(TTableCollectionNode node) {
        throw new RuntimeException();
    }

    @Override
    @SuppressWarnings("unused")
    public JoinFunction visit(LTableCollectionNode node) {
        return (l, r, args, ctx) -> l + "." + node.member();
    }

    @Override
    public JoinFunction visit(MTableCollectionNode node) {
        throw new RuntimeException();
    }

    @Override
    @SuppressWarnings("unused")
    public JoinFunction visit(RTableCollectionNode node) {
        return (l, r, args, ctx) -> r + "." + node.member();
    }

    @Override
    @SuppressWarnings("unused")
    public JoinFunction visit(ArgCollectionNode node) {
        return (l, r, args, ctx) -> args.get(node.member());
    }

    @Override
    @SuppressWarnings("unused")
    public JoinFunction visit(CtxCollectionNode node) {
        return (l, r, args, ctx) -> ctx.get(node.member());
    }
}
