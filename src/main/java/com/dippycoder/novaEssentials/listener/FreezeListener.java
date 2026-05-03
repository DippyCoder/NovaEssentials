package com.dippycoder.novaEssentials.listener;

import com.dippycoder.novaEssentials.NovaEssentials;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class FreezeListener implements Listener {

    private final NovaEssentials plugin;

    public FreezeListener(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getFreezeManager().isFrozen(player)) return;

        // Cancel movement if the player moved to a different block position
        var from = event.getFrom();
        var to = event.getTo();
        if (to == null) return;

        boolean movedBlock = from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();

        if (movedBlock) {
            event.setTo(from.clone());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        if (plugin.getFreezeManager().isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (plugin.getFreezeManager().isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (plugin.getFreezeManager().isFrozen(damager)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getFreezeManager().isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getFreezeManager().isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (plugin.getFreezeManager().isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /** Blocks all commands for frozen players except /recaptcha (only for captcha holders). */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getFreezeManager().isFrozen(player)) return;

        String cmd = event.getMessage().toLowerCase();
        // Allow /recaptcha only when the player has an active captcha
        if (plugin.getCaptchaManager().hasActiveCaptcha(player)
                && cmd.startsWith("/recaptcha")) {
            return;
        }

        event.setCancelled(true);
        plugin.getMessageManager().send(player, "freeze.blocked");
    }

    /**
     * Intercepts chat from frozen players.
     * Must run at LOW so it fires before ChatListener (NORMAL) modifies the message
     * with SmallCaps — otherwise the extracted text won't match the stored code.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getFreezeManager().isFrozen(player)) return;

        event.setCancelled(true);

        if (plugin.getCaptchaManager().hasActiveCaptcha(player)) {
            String text = PlainTextComponentSerializer.plainText().serialize(event.message());
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> plugin.getCaptchaManager().verify(player, text));
        } else {
            plugin.getMessageManager().send(player, "freeze.blocked");
        }
    }
}
