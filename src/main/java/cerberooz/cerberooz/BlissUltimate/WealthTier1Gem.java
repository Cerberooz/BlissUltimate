package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WealthTier1Gem implements Listener {
  final Bliss plugin;
  WealthPocketsManager wealthPocketsManager;
  static Map<UUID, Long> cooldownTimestamps;
  static Map<UUID, Integer> playerCounters;
  static Map<UUID, Integer> abilityStages;
  static Map<UUID, ItemStack> itemsByPlayer;
  static Map<UUID, Long> lastUseTimes;
  static Cache<UUID, Long> cache;
  AstraTier2Gem astraTier2Gem;
  private final Runnable actionBarRenderer = this::updateActionBar;

  public Cache<UUID, Long> getCache() {
    return cache;
  }

  public void setAstraTier2Gem(AstraTier2Gem astraTier2Gem) {
    this.astraTier2Gem = astraTier2Gem;
  }

  boolean isAbilityActive(UUID playerId) {
    return this.astraTier2Gem != null && this.astraTier2Gem.hasRequiredState(playerId);
  }

  long getAbilityLong(UUID playerId) {
    return this.astraTier2Gem == null ? 0L : this.astraTier2Gem.getAbilityLong(playerId);
  }

  public WealthTier1Gem(Bliss bliss) {
    this.plugin = bliss;
    this.wealthPocketsManager = WealthPocketsManager.getInstance(bliss);
    this.initialize();
    this.startBackgroundTasks();
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          final Bliss plugin = bliss;

          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if ((this.plugin.isGemsDisabled())
                  && (WealthTier1Gem.hasRequiredState(mainHandItem) || WealthTier1Gem.hasRequiredState(offHandItem))) {
                String darkGray = ChatColor.DARK_GRAY.toString();
                String bold = ChatColor.BOLD.toString();
                ActionBarQueue.enqueue(
                    player,
                    "🔒 " + darkGray + bold + "ᴅɪꜱᴀʙʟᴇᴅ");
              }

              if (this.plugin.canUseAbility(player)) {
                return;
              }
            }
          }
        },
        bliss,
        SharedScheduler.staggeredInitialDelay(20L),
        20L);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    this.wealthPocketsManager.preload(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    this.wealthPocketsManager.saveAndUnload(playerId);
    cooldownTimestamps.remove(playerId);
    lastUseTimes.remove(playerId);
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getView().getTitle().equals("Pockets")) {
      Bukkit.getScheduler().runTaskLater(this.plugin, this.wealthPocketsManager::saveDirtyPockets, 1L);
    }
  }

  boolean isGemItem(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Material material = item.getType();
      return material == Material.FIREWORK_ROCKET
              || material == Material.WIND_CHARGE
              || material == Material.COBWEB
              || material == Material.SHIELD
              || material == Material.TRIDENT
              || material == Material.FISHING_ROD
              || material == Material.SNOWBALL
              || material == Material.EGG
              || material == Material.ENDER_PEARL
              || material == Material.SPLASH_POTION
              || material == Material.LINGERING_POTION
              || material == Material.LAVA_BUCKET
              || material == Material.WATER_BUCKET
              || material == Material.FLINT_AND_STEEL
              || material == Material.FIRE_CHARGE
              || material == Material.TNT
              || material == Material.END_CRYSTAL
              || material == Material.EXPERIENCE_BOTTLE
              || material == Material.ARROW
              || material == Material.SPECTRAL_ARROW
              || material == Material.TIPPED_ARROW
          ? true
          : material.isEdible();
    } else {
      return false;
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_AIR) {
      Player player = event.getPlayer();
      if (player.isSneaking()) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          if (!this.plugin.isGemsDisabled()
             ) {
            if (!this.plugin.canUseAbility(player)) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if (this.isProtectedTarget(offHandItem, player)) {
                if (ConfigValueCache.getBoolean(this.plugin, "DisablePockets", false)) {
                  return;
                }

                if (!this.isActiveForPlayer(mainHandItem)) {
                  return;
                }

                event.setCancelled(true);
                cooldownTimestamps.remove(playerId);
                Material material = mainHandItem.getType();
                if (material == Material.BOW || material == Material.CROSSBOW) {
                  return;
                }

                if (this.isGemItem(mainHandItem)) {
                  return;
                }

                player.openInventory(this.wealthPocketsManager.getPocket(playerId));
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        boolean enabled = hasRequiredState(mainHandItem) || hasRequiredState(offHandItem);
        if (enabled) {
          if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT1.ancientDebrisBonus", 2);
            ItemStack item = new ItemStack(Material.NETHERITE_SCRAP, configuredValue);
            event.setDropItems(false);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
          }

          if (this.isConditionMet(event.getBlock().getType())) {
            boolean active =
                mainHandItem.containsEnchantment(Enchantment.SILK_TOUCH)
                    || offHandItem.containsEnchantment(Enchantment.SILK_TOUCH);
            if (!active) {
              double distance = ConfigValueCache.getDouble(this.plugin, "wealthT1.oreBonusChance", 0.15);
              if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < distance) {
                ItemStack block = new ItemStack(event.getBlock().getType(), 1);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), block);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    UUID playerId = player.getUniqueId();
    if (this.wealthPocketsManager.isLoaded(playerId)) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          Inventory inventory = this.wealthPocketsManager.getPocket(playerId);
          ArrayList<Integer> entries = new ArrayList<>();

          for (int count = 0; count < inventory.getSize(); count++) {
            ItemStack item = inventory.getItem(count);
            if (item != null && item.getType() != Material.AIR) {
              entries.add(count);
            }
          }

          int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT1.pocketsDeathDropCount", 5);
          int index = Math.min(configuredValue, entries.size());
          Collections.shuffle(entries, java.util.concurrent.ThreadLocalRandom.current());

          for (int remaining = 0; remaining < index; remaining++) {
            int step = entries.get(remaining);
            ItemStack heldItem = inventory.getItem(step);
            if (heldItem != null && heldItem.getType() != Material.AIR) {
              player.getWorld().dropItemNaturally(player.getLocation(), heldItem.clone());
              inventory.setItem(step, null);
            }
          }

          this.wealthPocketsManager.saveDirtyPockets();
        }
      }
    }
  }

  @EventHandler
  public void onPlayerExpChange(PlayerExpChangeEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        boolean enabled = hasRequiredState(mainHandItem) || hasRequiredState(offHandItem);
        if (enabled) {
          int count = event.getAmount();
          if (count > 0 && this.getAbilityInt(playerId) < 0) {
            double value;
            if (this.getDurationTicks(playerId) > 0) {
              value = ConfigValueCache.getDouble(this.plugin, "wealthT1.normalXpMultiplier", 2.0);
            } else {
              value = ConfigValueCache.getDouble(this.plugin, "wealthT1.lowEnergyXpMultiplier", 1.5);
            }

            event.setAmount((int) (count * value));
          }
        }
      }
    }
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              if (!WealthTier1Gem.this.plugin.isGemsDisabled()
                 
                  && !WealthTier1Gem.this.plugin.canUseAbility(player)) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (WealthTier1Gem.hasRequiredState(mainHandItem) || WealthTier1Gem.hasRequiredState(offHandItem)) {
                  WealthTier1Gem.this.activateAbility(player, playerId);
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(100L), 100L);
  }

  void activateAbility(Player player, UUID playerId) {
    Long storedTimestamp = lastUseTimes.get(playerId);
    long now = System.currentTimeMillis();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT1.enchantmentCooldown", 5);
        long timestamp = configuredValue * 1000L;
        if (storedTimestamp == null || now - storedTimestamp > timestamp) {
          int index = ConfigValueCache.getInt(this.plugin, "wealthT1.fortuneLevel", 1);
          int remaining = ConfigValueCache.getInt(this.plugin, "wealthT1.lootingLevel", 1);
          int step = ConfigValueCache.getInt(this.plugin, "wealthT1.mendingLevel", 1);

          for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
              if ((this.shouldApplyEffect(item) || this.canAffectTarget(item))
                  && !item.containsEnchantment(Enchantment.FORTUNE)
                  && !item.containsEnchantment(Enchantment.SILK_TOUCH)) {
                item.addUnsafeEnchantment(Enchantment.FORTUNE, index);
                item.addUnsafeEnchantment(Enchantment.MENDING, step);
              }

              if ((this.isAbilityAllowed(item)
                      || this.isAbilityBlocked(item)
                      || this.isMatchingState(item)
                      || this.isValidTarget(item))
                  && !item.containsEnchantment(Enchantment.MENDING)) {
                item.addUnsafeEnchantment(Enchantment.MENDING, step);
              }

              if (this.isTrackedTarget(item) && !item.containsEnchantment(Enchantment.LOOTING)) {
                item.addUnsafeEnchantment(Enchantment.LOOTING, remaining);
                item.addUnsafeEnchantment(Enchantment.MENDING, step);
              }
            }
          }

          lastUseTimes.put(playerId, now);
        }
      }
    }
  }

  boolean isAbilityAllowed(ItemStack item) {
    return item.getType().name().endsWith("_HELMET");
  }

  boolean isAbilityBlocked(ItemStack item) {
    return item.getType().name().endsWith("_CHESTPLATE");
  }

  boolean isMatchingState(ItemStack item) {
    return item.getType().name().endsWith("_LEGGINGS");
  }

  boolean isValidTarget(ItemStack item) {
    return item.getType().name().endsWith("_BOOTS");
  }

  void initialize() {
    ActionBarQueue.registerRenderer(this.actionBarRenderer);
  }

  public void shutdown() {
    ActionBarQueue.unregisterRenderer(this.actionBarRenderer);
    cooldownTimestamps.clear();
    playerCounters.clear();
    abilityStages.clear();
    itemsByPlayer.clear();
    lastUseTimes.clear();
    cache.invalidateAll();
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    ItemStack item = event.getItemDrop().getItemStack();
    if (hasRequiredState(item)) {
      event.setCancelled(true);
      event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop the Wealth Gem!");
    }
  }

  boolean isProtectedTarget(ItemStack item, Player player) {
    if (item != null && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      return itemMeta.hasCustomModelData()
          && (itemMeta.getCustomModelData() == 11
              || itemMeta.getCustomModelData() == 91
              || itemMeta.getCustomModelData() == 71
              || itemMeta.getCustomModelData() == 51
              || itemMeta.getCustomModelData() == 31);
    } else {
      return false;
    }
  }

  boolean isActiveForPlayer(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Material material = item.getType();
      return material.name().endsWith("_SWORD") || material.name().endsWith("_AXE");
    } else {
      return true;
    }
  }

  public static boolean hasRequiredState(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      if (item.getType() != Material.AMETHYST_SHARD) {
        return false;
      } else {
        return itemMeta.hasCustomModelData()
                && (itemMeta.getCustomModelData() == 11
                    || itemMeta.getCustomModelData() == 91
                    || itemMeta.getCustomModelData() == 71
                    || itemMeta.getCustomModelData() == 51
                    || itemMeta.getCustomModelData() == 31)
            ? itemMeta.hasDisplayName()
                && ChatColor.stripColor(itemMeta.getDisplayName())
                    .equalsIgnoreCase("ᴡᴇᴀʟᴛʜ ɢᴇᴍ")
            : false;
      }
    } else {
      return false;
    }
  }

  boolean isConditionMet(Material material) {
    return material == Material.COAL_ORE
        || material == Material.DEEPSLATE_COAL_ORE
        || material == Material.IRON_ORE
        || material == Material.DEEPSLATE_IRON_ORE
        || material == Material.GOLD_ORE
        || material == Material.DEEPSLATE_GOLD_ORE
        || material == Material.DIAMOND_ORE
        || material == Material.DEEPSLATE_DIAMOND_ORE
        || material == Material.EMERALD_ORE
        || material == Material.DEEPSLATE_EMERALD_ORE
        || material == Material.LAPIS_ORE
        || material == Material.DEEPSLATE_LAPIS_ORE
        || material == Material.REDSTONE_ORE
        || material == Material.DEEPSLATE_REDSTONE_ORE
        || material == Material.COPPER_ORE
        || material == Material.DEEPSLATE_COPPER_ORE
        || material == Material.NETHER_GOLD_ORE
        || material == Material.NETHER_QUARTZ_ORE;
  }

  boolean shouldApplyEffect(ItemStack item) {
    return item.getType().name().endsWith("_PICKAXE");
  }

  boolean canAffectTarget(ItemStack item) {
    return item.getType().name().endsWith("_AXE");
  }

  boolean isTrackedTarget(ItemStack item) {
    return item.getType().name().endsWith("_SWORD");
  }

  boolean isPrimaryGemItem(ItemStack item) {
    String material = item.getType().name();
    return material.endsWith("_HELMET")
        || material.endsWith("_CHESTPLATE")
        || material.endsWith("_LEGGINGS")
        || material.endsWith("_BOOTS");
  }

  boolean isTargetGemItem(ItemStack item) {
    String material = item.getType().name();
    return material.endsWith("_SHOVEL") || material.endsWith("_HOE");
  }

  int getAbilityInt(UUID playerId) {
    return playerCounters.getOrDefault(playerId, 0);
  }

  int getDurationTicks(UUID playerId) {
    return abilityStages.getOrDefault(playerId, 0);
  }

  public static void updateAbilityState(UUID playerId, int count) {
    playerCounters.put(playerId, count);
  }

  public static void refreshAbilityState(UUID playerId, int count) {
    abilityStages.put(playerId, count);
  }

  public static void applyAbilityEffects(UUID playerId, ItemStack item) {
    itemsByPlayer.put(playerId, item);
  }

  public static Map<UUID, Inventory> getState() {
    return WealthPocketsManager.loadedPockets();
  }

  public void playAbilityEffects() {
    this.wealthPocketsManager.saveDirtyPockets();
  }

  public void spawnAbilityEffects() {
    this.wealthPocketsManager.saveAllAndClear();
    cooldownTimestamps.clear();
    lastUseTimes.clear();
  }

  public ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(11);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.of("#befff7") + "Energy:");
      lore.add(ChatColor.of("#82EDBF") + "Pristine");
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      lore.add("");
      lore.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Hero of the Village");
      lore.add(ChatColor.GRAY + "- Luck");
      lore.add(ChatColor.GRAY + "- Enchants Mending");
      lore.add(ChatColor.GRAY + "- Enchants Fortune");
      lore.add(ChatColor.GRAY + "- Enchants Looting");
      lore.add(ChatColor.GRAY + "- Bonus Ores");
      lore.add(ChatColor.GRAY + "- Extra EXP");
      lore.add(ChatColor.GRAY + "- Durability Chip");
      lore.add(ChatColor.GRAY + "- Double Debris");
      lore.add("");
      lore.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      lore.add(ChatColor.GRAY + "  Double right-click to open");
      lore.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  void updateActionBar() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      ItemStack offHandItem = player.getInventory().getItemInOffHand();
      if ((hasRequiredState(mainHandItem) || hasRequiredState(offHandItem))
          && this.isAbilityActive(player.getUniqueId())) {
        long remainingMillis = this.getAbilityLong(player.getUniqueId());
        long timestamp = remainingMillis / 1000L;
        String darkGray = ChatColor.DARK_GRAY.toString();
        String bold = ChatColor.BOLD.toString();
        ActionBarQueue.enqueue(
            player,
            "🔒 "
                + darkGray
                + bold
                + "ᴅɪꜱᴀʙʟᴇᴅ: "
                + timestamp);
      } else if (!this.plugin.isGemsDisabled()
         
          && !this.plugin.canUseAbility(player)
          && (hasRequiredState(offHandItem) || hasRequiredState(mainHandItem))) {
        ActionBarQueue.enqueue(player, ChatColor.DARK_GREEN + "🔺");
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 40, 2, true, true, true), true);
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.LUCK, 40, 0, true, true, true), true);
      }
    }
  }

  static {
    cooldownTimestamps = new HashMap<>();
    playerCounters = new HashMap<>();
    abilityStages = new HashMap<>();
    itemsByPlayer = new HashMap<>();
    lastUseTimes = new HashMap<>();
    cache = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.SECONDS).build();
  }
}
