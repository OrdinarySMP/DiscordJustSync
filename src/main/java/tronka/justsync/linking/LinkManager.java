package tronka.justsync.linking;

import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import tronka.justsync.JustSyncApplication;
import tronka.justsync.Utils;
import tronka.justsync.compat.FloodgateIntegration;
import tronka.justsync.config.Config;

public class LinkManager {

    private static final int PURGE_LIMIT = 30;
    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, LinkRequest> linkRequests = new HashMap<>();
    private final JustSyncApplication integration;
    private LinkData linkData;
    private List<Role> requiredRoles;
    private List<Role> joinRoles;
    private List<Role> allowMixedAccountTypesBypass;
    private List<Role> allowJoiningMixedAccountTypesBypass;


    public LinkManager(JustSyncApplication integration) {
        this.integration = integration;
        integration.registerConfigReloadHandler(this::onConfigLoaded);
    }

    private void onConfigLoaded(Config config) {
        this.linkData = JsonLinkData.from(
                JustSyncApplication.getConfigFolder().resolve(JustSyncApplication.MOD_ID + ".player-links.json")
                        .toFile());
        this.requiredRoles = Utils.parseRoleList(this.integration.getGuild(),
                this.integration.getConfig().linking.requiredRoles);
        this.joinRoles = Utils.parseRoleList(this.integration.getGuild(),
                this.integration.getConfig().linking.joinRoles);
        this.allowMixedAccountTypesBypass = Utils.parseRoleList(this.integration.getGuild(),
                this.integration.getConfig().integrations.floodgate.allowLinkingMixedAccountTypesBypass);
        this.allowJoiningMixedAccountTypesBypass = Utils.parseRoleList(this.integration.getGuild(),
                this.integration.getConfig().integrations.floodgate.allowJoiningMixedAccountTypesBypass);
    }

    public Stream<PlayerLink> getAllLinks() {
        return this.linkData.getPlayerLinks();
    }

    public Optional<Member> getDiscordOf(UUID playerId) {
        Optional<PlayerLink> link = this.linkData.getPlayerLink(playerId);
        if (link.isPresent()) {
            return this.getDiscordOf(link.get());
        }
        return Optional.empty();
    }

    public boolean isAllowedToJoin(Member member) {
        List<Role> roles = new ArrayList<>(member.getRoles());
        if (this.integration.getConfig().linking.requiredRolesCount == -1) {
            return roles.containsAll(this.requiredRoles);
        }
        roles.retainAll(this.requiredRoles);
        return roles.size() >= this.integration.getConfig().linking.requiredRolesCount;
    }

    public boolean isAllowedToJoin(long discordId) {
        return this.isAllowedToJoin(this.integration.getGuild().getMemberById(discordId));
    }

    public Optional<Member> getDiscordOf(PlayerLink link) {
        Member member = this.integration.getGuild().getMemberById(link.getDiscordId());
        if (member != null) {
            return Optional.of(member);
        }
        return Optional.empty();
    }

    public Optional<PlayerLink> getDataOf(long discordId) {
        return this.linkData.getPlayerLink(discordId);
    }

    public Optional<PlayerLink> getDataOf(UUID playerId) {
        return this.linkData.getPlayerLink(playerId);
    }

    public boolean canJoin(UUID playerId) {
        // always joinable if no linking
        if (!this.integration.getConfig().linking.enableLinking) {
            return true;
        }

        Optional<Member> member = this.getDiscordOf(playerId);
        if (member.isEmpty()) {
            return false;
        }
        // check requirements
        if (!this.isAllowedToJoin(member.get())) {
            return false;
        }
        return !this.integration.getConfig().linking.disallowTimeoutMembersToJoin || !member.get().isTimedOut();
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        Optional<PlayerLink> dataOptional = this.linkData.getPlayerLink(player.getUuid());
        if (dataOptional.isEmpty()) {
            return;
        }
        PlayerLink data = dataOptional.get();
        Optional<Member> memberOptional = this.getDiscordOf(data);
        if (memberOptional.isEmpty()) {
            return;
        }
        Member member = memberOptional.get();
        if (!PermissionUtil.canInteract(this.integration.getGuild().getSelfMember(), member)) {
            return;
        }

        if (data.getPlayerId().equals(player.getUuid()) && this.integration.getConfig().linking.renameOnJoin
                && PermissionUtil.checkPermission(this.integration.getGuild().getSelfMember(),
                        Permission.NICKNAME_MANAGE)) {
            member.modifyNickname(player.getName().getString()).queue();
        }
        if (PermissionUtil.checkPermission(this.integration.getGuild().getSelfMember(), Permission.MANAGE_ROLES)) {
            member.getGuild().modifyMemberRoles(member, this.joinRoles, Collections.emptyList()).queue();
        }
    }

