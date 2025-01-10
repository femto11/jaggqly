package org.femto.jaggqlyapp.aggqly.execution;

public interface AstNode {
    Generated accept(SqlGenerator visitor);
}