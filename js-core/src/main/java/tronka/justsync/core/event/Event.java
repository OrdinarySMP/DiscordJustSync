package tronka.justsync.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Event<T> {
    List<Consumer<T>> subscribers = new ArrayList<>();

    Event() {}

    public void subscribe(Consumer<T> subscriber) {
        this.subscribers.add(subscriber);
    }

    void clear() {
        this.subscribers.clear();
    }

    public void invoke(T payload) {
        this.subscribers.forEach(subscriber -> subscriber.accept(payload));
    }
}
