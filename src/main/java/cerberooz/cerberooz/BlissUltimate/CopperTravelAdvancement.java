package cerberooz.cerberooz.BlissUltimate;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Range;

public class CopperTravelAdvancement extends BaseAdvancement {
  final Bliss plugin;
  final HashMap<UUID, Location> playerLocations = new HashMap<>();
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();

  public CopperTravelAdvancement(
      Bliss bliss,
      String text,
      AdvancementDisplay advancementDisplay,
      Advancement advancement,
      @Range(from = 1L, to = 2147483647L) int count) {
    super(text, advancementDisplay, advancement, count);
    this.plugin = bliss;
    this.startBackgroundTasks();
  }

  void startBackgroundTasks() {
    int period = this.plugin.getAdvancementCheckInterval();
    SharedScheduler.scheduleRepeating(
        this.plugin,
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              if (CopperTravelAdvancement.this.playerIds.contains(playerId)) {
                continue;
              }

              try {
                if (CopperTravelAdvancement.this.isGranted(player)) {
                  CopperTravelAdvancement.this.playerIds.add(playerId);
                  continue;
                }
              } catch (Exception ignored) {
                // Failure is non-fatal; normal event and lifecycle processing continues.
                continue;
              }

              Location location = player.getLocation();
              Location targetLocation = CopperTravelAdvancement.this.playerLocations.get(playerId);
              Block block = location.clone().subtract(0.0, 1.0, 0.0).getBlock();
              boolean material = CopperTravelAdvancement.this.isConditionMet(block.getType());
              if (material && targetLocation != null) {
                double distance = location.distance(targetLocation);
                if (distance > 0.1) {
                  int count = CopperTravelAdvancement.this.plugin.getConfiguredInt(player, "zipAway");
                  int index = (int) Math.round(distance);
                  int remaining = Math.min(count + index, 1914);
                  CopperTravelAdvancement.this.plugin.processAbilityState(player, "zipAway", remaining);
                }
              }

              if (material) {
                CopperTravelAdvancement.this.playerLocations.put(playerId, location.clone());
              }

              int step = CopperTravelAdvancement.this.plugin.getConfiguredInt(player, "zipAway");
              int ticks = CopperTravelAdvancement.this.getProgression(player);
              if (step > ticks && step < 1914) {
                try {
                  int durationTicks = step - ticks;

                  for (int attempts = 0; attempts < durationTicks; attempts++) {
                    CopperTravelAdvancement.this.incrementProgression(player);
                  }
                } catch (Exception ignored) {
                  // Failure is non-fatal; normal event and lifecycle processing continues.
                }
              } else if (step >= 1914 && ticks < 1914) {
                try {
                  int amount = 1914 - ticks;

                  for (int level = 0; level < amount; level++) {
                    CopperTravelAdvancement.this.incrementProgression(player);
                  }
                } catch (Exception ignored) {
                  // Failure is non-fatal; normal event and lifecycle processing continues.
                }
              }
            }
          }
        },
        SharedScheduler.staggeredInitialDelay(period),
        period);
  }

  boolean isConditionMet(Material material) {
    return material == Material.COPPER_BLOCK;
  }
}
