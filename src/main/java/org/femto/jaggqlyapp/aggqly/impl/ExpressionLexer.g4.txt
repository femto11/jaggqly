lexer grammar ExpressionLexer;

OPENCURLY: '{';
CLOSECURLY: '}';
OPENPAREN: '(';
CLOSEPAREN: ')';
QUESTION: '?';
EXCLAMATION: '!';
DOLLAR: '$';
DOT: '.';
ARG: 'arg';
CTX: 'ctx';
LEFT: 'l';
MIDDLE: 'm';
RIGHT: 'r';

IDENTIFIER
    : [a-zA-Z][_a-zA-Z0-9]*
    ;

SQL
    : [^{}]+
    ;
