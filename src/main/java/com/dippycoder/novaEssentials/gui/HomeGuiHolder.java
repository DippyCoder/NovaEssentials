package com.dippycoder.novaEssentials.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class HomeGuiHolder implements InventoryHolder {

    private final Player player;
    private final int page;
    private Inventory inventory;

    public HomeGuiHolder(Player player, int page) {
        this.player = player;
        this.page = page;
    }

    public Player getPlayer() {
        return player;
    }

    public int getPage() {
        return page;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
