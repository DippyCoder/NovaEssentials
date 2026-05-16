package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class GiveHeadCommand extends BaseCommand {

    public GiveHeadCommand(NovaEssentials plugin) {
        super(plugin, "givehead");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.givehead")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        String targetName = args.length > 0 ? args[0] : player.getName();

        @SuppressWarnings("deprecation")
        OfflinePlayer skullOwner = plugin.getServer().getOfflinePlayer(targetName);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(skullOwner);
        skull.setItemMeta(meta);

        player.getInventory().addItem(skull);
        msg.send(sender, "givehead.given", "player", targetName);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterPrefix(onlinePlayerNames(sender), args[0]);
        return List.of();
    }
}
