package com.dippycoder.novaEssentials.manager;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlyManager {

    /** Players whose flight was enabled by the plugin (not by gamemode). */
    private final Set<UUID> pluginFly = new HashSet<>();

    public void enable(Player player) {
        pluginFly.add(player.getUniqueId());
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void disable(Player player) {
        pluginFly.remove(player.getUniqueId());
        // Only remove flight if not in creative/spectator
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE
                && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    public boolean hasPluginFly(Player player) {
        return pluginFly.contains(player.getUniqueId());
    }

    public boolean toggle(Player player) {
        if (hasPluginFly(player)) {
            disable(player);
            return false;
        } else {
            enable(player);
            return true;
        }
    }

    public void remove(Player player) {
        pluginFly.remove(player.getUniqueId());
    }
}
