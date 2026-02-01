package tronka.justsync.events;

import tronka.justsync.events.payload.MinecraftToDiscordMessagePayload;

public class ChatEvents {

    private ChatEvents() {}

    public static final Event<MinecraftToDiscordMessagePayload> MINECRAFT_TO_DISCORD_CHAT_MESSAGE =
            new Event<>();
}
