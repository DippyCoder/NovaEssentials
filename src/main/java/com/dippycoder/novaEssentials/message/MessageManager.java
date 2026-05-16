package com.dippycoder.novaEssentials.message;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.util.SmallCapsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages all plugin messages with per-player locale support.
 *
 * Language resolution order for a Player:
 *   1. language + "_" + country  (e.g., "en_us", "de_de")
 *   2. language only             (e.g., "en", "de")
 *   3. configured default language in config.yml
 *
 * Message files live in plugins/NovaEssentials/messages/<lang>.yml.
 * The plugin ships messages/en.yml as the default.
 */
public class MessageManager {

    private static final String[] PREFIX_KEYS = {
        "general", "msg", "staff", "home", "warp", "tp",
        "broadcast", "chat", "kit", "error"
    };

    private final NovaEssentials plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    /** Locale-key → loaded YAML. The key is lowercase "lang" or "lang_country". */
    private final Map<String, FileConfiguration> localeCache = new ConcurrentHashMap<>();
    private FileConfiguration defaultMessages;
    private boolean papiLoaded;

    public MessageManager(NovaEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        localeCache.clear();
        papiLoaded = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

        // Load configured default language
        String defaultLang = plugin.getConfigManager().getLanguage();
        defaultMessages = loadLocale(defaultLang);
        localeCache.put(defaultLang.toLowerCase(), defaultMessages);

        // Pre-load any other language files already present in the messages/ folder
        File messagesDir = new File(plugin.getDataFolder(), "messages");
        if (messagesDir.isDirectory()) {
            File[] files = messagesDir.listFiles((d, n) -> n.endsWith(".yml"));
            if (files != null) {
                for (File f : files) {
                    String key = f.getName().replace(".yml", "").toLowerCase();
                    localeCache.computeIfAbsent(key, k -> {
                        FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
                        fc.setDefaults(defaultMessages);
                        return fc;
                    });
                }
            }
        }
    }

    // ── Public API ────────────────────────────────────────────

    /**
     * Send a message to a CommandSender.
     *
     * @param sender  recipient — if a Player, their Minecraft locale is used
     * @param key     dot-separated YAML key, e.g. "gamemode.changed-self"
     * @param kvPairs alternating key/value tag replacements: "player", "Steve", "mode", "Creative"
     */
    public void send(CommandSender sender, String key, Object... kvPairs) {
        Component component = get(sender, key, kvPairs);
        if (component != null) sender.sendMessage(component);
    }

    /** Build a Component for the given key without sending it. */
    public Component get(CommandSender sender, String key, Object... kvPairs) {
        FileConfiguration messages = getMessagesFor(sender);
        String raw = messages.getString(key);
        if (raw == null) {
            plugin.getLogger().warning("Missing message key: " + key);
            return Component.text("(missing: " + key + ")");
        }

        if (papiLoaded && sender instanceof Player player) {
            raw = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, raw);
        }

        List<TagResolver> resolvers = buildResolvers(messages, kvPairs);
        Component component = mm.deserialize(raw, TagResolver.resolver(resolvers));

