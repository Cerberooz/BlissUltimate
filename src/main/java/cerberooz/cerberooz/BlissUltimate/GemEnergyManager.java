package cerberooz.cerberooz.BlissUltimate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GemEnergyManager implements Listener {
  static final Logger logger;
  final int durationTicks = 10;
  static final HashMap<UUID, Integer> energyByPlayer;
  static final Set<UUID> dirtyPlayers;
  static final Map<UUID, String> lastKnownNamesByPlayer;
  static final Object saveLock;
  static BukkitTask scheduledTask;
  static BukkitTask saveCompletionMonitor;
  static volatile boolean saveInProgress;
  static boolean saveRequested;
  static volatile int saveGeneration;
  static Bliss plugin;
  static File energyDataFile;
  static FileConfiguration energyConfig;
  static final Map<Integer, String> tierOneTypesByModelData;
  static final Map<Integer, String> tierTwoTypesByModelData;
  static final List<GemItemDefinition> tierOneGemDefinitions;
  static final List<GemItemDefinition> tierTwoGemDefinitions;
  final Map<UUID, ItemStack> itemsByPlayer = new HashMap<>();
  final Map<UUID, String> secondaryValuesByPlayer = new HashMap<>();
  final Set<UUID> playerIds = new HashSet<>();

  public GemEnergyManager(Bliss bliss) {
    plugin = bliss;
    this.initialize();
    this.refreshAbilityState();
  }

  void initialize() {
    if (!plugin.getDataFolder().exists()) {
      plugin.getDataFolder().mkdirs();
    }

    energyDataFile = new File(plugin.getDataFolder(), "energy.yml");
    if (!energyDataFile.exists()) {
      try {
        energyDataFile.createNewFile();
      } catch (IOException exception) {
        plugin.getLogger().severe("Error while creating energy.yml: " + exception.getMessage());
      }
    }

    energyConfig = YamlConfiguration.loadConfiguration(energyDataFile);
  }

  public void refreshAbilityState() {
    if (energyConfig != null) {
      for (String text : energyConfig.getKeys(false)) {
        try {
          UUID playerId = UUID.fromString(text);
          int configuredValue = energyConfig.getInt(text + ".energy", 5);
          String configuredText = energyConfig.getString(text + ".lastKnownName");
          energyByPlayer.put(playerId, Math.min(configuredValue, 10));
          if (configuredText != null && !configuredText.isBlank()) {
            lastKnownNamesByPlayer.put(playerId, configuredText);
          }
        } catch (IllegalArgumentException illegalArgumentException) {
          plugin.getLogger().warning("Invalid UUID in Energy.yml: " + text);
        }
      }
    }
  }

  public static void updateAbilityState() {
    saveRequested = true;
    if (scheduledTask != null) {
      scheduledTask.cancel();
      scheduledTask = null;
    }

    updateState(1L);
  }

  public static void cleanup() {
    if (energyConfig != null && energyDataFile != null) {
      if (scheduledTask != null) {
        scheduledTask.cancel();
        scheduledTask = null;
      }
      if (saveCompletionMonitor != null) {
        saveCompletionMonitor.cancel();
        saveCompletionMonitor = null;
      }

      dirtyPlayers.clear();
      saveRequested = false;
      EnergySnapshot snapshot = resolveEnergySnapshot();
      saveEnergySnapshot(serializeEnergySnapshot(snapshot), ++saveGeneration, true);
      saveInProgress = false;
      energyByPlayer.clear();
      lastKnownNamesByPlayer.clear();
      energyConfig = null;
      energyDataFile = null;
      plugin = null;
    }
  }

  static void updateState(long timestamp) {
    if (plugin != null && energyConfig != null && energyDataFile != null) {
      if (scheduledTask == null || scheduledTask.isCancelled()) {
        scheduledTask = Bukkit.getScheduler().runTaskLater(plugin, () -> applyAbilityEffects(), timestamp);
      }
    }
  }

  static void applyAbilityEffects() {
    scheduledTask = null;
    boolean forceSaveRequested = saveRequested;
    if (!saveInProgress) {
      if (!dirtyPlayers.isEmpty() || forceSaveRequested) {
        EnergySnapshot energySnapshot = resolveEnergySnapshot();
        dirtyPlayers.clear();
        saveRequested = false;
        saveInProgress = true;
        int count = ++saveGeneration;
        String serializedSnapshot = serializeEnergySnapshot(energySnapshot);
        monitorAsyncSaveCompletion();
        Bukkit.getScheduler()
            .runTaskAsynchronously(
                plugin,
                () -> {
                  try {
                    saveEnergySnapshot(serializedSnapshot, count, false);
                  } finally {
                    saveInProgress = false;
                  }
                });
      }
    } else {
      saveRequested = saveRequested || forceSaveRequested;
      updateState(20L);
    }
  }

  static EnergySnapshot resolveEnergySnapshot() {
    HashMap<UUID, Integer> energySnapshotByPlayer = new HashMap<>(energyByPlayer);
    HashMap<UUID, String> namesByPlayer = new HashMap<>(lastKnownNamesByPlayer);

    for (Player player : Bukkit.getOnlinePlayers()) {
      namesByPlayer.put(player.getUniqueId(), player.getName());
    }

    return new EnergySnapshot(energySnapshotByPlayer, namesByPlayer);
  }

  private static String serializeEnergySnapshot(EnergySnapshot snapshot) {
    YamlConfiguration yaml = new YamlConfiguration();

    for (Entry<UUID, Integer> entry : snapshot.energies().entrySet()) {
      String playerKey = entry.getKey().toString();
      yaml.set(playerKey + ".energy", entry.getValue());
      String lastKnownName = snapshot.lastKnownNames().get(entry.getKey());
      if (lastKnownName != null && !lastKnownName.isBlank()) {
        yaml.set(playerKey + ".lastKnownName", lastKnownName);
      }
    }
    return yaml.saveToString();
  }

  private static void saveEnergySnapshot(String snapshot, int generation, boolean synchronous) {
    try {
      synchronized (saveLock) {
        if (!synchronous && generation < saveGeneration) {
          return;
        }
        Path energyPath = energyDataFile.toPath();
        Path temporaryFile = energyPath.resolveSibling(energyPath.getFileName() + ".tmp");
        Files.writeString(
            temporaryFile,
            snapshot,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE);
        try {
          Files.move(
              temporaryFile,
              energyPath,
              StandardCopyOption.ATOMIC_MOVE,
              StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
          Files.move(temporaryFile, energyPath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    } catch (IOException failure) {
      plugin.getLogger().severe("Cannot save energy.yml: " + failure.getMessage());
    }
  }

  private static void monitorAsyncSaveCompletion() {
    if (saveCompletionMonitor != null && !saveCompletionMonitor.isCancelled()) {
      return;
    }
    saveCompletionMonitor =
        SharedScheduler.scheduleRepeating(
            plugin,
            () -> {
              if (saveInProgress) {
                return;
              }
              BukkitTask monitorTask = saveCompletionMonitor;
              saveCompletionMonitor = null;
              if (monitorTask != null) {
                monitorTask.cancel();
              }
              if (!dirtyPlayers.isEmpty() || saveRequested) {
                updateState(100L);
              }
            },
            1L,
            1L);
  }

  public static void playAbilityEffects(UUID playerId) {
    dirtyPlayers.add(playerId);
    updateState(100L);
  }

  public static ItemStack createItem(String text, int count) {
    if (count == 0) {
      return createDisplayItem(text);
    }

    return switch (text) {
      case "PUFF" -> {
        switch (count) {
          case 0:
            yield BlissItems.createPuffTierOneGemStage1();
          case 1:
            yield BlissItems.createPuffTierOneGemStage2();
          case 2:
            yield BlissItems.createPuffTierOneGemStage3();
          case 3:
            yield BlissItems.createPuffTierOneGemStage4();
          case 4:
            yield BlissItems.createPuffTierOneGemStage5();
          case 5:
            yield BlissItems.createPuffTierOneGemStage6();
          case 6:
            yield BlissItems.createPuffTierOneGemStage7();
          case 7:
            yield BlissItems.createPuffTierOneGemStage8();
          case 8:
            yield BlissItems.createPuffTierOneGemStage9();
          case 9:
            yield BlissItems.createPuffTierOneGemStage10();
          case 10:
            yield BlissItems.createPuffTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "PUFF_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createPuffTierTwoGemStage1();
          case 1:
            yield BlissItems.createPuffTierTwoGemStage2();
          case 2:
            yield BlissItems.createPuffTierTwoGemStage3();
          case 3:
            yield BlissItems.createPuffTierTwoGemStage4();
          case 4:
            yield BlissItems.createPuffTierTwoGemStage5();
          case 5:
            yield BlissItems.createPuffTierTwoGemStage6();
          case 6:
            yield BlissItems.createPuffTierTwoGemStage7();
          case 7:
            yield BlissItems.createPuffTierTwoGemStage8();
          case 8:
            yield BlissItems.createPuffTierTwoGemStage9();
          case 9:
            yield BlissItems.createPuffTierTwoGemStage10();
          case 10:
            yield BlissItems.createPuffTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "LIFE" -> {
        switch (count) {
          case 0:
            yield BlissItems.createLifeTierOneGemStage1();
          case 1:
            yield BlissItems.createLifeTierOneGemStage2();
          case 2:
            yield BlissItems.createLifeTierOneGemStage3();
          case 3:
            yield BlissItems.createLifeTierOneGemStage4();
          case 4:
            yield BlissItems.createLifeTierOneGemStage5();
          case 5:
            yield BlissItems.createLifeTierOneGemStage6();
          case 6:
            yield BlissItems.createLifeTierOneGemStage7();
          case 7:
            yield BlissItems.createLifeTierOneGemStage8();
          case 8:
            yield BlissItems.createLifeTierOneGemStage9();
          case 9:
            yield BlissItems.createLifeTierOneGemStage10();
          case 10:
            yield BlissItems.createLifeTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "LIFE_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createLifeTierTwoGemStage1();
          case 1:
            yield BlissItems.createLifeTierTwoGemStage2();
          case 2:
            yield BlissItems.createLifeTierTwoGemStage3();
          case 3:
            yield BlissItems.createLifeTierTwoGemStage4();
          case 4:
            yield BlissItems.createLifeTierTwoGemStage5();
          case 5:
            yield BlissItems.createLifeTierTwoGemStage6();
          case 6:
            yield BlissItems.createLifeTierTwoGemStage7();
          case 7:
            yield BlissItems.createLifeTierTwoGemStage8();
          case 8:
            yield BlissItems.createLifeTierTwoGemStage9();
          case 9:
            yield BlissItems.createLifeTierTwoGemStage10();
          case 10:
            yield BlissItems.createLifeTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "WEALTH" -> {
        switch (count) {
          case 0:
            yield BlissItems.createWealthTierOneGemStage1();
          case 1:
            yield BlissItems.createWealthTierOneGemStage2();
          case 2:
            yield BlissItems.createWealthTierOneGemStage3();
          case 3:
            yield BlissItems.createWealthTierOneGemStage4();
          case 4:
            yield BlissItems.createWealthTierOneGemStage5();
          case 5:
            yield BlissItems.createWealthTierOneGemStage6();
          case 6:
            yield BlissItems.createWealthTierOneGemStage7();
          case 7:
            yield BlissItems.createWealthTierOneGemStage8();
          case 8:
            yield BlissItems.createWealthTierOneGemStage9();
          case 9:
            yield BlissItems.createWealthTierOneGemStage10();
          case 10:
            yield BlissItems.createWealthTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "WEALTH_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createWealthTierTwoGemStage1();
          case 1:
            yield BlissItems.createWealthTierTwoGemStage2();
          case 2:
            yield BlissItems.createWealthTierTwoGemStage3();
          case 3:
            yield BlissItems.createWealthTierTwoGemStage4();
          case 4:
            yield BlissItems.createWealthTierTwoGemStage5();
          case 5:
            yield BlissItems.createWealthTierTwoGemStage11();
          case 6:
            yield BlissItems.createWealthTierTwoGemStage6();
          case 7:
            yield BlissItems.createWealthTierTwoGemStage7();
          case 8:
            yield BlissItems.createWealthTierTwoGemStage8();
          case 9:
            yield BlissItems.createWealthTierTwoGemStage9();
          case 10:
            yield BlissItems.createWealthTierTwoGemStage10();
          default:
            yield null;
        }
      }
      case "STRENGTH" -> {
        switch (count) {
          case 0:
            yield BlissItems.createStrengthTierOneGemStage1();
          case 1:
            yield BlissItems.createStrengthTierOneGemStage2();
          case 2:
            yield BlissItems.createStrengthTierOneGemStage3();
          case 3:
            yield BlissItems.createStrengthTierOneGemStage4();
          case 4:
            yield BlissItems.createStrengthTierOneGemStage5();
          case 5:
            yield BlissItems.createStrengthTierOneGemStage6();
          case 6:
            yield BlissItems.createStrengthTierOneGemStage7();
          case 7:
            yield BlissItems.createStrengthTierOneGemStage8();
          case 8:
            yield BlissItems.createStrengthTierOneGemStage9();
          case 9:
            yield BlissItems.createStrengthTierOneGemStage10();
          case 10:
            yield BlissItems.createStrengthTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "STRENGTH_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createStrengthTierTwoGemStage1();
          case 1:
            yield BlissItems.createStrengthTierTwoGemStage2();
          case 2:
            yield BlissItems.createStrengthTierTwoGemStage3();
          case 3:
            yield BlissItems.createStrengthTierTwoGemStage4();
          case 4:
            yield BlissItems.createStrengthTierTwoGemStage5();
          case 5:
            yield BlissItems.createStrengthTierTwoGemStage6();
          case 6:
            yield BlissItems.createStrengthTierTwoGemStage7();
          case 7:
            yield BlissItems.createStrengthTierTwoGemStage8();
          case 8:
            yield BlissItems.createStrengthTierTwoGemStage9();
          case 9:
            yield BlissItems.createStrengthTierTwoGemStage10();
          case 10:
            yield BlissItems.createStrengthTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "SPEED" -> {
        switch (count) {
          case 0:
            yield BlissItems.createSpeedTierOneGemStage1();
          case 1:
            yield BlissItems.createSpeedTierOneGemStage2();
          case 2:
            yield BlissItems.createSpeedTierOneGemStage3();
          case 3:
            yield BlissItems.createSpeedTierOneGemStage4();
          case 4:
            yield BlissItems.createSpeedTierOneGemStage5();
          case 5:
            yield BlissItems.createSpeedTierOneGemStage6();
          case 6:
            yield BlissItems.createSpeedTierOneGemStage7();
          case 7:
            yield BlissItems.createSpeedTierOneGemStage8();
          case 8:
            yield BlissItems.createSpeedTierOneGemStage9();
          case 9:
            yield BlissItems.createSpeedTierOneGemStage10();
          case 10:
            yield BlissItems.createSpeedTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "SPEED_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createSpeedTierTwoGemStage1();
          case 1:
            yield BlissItems.createSpeedTierTwoGemStage2();
          case 2:
            yield BlissItems.createSpeedTierTwoGemStage3();
          case 3:
            yield BlissItems.createSpeedTierTwoGemStage4();
          case 4:
            yield BlissItems.createSpeedTierTwoGemStage5();
          case 5:
            yield BlissItems.createSpeedTierTwoGemStage6();
          case 6:
            yield BlissItems.createSpeedTierTwoGemStage7();
          case 7:
            yield BlissItems.createSpeedTierTwoGemStage8();
          case 8:
            yield BlissItems.createSpeedTierTwoGemStage9();
          case 9:
            yield BlissItems.createSpeedTierTwoGemStage10();
          case 10:
            yield BlissItems.createSpeedTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "FIRE_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createFireTierOneGemStage1();
          case 1:
            yield BlissItems.createFireTierOneGemStage2();
          case 2:
            yield BlissItems.createFireTierOneGemStage3();
          case 3:
            yield BlissItems.createFireTierOneGemStage4();
          case 4:
            yield BlissItems.createFireTierOneGemStage5();
          case 5:
            yield BlissItems.createFireTierOneGemStage6();
          case 6:
            yield BlissItems.createFireTierOneGemStage7();
          case 7:
            yield BlissItems.createFireTierOneGemStage8();
          case 8:
            yield BlissItems.createFireTierOneGemStage9();
          case 9:
            yield BlissItems.createFireTierOneGemStage10();
          case 10:
            yield BlissItems.createFireTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "FIRE_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createFireTierTwoGemStage1();
          case 1:
            yield BlissItems.createFireTierTwoGemStage2();
          case 2:
            yield BlissItems.createFireTierTwoGemStage3();
          case 3:
            yield BlissItems.createFireTierTwoGemStage4();
          case 4:
            yield BlissItems.createFireTierTwoGemStage5();
          case 5:
            yield BlissItems.createFireTierTwoGemStage6();
          case 6:
            yield BlissItems.createFireTierTwoGemStage7();
          case 7:
            yield BlissItems.createFireTierTwoGemStage8();
          case 8:
            yield BlissItems.createFireTierTwoGemStage9();
          case 9:
            yield BlissItems.createFireTierTwoGemStage10();
          case 10:
            yield BlissItems.createFireTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "ASTRA_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createAstraTierOneGemStage1();
          case 1:
            yield BlissItems.createAstraTierOneGemStage2();
          case 2:
            yield BlissItems.createAstraTierOneGemStage3();
          case 3:
            yield BlissItems.createAstraTierOneGemStage4();
          case 4:
            yield BlissItems.createAstraTierOneGemStage5();
          case 5:
            yield BlissItems.createAstraTierOneGemStage6();
          case 6:
            yield BlissItems.createAstraTierOneGemStage7();
          case 7:
            yield BlissItems.createAstraTierOneGemStage8();
          case 8:
            yield BlissItems.createAstraTierOneGemStage9();
          case 9:
            yield BlissItems.createAstraTierOneGemStage10();
          case 10:
            yield BlissItems.createAstraTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "ASTRA_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createAstraTierTwoGemStage1();
          case 1:
            yield BlissItems.createAstraTierTwoGemStage2();
          case 2:
            yield BlissItems.createAstraTierTwoGemStage3();
          case 3:
            yield BlissItems.createAstraTierTwoGemStage4();
          case 4:
            yield BlissItems.createAstraTierTwoGemStage5();
          case 5:
            yield BlissItems.createAstraTierTwoGemStage6();
          case 6:
            yield BlissItems.createAstraTierTwoGemStage7();
          case 7:
            yield BlissItems.createAstraTierTwoGemStage8();
          case 8:
            yield BlissItems.createAstraTierTwoGemStage9();
          case 9:
            yield BlissItems.createAstraTierTwoGemStage10();
          case 10:
            yield BlissItems.createAstraTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "FLUX_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createFluxTierOneGemStage1();
          case 1:
            yield BlissItems.createFluxTierOneGemStage2();
          case 2:
            yield BlissItems.createFluxTierOneGemStage3();
          case 3:
            yield BlissItems.createFluxTierOneGemStage4();
          case 4:
            yield BlissItems.createFluxTierOneGemStage5();
          case 5:
            yield BlissItems.createFluxTierOneGemStage6();
          case 6:
            yield BlissItems.createFluxTierOneGemStage7();
          case 7:
            yield BlissItems.createFluxTierOneGemStage8();
          case 8:
            yield BlissItems.createFluxTierOneGemStage9();
          case 9:
            yield BlissItems.createFluxTierOneGemStage10();
          case 10:
            yield BlissItems.createFluxTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "FLUX_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createFluxTierTwoGemStage1();
          case 1:
            yield BlissItems.createFluxTierTwoGemStage2();
          case 2:
            yield BlissItems.createFluxTierTwoGemStage3();
          case 3:
            yield BlissItems.createFluxTierTwoGemStage4();
          case 4:
            yield BlissItems.createFluxTierTwoGemStage5();
          case 5:
            yield BlissItems.createFluxTierTwoGemStage6();
          case 6:
            yield BlissItems.createFluxTierTwoGemStage7();
          case 7:
            yield BlissItems.createFluxTierTwoGemStage8();
          case 8:
            yield BlissItems.createFluxTierTwoGemStage9();
          case 9:
            yield BlissItems.createFluxTierTwoGemStage10();
          case 10:
            yield BlissItems.createFluxTierTwoGemStage11();
          default:
            yield null;
        }
      }
      default -> null;
    };
  }

  ItemStack createAbilityItem(String text, int count) {
    if (count == 0) {
      return this.createUpgradeItem(text);
    }

    return switch (text) {
      case "PUFF_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createPuffTierOneGemStage1();
          case 1:
            yield BlissItems.createPuffTierOneGemStage2();
          case 2:
            yield BlissItems.createPuffTierOneGemStage3();
          case 3:
            yield BlissItems.createPuffTierOneGemStage4();
          case 4:
            yield BlissItems.createPuffTierOneGemStage5();
          case 5:
            yield BlissItems.createPuffTierOneGemStage6();
          case 6:
            yield BlissItems.createPuffTierOneGemStage7();
          case 7:
            yield BlissItems.createPuffTierOneGemStage8();
          case 8:
            yield BlissItems.createPuffTierOneGemStage9();
          case 9:
            yield BlissItems.createPuffTierOneGemStage10();
          case 10:
            yield BlissItems.createPuffTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "PUFF_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createPuffTierTwoGemStage1();
          case 1:
            yield BlissItems.createPuffTierTwoGemStage2();
          case 2:
            yield BlissItems.createPuffTierTwoGemStage3();
          case 3:
            yield BlissItems.createPuffTierTwoGemStage4();
          case 4:
            yield BlissItems.createPuffTierTwoGemStage5();
          case 5:
            yield BlissItems.createPuffTierTwoGemStage6();
          case 6:
            yield BlissItems.createPuffTierTwoGemStage7();
          case 7:
            yield BlissItems.createPuffTierTwoGemStage8();
          case 8:
            yield BlissItems.createPuffTierTwoGemStage9();
          case 9:
            yield BlissItems.createPuffTierTwoGemStage10();
          case 10:
            yield BlissItems.createPuffTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "LIFE_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createLifeTierOneGemStage1();
          case 1:
            yield BlissItems.createLifeTierOneGemStage2();
          case 2:
            yield BlissItems.createLifeTierOneGemStage3();
          case 3:
            yield BlissItems.createLifeTierOneGemStage4();
          case 4:
            yield BlissItems.createLifeTierOneGemStage5();
          case 5:
            yield BlissItems.createLifeTierOneGemStage6();
          case 6:
            yield BlissItems.createLifeTierOneGemStage7();
          case 7:
            yield BlissItems.createLifeTierOneGemStage8();
          case 8:
            yield BlissItems.createLifeTierOneGemStage9();
          case 9:
            yield BlissItems.createLifeTierOneGemStage10();
          case 10:
            yield BlissItems.createLifeTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "LIFE_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createLifeTierTwoGemStage1();
          case 1:
            yield BlissItems.createLifeTierTwoGemStage2();
          case 2:
            yield BlissItems.createLifeTierTwoGemStage3();
          case 3:
            yield BlissItems.createLifeTierTwoGemStage4();
          case 4:
            yield BlissItems.createLifeTierTwoGemStage5();
          case 5:
            yield BlissItems.createLifeTierTwoGemStage6();
          case 6:
            yield BlissItems.createLifeTierTwoGemStage7();
          case 7:
            yield BlissItems.createLifeTierTwoGemStage8();
          case 8:
            yield BlissItems.createLifeTierTwoGemStage9();
          case 9:
            yield BlissItems.createLifeTierTwoGemStage10();
          case 10:
            yield BlissItems.createLifeTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "WEALTH_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createWealthTierOneGemStage1();
          case 1:
            yield BlissItems.createWealthTierOneGemStage2();
          case 2:
            yield BlissItems.createWealthTierOneGemStage3();
          case 3:
            yield BlissItems.createWealthTierOneGemStage4();
          case 4:
            yield BlissItems.createWealthTierOneGemStage5();
          case 5:
            yield BlissItems.createWealthTierOneGemStage6();
          case 6:
            yield BlissItems.createWealthTierOneGemStage7();
          case 7:
            yield BlissItems.createWealthTierOneGemStage8();
          case 8:
            yield BlissItems.createWealthTierOneGemStage9();
          case 9:
            yield BlissItems.createWealthTierOneGemStage10();
          case 10:
            yield BlissItems.createWealthTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "WEALTH_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createWealthTierTwoGemStage1();
          case 1:
            yield BlissItems.createWealthTierTwoGemStage2();
          case 2:
            yield BlissItems.createWealthTierTwoGemStage3();
          case 3:
            yield BlissItems.createWealthTierTwoGemStage4();
          case 4:
            yield BlissItems.createWealthTierTwoGemStage5();
          case 5:
            yield BlissItems.createWealthTierTwoGemStage11();
          case 6:
            yield BlissItems.createWealthTierTwoGemStage6();
          case 7:
            yield BlissItems.createWealthTierTwoGemStage7();
          case 8:
            yield BlissItems.createWealthTierTwoGemStage8();
          case 9:
            yield BlissItems.createWealthTierTwoGemStage9();
          case 10:
            yield BlissItems.createWealthTierTwoGemStage10();
          default:
            yield null;
        }
      }
      case "STRENGTH_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createStrengthTierOneGemStage1();
          case 1:
            yield BlissItems.createStrengthTierOneGemStage2();
          case 2:
            yield BlissItems.createStrengthTierOneGemStage3();
          case 3:
            yield BlissItems.createStrengthTierOneGemStage4();
          case 4:
            yield BlissItems.createStrengthTierOneGemStage5();
          case 5:
            yield BlissItems.createStrengthTierOneGemStage6();
          case 6:
            yield BlissItems.createStrengthTierOneGemStage7();
          case 7:
            yield BlissItems.createStrengthTierOneGemStage8();
          case 8:
            yield BlissItems.createStrengthTierOneGemStage9();
          case 9:
            yield BlissItems.createStrengthTierOneGemStage10();
          case 10:
            yield BlissItems.createStrengthTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "STRENGTH_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createStrengthTierTwoGemStage1();
          case 1:
            yield BlissItems.createStrengthTierTwoGemStage2();
          case 2:
            yield BlissItems.createStrengthTierTwoGemStage3();
          case 3:
            yield BlissItems.createStrengthTierTwoGemStage4();
          case 4:
            yield BlissItems.createStrengthTierTwoGemStage5();
          case 5:
            yield BlissItems.createStrengthTierTwoGemStage6();
          case 6:
            yield BlissItems.createStrengthTierTwoGemStage7();
          case 7:
            yield BlissItems.createStrengthTierTwoGemStage8();
          case 8:
            yield BlissItems.createStrengthTierTwoGemStage9();
          case 9:
            yield BlissItems.createStrengthTierTwoGemStage10();
          case 10:
            yield BlissItems.createStrengthTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "SPEED_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createSpeedTierOneGemStage1();
          case 1:
            yield BlissItems.createSpeedTierOneGemStage2();
          case 2:
            yield BlissItems.createSpeedTierOneGemStage3();
          case 3:
            yield BlissItems.createSpeedTierOneGemStage4();
          case 4:
            yield BlissItems.createSpeedTierOneGemStage5();
          case 5:
            yield BlissItems.createSpeedTierOneGemStage6();
          case 6:
            yield BlissItems.createSpeedTierOneGemStage7();
          case 7:
            yield BlissItems.createSpeedTierOneGemStage8();
          case 8:
            yield BlissItems.createSpeedTierOneGemStage9();
          case 9:
            yield BlissItems.createSpeedTierOneGemStage10();
          case 10:
            yield BlissItems.createSpeedTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "SPEED_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createSpeedTierTwoGemStage1();
          case 1:
            yield BlissItems.createSpeedTierTwoGemStage2();
          case 2:
            yield BlissItems.createSpeedTierTwoGemStage3();
          case 3:
            yield BlissItems.createSpeedTierTwoGemStage4();
          case 4:
            yield BlissItems.createSpeedTierTwoGemStage5();
          case 5:
            yield BlissItems.createSpeedTierTwoGemStage6();
          case 6:
            yield BlissItems.createSpeedTierTwoGemStage7();
          case 7:
            yield BlissItems.createSpeedTierTwoGemStage8();
          case 8:
            yield BlissItems.createSpeedTierTwoGemStage9();
          case 9:
            yield BlissItems.createSpeedTierTwoGemStage10();
          case 10:
            yield BlissItems.createSpeedTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "FIRE_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createFireTierOneGemStage1();
          case 1:
            yield BlissItems.createFireTierOneGemStage2();
          case 2:
            yield BlissItems.createFireTierOneGemStage3();
          case 3:
            yield BlissItems.createFireTierOneGemStage4();
          case 4:
            yield BlissItems.createFireTierOneGemStage5();
          case 5:
            yield BlissItems.createFireTierOneGemStage6();
          case 6:
            yield BlissItems.createFireTierOneGemStage7();
          case 7:
            yield BlissItems.createFireTierOneGemStage8();
          case 8:
            yield BlissItems.createFireTierOneGemStage9();
          case 9:
            yield BlissItems.createFireTierOneGemStage10();
          case 10:
            yield BlissItems.createFireTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "FIRE_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createFireTierTwoGemStage1();
          case 1:
            yield BlissItems.createFireTierTwoGemStage2();
          case 2:
            yield BlissItems.createFireTierTwoGemStage3();
          case 3:
            yield BlissItems.createFireTierTwoGemStage4();
          case 4:
            yield BlissItems.createFireTierTwoGemStage5();
          case 5:
            yield BlissItems.createFireTierTwoGemStage6();
          case 6:
            yield BlissItems.createFireTierTwoGemStage7();
          case 7:
            yield BlissItems.createFireTierTwoGemStage8();
          case 8:
            yield BlissItems.createFireTierTwoGemStage9();
          case 9:
            yield BlissItems.createFireTierTwoGemStage10();
          case 10:
            yield BlissItems.createFireTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "ASTRA_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createAstraTierOneGemStage1();
          case 1:
            yield BlissItems.createAstraTierOneGemStage2();
          case 2:
            yield BlissItems.createAstraTierOneGemStage3();
          case 3:
            yield BlissItems.createAstraTierOneGemStage4();
          case 4:
            yield BlissItems.createAstraTierOneGemStage5();
          case 5:
            yield BlissItems.createAstraTierOneGemStage6();
          case 6:
            yield BlissItems.createAstraTierOneGemStage7();
          case 7:
            yield BlissItems.createAstraTierOneGemStage8();
          case 8:
            yield BlissItems.createAstraTierOneGemStage9();
          case 9:
            yield BlissItems.createAstraTierOneGemStage10();
          case 10:
            yield BlissItems.createAstraTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "ASTRA_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createAstraTierTwoGemStage1();
          case 1:
            yield BlissItems.createAstraTierTwoGemStage2();
          case 2:
            yield BlissItems.createAstraTierTwoGemStage3();
          case 3:
            yield BlissItems.createAstraTierTwoGemStage4();
          case 4:
            yield BlissItems.createAstraTierTwoGemStage5();
          case 5:
            yield BlissItems.createAstraTierTwoGemStage6();
          case 6:
            yield BlissItems.createAstraTierTwoGemStage7();
          case 7:
            yield BlissItems.createAstraTierTwoGemStage8();
          case 8:
            yield BlissItems.createAstraTierTwoGemStage9();
          case 9:
            yield BlissItems.createAstraTierTwoGemStage10();
          case 10:
            yield BlissItems.createAstraTierTwoGemStage11();
          default:
            yield null;
        }
      }
      case "FLUX_T1" -> {
        switch (count) {
          case 0:
            yield BlissItems.createFluxTierOneGemStage1();
          case 1:
            yield BlissItems.createFluxTierOneGemStage2();
          case 2:
            yield BlissItems.createFluxTierOneGemStage3();
          case 3:
            yield BlissItems.createFluxTierOneGemStage4();
          case 4:
            yield BlissItems.createFluxTierOneGemStage5();
          case 5:
            yield BlissItems.createFluxTierOneGemStage6();
          case 6:
            yield BlissItems.createFluxTierOneGemStage7();
          case 7:
            yield BlissItems.createFluxTierOneGemStage8();
          case 8:
            yield BlissItems.createFluxTierOneGemStage9();
          case 9:
            yield BlissItems.createFluxTierOneGemStage10();
          case 10:
            yield BlissItems.createFluxTierOneGemStage11();
          default:
            yield null;
        }
      }
      case "FLUX_T2" -> {
        switch (count) {
          case 0:
            yield BlissItems.createFluxTierTwoGemStage1();
          case 1:
            yield BlissItems.createFluxTierTwoGemStage2();
          case 2:
            yield BlissItems.createFluxTierTwoGemStage3();
          case 3:
            yield BlissItems.createFluxTierTwoGemStage4();
          case 4:
            yield BlissItems.createFluxTierTwoGemStage5();
          case 5:
            yield BlissItems.createFluxTierTwoGemStage6();
          case 6:
            yield BlissItems.createFluxTierTwoGemStage7();
          case 7:
            yield BlissItems.createFluxTierTwoGemStage8();
          case 8:
            yield BlissItems.createFluxTierTwoGemStage9();
          case 9:
            yield BlissItems.createFluxTierTwoGemStage10();
          case 10:
            yield BlissItems.createFluxTierTwoGemStage11();
          default:
            yield null;
        }
      }
      default -> null;
    };
  }

  static ItemStack createDisplayItem(String text) {
    return switch (text) {
      case "PUFF" -> BlissItems.createPuffTierOneGemStage1();
      case "LIFE" -> BlissItems.createLifeTierOneGemStage1();
      case "WEALTH" -> BlissItems.createWealthTierOneGemStage1();
      case "STRENGTH" -> BlissItems.createStrengthTierOneGemStage1();
      case "SPEED" -> BlissItems.createSpeedTierOneGemStage1();
      case "FIRE_T1" -> BlissItems.createFireTierOneGemStage1();
      case "ASTRA_T1" -> BlissItems.createAstraTierOneGemStage1();
      case "FLUX_T1" -> BlissItems.createFluxTierOneGemStage1();
      case "PUFF_T2" -> BlissItems.createPuffTierTwoGemStage1();
      case "LIFE_T2" -> BlissItems.createLifeTierTwoGemStage1();
      case "WEALTH_T2" -> BlissItems.createWealthTierTwoGemStage1();
      case "STRENGTH_T2" -> BlissItems.createStrengthTierTwoGemStage1();
      case "SPEED_T2" -> BlissItems.createSpeedTierTwoGemStage1();
      case "FIRE_T2" -> BlissItems.createFireTierTwoGemStage1();
      case "ASTRA_T2" -> BlissItems.createAstraTierTwoGemStage1();
      case "FLUX_T2" -> BlissItems.createFluxTierTwoGemStage1();
      default -> null;
    };
  }

  ItemStack createUpgradeItem(String text) {
    return switch (text) {
      case "PUFF_T1" -> BlissItems.createPuffTierOneGemStage1();
      case "LIFE_T1" -> BlissItems.createLifeTierOneGemStage1();
      case "WEALTH_T1" -> BlissItems.createWealthTierOneGemStage1();
      case "STRENGTH_T1" -> BlissItems.createStrengthTierOneGemStage1();
      case "SPEED_T1" -> BlissItems.createSpeedTierOneGemStage1();
      case "FIRE_T1" -> BlissItems.createFireTierOneGemStage1();
      case "ASTRA_T1" -> BlissItems.createAstraTierOneGemStage1();
      case "FLUX_T1" -> BlissItems.createFluxTierOneGemStage1();
      case "PUFF_T2" -> BlissItems.createPuffTierTwoGemStage1();
      case "LIFE_T2" -> BlissItems.createLifeTierTwoGemStage1();
      case "WEALTH_T2" -> BlissItems.createWealthTierTwoGemStage1();
      case "STRENGTH_T2" -> BlissItems.createStrengthTierTwoGemStage1();
      case "SPEED_T2" -> BlissItems.createSpeedTierTwoGemStage1();
      case "FIRE_T2" -> BlissItems.createFireTierTwoGemStage1();
      case "ASTRA_T2" -> BlissItems.createAstraTierTwoGemStage1();
      case "FLUX_T2" -> BlissItems.createFluxTierTwoGemStage1();
      default -> null;
    };
  }

  public void activateAbility(Player player) {
    String text = this.formatDisplayText(player);
    if (text != null) {
      this.cleanupAbilityState(player);
      int count = getAbilityIntValue(player);
      ItemStack heldItem = createItem(text, count);
      if (heldItem != null) {
        boolean enabled =
            Arrays.stream(player.getInventory().getStorageContents())
                .allMatch(item -> shouldGivePlayerGem(item));
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (enabled && offHandItem != null && offHandItem.getType() == heldItem.getType()) {
          player.getInventory().setItemInOffHand(heldItem);
        } else {
          this.trackAbilityState(player, heldItem);
        }
      }
    }
  }

  public void spawnAbilityEffects(Player player) {
    String text = this.formatDisplayText(player);
    if (text != null) {
      int count = getAbilityIntValue(player);
      ItemStack item = createItem(text, count);
      if (item != null) {
        ItemStack[] heldItem = player.getInventory().getContents();

        for (int index = 0; index < heldItem.length; index++) {
          ItemStack targetItem = heldItem[index];
          if (targetItem != null && this.isCachedGemItem(targetItem)) {
            player.getInventory().setItem(index, item);
            return;
          }
        }

        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (offHandItem != null && this.isCachedGemItem(offHandItem)) {
          player.getInventory().setItemInOffHand(item);
        }
      }
    }
  }

  public void cleanupAbilityState(Player player) {
    PlayerInventory playerInventory = player.getInventory();

    for (int count = 0; count < playerInventory.getSize(); count++) {
      ItemStack item = playerInventory.getItem(count);
      if (item != null && this.isCachedGemItem(item)) {
        playerInventory.setItem(count, null);
      }
    }
  }

  public static int getAbilityIntValue(Player player) {
    return energyByPlayer.getOrDefault(player.getUniqueId(), 0);
  }

  public void scheduleAbilityUpdate(Player player, int count) {
    int clampedEnergy = Math.min(Math.max(0, count), 10);
    energyByPlayer.put(player.getUniqueId(), clampedEnergy);
    playAbilityEffects(player.getUniqueId());
  }

  public void completeAbilityAction(Player player, int count) {
    int currentEnergy = getAbilityIntValue(player);
    int updatedEnergy = Math.min(currentEnergy + count, 10);
    energyByPlayer.put(player.getUniqueId(), updatedEnergy);
    playAbilityEffects(player.getUniqueId());
  }

  public static boolean canUseAbility(Player player, int count) {
    int currentEnergy = getAbilityIntValue(player);
    int updatedEnergy = Math.max(0, currentEnergy - count);
    energyByPlayer.put(player.getUniqueId(), updatedEnergy);
    playAbilityEffects(player.getUniqueId());
    return true;
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    if (energyByPlayer.containsKey(playerId)) {
      lastKnownNamesByPlayer.put(playerId, event.getPlayer().getName());
      playAbilityEffects(playerId);
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    int configuredValue = energyByPlayer.getOrDefault(playerId, energyConfig.getInt(playerId.toString() + ".energy", 5));
    int count = Math.min(configuredValue, 10);
    energyByPlayer.put(playerId, count);
    lastKnownNamesByPlayer.put(playerId, player.getName());
    playAbilityEffects(playerId);
  }

  boolean isGemItem(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createPuffTierOneGemStage1())
            || BlissItems.isMatchingItem(
                item, BlissItems.createPuffTierOneGemStage2())
            || BlissItems.isMatchingItem(
                item, BlissItems.createPuffTierOneGemStage3())
            || BlissItems.isMatchingItem(
                item, BlissItems.createPuffTierOneGemStage4())
            || BlissItems.isMatchingItem(
                item, BlissItems.createPuffTierOneGemStage5())
            || BlissItems.isMatchingItem(
                item, BlissItems.createPuffTierOneGemStage6())
            || BlissItems.isMatchingItem(
                item, BlissItems.createPuffTierOneGemStage7())
            || BlissItems.isMatchingItem(
                item, BlissItems.createPuffTierOneGemStage8())
            || BlissItems.isMatchingItem(
                item, BlissItems.createPuffTierOneGemStage9())
            || BlissItems.isMatchingItem(
                item, BlissItems.createPuffTierOneGemStage10())
            || BlissItems.isMatchingItem(
                item, BlissItems.createPuffTierOneGemStage11());
  }

  boolean isAbilityAllowed(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage11());
  }

  boolean isAbilityBlocked(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createLifeTierOneGemStage1())
            || BlissItems.isMatchingItem(
                item, BlissItems.createLifeTierOneGemStage2())
            || BlissItems.isMatchingItem(
                item, BlissItems.createLifeTierOneGemStage3())
            || BlissItems.isMatchingItem(
                item, BlissItems.createLifeTierOneGemStage4())
            || BlissItems.isMatchingItem(
                item, BlissItems.createLifeTierOneGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierOneGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierOneGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierOneGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierOneGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierOneGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierOneGemStage11());
  }

  boolean isMatchingState(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage11());
  }

  boolean isValidTarget(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage11());
  }

  boolean isProtectedTarget(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage11())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage10());
  }

  boolean isActiveForPlayer(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage11());
  }

  boolean hasRequiredState(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage11());
  }

  boolean shouldApplyEffect(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage11());
  }

  boolean canAffectTarget(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage11());
  }

  boolean isTrackedTarget(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage11());
  }

  boolean isPrimaryGemItem(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage11());
  }

  boolean isTargetGemItem(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage11());
  }

  boolean isSourceGemItem(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage11());
  }

  boolean isActiveGemItem(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage11());
  }

  boolean isPendingGemItem(ItemStack item) {
    return item == null
        ? false
        : BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage1())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage2())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage3())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage4())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage5())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage6())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage7())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage8())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage9())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage10())
            || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage11());
  }

  String formatDisplayText(Player player) {
    for (ItemStack item : player.getInventory().getContents()) {
      String text = this.formatDisplayTextForState(item);
      if (text != null) {
        return text;
      }
    }

    return null;
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    Player targetPlayer = player.getKiller();
    int count = getAbilityIntValue(player);
    String text = this.formatDisplayText(player);
    this.itemsByPlayer.remove(player.getUniqueId());
    this.secondaryValuesByPlayer.remove(player.getUniqueId());
    if (count <= 0) {
      boolean enabled = ConfigValueCache.getBoolean(plugin, "BanOnBrokenGem", false);
      if (enabled) {
        Bukkit.getScheduler().runTask(plugin, () -> handlePlayerDeath(player));
      } else if (text != null) {
          this.resetAbilityState(event);
          ItemStack item = createItem(text, 0);
          String message = this.formatDisplayTextForTarget(text, 0);
          Bukkit.getServer()
              .dispatchCommand(Bukkit.getConsoleSender(), "cooldown " + player.getName());
          if (item != null) {
            event.getDrops().removeIf(this::isCachedGemItem);
            this.itemsByPlayer.put(player.getUniqueId(), item);
          }

          if (message != null) {
            this.secondaryValuesByPlayer.put(player.getUniqueId(), message);
            player.sendMessage(message);
          }
        }
      
    } else {
      canUseAbility(player, 1);
      playAbilityEffects(player.getUniqueId());
      int index = getAbilityIntValue(player);
      this.resetAbilityState(event);
      if (targetPlayer == null) {
        this.processAbilityState(event, player, text, index);
      } else {
        String displayText = this.formatDisplayText(targetPlayer);
        int remaining = getAbilityIntValue(targetPlayer);
        if (displayText != null && remaining != 0 && remaining < 10) {
          this.completeAbilityAction(targetPlayer, 1);
          playAbilityEffects(targetPlayer.getUniqueId());
          remaining = getAbilityIntValue(targetPlayer);
          ItemStack heldItem = createItem(displayText, remaining);
          String formattedText = this.formatDisplayTextForPlayer(displayText, remaining);
          if (heldItem != null) {
            for (int step = 0; step < targetPlayer.getInventory().getSize(); step++) {
              ItemStack targetItem = targetPlayer.getInventory().getItem(step);
              if (this.isCachedGemItem(targetItem)) {
                targetPlayer.getInventory().setItem(step, heldItem);
                if (formattedText != null) {
                  targetPlayer.sendMessage(formattedText);
                }
                break;
              }
            }
          }

          if (text != null) {
            this.handleAbilityAction(event, player, text, index);
          }
        } else {
          this.processAbilityState(event, player, text, index);
        }
      }
    }
  }

  void processAbilityState(PlayerDeathEvent event, Player player, String text, int count) {
    event.getDrops().add(createRecipeItem(player));
    if (text != null) {
      this.handleAbilityAction(event, player, text, count);
    }
  }

  void handleAbilityAction(PlayerDeathEvent event, Player player, String gemType, int count) {
    String baseGemType = gemType;
    if (gemType.endsWith("_T2")) {
      baseGemType =
          switch (gemType) {
            case "PUFF_T2" -> "PUFF";
            case "LIFE_T2" -> "LIFE";
            case "WEALTH_T2" -> "WEALTH";
            case "STRENGTH_T2" -> "STRENGTH";
            case "SPEED_T2" -> "SPEED";
            case "FIRE_T2" -> "FIRE_T1";
            case "ASTRA_T2" -> "ASTRA_T1";
            case "FLUX_T2" -> "FLUX_T1";
            default -> gemType;
          };
      event.getDrops().add(BlissItems.createUpgraderMenuItem());
    }

    ItemStack item = createItem(baseGemType, count);
    String displayText = this.formatDisplayTextForTarget(baseGemType, count);
    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "cooldown " + player.getName());
    if (item != null) {
      event.getDrops().removeIf(this::isCachedGemItem);
      this.itemsByPlayer.put(player.getUniqueId(), item);
    }

    if (displayText != null) {
      this.secondaryValuesByPlayer.put(player.getUniqueId(), displayText);
      player.sendMessage(displayText);
    }
  }

  void resetAbilityState(PlayerDeathEvent event) {
    event.getDrops().removeIf(this::isCachedGemItem);
  }

  void trackAbilityState(Player player, ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);

      for (ItemStack heldItem : leftovers.values()) {
        if (heldItem != null && heldItem.getType() != Material.AIR) {
          player.getWorld().dropItemNaturally(player.getLocation(), heldItem);
        }
      }
    }
  }

  public static ItemStack createRecipeItem(Player player) {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setCustomModelData(300);
    itemMeta.setDisplayName(
        ChatColor.of("#96FFD9")
            + org.bukkit.ChatColor.BOLD.toString()
            + "ᴇɴᴇʀɢʏ "
            + ChatColor.of("#EFE4CC")
            + "ɪɴ ᴀ ʙᴏᴛᴛʟᴇ");
    if (player != null) {
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.of("#96FFD9") + "From" + ChatColor.of("#EFE4CC") + ": " + player.getName());
      itemMeta.setLore(lore);
    }

    item.setItemMeta(itemMeta);
    return item;
  }

  String formatDisplayTextForPlayer(String text, int count) {
    String message =
        switch (text) {
          case "PUFF", "PUFF_T2" -> "§f§lᴘᴜꜰꜰ";
          case "LIFE", "LIFE_T2" -> "§d§lʟɪғᴇ";
          case "WEALTH", "WEALTH_T2" -> "§2§lᴡᴇᴀʟᴛʜ";
          case "STRENGTH", "STRENGTH_T2" ->
              "§4§lѕᴛʀᴇɴɢᴛʜ";
          case "SPEED", "SPEED_T2" -> "§e§lѕᴘᴇᴇᴅ";
          case "FIRE_T1", "FIRE_T2" -> "§6§lғɪʀᴇ";
          case "ASTRA_T1", "ASTRA_T2" -> "§5§lᴀѕᴛʀᴀ";
          case "FLUX_T1", "FLUX_T2" -> "§3§lғʟᴜx";
          default -> "";
        };
    String displayText =
        text.contains("PUFF")
            ? "§f🔮"
            : (text.contains("LIFE")
                ? "§d🔮"
                : (text.contains("WEALTH")
                    ? "§2🔮"
                    : (text.contains("STRENGTH")
                        ? "§4🔮"
                        : (text.contains("SPEED")
                            ? "§e🔮"
                            : (text.contains("FIRE")
                                ? "§6🔮"
                                : (text.contains("ASTRA")
                                    ? "§5🔮"
                                    : (text.contains("FLUX")
                                        ? "§b🔮"
                                        : "🔮")))))));

    String accentColor =
        switch (count) {
          case 1 -> "§cRuined";
          case 2 -> ChatColor.of("#E2C35D") + "Damaged";
          case 3 -> ChatColor.of("#7963B3") + "Cracked";
          case 4 -> ChatColor.of("#82F5AE") + "Scratched";
          case 5 -> ChatColor.of("#82EDBF") + "Pristine";
          case 6 -> ChatColor.of("#82EDBF") + "Pristine +1";
          case 7 -> ChatColor.of("#82EDBF") + "Pristine +2";
          case 8 -> ChatColor.of("#82EDBF") + "Pristine +3";
          case 9 -> ChatColor.of("#82EDBF") + "Pristine +4";
          case 10 -> ChatColor.of("#82EDBF") + "Pristine +5";
          default -> "";
        };
    String textColor = ChatColor.of("#befff7").toString();
    String labelColor = ChatColor.of("#befff7").toString();
    String nameColor = ChatColor.of("#befff7").toString();
    String configColor = ChatColor.of("#befff7").toString();
    return displayText
        + textColor
        + " You §agained "
        + labelColor
        + "energy."
        + nameColor
        + " Your "
        + message
        + configColor
        + " gem is now "
        + accentColor;
  }

  String formatDisplayTextForTarget(String text, int count) {
    String message =
        switch (text) {
          case "PUFF", "PUFF_T2" -> "§f§lᴘᴜꜰꜰ";
          case "LIFE", "LIFE_T2" -> "§d§lʟɪғᴇ";
          case "WEALTH", "WEALTH_T2" -> "§2§lᴡᴇᴀʟᴛʜ";
          case "STRENGTH", "STRENGTH_T2" ->
              "§4§lѕᴛʀᴇɴɢᴛʜ";
          case "SPEED", "SPEED_T2" -> "§e§lѕᴘᴇᴇᴅ";
          case "FIRE_T1", "FIRE_T2" -> "§6§lғɪʀᴇ";
          case "ASTRA_T1", "ASTRA_T2" -> "§5§lᴀѕᴛʀᴀ";
          case "FLUX_T1", "FLUX_T2" -> "§3§lғʟᴜx";
          default -> "";
        };
    String displayText =
        text.contains("PUFF")
            ? "§f🔮"
            : (text.contains("LIFE")
                ? "§d🔮"
                : (text.contains("WEALTH")
                    ? "§2🔮"
                    : (text.contains("STRENGTH")
                        ? "§4🔮"
                        : (text.contains("SPEED")
                            ? "§e🔮"
                            : (text.contains("FIRE")
                                ? "§6🔮"
                                : (text.contains("ASTRA")
                                    ? "§5🔮"
                                    : (text.contains("FLUX")
                                        ? "§3🔮"
                                        : "🔮")))))));

    String accentColor =
        switch (count) {
          case 1 -> "§cRuined";
          case 2 -> ChatColor.of("#E2C35D") + "Damaged";
          case 3 -> ChatColor.of("#7963B3") + "Cracked";
          case 4 -> ChatColor.of("#82F5AE") + "Scratched";
          case 5 -> ChatColor.of("#82EDBF") + "Pristine";
          case 6 -> ChatColor.of("#82EDBF") + "Pristine +1";
          case 7 -> ChatColor.of("#82EDBF") + "Pristine +2";
          case 8 -> ChatColor.of("#82EDBF") + "Pristine +3";
          case 9 -> ChatColor.of("#82EDBF") + "Pristine +4";
          case 10 -> ChatColor.of("#82EDBF") + "Pristine +5";
          default -> "§7Unknown";
        };
    String textColor = ChatColor.of("#befff7").toString();
    String labelColor = ChatColor.of("#befff7").toString();
    String nameColor = ChatColor.of("#befff7").toString();
    String configColor = ChatColor.of("#befff7").toString();
    return displayText
        + textColor
        + " You §clost "
        + labelColor
        + "energy."
        + nameColor
        + " Your "
        + message
        + configColor
        + " gem is now "
        + accentColor;
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    ItemStack item = this.itemsByPlayer.remove(playerId);
    if (item != null && item.getType() != Material.AIR) {
      Bukkit.getScheduler().runTask(plugin, () -> this.restoreEnergyStateAfterRespawn(player, item, playerId));
    } else {
      this.secondaryValuesByPlayer.remove(playerId);
    }
  }

  boolean isCachedGemItem(ItemStack item) {
    return this.formatDisplayTextForState(item) != null;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getHand() == EquipmentSlot.HAND) {
      Player player = event.getPlayer();
      if (!this.playerIds.contains(player.getUniqueId())) {
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
          ItemStack mainHandItem = player.getInventory().getItemInMainHand();
          if (mainHandItem != null && !mainHandItem.getType().isAir()) {
            if (BlissItems.isMatchingItem(
                mainHandItem, BlissItems.createRewardItem())) {
              event.setCancelled(true);
              if (mainHandItem.getAmount() > 1) {
                mainHandItem.setAmount(mainHandItem.getAmount() - 1);
              } else {
                player.getInventory().setItemInMainHand(null);
              }

              player.getInventory()
                  .addItem(BlissItems.createEnergyToken());
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    Player player = event.getPlayer();
    ItemStack item = event.getItemDrop().getItemStack();
    if (BlissItems.isMatchingItem(item, BlissItems.createRewardItem())) {
      this.playerIds.add(player.getUniqueId());
      Bukkit.getScheduler()
          .runTaskLater(Bliss.getInstance(), () -> this.clearDroppedEnergyState(player), 2L);
    }
  }

  @EventHandler
  public void refreshPlayerState(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK
        || event.getAction() == Action.RIGHT_CLICK_AIR) {
      boolean enabled = ConfigValueCache.getBoolean(plugin, "BrokenGemEnergyUse", false);
      if (!enabled
          && mainHandItem != null
          && BlissItems.isMatchingItem(
              mainHandItem, BlissItems.createRewardItem())) {
        int count = getAbilityIntValue(player);
        if (count == 0) {
          player.sendMessage("🔮 §cYour gem is broken, you can´t use energy!");
          return;
        }

        if (count == 10) {
          player.sendMessage(
              "🔮 §cYour gem is already "
                  + ChatColor.of("#82EDBF")
                  + "Pristine +5, §cyou can´t use energy!");
          return;
        }

        String text = this.formatDisplayText(player);
        if (text == null) {
          player.sendMessage("🔮 §cYou don't have any gem to upgrade!");
          return;
        }

        this.completeAbilityAction(player, 1);
        this.spawnAbilityEffects(player);
        String message = this.formatDisplayTextFromConfig(text);
        String displayText = this.formatDisplayTextForAbility(count + 1);
        String accentColor = ChatColor.of("#E4BC74").toString();
        String textColor = ChatColor.of("#9DFFDF").toString();
        String bold = ChatColor.BOLD.toString();
        String labelColor = ChatColor.WHITE.toString();
        String nameColor = ChatColor.of("#befff7").toString();
        String configPath = this.formatDisplayTextFromConfig(text);
        String keyColor = ChatColor.of("#befff7").toString();
        player.sendMessage(
            String.format(
                accentColor
                    + "🔮 "
                    + textColor
                    + bold
                    + "ᴇɴᴇʀɢʏ "
                    + labelColor
                    + nameColor
                    + "absorbed, your "
                    + configPath
                    + keyColor
                    + " gem is now at %s",
                displayText));
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem != null
            && BlissItems.isMatchingItem(
                heldItem, BlissItems.createRewardItem())) {
          if (heldItem.getAmount() > 1) {
            heldItem.setAmount(heldItem.getAmount() - 1);
          } else {
            player.getInventory().setItemInMainHand(null);
          }
        }

        updateAbilityState();
      }
    }
  }

  String formatDisplayTextFromConfig(String text) {
    switch (text.toLowerCase()) {
      case "puff":
      case "puff_t2":
        return "§f§lᴘᴜꜰꜰ";
      case "life":
      case "life_t2":
        return "§d§lʟɪғᴇ";
      case "astra_t1":
      case "astra_t2":
        return "§5§lᴀѕᴛʀᴀ";
      case "wealth":
      case "wealth_t2":
        return "§2§lᴡᴇᴀʟᴛʜ";
      case "speed":
      case "speed_t2":
        return "§e§lѕᴘᴇᴇᴅ";
      case "fire_t1":
      case "fire_t2":
        return "§6§lғɪʀᴇ";
      case "strength":
      case "strength_t2":
        return "§4§lѕᴛʀᴇɴɢᴛʜ";
      case "flux_t1":
      case "flux_t2":
        return "§3§lғʟᴜx";
      default:
        return "§7Unknown";
    }
  }

  String formatDisplayTextForAbility(int count) {
    switch (count) {
      case 1:
        return "§cRuined";
      case 2:
        return ChatColor.of("#E2C35D") + "Damaged";
      case 3:
        return ChatColor.of("#7963B3") + "Cracked";
      case 4:
        return ChatColor.of("#82F5AE") + "Scratched";
      case 5:
        return ChatColor.of("#82EDBF") + "Pristine";
      case 6:
        return ChatColor.of("#82EDBF") + "Pristine +1";
      case 7:
        return ChatColor.of("#82EDBF") + "Pristine +2";
      case 8:
        return ChatColor.of("#82EDBF") + "Pristine +3";
      case 9:
        return ChatColor.of("#82EDBF") + "Pristine +4";
      case 10:
        return ChatColor.of("#82EDBF") + "Pristine +5";
      default:
        return "§7Unknown";
    }
  }

  @EventHandler
  public void updatePlayerState(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    boolean enabled = ConfigValueCache.getBoolean(plugin, "BrokenGemEnergyUse", false);
    if (enabled
        && (event.getAction() == Action.RIGHT_CLICK_BLOCK
            || event.getAction() == Action.RIGHT_CLICK_AIR)
        && mainHandItem != null
        && BlissItems.isMatchingItem(
            mainHandItem, BlissItems.createRewardItem())) {
      int count = getAbilityIntValue(player);
      if (count == 10) {
        player.sendMessage(
            "🔮 §cYour gem is already "
                + ChatColor.of("#82EDBF")
                + "Pristine +5, §cyou can´t use energy!");
        return;
      }

      String text = this.formatDisplayText(player);
      if (text == null) {
        player.sendMessage("🔮 §cYou don't have any gem to upgrade!");
        return;
      }

      this.completeAbilityAction(player, 1);
      this.spawnAbilityEffects(player);
      String message = this.formatDisplayTextFromConfig(text);
      String displayText = this.formatDisplayTextForAbility(count + 1);
      String accentColor = ChatColor.of("#E4BC74").toString();
      String textColor = ChatColor.of("#9DFFDF").toString();
      String bold = ChatColor.BOLD.toString();
      String labelColor = ChatColor.WHITE.toString();
      player.sendMessage(
          String.format(
              accentColor
                  + "🔮 "
                  + textColor
                  + bold
                  + "ᴇɴᴇʀɢʏ "
                  + labelColor
                  + "absorbed, your gem is now at %s",
              displayText));
      ItemStack heldItem = player.getInventory().getItemInMainHand();
      if (heldItem != null
          && BlissItems.isMatchingItem(
              heldItem, BlissItems.createRewardItem())) {
        if (heldItem.getAmount() > 1) {
          heldItem.setAmount(heldItem.getAmount() - 1);
        } else {
          player.getInventory().setItemInMainHand(null);
        }
      }

      updateAbilityState();
    }
  }

  public void finishAbilityAction(Player player, String text) {
    int count = getAbilityIntValue(player);
    ItemStack item = this.createAbilityItem(text, count);
    if (item != null) {
      this.trackAbilityState(player, item);
    }
  }

  public boolean isConditionMet(String text) {
    return switch (text.toUpperCase()) {
      case "PUFF_T1",
          "PUFF_T2",
          "LIFE_T1",
          "LIFE_T2",
          "WEALTH_T1",
          "WEALTH_T2",
          "STRENGTH_T1",
          "STRENGTH_T2",
          "SPEED_T1",
          "SPEED_T2",
          "FIRE_T1",
          "FIRE_T2",
          "ASTRA_T1",
          "ASTRA_T2",
          "FLUX_T1",
          "FLUX_T2" ->
          true;
      default -> false;
    };
  }

  public List<String> getState() {
    return Arrays.asList(
        "PUFF_T1",
        "PUFF_T2",
        "LIFE_T1",
        "LIFE_T2",
        "WEALTH_T1",
        "WEALTH_T2",
        "STRENGTH_T1",
        "STRENGTH_T2",
        "SPEED_T1",
        "SPEED_T2",
        "FIRE_T1",
        "FIRE_T2",
        "ASTRA_T1",
        "ASTRA_T2",
        "FLUX_T1",
        "FLUX_T2");
  }

  static Map<Integer, String> getStateForPlayer() {
    HashMap<Integer, String> gemTypesByModelData = new HashMap<>();
    updatePrimaryState(gemTypesByModelData, "FIRE_T1", 1, 21, 41, 61, 81);
    updatePrimaryState(gemTypesByModelData, "LIFE", 3, 23, 43, 63, 83);
    updatePrimaryState(gemTypesByModelData, "PUFF", 5, 25, 45, 65, 85);
    updatePrimaryState(gemTypesByModelData, "SPEED", 7, 27, 47, 67, 87);
    updatePrimaryState(gemTypesByModelData, "STRENGTH", 9, 29, 49, 69, 89);
    updatePrimaryState(gemTypesByModelData, "WEALTH", 11, 31, 51, 71, 91);
    updatePrimaryState(gemTypesByModelData, "ASTRA_T1", 13, 33, 53, 73, 93);
    updatePrimaryState(gemTypesByModelData, "FLUX_T1", 97, 117, 137, 157, 177);
    return Collections.unmodifiableMap(gemTypesByModelData);
  }

  static Map<Integer, String> getStateForTarget() {
    HashMap<Integer, String> gemTypesByModelData = new HashMap<>();
    updatePrimaryState(gemTypesByModelData, "FIRE_T2", 2, 22, 42, 62, 82);
    updatePrimaryState(gemTypesByModelData, "LIFE_T2", 4, 24, 44, 64, 84);
    updatePrimaryState(gemTypesByModelData, "PUFF_T2", 6, 26, 46, 66, 86);
    updatePrimaryState(gemTypesByModelData, "SPEED_T2", 8, 28, 48, 68, 88);
    updatePrimaryState(gemTypesByModelData, "STRENGTH_T2", 10, 30, 50, 70, 90);
    updatePrimaryState(gemTypesByModelData, "WEALTH_T2", 12, 32, 52, 72, 92);
    updatePrimaryState(gemTypesByModelData, "ASTRA_T2", 14, 34, 54, 74, 94);
    updatePrimaryState(gemTypesByModelData, "FLUX_T2", 98, 118, 138, 158, 178);
    return Collections.unmodifiableMap(gemTypesByModelData);
  }

  static void updatePrimaryState(Map<Integer, String> gemTypesByModelData, String text, int... count) {
    for (int index : count) {
      gemTypesByModelData.put(index, text);
    }
  }

  String formatDisplayTextForState(ItemStack item) {
    if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      if (itemMeta != null && itemMeta.hasCustomModelData()) {
        int count = itemMeta.getCustomModelData();
        Material material = item.getType();
        if (material == Material.AMETHYST_SHARD) {
          return count == 95 ? this.formatPrimaryDisplayText(item, tierOneGemDefinitions) : tierOneTypesByModelData.get(count);
        } else if (material == Material.PRISMARINE_SHARD) {
          return count == 96 ? this.formatPrimaryDisplayText(item, tierTwoGemDefinitions) : tierTwoTypesByModelData.get(count);
        } else {
          return null;
        }
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  String formatPrimaryDisplayText(ItemStack item, List<GemItemDefinition> entries) {
    for (GemItemDefinition gemItemDefinition : entries) {
      if (BlissItems.isMatchingItem(item, gemItemDefinition.item())) {
        return gemItemDefinition.type();
      }
    }

    return null;
  }

  void clearDroppedEnergyState(Player player) {
    this.playerIds.remove(player.getUniqueId());
  }

  void restoreEnergyStateAfterRespawn(Player player, ItemStack item, UUID playerId) {
    this.cleanupAbilityState(player);
    this.trackAbilityState(player, item);
    this.secondaryValuesByPlayer.remove(playerId);
  }

  static void handlePlayerDeath(Player player) {
    Bukkit.getServer()
        .dispatchCommand(
            Bukkit.getConsoleSender(),
            "ban " + player.getName() + " Your gem broke! You have been banned.");
  }

  static boolean shouldGivePlayerGem(ItemStack item) {
    return item != null && item.getType() != Material.AIR;
  }

  static {
    logger = LoggerFactory.getLogger(GemEnergyManager.class);
    energyByPlayer = new HashMap<>();
    dirtyPlayers = new HashSet<>();
    lastKnownNamesByPlayer = new HashMap<>();
    saveLock = new Object();
    tierOneTypesByModelData = getStateForPlayer();
    tierTwoTypesByModelData = getStateForTarget();
    tierOneGemDefinitions =
        List.of(
            new GemItemDefinition("PUFF", BlissItems.createPuffTierOneGemStage1()),
            new GemItemDefinition("LIFE", BlissItems.createLifeTierOneGemStage1()),
            new GemItemDefinition("WEALTH", BlissItems.createWealthTierOneGemStage1()),
            new GemItemDefinition("STRENGTH", BlissItems.createStrengthTierOneGemStage1()),
            new GemItemDefinition("SPEED", BlissItems.createSpeedTierOneGemStage1()),
            new GemItemDefinition("FIRE_T1", BlissItems.createFireTierOneGemStage1()),
            new GemItemDefinition("ASTRA_T1", BlissItems.createAstraTierOneGemStage1()),
            new GemItemDefinition("FLUX_T1", BlissItems.createFluxTierOneGemStage1()));
    tierTwoGemDefinitions =
        List.of(
            new GemItemDefinition("PUFF_T2", BlissItems.createPuffTierTwoGemStage1()),
            new GemItemDefinition("LIFE_T2", BlissItems.createLifeTierTwoGemStage1()),
            new GemItemDefinition("WEALTH_T2", BlissItems.createWealthTierTwoGemStage1()),
            new GemItemDefinition("STRENGTH_T2", BlissItems.createStrengthTierTwoGemStage1()),
            new GemItemDefinition("SPEED_T2", BlissItems.createSpeedTierTwoGemStage1()),
            new GemItemDefinition("FIRE_T2", BlissItems.createFireTierTwoGemStage1()),
            new GemItemDefinition("ASTRA_T2", BlissItems.createAstraTierTwoGemStage1()),
            new GemItemDefinition("FLUX_T2", BlissItems.createFluxTierTwoGemStage1()));
  }
}
