package dev.lightdream.redismanager;

import dev.lightdream.redismanager.annotation.RedisEventHandler;

import java.util.ArrayList;
import java.util.List;

public class TestListener {

    @RedisEventHandler(autoRegister = true)
    public void onSimpleEvent1(SimpleEvent1 event) {

        event.respond(event.getA() + event.getB());
    }

    @RedisEventHandler(autoRegister = true)
    public void onSimpleEvent2(SimpleEvent2 event) {
        StringBuilder output = new StringBuilder();

        for (String s : event.getA()) {
            output.append(s).append(event.getB());
        }

        event.respond(output.toString());
    }

    @RedisEventHandler(autoRegister = true)
    public void onComplexEvent1(ComplexEvent1 event) {
        List<String> output = new ArrayList<>(event.getA());
        output.add(event.getB());

        event.respond(output);
    }

}
