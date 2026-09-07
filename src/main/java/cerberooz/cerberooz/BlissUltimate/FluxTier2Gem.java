package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import static cerberooz.cerberooz.BlissUltimate.FluxPowerLevel.*;
import static cerberooz.cerberooz.BlissUltimate.FluxPowerLevel.OVERLOAD;

public class FluxTier2Gem implements Listener {
  static Bliss plugin;
  TrustCommand trustCommand;
  static final String FLUX_BEAM_GROUND_T2_ID;
  static final String FLUX_STATIC_T2_ID;
  static final String FLUX_KINETIC_T2_ID;
  static final String FLUX_T2_ID;
  public static final Map<UUID, Boolean> resolvedFlagsByPlayer;
  static final Map<UUID, Double> trackedStateByKey;
  static final Map<UUID, Double> secondaryValuesByPlayer;
  static final Map<UUID, Double> nextValuesByPlayer;
  final Map<UUID, Boolean> activeStateByKey = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> activeFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> pendingFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Integer> chargeLevels = new ConcurrentHashMap<>();
  final Map<UUID, Integer> animationSteps = new ConcurrentHashMap<>();
  final Map<UUID, Integer> abilityStages = new ConcurrentHashMap<>();
  final Map<UUID, Integer> remainingTicks = new ConcurrentHashMap<>();
  final Map<UUID, Long> cooldownTimestamps = new ConcurrentHashMap<>();
  final Map<UUID, Double> trackedValuesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Deque<FluxTier2DamageSample>> targetDamageSamplesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> cachedFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> primaryFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> currentFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Integer> playerCounters = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastUpdateTimes = new ConcurrentHashMap<>();
  final Map<UUID, BukkitRunnable> sourceTasksByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, BukkitTask> scheduledTasks = new ConcurrentHashMap<>();
  final Map<UUID, Map<Integer, ItemStack>> pendingItemsBySlotByPlayer = new ConcurrentHashMap<>();
  final Map<Entity, Boolean> cachedFlagsByEntity = new ConcurrentHashMap<>();
  final Map<UUID, Set<String>> previousTagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Long> expiryTimes = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastActivationTimes = new ConcurrentHashMap<>();
  final Map<UUID, Inventory> inventoriesByPlayer = new ConcurrentHashMap<>();
  static final String FLUX_ID;
  AstraTier2Gem astraTier2Gem;
  static final Map<UUID, Boolean> storedFlagsByPlayer;
  static Player player;
  static final Set<UUID> playerIds;
  static final String CROSSBOW_STUN_ARROW_ID;
  static final int cooldownTicks;
  static final int maxCount;
  final Map<UUID, BukkitTask> cooldownTasks = new ConcurrentHashMap<>();

  static int getConfiguredInt(String text, int count) {
    return ConfigValueCache.getInt(plugin, "fluxT2." + text, count);
  }

  static double getConfiguredDouble(String text, double value) {
    return ConfigValueCache.getDouble(plugin, "fluxT2." + text, value);
  }

  long getAbilityLongValue(Player player, int count) {
    return player.getInventory().contains(Material.DRAGON_EGG) ? Math.max(1L, count / 2L) : count;
  }

