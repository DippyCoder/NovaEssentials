package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Location;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class WarpManager {

    private final NovaEssentials plugin;
    private final Map<String, Location> warps = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public WarpManager(NovaEssentials plugin) {
        this.plugin = plugin;
        warps.putAll(plugin.getDatabaseManager().loadWarps());
    }

    public Location getWarp(String name) {
        return warps.get(name);
    }

    public boolean exists(String name) {
        return warps.containsKey(name);
    }

    public void setWarp(String name, Location location) {
        warps.put(name, location.clone());
        plugin.getDatabaseManager().saveWarp(name, location);
    }

    public boolean deleteWarp(String name) {
        if (!warps.containsKey(name)) return false;
        warps.remove(name);
        plugin.getDatabaseManager().deleteWarp(name);
        return true;
    }

    public Set<String> getWarpNames() {
        return warps.keySet();
    }
}
