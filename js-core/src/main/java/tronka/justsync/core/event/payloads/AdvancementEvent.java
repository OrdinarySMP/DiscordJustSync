package tronka.justsync.core.event.payloads;

import tronka.justsync.core.view.minecraft.Player;

public record AdvancementEvent(Player player, String name, String description, boolean shouldAnnounce) {

}
