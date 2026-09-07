package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

class RestorationRitualSession {
  UUID playerId;
  EnderCrystal enderCrystal;
  int durationTicks;
  boolean currentActive;
  List<ItemDisplay> entries = new ArrayList<>();
  BukkitRunnable activeRunnable;
  BukkitRunnable secondaryRunnable;
  boolean active;
  BukkitRunnable runnable;
  int maxCount = 1;
  ItemStack item;
  boolean pendingActive;
  boolean activeState;
  int cooldownTicks = -1;
  ItemStack secondaryItem;
  boolean secondaryActive;

  RestorationRitualSession() {}
}
