package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Location;

public class SpawnManager {

    private final NovaEssentials plugin;
    private Location spawn;

    public SpawnManager(NovaEssentials plugin) {
        this.plugin = plugin;
        spawn = plugin.getDatabaseManager().loadSpawn();
    }

    public boolean hasSpawn() {
        return spawn != null;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location location) {
        this.spawn = location.clone();
        plugin.getDatabaseManager().saveSpawn(location);
    }
}
