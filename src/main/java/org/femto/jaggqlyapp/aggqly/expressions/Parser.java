package org.femto.jaggqlyapp.aggqly.expressions;

import java.lang.classfile.components.ClassPrinter.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<TopLevelAstNode> parse(TokenStream tokens) {
        this.stream = tokens;

        final var nodes = parseExpression();

        return nodes;
    }

    // (text | directive)*
    private List<TopLevelAstNode> parseExpression() {
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
    private DirectiveNode parseDirective() {
        if (this.stream.eat().kind() != TokenKind.OPENCURLY) {
            // Error based on token
            return null;
        }

        final var node = switch (this.stream.peek().kind()) {
            case TokenKind.QUESTIONMARK -> this.parseConditional();
            case TokenKind.EXCLAMATIONMARK -> this.parseAccessor();
            default -> null;
        };

        if (this.stream.eat().kind() != TokenKind.CLOSECURLY) {
            // Error based on token
            return null;
        }

        return node;
    }

    // conditional
    // : QUESTIONMARK memberaccess expression+
    // ;
    private ConditionalNode parseConditional() {
        if (this.stream.eat().kind() != TokenKind.QUESTIONMARK) {
            // Error based on token
            return null;
        }

        final var memberAccessNode = this.parseCollection();
        final var expressionNodes = this.parseExpression();

        return new ConditionalNode(memberAccessNode, expressionNodes);
    }

    // accessor
    // : EXCLAMATIONMARK memberaccess
    // ;
    private CollectionNode parseAccessor() {
        if (this.stream.eat().kind() != TokenKind.EXCLAMATIONMARK) {
            // Error based on token
            return null;
        }

        return parseCollection();
    }

    // memberaccess
    // : collection identifier
    // ;
    private MemberAccessorNode parseMemberAccess() {

        final var collectionNode = this.parseCollection();
        final var identifierNode = this.parseIdentifier();

        return new MemberAccessorNode(collectionNode, identifierNode);
    }

    private CollectionNode parseCollection() {
        if (this.stream.peek().clazz() != TokenClass.CONTAINER) {
            // Error based on token
            return null;
        }

        final var collection = this.stream.eat().kind();

        if (this.stream.peek().kind() != TokenKind.OPENPAREN) {
            return null;
        }

        this.stream.eat();

        if (this.stream.peek().kind() != TokenKind.IDENTIFIER) {
            return null;
        }

        final var member = this.stream.eat().text().toString();

        if (this.stream.peek().kind() != TokenKind.CLOSEPAREN) {
            return null;
        }

        this.stream.eat();

        return switch (collection) {
            case TokenKind.L -> new LTableCollectionNode(member);
            case TokenKind.M -> new MTableCollectionNode(member);
            case TokenKind.R -> new RTableCollectionNode(member);
            case TokenKind.ARG -> new ArgCollectionNode(member);
            case TokenKind.CTX -> new CtxCollectionNode(member);
            default -> null;
        };
    }

    // identifier
    // : OPENPAREN IDENTIFIER CLOSEPAREN
    private IdentifierNode parseIdentifier() {
        if (this.stream.peek().kind() != TokenKind.OPENPAREN) {
            return null;
        }

        this.stream.eat();

        if (this.stream.peek().kind() != TokenKind.IDENTIFIER) {
            return null;
        }

        final var identifierNode = new IdentifierNode(this.stream.eat().text());

        if (this.stream.peek().kind() != TokenKind.CLOSEPAREN) {
            return null;
        }

        this.stream.eat();

        return identifierNode;
    }
}
