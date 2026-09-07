package cerberooz.cerberooz.BlissUltimate;

import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Runs short-lived repeating effects through one event-owned Bukkit task. The ticker stops as soon
 * as the last consumer is cancelled.
 */
public final class SharedScheduler {
  private static final AtomicInteger STAGGER_COUNTER = new AtomicInteger();
  private static final PriorityQueue<ScheduledWorkItem> TASKS = new PriorityQueue<>();

  private static BukkitTask ticker;
  private static long currentTick;
  private static long sequence;

  private SharedScheduler() {}

  public static long staggeredInitialDelay(long periodTicks) {
    if (periodTicks <= 1L) {
      return 1L;
    }

    int offset = STAGGER_COUNTER.getAndIncrement();
    return 1L + Math.floorMod(offset * 3, (int) Math.min(periodTicks, 100L));
  }

  public static synchronized BukkitTask scheduleRepeating(
      Plugin plugin, Runnable runnable, long initialDelayTicks, long periodTicks) {
    if (plugin == null || runnable == null) {
      throw new IllegalArgumentException("plugin and runnable are required");
    }

    long firstRunTick = currentTick + Math.max(1L, initialDelayTicks);
    ScheduledWorkItem task =
        new ScheduledWorkItem(
            plugin, runnable, Math.max(1L, periodTicks), firstRunTick, sequence++);
    TASKS.add(task);
    ensureTicker(plugin);
    return task;
  }

  public static BukkitTask scheduleRepeating(
      Runnable runnable, Plugin plugin, long initialDelayTicks, long periodTicks) {
    return scheduleRepeating(plugin, runnable, initialDelayTicks, periodTicks);
  }

  public static synchronized void shutdown() {
    if (ticker != null) {
      ticker.cancel();
      ticker = null;
    }

    for (ScheduledWorkItem task : TASKS) {
      task.markCancelled();
    }
    TASKS.clear();
    currentTick = 0L;
    sequence = 0L;
    STAGGER_COUNTER.set(0);
  }

  static synchronized void cancel(ScheduledWorkItem task) {
    if (!task.markCancelled()) {
      return;
    }

    TASKS.remove(task);
    stopTickerIfIdle();
  }

  private static void ensureTicker(Plugin plugin) {
    if (ticker == null || ticker.isCancelled()) {
      ticker = Bukkit.getScheduler().runTaskTimer(plugin, SharedScheduler::tick, 1L, 1L);
    }
  }

  private static synchronized void tick() {
    currentTick++;

    while (!TASKS.isEmpty() && TASKS.peek().nextRunTick() <= currentTick) {
      ScheduledWorkItem task = TASKS.poll();
      if (task.isCancelled()) {
        continue;
      }

      try {
        task.runnable().run();
      } catch (Throwable failure) {
        task.getOwner().getLogger().log(Level.WARNING, "A shared scheduled task failed", failure);
      }

      if (!task.isCancelled()) {
        task.scheduleNextRun(currentTick + task.periodTicks());
        TASKS.add(task);
      }
    }

    stopTickerIfIdle();
  }

  private static void stopTickerIfIdle() {
    if (TASKS.isEmpty() && ticker != null) {
      ticker.cancel();
      ticker = null;
    }
  }
}
