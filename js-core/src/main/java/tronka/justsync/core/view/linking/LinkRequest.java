package tronka.justsync.core.view.linking;

import java.util.UUID;

public class LinkRequest {
    private final UUID uuid;
    private final String name;
    private final long expiresAt;

    public LinkRequest(UUID uuid, String name, long expiresAt) {
        this.uuid = uuid;
        this.name = name;
        this.expiresAt = expiresAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean isExpired() {
        return expiresAt < System.currentTimeMillis();
    }
}
