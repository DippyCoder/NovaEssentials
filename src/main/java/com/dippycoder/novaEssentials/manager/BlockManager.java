package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.entity.Player;

import java.util.*;

public class BlockManager {

    private final NovaEssentials plugin;
    private final Map<UUID, Set<UUID>> blockCache = new HashMap<>();

    public BlockManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    public void loadPlayer(UUID uuid) {
        blockCache.put(uuid, plugin.getDatabaseManager().loadBlockedPlayers(uuid));
    }

    public void unloadPlayer(UUID uuid) {
        blockCache.remove(uuid);
    }

    private Set<UUID> getBlocked(UUID uuid) {
        return blockCache.computeIfAbsent(uuid,
                u -> plugin.getDatabaseManager().loadBlockedPlayers(u));
    }

    public boolean isBlocked(Player blocker, Player target) {
        return getBlocked(blocker.getUniqueId()).contains(target.getUniqueId());
    }

    /** Returns true if target blocked blocker. */
    public boolean isBlockedBy(Player sender, Player target) {
        return getBlocked(target.getUniqueId()).contains(sender.getUniqueId());
    }

    public int getBlockedCount(UUID uuid) {
        return getBlocked(uuid).size();
    }

    public boolean toggleBlock(Player blocker, Player target) {
        Set<UUID> set = getBlocked(blocker.getUniqueId());
        UUID tid = target.getUniqueId();
        if (set.contains(tid)) {
            set.remove(tid);
            plugin.getDatabaseManager().removeBlock(blocker.getUniqueId(), tid);
            return false; // unblocked
        } else {
            set.add(tid);
            plugin.getDatabaseManager().addBlock(blocker.getUniqueId(), tid);
            return true; // blocked
        }
    }
}