    public String getJoinError(/*$ profile_class {*/net.minecraft.server.PlayerConfigEntry/*$}*/ profile) {
        //? if >= 1.21.9 {
        Optional<Member> member = this.getDiscordOf(profile.id());
        //?} else {
        /*Optional<Member> member = this.getDiscordOf(profile.getId());
        *///?}
        if (member.isEmpty()) {
            String code = this.generateLinkCode(profile);
            return this.integration.getConfig().kickMessages.kickLinkCode.formatted(code);
        }
        return this.getJoinError(member.get());
    }

    public String getJoinError(Member member) {
        if (member.isTimedOut()) {
            return this.integration.getConfig().kickMessages.kickTimedOut;
        }
        return this.integration.getConfig().kickMessages.kickMissingRoles;
    }

    public String confirmLink(long discordId, String code) {
        if (!this.isAllowedToJoin(discordId)) {
            return this.integration.getConfig().linkResults.linkNotAllowed;
        }
        Optional<LinkRequest> linkRequest = this.getPlayerLinkFromCode(code);
        if (linkRequest.isEmpty()) {
            return this.integration.getConfig().linkResults.failedUnknownCode;
        }
        Optional<PlayerLink> existing = this.linkData.getPlayerLink(discordId);
        if (existing.isPresent()) {
            PlayerLink link = existing.get();

            if (this.isMixedAlt(link, linkRequest.get())) {
                return this.integration.getConfig().integrations.floodgate.linkingMixedAccountTypesDenyMessage;
            }

            if (this.reachedMaxAlts(link)) {
                return this.integration.getConfig().linkResults.failedTooManyLinked;
            }
            link.addAlt(PlayerData.from(linkRequest.get()));
            this.integration.getLuckPermsIntegration().setAlt(linkRequest.get().getPlayerId());
            this.integration.getDiscordLogger().onLinkAlt(linkRequest.get().getPlayerId());
        } else {
            this.linkData.addPlayerLink(new PlayerLink(linkRequest.get(), discordId));
            this.integration.getDiscordLogger().onLinkMain(linkRequest.get().getPlayerId());
        }
        return this.integration.getConfig().linkResults.linkSuccess.replace("%name%", linkRequest.get().getName());
    }

    // also returns false if is mixed alt but bypasses or mixed accounts are allowed
    private boolean isMixedAlt(PlayerLink playerLink, LinkRequest linkRequest) {
        if (this.integration.getConfig().integrations.floodgate.allowLinkingMixedAccountTypes) {
            return false;
        }

        FloodgateIntegration floodgateIntegration = this.integration.getFloodgateIntegration();
        if (floodgateIntegration.isBedrock(playerLink.getPlayerId())
                == floodgateIntegration.isBedrock(linkRequest.getPlayerId())) {
            return false;
        }
        Optional<Member> member = this.getDiscordOf(playerLink);
        if (member.isEmpty()) {
            // should never occure
            return false;
        }
        List<Role> roles = new ArrayList<>(member.get().getRoles());

        roles.retainAll(this.allowMixedAccountTypesBypass);

        return roles.isEmpty();
    }

    private boolean reachedMaxAlts(PlayerLink link) {
        Optional<Member> member = this.getDiscordOf(link);
        if (member.isEmpty()) {
            return true;
        }
        Map<String, Integer> maxAltsForRoles = this.integration.getConfig().linking.maxAltsForRoles;
        for (Role role : member.get().getRoles()) {
            if (maxAltsForRoles.containsKey(role.getId())) {
                return link.altCount() >= maxAltsForRoles.get(role.getId());
            }
            if (maxAltsForRoles.containsKey(role.getName())) {
                return link.altCount() >= maxAltsForRoles.get(role.getName());
            }
        }

        return link.altCount() >= this.integration.getConfig().linking.maxAlts;
    }

    private Optional<LinkRequest> getPlayerLinkFromCode(String code) {
        if (!this.linkRequests.containsKey(code)) {
            return Optional.empty();
        }
        LinkRequest request = this.linkRequests.remove(code);
        if (request.isExpired()) {
            return Optional.empty();
        }
        return Optional.of(request);
    }

