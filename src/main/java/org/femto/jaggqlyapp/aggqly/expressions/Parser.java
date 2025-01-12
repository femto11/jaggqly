package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

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

record TTableCollectionNode(String member, int lookback) implements CollectionNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

record LTableCollectionNode(String member, int lookback) implements CollectionNode {
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

record ArgCollectionNode(String member, int lookback) implements CollectionNode {
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

record FragmentNode(String text) implements TopLevelAstNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

public class Parser {
    private record CollectionTokenInfo(EnumSet<ParserMode> availableIn, Boolean supportsAncestorAcesss) {
    }

    private static final Map<TokenKind, CollectionTokenInfo> collectionTokenInfos = Map.of(
            TokenKind.T, new CollectionTokenInfo(EnumSet.allOf(ParserMode.class), true),
            TokenKind.L, new CollectionTokenInfo(EnumSet.allOf(ParserMode.class), false),
            TokenKind.M, new CollectionTokenInfo(EnumSet.of(ParserMode.JUNCTION_EXPRESSION), false),
            TokenKind.R,
            new CollectionTokenInfo(EnumSet.of(ParserMode.JOIN_EXPRESSION, ParserMode.JUNCTION_EXPRESSION), false),
            TokenKind.ARG, new CollectionTokenInfo(EnumSet.allOf(ParserMode.class), true),
            TokenKind.CTX, new CollectionTokenInfo(EnumSet.allOf(ParserMode.class), false));

    private TokenStream stream;

    private final ParserMode mode;

    public Parser(ParserMode mode) {
        this.mode = mode;
    }

    public List<TopLevelAstNode> parse(TokenStream tokens) throws ParserException {
        this.stream = tokens;

        final var nodes = parseExpression();

        return nodes;
    }

    private static String removeRedundantCharacters(String source) {
        return source.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ");
    }

    // (text | directive)*
    private List<TopLevelAstNode> parseExpression() throws ParserException {
        var nodes = new ArrayList<TopLevelAstNode>();

        while (true) {
            switch (this.stream.peek().kind()) {
                case TokenKind.TEXT: {
                    nodes.add(new FragmentNode(removeRedundantCharacters(this.stream.eat().text().toString())));
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

        final var collectionTokenInfo = collectionTokenInfos.get(token.kind());

        if (!collectionTokenInfo.availableIn().contains(this.mode)) {
            throw new ParserException(token.kind() + " not allowed in " + this.mode);
        }

        final var collection = this.stream.eat().kind();

        // {!l.$.$(foo)} | {!arg.$.$(bar)}
        // (EXCLAMATIONMARK | QUESTIONMARK) COLLECTION (.$)* OPENPAREN ...

        var lookbacks = 0;
        while (true) {
            if (this.stream.peek(lookbacks * 2).kind() != TokenKind.DOT) {
                if (this.stream.peek().kind() == TokenKind.DOLLAR) {
                    throw new ParserException(
                            "Incomplete ancestor access expression. Expected DOT but got DOLLAR");
                }

                break;
            }

            if (this.stream.peek(lookbacks * 2 + 1).kind() != TokenKind.DOLLAR) {
                throw new ParserException(
                        "Incomplete ancestor access expression. Expected DOLLAR but got " + this.stream.peek(2).kind());
            }

            ++lookbacks;
        }

        if (lookbacks > 0 && !collectionTokenInfo.supportsAncestorAcesss) {
            throw new ParserException(
                    "Illegal ancestor access on " + collection);
        }

        this.stream.eat(lookbacks * 2);

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
            case TokenKind.T -> new TTableCollectionNode(member, lookbacks);
            case TokenKind.L -> new LTableCollectionNode(member, lookbacks);
            case TokenKind.M -> new MTableCollectionNode(member);
            case TokenKind.R -> new RTableCollectionNode(member);
            case TokenKind.ARG -> new ArgCollectionNode(member, lookbacks);
            case TokenKind.CTX -> new CtxCollectionNode(member);
            default -> throw new RuntimeException("Fatal. Unknown collection type for " + collection);
        };
    }
}

/*
 * TableNode, $parent (TableNode, $parent (TableNode, null))
 * 
 * (TableNodWithAncestor) -> (TableNodWithAncestor) -> return TableNode
 * 
 */