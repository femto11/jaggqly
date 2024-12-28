package org.femto.jaggqlyapp.aggqly.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.femto.jaggqlyapp.aggqly.JoinExpression;

public class JoinExpressionImpl implements JoinExpression {
    private static Pattern pattern = ExpressionRegEx.buildPattern(new String[] {
            ExpressionRegEx.leftFragment,
            ExpressionRegEx.rightFragment,
            ExpressionRegEx.argFragment,
            ExpressionRegEx.ctxFragment
    });

    private final List<String> fragments;

    private final List<Getter> accessors;

    public JoinExpressionImpl(String expression) {
        super();

        final var fragments = new ArrayList<String>();
        final var accessors = new ArrayList<Getter>();
        var fragmentStart = 0;
        var matches = pattern.matcher(expression).results().toList();
        for (var match : matches) {
            for (var group : match.namedGroups().keySet()) {
                if (match.group(group) != null) {
                    final var value = match.group(match.namedGroups().get(group));

                    final var getter = switch (group) {
                        case "left" -> lTableCol(value);
                        case "right" -> rTableCol(value);
                        case "arg" -> argGetter(value);
                        case "ctx" -> ctxGetter(value);
                        default -> null;
                    };

                    accessors.add(getter);
                    break;
                }
            }
            fragments.add(expression.substring(fragmentStart, match.start()));
            fragmentStart = match.end();
        }

        this.fragments = Collections.unmodifiableList(fragments);
        this.accessors = Collections.unmodifiableList(accessors);
    }

    @FunctionalInterface
    interface Getter {
        String method(String ltable, String rtable, Map<String, Object> args, Map<String, Object> ctx);
    }

    @SuppressWarnings({ "unused" })
    private static Getter lTableCol(String name) {
        return (ltable, rtable, args, ctx) -> ltable + '.' + name;
    };

    @SuppressWarnings({ "unused" })
    private static Getter rTableCol(String name) {
        return (ltable, rtable, args, ctx) -> rtable + '.' + name;
    };

    @SuppressWarnings({ "unused" })
    private Getter argGetter(String name) {
        return (ltable, rtable, args, ctx) -> {
            return args.containsKey(name) ? args.get(name).toString() : "unknown";
        };
    }

    @SuppressWarnings({ "unused" })
    private Getter ctxGetter(String name) {
        return (ltable, rtable, args, ctx) -> {
            return args.containsKey(name) ? ctx.get(name).toString() : "unknown";
        };
    }

    @Override
    public String method(String ltable, String rtable, Map<String, Object> args, Map<String, Object> ctx) {
        var sb = new StringBuilder();

        for (var i = 0; i < this.fragments.size(); i++) {
            sb.append(fragments.get(i));
            sb.append(accessors.get(i).method(ltable, rtable, args, ctx));
        }

        return sb.toString();
    }
}
