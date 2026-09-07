package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpeedTier2Gem implements Listener {
  static final String SPEED_THUNDER_T2_ID;
  static final String SPEED_BLUR_T2_ID;
  final Cache<UUID, Long> activeCache;
  final Cache<UUID, Long> secondaryCache;
  final Map<UUID, Integer> playerCounters = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastActivationTimes = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastUpdateTimes = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> secondaryFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Set<UUID>> playerRelations = new ConcurrentHashMap<>();
  final Map<UUID, Integer> chargeLevels = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> currentFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Integer> abilityStages = new ConcurrentHashMap<>();
  final Cache<UUID, Boolean> cache;
  final Map<UUID, Long> expiryTimes = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> flagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, List<NPC>> activeNpcsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, UUID> playerLinks = new ConcurrentHashMap<>();
  final Map<UUID, Long> cooldownTimestamps = new ConcurrentHashMap<>();
  Bliss plugin;
  int maxCount;
  int durationTicks;
  TrustCommand trustCommand;
  final Map<UUID, UUID> secondaryPlayerLinks = new ConcurrentHashMap<>();
  AstraTier2Gem astraTier2Gem;
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
    return player.getInventory().contains(Material.DRAGON_EGG)
        || this.currentFlagsByPlayer.getOrDefault(player.getUniqueId(), false);
  }

  public SpeedTier2Gem(Bliss bliss) {
    this.plugin = bliss;
    this.maxCount = ConfigValueCache.getInt(bliss, "speedT2.speedGemPotionLevel", 1);
    this.durationTicks = ConfigValueCache.getInt(bliss, "speedT2.speedGemDolphinsGraceLvl", 0);
    this.activeCache =
        CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(1L, TimeUnit.HOURS).build();
    this.secondaryCache =
        CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(1L, TimeUnit.HOURS).build();
    this.cache =
        CacheBuilder.newBuilder().maximumSize(1000L).expireAfterWrite(5L, TimeUnit.SECONDS).build();
    this.cleanupAbilityState();
    this.scheduleAbilityUpdate();
    this.playAbilityEffects();
    this.initialize();
    this.refreshAbilityState();
    this.updateAbilityState();
    this.spawnAbilityEffects();
    this.startBackgroundTasks();
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            long now = System.currentTimeMillis();
            Iterator<Entry<UUID,Long>> iterator = SpeedTier2Gem.this.expiryTimes.entrySet().iterator();

            while (iterator.hasNext()) {
              Entry<UUID, Long> entry = iterator.next();
              UUID playerId = entry.getKey();
              Long storedTimestamp = entry.getValue();
              if (storedTimestamp <= now) {
                iterator.remove();
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void refreshAbilityState() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if (SpeedTier2Gem.this.plugin.isGemsDisabled()
                 ) {
                if (SpeedTier2Gem.this.plugin.canUseAbility(player)) {
                  return;
                }

                if (SpeedTier2Gem.isActiveForPlayer(mainHandItem) || SpeedTier2Gem.isActiveForPlayer(offHandItem)) {
                  String darkGray = ChatColor.DARK_GRAY.toString();
                  String bold = ChatColor.BOLD.toString();
                  ActionBarQueue.enqueue(
                      player,
                      "🔒 " + darkGray + bold + "ᴅɪꜱᴀʙʟᴇᴅ");
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void updateAbilityState() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              if (!SpeedTier2Gem.this.plugin.isGemsDisabled()
                 
                  && !SpeedTier2Gem.this.plugin.canUseAbility(player)) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (SpeedTier2Gem.isActiveForPlayer(mainHandItem) || SpeedTier2Gem.isActiveForPlayer(offHandItem)) {
                  SpeedTier2Gem.this.activateAbility(player, playerId);
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(100L), 100L);
  }

  void activateAbility(Player player, UUID playerId) {
    long now = System.currentTimeMillis();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        for (ItemStack item : player.getInventory().getContents()) {
          if (item != null && item.getType() != Material.AIR) {
            if ((this.isGemItem(item) || this.isAbilityAllowed(item))
                && !item.containsEnchantment(Enchantment.EFFICIENCY)) {
              item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 5);
            }

            if (this.isAbilityBlocked(item)
                && item.getEnchantmentLevel(Enchantment.SOUL_SPEED) < 3) {
              item.addUnsafeEnchantment(Enchantment.SOUL_SPEED, 3);
            }
          }
        }
      }
    }
  }

  boolean isGemItem(ItemStack item) {
    return item.getType().name().endsWith("_PICKAXE");
  }

  boolean isAbilityAllowed(ItemStack item) {
    return item.getType().name().endsWith("_AXE");
  }

  boolean isAbilityBlocked(ItemStack item) {
    return item.getType().name().endsWith("_BOOTS");
  }

  boolean isMatchingState(ItemStack item) {
    return item.getType().name().endsWith("_SWORD");
  }

  boolean isValidTarget(ItemStack item) {
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

  void initialize() {
    ActionBarQueue.registerRenderer(this.actionBarRenderer);
  }

  void applyAbilityEffects(UUID playerId, Cache<UUID, Long> cache, long timestamp) {
    if (timestamp <= 0L) {
      cache.invalidate(playerId);
      String text = this.formatDisplayText(cache);
      if (text != null) {
        ActiveAbilityStore.removeAbility(playerId, text);
      }
    } else {
      String message = this.formatDisplayText(cache);
      int count = this.getSpeedT2ThunderStepActiveSeconds(cache);
      if (message != null && count > 0) {
        ActiveAbilityStore.startActive(playerId, message, count, (long) Math.ceil(timestamp / 1000.0));
        timestamp += count * 1000L;
      }

      long now = System.currentTimeMillis() + timestamp;
      cache.put(playerId, now);
    }
  }

  boolean isProtectedTarget(UUID playerId, Cache<UUID, Long> cache) {
    Long storedTimestamp = cache.getIfPresent(playerId);
    return storedTimestamp != null && System.currentTimeMillis() < storedTimestamp;
  }

  long getCooldownMillis(UUID playerId, Cache<UUID, Long> cache) {
    Long storedTimestamp = cache.getIfPresent(playerId);
    if (storedTimestamp == null) {
      return 0L;
    }

    long now = storedTimestamp - System.currentTimeMillis();
    return Math.max(0L, now);
  }

  String formatDisplayText(Cache<UUID, Long> cache) {
    if (cache == this.activeCache) {
      return "speed_thunder_t2";
    } else {
      return cache == this.secondaryCache ? "speed_blur_t2" : null;
    }
  }

  int getSpeedT2ThunderStepActiveSeconds(Cache<UUID, Long> cache) {
    if (cache == this.activeCache) {
      return ConfigValueCache.getInt(this.plugin, "speedT2.thunderStepActiveSeconds", 0);
    } else {
      return cache == this.secondaryCache
          ? ConfigValueCache.getInt(this.plugin, "speedT2.blurCooldownActiveSeconds", 0)
          : 0;
    }
  }

  String formatDisplayTextForPlayer(UUID playerId, Cache<UUID, Long> cache) {
    String text = this.formatDisplayText(cache);
    return text != null && ActiveAbilityStore.isActive(playerId, text)
        ? ChatColor.RED + "Active..."
        : this.formatDisplayTextForTarget(this.getCooldownMillis(playerId, cache));
  }

  String formatDisplayTextForTarget(long timestamp) {
    if (timestamp <= 1000L) {
      return ChatColor.GREEN + "Ready!";
    }

    long lastUpdateTime = timestamp / 1000L;
    if (lastUpdateTime <= 0L) {
      lastUpdateTime = 1L;
    }

    long startTime = lastUpdateTime / 60L;
    lastUpdateTime %= 60L;
    return startTime > 0L
        ? ChatColor.AQUA + String.format("%dm %ds", startTime, lastUpdateTime)
        : ChatColor.AQUA + String.format("%ds", lastUpdateTime);
  }

  String formatDisplayTextFromConfig(long timestamp, boolean enabled) {
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

  void playAbilityEffects() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            SpeedTier2Gem.this.activeCache.cleanUp();
            SpeedTier2Gem.this.secondaryCache.cleanUp();
            SpeedTier2Gem.this.cache.cleanUp();
            Iterator<Entry<UUID,Integer>> iterator = SpeedTier2Gem.this.abilityStages.entrySet().iterator();

            while (iterator.hasNext()) {
              Entry<UUID, Integer> entry = iterator.next();
              int count = entry.getValue() - 1;
              if (count <= 0) {
                iterator.remove();
              } else {
                entry.setValue(count);
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void spawnAbilityEffects() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            long now = System.currentTimeMillis();
            int configuredValue = ConfigValueCache.getInt(SpeedTier2Gem.this.plugin, "speedT2.speedStormMaxCharges", 3);
            int index = ConfigValueCache.getInt(SpeedTier2Gem.this.plugin, "speedT2.speedStormInitialCharges", 3);
            int remaining = ConfigValueCache.getInt(SpeedTier2Gem.this.plugin, "speedT2.speedStormRecharge", 20);
            long timestamp = remaining * 1000L;

            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              long lastUpdateTime = SpeedTier2Gem.this.canUseAbility(player) ? Math.max(1000L, timestamp / 2L) : timestamp;
              SpeedTier2Gem.this.playerCounters.putIfAbsent(playerId, index);
              int count = SpeedTier2Gem.this.playerCounters.get(playerId);
              if (count < configuredValue) {
                Long storedTimestamp = SpeedTier2Gem.this.lastUseTimes.get(playerId);
                if (storedTimestamp == null) {
                  SpeedTier2Gem.this.lastUseTimes.put(playerId, now);
                } else {
                  long startTime = now - storedTimestamp;
                  if (startTime >= lastUpdateTime) {
                    SpeedTier2Gem.this.playerCounters.put(playerId, count + 1);
                    SpeedTier2Gem.this.lastUseTimes.put(playerId, now);
                  }
                }
              } else {
                SpeedTier2Gem.this.lastUseTimes.put(playerId, now);
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void cleanupAbilityState() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              if (!SpeedTier2Gem.this.plugin.isGemsDisabled()
                 
                  && !SpeedTier2Gem.this.plugin.canUseAbility(player)) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                boolean enabled = SpeedTier2Gem.isActiveForPlayer(mainHandItem) || SpeedTier2Gem.isActiveForPlayer(offHandItem);
                if (enabled) {
                  SpeedTier2Gem.this.spawnAbilityParticles(player, mainHandItem, offHandItem);
                  ItemStack item = player.getInventory().getBoots();
                  if (item != null && item.getType() != Material.AIR) {
                    item.addUnsafeEnchantment(Enchantment.SOUL_SPEED, 3);
                  }
                }

                if (player.getLocation().getBlock().getType() == Material.SOUL_SAND) {
                  SpeedTier2Gem.this.completeAbilityAction(player, mainHandItem, offHandItem);
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void scheduleAbilityUpdate() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              if (SpeedTier2Gem.this.plugin.isGemsDisabled()
                 ) {
                return;
              }

              if (SpeedTier2Gem.this.plugin.canUseAbility(player)) {
                return;
              }

              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if ((SpeedTier2Gem.isActiveForPlayer(mainHandItem) || SpeedTier2Gem.isActiveForPlayer(offHandItem))
                  && !Boolean.TRUE.equals(SpeedTier2Gem.this.cache.getIfPresent(player.getUniqueId()))) {
                SpeedTier2Gem.this.cache.put(player.getUniqueId(), true);
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void spawnAbilityParticles(Player player, ItemStack item, ItemStack heldItem) {
    if (isActiveForPlayer(item) || isActiveForPlayer(heldItem)) {
      if (this.plugin.isGemsDisabled()) {
        return;
      }

      if (this.plugin.canUseAbility(player)) {
        return;
      }

      if (this.isAbilityActive(player.getUniqueId())) {
        return;
      }

      if (player.getLocation().getBlock().getType() != Material.WATER && this.maxCount >= 0) {
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.SPEED, 20, this.maxCount, true, true, true));
      }

      if (player.getLocation().getBlock().getType() == Material.WATER && this.durationTicks >= 0) {
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, this.durationTicks, false, true));
      }

      if (player.isSprinting() && player.getLocation().getBlock().getType() == Material.WATER) {
        Location location = player.getLocation().subtract(0.0, 0.5, 0.0);
        player.getWorld()
            .spawnParticle(
                Particle.DUST, location, 1, new DustOptions(Color.fromRGB(244, 255, 28), 1.0F));
      }
    }
  }

  void completeAbilityAction(Player player, ItemStack item, ItemStack heldItem) {
    int chargeLevel = this.chargeLevels.getOrDefault(player.getUniqueId(), 6);
    if (chargeLevel <= 1 && (isActiveForPlayer(item) || isActiveForPlayer(heldItem))) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 8, false, true));
    }
  }

  void sendAbilityFeedback(Player player) {
    UUID playerId = player.getUniqueId();
    Long storedTimestamp = this.expiryTimes.get(playerId);
    String text;
    if (storedTimestamp != null && storedTimestamp > System.currentTimeMillis()) {
      text = ChatColor.RED + "Active...";
    } else {
      text = this.formatDisplayTextForPlayer(playerId, this.secondaryCache);
    }

    String message = this.formatDisplayTextForPlayer(playerId, this.activeCache);
    int configuredValue = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormMaxCharges", 3);
    int count = this.playerCounters.getOrDefault(playerId, configuredValue);
    String displayText;
    if (count == configuredValue) {
      displayText = ChatColor.GREEN + "Ready!";
    } else {
      long now = System.currentTimeMillis();
      Long targetLong = this.lastUseTimes.get(playerId);
      if (targetLong != null) {
        int index = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormRecharge", 20);
        if (this.canUseAbility(player)) {
          index = Math.max(1, index / 2);
        }

        long timestamp = now - targetLong;
        long lastUpdateTime = index * 1000L - timestamp;
        long startTime = Math.max(0L, lastUpdateTime / 1000L);
        displayText = ChatColor.AQUA.toString() + count + "/" + configuredValue + " " + ChatColor.AQUA + startTime + "s";
      } else {
        displayText = ChatColor.AQUA.toString() + count + "/" + configuredValue;
      }
    }

    if (isActiveForPlayer(player.getInventory().getItemInMainHand())
        || isActiveForPlayer(player.getInventory().getItemInOffHand())) {
      String textColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.YELLOW.toString();
      String gray = ChatColor.GRAY.toString();
      String nameColor = ChatColor.WHITE.toString();
      ActionBarQueue.enqueue(
          player,
          textColor
              + "🎯 "
              + text
              + " "
              + labelColor
              + "🔮 "
              + message
              + gray
              + " "
              + nameColor
              + "🌩 "
              + displayText);
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (player.isSneaking()) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          if (!this.isAbilityActive(playerId)) {
            if ((event.getAction() == Action.RIGHT_CLICK_AIR
                    || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && isActiveForPlayer(player.getInventory().getItemInOffHand())) {
              if (this.isValidTarget(player.getInventory().getItemInMainHand())) {
                return;
              }

              long now = System.currentTimeMillis();
              if (!this.isProtectedTarget(player.getUniqueId(), this.activeCache)) {
                Location location = player.getLocation().clone();
                Location targetLocation = player.getLocation().clone().add(0.0, 1.0, 0.0);
                this.processAbilityState(targetLocation);
                double configuredValue =
                    ConfigValueCache.getDouble(this.plugin, "speedT2.thunderStepDistance", 6.0);
                Vector offset = player.getLocation().getDirection().normalize();
                Location origin = player.getLocation().add(offset.multiply(configuredValue));
                origin = this.getTargetLocation(player, location, origin, configuredValue);
                player.getWorld().playSound(location, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0F, 1.0F);
                this.handleAbilityAction(player, origin);
                player.sendMessage(
                    ChatColor.YELLOW
                        + "🔮 "
                        + ChatColor.YELLOW
                        + "You have used "
                        + ChatColor.of("#ED8790")
                        + "Thunder Step");
                this.secondaryFlagsByPlayer.put(playerId, false);
                int index = ConfigValueCache.getInt(this.plugin, "speedT2.thunderStepCooldown", 15);
                long timestamp;
                if (this.canUseAbility(player)) {
                  timestamp = index * 1000L / 2L;
                } else {
                  timestamp = index * 1000L;
                }

                this.applyAbilityEffects(player.getUniqueId(), this.activeCache, timestamp);
              } else {
                long lastUpdateTime = this.getCooldownMillis(player.getUniqueId(), this.activeCache);
                player.sendMessage(
                    ChatColor.YELLOW
                        + "🔮 §cYour "
                        + ChatColor.of("#ED8790")
                        + "Thunder Step §cis on cooldown for§c "
                        + this.formatDisplayTextFromConfig(lastUpdateTime, false));
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (event.getEntity() instanceof Player) {
        UUID playerId = player.getUniqueId();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (isActiveForPlayer(mainHandItem) || isActiveForPlayer(offHandItem)) {
          Long storedTimestamp = this.activeCache.getIfPresent(playerId);
          if (storedTimestamp != null) {
            long now = storedTimestamp - System.currentTimeMillis();
            if (now > 0L) {
              if (!this.secondaryFlagsByPlayer.getOrDefault(playerId, false)) {
                int configuredValue =
                    ConfigValueCache.getInt(this.plugin, "speedT2.thunderStepCooldownReduction", 5);
                long timestamp = configuredValue * 1000L;
                long lastUpdateTime = storedTimestamp - timestamp;
                long startTime = System.currentTimeMillis() + 1000L;
                this.activeCache.put(playerId, Math.max(lastUpdateTime, startTime));
                this.secondaryFlagsByPlayer.put(playerId, true);
                event.getEntity()
                    .getWorld()
                    .spawnParticle(
                        Particle.CRIT,
                        event.getEntity().getLocation().add(0.0, 1.0, 0.0),
                        20,
                        0.5,
                        0.5,
                        0.5,
                        0.1);
              }
            }
          }
        }
      }
    }
  }

  Location getTargetLocation(Player player, Location location, Location targetLocation, double value) {
    Vector offset = player.getLocation().getDirection().clone();
    offset.setY(0);
    offset.normalize();
    Location origin = location.clone();
    boolean enabled = false;
    double distance = 0.5;

    for (double radius = distance; radius <= value; radius += distance) {
      Location center = location.clone().add(offset.clone().multiply(radius));
      Location destination = center.clone();
      destination.setY(location.getY());
      Location effectLocation = destination.clone().add(0.0, 1.0, 0.0);
      if (destination.getBlock().getType().isSolid() || effectLocation.getBlock().getType().isSolid()) {
        enabled = true;
        break;
      }

      origin = center.clone();
    }

    if (!enabled) {
      origin = location.clone().add(offset.clone().multiply(value));
    }

    origin.setY(this.getAbilityDoubleValue(origin));
    return origin;
  }

  void processAbilityState(Location location) {
    byte step = 40;
    double value = 1.5;

    for (int count = 0; count < step; count++) {
      double distance = (Math.PI * 2) * count / step;
      double offset = location.getY() + value * Math.sin(distance);
      double radius = location.getZ() + value * Math.cos(distance);
      Location effectLocation = new Location(location.getWorld(), location.getX(), offset, radius);
      location.getWorld().spawnParticle(Particle.CLOUD, effectLocation, 1, 0.0, 0.0, 0.0, 0.0);
    }
  }

  void handleAbilityAction(Player player, Location location) {
    if (player != null && player.isOnline()) {
      player.teleport(location);
      DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 255, 0), 1.5F);

      for (int count = 0; count < 25; count++) {
        double value = (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0;
        double distance = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 2.0;
        double radius = (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0;
        location.getWorld().spawnParticle(Particle.DUST, location.clone().add(value, distance, radius), 1, dustOptions);
      }
    }
  }

  double getAbilityDoubleValue(Location location) {
    Location targetLocation = location.clone();

    for (int count = 0; count < 5; count++) {
      if (targetLocation.getBlock().getType().isSolid()
          && !targetLocation.clone().add(0.0, 1.0, 0.0).getBlock().getType().isSolid()
          && !targetLocation.clone().add(0.0, 2.0, 0.0).getBlock().getType().isSolid()) {
        return targetLocation.getY() + 1.0;
      }

      targetLocation.add(0.0, -1.0, 0.0);
    }

    targetLocation = location.clone();

    for (int index = 0; index < 5; index++) {
      if (targetLocation.getBlock().getType().isSolid()
          && !targetLocation.clone().add(0.0, 1.0, 0.0).getBlock().getType().isSolid()
          && !targetLocation.clone().add(0.0, 2.0, 0.0).getBlock().getType().isSolid()) {
        return targetLocation.getY() + 1.0;
      }

      targetLocation.add(0.0, 1.0, 0.0);
    }

    return location.getY();
  }

  @EventHandler
  public void resetAbilityState(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player && event.getEntity() instanceof Player targetPlayer) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          if (!this.plugin.canUseAbility(targetPlayer)) {
            UUID playerId = player.getUniqueId();
            if (!this.isAbilityActive(playerId)) {
              Long storedTimestamp = this.lastActivationTimes.get(playerId);
              if (storedTimestamp == null || System.currentTimeMillis() - storedTimestamp >= 2000L) {
                int index = this.chargeLevels.getOrDefault(player.getUniqueId(), 6);
                if (index > 1) {
                  long now = System.currentTimeMillis();
                  Long targetLong = this.expiryTimes.get(playerId);
                  int configuredValue = ConfigValueCache.getInt(this.plugin, "speedT2.blurActive", 10);
                  int remaining = ConfigValueCache.getInt(this.plugin, "speedT2.blurPrimeWindow", 5);
                  if (targetLong != null && targetLong > now) {
                    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                    ItemStack offHandItem = player.getInventory().getItemInOffHand();
                    if (!isActiveForPlayer(mainHandItem) && !isActiveForPlayer(offHandItem)) {
                      return;
                    }

                    if (Boolean.TRUE.equals(this.flagsByPlayer.get(playerId))) {
                      return;
                    }

                    if (this.isTrustedPlayer(player, targetPlayer)) {
                      player.sendMessage(
                          ChatColor.of("#FEFD17")
                              + "🔮 "
                              + ChatColor.WHITE
                              + "You cannot cast negative powers on allies!");
                      return;
                    }

                    this.trackAbilityState(player, targetPlayer);
                  } else {
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (this.isProtectedTarget(playerId, this.secondaryCache)) {
                      return;
                    }

                    Long sourceLong = this.lastUpdateTimes.get(playerId);
                    if (sourceLong != null && now - sourceLong <= remaining * 1000L) {
                      if (this.isTrustedPlayer(player, targetPlayer)) {
                        player.sendMessage(
                            ChatColor.of("#FEFD17")
                                + "🔮 "
                                + ChatColor.WHITE
                                + "You cannot cast negative powers on allies!");
                        return;
                      }

                      this.lastUpdateTimes.remove(playerId);
                      this.expiryTimes.put(playerId, now + configuredValue * 1000L);
                      this.trackAbilityState(player, targetPlayer);
                    } else {
                      if (!isActiveForPlayer(heldItem)) {
                        return;
                      }

                      if (this.isTrustedPlayer(player, targetPlayer)) {
                        player.sendMessage(
                            ChatColor.of("#FEFD17")
                                + "🔮 "
                                + ChatColor.WHITE
                                + "You cannot cast negative powers on allies!");
                        return;
                      }

                      this.lastUpdateTimes.put(playerId, now);
                      player.sendMessage(ChatColor.YELLOW + "Your next hit will inflict Blur!");
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

  void trackAbilityState(Player player, Player targetPlayer) {
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          if (!this.plugin.canUseAbility(targetPlayer)) {
            int configuredValue = ConfigValueCache.getInt(this.plugin, "speedT2.blurCooldown", 90);
            if (this.canUseAbility(player)) {
              configuredValue = Math.max(1, configuredValue / 2);
            }

            this.applyAbilityEffects(player.getUniqueId(), this.secondaryCache, configuredValue * 1000L);
            String accentColor = ChatColor.of("#FEFD17").toString();
            String messageColor = ChatColor.of("#B8FFFB").toString();
            String displayColor = ChatColor.of("#FEFD17").toString();
            String textColor = ChatColor.of("#B8FFFB").toString();
            String labelColor = ChatColor.of("#FEFD17").toString();
            String name = targetPlayer.getName();
            player.sendMessage(
                accentColor
                    + "🔮 "
                    + messageColor
                    + "You have used "
                    + displayColor
                    + "Blur "
                    + textColor
                    + "on "
                    + labelColor
                    + name);
            accentColor = ChatColor.of("#FEFD17").toString();
            messageColor = ChatColor.of("#B8FFFB").toString();
            displayColor = ChatColor.of("#FEFD17").toString();
            textColor = ChatColor.of("#B8FFFB").toString();
            labelColor = ChatColor.of("#FEFD17").toString();
            String statusText = player.getName();
            targetPlayer.sendMessage(
                accentColor
                    + "🔮 "
                    + messageColor
                    + "You have been affected by "
                    + displayColor
                    + "Blur "
                    + textColor
                    + "by "
                    + labelColor
                    + statusText);
            this.finishAbilityAction(player, targetPlayer);
          }
        }
      }
    }
  }

  Location getTargetLocationForPlayer(Location location, int count) {
    double configuredValue = ConfigValueCache.getDouble(this.plugin, "speedT2.blurNPCDistance", 2.5);

    return switch (count) {
      case 0 -> {
        Vector direction = location.getDirection().normalize();
        yield location.clone().add(direction.multiply(configuredValue));
      }
      case 1 -> {
        Vector offset = location.getDirection().normalize();
        offset = new Vector(-offset.getZ(), 0.0, offset.getX());
        yield location.clone().add(offset.multiply(configuredValue));
      }
      case 2 -> {
        Vector velocity = location.getDirection().normalize().multiply(-1);
        yield location.clone().add(velocity.multiply(configuredValue));
      }
      case 3 -> {
        Vector normalizedDirection = location.getDirection().normalize();
        normalizedDirection = new Vector(normalizedDirection.getZ(), 0.0, -normalizedDirection.getX());
        yield location.clone().add(normalizedDirection.multiply(configuredValue));
      }
      default -> location.clone();
    };
  }

  void refreshPlayerState(Player player, double value) {
    double distance = player.getHealth();
    double radius = Math.max(0.0, distance - value);
    player.setHealth(radius);
  }

  void updatePlayerState(Location location, Location targetLocation) {
    Vector direction = targetLocation.toVector().subtract(location.toVector());
    double distance = location.distance(targetLocation);
    int count = (int) (distance * 8.0);

    for (int index = 0; index <= count; index++) {
      double value = (double) index / count;
      Location origin = location.clone().add(direction.clone().multiply(value).setY(1));
      location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, origin, 1, 0.05, 0.05, 0.05, 0.0);
    }
  }

  void finishAbilityAction(Player player, Player targetPlayer) {
    int configuredValue = ConfigValueCache.getInt(this.plugin, "speedT2.blurIterations", 4);
    UUID playerId = player.getUniqueId();
    this.flagsByPlayer.put(playerId, true);
    List<NPC> entries = this.activeNpcsByPlayer.remove(playerId);
    if (entries != null) {
      for (NPC npc : entries) {
        if (npc != null) {
          npc.destroy();
        }
      }
    }

    ArrayList<NPC> currentEntries = new ArrayList<>();
    this.activeNpcsByPlayer.put(playerId, currentEntries);
    new BukkitRunnable() {
      final int maxCount = configuredValue;
      final Player secondaryPlayer = targetPlayer;
      final List<NPC> entries = currentEntries;
      final UUID capturedPlayerId = playerId;
      final Player player = targetPlayer;
      int durationTicks = 0;
      NPC npc = null;
      Location anchorLocation = null;

      @Override
      public void run() {
        if (this.durationTicks < this.maxCount && this.secondaryPlayer.isOnline() && !this.secondaryPlayer.isDead()) {
          if (this.npc != null) {
            this.npc.destroy();
          }

          Location location = SpeedTier2Gem.this.getTargetLocationForPlayer(this.secondaryPlayer.getLocation(), this.durationTicks);
          this.npc = SpeedTier2Gem.this.resolveNPC(this.player, this.secondaryPlayer, location);
          if (this.npc != null) {
            this.entries.add(this.npc);
          }

          this.secondaryPlayer.getWorld().playSound(location, Sound.ITEM_TRIDENT_THUNDER, 1.0F, 1.0F);
          if (this.anchorLocation != null) {
            SpeedTier2Gem.this.updatePlayerState(this.anchorLocation, location);
          }

          double configuredValue = ConfigValueCache.getDouble(SpeedTier2Gem.this.plugin, "speedT2.blurDamagePerHit", 1.0);
          SpeedTier2Gem.this.secondaryPlayerLinks.put(this.secondaryPlayer.getUniqueId(), this.player.getUniqueId());
          Bukkit.getScheduler().runTaskLater(SpeedTier2Gem.this.plugin, () -> this.clearPlayerLink(this.secondaryPlayer, this.player), 100L);
          this.secondaryPlayer.damage(configuredValue, DamageSource.builder(DamageType.SONIC_BOOM).build());
          double distance = ConfigValueCache.getDouble(SpeedTier2Gem.this.plugin, "speedT2.blurKnockback", 0.3);
          Vector offset = this.secondaryPlayer.getLocation().subtract(location).toVector().normalize();
          this.secondaryPlayer.setVelocity(offset.multiply(distance));
          this.anchorLocation = location.clone();
          this.durationTicks++;
        } else {
          for (NPC npc : this.entries) {
            if (npc != null) {
              npc.destroy();
            }
          }

          SpeedTier2Gem.this.activeNpcsByPlayer.remove(this.capturedPlayerId);
          SpeedTier2Gem.this.flagsByPlayer.remove(this.capturedPlayerId);
          this.cancel();
        }
      }

      private void clearPlayerLink(Player player, Player targetPlayer) {
        UUID playerId = SpeedTier2Gem.this.secondaryPlayerLinks.get(player.getUniqueId());
        if (playerId != null && playerId.equals(targetPlayer.getUniqueId())) {
          SpeedTier2Gem.this.secondaryPlayerLinks.remove(player.getUniqueId());
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(10L), 10L);
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    UUID playerId = player.getUniqueId();
    UUID targetId = this.secondaryPlayerLinks.get(playerId);
    if (targetId != null) {
      Player targetPlayer = Bukkit.getPlayer(targetId);
      if (targetPlayer != null) {
        String textColor = ChatColor.WHITE.toString();
        String name = player.getName();
        String messageColor = ChatColor.WHITE.toString();
        String displayColor = ChatColor.WHITE.toString();
        String label = targetPlayer.getName();
        event.setDeathMessage(textColor + name + messageColor + " couldn´t keep up with " + displayColor + label);
      } else {
        event.setDeathMessage(
            ChatColor.WHITE + player.getName() + ChatColor.WHITE + " couldn´t keep up");
      }

      this.secondaryPlayerLinks.remove(playerId);
    }
  }

  NPC resolveNPC(Player player, Player targetPlayer, Location location) {
    Location targetLocation = location.clone();
    if (targetLocation.getY() < 1.0) {
      targetLocation.setY(targetLocation.getWorld().getHighestBlockYAt(targetLocation) + 1);
    }

    NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, player.getName());
    npc.spawn(targetLocation);
    SkinTrait skinTrait = (SkinTrait) npc.getOrAddTrait(SkinTrait.class);
    skinTrait.setSkinName(player.getName());
    LookClose lookClose = (LookClose) npc.getOrAddTrait(LookClose.class);
    lookClose.lookClose(true);
    lookClose.setRange(10.0);
    ItemStack item = player.getInventory().getHelmet();
    ItemStack heldItem = player.getInventory().getChestplate();
    ItemStack targetItem = player.getInventory().getLeggings();
    ItemStack candidateItem = player.getInventory().getBoots();
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    Bukkit.getScheduler()
        .runTaskLater(
            this.plugin, () -> configureBlurNpc(npc, item, heldItem, targetItem, candidateItem, mainHandItem), 2L);

    try {
      npc.getNavigator().setTarget(targetPlayer, true);
    } catch (Exception ignored) {
      // Failure is non-fatal; normal event and lifecycle processing continues.
    }

    this.spawnPrimaryAbilityParticles(targetLocation, targetPlayer.getLocation());
    return npc;
  }

  void spawnPrimaryAbilityParticles(Location location, Location targetLocation) {
    World world = location.getWorld();
    world.spawnParticle(
        Particle.DAMAGE_INDICATOR, targetLocation.clone().add(0.0, 1.0, 0.0), 8, 0.3, 0.5, 0.3, 0.0);
    world.spawnParticle(Particle.CRIT, targetLocation.clone().add(0.0, 1.0, 0.0), 15, 0.3, 0.5, 0.3, 0.1);
    world.spawnParticle(
        Particle.SWEEP_ATTACK, targetLocation.clone().add(0.0, 1.0, 0.0), 2, 0.3, 0.3, 0.3, 0.0);
    DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 255, 0), 2.0F);
    world.spawnParticle(
        Particle.DUST, location.clone().add(0.0, 1.0, 0.0), 20, 0.3, 0.5, 0.3, 0.0, dustOptions);
  }

  @EventHandler
  public void onPrimaryPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          if (event.getAction() == Action.RIGHT_CLICK_AIR
              || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (isActiveForPlayer(player.getInventory().getItemInMainHand())) {
              if (!this.isValidTarget(player.getInventory().getItemInOffHand())) {
                int index = this.chargeLevels.getOrDefault(player.getUniqueId(), 6);
                if (index > 1) {
                  int count = this.playerCounters.getOrDefault(playerId, 3);
                  if (count <= 0) {
                    player.sendMessage(ChatColor.of("#ED8790") + "You dont have any clouds left!");
                  } else {
                    this.applyPrimaryAbilityEffects(player);
                    event.setCancelled(true);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  void applyPrimaryAbilityEffects(Player player) {
    UUID playerId = player.getUniqueId();
    int count = this.playerCounters.getOrDefault(playerId, 3);
    this.playerCounters.put(playerId, --count);
    int configuredValue = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormMaxCharges", 3);
    if (count == configuredValue - 1) {
      this.lastUseTimes.put(playerId, System.currentTimeMillis());
    }

    player.removePotionEffect(PotionEffectType.SPEED);
    int index = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormSpeedDuration", 100);
    int remaining = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormSpeedLevelNormal", 2);
    int step = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormSpeedLevelLast", 4);
    if (count > 0) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, index, remaining, true, true, true));
    } else {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, index, step, true, true, true));
    }

    int ticks = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormTargetRange", 15);
    Location location = this.getTargetLocationForTarget(player, ticks);
    if (location == null) {
      this.playerCounters.put(playerId, count + 1);
      player.sendMessage(ChatColor.RED + "No valid target location!");
    } else {
      this.playAbilitySound(player, location);
    }
  }

  Location getTargetLocationForTarget(Player player, int count) {
    Location location = player.getEyeLocation();
    Vector direction = location.getDirection().normalize();

    for (int index = 1; index <= count; index++) {
      Location targetLocation = location.clone().add(direction.clone().multiply(index));
      if (targetLocation.getBlock().getType().isSolid()) {
        return targetLocation.add(0.0, 1.0, 0.0);
      }
    }

    return location.clone().add(direction.clone().multiply(count));
  }

  void playAbilitySound(Player player, Location location) {
    World world = location.getWorld();
    Location targetLocation = location.clone().add(0.0, 8.0, 0.0);
    this.spawnTargetAbilityParticles(location, targetLocation);
    world.playSound(location, Sound.ITEM_TRIDENT_THUNDER, 1.5F, 0.8F);
    this.lastActivationTimes.put(player.getUniqueId(), System.currentTimeMillis());
    double configuredValue = ConfigValueCache.getDouble(this.plugin, "speedT2.speedStormMaxRadius", 6.0);
    int index = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormDuration", 6);
    int count = 15 + index * 20;
    new BukkitRunnable() {
      final double distanceThreshold = configuredValue;
      final Location anchorLocation = location;
      final int durationTicks = count;
      int maxCount = 0;
      double effectScale = 0.5;
      double effectRadius = 0.0;

      @Override
      public void run() {
        if (this.maxCount < 15) {
          this.effectScale = 0.5 + this.maxCount * (this.distanceThreshold - 0.5) / 15.0;
          SpeedTier2Gem.this.spawnSourceAbilityParticles(this.anchorLocation, this.effectScale, this.effectRadius);
          this.effectRadius += Math.PI / 8;
          if (this.maxCount % 5 == 0) {
            Location location = SpeedTier2Gem.this.getTargetLocationFromConfig(this.anchorLocation, this.effectScale);
            Location targetLocation = location.clone().add(0.0, 8.0, 0.0);
            SpeedTier2Gem.this.spawnTargetAbilityParticles(location, targetLocation);
            world.playSound(location, Sound.ITEM_TRIDENT_THUNDER, 1.0F, 1.2F);
          }
        } else if (this.maxCount < this.durationTicks) {
          SpeedTier2Gem.this.spawnSourceAbilityParticles(this.anchorLocation, this.distanceThreshold, this.effectRadius);
          this.effectRadius += Math.PI / 16;
          if (this.maxCount % 2 == 0) {
            SpeedTier2Gem.this.spawnActiveAbilityParticles(this.anchorLocation, this.distanceThreshold);
            SpeedTier2Gem.this.spawnPendingAbilityParticles(this.anchorLocation, this.distanceThreshold);
            SpeedTier2Gem.this.spawnCachedAbilityParticles(this.anchorLocation, this.distanceThreshold);
          }

          if (this.maxCount % 12 == 0) {
            int count = 1 + java.util.concurrent.ThreadLocalRandom.current().nextInt(2);

            for (int index = 0; index < count; index++) {
              Location origin = SpeedTier2Gem.this.getTargetLocationFromConfig(this.anchorLocation, this.distanceThreshold);
              Location center = origin.clone().add(0.0, 8.0, 0.0);
              SpeedTier2Gem.this.spawnTargetAbilityParticles(origin, center);
              world.playSound(
                  origin,
                  Sound.ITEM_TRIDENT_THUNDER,
                  1.0F,
                  0.9F + java.util.concurrent.ThreadLocalRandom.current().nextFloat() * 0.4F);
            }
          }

          if (this.maxCount % 20 == 0) {
            SpeedTier2Gem.this.spawnResolvedAbilityParticles(player, this.anchorLocation, this.distanceThreshold);
          }
        } else {
          this.cancel();
        }

        this.maxCount++;
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void spawnTargetAbilityParticles(Location location, Location targetLocation) {
    World world = location.getWorld();
    Location origin = location.clone();
    byte step = 5;
    double y = (targetLocation.getY() - location.getY()) / step;

    for (int count = 0; count < step; count++) {
      double value = (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 0.8;
      double distance = (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 0.8;
      Location center = origin.clone().add(value, y, distance);
      Vector direction = center.toVector().subtract(origin.toVector());
      double radius = origin.distance(center);
      int index = (int) (radius * 8.0);

      for (int remaining = 0; remaining <= index; remaining++) {
        double angle = (double) remaining / index;
        Location destination = origin.clone().add(direction.clone().multiply(angle));
        world.spawnParticle(Particle.ELECTRIC_SPARK, destination, 2, 0.05, 0.05, 0.05, 0.05);
      }

      origin = center;
    }
  }

  void spawnSourceAbilityParticles(Location location, double value, double distance) {
    byte step = 10;
    World world = location.getWorld();

    for (int count = 0; count < step; count++) {
      double radius = distance + (Math.PI * 2) * count / step;
      double offset = location.getX() + value * Math.cos(radius);
      double angle = location.getZ() + value * Math.sin(radius);
      Location targetLocation = new Location(world, offset, location.getY(), angle);
      world.spawnParticle(Particle.CLOUD, targetLocation, 1, 0.05, 0.0, 0.05, 0.0);
    }
  }

  void spawnActiveAbilityParticles(Location location, double value) {
    World world = location.getWorld();

    for (int count = 0; count < 5; count++) {
      Location targetLocation = this.getTargetLocationFromConfig(location, value);
      world.spawnParticle(Particle.END_ROD, targetLocation, 1, 0.0, 0.0, 0.0, 0.03);
    }
  }

  void spawnPendingAbilityParticles(Location location, double value) {
    World world = location.getWorld();

    for (int count = 0; count < 20; count++) {
      Location targetLocation = this.getTargetLocationFromConfig(location, value);
      double distance = 6.0 + java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 4.0;
      targetLocation.add(0.0, distance, 0.0);
      world.spawnParticle(Particle.CLOUD, targetLocation, 1, 0.3, 0.2, 0.3, 0.0);
    }
  }

  void spawnCachedAbilityParticles(Location location, double value) {
    World world = location.getWorld();

    for (int count = 0; count < 15; count++) {
      Location targetLocation = this.getTargetLocationFromConfig(location, value);
      double distance = 5.0 + java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 5.0;
      targetLocation.add(0.0, distance, 0.0);
      world.spawnParticle(Particle.FALLING_WATER, targetLocation, 1, 0.1, 0.0, 0.1, 0.0);
    }
  }

  void spawnResolvedAbilityParticles(Player player, Location location, double value) {
    World world = location.getWorld();
    double configuredValue = ConfigValueCache.getDouble(this.plugin, "speedT2.speedStormDamagePerSecond", 1.0);
    int index = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormSlownessDuration", 60);
    int remaining = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormSlownessLevel", 0);
    int step = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormTrustedSpeedDuration", 40);
    int ticks = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormTrustedSpeedLevel", 1);

    for (Player targetPlayer : world.getPlayers()) {
      if (!targetPlayer.equals(player)) {
        double distance = targetPlayer.getLocation().distance(location);
        if (distance <= value) {
          if (this.isTrustedPlayer(player, targetPlayer)) {
            targetPlayer.addPotionEffect(
                new PotionEffect(PotionEffectType.SPEED, step, ticks, false, false));
          } else if (!this.plugin.canUseAbility(targetPlayer)) {
            targetPlayer.damage(configuredValue, DamageSource.builder(DamageType.SONIC_BOOM).build());
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, index, remaining));
            targetPlayer.setCooldown(Material.WIND_CHARGE, 5);
            targetPlayer
                .getWorld()
                .spawnParticle(
                    Particle.ELECTRIC_SPARK,
                    targetPlayer.getLocation().add(0.0, 1.0, 0.0),
                    10,
                    0.3,
                    0.5,
                    0.3,
                    0.1);
          }
        }
      }
    }
  }

  Location getTargetLocationFromConfig(Location location, double value) {
    double distance = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * value;
    double radius = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 2.0 * Math.PI;
    double offset = location.getX() + distance * Math.cos(radius);
    double angle = location.getZ() + distance * Math.sin(radius);
    return new Location(location.getWorld(), offset, location.getY(), angle);
  }

  public static boolean isActiveForPlayer(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      if (!itemMeta.hasCustomModelData()) {
        return false;
      }

      if (item.getType() != Material.PRISMARINE_SHARD) {
        return false;
      }

      int count = itemMeta.getCustomModelData();
      return count == 8 || count == 28 || count == 48 || count == 68 || count == 88;
    } else {
      return false;
    }
  }

  boolean isTrustedPlayer(Player player, Player targetPlayer) {
    if (this.trustCommand != null) {
      return this.trustCommand.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId());
    }

    Set<UUID> relatedPlayers = this.playerRelations.get(player.getUniqueId());
    return relatedPlayers != null && relatedPlayers.contains(targetPlayer.getUniqueId());
  }

  public ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(8);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.of("#befff7") + "Energy:");
      lore.add(ChatColor.of("#82EDBF") + "Pristine");
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      lore.add("");
      lore.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Speed II + Dolphins Grace");
      lore.add(ChatColor.GRAY + "- Auto Enchant");
      lore.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      lore.add(ChatColor.DARK_GRAY + "  Teleport 6 blocks forward");
      lore.add(ChatColor.DARK_GRAY + "  Cooldown: 15s");
      lore.add(ChatColor.DARK_GRAY + "  First hit: -5s cooldown");
      lore.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Blur (10s window)");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Speed Storm (3 charges)");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    ItemStack item = event.getItemDrop().getItemStack();
    if (isActiveForPlayer(item)) {
      event.setCancelled(true);
    }
  }

  public void activatePrimaryAbility(Player player, int count) {
    this.chargeLevels.put(player.getUniqueId(), count);
  }

  public void activateTargetAbility(Player player, boolean enabled) {
    this.currentFlagsByPlayer.put(player.getUniqueId(), enabled);
  }

  public void activateSourceAbility(Player player, Player targetPlayer) {
    this.playerRelations
        .computeIfAbsent(player.getUniqueId(), SpeedTier2Gem::createTrackingSet)
        .add(targetPlayer.getUniqueId());
  }

  public void activateActiveAbility(Player player, Player targetPlayer) {
    Set<UUID> relatedPlayers = this.playerRelations.get(player.getUniqueId());
    if (relatedPlayers != null) {
      relatedPlayers.remove(targetPlayer.getUniqueId());
    }
  }

  public boolean hasRequiredState(Player player) {
    return this.abilityStages.getOrDefault(player.getUniqueId(), 0) > 0;
  }

  public void cleanup() {
    ActionBarQueue.unregisterRenderer(this.actionBarRenderer);
    for (List<NPC> entries : this.activeNpcsByPlayer.values()) {
      if (entries != null) {
        for (NPC npc : entries) {
          if (npc != null) {
            npc.destroy();
          }
        }
      }
    }

    this.activeNpcsByPlayer.clear();
    this.playerCounters.clear();
    this.lastUseTimes.clear();
    this.expiryTimes.clear();
    this.lastUpdateTimes.clear();
    this.flagsByPlayer.clear();
    this.lastActivationTimes.clear();
    this.secondaryFlagsByPlayer.clear();
    this.playerRelations.clear();
    this.chargeLevels.clear();
    this.currentFlagsByPlayer.clear();
    this.abilityStages.clear();
    this.playerLinks.clear();
    this.cooldownTimestamps.clear();
    this.secondaryPlayerLinks.clear();
    this.activeCache.invalidateAll();
    this.secondaryCache.invalidateAll();
    this.cache.invalidateAll();
  }

  public void activatePendingAbility(Player player) {
    this.applyAbilityEffects(player.getUniqueId(), this.activeCache, 0L);
    this.applyAbilityEffects(player.getUniqueId(), this.secondaryCache, 0L);
    this.expiryTimes.remove(player.getUniqueId());
    this.lastUpdateTimes.remove(player.getUniqueId());
    this.flagsByPlayer.remove(player.getUniqueId());
    int configuredValue = ConfigValueCache.getInt(this.plugin, "speedT2.speedStormMaxCharges", 3);
    this.playerCounters.put(player.getUniqueId(), configuredValue);
    this.lastUseTimes.put(player.getUniqueId(), System.currentTimeMillis());
  }

  static Set<UUID> createTrackingSet(UUID playerId) {
    return new HashSet<>();
  }

  static void configureBlurNpc(
      NPC npc, ItemStack item, ItemStack heldItem, ItemStack targetItem, ItemStack candidateItem, ItemStack secondaryItem) {
    if (npc.getEntity() instanceof Player player) {
      if (item != null && item.getType() != Material.AIR) {
        player.getInventory().setHelmet(item.clone());
      }

      if (heldItem != null && heldItem.getType() != Material.AIR) {
        player.getInventory().setChestplate(heldItem.clone());
      }

      if (targetItem != null && targetItem.getType() != Material.AIR) {
        player.getInventory().setLeggings(targetItem.clone());
      }

      if (candidateItem != null && candidateItem.getType() != Material.AIR) {
        player.getInventory().setBoots(candidateItem.clone());
      }

      if (secondaryItem != null && secondaryItem.getType() != Material.AIR) {
        player.getInventory().setItemInMainHand(secondaryItem.clone());
      } else {
        player.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
      }

      player.updateInventory();
    }
  }

  void updateActionBar() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (!this.plugin.isGemsDisabled()
         
          && !this.plugin.canUseAbility(player)) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if ((isActiveForPlayer(mainHandItem) || isActiveForPlayer(offHandItem))
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
        } else if (isActiveForPlayer(mainHandItem) || isActiveForPlayer(offHandItem)) {
          this.sendAbilityFeedback(player);
        }
      }
    }
  }

  static {
    SPEED_THUNDER_T2_ID = "speed_thunder_t2";
    SPEED_BLUR_T2_ID = "speed_blur_t2";
  }
}
