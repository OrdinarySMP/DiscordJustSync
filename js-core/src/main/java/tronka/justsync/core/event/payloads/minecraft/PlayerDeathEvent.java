package tronka.justsync.core.event.payloads.minecraft;

import tronka.justsync.core.view.minecraft.Player;

public record PlayerDeathEvent(Player player, String message) {}
