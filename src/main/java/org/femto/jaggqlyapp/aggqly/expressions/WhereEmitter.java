package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.List;

public class WhereEmitter implements NodeVisitor<WhereFunction> {
    public WhereFunction emit(List<TopLevelAstNode> nodes) {
        var innerGetters = nodes
                .stream()
                .map(n -> n.accept(this))
                .toList();

        return (t, args, ctx) -> {
            var sb = new StringBuilder();
            for (var g : innerGetters) {
                sb.append(g.get(t, args, ctx));
            }
            return sb.toString();
        };
    }

    @Override
    @SuppressWarnings("unused")
    public WhereFunction visit(FragmentNode node) {
        return (t, args, ctx) -> node.text().toString();
    }

    @Override
    public WhereFunction visit(ConditionalNode node) {
        var accessor = node.accessNode().accept(this);

        var innerGetters = node.innerNodes()
                .stream()
                .map(n -> n.accept(this))
                .toList();

        WhereFunction inner = (t, args, ctx) -> {
            var sb = new StringBuilder();
            for (var g : innerGetters) {
                sb.append(g.get(t, args, ctx));
            }
            return sb.toString();
        };

        return (t, args, ctx) -> accessor.get(t, args, ctx) != null ? inner.get(t, args, ctx) : "";
    }

    @Override
    @SuppressWarnings("unused")
    public WhereFunction visit(TTableCollectionNode node) {
        return (t, args, ctx) -> t + "." + node.member();
    }

    @Override
    public WhereFunction visit(LTableCollectionNode node) {
        throw new RuntimeException();
    }

    @Override
    public WhereFunction visit(MTableCollectionNode node) {
        throw new RuntimeException();
    }

    @Override
    public WhereFunction visit(RTableCollectionNode node) {
        throw new RuntimeException();
    }

    @Override
    @SuppressWarnings("unused")
    public WhereFunction visit(ArgCollectionNode node) {
        return (t, args, ctx) -> args.get(node.member());
    }

    @Override
    @SuppressWarnings("unused")
    public WhereFunction visit(CtxCollectionNode node) {
        return (t, args, ctx) -> ctx.get(node.member());
    }
}
