package cerberooz.cerberooz.BlissUltimate;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

final class ScheduledWorkItem implements BukkitTask, Comparable<ScheduledWorkItem> {
  private final Plugin owner;
  private final Runnable runnable;
  private final long periodTicks;
  private final long sequence;

  private long nextRunTick;
  private volatile boolean cancelled;

  ScheduledWorkItem(
      Plugin owner, Runnable runnable, long periodTicks, long nextRunTick, long sequence) {
    this.owner = owner;
    this.runnable = runnable;
    this.periodTicks = periodTicks;
    this.nextRunTick = nextRunTick;
    this.sequence = sequence;
  }

  Runnable runnable() {
    return this.runnable;
  }

  long periodTicks() {
    return this.periodTicks;
  }

  long nextRunTick() {
    return this.nextRunTick;
  }

  void scheduleNextRun(long tick) {
    this.nextRunTick = tick;
  }

  boolean markCancelled() {
    if (this.cancelled) {
      return false;
    }
    this.cancelled = true;
    return true;
  }

  @Override
  public int getTaskId() {
    return System.identityHashCode(this);
  }

  @Override
  public Plugin getOwner() {
    return this.owner;
  }

  @Override
  public boolean isSync() {
    return true;
  }

  @Override
  public void cancel() {
    SharedScheduler.cancel(this);
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public int compareTo(ScheduledWorkItem other) {
    int byTick = Long.compare(this.nextRunTick, other.nextRunTick);
    return byTick != 0 ? byTick : Long.compare(this.sequence, other.sequence);
  }
}
