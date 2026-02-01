package tronka.justsync.chat;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import tronka.justsync.JustSyncApplication;
import tronka.justsync.Utils;
import tronka.justsync.config.Config;
import tronka.justsync.config.MessageFormat;
import tronka.justsync.events.ChatEvents;
import tronka.justsync.events.CoreEvents;
import tronka.justsync.events.payload.AdvancementPayload;
import tronka.justsync.events.payload.CommandPayload;
import tronka.justsync.events.payload.DeathPayload;
import tronka.justsync.events.payload.MessageType;
import tronka.justsync.events.payload.MinecraftChatMessagePayload;
import tronka.justsync.events.payload.MinecraftToDiscordMessagePayload;


public class MinecraftToDiscordPreprocessor {
    private final JustSyncApplication integration;
    private Config config;

    public MinecraftToDiscordPreprocessor(JustSyncApplication integration) {
        this.integration = integration;
        this.config = integration.getConfig();
        integration.registerConfigReloadHandler(this::onConfigLoaded);

        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        CoreEvents.MINECRAFT_CHAT_MESSAGE.subscribe(this::onChatMessage);
        CoreEvents.ADVANCEMENT_GRANTED.subscribe(this::onAdvancement);
        CoreEvents.PLAYER_DEATH.subscribe(this::onPlayerDeath);
        CoreEvents.PLAYER_JOIN.subscribe(this::onPlayerJoin);
        CoreEvents.PLAYER_DISCONNECT.subscribe(this::onPlayerDisconnect);
        CoreEvents.PLAYER_TIMEOUT.subscribe(this::onPlayerTimeout);
        CoreEvents.COMMAND_EXECUTED.subscribe(this::onCommandExecute);

        this.sendChatMessageToDiscord(this.getFormatString(MessageType.SERVER_START), null, MessageType.SERVER_START);
    }

    private void onConfigLoaded(Config config) {
        this.config = config;
    }

    private void onServerStopping(MinecraftServer server) {
        this.sendChatMessageToDiscord(this.getFormatString(MessageType.SERVER_STOP), null, MessageType.SERVER_STOP);
    }

    private void onChatMessage(MinecraftChatMessagePayload payload) {
        String message = payload.message();
        if (this.config.waypoints.formatWaypoints) {
            message = Utils.formatXaero(message, this.config);
            message = Utils.formatVoxel(message, this.config, payload.player());
        }
        this.sendChatMessageToDiscord(message, payload.player(), MessageType.CHAT);
    }

    private void onAdvancement(AdvancementPayload payload) {
        if (payload.advancement().display().isEmpty()) {
            return;
        }
        DisplayInfo advancement = payload.advancement().display().get();
        if (!this.config.announceAdvancements || !advancement.shouldAnnounceChat()) {
            return;
        }

        String message = this.getFormatString(MessageType.ADVANCEMENT)
                .replace("%title%", advancement.getTitle().getString())
                .replace("%description%", advancement.getDescription().getString());
        this.sendChatMessageToDiscord(message, payload.player(), MessageType.ADVANCEMENT);
    }

    private void onPlayerDeath(DeathPayload payload) {
        if (!this.config.broadCastDeathMessages) {
            return;
        }
        String message = payload.damageSource().getLocalizedDeathMessage(payload.player()).getString();
        if (message.equals("death.attack.badRespawnPoint")) {
            message = "%s was killed by [Intentional Mod Design]".formatted(payload.player().getName().getString());
        }
        this.sendChatMessageToDiscord(Utils.escapeUnderscores(message), payload.player(), MessageType.DEATH);
    }

    public void onPlayerJoin(ServerPlayer player) {
        this.sendChatMessageToDiscord(this.getFormatString(MessageType.JOIN), player, MessageType.JOIN);
    }

    public void onPlayerDisconnect(ServerPlayer player) {
        this.sendChatMessageToDiscord(this.getFormatString(MessageType.LEAVE), player, MessageType.LEAVE);
    }

    private void onPlayerTimeout(ServerPlayer player) {
        this.sendChatMessageToDiscord(this.getFormatString(MessageType.TIMEOUT), player, MessageType.TIMEOUT);
    }

    public void onCommandExecute(CommandPayload payload) {
        String command = payload.command();
        CommandSourceStack source = payload.source();
        if (!command.startsWith("me") && !command.startsWith("say")) {
            return;
        }
        ServerPlayer sender;
        String prefix;
        if (source.getEntity() instanceof ServerPlayer player) {
            sender = player;
            prefix = "";
        } else {
            sender = null;
            prefix = Utils.escapeUnderscores(source.getTextName()) + ": ";
        }
        String data = command.split(" ", 2)[1];
        String message;
        if (command.startsWith("me")) {
            message = prefix + "*" + data + "*";
        } else {
            message = prefix + data;
        }
        this.sendChatMessageToDiscord(message, sender, MessageType.COMMAND_SAY);
    }

    private String getFormatString(MessageType type) {
        MessageFormat messageFormat = this.config.messages.formats.get(type);
        if (messageFormat != null && !messageFormat.format.isEmpty()) {
            return messageFormat.format;
        }
        return "?MISSING?";
    }

    private void sendChatMessageToDiscord(String message, ServerPlayer player, MessageType type) {
        message = Utils.escapeMentions(message);
        if (type == MessageType.CHAT || type == MessageType.COMMAND_SAY) {
            message = Utils.formatMentions(message, this.integration, player);
        }
        ChatEvents.MINECRAFT_TO_DISCORD_CHAT_MESSAGE.invoke(new MinecraftToDiscordMessagePayload(message, player, type));
    }
}
