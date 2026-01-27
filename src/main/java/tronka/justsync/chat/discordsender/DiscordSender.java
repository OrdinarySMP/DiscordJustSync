package tronka.justsync.chat.discordsender;

public interface DiscordSender {

    public void send();

    public void incrementCountAndEdit();

    public boolean hasChanged(DiscordSender sender);

    DiscordSenderState getState();
}
