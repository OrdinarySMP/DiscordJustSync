package tronka.justsync.core.view.chat;

import tronka.justsync.core.view.minecraft.Player;

public interface MessageChannel {
    void send(String message, MessageType type, Player player);
    void editLast(String newMessage);
}
