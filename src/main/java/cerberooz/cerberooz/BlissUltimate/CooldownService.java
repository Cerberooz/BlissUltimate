package cerberooz.cerberooz.BlissUltimate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class CooldownService {
  private static final Map<UUID, Map<String, Long>> EXPIRES_AT = new ConcurrentHashMap<>();
  private static final Map<UUID, Map<String, Long>> ORIGINAL_DURATIONS = new ConcurrentHashMap<>();
  private static BukkitTask cleanupTask;

  private CooldownService() {}

  public static void initialize(Plugin plugin) {
    if (cleanupTask != null && !cleanupTask.isCancelled()) {
      return;
    }
    cleanupTask =
        SharedScheduler.scheduleRepeating(plugin, CooldownService::purgeExpired, 12_000L, 12_000L);
  }

  public static boolean isOnCooldown(UUID playerId, String ability) {
    ActiveAbilityStore.settleExpired(playerId, ability);
    return ActiveAbilityStore.isActive(playerId, ability)
        || remainingMillis(playerId, ability) > 0L;
  }

  public static void setCooldown(UUID playerId, String ability, long durationSeconds) {
    EXPIRES_AT
        .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
        .put(ability, System.currentTimeMillis() + durationSeconds * 1000L);
    ORIGINAL_DURATIONS
        .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
        .put(ability, durationSeconds);
  }

  public static long originalDurationSeconds(UUID playerId, String ability) {
    return ORIGINAL_DURATIONS
        .getOrDefault(playerId, Collections.emptyMap())
        .getOrDefault(ability, 0L);
  }

  public static Integer remainingWholeSeconds(UUID playerId, String ability) {
    Map<String, Long> playerCooldowns = EXPIRES_AT.get(playerId);
    if (playerCooldowns == null) {
      return 0;
    }

    Long expiration = playerCooldowns.get(ability);
    if (expiration == null) {
      return 0;
    }

    long remainingMillis = expiration - System.currentTimeMillis();
    if (remainingMillis <= 0L) {
      removeExpired(playerId, ability, playerCooldowns, expiration);
      return 0;
    }
    return (int) (remainingMillis / 1000L);
  }

  public static long remainingMillis(UUID playerId, String ability) {
    Map<String, Long> playerCooldowns = EXPIRES_AT.get(playerId);
    if (playerCooldowns == null) {
      return 0L;
    }

    Long expiration = playerCooldowns.get(ability);
    if (expiration == null) {
      return 0L;
    }

    long remainingMillis = expiration - System.currentTimeMillis();
    if (remainingMillis <= 0L) {
      removeExpired(playerId, ability, playerCooldowns, expiration);
      return 0L;
    }
    return remainingMillis;
  }

  public static long remainingSeconds(UUID playerId, String ability) {
    return remainingMillis(playerId, ability) / 1000L;
  }

  public static long configuredDurationSeconds(UUID playerId, String ability) {
    return ORIGINAL_DURATIONS
        .getOrDefault(playerId, Collections.emptyMap())
        .getOrDefault(ability, 0L);
  }

  public static boolean hasActiveCooldown(UUID playerId) {
    Map<String, Long> playerCooldowns = EXPIRES_AT.get(playerId);
    if (playerCooldowns == null) {
      return false;
    }

    long now = System.currentTimeMillis();
    boolean active = false;
    for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
      if (entry.getValue() > now) {
        active = true;
      } else if (playerCooldowns.remove(entry.getKey(), entry.getValue())) {
        removeOriginalDuration(playerId, entry.getKey());
      }
    }
    if (playerCooldowns.isEmpty()) {
      EXPIRES_AT.remove(playerId, playerCooldowns);
    }
    return active;
  }

  public static void clearActiveCooldowns(UUID playerId) {
    EXPIRES_AT.remove(playerId);
  }

  public static void clearOriginalDurations(UUID playerId) {
    ORIGINAL_DURATIONS.remove(playerId);
  }

  public static void restartCooldowns(UUID playerId) {
    Map<String, Long> currentCooldowns = EXPIRES_AT.get(playerId);
    Map<String, Long> originalDurations = ORIGINAL_DURATIONS.get(playerId);
    if (currentCooldowns == null || originalDurations == null) {
      return;
    }

    for (String ability : currentCooldowns.keySet()) {
      Long duration = originalDurations.get(ability);
      if (duration != null && duration > 0L) {
        setCooldown(playerId, ability, duration);
      }
    }
  }

  public static void clearAll() {
    if (cleanupTask != null) {
      cleanupTask.cancel();
      cleanupTask = null;
    }
    EXPIRES_AT.clear();
    ORIGINAL_DURATIONS.clear();
  }

  private static void purgeExpired() {
    long now = System.currentTimeMillis();
    for (Map.Entry<UUID, Map<String, Long>> playerEntry : EXPIRES_AT.entrySet()) {
      UUID playerId = playerEntry.getKey();
      Map<String, Long> playerCooldowns = playerEntry.getValue();
      for (Map.Entry<String, Long> cooldownEntry : playerCooldowns.entrySet()) {
        if (cooldownEntry.getValue() <= now
            && playerCooldowns.remove(cooldownEntry.getKey(), cooldownEntry.getValue())) {
          removeOriginalDuration(playerId, cooldownEntry.getKey());
        }
      }
      if (playerCooldowns.isEmpty()) {
        EXPIRES_AT.remove(playerId, playerCooldowns);
      }
    }
  }

  private static void removeExpired(
      UUID playerId, String ability, Map<String, Long> playerCooldowns, Long expectedExpiration) {
    if (playerCooldowns.remove(ability, expectedExpiration)) {
      removeOriginalDuration(playerId, ability);
    }
    if (playerCooldowns.isEmpty()) {
      EXPIRES_AT.remove(playerId, playerCooldowns);
    }
  }

  private static void removeOriginalDuration(UUID playerId, String ability) {
    Map<String, Long> durations = ORIGINAL_DURATIONS.get(playerId);
    if (durations == null) {
      return;
    }
    durations.remove(ability);
    if (durations.isEmpty()) {
      ORIGINAL_DURATIONS.remove(playerId, durations);
    }
  }
}
