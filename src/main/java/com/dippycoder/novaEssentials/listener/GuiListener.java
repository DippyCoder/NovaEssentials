package com.dippycoder.novaEssentials.listener;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.gui.*;
import com.dippycoder.novaEssentials.manager.KitManager;
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

public class GuiListener implements Listener {

    private static final int NAV_PREV_SLOT  = 45;
    private static final int NAV_CLOSE_SLOT = 49;
    private static final int NAV_NEXT_SLOT  = 53;

    // InvseeHolder glass pane ranges: armor-row fillers (4-7) + separator row (36-44)

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
                || holder instanceof InvseeHolder) {
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
        }
    }

    // ── KitGui click ──────────────────────────────────────────────

    private void handleKitClick(InventoryClickEvent event, Player player, KitGuiHolder holder) {
        event.setCancelled(true);

        int slot = event.getRawSlot();
        // Only handle clicks inside the custom inventory (top inventory)
        if (slot < 0 || slot >= 54) return;

        // Navigation row
        if (slot >= 45 && slot <= 53) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            if (slot == NAV_PREV_SLOT && clicked.getType() == Material.ARROW) {
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> KitGui.open(player, plugin, holder.getPage() - 1));
            } else if (slot == NAV_CLOSE_SLOT && clicked.getType() == Material.BARRIER) {
                plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
            } else if (slot == NAV_NEXT_SLOT && clicked.getType() == Material.ARROW) {
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> KitGui.open(player, plugin, holder.getPage() + 1));
            }
            return;
        }

        // Item area (slots 0-44)
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

            // Permission check
            String kitPerm = kitManager.getKitPermission(kitName);
            if (!kitPerm.isEmpty() && !player.hasPermission(kitPerm)) {
                plugin.getMessageManager().send(player, "kit.no-perm", "kit", kitName);
                return;
            }

            // Cooldown check
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

            // Economy check
            if (plugin.getConfigManager().isEconomyEnabled()) {
                double price = plugin.getConfigManager().getKitPrice(kitName);
                if (price > 0) {
                    // Economy integration point — for now just check if Vault is available
                    // If not configured/available, skip the check
                    // This can be expanded when a VaultManager is added
                }
            }

            // Give kit
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
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> WarpGui.open(player, plugin, holder.getPage() - 1));
            } else if (slot == NAV_CLOSE_SLOT && clicked.getType() == Material.BARRIER) {
                plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
            } else if (slot == NAV_NEXT_SLOT && clicked.getType() == Material.ARROW) {
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

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                player.teleport(warp);
                plugin.getMessageManager().send(player, "warp.teleported", "warp", warpName);
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
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> HomeGui.open(player, plugin, holder.getPage() - 1));
            } else if (slot == NAV_CLOSE_SLOT && clicked.getType() == Material.BARRIER) {
                plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
            } else if (slot == NAV_NEXT_SLOT && clicked.getType() == Material.ARROW) {
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

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                player.teleport(home);
                plugin.getMessageManager().send(player, "home.teleported", "home", homeName);
            });
        }
    }

    // ── InvseeGui click ───────────────────────────────────────────

    private void handleInvseeClick(InventoryClickEvent event, Player player, InvseeHolder holder) {
        int slot = event.getRawSlot();
        int invSize = event.getInventory().getSize(); // 54

        // Clicks on glass pane filler slots — always cancel
        if (isGlassPaneSlot(slot)) {
            event.setCancelled(true);
            return;
        }

        // If viewer cannot modify, cancel everything in the custom inventory
        if (!holder.canModify()) {
            if (slot < invSize) {
                event.setCancelled(true);
            }
            return;
        }

        // Can modify: allow all interactions within the custom inventory,
        // but cancel shift-click that would move items to the viewer's own inventory
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
    }

    /**
     * Syncs the contents of the InvseeHolder inventory back to the target player.
     *
     * Layout (54 slots):
     *  Row 1 (slots 0-8):   0=Helmet, 1=Chestplate, 2=Leggings, 3=Boots, 4-7=panes, 8=Offhand
     *  Rows 2-4 (slots 9-35)  → main inventory slots 9-35
     *  Row 5 (slots 36-44): separator panes (ignored)
     *  Row 6 (slots 45-53) → hotbar slots 0-8
     */
    private void syncInvseeToTarget(Inventory inv, Player target) {
        // Row 1: armor + offhand
        target.getInventory().setHelmet(inv.getItem(0));
        target.getInventory().setChestplate(inv.getItem(1));
        target.getInventory().setLeggings(inv.getItem(2));
        target.getInventory().setBoots(inv.getItem(3));
        target.getInventory().setItemInOffHand(inv.getItem(8));

        // Rows 2-4 → main inventory 9-35
        for (int i = 0; i <= 26; i++) {
            target.getInventory().setItem(9 + i, inv.getItem(9 + i));
        }

        // Row 6 → hotbar slots 0-8
        for (int i = 0; i < 9; i++) {
            target.getInventory().setItem(i, inv.getItem(45 + i));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private boolean isGlassPaneSlot(int slot) {
        return (slot >= 4 && slot <= 7) || (slot >= 36 && slot <= 44);
    }
}
