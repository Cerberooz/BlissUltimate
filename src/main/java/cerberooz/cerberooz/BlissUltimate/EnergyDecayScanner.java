package cerberooz.cerberooz.BlissUltimate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

/** Gradually turns Energy items in loaded inventories and chunks into Useless Energy. */
public final class EnergyDecayScanner {
  private static final int ENERGY_MODEL_DATA = 300;
  private static final int USELESS_ENERGY_MODEL_DATA = 301;
  private static final int CHUNKS_PER_SCAN = 8;

  private static final Map<String, Long> EXPIRY_BY_SLOT = new HashMap<>();
  private static final Map<UUID, Integer> WORLD_CHUNK_CURSOR = new HashMap<>();

  private static long decayDurationMillis;
  private static BukkitTask scanTask;

  private EnergyDecayScanner() {}

  public static void start() {
    Bliss plugin = Bliss.getInstance();
    long configuredMinutes = ConfigValueCache.getLong(plugin, "TurnEnergyToUseless", 60L);
    decayDurationMillis = Math.max(1L, configuredMinutes) * 60L * 1_000L;

    if (scanTask != null && !scanTask.isCancelled()) {
      scanTask.cancel();
    }
    scanTask = SharedScheduler.scheduleRepeating(plugin, EnergyDecayScanner::scan, 100L, 100L);
  }

  public static void stop() {
    if (scanTask != null) {
      scanTask.cancel();
      scanTask = null;
    }
    EXPIRY_BY_SLOT.clear();
    WORLD_CHUNK_CURSOR.clear();
  }

  public static boolean isEnergy(ItemStack item) {
    return hasModelData(item, ENERGY_MODEL_DATA);
  }

  public static boolean isUselessEnergy(ItemStack item) {
    return hasModelData(item, USELESS_ENERGY_MODEL_DATA);
  }

  private static boolean hasModelData(ItemStack item, int expectedModelData) {
    if (item == null || item.getType() != Material.NAUTILUS_SHELL) {
      return false;
    }
    ItemMeta meta = item.getItemMeta();
    return meta != null
        && meta.hasCustomModelData()
        && meta.getCustomModelData() == expectedModelData;
  }

  private static void scan() {
    long now = System.currentTimeMillis();
    scanLoadedChunks(now);

    for (Player player : Bukkit.getOnlinePlayers()) {
      UUID playerId = player.getUniqueId();
      processInventory(player.getInventory(), "player:" + playerId, now);
      processInventory(player.getEnderChest(), "enderchest:" + playerId, now);
    }
  }

  private static void scanLoadedChunks(long now) {
    int remainingChunkBudget = CHUNKS_PER_SCAN;
    Set<UUID> activeWorlds = new HashSet<>();

    for (World world : Bukkit.getWorlds()) {
      if (remainingChunkBudget <= 0) {
        break;
      }

      UUID worldId = world.getUID();
      activeWorlds.add(worldId);
      Chunk[] loadedChunks = world.getLoadedChunks();
      if (loadedChunks.length == 0) {
        WORLD_CHUNK_CURSOR.remove(worldId);
        continue;
      }

      int cursor = WORLD_CHUNK_CURSOR.getOrDefault(worldId, 0);
      if (cursor >= loadedChunks.length) {
        cursor = 0;
      }
      int chunksToScan = Math.min(remainingChunkBudget, loadedChunks.length);
      for (int offset = 0; offset < chunksToScan; offset++) {
        Chunk chunk = loadedChunks[(cursor + offset) % loadedChunks.length];
        scanDroppedEnergy(chunk, now);
        scanContainers(chunk, now);
      }
      WORLD_CHUNK_CURSOR.put(worldId, (cursor + chunksToScan) % loadedChunks.length);
      remainingChunkBudget -= chunksToScan;
    }

    WORLD_CHUNK_CURSOR.keySet().removeIf(worldId -> !activeWorlds.contains(worldId));
  }

