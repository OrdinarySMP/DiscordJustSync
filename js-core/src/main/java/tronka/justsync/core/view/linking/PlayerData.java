package tronka.justsync.core.view.linking;

import java.util.UUID;
import tronka.justsync.core.data.DataProviders;

public class PlayerData {
    private UUID id;

    public PlayerData(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return DataProviders.USERNAME.getValue(this.id).orElse("unknown");
    }
}
