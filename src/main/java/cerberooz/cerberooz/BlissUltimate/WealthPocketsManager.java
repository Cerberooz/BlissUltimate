package cerberooz.cerberooz.BlissUltimate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/** Owns the persistent nine-slot inventory used by the Wealth pocket abilities. */
public final class WealthPocketsManager {
  private static final long CACHE_IDLE_MILLIS = 600_000L;
  private static final Object FILE_LOCK = new Object();

  private static WealthPocketsManager instance;

  private final JavaPlugin plugin;
  private final File dataFile;
  private final YamlConfiguration data;
  private final Map<UUID, Inventory> loadedPockets = new ConcurrentHashMap<>();
  private final Map<UUID, Long> dirtyVersions = new ConcurrentHashMap<>();
  private final Map<UUID, Long> lastAccess = new ConcurrentHashMap<>();
  private final Map<UUID, Long> accessTokens = new ConcurrentHashMap<>();
  private final AtomicLong sequence = new AtomicLong();
  private final Object queueLock = new Object();
  private final Map<UUID, PocketSnapshot> pendingSaves = new HashMap<>();

  private BukkitTask autoSaveTask;
  private BukkitTask cleanupTask;
  private boolean saveWorkerRunning;
  private volatile boolean shuttingDown;

  private record PocketSnapshot(
      String contents, long dirtyVersion, long accessToken, boolean unload, boolean autoSave) {
    PocketSnapshot merge(PocketSnapshot newer) {
      PocketSnapshot selected = newer.dirtyVersion >= this.dirtyVersion ? newer : this;
      return new PocketSnapshot(
          selected.contents,
          selected.dirtyVersion,
          selected.accessToken,
          this.unload || newer.unload,
          this.autoSave || newer.autoSave);
    }
  }

