package com.dippycoder.novaEssentials.gui;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.manager.PlayerSettingsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class SettingsGui {

    // Setting item slots
    public static final int SLOT_ALLOW_TPA      = 10;
    public static final int SLOT_ALLOW_TPAHERE  = 11;
    public static final int SLOT_TPAUTO         = 12;
    public static final int SLOT_LANGUAGE       = 13;
    public static final int SLOT_SOUNDS         = 14;
    public static final int SLOT_HIDE_CHAT      = 19;
    public static final int SLOT_ALLOW_MSG      = 20;
    public static final int SLOT_ALLOW_PAYMENTS = 21;
    public static final int SLOT_ALLOW_BALANCE  = 22;
    public static final int SLOT_CLOSE          = 49;

    private SettingsGui() {}

    public static void open(Player player, NovaEssentials plugin) {
        PlayerSettingsManager.PlayerSettings settings =
                plugin.getPlayerSettingsManager().getSettings(player.getUniqueId());

        SettingsGuiHolder holder = new SettingsGuiHolder(player);
        Component title = plugin.getMessageManager().get(player, "settings.title");
        Inventory inv = plugin.getServer().createInventory(holder, 54, title);
        holder.setInventory(inv);

        NamespacedKey settingKey = new NamespacedKey(plugin, "setting_key");

        // Fill background
        ItemStack filler = buildFiller();
        for (int i = 0; i < 54; i++) inv.setItem(i, filler);

        // Row 1: TPA settings
        inv.setItem(SLOT_ALLOW_TPA,
                buildBoolItem(player, plugin, settingKey, "allow-tpa",
                        settings.allowTpa,
                        settings.allowTpa ? Material.LIME_WOOL : Material.RED_WOOL));
        inv.setItem(SLOT_ALLOW_TPAHERE,
                buildBoolItem(player, plugin, settingKey, "allow-tpahere",
                        settings.allowTpaHere,
                        settings.allowTpaHere ? Material.LIME_WOOL : Material.RED_WOOL));
        inv.setItem(SLOT_TPAUTO,
                buildBoolItem(player, plugin, settingKey, "tpauto",
                        settings.tpauto,
                        settings.tpauto ? Material.CLOCK : Material.COMPARATOR));
        inv.setItem(SLOT_LANGUAGE,
                buildLangItem(player, plugin, settingKey, settings));
        inv.setItem(SLOT_SOUNDS,
                buildBoolItem(player, plugin, settingKey, "sounds",
                        settings.soundsEnabled,
                        settings.soundsEnabled ? Material.NOTE_BLOCK : Material.BARRIER));

        // Row 2: Chat/message settings
        inv.setItem(SLOT_HIDE_CHAT,
                buildBoolItem(player, plugin, settingKey, "hide-chat",
                        settings.hideChat,
                        settings.hideChat ? Material.RED_STAINED_GLASS : Material.GREEN_STAINED_GLASS));
        inv.setItem(SLOT_ALLOW_MSG,
                buildBoolItem(player, plugin, settingKey, "allow-msg",
                        settings.allowMsg,
                        settings.allowMsg ? Material.LIME_WOOL : Material.RED_WOOL));
        inv.setItem(SLOT_ALLOW_PAYMENTS,
                buildBoolItem(player, plugin, settingKey, "allow-payments",
                        settings.allowPayments,
                        settings.allowPayments ? Material.GOLD_NUGGET : Material.IRON_NUGGET));
        inv.setItem(SLOT_ALLOW_BALANCE,
                buildBoolItem(player, plugin, settingKey, "allow-balance",
                        settings.allowBalance,
                        settings.allowBalance ? Material.GOLD_INGOT : Material.IRON_INGOT));

        // Close button
        inv.setItem(SLOT_CLOSE, buildCloseItem(player, plugin));

        plugin.getSoundManager().playGuiOpen(player);
        player.openInventory(inv);
    }

    private static ItemStack buildBoolItem(Player player, NovaEssentials plugin,
            NamespacedKey key, String settingId, boolean enabled, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plugin.getMessageManager().get(player,
                "settings." + settingId + ".name"));
        meta.lore(List.of(plugin.getMessageManager().get(player,
                "settings." + settingId + (enabled ? ".lore-on" : ".lore-off"))));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, settingId);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildLangItem(Player player, NovaEssentials plugin,
            NamespacedKey key, PlayerSettingsManager.PlayerSettings settings) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plugin.getMessageManager().get(player, "settings.language.name"));

        String currentLang = settings.preferredLang != null
                ? settings.preferredLang
                : player.locale().getLanguage();
        String loreKey = settings.preferredLang != null
                ? "settings.language.lore"
                : "settings.language.lore-auto";
        meta.lore(List.of(plugin.getMessageManager().get(player, loreKey,
                "language", currentLang)));

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "language");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildCloseItem(Player player, NovaEssentials plugin) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plugin.getMessageManager().get(player, "gui.close"));
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
