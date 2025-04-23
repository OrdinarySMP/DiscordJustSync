package tronka.justsync;

import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import tronka.justsync.chat.ChatBridge;
import tronka.justsync.compat.FloodgateIntegration;
import tronka.justsync.compat.LuckPermsIntegration;
import tronka.justsync.compat.VanishIntegration;
import tronka.justsync.config.Config;
import tronka.justsync.linking.LinkManager;

public class JustSyncApplication extends ListenerAdapter implements DedicatedServerModInitializer {

    public static final String ModId = "discord-justsync";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static JustSyncApplication instance;
    private final List<Consumer<Config>> configReloadHandlers = new ArrayList<>();
    private JDA jda;
    private Guild guild;
    private MinecraftServer server;
    private boolean ready = false;
    private ConsoleBridge consoleBridge;
    private ChatBridge chatBridge;
    private Config config = Config.loadConfig();
    private LinkManager linkManager;
    private LuckPermsIntegration luckPermsIntegration;
    private VanishIntegration vanishIntegration;
    private FloodgateIntegration floodgateIntegration;
    private TimeoutManager timeoutManager;
    private DiscordLogger discordLogger;

    public static JustSyncApplication getInstance() {
        return instance;
    }

    public static Path getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve("discord-js");
    }

    @Override
    public void onInitializeServer() {
        instance = this;
        if (this.config.botToken == null || this.config.botToken.length() < 20) {
            throw new RuntimeException("Please enter a valid bot token in the Discord-JS config file in " + getConfigFolder().toAbsolutePath());
        }
        ServerLifecycleEvents.SERVER_STARTING.register(s -> this.server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
        Thread jdaThread = new Thread(this::startJDA);
        new InGameCommand(this);
        jdaThread.start();
    }

    private void startJDA() {
        try {
            this.jda = JDABuilder.createLight(this.config.botToken, GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT,
                    GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL).addEventListeners(this)
                .build();
        } catch (InvalidTokenException e) {
            LOGGER.error("Please enter a valid bot token in the Discord-JS config file in {}",
                getConfigFolder().toAbsolutePath());
            System.exit(-1);
        }
    }

    private void onServerStopped(MinecraftServer server) {
        this.jda.shutdownNow();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        String reloadResult = this.tryReloadConfig();
        if (!reloadResult.isEmpty()) {
            throw new RuntimeException(reloadResult);
        }
        try {
            this.jda.addEventListener(new DiscordCommandHandler(this));
            this.jda.addEventListener(this.chatBridge = new ChatBridge(this));
            this.jda.addEventListener(this.consoleBridge = new ConsoleBridge(this));
            this.jda.addEventListener(this.linkManager = new LinkManager(this));
            this.jda.addEventListener(this.timeoutManager = new TimeoutManager(this));
            this.discordLogger = new DiscordLogger(this);
            this.luckPermsIntegration = new LuckPermsIntegration(this);
            this.vanishIntegration = new VanishIntegration(this);
            this.floodgateIntegration = new FloodgateIntegration(this);
            this.registerConfigReloadHandler(this::onConfigReloaded);
        } catch (Exception e) {
            LOGGER.error("failed to initialize Discord Just Sync! Please report this crash on github or our discord server!", e);
            System.exit(-1);
        }
    }

    private void onConfigReloaded(Config config) {
        // bring all members into cache
        this.guild.loadMembers().onSuccess(members -> {
            this.setReady();
            this.linkManager.unlinkPlayers(members);
        }).onError(t -> {
            LOGGER.error("Unable to load members", t);
            this.setReady();
        });
    }

    private void setReady() {
        this.ready = true;
    }

    public boolean isReady() {
        return this.ready;
    }

    public JDA getJda() {
        return this.jda;
    }

    public Config getConfig() {
        return this.config;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public ConsoleBridge getConsoleBridge() {
        return this.consoleBridge;
    }

    public ChatBridge getChatBridge() {
        return this.chatBridge;
    }

    public LinkManager getLinkManager() {
        return this.linkManager;
    }

    public LuckPermsIntegration getLuckPermsIntegration() {
        return this.luckPermsIntegration;
    }

    public VanishIntegration getVanishIntegration() {
        return this.vanishIntegration;
    }

    public FloodgateIntegration getFloodgateIntegration() { return this.floodgateIntegration;}

    public TimeoutManager getTimeoutManager() {
        return this.timeoutManager;
    }

    public DiscordLogger getDiscordLogger() {
        return this.discordLogger;
    }

    public String tryReloadConfig() {
        LOGGER.info("Reloading Config...");
        Config newConfig = Config.loadConfig();
        TextChannel serverChatChannel = Utils.getTextChannel(this.jda, newConfig.serverChatChannel, "serverChatChannel");
        if (serverChatChannel == null) {
            return "Fail to load config: Please enter a valid serverChatChannelId in the config file in " + getConfigFolder().toAbsolutePath();
        }

        this.guild = serverChatChannel.getGuild();
        this.config = newConfig;

        for (Consumer<Config> handler : this.configReloadHandlers) {
            handler.accept(this.config);
        }
        LOGGER.info("Config successfully reloaded!");
        return "";
    }

    public void registerConfigReloadHandler(Consumer<Config> handler) {
        this.configReloadHandlers.add(handler);
        handler.accept(this.config);
    }
}
