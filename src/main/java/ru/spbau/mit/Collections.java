package ru.spbau.mit;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by olga on 27.09.15.
 */

public class Collections {
    static public<T, R> void map(Function1<T, R> f, Collection<T> src, Collection<? super R> dest) {
        for(T o: src) {
            dest.add(f.apply(o));
        }
    }

    static public <T> void filter(Predicate<T> p, Collection<T> src, Collection<? super T> dest) {
        for (T o:src) {
            if (p.apply(o)) {
                dest.add(o);
            }
        }
    }

    static public <T> void takeWhile(Predicate<T> p, Collection<T> src, Collection<? super T> dest) {
        for (T o:src) {
            if(!p.apply(o)) {
                break;
            }
            dest.add(o);
        }
    }

    static public <T> void takeUnless(Predicate<T> p, Collection<T> src, Collection<? super T> dest) {
        for (T o:src) {
            if(p.apply(o)) {
                break;
            }
            dest.add(o);
        }
    }

    static public <T, S> S foldr(Function2<? super T, ? super S, S> f, Collection<T> src, S start) {
        T[] cur = (T[]) src.toArray();
        for (int i = cur.length - 1; i >= 0; --i) {
            start = f.apply(cur[i], start);
        }
        return start;
    }

    static public <T, S> T foldl(Function2<? super T, ? super S, T> f, Collection<S> src, T start) {
        for (S o: src) {
            start = f.apply(start, o);
        }
        return start;
    }
}
