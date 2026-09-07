package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class LifeTier2Gem implements Listener {
  static final String LIFE_HEART_T2_ID;
  static final String LIFE_CIRCLE_T2_ID;
  static final String LIFE_HEART_LOCK_ID;
  static final String LIFE_HEART_DRAINER_ID;
  static final String LIFE_CIRCLE_SELF_ID;
  static final String LIFE_CIRCLE_TRUSTED_ID;
  static final String LIFE_CIRCLE_ENEMY_ID;
  final Bliss plugin;
  final Map<UUID, Long> cooldownTimestamps = new ConcurrentHashMap<>();
  final Map<UUID, Integer> abilityStages = new ConcurrentHashMap<>();
  final Map<UUID, Integer> playerCounters = new ConcurrentHashMap<>();
  final Set<Location> trackedLocations = ConcurrentHashMap.newKeySet();
  boolean active = false;
  final Cache<UUID, Long> secondaryCache = CacheBuilder.newBuilder().build();
  final Cache<UUID, Long> activeCache = CacheBuilder.newBuilder().build();
  final Cache<UUID, Long> cache = CacheBuilder.newBuilder().build();
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastActivationTimes = new ConcurrentHashMap<>();
  final Map<UUID, Double> activeValuesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Double> valuesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Integer> animationSteps = new ConcurrentHashMap<>();
  final Map<UUID, Integer> chargeLevels = new ConcurrentHashMap<>();
  final Map<UUID, Integer> remainingTicks = new ConcurrentHashMap<>();
  final Map<UUID, BukkitRunnable> currentTasksByPlayer = new ConcurrentHashMap<>();
  final Map<Location, Material> cachedMaterialsByLocation = new ConcurrentHashMap<>();
  final Map<Location, Material> secondaryMaterialsByLocation = new ConcurrentHashMap<>();
  final Map<Location, Material> pendingMaterialsByLocation = new ConcurrentHashMap<>();
  final Map<Location, Material> trackedMaterialsByLocation = new ConcurrentHashMap<>();
  TrustCommand trustCommand;
  AstraTier2Gem astraTier2Gem;
  BukkitTask scheduledTask;
  private final Runnable actionBarRenderer = this::updateActionBar;

  public Cache<UUID, Long> getSecondaryCache() {
    return this.secondaryCache;
  }

  public Cache<UUID, Long> getCache() {
    return this.cache;
  }

  public Cache<UUID, Long> getActiveCache() {
    return this.activeCache;
  }

  public LifeTier2Gem(Bliss bliss) {
    this.plugin = bliss;
    this.initialize();
    this.refreshAbilityState();
    this.updateAbilityState();
    this.startBackgroundTasks();
    this.refreshPlayerState();
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

  public void setTrustCommand(TrustCommand trustCommand) {
    this.trustCommand = trustCommand;
  }

  boolean isTrustedPlayer(Player player, Player targetPlayer) {
    return this.trustCommand != null && this.trustCommand.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId());
  }

  void initialize() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      this.abilityStages.putIfAbsent(player.getUniqueId(), 10);
      this.playerCounters.putIfAbsent(player.getUniqueId(), 0);
    }
  }

  boolean canUseAbility(Player player) {
    return isGemItem(player.getInventory().getItemInOffHand())
        || isGemItem(player.getInventory().getItemInMainHand());
  }

  int getAbilityIntValue(Player player) {
    return this.plugin.getEnergyManager().getAbilityIntValue(player);
  }

  public static boolean isGemItem(ItemStack item) {
    if (item != null && item.hasItemMeta() && item.getType() == Material.PRISMARINE_SHARD) {
      ItemMeta itemMeta = item.getItemMeta();
      if (!itemMeta.hasCustomModelData()) {
        return false;
      }

      int count = itemMeta.getCustomModelData();
      return count == 4 || count == 24 || count == 44 || count == 64 || count == 84;
    } else {
      return false;
    }
  }

  boolean isAbilityAllowed(ItemStack item) {
    if (item == null) {
      return true;
    }

    Material material = item.getType();
    return material.toString().contains("SWORD")
        || material.toString().contains("AXE")
        || material.toString().contains("PICKAXE")
        || material == Material.AIR;
  }

  boolean isConditionMet(Entity entity) {
    return entity.getType().toString().contains("ZOMBIE")
        || entity.getType().toString().contains("SKELETON")
        || entity.getType().toString().contains("WITHER")
        || entity.getType().toString().contains("PHANTOM")
        || entity.getType().toString().contains("STRAY")
        || entity.getType().toString().contains("HUSK")
        || entity.getType().toString().contains("DROWNED")
        || entity.getType().toString().contains("ZOGLIN");
  }

  boolean isAbilityBlocked(ItemStack item) {
    return isGemItem(item);
  }

  boolean isMatchingState(ItemStack item) {
    return item.getType().name().endsWith("_HELMET");
  }

  boolean isValidTarget(ItemStack item) {
    return item.getType().name().endsWith("_CHESTPLATE");
  }

  boolean isProtectedTarget(ItemStack item) {
    return item.getType().name().endsWith("_LEGGINGS");
  }

  boolean isActiveForPlayer(ItemStack item) {
    return item.getType().name().endsWith("_BOOTS");
  }

  boolean hasRequiredState(ItemStack item) {
    return item.getType().name().endsWith("_PICKAXE");
  }

  boolean shouldApplyEffect(ItemStack item) {
    return item.getType().name().endsWith("_AXE");
  }

  boolean canAffectTarget(ItemStack item) {
    return item.getType().name().endsWith("_SWORD");
  }

  boolean isTrackedTarget(Material material) {
    return material == Material.CORNFLOWER
        || material == Material.POPPY
        || material == Material.SUNFLOWER
        || material == Material.DANDELION
        || material == Material.BLUE_ORCHID
        || material == Material.ALLIUM
        || material == Material.AZURE_BLUET
        || material == Material.RED_TULIP
        || material == Material.ORANGE_TULIP
        || material == Material.WHITE_TULIP
        || material == Material.PINK_TULIP
        || material == Material.OXEYE_DAISY
        || material == Material.LILY_OF_THE_VALLEY
        || material == Material.TORCHFLOWER
        || material == Material.PINK_PETALS
        || material == Material.LILAC
        || material == Material.ROSE_BUSH
        || material == Material.PEONY
        || material == Material.BROWN_MUSHROOM
        || material == Material.RED_MUSHROOM;
  }

  boolean meetsPrimaryCondition(Material material) {
    return material == Material.OAK_LEAVES
        || material == Material.SPRUCE_LEAVES
        || material == Material.BIRCH_LEAVES
        || material == Material.ACACIA_LEAVES
        || material == Material.CHERRY_LEAVES
        || material == Material.MANGROVE_LEAVES
        || material == Material.JUNGLE_LEAVES
        || material == Material.DARK_OAK_LEAVES;
  }

  boolean meetsTargetCondition(Material material) {
    return material == Material.GRASS_BLOCK || material == Material.STONE || material == Material.DIRT;
  }

  boolean meetsSourceCondition(Material material) {
    return material == Material.OAK_LOG
        || material == Material.SPRUCE_LOG
        || material == Material.ACACIA_LOG
        || material == Material.DARK_OAK_LOG
        || material == Material.CHERRY_LOG
        || material == Material.BIRCH_LOG
        || material == Material.JUNGLE_LOG
        || material == Material.MANGROVE_LOG;
  }

  Material resolveMaterial() {
    Material[] material =
        new Material[] {
          Material.DEAD_BUBBLE_CORAL_FAN,
          Material.DEAD_TUBE_CORAL,
          Material.DEAD_BRAIN_CORAL,
          Material.DEAD_BUBBLE_CORAL,
          Material.DEAD_FIRE_CORAL,
          Material.DEAD_HORN_CORAL,
          Material.DEAD_TUBE_CORAL_FAN,
          Material.DEAD_BRAIN_CORAL_FAN,
          Material.DEAD_FIRE_CORAL_FAN,
          Material.DEAD_HORN_CORAL_FAN
        };
    return material[java.util.concurrent.ThreadLocalRandom.current().nextInt(material.length)];
  }

  Material resolveMaterialForPlayer() {
    Material[] material =
        new Material[] {
          Material.TUFF,
          Material.DEAD_TUBE_CORAL_BLOCK,
          Material.DEAD_BUBBLE_CORAL_BLOCK,
          Material.DEAD_BRAIN_CORAL_BLOCK,
          Material.DEAD_FIRE_CORAL_BLOCK,
          Material.DEAD_HORN_CORAL_BLOCK
        };
    return material[java.util.concurrent.ThreadLocalRandom.current().nextInt(material.length)];
  }

  long getAbilityLongValue(Player player) {
    int count = this.getAbilityIntValue(player);
    boolean enabled = player.getInventory().contains(Material.DRAGON_EGG);
    int configuredValue = ConfigValueCache.getInt(this.plugin, "lifeT2.heartLock", 75);
    if (count == 5) {
      configuredValue += 15;
    } else if (count == 4) {
      configuredValue += 30;
    } else if (count == 3) {
      configuredValue += 45;
    } else if (count == 2) {
      configuredValue += 60;
    } else if (count <= 1) {
      configuredValue += 75;
    }

    return enabled ? configuredValue / 2 : configuredValue;
  }

  long getCooldownMillis(Player player) {
    int count = this.getAbilityIntValue(player);
    boolean enabled = player.getInventory().contains(Material.DRAGON_EGG);
    int configuredValue = ConfigValueCache.getInt(this.plugin, "lifeT2.circleOfLifeCooldown", 180);
    if (count == 5) {
      configuredValue += 15;
    } else if (count == 4) {
      configuredValue += 30;
    } else if (count == 3) {
      configuredValue += 45;
    } else if (count == 2) {
      configuredValue += 60;
    } else if (count <= 1) {
      configuredValue += 75;
    }

    return enabled ? configuredValue / 2 : configuredValue;
  }

  String formatDisplayText(Player player, Cache<UUID, Long> cache) {
    String text = this.formatDisplayTextForState(cache);
    if (text != null && ActiveAbilityStore.isActive(player.getUniqueId(), text)) {
      return ChatColor.RED + "Active...";
    }

    Long expiryTimestamp = cache.getIfPresent(player.getUniqueId());
    if (expiryTimestamp == null) {
      return ChatColor.GREEN + "Ready!";
    }

    long now = expiryTimestamp - System.currentTimeMillis();
    if (now <= 1000L) {
      cache.invalidate(player.getUniqueId());
      return ChatColor.GREEN + "Ready!";
    }

    long timestamp = now / 1000L;
    if (timestamp <= 0L) {
      timestamp = 1L;
    }

    return timestamp + "s";
  }

  String formatDisplayTextForPlayer(long timestamp, boolean enabled) {
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

  String formatDisplayTextForTarget(long timestamp, boolean enabled) {
    if (timestamp <= 1000L) {
      return ChatColor.GREEN + "Ready!";
    }

    if (timestamp < 10000L) {
      double value = timestamp / 1000.0;
      return value <= 1.0 ? ChatColor.GREEN + "Ready!" : String.format("%.1fs", value);
    }

    long lastUpdateTime = timestamp / 1000L;
    if (lastUpdateTime <= 0L) {
      lastUpdateTime = 1L;
    }

    if (enabled) {
      return lastUpdateTime + "s";
    }

    long startTime = lastUpdateTime / 60L;
    long expiryTime = lastUpdateTime % 60L;
    return startTime > 0L ? String.format("%dm %02ds", startTime, expiryTime) : lastUpdateTime + "s";
  }

  String formatDisplayTextFromConfig(Player player, Cache<UUID, Long> cache) {
    String text = this.formatDisplayTextForState(cache);
    if (text != null && ActiveAbilityStore.isActive(player.getUniqueId(), text)) {
      return ChatColor.RED + "Active...";
    } else {
      Long expiryTimestamp = cache.getIfPresent(player.getUniqueId());
      if (expiryTimestamp == null) {
        return ChatColor.GREEN + "Ready!";
      } else {
        long now = expiryTimestamp - System.currentTimeMillis();
        if (now <= 1000L) {
          cache.invalidate(player.getUniqueId());
          return ChatColor.GREEN + "Ready!";
        } else {
          return this.formatDisplayTextForPlayer(now, false);
        }
      }
    }
  }

  String formatDisplayTextForAbility(Player player, String text) {
    UUID playerId = player.getUniqueId();
    if (!CooldownService.isOnCooldown(playerId, "life_vitalityt2")) {
      return ChatColor.GREEN + "Ready!";
    }

    long timestamp = CooldownService.remainingWholeSeconds(playerId, "life_vitalityt2").intValue();
    return this.formatDisplayTextForTarget(timestamp, false);
  }

  void activateAbility(Player player, Cache<UUID, Long> cache, long timestamp) {
    String text = this.formatDisplayTextForState(cache);
    int count = this.getDurationTicks(cache);
    if (text != null && count > 0 && timestamp > 0L) {
      ActiveAbilityStore.startActive(
          player.getUniqueId(), text, count, (long) Math.ceil(timestamp / 1000.0));
      timestamp += count * 1000L;
    }

    cache.put(player.getUniqueId(), System.currentTimeMillis() + timestamp);
  }

  String formatDisplayTextForState(Cache<UUID, Long> cache) {
    if (cache == this.cache) {
      return "life_heart_t2";
    } else {
      return cache == this.secondaryCache ? "life_circle_t2" : null;
    }
  }

  int getDurationTicks(Cache<UUID, Long> cache) {
    if (cache == this.cache) {
      return ConfigValueCache.getInt(this.plugin, "lifeT2.heartAbilitiesActiveSeconds", 6);
    } else {
      return cache == this.secondaryCache
          ? ConfigValueCache.getInt(this.plugin, "lifeT2.circleOfLifeActiveSeconds", 7)
          : 0;
    }
  }

  boolean canPrimaryUseAbility(Player player, Cache<UUID, Long> cache) {
    Long expiryTimestamp = cache.getIfPresent(player.getUniqueId());
    if (expiryTimestamp == null) {
      return false;
    } else {
      long now = expiryTimestamp - System.currentTimeMillis();
      if (now <= 0L) {
        cache.invalidate(player.getUniqueId());
        return false;
      } else {
        return true;
      }
    }
  }

  String formatPrimaryDisplayText(Player player) {
    String text = "💘";
    String message = "🔮";
    String displayText = "💖";
    String heartLockStatus =
        AbilityStatusFormatter.formatDisplayText(player.getUniqueId(), "heart_lock", false);
    String formattedText =
        AbilityStatusFormatter.formatDisplayText(
            player.getUniqueId(), "life_vitalityt2", false);
    String label =
        AbilityStatusFormatter.formatDisplayText(
            player.getUniqueId(), "life_circle_t2", false);
    return text
        + " "
        + ChatColor.AQUA
        + heartLockStatus
        + " "
        + ChatColor.LIGHT_PURPLE
        + message
        + " "
        + ChatColor.AQUA
        + formattedText
        + " "
        + ChatColor.WHITE
        + displayText
        + " "
        + ChatColor.AQUA
        + label;
  }

  void applyAbilityEffects(Player player) {
    UUID playerId = player.getUniqueId();
    long now = System.currentTimeMillis();
    Long storedTimestamp = this.lastUseTimes.get(playerId);
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        int configuredValue = ConfigValueCache.getInt(this.plugin, "lifeT2.passiveHealInterval", 6);
        long timestamp = configuredValue * 1000L;
        if ((player.getInventory()
                    .getItemInMainHand()
                    .isSimilar(this.createGemItem())
                || player.getInventory()
                    .getItemInOffHand()
                    .isSimilar(this.createGemItem()))
            && (storedTimestamp == null || now - storedTimestamp >= timestamp)) {
          if (player.getHealth() < player.getMaxHealth()) {
            int index = ConfigValueCache.getInt(this.plugin, "lifeT2.passiveRegenerationDuration", 60);
            int remaining = ConfigValueCache.getInt(this.plugin, "lifeT2.passiveRegenerationLevel", 2);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, index, remaining));
            double distance = ConfigValueCache.getDouble(this.plugin, "lifeT2.passiveHealAmount", 1.0);
            double value = Math.min(player.getMaxHealth(), player.getHealth() + distance);
            player.setHealth(value);
          }

          this.lastUseTimes.put(playerId, now);
        }
      }
    }
  }

  void refreshAbilityState() {
    ActionBarQueue.registerRenderer(this.actionBarRenderer);
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              if (LifeTier2Gem.this.plugin.isGemsDisabled()
                 ) {
                return;
              }

              if (LifeTier2Gem.this.plugin.canUseAbility(player)) {
                return;
              }

              UUID playerId = player.getUniqueId();
              double configuredValue = ConfigValueCache.getDouble(LifeTier2Gem.this.plugin, "lifeT2.lifeDrainedSoloHealRate", 0.5);
              double distance = ConfigValueCache.getDouble(LifeTier2Gem.this.plugin, "lifeT2.lifeCoLHealRate", 0.25);
              Integer storedCount = LifeTier2Gem.this.animationSteps.get(playerId);
              if (storedCount != null && storedCount > 0) {
                if (player.getHealth() < player.getMaxHealth()) {
                  player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + configuredValue));
                }

                LifeTier2Gem.this.animationSteps.put(playerId, storedCount - 1);
                if (storedCount - 1 <= 0) {
                  LifeTier2Gem.this.animationSteps.remove(playerId);
                }
              }

              Integer currentInteger = LifeTier2Gem.this.chargeLevels.get(playerId);
              if (currentInteger != null && currentInteger > 0) {
                LifeTier2Gem.this.chargeLevels.put(playerId, currentInteger - 1);
                if (currentInteger - 1 <= 0) {
                  LifeTier2Gem.this.chargeLevels.remove(playerId);
                  LifeTier2Gem.this.playAbilityEffects(player, "life_heart_drainer");
                  LifeTier2Gem.this.processAbilityState(player);
                }
              }

              Integer targetInteger = LifeTier2Gem.this.remainingTicks.get(playerId);
              if (targetInteger != null && targetInteger > 0) {
                if (player.getHealth() < player.getMaxHealth()) {
                  player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + distance));
                }

                LifeTier2Gem.this.remainingTicks.put(playerId, targetInteger - 1);
                if (targetInteger - 1 <= 0) {
                  LifeTier2Gem.this.remainingTicks.remove(playerId);
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void updateAbilityState() {
    if (this.scheduledTask == null || this.scheduledTask.isCancelled()) {
      this.scheduledTask =
          SharedScheduler.scheduleRepeating(
              new BukkitRunnable() {
                @Override
                public void run() {
                  long now = System.currentTimeMillis();

                  for (Map.Entry<UUID, Long> activeEntry : LifeTier2Gem.this.lastActivationTimes.entrySet()) {
                    UUID playerId = activeEntry.getKey();
                    Player player = Bukkit.getPlayer(playerId);
                    if (now < activeEntry.getValue()) {
                      Double originalMaxHealth = LifeTier2Gem.this.valuesByPlayer.get(playerId);
                      if (player != null && player.isOnline() && originalMaxHealth != null) {
                        LifeTier2Gem.this.trackAbilityState(player, originalMaxHealth);
                      }
                    } else if (LifeTier2Gem.this.lastActivationTimes.remove(playerId, activeEntry.getValue())) {
                      LifeTier2Gem.this.activeValuesByPlayer.remove(playerId);
                      LifeTier2Gem.this.valuesByPlayer.remove(playerId);
                      BukkitRunnable healthTask = LifeTier2Gem.this.currentTasksByPlayer.remove(playerId);
                      if (healthTask != null) {
                        healthTask.cancel();
                      }

                      if (player != null && player.isOnline()) {
                        LifeTier2Gem.this.resetAbilityState(player);
                      }
                    }
                  }
                }
              },
              this.plugin,
              SharedScheduler.staggeredInitialDelay(20L),
              20L);
    }
  }

  NamespacedKey resolveNamespacedKey(String text, UUID playerId) {
    return new NamespacedKey(this.plugin, text + "_" + playerId.toString().replace("-", ""));
  }

  NamespacedKey resolveNamespacedKeyForPlayer(UUID playerId) {
    return this.resolveNamespacedKey("life_heart_lock", playerId);
  }

  double getLifeT2NormalMaxHealthEgg(Player player) {
    return player.getInventory().contains(Material.DRAGON_EGG)
        ? ConfigValueCache.getInt(this.plugin, "lifeT2.normalMaxHealthEgg", 40)
        : ConfigValueCache.getInt(this.plugin, "lifeT2.normalMaxHealth", 20);
  }

  void playAbilityEffects(Player player, String text) {
    AttributeInstance attributeInstance = player.getAttribute(Attribute.MAX_HEALTH);
    if (attributeInstance != null) {
      NamespacedKey modifierKey = this.resolveNamespacedKey(text, player.getUniqueId());

      for (AttributeModifier attributeModifier : new ArrayList<>(attributeInstance.getModifiers())) {
        if (attributeModifier.getKey().equals(modifierKey)) {
          attributeInstance.removeModifier(attributeModifier);
        }
      }
    }
  }

  void spawnAbilityEffects(Player player, String text, double value) {
    AttributeInstance attributeInstance = player.getAttribute(Attribute.MAX_HEALTH);
    if (attributeInstance != null) {
      this.playAbilityEffects(player, text);
      if (Math.abs(value) > 1.0E-4) {
        attributeInstance.addModifier(
            new AttributeModifier(
                this.resolveNamespacedKey(text, player.getUniqueId()),
                value,
                Operation.ADD_NUMBER));
      }

      this.processAbilityState(player);
    }
  }

  void cleanupAbilityState(Player player, String text, double value) {
    this.spawnAbilityEffects(player, text, value - this.getLifeT2NormalMaxHealthEgg(player));
  }

  void scheduleAbilityUpdate(Player player) {
    this.playAbilityEffects(player, "life_circle_self");
    this.playAbilityEffects(player, "life_circle_trusted");
    this.playAbilityEffects(player, "life_circle_enemy");
  }

  void completeAbilityAction(Player player) {
    this.handleAbilityAction(player);
    this.playAbilityEffects(player, "life_heart_drainer");
    this.scheduleAbilityUpdate(player);
    this.processAbilityState(player);
  }

  void processAbilityState(Player player) {
    if (player != null && !player.isDead()) {
      AttributeInstance attributeInstance = player.getAttribute(Attribute.MAX_HEALTH);
      if (attributeInstance != null) {
        double value = attributeInstance.getValue();
        if (player.getHealth() > value) {
          player.setHealth(value);
        }
      }
    }
  }

  void handleAbilityAction(Player player) {
    AttributeInstance attributeInstance = player.getAttribute(Attribute.MAX_HEALTH);
    if (attributeInstance != null) {
      NamespacedKey modifierKey = this.resolveNamespacedKeyForPlayer(player.getUniqueId());

      for (AttributeModifier attributeModifier : new ArrayList<>(attributeInstance.getModifiers())) {
        if (attributeModifier.getKey().equals(modifierKey)) {
          attributeInstance.removeModifier(attributeModifier);
        }
      }
    }
  }

  void resetAbilityState(Player player) {
    this.handleAbilityAction(player);
    AttributeInstance attributeInstance = player.getAttribute(Attribute.MAX_HEALTH);
    if (attributeInstance != null && !player.isDead()) {
      double value = attributeInstance.getValue();
      if (player.getHealth() > value) {
        player.setHealth(value);
      }
    }
  }

  void trackAbilityState(Player player, double value) {
    AttributeInstance attributeInstance = player.getAttribute(Attribute.MAX_HEALTH);
    if (attributeInstance != null) {
      this.handleAbilityAction(player);
      double distance = value - attributeInstance.getValue();
      if (Math.abs(distance) > 1.0E-4) {
        attributeInstance.addModifier(
            new AttributeModifier(
                this.resolveNamespacedKeyForPlayer(player.getUniqueId()),
                distance,
                Operation.ADD_NUMBER));
      }

      if (!player.isDead() && player.getHealth() > value) {
        player.setHealth(value);
      }
    }
  }

  void refreshPlayerState() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if (LifeTier2Gem.isGemItem(mainHandItem) || LifeTier2Gem.isGemItem(offHandItem)) {
                LifeTier2Gem.this.updatePlayerState(player);
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void updatePlayerState(Player player) {
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        for (ItemStack item : player.getInventory().getContents()) {
          if (item != null && item.getType() != Material.AIR) {
            if ((this.hasRequiredState(item) || this.shouldApplyEffect(item))
                && !item.containsEnchantment(Enchantment.UNBREAKING)) {
              item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
            }

            if (this.isMatchingState(item) && !item.containsEnchantment(Enchantment.UNBREAKING)) {
              item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
            }

            if (this.isValidTarget(item) && !item.containsEnchantment(Enchantment.UNBREAKING)) {
              item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
            }

            if (this.isProtectedTarget(item) && !item.containsEnchantment(Enchantment.UNBREAKING)) {
              item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
            }

            if (this.isActiveForPlayer(item) && !item.containsEnchantment(Enchantment.UNBREAKING)) {
              item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
            }

            if (this.canAffectTarget(item) && !item.containsEnchantment(Enchantment.UNBREAKING)) {
              item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
            }
          }
        }
      }
    }
  }

  void sendAbilityFeedback(Player player) {
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        player.sendMessage(
            ChatColor.LIGHT_PURPLE
                + "🔮 "
                + ChatColor.of("#befff7")
                + "You have used "
                + ChatColor.of("#FE04B4")
                + "💘Heart Drainer");
        long timestamp = this.getAbilityLongValue(player);
        int configuredValue = ConfigValueCache.getInt(this.plugin, "lifeT2.heartDrainerMaxHealthDuration", 20);
        ActiveAbilityStore.startActive(player.getUniqueId(), "heart_lock", configuredValue, timestamp);
        this.updateState(player.getLocation());
        double distance = ConfigValueCache.getDouble(this.plugin, "lifeT2.heartDrainerRadius", 3.0);
        int index = ConfigValueCache.getInt(this.plugin, "lifeT2.heartDrainerVictimHealth", 12);
        int remaining = ConfigValueCache.getInt(this.plugin, "lifeT2.heartDrainerVictimHealthEgg", 32);
        ArrayList<UUID> entries = new ArrayList<>();

        for (Player targetPlayer : player.getWorld().getPlayers()) {
          if (!targetPlayer.equals(player)
              && player.getLocation().distance(targetPlayer.getLocation()) <= distance
              && !this.isTrustedPlayer(player, targetPlayer)) {
            if (targetPlayer.getInventory().contains(Material.DRAGON_EGG)) {
              this.cleanupAbilityState(targetPlayer, "life_heart_drainer", remaining);
            } else {
              this.cleanupAbilityState(targetPlayer, "life_heart_drainer", index);
            }

            String textColor = ChatColor.LIGHT_PURPLE.toString();
            String accentColor = ChatColor.of("#befff7").toString();
            String messageColor = ChatColor.LIGHT_PURPLE.toString();
            String displayColor = ChatColor.of("#befff7").toString();
            String labelColor = ChatColor.LIGHT_PURPLE.toString();
            String name = player.getName();
            targetPlayer.sendMessage(
                textColor
                    + "🔮 "
                    + accentColor
                    + "You have been affected with "
                    + messageColor
                    + "💘Heart Drainer "
                    + displayColor
                    + "by "
                    + labelColor
                    + name);
            this.chargeLevels.put(targetPlayer.getUniqueId(), configuredValue);
            entries.add(targetPlayer.getUniqueId());
          }
        }

        new BukkitRunnable() {
          final List<UUID> capturedEntries = entries;
          static final String LIFE_HEART_DRAINER_ID = "life_heart_drainer";
          @Override
          public void run() {
            for (UUID playerId : this.capturedEntries) {
              Player player = Bukkit.getPlayer(playerId);
              if (player != null && player.isOnline()) {
                Integer storedCount = LifeTier2Gem.this.chargeLevels.get(playerId);
                if (storedCount == null || storedCount <= 0) {
                  LifeTier2Gem.this.playAbilityEffects(player, LIFE_HEART_DRAINER_ID);
                  LifeTier2Gem.this.processAbilityState(player);
                }
              }
            }
          }


        }.runTaskLater(this.plugin, configuredValue * 20L);
      }
    }
  }

  void finishAbilityAction(Player player, Player targetPlayer) {
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          if (!this.plugin.canUseAbility(targetPlayer)) {
            String accentColor = ChatColor.of("#FE04B4").toString();
            String messageColor = ChatColor.of("#B8FFFB").toString();
            String textColor = ChatColor.WHITE.toString();
            String displayColor = ChatColor.of("#FE04B4").toString();
            String labelColor = ChatColor.of("#B8FFFB").toString();
            String nameColor = ChatColor.of("#FE04B4").toString();
            String configPath = targetPlayer.getName();
            player.sendMessage(
                accentColor
                    + "🔮 "
                    + messageColor
                    + "You used "
                    + textColor
                    + "💘"
                    + displayColor
                    + "Heart Lock "
                    + labelColor
                    + "on "
                    + nameColor
                    + configPath);
            int configuredValue = ConfigValueCache.getInt(this.plugin, "lifeT2.heartLockDuration", 20);
            accentColor = ChatColor.of("#FE04B4").toString();
            messageColor = ChatColor.of("#B8FFFB").toString();
            textColor = ChatColor.of("#FE04B4").toString();
            labelColor = ChatColor.of("#B8FFFB").toString();
            nameColor = ChatColor.of("#FE04B4").toString();
            String prefix = player.getName();
            targetPlayer.sendMessage(
                accentColor
                    + "🔮 "
                    + messageColor
                    + "Your heart count has been locked for "
                    + textColor
                    + configuredValue
                    + " seconds "
                    + labelColor
                    + "by "
                    + nameColor
                    + prefix);
            long timestamp = this.getAbilityLongValue(player);
            ActiveAbilityStore.startActive(player.getUniqueId(), "heart_lock", configuredValue, timestamp);
            UUID playerId = targetPlayer.getUniqueId();
            double value = targetPlayer.getHealth();
            int index = ConfigValueCache.getInt(this.plugin, "lifeT2.heartLockMinimumHealth", 6);
            double distance = Math.max(index, value);
            this.activeValuesByPlayer.put(playerId, value);
            this.valuesByPlayer.put(playerId, distance);
            this.trackAbilityState(targetPlayer, distance);
            if (!targetPlayer.isDead() && targetPlayer.getHealth() < distance) {
              targetPlayer.setHealth(distance);
            }

            this.activateTargetAbility(player, targetPlayer);
            this.lastActivationTimes.put(playerId, System.currentTimeMillis() + configuredValue * 1000L);
          }
        }
      }
    }
  }

  void sendPrimaryAbilityFeedback(Player player) {
    String textColor = ChatColor.LIGHT_PURPLE.toString();
    String accentColor = ChatColor.of("#befff7").toString();
    String messageColor = ChatColor.WHITE.toString();
    String displayColor = ChatColor.LIGHT_PURPLE.toString();
    player.sendMessage(
        textColor
            + "🔮 "
            + accentColor
            + "You have summoned the "
            + messageColor
            + "💖"
            + displayColor
            + "Circle of Life");
    int configuredValue = ConfigValueCache.getInt(this.plugin, "lifeT2.circleOfLifeDuration", 15);
    double distance = ConfigValueCache.getDouble(this.plugin, "lifeT2.circleOfLifeHealRadius", 4.0);
    long timestamp = this.getCooldownMillis(player);
    ActiveAbilityStore.startActive(player.getUniqueId(), "life_circle_t2", configuredValue, timestamp);
    int index = ConfigValueCache.getInt(this.plugin, "lifeT2.lifeCoLGroupDuration", 2);
    double radius = ConfigValueCache.getDouble(this.plugin, "lifeT2.circleOfLifeRadius", 5.0);
    this.remainingTicks.put(player.getUniqueId(), index);
    ArrayList<BukkitTask> entries = new ArrayList<>();
    ArrayList<Player> currentEntries = new ArrayList<>();
    int remaining = ConfigValueCache.getInt(this.plugin, "lifeT2.normalMaxHealth", 20);
    int step = ConfigValueCache.getInt(this.plugin, "lifeT2.normalMaxHealthEgg", 40);
    int ticks = ConfigValueCache.getInt(this.plugin, "lifeT2.circleOfLifeMaxHealth", 30);
    int durationTicks = ConfigValueCache.getInt(this.plugin, "lifeT2.circleOfLifeMaxHealthEgg", 50);
    BukkitTask scheduledTask =
        new CircleOfLifeController(
                this, configuredValue, player, entries, currentEntries, remaining, step, durationTicks, ticks, radius, distance, index)
            .runTaskTimer(Bliss.getInstance(), 0L, 10L);
  }

  void activatePrimaryAbility(Player player, int count, int index) {
    if (player != null && player.isOnline()) {
      this.scheduleAbilityUpdate(player);
      this.processAbilityState(player);
    }
  }

  void applyPrimaryAbilityEffects(Player player) {
    player.sendMessage(
        ChatColor.LIGHT_PURPLE
            + "🔮 "
            + ChatColor.RED
            + "You have activated "
            + ChatColor.of("#FE04B4")
            + "Vitality Vortex");
    long timestamp = ConfigValueCache.getInt(this.plugin, "lifeT1.vitalityCooldown", 40);
    if (player.getInventory().contains(Material.DRAGON_EGG)) {
      timestamp /= 2L;
    }

    long lastUpdateTime = ConfigValueCache.getInt(this.plugin, "lifeT2.vitalityActiveSeconds", 9);
    ActiveAbilityStore.startActive(player.getUniqueId(), "life_vitalityt2", lastUpdateTime, timestamp);
    this.active = true;
    Location location = player.getLocation().clone();
    player.setFoodLevel(20);
    player.setSaturation(20.0F);
    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, true, true));
    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 1, true, true, true));

    for (Player targetPlayer : player.getWorld().getPlayers()) {
      if (!targetPlayer.equals(player)
          && player.getLocation().distance(targetPlayer.getLocation()) <= 5.0
          && this.isTrustedPlayer(player, targetPlayer)) {
        targetPlayer.setFoodLevel(20);
        targetPlayer.setSaturation(20.0F);
        targetPlayer.addPotionEffect(
            new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, true, true));
        targetPlayer.addPotionEffect(
            new PotionEffect(PotionEffectType.ABSORPTION, 600, 1, true, true, true));
        String accentColor = ChatColor.of("#FE04B4").toString();
        String messageColor = ChatColor.of("#befff7").toString();
        String displayColor = ChatColor.of("#FE04B4").toString();
        String name = player.getName();
        targetPlayer.sendMessage(
            accentColor
                + "🔮 "
                + messageColor
                + "You received Vitality effects from "
                + displayColor
                + name);
      }
    }

    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Player capturedPlayer = player;
      int maxCount = 1;
      final int durationTicks = 5;

      @Override
      public void run() {
        if (this.maxCount <= 5) {
          LifeTier2Gem.this.applyTargetAbilityEffects(this.anchorLocation, this.maxCount, this.capturedPlayer);
          LifeTier2Gem.this.spawnAbilityParticles(this.anchorLocation, this.maxCount);
          this.maxCount++;
        } else {
          BukkitRunnable cleanupAbilityTickTask = new BukkitRunnable() {
            double effectRadius = 0.0;

            @Override
            public void run() {
              if (player.isOnline() && LifeTier2Gem.this.active) {
                LifeTier2Gem.this.spawnPrimaryAbilityParticles(anchorLocation, 4.5, this.effectRadius);
                this.effectRadius += 8.0;
              } else {
                this.cancel();
              }
            }
          };
          cleanupAbilityTickTask.runTaskTimer(LifeTier2Gem.this.plugin, SharedScheduler.staggeredInitialDelay(10L), 10L);
          Bukkit.getScheduler()
          .runTaskLater(
              LifeTier2Gem.this.plugin,
              () -> {
                LifeTier2Gem.this.updateSourceState(this.anchorLocation, 5);
                Bukkit.getScheduler()
                .runTaskLater(
                LifeTier2Gem.this.plugin,
                () -> {
                  LifeTier2Gem.this.initializePrimaryState();
                  LifeTier2Gem.this.active = false;
                  cleanupAbilityTickTask.cancel();
                },
                180L);
              },
              4L);
          this.cancel();
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(5L), 5L);
  }

  void applyTargetAbilityEffects(Location location, int count, Player player) {
    int index = Math.max(0, (count - 1) * (count - 1));
    int remaining = count * count;

    for (int step = -count; step <= count; step++) {
      for (int ticks = -count; ticks <= count; ticks++) {
        for (int durationTicks = -count; durationTicks <= count; durationTicks++) {
          int attempts = step * step + ticks * ticks + durationTicks * durationTicks;
          if (attempts >= index && attempts <= remaining + count) {
            Location targetLocation = location.clone().add(step, ticks, durationTicks);
            Block block = targetLocation.getBlock();
            Material material = block.getType();
            this.trackedLocations.add(targetLocation);
            if (this.isTrackedTarget(material)) {
              if (!"world_nether".equals(location.getWorld().getName())) {
                this.trackedMaterialsByLocation.putIfAbsent(targetLocation, material);
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.REGENERATION, 20, 1, true, false, true));
                block.setType(this.resolveMaterial());
              }
            } else if (this.meetsPrimaryCondition(material)) {
              if (!"world_nether".equals(location.getWorld().getName())) {
                this.pendingMaterialsByLocation.putIfAbsent(targetLocation, material);
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.ABSORPTION, 20, 1, true, false, true));
                block.setType(this.resolveMaterialForPlayer());
              }
            } else if (this.meetsTargetCondition(material)) {
              if (!"world_nether".equals(location.getWorld().getName())) {
                this.cachedMaterialsByLocation.putIfAbsent(targetLocation, material);
                block.setType(this.resolveMaterialForPlayer());
              }
            } else if (this.meetsSourceCondition(material)) {
              this.secondaryMaterialsByLocation.putIfAbsent(targetLocation, material);
              player.addPotionEffect(
                  new PotionEffect(PotionEffectType.RESISTANCE, 20, 0, true, false, true));
              block.setType(Material.ACACIA_LOG);
            }
          }
        }
      }
    }
  }

  void spawnAbilityParticles(Location location, int count) {
    for (double value = 0.0; value < 360.0; value += 2.0) {
      double angleRadians = location.getX() + count * Math.cos(Math.toRadians(value));
      double distance = location.getZ() + count * Math.sin(Math.toRadians(value));
      Location effectLocation = new Location(location.getWorld(), angleRadians, location.getY() + 0.5, distance);
      location.getWorld()
          .spawnParticle(
              Particle.DUST,
              effectLocation,
              1,
              0.0,
              0.0,
              0.0,
              0.0,
              new DustOptions(Color.fromRGB(255, 0, 179), 1.0F));
    }
  }

  void spawnPrimaryAbilityParticles(Location location, double value, double distance) {
    for (int count = 0; count < 60; count++) {
      double radius = distance + count * 6;
      double angleRadians = Math.toRadians(radius);
      double offset = Math.cos(angleRadians) * value;
      double angle = Math.sin(angleRadians) * value;
      Location targetLocation = location.clone().add(offset, 0.2, angle);
      Location origin = location.clone().add(offset, 0.5, angle);
      location.getWorld()
          .spawnParticle(
              Particle.DUST,
              origin,
              2,
              0.0,
              0.0,
              0.0,
              0.0,
              new DustOptions(Color.fromRGB(255, 0, 179), 1.0F));
    }
  }

  void activateTargetAbility(Player player, Player targetPlayer) {
    BukkitRunnable playerId = this.currentTasksByPlayer.get(targetPlayer.getUniqueId());
    if (playerId != null) {
      playerId.cancel();
    }

    int configuredValue = ConfigValueCache.getInt(this.plugin, "lifeT2.heartLockParticleLoops", 15);
    HeartLockParticleController activeAbilityTickTask = new HeartLockParticleController(this, configuredValue, player, targetPlayer);
    this.currentTasksByPlayer.put(targetPlayer.getUniqueId(), activeAbilityTickTask);
    activeAbilityTickTask.runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void updateState(Location location) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      double[] state = new double[] {0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5, 2.75, 3.0};
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= this.state.length) {
          this.cancel();
        } else {
          LifeTier2Gem.this.spawnSourceAbilityParticles(this.anchorLocation, this.state[this.durationTicks]);
          if (this.durationTicks + 1 < this.state.length) {
            LifeTier2Gem.this.spawnSourceAbilityParticles(this.anchorLocation, this.state[this.durationTicks + 1]);
            this.durationTicks += 2;
          } else {
            this.durationTicks++;
          }
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void updatePrimaryState(Location location) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      double[] state = new double[] {0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5, 2.75, 3.0, 3.25, 3.5, 3.75, 4.0};
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= this.state.length) {
          LifeTier2Gem.this.spawnActiveAbilityParticles(this.anchorLocation, 4.0);
          this.cancel();
        } else {
          LifeTier2Gem.this.spawnActiveAbilityParticles(this.anchorLocation, this.state[this.durationTicks]);
          if (this.durationTicks + 1 < this.state.length) {
            LifeTier2Gem.this.spawnActiveAbilityParticles(this.anchorLocation, this.state[this.durationTicks + 1]);
            this.durationTicks += 2;
          } else {
            this.durationTicks++;
          }
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void spawnTargetAbilityParticles(Location location, double value) {
    int configuredValue = ConfigValueCache.getInt(this.plugin, "lifeT2.circleOfLifeParticleCount", 100);

    for (int count = 0; count < configuredValue; count++) {
      double distance = count * (360.0 / configuredValue);
      double angleRadians = Math.cos(Math.toRadians(distance)) * value;
      double radius = Math.sin(Math.toRadians(distance)) * value;
      Location targetLocation = location.clone().add(angleRadians, 0.0, radius);
      location.getWorld()
          .spawnParticle(
              Particle.DUST, targetLocation, 1, new DustOptions(Color.fromRGB(255, 0, 179), 1.0F));
    }
  }

  public void updateTargetState(Location location) {
    byte step = 10;
    double value = 2.5;
    byte phase = 4;
    BukkitRunnable particleOrbitTask = new BukkitRunnable() {
      final int maxCount = step;
      final double effectRadius = value;
      final int durationTicks = phase;
      final Location anchorLocation = location;
      int cooldownTicks = 0;

      @Override
      public void run() {
        if (this.cooldownTicks >= this.maxCount) {
          this.cancel();
        } else {
          double value = (double) this.cooldownTicks / this.maxCount;
          double distance = this.effectRadius * (1.0 - value);

          for (int count = 0; count < this.durationTicks; count++) {
            double radius = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 2.0 * Math.PI;
            double angle =
            Math.acos(java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0);
            double progress =
            distance * (0.8 + java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 0.4);
            double offset = progress * Math.sin(angle) * Math.cos(radius);
            double scale = progress * Math.sin(angle) * Math.sin(radius) * 0.5;
            double amount = progress * Math.cos(angle);
            Location location = this.anchorLocation.clone().add(offset, scale + 0.3, amount);
            if (location.getWorld() != null) {
              location.getWorld().spawnParticle(Particle.CHERRY_LEAVES, location, 0, 0.0, 0.0, 0.0, 0.0);
            }
          }

          this.cooldownTicks++;
        }
      }
    };
    particleOrbitTask.runTaskTimer(this.plugin, 0L, 1L);
  }

  void spawnSourceAbilityParticles(Location location, double value) {
    for (int count = 0; count < 36; count++) {
      double distance = count * 10;
      double angleRadians = Math.cos(Math.toRadians(distance)) * value;
      double radius = Math.sin(Math.toRadians(distance)) * value;
      Location targetLocation = location.clone().add(angleRadians, 0.0, radius);
      location.getWorld().spawnParticle(Particle.SMOKE, targetLocation, 1, 0.0, 0.0, 0.0, 0.0);
      location.getWorld()
          .spawnParticle(
              Particle.DUST, targetLocation, 1, new DustOptions(Color.fromRGB(255, 0, 180), 1.5F));
    }
  }

  void spawnActiveAbilityParticles(Location location, double value) {
    for (int count = 0; count < 36; count++) {
      double distance = count * 10;
      double angleRadians = Math.cos(Math.toRadians(distance)) * value;
      double radius = Math.sin(Math.toRadians(distance)) * value;
      Location targetLocation = location.clone().add(angleRadians, 0.0, radius);
      location.getWorld()
          .spawnParticle(
              Particle.DUST, targetLocation, 1, new DustOptions(Color.fromRGB(255, 0, 179), 1.0F));
    }
  }

  void spawnPendingAbilityParticles(Location location, Location targetLocation) {
    double distance = location.distance(targetLocation);
    if (distance > 0.0) {
      double value = distance / 16.0;

      for (int count = 0; count < 16; count++) {
        double radius = value * count / distance;
        Location origin = location.clone().add(targetLocation.clone().subtract(location).multiply(radius));
        location.getWorld()
            .spawnParticle(
                Particle.DUST,
                origin,
                1,
                0.1,
                0.1,
                0.1,
                0.0,
                new DustOptions(Color.fromRGB(255, 0, 179), 1.0F));
      }
    }
  }

  void applySourceAbilityEffects(Location location, int count, Player player) {
    int configuredValue = ConfigValueCache.getInt(this.plugin, "lifeT2.flowerRegenerationDuration", 20);
    int index = ConfigValueCache.getInt(this.plugin, "lifeT2.flowerRegenerationLevel", 1);
    int remaining = ConfigValueCache.getInt(this.plugin, "lifeT2.leavesAbsorptionDuration", 20);
    int step = ConfigValueCache.getInt(this.plugin, "lifeT2.leavesAbsorptionLevel", 1);
    int ticks = ConfigValueCache.getInt(this.plugin, "lifeT2.logResistanceDuration", 20);
    int durationTicks = ConfigValueCache.getInt(this.plugin, "lifeT2.logResistanceLevel", 0);

    for (int attempts = -count; attempts <= count; attempts++) {
      for (int amount = -count; amount <= count; amount++) {
        for (int level = -count; level <= count; level++) {
          if (attempts * attempts + amount * amount + level * level <= count * count) {
            Location targetLocation = location.clone().add(attempts, amount, level);
            Block block = targetLocation.getBlock();
            Material material = block.getType();
            this.trackedLocations.add(targetLocation);
            if (this.isTrackedTarget(material)) {
              this.trackedMaterialsByLocation.put(targetLocation, material);
              player.addPotionEffect(
                  new PotionEffect(PotionEffectType.REGENERATION, configuredValue, index, false, false, true));
              block.setType(this.resolveMaterial());
            } else if (this.meetsPrimaryCondition(material)) {
              this.pendingMaterialsByLocation.put(targetLocation, material);
              player.addPotionEffect(
                  new PotionEffect(PotionEffectType.ABSORPTION, remaining, step, false, false, true));
              block.setType(this.resolveMaterialForPlayer());
            } else if (this.meetsTargetCondition(material)) {
              this.cachedMaterialsByLocation.put(targetLocation, material);
              block.setType(this.resolveMaterialForPlayer());
            } else if (this.meetsSourceCondition(material)) {
              this.secondaryMaterialsByLocation.put(targetLocation, material);
              player.addPotionEffect(
                  new PotionEffect(PotionEffectType.RESISTANCE, ticks, durationTicks, false, false, true));
              block.setType(Material.ACACIA_LOG);
            }
          }
        }
      }
    }
  }

  void updateSourceState(Location location, int count) {
    for (int index = -count; index <= count; index++) {
      for (int remaining = -count; remaining <= count; remaining++) {
        for (int step = -count; step <= count; step++) {
          if (index * index + remaining * remaining + step * step <= count * count) {
            Location targetLocation = location.clone().add(index, remaining, step);
            Block block = targetLocation.getBlock();
            if (block.getType() == Material.WATER) {
              block.setType(Material.AIR);
            }
          }
        }
      }
    }
  }

  void initializePrimaryState() {
    this.active = false;
    this.updateActiveState(this.cachedMaterialsByLocation);
    this.updateActiveState(this.secondaryMaterialsByLocation);
    this.updateActiveState(this.pendingMaterialsByLocation);
    this.updateActiveState(this.trackedMaterialsByLocation);
    this.trackedLocations.clear();
  }

  void updateActiveState(Map<Location, Material> materialsByLocation) {
    for (Entry<Location,Material> entry : materialsByLocation.entrySet()) {
      Location location = entry.getKey();
      Material material = entry.getValue();
      if (material != Material.AIR && this.meetsActiveCondition(materialsByLocation, location.getBlock().getType())) {
        location.getBlock().setType(material);
      }
    }

    materialsByLocation.clear();
  }

  boolean meetsActiveCondition(Map<Location, Material> materialsByLocation, Material material) {
    if (materialsByLocation != this.cachedMaterialsByLocation && materialsByLocation != this.pendingMaterialsByLocation) {
      if (materialsByLocation == this.secondaryMaterialsByLocation) {
        return material == Material.ACACIA_LOG;
      } else {
        return materialsByLocation != this.trackedMaterialsByLocation
            ? true
            : material.name().startsWith("DEAD_") && material.name().contains("CORAL");
      }
    } else {
      return material == Material.TUFF
          || material.name().startsWith("DEAD_") && material.name().endsWith("_CORAL_BLOCK");
    }
  }

  void activateSourceAbility(Player player) {
    int configuredValue = ConfigValueCache.getInt(this.plugin, "lifeT2.armorRepairAmount", 6);
    ItemStack item = player.getInventory().getHelmet();
    ItemStack heldItem = player.getInventory().getChestplate();
    ItemStack targetItem = player.getInventory().getLeggings();
    ItemStack candidateItem = player.getInventory().getBoots();
    if (item != null && item.hasItemMeta()) {
      this.updatePendingState(item, configuredValue);
    }

    if (heldItem != null && heldItem.hasItemMeta()) {
      this.updatePendingState(heldItem, configuredValue);
    }

    if (targetItem != null && targetItem.hasItemMeta()) {
      this.updatePendingState(targetItem, configuredValue);
    }

    if (candidateItem != null && candidateItem.hasItemMeta()) {
      this.updatePendingState(candidateItem, configuredValue);
    }
  }

  void updatePendingState(ItemStack item, int count) {
    if (item.getType().getMaxDurability() > 0) {
      short value = item.getDurability();
      short currentValue = (short) Math.max(0, value - count);
      item.setDurability(currentValue);
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        if (!this.plugin.isGemsDisabled()
           ) {
          if (!this.plugin.canUseAbility(player)) {
            boolean mainHandItem =
                isGemItem(player.getInventory().getItemInMainHand())
                    || isGemItem(player.getInventory().getItemInOffHand());
            if (mainHandItem) {
              if (event.getCause() == DamageCause.WITHER) {
                event.setCancelled(true);
              }

              if (this.getAbilityIntValue(player) > 4 && player.hasPotionEffect(PotionEffectType.WITHER)) {
                player.removePotionEffect(PotionEffectType.WITHER);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    ItemStack item = event.getItemDrop().getItemStack();
    if (isGemItem(item)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          boolean hasGemInOffHand = isGemItem(player.getInventory().getItemInOffHand());
          if (hasGemInOffHand) {
            Material material = event.getItem().getType();
            if (material == Material.GOLDEN_APPLE) {
              int configuredValue =
                  ConfigValueCache.getInt(this.plugin, "lifeT2.goldenAppleAbsorptionDuration", 2400);
              int index = ConfigValueCache.getInt(this.plugin, "lifeT2.goldenAppleAbsorptionLevel", 1);
              float scale =
                  (float) ConfigValueCache.getDouble(this.plugin, "lifeT2.goldenAppleSaturation", 9.6);
              player.addPotionEffect(
                  new PotionEffect(PotionEffectType.ABSORPTION, configuredValue, index, false, false, true));
              player.setSaturation(Math.min(20.0F, player.getSaturation() + scale));
            } else if (material == Material.ENCHANTED_GOLDEN_APPLE) {
              int remaining =
                  ConfigValueCache.getInt(
                      this.plugin, "lifeT2.enchantedGoldenAppleAbsorptionDuration", 6000);
              int step =
                  ConfigValueCache.getInt(this.plugin, "lifeT2.enchantedGoldenAppleAbsorptionLevel", 4);
              float size =
                  (float)
                      ConfigValueCache.getDouble(
                          this.plugin, "lifeT2.enchantedGoldenAppleSaturation", 9.6);
              player.addPotionEffect(
                  new PotionEffect(PotionEffectType.ABSORPTION, remaining, step, false, false, true));
              player.setSaturation(Math.min(20.0F, player.getSaturation() + size));
            } else if (material == Material.GOLDEN_CARROT) {
              float speed =
                  (float) ConfigValueCache.getDouble(this.plugin, "lifeT2.goldenCarrotSaturation", 14.4);
              player.setSaturation(Math.min(20.0F, player.getSaturation() + speed));
            } else if (material == Material.COOKED_BEEF) {
              float volume =
                  (float) ConfigValueCache.getDouble(this.plugin, "lifeT2.cookedBeefSaturation", 12.8);
              player.setSaturation(Math.min(20.0F, player.getSaturation() + volume));
            } else if (material == Material.COOKED_PORKCHOP) {
              float pitch =
                  (float)
                      ConfigValueCache.getDouble(this.plugin, "lifeT2.cookedPorkchopSaturation", 12.8);
              player.setSaturation(Math.min(20.0F, player.getSaturation() + pitch));
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player
        && event.getEntity() instanceof Player targetPlayer) {
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        if (!this.plugin.isGemsDisabled()
           ) {
          if (!this.plugin.isGemsDisabled()
             ) {
            if (!this.plugin.canUseAbility(targetPlayer)) {
              if (!this.plugin.canUseAbility(player)) {
                if (isGemItem(player.getInventory().getItemInMainHand())) {
                  if (CooldownService.isOnCooldown(player.getUniqueId(), "heart_lock")) {
                    if (ActiveAbilityStore.isActive(player.getUniqueId(), "heart_lock")) {
                      return;
                    }

                    String message =
                        AbilityStatusFormatter.formatDisplayTextForPlayer(
                            CooldownService.remainingMillis(player.getUniqueId(), "heart_lock"),
                            false);
                    if (!message.equals("Ready!")) {
                      String accentColor = ChatColor.of("#FE04B4").toString();
                      String red = ChatColor.RED.toString();
                      String displayColor = ChatColor.of("#FE04B4").toString();
                      String textColor = ChatColor.RED.toString();
                      String labelColor = ChatColor.LIGHT_PURPLE.toString();
                      player.sendMessage(
                          accentColor
                              + "🔮 "
                              + red
                              + "Your "
                              + displayColor
                              + "💘Heart Lock "
                              + textColor
                              + "skill is on cooldown for "
                              + labelColor
                              + message);
                    }

                    return;
                  }

                  if (this.isTrustedPlayer(player, targetPlayer)) {
                    return;
                  }

                  this.finishAbilityAction(player, targetPlayer);
                }
              }
            }
          }
        }
      }
    }
  }

  void activateActiveAbility(Player player) {
    this.completeAbilityAction(player);
  }

  void updatePrimaryAbilityState(UUID playerId) {
    this.animationSteps.remove(playerId);
    this.chargeLevels.remove(playerId);
    this.remainingTicks.remove(playerId);
    this.lastActivationTimes.remove(playerId);
    this.activeValuesByPlayer.remove(playerId);
    this.valuesByPlayer.remove(playerId);
    Player player = Bukkit.getPlayer(playerId);
    if (player != null) {
      this.completeAbilityAction(player);
    }

    BukkitRunnable runnable = this.currentTasksByPlayer.remove(playerId);
    if (runnable != null) {
      runnable.cancel();
    }
  }

  public void cleanup() {
    ActionBarQueue.unregisterRenderer(this.actionBarRenderer);
    if (this.scheduledTask != null) {
      this.scheduledTask.cancel();
      this.scheduledTask = null;
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      this.updatePrimaryAbilityState(player.getUniqueId());
    }

    for (BukkitRunnable task : this.currentTasksByPlayer.values()) {
      task.cancel();
    }

    this.initializePrimaryState();

    this.cooldownTimestamps.clear();
    this.abilityStages.clear();
    this.playerCounters.clear();
    this.trackedLocations.clear();
    this.secondaryCache.invalidateAll();
    this.activeCache.invalidateAll();
    this.cache.invalidateAll();
    this.lastUseTimes.clear();
    this.animationSteps.clear();
    this.chargeLevels.clear();
    this.remainingTicks.clear();
    this.lastActivationTimes.clear();
    this.activeValuesByPlayer.clear();
    this.valuesByPlayer.clear();
    this.currentTasksByPlayer.clear();
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    UUID playerId = player.getUniqueId();
    this.updatePrimaryAbilityState(playerId);
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.handlePlayerDeath(player), 1L);
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.restorePrimaryRespawnState(player), 1L);
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.restorePlayerAfterRespawn(player), 5L);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    this.updatePrimaryAbilityState(player.getUniqueId());
    this.activateActiveAbility(player);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      Player player = event.getPlayer();
      if (player.isSneaking()) {
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (isGemItem(offHandItem)) {
          if (player.isSneaking()) {
            Block block = event.getClickedBlock();
            if (block != null) {
              if (block.getBlockData() instanceof Ageable ageable
                  && ageable.getAge() < ageable.getMaximumAge()) {
                ageable.setAge(ageable.getMaximumAge());
                block.setBlockData(ageable);
                event.setCancelled(true);
                block.getWorld()
                    .spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        10,
                        1.0,
                        1.0,
                        1.0);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPrimaryPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      Action action = event.getAction();
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          if (this.canUseAbility(player)) {
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
              if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
                  && isGemItem(player.getInventory().getItemInMainHand())) {
                if (CooldownService.isOnCooldown(player.getUniqueId(), "heart_lock")) {
                  if (ActiveAbilityStore.isActive(player.getUniqueId(), "heart_lock")) {
                    return;
                  }

                  String message =
                      AbilityStatusFormatter.formatDisplayTextForPlayer(
                          CooldownService.remainingMillis(player.getUniqueId(), "heart_lock"), false);
                  if (!message.equals("Ready!")) {
                    String accentColor = ChatColor.of("#FE04B4").toString();
                    String red = ChatColor.RED.toString();
                    String displayColor = ChatColor.of("#FE04B4").toString();
                    String textColor = ChatColor.RED.toString();
                    String gold = ChatColor.GOLD.toString();
                    player.sendMessage(
                        accentColor
                            + "💘 "
                            + red
                            + "Your "
                            + displayColor
                            + "Heart Drainer "
                            + textColor
                            + "skill is on cooldown for "
                            + gold
                            + message);
                  }

                  return;
                }

                this.sendAbilityFeedback(player);
              }
            } else {
              if (isGemItem(player.getInventory().getItemInOffHand())) {
                if (!this.isAbilityAllowed(player.getInventory().getItemInMainHand())) {
                  return;
                }

                if (!player.isSneaking()) {
                  return;
                }

                if (CooldownService.isOnCooldown(player.getUniqueId(), "life_vitalityt2")) {
                  String argument =
                      Long.toString(CooldownService.remainingSeconds(playerId, "life_vitalityt2"));
                  if (ActiveAbilityStore.isActive(player.getUniqueId(), "life_vitalityt2")) {
                    return;
                  }

                  if (!argument.equals("Ready!")) {
                    String statusColor = ChatColor.of("#FE04B4").toString();
                    String loreColor = ChatColor.RED.toString();
                    String prefixColor = ChatColor.of("#FE04B4").toString();
                    String suffixColor = ChatColor.RED.toString();
                    String normalizedColor = ChatColor.GOLD.toString();
                    player.sendMessage(
                        statusColor
                            + "🔮 "
                            + loreColor
                            + "Your "
                            + prefixColor
                            + "Vitality Vortex "
                            + suffixColor
                            + "skill is on cooldown for "
                            + normalizedColor
                            + argument
                            + "s");
                  }

                  return;
                }

                this.applyPrimaryAbilityEffects(player);
                return;
              }

              if (isGemItem(player.getInventory().getItemInMainHand())) {
                if (CooldownService.isOnCooldown(player.getUniqueId(), "life_circle_t2")) {
                  String feedbackText =
                      AbilityStatusFormatter.formatDisplayTextForPlayer(
                          CooldownService.remainingMillis(player.getUniqueId(), "life_circle_t2"),
                          false);
                  if (!ActiveAbilityStore.isActive(playerId, "life_circle_t2")) {
                    String accentColor = ChatColor.of("#B8FFFA").toString();
                    String warningColor = ChatColor.RED.toString();
                    player.sendMessage(
                        accentColor
                            + "💖 "
                            + warningColor
                            + "Your "
                            + accentColor
                            + "Circle of Life "
                            + warningColor
                            + "skill is on cooldown for "
                            + ChatColor.GOLD
                            + feedbackText);
                  }

                  return;
                }

                this.sendPrimaryAbilityFeedback(player);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          if (isGemItem(player.getInventory().getItemInMainHand())
              && event.getRightClicked() instanceof Player) {
            if (CooldownService.isOnCooldown(player.getUniqueId(), "heart_lock")) {
              if (ActiveAbilityStore.isActive(player.getUniqueId(), "heart_lock")) {
                return;
              }

              String message =
                  AbilityStatusFormatter.formatDisplayTextForPlayer(
                      CooldownService.remainingMillis(player.getUniqueId(), "heart_lock"), false);
              if (!message.equals("Ready!")) {
                String accentColor = ChatColor.of("#FE04B4").toString();
                String red = ChatColor.RED.toString();
                String displayColor = ChatColor.of("#FE04B4").toString();
                String textColor = ChatColor.RED.toString();
                String labelColor = ChatColor.LIGHT_PURPLE.toString();
                player.sendMessage(
                    accentColor
                        + "🔮 "
                        + red
                        + "Your "
                        + displayColor
                        + "💘Heart Lock "
                        + textColor
                        + "skill is on cooldown for "
                        + labelColor
                        + message);
              }

              return;
            }

            Player targetPlayer = (Player) event.getRightClicked();
            if (this.isTrustedPlayer(player, targetPlayer)) {
              return;
            }

            this.finishAbilityAction(player, targetPlayer);
          }
        }
      }
    }
  }

  public ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(4);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.of("#befff7") + "Energy:");
      lore.add(ChatColor.of("#82EDBF") + "Pristine");
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏꜰ"
              + " ʟɪꜰᴇ");
      lore.add("");
      lore.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Green Thumb");
      lore.add(ChatColor.GRAY + "- Radiant Fist");
      lore.add(ChatColor.GRAY + "- Bonus Saturation");
      lore.add(ChatColor.GRAY + "- Bonus Absorption");
      lore.add(ChatColor.GRAY + "- Wither Immune");
      lore.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      lore.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      lore.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      lore.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      lore.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      lore.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      lore.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  void restorePlayerAfterRespawn(Player player) {
    if (player.isOnline()) {
      this.activateActiveAbility(player);
    }
  }

  void restorePrimaryRespawnState(Player player) {
    if (player.isOnline()) {
      this.updatePrimaryAbilityState(player.getUniqueId());
      this.activateActiveAbility(player);
    }
  }

  void handlePlayerDeath(Player player) {
    if (player.isOnline()) {
      this.activateActiveAbility(player);
    }
  }

  void updateActionBar() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (!this.plugin.isGemsDisabled()
         
          && !this.plugin.canUseAbility(player)) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if ((isGemItem(mainHandItem) || isGemItem(offHandItem))
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
        } else if (isGemItem(mainHandItem) || isGemItem(offHandItem)) {
          if (this.getAbilityIntValue(player) > 4 && player.hasPotionEffect(PotionEffectType.WITHER)) {
            player.removePotionEffect(PotionEffectType.WITHER);
          }

          this.applyAbilityEffects(player);
          String displayText = this.formatPrimaryDisplayText(player);
          ActionBarQueue.enqueue(player, displayText);
        }
      }
    }
  }

  static {
    LIFE_HEART_T2_ID = "life_heart_t2";
    LIFE_CIRCLE_ENEMY_ID = "life_circle_enemy";
    LIFE_HEART_DRAINER_ID = "life_heart_drainer";
    LIFE_CIRCLE_SELF_ID = "life_circle_self";
    LIFE_CIRCLE_T2_ID = "life_circle_t2";
    LIFE_CIRCLE_TRUSTED_ID = "life_circle_trusted";
    LIFE_HEART_LOCK_ID = "life_heart_lock";
  }

  private static final class HeartLockParticleController extends BukkitRunnable {
    int maxCount;
    final int durationTicks;
    final Player secondaryPlayer;
    final Player player;
    final LifeTier2Gem lifeTier2Gem;
    static final String LIFE_T2_HEART_LOCK_PARTICLE_INNER_LOOPS_ID = "lifeT2.heartLockParticleInnerLoops";
    HeartLockParticleController(LifeTier2Gem lifeTier2Gem, int count, Player player, Player targetPlayer) {
      this.lifeTier2Gem = lifeTier2Gem;
      this.durationTicks = count;
      this.secondaryPlayer = player;
      this.player = targetPlayer;
      this.maxCount = 0;
    }

    public void run() {
      if (this.maxCount < this.durationTicks && this.secondaryPlayer.isOnline() && this.player.isOnline()) {
        int configuredValue = ConfigValueCache.getInt(this.lifeTier2Gem.plugin, LIFE_T2_HEART_LOCK_PARTICLE_INNER_LOOPS_ID, 10);
        new BukkitRunnable() {
          final HeartLockParticleController activeAbilityTickTask = HeartLockParticleController.this;
          final int maxCount = configuredValue;
          int durationTicks = 0;

          @Override
          public void run() {
            if (this.durationTicks < this.maxCount && this.activeAbilityTickTask.secondaryPlayer.isOnline() && this.activeAbilityTickTask.player.isOnline()) {
              Location location = this.activeAbilityTickTask.secondaryPlayer.getLocation().add(0.0, 0.5, 0.0);
              Location targetLocation = this.activeAbilityTickTask.player.getLocation().add(0.0, 0.5, 0.0);
              this.activeAbilityTickTask.lifeTier2Gem.spawnPendingAbilityParticles(targetLocation, location);
              this.durationTicks++;
            } else {
              this.cancel();
            }
          }
        }
            .runTaskTimer(this.lifeTier2Gem.plugin, SharedScheduler.staggeredInitialDelay(2L), 2L);
        this.maxCount++;
      } else {
        this.lifeTier2Gem.currentTasksByPlayer.remove(this.player.getUniqueId());
        this.cancel();
      }
    }

}

  private static final class CircleOfLifeController extends BukkitRunnable {
    int remainingTicks;
    boolean active;
    final DustOptions secondaryDustOptions;
    final DustOptions dustOptions;
    final int cooldownTicks;
    final Player player;
    final List<BukkitTask> secondaryEntries;
    final List<Player> entries;
    final int chargeLevel;
    final int animationStep;
    final int maxCount;
    final int durationTicks;
    final double effectRadius;
    final double distanceThreshold;
    final int durationSeconds;
    final LifeTier2Gem lifeTier2Gem;

    CircleOfLifeController(
        LifeTier2Gem lifeTier2Gem,
        int count,
        Player player,
        List<BukkitTask> entries,
        List<Player> currentEntries,
        int index,
        int remaining,
        int step,
        int ticks,
        double value,
        double distance,
        int durationTicks) {
      this.lifeTier2Gem = lifeTier2Gem;
      this.cooldownTicks = count;
      this.player = player;
      this.secondaryEntries = entries;
      this.entries = currentEntries;
      this.chargeLevel = index;
      this.animationStep = remaining;
      this.maxCount = step;
      this.durationTicks = ticks;
      this.effectRadius = value;
      this.distanceThreshold = distance;
      this.durationSeconds = durationTicks;
      this.remainingTicks = this.cooldownTicks * 2;
      this.active = false;
      this.secondaryDustOptions = new DustOptions(Color.fromRGB(0, 166, 44), 1.0F);
      this.dustOptions = new DustOptions(Color.fromRGB(255, 0, 179), 1.0F);
    }

    public void run() {
      if (this.remainingTicks > 0 && this.player.isOnline() && !this.player.isDead()) {
        ArrayList<Player> entries = new ArrayList<>();
        this.lifeTier2Gem.scheduleAbilityUpdate(this.player);
        if (this.player.getInventory().contains(Material.DRAGON_EGG)) {
          this.lifeTier2Gem.cleanupAbilityState(this.player, "life_circle_self", this.maxCount);
        } else {
          this.lifeTier2Gem.cleanupAbilityState(this.player, "life_circle_self", this.durationTicks);
        }

        for (Player player : this.player.getLocation().getNearbyPlayers(this.effectRadius, this.effectRadius, this.effectRadius)) {
          if (!player.equals(this.player)) {
            entries.add(player);
            this.lifeTier2Gem.scheduleAbilityUpdate(player);
            if (this.lifeTier2Gem.isTrustedPlayer(this.player, player)) {
              if (player.getInventory().contains(Material.DRAGON_EGG)) {
                this.lifeTier2Gem.cleanupAbilityState(player, "life_circle_trusted", this.animationStep + 4);
              } else {
                this.lifeTier2Gem.cleanupAbilityState(player, "life_circle_trusted", this.chargeLevel + 4);
              }

              this.lifeTier2Gem.activateSourceAbility(this.player);
              this.lifeTier2Gem.activateSourceAbility(player);
            } else if (player.getInventory().contains(Material.DRAGON_EGG)) {
              this.lifeTier2Gem.cleanupAbilityState(player, "life_circle_enemy", this.animationStep - 4);
            } else {
              this.lifeTier2Gem.cleanupAbilityState(player, "life_circle_enemy", this.chargeLevel - 4);
            }

            this.lifeTier2Gem.processAbilityState(player);
          }
        }

        for (Player targetPlayer : new ArrayList<>(this.entries)) {
          if (!entries.contains(targetPlayer)) {
            this.lifeTier2Gem.activatePrimaryAbility(targetPlayer, this.chargeLevel, this.animationStep);
          }
        }

        this.entries.clear();
        this.entries.addAll(entries);
        Location location = this.player.getLocation().clone();
        if (!this.active) {
          ParticleEffects.createSimpleDustTrailTask(this.dustOptions, location, this.distanceThreshold, 10);
          this.active = true;
        }

        if (this.remainingTicks % 8 == 0 && this.remainingTicks != this.cooldownTicks && this.active) {
          ParticleEffects.createSimpleDustTrailTask(this.secondaryDustOptions, location, this.distanceThreshold, 5);
          ParticleEffects.updatePlayerState(
              Particle.CHERRY_LEAVES, location.clone().add(0.0, 1.0, 0.0), this.distanceThreshold, 75);
        }

        int configuredValue = ConfigValueCache.getInt(this.lifeTier2Gem.plugin, "lifeT2.circleOfLifeInnerLoopDelay", 5);
        int index = ConfigValueCache.getInt(this.lifeTier2Gem.plugin, "lifeT2.circleOfLifeCircleDrawDelay", 15);
        int remaining = ConfigValueCache.getInt(this.lifeTier2Gem.plugin, "lifeT2.lifeDrainedSoloDuration", 20);
        BukkitTask scheduledTask =
            new BukkitRunnable() {
              final CircleOfLifeController dustTrailTask = CircleOfLifeController.this;
              final int cooldownTicks = remaining;
              final int maxCount = index;
              int durationTicks = 0;

              @Override
              public void run() {
                if (this.durationTicks < 5 && this.dustTrailTask.player.isOnline() && !this.dustTrailTask.player.isDead()) {
                  Location location = this.dustTrailTask.player.getLocation();

                  for (Player player : this.dustTrailTask.player.getWorld().getPlayers()) {
                    if (location.distance(player.getLocation()) <= this.dustTrailTask.distanceThreshold && !player.equals(this.dustTrailTask.player)) {
                      boolean enabled = this.dustTrailTask.lifeTier2Gem.isTrustedPlayer(this.dustTrailTask.player, player);
                      if (enabled) {
                        this.dustTrailTask.lifeTier2Gem.activateSourceAbility(player);
                        Integer remainingDuration = this.dustTrailTask.lifeTier2Gem.remainingTicks.get(player.getUniqueId());
                        if (remainingDuration == null || remainingDuration < 1) {
                          this.dustTrailTask.lifeTier2Gem.remainingTicks.put(player.getUniqueId(), this.dustTrailTask.durationSeconds);
                        }
                      } else {
                        Integer currentPlayerId = this.dustTrailTask.lifeTier2Gem.animationSteps.get(player.getUniqueId());
                        if (currentPlayerId == null || currentPlayerId < 1) {
                          this.dustTrailTask.lifeTier2Gem.animationSteps.put(player.getUniqueId(), this.cooldownTicks);
                        }
                      }
                    }
                  }

                  BukkitTask scheduledTask =
                  new BukkitRunnable() {
                    int durationTicks = 0;

                    @Override
                    public void run() {
                      if (this.durationTicks < 5 && dustTrailTask.player.isOnline() && !dustTrailTask.player.isDead()) {
                        Location location = dustTrailTask.player.getLocation().clone();
                        World world = location.getWorld();
                        if (world == null) {
                          this.cancel();
                          dustTrailTask.secondaryEntries.remove(this);
                        } else {
                          dustTrailTask.lifeTier2Gem.spawnTargetAbilityParticles(location, dustTrailTask.distanceThreshold);
                          if (dustTrailTask.remainingTicks % 4 == 0) {
                            world.spawnParticle(
                                Particle.CHERRY_LEAVES,
                                location,
                                1,
                                Math.max(0.2, dustTrailTask.distanceThreshold - 2.0),
                                Math.max(0.2, dustTrailTask.distanceThreshold - 2.0),
                                Math.max(0.2, dustTrailTask.distanceThreshold - 2.0),
                                0.01);
                          }

                          if (dustTrailTask.remainingTicks % 2 == 0) {
                            world.spawnParticle(
                                Particle.HAPPY_VILLAGER,
                                location,
                                2,
                                Math.max(0.2, dustTrailTask.distanceThreshold - 2.0),
                                Math.max(0.2, dustTrailTask.distanceThreshold - 2.0),
                                Math.max(0.2, dustTrailTask.distanceThreshold - 2.0),
                                0.0);
                          }

                          this.durationTicks++;
                        }
                      } else {
                        this.cancel();
                        dustTrailTask.secondaryEntries.remove(this);
                      }
                    }
                  }.runTaskTimer(this.dustTrailTask.lifeTier2Gem.plugin, 0L, Math.max(1, this.maxCount));
                  this.dustTrailTask.secondaryEntries.add(scheduledTask);
                  this.durationTicks++;
                } else {
                  this.cancel();
                  this.dustTrailTask.secondaryEntries.remove(this);
                }
              }
            }.runTaskTimer(this.lifeTier2Gem.plugin, 0L, Math.max(1, configuredValue));
        this.secondaryEntries.add(scheduledTask);
        this.remainingTicks--;
      } else {
        for (BukkitTask task : new ArrayList<>(this.secondaryEntries)) {
          if (task != null) {
            task.cancel();
          }
        }

        this.secondaryEntries.clear();

        for (Player sourcePlayer : new ArrayList<>(this.entries)) {
          this.lifeTier2Gem.activatePrimaryAbility(sourcePlayer, this.chargeLevel, this.animationStep);
        }

        this.lifeTier2Gem.activatePrimaryAbility(this.player, this.chargeLevel, this.animationStep);
        this.entries.clear();
        this.cancel();
      }
    }
  }
}
