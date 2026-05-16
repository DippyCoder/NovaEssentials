package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TpaManager {

    /**
     * @param sender      UUID of the player who sent the request
     * @param receiver    UUID of the player who must accept/deny
     * @param here        true for /tpahere — receiver teleports TO sender's location
     * @param expiryTask  task that fires when the request expires
     */
    public record TpaRequest(UUID sender, UUID receiver, boolean here, BukkitTask expiryTask) {}

    private final NovaEssentials plugin;
    /** Keyed by receiver UUID → pending request */
    private final Map<UUID, TpaRequest> requests = new HashMap<>();
    /** Keyed by player UUID → pending delayed-teleport task */
    private final Map<UUID, BukkitTask> pendingTeleports = new HashMap<>();
    /** Location recorded before a delayed teleport (for cancel-on-move check) */
    private final Map<UUID, Location> preTeleportLocations = new HashMap<>();

    public TpaManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    // ── Request creation ──────────────────────────────────────

    /** /tpa — sender teleports TO receiver. */
    public void sendRequest(Player sender, Player receiver) {
        createRequest(sender, receiver, false);
    }

    /** /tpahere — receiver teleports TO sender. */
    public void sendHereRequest(Player sender, Player receiver) {
        createRequest(sender, receiver, true);
    }

    private void createRequest(Player sender, Player receiver, boolean here) {
        int timeout = plugin.getConfigManager().getTpaTimeout();
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            TpaRequest req = requests.remove(receiver.getUniqueId());
            if (req == null) return;
            Player s = plugin.getServer().getPlayer(req.sender());
            Player r = plugin.getServer().getPlayer(req.receiver());
            if (s != null) plugin.getMessageManager().send(s, "tpa.expired-sender",
                    "player", r != null ? r.getName() : "?");
            if (r != null) plugin.getMessageManager().send(r, "tpa.expired-receiver",
                    "player", s != null ? s.getName() : "?");
        }, timeout * 20L);

        requests.put(receiver.getUniqueId(),
                new TpaRequest(sender.getUniqueId(), receiver.getUniqueId(), here, task));
    }

    // ── Request queries ───────────────────────────────────────

    public boolean hasPendingRequest(Player receiver) {
        return requests.containsKey(receiver.getUniqueId());
    }

    public TpaRequest getRequest(Player receiver) {
        return requests.get(receiver.getUniqueId());
    }

    public boolean hasSentRequest(Player sender, Player receiver) {
        TpaRequest req = requests.get(receiver.getUniqueId());
        return req != null && req.sender().equals(sender.getUniqueId());
    }

    public TpaRequest removeRequest(Player receiver) {
        TpaRequest req = requests.remove(receiver.getUniqueId());
        if (req != null) req.expiryTask().cancel();
        return req;
    }

    // ── Delayed teleport ──────────────────────────────────────

    public void startDelayedTeleport(Player teleporting, Location destination) {
        int delay = plugin.getConfigManager().getTeleportDelay();
        if (delay <= 0) {
            teleporting.teleport(destination);
            return;
        }
        preTeleportLocations.put(teleporting.getUniqueId(), teleporting.getLocation().clone());
        plugin.getMessageManager().send(teleporting, "tpa.teleporting", "delay", delay);

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(teleporting.getUniqueId());
            preTeleportLocations.remove(teleporting.getUniqueId());
            teleporting.teleport(destination);
        }, delay * 20L);

        pendingTeleports.put(teleporting.getUniqueId(), task);
    }

    public boolean cancelTeleport(Player player) {
        BukkitTask task = pendingTeleports.remove(player.getUniqueId());
        preTeleportLocations.remove(player.getUniqueId());
        if (task != null) { task.cancel(); return true; }
        return false;
    }

    public boolean hasPendingTeleport(Player player) {
        return pendingTeleports.containsKey(player.getUniqueId());
    }

    public Location getPreTeleportLocation(Player player) {
        return preTeleportLocations.get(player.getUniqueId());
    }

    // ── Cleanup ───────────────────────────────────────────────

    public void cleanup(Player player) {
        requests.entrySet().removeIf(e -> {
            TpaRequest r = e.getValue();
            if (r.sender().equals(player.getUniqueId())
                    || r.receiver().equals(player.getUniqueId())) {
                r.expiryTask().cancel();
                return true;
            }
            return false;
        });
        cancelTeleport(player);
    }
}
