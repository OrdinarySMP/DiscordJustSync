package tronka.justsync.core.event;

import java.util.ArrayList;
import java.util.List;
import tronka.justsync.core.event.payloads.chat.ChatMessageEvent;
import tronka.justsync.core.event.payloads.minecraft.AdvancementEvent;
import tronka.justsync.core.event.payloads.minecraft.CommandExecutedEvent;
import tronka.justsync.core.event.payloads.minecraft.MinecraftMessageEvent;
import tronka.justsync.core.event.payloads.minecraft.PlayerDeathEvent;
import tronka.justsync.core.event.payloads.minecraft.PlayerJoinedEvent;
import tronka.justsync.core.event.payloads.minecraft.PlayerJoiningEvent;
import tronka.justsync.core.event.payloads.minecraft.PlayerLeaveEvent;

public class EventRegister {
    private static final List<Event<?>> EVENTS = new ArrayList<>();

    // Minecraft
    public static final Event<MinecraftMessageEvent> MINECRAFT_MESSAGE_EVENT = registerEvent();
    public static final Event<AdvancementEvent> ADVANCEMENT_EVENT = registerEvent();
    public static final Event<PlayerJoiningEvent> PLAYER_JOINING_EVENT = registerEvent();
    public static final Event<PlayerJoinedEvent> PLAYER_JOINED_EVENT = registerEvent();
    public static final Event<PlayerLeaveEvent> PLAYER_LEAVE_EVENT = registerEvent();
    public static final Event<PlayerDeathEvent> PLAYER_DEATH_EVENT = registerEvent();
    public static final Event<CommandExecutedEvent> COMMAND_EXECUTED_EVENT = registerEvent();
    public static final Event<Void> SERVER_STARTED_EVENT = registerEvent();
    public static final Event<Void> SERVER_STOPPING_EVENT = registerEvent();
    public static final Event<Void> SERVER_STOPPED_EVENT = new Event<>(); // should not be reset

    // Chat
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
