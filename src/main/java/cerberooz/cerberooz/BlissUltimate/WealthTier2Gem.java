package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class WealthTier2Gem implements Listener {
  static final String WEALTH_UNFORTUNATE_T2_ID;
  static final String WEALTH_RICH_RUSH_T2_ID;
  static final String WEALTH_ITEM_LOCK_T2_ID;
  static final String WEALTH_AMPLIFICATION_T2_ID;
  Bliss plugin;
  WealthPocketsManager wealthPocketsManager;
  static Cache<UUID, Long> secondaryCache;
  static Cache<UUID, Boolean> cachedCache;
  static Cache<UUID, Boolean> cache;
  static Cache<UUID, Boolean> trackedCache;
  static Cache<String, Boolean> pendingCache;
  final Map<UUID, BukkitTask> animationTasks = new ConcurrentHashMap<>();
  final Map<UUID, BukkitTask> scheduledTasks = new ConcurrentHashMap<>();
  static Map<UUID, Player> players;
  final Cache<UUID, Long> primaryCooldownCache =
      CacheBuilder.newBuilder().expireAfterWrite(360L, TimeUnit.SECONDS).build();
  final Cache<UUID, Long> targetCooldownCache = CacheBuilder.newBuilder().build();
  static Map<UUID, Integer> playerCounters;
  static Map<UUID, BossBar> bossBars;
  static Map<UUID, BossBar> abilityBossBars;
  static Map<UUID, BossBar> cooldownBossBars;
  static Cache<UUID, Boolean> currentCache;
  static Cache<UUID, Boolean> activeCache;
  public static Map<UUID, Map<String, Map<Enchantment, Integer>>> savedEnchantmentsByPlayer;
  static Map<UUID, Boolean> flagsByPlayer;
  TrustCommand trustCommand;
  AstraTier2Gem astraTier2Gem;
  final Map<UUID, BukkitTask> cooldownTasks = new ConcurrentHashMap<>();
  private final Runnable actionBarRenderer = this::updateActionBar;

  public void setTrustCommand(TrustCommand trustCommand) {
    this.trustCommand = trustCommand;
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

  boolean canUseAbility(Player player) {
    for (ItemStack item : player.getInventory().getContents()) {
      if (item != null && item.getType() == Material.DRAGON_EGG) {
        return true;
      }
    }

    ItemStack[] heldItem = player.getInventory().getArmorContents();

    for (ItemStack targetItem : heldItem) {
      if (targetItem != null && targetItem.getType() == Material.DRAGON_EGG) {
        return true;
      }
    }

    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    return offHandItem != null && offHandItem.getType() == Material.DRAGON_EGG;
  }

  boolean isAbilityAllowed(UUID playerId, Cache<UUID, Long> cache) {
    Long storedTimestamp = cache.getIfPresent(playerId);
    if (storedTimestamp == null) {
      return false;
    } else {
      long now = storedTimestamp - System.currentTimeMillis();
      if (now <= 0L) {
        cache.invalidate(playerId);
        return false;
      } else {
        return true;
      }
    }
  }

  boolean isAbilityBlocked(UUID playerId) {
    Long storedTimestamp = secondaryCache.getIfPresent(playerId);
    if (storedTimestamp == null) {
      return false;
    } else if (System.currentTimeMillis() >= storedTimestamp) {
      secondaryCache.invalidate(playerId);
      return false;
    } else {
      return true;
    }
  }

  public WealthTier2Gem(Bliss bliss) {
    this.plugin = bliss;
    this.wealthPocketsManager = WealthPocketsManager.getInstance(bliss);
    this.initialize();
    this.initializePrimaryState();
    this.startBackgroundTasks();
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          final Bliss plugin = bliss;

          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if (this.plugin.isGemsDisabled()) {
                if (this.plugin.canUseAbility(player)) {
                  return;
                }

                if (WealthTier2Gem.isStoredGemItem(mainHandItem) || WealthTier2Gem.isStoredGemItem(offHandItem)) {
                  String darkGray = ChatColor.DARK_GRAY.toString();
                  String bold = ChatColor.BOLD.toString();
                  ActionBarQueue.enqueue(
                      player,
                      "🔒 " + darkGray + bold + "ᴅɪꜱᴀʙʟᴇᴅ");
                }
              }
            }
          }
        },
        bliss,
        SharedScheduler.staggeredInitialDelay(20L),
        20L);
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              if (!WealthTier2Gem.this.plugin.isGemsDisabled()
                 
                  && !WealthTier2Gem.this.plugin.canUseAbility(player)) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (!WealthTier2Gem.this.isAbilityActive(player.getUniqueId())
                    && (WealthTier2Gem.isStoredGemItem(mainHandItem) || WealthTier2Gem.isStoredGemItem(offHandItem))) {
                  WealthTier2Gem.this.activateAbility(player, playerId);
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(100L), 100L);
  }

  @EventHandler
  public void onPlayerExpChange(PlayerExpChangeEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      ItemStack offHandItem = player.getInventory().getItemInOffHand();
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          boolean enabled = isStoredGemItem(mainHandItem) || isStoredGemItem(offHandItem);
          if (enabled) {
            int count = event.getAmount();
            if (count > 0) {
              double value = 1.0;
              if (this.isAbilityBlocked(playerId)) {
                value = ConfigValueCache.getDouble(this.plugin, "wealthT2.richRushXpMultiplier", 2.0);
              } else if (this.getAbilityInt(playerId) < 0) {
                if (this.getDurationTicks(playerId) > 0) {
                  value = ConfigValueCache.getDouble(this.plugin, "wealthT2.normalXpMultiplier", 2.0);
                } else {
                  value = ConfigValueCache.getDouble(this.plugin, "wealthT2.lowEnergyXpMultiplier", 1.5);
                }
              }

              event.setAmount((int) (count * value));
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
    if (!this.isAbilityActive(playerId)) {
      if (cachedCache.getIfPresent(playerId) != null) {
        double configuredValue =
            ConfigValueCache.getDouble(this.plugin, "wealthT2.unfortunateBlockBreakChance", 50.0);
        if (java.util.concurrent.ThreadLocalRandom.current().nextInt(100) < configuredValue) {
          event.setCancelled(true);
          player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.2F, 1.0F);
          return;
        }
      }

      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      ItemStack offHandItem = player.getInventory().getItemInOffHand();
      boolean enabled = isStoredGemItem(mainHandItem) || isStoredGemItem(offHandItem);
      if (enabled) {
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
          int index = ConfigValueCache.getInt(this.plugin, "wealthT2.ancientDebrisBonus", 2);
          ItemStack item = new ItemStack(Material.NETHERITE_SCRAP, index);
          event.setDropItems(false);
          event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
        }

        Material block = event.getBlock().getType();
        boolean active = this.isAbilityBlocked(playerId);
        if (this.isConditionMet(block)) {
          this.refreshAbilityState(event, player, block, active);
        }
      }
    }
  }

  boolean isConditionMet(Material material) {
    return material == Material.IRON_ORE
        || material == Material.DEEPSLATE_IRON_ORE
        || material == Material.GOLD_ORE
        || material == Material.DEEPSLATE_GOLD_ORE
        || material == Material.DIAMOND_ORE
        || material == Material.DEEPSLATE_DIAMOND_ORE
        || material == Material.LAPIS_ORE
        || material == Material.DEEPSLATE_LAPIS_ORE
        || material == Material.EMERALD_ORE
        || material == Material.DEEPSLATE_EMERALD_ORE
        || material == Material.REDSTONE_ORE
        || material == Material.DEEPSLATE_REDSTONE_ORE
        || material == Material.COPPER_ORE
        || material == Material.DEEPSLATE_COPPER_ORE
        || material == Material.COAL_ORE
        || material == Material.DEEPSLATE_COAL_ORE;
  }

  void refreshAbilityState(BlockBreakEvent event, Player player, Material material, boolean enabled) {
    UUID playerId = player.getUniqueId();
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    if (!mainHandItem.containsEnchantment(Enchantment.SILK_TOUCH)
        && !offHandItem.containsEnchantment(Enchantment.SILK_TOUCH)) {
      ItemStack item = null;
      switch (material) {
        case IRON_ORE, DEEPSLATE_IRON_ORE -> item = new ItemStack(Material.RAW_IRON, 1);
        case GOLD_ORE, DEEPSLATE_GOLD_ORE -> item = new ItemStack(Material.RAW_GOLD, 1);
        case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> item = new ItemStack(Material.DIAMOND, 1);
        case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> item = new ItemStack(Material.LAPIS_LAZULI, 3);
        case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> item = new ItemStack(Material.EMERALD, 1);
        case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> item = new ItemStack(Material.REDSTONE, 3);
        case COPPER_ORE, DEEPSLATE_COPPER_ORE -> item = new ItemStack(Material.RAW_COPPER, 1);
        case COAL_ORE, DEEPSLATE_COAL_ORE -> item = new ItemStack(Material.COAL, 1);
      }

      if (item != null) {
        if (enabled) {
          event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
        } else {
          int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT2.oreEveryNthMine", 3);
          int count = playerCounters.getOrDefault(playerId, 0);
          if (count < configuredValue + 1) {
            count++;
          }

          if (count == configuredValue) {
            playerCounters.put(playerId, 0);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
          } else {
            playerCounters.put(playerId, count);
          }
        }
      }
    }
  }

  void activateAbility(Player player, UUID playerId) {
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        for (ItemStack item : player.getInventory().getContents()) {
          if (item != null && item.getType() != Material.AIR) {
            if ((this.isActiveForPlayer(item) || this.hasRequiredState(item))
                && !item.containsEnchantment(Enchantment.FORTUNE)
                && !item.containsEnchantment(Enchantment.SILK_TOUCH)) {
              item.addUnsafeEnchantment(Enchantment.FORTUNE, 3);
              item.addUnsafeEnchantment(Enchantment.MENDING, 1);
            }

            if ((this.isGemItem(item)
                    || this.isMatchingState(item)
                    || this.isValidTarget(item)
                    || this.isProtectedTarget(item))
                && !item.containsEnchantment(Enchantment.MENDING)) {
              item.addUnsafeEnchantment(Enchantment.MENDING, 1);
            }

            if (this.shouldApplyEffect(item) && !item.containsEnchantment(Enchantment.LOOTING)) {
              item.addUnsafeEnchantment(Enchantment.LOOTING, 3);
              item.addUnsafeEnchantment(Enchantment.MENDING, 1);
            }
          }
        }
      }
    }
  }

  boolean isGemItem(ItemStack item) {
    return item.getType().name().endsWith("_HELMET");
  }

  boolean isMatchingState(ItemStack item) {
    return item.getType().name().endsWith("_CHESTPLATE");
  }

  boolean isValidTarget(ItemStack item) {
    return item.getType().name().endsWith("_LEGGINGS");
  }

  boolean isProtectedTarget(ItemStack item) {
    return item.getType().name().endsWith("_BOOTS");
  }

  boolean isActiveForPlayer(ItemStack item) {
    return item.getType().name().endsWith("_PICKAXE");
  }

  boolean hasRequiredState(ItemStack item) {
    return item.getType().name().endsWith("_AXE");
  }

  boolean shouldApplyEffect(ItemStack item) {
    return item.getType().name().endsWith("_SWORD");
  }

  void initialize() {
    int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT2.unfortunateDuration", 40);
    int index = ConfigValueCache.getInt(this.plugin, "wealthT2.richRushDuration", 300);
    int remaining = ConfigValueCache.getInt(this.plugin, "wealthT2.itemLockDuration", 30);
    int step = ConfigValueCache.getInt(this.plugin, "wealthT2.amplificationDuration", 30);
    secondaryCache = CacheBuilder.newBuilder().expireAfterWrite(index, TimeUnit.SECONDS).build();
    cachedCache = CacheBuilder.newBuilder().expireAfterWrite(configuredValue, TimeUnit.SECONDS).build();
    cache = CacheBuilder.newBuilder().expireAfterWrite(step, TimeUnit.SECONDS).build();
    trackedCache = CacheBuilder.newBuilder().expireAfterWrite(500L, TimeUnit.MILLISECONDS).build();
    pendingCache = CacheBuilder.newBuilder().expireAfterWrite(remaining, TimeUnit.SECONDS).build();
    currentCache = CacheBuilder.newBuilder().build();
    activeCache = CacheBuilder.newBuilder().build();
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          Entity entity = event.getRightClicked();
          if (this.getAbilityInt(playerId) < 1) {
            if (this.getDurationTicks(playerId) > 1) {
              if (this.isPrimaryGemItem(player.getInventory().getItemInMainHand(), player)) {
                if (currentCache.getIfPresent(playerId) == null) {
                  if (!this.isAbilityAllowed(playerId, this.primaryCooldownCache)) {
                    String accentColor = ChatColor.of("#0EC912").toString();
                    String messageColor = ChatColor.of("#B8FFFB").toString();
                    String displayColor = ChatColor.of("#0EC912").toString();
                    String textColor = ChatColor.of("#B8FFFB").toString();
                    String labelColor = ChatColor.of("#0EC912").toString();
                    String name = entity.getName();
                    player.sendMessage(
                        accentColor
                            + "🔮 "
                            + messageColor
                            + "You have used "
                            + displayColor
                            + "Amplification "
                            + textColor
                            + "on "
                            + labelColor
                            + name);
                    Location location = entity.getLocation();
                    int configuredValue =
                        ConfigValueCache.getInt(this.plugin, "wealthT2.amplificationCooldown", 360);
                    long timestamp = this.canUseAbility(player) ? configuredValue / 2L : configuredValue;
                    int index =
                        ConfigValueCache.getInt(this.plugin, "wealthT2.amplificationDuration", 30);
                    ActiveAbilityStore.startActive(playerId, "wealth_amplification_t2", index, timestamp);
                    long lastUpdateTime = (index + timestamp) * 1000L;
                    this.primaryCooldownCache.put(playerId, System.currentTimeMillis() + lastUpdateTime);
                    this.updatePlayerState(player.getLocation());
                    this.handleAbilityAction(player, location);
                  }
                }
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
    this.onPrimaryPlayerDeath(event, playerId);
    this.scheduleAbilityUpdate(player);
    this.applyAbilityEffects(player, playerId);
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        UUID targetId = player.getUniqueId();
        BukkitTask task = this.animationTasks.remove(targetId);
        if (task != null) {
          task.cancel();
        }

        BukkitTask scheduledTask = this.scheduledTasks.remove(targetId);
        if (scheduledTask != null) {
          scheduledTask.cancel();
        }

        if (!this.isAbilityActive(targetId)) {
          if (this.wealthPocketsManager.isLoaded(playerId)) {
            Inventory inventory = this.wealthPocketsManager.getPocket(playerId);
            int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT2.pocketsDeathDropCount", 5);

            for (int count = 0; count < configuredValue; count++) {
              int index = java.util.concurrent.ThreadLocalRandom.current().nextInt(9);
              ItemStack item = inventory.getItem(index);
              if (item != null && item.getType() != Material.AIR) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                inventory.setItem(index, null);
              }
            }

            this.wealthPocketsManager.saveDirtyPockets();
          }
        }
      }
    }
  }

  boolean canAffectTarget(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Material material = item.getType();
      return material == Material.FIREWORK_ROCKET
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
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          UUID playerId = player.getUniqueId();
          if (!this.isAbilityActive(playerId)) {
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (this.isPrimaryGemItem(offHandItem, player)) {
              if (player.isSneaking()) {
                if (!ConfigValueCache.getBoolean(this.plugin, "DisablePockets", false)) {
                  Material targetMaterial = player.getInventory().getItemInMainHand().getType();
                  if (targetMaterial != Material.BOW && targetMaterial != Material.CROSSBOW) {
                    if (!this.canAffectTarget(heldItem)) {
                      player.openInventory(this.wealthPocketsManager.getPocket(playerId));
                    }
                  }
                }
              }
            } else if (isStoredGemItem(mainHandItem)) {
                if (this.getAbilityInt(playerId) >= 1) {
                  return;
                }

                if (this.getDurationTicks(playerId) <= 1) {
                  return;
                }

                if (!this.isPrimaryGemItem(event.getItem(), player)) {
                  return;
                }

                if (currentCache.getIfPresent(playerId) != null) {
                  return;
                }

                if (this.isAbilityAllowed(playerId, this.primaryCooldownCache)) {
                  return;
                }

                player.sendMessage(
                    ChatColor.of("#0EC912")
                        + "🔮 "
                        + ChatColor.of("#B8FFFB")
                        + "You have used "
                        + ChatColor.of("#0EC912")
                        + "Rich Rush");
                int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT2.richRushCooldown", 360);
                long timestamp = this.canUseAbility(player) ? configuredValue / 2L : configuredValue;
                int index = ConfigValueCache.getInt(this.plugin, "wealthT2.richRushDuration", 300);
                ActiveAbilityStore.startActive(playerId, "wealth_rich_rush_t2", index, timestamp);
                long lastUpdateTime = (index + timestamp) * 1000L;
                this.primaryCooldownCache.put(playerId, System.currentTimeMillis() + lastUpdateTime);
                long now = System.currentTimeMillis() + index * 1000L;
                secondaryCache.put(playerId, now);
                this.updateBossBar(playerId, player);
              }
            
          }
        }
      }
    }
  }

  void updateBossBar(UUID playerId, Player player) {
    BukkitTask task = this.cooldownTasks.remove(playerId);
    if (task != null && !task.isCancelled()) {
      task.cancel();
    }

    BossBar bossBar = cooldownBossBars.remove(playerId);
    if (bossBar != null) {
      bossBar.removePlayer(player);
    }

    BossBar abilityBossBar =
        Bukkit.createBossBar(
            org.bukkit.ChatColor.GREEN + "Rich Rush",
            BarColor.GREEN,
            BarStyle.SOLID);
    abilityBossBar.setProgress(1.0);
    abilityBossBar.addPlayer(player);
    cooldownBossBars.put(playerId, abilityBossBar);
    int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT2.richRushDuration", 300);
    int count = configuredValue * 20;
    BukkitTask scheduledTask =
        new BukkitRunnable() {
          final int durationTicks = count;
          final Player capturedPlayer = player;
          final BossBar bossBar = abilityBossBar;
          final UUID capturedPlayerId = playerId;
          int maxCount = 0;
          static final String RICH_RUSH_ID = "Rich Rush";
          @Override
          public void run() {
            if (this.maxCount < this.durationTicks && this.capturedPlayer.isOnline()) {
              double value = 1.0 - (double) this.maxCount / this.durationTicks;
              this.bossBar.setProgress(Math.max(0.0, value));
              this.bossBar.setTitle(String.format(ChatColor.GREEN + RICH_RUSH_ID));
              this.maxCount++;
            } else {
              this.bossBar.removePlayer(this.capturedPlayer);
              WealthTier2Gem.cooldownBossBars.remove(this.capturedPlayerId);
              WealthTier2Gem.this.cooldownTasks.remove(this.capturedPlayerId);
              WealthTier2Gem.secondaryCache.invalidate(this.capturedPlayerId);
              this.cancel();
            }
          }


        }
            .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
    this.cooldownTasks.put(playerId, scheduledTask);
  }

  @EventHandler
  public void updateAbilityState(PlayerInteractEvent event) {
    if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
      Player player = event.getPlayer();
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          UUID playerId = player.getUniqueId();
          if (!this.isAbilityActive(playerId)) {
            if (this.getAbilityInt(playerId) < 1) {
              if (this.getDurationTicks(playerId) > 1) {
                if (this.isPrimaryGemItem(event.getItem(), player)) {
                  if (activeCache.getIfPresent(playerId) == null) {
                    if (!this.isAbilityAllowed(playerId, this.targetCooldownCache)) {
                      if (this.isTrackedTarget(
                          player, player.getInventory().getItemInMainHand().getType())) {
                        event.setCancelled(true);
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.2F, 1.0F);
                      } else {
                        String accentColor = ChatColor.of("#0EC912").toString();
                        String messageColor = ChatColor.of("#B8FFFB").toString();
                        String textColor = ChatColor.WHITE.toString();
                        String displayColor = ChatColor.of("#0EC912").toString();
                        player.sendMessage(
                            accentColor
                                + "🔮 "
                                + messageColor
                                + "You have used "
                                + textColor
                                + "🍀"
                                + displayColor
                                + "Item Lock");
                        int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT2.itemLockCooldown", 90);
                        long timestamp = this.canUseAbility(player) ? configuredValue / 2L : configuredValue;
                        long lastUpdateTime = timestamp * 1000L;
                        this.targetCooldownCache.put(playerId, System.currentTimeMillis() + lastUpdateTime);
                        int index = ConfigValueCache.getInt(this.plugin, "wealthT2.itemLockDuration", 30);
                        ActiveAbilityStore.startActive(playerId, "wealth_item_lock_t2", index, timestamp);
                        this.finishAbilityAction(player.getLocation());
                        this.refreshPlayerState(player);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (savedEnchantmentsByPlayer.containsKey(playerId)) {
      this.applyAbilityEffects(player, playerId);
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
          if (player.isOnline()) {
            this.scheduleAbilityUpdate(player);
          }

        }, 1L);
    }
  }

  public void sendAbilityFeedback(Player player, String text) {
    if (player != null) {
      UUID playerId = player.getUniqueId();
      ActiveAbilityStore.removeAbility(playerId, "wealth_amplification_t2");
      if (cache.getIfPresent(playerId) != null || savedEnchantmentsByPlayer.containsKey(playerId)) {
        this.applyAbilityEffects(player, playerId);
        players.remove(playerId);
        flagsByPlayer.remove(playerId);
        if (text != null && !text.isEmpty()) {
          player.sendMessage(ChatColor.RED + text);
        }
      }
    }
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      Player player = event.getEntity().getKiller();
      if (player != null) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          if (!this.plugin.isGemsDisabled()
             ) {
            if (!this.plugin.canUseAbility(player)) {
              if (this.isAbilityBlocked(playerId)) {
                List<ItemStack> entries = this.getState(event.getEntity());
                ArrayList<ItemStack> currentEntries = new ArrayList<>();

                for (ItemStack item : event.getDrops()) {
                  if (item != null && !item.getType().isAir()) {
                    int count = this.getAbilityIntValue(item, entries);
                    int index = item.getAmount() - count;
                    if (index > 0) {
                      ItemStack heldItem = item.clone();
                      heldItem.setAmount(index);
                      currentEntries.add(heldItem);
                    }
                  }
                }

                if (!currentEntries.isEmpty()) {
                  event.getDrops().addAll(currentEntries);
                }

                DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 166, 44), 1.5F);
                event.getEntity()
                    .getLocation()
                    .getWorld()
                    .spawnParticle(
                        Particle.DUST,
                        event.getEntity().getLocation(),
                        10,
                        0.3,
                        0.3,
                        0.3,
                        0.0,
                        dustOptions);
                event.setDroppedExp(event.getDroppedExp() * 2);
              }
            }
          }
        }
      }
    }
  }

  List<ItemStack> getState(Entity entity) {
    ArrayList<ItemStack> entries = new ArrayList<>();
    if (entity instanceof LivingEntity livingEntity) {
      EntityEquipment entityEquipment = livingEntity.getEquipment();
      if (entityEquipment != null) {
        this.updateState(entries, entityEquipment.getItemInMainHand());
        this.updateState(entries, entityEquipment.getItemInOffHand());

        for (ItemStack item : entityEquipment.getArmorContents()) {
          this.updateState(entries, item);
        }
      }
    }

    if (entity instanceof LivingEntity && entity instanceof InventoryHolder inventoryHolder) {
      for (ItemStack heldItem : inventoryHolder.getInventory().getContents()) {
        this.updateState(entries, heldItem);
      }
    }

    return entries;
  }

  void updateState(List<ItemStack> entries, ItemStack item) {
    if (item != null && !item.getType().isAir()) {
      entries.add(item.clone());
    }
  }

  int getAbilityIntValue(ItemStack item, List<ItemStack> entries) {
    int count = item.getAmount();
    int index = 0;

    for (ItemStack heldItem : entries) {
      if (count <= 0) {
        break;
      }

      if (heldItem != null && !heldItem.getType().isAir() && heldItem.getAmount() > 0 && heldItem.isSimilar(item)) {
        int remaining = Math.min(count, heldItem.getAmount());
        count -= remaining;
        index += remaining;
        heldItem.setAmount(heldItem.getAmount() - remaining);
      }
    }

    return index;
  }

  void applyAbilityEffects(Player player, UUID playerId) {
    Map<String, Map<Enchantment,Integer>> enchantmentsByItem = savedEnchantmentsByPlayer.get(playerId);
    if (enchantmentsByItem != null && !enchantmentsByItem.isEmpty()) {
      try {
        PlayerInventory playerInventory = player.getInventory();

        for (int count = 0; count < 36; count++) {
          ItemStack item = playerInventory.getItem(count);
          if (item != null && item.getType() != Material.AIR && !isStoredGemItem(item)) {
            String text = this.formatDisplayText(item);
            if (text != null && enchantmentsByItem.containsKey(text)) {
              this.completeAbilityAction(item, enchantmentsByItem.get(text));
              playerInventory.setItem(count, item);
            }
          }
        }

        ItemStack[] heldItem = playerInventory.getArmorContents();
        boolean enabled = false;

        for (int index = 0; index < heldItem.length; index++) {
          ItemStack targetItem = heldItem[index];
          if (targetItem != null && targetItem.getType() != Material.AIR && !isStoredGemItem(targetItem)) {
            String message = this.formatDisplayText(targetItem);
            if (message != null && enchantmentsByItem.containsKey(message)) {
              this.completeAbilityAction(targetItem, enchantmentsByItem.get(message));
              heldItem[index] = targetItem;
              enabled = true;
            }
          }
        }

        if (enabled) {
          playerInventory.setArmorContents(heldItem);
        }

        ItemStack offHandItem = playerInventory.getItemInOffHand();
        if (offHandItem != null && offHandItem.getType() != Material.AIR && !isStoredGemItem(offHandItem)) {
          String displayText = this.formatDisplayText(offHandItem);
          if (displayText != null && enchantmentsByItem.containsKey(displayText)) {
            this.completeAbilityAction(offHandItem, enchantmentsByItem.get(displayText));
            playerInventory.setItemInOffHand(offHandItem);
          }
        }

        player.saveData();
      } catch (Exception exception) {
        this.plugin
            .getLogger()
            .log(java.util.logging.Level.WARNING, "Could not apply the Wealth NPC state", exception);
      } finally {
        savedEnchantmentsByPlayer.remove(playerId);
        cache.invalidate(playerId);
        players.remove(playerId);
      }
    } else {
      savedEnchantmentsByPlayer.remove(playerId);
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (event.getEntity() instanceof Player targetPlayer) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          if (!this.plugin.isGemsDisabled()
             ) {
            if (!this.plugin.isGemsDisabled()
               ) {
              if (!this.plugin.canUseAbility(player)) {
                if (!this.plugin.canUseAbility(targetPlayer)) {
                  UUID targetId = player.getUniqueId();
                  UUID ownerId = targetPlayer.getUniqueId();
                  if (this.isTrackedTarget(
                      player, player.getInventory().getItemInMainHand().getType())) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.2F, 1.0F);
                  } else {
                    if (cachedCache.getIfPresent(targetId) != null) {
                      double configuredValue =
                          ConfigValueCache.getDouble(
                              this.plugin, "wealthT2.unfortunateAttackChance", 50.0);
                      if (java.util.concurrent.ThreadLocalRandom.current().nextInt(100) < configuredValue) {
                        event.setCancelled(true);
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.2F, 1.0F);
                        return;
                      }
                    }

                    if (this.getAbilityInt(targetId) < 1) {
                      if (this.getDurationTicks(targetId) > 1) {
                        if (this.isPrimaryGemItem(player.getInventory().getItemInMainHand(), player)) {
                          if (this.isAbilityAllowed(targetId, this.targetCooldownCache)) {
                            if (!ActiveAbilityStore.isActive(targetId, "wealth_unfortunate_t2")) {
                              player.sendMessage(
                                  "§2🔮 §cYour"
                                      + " §a🍀§2Unfortunate §cskill is on"
                                      + " cooldown");
                            }
                          } else if (this.isPrimaryAbilityActive(targetId, ownerId)) {
                            player.sendMessage(
                                "§2🔮 §fYou cannot cast negative powers on"
                                    + " allies!");
                          } else {
                            int index =
                                ConfigValueCache.getInt(this.plugin, "wealthT2.unfortunateCooldown", 90);
                            long timestamp = this.canUseAbility(player) ? index / 2L : index;
                            int remaining =
                                ConfigValueCache.getInt(this.plugin, "wealthT2.unfortunateDuration", 40);
                            ActiveAbilityStore.startActive(
                                targetId, "wealth_unfortunate_t2", remaining, timestamp);
                            long lastUpdateTime = (remaining + timestamp) * 1000L;
                            this.targetCooldownCache.put(targetId, System.currentTimeMillis() + lastUpdateTime);
                            player.sendMessage(
                                "§2🔮 §bYou used"
                                    + " §a🍀§2Unfortunate §bskill on"
                                    + " §2"
                                    + targetPlayer.getName());
                            targetPlayer.sendMessage(
                                "§2🔮 §bYou have been affected with"
                                    + " §a🍀§2Unfortunate §bby §e"
                                    + player.getName());
                            cachedCache.put(ownerId, true);
                            this.processAbilityState(targetId, 3, player);
                            this.playAbilityEffects(targetId, ownerId);
                            this.activatePrimaryAbility(player, targetPlayer);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (cachedCache.getIfPresent(playerId) != null) {
        double configuredValue = ConfigValueCache.getDouble(this.plugin, "wealthT2.unfortunateEatChance", 50.0);
        if (java.util.concurrent.ThreadLocalRandom.current().nextInt(100) < configuredValue) {
          event.setCancelled(true);
          player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.2F, 1.0F);
        }
      }
    }
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (cachedCache.getIfPresent(playerId) != null) {
        double configuredValue = ConfigValueCache.getDouble(this.plugin, "wealthT2.unfortunatePlaceChance", 50.0);
        if (java.util.concurrent.ThreadLocalRandom.current().nextInt(100) < configuredValue) {
          event.setCancelled(true);
          player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.2F, 1.0F);
        }
      }
    }
  }

  @EventHandler
  public void onEntityShootBow(EntityShootBowEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        if (cachedCache.getIfPresent(playerId) != null) {
          double value = ConfigValueCache.getInt(this.plugin, "wealthT2.unfortunateBowChance", 50);
          if (java.util.concurrent.ThreadLocalRandom.current().nextInt(100) < value) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.2F, 1.0F);
          }
        }
      }
    }
  }

  void playAbilityEffects(UUID playerId, UUID targetId) {
    Player player = Bukkit.getPlayer(targetId);
    if (player != null) {
      BukkitTask task = this.animationTasks.remove(targetId);
      if (task != null && !task.isCancelled()) {
        task.cancel();
      }

      BossBar bossBar = bossBars.remove(targetId);
      if (bossBar != null) {
        bossBar.removePlayer(player);
      }

      BossBar abilityBossBar =
          Bukkit.createBossBar("Unfortunate", BarColor.GREEN, BarStyle.SOLID);
      abilityBossBar.setProgress(1.0);
      abilityBossBar.addPlayer(player);
      bossBars.put(targetId, abilityBossBar);
      int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT2.unfortunateDuration", 40);
      int count = configuredValue * 20;
      BukkitTask scheduledTask =
          new BukkitRunnable() {
            final int maxCount = count;
            final BossBar bossBar = abilityBossBar;
            final UUID playerId = targetId;
            int durationTicks = 0;

            @Override
            public void run() {
              if (this.durationTicks >= this.maxCount) {
                this.bossBar.removePlayer(player);
                WealthTier2Gem.bossBars.remove(this.playerId);
                WealthTier2Gem.this.animationTasks.remove(this.playerId);
                this.cancel();
              } else {
                double value = 1.0 - (double) this.durationTicks / this.maxCount;
                this.bossBar.setProgress(Math.max(0.0, value));
                this.durationTicks++;
              }
            }
          }
              .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
      this.animationTasks.put(targetId, scheduledTask);
    }
  }

  void spawnAbilityEffects(UUID playerId, Player player) {
    BukkitTask task = this.scheduledTasks.remove(playerId);
    if (task != null && !task.isCancelled()) {
      task.cancel();
    }

    BossBar bossBar = abilityBossBars.remove(playerId);
    if (bossBar != null) {
      bossBar.removePlayer(player);
    }

    BossBar abilityBossBar =
        Bukkit.createBossBar("Item Lock", BarColor.GREEN, BarStyle.SOLID);
    abilityBossBar.setProgress(1.0);
    abilityBossBar.addPlayer(player);
    abilityBossBars.put(playerId, abilityBossBar);
    int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT2.itemLockDuration", 30);
    int count = configuredValue * 20;
    BukkitTask scheduledTask =
        new BukkitRunnable() {
          final int durationTicks = count;
          final BossBar bossBar = abilityBossBar;
          int maxCount = 0;

          @Override
          public void run() {
            if (this.maxCount >= this.durationTicks) {
              this.bossBar.removePlayer(player);
              WealthTier2Gem.abilityBossBars.remove(playerId);
              WealthTier2Gem.this.scheduledTasks.remove(playerId);
              this.cancel();
            } else {
              double value = 1.0 - (double) this.maxCount / this.durationTicks;
              this.bossBar.setProgress(Math.max(0.0, value));
              this.maxCount++;
            }
          }
        }
            .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
    this.scheduledTasks.put(playerId, scheduledTask);
  }

  boolean isTrackedTarget(Player player, Material material) {
    String cacheKey = player.getUniqueId() + ":" + material.name();
    return pendingCache.getIfPresent(cacheKey) != null;
  }

  void cleanupAbilityState(UUID playerId, Material material) {
    String text = playerId + ":" + material.name();
    pendingCache.put(text, true);
    Player player = Bukkit.getPlayer(playerId);
    if (player != null) {
      int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT2.itemLockDuration", 30);
      int count = configuredValue * 20;
      player.setCooldown(material, count);
    }
  }

  void scheduleAbilityUpdate(Player player) {
    UUID playerId = player.getUniqueId();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        Map<String, Map<Enchantment,Integer>> enchantmentsByItem = savedEnchantmentsByPlayer.get(playerId);
        if (enchantmentsByItem != null && !enchantmentsByItem.isEmpty()) {
          player.sendMessage(
              ChatColor.of("#0EC912")
                  + "🔮 "
                  + ChatColor.of("#B8FFFB")
                  + "Amplification has worn off!");
          ActiveAbilityStore.removeAbility(playerId, "wealth_amplification_t2");
          PlayerInventory playerInventory = player.getInventory();

          for (int count = 0; count < 36; count++) {
            ItemStack item = playerInventory.getItem(count);
            if (item != null && item.getType() != Material.AIR && !isStoredGemItem(item)) {
              String text = this.formatDisplayText(item);
              if (text != null && enchantmentsByItem.containsKey(text)) {
                this.completeAbilityAction(item, enchantmentsByItem.get(text));
                player.updateInventory();
              }
            }
          }

          ItemStack heldItem = playerInventory.getBoots();
          if (heldItem != null && heldItem.getType() != Material.AIR && !isStoredGemItem(heldItem)) {
            String message = this.formatDisplayText(heldItem);
            if (message != null && enchantmentsByItem.containsKey(message)) {
              this.completeAbilityAction(heldItem, enchantmentsByItem.get(message));
            }
          }

          ItemStack targetItem = playerInventory.getLeggings();
          if (targetItem != null && targetItem.getType() != Material.AIR && !isStoredGemItem(targetItem)) {
            String displayText = this.formatDisplayText(targetItem);
            if (displayText != null && enchantmentsByItem.containsKey(displayText)) {
              this.completeAbilityAction(targetItem, enchantmentsByItem.get(displayText));
            }
          }

          ItemStack candidateItem = playerInventory.getChestplate();
          if (candidateItem != null && candidateItem.getType() != Material.AIR && !isStoredGemItem(candidateItem)) {
            String formattedText = this.formatDisplayText(candidateItem);
            if (formattedText != null && enchantmentsByItem.containsKey(formattedText)) {
              this.completeAbilityAction(candidateItem, enchantmentsByItem.get(formattedText));
            }
          }

          ItemStack secondaryItem = playerInventory.getHelmet();
          if (secondaryItem != null && secondaryItem.getType() != Material.AIR && !isStoredGemItem(secondaryItem)) {
            String label = this.formatDisplayText(secondaryItem);
            if (label != null && enchantmentsByItem.containsKey(label)) {
              this.completeAbilityAction(secondaryItem, enchantmentsByItem.get(label));
            }
          }

          ItemStack offHandItem = playerInventory.getItemInOffHand();
          if (offHandItem != null && offHandItem.getType() != Material.AIR && !isStoredGemItem(offHandItem)) {
            String name = this.formatDisplayText(offHandItem);
            if (name != null && enchantmentsByItem.containsKey(name)) {
              this.completeAbilityAction(offHandItem, enchantmentsByItem.get(name));
            }
          }

          savedEnchantmentsByPlayer.remove(playerId);
          cache.invalidate(playerId);
          player.updateInventory();
        }
      }
    }
  }

  void completeAbilityAction(ItemStack item, Map<Enchantment, Integer> enchantmentLevels) {
    HashMap<Enchantment, Integer> currentEnchantmentLevels = new HashMap<>(item.getEnchantments());

    for (Enchantment enchantment : currentEnchantmentLevels.keySet()) {
      item.removeEnchantment(enchantment);
    }

    for (Entry<Enchantment, Integer> entry : enchantmentLevels.entrySet()) {
      item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
    }
  }

  void processAbilityState(UUID playerId, int count, Player player) {
    boolean enabled = this.canUseAbility(player);
    if (count == 2) {
      long timestamp = ConfigValueCache.getLong(this.plugin, "wealthT2.unfortunateCooldown", 420L);
      long lastUpdateTime = enabled ? timestamp * 500L : timestamp * 1000L;
      currentCache = CacheBuilder.newBuilder().expireAfterWrite(lastUpdateTime, TimeUnit.MILLISECONDS).build();
      currentCache.put(playerId, true);
    } else if (count == 3) {
      long startTime = ConfigValueCache.getLong(this.plugin, "wealthT2.unfortunateCooldown", 360L);
      long expiryTime = enabled ? startTime * 500L : startTime * 1000L;
      activeCache = CacheBuilder.newBuilder().expireAfterWrite(expiryTime, TimeUnit.MILLISECONDS).build();
      activeCache.put(playerId, true);
    }
  }

  boolean isPrimaryGemItem(ItemStack item, Player player) {
    if (item == null || item.getType() != Material.PRISMARINE_SHARD) {
      return false;
    } else if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
      int customModelData = item.getItemMeta().getCustomModelData();
      return customModelData == 12 || customModelData == 32 || customModelData == 52 || customModelData == 72 || customModelData == 92;
    } else {
      return false;
    }
  }

  boolean isTargetGemItem(ItemStack item, Player player) {
    return item != null && item.equals(this.createAbilityItem(player.getUniqueId()));
  }

  boolean isSourceGemItem(ItemStack item) {
    return item != null && item.getType() == Material.PRISMARINE_SHARD || item.getType() == Material.AMETHYST_SHARD;
  }

  int getAbilityInt(UUID playerId) {
    return 0;
  }

  int getDurationTicks(UUID playerId) {
    return 6;
  }

  boolean isPrimaryAbilityActive(UUID playerId, UUID targetId) {
    return this.trustCommand != null && this.trustCommand.isAbilityActive(playerId, targetId);
  }

  ItemStack createItem(UUID playerId) {
    return null;
  }

  ItemStack createAbilityItem(UUID playerId) {
    return null;
  }

  boolean meetsPrimaryCondition() {
    return false;
  }

  boolean meetsTargetCondition() {
    return false;
  }

  public static boolean canPrimaryUseAbility(Player player) {
    return players.containsKey(player.getUniqueId());
  }

  public boolean isActiveGemItem(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Map<Enchantment, Integer> enchantmentLevels = item.getEnchantments();
      if (enchantmentLevels.isEmpty()) {
        return false;
      }

      for (Entry<Enchantment, Integer> entry : enchantmentLevels.entrySet()) {
        Enchantment enchantment = entry.getKey();
        int count = entry.getValue();
        if (count > 5) {
          return true;
        }

        if ((enchantment == Enchantment.INFINITY
                || enchantment == Enchantment.SILK_TOUCH
                || enchantment == Enchantment.CHANNELING
                || enchantment == Enchantment.AQUA_AFFINITY
                || enchantment == Enchantment.MULTISHOT
                || enchantment == Enchantment.FLAME)
            && count > 1) {
          return true;
        }

        if ((enchantment == Enchantment.FIRE_ASPECT || enchantment == Enchantment.PUNCH) && count > 2) {
          return true;
        }

        if ((enchantment == Enchantment.UNBREAKING
                || enchantment == Enchantment.LOOTING
                || enchantment == Enchantment.SWEEPING_EDGE
                || enchantment == Enchantment.DEPTH_STRIDER
                || enchantment == Enchantment.SWIFT_SNEAK
                || enchantment == Enchantment.SOUL_SPEED
                || enchantment == Enchantment.RESPIRATION
                || enchantment == Enchantment.THORNS
                || enchantment == Enchantment.FORTUNE
                || enchantment == Enchantment.QUICK_CHARGE
                || enchantment == Enchantment.LURE
                || enchantment == Enchantment.LUCK_OF_THE_SEA
                || enchantment == Enchantment.LOYALTY
                || enchantment == Enchantment.RIPTIDE)
            && count > 3) {
          return true;
        }

        if (enchantment == Enchantment.PIERCING && count > 4) {
          return true;
        }

        if ((enchantment == Enchantment.PROTECTION
                || enchantment == Enchantment.FIRE_PROTECTION
                || enchantment == Enchantment.BLAST_PROTECTION
                || enchantment == Enchantment.PROJECTILE_PROTECTION)
            && count > 4) {
          return true;
        }

        if ((enchantment == Enchantment.SHARPNESS
                || enchantment == Enchantment.POWER
                || enchantment == Enchantment.EFFICIENCY
                || enchantment == Enchantment.IMPALING)
            && count > 5) {
          return true;
        }

        if (enchantment == Enchantment.FEATHER_FALLING && count > 4) {
          return true;
        }
      }

      return false;
    } else {
      return false;
    }
  }

  void handleAbilityAction(Player player, Location location) {
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        UUID playerId = player.getUniqueId();
        if (cache.getIfPresent(playerId) != null) {
          player.sendMessage(ChatColor.RED + "Amplification is already active!");
        } else {
          cache.put(playerId, true);
          players.put(playerId, player);
          int configuredValue = ConfigValueCache.getInt(this.plugin, "wealthT2.amplificationDuration", 30);
          long timestamp = configuredValue * 20L;
          Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
              if (player.isOnline()) {
                this.scheduleAbilityUpdate(player);
                WealthTier2Gem.players.remove(playerId, player);
              }

            }, timestamp);
          double distance = ConfigValueCache.getDouble(this.plugin, "wealthT2.amplificationRadius", 2.0);

          for (Entity entity : location.getWorld().getNearbyEntities(location, distance, distance, distance)) {
            if (entity instanceof Player targetPlayer) {
              UUID targetId = targetPlayer.getUniqueId();
              if ((this.isPrimaryAbilityActive(playerId, targetId) || targetPlayer.equals(player))
                  && cache.getIfPresent(targetId) == null) {
                if (!targetPlayer.equals(player)) {
                  String accentColor = ChatColor.of("#0EC912").toString();
                  String messageColor = ChatColor.of("#B8FFFB").toString();
                  String displayColor = ChatColor.of("#0EC912").toString();
                  String textColor = ChatColor.of("#B8FFFB").toString();
                  String labelColor = ChatColor.of("#0EC912").toString();
                  String name = player.getName();
                  targetPlayer.sendMessage(
                      accentColor
                          + "🔮 "
                          + messageColor
                          + "You have been affected with "
                          + displayColor
                          + "Amplification "
                          + textColor
                          + "by "
                          + labelColor
                          + name);
                }

                cache.put(targetId, true);
                Bukkit.getScheduler()
                    .runTaskLater(
                        this.plugin,
                        () -> {
                          if (targetPlayer.isOnline()) {
                            this.scheduleAbilityUpdate(targetPlayer);
                          }
                        },
                        timestamp);
                this.resetAbilityState(targetPlayer);
              }
            }
          }

          this.resetAbilityState(player);
        }
      }
    }
  }

  String formatDisplayText(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(item.getType().name());
      if (item.hasItemMeta()) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta.hasDisplayName()) {
          stringBuilder.append("|name:").append(itemMeta.getDisplayName());
        }

        if (itemMeta.hasLore()) {
          stringBuilder.append("|lore:").append(String.join(",", itemMeta.getLore()));
        }

        if (itemMeta.hasCustomModelData()) {
          stringBuilder.append("|cmd:").append(itemMeta.getCustomModelData());
        }
      }

      return stringBuilder.toString();
    } else {
      return null;
    }
  }

  boolean isPendingGemItem(ItemStack item, ItemStack heldItem) {
    if (item != null && heldItem != null) {
      if (item.getType() != heldItem.getType()) {
        return false;
      }

      ItemMeta itemMeta = item.getItemMeta();
      ItemMeta meta = heldItem.getItemMeta();
      if (itemMeta != null && meta != null) {
        if (itemMeta.hasDisplayName() != meta.hasDisplayName()) {
          return false;
        } else if (itemMeta.hasDisplayName() && !itemMeta.getDisplayName().equals(meta.getDisplayName())) {
          return false;
        } else if (itemMeta.hasLore() != meta.hasLore()) {
          return false;
        } else if (itemMeta.hasLore() && !itemMeta.getLore().equals(meta.getLore())) {
          return false;
        } else {
          return itemMeta.hasCustomModelData() != meta.hasCustomModelData()
              ? false
              : !itemMeta.hasCustomModelData()
                  || itemMeta.getCustomModelData() == meta.getCustomModelData();
        }
      } else {
        return itemMeta == meta;
      }
    } else {
      return false;
    }
  }

  boolean isCachedGemItem(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      String material = item.getType().name();
      return material.endsWith("_BOOTS")
          || material.endsWith("_HELMET")
          || material.endsWith("_LEGGINGS")
          || material.endsWith("_CHESTPLATE");
    } else {
      return false;
    }
  }

  void resetAbilityState(Player player) {
    UUID playerId = player.getUniqueId();
    if (!savedEnchantmentsByPlayer.containsKey(playerId)) {
      HashMap<String, Map<Enchantment, Integer>> enchantmentsByItem = new HashMap<>();
      PlayerInventory playerInventory = player.getInventory();

      for (int count = 0; count < 36; count++) {
        ItemStack item = playerInventory.getItem(count);
        if (item != null
            && item.getType() != Material.AIR
            && !this.isCachedGemItem(item)
            && !isStoredGemItem(item)) {
          String text = this.formatDisplayText(item);
          if (text != null) {
            HashMap<Enchantment, Integer> currentEnchantmentLevels = new HashMap<>(item.getEnchantments());
            enchantmentsByItem.put(text, currentEnchantmentLevels);
            this.trackAbilityState(item);
          }
        }
      }

      ItemStack heldItem = playerInventory.getBoots();
      if (heldItem != null && heldItem.getType() != Material.AIR && !isStoredGemItem(heldItem)) {
        String message = this.formatDisplayText(heldItem);
        if (message != null) {
          HashMap<Enchantment, Integer> targetStateByKey = new HashMap<>(heldItem.getEnchantments());
          enchantmentsByItem.put(message, targetStateByKey);
          this.trackAbilityState(heldItem);
        }
      }

      ItemStack targetItem = playerInventory.getLeggings();
      if (targetItem != null && targetItem.getType() != Material.AIR && !isStoredGemItem(targetItem)) {
        String displayText = this.formatDisplayText(targetItem);
        if (displayText != null) {
          HashMap<Enchantment, Integer> sourceStateByKey = new HashMap<>(targetItem.getEnchantments());
          enchantmentsByItem.put(displayText, sourceStateByKey);
          this.trackAbilityState(targetItem);
        }
      }

      ItemStack candidateItem = playerInventory.getChestplate();
      if (candidateItem != null && candidateItem.getType() != Material.AIR && !isStoredGemItem(candidateItem)) {
        String formattedText = this.formatDisplayText(candidateItem);
        if (formattedText != null) {
          HashMap<Enchantment, Integer> otherStateByKey = new HashMap<>(candidateItem.getEnchantments());
          enchantmentsByItem.put(formattedText, otherStateByKey);
          this.trackAbilityState(candidateItem);
        }
      }

      ItemStack secondaryItem = playerInventory.getHelmet();
      if (secondaryItem != null && secondaryItem.getType() != Material.AIR && !isStoredGemItem(secondaryItem)) {
        String label = this.formatDisplayText(secondaryItem);
        if (label != null) {
          HashMap<Enchantment, Integer> nextStateByKey = new HashMap<>(secondaryItem.getEnchantments());
          enchantmentsByItem.put(label, nextStateByKey);
          this.trackAbilityState(secondaryItem);
        }
      }

      ItemStack offHandItem = playerInventory.getItemInOffHand();
      if (offHandItem != null && offHandItem.getType() != Material.AIR && !isStoredGemItem(offHandItem)) {
        String name = this.formatDisplayText(offHandItem);
        if (name != null) {
          HashMap<Enchantment, Integer> previousStateByKey = new HashMap<>(offHandItem.getEnchantments());
          enchantmentsByItem.put(name, previousStateByKey);
          this.trackAbilityState(offHandItem);
        }
      }

      savedEnchantmentsByPlayer.put(playerId, enchantmentsByItem);
      player.updateInventory();
    }
  }

  String formatDisplayTextForPlayer(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(item.getType().name());
      if (item.hasItemMeta()) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta.hasDisplayName()) {
          stringBuilder.append("|").append(itemMeta.getDisplayName());
        }

        if (itemMeta.hasLore()) {
          stringBuilder.append("|").append(String.join(",", itemMeta.getLore()));
        }

        if (itemMeta.hasCustomModelData()) {
          stringBuilder.append("|").append(itemMeta.getCustomModelData());
        }
      }

      return stringBuilder.toString();
    } else {
      return null;
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    ItemStack item = event.getItemDrop().getItemStack();
    if (isStoredGemItem(item)) {
      event.setCancelled(true);
    } else if (cache.getIfPresent(playerId) != null) {
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You cannot drop items while Amplification is active!");
      }
    
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        if (cache.getIfPresent(playerId) != null) {
          ItemStack item = event.getCurrentItem();
          ItemStack heldItem = event.getCursor();
          if (item != null && item.getType() != Material.AIR && this.isResolvedGemItem(item)) {
            player.sendMessage(
                ChatColor.RED + "You moved an enhanced item! Amplification has been cancelled.");
            this.scheduleAbilityUpdate(player);
            return;
          }

          if (heldItem != null && heldItem.getType() != Material.AIR && this.isResolvedGemItem(heldItem)) {
            player.sendMessage(
                ChatColor.RED + "You moved an enhanced item! Amplification has been cancelled.");
            this.scheduleAbilityUpdate(player);
            return;
          }
        }
      }
    }
  }

  @EventHandler
  public void onInventoryDrag(InventoryDragEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (cache.getIfPresent(playerId) != null) {
        ItemStack item = event.getOldCursor();
        if (item != null && item.getType() != Material.AIR && this.isResolvedGemItem(item)) {
          player.sendMessage(
              ChatColor.RED + "You moved an enhanced item! Amplification has been cancelled.");
          event.setCancelled(true);
          this.scheduleAbilityUpdate(player);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onInventoryMoveItem(InventoryMoveItemEvent event) {
    for (Player player : Bukkit.getOnlinePlayers()) {
      UUID playerId = player.getUniqueId();
      if (cache.getIfPresent(playerId) != null
          && event.getSource().getHolder() instanceof Player targetPlayer) {
        if (targetPlayer.getUniqueId().equals(playerId)) {
          ItemStack item = event.getItem();
          if (item != null && this.isResolvedGemItem(item)) {
            player.sendMessage(
                ChatColor.RED + "An enhanced item was moved! Amplification has been cancelled.");
            event.setCancelled(true);
            this.scheduleAbilityUpdate(player);
          }
        }
      }
    }
  }

  @EventHandler
  public void onEntityPickupItem(EntityPickupItemEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        if (cache.getIfPresent(playerId) != null) {
          ItemStack item = event.getItem().getItemStack();
          if (item != null && this.isResolvedGemItem(item)) {
            event.setCancelled(true);
          }
        }
      }
    }
  }

  boolean isResolvedGemItem(ItemStack item) {
    if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
      Map<Enchantment, Integer> enchantmentLevels = item.getEnchantments();

      for (Entry<Enchantment, Integer> entry : enchantmentLevels.entrySet()) {
        Enchantment enchantment = entry.getKey();
        int count = entry.getValue();
        if (count > 5) {
          return true;
        }

        if ((enchantment == Enchantment.INFINITY
                || enchantment == Enchantment.SILK_TOUCH
                || enchantment == Enchantment.CHANNELING
                || enchantment == Enchantment.AQUA_AFFINITY
                || enchantment == Enchantment.MULTISHOT
                || enchantment == Enchantment.FLAME)
            && count > 1) {
          return true;
        }

        if ((enchantment == Enchantment.FIRE_ASPECT || enchantment == Enchantment.PUNCH) && count > 2) {
          return true;
        }

        if ((enchantment == Enchantment.UNBREAKING
                || enchantment == Enchantment.LOOTING
                || enchantment == Enchantment.SWEEPING_EDGE
                || enchantment == Enchantment.DEPTH_STRIDER
                || enchantment == Enchantment.SWIFT_SNEAK
                || enchantment == Enchantment.SOUL_SPEED
                || enchantment == Enchantment.RESPIRATION
                || enchantment == Enchantment.THORNS
                || enchantment == Enchantment.FORTUNE
                || enchantment == Enchantment.QUICK_CHARGE
                || enchantment == Enchantment.LURE
                || enchantment == Enchantment.LUCK_OF_THE_SEA
                || enchantment == Enchantment.LOYALTY
                || enchantment == Enchantment.RIPTIDE)
            && count > 3) {
          return true;
        }

        if (enchantment == Enchantment.PIERCING && count > 4) {
          return true;
        }
      }

      return false;
    } else {
      return false;
    }
  }

  void trackAbilityState(ItemStack item) {
    HashMap<Enchantment, Integer> enchantmentLevels = new HashMap<>(item.getEnchantments());

    for (Entry<Enchantment, Integer> entry : enchantmentLevels.entrySet()) {
      Enchantment enchantment = entry.getKey();
      int count = entry.getValue();
      if (enchantment == Enchantment.PROTECTION && count == 4) {
        item.addUnsafeEnchantment(Enchantment.PROTECTION, 5);
      } else if (enchantment == Enchantment.FIRE_PROTECTION && count == 4) {
        item.addUnsafeEnchantment(Enchantment.FIRE_PROTECTION, 5);
      } else if (enchantment == Enchantment.BLAST_PROTECTION && count == 4) {
        item.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 5);
      } else if (enchantment == Enchantment.PROJECTILE_PROTECTION && count == 4) {
        item.addUnsafeEnchantment(Enchantment.PROJECTILE_PROTECTION, 5);
      } else if (!this.meetsPrimaryCondition() && !this.meetsTargetCondition()) {
        if (enchantment == Enchantment.SHARPNESS && count == 5) {
          item.addUnsafeEnchantment(Enchantment.SHARPNESS, 6);
        } else if (enchantment == Enchantment.POWER && count == 5) {
          item.addUnsafeEnchantment(Enchantment.POWER, 6);
        } else if (enchantment == Enchantment.EFFICIENCY && count == 5) {
          item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 6);
        }
      } else if (this.meetsPrimaryCondition()) {
        if (enchantment == Enchantment.SHARPNESS && count == 4) {
          item.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
        } else if (enchantment == Enchantment.POWER && count == 3) {
          item.addUnsafeEnchantment(Enchantment.POWER, 4);
        } else if (enchantment == Enchantment.FEATHER_FALLING && count == 3) {
          item.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, 4);
        }
      } else if (this.meetsTargetCondition()) {
        if (enchantment == Enchantment.SHARPNESS && count == 4) {
          item.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
        } else if (enchantment == Enchantment.EFFICIENCY && count == 4) {
          item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 5);
        } else if (enchantment == Enchantment.FEATHER_FALLING && count == 4) {
          item.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, 5);
        } else if (enchantment == Enchantment.POWER && count == 3) {
          item.addUnsafeEnchantment(Enchantment.POWER, 4);
        }
      }

      if (enchantment == Enchantment.UNBREAKING && count == 3) {
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, 4);
      } else if (enchantment == Enchantment.LOOTING && count == 3) {
        item.addUnsafeEnchantment(Enchantment.LOOTING, 4);
      } else if (enchantment == Enchantment.SWEEPING_EDGE && count == 3) {
        item.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 4);
      } else if (enchantment == Enchantment.FIRE_ASPECT && count == 2) {
        item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 3);
      } else if (enchantment == Enchantment.DEPTH_STRIDER && count == 3) {
        item.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 4);
      } else if (enchantment == Enchantment.SWIFT_SNEAK && count == 3) {
        item.addUnsafeEnchantment(Enchantment.SWIFT_SNEAK, 4);
      } else if (enchantment == Enchantment.SOUL_SPEED && count == 3) {
        item.addUnsafeEnchantment(Enchantment.SOUL_SPEED, 4);
      } else if (enchantment == Enchantment.RESPIRATION && count == 3) {
        item.addUnsafeEnchantment(Enchantment.RESPIRATION, 4);
      } else if (enchantment == Enchantment.AQUA_AFFINITY && count == 1) {
        item.addUnsafeEnchantment(Enchantment.AQUA_AFFINITY, 2);
      } else if (enchantment == Enchantment.THORNS && count == 3) {
        item.addUnsafeEnchantment(Enchantment.THORNS, 4);
      } else if (enchantment == Enchantment.FORTUNE && count == 3) {
        item.addUnsafeEnchantment(Enchantment.FORTUNE, 4);
      } else if (enchantment == Enchantment.SILK_TOUCH && count == 1) {
        item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 2);
      } else if (enchantment == Enchantment.INFINITY && count == 1) {
        item.addUnsafeEnchantment(Enchantment.INFINITY, 2);
      } else if (enchantment == Enchantment.FLAME && count == 1) {
        item.addUnsafeEnchantment(Enchantment.FLAME, 2);
      } else if (enchantment == Enchantment.MULTISHOT && count == 1) {
        item.addUnsafeEnchantment(Enchantment.MULTISHOT, 2);
      } else if (enchantment == Enchantment.PUNCH && count == 2) {
        item.addUnsafeEnchantment(Enchantment.PUNCH, 3);
      } else if (enchantment == Enchantment.QUICK_CHARGE && count == 3) {
        item.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 4);
      } else if (enchantment == Enchantment.LURE && count == 3) {
        item.addUnsafeEnchantment(Enchantment.LURE, 4);
      } else if (enchantment == Enchantment.LUCK_OF_THE_SEA && count == 3) {
        item.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 4);
      } else if (enchantment == Enchantment.LOYALTY && count == 3) {
        item.addUnsafeEnchantment(Enchantment.LOYALTY, 4);
      } else if (enchantment == Enchantment.IMPALING && count == 5) {
        item.addUnsafeEnchantment(Enchantment.IMPALING, 6);
      } else if (enchantment == Enchantment.RIPTIDE && count == 3) {
        item.addUnsafeEnchantment(Enchantment.RIPTIDE, 4);
      } else if (enchantment == Enchantment.CHANNELING && count == 1) {
        item.addUnsafeEnchantment(Enchantment.CHANNELING, 2);
      } else if (enchantment == Enchantment.PIERCING && count == 4) {
        item.addUnsafeEnchantment(Enchantment.PIERCING, 5);
      }
    }
  }

  void refreshPlayerState(Player player) {
    UUID playerId = player.getUniqueId();
    Location location = player.getLocation();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        double configuredValue = ConfigValueCache.getDouble(this.plugin, "wealthT2.itemLockRadius", 2.0);

        for (Entity entity : location.getWorld().getNearbyEntities(location, configuredValue, configuredValue, configuredValue)) {
          if (entity instanceof Player targetPlayer && !targetPlayer.equals(player)) {
            UUID targetId = targetPlayer.getUniqueId();
            if (!this.isPrimaryAbilityActive(playerId, targetId) && !targetId.equals(playerId)) {
              ItemStack mainHandItem = targetPlayer.getInventory().getItemInMainHand();
              if (!this.isSourceGemItem(mainHandItem)) {
                Material material = mainHandItem.getType();
                this.cleanupAbilityState(targetId, material);
                int index = ConfigValueCache.getInt(this.plugin, "wealthT2.itemLockDuration", 30);
                String accentColor = ChatColor.of("#0EC912").toString();
                String messageColor = ChatColor.of("#B8FFFB").toString();
                String displayColor = ChatColor.of("#0EC912").toString();
                String textColor = ChatColor.of("#B8FFFB").toString();
                String labelColor = ChatColor.of("#0EC912").toString();
                String name = player.getName();
                targetPlayer.sendMessage(
                    accentColor
                        + "🔮 "
                        + messageColor
                        + "Your held item has been locked for "
                        + displayColor
                        + index
                        + " seconds "
                        + textColor
                        + "by "
                        + labelColor
                        + name);
                this.spawnAbilityEffects(playerId, targetPlayer);
              }
            }
          }
        }
      }
    }
  }

  void updatePlayerState(Location location) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;

      @Override
      public void run() {
        DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 166, 44), 1.5F);
        switch (this.durationTicks) {
        case 0:
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 1.0, dustOptions);
          break;
        case 1:
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 1.5, dustOptions);
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 2.0, dustOptions);
          break;
        case 2:
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 2.5, dustOptions);
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 2.0, dustOptions);
          this.cancel();
        }

        this.durationTicks++;
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void finishAbilityAction(Location location) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;

      @Override
      public void run() {
        DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 166, 44), 1.5F);
        switch (this.durationTicks) {
        case 0:
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 0.75, dustOptions);
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 1.0, dustOptions);
          break;
        case 1:
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 1.25, dustOptions);
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 1.5, dustOptions);
          break;
        case 2:
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 1.75, dustOptions);
          WealthTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 2.0, dustOptions);
          this.cancel();
        }

        this.durationTicks++;
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void activatePrimaryAbility(Player player, Player targetPlayer) {
    new BukkitRunnable() {
      final Player capturedPlayer = player;
      final Player secondaryPlayer = targetPlayer;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= 5) {
          this.cancel();
        } else {
          new BukkitRunnable() {
            int durationTicks = 0;

            @Override
            public void run() {
              if (this.durationTicks >= 10) {
                this.cancel();
              } else {
                Location location = player.getLocation().add(0.0, 0.5, 0.0);
                Location targetLocation = secondaryPlayer.getLocation().add(0.0, 0.5, 0.0);
                WealthTier2Gem.this.spawnPrimaryAbilityParticles(targetLocation, location);
                this.durationTicks++;
              }
            }
          }
          .runTaskTimer(WealthTier2Gem.this.plugin, SharedScheduler.staggeredInitialDelay(2L), 2L);
          this.durationTicks++;
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void spawnAbilityParticles(Location location, double value, DustOptions dustOptions) {
    byte step = 36;
    double distance = (Math.PI * 2) / step;

    for (int count = 0; count < step; count++) {
      double radius = count * distance;
      double offset = location.getX() + value * Math.cos(radius);
      double angle = location.getZ() + value * Math.sin(radius);
      Location effectLocation = new Location(location.getWorld(), offset, location.getY(), angle);
      location.getWorld().spawnParticle(Particle.DUST, effectLocation, 1, dustOptions);
    }
  }

  void spawnPrimaryAbilityParticles(Location location, Location targetLocation) {
    DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 166, 44), 1.5F);
    Vector direction = targetLocation.toVector().subtract(location.toVector());
    double value = direction.length();
    direction.normalize();

    for (double distance = 0.0; distance < value; distance += 0.2) {
      Location origin = location.clone().add(direction.clone().multiply(distance));
      location.getWorld().spawnParticle(Particle.DUST, origin, 1, dustOptions);
    }
  }

  public static boolean isRichRushActive(UUID playerId) {
    Long storedTimestamp = secondaryCache.getIfPresent(playerId);
    if (storedTimestamp == null) {
      return false;
    } else if (System.currentTimeMillis() >= storedTimestamp) {
      secondaryCache.invalidate(playerId);
      return false;
    } else {
      return true;
    }
  }

  public static boolean isUnfortunateActive(UUID playerId) {
    return cachedCache.getIfPresent(playerId) != null;
  }

  public static boolean isAmplificationActive(UUID playerId) {
    return cache.getIfPresent(playerId) != null;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    this.wealthPocketsManager.preload(event.getPlayer().getUniqueId());
    UUID playerId = event.getPlayer().getUniqueId();
    BukkitTask task = this.animationTasks.remove(playerId);
    if (task != null) {
      task.cancel();
    }

    BukkitTask scheduledTask = this.scheduledTasks.remove(playerId);
    if (scheduledTask != null) {
      scheduledTask.cancel();
    }

    BossBar bossBar = cooldownBossBars.remove(playerId);
    if (bossBar != null) {
      bossBar.removeAll();
    }

    BossBar abilityBossBar = bossBars.remove(playerId);
    if (abilityBossBar != null) {
      abilityBossBar.removeAll();
    }

    this.applyAbilityEffects(event.getPlayer(), playerId);
  }

  @EventHandler
  public void onPrimaryPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    this.applyAbilityEffects(event.getPlayer(), playerId);
    this.wealthPocketsManager.saveAndUnload(playerId);
    BukkitTask task = this.animationTasks.remove(playerId);
    if (task != null) {
      task.cancel();
    }

    BukkitTask scheduledTask = this.scheduledTasks.remove(playerId);
    if (scheduledTask != null) {
      scheduledTask.cancel();
    }

    BossBar bossBar = cooldownBossBars.remove(playerId);
    if (bossBar != null) {
      bossBar.removeAll();
    }

    BossBar abilityBossBar = bossBars.remove(playerId);
    if (abilityBossBar != null) {
      abilityBossBar.removeAll();
    }
  }

  void onPrimaryPlayerDeath(PlayerDeathEvent event, UUID playerId) {
    Map<String, Map<Enchantment,Integer>> enchantmentsByItem = savedEnchantmentsByPlayer.get(playerId);
    if (enchantmentsByItem != null && !enchantmentsByItem.isEmpty()) {
      for (ItemStack item : event.getDrops()) {
        if (item != null && item.getType() != Material.AIR && !isStoredGemItem(item)) {
          String text = this.formatDisplayText(item);
          if (text != null && enchantmentsByItem.containsKey(text)) {
            this.completeAbilityAction(item, enchantmentsByItem.get(text));
          }
        }
      }
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getView().getTitle().equals("Pockets")) {
      this.wealthPocketsManager.saveDirtyPockets();
    }
  }

  String formatDisplayTextForTarget(UUID playerId, Cache<UUID, Long> cache, boolean enabled) {
    String text = this.formatPrimaryDisplayText(cache);
    if (text != null && ActiveAbilityStore.isActive(playerId, text)) {
      return ChatColor.RED + "Active...";
    }

    Long storedTimestamp = cache.getIfPresent(playerId);
    if (storedTimestamp != null) {
      long now = storedTimestamp - System.currentTimeMillis();
      if (now > 0L) {
        return this.formatDisplayTextForState(now, enabled);
      }
    }

    return ChatColor.GREEN + "Ready!";
  }

  String formatDisplayTextFromConfig(UUID playerId) {
    return !ActiveAbilityStore.isActive(playerId, "wealth_amplification_t2")
            && !ActiveAbilityStore.isActive(playerId, "wealth_rich_rush_t2")
        ? this.formatDisplayTextForTarget(playerId, this.primaryCooldownCache, false)
        : ChatColor.RED + "Active...";
  }

  String formatDisplayTextForAbility(UUID playerId) {
    return !ActiveAbilityStore.isActive(playerId, "wealth_item_lock_t2")
            && !ActiveAbilityStore.isActive(playerId, "wealth_unfortunate_t2")
        ? this.formatDisplayTextForTarget(playerId, this.targetCooldownCache, false)
        : ChatColor.RED + "Active...";
  }

  String formatDisplayTextForState(long timestamp, boolean enabled) {
    if (timestamp <= 1000L) {
      return ChatColor.GREEN + "Ready!";
    }

    long lastUpdateTime = timestamp / 1000L;
    if (lastUpdateTime <= 0L) {
      lastUpdateTime = 1L;
    }

    if (enabled) {
      return String.format("%ds", lastUpdateTime);
    }

    long startTime = lastUpdateTime / 60L;
    long expiryTime = lastUpdateTime % 60L;
    return startTime > 0L ? String.format("%dm %ds", startTime, expiryTime) : String.format("%ds", expiryTime);
  }

  String formatPrimaryDisplayText(Cache<UUID, Long> cache) {
    if (cache == this.targetCooldownCache) {
      return "wealth_unfortunate_t2";
    } else {
      return cache == this.primaryCooldownCache ? "wealth_rich_rush_t2" : null;
    }
  }

  void initializePrimaryState() {
    ActionBarQueue.registerRenderer(this.actionBarRenderer);
  }

  public static boolean isStoredGemItem(ItemStack item) {
    if (item == null || item.getType() != Material.PRISMARINE_SHARD) {
      return false;
    } else if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
      int customModelData = item.getItemMeta().getCustomModelData();
      return customModelData == 12 || customModelData == 32 || customModelData == 52 || customModelData == 72 || customModelData == 92;
    } else {
      return false;
    }
  }

  public void cleanup() {
    ActionBarQueue.unregisterRenderer(this.actionBarRenderer);
    for (Player player : Bukkit.getOnlinePlayers()) {
      this.updatePrimaryBossBar(player);
    }
    this.animationTasks.values().forEach(BukkitTask::cancel);
    this.scheduledTasks.values().forEach(BukkitTask::cancel);
    this.cooldownTasks.values().forEach(BukkitTask::cancel);
    this.animationTasks.clear();
    this.scheduledTasks.clear();
    this.cooldownTasks.clear();

    for (BossBar bossBar : bossBars.values()) {
      bossBar.removeAll();
    }

    for (BossBar abilityBossBar : abilityBossBars.values()) {
      abilityBossBar.removeAll();
    }

    for (BossBar cooldownBossBar : cooldownBossBars.values()) {
      cooldownBossBar.removeAll();
    }

    if (secondaryCache != null) {
      secondaryCache.invalidateAll();
    }

    if (cachedCache != null) {
      cachedCache.invalidateAll();
    }

    if (cache != null) {
      cache.invalidateAll();
    }

    if (trackedCache != null) {
      trackedCache.invalidateAll();
    }

    if (pendingCache != null) {
      pendingCache.invalidateAll();
    }

    if (currentCache != null) {
      currentCache.invalidateAll();
    }

    if (activeCache != null) {
      activeCache.invalidateAll();
    }

    this.primaryCooldownCache.invalidateAll();
    this.targetCooldownCache.invalidateAll();

    bossBars.clear();
    abilityBossBars.clear();
    cooldownBossBars.clear();
    savedEnchantmentsByPlayer.clear();
    players.clear();
    playerCounters.clear();
    flagsByPlayer.clear();
  }

  public void updatePrimaryBossBar(Player player) {
    UUID playerId = player.getUniqueId();
    this.processAbilityState(playerId, 1, player);
    this.processAbilityState(playerId, 2, player);
    this.processAbilityState(playerId, 3, player);
    this.targetCooldownCache.invalidate(playerId);
    cachedCache.invalidate(playerId);
    this.primaryCooldownCache.invalidate(playerId);
    secondaryCache.invalidate(playerId);
    cache.invalidate(playerId);
    trackedCache.invalidate(playerId);
    pendingCache.invalidate(playerId);
    currentCache.invalidate(playerId);
    activeCache.invalidate(playerId);
    BossBar bossBar = cooldownBossBars.remove(playerId);
    if (bossBar != null) {
      bossBar.removeAll();
    }

    BossBar abilityBossBar = bossBars.remove(playerId);
    if (abilityBossBar != null) {
      abilityBossBar.removeAll();
    }

    BossBar cooldownBossBar = abilityBossBars.remove(playerId);
    if (cooldownBossBar != null) {
      cooldownBossBar.removeAll();
    }

    if (savedEnchantmentsByPlayer.containsKey(playerId)) {
      this.scheduleAbilityUpdate(player);
    }
  }

  void updateActionBar() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (!this.plugin.isGemsDisabled()
         
          && !this.plugin.canUseAbility(player)) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if ((isStoredGemItem(mainHandItem) || isStoredGemItem(offHandItem))
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
        } else {
          String displayText = this.formatDisplayTextForAbility(player.getUniqueId());
          String formattedText = this.formatDisplayTextFromConfig(player.getUniqueId());
          String label;
          if (ActiveAbilityStore.isActive(player.getUniqueId(), "wealth_amplification_t2")) {
            label = ChatColor.RED + "Active...";
          } else {
            label = ChatColor.GREEN + "Ready!";
          }

          if (isStoredGemItem(offHandItem) || isStoredGemItem(mainHandItem)) {
            String nameColor = ChatColor.WHITE.toString();
            String configColor = ChatColor.AQUA.toString();
            String keyColor = ChatColor.DARK_GREEN.toString();
            String argumentColor = ChatColor.WHITE.toString();
            String statusColor = ChatColor.AQUA.toString();
            String subtitle =
                nameColor
                    + "🍀 "
                    + configColor
                    + displayText
                    + keyColor
                    + " 🔮"
                    + argumentColor
                    + " 💸 "
                    + statusColor
                    + formattedText;
            ActionBarQueue.enqueue(player, subtitle);
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 40, 2, true, true, true),
                true);
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.LUCK, 40, 0, true, true, true), true);
          }
        }
      }
    }
  }

  static {
    WEALTH_AMPLIFICATION_T2_ID = "wealth_amplification_t2";
    WEALTH_ITEM_LOCK_T2_ID = "wealth_item_lock_t2";
    WEALTH_UNFORTUNATE_T2_ID = "wealth_unfortunate_t2";
    WEALTH_RICH_RUSH_T2_ID = "wealth_rich_rush_t2";
    players = new HashMap<>();
    playerCounters = new HashMap<>();
    bossBars = new HashMap<>();
    abilityBossBars = new HashMap<>();
    cooldownBossBars = new HashMap<>();
    savedEnchantmentsByPlayer = new HashMap<>();
    flagsByPlayer = new HashMap<>();
  }
}
