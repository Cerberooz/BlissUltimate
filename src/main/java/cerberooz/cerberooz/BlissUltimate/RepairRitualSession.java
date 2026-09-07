package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

class RepairRitualSession {
  UUID playerId;
  EnderCrystal enderCrystal;
  int durationTicks;
  boolean active;
  List<ItemDisplay> entries = new ArrayList<>();
  BukkitRunnable activeRunnable;
  BukkitRunnable secondaryRunnable;
  BukkitTask scheduledTask;
  boolean secondaryActive;
  BukkitRunnable runnable;
  int maxCount = 1;
  ItemStack item;
  Set<UUID> playerIds = new HashSet<>();

  RepairRitualSession() {}
}
