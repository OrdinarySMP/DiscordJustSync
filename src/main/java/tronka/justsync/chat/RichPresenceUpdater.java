package tronka.justsync.chat;

import net.dv8tion.jda.api.entities.Activity;
import net.minecraft.server.level.ServerPlayer;
import tronka.justsync.JustSyncApplication;
import tronka.justsync.config.Config;
import tronka.justsync.events.CoreEvents;

public class RichPresenceUpdater {
    private final JustSyncApplication integration;
    private long onlineCount = 0;
    public RichPresenceUpdater(JustSyncApplication integration) {
        this.integration = integration;
        integration.registerConfigReloadHandler(this::onConfigLoaded);

        CoreEvents.PLAYER_JOIN.subscribe(this::onPlayerJoin);
        CoreEvents.PLAYER_DISCONNECT.subscribe(this::onPlayerLeave);
        CoreEvents.PLAYER_TIMEOUT.subscribe(this::onPlayerLeave);
    }

    private void onConfigLoaded(Config config) {
        if (this.integration.getServer() == null) {
            return;
        }
        this.onlineCount = this.integration.getServer().getPlayerList()
                .getPlayers().stream()
                .filter(p -> !this.integration.getVanishIntegration().isVanished(p))
                .count();
        this.updateRichPresence();
    }

    private void onPlayerJoin(ServerPlayer player) {
        this.onlineCount++;
    }

    private void onPlayerLeave(ServerPlayer player) {
        this.onlineCount--;
    }

    private void updateRichPresence() {
        if (!this.integration.getConfig().showPlayerCountStatus) {
            return;
        }

        this.integration.getJda().getPresence().setPresence(Activity.playing(switch ((int) this.onlineCount) {
            case 0 -> this.integration.getConfig().messages.onlineCountZero;
            case 1 -> this.integration.getConfig().messages.onlineCountSingular;
            default -> this.integration.getConfig().messages.onlineCountPlural.formatted(this.onlineCount);
        }), false);
    }
}
