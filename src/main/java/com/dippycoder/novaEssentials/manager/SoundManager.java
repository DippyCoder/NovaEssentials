package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.logging.Level;

public class SoundManager {

    private final NovaEssentials plugin;
    private FileConfiguration soundsConfig;

    public SoundManager(NovaEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "sounds.yml");
        if (!file.exists()) plugin.saveResource("sounds.yml", false);
        soundsConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void playGuiOpen(Player player)       { play(player, "gui.open"); }
    public void playGuiClose(Player player)      { play(player, "gui.close"); }
    public void playGuiClick(Player player)      { play(player, "gui.click"); }
    public void playSettingsOn(Player player)    { play(player, "gui.settings-on"); }
    public void playSettingsOff(Player player)   { play(player, "gui.settings-off"); }
    public void playSettingsChange(Player player){ play(player, "gui.settings-change"); }

    private void play(Player player, String key) {
        if (!soundsConfig.getBoolean(key + ".enabled", true)) return;

        PlayerSettingsManager sm = plugin.getPlayerSettingsManager();
        if (sm != null && !sm.getSettings(player.getUniqueId()).soundsEnabled) return;

        String soundName = soundsConfig.getString(key + ".sound");
        if (soundName == null || soundName.isBlank()) return;

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = (float) soundsConfig.getDouble(key + ".volume", 0.5);
            float pitch  = (float) soundsConfig.getDouble(key + ".pitch",  1.0);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Invalid sound name in sounds.yml at key '" + key + "': " + soundName);
        }
    }
}
