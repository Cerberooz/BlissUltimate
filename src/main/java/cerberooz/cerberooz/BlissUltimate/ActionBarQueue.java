package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class ActionBarQueue {
  private static final Map<UUID, String> PENDING_MESSAGES = new ConcurrentHashMap<>();
  private static final Map<UUID, String> LAST_MESSAGES = new ConcurrentHashMap<>();
  private static final Map<UUID, Long> LAST_SENT_AT = new ConcurrentHashMap<>();
  private static final List<Runnable> RENDERERS = new CopyOnWriteArrayList<>();
  private static final LegacyComponentSerializer LEGACY_SERIALIZER =
      LegacyComponentSerializer.legacySection();
  private static final Cache<String, Component> COMPONENT_CACHE =
      CacheBuilder.newBuilder().maximumSize(512L).build();

  private static BukkitTask flushTask;
  private static Bliss plugin;
  private static long minimumIntervalMillis = 250L;

  private ActionBarQueue() {}

  public static void initialize(Bliss bliss) {
    if (flushTask != null && !flushTask.isCancelled()) {
      return;
    }

    plugin = bliss;
    minimumIntervalMillis =
        Math.max(
            100L, ConfigValueCache.getLong(bliss, "performance.actionbar_min_interval_ms", 250L));
    long flushPeriodTicks =
        Math.max(
            2L, ConfigValueCache.getLong(bliss, "performance.actionbar_flush_period_ticks", 10L));
    flushTask =
        SharedScheduler.scheduleRepeating(bliss, ActionBarQueue::flush, 1L, flushPeriodTicks);
  }

  public static void shutdown() {
    if (flushTask != null) {
      flushTask.cancel();
      flushTask = null;
    }

    PENDING_MESSAGES.clear();
    LAST_MESSAGES.clear();
    LAST_SENT_AT.clear();
    RENDERERS.clear();
    COMPONENT_CACHE.invalidateAll();
    plugin = null;
  }

  public static void enqueue(Player player, String message) {
    if (player != null && player.isOnline() && message != null && !message.isEmpty()) {
      PENDING_MESSAGES.put(player.getUniqueId(), message);
    }
  }

  public static void registerRenderer(Runnable renderer) {
    if (renderer != null && !RENDERERS.contains(renderer)) {
      RENDERERS.add(renderer);
    }
  }

  public static void unregisterRenderer(Runnable renderer) {
    RENDERERS.remove(renderer);
  }

  private static void flush() {
    runRenderers();
    if (PENDING_MESSAGES.isEmpty()) {
      removeOfflineEntries();
      return;
    }

    long now = System.currentTimeMillis();
    int processed = 0;
    int processingLimit = Math.max(100, Bukkit.getOnlinePlayers().size() * 4);
    Iterator<Map.Entry<UUID, String>> iterator = PENDING_MESSAGES.entrySet().iterator();

    while (iterator.hasNext() && processed++ < processingLimit) {
      Map.Entry<UUID, String> entry = iterator.next();
      UUID playerId = entry.getKey();
      String message = entry.getValue();
      if (!PENDING_MESSAGES.remove(playerId, message)) {
        continue;
      }

      Player player = Bukkit.getPlayer(playerId);
      if (player == null || !player.isOnline()) {
        LAST_MESSAGES.remove(playerId);
        LAST_SENT_AT.remove(playerId);
        continue;
      }

      String previousMessage = LAST_MESSAGES.get(playerId);
      long previousSendAt = LAST_SENT_AT.getOrDefault(playerId, 0L);
      if (message.equals(previousMessage) && now - previousSendAt < 1000L) {
        continue;
      }
      if (now - previousSendAt < minimumIntervalMillis) {
        PENDING_MESSAGES.putIfAbsent(playerId, message);
        continue;
      }

      Component component =
          COMPONENT_CACHE.asMap().computeIfAbsent(message, LEGACY_SERIALIZER::deserialize);
      player.sendActionBar(component);
      LAST_MESSAGES.put(playerId, message);
      LAST_SENT_AT.put(playerId, now);
    }
  }

  private static void runRenderers() {
    for (Runnable renderer : RENDERERS) {
      try {
        renderer.run();
      } catch (Throwable failure) {
        if (plugin != null) {
          plugin.getLogger().log(Level.WARNING, "An action-bar renderer failed", failure);
        }
      }
    }
  }

  private static void removeOfflineEntries() {
    for (UUID playerId : LAST_MESSAGES.keySet()) {
      if (Bukkit.getPlayer(playerId) == null) {
        removePlayer(playerId);
      }
    }
    for (UUID playerId : PENDING_MESSAGES.keySet()) {
      if (Bukkit.getPlayer(playerId) == null) {
        removePlayer(playerId);
      }
    }
  }

  private static void removePlayer(UUID playerId) {
    PENDING_MESSAGES.remove(playerId);
    LAST_MESSAGES.remove(playerId);
    LAST_SENT_AT.remove(playerId);
  }

  public static void removePlayer(Player player) {
    if (player != null) {
      removePlayer(player.getUniqueId());
    }
  }
}
