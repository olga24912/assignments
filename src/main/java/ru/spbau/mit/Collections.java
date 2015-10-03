package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by olga on 27.09.15.
 */

public class Collections {
    public static <T, R> Iterable<R> map(Function1<? super T, R> f, Iterable<T> src) {
        ArrayList<R> dest = new ArrayList<R>();
        for (T o : src) {
            dest.add(f.apply(o));
        }
        return dest;
    }

    public static <T> Iterable<T> filter(Predicate<? super T> p, Iterable<T> src) {
        ArrayList<T> dest = new ArrayList<T>();
        for (T o : src) {
            if (p.apply(o)) {
                dest.add(o);
            }
        }
        return dest;
    }

    public static <T> Iterable<T> takeWhile(Predicate<? super T> p, Iterable<T> src) {
        ArrayList<T> dest = new ArrayList<T>();
        for (T o : src) {
            if (!p.apply(o)) {
                break;
            }
            dest.add(o);
        }
        return dest;
    }

    public static <T> Iterable<T> takeUnless(Predicate<? super T> p, Iterable<T> src) {
        ArrayList<T> dest = new ArrayList<T>();
        for (T o : src) {
            if (p.apply(o)) {
                break;
            }
            dest.add(o);
        }
        return dest;
    }

    public static <T, S> S foldr(Function2<? super T, ? super S, S> f, Iterable<T> src, S start) {
        ArrayList<T> srcToArray = new ArrayList<>();
        for (T o : src) {
            srcToArray.add(o);
        }

        for (int i = srcToArray.size() - 1; i >= 0; --i) {
            start = f.apply(srcToArray.get(i), start);
        }
        return start;
    }

    public static <T, S> T foldl(Function2<? super T, ? super S, T> f, Iterable<S> src, T start) {
        for (S o : src) {
            start = f.apply(start, o);
        }
        return start;
    }
}
