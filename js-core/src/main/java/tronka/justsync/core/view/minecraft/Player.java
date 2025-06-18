package tronka.justsync.core.view.minecraft;

import java.util.UUID;

public abstract class Player {
    public abstract UUID getID();
    public abstract String getName();
    public abstract boolean equals(Player player);
}
