package com.dippycoder.novaEssentials;

import com.dippycoder.novaEssentials.command.*;
import com.dippycoder.novaEssentials.config.ConfigManager;
import com.dippycoder.novaEssentials.database.DatabaseManager;
import com.dippycoder.novaEssentials.listener.ChatListener;
import com.dippycoder.novaEssentials.listener.FreezeListener;
import com.dippycoder.novaEssentials.listener.GuiListener;
import com.dippycoder.novaEssentials.listener.PlayerListener;
import com.dippycoder.novaEssentials.manager.*;
import com.dippycoder.novaEssentials.message.MessageManager;
import com.dippycoder.novaEssentials.papi.NovaEssentialsExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class NovaEssentials extends JavaPlugin {

    private static NovaEssentials instance;

    private ConfigManager          configManager;
    private DatabaseManager        databaseManager;
    private MessageManager         messageManager;
    private PlayerSettingsManager  playerSettingsManager;
    private VanishManager          vanishManager;
    private GodManager             godManager;
    private FlyManager             flyManager;
    private TpaManager             tpaManager;
    private TeleportDelayManager   teleportDelayManager;
    private MuteManager            muteManager;
    private HomeManager            homeManager;
    private WarpManager            warpManager;
    private BlockManager           blockManager;
    private ChatFilterManager      chatFilterManager;
    private MsgManager             msgManager;
    private SpawnManager           spawnManager;
    private KitManager             kitManager;
    private AfkManager             afkManager;
    private BackManager            backManager;
    private FreezeManager          freezeManager;
    private CaptchaManager         captchaManager;
    private DiscordWebhookManager  discordWebhookManager;
    private SoundManager           soundManager;
    private PlaytimeManager        playtimeManager;
    private UpdateChecker          updateChecker;
    private ChatCommand            chatCommandInstance;

    private static final int EXPECTED_CONFIG_VERSION = 1;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Config version guard — must run before ConfigManager reads values
        int cfgVersion = getConfig().getInt("config-version", -1);
        if (cfgVersion == -1) {
            getLogger().warning("config-version is missing from config.yml."
                    + " You may be using an outdated config. Please regenerate it.");
        } else if (cfgVersion != EXPECTED_CONFIG_VERSION) {
            getLogger().severe("Config version mismatch! Expected v" + EXPECTED_CONFIG_VERSION
                    + " but config.yml has v" + cfgVersion + "."
                    + " The plugin may behave incorrectly. Back up and regenerate config.yml.");
        }

        configManager         = new ConfigManager(this);
        messageManager        = new MessageManager(this);
        databaseManager       = new DatabaseManager(this);
        databaseManager.connect();

        playerSettingsManager = new PlayerSettingsManager(this);
        vanishManager         = new VanishManager(this);
        godManager            = new GodManager();
        flyManager            = new FlyManager();
        teleportDelayManager  = new TeleportDelayManager(this);
        tpaManager            = new TpaManager(this);
        muteManager           = new MuteManager(this);
        homeManager           = new HomeManager(this);
        warpManager           = new WarpManager(this);
        blockManager          = new BlockManager(this);
        chatFilterManager     = new ChatFilterManager(this);
        msgManager            = new MsgManager();
        spawnManager          = new SpawnManager(this);
        kitManager            = new KitManager(this);
        afkManager            = new AfkManager();
        backManager           = new BackManager();
        freezeManager         = new FreezeManager(this);
        captchaManager        = new CaptchaManager(this);
        discordWebhookManager = new DiscordWebhookManager(this);
        soundManager          = new SoundManager(this);
        playtimeManager       = new PlaytimeManager(this);
        updateChecker         = new UpdateChecker(this);
        updateChecker.checkAsync();

        registerCommands();

        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FreezeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new NovaEssentialsExpansion(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }

        getLogger().info("NovaEssentials v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) databaseManager.disconnect();
        getLogger().info("NovaEssentials disabled.");
    }

    private void registerCommands() {
        register("gm",        new GamemodeCommand(this));
        register("msg",       new MsgCommand(this));
        register("r",         new ReplyCommand(this));
        register("vanish",    new VanishCommand(this));
        register("fly",       new FlyCommand(this));
        register("speed",     new SpeedCommand(this));
        register("enchant",   new EnchantCommand(this));
        register("heal",      new HealCommand(this));
        register("feed",      new FeedCommand(this));
        register("god",       new GodCommand(this));
        register("invsee",    new InvseeCommand(this));
        register("ec",        new EnderchestCommand(this));
        register("workbench", new WorkbenchCommand(this));
        register("anvil",     new AnvilCommand(this));
        register("sudo",      new SudoCommand(this));
        register("tpa",       new TpaCommand(this));
        register("tpaccept",  new TpAcceptCommand(this));
        register("tpdeny",    new TpDenyCommand(this));
        register("tpahere",   new TpaHereCommand(this));
        register("tpall",     new TpAllCommand(this));
        register("back",      new BackCommand(this));
        register("block",     new BlockCommand(this));
        register("home",      new HomeCommand(this));
        register("sethome",   new SetHomeCommand(this));
        register("delhome",   new DelHomeCommand(this));
        register("spawn",     new SpawnCommand(this));
        register("setspawn",  new SetSpawnCommand(this));
        register("warp",      new WarpCommand(this));
        register("setwarp",   new SetWarpCommand(this));
        register("delwarp",   new DelWarpCommand(this));
        register("time",      new TimeCommand(this));
        register("kit",       new KitCommand(this));
        register("broadcast", new BroadcastCommand(this));
        chatCommandInstance = new ChatCommand(this);
        register("chat",      chatCommandInstance);
        register("mute",      new MuteCommand(this));
        register("unmute",    new UnmuteCommand(this));
        register("kick",      new KickCommand(this));
        register("ban",       new BanCommand(this));
        register("unban",     new UnbanCommand(this));
        register("afk",       new AfkCommand(this));
        register("rename",    new RenameCommand(this));
        register("lore",      new LoreCommand(this));
        register("effect",    new EffectCommand(this));
        register("freeze",    new FreezeCommand(this));
        register("unfreeze",  new UnfreezeCommand(this));
        register("tiny",      new TinyCommand(this));
        register("giant",     new GiantCommand(this));
        register("captcha",   new CaptchaCommand(this));
        register("recaptcha", new RecaptchaCommand(this));
        register("repair",    new RepairCommand(this));
        register("settings",  new SettingsCommand(this));
        register("novaess",   new NovaessCommand(this));
        register("summon",    new SummonCommand(this));
        register("strike",    new StrikeCommand(this));
        register("ipban",     new IpBanCommand(this));
        register("book",      new BookCommand(this));
        register("find",      new FindCommand(this));
        register("givehead",  new GiveHeadCommand(this));
        register("playtime",  new PlaytimeCommand(this));
        register("sun",       new SunCommand(this));
        register("rain",      new RainCommand(this));
        register("thunder",   new ThunderCommand(this));
        register("nuke",      new NukeCommand(this));
        register("fnuke",     new FNukeCommand(this));
        register("burn",      new BurnCommand(this));
    }

    private void register(String name, Object handler) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) return;
        if (handler instanceof CommandExecutor ce) cmd.setExecutor(ce);
        if (handler instanceof TabCompleter tc)    cmd.setTabCompleter(tc);
    }

    // ── Accessors ─────────────────────────────────────────────

    public static NovaEssentials getInstance()               { return instance; }
    public ConfigManager getConfigManager()                  { return configManager; }
    public DatabaseManager getDatabaseManager()              { return databaseManager; }
    public MessageManager getMessageManager()                { return messageManager; }
    public PlayerSettingsManager getPlayerSettingsManager()  { return playerSettingsManager; }
    public VanishManager getVanishManager()                  { return vanishManager; }
    public GodManager getGodManager()                        { return godManager; }
    public FlyManager getFlyManager()                        { return flyManager; }
    public TpaManager getTpaManager()                        { return tpaManager; }
    public TeleportDelayManager getTeleportDelayManager()    { return teleportDelayManager; }
    public MuteManager getMuteManager()                      { return muteManager; }
    public HomeManager getHomeManager()                      { return homeManager; }
    public WarpManager getWarpManager()                      { return warpManager; }
    public BlockManager getBlockManager()                    { return blockManager; }
    public ChatFilterManager getChatFilterManager()          { return chatFilterManager; }
    public MsgManager getMsgManager()                        { return msgManager; }
    public SpawnManager getSpawnManager()                    { return spawnManager; }
    public KitManager getKitManager()                        { return kitManager; }
    public AfkManager getAfkManager()                        { return afkManager; }
    public BackManager getBackManager()                      { return backManager; }
    public FreezeManager getFreezeManager()                  { return freezeManager; }
    public CaptchaManager getCaptchaManager()                { return captchaManager; }
    public DiscordWebhookManager getDiscordWebhookManager()  { return discordWebhookManager; }
    public SoundManager getSoundManager()                    { return soundManager; }
    public PlaytimeManager getPlaytimeManager()              { return playtimeManager; }
    public UpdateChecker getUpdateChecker()                  { return updateChecker; }
    public ChatCommand getChatCommand()                      { return chatCommandInstance; }
}
