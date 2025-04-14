package tronka.justsync.compat;

import net.fabricmc.loader.api.FabricLoader;
import org.geysermc.floodgate.api.FloodgateApi;
import tronka.justsync.JustSyncApplication;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FloodgateIntegration {
    private FloodgateApi floodgateApi;

    public FloodgateIntegration(JustSyncApplication integration) {
        if (FabricLoader.getInstance().isModLoaded("floodgate-modded")) {
            this.floodgateApi = FloodgateApi.getInstance();
        }
    }

    public boolean isBedrock(UUID uuid) {
        return this.floodgateApi != null && this.floodgateApi.isFloodgateId(uuid);
    }

    public String getUsername(UUID uuid) {
        if (this.floodgateApi != null) {
            try {
                return this.floodgateApi.getGamertagFor(uuid.getLeastSignificantBits()).get();
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }
        return "unknown";
    }

}

