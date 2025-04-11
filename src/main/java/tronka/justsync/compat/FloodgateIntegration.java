package tronka.justsync.compat;

import org.geysermc.floodgate.api.FloodgateApi;
import tronka.justsync.JustSyncApplication;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FloodgateIntegration {

    private boolean loaded;

    private FloodgateApi floodgateApi;

    public FloodgateIntegration(JustSyncApplication integration) {
        if (integration.getConfig().integrations.allowMixedAccountTypes) {
            return;
        }
        this.floodgateApi = FloodgateApi.getInstance();
        this.loaded = true;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public boolean isBedrock(UUID uuid) {
        return this.floodgateApi.isFloodgateId(uuid);
    }

    public String getUsername(UUID uuid) {
        try {
            return this.floodgateApi.getGamertagFor(uuid.getLeastSignificantBits()).get();
        } catch (InterruptedException | ExecutionException ignored) {
        }
        return "unknown";
    }

    private FloodgateApi getFloodgateApi() {
        return this.floodgateApi;
    }

}

