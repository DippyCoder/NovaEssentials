package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final NovaEssentials plugin;
    private final Set<UUID> vanished = new HashSet<>();

    public VanishManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    public void vanish(Player player) {
        vanished.add(player.getUniqueId());
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!canSee(online)) {
                online.hidePlayer(plugin, player);
            }
        }
    }

    public void unvanish(Player player) {
        vanished.remove(player.getUniqueId());
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
    }

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public boolean toggle(Player player) {
        if (isVanished(player)) {
            unvanish(player);
            return false;
        } else {
            vanish(player);
            return true;
        }
    }

    public boolean canSee(Player viewer) {
        return viewer.hasPermission(plugin.getConfigManager().getPermission("vanish.see"));
    }

    /** Apply vanish visibility to a newly joined player. */
    public void applyToNewPlayer(Player newPlayer) {
        for (UUID uuid : vanished) {
            Player v = plugin.getServer().getPlayer(uuid);
            if (v != null && !canSee(newPlayer)) {
                newPlayer.hidePlayer(plugin, v);
            }
        }
    }

    public Set<UUID> getVanished() {
        return vanished;
    }
}
