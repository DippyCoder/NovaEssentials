package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class HomeManager {

    private final NovaEssentials plugin;
    private final Map<UUID, Map<String, Location>> homeCache = new HashMap<>();

    public HomeManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    public void loadPlayer(UUID uuid) {
        homeCache.put(uuid, plugin.getDatabaseManager().loadHomes(uuid));
    }

    public void unloadPlayer(UUID uuid) {
        homeCache.remove(uuid);
    }

    public Map<String, Location> getHomes(Player player) {
        return homeCache.computeIfAbsent(player.getUniqueId(),
                uuid -> plugin.getDatabaseManager().loadHomes(uuid));
    }

    public Location getHome(Player player, String name) {
        return getHomes(player).get(name);
    }

    public boolean hasHome(Player player, String name) {
        return getHomes(player).containsKey(name);
    }

    public void setHome(Player player, String name, Location location) {
        getHomes(player).put(name, location.clone());
        plugin.getDatabaseManager().saveHome(player.getUniqueId(), name, location);
    }

    public boolean deleteHome(Player player, String name) {
        Map<String, Location> homes = getHomes(player);
        if (!homes.containsKey(name)) return false;
        homes.remove(name);
        plugin.getDatabaseManager().deleteHome(player.getUniqueId(), name);
        return true;
    }

    public int getHomeCount(Player player) {
        return getHomes(player).size();
    }

    public Set<String> getHomeNames(Player player) {
        return getHomes(player).keySet();
    }
}
