package com.dippycoder.novaEssentials.gui;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.manager.KitManager;
import com.dippycoder.novaEssentials.util.DurationUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class KitGui {

    private static final int ITEMS_PER_PAGE = 45;
    private static final int NAV_PREV_SLOT  = 45;
    private static final int NAV_CLOSE_SLOT = 49;
    private static final int NAV_NEXT_SLOT  = 53;

    private KitGui() {}

    public static void open(Player player, NovaEssentials plugin, int page) {
        if (!plugin.getConfigManager().isKitGuiEnabled()) {
            plugin.getMessageManager().send(player, "gui.disabled");
            return;
        }

        KitManager kitManager = plugin.getKitManager();

        // Collect kits accessible by this player
        List<String> accessibleKits = kitManager.getKitNames().stream()
                .filter(name -> {
                    String perm = kitManager.getKitPermission(name);
                    return perm.isEmpty() || player.hasPermission(perm);
                })
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        int totalPages = Math.max(1, (int) Math.ceil(accessibleKits.size() / (double) ITEMS_PER_PAGE));
        int clampedPage = Math.max(0, Math.min(page, totalPages - 1));

        int start = clampedPage * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, accessibleKits.size());
        List<String> pageKits = accessibleKits.subList(start, end);

        // Build inventory
        KitGuiHolder holder = new KitGuiHolder(player, clampedPage);
        Component title = plugin.getMessageManager().get(player, "gui.kit-title");
        Inventory inv = plugin.getServer().createInventory(holder, 54, title);
        holder.setInventory(inv);

        NamespacedKey kitNameKey = new NamespacedKey(plugin, "kit_name");

        // Populate kit items
        for (int i = 0; i < pageKits.size(); i++) {
            String kitName = pageKits.get(i);
            ItemStack displayItem = buildKitItem(player, plugin, kitManager, kitName, kitNameKey);
            inv.setItem(i, displayItem);
        }

        // Navigation row: fill with gray glass panes first
        ItemStack filler = buildFiller();
        for (int slot = 45; slot <= 53; slot++) {
            inv.setItem(slot, filler);
        }

        // Previous page button
        if (clampedPage > 0) {
            inv.setItem(NAV_PREV_SLOT, buildNavItem(Material.ARROW,
                    Component.text("← Previous", NamedTextColor.YELLOW)));
        }

        // Close button
        inv.setItem(NAV_CLOSE_SLOT, buildNavItem(Material.BARRIER,
                Component.text("✗ Close", NamedTextColor.RED)));

        // Next page button
        if (end < accessibleKits.size()) {
            inv.setItem(NAV_NEXT_SLOT, buildNavItem(Material.ARROW,
                    Component.text("Next →", NamedTextColor.YELLOW)));
        }

        player.openInventory(inv);
    }

    private static ItemStack buildKitItem(Player player, NovaEssentials plugin,
                                          KitManager kitManager, String kitName,
                                          NamespacedKey kitNameKey) {
        // Determine display material: use first item in kit if available, else CHEST
        Material material = Material.CHEST;
        ConfigurationSection kitSection = plugin.getConfigManager().getKitSection(kitName);
        if (kitSection != null) {
            List<?> items = kitSection.getList("items");
            if (items != null && !items.isEmpty()) {
                Object first = items.get(0);
                if (first instanceof java.util.Map<?, ?> map) {
                    Object matObj = map.get("material");
                    if (matObj != null) {
                        Material mat = Material.matchMaterial(String.valueOf(matObj));
                        if (mat != null && !mat.isAir()) material = mat;
                    }
                }
            }
        }

        // Display name
        String displayName = kitName;
        if (kitSection != null) {
            displayName = kitSection.getString("display-name", kitName);
        }
        Component nameComponent = MiniMessage.miniMessage().deserialize(displayName);

        // Lore: cooldown status + price
        List<Component> lore = new ArrayList<>();
        long remaining = kitManager.getRemainingCooldown(player, kitName);
        if (remaining == -2) {
            lore.add(MiniMessage.miniMessage().deserialize("<red>Already claimed (one-time kit)"));
        } else if (remaining > 0) {
            lore.add(MiniMessage.miniMessage().deserialize(
                    "<yellow>Cooldown: <white>" + DurationUtil.format(remaining)));
        } else {
            lore.add(MiniMessage.miniMessage().deserialize("<green>Ready"));
        }

        double price = plugin.getConfigManager().getKitPrice(kitName);
        if (price > 0) {
            lore.add(MiniMessage.miniMessage().deserialize(
                    "<gold>Price: <white>$" + String.format("%.2f", price)));
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(nameComponent);
        meta.lore(lore);
        meta.getPersistentDataContainer().set(kitNameKey, PersistentDataType.STRING, kitName);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildNavItem(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(List.of());
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.space());
        item.setItemMeta(meta);
        return item;
    }
}
