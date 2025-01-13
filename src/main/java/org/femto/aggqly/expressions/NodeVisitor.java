package org.femto.aggqly.expressions;

interface NodeVisitor<T> {
    T visit(FragmentNode node);

    T visit(ConditionalNode node);

    T visit(TTableCollectionNode node);

    T visit(LTableCollectionNode node);

    T visit(MTableCollectionNode node);

    T visit(RTableCollectionNode node);

    T visit(ArgCollectionNode node);

    T visit(CtxCollectionNode node);
}