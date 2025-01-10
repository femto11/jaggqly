package org.femto.jaggqlyapp.aggqly.execution;

public interface SqlGenerator {

    Generated generate(SelectNode node);

    Generated generate(JoinNode node);

    Generated generate(InterfaceNode node);

    Generated generate(ColumnNode node);

    Generated generate(NullNode node);
}
