package cerberooz.cerberooz.BlissUltimate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitTask;

/** Persists active ability windows and their deferred cooldowns. */
public final class ActiveAbilityStore implements Listener {
  private static final Map<UUID, Map<String, Long>> ACTIVE_UNTIL = new ConcurrentHashMap<>();
  private static final Map<UUID, Map<String, Long>> PENDING_COOLDOWNS = new ConcurrentHashMap<>();
  private static final Object STATE_LOCK = new Object();
  private static final Object FILE_LOCK = new Object();

  private static Bliss plugin;
  private static File storeFile;
  private static BukkitTask scheduledFlush;
  private static boolean writeInFlight;
  private static String queuedSnapshot;
  private static long queuedGeneration;
  private static long generation;
  private static long highestWrittenGeneration;

  public static void initialize(Bliss bliss) {
    File file = new File(new File(bliss.getDataFolder(), "other"), "activePlayers.yml");
    try {
      Files.createDirectories(file.toPath().getParent());
      if (!file.exists()) {
        Files.createFile(file.toPath());
      }
    } catch (IOException failure) {
      bliss.getLogger().log(Level.WARNING, "Could not prepare activePlayers.yml", failure);
    }

    synchronized (STATE_LOCK) {
      if (scheduledFlush != null) {
        scheduledFlush.cancel();
      }
      plugin = bliss;
      storeFile = file;
      scheduledFlush = null;
      writeInFlight = false;
      queuedSnapshot = null;
      queuedGeneration = 0L;
      generation = 0L;
      highestWrittenGeneration = 0L;
      loadFromDiskLocked();
      markDirtyLocked();
    }
  }

  public static void startActive(
      UUID playerId, String ability, long activeSeconds, long pendingCooldownSeconds) {
    long now = System.currentTimeMillis();
    long activeDuration = Math.max(0L, activeSeconds);
    long cooldownDuration = Math.max(0L, pendingCooldownSeconds);
    synchronized (STATE_LOCK) {
      if (activeDuration > 0L) {
        ACTIVE_UNTIL
            .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
            .put(ability, now + activeDuration * 1000L);
      } else {
        removeEntry(ACTIVE_UNTIL, playerId, ability);
      }

      if (cooldownDuration > 0L) {
        PENDING_COOLDOWNS
            .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
            .put(ability, cooldownDuration);
      } else {
        removeEntry(PENDING_COOLDOWNS, playerId, ability);
      }
      markDirtyLocked();
    }
  }

  public static boolean isActive(UUID playerId, String ability) {
    Long activeUntil = ACTIVE_UNTIL.getOrDefault(playerId, Map.of()).get(ability);
    return activeUntil != null && activeUntil > System.currentTimeMillis();
  }

  public static long remainingSeconds(UUID playerId, String ability) {
    Long activeUntil = ACTIVE_UNTIL.getOrDefault(playerId, Map.of()).get(ability);
    if (activeUntil == null) {
      return 0L;
    }
    long remainingMillis = activeUntil - System.currentTimeMillis();
    return remainingMillis > 0L ? (long) Math.ceil(remainingMillis / 1000.0) : 0L;
  }

  /** Moves an expired ability's deferred cooldown into {@link CooldownService}. */
  public static void settleExpired(UUID playerId, String ability) {
    long now = System.currentTimeMillis();
    synchronized (STATE_LOCK) {
      Long activeUntil = ACTIVE_UNTIL.getOrDefault(playerId, Map.of()).get(ability);
      if (activeUntil != null && activeUntil > now) {
        return;
      }

      Long pendingSeconds = removeEntry(PENDING_COOLDOWNS, playerId, ability);
      if (pendingSeconds != null && pendingSeconds > 0L) {
        long cooldownEnd = (activeUntil != null ? activeUntil : now) + pendingSeconds * 1000L;
        long remainingMillis = cooldownEnd - now;
        if (remainingMillis > 0L) {
          CooldownService.setCooldown(
              playerId, ability, (long) Math.ceil(remainingMillis / 1000.0));
        }
      }

      removeEntry(ACTIVE_UNTIL, playerId, ability);
      markDirtyLocked();
    }
  }