  private static void scanDroppedEnergy(Chunk chunk, long now) {
    for (Entity entity : chunk.getEntities()) {
      if (!(entity instanceof Item droppedItem)) {
        continue;
      }
      ItemStack item = droppedItem.getItemStack();
      if (!isEnergy(item)) {
        continue;
      }

      String timerKey = "drop:" + droppedItem.getUniqueId();
      ItemStack processed = processEnergy(item, timerKey, now);
      if (processed == item) {
        continue;
      }
      if (processed != null && !processed.getType().isAir()) {
        droppedItem.setItemStack(processed);
      } else {
        EXPIRY_BY_SLOT.remove(timerKey);
        droppedItem.remove();
      }
    }
  }

  private static void scanContainers(Chunk chunk, long now) {
    for (BlockState state : chunk.getTileEntities()) {
      if (!(state instanceof Container container)) {
        continue;
      }
      Block block = state.getBlock();
      String timerPrefix =
          "container:"
              + block.getWorld().getUID()
              + ':'
              + block.getX()
              + ':'
              + block.getY()
              + ':'
              + block.getZ();
      processInventory(container.getInventory(), timerPrefix, now);
    }
  }

  private static void processInventory(Inventory inventory, String timerPrefix, long now) {
    for (int slot = 0; slot < inventory.getSize(); slot++) {
      ItemStack item = inventory.getItem(slot);
      String timerKey = timerPrefix + ":slot:" + slot;
      if (!isEnergy(item)) {
        EXPIRY_BY_SLOT.remove(timerKey);
        continue;
      }

      ItemStack processed = processEnergy(item, timerKey, now);
      if (processed != item) {
        inventory.setItem(slot, processed);
      }
    }
  }

  private static ItemStack processEnergy(ItemStack item, String timerKey, long now) {
    if (!isEnergy(item)) {
      return item;
    }

    Long expiresAt = EXPIRY_BY_SLOT.get(timerKey);
    if (expiresAt == null) {
      EXPIRY_BY_SLOT.put(timerKey, now + decayDurationMillis);
      return item;
    }
    if (now < expiresAt) {
      return item;
    }

    EXPIRY_BY_SLOT.remove(timerKey);
    ItemStack converted = convertToUselessEnergy(item);
    if (converted == null) {
      return item;
    }
    converted.setAmount(item.getAmount());
    return converted;
  }

  public static ItemStack convertToUselessEnergy(ItemStack source) {
    if (source == null || source.getType().isAir() || source.getItemMeta() == null) {
      return null;
    }

    ItemStack result = new ItemStack(Material.NAUTILUS_SHELL, source.getAmount());
    ItemMeta meta = result.getItemMeta();
    if (meta == null) {
      return null;
    }
    meta.setDisplayName(
        ChatColor.DARK_GRAY
            + ChatColor.BOLD.toString()
            + "ᴇɴᴇʀɢʏ "
            + net.md_5.bungee.api.ChatColor.of("#F6D7B3")
            + "ɪɴ ᴀ ʙᴏᴛᴛʟᴇ");
    meta.setCustomModelData(USELESS_ENERGY_MODEL_DATA);
    meta.setLore(List.of(ChatColor.WHITE + ChatColor.BOLD.toString() + "ᴜѕᴇʟᴇѕѕ"));
    result.setItemMeta(meta);
    return result;
  }

  public static ItemStack createUselessEnergyTradeItem() {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      return null;
    }
    meta.setDisplayName(
        ChatColor.DARK_GRAY
            + ChatColor.BOLD.toString()
            + "ᴇɴᴇʀɢʏ "
            + net.md_5.bungee.api.ChatColor.of("#F6D7B3")
            + "ɪɴ ᴀ ʙᴏᴛᴛʟᴇ");
    meta.setCustomModelData(USELESS_ENERGY_MODEL_DATA);
    meta.setLore(List.of(ChatColor.WHITE + "Left click to trade!"));
    item.setItemMeta(meta);
    return item;
  }
}
