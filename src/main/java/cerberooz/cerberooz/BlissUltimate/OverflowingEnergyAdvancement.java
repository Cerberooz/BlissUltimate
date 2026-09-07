package cerberooz.cerberooz.BlissUltimate;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Range;

public class OverflowingEnergyAdvancement extends BaseAdvancement {
  final Bliss plugin;
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();

  public OverflowingEnergyAdvancement(
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
              if (OverflowingEnergyAdvancement.this.playerIds.contains(playerId)) {
                continue;
              }

              try {
                if (OverflowingEnergyAdvancement.this.isGranted(player)) {
                  OverflowingEnergyAdvancement.this.playerIds.add(playerId);
                  continue;
                }
              } catch (Exception ignored) {
                // Failure is non-fatal; normal event and lifecycle processing continues.
                continue;
              }

              if (OverflowingEnergyAdvancement.this.plugin.getEnergyManager() != null) {
                OverflowingEnergyAdvancement.this.plugin.getEnergyManager();
                if (GemEnergyManager.getAbilityIntValue(player) == 10) {
                  try {
                    OverflowingEnergyAdvancement.this.incrementProgression(player);
                  } catch (Exception ignored) {
                    // Failure is non-fatal; normal event and lifecycle processing continues.
                  }
                }
              }
            }
          }
        },
        SharedScheduler.staggeredInitialDelay(period),
        period);
  }
}
