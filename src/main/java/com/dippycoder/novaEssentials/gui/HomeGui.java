package com.dippycoder.novaEssentials.gui;

import com.dippycoder.novaEssentials.NovaEssentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class HomeGui {

    private static final int ITEMS_PER_PAGE = 45;
    private static final int NAV_PREV_SLOT  = 45;
    private static final int NAV_CLOSE_SLOT = 49;
    private static final int NAV_NEXT_SLOT  = 53;

    private HomeGui() {}

    public static void open(Player player, NovaEssentials plugin, int page) {
        if (!plugin.getConfigManager().isHomeGuiEnabled()) {
            plugin.getMessageManager().send(player, "gui.disabled");
            return;
        }

        // Collect this player's homes, sorted
        List<String> homeNames = plugin.getHomeManager().getHomeNames(player).stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        int totalPages = Math.max(1, (int) Math.ceil(homeNames.size() / (double) ITEMS_PER_PAGE));
        int clampedPage = Math.max(0, Math.min(page, totalPages - 1));

        int start = clampedPage * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, homeNames.size());
        List<String> pageHomes = homeNames.subList(start, end);

        // Build inventory
        HomeGuiHolder holder = new HomeGuiHolder(player, clampedPage);
        Component title = plugin.getMessageManager().get(player, "gui.home-title");
        Inventory inv = plugin.getServer().createInventory(holder, 54, title);
        holder.setInventory(inv);

        NamespacedKey homeNameKey = new NamespacedKey(plugin, "home_name");

        // Populate home items
        for (int i = 0; i < pageHomes.size(); i++) {
            String homeName = pageHomes.get(i);
            inv.setItem(i, buildHomeItem(homeName, homeNameKey));
        }

        // Navigation row filler
        ItemStack filler = buildFiller();
        for (int slot = 45; slot <= 53; slot++) {
            inv.setItem(slot, filler);
        }

        if (clampedPage > 0) {
            inv.setItem(NAV_PREV_SLOT, buildNavItem(Material.ARROW,
                    Component.text("← Previous", NamedTextColor.YELLOW)));
        }

        inv.setItem(NAV_CLOSE_SLOT, buildNavItem(Material.BARRIER,
                Component.text("✗ Close", NamedTextColor.RED)));

        if (end < homeNames.size()) {
            inv.setItem(NAV_NEXT_SLOT, buildNavItem(Material.ARROW,
                    Component.text("Next →", NamedTextColor.YELLOW)));
        }

        player.openInventory(inv);
    }

    private static ItemStack buildHomeItem(String homeName, NamespacedKey key) {
        ItemStack item = new ItemStack(Material.WHITE_BED);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize("<green>" + homeName));
        meta.lore(List.of(MiniMessage.miniMessage().deserialize("<gray>Click to teleport")));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, homeName);
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
