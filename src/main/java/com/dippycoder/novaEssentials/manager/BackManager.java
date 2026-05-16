package com.dippycoder.novaEssentials.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackManager {

    private final Map<UUID, Location> lastDeathLocations = new HashMap<>();

    public void recordDeath(Player player) {
        lastDeathLocations.put(player.getUniqueId(), player.getLocation().clone());
    }

    public Location getLastDeathLocation(Player player) {
        return lastDeathLocations.get(player.getUniqueId());
    }

    public boolean hasLocation(Player player) {
        return lastDeathLocations.containsKey(player.getUniqueId());
    }

    public void remove(Player player) {
        lastDeathLocations.remove(player.getUniqueId());
    }
}
