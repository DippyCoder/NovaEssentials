package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.database.DatabaseManager.MuteInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MuteManager {

    private final NovaEssentials plugin;
    private final Map<UUID, MuteInfo> muteCache = new HashMap<>();

    public MuteManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    public void loadPlayer(UUID uuid) {
        MuteInfo info = plugin.getDatabaseManager().loadMute(uuid);
        if (info == null) return;
        if (info.isExpired()) {
            plugin.getDatabaseManager().deleteMute(uuid);
        } else {
            muteCache.put(uuid, info);
        }
    }

    public void unloadPlayer(UUID uuid) {
        muteCache.remove(uuid);
    }

    public void mute(UUID uuid, String mutedBy, String reason, long until) {
        MuteInfo info = new MuteInfo(mutedBy, reason, until);
        muteCache.put(uuid, info);
        plugin.getDatabaseManager().saveMute(uuid, mutedBy, reason, until);
    }

    public void unmute(UUID uuid) {
        muteCache.remove(uuid);
        plugin.getDatabaseManager().deleteMute(uuid);
    }

    public boolean isMuted(UUID uuid) {
        MuteInfo info = getMuteInfo(uuid);
        return info != null;
    }

    public MuteInfo getMuteInfo(UUID uuid) {
        MuteInfo info = muteCache.get(uuid);
        if (info == null) {
            // Check DB for players who were muted while offline
            info = plugin.getDatabaseManager().loadMute(uuid);
            if (info != null) {
                if (info.isExpired()) {
                    plugin.getDatabaseManager().deleteMute(uuid);
                    return null;
                }
                muteCache.put(uuid, info);
            }
        } else if (info.isExpired()) {
            muteCache.remove(uuid);
            plugin.getDatabaseManager().deleteMute(uuid);
            return null;
        }
        return info;
    }
}
