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

    // ── Delayed teleport (delegates to TeleportDelayManager) ─────

    /**
     * Start the delayed teleport for an accepted TPA request.
     * sendChatMsg=false because TpAcceptCommand already notified the teleporting player.
     */
    public void startDelayedTeleport(Player teleporting, Location destination) {
        plugin.getTeleportDelayManager().startDelayedTeleport(teleporting, destination, "tpa", false);
    }

    // Forwarded convenience methods used by PlayerListener cancel-on-move
    public boolean cancelTeleport(Player player) {
        return plugin.getTeleportDelayManager().cancelTeleport(player);
    }

    public boolean hasPendingTeleport(Player player) {
        return plugin.getTeleportDelayManager().hasPendingTeleport(player);
    }

    public Location getPreTeleportLocation(Player player) {
        return plugin.getTeleportDelayManager().getPreTeleportLocation(player);
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
    }
}
