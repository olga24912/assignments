package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by olga on 27.09.15.
 */

public class Collections {
    static public <T, R> Iterable<R> map(Function1<? super T, R> f, Iterable<T> src) {
        ArrayList<R> dest = new ArrayList<R>();
        for (T o : src) {
            dest.add(f.apply(o));
        }
        return dest;
    }

    static public <T> Iterable<T> filter(Predicate<? super T> p, Iterable<T> src) {
        ArrayList<T> dest = new ArrayList<T>();
        for (T o : src) {
            if (p.apply(o)) {
                dest.add(o);
            }
        }
        return dest;
    }

    static public <T> Iterable<T> takeWhile(Predicate<? super T> p, Iterable<T> src) {
        ArrayList<T> dest = new ArrayList<T>();
        for (T o : src) {
            if (!p.apply(o)) {
                break;
            }
            dest.add(o);
        }
        return dest;
    }

    static public <T> Iterable<T> takeUnless(Predicate<? super T> p, Iterable<T> src) {
        ArrayList<T> dest = new ArrayList<T>();
        for (T o : src) {
            if (p.apply(o)) {
                break;
            }
            dest.add(o);
        }
        return dest;
    }

    static public <T, S> S foldr(Function2<? super T, ? super S, S> f, Iterable<T> src, S start) {
        ArrayList<T> cur = new ArrayList<>();
        for (T o : src) {
            cur.add(o);
        }

        for (int i = cur.size() - 1; i >= 0; --i) {
            start = f.apply(cur.get(i), start);
        }
        return start;
    }

    static public <T, S> T foldl(Function2<? super T, ? super S, T> f, Iterable<S> src, T start) {
        for (S o : src) {
            start = f.apply(start, o);
        }
        return start;
    }
}
