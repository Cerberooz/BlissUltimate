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

public class AlmostFellFromGraceAdvancement extends BaseAdvancement {
  final Bliss plugin;
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();

  public AlmostFellFromGraceAdvancement(
      Bliss bliss,
      String text,
      AdvancementDisplay advancementDisplay,
      Advancement advancement,
      @Range(from = 1L, to = 2147483647L) int count,
      PuffTier1Gem puffTier1Gem,
      PuffTier2Gem puffTier2Gem) {
    super(text, advancementDisplay, advancement, count);
    this.plugin = bliss;
    this.startBackgroundTasks();
  }

  void startBackgroundTasks() {
    int period = this.plugin.getAdvancementCheckInterval();
    SharedScheduler.scheduleRepeating(
        this.plugin,
        new BukkitRunnable() {
          static final String ALMOST_FELL_FROM_GRACE_ID = "almostFellFromGrace";
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              if (AlmostFellFromGraceAdvancement.this.playerIds.contains(playerId)) {
                continue;
              }

              try {
                if (AlmostFellFromGraceAdvancement.this.isGranted(player)) {
                  AlmostFellFromGraceAdvancement.this.playerIds.add(playerId);
                  continue;
                }
              } catch (Exception ignored) {
                // Failure is non-fatal; normal event and lifecycle processing continues.
                continue;
              }

              int count = AlmostFellFromGraceAdvancement.this.plugin.getConfiguredInt(player, ALMOST_FELL_FROM_GRACE_ID);
              int index = AlmostFellFromGraceAdvancement.this.getProgression(player);
              if (count > index && count < 30000) {
                try {
                  int remaining = count - index;

                  for (int step = 0; step < remaining; step++) {
                    AlmostFellFromGraceAdvancement.this.incrementProgression(player);
                  }
                } catch (Exception ignored) {
                  // Failure is non-fatal; normal event and lifecycle processing continues.
                }
              } else if (count >= 30000 && index < 30000) {
                try {
                  int ticks = 30000 - index;

                  for (int durationTicks = 0; durationTicks < ticks; durationTicks++) {
                    AlmostFellFromGraceAdvancement.this.incrementProgression(player);
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
