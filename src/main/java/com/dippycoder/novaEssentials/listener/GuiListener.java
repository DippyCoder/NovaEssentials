package com.dippycoder.novaEssentials.listener;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.gui.*;
import com.dippycoder.novaEssentials.manager.KitManager;
import com.dippycoder.novaEssentials.manager.PlayerSettingsManager;
import com.dippycoder.novaEssentials.util.DurationUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GuiListener implements Listener {

    private static final int NAV_PREV_SLOT  = 45;
    private static final int NAV_CLOSE_SLOT = 49;
    private static final int NAV_NEXT_SLOT  = 53;

    private final NovaEssentials plugin;

    public GuiListener(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    // ── InventoryDragEvent — cancel for all plugin GUIs ──────────

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof KitGuiHolder
                || holder instanceof WarpGuiHolder
                || holder instanceof HomeGuiHolder
                || holder instanceof InvseeHolder
                || holder instanceof SettingsGuiHolder) {
            event.setCancelled(true);
        }
    }

    // ── InventoryClickEvent ───────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof KitGuiHolder kitHolder) {
            handleKitClick(event, player, kitHolder);
        } else if (holder instanceof WarpGuiHolder warpHolder) {
            handleWarpClick(event, player, warpHolder);
        } else if (holder instanceof HomeGuiHolder homeHolder) {
            handleHomeClick(event, player, homeHolder);
        } else if (holder instanceof InvseeHolder invseeHolder) {
            handleInvseeClick(event, player, invseeHolder);
        } else if (holder instanceof SettingsGuiHolder) {
            handleSettingsClick(event, player);
        }
    }

    // ── KitGui click ──────────────────────────────────────────────

    private void handleKitClick(InventoryClickEvent event, Player player, KitGuiHolder holder) {
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        if (slot >= 45 && slot <= 53) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            if (slot == NAV_PREV_SLOT && clicked.getType() == Material.ARROW) {
                plugin.getSoundManager().playGuiClick(player);
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> KitGui.open(player, plugin, holder.getPage() - 1));
            } else if (slot == NAV_CLOSE_SLOT && clicked.getType() == Material.BARRIER) {
                plugin.getSoundManager().playGuiClick(player);
                plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
            } else if (slot == NAV_NEXT_SLOT && clicked.getType() == Material.ARROW) {
                plugin.getSoundManager().playGuiClick(player);
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> KitGui.open(player, plugin, holder.getPage() + 1));
            }
            return;
        }

        if (slot >= 0 && slot <= 44) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            ItemMeta meta = clicked.getItemMeta();
            if (meta == null) return;

            NamespacedKey kitNameKey = new NamespacedKey(plugin, "kit_name");
            String kitName = meta.getPersistentDataContainer()
                    .get(kitNameKey, PersistentDataType.STRING);
            if (kitName == null) return;

            KitManager kitManager = plugin.getKitManager();

            String kitPerm = kitManager.getKitPermission(kitName);
            if (!kitPerm.isEmpty() && !player.hasPermission(kitPerm)) {
                plugin.getMessageManager().send(player, "kit.no-perm", "kit", kitName);
                return;
            }

            long remaining = kitManager.getRemainingCooldown(player, kitName);
            if (remaining == -2) {
                plugin.getMessageManager().send(player, "kit.one-time", "kit", kitName);
                return;
            }
            if (remaining > 0) {
                plugin.getMessageManager().send(player, "kit.cooldown",
                        "kit", kitName, "time", DurationUtil.format(remaining));
                return;
            }

            plugin.getSoundManager().playGuiClick(player);
            kitManager.giveKit(player, kitName);
            kitManager.recordUse(player, kitName);
            plugin.getMessageManager().send(player, "kit.given", "kit", kitName);

            plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
        }
    }

    // ── WarpGui click ─────────────────────────────────────────────

    private void handleWarpClick(InventoryClickEvent event, Player player, WarpGuiHolder holder) {
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        if (slot >= 45 && slot <= 53) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            if (slot == NAV_PREV_SLOT && clicked.getType() == Material.ARROW) {
                plugin.getSoundManager().playGuiClick(player);
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> WarpGui.open(player, plugin, holder.getPage() - 1));
            } else if (slot == NAV_CLOSE_SLOT && clicked.getType() == Material.BARRIER) {
                plugin.getSoundManager().playGuiClick(player);
                plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
            } else if (slot == NAV_NEXT_SLOT && clicked.getType() == Material.ARROW) {
                plugin.getSoundManager().playGuiClick(player);
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> WarpGui.open(player, plugin, holder.getPage() + 1));
            }
            return;
        }

        if (slot >= 0 && slot <= 44) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            ItemMeta meta = clicked.getItemMeta();
            if (meta == null) return;

            NamespacedKey warpNameKey = new NamespacedKey(plugin, "warp_name");
            String warpName = meta.getPersistentDataContainer()
                    .get(warpNameKey, PersistentDataType.STRING);
            if (warpName == null) return;

            Location warp = plugin.getWarpManager().getWarp(warpName);
            if (warp == null) {
                plugin.getMessageManager().send(player, "warp.not-found", "warp", warpName);
                return;
            }

            plugin.getSoundManager().playGuiClick(player);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                if (plugin.getTeleportDelayManager().startDelayedTeleport(player, warp, "warp")) {
                    plugin.getMessageManager().send(player, "warp.teleported", "warp", warpName);
                }
            });
        }
    }

    // ── HomeGui click ─────────────────────────────────────────────

    private void handleHomeClick(InventoryClickEvent event, Player player, HomeGuiHolder holder) {
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        if (slot >= 45 && slot <= 53) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            if (slot == NAV_PREV_SLOT && clicked.getType() == Material.ARROW) {
                plugin.getSoundManager().playGuiClick(player);
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> HomeGui.open(player, plugin, holder.getPage() - 1));
            } else if (slot == NAV_CLOSE_SLOT && clicked.getType() == Material.BARRIER) {
                plugin.getSoundManager().playGuiClick(player);
                plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
            } else if (slot == NAV_NEXT_SLOT && clicked.getType() == Material.ARROW) {
                plugin.getSoundManager().playGuiClick(player);
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> HomeGui.open(player, plugin, holder.getPage() + 1));
            }
            return;
        }

        if (slot >= 0 && slot <= 44) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            ItemMeta meta = clicked.getItemMeta();
            if (meta == null) return;

            NamespacedKey homeNameKey = new NamespacedKey(plugin, "home_name");
            String homeName = meta.getPersistentDataContainer()
                    .get(homeNameKey, PersistentDataType.STRING);
            if (homeName == null) return;

            Location home = plugin.getHomeManager().getHome(player, homeName);
            if (home == null) {
                plugin.getMessageManager().send(player, "home.not-found", "home", homeName);
                return;
            }

            plugin.getSoundManager().playGuiClick(player);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                if (plugin.getTeleportDelayManager().startDelayedTeleport(player, home, "home")) {
                    plugin.getMessageManager().send(player, "home.teleported", "home", homeName);
                }
            });
        }
    }

    // ── SettingsGui click ─────────────────────────────────────────

    private void handleSettingsClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        // Close button
        if (slot == SettingsGui.SLOT_CLOSE) {
            plugin.getSoundManager().playGuiClick(player);
            plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()
                || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        NamespacedKey settingKey = new NamespacedKey(plugin, "setting_key");
        String settingId = meta.getPersistentDataContainer()
                .get(settingKey, PersistentDataType.STRING);
        if (settingId == null) return;

        PlayerSettingsManager sm = plugin.getPlayerSettingsManager();
        PlayerSettingsManager.PlayerSettings settings = sm.getSettings(player.getUniqueId());

        switch (settingId) {
            case "allow-tpa" -> {
                settings.allowTpa = !settings.allowTpa;
                playToggleSound(player, settings.allowTpa);
            }
            case "allow-tpahere" -> {
                settings.allowTpaHere = !settings.allowTpaHere;
                playToggleSound(player, settings.allowTpaHere);
            }
            case "tpauto" -> {
                settings.tpauto = !settings.tpauto;
                playToggleSound(player, settings.tpauto);
            }
            case "sounds" -> {
                settings.soundsEnabled = !settings.soundsEnabled;
                // Play feedback sound before toggling (if currently on)
                if (!settings.soundsEnabled) {
                    plugin.getSoundManager().playSettingsOff(player); // plays if was enabled
                }
                // (no sound when disabling since it's already off)
            }
            case "hide-chat" -> {
                settings.hideChat = !settings.hideChat;
                playToggleSound(player, settings.hideChat);
            }
            case "allow-msg" -> {
                settings.allowMsg = !settings.allowMsg;
                playToggleSound(player, settings.allowMsg);
            }
            case "allow-payments" -> {
                settings.allowPayments = !settings.allowPayments;
                playToggleSound(player, settings.allowPayments);
                sm.applyPermissions(player);
            }
            case "allow-balance" -> {
                settings.allowBalance = !settings.allowBalance;
                playToggleSound(player, settings.allowBalance);
                sm.applyPermissions(player);
            }
            case "language" -> {
                plugin.getSoundManager().playSettingsChange(player);
                cycleLanguage(settings, player);
            }
            default -> { return; }
        }

        sm.saveSettings(player.getUniqueId());
        plugin.getServer().getScheduler().runTask(plugin,
                () -> SettingsGui.open(player, plugin));
    }

    private void playToggleSound(Player player, boolean enabled) {
        if (enabled) plugin.getSoundManager().playSettingsOn(player);
        else         plugin.getSoundManager().playSettingsOff(player);
    }

    private void cycleLanguage(PlayerSettingsManager.PlayerSettings settings, Player player) {
        List<String> available = new ArrayList<>();
        available.add(null); // "auto" = use Minecraft client locale
        available.addAll(plugin.getMessageManager().getAvailableLocales());

        String current = settings.preferredLang;
        int idx = available.indexOf(current);
        int next = (idx + 1) % available.size();
        settings.preferredLang = available.get(next);
    }

    // ── InvseeGui click ───────────────────────────────────────────

    private void handleInvseeClick(InventoryClickEvent event, Player player, InvseeHolder holder) {
        int slot = event.getRawSlot();
        int invSize = event.getInventory().getSize();

        if (isGlassPaneSlot(slot)) {
            event.setCancelled(true);
            return;
        }

        if (!holder.canModify()) {
            if (slot < invSize) {
                event.setCancelled(true);
            }
            return;
        }

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
        }
    }

    // ── InventoryCloseEvent ───────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof InvseeHolder invseeHolder && invseeHolder.canModify()) {
            syncInvseeToTarget(event.getInventory(), invseeHolder.getTarget());
        }

        if (holder instanceof KitGuiHolder
                || holder instanceof WarpGuiHolder
                || holder instanceof HomeGuiHolder
                || holder instanceof SettingsGuiHolder) {
            plugin.getSoundManager().playGuiClose(player);
        }
    }

    private void syncInvseeToTarget(Inventory inv, Player target) {
        target.getInventory().setHelmet(inv.getItem(0));
        target.getInventory().setChestplate(inv.getItem(1));
        target.getInventory().setLeggings(inv.getItem(2));
        target.getInventory().setBoots(inv.getItem(3));
        target.getInventory().setItemInOffHand(inv.getItem(8));
        for (int i = 0; i <= 26; i++) {
            target.getInventory().setItem(9 + i, inv.getItem(9 + i));
        }
        for (int i = 0; i < 9; i++) {
            target.getInventory().setItem(i, inv.getItem(45 + i));
        }
    }

    private boolean isGlassPaneSlot(int slot) {
        return (slot >= 4 && slot <= 7) || (slot >= 36 && slot <= 44);
    }
}
