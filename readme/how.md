### Create the main class

```java
public class ExampleMain implements RedisMain {

    private final RedisManager redisManager;
    private final RedisConfig redisConfig;
    private final Reflections reflections;
    private final Gson gson;

    public ExampleMain(Reflections reflections, Gson gson) {
        initializeRedisMain();

        this.reflections = reflections;
        this.redisConfig = new RedisConfig(); // This would usually be loaded from disk using a library like FileManager
        this.gson = gson;

        this.redisManager = new RedisManager();
    }

    @Override
    public @NotNull RedisManager getRedisManager() {
        return redisManager;
    }

    @Override
    public @NotNull RedisConfig getRedisConfig() {
        return redisConfig;
    }

    @Override
    public @NotNull Reflections getReflections() {
        return reflections;
    }

    @Override
    public @NotNull Gson getGson() {
        return gson;
    }
}
```

### Creating event

```java
public class ExampleEvent extends RedisEvent<Boolean> {

    public int data1;
    public String data2;

    public ExampleEvent(String redisTarget, int data1, String data2) {
        super(redisTarget);
        this.data1 = data1;
        this.data2 = data2;
    }
}
```

### Listen for events
```java
public class ExampleListener {
    @RedisEventHandler(autoRegister = true)
    public void onPing(PingEvent event) {
        event.respond(true);
    }
}
```

### Sending events
More methods for the RedisEvent class can be found in the JavaDocs

```java
public class ExampleListener {
    public void sendEvent(){
        new PingEvent("example_target").sendAndWait();
    }
}
```