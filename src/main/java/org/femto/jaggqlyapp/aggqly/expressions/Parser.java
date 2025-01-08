package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.ArrayList;
import java.util.List;

import org.femto.jaggqlyapp.aggqly.expressions.Lexer.Token;

// {?arg:jjjkgj AND COALESCE({arg:jjkgj}, 0) = 1}

// start := TEXT directive TEXT* EOS
// directive := conditional | arg | ctx | ltab 
// conditional := {?:name (TEXT|directive)+}
// arg := {arg:name}
// arg := {ctx:name}
// ltab := {ltab:name}

interface AstNode {
}

interface EmittableNode {
    <T> T accept(NodeVisitor<T> visitor);
}

interface TopLevelAstNode extends AstNode, EmittableNode {
}

record MemberAccessorNode(CollectionNode collection, IdentifierNode member) implements AstNode {
}

record ConditionalNode(CollectionNode accessNode, List<TopLevelAstNode> innerNodes)
        implements DirectiveNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

interface CollectionNode extends DirectiveNode {
    String member();
}

record TTableCollectionNode(String member) implements CollectionNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

record LTableCollectionNode(String member) implements CollectionNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

record MTableCollectionNode(String member) implements CollectionNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

record RTableCollectionNode(String member) implements CollectionNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

record ArgCollectionNode(String member) implements CollectionNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

record CtxCollectionNode(String member) implements CollectionNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

record IdentifierNode(CharSequence name) implements AstNode {
}

interface DirectiveNode extends TopLevelAstNode {

}

record FragmentNode(CharSequence text) implements TopLevelAstNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

public class Parser {
    private TokenStream stream;

    public List<TopLevelAstNode> parse(TokenStream tokens) throws ParserException {
        this.stream = tokens;

        final var nodes = parseExpression();

        return nodes;
    }

    // (text | directive)*
    private List<TopLevelAstNode> parseExpression() throws ParserException {
        var nodes = new ArrayList<TopLevelAstNode>();

        while (true) {
            switch (this.stream.peek().kind()) {
                case TokenKind.TEXT: {
                    nodes.add(new FragmentNode(this.stream.eat().text()));
                    break;
                }
                case TokenKind.OPENCURLY: {
                    nodes.add(parseDirective());
                    break;
                }
                default: {
                    return nodes;
                }
            }
        }
    }

    // directive
    // : OPENCURLY (conditional | accessor) CLOSECURLY
    // ;
    private DirectiveNode parseDirective() throws ParserException {
        TokenKind token = this.stream.eat().kind();
        if (token != TokenKind.OPENCURLY) {
            throw new ParserException("Expected OPENCURLY but got " + token);
        }

        token = this.stream.peek().kind();
        final var node = switch (token) {
            case TokenKind.QUESTIONMARK -> this.parseConditional();
            case TokenKind.EXCLAMATIONMARK -> this.parseAccessor();
            default ->
                throw new ParserException("Expected QUESTIONMARK | EXCLAMATIONMARK but got " + token);
        };

        token = this.stream.eat().kind();
        if (token != TokenKind.CLOSECURLY) {
            throw new ParserException("Expected CLOSECURLY but got " + token);
        }

        return node;
    }

    // conditional
    // : QUESTIONMARK memberaccess expression+
    // ;
    private ConditionalNode parseConditional() throws ParserException {
        TokenKind token = this.stream.eat().kind();
        if (token != TokenKind.QUESTIONMARK) {
            throw new ParserException("Expected QUESTIONMARK but got " + token);
        }

        final var memberAccessNode = this.parseCollection();
        final var expressionNodes = this.parseExpression();

        return new ConditionalNode(memberAccessNode, expressionNodes);
    }

    // accessor
    // : EXCLAMATIONMARK memberaccess
    // ;
    private CollectionNode parseAccessor() throws ParserException {
        TokenKind token = this.stream.eat().kind();
        if (token != TokenKind.EXCLAMATIONMARK) {
            throw new ParserException("Expected EXCLAMATIONMARK but got " + token);
        }

        return parseCollection();
    }

    private CollectionNode parseCollection() throws ParserException {
        Token token = this.stream.peek();
        if (token.clazz() != TokenClass.COLLECTION) {
            throw new ParserException("Expected COLLECTION but got " + token.kind());
        }

        final var collection = this.stream.eat().kind();

        token = this.stream.peek();
        if (token.kind() != TokenKind.OPENPAREN) {
            throw new ParserException("Expected OPENPAREN but got " + token);
        }

        this.stream.eat();

        token = this.stream.peek();
        if (token.kind() != TokenKind.IDENTIFIER) {
            throw new ParserException("Expected IDENTIFIER but got " + token);
        }

        final var member = this.stream.eat().text().toString();

        token = this.stream.peek();
        if (token.kind() != TokenKind.CLOSEPAREN) {
            throw new ParserException("Expected CLOSEPAREN but got " + token);
        }

        this.stream.eat();

        return switch (collection) {
            case TokenKind.T -> new TTableCollectionNode(member);
            case TokenKind.L -> new LTableCollectionNode(member);
            case TokenKind.M -> new MTableCollectionNode(member);
            case TokenKind.R -> new RTableCollectionNode(member);
            case TokenKind.ARG -> new ArgCollectionNode(member);
            case TokenKind.CTX -> new CtxCollectionNode(member);
            default -> null;
        };
    }
}
