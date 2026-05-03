package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeManager {

    private final NovaEssentials plugin;
    private final Set<UUID> frozen = new HashSet<>();

    public FreezeManager(NovaEssentials plugin) {
        this.plugin = plugin;
        frozen.addAll(plugin.getDatabaseManager().loadAllFrozen());
    }

    public void freeze(Player player) {
        frozen.add(player.getUniqueId());
        plugin.getDatabaseManager().addFrozen(player.getUniqueId());
    }

    public void unfreeze(Player player) {
        frozen.remove(player.getUniqueId());
        plugin.getDatabaseManager().removeFrozen(player.getUniqueId());
    }

    public boolean isFrozen(Player player) {
        return frozen.contains(player.getUniqueId());
    }

    /** Removes freeze state from memory only (no DB write) — used on player quit. */
    public void remove(Player player) {
        frozen.remove(player.getUniqueId());
    }
}
