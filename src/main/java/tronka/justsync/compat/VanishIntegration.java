package tronka.justsync.compat;

import me.drex.vanish.api.VanishAPI;
import me.drex.vanish.api.VanishEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import tronka.justsync.JustSyncApplication;
import tronka.justsync.config.Config;

public class VanishIntegration {

    private final JustSyncApplication integration;
    private boolean loaded;

    public VanishIntegration(JustSyncApplication integration) {
        this.integration = integration;
        if (!FabricLoader.getInstance().isModLoaded("melius-vanish")) {
            return;
        }
        integration.registerConfigReloadHandler(this::onConfigLoaded);
        VanishEvents.VANISH_EVENT.register(this::onVanishChanged);
    }

    private void onVanishChanged(ServerPlayerEntity player, boolean isVanished) {
        if (!this.loaded) {
            return;
        }
        if (isVanished) {
            this.integration.getChatBridge().onPlayerLeave(player, true);
        } else {
            this.integration.getChatBridge().onPlayerJoin(player, true);
        }
    }

    private void onConfigLoaded(Config config) {
        this.loaded = config.integrations.enableVanishIntegration;
    }


    public boolean isVanished(ServerPlayerEntity player) {
        return this.loaded && VanishAPI.isVanished(player);
    }
}
