package tronka.justsync.events.payload;

import net.minecraft.server.level.ServerPlayer;

public record MinecraftToDiscordChatMessagePayload(
        String message, ServerPlayer player, MessageType type) {}
