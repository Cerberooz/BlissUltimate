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

public class StrengthTrackerAdvancement extends BaseAdvancement {
  final Bliss plugin;
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();

  public StrengthTrackerAdvancement(
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
          static final String RABBIT_SEASON_ID = "rabbitSeason";
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              if (StrengthTrackerAdvancement.this.playerIds.contains(playerId)) {
                continue;
              }

              try {
                if (StrengthTrackerAdvancement.this.isGranted(player)) {
                  StrengthTrackerAdvancement.this.playerIds.add(playerId);
                  continue;
                }
              } catch (Exception ignored) {
                // Failure is non-fatal; normal event and lifecycle processing continues.
                continue;
              }

              int count = StrengthTrackerAdvancement.this.plugin.getConfiguredInt(player, RABBIT_SEASON_ID);
              int index = StrengthTrackerAdvancement.this.getProgression(player);
              if (count > index && count < 17) {
                try {
                  int remaining = count - index;

                  for (int step = 0; step < remaining; step++) {
                    StrengthTrackerAdvancement.this.incrementProgression(player);
                  }
                } catch (Exception ignored) {
                  // Failure is non-fatal; normal event and lifecycle processing continues.
                }
              } else if (count >= 17 && index < 17) {
                try {
                  int ticks = 17 - index;

                  for (int durationTicks = 0; durationTicks < ticks; durationTicks++) {
                    StrengthTrackerAdvancement.this.incrementProgression(player);
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
}
