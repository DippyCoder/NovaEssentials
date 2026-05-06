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

import java.util.List;
import java.util.Random;

public class PlayerListener implements Listener {

    private final NovaEssentials plugin;
    private final Random random = new Random();

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
        plugin.getPlayerSettingsManager().loadPlayer(player.getUniqueId());
        plugin.getPlayerSettingsManager().applyPermissions(player);

        // Apply vanish visibility
        plugin.getVanishManager().applyToNewPlayer(player);

        if (plugin.getVanishManager().isVanished(player)) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!plugin.getVanishManager().canSee(online) && !online.equals(player)) {
                    online.hidePlayer(plugin, player);
                }
            }
        }

        if (plugin.getFreezeManager().isFrozen(player)) {
            plugin.getMessageManager().send(player, "freeze.still-frozen");
        }

        if (plugin.getConfigManager().isSpawnOnJoin()) {
            Location spawn = plugin.getSpawnManager().getSpawn();
            if (spawn != null) player.teleport(spawn);
        }

        if (player.hasPermission("novaess.update-notify")) {
            plugin.getUpdateChecker().notifyPlayer(player);
        }

        // Custom join message
        if (plugin.getConfigManager().isJoinMessageEnabled()
                && !plugin.getVanishManager().isVanished(player)) {
            Component joinMsg = plugin.getMessageManager().get(player, "join.message",
                    "player", player.getName());
            event.joinMessage(joinMsg);
        } else if (plugin.getVanishManager().isVanished(player)) {
            event.joinMessage(null); // suppress join message for vanished players
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getHomeManager().unloadPlayer(player.getUniqueId());
        plugin.getBlockManager().unloadPlayer(player.getUniqueId());
        plugin.getMuteManager().unloadPlayer(player.getUniqueId());
        plugin.getKitManager().unloadPlayer(player.getUniqueId());
        plugin.getPlayerSettingsManager().saveSettings(player.getUniqueId());
        plugin.getPlayerSettingsManager().unloadPlayer(player.getUniqueId());
        plugin.getPlayerSettingsManager().removeAttachment(player);
        plugin.getTpaManager().cleanup(player);
        plugin.getTeleportDelayManager().remove(player);
        plugin.getMsgManager().remove(player.getUniqueId());
        plugin.getFlyManager().remove(player);
        plugin.getGodManager().remove(player);
        plugin.getAfkManager().remove(player);
        plugin.getBackManager().remove(player);
        plugin.getFreezeManager().remove(player);

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

        // Custom quit message
        if (plugin.getConfigManager().isLeaveMessageEnabled()
                && !plugin.getVanishManager().isVanished(player)) {
            Component quitMsg = plugin.getMessageManager().get(player, "leave.message",
                    "player", player.getName());
            event.quitMessage(quitMsg);
        } else if (plugin.getVanishManager().isVanished(player)) {
            event.quitMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        plugin.getBackManager().recordDeath(player);

        // Custom death message
        if (!plugin.getConfigManager().isDeathMessagesEnabled()) return;

        List<String> messages = plugin.getMessageManager()
                .getDeathMessages(player);
        if (messages.isEmpty()) return;

        String template = messages.get(random.nextInt(messages.size()));

        Player killer = player.getKiller();
        String killerName = killer != null ? killer.getName() : "Unknown";

        String causeString = "unknown";
        if (player.getLastDamageCause() != null) {
            causeString = player.getLastDamageCause().getCause().name()
                    .toLowerCase().replace('_', ' ');
        }

        Component deathMsg = plugin.getMessageManager().parse(player, template,
                "player", player.getName(),
                "killer", killerName,
                "cause", causeString);

        event.deathMessage(deathMsg);
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

        // Cancel pending delayed teleport on move
        if (plugin.getTeleportDelayManager().hasPendingTeleport(player)
                && plugin.getConfigManager().isCancelOnMove()) {
            Location preTp = plugin.getTeleportDelayManager().getPreTeleportLocation(player);
            if (preTp != null
                    && (preTp.getBlockX() != to.getBlockX()
                    ||  preTp.getBlockY() != to.getBlockY()
                    ||  preTp.getBlockZ() != to.getBlockZ())) {
                plugin.getTeleportDelayManager().cancelTeleport(player);
                plugin.getMessageManager().send(player, "tp.cancelled-move");
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
        if (newMode == GameMode.CREATIVE || newMode == GameMode.SPECTATOR) {
            return;
        }
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
