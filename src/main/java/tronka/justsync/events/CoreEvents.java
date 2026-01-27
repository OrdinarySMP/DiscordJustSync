package tronka.justsync.events;

import net.minecraft.server.level.ServerPlayer;
import tronka.justsync.events.payload.AdvancementPayload;
import tronka.justsync.events.payload.CommandPayload;
import tronka.justsync.events.payload.DeathPayload;
import tronka.justsync.events.payload.MinecraftChatMessagePayload;

public class CoreEvents {
    public static final Event<MinecraftChatMessagePayload> MINECRAFT_CHAT_MESSAGE = new Event<>();
    public static final Event<AdvancementPayload> ADVANCEMENT_GRANTED = new Event<>();
    public static final Event<DeathPayload> PLAYER_DEATH = new Event<>();
    public static final Event<ServerPlayer> PLAYER_JOIN = new Event<>();
    public static final Event<ServerPlayer> PLAYER_DISCONNECT = new Event<>();
    public static final Event<ServerPlayer> PLAYER_TIMEOUT = new Event<>();
    public static final Event<CommandPayload> COMMAND_EXECUTED = new Event<>();
}
