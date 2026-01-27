package tronka.justsync.compat;

import me.drex.vanish.api.VanishAPI;
import me.drex.vanish.api.VanishEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import tronka.justsync.JustSyncApplication;
import tronka.justsync.config.Config;
import tronka.justsync.events.ChatEvents;
import tronka.justsync.events.CoreEvents;
import tronka.justsync.events.payload.MinecraftToDiscordChatMessagePayload;

public class VanishIntegration {

    private boolean loaded = false;
    private boolean priorityMessageSending = false;

    public VanishIntegration(JustSyncApplication integration) {
        if (!FabricLoader.getInstance().isModLoaded("melius-vanish")) {
            return;
        }
        integration.registerConfigReloadHandler(this::onConfigLoaded);
        VanishEvents.VANISH_EVENT.register(this::onVanishChanged);
        ChatEvents.MINECRAFT_TO_DISCORD_CHAT_MESSAGE.addFilter(this::filterVanishChat);
    }

    private void onVanishChanged(ServerPlayer player, boolean isVanished) {
        if (!this.loaded) {
            return;
        }
        this.priorityMessageSending = true;
        if (isVanished) {
            CoreEvents.PLAYER_DISCONNECT.invoke(player);
        } else {
            CoreEvents.PLAYER_JOIN.invoke(player);
        }
        this.priorityMessageSending = false;
    }

    private boolean filterVanishChat(MinecraftToDiscordChatMessagePayload payload) {
        return !this.isVanished(payload.player()) || this.priorityMessageSending;
    }

    private void onConfigLoaded(Config config) {
        this.loaded = config.integrations.enableVanishIntegration;
    }


    public boolean isVanished(ServerPlayer player) {
        return this.loaded && VanishAPI.isVanished(player);
    }
}
