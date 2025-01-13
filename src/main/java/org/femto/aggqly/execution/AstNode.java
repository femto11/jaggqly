package org.femto.aggqly.execution;

public interface AstNode {
    Generated accept(SqlGenerator visitor);
}