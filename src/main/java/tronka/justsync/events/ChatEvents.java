package tronka.justsync.events;

import tronka.justsync.events.payload.MinecraftToDiscordChatMessagePayload;

public class ChatEvents {

    private ChatEvents() {}

    public static final Event<MinecraftToDiscordChatMessagePayload> MINECRAFT_TO_DISCORD_CHAT_MESSAGE =
            new Event<>();
}
