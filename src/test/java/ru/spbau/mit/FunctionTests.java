package ru.spbau.mit;

import static org.junit.Assert.*;
import org.junit.Test;



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
}