package tronka.justsync.core.event;

import tronka.justsync.core.event.payloads.AdvancementEvent;
import tronka.justsync.core.event.payloads.ChatMessageEvent;
import tronka.justsync.core.event.payloads.PlayerJoinedEvent;
import tronka.justsync.core.event.payloads.PlayerJoiningEvent;
import tronka.justsync.core.event.payloads.PlayerLeaveEvent;
import java.util.ArrayList;
import java.util.List;

public class EventRegister {
    private static final List<Event<?>> EVENTS = new ArrayList<>();

    public static final Event<ChatMessageEvent> CHAT_MESSAGE_EVENT = registerEvent();
    public static final Event<AdvancementEvent> ADVANCEMENT_EVENT = registerEvent();
    public static final Event<PlayerJoiningEvent> PLAYER_JOINING_EVENT = registerEvent();
    public static final Event<PlayerJoinedEvent> PLAYER_JOINED_EVENT = registerEvent();
    public static final Event<PlayerLeaveEvent> PLAYER_LEAVE_EVENT = registerEvent();

    public static <T> Event<T> registerEvent() {
        Event<T> event = new Event<>();
        EVENTS.add(event);
        return event;
    }

    public void clear() {
        EVENTS.forEach(Event::clear);
    }
}
