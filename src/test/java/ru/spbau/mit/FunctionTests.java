package ru.spbau.mit;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;


public class FunctionTests {

    @Test
    public void testCompose() {
        Function1<Integer, Integer> plus5 = new Function1<Integer, Integer>() {
            @Override
            public Integer apply(Integer x) {
                return x + 5;
            }
        };

        Function1<Integer, Integer> mul2 = new Function1<Integer, Integer>() {
            @Override
            public Integer apply(Integer x) {
                return 2*x;
            }
        };

        assertTrue(mul2.compose(plus5).apply(1).equals(7));
        assertTrue(plus5.compose(mul2).apply(1).equals(12));
    }

    @Test
    public void testCompose2() {
        Function1<Integer, Integer> plus5 = new Function1<Integer, Integer>() {
            @Override
            public Integer apply(Integer x) {
                return x + 5;
            }
        };

        Function2<Integer, Integer, Integer> mul = new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer x, Integer y) {
                return x * y;
            }
        };

        assertTrue(mul.compose(plus5).apply(2, 1).equals(7));
    }

    @Test
    public void testBind() {
        Function2<Integer, Integer, Integer> div = new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer x, Integer y) {
                return x/y;
            }
        };

        Function1<Integer, Integer> div2 = div.bind2(2);

        Function1<Integer, Integer> div128 = div.bind1(128);

        assertTrue(div2.apply(128).equals(div128.apply(2)));
        assertTrue(div2.apply(128) == 64);
    }

    @Test
    public void testCurry() {
        Function2<Integer, Integer, Integer> div = new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer x, Integer y) {
                return x/y;
            }
        };


        Function1<Integer, Integer> div128 = div.bind1(128);

        assertTrue(div.curry().apply(128).apply(2).equals(div128.apply(2)));

        assertTrue(div.curry().apply(128).apply(24).equals(div128.apply(24)));
    }

    @Test
    public void testPredicate() {
        Predicate<String> isPalindrome = new Predicate<String>() {
            @Override
            public Boolean apply(String x) {
                for (int i = 0, j = x.length() - 1; i < x.length(); i++, j--) {
                    if (x.charAt(i) != x.charAt(j)) {
                        return false;
                    }
                }
                return true;
            }
        };

        Predicate<String> isAB = new Predicate<String>() {
            @Override
            public Boolean apply(String x) {
                for (int i = 0; i < x.length(); ++i) {
                    if (x.charAt(i) != 'a' && x.charAt(i) != 'b') {
                        return false;
                    }
                }
                return true;
            }
        };

        assertTrue(isPalindrome.apply("abacaba"));
        assertFalse(isAB.apply("abacaba"));

        assertFalse(isAB.and(isPalindrome).apply("abacaba"));
        assertFalse(isPalindrome.and(isAB).apply("abacaba"));

        Predicate<String> notUse = new Predicate<String>() {
            @Override
            public Boolean apply(String x) {
                return (1/0 == 0);
            }
        };

        assertFalse(isPalindrome.and(notUse).apply("abaaa"));


        assertTrue(isAB.apply("a"));
        assertTrue(isPalindrome.apply("a"));

        assertTrue(isAB.and(isPalindrome).apply("a"));

        assertTrue(isPalindrome.or(notUse).apply("abacaba"));
        assertTrue(isPalindrome.or(isAB).apply("abbbb"));

        assertTrue(Predicate.ALWAYS_FALSE.not().apply(null));

        assertFalse(Predicate.ALWAYS_TRUE.not().apply(null));

    }

    @Test
    public void testMap() {
        ArrayList<Integer> arr = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            arr.add(i);
        }

        Function1<Integer, Integer> plus5 = new Function1<Integer, Integer>() {
            @Override
            public Integer apply(Integer x) {
                return x + 5;
            }
        };

        ArrayList<Integer> res = (ArrayList<Integer>) Collections.map(plus5, arr);

        assertTrue(res.size() == 10);
        for (int i = 0; i < 10; i++) {
            assertTrue(res.get(i) == i + 5);
        }
    }

    @Test
    public void testFilter() {
        ArrayList<Integer> arr = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            arr.add(i);
        }

        Predicate<Integer> mod2 = new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                return x % 2 == 0;
            }
        };

        ArrayList<Integer> res = (ArrayList<Integer>) Collections.filter(mod2, arr);

        assertTrue(res.size() == 5);
        for (int i = 0; i < 5; i++) {
            assertTrue(res.get(i) == 2 * i);
        }
    }

    @Test
    public void testTakeWhile() {
        ArrayList<Integer> arr = new ArrayList<Integer>();
        for (int i = 0; i < 9; i++) {
            arr.add(i);
        }
        arr.add(0);

        Predicate<Integer> less6 = new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                return x < 6;
            }
        };

        ArrayList<Integer> res = (ArrayList<Integer>) Collections.takeWhile(less6, arr);

        assertTrue(res.size() == 6);
        for (int i = 0; i < 6; i++) {
            assertTrue(res.get(i) == i);
        }
    }

    @Test
    public void testTakeUnless() {
        ArrayList<Integer> arr = new ArrayList<Integer>();
        for (int i = 0; i < 9; i++) {
            arr.add(i);
        }
        arr.add(0);

        Predicate<Integer> more6 = new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                return x > 6;
            }
        };

        ArrayList<Integer> res = (ArrayList<Integer>) Collections.takeUnless(more6, arr);

        assertTrue(res.size() == 7);
        for (int i = 0; i < 7; i++) {
            assertTrue(res.get(i) == i);
        }
    }

    @Test
    public void testFold() {
        ArrayList<Integer> arr = new ArrayList<Integer>();
        for (int i = 0; i < 5; i++) {
            arr.add(i);
        }

        Function2<Integer, Integer, Integer> a2b = new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer x, Integer y) {
                return 2*x + y;
            }
        };

        Collections col = new Collections();

        assertTrue(Collections.foldr(a2b, arr, 0) == 2 + 4 + 6 + 8);
        assertTrue(col.foldl(a2b, arr, 0) == (((2 + 2) * 2 + 3) * 2 + 4));
    }
}