package tronka.justsync.chat.discordsender;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.concurrent.CompletableFuture;

public class EmbedSender implements SenderStrategy {

    private int color;
    private String avatarUrl;
    private String user;
    private TextChannel channel;

    public EmbedSender(int color, String avatarUrl, String user) {
        this.color = color;
        this.avatarUrl = avatarUrl;
        this.user = user;
    }

    @Override
    public CompletableFuture<Long> send(String message) {
        MessageEmbed embed =
                new EmbedBuilder()
                        .setAuthor(null, null, this.avatarUrl)
                        .setColor(this.color)
                        .setTitle(user)
                        .setDescription(message)
                        .build();

        return this.channel.sendMessageEmbeds(embed).submit().thenApply(Message::getIdLong);
    }

    @Override
    public CompletableFuture<Void> edit(String message, Long messageId) {
        MessageEmbed embed =
                new EmbedBuilder()
                        .setAuthor(null, null, this.avatarUrl)
                        .setColor(this.color)
                        .setTitle(user)
                        .setDescription(message)
                        .build();
        return this.channel.editMessageEmbedsById(messageId, embed).submit().thenAccept(m -> {});
    }

    @Override
    public boolean hasChanged(SenderStrategy strategy) {
        return this.equals(strategy);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + color;
        result = prime * result + ((avatarUrl == null) ? 0 : avatarUrl.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EmbedSender other = (EmbedSender) obj;
        if (color != other.color) {
            return false;
        }
        if (avatarUrl == null) {
            if (other.avatarUrl != null) {
                return false;
            }
        } else if (!avatarUrl.equals(other.avatarUrl)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        return true;
    }
}
