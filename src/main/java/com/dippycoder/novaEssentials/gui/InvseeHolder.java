package com.dippycoder.novaEssentials.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class InvseeHolder implements InventoryHolder {

    private final Player target;
    private final Player viewer;
    private final boolean canModify;
    private Inventory inventory;

    public InvseeHolder(Player target, Player viewer, boolean canModify) {
        this.target = target;
        this.viewer = viewer;
        this.canModify = canModify;
    }

    public Player getTarget() {
        return target;
    }

    public Player getViewer() {
        return viewer;
    }

    public boolean canModify() {
        return canModify;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
