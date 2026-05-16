package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaytimeManager {

    private final NovaEssentials plugin;
    private final Map<UUID, Long> sessionStart = new HashMap<>();

    public PlaytimeManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    public void onJoin(UUID uuid) {
        sessionStart.put(uuid, System.currentTimeMillis());
    }

    public void onQuit(UUID uuid) {
        Long start = sessionStart.remove(uuid);
        if (start == null) return;
        long sessionMs = System.currentTimeMillis() - start;
        long stored = plugin.getDatabaseManager().loadPlaytime(uuid);
        plugin.getDatabaseManager().savePlaytime(uuid, stored + sessionMs);
    }

    public long getPlaytime(UUID uuid) {
        long stored = plugin.getDatabaseManager().loadPlaytime(uuid);
        Long start = sessionStart.get(uuid);
        long sessionMs = start != null ? System.currentTimeMillis() - start : 0;
        return stored + sessionMs;
    }

    public String format(long ms) {
        long secs  = ms / 1000;
        long mins  = secs / 60;
        long hours = mins / 60;
        long days  = hours / 24;

        if (days > 0)  return days + "d " + (hours % 24) + "h " + (mins % 60) + "m " + (secs % 60) + "s";
        if (hours > 0) return hours + "h " + (mins % 60) + "m " + (secs % 60) + "s";
        if (mins > 0)  return mins + "m " + (secs % 60) + "s";
        return secs + "s";
    }
}
