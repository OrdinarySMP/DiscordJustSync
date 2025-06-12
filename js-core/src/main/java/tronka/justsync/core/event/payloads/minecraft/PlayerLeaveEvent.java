package tronka.justsync.core.event.payloads.minecraft;

import tronka.justsync.core.view.minecraft.Player;

public record PlayerLeaveEvent(Player player, String reason) {

}
