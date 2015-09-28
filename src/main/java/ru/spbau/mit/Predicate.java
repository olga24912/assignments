package ru.spbau.mit;

import java.util.Objects;

/**
 * Created by olga on 27.09.15.
 */
public abstract class Predicate<T> extends Function1<T, Boolean> {
    public Predicate<T> not() {
        return new Predicate<T>() {
            @Override
            public Boolean apply(T x) {
                return !Predicate.this.apply(x);
            }
        };
    }

    public Predicate<T> or(final Predicate<? super T> b) {
        return new Predicate<T>() {
            @Override
            public Boolean apply(T x) {
                return Predicate.this.apply(x) || b.apply(x);
            }
        };
    }

    public Predicate<T> and(final Predicate<? super T> b) {
        return new Predicate<T>() {
            @Override
            public Boolean apply(T x) {
                return Predicate.this.apply(x) && b.apply(x);
            }
        };
    }

    public static Predicate<?> ALWAYS_TRUE = new Predicate<Object>() {
        @Override
        public Boolean apply(Object x) {
            return true;
        }
    };

    public static Predicate<?> ALWAYS_FALSE = new Predicate<Object>() {
        @Override
        public Boolean apply(Object x) {
            return false;
        }
    };
}
