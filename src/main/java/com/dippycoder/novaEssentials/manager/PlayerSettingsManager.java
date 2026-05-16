package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSettingsManager {

    public static class PlayerSettings {
        public boolean allowTpa      = true;
        public boolean allowTpaHere  = true;
        public boolean tpauto        = false;
        public String  preferredLang = null;   // null = use Minecraft client locale
        public boolean soundsEnabled = true;
        public boolean hideChat      = false;
        public boolean allowMsg      = true;
        public boolean allowPayments = true;
        public boolean allowBalance  = true;
    }

    private final NovaEssentials plugin;
    private final Map<UUID, PlayerSettings>       cache       = new ConcurrentHashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();

    public PlayerSettingsManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    public PlayerSettings getSettings(UUID uuid) {
        return cache.getOrDefault(uuid, new PlayerSettings());
    }

    public void loadPlayer(UUID uuid) {
        PlayerSettings s = plugin.getDatabaseManager().loadPlayerSettings(uuid);
        cache.put(uuid, s != null ? s : new PlayerSettings());
    }

    public void unloadPlayer(UUID uuid) {
        cache.remove(uuid);
    }

    public void saveSettings(UUID uuid) {
        PlayerSettings s = cache.get(uuid);
        if (s != null) plugin.getDatabaseManager().savePlayerSettings(uuid, s);
    }

    /** Apply payment/balance permission attachments based on current settings. */
    public void applyPermissions(Player player) {
        removeAttachment(player);
        PlayerSettings s = getSettings(player.getUniqueId());

        PermissionAttachment att = player.addAttachment(plugin);

        String payPerm = plugin.getConfigManager().getPaymentsPermission();
        if (payPerm != null && !payPerm.isBlank()) {
            att.setPermission(payPerm, s.allowPayments);
        }

        String balPerm = plugin.getConfigManager().getBalancePermission();
        if (balPerm != null && !balPerm.isBlank()) {
            att.setPermission(balPerm, s.allowBalance);
        }

        attachments.put(player.getUniqueId(), att);
    }

    public void removeAttachment(Player player) {
        PermissionAttachment att = attachments.remove(player.getUniqueId());
        if (att != null) player.removeAttachment(att);
    }
}
