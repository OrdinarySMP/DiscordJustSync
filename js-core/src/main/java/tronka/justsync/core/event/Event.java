package tronka.justsync.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Event<T> {
    List<Consumer<T>> subscribers = new ArrayList<>();
    List<Predicate<T>> filters = new ArrayList<>();

    Event() {}

    public void subscribe(Consumer<T> subscriber) {
        this.subscribers.add(subscriber);
    }

    void clear() {
        this.subscribers.clear();
    }

    public void invoke(T payload) {
        if (!shouldBroadcast(payload)) {
            return;
        }
        this.subscribers.forEach(subscriber -> subscriber.accept(payload));
    }

    public void addFilter(Predicate<T> filter) {
        this.filters.add(filter);
    }

    void clearFilter() {
        this.filters.clear();
    }

    private boolean shouldBroadcast(T payload) {
        for (Predicate<T> filter : this.filters) {
            if (!filter.test(payload)) {
                return false;
            }
        }
        return true;
    }
}
