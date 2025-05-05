package tronka.justsync.linking;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tronka.justsync.Utils;

public class PlayerLink {

    private UUID playerId;
    private long discordId;
    private List<PlayerData> alts;
    private transient LinkData dataObj;

    public PlayerLink() {

    }

    public PlayerLink(LinkRequest request, long discordId) {
        this(request.getPlayerId(), discordId);
    }

    public PlayerLink(UUID playerId, long discordId) {
        this.playerId = playerId;
        this.discordId = discordId;
        this.alts = new ArrayList<>();
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public String getPlayerName() {
        return Utils.getPlayerName(this.playerId);
    }

    public long getDiscordId() {
        return this.discordId;
    }

    public void addAlt(PlayerData data) {
        this.alts.add(data);
        this.dataObj.updatePlayerLink(this);
    }

    public void removeAlt(UUID uuid) {
        this.alts.removeIf(data -> data.getId().equals(uuid));
        this.dataObj.updatePlayerLink(this);
    }

    public void removeAlt(PlayerData data) {
        this.alts.remove(data);
        this.dataObj.updatePlayerLink(this);
    }

    public boolean hasAlt(UUID uuid) {
        return this.alts.stream().map(PlayerData::getId).anyMatch(uuid::equals);
    }

    public int altCount() {
        return this.alts.size();
    }

    public ImmutableList<PlayerData> getAlts() {
        return ImmutableList.copyOf(this.alts);
    }

    public List<UUID> getAllUuids() {
        List<UUID> uuids = new ArrayList<>();
        this.alts.forEach(alt -> uuids.add(alt.getId()));
        uuids.add(this.playerId);
        return uuids;
    }

    public void setDataObj(LinkData dataObj) {
        this.dataObj = dataObj;
    }

    @Override
    public String toString() {
        return "PlayerLink{" + "playerId=" + this.playerId + ", discordId=" + this.discordId + ", alts=" + this.alts + ", dataObj="
            + this.dataObj + '}';
    }
}
