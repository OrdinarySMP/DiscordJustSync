package tronka.justsync.core.event.payloads;

import tronka.justsync.core.view.minecraft.Player;

public class PlayerJoiningEvent {
    private final Player player;
    private boolean allowed;
    private String reason;

    public PlayerJoiningEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isAllowed() {
        return this.allowed;
    }

    public void deny(String reason) {
        this.allowed = false;
        this.reason = reason;
    }

    public String getReason() {
        return this.reason;
    }
}
