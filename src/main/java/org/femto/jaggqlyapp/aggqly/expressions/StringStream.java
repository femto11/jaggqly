package org.femto.jaggqlyapp.aggqly.expressions;

class StringStream {
    private final CharSequence str;
    private int pos;

    public StringStream(CharSequence str) {
        this.str = str;
        this.pos = 0;
    }

    public boolean eat() {
        return this.eat(1);
    }

    public boolean eat(int n) {
        this.pos += n;
        return this.pos >= this.str.length();
    }

    public Character peek() {
        return this.pos >= this.str.length() ? 0 : this.str.charAt(pos);
    }

    public Character peek(int n) {
        return this.pos + n >= this.str.length() ? 0 : this.str.charAt(pos + n);
    }

    public boolean eos() {
        return this.pos >= this.str.length();
    }

    public int at() {
        return this.pos;
    }

    public CharSequence extract(int start, int end) {
        return this.str.subSequence(start, end);
    }
}