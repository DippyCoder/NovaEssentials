package com.dippycoder.novaEssentials.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkManager {

    private record AfkInfo(String message) {}

    private final Map<UUID, AfkInfo> afkPlayers = new HashMap<>();

    public boolean isAfk(Player player) {
        return afkPlayers.containsKey(player.getUniqueId());
    }

    public String getAfkMessage(Player player) {
        AfkInfo info = afkPlayers.get(player.getUniqueId());
        return info != null ? info.message() : null;
    }

    /** Toggles AFK. Returns true if now AFK, false if no longer AFK. */
    public boolean toggle(Player player, String message) {
        UUID uuid = player.getUniqueId();
        if (afkPlayers.containsKey(uuid)) {
            afkPlayers.remove(uuid);
            return false;
        } else {
            afkPlayers.put(uuid, new AfkInfo(message));
            return true;
        }
    }

    public void setAfk(Player player, String message) {
        afkPlayers.put(player.getUniqueId(), new AfkInfo(message));
    }

    public void clearAfk(Player player) {
        afkPlayers.remove(player.getUniqueId());
    }

    public void remove(Player player) {
        afkPlayers.remove(player.getUniqueId());
    }
}
