package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LoreCommand extends BaseCommand {

    public LoreCommand(NovaEssentials plugin) {
        super(plugin, "lore");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.lore")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            msg.send(sender, "lore.no-item");
            return;
        }

        if (args.length == 0) {
            msg.send(sender, "general.invalid-args",
                    "usage", "/" + label + " <add|remove|clear|insert|set> [args]");
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "add"    -> handleAdd(sender, player, item, args);
            case "remove" -> handleRemove(sender, player, item, args);
            case "clear"  -> handleClear(sender, player, item);
            case "insert" -> handleInsert(sender, player, item, args);
            case "set"    -> handleSet(sender, player, item, args);
            default       -> msg.send(sender, "general.invalid-args",
                    "usage", "/" + label + " <add|remove|clear|insert|set> [args]");
        }
    }

    // /lore add <line>
    private void handleAdd(CommandSender sender, Player player, ItemStack item, String[] args) {
        if (args.length < 2) {
            msg.send(sender, "general.invalid-args", "usage", "/lore add <line>");
            return;
        }
        String rawLine = joinFrom(args, 1);
        Component line = parseLine(rawLine);

        ItemMeta meta = item.getItemMeta();
        List<Component> lore = getLore(meta);
        lore.add(line);
        meta.lore(lore);
        item.setItemMeta(meta);

        msg.send(sender, "lore.added");
    }

    // /lore remove <index>
    private void handleRemove(CommandSender sender, Player player, ItemStack item, String[] args) {
        if (args.length < 2) {
            msg.send(sender, "general.invalid-args", "usage", "/lore remove <index>");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<Component> lore = getLore(meta);

        if (lore.isEmpty()) {
            msg.send(sender, "lore.no-lore");
            return;
        }

        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            msg.send(sender, "general.invalid-number", "value", args[1]);
            return;
        }

        if (index < 1 || index > lore.size()) {
            msg.send(sender, "lore.invalid-index", "index", index);
            return;
        }

        lore.remove(index - 1);
        meta.lore(lore);
        item.setItemMeta(meta);

        msg.send(sender, "lore.removed", "index", index);
    }

    // /lore clear
    private void handleClear(CommandSender sender, Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.lore(new ArrayList<>());
        item.setItemMeta(meta);
        msg.send(sender, "lore.cleared");
    }

    // /lore insert <index> <line>
    private void handleInsert(CommandSender sender, Player player, ItemStack item, String[] args) {
        if (args.length < 3) {
            msg.send(sender, "general.invalid-args", "usage", "/lore insert <index> <line>");
            return;
        }

        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            msg.send(sender, "general.invalid-number", "value", args[1]);
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<Component> lore = getLore(meta);

        // Allow inserting at the end (index == lore.size() + 1) or anywhere within
        if (index < 1 || index > lore.size() + 1) {
            msg.send(sender, "lore.invalid-index", "index", index);
            return;
        }

        String rawLine = joinFrom(args, 2);
        Component line = parseLine(rawLine);
        lore.add(index - 1, line);
        meta.lore(lore);
        item.setItemMeta(meta);

        msg.send(sender, "lore.inserted", "index", index);
    }

    // /lore set <index> <line>
    private void handleSet(CommandSender sender, Player player, ItemStack item, String[] args) {
        if (args.length < 3) {
            msg.send(sender, "general.invalid-args", "usage", "/lore set <index> <line>");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        List<Component> lore = getLore(meta);

        if (lore.isEmpty()) {
            msg.send(sender, "lore.no-lore");
            return;
        }

        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            msg.send(sender, "general.invalid-number", "value", args[1]);
            return;
        }

        if (index < 1 || index > lore.size()) {
            msg.send(sender, "lore.invalid-index", "index", index);
            return;
        }

        String rawLine = joinFrom(args, 2);
        Component line = parseLine(rawLine);
        lore.set(index - 1, line);
        meta.lore(lore);
        item.setItemMeta(meta);

        msg.send(sender, "lore.set", "index", index);
    }

    /**
     * Gets the current lore list from an ItemMeta, returning a mutable copy.
     * Returns an empty mutable list if the item has no lore.
     */
    private List<Component> getLore(ItemMeta meta) {
        List<Component> existing = meta.lore();
        return existing != null ? new ArrayList<>(existing) : new ArrayList<>();
    }

    /**
     * Parses a lore line supporting both MiniMessage tags and legacy & color codes.
     */
    private Component parseLine(String raw) {
        String prepared = translateLegacyToMiniMessage(raw);
        return MiniMessage.miniMessage().deserialize(prepared);
    }

    private String translateLegacyToMiniMessage(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < input.length()) {
                char code = input.charAt(i + 1);
                String tag = legacyCodeToTag(code);
                if (tag != null) {
                    sb.append(tag);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private String legacyCodeToTag(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default  -> null;
        };
    }

    private String joinFrom(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterPrefix(List.of("add", "remove", "clear", "insert", "set"), args[0]);
        }
        return List.of();
    }
}
