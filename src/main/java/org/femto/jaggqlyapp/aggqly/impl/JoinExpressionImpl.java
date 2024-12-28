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

    public static JoinExpression fromString(String expression) {
        final var fragments = new ArrayList<String>();
        final var accessors = new ArrayList<Getter>();

        var matches = JoinExpressionImpl.pattern.matcher(expression).results().toList();

        var fragmentStart = 0;
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

        return new JoinExpressionImpl(fragments, accessors);
    }

    private final List<String> fragments;

    private final List<Getter> accessors;

    private JoinExpressionImpl(List<String> fragments, List<Getter> accessors) {
        this.fragments = Collections.unmodifiableList(fragments);
        this.accessors = Collections.unmodifiableList(accessors);
    }

    @FunctionalInterface
    interface Getter {
        String method(String ltable, String rtable, Map<String, String> args, Map<String, String> ctx);
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
    private static Getter argGetter(String name) {
        return (ltable, rtable, args, ctx) -> {
            return args.containsKey(name) ? args.get(name).toString() : "unknown";
        };
    }

    @SuppressWarnings({ "unused" })
    private static Getter ctxGetter(String name) {
        return (ltable, rtable, args, ctx) -> {
            return args.containsKey(name) ? ctx.get(name).toString() : "unknown";
        };
    }

    @Override
    public String method(String ltable, String rtable, Map<String, String> args, Map<String, String> ctx) {
        var sb = new StringBuilder();

        for (var i = 0; i < this.fragments.size(); i++) {
            sb.append(fragments.get(i));
            sb.append(accessors.get(i).method(ltable, rtable, args, ctx));
        }

        return sb.toString();
    }
}
