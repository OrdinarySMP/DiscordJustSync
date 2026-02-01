package tronka.justsync.chat.discordsender;

import java.util.concurrent.CompletableFuture;

public interface SenderStrategy {

    public CompletableFuture<Long> send(String message);

    public CompletableFuture<Void> edit(String message, Long messageId);

    public boolean hasChanged(SenderStrategy strategy);
}