        if (shouldApplySmallCaps(sender)) {
            component = SmallCapsUtil.applyToComponent(component);
        }
        return component;
    }

    /** Parse an arbitrary raw MiniMessage string with prefix + kv resolvers. */
    public Component parse(CommandSender sender, String raw, Object... kvPairs) {
        FileConfiguration messages = getMessagesFor(sender);
        if (papiLoaded && sender instanceof Player player) {
            raw = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, raw);
        }
        return mm.deserialize(raw, TagResolver.resolver(buildResolvers(messages, kvPairs)));
    }

    /** Raw string for a key from the default locale (useful for formats). */
    public String getRaw(String key) {
        return defaultMessages.getString(key, "");
    }

    // ── Locale resolution ─────────────────────────────────────

    private FileConfiguration getMessagesFor(CommandSender sender) {
        if (!(sender instanceof Player player)) return defaultMessages;

        // Check player's preferred language setting first
        var sm = plugin.getPlayerSettingsManager();
        if (sm != null) {
            String prefLang = sm.getSettings(player.getUniqueId()).preferredLang;
            if (prefLang != null && !prefLang.isBlank()) {
                FileConfiguration fc = localeCache.computeIfAbsent(
                        prefLang.toLowerCase(), k -> tryLoadLocale(k));
                if (fc != null) return fc;
            }
        }

        // Fall back to Minecraft client locale
        Locale locale = player.locale();
        String lang    = locale.getLanguage().toLowerCase();
        String country = locale.getCountry().toLowerCase();

        if (!country.isEmpty()) {
            FileConfiguration fc = localeCache.computeIfAbsent(
                    lang + "_" + country, k -> tryLoadLocale(lang + "_" + country));
            if (fc != null) return fc;
        }

        FileConfiguration fc = localeCache.computeIfAbsent(lang, k -> tryLoadLocale(lang));
        return fc != null ? fc : defaultMessages;
    }

    /**
     * Try loading a locale — returns null if no file exists (so computeIfAbsent
     * stores null and we don't re-try on every message).
     * Loaded configs have defaultMessages set as their defaults so missing keys
     * fall back to English automatically.
     */
    private FileConfiguration tryLoadLocale(String key) {
        FileConfiguration fc = null;
        File f = new File(plugin.getDataFolder(), "messages/" + key + ".yml");
        if (f.exists()) {
            fc = YamlConfiguration.loadConfiguration(f);
        } else {
            InputStream is = plugin.getResource("messages/" + key + ".yml");
            if (is != null) {
                plugin.saveResource("messages/" + key + ".yml", false);
                fc = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }
        if (fc != null && defaultMessages != null) fc.setDefaults(defaultMessages);
        return fc;
    }

    private FileConfiguration loadLocale(String lang) {
        File file = new File(plugin.getDataFolder(), "messages/" + lang + ".yml");
        if (!file.exists()) {
            plugin.saveResource("messages/" + lang + ".yml", false);
        }
        if (file.exists()) return YamlConfiguration.loadConfiguration(file);

        InputStream is = plugin.getResource("messages/en.yml");
        if (is == null) {
            plugin.getLogger().warning("No messages file found; using empty config.");
            return new YamlConfiguration();
        }
        plugin.getLogger().info("Locale '" + lang + "' not found, falling back to en.");
        return YamlConfiguration.loadConfiguration(
                new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    /** Returns the list of death message templates for the player's locale. */
    public List<String> getDeathMessages(Player player) {
        FileConfiguration messages = getMessagesFor(player);
        return messages.getStringList("death.messages");
    }

    /** Returns a sorted list of available locale keys (for the settings language picker). */
    public List<String> getAvailableLocales() {
        return localeCache.keySet().stream()
                .filter(k -> localeCache.get(k) != null)
                .sorted()
                .toList();
    }

    // ── Tag resolver builder ──────────────────────────────────

    private List<TagResolver> buildResolvers(FileConfiguration messages, Object[] kvPairs) {
        List<TagResolver> resolvers = new ArrayList<>();

        for (String prefix : PREFIX_KEYS) {
            String val = messages.getString("prefix." + prefix, "");
            resolvers.add(Placeholder.component("prefix_" + prefix, mm.deserialize(val)));
        }

        for (int i = 0; i + 1 < kvPairs.length; i += 2) {
            String tagKey = String.valueOf(kvPairs[i]);
            Object tagVal = kvPairs[i + 1];
            if (tagVal instanceof Component c) {
                resolvers.add(Placeholder.component(tagKey, c));
            } else {
                resolvers.add(Placeholder.unparsed(tagKey, String.valueOf(tagVal)));
            }
        }
        return resolvers;
    }

    private boolean shouldApplySmallCaps(CommandSender sender) {
        if (!plugin.getConfigManager().isSmallCapsEnabled()) return false;
        if (!plugin.getConfigManager().isSmallCapsPermissionRequired()) return true;
        return sender.hasPermission(plugin.getConfigManager().getPermission("chat.smallcaps"));
    }

    /** Send a keyed message to every online player individually (each in their own locale). */
    public void broadcastAll(String key, Object... kvPairs) {
        for (Player p : plugin.getServer().getOnlinePlayers()) send(p, key, kvPairs);
    }

    /**
     * Send a keyed message to every online player who has {@code permission},
     * each in their own locale, and also to the console.
     */
    public void broadcastToPermission(String permission, String key, Object... kvPairs) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.hasPermission(permission)) send(p, key, kvPairs);
        }
        send(plugin.getServer().getConsoleSender(), key, kvPairs);
    }

    public boolean isPapiLoaded() { return papiLoaded; }
}
