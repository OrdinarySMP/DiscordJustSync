package tronka.justsync.core.event.payloads.chat;

import tronka.justsync.core.view.chat.ChatMessageSender;

public record ChatMessageEvent(ChatMessageSender sender, String content) {

}