  private WealthPocketsManager(JavaPlugin plugin) {
    this.plugin = plugin;
    if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
      plugin.getLogger().warning("Could not create the BlissSMP data directory.");
    }
    this.dataFile = new File(plugin.getDataFolder(), "wealth_pockets.yml");
    try {
      if (!this.dataFile.exists()) {
        Files.createFile(this.dataFile.toPath());
      }
    } catch (IOException failure) {
      plugin.getLogger().log(Level.SEVERE, "Could not create wealth_pockets.yml!", failure);
    }
    this.data = YamlConfiguration.loadConfiguration(this.dataFile);
    this.startTasks();
  }

  public static synchronized WealthPocketsManager getInstance(JavaPlugin plugin) {
    if (instance == null) {
      instance = new WealthPocketsManager(plugin);
    }
    return instance;
  }

  public Inventory getPocket(UUID playerId) {
    this.touch(playerId);
    Inventory pocket = this.loadedPockets.get(playerId);
    if (pocket != null) {
      return pocket;
    }
    pocket = this.loadPocket(playerId);
    if (pocket == null) {
      pocket = Bukkit.createInventory(null, InventoryType.DROPPER, "Pockets");
    }
    Inventory existing = this.loadedPockets.putIfAbsent(playerId, pocket);
    return existing != null ? existing : pocket;
  }

  public void markDirty(UUID playerId) {
    this.dirtyVersions.put(playerId, this.sequence.incrementAndGet());
  }

  public boolean isLoaded(UUID playerId) {
    return this.loadedPockets.containsKey(playerId);
  }

  public boolean hasStoredPocket(UUID playerId) {
    synchronized (FILE_LOCK) {
      return this.data.contains(path(playerId));
    }
  }

  public static Map<UUID, Inventory> loadedPockets() {
    return instance != null ? instance.loadedPockets : new HashMap<>();
  }

  public void preload(UUID playerId) {
    this.touch(playerId);
    if (this.loadedPockets.containsKey(playerId)) {
      return;
    }
    Inventory pocket = this.loadPocket(playerId);
    if (pocket != null) {
      this.loadedPockets.putIfAbsent(playerId, pocket);
    }
  }

  public void saveAndUnload(UUID playerId) {
    if (!this.loadedPockets.containsKey(playerId) && !this.dirtyVersions.containsKey(playerId)) {
      return;
    }
    this.queueSave(playerId, true, false);
  }

  public void saveDirtyPockets() {
    if (this.dirtyVersions.isEmpty() || this.shuttingDown) {
      return;
    }
    for (UUID playerId : new ArrayList<>(this.dirtyVersions.keySet())) {
      this.queueSave(playerId, false, true);
    }
  }

  /** Saves every loaded pocket synchronously. This is only used during lifecycle shutdown. */
  public void saveAllAndClear() {
    this.plugin.getLogger().info("Saving all wealth pockets data...");
    this.shuttingDown = true;
    this.cancelTasks();

    Map<UUID, String> snapshots = new HashMap<>();
    for (Map.Entry<UUID, Inventory> entry : this.loadedPockets.entrySet()) {
      snapshots.put(entry.getKey(), this.serializeInventory(entry.getValue()));
    }

    synchronized (this.queueLock) {
      this.pendingSaves.clear();
    }
    synchronized (FILE_LOCK) {
      for (Map.Entry<UUID, String> entry : snapshots.entrySet()) {
        if (entry.getValue() != null) {
          this.data.set(path(entry.getKey()), entry.getValue());
        }
      }
      try {
        this.writeDataLocked();
        this.plugin
            .getLogger()
            .info("Successfully saved " + this.loadedPockets.size() + " wealth pockets");
      } catch (IOException failure) {
        this.plugin
            .getLogger()
            .log(Level.SEVERE, "Could not save wealth pockets file on shutdown!", failure);
      }
    }

    this.loadedPockets.clear();
    this.dirtyVersions.clear();
    this.lastAccess.clear();
    this.accessTokens.clear();
  }

  public void shutdown() {
    this.saveAllAndClear();
    synchronized (WealthPocketsManager.class) {
      if (instance == this) {
        instance = null;
      }
    }
  }

  private void startTasks() {
    this.autoSaveTask =
        SharedScheduler.scheduleRepeating(this.plugin, this::saveDirtyPockets, 18_000L, 18_000L);
    this.cleanupTask =
        SharedScheduler.scheduleRepeating(this.plugin, this::cleanIdleCache, 6_000L, 6_000L);
  }

  private void cancelTasks() {
    if (this.autoSaveTask != null) {
      this.autoSaveTask.cancel();
      this.autoSaveTask = null;
    }
    if (this.cleanupTask != null) {
      this.cleanupTask.cancel();
      this.cleanupTask = null;
    }
  }

  private void cleanIdleCache() {
    long now = System.currentTimeMillis();
    ArrayList<UUID> removable = new ArrayList<>();
    for (Map.Entry<UUID, Long> entry : this.lastAccess.entrySet()) {
      UUID playerId = entry.getKey();
      if (Bukkit.getPlayer(playerId) != null || now - entry.getValue() <= CACHE_IDLE_MILLIS) {
        continue;
      }
      if (this.dirtyVersions.containsKey(playerId)) {
        this.saveAndUnload(playerId);
      } else {
        removable.add(playerId);
      }
    }

    for (UUID playerId : removable) {
      this.loadedPockets.remove(playerId);
      this.lastAccess.remove(playerId);
      this.accessTokens.remove(playerId);
      this.dirtyVersions.remove(playerId);
    }
    if (!removable.isEmpty()) {
      this.plugin.getLogger().info("Cleaned " + removable.size() + " inactive pockets from cache");
    }
  }

  private void queueSave(UUID playerId, boolean unload, boolean autoSave) {
    if (this.shuttingDown) {
      return;
    }
    Inventory pocket = this.loadedPockets.get(playerId);
    String contents = pocket == null ? null : this.serializeInventory(pocket);
    PocketSnapshot snapshot =
        new PocketSnapshot(
            contents,
            this.dirtyVersions.getOrDefault(playerId, 0L),
            this.accessTokens.getOrDefault(playerId, 0L),
            unload,
            autoSave);

    boolean startWorker = false;
    synchronized (this.queueLock) {
      this.pendingSaves.merge(playerId, snapshot, PocketSnapshot::merge);
      if (!this.saveWorkerRunning) {
        this.saveWorkerRunning = true;
        startWorker = true;
      }
    }
    if (startWorker) {
      try {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, this::drainSaveQueue);
      } catch (RuntimeException schedulingFailure) {
        synchronized (this.queueLock) {
          this.saveWorkerRunning = false;
        }
        this.plugin
            .getLogger()
            .log(Level.WARNING, "Could not schedule a wealth pocket save", schedulingFailure);
      }
    }
  }

  private void drainSaveQueue() {
    boolean draining = true;
    while (draining) {
      Map<UUID, PocketSnapshot> batch = null;
      synchronized (this.queueLock) {
        if (this.shuttingDown) {
          this.pendingSaves.clear();
          this.saveWorkerRunning = false;
          draining = false;
        } else if (this.pendingSaves.isEmpty()) {
          this.saveWorkerRunning = false;
          draining = false;
        } else {
          batch = new HashMap<>(this.pendingSaves);
          this.pendingSaves.clear();
        }
      }
      if (batch != null) {
        this.persistBatch(batch);
      }
    }
  }

  private void persistBatch(Map<UUID, PocketSnapshot> batch) {
    int saved = 0;
    boolean succeeded = false;
    synchronized (FILE_LOCK) {
      if (this.shuttingDown) {
        return;
      }
      for (Map.Entry<UUID, PocketSnapshot> entry : batch.entrySet()) {
        if (entry.getValue().contents() != null) {
          this.data.set(path(entry.getKey()), entry.getValue().contents());
          saved++;
        }
      }
      try {
        if (saved > 0) {
          this.writeDataLocked();
        }
        succeeded = true;
      } catch (IOException failure) {
        this.plugin.getLogger().log(Level.SEVERE, "Could not save wealth pockets file!", failure);
      }
    }
    if (!succeeded) {
      return;
    }

    int autoSaved = 0;
    for (Map.Entry<UUID, PocketSnapshot> entry : batch.entrySet()) {
      UUID playerId = entry.getKey();
      PocketSnapshot snapshot = entry.getValue();
      if (snapshot.dirtyVersion() > 0L
          && this.dirtyVersions.remove(playerId, snapshot.dirtyVersion())
          && snapshot.autoSave()) {
        autoSaved++;
      }
      if (snapshot.unload()
          && this.accessTokens.getOrDefault(playerId, 0L) == snapshot.accessToken()
          && this.dirtyVersions.getOrDefault(playerId, 0L) <= snapshot.dirtyVersion()) {
        this.loadedPockets.remove(playerId);
        this.lastAccess.remove(playerId);
        this.accessTokens.remove(playerId);
        this.dirtyVersions.remove(playerId);
      }
    }
    if (autoSaved > 0) {
      this.plugin.getLogger().info("Auto-saved " + autoSaved + " modified wealth pockets");
    }
  }

  private Inventory loadPocket(UUID playerId) {
    try {
      String encoded;
      synchronized (FILE_LOCK) {
        encoded = this.data.getString(path(playerId));
      }
      return encoded == null || encoded.isEmpty() ? null : this.deserializeInventory(encoded);
    } catch (Exception failure) {
      this.plugin.getLogger().log(Level.WARNING, "Could not load pockets for " + playerId, failure);
      return null;
    }
  }

  private String serializeInventory(Inventory inventory) {
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BukkitObjectOutputStream output = new BukkitObjectOutputStream(bytes)) {
      output.writeInt(inventory.getSize());
      for (int slot = 0; slot < inventory.getSize(); slot++) {
        output.writeObject(inventory.getItem(slot));
      }
      output.flush();
      return Base64.getEncoder().encodeToString(bytes.toByteArray());
    } catch (Exception failure) {
      this.plugin.getLogger().log(Level.WARNING, "Could not serialize inventory", failure);
      return "";
    }
  }

  private Inventory deserializeInventory(String encoded) {
    try (ByteArrayInputStream bytes =
            new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
        BukkitObjectInputStream input = new BukkitObjectInputStream(bytes)) {
      int size = input.readInt();
      Inventory inventory = Bukkit.createInventory(null, InventoryType.DROPPER, "Pockets");
      for (int slot = 0; slot < size; slot++) {
        ItemStack item = (ItemStack) input.readObject();
        if (item != null) {
          inventory.setItem(slot, item);
        }
      }
      return inventory;
    } catch (Exception failure) {
      this.plugin.getLogger().log(Level.WARNING, "Could not deserialize inventory", failure);
      return null;
    }
  }

  private void touch(UUID playerId) {
    this.lastAccess.put(playerId, System.currentTimeMillis());
    this.accessTokens.put(playerId, this.sequence.incrementAndGet());
  }

  private void writeDataLocked() throws IOException {
    Path destination = this.dataFile.toPath();
    Path temporary = Files.createTempFile(destination.getParent(), "wealth-pockets-", ".tmp");
    try {
      Files.writeString(temporary, this.data.saveToString(), StandardCharsets.UTF_8);
      try {
        Files.move(
            temporary,
            destination,
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException ignored) {
        Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
      }
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  private static String path(UUID playerId) {
    return "pockets." + playerId;
  }
}
