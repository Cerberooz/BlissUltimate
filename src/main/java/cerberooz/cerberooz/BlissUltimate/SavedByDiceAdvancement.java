package cerberooz.cerberooz.BlissUltimate;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Range;

public class SavedByDiceAdvancement extends BaseAdvancement {
  final Bliss plugin;
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();

  public SavedByDiceAdvancement(
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
    SharedScheduler.scheduleRepeating(
        this.plugin,
        new BukkitRunnable() {
          @Override
          public void run() {
            Collection<? extends Player> collection = Bukkit.getOnlinePlayers();
            if (!collection.isEmpty()) {
              for (Player player : collection) {
                UUID playerId = player.getUniqueId();

                try {
                  if (SavedByDiceAdvancement.this.isGranted(player)) {
                    continue;
                  }
                } catch (Exception exception) {
                  continue;
                }

                if ((AstraTier1Gem.isAbilityAllowed(player) || AstraTier2Gem.canUseAbility(player))
                    && !SavedByDiceAdvancement.this.playerIds.contains(playerId)) {
                  SavedByDiceAdvancement.this.playerIds.add(playerId);

                  try {
                    SavedByDiceAdvancement.this.incrementProgression(player);
                  } catch (Exception ignored) {
                    // Failure is non-fatal; normal event and lifecycle processing continues.
                  }

                  Bukkit.getScheduler().runTaskLater(SavedByDiceAdvancement.this.plugin, () -> {
                        SavedByDiceAdvancement.this.playerIds.remove(playerId);

                      }, 40L);
                }
              }
            }
          }
        },
        SharedScheduler.staggeredInitialDelay(30L),
        30L);
  }
}
