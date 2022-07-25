import dev.lightdream.logger.Debugger;
import dev.lightdream.redismanager.event.impl.PingEvent;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IssuesTest {

    private static RedisMainImpl redisMain;

    @BeforeAll
    static void setup() {
        redisMain = new RedisMainImpl();
        redisMain.enable();
    }

    // Title:       Infinite reconnect
    // Description: Happens when the redis server is down and the client tries to send a command.
    @SneakyThrows
    @Test
    @Order(1)
    public void issue1() {
        Debugger.log("Restarting redis thread");
        redisMain.getRedisManager().startRedisThread();
        Debugger.log("Sending event");
        new PingEvent("test_target").sendAndWait(redisMain);
        Thread.sleep(10000);
    }

}
