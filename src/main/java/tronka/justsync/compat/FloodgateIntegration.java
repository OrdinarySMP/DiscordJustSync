package tronka.justsync.compat;

import net.fabricmc.loader.api.FabricLoader;
import org.geysermc.floodgate.api.FloodgateApi;
import tronka.justsync.JustSyncApplication;
import tronka.justsync.config.Config;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FloodgateIntegration {

    private boolean loaded;

    private FloodgateApi floodgateApi;

    public FloodgateIntegration(JustSyncApplication integration) {
        if (FabricLoader.getInstance().isModLoaded("floodgate-modded")) {
            this.floodgateApi = FloodgateApi.getInstance();
        }
        integration.registerConfigReloadHandler(this::onConfigLoaded);
    }

    private void onConfigLoaded(Config config) {
        this.loaded = !config.integrations.floodgate.allowMixedAccountTypes && this.floodgateApi != null;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public boolean isBedrock(UUID uuid) {
        return this.isLoaded() && this.floodgateApi.isFloodgateId(uuid);
    }

    public String getUsername(UUID uuid) {
        if (this.isLoaded()) {
            try {
                return this.floodgateApi.getGamertagFor(uuid.getLeastSignificantBits()).get();
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }
        return "unknown";
    }

}

