package dev.lightdream.redismanager;

import dev.lightdream.redismanager.event.RedisEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testGsonImplementation1() {
        RedisEvent<Boolean> event = new RedisEvent<>("test");
        event.setId(100);
        event.setOriginator("test_env");

        String json = event.serialize();

        RedisEvent<?> event2 = RedisEvent.deserialize(json);

        assertNotNull(event2);
        assertEquals(event.getClassName(), event2.getClassName());
        assertEquals(event.getId(), event2.getId());
        assertEquals(event.getOriginator(), event2.getOriginator());
        assertEquals(event.getRedisTarget(), event2.getRedisTarget());
    }

    @Test
    public void testGsonImplementation2() {
        ComplexEvent1 event = new ComplexEvent1(main, Arrays.asList("test1", "test2"), "test3");
        event.setId(100);
        event.setOriginator("test_env");

        String json = event.serialize();

        RedisEvent<?> event2 = RedisEvent.deserialize(json);

        assertNotNull(event2);
        assertEquals(event.getClassName(), event2.getClassName());
        assertEquals(event.getId(), event2.getId());
        assertEquals(event.getOriginator(), event2.getOriginator());
        assertEquals(event.getRedisTarget(), event2.getRedisTarget());
        assertTrue(event2 instanceof ComplexEvent1);

        ComplexEvent1 event3 = (ComplexEvent1) event2;

        assertNotNull(event3);
        assertEquals(event.getA(), event3.getA());
        assertEquals(event.getB(), event3.getB());
    }

}
