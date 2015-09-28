package ru.spbau.mit;

import java.util.Collections;

/**
 * Created by olga on 27.09.15.
 */
public abstract class Function2<T,S,R> {
    public abstract R apply(T x, S y);

    public <E> Function2<T, S, E> compose(final Function1<? super R, E> g) {
        return new Function2<T, S, E>() {
            @Override
            public E apply(T x, S y) {
                return g.apply(Function2.this.apply(x, y));
            }
        };
    }

    public Function1<S, R> bind1(final T x) {
        return new Function1<S, R>() {
            @Override
            public R apply(S y) {
                return Function2.this.apply(x, y);
            }
        };
    }

    public Function1<T, R> bind2(final S y) {
        return new Function1<T, R>() {
            @Override
            public R apply(T x) {
                return Function2.this.apply(x, y);
            }
        };
    }

    public Function1<T, Function1<S, R> > curry() {
        return new Function1<T, Function1<S, R>>() {
            @Override
            public Function1<S, R> apply(T x) {
                return Function2.this.bind1(x);
            }
        };
    }
}