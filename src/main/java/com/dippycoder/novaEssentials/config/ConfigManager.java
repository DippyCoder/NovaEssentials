package com.dippycoder.novaEssentials.config;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final NovaEssentials plugin;
    private FileConfiguration config;

    public ConfigManager(NovaEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    // ── Config version ────────────────────────────────────────

    public int getConfigVersion() {
        return config.getInt("config-version", -1);
    }

    // ── General ──────────────────────────────────────────────

    public String getLanguage() {
        return config.getString("general.language", "en");
    }

    public boolean isDebug() {
        return config.getBoolean("general.debug", false);
    }

    public boolean isCheckForUpdates() {
        return config.getBoolean("general.check-for-updates", true);
    }

    public void setCheckForUpdates(boolean value) {
        plugin.getConfig().set("general.check-for-updates", value);
        plugin.saveConfig();
    }

    // ── Commands ──────────────────────────────────────────────

    public boolean isCommandEnabled(String command) {
        List<String> disabled = config.getStringList("commands.disabled");
        return disabled.stream().noneMatch(s -> s.equalsIgnoreCase(command));
    }

    // ── Permissions ───────────────────────────────────────────

    /**
     * Returns the (possibly overridden) permission node for the given key.
     * Default: "novaess." + key
     */
    public String getPermission(String key) {
        String override = config.getString("permissions." + key);
        if (override != null && !override.isBlank()) return override;
        return "novaess." + key;
    }

    // ── Chat ─────────────────────────────────────────────────

    public boolean isChatFormatterEnabled() {
        return config.getBoolean("chat.formatter.enabled", true);
    }

    public String getChatFormat() {
        return config.getString("chat.formatter.format",
                "<dark_gray>[<gray><world></gray><dark_gray>] <green><bold><display_name></bold></green> <dark_gray>» <gray><message>");
    }

    public boolean isItemPlaceholderEnabled() {
        return config.getBoolean("chat.item-placeholder.enabled", true);
    }

    public String getItemPlaceholderFormat() {
        return config.getString("chat.item-placeholder.format", "<green><bold>[<name>]</bold></green>");
    }

    public boolean isSmallCapsEnabled() {
        return config.getBoolean("chat.small-caps.enabled", false);
    }

    public boolean isSmallCapsPermissionRequired() {
        return config.getBoolean("chat.small-caps.require-permission", false);
    }

    public boolean isChatFilterEnabled() {
        return config.getBoolean("chat.filter.enabled", false);
    }

    public List<String> getBlockedWords() {
        return config.getStringList("chat.filter.blocked-words");
    }

    public String getFilterReplacement() {
        return config.getString("chat.filter.replacement", "***");
    }

    public boolean isFilterNotifyStaff() {
        return config.getBoolean("chat.filter.notify-staff", true);
    }

    // ── Teleport ─────────────────────────────────────────────

    public int getTpaTimeout() {
        return config.getInt("teleport.tpa-timeout", 60);
    }

    public int getTeleportDelay() {
        return config.getInt("teleport.teleport-delay", 3);
    }

    public boolean isCancelOnMove() {
        return config.getBoolean("teleport.cancel-on-move", true);
    }

    // ── Homes ────────────────────────────────────────────────

    public int getDefaultHomeLimit() {
        return config.getInt("homes.default-limit", 1);
    }

    public String getDefaultHomeName() {
        return config.getString("homes.default-name", "home");
    }

    /**
     * Returns the highest home limit granted by any of the player's permissions.
     * -1 = unlimited.
     */
    public int getHomeLimit(org.bukkit.entity.Player player) {
        if (!config.isConfigurationSection("homes.limits")) return getDefaultHomeLimit();
        var section = config.getConfigurationSection("homes.limits");
        if (section == null) return getDefaultHomeLimit();
        int highest = getDefaultHomeLimit();
        // getKeys(true) traverses nested paths — required because Bukkit treats dots
        // in YAML keys as path separators, so "novaess.homes.2: 2" is stored nested.
        for (String key : section.getKeys(true)) {
            if (!section.isInt(key)) continue; // skip sub-sections, only leaf ints
            if (player.hasPermission(key)) {
                int limit = section.getInt(key);
                if (limit == -1) return -1;
                if (limit > highest) highest = limit;
            }
        }
        return highest;
    }

    // ── Spawn ────────────────────────────────────────────────

    public boolean isSpawnOnJoin() {
        return config.getBoolean("spawn.teleport-on-join", false);
    }

    public boolean isSpawnOnDeath() {
        return config.getBoolean("spawn.teleport-on-death", false);
    }

    // ── AFK ──────────────────────────────────────────────────

    public boolean isAfkCancelOnMove() {
        return config.getBoolean("afk.cancel-on-move", true);
    }

    // ── Kits ─────────────────────────────────────────────────

    public org.bukkit.configuration.ConfigurationSection getKitsSection() {
        return config.getConfigurationSection("kits");
    }

    public org.bukkit.configuration.ConfigurationSection getKitSection(String name) {
        var kits = getKitsSection();
        if (kits == null) return null;
        return kits.getConfigurationSection(name);
    }

    public FileConfiguration getRawConfig() {
        return config;
    }

    // ── GUI ──────────────────────────────────────────────────

    public boolean isKitGuiEnabled() {
        return config.getBoolean("gui.kits.enabled", false);
    }

    public boolean isWarpGuiEnabled() {
        return config.getBoolean("gui.warps.enabled", false);
    }

    public boolean isHomeGuiEnabled() {
        return config.getBoolean("gui.homes.enabled", false);
    }

    // ── Economy ───────────────────────────────────────────────

    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled", false);
    }

    public double getKitPrice(String kitName) {
        var section = getKitSection(kitName);
        return section == null ? 0.0 : section.getDouble("price", 0.0);
    }

    // ── Teleport cooldowns ────────────────────────────────────

    public int getTpCooldown(String commandKey) {
        return config.getInt("teleport.cooldowns." + commandKey, 0);
    }

    // ── Chat ─────────────────────────────────────────────────

    public int getChatClearLines() {
        return config.getInt("chat.clear-lines", 100);
    }

    // ── Join / Leave messages ─────────────────────────────────

    public boolean isJoinMessageEnabled() {
        return config.getBoolean("join-leave.join.enabled", true);
    }

    public int getJoinLanguageMode() {
        return config.getInt("join-leave.join.language-mode", 2);
    }

    public boolean isLeaveMessageEnabled() {
        return config.getBoolean("join-leave.leave.enabled", true);
    }

    public int getLeaveLanguageMode() {
        return config.getInt("join-leave.leave.language-mode", 2);
    }

    // ── Death messages ────────────────────────────────────────

    public boolean isDeathMessagesEnabled() {
        return config.getBoolean("death.enabled", true);
    }

    // ── Settings permissions ──────────────────────────────────

    public String getPaymentsPermission() {
        return config.getString("settings.payments-permission", "novaess.receive.payment");
    }

    public String getBalancePermission() {
        return config.getString("settings.balance-permission", "novaess.show.balance");
    }

    // ── Playtime ──────────────────────────────────────────────

    public boolean isPlaytimeEnabled() {
        return config.getBoolean("playtime.enabled", true);
    }

    public boolean isPlaytimeShowSeconds() {
        return config.getBoolean("playtime.format.show-seconds", true);
    }

    public boolean isPlaytimeShowSecondsAboveHour() {
        return config.getBoolean("playtime.format.show-seconds-above-hour", false);
    }

    public boolean isPlaytimeShowSecondsAboveDay() {
        return config.getBoolean("playtime.format.show-seconds-above-day", false);
    }

    // ── Database ──────────────────────────────────────────────

    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }

    public String getMysqlHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMysqlPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMysqlDatabase() {
        return config.getString("database.mysql.database", "novaessentials");
    }

    public String getMysqlUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMysqlPassword() {
        return config.getString("database.mysql.password", "");
    }

    public int getMysqlPoolSize() {
        return config.getInt("database.mysql.pool-size", 10);
    }

    // ── Redis ─────────────────────────────────────────────────

    public String getRedisHost() {
        return config.getString("database.redis.host", "localhost");
    }

    public int getRedisPort() {
        return config.getInt("database.redis.port", 6379);
    }

    public String getRedisPassword() {
        return config.getString("database.redis.password", "");
    }

    public int getRedisDatabase() {
        return config.getInt("database.redis.database", 0);
    }
}
