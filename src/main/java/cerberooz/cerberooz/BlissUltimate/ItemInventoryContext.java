package cerberooz.cerberooz.BlissUltimate;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

record ItemInventoryContext(ItemStack item, Inventory destination) {}
