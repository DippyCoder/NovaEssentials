package com.dippycoder.novaEssentials.listener;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.database.DatabaseManager.MuteInfo;
import com.dippycoder.novaEssentials.util.DurationUtil;
import com.dippycoder.novaEssentials.util.SmallCapsUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChatListener implements Listener {

    private final NovaEssentials plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ChatListener(NovaEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // ── Mute check ────────────────────────────────────────
        if (!player.hasPermission(plugin.getConfigManager().getPermission("chat.bypass-mute"))) {
            MuteInfo muteInfo = plugin.getMuteManager().getMuteInfo(player.getUniqueId());
            if (muteInfo != null) {
                event.setCancelled(true);
                if (muteInfo.isPermanent()) {
                    plugin.getMessageManager().send(player, "chat.muted-permanent");
                } else {
                    long remaining = muteInfo.until() - System.currentTimeMillis();
                    plugin.getMessageManager().send(player, "chat.muted",
                            "time", DurationUtil.format(remaining));
                }
                return;
            }
        }

        // ── Chat pause check ───────────────────────────────────
        if (plugin.getChatCommand().isChatPaused()
                && !player.hasPermission(plugin.getConfigManager().getPermission("chat.bypass-pause"))) {
            event.setCancelled(true);
            plugin.getMessageManager().send(player, "chat.blocked-pause");
            return;
        }

        // ── Get raw message text ───────────────────────────────
        String rawText = PlainTextComponentSerializer.plainText().serialize(event.message());

        // ── Chat filter ────────────────────────────────────────
        if (!player.hasPermission(plugin.getConfigManager().getPermission("chat.bypass-filter"))) {
            if (plugin.getChatFilterManager().contains(rawText)) {
                if (plugin.getConfigManager().isFilterNotifyStaff()) {
                    final String capturedRaw = rawText;
                    final String capturedName = player.getName();
                    // Run on main thread; each recipient gets the message in their own locale
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        plugin.getMessageManager().broadcastToPermission(
                                plugin.getConfigManager().getPermission("cmd.mute"),
                                "chat.staff-filter-notify",
                                "player", capturedName, "message", capturedRaw)
                    );
                }
                rawText = plugin.getChatFilterManager().filter(rawText);
            }
        }

        // ── Build message component ────────────────────────────
        Component messageComponent = buildMessageComponent(player, rawText);

        // ── SmallCaps ─────────────────────────────────────────
        if (shouldApplySmallCaps(player)) {
            messageComponent = SmallCapsUtil.applyToComponent(messageComponent);
        }

        event.message(messageComponent);

        // ── Custom formatter ───────────────────────────────────
        if (plugin.getConfigManager().isChatFormatterEnabled()) {
            final Component finalMsg = messageComponent;
            event.renderer((source, sourceDisplayName, message, viewer) ->
                    buildFormattedChat(source, sourceDisplayName, finalMsg));
        }

        // ── Hide-chat filter — remove viewers who opted out ────
        var sm = plugin.getPlayerSettingsManager();
        if (sm != null) {
            event.viewers().removeIf(audience -> {
                if (!(audience instanceof Player viewer)) return false;
                if (viewer.equals(player)) return false;
                return sm.getSettings(viewer.getUniqueId()).hideChat;
            });
        }
    }

    private Component buildMessageComponent(Player player, String rawText) {
        // Handle [item] placeholder
        if (plugin.getConfigManager().isItemPlaceholderEnabled() && rawText.contains("[item]")) {
            return buildWithItemPlaceholder(player, rawText);
        }

        // Apply color/MiniMessage based on permissions
        return applyFormatting(player, rawText);
    }

    private Component buildWithItemPlaceholder(Player player, String rawText) {
        String[] parts = rawText.split("(?i)\\[item\\]", -1);
        Component result = Component.empty();
        for (int i = 0; i < parts.length; i++) {
            result = result.append(applyFormatting(player, parts[i]));
            if (i < parts.length - 1) {
                result = result.append(buildItemComponent(player));
            }
        }
        return result;
    }

    private Component applyFormatting(Player player, String text) {
        if (text.isEmpty()) return Component.empty();
        if (player.hasPermission(plugin.getConfigManager().getPermission("chat.minimessage"))) {
            return mm.deserialize(text);
        }
        if (player.hasPermission(plugin.getConfigManager().getPermission("chat.color"))) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }
        return Component.text(text);
    }

    private Component buildItemComponent(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        String displayName;
        if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            displayName = PlainTextComponentSerializer.plainText().serialize(
                    item.getItemMeta().displayName() != null
                            ? item.getItemMeta().displayName()
                            : Component.text(formatMaterial(item.getType().name())));
        } else {
            displayName = formatMaterial(item.getType().name());
        }

        String format = plugin.getConfigManager().getItemPlaceholderFormat()
                .replace("<name>", displayName)
                .replace("<material>", item.getType().name().toLowerCase())
                .replace("<amount>", String.valueOf(item.getAmount()));

        // Use Paper's asHoverEvent() for correct item display
        Component itemComp = mm.deserialize(format);
        return itemComp.hoverEvent(item.asHoverEvent());
    }

    private Component buildFormattedChat(Player source, Component displayName, Component message) {
        String format = plugin.getConfigManager().getChatFormat();

        // PAPI support
        if (plugin.getMessageManager().isPapiLoaded()) {
            format = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(source, format);
        }

        return mm.deserialize(format,
                TagResolver.resolver(
                        Placeholder.component("player", Component.text(source.getName())),
                        Placeholder.component("display_name", displayName),
                        Placeholder.unparsed("world", source.getWorld().getName()),
                        Placeholder.component("message", message)
                ));
    }

    private boolean shouldApplySmallCaps(Player player) {
        if (!plugin.getConfigManager().isSmallCapsEnabled()) return false;
        if (!plugin.getConfigManager().isSmallCapsPermissionRequired()) return true;
        return player.hasPermission(plugin.getConfigManager().getPermission("chat.smallcaps"));
    }

    private String formatMaterial(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}
