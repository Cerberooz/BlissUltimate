package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class ChainGrappleManager implements Listener {
  final Bliss plugin;
  final Map<UUID, List<ChainGrappleSession>> sessionsByPlayer = new ConcurrentHashMap<>();
  static final Map<UUID, Long> lastUseTimes;
  static final DustOptions dustOptions;
  static final DustOptions secondaryDustOptions;

  public ChainGrappleManager(Bliss bliss) {
    this.plugin = bliss;
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

  double getChainGrappleTravelSpeed() {
    return this.getConfiguredDouble("chainGrappleTravelSpeed", 20.0);
  }

  double getEffectRadius() {
    return this.getConfiguredDouble("chainGrappleRetractSpeed", 4.0);
  }

  double getEffectScale() {
    return this.getConfiguredDouble("chainGrappleMaxRange", 150.0);
  }

  double getDamageAmount() {
    return this.getConfiguredDouble("chainGrapplePullPlayerStrength", 2.0);
  }

  double getKnockbackStrength() {
    return this.getConfiguredDouble("chainGrapplePullEntityStrength", 2.0);
  }

  double getMaxDistance() {
    return this.getConfiguredDouble("chainGrappleArrivePlayer", 2.5);
  }

  double getVerticalOffset() {
    return this.getConfiguredDouble("chainGrappleArriveEntity", 2.5);
  }

  long getDurationMillis() {
    return this.getCooldownMillis(this.getConfiguredDouble("chainGrappleTimeoutSeconds", 2.0));
  }

  double getHorizontalOffset() {
    return this.getConfiguredDouble("chainGrappleSlingshotControlStrength", 0.85);
  }

  double getAnimationProgress() {
    return this.getConfiguredDouble("chainGrappleSlingshotMaxSpeed", 3.35);
  }

  double getChainGrappleEntityHookDamage() {
    return this.getConfiguredDouble("chainGrappleEntityHookDamage", 5.0);
  }

  double getChainGrappleGlideAssistStrength() {
    return this.getConfiguredDouble("chainGrappleGlideAssistStrength", 0.2);
  }

  double getChainGrappleGlideMaxSpeed() {
    return this.getConfiguredDouble("chainGrappleGlideMaxSpeed", 2.6);
  }

  double getActiveAbilityDoubleValue() {
    return this.getConfiguredDouble("chainGrappleGlideMaxUp", 0.16);
  }

  double getPendingAbilityDoubleValue() {
    return this.getConfiguredDouble("chainGrappleGlideMaxDown", -0.14);
  }

  double getCachedAbilityDoubleValue() {
    return this.getConfiguredDouble("chainGrappleGlidePassedHookDistance", 0.75);
  }

  double getResolvedAbilityDoubleValue() {
    return this.getConfiguredDouble("chainGrappleSlamOuterRadius", 7.0);
  }

  double getChainGrappleSlamInnerRadius() {
    return this.getConfiguredDouble("chainGrappleSlamInnerRadius", 3.5);
  }

  double getChainGrappleSlamBaseDamage() {
    return this.getConfiguredDouble("chainGrappleSlamBaseDamage", 10.0);
  }

  long getLastUseTime() {
    return this.getAbilityLongValue(this.getConfiguredDouble("chainGrappleWindDisableSeconds", 6.0));
  }

  public void activateAbility(Player player) {
    this.updateAbilityState(player, true, false);
  }

  public void refreshAbilityState(Player player, boolean enabled) {
    this.updateAbilityState(player, enabled, false);
  }

  public void updateAbilityState(Player player, boolean enabled, boolean active) {
    UUID playerId = player.getUniqueId();
    List<ChainGrappleSession> entries =
        this.sessionsByPlayer.computeIfAbsent(playerId, ChainGrappleManager::getSessionsForPlayer);
    entries.removeIf(ChainGrappleManager::isValidTarget);

    for (ChainGrappleSession chainGrappleSession : entries) {
      chainGrappleSession.updateState(true);
    }

    entries.clear();
    ChainGrappleSession currentChainGrappleSession = new ChainGrappleSession(this, player, enabled, active);
    entries.add(currentChainGrappleSession);
    currentChainGrappleSession.startBackgroundTasks();
  }

  public boolean isAbilityActive(UUID playerId) {
    Long storedTimestamp = lastUseTimes.get(playerId);
    if (storedTimestamp == null) {
      return false;
    } else if (System.currentTimeMillis() >= storedTimestamp) {
      lastUseTimes.remove(playerId);
      return false;
    } else {
      return true;
    }
  }

  public void applyAbilityEffects(UUID playerId) {
    List<ChainGrappleSession> entries = this.sessionsByPlayer.remove(playerId);
    if (entries != null) {
      for (ChainGrappleSession chainGrappleSession : entries) {
        chainGrappleSession.updateState(true);
      }
    }
  }

  public void shutdown() {
    for (UUID playerId : new ArrayList<>(this.sessionsByPlayer.keySet())) {
      this.applyAbilityEffects(playerId);
    }

    this.sessionsByPlayer.clear();
    lastUseTimes.clear();
  }

  public boolean isAbilityAllowed(UUID playerId) {
    List<ChainGrappleSession> entries = this.sessionsByPlayer.get(playerId);
    if (entries == null) {
      return false;
    }

    entries.removeIf(ChainGrappleManager::isMatchingState);
    return !entries.isEmpty();
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    this.applyAbilityEffects(event.getEntity().getUniqueId());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    this.applyAbilityEffects(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    this.applyAbilityEffects(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
    this.applyAbilityEffects(event.getPlayer().getUniqueId());
  }

  void spawnAbilityParticles(Player player) {
    Location location = player.getLocation();
    World world = location.getWorld();
    if (world != null) {
      this.spawnAbilityEffects(location, this.getResolvedAbilityDoubleValue(), Particle.SOUL_FIRE_FLAME, 60, null);
      this.spawnAbilityEffects(location, this.getChainGrappleSlamInnerRadius(), Particle.SOUL_FIRE_FLAME, 30, null);
      this.spawnAbilityEffects(location, this.getResolvedAbilityDoubleValue() * 0.7, Particle.DUST, 45, dustOptions);
      world.spawnParticle(Particle.EXPLOSION, location, 4, 1.2, 0.2, 1.2, 0.0);
      world.spawnParticle(
          Particle.BLOCK,
          location.clone().add(0.0, 0.1, 0.0),
          60,
          1.5,
          0.1,
          1.5,
          0.3,
          Material.REDSTONE_BLOCK.createBlockData());
      world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.9F, 0.55F);
      world.playSound(location, Sound.ITEM_MACE_SMASH_AIR, 1.0F, 0.6F);
      world.playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.8F, 1.1F);

      for (Entity entity :
          world.getNearbyEntities(
              location, this.getResolvedAbilityDoubleValue(), this.getResolvedAbilityDoubleValue(), this.getResolvedAbilityDoubleValue())) {
        if (entity instanceof LivingEntity livingEntity
            && !entity.equals(player)
            && !(livingEntity instanceof Player targetPlayer
                && (targetPlayer.getGameMode() == GameMode.CREATIVE
                    || targetPlayer.getGameMode() == GameMode.SPECTATOR))) {
          double distance = entity.getLocation().distance(location);
          if (!(distance > this.getResolvedAbilityDoubleValue())) {
            double value = distance / this.getResolvedAbilityDoubleValue();
            double radius = this.getChainGrappleSlamBaseDamage() * (1.0 - value * 0.4);
            livingEntity.damage(radius, DamageSource.builder(DamageType.SONIC_BOOM).build());
            Vector offset =
                entity.getLocation()
                    .toVector()
                    .subtract(location.toVector())
                    .normalize()
                    .multiply(1.8 * (1.0 - value * 0.5));
            offset.setY(0.7);
            entity.setVelocity(offset);
            world.spawnParticle(
                Particle.DUST, livingEntity.getLocation().add(0.0, 1.0, 0.0), 12, 0.4, 0.4, 0.4, 0.0, secondaryDustOptions);
            if (distance <= this.getChainGrappleSlamInnerRadius() && entity instanceof Player sourcePlayer) {
              lastUseTimes.put(sourcePlayer.getUniqueId(), System.currentTimeMillis() + this.getLastUseTime());
              sourcePlayer.playSound(sourcePlayer.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.6F, 1.8F);
            }
          }
        }
      }
    }
  }

  void playAbilityEffects(Location location, double value, boolean enabled) {
    int count = Math.max(1, (int) Math.ceil(value));

    for (int index = -count; index <= count; index++) {
      for (int remaining = -count; remaining <= count; remaining++) {
        for (int step = -count; step <= count; step++) {
          Location targetLocation = location.clone().add(index, remaining, step);
          if (!(targetLocation.distanceSquared(location) > value * value)) {
            Block block = targetLocation.getBlock();
            if (block.getType() == Material.COBWEB) {
              if (enabled) {
                block.breakNaturally();
              } else {
                block.setType(Material.AIR, false);
              }

              block
                  .getWorld()
                  .spawnParticle(
                      Particle.BLOCK,
                      block.getLocation().add(0.5, 0.5, 0.5),
                      10,
                      0.2,
                      0.2,
                      0.2,
                      Material.COBWEB.createBlockData());
            }
          }
        }
      }
    }
  }

  public boolean isAbilityBlocked(UUID playerId) {
    List<ChainGrappleSession> entries = this.sessionsByPlayer.get(playerId);
    if (entries == null) {
      return false;
    } else {
      entries.removeIf(ChainGrappleManager::isActive);
      if (entries.isEmpty()) {
        this.sessionsByPlayer.remove(playerId);
        return false;
      } else {
        return true;
      }
    }
  }

  void spawnAbilityEffects(Location location, double value, Particle particle, int count, Object particleData) {
    World world = location.getWorld();
    if (world != null) {
      double distance = 360.0 / count;

      for (int index = 0; index < count; index++) {
        double angleRadians = Math.toRadians(index * distance);
        Location targetLocation = location.clone().add(Math.cos(angleRadians) * value, 0.0, Math.sin(angleRadians) * value);
        if (particleData != null) {
          world.spawnParticle(particle, targetLocation, 1, 0.0, 0.0, 0.0, 0.0, particleData);
        } else {
          world.spawnParticle(particle, targetLocation, 1, 0.0, 0.0, 0.0, 0.0);
        }
      }
    }
  }

  static boolean isActive(ChainGrappleSession chainGrappleSession) {
    return chainGrappleSession.active;
  }

  static boolean isMatchingState(ChainGrappleSession chainGrappleSession) {
    return chainGrappleSession.active;
  }

  static boolean isValidTarget(ChainGrappleSession chainGrappleSession) {
    return chainGrappleSession.active;
  }

  static List<ChainGrappleSession> getSessionsForPlayer(UUID playerId) {
    return new ArrayList<>();
  }

  static {
    lastUseTimes = new ConcurrentHashMap<>();
    dustOptions = new DustOptions(Color.RED, 1.8F);
    secondaryDustOptions = new DustOptions(Color.fromRGB(139, 0, 0), 2.5F);
  }
}
