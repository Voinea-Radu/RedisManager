package dev.lightdream.redismanager;

import dev.lightdream.filemanager.FileManager;
import dev.lightdream.filemanager.GsonSerializer;
import dev.lightdream.filemanager.GsonSettings;
import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.Printer;
import dev.lightdream.messagebuilder.MessageBuilderManager;
import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.manager.RedisManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RedisTest {

    @BeforeAll
    public static void init() {
        GsonSettings gsonSettings = new GsonSettings();

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages("dev.lightdream.redismanager")
                        .setScanners(Scanners.MethodsAnnotated, Scanners.TypesAnnotated, Scanners.SubTypes)
        );

        Printer.builder()
                .debugToConsole(true)
                .build();

        Debugger.log("HERE");
        Debugger.log(reflections.getSubTypesOf(GsonSerializer.class));

        FileManager.builder()
                .gsonSettings(gsonSettings)
                .build();

        MessageBuilderManager.builder().build();

        RedisManager.builder()
                .gsonSettings(gsonSettings)
                .redisConfig(new RedisConfig())
                .reflections(reflections)
                .localOnly(true)
                .build();
    }

    @Test
    public void simpleEvent1() {
        SimpleEvent1 event1 = new SimpleEvent1(25, 10);
        Integer result = event1.sendAndGet();

        assertEquals(35, result);
    }

    @Test
    public void simpleEvent2() {
        SimpleEvent2 event1 = new SimpleEvent2( Arrays.asList("test1", "test2"), "-");
        String result = event1.sendAndGet();

        assertEquals("test1-test2-", result);
    }

    @Test
    public void complexEvent1() {
        ComplexEvent1 event1 = new ComplexEvent1(Arrays.asList("test1", "test2"), "test3");
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
        ComplexEvent1 event = new ComplexEvent1( Arrays.asList("test1", "test2"), "test3");
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