  public static void clearPlayer(UUID playerId) {
    synchronized (STATE_LOCK) {
      ACTIVE_UNTIL.remove(playerId);
      PENDING_COOLDOWNS.remove(playerId);
      markDirtyLocked();
    }
  }

  public static void removeAbility(UUID playerId, String ability) {
    synchronized (STATE_LOCK) {
      removeEntry(ACTIVE_UNTIL, playerId, ability);
      removeEntry(PENDING_COOLDOWNS, playerId, ability);
      markDirtyLocked();
    }
  }

  public static void settlePlayer(UUID playerId) {
    Map<String, Long> activeAbilities = ACTIVE_UNTIL.get(playerId);
    if (activeAbilities == null || activeAbilities.isEmpty()) {
      return;
    }
    for (String ability : new ArrayList<>(activeAbilities.keySet())) {
      settleExpired(playerId, ability);
    }
  }

  public static void shutdown() {
    String snapshot;
    long snapshotGeneration;
    synchronized (STATE_LOCK) {
      if (storeFile == null) {
        return;
      }
      if (scheduledFlush != null) {
        scheduledFlush.cancel();
        scheduledFlush = null;
      }

      // Clear online active effects during shutdown. Offline entries remain in the snapshot so
      // their elapsed-time semantics survive.
      for (Player player : Bukkit.getOnlinePlayers()) {
        ACTIVE_UNTIL.remove(player.getUniqueId());
        PENDING_COOLDOWNS.remove(player.getUniqueId());
      }
      generation++;
      snapshot = buildSnapshotLocked();
      snapshotGeneration = generation;
      queuedSnapshot = null;
      queuedGeneration = 0L;
    }
    writeSnapshot(snapshot, snapshotGeneration);

    synchronized (STATE_LOCK) {
      ACTIVE_UNTIL.clear();
      PENDING_COOLDOWNS.clear();
      plugin = null;
      storeFile = null;
      writeInFlight = false;
      queuedSnapshot = null;
      queuedGeneration = 0L;
    }
  }

  private static <K, V> V removeEntry(Map<UUID, Map<K, V>> values, UUID playerId, K key) {
    Map<K, V> playerValues = values.get(playerId);
    if (playerValues == null) {
      return null;
    }
    V removed = playerValues.remove(key);
    if (playerValues.isEmpty()) {
      values.remove(playerId, playerValues);
    }
    return removed;
  }

  private static void markDirtyLocked() {
    generation++;
    if (plugin == null || storeFile == null || scheduledFlush != null || !plugin.isEnabled()) {
      return;
    }
    scheduledFlush =
        Bukkit.getScheduler().runTaskLater(plugin, ActiveAbilityStore::flushScheduled, 40L);
  }

  private static void flushScheduled() {
    String snapshot;
    long snapshotGeneration;
    synchronized (STATE_LOCK) {
      scheduledFlush = null;
      snapshot = buildSnapshotLocked();
      snapshotGeneration = generation;
    }
    queueAsyncWrite(snapshot, snapshotGeneration);
  }

  private static void queueAsyncWrite(String snapshot, long snapshotGeneration) {
    synchronized (STATE_LOCK) {
      if (writeInFlight) {
        queuedSnapshot = snapshot;
        queuedGeneration = snapshotGeneration;
        return;
      }
      writeInFlight = true;
    }

    try {
      Bukkit.getScheduler()
          .runTaskAsynchronously(plugin, () -> drainWriteQueue(snapshot, snapshotGeneration));
    } catch (RuntimeException schedulingFailure) {
      synchronized (STATE_LOCK) {
        writeInFlight = false;
      }
      writeSnapshot(snapshot, snapshotGeneration);
    }
  }

  private static void drainWriteQueue(String firstSnapshot, long firstGeneration) {
    String snapshot = firstSnapshot;
    long snapshotGeneration = firstGeneration;
    while (snapshot != null) {
      writeSnapshot(snapshot, snapshotGeneration);
      synchronized (STATE_LOCK) {
        snapshot = queuedSnapshot;
        snapshotGeneration = queuedGeneration;
        queuedSnapshot = null;
        queuedGeneration = 0L;
        if (snapshot == null) {
          writeInFlight = false;
        }
      }
    }
  }

