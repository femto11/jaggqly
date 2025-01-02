parser grammar ExpressionParser;
options { tokenVocab=ExpressionLexer; }

expression
    : (element)* EOF
    ;

element
    : SQL 
    | directive 
    ;

directive 
    : OPENCURLY (accessor|conditional) CLOSECURLY
    ;

accessor
    : EXCLAMATION container OPENPAREN IDENTIFIER CLOSEPAREN
    ;

conditional
    : QUESTION container CLOSEPAREN IDENTIFIER OPENPAREN (element)*
    ;

container
    : LEFT | MIDDLE | RIGHT | CTX | ARG
    ;

