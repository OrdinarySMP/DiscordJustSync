package tronka.justsync.core.event.payloads;

import tronka.justsync.core.view.minecraft.Player;

public record ChatMessageEvent(Player sender, String content) {

}
