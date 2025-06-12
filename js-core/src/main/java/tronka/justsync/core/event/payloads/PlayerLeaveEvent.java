package tronka.justsync.core.event.payloads;

import tronka.justsync.core.view.minecraft.Player;

public record PlayerLeaveEvent(Player player, String reason) {

}