  public FluxTier2Gem(Bliss bliss) {
    plugin = bliss;
    this.startBackgroundTasks();

    for (Player player : Bukkit.getOnlinePlayers()) {
      this.activateAbility(player);
    }
  }

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
    return this.astraTier2Gem != null ? this.astraTier2Gem.getAbilityLong(playerId) : 0L;
  }

  boolean isTrustedPlayer(Player player, Entity entity) {
    if (this.trustCommand == null) {
      return false;
    } else {
      return (entity instanceof Player) && this.trustCommand.isAbilityActive(player.getUniqueId(), entity.getUniqueId());
    }
  }

  boolean isAbilityAllowed(UUID playerId) {
    Long storedTimestamp = this.lastActivationTimes.get(playerId);
    return storedTimestamp != null && System.currentTimeMillis() < storedTimestamp;
  }

  boolean isAbilityBlocked(UUID playerId) {
    return CooldownService.isOnCooldown(playerId, "flux_beam_ground_t2");
  }

  static void updateAbilityState(UUID playerId, String text) {
    CooldownService.setCooldown(playerId, text, 0L);
  }

  void activateAbility(Player player) {
    UUID playerId = player.getUniqueId();
    trackedStateByKey.putIfAbsent(playerId, 0.0);
    secondaryValuesByPlayer.putIfAbsent(playerId, 0.0);
    nextValuesByPlayer.putIfAbsent(playerId, 0.0);
    this.chargeLevels.put(playerId, 0);
    BukkitTask task = this.scheduledTasks.remove(playerId);
    if (task != null && !task.isCancelled()) {
      task.cancel();
    }

    BukkitTask scheduledTask =
        new BukkitRunnable() {
          @Override
          public void run() {
            if (!player.isOnline()) {
              FluxTier2Gem.this.scheduledTasks.remove(playerId);
              this.cancel();
            } else {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if (FluxTier2Gem.isProtectedTarget(mainHandItem) || FluxTier2Gem.isProtectedTarget(offHandItem)) {
                FluxTier2Gem.this.processAbilityState(player);
              }
            }
          }
        }
            .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
    this.scheduledTasks.put(playerId, scheduledTask);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    this.updatePrimaryAbilityState(playerId);
    this.updateTargetAbilityState(playerId);
    this.targetDamageSamplesByPlayer.remove(playerId);
    this.activateAbility(player);
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              FluxTier2Gem.this.updateActiveAbilityState(playerId);
              ActiveAbilityStore.settleExpired(playerId, "flux_beam_ground_t2");
              ActiveAbilityStore.settleExpired(playerId, "flux_static_t2");
              ActiveAbilityStore.settleExpired(playerId, "flux_kinetic_t2");
              if (FluxTier2Gem.this.canUseAbility(player)
                  || player.getInventory().getItemInOffHand().getType() == Material.PRISMARINE_SHARD) {
                if (FluxTier2Gem.plugin.isGemsDisabled()
                   
                    || FluxTier2Gem.plugin.canUseAbility(player)) {
                  continue;
                }

                FluxTier2Gem.this.spawnAbilityParticles(player);
              }

              if (!FluxTier2Gem.plugin.isGemsDisabled()
                 
                  && !FluxTier2Gem.plugin.canUseAbility(player)) {
                FluxTier2Gem.this.applyAbilityEffects(player);
                if (FluxTier2Gem.this.canUseAbility(player)) {
                  if (FluxTier2Gem.this.isAbilityActive(playerId)) {
                    long timestamp = FluxTier2Gem.this.getAbilityLong(playerId) / 1000L;
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
                    FluxTier2Gem.this.completeAbilityAction(player);
                  }
                }
              }
            }
          }
        }, plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            FluxTier2Gem.this.cachedFlagsByPlayer.clear();
          }
        }, plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            long now = System.currentTimeMillis();
            Iterator<Entry<UUID,Long>> iterator = FluxTier2Gem.this.cooldownTimestamps.entrySet().iterator();

            while (iterator.hasNext()) {
              Entry<UUID, Long> entry = iterator.next();
              if (now - entry.getValue() > 5000L) {
                FluxTier2Gem.this.remainingTicks.put(entry.getKey(), 0);
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                  player.removePotionEffect(PotionEffectType.HASTE);
                }

                iterator.remove();
              }
            }
          }
        }, plugin, SharedScheduler.staggeredInitialDelay(100L), 100L);
  }

  public void shutdown() {
    for (BukkitRunnable task : new ArrayList<>(this.sourceTasksByPlayer.values())) {
      task.cancel();
    }
    for (BukkitTask task : new ArrayList<>(this.scheduledTasks.values())) {
      task.cancel();
    }
    for (BukkitTask task : new ArrayList<>(this.cooldownTasks.values())) {
      task.cancel();
    }

    for (Map.Entry<UUID, Inventory> entry : this.inventoriesByPlayer.entrySet()) {
      Player player = Bukkit.getPlayer(entry.getKey());
      if (player != null && player.getOpenInventory().getTopInventory() == entry.getValue()) {
        player.closeInventory();
      }
    }

    Set<UUID> movementPlayers = new HashSet<>(this.previousTagsByPlayer.keySet());
    movementPlayers.addAll(this.animationSteps.keySet());
    for (UUID playerId : movementPlayers) {
      Player player = Bukkit.getPlayer(playerId);
      if (player != null) {
        this.activateActiveAbility(player);
      }
    }

    Set<UUID> hastePlayers = new HashSet<>(this.cooldownTimestamps.keySet());
    hastePlayers.addAll(this.primaryFlagsByPlayer.keySet());
    for (UUID playerId : hastePlayers) {
      Player player = Bukkit.getPlayer(playerId);
      if (player != null) {
        player.removePotionEffect(PotionEffectType.HASTE);
      }
    }

    this.activeStateByKey.clear();
    this.activeFlagsByPlayer.clear();
    this.pendingFlagsByPlayer.clear();
    this.chargeLevels.clear();
    this.animationSteps.clear();
    this.abilityStages.clear();
    this.remainingTicks.clear();
    this.cooldownTimestamps.clear();
    this.trackedValuesByPlayer.clear();
    this.targetDamageSamplesByPlayer.clear();
    this.cachedFlagsByPlayer.clear();
    this.primaryFlagsByPlayer.clear();
    this.currentFlagsByPlayer.clear();
    this.playerCounters.clear();
    this.lastUseTimes.clear();
    this.lastUpdateTimes.clear();
    this.sourceTasksByPlayer.clear();
    this.scheduledTasks.clear();
    this.pendingItemsBySlotByPlayer.clear();
    this.cachedFlagsByEntity.clear();
    this.previousTagsByPlayer.clear();
    this.expiryTimes.clear();
    this.lastActivationTimes.clear();
    this.inventoriesByPlayer.clear();
    this.cooldownTasks.clear();

    resolvedFlagsByPlayer.clear();
    trackedStateByKey.clear();
    secondaryValuesByPlayer.clear();
    nextValuesByPlayer.clear();
    storedFlagsByPlayer.clear();
    playerIds.clear();
    player = null;
    FluxAbilityEffects.shutdown();
    this.trustCommand = null;
    this.astraTier2Gem = null;
    plugin = null;
  }

  void spawnAbilityParticles(Player player) {
    if (!plugin.isGemsDisabled()) {
      if (true) {
        if (!plugin.canUseAbility(player)) {
          UUID playerId = player.getUniqueId();
          if (this.activeStateByKey.getOrDefault(playerId, false)) {
            if (!isProtectedTarget(player.getInventory().getItemInMainHand())
                && !isProtectedTarget(player.getInventory().getItemInOffHand())) {
              this.activeStateByKey.put(playerId, false);
              player.sendMessage(ChatColor.RED + "Charging paused!");
            } else {
              Location location = player.getLocation().add(0.0, 1.0, 0.0);
              player.getWorld()
                  .spawnParticle(
                      Particle.DUST,
                      location,
                      16,
                      0.4,
                      0.4,
                      0.4,
                      0.0,
                      new DustOptions(Color.fromRGB(94, 215, 255), 1.0F));
              player.getWorld().spawnParticle(Particle.SMOKE, location, 4, 0.5, 0.5, 0.5, 0.0);
              double value = getConfiguredDouble("chargePerTick", 0.667);
              double distance = nextValuesByPlayer.getOrDefault(playerId, 0.0);
              double radius = trackedStateByKey.getOrDefault(playerId, 0.0);
              double configuredValue = ConfigValueCache.getDouble(plugin, "fluxT2.beamMaxCharge", 200.0);
              boolean enabled = configuredValue > 100.0;
              if (distance <= 0.0) {
                this.activeStateByKey.put(playerId, false);
                secondaryValuesByPlayer.put(playerId, 0.0);
                nextValuesByPlayer.put(playerId, 0.0);
                player.sendMessage(
                    ChatColor.of("#5ED7FF")
                        + "🔮 §bYou ran out of watts, your gem is charged at "
                        + String.format("%.2f%%", radius));
              } else if (!enabled && radius >= 100.0) {
                trackedStateByKey.put(playerId, 100.0);
                this.activeStateByKey.put(playerId, false);
                this.activeFlagsByPlayer.put(playerId, false);
                this.pendingFlagsByPlayer.put(playerId, false);
                this.chargeLevels.put(playerId, 0);
                player.sendMessage(ChatColor.of("#5ED7FF") + "Maximum charge reached!");
                player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0F, 1.2F);
              } else if (enabled && radius >= 100.0) {
                this.activeStateByKey.put(playerId, false);
                if (!this.pendingFlagsByPlayer.getOrDefault(playerId, false)) {
                  this.activeFlagsByPlayer.put(playerId, true);
                  player.sendMessage(ChatColor.of("#5ED7FF") + "Started Overcharging.");
                }
              } else {
                double angle = 100.0;
                if (radius >= angle) {
                  trackedStateByKey.put(playerId, angle);
                  this.activeStateByKey.put(playerId, false);
                  player.sendMessage(
                      ChatColor.of("#5ED7FF")
                          + "Your gem is charged to 100%. Start charging again to overcharge.");
                  player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.2F);
                } else {
                  double progress = Math.min(value, distance);
                  progress = Math.min(progress, angle - radius);
                  double scale = radius + progress;
                  double amount = distance - progress;
                  trackedStateByKey.put(playerId, scale);
                  nextValuesByPlayer.put(playerId, amount);
                  this.chargeLevels.put(playerId, 0);
                  if (amount <= 0.0) {
                    secondaryValuesByPlayer.put(playerId, 0.0);
                    nextValuesByPlayer.put(playerId, 0.0);
                  } else {
                    int count = getConfiguredInt("maxWatts", 2000000);
                    secondaryValuesByPlayer.put(playerId, amount / 100.0 * count);
                  }

                  if (scale >= angle - 1.0E-4) {
                    trackedStateByKey.put(playerId, angle);
                    this.activeStateByKey.put(playerId, false);
                    player.sendMessage(
                        ChatColor.of("#5ED7FF")
                            + "Your gem is charged to 100%. Start charging again to overcharge.");
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
  public void onEntityShootBow(EntityShootBowEvent event) {
    if (event.getProjectile() instanceof AbstractArrow abstractArrow) {
      if (event.getBow() != null && event.getBow().getType() == Material.CROSSBOW) {
        if (event.getEntity() instanceof Player player) {
          if (isProtectedTarget(player.getInventory().getItemInMainHand())
              || isProtectedTarget(player.getInventory().getItemInOffHand())) {
            if (java.util.concurrent.ThreadLocalRandom.current().nextInt(100) <= cooldownTicks) {
              abstractArrow.setMetadata(
                  "crossbow_stun_arrow", new FixedMetadataValue(Bliss.getInstance(), true));
              Location location = abstractArrow.getLocation();
              World world = location.getWorld();
              if (world != null) {
                world.spawnParticle(
                    Particle.DUST, location, 12, 0.15, 0.15, 0.15, new DustOptions(Color.AQUA, 1.2F));
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onProjectileHit(ProjectileHitEvent event) {
    Projectile projectile = event.getEntity();
    Entity entity = event.getHitEntity();
    if (projectile instanceof AbstractArrow abstractArrow) {
      if (abstractArrow.hasMetadata("crossbow_stun_arrow")) {
        if (entity instanceof LivingEntity livingEntity) {
          Location location = livingEntity.getLocation();
          World world = location.getWorld();
          if (world != null) {
            world.strikeLightningEffect(location);
            world.spawnParticle(
                Particle.DUST,
                livingEntity.getLocation().add(0.0, 1.0, 0.0),
                30,
                0.4,
                0.6,
                0.4,
                new DustOptions(Color.AQUA, 1.4F));
            if (livingEntity instanceof Player player) {
              UUID playerId = player.getUniqueId();
              long now = System.currentTimeMillis() + maxCount * 1000L;
              this.lastUseTimes.put(playerId, now);
              Bukkit.getScheduler()
                  .runTaskLater(plugin, () -> this.expireProjectileCooldown(playerId), maxCount * 20L + 2L);
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  public void onPlayerMove(PlayerMoveEvent event) {
    Location location = event.getFrom();
    Location targetLocation = event.getTo();
    if (targetLocation == null) {
      return;
    }
    if (location.getWorld().equals(targetLocation.getWorld())
        && location.getX() == targetLocation.getX()
        && location.getY() == targetLocation.getY()
        && location.getZ() == targetLocation.getZ()) {
      return;
    }

    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    boolean enabled = this.shouldApplyEffect(playerId);
    boolean active = this.canAffectTarget(playerId);
    if (!enabled && !active) {
      if (!this.canUseAbility(player) && !this.isMatchingState(player)) {
        this.activateActiveAbility(player);
      } else {
        this.sendSourceAbilityFeedback(player, location, targetLocation);
      }
    } else {
      Location origin = location.clone();
      origin.setYaw(targetLocation.getYaw());
      origin.setPitch(targetLocation.getPitch());
      event.setTo(origin);
    }
  }

  public void refreshAbilityState(Player player, double value) {
    UUID playerId = player.getUniqueId();
    double distance = Math.max(0.0, Math.min(value, 200.0));
    trackedStateByKey.put(playerId, distance);
    this.activeStateByKey.put(playerId, false);
    this.activeFlagsByPlayer.put(playerId, false);
    this.pendingFlagsByPlayer.put(playerId, false);
  }

  boolean isGemItem(ItemStack item) {
    if (item == null || item.getType() != Material.AMETHYST_SHARD) {
      return false;
    }

    if (!item.hasItemMeta()) {
      return false;
    }

    ItemMeta itemMeta = item.getItemMeta();
    if (!itemMeta.hasCustomModelData()) {
      return false;
    }

    int count = itemMeta.getCustomModelData();
    return count == 97 || count == 117 || count == 137 || count == 157 || count == 177;
  }

  void applyAbilityEffects(Player player) {
    UUID playerId = player.getUniqueId();
    double configuredValue = ConfigValueCache.getDouble(plugin, "fluxT2.beamMaxCharge", 200.0);
    boolean enabled = configuredValue > 100.0;
    if (!enabled) {
      this.activeFlagsByPlayer.put(playerId, false);
      this.pendingFlagsByPlayer.put(playerId, false);
      this.chargeLevels.put(playerId, 0);
      double value = trackedStateByKey.getOrDefault(playerId, 0.0);
      if (value > 100.0) {
        trackedStateByKey.put(playerId, 100.0);
      }
    } else {
      if (this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
        this.activeFlagsByPlayer.put(playerId, false);
        this.chargeLevels.put(playerId, 0);
        player.sendMessage(
            ChatColor.translateAlternateColorCodes(
                '&',
                "&cᴡᴀʀɴɪɴɢ: ᴅᴏ ɴᴏᴛ"
                    + " ʀᴇʟᴇᴀsᴇ ʙᴇᴀᴍ"
                    + " ᴜɴᴛɪʟ 200%"));
        player.getWorld()
            .spawnParticle(
                Particle.DUST,
                player.getLocation().add(0.0, 1.0, 0.0),
                16,
                0.4,
                0.4,
                0.4,
                0.0,
                new DustOptions(Color.fromRGB(94, 215, 255), 1.0F));
        this.playAbilityEffects(player);
      }

      if (this.pendingFlagsByPlayer.getOrDefault(playerId, false)) {
        double distance = trackedStateByKey.getOrDefault(playerId, 0.0);
        if (distance < 199.0) {
          trackedStateByKey.put(playerId, distance + getConfiguredDouble("chargePerTick", 0.667));
          Location location = player.getLocation().add(0.0, 1.0, 0.0);
          player.getWorld()
              .spawnParticle(
                  Particle.DUST,
                  location,
                  16,
                  0.4,
                  0.4,
                  0.4,
                  0.0,
                  new DustOptions(Color.fromRGB(94, 215, 255), 1.0F));
          player.getWorld().spawnParticle(Particle.SMOKE, location, 4, 0.5, 0.5, 0.5, 0.0);
          if (distance > 101.0 && distance < 121.0) {
            player.damage(2.0);
          } else if (distance > 121.0 && distance < 140.0) {
            player.damage(4.0);
          } else if (distance > 141.0 && distance < 161.0) {
            player.damage(6.0);
          } else if (distance > 161.0 && distance < 181.0) {
            player.damage(8.0);
          } else if (distance > 181.0 && distance < 201.0) {
            player.damage(10.0);
          }
        } else {
          Location targetLocation = player.getLocation().add(0.0, 1.0, 0.0);
          player.getWorld()
              .spawnParticle(
                  Particle.DUST,
                  targetLocation,
                  15,
                  0.4,
                  0.4,
                  0.4,
                  0.0,
                  new DustOptions(Color.fromRGB(94, 215, 255), 0.8F));
          player.getWorld().spawnParticle(Particle.SMOKE, targetLocation, 4, 0.5, 0.5, 0.5, 0.0);
          player.damage(10.0);
          trackedStateByKey.put(playerId, 200.0);
          secondaryValuesByPlayer.put(playerId, 0.0);
          nextValuesByPlayer.put(playerId, 0.0);
        }
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    BukkitTask task = this.cooldownTasks.remove(playerId);
    if (task != null) {
      task.cancel();
    }

    BukkitTask scheduledTask = this.scheduledTasks.remove(playerId);
    if (scheduledTask != null) {
      scheduledTask.cancel();
    }

    this.updatePrimaryAbilityState(playerId);
    this.updateTargetAbilityState(playerId);
  }

  void playAbilityEffects(Player player) {
    UUID playerId = player.getUniqueId();
    BukkitTask task = this.cooldownTasks.remove(playerId);
    if (task != null && !task.isCancelled()) {
      task.cancel();
    }

    BukkitTask scheduledTask =
        new BukkitRunnable() {
          int durationTicks = 5;

          @Override
          public void run() {
            double configuredValue = ConfigValueCache.getDouble(FluxTier2Gem.plugin, "fluxT2.beamMaxCharge", 200.0);
            if (configuredValue <= 100.0) {
              FluxTier2Gem.this.pendingFlagsByPlayer.put(playerId, false);
              FluxTier2Gem.this.activeFlagsByPlayer.put(playerId, false);
              FluxTier2Gem.this.cooldownTasks.remove(playerId);
              this.cancel();
            } else if (this.durationTicks <= 0) {
              FluxTier2Gem.this.pendingFlagsByPlayer.put(playerId, true);
              FluxTier2Gem.this.cooldownTasks.remove(playerId);
              this.cancel();
            } else {
              player
              .getWorld()
              .spawnParticle(
                  Particle.DUST,
                  player.getLocation().add(0.0, 1.0, 0.0),
                  16,
                  0.4,
                  0.4,
                  0.4,
                  0.0,
                  new DustOptions(Color.fromRGB(94, 215, 255), 1.0F));
              String bold = ChatColor.RED.toString() + ChatColor.BOLD + "WARNING";
              String gold = ChatColor.GOLD.toString();
              int count = this.durationTicks;
              player.sendTitle(bold, gold + "Overcharging begins in... " + count, 0, 20, 10);
              this.durationTicks--;
            }
          }
        }
            .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
    this.cooldownTasks.put(playerId, scheduledTask);
  }

  public void spawnAbilityEffects(Player player) {
    Inventory inventory = Bukkit.createInventory(null, 27, FLUX_ID);
    ItemStack item = new ItemStack(Material.BOOK);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setDisplayName(ChatColor.of("#befff7").toString() + ChatColor.BOLD + "How to Charge");
    ArrayList<String> entries = new ArrayList<>();
    entries.add(ChatColor.GRAY + "Place charging items in the slots");
    entries.add(ChatColor.GRAY + "to add energy to your Flux Gem");
    entries.add("");
    entries.add(ChatColor.YELLOW + "Charging Values:");
    String textColor = ChatColor.WHITE.toString();
    String green = ChatColor.GREEN.toString();
    String text = String.format("%,d", getConfiguredInt("copperIngotWatts", 1928));
    entries.add(textColor + "Copper Ingot: " + green + text + " watts");
    textColor = ChatColor.WHITE.toString();
    green = ChatColor.GREEN.toString();
    String displayText = String.format("%,d", getConfiguredInt("copperBlockWatts", 17352));
    entries.add(textColor + "Copper Block: " + green + displayText + " watts");
    textColor = ChatColor.WHITE.toString();
    green = ChatColor.GREEN.toString();
    String name = String.format("%,d", getConfiguredInt("ironIngotWatts", 106));
    entries.add(textColor + "Iron Ingot: " + green + name + " watts");
    textColor = ChatColor.WHITE.toString();
    green = ChatColor.GREEN.toString();
    String key = String.format("%,d", getConfiguredInt("ironBlockWatts", 1067));
    entries.add(textColor + "Iron Block: " + green + key + " watts");
    textColor = ChatColor.WHITE.toString();
    green = ChatColor.GREEN.toString();
    String statusText = String.format("%,d", getConfiguredInt("diamondWatts", 5202));
    entries.add(textColor + "Diamond: " + green + statusText + " watts");
    textColor = ChatColor.WHITE.toString();
    green = ChatColor.GREEN.toString();
    String prefix = String.format("%,d", getConfiguredInt("diamondBlockWatts", 46819));
    entries.add(textColor + "Diamond Block: " + green + prefix + " watts");
    textColor = ChatColor.WHITE.toString();
    green = ChatColor.GREEN.toString();
    String normalizedText = String.format("%,d", getConfiguredInt("netheriteIngotWatts", 46819));
    entries.add(textColor + "Netherite Ingot: " + green + normalizedText + " watts");
    textColor = ChatColor.WHITE.toString();
    green = ChatColor.GREEN.toString();
    String title = String.format("%,d", getConfiguredInt("netheriteBlockWatts", 421378));
    entries.add(textColor + "Netherite Block: " + green + title + " watts");
    textColor = ChatColor.WHITE.toString();
    green = ChatColor.GREEN.toString();
    String description = String.format("%,d", getConfiguredInt("witherSkullWatts", 112047));
    entries.add(textColor + "Wither Skull: " + green + description + " watts");
    entries.add("");
    textColor = ChatColor.RED.toString();
    green = ChatColor.GOLD.toString();
    String feedbackText = String.format("%,d", getConfiguredInt("maxWatts", 2000000));
    entries.add(textColor + "Max Capacity: " + green + feedbackText + " watts");
    itemMeta.setLore(entries);
    item.setItemMeta(itemMeta);
    inventory.setItem(13, item);
    this.cleanupAbilityState(player, inventory);
    ItemStack heldItem = new ItemStack(Material.LEVER);
    ItemMeta meta = heldItem.getItemMeta();
    boolean charging = this.activeStateByKey.getOrDefault(player.getUniqueId(), false);
    meta.setDisplayName(
        charging
            ? ChatColor.RED.toString() + ChatColor.BOLD + "Stop Charging"
            : ChatColor.GREEN.toString() + ChatColor.BOLD + "Start Charging");
    heldItem.setItemMeta(meta);
    inventory.setItem(22, heldItem);
    ItemStack targetItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta updatedMeta = targetItem.getItemMeta();
    updatedMeta.setDisplayName(" ");
    targetItem.setItemMeta(updatedMeta);

    for (int count = 0; count < 27; count++) {
      if (count != 10
          && count != 11
          && count != 12
          && count != 13
          && count != 14
          && count != 15
          && count != 16
          && count != 22) {
        inventory.setItem(count, targetItem);
      }
    }

    this.inventoriesByPlayer.put(player.getUniqueId(), inventory);
    player.openInventory(inventory);
  }

  void cleanupAbilityState(Player player, Inventory inventory) {
    UUID playerId = player.getUniqueId();
    double value = secondaryValuesByPlayer.getOrDefault(playerId, 0.0);
    double distance = nextValuesByPlayer.getOrDefault(playerId, 0.0);
    double radius = trackedStateByKey.getOrDefault(playerId, 0.0);
    int count = getConfiguredInt("maxWatts", 2000000);
    ItemStack item = new ItemStack(Material.NETHER_STAR);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "⚡ Current Status");
    ArrayList<String> entries = new ArrayList<>();
    entries.add("");
    String textColor = ChatColor.WHITE.toString();
    String accentColor = ChatColor.of("#befff7").toString();
    String text = String.format("%.0f", value);
    String message = String.format("%,d", count);
    entries.add(textColor + "Total Watts: " + accentColor + text + " / " + message);
    textColor = ChatColor.WHITE.toString();
    accentColor = ChatColor.GREEN.toString();
    String name = String.format("%.2f%%", distance);
    entries.add(textColor + "Charge: " + accentColor + name);
    textColor = ChatColor.WHITE.toString();
    accentColor = ChatColor.YELLOW.toString();
    String key = String.format("%.2f%%", radius);
    entries.add(textColor + "Kinetic Energy: " + accentColor + key);
    entries.add("");
    entries.add(
        this.activeStateByKey.getOrDefault(playerId, false)
            ? ChatColor.GREEN + "Currently Charging"
            : ChatColor.RED + "Not Charging");
    itemMeta.setLore(entries);
    item.setItemMeta(itemMeta);
    inventory.setItem(4, item);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
      if (event.getView().getTitle().equals(FLUX_ID)) {
        int count = event.getRawSlot();
        if (count == 22) {
          event.setCancelled(true);
          this.sendAbilityFeedback(player);
          this.spawnAbilityEffects(player);
        } else if (count != 4
            && count != 13
            && (count < 0
                || count >= 27
                || count == 10
                || count == 11
                || count == 12
                || count == 14
                || count == 15
                || count == 16)) {
          if (count >= 10 && count <= 16 && count != 13) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                this.scheduleAbilityUpdate(player, event.getInventory());

              }, 1L);
          }
        } else {
          event.setCancelled(true);
        }
      }
    }
  }

  void sendAbilityFeedback(Player player) {
    UUID playerId = player.getUniqueId();
    boolean enabled = this.activeStateByKey.getOrDefault(playerId, false);
    if (!enabled) {
      if (nextValuesByPlayer.getOrDefault(playerId, 0.0) > 0.0) {
        this.activeStateByKey.put(playerId, true);
        this.activeFlagsByPlayer.put(playerId, false);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aCharging started!"));
      } else {
        player.sendMessage(
            ChatColor.translateAlternateColorCodes(
                '&', "&cYou don't have enough energy to charge!"));
      }
    } else {
      this.activeStateByKey.put(playerId, false);
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cCharging paused!"));
    }
  }

  void scheduleAbilityUpdate(Player player, Inventory inventory) {
    UUID playerId = player.getUniqueId();
    double value = secondaryValuesByPlayer.getOrDefault(playerId, 0.0);
    int count = getConfiguredInt("maxWatts", 2000000);

    for (int index = 10; index <= 16; index++) {
      if (index != 13) {
        ItemStack item = inventory.getItem(index);
        if (item != null && item.getType() != Material.AIR) {
          int material = this.getAbilityIntValue(item.getType());
          if (material > 0) {
            int remaining = item.getAmount();
            double distance = (double) material * remaining;
            if (value + distance > count) {
              double radius = count - value;
              int step = (int) Math.ceil(radius / material);
              distance = (double) step * material;
              if (remaining > step) {
                item.setAmount(remaining - step);
              } else {
                inventory.setItem(index, null);
              }

              value = count;
            } else {
              value += distance;
              inventory.setItem(index, null);
            }

            secondaryValuesByPlayer.put(playerId, value);
            double angle = value / count * 100.0;
            nextValuesByPlayer.put(playerId, angle);
            player.sendMessage(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    ChatColor.of("#5ED7FF")
                        + "🔮 &bYou now have "
                        + String.format("%.0f", value)
                        + " watts, up to "
                        + String.format("%.2f%%", angle)
                        + " charge."));
          }
        }
      }
    }

    this.cleanupAbilityState(player, inventory);
  }

  int getAbilityIntValue(Material material) {
    return switch (material) {
      case COPPER_INGOT -> getConfiguredInt("copperIngotWatts", 1928);
      case COPPER_BLOCK -> getConfiguredInt("copperBlockWatts", 17352);
      case IRON_INGOT -> getConfiguredInt("ironIngotWatts", 106);
      case IRON_BLOCK -> getConfiguredInt("ironBlockWatts", 1067);
      case DIAMOND -> getConfiguredInt("diamondWatts", 5202);
      case DIAMOND_BLOCK -> getConfiguredInt("diamondBlockWatts", 46819);
      case NETHERITE_INGOT -> getConfiguredInt("netheriteIngotWatts", 46819);
      case NETHERITE_BLOCK -> getConfiguredInt("netheriteBlockWatts", 421378);
      case WITHER_SKELETON_SKULL -> getConfiguredInt("witherSkullWatts", 112047);
      default -> 0;
    };
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getView().getTitle().equals(FLUX_ID)) {
      Player player = (Player) event.getPlayer();

      for (int count = 10; count <= 16; count++) {
        if (count != 13) {
          ItemStack item = event.getInventory().getItem(count);
          if (item != null && item.getType() != Material.AIR) {
            player.getInventory().addItem(item);
          }
        }
      }

      this.inventoriesByPlayer.remove(player.getUniqueId());
    }
  }

  public ItemStack createItem(Player player) {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setUnbreakable(true);
    itemMeta.setCustomModelData(98);
    itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
    itemMeta.setDisplayName(
        ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
            + "ꜰʟᴜx "
            + ChatColor.of("#FFD773")
            + "ɢᴇᴍ ");
    ArrayList<String> lore = new ArrayList<>();
    lore.add(ChatColor.of("#befff7") + "Energy:");
    lore.add(ChatColor.of("#82EDBF") + "Pristine");
    lore.add(
        ChatColor.WHITE.toString() + ChatColor.BOLD
            + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
            + " ꜰʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
    lore.add("");
    lore.add(
        ChatColor.of("#befff7")
            + "🔺 "
            + ChatColor.of("#FFE4AB")
            + "ᴘᴀѕѕɪᴠᴇѕ");
    lore.add(ChatColor.GRAY + "- Flow State");
    lore.add(ChatColor.GRAY + "- Shocking Chance");
    lore.add(ChatColor.GRAY + "- Conduction");
    lore.add(ChatColor.GRAY + "- Charged");
    lore.add("");
    String accentColor = ChatColor.of("#befff7").toString();
    String messageColor = ChatColor.of("#82F3FF").toString();
    String bold = ChatColor.BOLD.toString();
    lore.add(
        accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
    lore.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
    lore.add("");
    accentColor = ChatColor.of("#befff7").toString();
    messageColor = ChatColor.of("#B8FFFB").toString();
    String displayColor = ChatColor.BOLD.toString();
    lore.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
    lore.add(
        ChatColor.GRAY
            + "- ☄ "
            + ChatColor.of("#03EAFF")
            + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
            + ChatColor.DARK_RED
            + "🧑🏻");
    lore.add(
        ChatColor.GRAY
            + "- ☄ "
            + ChatColor.of("#03EAFF")
            + "ɢʀᴏᴜɴᴅ "
            + ChatColor.DARK_RED
            + "🤼");
    lore.add("");
    lore.add(
        ChatColor.GRAY
            + "- 🌀 "
            + ChatColor.of("#03EAFF")
            + "ᴋɪɴᴇᴛɪᴄ"
            + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
            + ChatColor.GREEN
            + "🤼");
    itemMeta.setLore(lore);
    item.setItemMeta(itemMeta);
    return item;
  }

  boolean canUseAbility(Player player) {
    return isProtectedTarget(player.getInventory().getItemInMainHand())
        || isProtectedTarget(player.getInventory().getItemInOffHand());
  }

  boolean isMatchingState(Player player) {
    return this.isGemItem(player.getInventory().getItemInMainHand())
        || this.isGemItem(player.getInventory().getItemInOffHand());
  }

  boolean isValidTarget(Player player) {
    return isProtectedTarget(player.getInventory().getItemInMainHand());
  }

  public static boolean isProtectedTarget(ItemStack item) {
    if (item == null || item.getType() != Material.PRISMARINE_SHARD) {
      return false;
    }

    if (!item.hasItemMeta()) {
      return false;
    }

    ItemMeta itemMeta = item.getItemMeta();
    if (!itemMeta.hasCustomModelData()) {
      return false;
    }

    int count = itemMeta.getCustomModelData();
    return count == 178 || count == 158 || count == 138 || count == 118 || count == 98;
  }

  void completeAbilityAction(Player player) {
    UUID playerId = player.getUniqueId();
    if (!plugin.isGemsDisabled()
       
        && !plugin.canUseAbility(player)) {
      String text =
          ActiveAbilityStore.isActive(playerId, "flux_beam_ground_t2")
              ? "§cActive..."
              : (CooldownService.isOnCooldown(playerId, "flux_beam_ground_t2")
                  ? "§b"
                      + AbilityStatusFormatter.formatDisplayText(
                          playerId, "flux_beam_ground_t2", true)
                  : "§aReady!");
      String message =
          ActiveAbilityStore.isActive(playerId, "flux_static_t2")
              ? "§cActive..."
              : (CooldownService.isOnCooldown(playerId, "flux_static_t2")
                  ? "§b"
                      + AbilityStatusFormatter.formatDisplayText(
                          playerId, "flux_static_t2", true)
                  : "§b0");
      String displayText =
          ActiveAbilityStore.isActive(playerId, "flux_kinetic_t2")
              ? "§cActive..."
              : (CooldownService.isOnCooldown(playerId, "flux_kinetic_t2")
                  ? AbilityStatusFormatter.formatDisplayText(playerId, "flux_kinetic_t2", false)
                  : "§aReady!");
      double value = trackedStateByKey.getOrDefault(playerId, 0.0);
      String accentColor =
          String.format(
              "&7☄ %s "
                  + ChatColor.of("#5ED7FF")
                  + "🔮 %s "
                  + ChatColor.of("#5ED7FF")
                  + "🔮 §b%.1f%% &7🌀§b %s",
              text,
              message,
              value,
              displayText);
      ActionBarQueue.enqueue(player, ChatColor.translateAlternateColorCodes('&', accentColor));
    } else {
      String darkGray = ChatColor.DARK_GRAY.toString();
      String bold = ChatColor.BOLD.toString();
      ActionBarQueue.enqueue(
          player,
          "🔒 " + darkGray + bold + "ᴅɪꜱᴀʙʟᴇᴅ");
    }
  }

  void processAbilityState(Player player) {
    if (!plugin.isGemsDisabled()) {
      if (true) {
        if (!plugin.canUseAbility(player)) {
          player.removePotionEffect(PotionEffectType.SLOWNESS);
          player.removePotionEffect(PotionEffectType.HUNGER);
          player.removePotionEffect(PotionEffectType.WEAKNESS);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    Player player = event.getPlayer();
    if (isProtectedTarget(event.getItemDrop().getItemStack())) {
      event.setCancelled(true);
    } else {
      this.expiryTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }
  }

  @EventHandler
  public void handleAbilityAction(PlayerDropItemEvent event) {
    if (isProtectedTarget(event.getItemDrop().getItemStack())) {
      Player player = event.getPlayer();
      this.spawnAbilityEffects(player);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void resetAbilityState(PlayerDropItemEvent event) {
    this.expiryTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (!plugin.isGemsDisabled()) {
        if (true) {
          if (!plugin.canUseAbility(player)) {
            Long storedTimestamp = this.expiryTimes.get(playerId);
            if (storedTimestamp == null || System.currentTimeMillis() - storedTimestamp >= 300L) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if ((event.getAction() == Action.LEFT_CLICK_AIR
                      || event.getAction() == Action.LEFT_CLICK_BLOCK)
                  && isProtectedTarget(mainHandItem)) {
                event.setCancelled(true);
                ActiveAbilityStore.settleExpired(playerId, "flux_beam_ground_t2");
                if (!ActiveAbilityStore.isActive(playerId, "flux_beam_ground_t2")
                    && !this.isAbilityBlocked(playerId)) {
                  this.trackAbilityState(player);
                }
              } else if ((event.getAction() == Action.RIGHT_CLICK_AIR
                      || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                  && isProtectedTarget(mainHandItem)) {
                if (player.isSneaking()) {
                  if (this.isActiveForPlayer(player)) {
                    event.setCancelled(true);
                    return;
                  }
                } else {
                  ActiveAbilityStore.settleExpired(playerId, "flux_kinetic_t2");
                  if (!ActiveAbilityStore.isActive(playerId, "flux_kinetic_t2")
                      && !CooldownService.isOnCooldown(playerId, "flux_kinetic_t2")) {
                    this.sendPrimaryAbilityFeedback(player, (Entity) null);
                  }
                }
              } else if ((event.getAction() == Action.RIGHT_CLICK_AIR
                      || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                  && isProtectedTarget(offHandItem)
                  && player.isSneaking()) {
                ActiveAbilityStore.settleExpired(playerId, "flux_static_t2");
                if (!ActiveAbilityStore.isActive(playerId, "flux_static_t2")
                    && !CooldownService.isOnCooldown(playerId, "flux_static_t2")) {
                  this.applyAbilityMotion(player);
                }
              }
            }
          }
        }
      }
    }
  }

  boolean isActiveForPlayer(Player player) {
    Block block = this.resolveBlock(player, 5.0, 0.25);
    if (block == null) {
      return false;
    } else {
      Location location = this.getTargetLocation(block, player);
      if (location == null) {
        player.sendMessage(ChatColor.RED + "There is no safe space above that copper block.");
        return true;
      } else {
        World world = player.getWorld();
        Location targetLocation = player.getLocation().clone();
        world.spawnParticle(
            Particle.DUST,
            targetLocation.clone().add(0.0, 1.0, 0.0),
            20,
            0.3,
            0.5,
            0.3,
            0.0,
            new DustOptions(Color.fromRGB(94, 215, 255), 1.2F));
        player.teleport(location);
        world.spawnParticle(
            Particle.DUST,
            location.clone().add(0.0, 1.0, 0.0),
            20,
            0.3,
            0.5,
            0.3,
            0.0,
            new DustOptions(Color.fromRGB(94, 215, 255), 1.2F));
        world.spawnParticle(
            Particle.END_ROD, location.clone().add(0.0, 1.0, 0.0), 12, 0.25, 0.4, 0.25, 0.01);
        world.playSound(location, Sound.ITEM_TRIDENT_THUNDER, 1.0F, 1.35F);
        return true;
      }
    }
  }

  Block resolveBlock(Player player, double value, double distance) {
    Location location = player.getEyeLocation().clone();
    Vector direction = location.getDirection().normalize();
    double radius = 0.5;

    while (radius <= value) {
      Location targetLocation = location.clone().add(direction.clone().multiply(radius));
      Block block = targetLocation.getBlock();
      if (this.isConditionMet(block)) {
        return block;
      }

      radius += distance;
    }

    return null;
  }

  boolean isConditionMet(Block block) {
    if (block == null) {
      return false;
    }

    Material material = block.getType();
    return material.isBlock() && material.isSolid() && material.name().contains("COPPER");
  }

  Location getTargetLocation(Block block, Player player) {
    Location location = block.getLocation().add(0.5, 1.0, 0.5);
    Block targetBlock = location.getBlock();
    Block sourceBlock = location.clone().add(0.0, 1.0, 0.0).getBlock();
    if (targetBlock.isPassable() && sourceBlock.isPassable()) {
      location.setYaw(player.getLocation().getYaw());
      location.setPitch(player.getLocation().getPitch());
      return location;
    } else {
      return null;
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (!plugin.isGemsDisabled()) {
        if (true) {
          if (!plugin.canUseAbility(player)) {
            Long storedTimestamp = this.expiryTimes.get(playerId);
            if (storedTimestamp == null || System.currentTimeMillis() - storedTimestamp >= 300L) {
              if (isProtectedTarget(player.getInventory().getItemInMainHand())
                  && !ActiveAbilityStore.isActive(playerId, "flux_kinetic_t2")
                  && !CooldownService.isOnCooldown(playerId, "flux_kinetic_t2")) {
                event.setCancelled(true);
                this.sendPrimaryAbilityFeedback(player, event.getRightClicked());
              }
            }
          }
        }
      }
    }
  }

  void applyAbilityMotion(Player player) {
    UUID playerId = player.getUniqueId();
    int count = getConfiguredInt("kineticBurstCooldown", 30);
    if (player.getInventory().contains(Material.DRAGON_EGG)) {
      count /= 2;
    }

    ActiveAbilityStore.startActive(playerId, "flux_static_t2", 3L, count);
    player.sendMessage(
        ChatColor.translateAlternateColorCodes(
            '&', "&b🔮 You have activated " + ChatColor.of("#FF686F") + "Kinetic Burst"));
    Location location = player.getLocation();
    double value = getConfiguredInt("kineticBurstRadius", 5);

    for (Entity entity : location.getWorld().getNearbyEntities(location, value, value, value)) {
      if (entity != player && entity instanceof LivingEntity livingEntity && !this.isTrustedPlayer(player, entity)) {
        Vector offset = entity.getLocation().toVector().subtract(location.toVector());
        offset.setY(0);
        offset = offset.normalize();
        offset.setY(0.2);
        entity.setVelocity(offset.multiply(1.2));
        double distance = this.trackedValuesByPlayer.getOrDefault(playerId, 0.0) * 3.0;
        if (distance > 0.0) {
          livingEntity.damage(distance * livingEntity.getMaxHealth() / 100.0);
        }
      }
    }

    this.updateTargetState(location);
    this.trackedValuesByPlayer.put(playerId, 0.0);
  }

  void trackAbilityState(Player player) {
    UUID playerId = player.getUniqueId();
    ActiveAbilityStore.startActive(
        player.getUniqueId(),
        "flux_beam_ground_t2",
        0L,
        this.getAbilityLongValue(player, getConfiguredInt("beamCooldown", 90)));
    this.activeStateByKey.put(playerId, false);
    this.activeFlagsByPlayer.put(playerId, false);
    this.pendingFlagsByPlayer.put(playerId, false);
    double value = trackedStateByKey.getOrDefault(playerId, 0.0);
    trackedStateByKey.put(playerId, 0.0);
    Location location = player.getLocation().clone();
    Vector direction = location.getDirection().clone().normalize();
    FluxAbilityEffects.setPlugin(plugin);
    FluxAbilityEffects.setAbilityFlags(resolvedFlagsByPlayer);
    if (value >= 200.0) {
      resolvedFlagsByPlayer.put(playerId, true);
      FluxAbilityEffects.playAbilitySound(player);
    } else if (!(value > 100.1)) {
      Location targetLocation =
          player.getLocation()
              .clone()
              .add(0.0, 1.6, 0.0)
              .add(player.getLocation().getDirection().normalize().multiply(1.0));
      Vector offset = player.getLocation().getDirection().normalize().clone();
      FluxPowerLevel fluxPowerLevel = this.resolveFluxPowerLevel(value);
      this.playAbilitySound(player, targetLocation, offset, value, fluxPowerLevel);
    } else {
      double distance = 0.5;
      trackedStateByKey.put(playerId, 0.0);

      for (int count = 0; count < 20; count++) {
        double radius = distance + (count + 1) * 0.5;
        Bukkit.getScheduler()
            .runTaskLater(plugin, () -> this.spawnEnergyBeamEffects(player, radius), count + 1L);
      }
    }
  }

  FluxPowerLevel resolveFluxPowerLevel(double value) {
    if (value <= 0.01) {
      return FIZZLE;
    } else if (value <= 5.0) {
      return SPARK;
    } else if (value <= 11.0) {
      return LIGHT;
    } else if (value <= 20.0) {
      return MEDIUM;
    } else if (value <= 41.0) {
      return HEAVY;
    } else if (value <= 56.0) {
      return WAVE;
    } else if (value <= 76.0) {
      return LANCE;
    } else if (value <= 100.1) {
      return OVERLOAD;
    } else {
      return value < 200.0 ? SURGE : OVERCHARGE;
    }
  }

  void playAbilitySound(Player player, Location location, Vector direction, double value, FluxPowerLevel fluxPowerLevel) {
    World world = location.getWorld();
    if (world != null) {
      switch (fluxPowerLevel) {
        case FIZZLE:
          player.getWorld().playSound(location, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 20.0F, 1.0F);
          player.getWorld().playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 20.0F, 1.0F);
          player.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 20.0F, 2.0F);
          player.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 20.0F, 1.0F);
          player.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 20.0F, 1.0F);
          player.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 20.0F, 0.8F);
          this.refreshPlayerState(location, player);

          for (int count = 0; count < 6; count++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                this.updatePlayerState(location);

              }, count * 3L);
          }
          break;
        case SURGE:
          this.updateTargetState(player.getLocation());
          player.sendMessage(ChatColor.of("#5ED7FF") + "Kinetic surge released.");
          break;
        default:
          world.playSound(location, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.3F, 1.0F);
          world.playSound(location, Sound.ITEM_AXE_SCRAPE, 1.5F, 0.5F);
          new BukkitRunnable() {
            final Location anchorLocation = location;
            final double effectRadius = value;
            int durationTicks = 0;

            @Override
            public void run() {
              if (!player.isOnline()) {
                this.cancel();
              } else {
                if (this.durationTicks == 1) {
                  world.playSound(this.anchorLocation, Sound.ITEM_AXE_SCRAPE, 1.5F, 0.55F);
                } else if (this.durationTicks == 2) {
                  world.playSound(this.anchorLocation, Sound.ITEM_AXE_SCRAPE, 1.5F, 0.65F);
                } else if (this.durationTicks >= 6) {
                  world.playSound(this.anchorLocation, Sound.BLOCK_BEACON_AMBIENT, 2.0F, 1.6F);
                  world.playSound(this.anchorLocation, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0F, 1.0F);
                  world.playSound(this.anchorLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.8F, 0.75F);
                  world.playSound(this.anchorLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.6F, 0.9F);
                  FluxTier2Gem.this.finishAbilityAction(player, this.anchorLocation.clone(), direction.clone(), this.effectRadius, fluxPowerLevel);
                  this.cancel();
                  return;
                }

                this.durationTicks++;
              }
            }
          }
              .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(4L), 4L);
      }
    }
  }

  void refreshPlayerState(Location location, Player player) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      double distanceThreshold = 1.0;
      double effectRadius = 0.3;
      int maxCount = java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 61);
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= 12) {
          Location location = this.anchorLocation.clone().add(this.anchorLocation.getDirection().multiply(this.distanceThreshold));

          for (Entity entity : location.getWorld().getNearbyEntities(location, 3.0, 3.0, 3.0)) {
            if (entity != player
                && entity instanceof LivingEntity livingEntity
                && !FluxTier2Gem.this.isTrustedPlayer(player, entity)) {
              if (!FluxTier2Gem.this.hasRequiredState(livingEntity)) {
                livingEntity.damage(1.0E-6);
                livingEntity.setHealth(Math.max(0.1, livingEntity.getHealth() - 2.0));
              } else if (livingEntity.getHealth() > 2.0) {
                livingEntity.setHealth(livingEntity.getHealth() - 2.0);
              } else {
                livingEntity.setHealth(0.1);
                livingEntity.damage(500.0);
              }
            }
          }

          this.cancel();
        } else {
          Location targetLocation = this.anchorLocation.clone().add(this.anchorLocation.getDirection().multiply(this.distanceThreshold));

          for (int count = 0; count < 8; count++) {
            double value = (count * 45 + this.maxCount) * Math.PI / 180.0;
            targetLocation.getWorld()
            .spawnParticle(
                Particle.END_ROD,
                targetLocation.clone().add(new Vector(this.effectRadius * Math.cos(value), this.effectRadius * Math.sin(value), 0.0)),
                1,
                0.1,
                0.1,
                0.1,
                0.0);
          }

          this.distanceThreshold += 0.4;
          this.effectRadius += 0.22;
          this.maxCount += 5;
          this.durationTicks++;
        }
      }
    }
        .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void updatePlayerState(Location location) {
    double value = 1.0;
    double distance = 0.5;
    byte step = 20;
    DustTransition dustTransition = new DustTransition(Color.fromRGB(94, 215, 255), Color.WHITE, 1.0F);

    for (int count = 0; count < 2; count++) {
      Location targetLocation = location.clone().add(location.getDirection().multiply(value));

      for (int index = 0; index < 24; index++) {
        double radius = (index * 15 + step) * Math.PI / 180.0;
        targetLocation.getWorld()
            .spawnParticle(
                Particle.DUST_COLOR_TRANSITION,
                targetLocation.clone().add(new Vector(distance * Math.cos(radius), distance * Math.sin(radius), 0.0)),
                2,
                0.3,
                0.3,
                0.3,
                0.0,
                dustTransition);
      }

      value += 0.8;
      distance += 0.2;
      step += 5;
    }
  }

  void finishAbilityAction(Player player, Location location, Vector direction, double value, FluxPowerLevel fluxPowerLevel) {
    UUID playerId = player.getUniqueId();
    resolvedFlagsByPlayer.put(playerId, true);
    byte step;
    double distance;
    double radius;
    double angle;
    byte phase;
    double progress;
    double scale;
    boolean enabled;
    boolean active;
    switch (fluxPowerLevel) {
      case SPARK:
        step = 14;
        distance = 10.0;
        radius = 0.8;
        angle = 0.35;
        phase = 2;
        progress = 0.22;
        scale = 10.0;
        enabled = false;
        active = false;
        break;
      case LIGHT:
        step = 18;
        distance = 12.0;
        radius = 1.0;
        angle = 0.55;
        phase = 2;
        progress = 0.35;
        scale = 12.0;
        enabled = true;
        active = false;
        break;
      case MEDIUM:
        step = 22;
        distance = 14.0;
        radius = 1.2;
        angle = 0.8;
        phase = 4;
        progress = 0.55;
        scale = 14.0;
        enabled = true;
        active = false;
        break;
      case HEAVY:
        step = 26;
        distance = 16.0;
        radius = 1.35;
        angle = 1.2;
        phase = 6;
        progress = 0.85;
        scale = 16.0;
        enabled = true;
        active = true;
        break;
      case WAVE:
        step = 30;
        distance = 18.0;
        radius = 1.55;
        angle = 1.4;
        phase = 10;
        progress = 2.1;
        scale = 18.0;
        enabled = true;
        active = true;
        break;
      case LANCE:
        step = 34;
        distance = 20.0;
        radius = 1.8;
        angle = 1.45;
        phase = 8;
        progress = 1.65;
        scale = 20.0;
        enabled = true;
        active = true;
        break;
      case OVERLOAD:
        step = 50;
        distance = 24.0;
        radius = 3.2;
        angle = 2.0;
        phase = 14;
        progress = 2.8;
        scale = 24.0;
        enabled = true;
        active = true;
        break;
      default:
        step = 20;
        distance = 12.0;
        radius = 1.0;
        angle = 0.5;
        phase = 2;
        progress = 0.25;
        scale = 12.0;
        enabled = false;
        active = false;
    }

    double amount = getConfiguredDouble("beamBaseDamage", 5.0);
    double speed = amount * angle * Math.max(0.35, value / 100.0);
    double height = getConfiguredDouble("beamArmorBaseDamage", 40.0);
    double width = height * angle * Math.max(0.35, value / 100.0);
    new BukkitRunnable() {
      final int cooldownTicks = step;
      final Location anchorLocation = location;
      final double knockbackStrength = distance;
      final int maxCount = phase;
      final double effectScale = speed;
      final double damageAmount = width;
      final boolean active = enabled;
      final boolean secondaryActive = active;
      final double effectRadius = radius;
      final double verticalOffset = speed;
      final double distanceThreshold = width;
      int durationTicks = 0;
      final Set<UUID> playerIds = new HashSet<>();

      @Override
      public void run() {
        if (!player.isOnline()) {
          FluxTier2Gem.resolvedFlagsByPlayer.put(playerId, false);
          this.cancel();
        } else if (this.durationTicks < this.cooldownTicks && FluxTier2Gem.resolvedFlagsByPlayer.getOrDefault(playerId, false)) {
          if (fluxPowerLevel == OVERLOAD) {
            FluxTier2Gem.this.spawnSourceAbilityParticles(this.anchorLocation, direction, this.knockbackStrength, this.durationTicks);
          } else {
            FluxTier2Gem.this.spawnPrimaryAbilityParticles(
                this.anchorLocation, direction, this.knockbackStrength, this.durationTicks, this.maxCount, this.effectScale, this.damageAmount, this.active, this.secondaryActive);
          }

          FluxTier2Gem.this.applyAbilityDamage(player, this.anchorLocation, direction, this.knockbackStrength, this.effectRadius, this.verticalOffset, this.distanceThreshold, this.playerIds);
          this.durationTicks++;
        } else {
          FluxTier2Gem.resolvedFlagsByPlayer.put(playerId, false);
          this.cancel();
        }
      }
    }
        .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void spawnPrimaryAbilityParticles(
      Location location,
      Vector direction,
      double value,
      int count,
      int index,
      double distance,
      double radius,
      boolean enabled,
      boolean active) {
    World world = location.getWorld();
    if (world != null) {
      Vector offset = direction.clone().normalize();
      Vector velocity = new Vector(0, 1, 0);
      Vector normalizedDirection = offset.clone().crossProduct(velocity);
      if (normalizedDirection.lengthSquared() < 0.001) {
        normalizedDirection = new Vector(1, 0, 0);
      }

      normalizedDirection.normalize();
      Vector knockback = normalizedDirection.clone().crossProduct(offset).normalize();
      DustOptions dustOptions = new DustOptions(Color.fromRGB(94, 215, 255), 0.7F);
      DustOptions dust = new DustOptions(Color.fromRGB(94, 215, 255), 0.7F);
      ParticleEffects.cleanupAbilityState(
          Particle.END_ROD, location, direction, value, 0.8, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
      this.spawnTargetAbilityParticles(location, direction, 0.5);
      ParticleEffects.applyAbilityEffects(
          dustOptions, location, direction, value, 1.0, 1, 1.5, 1.8, 1.0, 0, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
      ParticleEffects.applyAbilityEffects(
          dust, location, direction, value, 1.0, 1, 1.5, 1.5, 1.0, 0, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
      ParticleEffects.resolvePreviousTask(
          Particle.END_ROD,
          location,
          direction,
          value,
          0.2,
          1.0,
          8.0,
          0.04,
          0.6,
          40,
          1,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0F,
          0.0F,
          0.0F);
      ParticleEffects.createPulseParticleAnimationTask(
          Particle.SONIC_BOOM,
          location,
          direction,
          value,
          2.0,
          2.0,
          20,
          1,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0F,
          0.0F,
          0.0F);
    }
  }

  void spawnTargetAbilityParticles(Location location, Vector direction, double value) {
    World world = location.getWorld();
    if (world != null) {
      Vector offset = direction.clone().normalize();
      Vector velocity = new Vector(0, 1, 0);
      Vector normalizedDirection = offset.clone().crossProduct(velocity);
      if (normalizedDirection.lengthSquared() < 0.001) {
        normalizedDirection = new Vector(1, 0, 0);
      }

      normalizedDirection.normalize();
      Vector knockback = normalizedDirection.clone().crossProduct(offset).normalize();
      DustOptions dustOptions = new DustOptions(Color.fromRGB(94, 215, 255), 0.9F);

      for (byte step = 0; step < 360; step += 20) {
        double angleRadians = Math.toRadians(step);
        Vector axis =
            normalizedDirection.clone()
                .multiply(Math.cos(angleRadians) * value)
                .add(knockback.clone().multiply(Math.sin(angleRadians) * value));
        world.spawnParticle(Particle.DUST, location.clone().add(axis), 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
      }
    }
  }

  void spawnSourceAbilityParticles(Location location, Vector direction, double value, int count) {
    World world = location.getWorld();
    if (world != null) {
      DustOptions dustOptions = new DustOptions(Color.fromRGB(94, 215, 255), 1.0F);
      DustOptions dust = new DustOptions(Color.fromRGB(94, 215, 255), 1.0F);
      ParticleEffects.cleanupAbilityState(
          Particle.END_ROD, location, direction, value, 0.5, 2, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
      this.spawnTargetAbilityParticles(location, direction, 1.0);
      ParticleEffects.applyAbilityEffects(
          dustOptions, location, direction, value, 2.0, 3, 3.0, 3.5, 2.0, 0, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
      ParticleEffects.applyAbilityEffects(
          dust, location, direction, value, 2.0, 3, 3.0, 3.0, 2.0, 0, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
      ParticleEffects.cleanupAbilityState(
          Particle.SONIC_BOOM, location, direction, value, 2.0, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
      ParticleEffects.resolvePreviousTask(
          Particle.END_ROD,
          location,
          direction,
          value,
          0.25,
          2.0,
          16.0,
          0.08,
          1.2,
          60,
          1,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0F,
          0.0F,
          0.0F);
    }
  }

  void applyAbilityDamage(
      Player player,
      Location location,
      Vector direction,
      double value,
      double distance,
      double radius,
      double angle,
      Set<UUID> hitPlayerIds) {
    World world = location.getWorld();
    if (world != null) {
      Vector offset = direction.clone().normalize();

      for (double progress = 0.0; progress <= value; progress += 0.55) {
        Location targetLocation = location.clone().add(offset.clone().multiply(progress));

        for (Entity entity : world.getNearbyEntities(targetLocation, distance, distance, distance)) {
          if (entity instanceof LivingEntity livingEntity
              && !entity.equals(player)
              && !this.isTrustedPlayer(player, entity)) {
            UUID playerId = entity.getUniqueId();
            if (!hitPlayerIds.contains(playerId)) {
              hitPlayerIds.add(playerId);
              if (livingEntity instanceof Player targetPlayer) {
                UUID targetId = targetPlayer.getUniqueId();
                if (!storedFlagsByPlayer.containsKey(targetId)) {
                  storedFlagsByPlayer.put(targetId, true);
                  FluxTier2Gem.player = player;
                  livingEntity.damage(radius, DamageSource.builder(DamageType.SONIC_BOOM).build());
                  this.activatePrimaryAbility(targetPlayer, (int) Math.ceil(angle));
                  Bukkit.getScheduler()
                      .runTaskLater(plugin, () -> clearBeamDamageFlag(targetId), 120L);
                }
              } else {
                livingEntity.damage(radius, player);
              }
            }
          }
        }
      }
    }
  }

  void activatePrimaryAbility(Player player, int count) {
    PlayerInventory playerInventory = player.getInventory();
    this.updateState(playerInventory, EquipmentSlot.HEAD, count);
    this.updateState(playerInventory, EquipmentSlot.CHEST, count);
    this.updateState(playerInventory, EquipmentSlot.LEGS, count);
    this.updateState(playerInventory, EquipmentSlot.FEET, count);
    player.updateInventory();
  }

  void updateState(PlayerInventory playerInventory, EquipmentSlot equipmentSlot, int count) {
    ItemStack item =
        switch (equipmentSlot) {
          case HEAD -> playerInventory.getHelmet();
          case CHEST -> playerInventory.getChestplate();
          case LEGS -> playerInventory.getLeggings();
          case FEET -> playerInventory.getBoots();
          default -> null;
        };
    if (item != null && item.getType() != Material.AIR) {
      if (item.hasItemMeta()) {
        if (item.getItemMeta() instanceof Damageable damageable) {
          short material = item.getType().getMaxDurability();
          if (material > 0) {
            int index = damageable.getDamage() + count;
            if (index >= material) {
              switch (equipmentSlot) {
                case HEAD -> playerInventory.setHelmet(null);
                case CHEST -> playerInventory.setChestplate(null);
                case LEGS -> playerInventory.setLeggings(null);
                case FEET -> playerInventory.setBoots(null);
              }
            } else {
              damageable.setDamage(index);
              item.setItemMeta(damageable);
              switch (equipmentSlot) {
                case HEAD -> playerInventory.setHelmet(item);
                case CHEST -> playerInventory.setChestplate(item);
                case LEGS -> playerInventory.setLeggings(item);
                case FEET -> playerInventory.setBoots(item);
              }
            }
          }
        }
      }
    }
  }

  void spawnActiveAbilityParticles(Location location, Player player) {
    World world = location.getWorld();
    if (world != null) {
      DustOptions dustOptions = new DustOptions(Color.fromRGB(94, 215, 255), 1.0F);

      for (int count = 0; count < 60; count++) {
        double angleRadians = Math.toRadians(count * 6);
        Vector offset =
            new Vector(Math.cos(angleRadians), Math.sin(angleRadians) * 0.35, Math.sin(angleRadians)).multiply(0.35);
        world.spawnParticle(Particle.DUST, location.clone().add(offset), 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
      }

      world.spawnParticle(Particle.END_ROD, location, 12, 0.35, 0.35, 0.35, 0.01);
    }
  }

  void sendPrimaryAbilityFeedback(Player player, Entity entity) {
    UUID playerId = player.getUniqueId();
    int count = getConfiguredInt("kineticOverdriveCooldown", 30);
    long timestamp = getConfiguredInt("kineticOverdriveDuration", 30);
    long lastUpdateTime = getConfiguredInt("kineticOverdriveDuration", 30);
    ActiveAbilityStore.startActive(playerId, "flux_kinetic_t2", 2L, this.getAbilityLongValue(player, count));
    if (entity instanceof Player targetPlayer) {
      if (this.isTrustedPlayer(player, targetPlayer)) {
        player.sendMessage(ChatColor.RED + "You cannot use Kinetic Overdrive on trusted players!");
        updateAbilityState(playerId, "flux_kinetic_t2");
        ActiveAbilityStore.clearPlayer(playerId);
        return;
      }

      UUID targetId = targetPlayer.getUniqueId();
      this.primaryFlagsByPlayer.put(playerId, true);
      this.currentFlagsByPlayer.put(targetId, true);
      this.playerCounters.put(playerId, 0);
      player.sendMessage(
          ChatColor.translateAlternateColorCodes(
              '&',
              "&b🔮 You used "
                  + ChatColor.of("#5ED7FF")
                  + "Kinetic Overdrive &bon "
                  + targetPlayer.getName()));
      String name = player.getName();
      String accentColor = ChatColor.of("#5ED7FF").toString();
      targetPlayer.sendMessage(
          ChatColor.translateAlternateColorCodes(
              '&',
              "&b🔮 You have been affected with "
                  + name
                  + accentColor
                  + "'s Kinetic Overdrive!"));
      this.activateResolvedAbility(player, targetPlayer);
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
          this.primaryFlagsByPlayer.remove(playerId);
          this.currentFlagsByPlayer.remove(targetId);
          this.playerCounters.remove(playerId);
          player.removePotionEffect(PotionEffectType.HASTE);
          this.remainingTicks.put(playerId, 0);

        }, timestamp * 20L);
    } else {
      this.primaryFlagsByPlayer.put(playerId, true);
      this.playerCounters.put(playerId, 0);
      player.sendMessage(
          ChatColor.translateAlternateColorCodes(
              '&', "&b🔮 You have activated Kinetic Overdrive!"));
      this.updateSourceState(player.getLocation());
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
          this.primaryFlagsByPlayer.remove(playerId);
          this.playerCounters.remove(playerId);
          player.removePotionEffect(PotionEffectType.HASTE);
          this.remainingTicks.put(playerId, 0);

        }, timestamp * 20L);
    }
  }

  void sendTargetAbilityFeedback(Player player, Player targetPlayer) {
    if (!this.isTrustedPlayer(player, targetPlayer)) {
      ActiveAbilityStore.startActive(
          player.getUniqueId(),
          "flux_beam_ground_t2",
          getConfiguredInt("groundEffectDuration", 3),
          this.getAbilityLongValue(player, getConfiguredInt("groundCooldown", 30)));
      UUID playerId = player.getUniqueId();
      UUID targetId = targetPlayer.getUniqueId();
      long timestamp = getConfiguredInt("groundEffectDuration", 3) * 1000L;
      this.lastUpdateTimes.put(targetId, System.currentTimeMillis() + timestamp);
      HashMap<Integer, ItemStack> itemsBySlot = new HashMap<>();

      for (int count = 0; count < 9; count++) {
        ItemStack item = targetPlayer.getInventory().getItem(count);
        if (item != null) {
          itemsBySlot.put(count, item.clone());
        }
      }

      this.pendingItemsBySlotByPlayer.put(targetId, itemsBySlot);
      String accentColor = ChatColor.of("#0EC912").toString();
      String name = targetPlayer.getName();
      player.sendMessage(
          ChatColor.translateAlternateColorCodes(
              '&', "&b🔮 You have used Ground on " + accentColor + name));
      accentColor = ChatColor.of("#0EC912").toString();
      String message = player.getName();
      targetPlayer.sendMessage(
          ChatColor.translateAlternateColorCodes(
              '&', "&b🔮 You have been affected with Ground by " + accentColor + message));
      BukkitRunnable runnable = this.sourceTasksByPlayer.remove(targetId);
      if (runnable != null) {
        runnable.cancel();
      }

      this.activateStoredAbility(player, targetPlayer);
      int index = Math.max(1, getConfiguredInt("groundShuffleInterval", 10));
      if (getAbilityDoubleValue(player) >= 100.0) {
        this.refreshAbilityState(player, 0.0);
        GroundEffectController delayedAbilityTickTask = new GroundEffectController(this, targetPlayer, targetId);
        delayedAbilityTickTask.runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(index), index);
        this.sourceTasksByPlayer.put(targetId, delayedAbilityTickTask);
      }

      Bukkit.getScheduler()
          .runTaskLater(plugin, () -> this.finishGroundEffect(targetId), timestamp / 50L + 2L);
    }
  }

  void activateTargetAbility(Player player) {
    Map<Integer, ItemStack> pendingItemsBySlot = this.pendingItemsBySlotByPlayer.get(player.getUniqueId());
    if (pendingItemsBySlot != null) {
      java.util.concurrent.ThreadLocalRandom threadLocalRandom =
          java.util.concurrent.ThreadLocalRandom.current();
      int count = threadLocalRandom.nextInt(9);

      int index;
      do {
        index = threadLocalRandom.nextInt(9);
      } while (index == count);

      ItemStack item = player.getInventory().getItem(count);
      ItemStack heldItem = player.getInventory().getItem(index);
      ItemStack targetItem = pendingItemsBySlot.get(count);
      ItemStack candidateItem = pendingItemsBySlot.get(index);
      boolean enabled =
          item == null && targetItem == null
              || item != null
                  && targetItem != null
                  && item.isSimilar(targetItem)
                  && item.getAmount() == targetItem.getAmount();
      boolean active =
          heldItem == null && candidateItem == null
              || heldItem != null
                  && candidateItem != null
                  && heldItem.isSimilar(candidateItem)
                  && heldItem.getAmount() == candidateItem.getAmount();
      if (enabled && active) {
        ItemStack secondaryItem = player.getInventory().getItem(count);
        player.getInventory().setItem(count, player.getInventory().getItem(index));
        player.getInventory().setItem(index, secondaryItem);
      }
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (!this.isAbilityActive(player.getUniqueId())) {
        if (event.getDamager() instanceof Creeper creeper
            && creeper.isPowered()
            && (isProtectedTarget(player.getInventory().getItemInMainHand())
                || isProtectedTarget(player.getInventory().getItemInOffHand()))) {
          event.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  public void onPrimaryEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (!this.isAbilityActive(player.getUniqueId())) {
        this.onEntityDamageByEntityTarget(player, event.getEntity(), event);
        if (isProtectedTarget(player.getInventory().getItemInMainHand())
            || isProtectedTarget(player.getInventory().getItemInOffHand())) {
          this.activateActiveAbility(player);
        }

        if (event.getEntity() instanceof Player targetPlayer) {
          this.handleIncomingPlayerDamage(targetPlayer, event);
        }
      }
    }
  }

  void onEntityDamageByEntityTarget(Player player, Entity entity, EntityDamageByEntityEvent event) {
    UUID playerId = player.getUniqueId();
    if (!this.isTrustedPlayer(player, entity)) {
      if (this.primaryFlagsByPlayer.getOrDefault(playerId, false)) {
        int count = this.playerCounters.getOrDefault(playerId, 0);
        double value = 1.0;
        if (count >= 15) {
          value = 1.5;
        } else if (count >= 12) {
          value = 1.4;
        } else if (count >= 9) {
          value = 1.3;
        } else if (count >= 6) {
          value = 1.2;
        } else if (count >= 3) {
          value = 1.1;
        }

        if (value > 1.0) {
          event.setDamage(event.getDamage() * value);
          this.playerCounters.put(playerId, 0);
        }
      }

      if (this.canUseAbility(player)) {
        int index = this.abilityStages.getOrDefault(playerId, 0) + 1;
        this.abilityStages.put(playerId, index);
        double distance =
            index >= 15
                ? 1.5
                : (index >= 12
                    ? 1.0
                    : (index >= 9 ? 0.8 : (index >= 6 ? 0.6 : (index >= 3 ? 0.4 : 0.3))));
        event.setDamage(event.getDamage() + distance);
      }

      if (this.isValidTarget(player)
          && entity instanceof Player targetPlayer
          && !this.isAbilityBlocked(playerId)
          && !this.isTrustedPlayer(player, targetPlayer)) {
        this.sendTargetAbilityFeedback(player, targetPlayer);
      }
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (!(event instanceof EntityDamageByEntityEvent)) {
        if (!this.isAbilityActive(player.getUniqueId())) {
          UUID playerId = player.getUniqueId();
          this.activateActiveAbility(player);
          this.animationSteps.put(playerId, 0);
          player.removePotionEffect(PotionEffectType.HASTE);
          this.remainingTicks.put(playerId, 0);
          this.cooldownTimestamps.remove(playerId);
          if (this.canUseAbility(player)) {
            this.activateSourceAbility(player, event.getFinalDamage());
          }
        }
      }
    }
  }

  void handleIncomingPlayerDamage(Player player, EntityDamageByEntityEvent event) {
    UUID playerId = player.getUniqueId();
    this.activateActiveAbility(player);
    this.animationSteps.put(playerId, 0);
    player.removePotionEffect(PotionEffectType.HASTE);
    this.remainingTicks.put(playerId, 0);
    this.cooldownTimestamps.remove(playerId);
    if (this.primaryFlagsByPlayer.getOrDefault(playerId, false)) {
      int count = this.playerCounters.getOrDefault(playerId, 0) + 1;
      this.playerCounters.put(playerId, count);
      switch (count) {
        case 3:
          player.sendMessage(
              "§7You have §b3 §cATTACK §7points. §7Next hit: §b1.1x");
        case 4:
        case 5:
        case 7:
        case 8:
        case 10:
        case 11:
        case 13:
        case 14:
        default:
          break;
        case 6:
          player.sendMessage(
              "§7You have §b6 §cATTACK §7points. §7Next hit: §b1.2x");
          break;
        case 9:
          player.sendMessage(
              "§7You have §b9 §cATTACK §7points. §7Next hit: §b1.3x");
          break;
        case 12:
          player.sendMessage(
              "§7You have §b12 §cATTACK §7points. §7Next hit:"
                  + " §b1.4x");
          break;
        case 15:
          player.sendMessage(
              "§7You have §b15 §cATTACK §7points. §7Next hit:"
                  + " §b1.5x");
      }
    }

    if (this.canUseAbility(player)) {
      this.activateSourceAbility(player, event.getFinalDamage());
    }
  }

  void activateSourceAbility(Player player, double value) {
    UUID playerId = player.getUniqueId();
    double distance = value / 6.0;
    this.updateSourceAbilityState(playerId, distance);
  }

  void sendSourceAbilityFeedback(Player player, Location location, Location targetLocation) {
    if (!this.canUseAbility(player) && !this.isMatchingState(player)) {
      this.activateActiveAbility(player);
    } else {
      UUID playerId = player.getUniqueId();
      double distance = location.distance(targetLocation);
      if (!(distance <= 0.001)) {
        int count = this.animationSteps.getOrDefault(playerId, 0) + (int) Math.max(1L, Math.round(distance * 2.0));
        this.animationSteps.put(playerId, count);
        if (this.isMatchingState(player) && count >= 1200) {
          short value = 1200;
        } else {
          float scale = 0.2F;
          String text = null;
          if (count >= 2100) {
            scale = 0.6F;
            text = "Now moving at +200% speed!";
          } else if (count >= 2000) {
            scale = 0.58F;
            text = "Now moving at +190% speed!";
          } else if (count >= 1900) {
            scale = 0.56F;
            text = "Now moving at +180% speed!";
          } else if (count >= 1800) {
            scale = 0.54F;
            text = "Now moving at +170% speed!";
          } else if (count >= 1700) {
            scale = 0.52F;
            text = "Now moving at +160% speed!";
          } else if (count >= 1600) {
            scale = 0.5F;
            text = "Now moving at +150% speed!";
          } else if (count >= 1500) {
            scale = 0.48F;
            text = "Now moving at +140% speed!";
          } else if (count >= 1400) {
            scale = 0.46F;
            text = "Now moving at +130% speed!";
          } else if (count >= 1300) {
            scale = 0.44F;
            text = "Now moving at +120% speed!";
          } else if (count >= 1200) {
            scale = 0.42F;
            text = "Now moving at +110% speed!";
          } else if (count >= 1100) {
            scale = 0.4F;
            text = "Now moving at +100% speed!";
          } else if (count >= 1000) {
            scale = 0.38F;
            text = "Now moving at +90% speed!";
          } else if (count >= 900) {
            scale = 0.36F;
            text = "Now moving at +80% speed!";
          } else if (count >= 700) {
            scale = 0.34F;
            text = "Now moving at +70% speed!";
          } else if (count >= 600) {
            scale = 0.32F;
            text = "Now moving at +60% speed!";
          } else if (count >= 500) {
            scale = 0.3F;
            text = "Now moving at +50% speed!";
          } else if (count >= 400) {
            scale = 0.28F;
            text = "Now moving at +40% speed!";
          } else if (count >= 300) {
            scale = 0.26F;
            text = "Now moving at +30% speed!";
          } else if (count >= 200) {
            scale = 0.24F;
            text = "Now moving at +20% speed!";
          } else if (count >= 100) {
            scale = 0.22F;
            text = "Now moving at +10% speed!";
          }

          if (player.getWalkSpeed() != scale) {
            player.setWalkSpeed(scale);
          }

          if (text != null) {
            Set<String> trackedTags = this.previousTagsByPlayer.computeIfAbsent(playerId, FluxTier2Gem::createTrackingSet);
            if (trackedTags.add(text)) {
              player.sendMessage("§7" + text);
            }
          }
        }
      }
    }
  }

  void activateActiveAbility(Player player) {
    UUID playerId = player.getUniqueId();
    if (player.getWalkSpeed() != 0.2F) {
      player.setWalkSpeed(0.2F);
    }

    this.animationSteps.put(playerId, 0);
    this.previousTagsByPlayer.remove(playerId);
  }

  @EventHandler
  public void onPlayerItemHeld(PlayerItemHeldEvent event) {
    Player player = event.getPlayer();
    ItemStack item = player.getInventory().getItem(event.getNewSlot());
    boolean enabled = isProtectedTarget(item) || this.isGemItem(item);
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    boolean active = isProtectedTarget(offHandItem) || this.isGemItem(offHandItem);
    if (!enabled && !active) {
      this.activateActiveAbility(player);
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (this.canUseAbility(player)) {
        this.cooldownTimestamps.put(playerId, System.currentTimeMillis());
        int count = this.remainingTicks.getOrDefault(playerId, 0) + 1;
        this.remainingTicks.put(playerId, count);
        int index = getConfiguredInt("hasteBlocksPerLevel", 10);
        int remaining = getConfiguredInt("hasteMaxLevel", 9);
        int step = Math.min(remaining, count / index - 1);
        if (count % index == 0 && count <= index * remaining) {
          player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, step));
        }

        if (this.currentFlagsByPlayer.getOrDefault(playerId, false)) {
          for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
            if (this.primaryFlagsByPlayer.getOrDefault(targetPlayer.getUniqueId(), false)) {
              UUID targetId = targetPlayer.getUniqueId();
              int ticks = this.remainingTicks.getOrDefault(targetId, 0) + 1;
              this.remainingTicks.put(targetId, ticks);
              switch (ticks) {
                case 5:
                  targetPlayer.sendMessage("§7Haste §b1 §7for §b20s§7.");
                  targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 400, 0));
                  break;
                case 10:
                  targetPlayer.sendMessage("§7Haste §b2 §7for §b40s§7.");
                  targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 800, 1));
                  break;
                case 15:
                  targetPlayer.sendMessage("§7Haste §b3 §7for §b60s§7.");
                  targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 1200, 2));
                  break;
                case 20:
                  targetPlayer.sendMessage("§7Haste §b4 §7for §b80s§7.");
                  targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 1600, 3));
                  break;
                case 25:
                  targetPlayer.sendMessage("§7Haste §b5 §7for §b100s§7.");
                  targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 2000, 4));
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPrimaryEntityShootBow(EntityShootBowEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (!this.isAbilityActive(player.getUniqueId())) {
        if (this.canUseAbility(player)
            && java.util.concurrent.ThreadLocalRandom.current().nextDouble() < 0.05) {
          this.cachedFlagsByEntity.put(event.getProjectile(), true);
          this.updatePrimaryState(event.getProjectile());
        }
      }
    }
  }

  void updatePrimaryState(Entity entity) {
    new BukkitRunnable() {
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks < 100 && FluxTier2Gem.this.cachedFlagsByEntity.getOrDefault(entity, false) && !entity.isDead()) {
          entity
          .getLocation()
          .getWorld()
          .spawnParticle(
              Particle.DUST,
              entity.getLocation(),
              1,
              0.0,
              0.0,
              0.0,
              0.0,
              new DustOptions(Color.fromRGB(94, 215, 255), 1.0F));
          this.durationTicks++;
        } else {
          FluxTier2Gem.this.cachedFlagsByEntity.remove(entity);
          this.cancel();
        }
      }
    }
        .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    trackedStateByKey.put(playerId, 0.0);
    BukkitTask task = this.cooldownTasks.remove(playerId);
    if (task != null) {
      task.cancel();
    }

    this.primaryFlagsByPlayer.remove(playerId);
    this.animationSteps.put(playerId, 0);
    this.remainingTicks.put(playerId, 0);
    this.cooldownTimestamps.remove(playerId);
    this.pendingFlagsByPlayer.put(playerId, false);
    this.activeStateByKey.put(playerId, false);
    this.updatePrimaryAbilityState(playerId);
    this.updateTargetAbilityState(playerId);
    this.targetDamageSamplesByPlayer.remove(playerId);
    ActiveAbilityStore.clearPlayer(playerId);
    this.activateActiveAbility(player);
    player.removePotionEffect(PotionEffectType.HASTE);
  }

  public static double getAbilityDoubleValue(Player player) {
    return player == null ? 0.0 : trackedStateByKey.getOrDefault(player.getUniqueId(), 0.0);
  }

  public static void activatePendingAbility(Player player) {
    resolvedFlagsByPlayer.put(player.getUniqueId(), false);
  }

  public void activateCachedAbility(Player player) {
    UUID playerId = player.getUniqueId();
    updateAbilityState(playerId, "flux_beam_ground_t2");
    updateAbilityState(playerId, "flux_static_t2");
    updateAbilityState(playerId, "flux_kinetic_t2");
    ActiveAbilityStore.clearPlayer(playerId);
  }

  boolean hasRequiredState(LivingEntity livingEntity) {
    return !(livingEntity instanceof Player player)
        ? false
        : player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
            || player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING;
  }

  void updateTargetState(Location location) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      double effectRadius = 1.0;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks < 10) {
          FluxTier2Gem.this.spawnPendingAbilityParticles(this.anchorLocation, this.effectRadius);
          this.effectRadius += 0.5;
          this.durationTicks++;
        } else {
          for (int count = 0; count < 7; count++) {
            double value = this.effectRadius;
            Bukkit.getScheduler().runTaskLater(FluxTier2Gem.plugin, () -> {
                  FluxTier2Gem.this.spawnPendingAbilityParticles(this.anchorLocation, value);

                }, count * 6L);
          }

          this.cancel();
        }
      }
    }
        .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void spawnPendingAbilityParticles(Location location, double value) {
    int count = (int) Math.floor((Math.PI * 2) * value / 0.1);

    for (int index = 0; index < count; index++) {
      double angleRadians = Math.toRadians(index * 360.0 / count);
      Location offset =
          location.clone().add(new Vector(value * Math.cos(angleRadians), 0.0, value * Math.sin(angleRadians)));
      location.getWorld()
          .spawnParticle(
              Particle.DUST,
              offset,
              1,
              0.0,
              0.0,
              0.0,
              0.0,
              new DustOptions(Color.fromRGB(94, 215, 255), 1.0F));
      location.getWorld().spawnParticle(Particle.SMOKE, offset, 1, 0.0, 0.0, 0.0, 0.0);
    }
  }

  void activateResolvedAbility(Player player, Player targetPlayer) {
    new BukkitRunnable() {
      final Player secondaryPlayer = targetPlayer;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= 30) {
          this.cancel();
        } else {
          Location location = player.getLocation().add(0.0, 1.0, 0.0);
          Location targetLocation = this.secondaryPlayer.getLocation().add(0.0, 1.0, 0.0);
          double distance = location.distance(targetLocation);
          Vector direction = targetLocation.toVector().subtract(location.toVector()).normalize().multiply(0.1);

          for (double value = 0.0; value < distance; value += 0.1) {
            location.getWorld()
            .spawnParticle(
                Particle.DUST,
                location.clone().add(direction.clone().multiply(value / 0.1)),
                1,
                0.0,
                0.0,
                0.0,
                0.0,
                new DustOptions(Color.fromRGB(94, 215, 255), 1.5F));
          }

          this.durationTicks++;
        }
      }
    }
        .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(2L), 2L);
  }

  void updateSourceState(Location location) {
    double[] value = new double[] {0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5, 2.75, 3.0};
    new BukkitRunnable() {
      final double[] state = value;
      final Location anchorLocation = location;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= this.state.length) {
          this.cancel();
        } else {
          FluxTier2Gem.this.spawnPendingAbilityParticles(this.anchorLocation, this.state[this.durationTicks]);
          if (this.durationTicks + 1 < this.state.length) {
            FluxTier2Gem.this.spawnPendingAbilityParticles(this.anchorLocation, this.state[this.durationTicks + 1]);
          }

          this.durationTicks += 2;
        }
      }
    }
        .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void activateStoredAbility(Player player, Player targetPlayer) {
    new BukkitRunnable() {
      final Player secondaryPlayer = targetPlayer;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= 10) {
          this.cancel();
        } else {
          Location location = player.getLocation().add(0.0, 1.0, 0.0);
          Location targetLocation = this.secondaryPlayer.getLocation().add(0.0, 1.0, 0.0);
          double distance = targetLocation.distance(location);
          Vector direction = location.toVector().subtract(targetLocation.toVector()).normalize().multiply(0.1);

          for (double value = 0.0; value < distance; value += 0.1) {
            targetLocation.getWorld()
            .spawnParticle(
                Particle.DUST,
                targetLocation.clone().add(direction.clone().multiply(value / 0.1)),
                1,
                0.0,
                0.0,
                0.0,
                0.0,
                new DustOptions(Color.fromRGB(16, 131, 173), 1.5F));
          }

          this.durationTicks++;
        }
      }
    }
        .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(2L), 2L);
  }

  boolean shouldApplyEffect(UUID playerId) {
    Long storedTimestamp = this.lastUseTimes.get(playerId);
    if (storedTimestamp == null) {
      return false;
    } else if (System.currentTimeMillis() >= storedTimestamp) {
      this.lastUseTimes.remove(playerId);
      return false;
    } else {
      return true;
    }
  }

  boolean canAffectTarget(UUID playerId) {
    Long storedTimestamp = this.lastUpdateTimes.get(playerId);
    if (storedTimestamp == null) {
      return false;
    } else if (System.currentTimeMillis() >= storedTimestamp) {
      this.lastUpdateTimes.remove(playerId);
      return false;
    } else {
      return true;
    }
  }

  void updatePrimaryAbilityState(UUID playerId) {
    this.lastUseTimes.remove(playerId);
  }

  void updateTargetAbilityState(UUID playerId) {
    this.lastUpdateTimes.remove(playerId);
    this.pendingItemsBySlotByPlayer.remove(playerId);
    BukkitRunnable runnable = this.sourceTasksByPlayer.remove(playerId);
    if (runnable != null) {
      runnable.cancel();
    }
  }

  void updateSourceAbilityState(UUID playerId, double value) {
    this.trackedValuesByPlayer.merge(playerId, value, Double::sum);
    long now = System.currentTimeMillis() + 120000L;
    this.targetDamageSamplesByPlayer
        .computeIfAbsent(playerId, FluxTier2Gem::createDamageSampleQueue)
        .addLast(new FluxTier2DamageSample(value, now));
  }

  void updateActiveAbilityState(UUID playerId) {
    Deque<FluxTier2DamageSample> deque = this.targetDamageSamplesByPlayer.get(playerId);
    if (deque != null && !deque.isEmpty()) {
      long now = System.currentTimeMillis();
      double value = 0.0;

      while (!deque.isEmpty()
          && (deque.peekFirst()).expiresAtMillis() <= now) {
        value += (deque.pollFirst()).amount();
      }

      if (value > 0.0) {
        double distance = this.trackedValuesByPlayer.getOrDefault(playerId, 0.0);
        double radius = Math.max(0.0, distance - value);
        if (radius <= 0.0) {
          this.trackedValuesByPlayer.remove(playerId);
        } else {
          this.trackedValuesByPlayer.put(playerId, radius);
        }
      }

      if (deque.isEmpty()) {
        this.targetDamageSamplesByPlayer.remove(playerId);
      }
    }
  }

  static Deque<FluxTier2DamageSample> createDamageSampleQueue(UUID playerId) {
    return new ArrayDeque<>();
  }

  static Set<String> createTrackingSet(UUID playerId) {
    return new HashSet<>();
  }

  void finishGroundEffect(UUID playerId) {
    if (!this.canAffectTarget(playerId)) {
      this.updateTargetAbilityState(playerId);
    }
  }

  static void clearBeamDamageFlag(UUID playerId) {
    storedFlagsByPlayer.remove(playerId);
  }

  void spawnEnergyBeamEffects(Player player, double value) {
    this.spawnPendingAbilityParticles(player.getLocation(), value);
  }

  void expireProjectileCooldown(UUID playerId) {
    Long storedTimestamp = this.lastUseTimes.get(playerId);
    if (storedTimestamp != null && System.currentTimeMillis() >= storedTimestamp) {
      this.lastUseTimes.remove(playerId);
    }
  }

  static {
    FLUX_BEAM_GROUND_T2_ID = "flux_beam_ground_t2";
    CROSSBOW_STUN_ARROW_ID = "crossbow_stun_arrow";
    FLUX_STATIC_T2_ID = "flux_static_t2";
    FLUX_KINETIC_T2_ID = "flux_kinetic_t2";
    FLUX_T2_ID = "fluxT2.";
    resolvedFlagsByPlayer = new ConcurrentHashMap<>();
    trackedStateByKey = new ConcurrentHashMap<>();
    secondaryValuesByPlayer = new ConcurrentHashMap<>();
    nextValuesByPlayer = new ConcurrentHashMap<>();
    FLUX_ID = ChatColor.translateAlternateColorCodes('&', "&b🔮 Flux Charging Station");
    storedFlagsByPlayer = new ConcurrentHashMap<>();
    player = null;
    playerIds = ConcurrentHashMap.newKeySet();
    cooldownTicks = ConfigValueCache.getInt(Bliss.getInstance(), "fluxT2.shockingChance", 15);
    maxCount = ConfigValueCache.getInt(Bliss.getInstance(), "fluxT2.shockDuration", 2);
  }

  private static final class GroundEffectController extends BukkitRunnable {
    final Player player;
    final UUID playerId;
    final FluxTier2Gem fluxTier2Gem;
    static final String GROUND_EFFECT_WORN_OFF_MESSAGE = "&7Ground effect has worn off.";
    GroundEffectController(FluxTier2Gem fluxTier2Gem, Player player, UUID playerId) {
      this.fluxTier2Gem = fluxTier2Gem;
      this.player = player;
      this.playerId = playerId;
    }

    public void run() {
      if (!this.player.isOnline()) {
        this.fluxTier2Gem.updateTargetAbilityState(this.playerId);
        this.cancel();
      } else if (!this.fluxTier2Gem.canAffectTarget(this.playerId)) {
        this.fluxTier2Gem.updateTargetAbilityState(this.playerId);
        this.player.sendMessage(ChatColor.translateAlternateColorCodes('&', GROUND_EFFECT_WORN_OFF_MESSAGE));
        this.cancel();
      } else {
        this.fluxTier2Gem.activateTargetAbility(this.player);
      }
    }

}
}
