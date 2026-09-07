package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class BossBarService {
  public static final List<BossBar> ACTIVE_BARS = new ArrayList<>();

  private static final Map<BossBar, Countdown> COUNTDOWNS = new IdentityHashMap<>();
  private static BukkitTask countdownTicker;

  private BossBarService() {}

  public static void showCountdown(
      Player player, String title, int durationSeconds, ChatColor color) {
    removeMatchingBars(player, title);
    KeyedBossBar bar =
        Bukkit.createBossBar(
            BossBarKey.KEY,
            color + ChatColor.BOLD.toString() + title,
            BarColor.PINK,
            BarStyle.SOLID);
    bar.setProgress(1.0);
    bar.addPlayer(player);
    ACTIVE_BARS.add(bar);
    startCountdown(player, bar, durationSeconds, 1.0);
  }

  public static void showCountdown(
      Player player, String title, int durationSeconds, ChatColor color, BarColor barColor) {
    removeMatchingBars(player, title);
    KeyedBossBar bar =
        Bukkit.createBossBar(
            BossBarKey.KEY, color + title, barColor, BarStyle.SOLID);
    bar.setProgress(1.0);
    bar.addPlayer(player);
    ACTIVE_BARS.add(bar);
    startCountdown(player, bar, durationSeconds, 1.0);
  }

  public static void showCountdown(
      Player player, String title, Integer durationSeconds, ChatColor color, BarColor barColor) {
    double initialProgress = removeParryBar(player);
    KeyedBossBar bar =
        Bukkit.createBossBar(
            BossBarKey.KEY,
            color + ChatColor.BOLD.toString() + title,
            barColor,
            BarStyle.SOLID);
    bar.setProgress(initialProgress);
    bar.addPlayer(player);
    ACTIVE_BARS.add(bar);
    startCountdown(player, bar, durationSeconds, initialProgress);
  }

  public static BossBar findByTitle(String title) {
    String normalizedTitle = normalizeTitle(title);
    for (BossBar bar : ACTIVE_BARS) {
      if (normalizeTitle(bar.getTitle()).equalsIgnoreCase(normalizedTitle)) {
        return bar;
      }
    }
    return null;
  }

  public static void removeByTitle(String title) {
    BossBar bar = findByTitle(title);
    if (bar != null) {
      removeBar(bar);
    }
  }

  public static void update(Player player, String title, double progress, BarColor color) {
    BossBar bar = findByTitle(title);
    double clampedProgress = Math.min(1.0, Math.max(0.0, progress));
    if (bar == null) {
      bar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
      bar.setProgress(clampedProgress);
      bar.addPlayer(player);
      ACTIVE_BARS.add(bar);
      return;
    }

    if (bar.getProgress() != clampedProgress) {
      bar.setProgress(clampedProgress);
    }
    if (bar.getColor() != color) {
      bar.setColor(color);
    }
    if (!bar.getPlayers().contains(player)) {
      bar.addPlayer(player);
    }
  }

  public static void remove(Player player, String title) {
    String normalizedTitle = normalizeTitle(title);
    Iterator<BossBar> iterator = ACTIVE_BARS.iterator();
    while (iterator.hasNext()) {
      BossBar bar = iterator.next();
      if (normalizeTitle(bar.getTitle()).equalsIgnoreCase(normalizedTitle)) {
        disposeBar(bar);
        iterator.remove();
        COUNTDOWNS.remove(bar);
      }
    }
    stopTickerIfIdle();
  }

  public static void removePlayer(Player player) {
    Iterator<BossBar> iterator = ACTIVE_BARS.iterator();
    while (iterator.hasNext()) {
      BossBar bar = iterator.next();
      if (bar.getPlayers().contains(player)) {
        bar.removePlayer(player);
        if (bar.getPlayers().isEmpty()) {
          disposeBar(bar);
          iterator.remove();
          COUNTDOWNS.remove(bar);
        }
      }
    }
    stopTickerIfIdle();
  }

  public static void clearAll() {
    if (countdownTicker != null) {
      countdownTicker.cancel();
      countdownTicker = null;
    }
    COUNTDOWNS.clear();
    for (BossBar bar : ACTIVE_BARS) {
      disposeBar(bar);
    }
    ACTIVE_BARS.clear();
  }

  private static void removeMatchingBars(Player player, String title) {
    String normalizedTitle = normalizeTitle(title);
    Iterator<BossBar> iterator = ACTIVE_BARS.iterator();
    while (iterator.hasNext()) {
      BossBar bar = iterator.next();
      if (normalizeTitle(bar.getTitle()).equalsIgnoreCase(normalizedTitle)) {
        disposeBar(bar);
        iterator.remove();
        COUNTDOWNS.remove(bar);
      }
    }
    stopTickerIfIdle();
  }

  private static double removeParryBar(Player player) {
    double initialProgress = 1.0;
    if (!CooldownService.isOnCooldown(player.getUniqueId(), "active_parry")) {
      return initialProgress;
    }

    Iterator<BossBar> iterator = ACTIVE_BARS.iterator();
    while (iterator.hasNext()) {
      BossBar bar = iterator.next();
      if (bar.getTitle().contains("Parry")) {
        initialProgress = bar.getProgress();
        disposeBar(bar);
        iterator.remove();
        COUNTDOWNS.remove(bar);
      }
    }
    stopTickerIfIdle();
    return initialProgress;
  }

  private static void startCountdown(
      Player player, BossBar bar, int durationSeconds, double initialProgress) {
    COUNTDOWNS.put(bar, new Countdown(player, bar, durationSeconds * 20, initialProgress));
    if (countdownTicker == null || countdownTicker.isCancelled()) {
      countdownTicker =
          SharedScheduler.scheduleRepeating(
              Bliss.getInstance(), BossBarService::tickCountdowns, 1L, 1L);
    }
  }

  private static void tickCountdowns() {
    Iterator<Countdown> iterator = COUNTDOWNS.values().iterator();
    while (iterator.hasNext()) {
      Countdown countdown = iterator.next();
      if (!countdown.player().isOnline()) {
        disposeBar(countdown.bar());
        ACTIVE_BARS.remove(countdown.bar());
        iterator.remove();
        continue;
      }

      int elapsedTicks = countdown.incrementAndGetElapsedTicks();
      int durationTicks = countdown.durationTicks();
      double progress =
          durationTicks == 0
              ? 0.0
              : countdown.initialProgress() - (double) elapsedTicks / durationTicks;
      countdown.bar().setProgress(Math.max(0.0, progress));
      if (elapsedTicks >= durationTicks) {
        disposeBar(countdown.bar());
        ACTIVE_BARS.remove(countdown.bar());
        iterator.remove();
      }
    }
    stopTickerIfIdle();
  }

  private static void removeBar(BossBar bar) {
    disposeBar(bar);
    ACTIVE_BARS.remove(bar);
    COUNTDOWNS.remove(bar);
    stopTickerIfIdle();
  }

  private static void stopTickerIfIdle() {
    if (COUNTDOWNS.isEmpty() && countdownTicker != null) {
      countdownTicker.cancel();
      countdownTicker = null;
    }
  }

  private static void disposeBar(BossBar bar) {
    bar.removeAll();
    if (bar instanceof KeyedBossBar keyedBossBar) {
      Bukkit.removeBossBar(keyedBossBar.getKey());
    }
  }

  private static String normalizeTitle(String title) {
    String stripped = ChatColor.stripColor(title);
    return stripped == null ? "" : stripped;
  }

  private static final class Countdown {
    private final Player player;
    private final BossBar bar;
    private final int durationTicks;
    private final double initialProgress;
    private int elapsedTicks;

    private Countdown(Player player, BossBar bar, int durationTicks, double initialProgress) {
      this.player = player;
      this.bar = bar;
      this.durationTicks = durationTicks;
      this.initialProgress = initialProgress;
    }

    Player player() {
      return this.player;
    }

    BossBar bar() {
      return this.bar;
    }

    int durationTicks() {
      return this.durationTicks;
    }

    double initialProgress() {
      return this.initialProgress;
    }

    int incrementAndGetElapsedTicks() {
      return ++this.elapsedTicks;
    }
  }
}
