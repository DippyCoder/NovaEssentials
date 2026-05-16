package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TeleportDelayManager {

    private final NovaEssentials plugin;
    /** [0]=teleportTask, [1]=countdownTask */
    private final Map<UUID, BukkitTask[]> pendingTasks          = new HashMap<>();
    private final Map<UUID, Location>     preTeleportLocations  = new HashMap<>();
    /** uuid → (commandKey → lastUseMillis) */
    private final Map<UUID, Map<String, Long>> cooldowns        = new HashMap<>();

    public TeleportDelayManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts a delayed teleport with an actionbar countdown.
     *
     * @param commandKey key used for cooldown lookup (e.g. "tpa", "home", "warp")
     * @param sendChatMsg whether to send a chat-message before counting down
     *                    (pass false for TPA since TpAcceptCommand already sends the message)
     * @return false if on cooldown (message sent to player), true if teleport was scheduled
     */
    public boolean startDelayedTeleport(Player player, Location destination,
                                        String commandKey, boolean sendChatMsg) {
        // Cooldown check
        int cooldownSec = plugin.getConfigManager().getTpCooldown(commandKey);
        if (cooldownSec > 0) {
            Map<String, Long> pCooldowns = cooldowns.get(player.getUniqueId());
            if (pCooldowns != null) {
                Long lastUse = pCooldowns.get(commandKey);
                if (lastUse != null) {
                    long elapsedSec = (System.currentTimeMillis() - lastUse) / 1000;
                    if (elapsedSec < cooldownSec) {
                        plugin.getMessageManager().send(player, "tp.cooldown",
                                "time", cooldownSec - elapsedSec);
                        return false;
                    }
                }
            }
        }

        int delay = plugin.getConfigManager().getTeleportDelay();
        if (delay <= 0) {
            player.teleport(destination);
            setCooldown(player.getUniqueId(), commandKey);
            return true;
        }

        preTeleportLocations.put(player.getUniqueId(), player.getLocation().clone());

        if (sendChatMsg) {
            plugin.getMessageManager().send(player, "tp.teleporting", "delay", delay);
        }

        int[] remaining = {delay};
        BukkitTask countdownTask = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, () -> {
                    if (!player.isOnline()) return;
                    if (remaining[0] > 0) {
                        Component bar = plugin.getMessageManager().get(player,
                                "tp.countdown", "seconds", remaining[0]);
                        player.sendActionBar(bar);
                        remaining[0]--;
                    }
                }, 0L, 20L);

        BukkitTask teleportTask = plugin.getServer().getScheduler()
                .runTaskLater(plugin, () -> {
                    countdownTask.cancel();
                    pendingTasks.remove(player.getUniqueId());
                    preTeleportLocations.remove(player.getUniqueId());
                    player.sendActionBar(Component.empty());
                    if (player.isOnline()) {
                        player.teleport(destination);
                        setCooldown(player.getUniqueId(), commandKey);
                    }
                }, delay * 20L);

        pendingTasks.put(player.getUniqueId(), new BukkitTask[]{teleportTask, countdownTask});
        return true;
    }

    /** Shorthand with sendChatMsg=true. */
    public boolean startDelayedTeleport(Player player, Location destination, String commandKey) {
        return startDelayedTeleport(player, destination, commandKey, true);
    }

    public boolean cancelTeleport(Player player) {
        BukkitTask[] tasks = pendingTasks.remove(player.getUniqueId());
        preTeleportLocations.remove(player.getUniqueId());
        if (tasks != null) {
            tasks[0].cancel();
            tasks[1].cancel();
            player.sendActionBar(Component.empty());
            return true;
        }
        return false;
    }

    public boolean hasPendingTeleport(Player player) {
        return pendingTasks.containsKey(player.getUniqueId());
    }

    public Location getPreTeleportLocation(Player player) {
        return preTeleportLocations.get(player.getUniqueId());
    }

    public boolean isOnCooldown(Player player, String commandKey) {
        return getRemainingCooldownSec(player, commandKey) > 0;
    }

    public long getRemainingCooldownSec(Player player, String commandKey) {
        int cooldownSec = plugin.getConfigManager().getTpCooldown(commandKey);
        if (cooldownSec <= 0) return 0;
        Map<String, Long> pCooldowns = cooldowns.get(player.getUniqueId());
        if (pCooldowns == null) return 0;
        Long lastUse = pCooldowns.get(commandKey);
        if (lastUse == null) return 0;
        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        return Math.max(0, cooldownSec - elapsed);
    }

    private void setCooldown(UUID uuid, String commandKey) {
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(commandKey, System.currentTimeMillis());
    }

    public void remove(Player player) {
        cancelTeleport(player);
        cooldowns.remove(player.getUniqueId());
    }
}
