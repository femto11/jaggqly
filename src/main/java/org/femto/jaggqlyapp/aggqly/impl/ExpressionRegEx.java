package org.femto.jaggqlyapp.aggqly.impl;

import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

class ExpressionRegEx {
    public static final String tableFragment = "/\\{t:(?<left>[_A-Za-z]\\w*)\\}";
    public static final String leftFragment = "/\\{l:(?<left>[_A-Za-z]\\w*)\\}";
    public static final String rightFragment = "/\\{r:(?<right>[_A-Za-z]\\w*)\\}";
    public static final String argFragment = "/\\{arg:(?<arg>($.)*[_A-Za-z]\\w*)\\}";
    public static final String ctxFragment = "/\\{ctx:(?<ctx>[_A-Za-z]\\w*)\\}";

    public static final Pattern buildPattern(@NotNull String[] fragments) {
        StringBuilder sb = new StringBuilder(fragments[0]);
        for (int i = 1; i < fragments.length; i++) {
            sb.append('|').append(fragments[i]);
        }
        return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
    }
}