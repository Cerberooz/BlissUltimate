package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import static cerberooz.cerberooz.BlissUltimate.GrappleMode.CHAIN;
import static cerberooz.cerberooz.BlissUltimate.GrappleMode.GRAPPLE_HOOK;
import static cerberooz.cerberooz.BlissUltimate.GrappleMode.MIDDLE;

public class AuratusGem implements Listener {
  final Map<UUID, Location> playerLocations = new ConcurrentHashMap<>();
  final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
  final Set<UUID> protectedPlayers = ConcurrentHashMap.newKeySet();
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();
  final Set<UUID> affectedPlayers = ConcurrentHashMap.newKeySet();
  final Map<UUID, Long> lastUpdateTimes = new ConcurrentHashMap<>();
  static final Map<UUID, Long> lastUseTimes;
  final Bliss plugin;
  static final Set<UUID> targetPlayerIds;
  static final Set<UUID> trackedPlayers;
  static final Map<UUID, Long> lastActivationTimes;
  static final Map<UUID, Long> expiryTimes;
  static final long startTime = 75L;
  static final boolean active = false;
  static final DustOptions secondaryDustOptions;
  static final DustOptions activeDustOptions;
  static final DustOptions dustOptions;
  final Set<UUID> primaryPlayerIds = ConcurrentHashMap.newKeySet();
  final Map<UUID, Double> currentValuesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Double> activeValuesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Long> cooldownTimestamps = new ConcurrentHashMap<>();
  final Set<UUID> pendingPlayers = ConcurrentHashMap.newKeySet();
  final Map<UUID, GrappleMode> grappleModesByPlayer = new ConcurrentHashMap<>();
  final Set<UUID> sourcePlayerIds = ConcurrentHashMap.newKeySet();
  final Map<UUID, List<Location>> secondaryLocationsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Integer> playerCounters = new ConcurrentHashMap<>();
  ChainGrappleManager chainGrappleManager;
  static final Map<UUID, Integer> abilityStages;
  static final Map<UUID, Long> sourceLastUseTimes;
  static final Map<UUID, Long> targetLastUseTimes;
  final Map<UUID, Location> targetLocations = new ConcurrentHashMap<>();
  static final Map<UUID, Long> primaryLastUseTimes;
  static final Map<UUID, Boolean> primaryFlagsByPlayer;
  static final Map<UUID, Integer> animationSteps;
  static final Map<UUID, Long> startTimes;
  final Map<UUID, BukkitRunnable> trackedTasksByPlayer = new ConcurrentHashMap<>();
  static final Map<UUID, String> cachedValuesByPlayer;
  static final Map<UUID, Long> activeLastUseTimes;
  final Set<UUID> cachedPlayerIds = ConcurrentHashMap.newKeySet();
  final Set<UUID> activePlayerIds = ConcurrentHashMap.newKeySet();
  final Set<UUID> resolvedPlayerIds = ConcurrentHashMap.newKeySet();
  final Map<UUID, Double> pendingValuesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Double> targetValuesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Integer> remainingTicks = new ConcurrentHashMap<>();
  final Set<UUID> pendingPlayerIds = ConcurrentHashMap.newKeySet();
  final Map<UUID, Integer> chargeLevels = new ConcurrentHashMap<>();
  final Map<UUID, Location> originLocations = new ConcurrentHashMap<>();
  final Set<UUID> trackedDisplays = ConcurrentHashMap.newKeySet();
  public AuratusGem(Bliss bliss) {
    this.plugin = bliss;
    this.startBackgroundTasks();
    this.chainGrappleManager = new ChainGrappleManager(bliss);
    bliss.getServer().getPluginManager().registerEvents(this.chainGrappleManager, bliss);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getHand() != null) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR
          || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        Player player = event.getPlayer();
        ItemStack mainHandItem =
            event.getHand() == EquipmentSlot.HAND
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();
        if (mainHandItem != null && mainHandItem.getType() == Material.WIND_CHARGE) {
          UUID playerId = player.getUniqueId();
          this.activePlayerIds.add(playerId);
          this.resolvedPlayerIds.remove(playerId);
          this.pendingPlayerIds.remove(playerId);
          this.chargeLevels.remove(playerId);
          this.originLocations.remove(playerId);
          double playerY = player.getLocation().getY();
          this.pendingValuesByPlayer.put(playerId, playerY);
          this.targetValuesByPlayer.put(playerId, playerY);
          Integer storedCount = this.remainingTicks.remove(playerId);
          if (storedCount != null) {
            Bukkit.getScheduler().cancelTask(storedCount);
          }

          int scheduledTask =
              Bukkit.getScheduler()
                  .runTaskLater(
                      this.plugin, () -> this.clearWindChargeState(playerId), this.getWindChargeArmSeconds())
                  .getTaskId();
          this.remainingTicks.put(playerId, scheduledTask);
        }
      }
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    Location location = event.getTo();
    if (location != null && event.getFrom().getY() != location.getY()) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();
      if (this.activePlayerIds.contains(playerId)) {
        double y = location.getY();
        Double currentDouble = this.targetValuesByPlayer.get(playerId);
        double value = currentDouble != null ? currentDouble : y;
        if (y > value) {
          value = y;
          this.targetValuesByPlayer.put(playerId, value);
        }

        double distance = this.pendingValuesByPlayer.getOrDefault(playerId, y);
        double radius = value - distance;
        if (!(radius < this.getEffectScale())) {
          this.refreshPlayerState(player);
        }
      }
    }
  }

  @EventHandler
  public void refreshAbilityState(PlayerMoveEvent event) {
    if (!event.hasChangedPosition()) {
      return;
    }

    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (this.pendingPlayerIds.contains(playerId)) {
      Location location = event.getTo();
      if (location != null) {
        if (player.isOnline()
            && !player.isDead()
            && !this.isAbilityAllowed(playerId)
            && !this.plugin.isGemsDisabled()
            && !this.plugin.isAbilityAllowed(player.getWorld())
            && !this.plugin.canUseAbility(player)) {
          Location targetLocation = this.originLocations.get(playerId);
          this.originLocations.put(playerId, location.clone());
          if (targetLocation != null
              && targetLocation.getWorld() != null
              && location.getWorld() != null
              && targetLocation.getWorld().equals(location.getWorld())) {
            double x = location.getX() - targetLocation.getX();
            double z = location.getZ() - targetLocation.getZ();
            double value = x * x + z * z;
            boolean block =
                player.isOnGround()
                    || location.clone().subtract(0.0, 0.12, 0.0).getBlock().getType().isSolid()
                    || location.clone().subtract(0.0, 0.35, 0.0).getBlock().getType().isSolid();
            if (!block) {
              this.chargeLevels.put(playerId, 0);
            } else if (value <= this.getDamageAmount()) {
              int count = this.chargeLevels.getOrDefault(playerId, 0) + 1;
              this.chargeLevels.put(playerId, count);
              if (count >= this.getMaxCount()) {
                this.updatePlayerState(playerId);
              }
            } else {
              this.chargeLevels.put(playerId, 0);
            }
          } else {
            this.chargeLevels.put(playerId, 0);
          }
        } else {
          this.updatePlayerState(playerId);
        }
      }
    }
  }

  boolean isAbilityActive(UUID playerId) {
    Long storedTimestamp = lastActivationTimes.get(playerId);
    return storedTimestamp != null && System.currentTimeMillis() < storedTimestamp;
  }

  void activateAbility(Player player, int count) {
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    if (mainHandItem != null && !mainHandItem.getType().isAir()) {
      if (mainHandItem.getType().getMaxDurability() > 0) {
        short value = (short) (mainHandItem.getDurability() + count);
        if (value >= mainHandItem.getType().getMaxDurability()) {
          player.getInventory().setItemInMainHand(null);
        } else {
          mainHandItem.setDurability(value);
        }
      }
    }
  }

  boolean isAbilityAllowed(UUID playerId) {
    return this.plugin.isAbilityBlocked(playerId);
  }

  long getAbilityLong(UUID playerId) {
    return this.plugin.getAbilityLong(playerId);
  }

  boolean canUseAbility(Player player, Entity entity) {
    if (entity instanceof Player targetPlayer) {
      if (player.getUniqueId().equals(targetPlayer.getUniqueId())) {
        return true;
      }

      try {
        return this.plugin.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId());
      } catch (NoSuchMethodError | Exception noSuchMethodErrorException) {
        return false;
      }
    } else {
      return false;
    }
  }

  void updateAbilityState(UUID playerId, long timestamp) {
    int count = abilityStages.computeIfAbsent(playerId, this::initializeCounterFromConfig);
    if (count >= this.getChainCharges()) {
      sourceLastUseTimes.put(playerId, timestamp);
    } else {
      long lastUpdateTime = sourceLastUseTimes.getOrDefault(playerId, timestamp);
      long startTime = timestamp - lastUpdateTime;
      if (startTime >= this.getDurationMillis()) {
        int index = (int) (startTime / this.getDurationMillis());
        int remaining = Math.min(this.getChainCharges(), count + index);
        abilityStages.put(playerId, remaining);
        if (remaining >= this.getChainCharges()) {
          sourceLastUseTimes.put(playerId, timestamp);
        } else {
          sourceLastUseTimes.put(playerId, lastUpdateTime + index * this.getDurationMillis());
        }

        cachedValuesByPlayer.remove(playerId);
      }
    }
  }

  double getAbilityDoubleValue(double value, double distance, double radius) {
    return Math.max(distance, Math.min(radius, value));
  }

  double getEffectRadius(double value, double distance, double radius) {
    return value + (distance - value) * radius;
  }

  int getConfiguredInt(String text, int count) {
    return ConfigValueCache.getInt(this.plugin, "auratusGem." + text, count);
  }

  double getConfiguredDouble(String text, double value) {
    return ConfigValueCache.getDouble(this.plugin, "auratusGem." + text, value);
  }

  long getAbilityLongValue(double value) {
    return (long) (value * 1000.0);
  }

  long getCooldownMillis(double value) {
    return Math.round(value * 20.0);
  }

  int getChainCharges() {
    return this.getConfiguredInt("chainCharges", 2);
  }

  long getDurationMillis() {
    return this.getAbilityLongValue(this.getConfiguredDouble("chainRechargeTime", 10.0));
  }

  long getLastUseTime() {
    return this.getAbilityLongValue(this.getConfiguredDouble("bloodLinkingCooldownSeconds", 10.0));
  }

  long getExpiryTime() {
    return this.getAbilityLongValue(this.getConfiguredDouble("skyHookCooldownSeconds", 10.0));
  }

  long getConfiguredLong() {
    return this.getAbilityLongValue(this.getConfiguredDouble("parryCooldownSeconds", 10.0));
  }

  long getParryWindowSeconds() {
    return this.getCooldownMillis(this.getConfiguredDouble("parryWindowSeconds", 2.0));
  }

  int getDurationTicks() {
    return this.getConfiguredInt("parryDurabilityDamage", 40);
  }

  long getFlagPoleDuration() {
    return this.getCooldownMillis(this.getConfiguredDouble("flagPoleDuration", 15.0));
  }

  boolean isAbilityBlocked(UUID playerId) {
    Long storedTimestamp = expiryTimes.get(playerId);
    if (storedTimestamp == null) {
      return false;
    } else if (System.currentTimeMillis() >= storedTimestamp) {
      expiryTimes.remove(playerId);
      return false;
    } else {
      return true;
    }
  }

  void applyAbilityEffects(UUID playerId) {
    expiryTimes.put(playerId, System.currentTimeMillis() + 75L);
  }

  double getEffectScale() {
    return this.getConfiguredDouble("windChargeMinHeight", 2.25);
  }

  long getWindChargeArmSeconds() {
    return this.getCooldownMillis(this.getConfiguredDouble("windChargeArmSeconds", 7.0));
  }

  double getDamageAmount() {
    return this.getConfiguredDouble("windHopStopSpeedSquared", 0.015);
  }

  int getMaxCount() {
    return this.getConfiguredInt("windHopStopTicks", 12);
  }

  double getKnockbackStrength() {
    return this.getConfiguredDouble("groundSlamMinBlocks", 6.5);
  }

  double getMaxDistance() {
    return this.getConfiguredDouble("groundSlamMaxBlocks", 50.0);
  }

  double getVerticalOffset() {
    return this.getConfiguredDouble("groundSlamMinDamage", 4.0);
  }

  double getHorizontalOffset() {
    return this.getConfiguredDouble("groundSlamMaxDamage", 12.0);
  }

  double getAnimationProgress() {
    return this.getConfiguredDouble("groundSlamMinRadius", 4.5);
  }

  double getGroundSlamMaxRadius() {
    return this.getConfiguredDouble("groundSlamMaxRadius", 6.5);
  }

  int getChargeLevel() {
    return (int)
        this.getCooldownMillis(this.getConfiguredDouble("groundSlamWindChargeDisableSeconds", 10.0));
  }

  long getActiveAbilityLongValue() {
    return this.getAbilityLongValue(this.getConfiguredDouble("groundSlamTrackTimeoutSeconds", 8.0));
  }

  double getGroundSlamLoftHeight() {
    return this.getConfiguredDouble("groundSlamLoftHeight", 2.0);
  }

  double getHopNearGroundDistance() {
    return this.getConfiguredDouble("hopNearGroundDistance", 1.35);
  }

  double getActiveAbilityDoubleValue() {
    return this.getConfiguredDouble("hopMinDownwardSpeed", 0.35);
  }

  double getPendingAbilityDoubleValue() {
    return this.getConfiguredDouble("hopForwardStrength", 3.2);
  }

  double getCachedAbilityDoubleValue() {
    return this.getConfiguredDouble("hopUpwardStrength", 1.25);
  }

  double getResolvedAbilityDoubleValue() {
    return this.getConfiguredDouble("hopLookUpSuppressY", 0.45);
  }

  double getSkyHookHeight() {
    return this.getConfiguredDouble("skyHookHeight", 30.0);
  }

  int getRemainingTicks() {
    return this.getConfiguredInt("skyHookBarrierRadius", 9);
  }

  long getPendingAbilityLongValue() {
    return Math.round(this.getConfiguredDouble("skyHookBarrierTicks", 120.0));
  }

  double getFlagPoleHookHeight() {
    return this.getConfiguredDouble("flagPoleHookHeight", 22.0);
  }

  int getEffectCount() {
    return this.getConfiguredInt("flagPoleBarrierRadius", 2);
  }

  long getCachedAbilityLongValue() {
    return Math.round(this.getConfiguredDouble("flagPoleBarrierTicks", 100.0));
  }

  double getPreviousAbilityDoubleValue(double value) {
    double distance = this.getAbilityDoubleValue(value, this.getKnockbackStrength(), this.getMaxDistance());
    return (distance - this.getKnockbackStrength()) / (this.getMaxDistance() - this.getKnockbackStrength());
  }

  void playAbilityEffects(Player player, GrappleMode grappleMode, boolean enabled) {
    UUID playerId = player.getUniqueId();
    double playerY = player.getLocation().getY();
    this.spawnAbilityEffects(player, grappleMode, enabled, playerY, false);
  }

  void spawnAbilityEffects(Player player, GrappleMode grappleMode, boolean enabled, double value, boolean active) {
    UUID playerId = player.getUniqueId();
    double playerY = player.getLocation().getY();
    this.primaryPlayerIds.add(playerId);
    this.currentValuesByPlayer.put(playerId, playerY);
    this.activeValuesByPlayer.put(playerId, Math.max(playerY, value));
    this.cooldownTimestamps.put(playerId, System.currentTimeMillis());
    this.grappleModesByPlayer.put(playerId, grappleMode);
    if (active) {
      this.pendingPlayers.add(playerId);
    } else {
      this.pendingPlayers.remove(playerId);
    }

    if (enabled) {
      this.sourcePlayerIds.add(playerId);
    } else {
      this.sourcePlayerIds.remove(playerId);
    }
  }

  boolean isMatchingState(UUID playerId) {
    return this.primaryPlayerIds.contains(playerId);
  }

  boolean isValidTarget(Player player) {
    Location location = player.getLocation();
    return player.isOnGround()
        || location.clone().subtract(0.0, 0.12, 0.0).getBlock().getType().isSolid()
        || location.clone().subtract(0.0, 0.35, 0.0).getBlock().getType().isSolid();
  }

  boolean isProtectedTarget(Player player) {
    return this.isValidTarget(player)
        ? false
        : this.getNextAbilityDoubleValue(player.getLocation(), this.getMaxDistance()) >= this.getKnockbackStrength()
            || player.getFallDistance() >= this.getKnockbackStrength();
  }

  void cleanupAbilityState(Player player, boolean enabled) {
    if (player.isOnGround()) {
      this.playAbilityEffects(player, CHAIN, false);
    } else {
      double playerY = player.getLocation().getY() + (enabled ? player.getFallDistance() : 0.0);
      this.spawnAbilityEffects(player, CHAIN, true, playerY, true);
    }
  }

  double getNextAbilityDoubleValue(Location location, double value) {
    World world = location.getWorld();
    if (world == null) {
      return 0.0;
    }

    for (double distance = 0.1; distance <= value; distance += 0.25) {
      if (location.clone().subtract(0.0, distance, 0.0).getBlock().getType().isSolid()) {
        return distance;
      }
    }

    return value;
  }

  void scheduleAbilityUpdate(UUID playerId) {
    if (this.grappleModesByPlayer.get(playerId) == CHAIN) {
      this.updatePlayerState(playerId);
    }

    this.primaryPlayerIds.remove(playerId);
    this.currentValuesByPlayer.remove(playerId);
    this.activeValuesByPlayer.remove(playerId);
    this.cooldownTimestamps.remove(playerId);
    this.pendingPlayers.remove(playerId);
    this.grappleModesByPlayer.remove(playerId);
    this.sourcePlayerIds.remove(playerId);
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(new BukkitRunnable() {
      @Override
      public void run() {
        AuratusGem.this.cachedPlayerIds.clear();
        Collection<? extends Player> collection = Bukkit.getOnlinePlayers();
        if (!collection.isEmpty()) {
          long now = System.currentTimeMillis();

          for (Player player : collection) {
            UUID playerId = player.getUniqueId();
            AuratusGem.this.updateAbilityState(playerId, now);
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            boolean enabled = AuratusGem.this.isGemItem(mainHandItem) || AuratusGem.this.isGemItem(offHandItem);
            if (!enabled) {
              AuratusGem.cachedValuesByPlayer.remove(playerId);
              AuratusGem.activeLastUseTimes.remove(playerId);
            } else {
              AuratusGem.this.completeAbilityAction(player);
              if (AuratusGem.this.isAbilityAllowed(playerId)) {
                long timestamp = AuratusGem.this.getAbilityLong(playerId);
                long lastUpdateTime = timestamp / 1000L;
                String darkGray = ChatColor.DARK_GRAY.toString();
                String bold = ChatColor.BOLD.toString();
                player.sendActionBar(
                    "🔒 "
                    + darkGray
                    + bold
                    + "ᴅɪꜱᴀʙʟᴇᴅ: "
                    + lastUpdateTime
                    + "s");
              } else if (!AuratusGem.this.plugin.isGemsDisabled()
                  && !AuratusGem.this.plugin.isAbilityAllowed(player.getWorld())
                  && !AuratusGem.this.plugin.canUseAbility(player)) {
                AuratusGem.this.cachedPlayerIds.add(playerId);
                AuratusGem.this.trackAbilityState(player);
              } else {
                String displayColor = ChatColor.DARK_GRAY.toString();
                String textColor = ChatColor.BOLD.toString();
                player.sendActionBar(
                    "🔒 "
                    + displayColor
                    + textColor
                    + "ᴅɪꜱᴀʙʟᴇᴅ");
              }
            }
          }
        }
      }
    }, this.plugin, 1L, 20L);
  }

  void completeAbilityAction(Player player) {
    ItemStack[] item = player.getInventory().getArmorContents();

    for (ItemStack heldItem : item) {
      if (heldItem != null && !heldItem.getType().isAir() && heldItem.getType().getMaxDurability() > 0) {
        short value = heldItem.getDurability();
        if (value > 0) {
          heldItem.setDurability((short) Math.max(0, value - 1));
        }
      }
    }
  }

  @EventHandler
  public void processAbilityState(PlayerMoveEvent event) {
    if (!event.hasChangedPosition()) {
      return;
    }

    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (this.isMatchingState(playerId)) {
      if (this.grappleModesByPlayer.get(playerId) != GRAPPLE_HOOK) {
        if (player.isOnline()
            && !player.isDead()
            && !this.isAbilityAllowed(playerId)
            && !this.plugin.isGemsDisabled()
            && !this.plugin.isAbilityAllowed(player.getWorld())
            && !this.plugin.canUseAbility(player)) {
          long timestamp = this.cooldownTimestamps.getOrDefault(playerId, 0L);
          if (System.currentTimeMillis() - timestamp > this.getActiveAbilityLongValue()) {
            this.scheduleAbilityUpdate(playerId);
          } else {
            Location location = event.getTo();
            if (location != null) {
              double y = location.getY();
              this.activeValuesByPlayer.merge(playerId, y, Math::max);
              boolean block =
                  player.isOnGround()
                      || location.clone().subtract(0.0, 0.12, 0.0).getBlock().getType().isSolid()
                      || location.clone().subtract(0.0, 0.35, 0.0).getBlock().getType().isSolid();
              GrappleMode grappleMode = this.grappleModesByPlayer.get(playerId);
              boolean enabled = this.sourcePlayerIds.contains(playerId);
              if (!this.pendingPlayers.contains(playerId)) {
                double value = this.currentValuesByPlayer.getOrDefault(playerId, y);
                if (!block && y - value >= this.getGroundSlamLoftHeight()) {
                  this.pendingPlayers.add(playerId);
                }
              } else if (!block) {
                if (grappleMode == CHAIN && enabled && this.isTrackedTarget(player, location)) {
                  this.scheduleAbilityUpdate(playerId);
                  this.spawnAbilityParticles(player);
                  this.updateSourceAbilityState(playerId, 1);
                }
              } else {
                double distance = this.activeValuesByPlayer.getOrDefault(playerId, y);
                double radius = Math.max(player.getFallDistance(), Math.max(0.0, distance - y));
                this.scheduleAbilityUpdate(playerId);
                if (enabled) {
                  if (radius >= this.getKnockbackStrength()) {
                    Location targetLocation = player.getLocation().clone().add(0.0, -1.0, 0.0);
                    if (this.isLocationBlocked(player, targetLocation)) {
                      this.spawnPrimaryAbilityParticles(player, targetLocation, radius);
                    } else if (grappleMode == CHAIN && player.isSneaking()) {
                      this.spawnAbilityParticles(player);
                      this.updateSourceAbilityState(playerId, 1);
                    } else if (grappleMode == MIDDLE) {
                      this.spawnAbilityParticles(player);
                    }

                    if (grappleMode == MIDDLE) {
                      lastUseTimes.put(playerId, System.currentTimeMillis() + this.getExpiryTime());
                      cachedValuesByPlayer.remove(playerId);
                    }
                  } else {
                    if (grappleMode == CHAIN && player.isSneaking()) {
                      this.spawnAbilityParticles(player);
                      this.updateSourceAbilityState(playerId, 1);
                    } else if (grappleMode == MIDDLE) {
                      this.spawnAbilityParticles(player);
                    }

                    if (grappleMode == MIDDLE) {
                      lastUseTimes.put(playerId, System.currentTimeMillis() + this.getExpiryTime());
                      cachedValuesByPlayer.remove(playerId);
                    }
                  }
                }
              }
            }
          }
        } else {
          this.scheduleAbilityUpdate(playerId);
        }
      }
    }
  }

  @EventHandler
  public void handleAbilityAction(PlayerMoveEvent event) {
    if (!event.hasChangedPosition()) {
      return;
    }

    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (this.isMatchingState(playerId)) {
      if (this.grappleModesByPlayer.get(playerId) == GRAPPLE_HOOK) {
        if (player.isOnline()
            && !player.isDead()
            && !this.isAbilityAllowed(playerId)
            && !this.plugin.isGemsDisabled()
            && !this.plugin.isAbilityAllowed(player.getWorld())
            && !this.plugin.canUseAbility(player)) {
          long timestamp = this.cooldownTimestamps.getOrDefault(playerId, 0L);
          if (System.currentTimeMillis() - timestamp > this.getActiveAbilityLongValue()) {
            this.scheduleAbilityUpdate(playerId);
            this.chainGrappleManager.applyAbilityEffects(playerId);
          } else {
            Location location = event.getTo();
            if (location != null) {
              boolean block =
                  player.isOnGround()
                      || location.clone().subtract(0.0, 0.12, 0.0).getBlock().getType().isSolid()
                      || location.clone().subtract(0.0, 0.35, 0.0).getBlock().getType().isSolid();
              if (!this.pendingPlayers.contains(playerId)) {
                if (!block) {
                  this.pendingPlayers.add(playerId);
                }
              } else if (!block) {
                if (this.isTrackedTarget(player, location)) {
                  this.scheduleAbilityUpdate(playerId);
                  this.chainGrappleManager.applyAbilityEffects(playerId);
                  this.spawnAbilityParticles(player);
                }
              } else {
                this.scheduleAbilityUpdate(playerId);
                this.chainGrappleManager.applyAbilityEffects(playerId);
              }
            }
          }
        } else {
          this.scheduleAbilityUpdate(playerId);
          this.chainGrappleManager.applyAbilityEffects(playerId);
        }
      }
    }
  }

  @EventHandler
  public void onPluginDisable(PluginDisableEvent event) {
    if (event.getPlugin().getName().equalsIgnoreCase(Bliss.getInstance().getName())) {
      this.shutdown();
    }
  }

  public void shutdown() {
    Set<UUID> affectedIds = new HashSet<>();
    for (Player player : Bukkit.getOnlinePlayers()) {
      affectedIds.add(player.getUniqueId());
    }

    affectedIds.addAll(this.playerLocations.keySet());
    affectedIds.addAll(this.activePlayers);
    affectedIds.addAll(this.protectedPlayers);
    affectedIds.addAll(this.playerIds);
    affectedIds.addAll(this.affectedPlayers);
    affectedIds.addAll(this.lastUpdateTimes.keySet());
    affectedIds.addAll(this.primaryPlayerIds);
    affectedIds.addAll(this.currentValuesByPlayer.keySet());
    affectedIds.addAll(this.activeValuesByPlayer.keySet());
    affectedIds.addAll(this.cooldownTimestamps.keySet());
    affectedIds.addAll(this.pendingPlayers);
    affectedIds.addAll(this.grappleModesByPlayer.keySet());
    affectedIds.addAll(this.sourcePlayerIds);
    affectedIds.addAll(this.secondaryLocationsByPlayer.keySet());
    affectedIds.addAll(this.playerCounters.keySet());
    affectedIds.addAll(this.trackedTasksByPlayer.keySet());
    affectedIds.addAll(this.cachedPlayerIds);
    affectedIds.addAll(this.activePlayerIds);
    affectedIds.addAll(this.resolvedPlayerIds);
    affectedIds.addAll(this.pendingValuesByPlayer.keySet());
    affectedIds.addAll(this.targetValuesByPlayer.keySet());
    affectedIds.addAll(this.remainingTicks.keySet());
    affectedIds.addAll(this.pendingPlayerIds);
    affectedIds.addAll(this.chargeLevels.keySet());
    affectedIds.addAll(this.originLocations.keySet());
    affectedIds.addAll(targetPlayerIds);
    affectedIds.addAll(trackedPlayers);
    affectedIds.addAll(lastActivationTimes.keySet());
    affectedIds.addAll(expiryTimes.keySet());
    affectedIds.addAll(abilityStages.keySet());
    affectedIds.addAll(sourceLastUseTimes.keySet());
    affectedIds.addAll(targetLastUseTimes.keySet());
    affectedIds.addAll(primaryLastUseTimes.keySet());
    affectedIds.addAll(lastUseTimes.keySet());
    affectedIds.addAll(primaryFlagsByPlayer.keySet());
    affectedIds.addAll(animationSteps.keySet());
    affectedIds.addAll(startTimes.keySet());
    affectedIds.addAll(cachedValuesByPlayer.keySet());
    affectedIds.addAll(activeLastUseTimes.keySet());

    for (UUID affectedId : affectedIds) {
      this.updatePendingAbilityState(affectedId);
    }

    for (Map.Entry<UUID, BukkitRunnable> entry : new ArrayList<>(this.trackedTasksByPlayer.entrySet())) {
      entry.getValue().cancel();
      if (Bukkit.getEntity(entry.getKey()) instanceof LivingEntity livingEntity) {
        livingEntity.setGravity(true);
      }
    }
    this.trackedTasksByPlayer.clear();

    this.chainGrappleManager.shutdown();
    for (UUID displayId : this.trackedDisplays) {
      if (Bukkit.getEntity(displayId) instanceof ItemDisplay display) {
        display.remove();
      }
    }
    this.trackedDisplays.clear();
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (event.getEntity() instanceof Player targetPlayer) {
        if (this.cachedPlayerIds.contains(player.getUniqueId())) {
          ItemStack mainHandItem = targetPlayer.getInventory().getItemInMainHand();
          ItemStack offHandItem = targetPlayer.getInventory().getItemInOffHand();
          if (mainHandItem.getType() == Material.SHIELD) {
            targetPlayer.setCooldown(mainHandItem, 100);
          } else if (offHandItem.getType() == Material.SHIELD) {
            targetPlayer.setCooldown(offHandItem, 100);
          }
        }
      }
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (event.getCause() == DamageCause.FALL) {
        UUID playerId = player.getUniqueId();
        if (!this.isMatchingState(playerId) && !this.playerIds.contains(playerId)) {
          ItemStack mainHandItem = player.getInventory().getItemInMainHand();
          ItemStack offHandItem = player.getInventory().getItemInOffHand();
          boolean enabled = this.isGemItem(mainHandItem) || this.isGemItem(offHandItem);
          if (enabled) {
            double value = event.getFinalDamage();
            event.setDamage(value / 3.0);
          }
        } else {
          event.setCancelled(true);
          player.setFallDistance(0.0F);
        }
      }
    }
  }

  public static void resetAbilityState(Player player) {
    UUID playerId = player.getUniqueId();
    lastActivationTimes.remove(playerId);
    targetPlayerIds.remove(playerId);
    trackedPlayers.remove(playerId);
    expiryTimes.remove(playerId);
    abilityStages.put(playerId, 2);
    sourceLastUseTimes.put(playerId, System.currentTimeMillis());
    primaryLastUseTimes.remove(playerId);
    lastUseTimes.remove(playerId);
    startTimes.remove(playerId);
    targetLastUseTimes.remove(playerId);
    animationSteps.remove(playerId);
    primaryFlagsByPlayer.remove(playerId);
    cachedValuesByPlayer.remove(playerId);
    activeLastUseTimes.remove(playerId);
  }

  void trackAbilityState(Player player) {
    UUID playerId = player.getUniqueId();
    long now = System.currentTimeMillis();
    Long storedTimestamp = activeLastUseTimes.get(playerId);
    if (storedTimestamp == null || now - storedTimestamp > 500L || !cachedValuesByPlayer.containsKey(playerId)) {
      String text = "󏿿󏿿󏿿󏿿";
      String message = this.formatDisplayTextForTarget(player);
      String displayText = this.formatDisplayTextForPlayer(player);
      String formattedText = this.formatDisplayText(player);
      String labelColor = ChatColor.WHITE.toString();
      String nameColor = ChatColor.WHITE.toString();
      String normalizedText =
          "⊵" + text + text + message + labelColor + " | " + displayText + nameColor + " ⊴ " + formattedText;
      cachedValuesByPlayer.put(playerId, normalizedText);
      activeLastUseTimes.put(playerId, now);
    }

    player.sendActionBar(cachedValuesByPlayer.get(playerId));
    if (player.hasPotionEffect(PotionEffectType.WITHER)
        || player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
      player.removePotionEffect(PotionEffectType.WITHER);
      player.removePotionEffect(PotionEffectType.WEAKNESS);
    }

    if (player.isSneaking()) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, false, false, false));
      Objects.requireNonNull(player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE))
          .setBaseValue(5.0);
      Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE))
          .setBaseValue(8.0);
    } else {
      Objects.requireNonNull(player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE))
          .setBaseValue(3.0);
      Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE))
          .setBaseValue(4.5);
    }
  }

  String formatDisplayText(Player player) {
    UUID playerId = player.getUniqueId();
    if (targetPlayerIds.contains(playerId)) {
      return ChatColor.of("#7CFFB2") + "Parry";
    } else {
      long now = System.currentTimeMillis();
      Long storedTimestamp = lastActivationTimes.get(playerId);
      if (storedTimestamp != null && now < storedTimestamp) {
        long timestamp = storedTimestamp - now;
        long lastUpdateTime = (long) Math.ceil(timestamp / 1000.0);
        return this.resolveChatColor(lastUpdateTime, 10L).toString() + lastUpdateTime + "s";
      } else {
        return ChatColor.GREEN + "Ready";
      }
    }
  }

  String formatDisplayTextForPlayer(Player player) {
    UUID playerId = player.getUniqueId();
    long now = System.currentTimeMillis();
    Long storedTimestamp = lastUseTimes.get(playerId);
    if (storedTimestamp != null && now < storedTimestamp) {
      long timestamp = storedTimestamp - now;
      long lastUpdateTime = (long) Math.ceil(timestamp / 1000.0);
      long startTime = Math.max(1L, (long) Math.ceil(this.getExpiryTime() / 1000.0));
      return this.resolveChatColor(lastUpdateTime, startTime).toString() + lastUpdateTime + "s";
    } else {
      return ChatColor.GREEN + "Ready";
    }
  }

  String formatDisplayTextForTarget(Player player) {
    UUID playerId = player.getUniqueId();
    int count = abilityStages.computeIfAbsent(playerId, this::initializeCounterForTarget);
    if (count == this.getChainCharges()) {
      return count + " " + ChatColor.GREEN + "Ready";
    }

    long now = System.currentTimeMillis();
    Long storedTimestamp = sourceLastUseTimes.get(playerId);
    if (storedTimestamp != null) {
      long timestamp = now - storedTimestamp;
      long lastUpdateTime = this.getDurationMillis() - timestamp;
      long startTime = (long) Math.ceil(Math.max(1L, lastUpdateTime) / 1000.0);
      ChatColor chatColor;
      if (startTime >= 5L) {
        chatColor = ChatColor.RED;
      } else if (startTime == 4L) {
        chatColor = ChatColor.of("#FF8C00");
      } else if (startTime == 3L) {
        chatColor = ChatColor.of("#FFB900");
      } else if (startTime == 2L) {
        chatColor = ChatColor.of("#C0D900");
      } else {
        chatColor = ChatColor.GREEN;
      }

      return count + " " + chatColor + startTime + "s";
    } else {
      return count + " " + ChatColor.RED + "5s";
    }
  }

  ChatColor resolveChatColor(long timestamp, long lastUpdateTime) {
    double value = (double) timestamp / lastUpdateTime;
    if (value >= 0.85) {
      return ChatColor.DARK_RED;
    } else if (value >= 0.75) {
      return ChatColor.of("#A00000");
    } else if (value >= 0.65) {
      return ChatColor.of("#B52020");
    } else if (value >= 0.55) {
      return ChatColor.of("#D04000");
    } else if (value >= 0.45) {
      return ChatColor.of("#E05000");
    } else if (value >= 0.35) {
      return ChatColor.of("#F08000");
    } else if (value >= 0.2) {
      return ChatColor.of("#FFB900");
    } else if (value >= 0.12) {
      return ChatColor.of("#E0D900");
    } else {
      return value >= 0.06 ? ChatColor.of("#C0D900") : ChatColor.GREEN;
    }
  }

  boolean isActiveForPlayer(UUID playerId) {
    return this.pendingPlayerIds.contains(playerId) || this.resolvedPlayerIds.contains(playerId);
  }

  void refreshPlayerState(Player player) {
    UUID playerId = player.getUniqueId();
    this.resolvedPlayerIds.remove(playerId);
    this.activePlayerIds.remove(playerId);
    this.pendingValuesByPlayer.remove(playerId);
    this.targetValuesByPlayer.remove(playerId);
    Integer storedCount = this.remainingTicks.remove(playerId);
    if (storedCount != null) {
      Bukkit.getScheduler().cancelTask(storedCount);
    }

    this.pendingPlayerIds.add(playerId);
    this.chargeLevels.put(playerId, 0);
    this.originLocations.put(playerId, player.getLocation().clone());
  }

  void updatePlayerState(UUID playerId) {
    this.pendingPlayerIds.remove(playerId);
    this.chargeLevels.remove(playerId);
    this.originLocations.remove(playerId);
    this.resolvedPlayerIds.remove(playerId);
    this.activePlayerIds.remove(playerId);
    this.pendingValuesByPlayer.remove(playerId);
    this.targetValuesByPlayer.remove(playerId);
    Integer storedCount = this.remainingTicks.remove(playerId);
    if (storedCount != null) {
      Bukkit.getScheduler().cancelTask(storedCount);
    }
  }

  boolean hasRequiredState(Player player) {
    LivingEntity livingEntity = this.resolveLivingEntity(player);
    if (livingEntity == null) {
      return false;
    }

    UUID playerId = livingEntity.getUniqueId();
    BukkitRunnable runnable = this.trackedTasksByPlayer.remove(playerId);
    if (runnable != null) {
      runnable.cancel();
    }

    this.chainGrappleManager.applyAbilityEffects(player.getUniqueId());
    targetPlayerIds.remove(player.getUniqueId());
    trackedPlayers.remove(player.getUniqueId());
    lastActivationTimes.put(player.getUniqueId(), System.currentTimeMillis() + this.getConfiguredLong());
    Location location = player.getEyeLocation();
    Location targetLocation = livingEntity.getLocation().clone().add(0.0, livingEntity.getHeight() * 0.5, 0.0);
    Vector direction = targetLocation.toVector().subtract(location.toVector()).normalize();
    this.plugin.createDirectionalParticleTask(
        Particle.FIREWORK,
        location,
        direction,
        location.distance(targetLocation),
        0.2,
        0.35,
        8,
        1,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0F,
        0.0F,
        0.0F);
    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1.0F, 0.7F);
    livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.BLOCK_CHAIN_HIT, 1.0F, 0.75F);
    Location origin = this.getTargetLocationForPlayer(livingEntity.getLocation());
    Location center = origin.clone().subtract(0.0, Math.max(3.0, livingEntity.getHeight() + 1.0), 0.0);
    BukkitRunnable motionEffectTask = new BukkitRunnable() {
      final Location targetLocation = center;
      final Location anchorLocation = origin;
      int durationTicks = 0;
      final long lastUpdateTime = AuratusGem.this.getFlagPoleDuration();

      @Override
      public void run() {
        if (livingEntity.isValid() && !livingEntity.isDead() && this.lastUpdateTime > 0L) {
          if (this.durationTicks >= this.lastUpdateTime) {
            livingEntity.setGravity(true);
            livingEntity.setVelocity(new Vector(0.0, -0.05, 0.0));
            AuratusGem.this.trackedTasksByPlayer.remove(playerId);
            this.cancel();
          } else {
            livingEntity.setGravity(false);
            Location location = livingEntity.getLocation();
            double offset = Math.sin(this.durationTicks * 0.34) * 4.5;
            Location targetLocation = this.targetLocation.clone().add(0.0, offset, 0.0);
            Vector direction = targetLocation.toVector().subtract(location.toVector()).multiply(0.72);
            direction.setY(Math.max(-1.6, Math.min(2.8, direction.getY())));
            livingEntity.setVelocity(direction);
            if (this.durationTicks % 50 == 0) {
              AuratusGem.this.plugin.trackAbilityState(
                  Particle.FIREWORK, this.anchorLocation, 3.0, 3, 50, 1, 0.0, 0.0, 1.0, 0.0, 0.0F, 0.0F, 0.0F);
            }

            if (this.durationTicks % 8 == 0) {
              Vector velocity = targetLocation.toVector().subtract(this.anchorLocation.toVector()).normalize();
              AuratusGem.this.plugin.resetAbilityState(
                  Particle.FIREWORK,
                  this.anchorLocation,
                  velocity,
                  this.anchorLocation.distance(targetLocation),
                  0.2,
                  6,
                  0.1,
                  0.1,
                  0.1,
                  1,
                  0.0,
                  0.0,
                  0.0,
                  0.0,
                  0.0F,
                  0.0F,
                  0.0F);
              livingEntity
              .getWorld()
              .spawnParticle(
                  Particle.DUST,
                  livingEntity.getLocation().add(0.0, livingEntity.getHeight() * 0.5, 0.0),
                  12,
                  0.35,
                  0.45,
                  0.35,
                  0.0,
                  AuratusGem.secondaryDustOptions);
            }

            this.durationTicks++;
          }
        } else {
          livingEntity.setGravity(true);
          AuratusGem.this.trackedTasksByPlayer.remove(playerId);
          this.cancel();
        }
      }
    };
    this.trackedTasksByPlayer.put(playerId, motionEffectTask);
    motionEffectTask.runTaskTimer(this.plugin, 0L, 1L);
    return true;
  }

  LivingEntity resolveLivingEntity(Player player) {
    Location location = player.getEyeLocation();
    Vector direction = location.getDirection().normalize();
    LivingEntity livingEntity = null;
    double value = Double.MAX_VALUE;

    for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 10.0, 8.0, 10.0)) {
      if (entity instanceof LivingEntity targetEntity
          && !targetEntity.equals(player)
          && !this.canUseAbility(player, targetEntity)
          && !(targetEntity instanceof Player targetPlayer
              && (targetPlayer.getGameMode() == GameMode.CREATIVE
                  || targetPlayer.getGameMode() == GameMode.SPECTATOR))) {
        Vector offset =
            targetEntity.getLocation()
                .add(0.0, targetEntity.getHeight() * 0.5, 0.0)
                .toVector()
                .subtract(location.toVector());
        double distance = offset.length();
        if (!(distance <= 0.1)) {
          double radius = direction.dot(offset.clone().normalize());
          if (!(radius < 0.1)) {
            double angle = distance - radius * 4.0;
            if (angle < value) {
              livingEntity = targetEntity;
              value = angle;
            }
          }
        }
      }
    }

    return livingEntity;
  }

  @EventHandler
  public void finishAbilityAction(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
      if (event.getHand() == EquipmentSlot.HAND) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (this.isGemItem(mainHandItem)) {
          if (!this.plugin.isMatchingState(playerId) || targetPlayerIds.contains(playerId)) {
            if (this.isAbilityBlocked(playerId)) {
              event.setCancelled(true);
            } else if (!this.playerIds.contains(playerId)) {
              if (this.isAbilityAllowed(playerId)) {
                event.setCancelled(true);
              } else if (!this.plugin.isGemsDisabled()
                  && !this.plugin.isAbilityAllowed(player.getWorld())
                  && !this.plugin.canUseAbility(player)) {
                event.setCancelled(true);
                UUID targetId = player.getUniqueId();
                long now = System.currentTimeMillis();
                this.updateAbilityState(targetId, now);
                int count = abilityStages.computeIfAbsent(targetId, this::initializeCounterForPlayer);
                if (count > 0) {
                  if (targetPlayerIds.contains(targetId)) {
                    if (this.hasRequiredState(player)) {
                      abilityStages.put(targetId, --count);
                      cachedValuesByPlayer.remove(targetId);
                      if (count == this.getChainCharges() - 1) {
                        sourceLastUseTimes.put(targetId, now);
                      }
                    }
                  } else {
                    boolean enabled = this.isProtectedTarget(player);
                    if (this.protectedPlayers.contains(playerId)) {
                      Location location = this.playerLocations.get(playerId);
                      if (location == null) {
                        this.updateTargetAbilityState(playerId);
                      } else {
                        this.protectedPlayers.remove(playerId);
                        this.affectedPlayers.add(playerId);
                        this.updatePrimaryAbilityState(playerId);
                        this.chainGrappleManager.applyAbilityEffects(targetId);
                        this.chainGrappleManager.activateAbility(player);
                        if (enabled) {
                          this.cleanupAbilityState(player, enabled);
                          this.activatePendingAbility(player, location);
                        } else {
                          this.updateTargetAbilityState(targetId);
                        }

                        abilityStages.put(targetId, --count);
                        cachedValuesByPlayer.remove(targetId);
                        if (count == this.getChainCharges() - 1) {
                          sourceLastUseTimes.put(targetId, now);
                        }
                      }
                    } else {
                      this.chainGrappleManager.applyAbilityEffects(targetId);
                      this.chainGrappleManager.activateAbility(player);
                      if (enabled) {
                        this.cleanupAbilityState(player, enabled);
                      }

                      abilityStages.put(targetId, --count);
                      cachedValuesByPlayer.remove(targetId);
                      if (count == this.getChainCharges() - 1) {
                        sourceLastUseTimes.put(targetId, now);
                      }

                      Long storedTimestamp = targetLastUseTimes.get(targetId);
                      int index = animationSteps.getOrDefault(targetId, 0);
                      if (storedTimestamp != null && now - storedTimestamp <= 1000L) {
                        animationSteps.put(targetId, ++index);
                        if (index >= 2) {
                          Long targetLong = primaryLastUseTimes.get(targetId);
                          if (targetLong == null || now >= targetLong) {
                            primaryFlagsByPlayer.put(targetId, true);
                            primaryLastUseTimes.put(targetId, now + this.getLastUseTime());
                            cachedValuesByPlayer.remove(targetId);
                          }

                          targetLastUseTimes.remove(targetId);
                          animationSteps.put(targetId, 0);
                        }
                      } else {
                        targetLastUseTimes.put(targetId, now);
                        animationSteps.put(targetId, 1);
                      }
                    }
                  }
                }
              } else {
                event.setCancelled(true);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    if (this.isPrimaryGemItem(mainHandItem)) {
      if (mainHandItem.getItemMeta().hasUseCooldown()) {
        event.setCancelled(true);
      } else if (!this.plugin.isMatchingState(playerId)) {
        event.setCancelled(true);
        player.swingHand(EquipmentSlot.HAND);
        player.setCooldown(mainHandItem, 20);
        if (this.isAbilityAllowed(playerId)) {
          event.setCancelled(true);
        } else {
          event.setCancelled(true);
          if (!this.playerIds.contains(playerId)) {
            UUID targetId = player.getUniqueId();
            long now = System.currentTimeMillis();
            this.updateAbilityState(targetId, now);
            int count = abilityStages.computeIfAbsent(targetId, this::initializeCounter);
            if (count > 0) {
              this.playAbilityEffects(player, GRAPPLE_HOOK, true);
              this.chainGrappleManager.applyAbilityEffects(targetId);
              this.chainGrappleManager.updateAbilityState(player, false, false);
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPrimaryPlayerInteract(PlayerInteractEvent event) {
    if (event.getHand() == EquipmentSlot.HAND) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR
          || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (this.isGemItem(mainHandItem)) {
          if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
          }

          if (!this.isAbilityAllowed(player.getUniqueId())
              && !this.plugin.isGemsDisabled()
              && !this.plugin.isAbilityAllowed(player.getWorld())) {
            if (player.isSneaking()) {
              this.activatePrimaryAbility(player);
            } else {
              this.activateTargetAbility(player);
            }
          }
        }
      }
    }
  }

  void activatePrimaryAbility(Player player) {
    UUID playerId = player.getUniqueId();
    long now = System.currentTimeMillis();
    long timestamp = lastUseTimes.getOrDefault(playerId, 0L);
    if (now >= timestamp) {
      this.updateTargetAbilityState(playerId);
      this.chainGrappleManager.applyAbilityEffects(playerId);
      if (this.canAffectTarget(player)) {
        this.playAbilityEffects(player, MIDDLE, true);
        lastUseTimes.put(playerId, now + this.getExpiryTime());
        cachedValuesByPlayer.remove(playerId);
      }
    }
  }

  void activateTargetAbility(Player player) {
    this.playAbilitySound(player);
    cachedValuesByPlayer.remove(player.getUniqueId());
  }

  void playAbilitySound(Player player) {
    UUID playerId = player.getUniqueId();
    if (!targetPlayerIds.contains(playerId)) {
      if (this.isAbilityActive(playerId)) {
        long now = Math.max(0L, (lastActivationTimes.get(playerId) - System.currentTimeMillis()) / 1000L);
      } else {
        Location location = player.getEyeLocation().clone();
        Vector direction = location.getDirection().normalize();
        player.swingHand(EquipmentSlot.HAND);
        this.plugin.handleAbilityAction(playerId, 1);
        targetPlayerIds.add(playerId);
        this.applyAbilityEffects(playerId);
        trackedPlayers.remove(playerId);
        cachedValuesByPlayer.remove(playerId);
        Location targetLocation = location.clone().add(direction.clone().multiply(0.6));
        float scale = location.getYaw();
        float size = location.getPitch();
        ItemDisplay display = (ItemDisplay) targetLocation.getWorld().spawnEntity(targetLocation, EntityType.ITEM_DISPLAY);
        this.trackDisplay(display);
        ItemStack item = new ItemStack(Material.ECHO_SHARD);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setCustomModelData(12346);
        item.setItemMeta(itemMeta);
        display.setItemStack(item);
        Transformation transformation = display.getTransformation();
        transformation.getScale().set(2.0F, 2.0F, 2.0F);
        transformation.getLeftRotation().rotationZ((float) Math.toRadians(90.0));
        transformation.getLeftRotation().rotationY((float) Math.toRadians(90.0));
        transformation.getLeftRotation().rotationX((float) Math.toRadians(90.0));
        display.setTransformation(transformation);
        display.setRotation(scale, size);
        this.plugin.resetAbilityState(
            Particle.FIREWORK,
            targetLocation,
            direction,
            10.0,
            5.0,
            10,
            2.0,
            2.0,
            2.0,
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0F,
            0.0F,
            0.0F);
        targetLocation.getWorld().playSound(targetLocation, "bliss.lowpitch", 1.0F, 1.0F);
        byte step = 20;
        double value = 5.0;
        Location origin = targetLocation.clone().add(direction.clone().multiply(5.0));
        int[] count = new int[] {0};
        Bukkit.getScheduler()
            .runTaskTimer(
                this.plugin,
                task ->
                    this.updateParryAbility(display, player, playerId, count, origin, direction, location, task),
                0L,
                1L);
        Bukkit.getScheduler()
            .runTaskLater(this.plugin, () -> this.finishParryWindow(playerId), this.getParryWindowSeconds());
      }
    }
  }

  @EventHandler
  public void onPrimaryEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (targetPlayerIds.contains(playerId)) {
        LivingEntity livingEntity = null;
        if (event.getDamager() instanceof LivingEntity targetEntity) {
          livingEntity = targetEntity;
        } else if (event.getDamager() instanceof Projectile projectile
            && projectile.getShooter() instanceof LivingEntity attacker) {
          livingEntity = attacker;
        }

        if (livingEntity != null) {
          if (!livingEntity.equals(player)) {
            if (!(livingEntity instanceof Player targetPlayer && this.canUseAbility(player, targetPlayer))) {
              double value = event.getFinalDamage();
              event.setCancelled(true);
              targetPlayerIds.remove(playerId);
              trackedPlayers.add(playerId);
              this.applyAbilityEffects(playerId);
              cachedValuesByPlayer.remove(playerId);
              livingEntity.damage(value, DamageSource.builder(DamageType.SONIC_BOOM).build());
              player.addPotionEffect(
                  new PotionEffect(PotionEffectType.SPEED, 100, 2, false, false, false));
              player.addPotionEffect(
                  new PotionEffect(PotionEffectType.WEAVING, 100, 0, false, false, false));
              if (livingEntity instanceof Player sourcePlayer) {
                this.activateAbility(sourcePlayer, this.getDurationTicks());
                sourcePlayer
                    .getWorld()
                    .playSound(sourcePlayer.getLocation(), Sound.ITEM_WOLF_ARMOR_CRACK, 1.0F, 1.0F);
              } else {
                livingEntity.getWorld()
                    .playSound(livingEntity.getLocation(), Sound.ITEM_WOLF_ARMOR_CRACK, 1.0F, 1.0F);
              }

              this.plugin.spawnAbilityParticles(player.getLocation(), Color.WHITE);
            }
          }
        }
      }
    }
  }

  boolean shouldApplyEffect(UUID playerId) {
    return this.activePlayers.contains(playerId)
        || this.protectedPlayers.contains(playerId)
        || this.affectedPlayers.contains(playerId)
        || this.playerIds.contains(playerId);
  }

  void updatePrimaryAbilityState(UUID playerId) {
    this.lastUpdateTimes.put(playerId, System.currentTimeMillis());
  }

  void updateTargetAbilityState(UUID playerId) {
    this.activePlayers.remove(playerId);
    this.protectedPlayers.remove(playerId);
    this.affectedPlayers.remove(playerId);
    this.playerIds.remove(playerId);
    this.playerLocations.remove(playerId);
    this.lastUpdateTimes.remove(playerId);
    this.updateActiveAbilityState(playerId);
    this.scheduleAbilityUpdate(playerId);
  }

  boolean canAffectTarget(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.shouldApplyEffect(playerId)) {
      return false;
    }

    Location location = player.getEyeLocation().clone();
    Vector direction = location.getDirection().normalize();
    player.swingHand(EquipmentSlot.HAND);
    this.plugin.handleAbilityAction(playerId, 1);
    Location targetLocation = location.clone().add(direction.clone().multiply(0.6));
    this.plugin.resetAbilityState(
        Particle.FIREWORK,
        targetLocation,
        direction,
        10.0,
        5.0,
        10,
        2.0,
        2.0,
        2.0,
        1,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0F,
        0.0F,
        0.0F);
    float scale = location.getYaw();
    float size = location.getPitch();
    ItemDisplay display = (ItemDisplay) targetLocation.getWorld().spawnEntity(targetLocation, EntityType.ITEM_DISPLAY);
    this.trackDisplay(display);
    ItemStack item = new ItemStack(Material.ECHO_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setCustomModelData(12346);
    item.setItemMeta(itemMeta);
    display.setItemStack(item);
    display.setBillboard(Billboard.FIXED);
    Transformation transformation = display.getTransformation();
    transformation.getScale().set(2.0F, 2.0F, 2.0F);
    transformation.getLeftRotation().rotationZ((float) Math.toRadians(90.0));
    transformation.getLeftRotation().rotationY((float) Math.toRadians(90.0));
    transformation.getLeftRotation().rotationX((float) Math.toRadians(90.0));
    display.setTransformation(transformation);
    display.setRotation(scale, size);
    targetLocation.getWorld().playSound(targetLocation, "bliss.normal", 1.0F, 1.0F);
    targetLocation.getWorld().playSound(targetLocation, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 1.0F);
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> spawnSkyAbilityEffects(targetLocation), 20L);

    for (Entity entity : player.getNearbyEntities(5.0, 5.0, 5.0)) {
      if (entity instanceof LivingEntity livingEntity
          && !entity.equals(player)
          && !this.canUseAbility(player, entity)) {
        livingEntity.damage(2.0, DamageSource.builder(DamageType.SONIC_BOOM).build());
        livingEntity
            .getLocation()
            .getWorld()
            .playSound(livingEntity.getLocation(), Sound.ITEM_WOLF_ARMOR_CRACK, 1.0F, 1.0F);
        this.plugin.spawnAbilityParticles(livingEntity.getLocation(), Color.WHITE);
      }
    }

    byte step = 20;
    double value = 5.0;
    Location origin = targetLocation.clone().add(direction.clone().multiply(5.0));
    int[] count = new int[] {0};
    Location[] center = new Location[] {null};
    BukkitTask[] task = new BukkitTask[1];
    task[0] =
        Bukkit.getScheduler()
            .runTaskTimer(
                this.plugin,
                () ->
                    this.updateSkyAbility(
                        display, player, task, playerId, count, targetLocation, direction, center, origin),
                0L,
                1L);
    return true;
  }

  void activateSourceAbility(Player player, Location location) {
    UUID playerId = player.getUniqueId();
    this.activePlayers.add(playerId);
    this.playerLocations.put(playerId, location.clone());
    this.updatePrimaryAbilityState(playerId);
    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;

      @Override
      public void run() {
        if (!player.isOnline() || player.isDead()) {
          AuratusGem.this.updateTargetAbilityState(playerId);
          this.cancel();
        } else if (AuratusGem.this.isAbilityAllowed(playerId)) {
          AuratusGem.this.updateTargetAbilityState(playerId);
          this.cancel();
        } else if (!AuratusGem.this.plugin.isGemsDisabled()
            && !AuratusGem.this.plugin.isAbilityAllowed(player.getWorld())
            && !AuratusGem.this.plugin.canUseAbility(player)) {
          Location location = player.getLocation().clone().add(0.0, 1.0, 0.0);
          Location targetLocation = this.anchorLocation.clone().add(0.0, -1.25, 0.0);
          double distance = location.distance(targetLocation);
          boolean enabled = distance <= 2.2;
          boolean active = AuratusGem.this.chainGrappleManager.isAbilityBlocked(playerId);
          if (enabled && active) {
            AuratusGem.this.activePlayers.remove(playerId);
            AuratusGem.this.chainGrappleManager.applyAbilityEffects(playerId);
            AuratusGem.this.activateActiveAbility(player, this.anchorLocation);
            this.cancel();
          } else {
            this.durationTicks++;
            if (this.durationTicks > 80) {
              AuratusGem.this.updateTargetAbilityState(playerId);
              this.cancel();
            }
          }
        } else {
          AuratusGem.this.updateTargetAbilityState(playerId);
          this.cancel();
        }
      }
    }.runTaskTimer(this.plugin, 0L, 1L);
  }

  void activateActiveAbility(Player player, Location location) {
    UUID playerId = player.getUniqueId();
    this.protectedPlayers.add(playerId);
    this.playerLocations.put(playerId, location.clone());
    this.updatePrimaryAbilityState(playerId);
    new BukkitRunnable() {
      int durationTicks = 0;

      @Override
      public void run() {
        if (!player.isOnline() || player.isDead()) {
          AuratusGem.this.updateTargetAbilityState(playerId);
          this.cancel();
        } else if (AuratusGem.this.isAbilityAllowed(playerId)) {
          AuratusGem.this.updateTargetAbilityState(playerId);
          this.cancel();
        } else if (!AuratusGem.this.plugin.isGemsDisabled()
            && !AuratusGem.this.plugin.isAbilityAllowed(player.getWorld())
            && !AuratusGem.this.plugin.canUseAbility(player)) {
          player.setFallDistance(0.0F);
          this.durationTicks++;
          if (this.durationTicks > 60) {
            AuratusGem.this.updateTargetAbilityState(playerId);
            this.cancel();
          }
        } else {
          AuratusGem.this.updateTargetAbilityState(playerId);
          this.cancel();
        }
      }
    }.runTaskTimer(this.plugin, 0L, 1L);
  }

  void activatePendingAbility(Player player, Location location) {
    UUID playerId = player.getUniqueId();
    this.playerIds.add(playerId);
    this.updatePrimaryAbilityState(playerId);
    new BukkitRunnable() {
      int durationTicks = 0;

      @Override
      public void run() {
        if (!player.isOnline() || player.isDead()) {
          AuratusGem.this.updateTargetAbilityState(playerId);
          this.cancel();
        } else if (AuratusGem.this.isAbilityAllowed(playerId)) {
          AuratusGem.this.updateTargetAbilityState(playerId);
          this.cancel();
        } else if (!AuratusGem.this.plugin.isGemsDisabled()
            && !AuratusGem.this.plugin.isAbilityAllowed(player.getWorld())
            && !AuratusGem.this.plugin.canUseAbility(player)) {
          Location location = player.getLocation();
          boolean block =
          player.isOnGround()
          || location.clone().subtract(0.0, 0.15, 0.0).getBlock().getType().isSolid()
          || location.clone().subtract(0.0, 0.6, 0.0).getBlock().getType().isSolid();
          if (block) {
            AuratusGem.this.updateTargetAbilityState(playerId);
            this.cancel();
          } else {
            this.durationTicks++;
            if (this.durationTicks > 120) {
              AuratusGem.this.updateTargetAbilityState(playerId);
              this.cancel();
            }
          }
        } else {
          AuratusGem.this.updateTargetAbilityState(playerId);
          this.cancel();
        }
      }
    }.runTaskTimer(this.plugin, 0L, 1L);
  }

  boolean isLocationBlocked(Player player, Location location) {
    for (Entity entity :
        player.getWorld()
            .getNearbyEntities(
                location, this.getGroundSlamMaxRadius(), this.getGroundSlamMaxRadius(), this.getGroundSlamMaxRadius())) {
      if (entity instanceof LivingEntity livingEntity
          && !livingEntity.equals(player)
          && !this.canUseAbility(player, livingEntity)
          && (!(livingEntity instanceof Player targetPlayer)
              || targetPlayer.getGameMode() != GameMode.CREATIVE
                  && targetPlayer.getGameMode() != GameMode.SPECTATOR)) {
        return true;
      }
    }

    return false;
  }

  boolean isTrackedTarget(Player player, Location location) {
    return player.isSneaking()
        && player.getVelocity().getY() <= -this.getActiveAbilityDoubleValue()
        && this.isPrimaryLocationBlocked(location, this.getHopNearGroundDistance());
  }

  boolean isPrimaryLocationBlocked(Location location, double value) {
    World world = location.getWorld();
    if (world == null) {
      return false;
    }

    for (double distance = 0.1; distance <= value; distance += 0.25) {
      if (location.clone().subtract(0.0, distance, 0.0).getBlock().getType().isSolid()) {
        return true;
      }
    }

    return false;
  }

  void updateSourceAbilityState(UUID playerId, int count) {
    if (count > 0) {
      int index = this.getChainCharges();
      int remaining = abilityStages.getOrDefault(playerId, index);
      int step = Math.min(index, remaining + count);
      abilityStages.put(playerId, step);
      if (step >= index) {
        sourceLastUseTimes.put(playerId, System.currentTimeMillis());
      }

      cachedValuesByPlayer.remove(playerId);
    }
  }

  void spawnAbilityParticles(Player player) {
    UUID playerId = player.getUniqueId();
    this.chainGrappleManager.applyAbilityEffects(playerId);
    Vector lookDirection = player.getLocation().getDirection().normalize();
    Vector direction = lookDirection.clone();
    direction.setY(0);
    if (direction.lengthSquared() < 0.001) {
      direction = player.getVelocity().clone();
      direction.setY(0);
    }

    if (direction.lengthSquared() < 0.001) {
      direction = new Vector(0, 0, 1);
    } else {
      direction.normalize();
    }

    Vector offset = direction.multiply(this.getPendingAbilityDoubleValue());
    double y = lookDirection.getY() > this.getResolvedAbilityDoubleValue() ? 0.25 : this.getCachedAbilityDoubleValue();
    offset.setY(y);
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> applyMissedSlamBoost(player, offset), 1L);
    player.getWorld()
        .spawnParticle(
            Particle.DUST,
            player.getEyeLocation(),
            900,
            2.5,
            0.25,
            2.5,
            0.0,
            new DustOptions(Color.GRAY, 2.0F));
    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.5F);
  }

  void spawnPrimaryAbilityParticles(Player player, Location location, double value) {
    double distance = this.getPreviousAbilityDoubleValue(value);
    double radius = this.getEffectRadius(this.getVerticalOffset(), this.getHorizontalOffset(), distance);
    double angle = this.getEffectRadius(this.getAnimationProgress(), this.getGroundSlamMaxRadius(), distance);
    double progress = this.getEffectRadius(0.8, 1.45, distance);
    double scale = this.getEffectRadius(0.45, 0.8, distance);
    World world = location.getWorld();
    if (world != null) {
      world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.2F, 0.65F);
      world.spawnParticle(
          Particle.EXPLOSION, location.clone().add(0.0, 1.0, 0.0), 2, 0.2, 0.1, 0.2, 0.0);
      world.spawnParticle(
          Particle.DUST,
          location.clone().add(0.0, 1.0, 0.0),
          (int) this.getEffectRadius(24.0, 40.0, distance),
          0.9 + distance * 0.8,
          0.16,
          0.9 + distance * 0.8,
          0.0,
          activeDustOptions);
      world.spawnParticle(
          Particle.DUST,
          location.clone().add(0.0, 1.0, 0.0),
          (int) this.getEffectRadius(30.0, 50.0, distance),
          1.2 + distance * 0.9,
          0.2,
          1.2 + distance * 0.9,
          0.0,
          dustOptions);
      this.updateState(location);

      for (Entity entity : world.getNearbyEntities(location, angle, Math.max(3.0, angle * 0.75), angle)) {
        if (entity instanceof LivingEntity livingEntity
            && !livingEntity.equals(player)
            && !this.canUseAbility(player, livingEntity)) {
          if (livingEntity instanceof Player targetPlayer) {
            if (targetPlayer.getGameMode() == GameMode.CREATIVE
                || targetPlayer.getGameMode() == GameMode.SPECTATOR) {
              continue;
            }

            targetPlayer.setCooldown(Material.WIND_CHARGE, this.getChargeLevel());
          }

          livingEntity.damage(radius, DamageSource.builder(DamageType.SONIC_BOOM).build());
          Vector offset = livingEntity.getLocation().toVector().subtract(location.toVector());
          if (offset.lengthSquared() < 0.001) {
            offset = new Vector(0, 0, 0);
          } else {
            offset.normalize().multiply(progress);
          }

          offset.setY(scale);
          livingEntity.setVelocity(offset);
          livingEntity
              .getWorld()
              .spawnParticle(
                  Particle.DUST,
                  livingEntity.getLocation().add(0.0, 1.0, 0.0),
                  (int) this.getEffectRadius(18.0, 30.0, distance),
                  0.65,
                  0.12,
                  0.65,
                  0.0,
                  secondaryDustOptions);
        }
      }
    }
  }

  void updateState(Location location) {
    World world = location.getWorld();
    if (world != null) {
      Location targetLocation = location.clone().getBlock().getLocation();
      Location origin = targetLocation.add(0.5, 2.01, 0.5);
      Vector offset = new Vector(0, -1, 0);
      this.plugin.resetAbilityState(
          Particle.FIREWORK,
          origin,
          offset,
          12.0,
          2.0,
          12,
          0.15,
          1.8,
          0.15,
          1,
          0.0,
          0.0,
          0.0,
          0.0,
          0.0F,
          0.0F,
          0.0F);
      ItemDisplay itemDisplay = (ItemDisplay) world.spawnEntity(origin, EntityType.ITEM_DISPLAY);
      this.trackDisplay(itemDisplay);
      ItemStack item = new ItemStack(Material.ECHO_SHARD);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.setCustomModelData(12345);
      item.setItemMeta(itemMeta);
      itemDisplay.setItemStack(item);
      itemDisplay.setBillboard(Billboard.FIXED);
      itemDisplay.setBrightness(new Brightness(15, 15));
      itemDisplay.setInterpolationDuration(1);
      itemDisplay.setTeleportDuration(1);
      itemDisplay.setInterpolationDelay(0);
      itemDisplay.setRotation(0.0F, 0.0F);
      Transformation transformation = itemDisplay.getTransformation();
      transformation.getScale().set(5.0F, 0.08F, 5.0F);
      transformation.getLeftRotation().rotationX((float) Math.toRadians(90.0));
      transformation.getLeftRotation().rotationY(0.0F);
      transformation.getLeftRotation().rotationZ(0.0F);
      transformation.getRightRotation().identity();
      transformation.getTranslation().set(0.0F, 0.0F, 0.0F);
      itemDisplay.setTransformation(transformation);
      new BukkitRunnable() {
        int durationTicks = 0;

        @Override
        public void run() {
          if (!itemDisplay.isValid()) {
            AuratusGem.this.trackedDisplays.remove(itemDisplay.getUniqueId());
            this.cancel();
          } else {
            this.durationTicks++;
            if (this.durationTicks <= 10) {
              float scale = this.durationTicks / 10.0F;
              float size = 5.0F + 7.0F * scale;
              Transformation transformation = itemDisplay.getTransformation();
              transformation.getScale().set(size, 0.08F, size);
              transformation.getLeftRotation().rotationX((float) Math.toRadians(90.0));
              transformation.getLeftRotation().rotationY(0.0F);
              transformation.getLeftRotation().rotationZ(0.0F);
              transformation.getRightRotation().identity();
              transformation.getTranslation().set(0.0F, 0.0F, 0.0F);
              itemDisplay.setInterpolationDuration(1);
              itemDisplay.setTeleportDuration(1);
              itemDisplay.setInterpolationDelay(0);
              itemDisplay.setTransformation(transformation);
            } else if (this.durationTicks > 20) {
              AuratusGem.this.removeTrackedDisplay(itemDisplay);
              this.cancel();
            }
          }
        }
      }.runTaskTimer(this.plugin, 0L, 1L);
    }
  }

  Location getTargetLocation(Location location, UUID playerId) {
    return this.getTargetLocationForTarget(
        location, this.getSkyHookHeight(), this.getRemainingTicks(), this.getPendingAbilityLongValue(), playerId);
  }

  Location getTargetLocationForPlayer(Location location) {
    return this.getTargetLocationForTarget(
        location, this.getFlagPoleHookHeight(), this.getEffectCount(), this.getCachedAbilityLongValue(), null);
  }

  Location getTargetLocationForTarget(
      Location location, double value, int count, long timestamp, UUID playerId) {
    World world = location.getWorld();
    if (world == null) {
      return location.clone();
    }

    if (playerId != null) {
      this.updateActiveAbilityState(playerId);
    }

    Vector offset = new Vector(0, 1, 0);
    Location targetLocation = location.clone().add(0.0, value, 0.0);
    this.plugin.spawnAbilityParticles(targetLocation, Color.WHITE);
    ArrayList<Location> entries = new ArrayList<>();

    for (int index = -count; index <= count; index++) {
      for (int remaining = -count; remaining <= count; remaining++) {
        if (index * index + remaining * remaining <= count * count) {
          Location origin = targetLocation.clone().add(index, 0.0, remaining);
          world.getBlockAt(origin).setType(Material.BARRIER);
          entries.add(origin);
        }
      }
    }

    if (playerId != null) {
      this.secondaryLocationsByPlayer.put(playerId, entries);
    }

    int scheduledTask =
        Bukkit.getScheduler()
            .runTaskLater(this.plugin, () -> this.cleanupHookPoints(entries, playerId), timestamp)
            .getTaskId();
    if (playerId != null) {
      this.playerCounters.put(playerId, scheduledTask);
    }

    this.plugin.createDirectionalParticleTask(
        Particle.FIREWORK, location, offset, value, 0.3, 1.0, 20, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
    this.plugin.createActiveParticleAnimationTask(
        Particle.FIREWORK, location, targetLocation, 20, 1, 0.0, 0.0, 0.0, 0.0);
    this.plugin.createCleanupParticleOrbitTask(
        Particle.FIREWORK, targetLocation, 1.5, 5.0, 3, 20, 1, 0.0, 0.0, 0.0, 0.0);
    this.plugin.trackAbilityState(
        Particle.FIREWORK, targetLocation, 3.0, 3, 50, 1, 0.0, 0.0, 1.0, 0.0, 0.0F, 0.0F, 0.0F);
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Vector direction = offset;
      final double effectRadius = value;
      @Override
      public void run() {
        AuratusGem.this.plugin.createDirectionalParticleTask(
            Particle.FIREWORK,
            this.anchorLocation,
            this.direction,
            this.effectRadius,
            0.3,
            1.0,
            20,
            1,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0F,
            0.0F,
            0.0F);
        AuratusGem.this.plugin.createActiveParticleAnimationTask(
            Particle.FIREWORK, this.anchorLocation, targetLocation, 20, 1, 0.0, 0.0, 0.0, 0.0);
        AuratusGem.this.plugin.createCleanupParticleOrbitTask(
            Particle.FIREWORK, targetLocation, 1.5, 5.0, 3, 20, 1, 0.0, 0.0, 0.0, 0.0);
        AuratusGem.this.plugin.trackAbilityState(
            Particle.FIREWORK, targetLocation, 3.0, 3, 50, 1, 0.0, 0.0, 1.0, 0.0, 0.0F, 0.0F, 0.0F);
      }
    }.runTaskLater(this.plugin, 50L);
    return targetLocation;
  }

  void updateActiveAbilityState(UUID playerId) {
    Integer storedCount = this.playerCounters.remove(playerId);
    if (storedCount != null) {
      Bukkit.getScheduler().cancelTask(storedCount);
    }

    List<Location> entries = this.secondaryLocationsByPlayer.remove(playerId);
    if (entries != null) {
      this.updatePrimaryState(entries);
    }
  }

  void updatePrimaryState(Collection<Location> collection) {
    for (Location location : collection) {
      World world = location.getWorld();
      if (world != null && world.getBlockAt(location).getType() == Material.BARRIER) {
        world.getBlockAt(location).setType(Material.AIR);
      }
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    Player player = event.getPlayer();
    if (event.getItemDrop().getItemStack().isSimilar(createGemItem())
        && !player.isOp()) {
      event.setCancelled(true);
    }
  }

  void updatePendingAbilityState(UUID playerId) {
    BukkitRunnable runnable = this.trackedTasksByPlayer.remove(playerId);
    if (runnable != null) {
      runnable.cancel();
      if (Bukkit.getEntity(playerId) instanceof LivingEntity livingEntity) {
        livingEntity.setGravity(true);
      }
    }

    this.updatePlayerState(playerId);
    this.updateTargetAbilityState(playerId);
    this.chainGrappleManager.applyAbilityEffects(playerId);
    targetPlayerIds.remove(playerId);
    trackedPlayers.remove(playerId);
    lastActivationTimes.remove(playerId);
    expiryTimes.remove(playerId);
    abilityStages.remove(playerId);
    sourceLastUseTimes.remove(playerId);
    targetLastUseTimes.remove(playerId);
    this.targetLocations.remove(playerId);
    primaryLastUseTimes.remove(playerId);
    lastUseTimes.remove(playerId);
    primaryFlagsByPlayer.remove(playerId);
    animationSteps.remove(playerId);
    startTimes.remove(playerId);
    cachedValuesByPlayer.remove(playerId);
    activeLastUseTimes.remove(playerId);
    this.cachedPlayerIds.remove(playerId);
  }

  public static ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.REPEATING_COMMAND_BLOCK);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setDisplayName(
        ChatColor.of("#FAF9C6")
            + ChatColor.BOLD.toString()
            + "ᴀᴜʀᴀᴛᴜѕ "
            + ChatColor.of("#FFD773")
            + "ɢᴇᴍ");
    ArrayList<String> lore = new ArrayList<>();
    lore.add(
        ChatColor.WHITE
            + ChatColor.BOLD.toString()
            + "ᴍᴀʏ ᴛʜᴇ"
            + " ʜᴇᴀʀᴛʏʀᴇᴅ ʙᴇ"
            + " ɴᴏᴛ ᴀꜰʀᴀɪᴅ");
    lore.add(ChatColor.WHITE + "(" + ChatColor.of("#8B65DD") + "Mythic" + ChatColor.WHITE + ")");
    lore.add(" ");
    lore.add(
        ChatColor.of("#FAF9C6")
            + "🔮 "
            + ChatColor.of("#FFD773")
            + "ᴘᴀѕѕɪᴠᴇѕ");
    lore.add(ChatColor.GRAY + "- Divine Purity");
    lore.add(ChatColor.GRAY + "- Angles Grasp");
    lore.add(ChatColor.GRAY + "- Feathered Fall");
    lore.add(ChatColor.GRAY + "- Hauling Strike");
    lore.add(" ");
    lore.add(
        ChatColor.of("#FAF9C6")
            + "🔮 "
            + ChatColor.of("#B8FFFB")
            + "ᴘᴏᴡᴇʀѕ");
    lore.add(
        ChatColor.GRAY
            + "- §f⊳"
            + ChatColor.of("#C5AE30")
            + " ᴠᴇɴᴇʀᴀᴛᴇᴅ"
            + " ᴘᴇʀꜰᴏʀᴀᴛᴏʀѕ");
    lore.add(" ");
    lore.add(
        ChatColor.GRAY
            + "- §f⊲"
            + ChatColor.of("#DFE251")
            + " ᴇᴄʜᴏɪɴɢ ᴀᴇɢɪѕ");
    itemMeta.setLore(lore);
    itemMeta.setCustomModelData(101);
    itemMeta.addEnchant(Enchantment.MENDING, 1, true);
    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(itemMeta);
    return item;
  }

  boolean isGemItem(ItemStack item) {
    return getPrimaryAbilityIntValue(item, Material.REPEATING_COMMAND_BLOCK) == 101;
  }

  boolean isPrimaryGemItem(ItemStack item) {
    return this.isGemItem(item);
  }

  public static int getPrimaryAbilityIntValue(ItemStack item, Material material) {
    if (item != null && item.getType() == material) {
      ItemMeta itemMeta = item.getItemMeta();
      return itemMeta != null && itemMeta.hasCustomModelData() ? itemMeta.getCustomModelData() : -1;
    } else {
      return -1;
    }
  }

  ItemStack createAbilityItem() {
    return createGemItem();
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    this.updatePendingAbilityState(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    this.updatePendingAbilityState(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    this.updatePendingAbilityState(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
    this.updatePendingAbilityState(event.getPlayer().getUniqueId());
  }

  void cleanupHookPoints(List<Location> entries, UUID playerId) {
    this.updatePrimaryState(entries);
    if (playerId != null) {
      this.secondaryLocationsByPlayer.remove(playerId, entries);
      this.playerCounters.remove(playerId);
    }
  }

  static void applyMissedSlamBoost(Player player, Vector direction) {
    if (player.isOnline() && !player.isDead()) {
      player.setVelocity(direction);
    }
  }

  void updateSkyAbility(
      ItemDisplay itemDisplay,
      Player player,
      BukkitTask[] task,
      UUID playerId,
      int[] count,
      Location location,
      Vector direction,
      Location[] targetLocation,
      Location origin) {
    if (itemDisplay.isDead() || !player.isOnline()) {
      this.removeTrackedDisplay(itemDisplay);
      task[0].cancel();
    } else if (this.isAbilityAllowed(playerId)) {
      this.removeTrackedDisplay(itemDisplay);
      task[0].cancel();
    } else if (!this.plugin.isGemsDisabled()
        && !this.plugin.isAbilityAllowed(player.getWorld())
        && !this.plugin.canUseAbility(player)) {
      double value = count[0] / 20.0;
      double distance = 1.0 - Math.pow(1.0 - value, 3.0);
      Location center = location.clone().add(direction.clone().multiply(5.0 * distance));
      itemDisplay.setTeleportDuration(1);
      itemDisplay.teleport(center);
      count[0]++;
      if (count[0] == 10) {
        targetLocation[0] = this.getTargetLocation(origin, playerId);
      }

      if (count[0] > 20) {
        this.removeTrackedDisplay(itemDisplay);
        if (targetLocation[0] != null) {
          this.activateSourceAbility(player, targetLocation[0]);
        }

        task[0].cancel();
      }
    } else {
      this.removeTrackedDisplay(itemDisplay);
      task[0].cancel();
    }
  }

  static void spawnSkyAbilityEffects(Location location) {
    location.getWorld().playSound(location, Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0F, 1.6F);
  }

  void finishParryWindow(UUID playerId) {
    targetPlayerIds.remove(playerId);
    if (!trackedPlayers.contains(playerId)) {
      lastActivationTimes.put(playerId, System.currentTimeMillis() + this.getConfiguredLong());
    }

    trackedPlayers.remove(playerId);
    cachedValuesByPlayer.remove(playerId);
  }

  void updateParryAbility(
      ItemDisplay itemDisplay,
      Player player,
      UUID playerId,
      int[] count,
      Location location,
      Vector direction,
      Location targetLocation,
      BukkitTask task) {
    if (itemDisplay.isDead() || !player.isOnline()) {
      this.removeTrackedDisplay(itemDisplay);
      task.cancel();
    } else if (this.isAbilityAllowed(playerId)) {
      this.removeTrackedDisplay(itemDisplay);
      task.cancel();
    } else if (!this.plugin.isGemsDisabled()
        && !this.plugin.isAbilityAllowed(player.getWorld())
        && !this.plugin.canUseAbility(player)) {
      double value = count[0] / 20.0;
      double distance = 1.0 - Math.pow(1.0 - value, 3.0);
      Location origin = location.clone().add(direction.clone().multiply(5.0 * distance));
      itemDisplay.setTeleportDuration(1);
      itemDisplay.teleport(origin);
      count[0]++;
      if (count[0] > 20) {
        this.plugin.spawnAbilityParticles(targetLocation, Color.WHITE);
        this.removeTrackedDisplay(itemDisplay);
        task.cancel();
      }
    } else {
      this.removeTrackedDisplay(itemDisplay);
      task.cancel();
    }
  }

  Integer initializeCounter(UUID playerId) {
    return this.getChainCharges();
  }

  Integer initializeCounterForPlayer(UUID playerId) {
    return this.getChainCharges();
  }

  Integer initializeCounterForTarget(UUID playerId) {
    return this.getChainCharges();
  }

  Integer initializeCounterFromConfig(UUID playerId) {
    return this.getChainCharges();
  }

  void clearWindChargeState(UUID playerId) {
    this.activePlayerIds.remove(playerId);
    this.resolvedPlayerIds.remove(playerId);
    this.pendingValuesByPlayer.remove(playerId);
    this.targetValuesByPlayer.remove(playerId);
    this.remainingTicks.remove(playerId);
  }

  void trackDisplay(ItemDisplay display) {
    this.trackedDisplays.add(display.getUniqueId());
  }

  void removeTrackedDisplay(ItemDisplay display) {
    this.trackedDisplays.remove(display.getUniqueId());
    display.remove();
  }

  static {
    lastUseTimes = new ConcurrentHashMap<>();
    targetPlayerIds = ConcurrentHashMap.newKeySet();
    trackedPlayers = ConcurrentHashMap.newKeySet();
    lastActivationTimes = new ConcurrentHashMap<>();
    expiryTimes = new ConcurrentHashMap<>();
    secondaryDustOptions = new DustOptions(Color.fromRGB(255, 210, 70), 1.8F);
    activeDustOptions = new DustOptions(Color.fromRGB(255, 140, 0), 2.0F);
    dustOptions = new DustOptions(Color.fromRGB(220, 40, 20), 2.2F);
    abilityStages = new ConcurrentHashMap<>();
    sourceLastUseTimes = new ConcurrentHashMap<>();
    targetLastUseTimes = new ConcurrentHashMap<>();
    primaryLastUseTimes = new ConcurrentHashMap<>();
    primaryFlagsByPlayer = new ConcurrentHashMap<>();
    animationSteps = new ConcurrentHashMap<>();
    startTimes = new ConcurrentHashMap<>();
    cachedValuesByPlayer = new ConcurrentHashMap<>();
    activeLastUseTimes = new ConcurrentHashMap<>();
  }
}
