package cerberooz.cerberooz.BlissUltimate;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FluxAbilityEffects {
  static Bliss plugin;
  static Map<UUID, Boolean> flagsByPlayer = new ConcurrentHashMap<>();
  static final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();
  static final BeamCurveSegment[] targetBeamCurveSegments =
      resolveBeamCurveSegment(
          -0.05, false, -0.06, false, -0.07, false, -0.08, false, -0.09, false, -0.1, false, -0.11,
          false, -0.14, false, -0.1, true, -0.08, false, -0.06, false, -0.04, false, -0.03, true,
          0.05, false, 0.06, true, 0.07, false, 0.08, false, 0.09, false, 0.1, true, 0.11, false,
          0.12, true, 0.1, false, 0.08, false, 0.06, false, 0.04, true, 0.02, false);
  static final BeamCurveSegment[] sourceBeamCurveSegments =
      resolveBeamCurveSegment(
          0.05, false, 0.06, true, 0.07, false, 0.08, false, 0.09, false, 0.1, true, 0.11, false,
          0.12, true, 0.1, false, 0.08, false, 0.06, false, 0.04, true, 0.02, false, -0.06, false,
          -0.07, false, -0.08, false, -0.09, false, -0.1, false, -0.11, false, -0.12, false, -0.13,
          false, -0.1, true, -0.08, false, -0.06, false, -0.04, false, -0.03, true);
  static final BeamCurveSegment[] activeBeamCurveSegment =
      resolveBeamCurveSegment(
          -0.06, false, -0.07, false, -0.08, false, -0.09, false, -0.1, false, -0.11, false, -0.12,
          false, -0.13, false, -0.1, true, -0.08, false, -0.06, false, -0.04, false, -0.03, true,
          0.05, false, 0.06, true, 0.07, false, 0.08, false, 0.09, false, 0.1, true, 0.11, false,
          0.12, true, 0.1, false, 0.08, false, 0.06, false, 0.04, true, 0.02, false);
  static final BeamCurveSegment[] secondaryBeamCurveSegment =
      resolveBeamCurveSegment(
          -0.06, false, -0.07, false, -0.08, false, -0.09, false, -0.1, false, -0.11, false, -0.12,
          false, -0.13, false, -0.1, true, -0.08, false, -0.06, false, -0.04, false, -0.03, true,
          0.05, false, 0.06, true, 0.07, false, 0.08, false, 0.09, false, 0.1, true, 0.11, false,
          0.12, true, 0.1, false, 0.08, false, 0.06, false, 0.04, true, 0.02, false);
  static final BeamCurveSegment[] cachedBeamCurveSegment =
      resolveBeamCurveSegment(
          0.09, false, 0.1, true, 0.11, false, 0.12, true, 0.1, false, 0.08, false, 0.06, false,
          0.04, true, 0.02, false, -0.06, false, -0.07, false, -0.08, false, -0.09, false, -0.1,
          false, -0.11, false, -0.12, false, -0.13, false, -0.1, true, -0.08, false, -0.06, false,
          -0.04, false, -0.03, true, 0.05, false, 0.06, true, 0.07, false, 0.08, false);
  static final BeamCurveSegment[] beamCurveSegment =
      resolveBeamCurveSegment(
          -0.06, false, -0.07, false, -0.08, false, -0.09, false, -0.1, false, -0.11, false, -0.12,
          false, -0.13, false, -0.1, true, -0.08, false, -0.06, false, -0.04, false, -0.03, true,
          0.05, false, 0.06, true, 0.07, false, 0.08, false, 0.09, false, 0.1, true, 0.11, false,
          0.12, true, 0.1, false, 0.08, false, 0.06, false, 0.04, true, 0.02, false);
  static final BeamCurveSegment[] primaryBeamCurveSegments =
      resolveBeamCurveSegment(
          -0.1, true, -0.08, false, -0.06, false, -0.04, false, -0.03, true, 0.05, false, 0.06,
          true, 0.07, false, 0.08, false, 0.09, false, 0.1, true, 0.11, false, 0.12, true, 0.1,
          false, 0.08, false, 0.06, false, 0.04, true, 0.02, false, -0.06, false, -0.07, false,
          -0.08, false, -0.09, false, -0.1, false, -0.11, false, -0.12, false, -0.13, false);
  static final BeamCurveSegment[] currentBeamCurveSegment = secondaryBeamCurveSegment;
  static final BeamCurveSegment[] trackedBeamCurveSegment =
      resolveBeamCurveSegment(
          -0.06, false, -0.07, false, -0.08, false, -0.09, false, -0.1, false, -0.11, false, -0.12,
          false, -0.13, false, -0.1, true, -0.08, false, -0.06, false, -0.04, false, -0.03, true,
          0.05, false, 0.06, true, 0.07, false, 0.08, false, 0.09, false, 0.1, true, 0.11, false,
          0.12, true, 0.1, false, 0.08, false, 0.06, false, 0.04, true, 0.02, true);
  static final BeamCurveSegment[] pendingBeamCurveSegment = primaryBeamCurveSegments;
  static final BeamBurstSegment[] beamBurstSegment =
      new BeamBurstSegment[] {
        new BeamBurstSegment(3, -0.1, true),
        new BeamBurstSegment(3, -0.2, false),
        new BeamBurstSegment(3, -0.3, false),
        new BeamBurstSegment(2, -0.2, true),
        new BeamBurstSegment(3, 0.1, false),
        new BeamBurstSegment(3, 0.2, true),
        new BeamBurstSegment(3, 0.3, false),
        new BeamBurstSegment(2, 0.2, true)
      };

  public FluxAbilityEffects(Bliss bliss) {
    plugin = bliss;
  }

  public static void setPlugin(Bliss bliss) {
    plugin = bliss;
  }

  public static void setAbilityFlags(Map<UUID, Boolean> flagsByPlayer) {
    FluxAbilityEffects.flagsByPlayer = flagsByPlayer;
  }

  static BeamCurveSegment[] resolveBeamCurveSegment(Object... curveData) {
    BeamCurveSegment[] beamCurveSegment = new BeamCurveSegment[curveData.length / 2];
    byte step = 0;

    for (int count = 0; step < curveData.length; count++) {
      beamCurveSegment[count] =
          new BeamCurveSegment(((Number) curveData[step]).doubleValue(), (Boolean) curveData[step + 1]);
      step += 2;
    }

    return beamCurveSegment;
  }

  public static void activateAbility(Location location, Player player) {
    refreshPlayerState(location, player, 2.5, 20, 2, 60, 20, targetBeamCurveSegments);
  }

  public static void refreshAbilityState(Location location, Player player) {
    refreshPlayerState(location, player, 2.5, 220, 2, 120, 20, sourceBeamCurveSegments);
  }

  public static void updateAbilityState(Location location, Player player) {
    refreshPlayerState(location, player, 2.5, 220, 2, 300, 20, activeBeamCurveSegment);
  }

  public static void applyAbilityEffects(Location location, Player player) {
    refreshPlayerState(location, player, 8.0, 60, 6, 30, 20, secondaryBeamCurveSegment);
  }

  public static void playAbilityEffects(Location location, Player player) {
    refreshPlayerState(location, player, 9.0, 60, 6, 30, 20, cachedBeamCurveSegment);
  }

  public static void spawnAbilityEffects(Location location, Player player) {
    refreshPlayerState(location, player, 11.0, 45, 8, 30, 20, beamCurveSegment);
  }

  public static void cleanupAbilityState(Location location, Player player) {
    refreshPlayerState(location, player, 11.0, 45, 8, 30, 20, primaryBeamCurveSegments);
  }

  public static void scheduleAbilityUpdate(Location location, Player player) {
    refreshPlayerState(location, player, 14.0, 60, 6, 30, 20, currentBeamCurveSegment);
  }

  public static void completeAbilityAction(Location location, Player player) {
    refreshPlayerState(location, player, 5.0, 60, 6, 30, 20, trackedBeamCurveSegment);
  }

  public static void processAbilityState(Location location, Player player) {
    refreshPlayerState(location, player, 5.0, 45, 8, 30, 20, pendingBeamCurveSegment);
  }

  public static void handleAbilityAction(Location location, Player player) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      double effectRadius = 1.0;
      double distanceThreshold = 5.0;
      int maxCount = 0;
      int durationTicks = 0;

      @Override
      public void run() {
        if (FluxAbilityEffects.canUseAbility(player)
            && this.maxCount < 25
            && this.durationTicks < FluxAbilityEffects.beamBurstSegment.length) {
          while (this.durationTicks < FluxAbilityEffects.beamBurstSegment.length) {
            BeamBurstSegment beamBurstSegment = FluxAbilityEffects.beamBurstSegment[this.durationTicks];

            for (int count = 0; count < beamBurstSegment.steps(); count++) {
              if (!FluxAbilityEffects.canUseAbility(player)) {
                this.cancel();
                return;
              }

              Location location = this.anchorLocation.clone().add(this.anchorLocation.getDirection().multiply(this.effectRadius));
              FluxAbilityEffects.spawnAbilityParticles(location, 8, 45, 30, this.distanceThreshold);
              FluxAbilityEffects.applyAbilityDamage(location, player, 5.0, true);
              this.effectRadius += 0.3;
              this.distanceThreshold = this.distanceThreshold + beamBurstSegment.radiusDelta();
            }

            this.durationTicks++;
            this.maxCount++;
            if (beamBurstSegment.pauseAfterSegment()) {
              return;
            }
          }

          this.cancel();
        } else {
          this.cancel();
        }
      }
    }
        .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  public static void resetAbilityState(Location location, Player player) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      double effectRadius = 1.0;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= 120) {
          this.cancel();
        } else {
          Location location = this.anchorLocation.clone().add(this.anchorLocation.getDirection().multiply(this.effectRadius));
          World world = location.getWorld();
          if (world == null) {
            this.cancel();
          } else {
            DustTransition dustTransition =
            new DustTransition(Color.fromRGB(94, 215, 255), Color.fromRGB(255, 255, 255), 4.0F);
            world.spawnParticle(
                Particle.DUST_COLOR_TRANSITION, location, 40, 2.0, 2.0, 2.0, 1.0E-9, dustTransition, true);
            this.effectRadius += 1.1;
            this.durationTicks++;
          }
        }
      }
    }
        .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  public static void trackAbilityState(Location location, Player player) {
    playerIds.clear();
    new BukkitRunnable() {
      final Location anchorLocation = location;
      double effectRadius = 1.0;
      int durationTicks = 0;

      @Override
      public void run() {
        if (FluxAbilityEffects.canUseAbility(player) && this.durationTicks < 440) {
          Location location = this.anchorLocation.clone().add(this.anchorLocation.getDirection().multiply(this.effectRadius));
          World world = location.getWorld();
          if (world == null) {
            this.cancel();
            FluxAbilityEffects.initialize();
          } else {
            world.spawnParticle(Particle.END_ROD, location, 3, 0.0, 0.0, 0.0, 0.03, null, true);
            FluxAbilityEffects.applyAbilityDamage(location, player, 5.0, true);
            this.effectRadius += 0.3;
            this.durationTicks++;
          }
        } else {
          this.cancel();
          FluxAbilityEffects.initialize();
        }
      }
    }
        .runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  public static void playAbilitySound(Player player) {
    UUID playerId = player.getUniqueId();
    flagsByPlayer.put(playerId, true);
    Location location = player.getLocation().clone();
    Vector offset = player.getLocation().getDirection().clone().normalize();
    World world = player.getWorld();
    world.playSound(location, Sound.ENTITY_WARDEN_SONIC_CHARGE, 10.0F, 1.0F);
    world.playSound(location, Sound.ITEM_AXE_SCRAPE, 20.0F, 0.5F);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        world.playSound(location, Sound.ITEM_AXE_SCRAPE, 20.0F, 0.5F);

      }, 12L);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        world.playSound(location, Sound.ITEM_AXE_SCRAPE, 20.0F, 0.65F);

      }, 24L);
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Vector direction = offset;

      @Override
      public void run() {
        player.damage(0.001);
        player.setHealth(0.1);
        world.playSound(this.anchorLocation, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 20.0F, 1.2F);
        player.setVelocity(new Vector(0, 2, 0).add(this.direction.clone().multiply(-4)));
        Location location = this.anchorLocation.clone().add(this.direction.clone().multiply(3)).add(0.0, 1.6, 0.0);
        Location targetLocation = this.anchorLocation.clone().add(this.direction.clone().multiply(2)).add(0.0, 1.6, 0.0);
        FluxAbilityEffects.trackAbilityState(targetLocation, player);
        FluxAbilityEffects.resetAbilityState(location, player);
      }
    }.runTaskLater(plugin, 108L);
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Vector direction = offset;
      @Override
      public void run() {
        Location location = this.anchorLocation.clone().add(this.direction.clone().multiply(3)).add(0.0, 1.6, 0.0);
        Location targetLocation = this.anchorLocation.clone().add(this.direction.clone().multiply(2)).add(0.0, 1.6, 0.0);
        FluxAbilityEffects.trackAbilityState(targetLocation, player);
        FluxAbilityEffects.resetAbilityState(location, player);
        FluxAbilityEffects.handleAbilityAction(targetLocation, player);
        FluxAbilityEffects.completeAbilityAction(targetLocation, player);
        FluxAbilityEffects.processAbilityState(targetLocation, player);
        world.playSound(this.anchorLocation, Sound.BLOCK_BEACON_AMBIENT, 50.0F, 1.6F);
        world.playSound(this.anchorLocation, Sound.ENTITY_WARDEN_SONIC_BOOM, 20.0F, 1.0F);
        world.playSound(this.anchorLocation, Sound.ENTITY_GENERIC_EXPLODE, 20.0F, 0.75F);
      }
    }.runTaskLater(plugin, 138L);
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Vector direction = offset;
      @Override
      public void run() {
        Location location = this.anchorLocation.clone().add(this.direction.clone().multiply(3)).add(0.0, 1.6, 0.0);
        Location targetLocation = this.anchorLocation.clone().add(this.direction.clone().multiply(2)).add(0.0, 1.6, 0.0);
        FluxAbilityEffects.trackAbilityState(targetLocation, player);
        FluxAbilityEffects.resetAbilityState(location, player);
        FluxAbilityEffects.handleAbilityAction(targetLocation, player);
        FluxAbilityEffects.applyAbilityEffects(targetLocation, player);
        FluxAbilityEffects.playAbilityEffects(targetLocation, player);
        FluxAbilityEffects.completeAbilityAction(targetLocation, player);
        FluxAbilityEffects.processAbilityState(targetLocation, player);
      }
    }.runTaskLater(plugin, 174L);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        world.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 20.0F, 1.6F);
        world.playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 20.0F, 2.0F);
        world.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 50.0F, 1.6F);

      }, 198L);
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Vector direction = offset;
      @Override
      public void run() {
        Location location = this.anchorLocation.clone().add(this.direction.clone().multiply(3)).add(0.0, 1.6, 0.0);
        Location targetLocation = this.anchorLocation.clone().add(this.direction.clone().multiply(2)).add(0.0, 1.6, 0.0);
        FluxAbilityEffects.trackAbilityState(targetLocation, player);
        FluxAbilityEffects.resetAbilityState(location, player);
        FluxAbilityEffects.handleAbilityAction(targetLocation, player);
        FluxAbilityEffects.applyAbilityEffects(targetLocation, player);
        FluxAbilityEffects.playAbilityEffects(targetLocation, player);
        FluxAbilityEffects.spawnAbilityEffects(targetLocation, player);
        FluxAbilityEffects.cleanupAbilityState(targetLocation, player);
        FluxAbilityEffects.completeAbilityAction(targetLocation, player);
        FluxAbilityEffects.processAbilityState(targetLocation, player);
      }
    }.runTaskLater(plugin, 210L);
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Vector direction = offset;
      @Override
      public void run() {
        Location location = this.anchorLocation.clone().add(this.direction.clone().multiply(3)).add(0.0, 1.6, 0.0);
        Location targetLocation = this.anchorLocation.clone().add(this.direction.clone().multiply(2)).add(0.0, 1.6, 0.0);
        FluxAbilityEffects.trackAbilityState(targetLocation, player);
        FluxAbilityEffects.resetAbilityState(location, player);
        FluxAbilityEffects.handleAbilityAction(targetLocation, player);
        FluxAbilityEffects.applyAbilityEffects(targetLocation, player);
        FluxAbilityEffects.playAbilityEffects(targetLocation, player);
        FluxAbilityEffects.spawnAbilityEffects(targetLocation, player);
        FluxAbilityEffects.cleanupAbilityState(targetLocation, player);
        FluxAbilityEffects.completeAbilityAction(targetLocation, player);
        FluxAbilityEffects.processAbilityState(targetLocation, player);
        FluxAbilityEffects.processAbilityState(targetLocation, player);
      }
    }.runTaskLater(plugin, 246L);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        world.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 20.0F, 1.6F);
        world.playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 20.0F, 2.0F);
        world.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 50.0F, 1.6F);

      }, 258L);
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Vector direction = offset;
      @Override
      public void run() {
        Location location = this.anchorLocation.clone().add(this.direction.clone().multiply(3)).add(0.0, 1.6, 0.0);
        Location targetLocation = this.anchorLocation.clone().add(this.direction.clone().multiply(2)).add(0.0, 1.6, 0.0);
        FluxAbilityEffects.trackAbilityState(targetLocation, player);
        FluxAbilityEffects.resetAbilityState(location, player);
        FluxAbilityEffects.handleAbilityAction(targetLocation, player);
        FluxAbilityEffects.applyAbilityEffects(targetLocation, player);
        FluxAbilityEffects.playAbilityEffects(targetLocation, player);
        FluxAbilityEffects.spawnAbilityEffects(targetLocation, player);
        FluxAbilityEffects.cleanupAbilityState(targetLocation, player);
        FluxAbilityEffects.completeAbilityAction(targetLocation, player);
        FluxAbilityEffects.processAbilityState(targetLocation, player);
      }
    }.runTaskLater(plugin, 282L);
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Vector direction = offset;
      @Override
      public void run() {
        Location location = this.anchorLocation.clone().add(this.direction.clone().multiply(3)).add(0.0, 1.6, 0.0);
        Location targetLocation = this.anchorLocation.clone().add(this.direction.clone().multiply(2)).add(0.0, 1.6, 0.0);
        FluxAbilityEffects.trackAbilityState(targetLocation, player);
        FluxAbilityEffects.resetAbilityState(location, player);
        FluxAbilityEffects.handleAbilityAction(targetLocation, player);
        FluxAbilityEffects.applyAbilityEffects(targetLocation, player);
        FluxAbilityEffects.playAbilityEffects(targetLocation, player);
        FluxAbilityEffects.spawnAbilityEffects(targetLocation, player);
        FluxAbilityEffects.cleanupAbilityState(targetLocation, player);
        FluxAbilityEffects.completeAbilityAction(targetLocation, player);
        FluxAbilityEffects.processAbilityState(targetLocation, player);
        world.playSound(this.anchorLocation, Sound.BLOCK_BEACON_POWER_SELECT, 20.0F, 1.6F);
        world.playSound(this.anchorLocation, Sound.BLOCK_BEACON_DEACTIVATE, 20.0F, 2.0F);
        world.playSound(this.anchorLocation, Sound.BLOCK_BEACON_AMBIENT, 50.0F, 1.6F);
      }
    }.runTaskLater(plugin, 318L);
    Bukkit.getScheduler().runTaskLater(plugin, () -> flagsByPlayer.put(playerId, false), 330L);
  }

  static void refreshPlayerState(
      Location location,
      Player player,
      double value,
      int count,
      int index,
      int remaining,
      int step,
      BeamCurveSegment[] beamCurveSegment) {
    final BeamCurveSegment[] segments = beamCurveSegment;
    new BukkitRunnable() {
      final double initialRadius = value;
      final Player caster = player;
      final int passes = step;
      final Location origin = location;
      final int particleCount = index;
      final int sampleCount = count;
      final int particleExtra = remaining;
      double distanceAlongBeam = 1.0;
      double radius = this.initialRadius;
      int pass = 0;
      int segmentIndex = 0;

      @Override
      public void run() {
        if (FluxAbilityEffects.canUseAbility(this.caster) && this.pass < this.passes) {
          while (this.segmentIndex < segments.length) {
            BeamCurveSegment segment = segments[this.segmentIndex];
            Location point =
                this.origin.clone().add(this.origin.getDirection().multiply(this.distanceAlongBeam));
            FluxAbilityEffects.updatePlayerState(
                point, this.particleCount, this.sampleCount, this.particleExtra, this.radius);
            FluxAbilityEffects.applyAbilityDamage(point, this.caster, 5.0, true);
            this.distanceAlongBeam += 0.3;
            this.radius += segment.radiusDelta();
            this.segmentIndex++;
            if (segment.pauseAfterSegment()) {
              return;
            }
          }

          this.segmentIndex = 0;
          this.pass++;
        } else {
          this.cancel();
        }
      }
    }.runTaskTimer(plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  static boolean canUseAbility(Player player) {
    return player != null && player.isOnline() && flagsByPlayer.getOrDefault(player.getUniqueId(), false);
  }

  static void initialize() {
    Bukkit.getScheduler().runTaskLater(plugin, playerIds::clear, 40L);
  }

  static void applyAbilityDamage(Location location, Player player, double value, boolean enabled) {
    World world = location.getWorld();
    if (world != null) {
      for (Entity entity : world.getNearbyEntities(location, value, value, value)) {
        if (entity instanceof Player targetPlayer && entity != player) {
          if (playerIds.add(targetPlayer.getUniqueId())) {
            targetPlayer.damage(1.0E12, player);
          }
        } else if (!(entity instanceof Player) && enabled) {
          entity.remove();
        }
      }
    }
  }

  static void spawnAbilityParticles(Location location, int count, int index, int remaining, double value) {
    World world = location.getWorld();
    if (world != null) {
      for (int step = 0; step < count; step++) {
        double distance = step * index + remaining;
        Vector direction = getDirection(value, location.getYaw() - 90.0, distance);
        Location targetLocation = location.clone().add(direction);
        world.spawnParticle(Particle.SONIC_BOOM, targetLocation, 3, 0.0, 0.0, 0.0, 1.0E-9, null, true);
      }
    }
  }

  static void updatePlayerState(Location location, int count, int index, int remaining, double value) {
    World world = location.getWorld();
    if (world != null) {
      for (int step = 0; step < count; step++) {
        double distance = step * index + remaining;
        Vector direction = getDirection(value, location.getYaw() - 90.0, distance);
        Location targetLocation = location.clone().add(direction);
        world.spawnParticle(Particle.END_ROD, targetLocation, 1, 0.0, 0.0, 0.0, 1.0E-9, null, true);
      }
    }
  }

  static Vector getDirection(double value, double distance, double radius) {
    double angleRadians = Math.toRadians(distance);
    double angle = Math.toRadians(radius);
    double offset = value * Math.cos(angle) * Math.cos(angleRadians);
    double progress = value * Math.sin(angle);
    double scale = value * Math.cos(angle) * Math.sin(angleRadians);
    return new Vector(offset, progress, scale);
  }

  public static void finishAbilityAction(Player player) {
    flagsByPlayer.put(player.getUniqueId(), false);
  }

  public static void shutdown() {
    flagsByPlayer.clear();
    playerIds.clear();
    plugin = null;
  }
}
