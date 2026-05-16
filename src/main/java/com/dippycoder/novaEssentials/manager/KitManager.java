package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class KitManager {

    private final NovaEssentials plugin;
    /** player UUID → kit name → last use epoch millis */
    private final Map<UUID, Map<String, Long>> cooldownCache = new HashMap<>();

    public KitManager(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    public void loadPlayer(UUID uuid) {
        cooldownCache.put(uuid, plugin.getDatabaseManager().loadKitCooldowns(uuid));
    }

    public void unloadPlayer(UUID uuid) {
        cooldownCache.remove(uuid);
    }

    private Map<String, Long> getCooldowns(UUID uuid) {
        return cooldownCache.computeIfAbsent(uuid,
                u -> plugin.getDatabaseManager().loadKitCooldowns(u));
    }

    public Set<String> getKitNames() {
        ConfigurationSection kits = plugin.getConfigManager().getKitsSection();
        if (kits == null) return Collections.emptySet();
        return kits.getKeys(false);
    }

    public boolean kitExists(String name) {
        return plugin.getConfigManager().getKitSection(name) != null;
    }

    /**
     * Remaining cooldown in millis. 0 = ready. -2 = already claimed (one-time kit).
     */
    public long getRemainingCooldown(Player player, String kitName) {
        ConfigurationSection kit = plugin.getConfigManager().getKitSection(kitName);
        if (kit == null) return 0;
        int cooldownSecs = kit.getInt("cooldown", 0);
        if (cooldownSecs == 0) return 0;

        Long lastUse = getCooldowns(player.getUniqueId()).get(kitName);
        if (lastUse == null) return 0;

        if (cooldownSecs == -1) return -2; // one-time kit already used

        long elapsed = System.currentTimeMillis() - lastUse;
        long cooldownMs = (long) cooldownSecs * 1000;
        return Math.max(0, cooldownMs - elapsed);
    }

    public String getKitPermission(String kitName) {
        ConfigurationSection kit = plugin.getConfigManager().getKitSection(kitName);
        if (kit == null) return "";
        return kit.getString("permission", "");
    }

    public void recordUse(Player player, String kitName) {
        long now = System.currentTimeMillis();
        getCooldowns(player.getUniqueId()).put(kitName, now);
        plugin.getDatabaseManager().saveKitCooldown(player.getUniqueId(), kitName, now);
    }

    public void giveKit(Player player, String kitName) {
        ConfigurationSection kit = plugin.getConfigManager().getKitSection(kitName);
        if (kit == null) return;
        List<?> items = kit.getList("items");
        if (items == null) return;

        for (Object obj : items) {
            if (!(obj instanceof Map<?, ?> map)) continue;
            ItemStack stack = buildItem(map);
            if (stack != null) {
                player.getInventory().addItem(stack).forEach((slot, leftover) ->
                        player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ItemStack buildItem(Map<?, ?> rawMap) {
        Map<Object, Object> map = (Map<Object, Object>) (Map) rawMap;
        Object matObj = map.getOrDefault("material", "AIR");
        String matName = matObj != null ? String.valueOf(matObj) : "AIR";
        Material mat = Material.matchMaterial(matName);
        if (mat == null) return null;

        Object amtObj = map.getOrDefault("amount", 1);
        int amount = amtObj instanceof Number n ? n.intValue() : 1;
        ItemStack stack = new ItemStack(mat, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        Object nameObj = map.get("name");
        if (nameObj instanceof String name && !name.isBlank()) {
            meta.displayName(MiniMessage.miniMessage().deserialize(name));
        }

        Object loreObj = map.get("lore");
        if (loreObj instanceof List<?> loreList) {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (Object line : loreList) {
                if (line instanceof String s && !s.isBlank()) {
                    lore.add(MiniMessage.miniMessage().deserialize(s));
                }
            }
            if (!lore.isEmpty()) meta.lore(lore);
        }

        Object enchObj = map.get("enchantments");
        if (enchObj instanceof Map<?, ?> enchMap) {
            for (Map.Entry<?, ?> entry : enchMap.entrySet()) {
                String enchName = String.valueOf(entry.getKey());
                int level = (entry.getValue() instanceof Number n) ? n.intValue() : 1;
                Enchantment ench = Enchantment.getByName(enchName.toUpperCase());
                if (ench != null) meta.addEnchant(ench, level, true);
            }
        }

        stack.setItemMeta(meta);
        return stack;
    }
}
