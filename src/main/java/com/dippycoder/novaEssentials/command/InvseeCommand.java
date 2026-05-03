package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import com.dippycoder.novaEssentials.gui.InvseeHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InvseeCommand extends BaseCommand {

    public InvseeCommand(NovaEssentials plugin) {
        super(plugin, "invsee");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.invsee")) return;
        if (!requirePlayer(sender)) return;

        if (args.length == 0) {
            msg.send(sender, "general.invalid-args", "usage", "/" + label + " <player>");
            return;
        }

        Player viewer = (Player) sender;
        Player target = findPlayer(sender, args[0]);
        if (target == null) return;

        if (target.equals(viewer)) {
            viewer.openInventory(viewer.getInventory());
            return;
        }

        boolean canModify = viewer.hasPermission(
                plugin.getConfigManager().getPermission("cmd.invsee.modify"));

        openInvseeGui(viewer, target, canModify);
        msg.send(sender, "invsee.opened", "player", target.getName());
    }

    /**
     * Builds a 6-row (54-slot) custom inventory displaying the target's full inventory.
     *
     * Layout:
     *  Row 1 (slots  0-8):  Helmet, Chestplate, Leggings, Boots, ░░░░, Offhand
     *  Rows 2-4 (slots 9-35): target's main inventory (player slots 9-35)
     *  Row 5 (slots 36-44): separator (glass panes)
     *  Row 6 (slots 45-53): target's hotbar (player slots 0-8)
     */
    private void openInvseeGui(Player viewer, Player target, boolean canModify) {
        InvseeHolder holder = new InvseeHolder(target, viewer, canModify);

        Component title = plugin.getMessageManager().get(viewer, "gui.invsee-title",
                "player", target.getName());

        Inventory inv = plugin.getServer().createInventory(holder, 54, title);
        holder.setInventory(inv);

        ItemStack pane = buildGlassPane();

        // Row 1 (slots 0-8): armor, fillers, offhand
        inv.setItem(0, target.getInventory().getHelmet());
        inv.setItem(1, target.getInventory().getChestplate());
        inv.setItem(2, target.getInventory().getLeggings());
        inv.setItem(3, target.getInventory().getBoots());
        for (int i = 4; i <= 7; i++) inv.setItem(i, pane);
        inv.setItem(8, target.getInventory().getItemInOffHand());

        // Rows 2-4 (slots 9-35) ← main inventory slots 9-35
        for (int i = 0; i <= 26; i++) {
            inv.setItem(9 + i, target.getInventory().getItem(9 + i));
        }

        // Row 5 (slots 36-44): separator
        for (int i = 36; i <= 44; i++) inv.setItem(i, pane);

        // Row 6 (slots 45-53) ← hotbar slots 0-8
        for (int i = 0; i < 9; i++) {
            inv.setItem(45 + i, target.getInventory().getItem(i));
        }

        viewer.openInventory(inv);
    }

    private ItemStack buildGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.space());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
