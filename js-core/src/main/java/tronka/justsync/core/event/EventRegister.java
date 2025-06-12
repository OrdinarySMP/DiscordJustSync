package tronka.justsync.core.event;

import tronka.justsync.core.event.payloads.ChatMessageEvent;
import java.util.ArrayList;
import java.util.List;

public class EventRegister {
    private static final List<Event<?>> EVENTS = new ArrayList<>();

    public static final Event<ChatMessageEvent> CHAT_MESSAGE_EVENT = registerEvent();

    public static <T> Event<T> registerEvent() {
        Event<T> event = new Event<>();
        EVENTS.add(event);
        return event;
    }

    public void clear() {
        EVENTS.forEach(Event::clear);
    }
}