    public String generateLinkCode(/*$ profile_class {*/net.minecraft.server.PlayerConfigEntry/*$}*/ profile) {
        if (this.linkRequests.size() >= PURGE_LIMIT) {
            this.purgeCodes();
        }

        long expiryTime = System.currentTimeMillis()
                + this.integration.getConfig().linking.linkCodeExpireMinutes * 60 * 1000;
        String code;
        do {
            code = String.valueOf(RANDOM.nextInt(100000, 1000000)); // 6-digit code
        } while (this.linkRequests.containsKey(code));
        //? if >= 1.21.9 {
        this.linkRequests.put(code, new LinkRequest(profile.id(), profile.name(), expiryTime));
        //?} else {
        /*this.linkRequests.put(code, new LinkRequest(profile.getId(), profile.getName(), expiryTime));
        *///?}
        return code;
    }

    private void purgeCodes() {
        this.linkRequests.entrySet().removeIf(request -> request.getValue().isExpired());
    }

    public void unlinkPlayers(List<Member> members) {
        if (!this.integration.getConfig().linking.unlinkOnLeave) {
            return;
        }
        Set<Long> memberSet = members.stream().map(Member::getIdLong).collect(Collectors.toSet());
        List<PlayerLink> toRemove = this.linkData.getPlayerLinks()
                .filter(link -> !memberSet.contains(link.getDiscordId()))
                .toList();
        toRemove.forEach(this::unlinkPlayer);
        if (!toRemove.isEmpty()) {
            LOGGER.info("Purged {} linked players", toRemove.size());
        }
    }

    public boolean unlinkPlayer(long id) {
        Optional<PlayerLink> dataOptional = this.linkData.getPlayerLink(id);
        dataOptional.ifPresent(this::unlinkPlayer);
        return dataOptional.isPresent();
    }

    public boolean unlinkPlayer(UUID uuid) {
        Optional<PlayerLink> dataOptional = this.linkData.getPlayerLink(uuid);
        if (dataOptional.isEmpty()) {
            return false;
        }
        PlayerLink data = dataOptional.get();
        if (data.getPlayerId().equals(uuid)) {
            this.unlinkPlayer(data);
        } else {
            this.unlinkAlt(data, uuid);
        }
        return true;
    }

    public void unlinkPlayer(PlayerLink link) {
        this.tryKickPlayer(link.getPlayerId(), this.integration.getConfig().kickMessages.kickUnlinked);
        for (PlayerData alt : link.getAlts()) {
            this.tryKickPlayer(alt.getId(), this.integration.getConfig().kickMessages.kickUnlinked);
            this.integration.getLuckPermsIntegration().unsetAlt(alt.getId());
        }
        this.integration.getDiscordLogger().onUnlink(link);
        this.integration.getLuckPermsIntegration().removeAllSyncedRoles(link.getAllUuids());
        this.linkData.removePlayerLink(link);
    }


    public void onMemberRemoved(Member member) {
        this.kickAccounts(member, this.integration.getConfig().kickMessages.kickOnLeave);
        if (this.unlinkPlayer(member.getIdLong())) {
            LOGGER.info("Removed link of \"{}\" because they left the guild.", member.getEffectiveName());
        }
    }

    public void kickAccounts(Member member, String reason) {
        Optional<PlayerLink> playerLink = this.integration.getLinkManager().getDataOf(member.getIdLong());
        if (playerLink.isEmpty()) {
            return;
        }

        this.tryKickPlayer(playerLink.get().getPlayerId(), reason);
        for (PlayerData alt : playerLink.get().getAlts()) {
            this.tryKickPlayer(alt.getId(), reason);
        }
    }

    private void tryKickPlayer(UUID uuid, String reason) {
        MinecraftServer server = this.integration.getServer();
        if (server == null) {
            return;
        }
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
        if (player != null) {
            player.networkHandler.disconnect(Text.of(reason));
        }
    }

    public void unlinkAlt(PlayerLink link, UUID altUuid) {
        this.tryKickPlayer(altUuid, this.integration.getConfig().kickMessages.kickUnlinked);
        link.removeAlt(altUuid);
        this.integration.getDiscordLogger().onUnlinkAlt(altUuid);
        this.integration.getLuckPermsIntegration().removeAllSyncedRoles(altUuid);
        this.integration.getLuckPermsIntegration().unsetAlt(altUuid);
    }

    public List<Role> getAllowJoiningMixedAccountTypesBypass() {
        return allowJoiningMixedAccountTypesBypass;
    }
}

