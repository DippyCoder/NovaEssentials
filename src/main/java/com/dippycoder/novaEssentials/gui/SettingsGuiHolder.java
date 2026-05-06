package com.dippycoder.novaEssentials.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SettingsGuiHolder implements InventoryHolder {

    private final Player player;
    private Inventory inventory;

    public SettingsGuiHolder(Player player) {
        this.player = player;
    }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public Player getPlayer() { return player; }
}
