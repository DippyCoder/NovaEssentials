package com.dippycoder.novaEssentials.listener;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.manager.CaptchaManager;
import net.kyori.adventure.text.Component;
import org.bukkit.BanList;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final NovaEssentials plugin;

    public PlayerListener(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load per-player data from DB
        plugin.getHomeManager().loadPlayer(player.getUniqueId());
        plugin.getBlockManager().loadPlayer(player.getUniqueId());
        plugin.getMuteManager().loadPlayer(player.getUniqueId());
        plugin.getKitManager().loadPlayer(player.getUniqueId());

        // Apply vanish visibility
        plugin.getVanishManager().applyToNewPlayer(player);

        // Hide vanished player from others who cannot see
        if (plugin.getVanishManager().isVanished(player)) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!plugin.getVanishManager().canSee(online) && !online.equals(player)) {
                    online.hidePlayer(plugin, player);
                }
            }
        }

        // Re-apply freeze if this player was frozen before the server stopped
        if (plugin.getFreezeManager().isFrozen(player)) {
            plugin.getMessageManager().send(player, "freeze.still-frozen");
        }

        // Spawn on join
        if (plugin.getConfigManager().isSpawnOnJoin()) {
            Location spawn = plugin.getSpawnManager().getSpawn();
            if (spawn != null) player.teleport(spawn);
        }

        // Notify staff about available updates
        if (player.hasPermission("novaess.update-notify")) {
            plugin.getUpdateChecker().notifyPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getHomeManager().unloadPlayer(player.getUniqueId());
        plugin.getBlockManager().unloadPlayer(player.getUniqueId());
        plugin.getMuteManager().unloadPlayer(player.getUniqueId());
        plugin.getKitManager().unloadPlayer(player.getUniqueId());
        plugin.getTpaManager().cleanup(player);
        plugin.getMsgManager().remove(player.getUniqueId());
        plugin.getFlyManager().remove(player);
        plugin.getGodManager().remove(player);
        plugin.getAfkManager().remove(player);
        plugin.getBackManager().remove(player);
        plugin.getFreezeManager().remove(player);

        // Ban players who disconnect while a captcha is pending
        CaptchaManager.CaptchaData captchaData =
                plugin.getCaptchaManager().getActiveCaptcha(player);
        if (captchaData != null) {
            plugin.getCaptchaManager().clearCaptcha(player);

            @SuppressWarnings({"unchecked", "rawtypes"})
            BanList banList = (BanList) plugin.getServer().getBanList(BanList.Type.NAME);
            banList.addBan(player.getName(), "Disconnected to avoid captcha",
                    (java.util.Date) null, "NovaEssentials");

            plugin.getLogger().warning("[Captcha] " + player.getName()
                    + " disconnected to avoid captcha. Code was: " + captchaData.code());
            plugin.getDiscordWebhookManager().onCaptchaDodge(player.getName(), captchaData.code());
        } else {
            plugin.getCaptchaManager().clearCaptcha(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent event) {
        plugin.getBackManager().recordDeath(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getGodManager().isGod(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfigManager().isSpawnOnDeath()) return;
        Location spawn = plugin.getSpawnManager().getSpawn();
        if (spawn != null) event.setRespawnLocation(spawn);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;
        Location from = event.getFrom();

        boolean blockMoved = from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();

        if (!blockMoved) return;

        // Cancel pending TPA teleport on move
        if (plugin.getTpaManager().hasPendingTeleport(player)
                && plugin.getConfigManager().isCancelOnMove()) {
            Location preTp = plugin.getTpaManager().getPreTeleportLocation(player);
            if (preTp != null
                    && (preTp.getBlockX() != to.getBlockX()
                    ||  preTp.getBlockY() != to.getBlockY()
                    ||  preTp.getBlockZ() != to.getBlockZ())) {
                plugin.getTpaManager().cancelTeleport(player);
                plugin.getMessageManager().send(player, "tpa.cancelled-move");
            }
        }

        // Clear AFK on move
        if (plugin.getAfkManager().isAfk(player)
                && plugin.getConfigManager().isAfkCancelOnMove()) {
            plugin.getAfkManager().clearAfk(player);
            plugin.getServer().broadcast(
                    plugin.getMessageManager().get(player, "afk.broadcast-off",
                            "player", player.getName()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode newMode = event.getNewGameMode();
        // When entering creative/spectator, ensure flight is set
        if (newMode == GameMode.CREATIVE || newMode == GameMode.SPECTATOR) {
            // Will be handled by Minecraft itself
            return;
        }
        // When leaving creative/spectator, remove plugin fly if not active
        if (!plugin.getFlyManager().hasPluginFly(player)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()
                        && player.getGameMode() != GameMode.CREATIVE
                        && player.getGameMode() != GameMode.SPECTATOR) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }, 1L);
        }
    }
}
