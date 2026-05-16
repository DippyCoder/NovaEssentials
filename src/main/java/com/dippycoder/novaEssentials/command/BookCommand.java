package com.dippycoder.novaEssentials.command;

import com.dippycoder.novaEssentials.NovaEssentials;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.WritableBookMeta;

import java.util.List;

public class BookCommand extends BaseCommand {

    public BookCommand(NovaEssentials plugin) {
        super(plugin, "book");
    }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        if (!requirePermission(sender, "cmd.book")) return;
        if (!requirePlayer(sender)) return;

        Player player = (Player) sender;
        ItemStack held = player.getInventory().getItemInMainHand();

        if (held.getType() != Material.WRITTEN_BOOK) {
            msg.send(sender, "book.not-written-book");
            return;
        }

        BookMeta bookMeta = (BookMeta) held.getItemMeta();
        List<String> pages = bookMeta.getPages();

        ItemStack writableBook = new ItemStack(Material.WRITABLE_BOOK);
        WritableBookMeta writableMeta = (WritableBookMeta) writableBook.getItemMeta();
        if (!pages.isEmpty()) {
            writableMeta.setPages(pages);
        }
        writableBook.setItemMeta(writableMeta);

        player.getInventory().setItemInMainHand(writableBook);
        msg.send(sender, "book.converted");
    }
}
