package dev.lightdream.redismanager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RedisTest {

    private static TestRedisMain main;

    @BeforeAll
    public static void init() {
        main = new TestRedisMain();
    }

    @Test
    public void simpleEvent1() {
        SimpleEvent1 event1 = new SimpleEvent1(main, 25, 10);
        Integer result = event1.sendAndGet();

        assertEquals(35, result);
    }

    @Test
    public void simpleEvent2() {
        SimpleEvent2 event1 = new SimpleEvent2(main, Arrays.asList("test1", "test2"), "-");
        String result = event1.sendAndGet();

        assertEquals("test1-test2-", result);
    }

    @Test
    public void complexEvent1() {
        ComplexEvent1 event1 = new ComplexEvent1(main, Arrays.asList("test1", "test2"), "test3");
        List<String> result = event1.sendAndGet();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("test1", result.get(0));
        assertEquals("test2", result.get(1));
        assertEquals("test3", result.get(2));
    }

}
