package com.dippycoder.novaEssentials.manager;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodManager {

    private final Set<UUID> godPlayers = new HashSet<>();

    public void enable(Player player) {
        godPlayers.add(player.getUniqueId());
    }

    public void disable(Player player) {
        godPlayers.remove(player.getUniqueId());
    }

    public boolean isGod(Player player) {
        return godPlayers.contains(player.getUniqueId());
    }

    public boolean toggle(Player player) {
        if (isGod(player)) {
            disable(player);
            return false;
        } else {
            enable(player);
            return true;
        }
    }

    public void remove(Player player) {
        godPlayers.remove(player.getUniqueId());
    }
}