  private static String buildSnapshotLocked() {
    YamlConfiguration data = new YamlConfiguration();
    for (Map.Entry<UUID, Map<String, Long>> playerEntry : ACTIVE_UNTIL.entrySet()) {
      UUID playerId = playerEntry.getKey();
      for (Map.Entry<String, Long> abilityEntry : playerEntry.getValue().entrySet()) {
        String ability = abilityEntry.getKey();
        String path = "players." + playerId + "." + encodeKey(ability);
        data.set(path + ".key", ability);
        data.set(path + ".activeUntil", abilityEntry.getValue());
        Long pendingSeconds = PENDING_COOLDOWNS.getOrDefault(playerId, Map.of()).get(ability);
        if (pendingSeconds != null && pendingSeconds > 0L) {
          data.set(path + ".pendingCooldownSeconds", pendingSeconds);
        }
      }
    }
    return data.saveToString();
  }

  private static void writeSnapshot(String snapshot, long snapshotGeneration) {
    File destination = storeFile;
    Bliss currentPlugin = plugin;
    if (destination == null) {
      return;
    }
    synchronized (FILE_LOCK) {
      if (snapshotGeneration <= highestWrittenGeneration) {
        return;
      }
      Path temporary = null;
      try {
        Path parent = destination.toPath().getParent();
        Files.createDirectories(parent);
        temporary = Files.createTempFile(parent, "activePlayers-", ".tmp");
        Files.writeString(temporary, snapshot, StandardCharsets.UTF_8);
        try {
          Files.move(
              temporary,
              destination.toPath(),
              StandardCopyOption.ATOMIC_MOVE,
              StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
          Files.move(temporary, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        highestWrittenGeneration = snapshotGeneration;
      } catch (IOException failure) {
        if (currentPlugin != null) {
          currentPlugin.getLogger().log(Level.WARNING, "Could not save activePlayers.yml", failure);
        }
      } finally {
        if (temporary != null) {
          try {
            Files.deleteIfExists(temporary);
          } catch (IOException ignored) {
            // The temporary file is harmless and will be replaced by a later save.
          }
        }
      }
    }
  }

  private static void loadFromDiskLocked() {
    ACTIVE_UNTIL.clear();
    PENDING_COOLDOWNS.clear();
    if (storeFile == null) {
      return;
    }

    YamlConfiguration data = YamlConfiguration.loadConfiguration(storeFile);
    ConfigurationSection players = data.getConfigurationSection("players");
    if (players == null) {
      return;
    }

    long now = System.currentTimeMillis();
    for (String playerKey : players.getKeys(false)) {
      UUID playerId;
      try {
        playerId = UUID.fromString(playerKey);
      } catch (IllegalArgumentException ignored) {
        continue;
      }

      ConfigurationSection abilities = players.getConfigurationSection(playerKey);
      if (abilities == null) {
        continue;
      }
      for (String encodedAbility : abilities.getKeys(false)) {
        String path = playerKey + "." + encodedAbility;
        String ability = players.getString(path + ".key");
        if (ability == null || ability.isBlank()) {
          ability = decodeKey(encodedAbility);
        }

        long activeUntil = players.getLong(path + ".activeUntil", 0L);
        long pendingSeconds = players.getLong(path + ".pendingCooldownSeconds", 0L);
        if (activeUntil > now) {
          ACTIVE_UNTIL
              .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
              .put(ability, activeUntil);
          if (pendingSeconds > 0L) {
            PENDING_COOLDOWNS
                .computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                .put(ability, pendingSeconds);
          }
        } else if (pendingSeconds > 0L) {
          long remainingMillis = activeUntil + pendingSeconds * 1000L - now;
          if (remainingMillis > 0L) {
            CooldownService.setCooldown(
                playerId, ability, (long) Math.ceil(remainingMillis / 1000.0));
          }
        }
      }
    }
  }

  private static String encodeKey(String value) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  private static String decodeKey(String value) {
    try {
      return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException ignored) {
      return value;
    }
  }

  @EventHandler
  public void onPluginDisable(PluginDisableEvent event) {
    if (event.getPlugin() != plugin) {
      return;
    }
    shutdown();
  }
}
