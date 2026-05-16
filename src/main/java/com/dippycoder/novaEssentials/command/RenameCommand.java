package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class RenameCommand extends BaseCommand {

    public RenameCommand(NovaEssentials plugin) {
        super(plugin, "rename");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.rename")) return;
        if (!requirePlayer(sender)) return;
        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <name>");
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            msg.send(sender, "rename.no-item");
            return;
        }

        // Join all args in case the name has spaces
        String rawName = String.join(" ", args);

        // Parse: first translate legacy & codes, then deserialize as MiniMessage
        Component nameComponent = parseName(rawName);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            msg.send(sender, "rename.no-item");
            return;
        }

        meta.displayName(nameComponent);
        item.setItemMeta(meta);

        msg.send(sender, "rename.renamed", "name", nameComponent);
    }

    /**
     * Parses a name that may contain MiniMessage tags and/or legacy &-color codes.
     * Legacy & codes are first translated to section symbols so MiniMessage's
     * legacy support handles them, or we manually convert them before deserializing.
     */
    private Component parseName(String rawName) {
        // Translate legacy & codes to § first, then use LegacyComponentSerializer
        // to convert to Component, but we want to also support MiniMessage tags.
        // Strategy: if the string contains MiniMessage tags (< and >), use MiniMessage directly.
        // Otherwise translate & codes via LegacyComponentSerializer.
        // For maximum compatibility we do: translate & -> § then feed to legacy serializer,
        // BUT if it also has MiniMessage syntax we want that too.
        // Best approach: translate & to § section-symbol legacy, then serialize to component via
        // LegacyComponentSerializer which gives us colored component — but we lose MiniMessage.
        // Instead: translate & codes first into MiniMessage-compatible form by replacing &X with
        // the equivalent MiniMessage tag, then deserialize with MiniMessage.
        // Actually the simplest reliable approach: translate & codes to their MiniMessage equivalents
        // then call MiniMessage.deserialize().
        String prepared = translateLegacyToMiniMessage(rawName);
        return MiniMessage.miniMessage().deserialize(prepared);
    }

    /**
     * Translates common legacy &-color codes into their MiniMessage tag equivalents
     * so that MiniMessage can parse both syntaxes in one pass.
     */
    private String translateLegacyToMiniMessage(String input) {
        // Use LegacyComponentSerializer to parse & codes, serialize to MiniMessage string
        // This handles: &0-9, &a-f, &k-o, &r
        // We convert to Component first, then back to MiniMessage serialized string.
        // However that loses MiniMessage tags already in the input.
        // The safest approach: replace & codes before any < > tags are processed.
        // We'll do a character-scan replacement of & followed by a valid code char.
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < input.length()) {
                char code = input.charAt(i + 1);
                String tag = legacyCodeToTag(code);
                if (tag != null) {
                    sb.append(tag);
                    i++; // skip the code char
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

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
