package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class ParticleEffects {
  static Bliss plugin;
  static final String _ID;

  public ParticleEffects(Bliss bliss) {
    plugin = bliss;
  }

  public static void shutdown() {
    plugin = null;
  }

  public static BukkitRunnable createActiveParticleAnimationTask(
      Particle particle,
      Location location,
      Location targetLocation,
      int count,
      int index,
      double value,
      double distance,
      double radius,
      double angle) {
    World world = resolveWorld(location);
    if (world != null
        && targetLocation != null
        && targetLocation.getWorld() != null
        && world.equals(targetLocation.getWorld())
        && plugin != null
        && particle != null) {
      int remaining = Math.max(1, count);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      double progress = targetLocation.getX();
      double scale = targetLocation.getY();
      double amount = targetLocation.getZ();
      double speed = scale - y;
      if (Math.abs(speed) <= 0.001) {
        return null;
      }

      double height = Math.abs(speed) * 0.07;
      BukkitRunnable activeParticleAnimationTask =
          new BukkitRunnable() {
            final int maxCount = remaining;
            final double animationProgress = x;
            final double verticalOffset = z;
            final double distanceThreshold = y;
            final double damageAmount = amount;
            final double effectHeight = z;
            final double effectRadius = amount;
            final double maxDistance = distance;
            final int cooldownTicks = index;
            final double primaryEffectRadius = value;
            final double knockbackStrength = distance;
            final double horizontalOffset = radius;
            final double effectScale = amount;
            int durationTicks = 0;

            @Override
            public void run() {
              if (this.durationTicks > this.maxCount) {
                this.cancel();
              } else {
                double value = (double) this.durationTicks / this.maxCount;
                double distance = this.animationProgress + (this.verticalOffset - this.animationProgress) * value;
                double radius = this.distanceThreshold + (this.damageAmount - this.distanceThreshold) * value;
                double angle = this.effectHeight + (this.effectRadius - this.effectHeight) * value;
                double offset = Math.sin(Math.PI * value) * this.maxDistance;
                double progress = distance + offset;
                world.spawnParticle(
                    particle, progress, radius, angle, Math.max(1, this.cooldownTicks), this.primaryEffectRadius, this.knockbackStrength, this.horizontalOffset, this.effectScale);
                this.durationTicks++;
              }
            }
          };
      activeParticleAnimationTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return activeParticleAnimationTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createCleanupParticleOrbitTask(
      Particle particle,
      Location location,
      double value,
      double distance,
      int count,
      int index,
      int remaining,
      double radius,
      double angle,
      double progress,
      double scale) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null) {
      int step = Math.max(1, index);
      double amount = Math.max(0.05, value);
      double speed = Math.max(0.05, distance);
      int ticks = Math.max(1, count);
      BukkitRunnable cleanupParticleOrbitTask =
          new BukkitRunnable() {
            final int animationStep = step;
            final int durationTicks = ticks;
            final Location anchorLocation = location;
            final double verticalOffset = amount;
            final double knockbackStrength = speed;
            final int maxCount = remaining;
            final double effectScale = radius;
            final double effectRadius = angle;
            final double distanceThreshold = progress;
            final double damageAmount = scale;
            int cooldownTicks = 0;

            @Override
            public void run() {
              if (this.cooldownTicks > this.animationStep) {
                this.cancel();
              } else {
                double value = (double) this.cooldownTicks / this.animationStep;
                double distance = (Math.PI * 2) * this.durationTicks * value;
                double offset = this.anchorLocation.getX() + Math.cos(distance) * this.verticalOffset;
                double y = this.anchorLocation.getY() + this.knockbackStrength * value;
                double radius = this.anchorLocation.getZ() + Math.sin(distance) * this.verticalOffset;
                world.spawnParticle(
                    particle, offset, y, radius, Math.max(1, this.maxCount), this.effectScale, this.effectRadius, this.distanceThreshold, this.damageAmount);
                this.cooldownTicks++;
              }
            }
          };
      cleanupParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return cleanupParticleOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createPulseParticleOrbitTask(
      Particle particle,
      Location location,
      Location targetLocation,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress) {
    World world = resolveWorld(location);
    if (world != null
        && targetLocation != null
        && targetLocation.getWorld() != null
        && world.equals(targetLocation.getWorld())
        && plugin != null
        && particle != null) {
      int step = Math.max(8, count);
      int ticks = Math.max(1, index);
      Vector direction = targetLocation.toVector().subtract(location.toVector());
      double scale = direction.length();
      if (scale <= 0.001) {
        return null;
      }

      Vector offset = direction.clone().normalize();
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      Location origin = location.clone().add(targetLocation).multiply(0.5);
      double amount = scale / 2.0;
      double speed = Math.max(0.0, value);
      BukkitRunnable pulseParticleOrbitTaskPrimary =
          new BukkitRunnable() {
            final int maxCount = ticks;
            final int cooldownTicks = step;
            final double effectScale = amount;
            final double damageAmount = speed;
            final Location anchorLocation = origin;
            final Vector secondaryDirection = normalizedDirection;
            final Vector direction = normalizedDirection;
            final int durationTicks = remaining;
            final double effectRadius = distance;
            final double knockbackStrength = radius;
            final double distanceThreshold = radius;
            final double verticalOffset = progress;
            int animationStep = 0;

            @Override
            public void run() {
              if (this.animationStep > this.maxCount) {
                this.cancel();
              } else {
                double value = (double) this.animationStep / this.maxCount;
                int count = Math.max(1, (int) Math.ceil(value * this.cooldownTicks));

                for (int index = 0; index <= count; index++) {
                  double distance = (double) index / this.cooldownTicks;
                  double radius = Math.PI * (1.0 - distance);
                  double offset = Math.cos(radius) * this.effectScale;
                  double angle = Math.sin(radius) * this.damageAmount;
                  Vector direction =
                  this.anchorLocation
                  .toVector()
                  .add(this.secondaryDirection.clone().multiply(offset))
                  .add(this.direction.clone().multiply(angle));
                  ParticleEffects.spawnAbilityParticles(
                      world,
                      particle,
                      direction.getX(),
                      direction.getY(),
                      direction.getZ(),
                      ParticleEffects.getAbilityIntValue(this.durationTicks),
                      this.effectRadius,
                      this.knockbackStrength,
                      this.distanceThreshold,
                      this.verticalOffset);
                }

                this.animationStep++;
              }
            }
          };
      pulseParticleOrbitTaskPrimary.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return pulseParticleOrbitTaskPrimary;
    } else {
      return null;
    }
  }

  static void spawnAbilityParticles(
      World world,
      Particle particle,
      double value,
      double distance,
      double radius,
      int count,
      double angle,
      double progress,
      double scale,
      double amount) {
    if (world != null && particle != null) {
      world.spawnParticle(particle, value, distance, radius, Math.max(1, count), angle, progress, scale, amount);
    }
  }

  static <T> void refreshAbilityState(
      World world,
      Particle particle,
      Location location,
      int count,
      double value,
      double distance,
      double radius,
      double angle,
      T t) {
    if (world != null && location != null && particle != null) {
      if (t == null) {
        world.spawnParticle(particle, location, Math.max(1, count), value, distance, radius, angle);
      } else {
        world.spawnParticle(particle, location, Math.max(1, count), value, distance, radius, angle, t, true);
      }
    }
  }

  public static <T> BukkitRunnable createVisualParticleOrbitTask(
      Particle particle,
      Location location,
      Location targetLocation,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress,
      T t) {
    World world = resolveWorld(location);
    if (world != null
        && targetLocation != null
        && targetLocation.getWorld() != null
        && world.equals(targetLocation.getWorld())
        && plugin != null
        && particle != null) {
      int step = Math.max(8, count);
      int ticks = Math.max(1, index);
      Vector direction = targetLocation.toVector().subtract(location.toVector());
      double scale = direction.length();
      if (scale <= 0.001) {
        return null;
      }

      Vector offset = direction.clone().normalize();
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      Location origin = location.clone().add(targetLocation).multiply(0.5);
      double amount = scale / 2.0;
      double speed = Math.max(0.0, value);
      BukkitRunnable visualParticleOrbitTask =
          new BukkitRunnable() {
            final int maxCount = ticks;
            final int durationTicks = step;
            final double distanceThreshold = amount;
            final double damageAmount = speed;
            final Location anchorLocation = origin;
            final Vector direction = normalizedDirection;
            final Vector secondaryDirection = normalizedDirection;
            final int animationStep = remaining;
            final double effectRadius = distance;
            final double verticalOffset = radius;
            final double knockbackStrength = radius;
            final double effectScale = progress;
            final Object particleData = t;
            int cooldownTicks = 0;

            @Override
            public void run() {
              if (this.cooldownTicks > this.maxCount) {
                this.cancel();
              } else {
                double value = (double) this.cooldownTicks / this.maxCount;
                int count = Math.max(1, (int) Math.ceil(value * this.durationTicks));

                for (int index = 0; index <= count; index++) {
                  double distance = (double) index / this.durationTicks;
                  double radius = Math.PI * (1.0 - distance);
                  double offset = Math.cos(radius) * this.distanceThreshold;
                  double angle = Math.sin(radius) * this.damageAmount;
                  Vector direction =
                  this.anchorLocation
                  .toVector()
                  .add(this.direction.clone().multiply(offset))
                  .add(this.secondaryDirection.clone().multiply(angle));
                  ParticleEffects.refreshAbilityState(
                      world,
                      particle,
                      new Location(world, direction.getX(), direction.getY(), direction.getZ()),
                      ParticleEffects.getAbilityIntValue(this.animationStep),
                      this.effectRadius,
                      this.verticalOffset,
                      this.knockbackStrength,
                      this.effectScale,
                      this.particleData);
                }

                this.cooldownTicks++;
              }
            }
          };
      visualParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return visualParticleOrbitTask;
    } else {
      return null;
    }
  }

  static int getAbilityIntValue(int count) {
    return Math.max(1, count);
  }

  public static BukkitRunnable createPulseDustOrbitTask(
      DustOptions dustOptions,
      Location location,
      Location targetLocation,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress) {
    World world = resolveWorld(location);
    if (world != null
        && targetLocation != null
        && targetLocation.getWorld() != null
        && world.equals(targetLocation.getWorld())
        && plugin != null
        && dustOptions != null) {
      int step = Math.max(8, count);
      int ticks = Math.max(1, index);
      Vector direction = targetLocation.toVector().subtract(location.toVector());
      double scale = direction.length();
      if (scale <= 0.001) {
        return null;
      }

      Vector offset = direction.clone().normalize();
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      Location origin = location.clone().add(targetLocation).multiply(0.5);
      double amount = scale / 2.0;
      double speed = Math.max(0.0, value);
      BukkitRunnable pulseDustOrbitTask =
          new BukkitRunnable() {
            final int durationTicks = ticks;
            final int cooldownTicks = step;
            final double verticalOffset = amount;
            final double effectScale = speed;
            final Location anchorLocation = origin;
            final Vector secondaryDirection = normalizedDirection;
            final Vector direction = normalizedDirection;
            final int maxCount = remaining;
            final double distanceThreshold = distance;
            final double knockbackStrength = radius;
            final double damageAmount = radius;
            final double effectRadius = progress;
            int animationStep = 0;

            @Override
            public void run() {
              if (this.animationStep > this.durationTicks) {
                this.cancel();
              } else {
                double value = (double) this.animationStep / this.durationTicks;
                int count = Math.max(1, (int) Math.ceil(value * this.cooldownTicks));

                for (int index = 0; index <= count; index++) {
                  double distance = (double) index / this.cooldownTicks;
                  double radius = Math.PI * (1.0 - distance);
                  double offset = Math.cos(radius) * this.verticalOffset;
                  double angle = Math.sin(radius) * this.effectScale;
                  Vector direction =
                  this.anchorLocation
                  .toVector()
                  .add(this.secondaryDirection.clone().multiply(offset))
                  .add(this.direction.clone().multiply(angle));
                  ParticleEffects.resetAbilityState(
                      world,
                      new Location(world, direction.getX(), direction.getY(), direction.getZ()),
                      ParticleEffects.getAbilityIntValue(this.maxCount),
                      this.distanceThreshold,
                      this.knockbackStrength,
                      this.damageAmount,
                      this.effectRadius,
                      dustOptions);
                }

                this.animationStep++;
              }
            }
          };
      pulseDustOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return pulseDustOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createCompletionParticleOrbitTask(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null) {
      int step = Math.max(8, count);
      int ticks = Math.max(1, index);
      BukkitRunnable completionParticleOrbitTask =
          new BukkitRunnable() {
            final int animationStep = ticks;
            final int maxCount = step;
            final Location anchorLocation = location;
            final double damageAmount = value;
            final int durationTicks = remaining;
            final double effectRadius = distance;
            final double knockbackStrength = radius;
            final double distanceThreshold = angle;
            final double effectScale = progress;
            int cooldownTicks = 0;

            @Override
            public void run() {
              if (this.cooldownTicks > this.animationStep) {
                this.cancel();
              } else {
                double value = (double) this.cooldownTicks / this.animationStep;
                int count = Math.max(1, (int) Math.ceil(value * this.maxCount));

                for (int index = 0; index <= count; index++) {
                  double distance = (double) index / this.maxCount;
                  double radius = Math.PI * distance;
                  double offset = this.anchorLocation.getX() + Math.cos(radius) * this.damageAmount;
                  double angle = this.anchorLocation.getY() + Math.sin(radius) * this.damageAmount;
                  double z = this.anchorLocation.getZ();
                  world.spawnParticle(
                      particle, offset, angle, z, Math.max(1, this.durationTicks), this.effectRadius, this.knockbackStrength, this.distanceThreshold, this.effectScale);
                }

                this.cooldownTicks++;
              }
            }
          };
      completionParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return completionParticleOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createBurstParticleOrbitTask(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null) {
      int step = Math.max(8, count);
      int ticks = Math.max(1, index);
      BukkitRunnable burstParticleOrbitTaskPrimary =
          new BukkitRunnable() {
            final int durationTicks = ticks;
            final int cooldownTicks = step;
            final Location anchorLocation = location;
            final double distanceThreshold = value;
            final int maxCount = remaining;
            final double effectScale = distance;
            final double effectRadius = radius;
            final double damageAmount = angle;
            final double knockbackStrength = progress;
            int animationStep = 0;

            @Override
            public void run() {
              if (this.animationStep > this.durationTicks) {
                this.cancel();
              } else {
                double value = (double) this.animationStep / this.durationTicks;
                int count = Math.max(1, (int) Math.ceil(value * this.cooldownTicks));

                for (int index = 0; index <= count; index++) {
                  double distance = (double) index / this.cooldownTicks;
                  double radius = Math.PI * distance;
                  double offset = this.anchorLocation.getX() + Math.cos(radius) * this.distanceThreshold;
                  double y = this.anchorLocation.getY();
                  double angle = this.anchorLocation.getZ() + Math.sin(radius) * this.distanceThreshold;
                  world.spawnParticle(
                      particle, offset, y, angle, Math.max(1, this.maxCount), this.effectScale, this.effectRadius, this.damageAmount, this.knockbackStrength);
                }

                this.animationStep++;
              }
            }
          };
      burstParticleOrbitTaskPrimary.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return burstParticleOrbitTaskPrimary;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createPulseParticleAnimationTask(
      Particle particle,
      Location location,
      Vector direction,
      double value,
      double distance,
      double radius,
      int count,
      int index,
      double angle,
      double progress,
      double scale,
      double amount,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = getDirectionFromConfig(direction.clone().normalize(), size, speed, volume);
      double height = Math.max(0.05, value);
      int remaining = Math.max(1, count);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      BukkitRunnable pulseParticleAnimationTask =
          new BukkitRunnable() {
            final int maxCount = remaining;
            final double knockbackStrength = height;
            final double damageAmount = x;
            final Vector direction = offset;
            final double horizontalOffset = y;
            final double effectRadius = z;
            final int cooldownTicks = index;
            final double effectScale = z;
            final double verticalOffset = progress;
            final double animationProgress = progress;
            final double distanceThreshold = amount;
            int durationTicks = 0;

            @Override
            public void run() {
              if (this.durationTicks > this.maxCount) {
                this.cancel();
              } else {
                double value = (double) this.durationTicks / this.maxCount;
                double distance = this.knockbackStrength * value;
                double x = this.damageAmount + this.direction.getX() * distance;
                double y = this.horizontalOffset + this.direction.getY() * distance;
                double z = this.effectRadius + this.direction.getZ() * distance;
                world.spawnParticle(
                    particle, x, y, z, Math.max(1, this.cooldownTicks), this.effectScale, this.verticalOffset, this.animationProgress, this.distanceThreshold);
                this.durationTicks++;
              }
            }
          };
      pulseParticleAnimationTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return pulseParticleAnimationTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolveSourceTask(
      DustOptions dustOptions,
      Location location,
      Vector direction,
      double value,
      double distance,
      double radius,
      int count,
      int index,
      double angle,
      double progress,
      double scale,
      double amount,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && plugin != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = getDirectionFromConfig(direction.clone().normalize(), size, speed, volume);
      double height = Math.max(0.05, value);
      double width = Math.max(0.05, distance);
      double stepSize = Math.max(width, radius);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      BukkitRunnable activeDustTrailTask =
          new BukkitRunnable() {
            final int cooldownTicks = count;
            final double horizontalOffset = stepSize;
            final double primaryEffectRadius = z;
            final double effectScale = amount;
            final double damageAmount = x;
            final Vector direction = offset;
            final double effectRadius = y;
            final double animationProgress = z;
            final int maxCount = index;
            final double verticalOffset = x;
            final double knockbackStrength = progress;
            final double maxDistance = z;
            final double distanceThreshold = amount;
            int durationTicks = 0;
            double effectHeight = 0.0;

            @Override
            public void run() {
              if (this.durationTicks >= Math.max(1, this.cooldownTicks)) {
                this.cancel();
              } else {
                double value = Math.max(0.0, this.effectHeight - this.horizontalOffset);
                double distance = Math.min(this.primaryEffectRadius, this.effectHeight);

                for (double radius = value; radius <= distance; radius += this.effectScale) {
                  double x = this.damageAmount + this.direction.getX() * radius;
                  double y = this.effectRadius + this.direction.getY() * radius;
                  double z = this.animationProgress + this.direction.getZ() * radius;
                  world.spawnParticle(
                      Particle.DUST,
                      x,
                      y,
                      z,
                      Math.max(1, this.maxCount),
                      this.verticalOffset,
                      this.knockbackStrength,
                      this.maxDistance,
                      this.distanceThreshold,
                      dustOptions,
                      true);
                }

                this.effectHeight = this.effectHeight + this.horizontalOffset;
                if (this.effectHeight > this.primaryEffectRadius) {
                  this.effectHeight = 0.0;
                }

                this.durationTicks++;
              }
            }
          };
      activeDustTrailTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return activeDustTrailTask;
    } else {
      return null;
    }
  }

  public static void updateAbilityState(
      Particle particle,
      Location location,
      Vector direction,
      double value,
      double distance,
      int count,
      double radius,
      double angle,
      double progress,
      int index,
      double scale,
      double amount,
      double speed,
      double height,
      float size,
      float volume,
      float pitch) {
    World world = resolveWorld(location);
    if (world != null && particle != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = direction.clone().normalize();
      offset = getDirectionFromConfig(offset, size, volume, pitch);
      double width = Math.max(0.05, value);
      double stepSize = Math.max(0.05, distance);
      int remaining = Math.max(1, count);
      double damage = Math.max(0.0, radius);
      double knockback = Math.max(0.0, angle);
      double xOffset = Math.max(0.0, progress);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

      for (double yOffset = 0.0; yOffset <= width; yOffset += stepSize) {
        double zOffset = x + offset.getX() * yOffset;
        double maxDistance = y + offset.getY() * yOffset;
        double minDistance = z + offset.getZ() * yOffset;

        for (int step = 0; step < remaining; step++) {
          double randomX = threadLocalRandom.nextDouble(-damage, damage);
          double randomY = threadLocalRandom.nextDouble(-knockback, knockback);
          double randomZ = threadLocalRandom.nextDouble(-xOffset, xOffset);
          Vector velocity = new Vector(randomX, randomY, randomZ);
          velocity = getDirectionFromConfig(velocity, size, volume, pitch);
          world.spawnParticle(
              particle,
              zOffset + velocity.getX(),
              maxDistance + velocity.getY(),
              minDistance + velocity.getZ(),
              Math.max(1, index),
              scale,
              amount,
              speed,
              height);
        }
      }
    }
  }

  public static void applyAbilityEffects(
      DustOptions dustOptions,
      Location location,
      Vector direction,
      double value,
      double distance,
      int count,
      double radius,
      double angle,
      double progress,
      int index,
      double scale,
      double amount,
      double speed,
      double height,
      float size,
      float volume,
      float pitch) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = direction.clone().normalize();
      offset = getDirectionFromConfig(offset, size, volume, pitch);
      double width = Math.max(0.05, value);
      double stepSize = Math.max(0.05, distance);
      int remaining = Math.max(1, count);
      double damage = Math.max(0.0, radius);
      double knockback = Math.max(0.0, angle);
      double xOffset = Math.max(0.0, progress);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

      for (double yOffset = 0.0; yOffset <= width; yOffset += stepSize) {
        double zOffset = x + offset.getX() * yOffset;
        double maxDistance = y + offset.getY() * yOffset;
        double minDistance = z + offset.getZ() * yOffset;

        for (int step = 0; step < remaining; step++) {
          double randomX = threadLocalRandom.nextDouble(-damage, damage);
          double randomY = threadLocalRandom.nextDouble(-knockback, knockback);
          double randomZ = threadLocalRandom.nextDouble(-xOffset, xOffset);
          Vector velocity = new Vector(randomX, randomY, randomZ);
          velocity = getDirectionFromConfig(velocity, size, volume, pitch);
          world.spawnParticle(
              Particle.DUST,
              zOffset + velocity.getX(),
              maxDistance + velocity.getY(),
              minDistance + velocity.getZ(),
              Math.max(1, index),
              scale,
              amount,
              speed,
              height,
              dustOptions,
              true);
        }
      }
    }
  }

  public static void playAbilityEffects(
      Particle particle,
      Location location,
      Vector direction,
      double value,
      double distance,
      double radius,
      int count,
      int index,
      double angle,
      double progress,
      double scale,
      double amount,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && particle != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = direction.clone().normalize();
      offset = getDirectionFromConfig(offset, size, speed, volume);
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      double height = Math.max(0.05, value);
      double width = Math.max(0.05, distance);
      int remaining = Math.max(3, count);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      double stepSize = (Math.PI * 2) / remaining;

      for (double damage = 0.0; damage <= height; damage += width) {
        double knockback = x + offset.getX() * damage;
        double xOffset = y + offset.getY() * damage;
        double yOffset = z + offset.getZ() * damage;

        for (int step = 0; step < remaining; step++) {
          double zOffset = step * stepSize;
          Vector axis =
              velocity
                  .clone()
                  .multiply(Math.cos(zOffset) * radius)
                  .add(normalizedDirection.clone().multiply(Math.sin(zOffset) * radius));
          world.spawnParticle(
              particle,
              knockback + axis.getX(),
              xOffset + axis.getY(),
              yOffset + axis.getZ(),
              Math.max(1, index),
              angle,
              progress,
              scale,
              amount);
        }
      }
    }
  }

  public static void spawnAbilityEffects(
      DustOptions dustOptions,
      Location location,
      Vector direction,
      double value,
      double distance,
      double radius,
      int count,
      int index,
      double angle,
      double progress,
      double scale,
      double amount,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = direction.clone().normalize();
      offset = getDirectionFromConfig(offset, size, speed, volume);
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      double height = Math.max(0.05, value);
      double width = Math.max(0.05, distance);
      int remaining = Math.max(3, count);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      double stepSize = (Math.PI * 2) / remaining;

      for (double damage = 0.0; damage <= height; damage += width) {
        double knockback = x + offset.getX() * damage;
        double xOffset = y + offset.getY() * damage;
        double yOffset = z + offset.getZ() * damage;

        for (int step = 0; step < remaining; step++) {
          double zOffset = step * stepSize;
          Vector axis =
              velocity
                  .clone()
                  .multiply(Math.cos(zOffset) * radius)
                  .add(normalizedDirection.clone().multiply(Math.sin(zOffset) * radius));
          world.spawnParticle(
              Particle.DUST,
              knockback + axis.getX(),
              xOffset + axis.getY(),
              yOffset + axis.getZ(),
              Math.max(1, index),
              angle,
              progress,
              scale,
              amount,
              dustOptions,
              true);
        }
      }
    }
  }

  public static void cleanupAbilityState(
      Particle particle,
      Location location,
      Vector direction,
      double value,
      double distance,
      int count,
      double radius,
      double angle,
      double progress,
      double scale,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && particle != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = direction.clone().normalize();
      offset = getDirectionFromConfig(offset, size, speed, volume);
      double amount = Math.max(0.05, value);
      double height = Math.max(0.05, distance);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();

      for (double width = 0.0; width <= amount; width += height) {
        double stepSize = x + offset.getX() * width;
        double damage = y + offset.getY() * width;
        double knockback = z + offset.getZ() * width;
        world.spawnParticle(
            particle, stepSize, damage, knockback, Math.max(1, count), radius, angle, progress, scale);
      }
    }
  }

  public static void scheduleAbilityUpdate(
      DustOptions dustOptions,
      Location location,
      Vector direction,
      double value,
      double distance,
      int count,
      double radius,
      double angle,
      double progress,
      double scale,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = direction.clone().normalize();
      offset = getDirectionFromConfig(offset, size, speed, volume);
      double amount = Math.max(0.05, value);
      double height = Math.max(0.05, distance);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();

      for (double width = 0.0; width <= amount; width += height) {
        double stepSize = x + offset.getX() * width;
        double damage = y + offset.getY() * width;
        double knockback = z + offset.getZ() * width;
        world.spawnParticle(
            Particle.DUST,
            stepSize,
            damage,
            knockback,
            Math.max(1, count),
            radius,
            angle,
            progress,
            scale,
            dustOptions,
            true);
      }
    }
  }

  public static BukkitRunnable resolveActiveTask(
      Particle particle, Location location, int count, int index, double value, double distance, double radius) {
    World world = location.getWorld();
    if (world != null && particle != null && plugin != null) {
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      BukkitRunnable orbitParticleOrbitTask =
          new BukkitRunnable() {
            final int maxCount = count;
            final double effectScale = distance;
            final int durationTicks = index;
            final double distanceThreshold = distance;
            final double verticalOffset = radius;
            final double knockbackStrength = x;
            final double horizontalOffset = y;
            final double damageAmount = z;
            int cooldownTicks = 0;
            double effectRadius = 0.0;

            @Override
            public void run() {
              if (this.cooldownTicks >= this.maxCount) {
                this.cancel();
              } else {
                this.effectRadius = this.effectRadius + this.effectScale;

                for (int count = 0; count < this.durationTicks; count++) {
                  double value = this.effectRadius + count * ((Math.PI * 2) / this.durationTicks);
                  double distance = 0.1;
                  double radius = value + distance;
                  double offset = Math.cos(radius) * this.distanceThreshold * this.verticalOffset;
                  double angle = Math.sin(radius) * this.distanceThreshold * this.verticalOffset;
                  world.spawnParticle(particle, this.knockbackStrength, this.horizontalOffset, this.damageAmount, 0, offset, 0.0, angle, 1.0);
                }

                this.cooldownTicks++;
              }
            }
          };
      orbitParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return orbitParticleOrbitTask;
    } else {
      return null;
    }
  }

  static Vector getDirection(Vector direction) {
    Vector offset = direction.clone().normalize();
    Vector velocity = Math.abs(offset.getY()) < 0.99 ? new Vector(0, 1, 0) : new Vector(1, 0, 0);
    return offset.clone().crossProduct(velocity).normalize();
  }

  public static void completeAbilityAction(
      Particle particle,
      Location location,
      Vector direction,
      double value,
      double distance,
      double radius,
      double angle,
      double progress,
      int count,
      double scale,
      double amount,
      double speed,
      double height,
      float size,
      float volume,
      float pitch) {
    World world = resolveWorld(location);
    if (world != null && particle != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = direction.clone().normalize();
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      double width = Math.max(0.1, value);
      double stepSize = Math.max(0.05, distance);
      double damage = Math.max(0.1, angle);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();

      for (double knockback = 0.0; knockback <= width; knockback += stepSize) {
        double xOffset = knockback % damage / damage;
        double yOffset = Math.abs(Math.cos(xOffset * Math.PI));
        double zOffset = radius * yOffset;
        double maxDistance = knockback * progress;
        Vector axis = offset.clone().multiply(knockback);
        axis = getDirectionForTarget(axis, size, volume, pitch);
        double minDistance = x + axis.getX();
        double ratio = y + axis.getY();
        double fraction = z + axis.getZ();

        for (int index = 0; index < 4; index++) {
          double phaseAngle = maxDistance + (Math.PI / 2) * index;
          Vector relativeOffset =
              velocity
                  .clone()
                  .multiply(Math.cos(phaseAngle) * zOffset)
                  .add(normalizedDirection.clone().multiply(Math.sin(phaseAngle) * zOffset));
          relativeOffset = getDirectionForTarget(relativeOffset, size, volume, pitch);
          world.spawnParticle(
              particle,
              minDistance + relativeOffset.getX(),
              ratio + relativeOffset.getY(),
              fraction + relativeOffset.getZ(),
              Math.max(1, count),
              scale,
              amount,
              speed,
              height,
              null,
              true);
        }
      }
    }
  }

  public static void processAbilityState(
      DustOptions dustOptions,
      Location location,
      Vector direction,
      double value,
      double distance,
      double radius,
      double angle,
      double progress,
      int count,
      double scale,
      double amount,
      double speed,
      double height,
      float size,
      float volume,
      float pitch) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = direction.clone().normalize();
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      double width = Math.max(0.1, value);
      double stepSize = Math.max(0.05, distance);
      double damage = Math.max(0.1, angle);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();

      for (double knockback = 0.0; knockback <= width; knockback += stepSize) {
        double xOffset = knockback % damage / damage;
        double yOffset = Math.abs(Math.cos(xOffset * Math.PI));
        double zOffset = radius * yOffset;
        double maxDistance = knockback * progress;
        Vector axis = offset.clone().multiply(knockback);
        axis = getDirectionForTarget(axis, size, volume, pitch);
        double minDistance = x + axis.getX();
        double ratio = y + axis.getY();
        double fraction = z + axis.getZ();

        for (int index = 0; index < 4; index++) {
          double phaseAngle = maxDistance + (Math.PI / 2) * index;
          Vector relativeOffset =
              velocity
                  .clone()
                  .multiply(Math.cos(phaseAngle) * zOffset)
                  .add(normalizedDirection.clone().multiply(Math.sin(phaseAngle) * zOffset));
          relativeOffset = getDirectionForTarget(relativeOffset, size, volume, pitch);
          world.spawnParticle(
              Particle.DUST,
              minDistance + relativeOffset.getX(),
              ratio + relativeOffset.getY(),
              fraction + relativeOffset.getZ(),
              Math.max(1, count),
              scale,
              amount,
              speed,
              height,
              dustOptions,
              true);
        }
      }
    }
  }

  public static BukkitRunnable resolvePendingTask(
      Particle particle,
      Location location,
      Location targetLocation,
      int count,
      double value,
      double distance,
      double radius,
      double angle,
      int index,
      double progress,
      int remaining,
      int step,
      double scale,
      double amount,
      double speed,
      double height,
      float size,
      float volume,
      float pitch) {
    World world = location == null ? null : location.getWorld();
    if (world != null
        && targetLocation != null
        && targetLocation.getWorld() != null
        && world.equals(targetLocation.getWorld())
        && plugin != null
        && particle != null) {
      Vector direction = targetLocation.toVector().subtract(location.toVector());
      double width = direction.length();
      if (width <= 1.0E-4) {
        return null;
      }

      Vector offset = direction.clone().normalize();
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      Vector knockback = offset.clone();
      Vector axis = getDirectionFromConfig(velocity, size, volume, pitch).normalize();
      Vector relativeOffset = getDirectionFromConfig(normalizedDirection, size, volume, pitch).normalize();
      Vector movement = location.toVector();
      int ticks = Math.max(1, count);
      int samplesPerBeam = Math.max(6, remaining);
      int attempts = Math.max(1, index);
      int level = Math.max(1, step);
      double stepSize = Math.max(0.01, angle);
      double damage = Math.max(0.1, progress);
      double xOffset = Math.max(width, progress);
      BukkitRunnable activeParticleOrbitTask =
          new BukkitRunnable() {
            final int chargeLevel = attempts;
            final double verticalOffset = stepSize;
            final double effectHeight = damage;
            final double effectScale = xOffset;
            final int cooldownTicks = ticks;
            final double knockbackStrength = value;
            final int maxCount = samplesPerBeam;
            final double animationProgress = radius;
            final double horizontalOffset = distance;
            final Vector secondaryDirection = movement;
            final Vector activeDirection = knockback;
            final Vector direction = axis;
            final Vector currentDirection = relativeOffset;
            final int animationStep = level;
            final double particleOffsetX = scale;
            final double particleOffsetY = amount;
            final double particleOffsetZ = speed;
            final double particleExtra = height;
            int durationTicks = 0;
            double effectRadius = 0.0;

            @Override
            public void run() {
              if (this.durationTicks >= this.chargeLevel) {
                this.cancel();
              } else {
                this.effectRadius = this.effectRadius + this.verticalOffset;
                double value = Math.max(0.0, this.effectRadius - this.effectHeight);
                double distance = Math.min(this.effectScale, this.effectRadius);
                if (value > this.effectScale) {
                  this.cancel();
                } else {
                  for (int count = 0; count < this.cooldownTicks; count++) {
                    double radius = (count - (this.cooldownTicks - 1) / 2.0) * this.knockbackStrength;

                    for (int index = 0; index <= this.maxCount; index++) {
                      double angle = (double) index / this.maxCount;
                      double progress = value + (distance - value) * angle;
                      double scale = progress - this.effectRadius;
                      double offset = Math.sin(scale * this.animationProgress) * this.horizontalOffset;
                      double amount = Math.cos(scale * this.animationProgress) * this.horizontalOffset * 0.35;
                      Vector direction =
                      this.secondaryDirection
                      .clone()
                      .add(this.activeDirection.clone().multiply(progress))
                      .add(this.direction.clone().multiply(radius + offset))
                      .add(this.currentDirection.clone().multiply(amount));
                      world.spawnParticle(
                          particle,
                          direction.getX(),
                          direction.getY(),
                          direction.getZ(),
                          this.animationStep,
                          this.particleOffsetX,
                          this.particleOffsetY,
                          this.particleOffsetZ,
                          this.particleExtra,
                          null,
                          true);
                    }
                  }

                  this.durationTicks++;
                }
              }
            }
          };
      activeParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return activeParticleOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolveCachedTask(
      Particle particle,
      Location location,
      Location targetLocation,
      int count,
      double value,
      double distance,
      double radius,
      int index,
      double angle,
      int remaining,
      int step,
      double progress,
      double scale,
      double amount,
      double speed,
      double height,
      float size,
      float volume,
      float pitch) {
    World world = location == null ? null : location.getWorld();
    if (world != null
        && targetLocation != null
        && targetLocation.getWorld() != null
        && world.equals(targetLocation.getWorld())
        && plugin != null
        && particle != null) {
      Vector direction = targetLocation.toVector().subtract(location.toVector());
      double width = direction.length();
      if (width <= 0.001) {
        return null;
      }

      int ticks = Math.max(1, count);
      int samplesPerCurve = Math.max(4, remaining);
      int attempts = Math.max(1, index);
      double offset = Math.max(0.01, radius);
      double stepSize = Math.max(0.1, angle);
      double damage = Math.max(0.0, height);
      ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
      ArrayList<ParticleCurvePoint> entries = new ArrayList<>();

      for (int level = 0; level < ticks; level++) {
        Vector velocity =
            new Vector(
                threadLocalRandom.nextDouble(-damage, damage),
                threadLocalRandom.nextDouble(-damage, damage),
                threadLocalRandom.nextDouble(-damage, damage));
        velocity = getDirectionFromConfig(velocity, size, volume, pitch);
        Location origin = location.clone().add(velocity);
        Location center = targetLocation.clone().add(velocity);
        Vector normalizedDirection = center.toVector().subtract(origin.toVector());
        double knockback = normalizedDirection.length();
        if (!(knockback <= 0.001)) {
          Vector axis = normalizedDirection.clone().normalize();
          Vector relativeOffset = getDirection(axis);
          Vector movement = axis.clone().crossProduct(relativeOffset).normalize();
          double xOffset = (Math.PI * 2) * level / ticks;
          entries.add(new ParticleCurvePoint(origin, axis, relativeOffset, movement, knockback, xOffset));
        }
      }

      if (entries.isEmpty()) {
        return null;
      }

      BukkitRunnable waveParticleOrbitTask =
          new BukkitRunnable() {
            final int cooldownTicks = attempts;
            final double effectScale = offset;
            final double effectRadius = stepSize;
            final int maxCount = samplesPerCurve;
            final double distanceThreshold = distance;
            final double animationProgress = value;
            final int particleCount = step;
            final double knockbackStrength = progress;
            final double horizontalOffset = scale;
            final double verticalOffset = amount;
            final double damageAmount = speed;
            int animationStep = 0;

            @Override
            public void run() {
              if (this.animationStep >= this.cooldownTicks) {
                this.cancel();
              } else {
                boolean enabled = false;

                for (ParticleCurvePoint particleCurvePoint : entries) {
                  particleCurvePoint.effectScale = particleCurvePoint.effectScale + this.effectScale;
                  double value = Math.max(0.0, particleCurvePoint.effectScale - this.effectRadius);
                  double distance = Math.min(particleCurvePoint.distanceThreshold, particleCurvePoint.effectScale);
                  if (value <= particleCurvePoint.distanceThreshold) {
                    enabled = true;
                  }

                  for (int count = 0; count <= this.maxCount; count++) {
                    double radius = (double) count / this.maxCount;
                    double angle = value + (distance - value) * radius;
                    double progress = angle * this.distanceThreshold + particleCurvePoint.effectRadius;
                    double offset = Math.sin(progress) * this.animationProgress;
                    double scale = Math.cos(progress * 0.7) * this.animationProgress * 0.25;
                    Vector direction =
                    particleCurvePoint.anchorLocation
                    .toVector()
                    .add(particleCurvePoint.activeDirection.clone().multiply(angle))
                    .add(particleCurvePoint.direction.clone().multiply(offset))
                    .add(particleCurvePoint.secondaryDirection.clone().multiply(scale));
                    world.spawnParticle(
                        particle,
                        direction.getX(),
                        direction.getY(),
                        direction.getZ(),
                        Math.max(1, this.particleCount),
                        this.knockbackStrength,
                        this.horizontalOffset,
                        this.verticalOffset,
                        this.damageAmount,
                        null,
                        true);
                  }
                }

                if (!enabled) {
                  this.cancel();
                } else {
                  this.animationStep++;
                }
              }
            }
          };
      waveParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return waveParticleOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolveResolvedTask(
      Particle particle,
      Location location,
      Location targetLocation,
      int count,
      double value,
      double distance,
      double radius,
      int index,
      int remaining,
      int step,
      double angle,
      double progress,
      double scale,
      double amount) {
    World world = location == null ? null : location.getWorld();
    if (world != null
        && targetLocation != null
        && targetLocation.getWorld() != null
        && world.equals(targetLocation.getWorld())
        && plugin != null
        && particle != null) {
      Vector direction = targetLocation.toVector().subtract(location.toVector());
      double speed = direction.length();
      if (speed <= 0.001) {
        return null;
      }

      Vector offset = direction.clone().normalize();
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      int ticks = Math.max(1, count);
      int durationTicks = Math.max(6, remaining);
      int attempts = Math.max(1, index);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      int level = Math.max(1, step);
      BukkitRunnable ambientParticleOrbitTask =
          new BukkitRunnable() {
            final int durationTicks = attempts;
            final double damageAmount = radius;
            final int cooldownTicks = ticks;
            final int chargeLevel = durationTicks;
            final double maxDistance = amount;
            final double effectRadius = distance;
            final double animationProgress = value;
            final double knockbackStrength = x;
            final double distanceThreshold = y;
            final double horizontalOffset = z;
            final Vector secondaryDirection = velocity;
            final Vector direction = velocity;
            final Vector activeDirection = normalizedDirection;
            final int maxCount = level;
            final double effectScale = amount;
            int animationStep = 0;
            double verticalOffset = 0.0;

            @Override
            public void run() {
              if (this.animationStep >= this.durationTicks) {
                this.cancel();
              } else {
                this.verticalOffset = this.verticalOffset + this.damageAmount;

                for (int count = 0; count < this.cooldownTicks; count++) {
                  double value = (Math.PI * 2) * count / this.cooldownTicks;

                  for (int index = 0; index <= this.chargeLevel; index++) {
                    double distance = (double) index / this.chargeLevel;
                    double radius = distance * this.maxDistance;
                    double angle = radius * this.effectRadius + this.verticalOffset + value;
                    double offset = Math.sin(angle) * this.animationProgress;
                    double progress = Math.cos(angle * 0.65) * this.animationProgress * 0.22;
                    Vector velocity =
                    new Vector(this.knockbackStrength, this.distanceThreshold, this.horizontalOffset)
                    .add(this.secondaryDirection.clone().multiply(radius))
                    .add(this.direction.clone().multiply(offset))
                    .add(this.activeDirection.clone().multiply(progress));
                    world.spawnParticle(
                        particle,
                        velocity.getX(),
                        velocity.getY(),
                        velocity.getZ(),
                        this.maxCount,
                        0.0,
                        0.0,
                        0.0,
                        this.effectScale,
                        null,
                        true);
                  }
                }

                this.animationStep++;
              }
            }
          };
      ambientParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return ambientParticleOrbitTask;
    } else {
      return null;
    }
  }

  public static <T> BukkitRunnable resolveStoredTask(
      Particle particle,
      Location location,
      Location targetLocation,
      int count,
      double value,
      double distance,
      double radius,
      int index,
      int remaining,
      int step,
      double angle,
      double progress,
      double scale,
      double amount,
      T t) {
    World world = resolveWorld(location);
    if (world != null
        && targetLocation != null
        && targetLocation.getWorld() != null
        && world.equals(targetLocation.getWorld())
        && plugin != null
        && particle != null) {
      Vector direction = targetLocation.toVector().subtract(location.toVector());
      double speed = direction.length();
      if (speed <= 0.001) {
        return null;
      }

      Vector offset = direction.clone().normalize();
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      int ticks = Math.max(1, count);
      int samplesPerRing = Math.max(8, remaining);
      int attempts = Math.max(1, index);
      double height = Math.max(0.001, radius);
      BukkitRunnable delayedParticleOrbitTask =
          new BukkitRunnable() {
            final int chargeLevel = attempts;
            final double verticalOffset = height;
            final int cooldownTicks = ticks;
            final double effectRadius = amount;
            final double animationProgress = distance;
            final double knockbackStrength = value;
            final Location anchorLocation = location;
            final Vector direction = velocity;
            final Vector secondaryDirection = velocity;
            final Vector activeDirection = normalizedDirection;
            final int animationStep = step;
            final double damageAmount = value;
            final double horizontalOffset = progress;
            final double effectScale = progress;
            final double maxDistance = amount;
            final Object particleData = t;
            int maxCount = 0;
            double distanceThreshold = 0.0;

            @Override
            public void run() {
              if (this.maxCount >= this.chargeLevel) {
                this.cancel();
              } else {
                this.distanceThreshold = this.distanceThreshold + this.verticalOffset;

                for (int count = 0; count < this.cooldownTicks; count++) {
                  double value = (Math.PI * 2) * count / this.cooldownTicks;

                  for (int index = 0; index <= samplesPerRing; index++) {
                    double distance = (double) index / samplesPerRing;
                    double radius = distance * this.effectRadius;
                    double angle = radius * this.animationProgress + this.distanceThreshold + value;
                    double offset = Math.sin(angle) * this.knockbackStrength;
                    double progress = Math.cos(angle * 0.7) * this.knockbackStrength * 0.35;
                    Vector direction =
                    this.anchorLocation
                    .toVector()
                    .add(this.direction.clone().multiply(radius))
                    .add(this.secondaryDirection.clone().multiply(offset))
                    .add(this.activeDirection.clone().multiply(progress));
                    world.spawnParticle(
                        particle,
                        direction.getX(),
                        direction.getY(),
                        direction.getZ(),
                        Math.max(1, this.animationStep),
                        this.damageAmount,
                        this.horizontalOffset,
                        this.effectScale,
                        this.maxDistance,
                        this.particleData,
                        true);
                  }
                }

                this.maxCount++;
              }
            }
          };
      delayedParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return delayedParticleOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolveTrackedTask(
      DustOptions dustOptions,
      Location location,
      Location targetLocation,
      int count,
      double value,
      double distance,
      double radius,
      int index,
      int remaining,
      int step,
      double angle,
      double progress,
      double scale,
      double amount) {
    World world = resolveWorld(location);
    if (world != null
        && targetLocation != null
        && targetLocation.getWorld() != null
        && world.equals(targetLocation.getWorld())
        && plugin != null
        && dustOptions != null) {
      Vector direction = targetLocation.toVector().subtract(location.toVector());
      double speed = direction.length();
      if (speed <= 0.001) {
        return null;
      }

      Vector offset = direction.clone().normalize();
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      int ticks = Math.max(1, count);
      int samplesPerRing = Math.max(8, remaining);
      int attempts = Math.max(1, index);
      double height = Math.max(0.001, radius);
      BukkitRunnable ambientDustOrbitTask =
          new BukkitRunnable() {
            final int chargeLevel = attempts;
            final double effectRadius = height;
            final int cooldownTicks = ticks;
            final int maxCount = samplesPerRing;
            final double beamLength = speed;
            final double phaseScale = distance;
            final double waveAmplitude = value;
            final Location anchorLocation = location;
            final Vector forwardDirection = offset;
            final Vector sideDirection = velocity;
            final Vector verticalDirection = normalizedDirection;
            final int particleCount = step;
            final double particleOffsetX = angle;
            final double particleOffsetY = progress;
            final double particleOffsetZ = scale;
            final double particleExtra = amount;
            int animationStep = 0;
            double animationProgress = 0.0;

            @Override
            public void run() {
              if (this.animationStep >= this.chargeLevel) {
                this.cancel();
              } else {
                this.animationProgress = this.animationProgress + this.effectRadius;

                for (int count = 0; count < this.cooldownTicks; count++) {
                  double value = (Math.PI * 2) * count / this.cooldownTicks;

                  for (int index = 0; index <= this.maxCount; index++) {
                    double distance = (double) index / this.maxCount;
                    double radius = distance * this.beamLength;
                    double angle = radius * this.phaseScale + this.animationProgress + value;
                    double offset = Math.sin(angle) * this.waveAmplitude;
                    double progress = Math.cos(angle * 0.7) * this.waveAmplitude * 0.35;
                    Vector direction =
                        this.anchorLocation
                            .toVector()
                            .add(this.forwardDirection.clone().multiply(radius))
                            .add(this.sideDirection.clone().multiply(offset))
                            .add(this.verticalDirection.clone().multiply(progress));
                    world.spawnParticle(
                        Particle.DUST,
                        direction.getX(),
                        direction.getY(),
                        direction.getZ(),
                        Math.max(1, this.particleCount),
                        this.particleOffsetX,
                        this.particleOffsetY,
                        this.particleOffsetZ,
                        this.particleExtra,
                        dustOptions,
                        true);
                  }
                }

                this.animationStep++;
              }
            }
          };
      ambientDustOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return ambientDustOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolvePreviousTask(
      Particle particle,
      Location location,
      Vector direction,
      double value,
      double distance,
      double radius,
      double angle,
      double progress,
      double scale,
      int count,
      int index,
      double amount,
      double speed,
      double height,
      double width,
      float size,
      float volume,
      float pitch) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null && direction != null && direction.lengthSquared() != 0.0) {
      Vector offset = direction.clone().normalize();
      Vector velocity = getDirection(offset);
      Vector normalizedDirection = offset.clone().crossProduct(velocity).normalize();
      double stepSize = Math.max(0.1, value);
      double damage = Math.max(0.05, distance);
      double knockback = Math.max(0.1, angle);
      double xOffset = Math.max(damage, scale);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      BukkitRunnable pulseParticleOrbitTask =
          new BukkitRunnable() {
            final int durationTicks = count;
            final double effectHeight = xOffset;
            final double pendingEffectRadius = x;
            final double effectRadius = volume;
            final double knockbackStrength = knockback;
            final double animationProgress = volume;
            final double activeEffectRadius = progress;
            final Vector direction = velocity;
            final float soundVolume = size;
            final float effectScale = volume;
            final float soundPitch = pitch;
            final double distanceThreshold = x;
            final double targetEffectRadius = y;
            final double primaryEffectRadius = z;
            final Vector secondaryDirection = velocity;
            final Vector activeDirection = normalizedDirection;
            final int maxCount = index;
            final double maxDistance = x;
            final double damageAmount = volume;
            final double sourceEffectRadius = y;
            final double horizontalOffset = z;
            int cooldownTicks = 0;
            double verticalOffset = 0.0;

            @Override
            public void run() {
              if (this.cooldownTicks >= Math.max(1, this.durationTicks)) {
                this.cancel();
              } else {
                double value = Math.max(0.0, this.verticalOffset - this.effectHeight);
                double distance = Math.min(this.pendingEffectRadius, this.verticalOffset);

                for (double radius = value; radius <= distance; radius += this.effectRadius) {
                  double angle = radius % this.knockbackStrength / this.knockbackStrength;
                  double offset = Math.abs(Math.cos(angle * Math.PI));
                  double progress = this.animationProgress * offset;
                  double scale = radius * this.activeEffectRadius;
                  Vector direction = this.direction.clone().multiply(radius);
                  direction = ParticleEffects.getDirectionForTarget(direction, this.soundVolume, this.effectScale, this.soundPitch);
                  double x = this.distanceThreshold + direction.getX();
                  double y = this.targetEffectRadius + direction.getY();
                  double z = this.primaryEffectRadius + direction.getZ();

                  for (int count = 0; count < 4; count++) {
                    double amount = scale + (Math.PI / 2) * count;
                    Vector velocity =
                    this.secondaryDirection
                    .clone()
                    .multiply(Math.cos(amount) * progress)
                    .add(this.activeDirection.clone().multiply(Math.sin(amount) * progress));
                    velocity = ParticleEffects.getDirectionForTarget(velocity, this.soundVolume, this.effectScale, this.soundPitch);
                    world.spawnParticle(
                        particle,
                        x + velocity.getX(),
                        y + velocity.getY(),
                        z + velocity.getZ(),
                        Math.max(1, this.maxCount),
                        this.maxDistance,
                        this.damageAmount,
                        this.sourceEffectRadius,
                        this.horizontalOffset,
                        null,
                        true);
                  }
                }

                this.verticalOffset = this.verticalOffset + this.effectHeight;
                if (this.verticalOffset > this.pendingEffectRadius) {
                  this.verticalOffset = 0.0;
                }

                this.cooldownTicks++;
              }
            }
          };
      pulseParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return pulseParticleOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolveNextTask(
      Particle particle, Location location, double value, int count, int index, double distance, int remaining) {
    World world = location.getWorld();
    if (world != null && particle != null && plugin != null) {
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      BukkitRunnable burstParticleOrbitTask =
          new BukkitRunnable() {
            final int animationStep = count;
            final int maxCount = index;
            final double distanceThreshold = value;
            final double knockbackStrength = x;
            final double damageAmount = z;
            final double effectRadius = y;
            final int durationTicks = remaining;
            final double effectScale = distance;
            int cooldownTicks = 0;

            @Override
            public void run() {
              if (this.cooldownTicks >= this.animationStep) {
                this.cancel();
              } else {
                for (int count = 0; count < this.maxCount; count++) {
                  double value = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
                  double distance = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * this.distanceThreshold;
                  double offset = this.knockbackStrength + Math.cos(value) * distance;
                  double radius = this.damageAmount + Math.sin(value) * distance;
                  world.spawnParticle(
                      particle,
                      this.knockbackStrength,
                      this.effectRadius,
                      this.damageAmount,
                      this.durationTicks,
                      Math.cos(value) * this.effectScale,
                      0.0,
                      Math.sin(value) * this.effectScale,
                      0.0);
                }

                this.cooldownTicks++;
              }
            }
          };
      burstParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return burstParticleOrbitTask;
    } else {
      return null;
    }
  }

  static World resolveWorld(Location location) {
    return location == null ? null : location.getWorld();
  }

  static void handleAbilityAction(
      World world,
      Particle particle,
      Location location,
      int count,
      double value,
      double distance,
      double radius,
      double angle) {
    if (world != null && location != null && particle != null) {
      world.spawnParticle(particle, location, Math.max(1, count), value, distance, radius, angle);
    }
  }

  static void resetAbilityState(
      World world,
      Location location,
      int count,
      double value,
      double distance,
      double radius,
      double angle,
      DustOptions dustOptions) {
    if (world != null && location != null && dustOptions != null) {
      world.spawnParticle(
          Particle.DUST, location, Math.max(1, count), value, distance, radius, angle, dustOptions, true);
    }
  }

  public static Vector getDirectionForPlayer(Vector direction, float scale) {
    double angleRadians = Math.toRadians(scale);
    double offset = Math.cos(angleRadians);
    double distance = Math.sin(angleRadians);
    double x = direction.getX() * offset - direction.getZ() * distance;
    double radius = direction.getX() * distance + direction.getZ() * offset;
    return new Vector(x, direction.getY(), radius);
  }

  public static BukkitRunnable resolveDefaultTask(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      double distance,
      double radius,
      double angle,
      double progress,
      int remaining) {
    World world = location.getWorld();
    if (world != null && particle != null && plugin != null) {
      int step = Math.max(1, index);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      BukkitRunnable particleOrbitTask =
          new BukkitRunnable() {
            final int cooldownTicks = count;
            final double effectRadius = radius;
            final double animationProgress = distance;
            final double effectHeight = radius;
            final int animationStep = step;
            final double maxDistance = radius;
            final double damageAmount = distance;
            final double knockbackStrength = x;
            final double effectScale = z;
            final double horizontalOffset = y;
            final int maxCount = remaining;
            int durationTicks = 0;
            double distanceThreshold = 0.0;
            double verticalOffset = 0.0;

            @Override
            public void run() {
              if (this.durationTicks >= this.cooldownTicks) {
                this.cancel();
              } else {
                this.distanceThreshold = this.distanceThreshold + this.effectRadius;
                this.verticalOffset = this.verticalOffset + this.animationProgress;
                double value = this.verticalOffset % this.effectHeight;

                for (int count = 0; count < this.animationStep; count++) {
                  double distance = this.distanceThreshold + (Math.PI * 2) * count / this.animationStep;
                  double offset = Math.sin(value * this.maxDistance) * this.damageAmount;
                  double radius = distance + offset;
                  double angle = this.knockbackStrength + Math.cos(radius) * value;
                  double progress = this.effectScale + Math.sin(radius) * value;
                  world.spawnParticle(particle, angle, this.horizontalOffset, progress, this.maxCount, 0.0, 0.0, 0.0, 0.0);
                }

                this.durationTicks++;
              }
            }
          };
      particleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return particleOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolveFallbackTask(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      double distance,
      double radius,
      double angle,
      double progress,
      int remaining,
      BlockData blockData) {
    World world = location.getWorld();
    if (world != null && particle != null && plugin != null) {
      int step = Math.max(1, index);
      double x = location.getX();
      double y = location.getY();
      double z = location.getZ();
      BukkitRunnable trailParticleOrbitTask =
          new BukkitRunnable() {
            final int cooldownTicks = count;
            final double damageAmount = radius;
            final double effectScale = distance;
            final double verticalOffset = radius;
            final int maxCount = step;
            final double animationProgress = radius;
            final double maxDistance = distance;
            final double distanceThreshold = x;
            final double horizontalOffset = z;
            final double effectRadius = y;
            final int durationTicks = remaining;
            int animationStep = 0;
            double knockbackStrength = 0.0;
            double effectHeight = 0.0;

            @Override
            public void run() {
              if (this.animationStep >= this.cooldownTicks) {
                this.cancel();
              } else {
                this.knockbackStrength = this.knockbackStrength + this.damageAmount;
                this.effectHeight = this.effectHeight + this.effectScale;
                double value = this.effectHeight % this.verticalOffset;

                for (int count = 0; count < this.maxCount; count++) {
                  double distance = this.knockbackStrength + (Math.PI * 2) * count / this.maxCount;
                  double offset = Math.sin(value * this.animationProgress) * this.maxDistance;
                  double radius = distance + offset;
                  double angle = this.distanceThreshold + Math.cos(radius) * value;
                  double progress = this.horizontalOffset + Math.sin(radius) * value;
                  world.spawnParticle(particle, angle, this.effectRadius, progress, this.durationTicks, 0.0, 0.0, 0.0, blockData);
                }

                this.animationStep++;
              }
            }
          };
      trailParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return trailParticleOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolveTemporaryTask(
      DustOptions dustOptions,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress,
      double scale,
      int step,
      double amount,
      double speed,
      double height,
      double width,
      float size,
      float volume,
      float pitch) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && plugin != null) {
      int ticks = Math.max(1, count);
      int durationTicks = Math.max(1, index);
      int attempts = Math.max(8, remaining);
      BukkitRunnable dustOrbitTask =
          new BukkitRunnable() {
            final int animationStep = ticks;
            final double maxDistance = progress;
            final double effectScale = distance;
            final int cooldownTicks = attempts;
            final double damageAmount = progress;
            final double effectRadius = angle;
            final double knockbackStrength = progress;
            final double animationProgress = distance;
            final float particleSize = size;
            final float soundPitch = volume;
            final float soundVolume = pitch;
            final Location anchorLocation = location;
            final int maxCount = step;
            final double effectHeight = amount;
            final double targetEffectRadius = volume;
            final double primaryEffectRadius = height;
            final double distanceThreshold = height;
            int chargeLevel = 0;
            double verticalOffset = 0.0;
            double horizontalOffset = 0.0;

            @Override
            public void run() {
              if (this.chargeLevel >= this.animationStep) {
                this.cancel();
              } else {
                this.verticalOffset = this.verticalOffset + this.maxDistance;
                this.horizontalOffset = this.horizontalOffset + this.effectScale;

                for (int count = 0; count < durationTicks; count++) {
                  double value = this.verticalOffset + (Math.PI * 2) * count / durationTicks;

                  for (int index = 0; index < this.cooldownTicks; index++) {
                    double distance = (double) index / (this.cooldownTicks - 1);
                    double radius = this.damageAmount * distance;
                    double offset = Math.sin(radius * this.effectRadius + this.horizontalOffset) * this.knockbackStrength;
                    double angle = value + offset;
                    double progress = Math.cos(angle) * radius;
                    double scale = Math.sin(angle) * radius;
                    Vector velocity = new Vector(progress, 0.0, scale);
                    if (this.animationProgress > 0.0) {
                      double amount = angle + (Math.PI / 2);
                      double speed = Math.cos(amount) * (Math.sin(radius * this.effectRadius + this.horizontalOffset) * this.animationProgress);
                      double height = Math.sin(amount) * (Math.sin(radius * this.effectRadius + this.horizontalOffset) * this.animationProgress);
                      velocity.add(new Vector(speed, 0.0, height));
                    }

                    velocity = ParticleEffects.getDirectionFromConfig(velocity, this.particleSize, this.soundPitch, this.soundVolume);
                    Location location = this.anchorLocation.clone().add(velocity);
                    ParticleEffects.resetAbilityState(
                        world, location, this.maxCount, this.effectHeight, this.targetEffectRadius, this.primaryEffectRadius, this.distanceThreshold, dustOptions);
                  }
                }

                this.chargeLevel++;
              }
            }
          };
      dustOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return dustOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolveWorkingTask(
      Particle particle, Location location, double value, int count) {
    return resolveDefaultTask(
        particle, location, value, count, 4, 0.25, 2.0, 0.02, 0.25, 1);
  }

  public static BukkitRunnable resolveBaselineTask(
      DustOptions dustOptions, Location location, double value, int count) {
    return resolveTemporaryTask(
        dustOptions, location, value, count, 4, 60, 0.1, 0.25, 2.0, 0.08, 0.35, 1, 0.0, 0.0, 0.0, 0.0, 0.0F,
        0.0F, 0.0F);
  }

  public static Vector getDirectionForTarget(
      Vector direction, float scale, float size, float speed) {
    double angleRadians = Math.toRadians(scale);
    double distance = Math.toRadians(size);
    double radius = Math.toRadians(speed);
    Vector offset = direction.clone();
    double angle = Math.cos(radius);
    double progress = Math.sin(radius);
    double x = offset.getX() * angle - offset.getY() * progress;
    double amount = offset.getX() * progress + offset.getY() * angle;
    offset.setX(x).setY(amount);
    double height = Math.cos(distance);
    double width = Math.sin(distance);
    double y = offset.getY() * height - offset.getZ() * width;
    double stepSize = offset.getY() * width + offset.getZ() * height;
    offset.setY(y).setZ(stepSize);
    double damage = Math.cos(angleRadians);
    double knockback = Math.sin(angleRadians);
    double xOffset = offset.getX() * damage + offset.getZ() * knockback;
    double yOffset = -offset.getX() * knockback + offset.getZ() * damage;
    offset.setX(xOffset).setZ(yOffset);
    return offset;
  }

  public static Vector getDirectionFromConfig(
      Vector direction, float scale, float size, float speed) {
    double angleRadians = Math.toRadians(scale);
    double distance = Math.toRadians(size);
    double radius = Math.toRadians(speed);
    double x = direction.getX();
    double y = direction.getY();
    double z = direction.getZ();
    double offset = Math.cos(angleRadians);
    double angle = Math.sin(angleRadians);
    double value = x * offset - z * angle;
    double progress = x * angle + z * offset;
    double amount = Math.cos(distance);
    double height = Math.sin(distance);
    double width = y * amount - progress * height;
    double stepSize = y * height + progress * amount;
    double damage = Math.cos(radius);
    double knockback = Math.sin(radius);
    double xOffset = value * damage - width * knockback;
    double yOffset = value * knockback + width * damage;
    return new Vector(xOffset, yOffset, stepSize);
  }

  public static BukkitRunnable resolveUpdatedTask(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      double distance,
      int remaining,
      int step,
      double radius,
      double angle,
      double progress,
      double scale,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null) {
      int ticks = Math.max(3, count);
      int activeDurationTicks = Math.max(0, index);
      int fadeDurationTicks = Math.max(1, remaining);
      final float particleSize = size;
      final float particleSpeed = speed;
      final float particleVolume = volume;
      BukkitRunnable ambientParticleAnimationTask =
          new BukkitRunnable() {
            final int activeTicks = activeDurationTicks;
            final Location anchorLocation = location;
            final double initialValue = value;
            final int particleCount = ticks;
            final int sampleCount = step;
            final double particleOffsetX = radius;
            final double particleOffsetY = angle;
            final double particleOffsetZ = progress;
            final double particleExtra = scale;
            final float size = particleSize;
            final float speed = particleSpeed;
            final float volume = particleVolume;
            final int fadeTicks = fadeDurationTicks;
            final double fadeDistance = distance;
            int elapsedTicks = 0;

            @Override
            public void run() {
              if (this.elapsedTicks < this.activeTicks) {
                ParticleEffects.updateState(
                    particle,
                    this.anchorLocation,
                    this.initialValue,
                    this.particleCount,
                    this.sampleCount,
                    this.particleOffsetX,
                    this.particleOffsetY,
                    this.particleOffsetZ,
                    this.particleExtra,
                    this.size,
                    this.speed,
                    this.volume);
                this.elapsedTicks++;
              } else {
                int fadeStep = this.elapsedTicks - this.activeTicks;
                if (fadeStep > this.fadeTicks) {
                  this.cancel();
                } else {
                  double fadeValue = this.fadeDistance / this.fadeTicks * fadeStep;
                  ParticleEffects.updateState(
                      particle,
                      this.anchorLocation,
                      fadeValue,
                      this.particleCount,
                      this.sampleCount,
                      this.particleOffsetX,
                      this.particleOffsetY,
                      this.particleOffsetZ,
                      this.particleExtra,
                      this.size,
                      this.speed,
                      this.volume);
                  this.elapsedTicks++;
                }
              }
            }
          };
      ambientParticleAnimationTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return ambientParticleAnimationTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolveEffectiveTask(
      DustOptions dustOptions,
      Location location,
      double value,
      int count,
      int index,
      double distance,
      int remaining,
      int step,
      double radius,
      double angle,
      double progress,
      double scale,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && plugin != null) {
      int ticks = Math.max(3, count);
      int activeDurationTicks = Math.max(0, index);
      int fadeDurationTicks = Math.max(1, remaining);
      final float particleSize = size;
      final float particleSpeed = speed;
      final float particleVolume = volume;
      BukkitRunnable ambientDustTrailTask =
          new BukkitRunnable() {
            final int activeTicks = activeDurationTicks;
            final Location anchorLocation = location;
            final double initialValue = value;
            final int particleCount = ticks;
            final int sampleCount = step;
            final double particleOffsetX = radius;
            final double particleOffsetY = angle;
            final double particleOffsetZ = progress;
            final double particleExtra = scale;
            final float size = particleSize;
            final float speed = particleSpeed;
            final float volume = particleVolume;
            final int fadeTicks = fadeDurationTicks;
            final double fadeDistance = distance;
            int elapsedTicks = 0;

            @Override
            public void run() {
              if (this.elapsedTicks < this.activeTicks) {
                ParticleEffects.refreshPlayerState(
                    dustOptions,
                    this.anchorLocation,
                    this.initialValue,
                    this.particleCount,
                    this.sampleCount,
                    this.particleOffsetX,
                    this.particleOffsetY,
                    this.particleOffsetZ,
                    this.particleExtra,
                    this.size,
                    this.speed,
                    this.volume);
                this.elapsedTicks++;
              } else {
                int fadeStep = this.elapsedTicks - this.activeTicks;
                if (fadeStep > this.fadeTicks) {
                  this.cancel();
                } else {
                  double fadeValue = this.fadeDistance / this.fadeTicks * fadeStep;
                  ParticleEffects.refreshPlayerState(
                      dustOptions,
                      this.anchorLocation,
                      fadeValue,
                      this.particleCount,
                      this.sampleCount,
                      this.particleOffsetX,
                      this.particleOffsetY,
                      this.particleOffsetZ,
                      this.particleExtra,
                      this.size,
                      this.speed,
                      this.volume);
                  this.elapsedTicks++;
                }
              }
            }
          };
      ambientDustTrailTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return ambientDustTrailTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable resolveCandidateTask(
      Particle particle, Location location, double value, int count, int index, double distance, int remaining) {
    return resolveUpdatedTask(
        particle, location, value, count, index, distance, remaining, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static BukkitRunnable resolveResultTask(
      DustOptions dustOptions, Location location, double value, int count, int index, double distance, int remaining) {
    return resolveEffectiveTask(
        dustOptions, location, value, count, index, distance, remaining, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static void updateState(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      double distance,
      double radius,
      double angle,
      double progress,
      float scale,
      float size,
      float speed) {
    World world = resolveWorld(location);
    if (world != null && particle != null) {
      int remaining = Math.max(3, count);
      double amount = (Math.PI * 2) / remaining;

      for (int step = 0; step < remaining; step++) {
        double height = step * amount;
        Vector offset = new Vector(Math.cos(height) * value, 0.0, Math.sin(height) * value);
        offset = getDirectionFromConfig(offset, scale, size, speed);
        Location targetLocation = location.clone().add(offset);
        handleAbilityAction(world, particle, targetLocation, index, distance, radius, angle, progress);
      }
    }
  }

  public static void trackAbilityState(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      double distance,
      double radius,
      double angle,
      double progress,
      Vector direction) {
    World world = location.getWorld();
    if (world != null && particle != null) {
      int remaining = Math.max(3, count);
      double scale = (Math.PI * 2) / remaining;
      Vector offset = direction.clone().normalize();
      Vector velocity = Math.abs(offset.getY()) > 0.99 ? new Vector(1, 0, 0) : new Vector(0, 1, 0);
      Vector normalizedDirection = offset.getCrossProduct(velocity).normalize();
      Vector knockback = normalizedDirection.getCrossProduct(offset).normalize();

      for (int step = 0; step < remaining; step++) {
        double amount = step * scale;
        Vector axis =
            normalizedDirection
                .clone()
                .multiply(Math.cos(amount) * value)
                .add(knockback.clone().multiply(Math.sin(amount) * value));
        Location targetLocation = location.clone().add(axis);
        world.spawnParticle(particle, targetLocation, index, distance, radius, angle, progress);
      }
    }
  }

  public static void refreshPlayerState(
      DustOptions dustOptions,
      Location location,
      double value,
      int count,
      int index,
      double distance,
      double radius,
      double angle,
      double progress,
      float scale,
      float size,
      float speed) {
    World world = resolveWorld(location);
    if (world != null) {
      int remaining = Math.max(3, count);
      double amount = (Math.PI * 2) / remaining;

      for (int step = 0; step < remaining; step++) {
        double height = step * amount;
        Vector offset = new Vector(Math.cos(height) * value, 0.0, Math.sin(height) * value);
        offset = getDirectionFromConfig(offset, scale, size, speed);
        Location targetLocation = location.clone().add(offset);
        resetAbilityState(world, targetLocation, index, distance, radius, angle, progress, dustOptions);
      }
    }
  }

  public static void updatePlayerState(Particle particle, Location location, double value, int count) {
    updateState(particle, location, value, count, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static void finishAbilityAction(DustOptions dustOptions, Location location, double value, int count) {
    refreshPlayerState(dustOptions, location, value, count, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static BukkitRunnable createParticleAnimationTask(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress,
      float scale,
      float size,
      float speed) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null) {
      int step = Math.max(3, index);
      int ticks = Math.max(1, count);
      BukkitRunnable particleAnimationTask =
          new BukkitRunnable() {
            final int maxCount = ticks;
            final double effectRadius = value;
            final Location anchorLocation = location;
            final int durationTicks = step;
            final int cooldownTicks = remaining;
            final double verticalOffset = distance;
            final double distanceThreshold = radius;
            final double knockbackStrength = angle;
            final double damageAmount = progress;
            final float soundVolume = scale;
            final float effectScale = size;
            final float soundPitch = speed;
            int animationStep = 0;

            @Override
            public void run() {
              if (this.animationStep >= this.maxCount) {
                this.cancel();
              } else {
                double value = this.effectRadius / this.maxCount * this.animationStep;
                ParticleEffects.updateState(
                    particle, this.anchorLocation, value, this.durationTicks, this.cooldownTicks, this.verticalOffset, this.distanceThreshold, this.knockbackStrength, this.damageAmount, this.soundVolume, this.effectScale,
                    this.soundPitch);
                this.animationStep++;
              }
            }
          };
      particleAnimationTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return particleAnimationTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createDustTrailTask(
      DustOptions dustOptions,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress,
      float scale,
      float size,
      float speed) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && plugin != null) {
      int step = Math.max(3, index);
      int ticks = Math.max(1, count);
      BukkitRunnable dustTrailTask =
          new BukkitRunnable() {
            final int animationStep = ticks;
            final double damageAmount = value;
            final Location anchorLocation = location;
            final int cooldownTicks = step;
            final int durationTicks = remaining;
            final double effectRadius = distance;
            final double knockbackStrength = radius;
            final double effectScale = angle;
            final double distanceThreshold = progress;
            final float soundPitch = scale;
            final float particleSize = size;
            final float soundVolume = speed;
            int maxCount = 0;

            @Override
            public void run() {
              if (this.maxCount >= this.animationStep) {
                this.cancel();
              } else {
                double value = this.damageAmount / this.animationStep * this.maxCount;
                ParticleEffects.refreshPlayerState(
                    dustOptions, this.anchorLocation, value, this.cooldownTicks, this.durationTicks, this.effectRadius, this.knockbackStrength, this.effectScale, this.distanceThreshold, this.soundPitch, this.particleSize,
                    this.soundVolume);
                this.maxCount++;
              }
            }
          };
      dustTrailTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return dustTrailTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createSimpleParticleAnimationTask(
      Particle particle, Location location, double value, int count) {
    return createParticleAnimationTask(
        particle, location, value, count, 100, 2, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static BukkitRunnable createSimpleDustTrailTask(
      DustOptions dustOptions, Location location, double value, int count) {
    return createDustTrailTask(
        dustOptions, location, value, count, 100, 2, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static BukkitRunnable createDirectionalParticleOrbitTask(
      Particle particle,
      Location location,
      double value,
      int count,
      boolean enabled,
      int index,
      double distance,
      int remaining,
      double radius,
      double angle,
      double progress,
      double scale,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null) {
      int step = Math.max(1, count);
      int ticks = Math.max(1, index);
      double amount = enabled ? 1.0 : -1.0;
      BukkitRunnable cleanupParticleOrbitTaskPrimary =
          new BukkitRunnable() {
            final int durationTicks = step;
            final int cooldownTicks = ticks;
            final double maxDistance = amount;
            final double verticalOffset = amount;
            final float soundPitch = speed;
            final float effectScale = speed;
            final float soundVolume = volume;
            final Location anchorLocation = location;
            final int maxCount = remaining;
            final double damageAmount = radius;
            final double distanceThreshold = angle;
            final double horizontalOffset = progress;
            final double effectRadius = speed;
            final double knockbackStrength = distance;
            int animationStep = 0;
            double animationProgress = 0.0;

            @Override
            public void run() {
              if (this.animationStep >= this.durationTicks) {
                this.cancel();
              } else {
                for (int count = 0; count < this.cooldownTicks; count++) {
                  double value = this.animationProgress - this.maxDistance * count * 0.1;
                  Vector offset = new Vector(Math.cos(value) * this.verticalOffset, 0.0, Math.sin(value) * this.verticalOffset);
                  offset = ParticleEffects.getDirectionFromConfig(offset, this.soundPitch, this.effectScale, this.soundVolume);
                  Location location = this.anchorLocation.clone().add(offset);
                  ParticleEffects.handleAbilityAction(world, particle, location, this.maxCount, this.damageAmount, this.distanceThreshold, this.horizontalOffset, this.effectRadius);
                }

                this.animationProgress = this.animationProgress + this.maxDistance * this.knockbackStrength;
                this.animationStep++;
              }
            }
          };
      cleanupParticleOrbitTaskPrimary.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return cleanupParticleOrbitTaskPrimary;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createCleanupDustOrbitTask(
      DustOptions dustOptions,
      Location location,
      double value,
      int count,
      boolean enabled,
      int index,
      double distance,
      int remaining,
      double radius,
      double angle,
      double progress,
      double scale,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && plugin != null) {
      int step = Math.max(1, count);
      int ticks = Math.max(1, index);
      double amount = enabled ? 1.0 : -1.0;
      BukkitRunnable cleanupDustOrbitTask =
          new BukkitRunnable() {
            final int durationTicks = step;
            final int maxCount = ticks;
            final double animationProgress = amount;
            final double distanceThreshold = amount;
            final float soundVolume = speed;
            final float effectScale = speed;
            final float soundPitch = volume;
            final Location anchorLocation = location;
            final int animationStep = remaining;
            final double damageAmount = radius;
            final double knockbackStrength = angle;
            final double horizontalOffset = progress;
            final double verticalOffset = speed;
            final double maxDistance = distance;
            int cooldownTicks = 0;
            double effectRadius = 0.0;

            @Override
            public void run() {
              if (this.cooldownTicks >= this.durationTicks) {
                this.cancel();
              } else {
                for (int count = 0; count < this.maxCount; count++) {
                  double value = this.effectRadius - this.animationProgress * count * 0.1;
                  Vector offset = new Vector(Math.cos(value) * this.distanceThreshold, 0.0, Math.sin(value) * this.distanceThreshold);
                  offset = ParticleEffects.getDirectionFromConfig(offset, this.soundVolume, this.effectScale, this.soundPitch);
                  Location location = this.anchorLocation.clone().add(offset);
                  ParticleEffects.resetAbilityState(world, location, this.animationStep, this.damageAmount, this.knockbackStrength, this.horizontalOffset, this.verticalOffset, dustOptions);
                }

                this.effectRadius = this.effectRadius + this.animationProgress * this.maxDistance;
                this.cooldownTicks++;
              }
            }
          };
      cleanupDustOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return cleanupDustOrbitTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createFollowupParticleOrbitTask(
      Particle particle,
      Location location,
      double value,
      double distance,
      int count,
      boolean enabled,
      int index,
      double radius,
      int remaining,
      double angle,
      double progress,
      double scale,
      double amount,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null) {
      int step = Math.max(1, count);
      int ticks = Math.max(12, index);
      int durationTicks = Math.max(1, remaining);
      double height = enabled ? -1.0 : 1.0;
      BukkitRunnable followupParticleOrbitTask =
          new BukkitRunnable() {
            final int maxCount = step;
            final float soundVolume = speed;
            final float soundPitch = speed;
            final float effectScale = volume;
            final int durationTicks = ticks;
            final double effectRadius = value;
            final double distanceThreshold = distance;
            final Location anchorLocation = location;
            final int cooldownTicks = durationTicks;
            final double maxDistance = progress;
            final double damageAmount = progress;
            final double verticalOffset = speed;
            final double horizontalOffset = amount;
            final double knockbackStrength = height;
            final double effectHeight = radius;
            int animationStep = 0;
            double animationProgress = 0.0;

            @Override
            public void run() {
              if (this.animationStep >= this.maxCount) {
                this.cancel();
              } else {
                double angleRadians = Math.toRadians(this.soundVolume) + this.animationProgress;
                double distance = Math.toRadians(this.soundPitch);
                double radius = Math.toRadians(this.effectScale);

                for (int count = 0; count < this.durationTicks; count++) {
                  double value = (Math.PI * 2) / this.durationTicks * count;
                  Vector offset = new Vector(Math.cos(value) * this.effectRadius, 0.0, Math.sin(value) * this.distanceThreshold);
                  offset = ParticleEffects.getPrimaryDirection(offset, radius);
                  offset = ParticleEffects.getDirectionForAbility(offset, distance);
                  offset = ParticleEffects.getDirectionForState(offset, angleRadians);
                  Location location = this.anchorLocation.clone().add(offset);
                  ParticleEffects.handleAbilityAction(
                      world, particle, location, this.cooldownTicks, this.maxDistance, this.damageAmount, this.verticalOffset, this.horizontalOffset);
                }

                this.animationProgress = this.animationProgress + this.knockbackStrength * this.effectHeight;
                this.animationStep++;
              }
            }
          };
      followupParticleOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return followupParticleOrbitTask;
    } else {
      return null;
    }
  }

  static Vector getDirectionForAbility(Vector direction, double value) {
    double offset = Math.cos(value);
    double distance = Math.sin(value);
    double y = direction.getY();
    double z = direction.getZ();
    return new Vector(direction.getX(), y * offset - z * distance, y * distance + z * offset);
  }

  static Vector getDirectionForState(Vector direction, double value) {
    double offset = Math.cos(value);
    double distance = Math.sin(value);
    double x = direction.getX();
    double z = direction.getZ();
    return new Vector(x * offset - z * distance, direction.getY(), x * distance + z * offset);
  }

  static Vector getPrimaryDirection(Vector direction, double value) {
    double offset = Math.cos(value);
    double distance = Math.sin(value);
    double x = direction.getX();
    double y = direction.getY();
    return new Vector(x * offset - y * distance, x * distance + y * offset, direction.getZ());
  }

  public static BukkitRunnable createParticleOrbitBurstTask(
      Particle particle, Location location, int count, double value, int index, boolean enabled) {
    return createDirectionalParticleOrbitTask(
        particle, location, value, index, enabled, 15, 0.15, count, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static BukkitRunnable createSingleParticleOrbitTask(
      Particle particle, Location location, double value, int count, boolean enabled) {
    return createDirectionalParticleOrbitTask(
        particle, location, value, count, enabled, 15, 0.15, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static BukkitRunnable createSingleDustOrbitTask(
      DustOptions dustOptions, Location location, double value, int count, boolean enabled) {
    return createCleanupDustOrbitTask(
        dustOptions, location, value, count, enabled, 15, 0.15, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static BukkitRunnable createDelayedDustOrbitTask(
      DustOptions dustOptions,
      Location location,
      double value,
      int count,
      float scale,
      int index,
      double distance,
      double radius,
      double angle,
      double progress) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && plugin != null) {
      int remaining = Math.max(1, count);
      BukkitRunnable delayedDustOrbitTask =
          new BukkitRunnable() {
            final int cooldownTicks = remaining;
            final double knockbackStrength = value;
            final float effectScale = scale;
            final Location anchorLocation = location;
            final int durationTicks = index;
            final double distanceThreshold = distance;
            final double damageAmount = radius;
            final double verticalOffset = angle;
            final double effectRadius = progress;
            int maxCount = 0;

            @Override
            public void run() {
              if (this.maxCount >= this.cooldownTicks) {
                this.cancel();
              } else {
                double value = this.knockbackStrength / this.cooldownTicks * this.maxCount;
                if (value <= 0.1) {
                  this.maxCount++;
                } else {
                  double angleRadians = Math.toRadians(this.effectScale);
                  double offset = Math.cos(angleRadians);
                  double distance = Math.sin(angleRadians);
                  double radius = value * Math.sqrt(2.0);
                  int count = (int) Math.max(5.0, radius * 10.0);
                  double angle = value / count;

                  for (int index = 0; index < count; index++) {
                    double progress = index * angle;
                    ParticleEffects.updatePrimaryState(
                        world,
                        this.anchorLocation,
                        value - progress,
                        progress,
                        offset,
                        distance,
                        dustOptions,
                        this.durationTicks,
                        this.distanceThreshold,
                        this.damageAmount,
                        this.verticalOffset,
                        this.effectRadius);
                    ParticleEffects.updatePrimaryState(
                        world,
                        this.anchorLocation,
                        -value + progress,
                        progress,
                        offset,
                        distance,
                        dustOptions,
                        this.durationTicks,
                        this.distanceThreshold,
                        this.damageAmount,
                        this.verticalOffset,
                        this.effectRadius);
                    ParticleEffects.updatePrimaryState(
                        world,
                        this.anchorLocation,
                        value - progress,
                        -progress,
                        offset,
                        distance,
                        dustOptions,
                        this.durationTicks,
                        this.distanceThreshold,
                        this.damageAmount,
                        this.verticalOffset,
                        this.effectRadius);
                    ParticleEffects.updatePrimaryState(
                        world,
                        this.anchorLocation,
                        -value + progress,
                        -progress,
                        offset,
                        distance,
                        dustOptions,
                        this.durationTicks,
                        this.distanceThreshold,
                        this.damageAmount,
                        this.verticalOffset,
                        this.effectRadius);
                  }

                  this.maxCount++;
                }
              }
            }
          };
      delayedDustOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return delayedDustOrbitTask;
    } else {
      return null;
    }
  }

  static void updatePrimaryState(
      World world,
      Location location,
      double value,
      double distance,
      double radius,
      double angle,
      DustOptions dustOptions,
      int count,
      double progress,
      double scale,
      double amount,
      double speed) {
    double height = value * radius - distance * angle;
    double width = value * angle + distance * radius;
    Location targetLocation = location.clone().add(height, 0.0, width);
    resetAbilityState(world, targetLocation, count, progress, scale, amount, speed, dustOptions);
  }

  public static void updateTargetState(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress,
      float scale,
      float size,
      float speed) {
    World world = resolveWorld(location);
    if (world != null && particle != null) {
      int step = Math.max(3, count);
      int ticks = Math.max(6, index);

      for (int durationTicks = 0; durationTicks <= step; durationTicks++) {
        double amount = Math.PI * ((double) durationTicks / step);
        double offset = Math.cos(amount) * value;
        double height = Math.sin(amount) * value;

        for (int attempts = 0; attempts < ticks; attempts++) {
          double width = (Math.PI * 2) * ((double) attempts / ticks);
          Vector velocity = new Vector(Math.cos(width) * height, offset, Math.sin(width) * height);
          velocity = getDirectionFromConfig(velocity, scale, size, speed);
          Location targetLocation = location.clone().add(velocity);
          handleAbilityAction(world, particle, targetLocation, remaining, distance, radius, angle, progress);
        }
      }
    }
  }

  public static void updateSourceState(
      DustOptions dustOptions,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress,
      float scale,
      float size,
      float speed) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null) {
      int step = Math.max(3, count);
      int ticks = Math.max(6, index);

      for (int durationTicks = 0; durationTicks <= step; durationTicks++) {
        double amount = Math.PI * ((double) durationTicks / step);
        double offset = Math.cos(amount) * value;
        double height = Math.sin(amount) * value;

        for (int attempts = 0; attempts < ticks; attempts++) {
          double width = (Math.PI * 2) * ((double) attempts / ticks);
          Vector velocity = new Vector(Math.cos(width) * height, offset, Math.sin(width) * height);
          velocity = getDirectionFromConfig(velocity, scale, size, speed);
          Location targetLocation = location.clone().add(velocity);
          resetAbilityState(world, targetLocation, remaining, distance, radius, angle, progress, dustOptions);
        }
      }
    }
  }

  public static void updateActiveState(Particle particle, Location location, double value, int count, int index) {
    updateTargetState(particle, location, value, count, index, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static void updatePendingState(
      DustOptions dustOptions, Location location, double value, int count, int index) {
    updateSourceState(dustOptions, location, value, count, index, 1, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
  }

  public static BukkitRunnable createAuxiliaryParticleAnimationTask(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      float scale,
      int step,
      double distance,
      double radius,
      double angle,
      double progress,
      float size,
      float speed) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null) {
      int ticks = Math.max(1, remaining);
      BukkitRunnable delayedParticleAnimationTask =
          new BukkitRunnable() {
            final int animationStep = ticks;
            final Location anchorLocation = location;
            final double distanceThreshold = value;
            final int durationTicks = count;
            final int cooldownTicks = count;
            final int chargeLevel = step;
            final double damageAmount = distance;
            final double effectScale = radius;
            final double knockbackStrength = angle;
            final double effectRadius = progress;
            final float soundPitch = speed;
            final float particleSize = speed;
            final float animationSpeed = scale;
            int maxCount = 0;
            float soundVolume = 0.0F;

            @Override
            public void run() {
              if (this.maxCount >= this.animationStep) {
                this.cancel();
              } else {
                ParticleEffects.updateTargetState(
                    particle, this.anchorLocation, this.distanceThreshold, this.durationTicks, this.cooldownTicks, this.chargeLevel, this.damageAmount, this.effectScale, this.knockbackStrength, this.effectRadius, this.soundVolume,
                    this.soundPitch, this.particleSize);
                this.soundVolume = this.soundVolume + this.animationSpeed;
                this.maxCount++;
              }
            }
          };
      delayedParticleAnimationTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return delayedParticleAnimationTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createPulseDustTrailTask(
      DustOptions dustOptions,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      float scale,
      int step,
      double distance,
      double radius,
      double angle,
      double progress,
      float size,
      float speed) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && plugin != null) {
      int ticks = Math.max(1, remaining);
      BukkitRunnable pulseDustTrailTask =
          new BukkitRunnable() {
            final int chargeLevel = ticks;
            final Location anchorLocation = location;
            final double damageAmount = value;
            final int animationStep = count;
            final int durationTicks = count;
            final int maxCount = step;
            final double verticalOffset = distance;
            final double effectRadius = radius;
            final double distanceThreshold = angle;
            final double knockbackStrength = progress;
            final float particleSize = speed;
            final float soundVolume = speed;
            final float effectScale = scale;
            int cooldownTicks = 0;
            float soundPitch = 0.0F;

            @Override
            public void run() {
              if (this.cooldownTicks >= this.chargeLevel) {
                this.cancel();
              } else {
                ParticleEffects.updateSourceState(
                    dustOptions, this.anchorLocation, this.damageAmount, this.animationStep, this.durationTicks, this.maxCount, this.verticalOffset, this.effectRadius, this.distanceThreshold, this.knockbackStrength, this.soundPitch,
                    this.particleSize, this.soundVolume);
                this.soundPitch = this.soundPitch + this.effectScale;
                this.cooldownTicks++;
              }
            }
          };
      pulseDustTrailTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return pulseDustTrailTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createAuxiliaryParticleAnimationTask(
      Particle particle,
      Location location,
      double value,
      double distance,
      int count,
      int index,
      int remaining,
      double radius,
      int step,
      double angle,
      double progress,
      double scale,
      double amount,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null) {
      int ticks = Math.max(1, remaining);
      BukkitRunnable cleanupParticleAnimationTask =
          new BukkitRunnable() {
            final int cooldownTicks = ticks;
            final double horizontalOffset = radius;
            final double effectRadius = radius;
            final double effectScale = distance;
            final Location anchorLocation = location;
            final int animationStep = count;
            final int chargeLevel = count;
            final int maxCount = step;
            final double knockbackStrength = angle;
            final double distanceThreshold = progress;
            final double verticalOffset = scale;
            final double damageAmount = amount;
            final float soundPitch = size;
            final float soundVolume = speed;
            final float particleSize = volume;
            int durationTicks = 0;

            @Override
            public void run() {
              if (this.durationTicks >= this.cooldownTicks) {
                this.cancel();
              } else {
                double value = (double) this.durationTicks / Math.max(1, this.cooldownTicks);
                double distance = (Math.PI * 2) * this.horizontalOffset * value;
                double offset = this.effectRadius + (this.effectScale - this.effectRadius) * (0.5 + 0.5 * Math.sin(distance));
                ParticleEffects.updateTargetState(
                    particle, this.anchorLocation, offset, this.animationStep, this.chargeLevel, this.maxCount, this.knockbackStrength, this.distanceThreshold, this.verticalOffset, this.damageAmount, this.soundPitch,
                    this.soundVolume, this.particleSize);
                this.durationTicks++;
              }
            }
          };
      cleanupParticleAnimationTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return cleanupParticleAnimationTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createDelayedDustTrailTask(
      DustOptions dustOptions,
      Location location,
      double value,
      double distance,
      int count,
      int index,
      int remaining,
      double radius,
      int step,
      double angle,
      double progress,
      double scale,
      double amount,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && plugin != null) {
      int ticks = Math.max(1, remaining);
      BukkitRunnable delayedDustTrailTask =
          new BukkitRunnable() {
            final int durationTicks = ticks;
            final double verticalOffset = radius;
            final double horizontalOffset = radius;
            final double distanceThreshold = distance;
            final Location anchorLocation = location;
            final int chargeLevel = count;
            final int cooldownTicks = count;
            final int maxCount = step;
            final double damageAmount = angle;
            final double knockbackStrength = progress;
            final double effectRadius = scale;
            final double effectScale = amount;
            final float soundPitch = size;
            final float soundVolume = speed;
            final float particleSize = volume;
            int animationStep = 0;

            @Override
            public void run() {
              if (this.animationStep >= this.durationTicks) {
                this.cancel();
              } else {
                double value = (double) this.animationStep / Math.max(1, this.durationTicks);
                double distance = (Math.PI * 2) * this.verticalOffset * value;
                double offset = this.horizontalOffset + (this.distanceThreshold - this.horizontalOffset) * (0.5 + 0.5 * Math.sin(distance));
                ParticleEffects.updateSourceState(
                    dustOptions, this.anchorLocation, offset, this.chargeLevel, this.cooldownTicks, this.maxCount, this.damageAmount, this.knockbackStrength, this.effectRadius, this.effectScale, this.soundPitch,
                    this.soundVolume, this.particleSize);
                this.animationStep++;
              }
            }
          };
      delayedDustTrailTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return delayedDustTrailTask;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createTrailParticleOrbitTask(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      int step,
      double angle,
      double progress,
      double scale,
      double amount,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && particle != null && plugin != null) {
      int ticks = Math.max(1, count);
      int durationTicks = Math.max(1, index);
      int attempts = Math.max(8, remaining);
      double height = (Math.PI * 2) / attempts;
      BukkitRunnable trailParticleOrbitTaskPrimary =
          new BukkitRunnable() {
            final int durationTicks = ticks;
            final double verticalOffset = radius;
            final int cooldownTicks = durationTicks;
            final double animationProgress = radius;
            final double horizontalOffset = distance;
            final int animationStep = attempts;
            final double effectRadius = height;
            final float effectScale = speed;
            final float soundVolume = speed;
            final float soundPitch = volume;
            final Location anchorLocation = location;
            final int maxCount = step;
            final double damageAmount = height;
            final double distanceThreshold = progress;
            final double effectHeight = speed;
            final double maxDistance = amount;
            int chargeLevel = 0;
            double knockbackStrength = 0.0;

            @Override
            public void run() {
              if (this.chargeLevel >= this.durationTicks) {
                this.cancel();
              } else {
                this.knockbackStrength = this.knockbackStrength + this.verticalOffset;

                for (int count = 0; count < this.cooldownTicks; count++) {
                  double value = (double) count / this.cooldownTicks * (Math.PI * 2);
                  double offset = this.animationProgress * ((double) this.chargeLevel / this.durationTicks) + Math.sin(value + this.chargeLevel * this.horizontalOffset) * 0.35;
                  if (!(offset <= 0.05)) {
                    for (int index = 0; index < this.animationStep; index++) {
                      double distance = index * this.effectRadius + this.knockbackStrength + value;
                      Vector velocity = new Vector(Math.cos(distance) * offset, 0.0, Math.sin(distance) * offset);
                      velocity = ParticleEffects.getDirectionFromConfig(velocity, this.effectScale, this.soundVolume, this.soundPitch);
                      Location location = this.anchorLocation.clone().add(velocity);
                      ParticleEffects.handleAbilityAction(
                          world, particle, location, this.maxCount, this.damageAmount, this.distanceThreshold, this.effectHeight, this.maxDistance);
                    }
                  }
                }

                this.chargeLevel++;
              }
            }
          };
      trailParticleOrbitTaskPrimary.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return trailParticleOrbitTaskPrimary;
    } else {
      return null;
    }
  }

  public static BukkitRunnable createActiveDustOrbitTask(
      DustOptions dustOptions,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      int step,
      double angle,
      double progress,
      double scale,
      double amount,
      float size,
      float speed,
      float volume) {
    World world = resolveWorld(location);
    if (world != null && dustOptions != null && plugin != null) {
      int ticks = Math.max(1, count);
      int durationTicks = Math.max(1, index);
      int attempts = Math.max(8, remaining);
      double height = (Math.PI * 2) / attempts;
      BukkitRunnable activeDustOrbitTask =
          new BukkitRunnable() {
            final int animationStep = ticks;
            final double damageAmount = radius;
            final double animationProgress = radius;
            final double effectHeight = distance;
            final int cooldownTicks = attempts;
            final double horizontalOffset = height;
            final float soundPitch = speed;
            final float soundVolume = speed;
            final float effectScale = volume;
            final Location anchorLocation = location;
            final int maxCount = step;
            final double maxDistance = height;
            final double verticalOffset = progress;
            final double knockbackStrength = speed;
            final double effectRadius = amount;
            int chargeLevel = 0;
            double distanceThreshold = 0.0;

            @Override
            public void run() {
              if (this.chargeLevel >= this.animationStep) {
                this.cancel();
              } else {
                this.distanceThreshold = this.distanceThreshold + this.damageAmount;

                for (int count = 0; count < durationTicks; count++) {
                  double value = (double) count / durationTicks * (Math.PI * 2);
                  double offset = this.animationProgress * ((double) this.chargeLevel / this.animationStep) + Math.sin(value + this.chargeLevel * this.effectHeight) * 0.35;
                  if (!(offset <= 0.05)) {
                    for (int index = 0; index < this.cooldownTicks; index++) {
                      double distance = index * this.horizontalOffset + this.distanceThreshold + value;
                      Vector velocity = new Vector(Math.cos(distance) * offset, 0.0, Math.sin(distance) * offset);
                      velocity = ParticleEffects.getDirectionFromConfig(velocity, this.soundPitch, this.soundVolume, this.effectScale);
                      Location location = this.anchorLocation.clone().add(velocity);
                      ParticleEffects.resetAbilityState(
                          world, location, this.maxCount, this.maxDistance, this.verticalOffset, this.knockbackStrength, this.effectRadius, dustOptions);
                    }
                  }
                }

                this.chargeLevel++;
              }
            }
          };
      activeDustOrbitTask.runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(1L), 1L);
      return activeDustOrbitTask;
    } else {
      return null;
    }
  }

  public static void activateAbility(
      Player player,
      Particle particle,
      double value,
      double distance,
      int count,
      int index,
      double radius,
      int remaining,
      double angle,
      double progress,
      double scale,
      double amount) {
    if (player != null && particle != null && plugin != null) {
      new BukkitRunnable() {
        final int animationStep = count;
        final double animationProgress = distance;
        final double horizontalOffset = distance;
        final int cooldownTicks = remaining;
        final double effectScale = progress;
        final double effectRadius = progress;
        final double distanceThreshold = amount;
        final double verticalOffset = amount;
        final double knockbackStrength = radius;
        final int maxCount = remaining;
        int durationTicks = 0;
        double damageAmount = 0.0;

        @Override
        public void run() {
          if (!player.isOnline()) {
            this.cancel();
          } else if (this.durationTicks >= this.animationStep) {
            this.cancel();
          } else {
            Location location = player.getLocation().clone().add(0.0, this.animationProgress, 0.0);
            double offset = Math.cos(this.damageAmount) * this.horizontalOffset;
            double distance = Math.sin(this.damageAmount) * this.horizontalOffset;
            Location targetLocation = location.add(offset, 0.0, distance);
            ParticleEffects.handleAbilityAction(
                location.getWorld(), particle, targetLocation, this.cooldownTicks, this.effectScale, this.effectRadius, this.distanceThreshold, this.verticalOffset);
            this.damageAmount = this.damageAmount + this.knockbackStrength;
            this.durationTicks = this.durationTicks + this.maxCount;
          }
        }
      }
          .runTaskTimer(
              Bliss.getInstance(),
              SharedScheduler.staggeredInitialDelay(Math.max(1, index)),
              Math.max(1, index));
    }
  }

  public static void activatePrimaryAbility(
      Player player,
      DustOptions dustOptions,
      double value,
      double distance,
      int count,
      int index,
      double radius,
      int remaining,
      double angle,
      double progress,
      double scale,
      double amount) {
    if (player != null && dustOptions != null && plugin != null) {
      new BukkitRunnable() {
        final int maxCount = count;
        final double horizontalOffset = distance;
        final double effectRadius = distance;
        final int animationStep = remaining;
        final double knockbackStrength = progress;
        final double damageAmount = progress;
        final double distanceThreshold = amount;
        final double verticalOffset = amount;
        final double animationProgress = radius;
        final int cooldownTicks = remaining;
        int durationTicks = 0;
        double effectScale = 0.0;

        @Override
        public void run() {
          if (!player.isOnline()) {
            this.cancel();
          } else if (this.durationTicks >= this.maxCount) {
            this.cancel();
          } else {
            Location location = player.getLocation().clone().add(0.0, this.horizontalOffset, 0.0);
            double offset = Math.cos(this.effectScale) * this.effectRadius;
            double distance = Math.sin(this.effectScale) * this.effectRadius;
            Location targetLocation = location.add(offset, 0.0, distance);
            ParticleEffects.resetAbilityState(
                location.getWorld(), targetLocation, this.animationStep, this.knockbackStrength, this.damageAmount, this.distanceThreshold, this.verticalOffset, dustOptions);
            this.effectScale = this.effectScale + this.animationProgress;
            this.durationTicks = this.durationTicks + this.cooldownTicks;
          }
        }
      }
          .runTaskTimer(
              Bliss.getInstance(),
              SharedScheduler.staggeredInitialDelay(Math.max(1, index)),
              Math.max(1, index));
    }
  }

  public static void spawnPrimaryAbilityParticles(Location location, Color color) {
    String text = Bukkit.getMinecraftVersion();
    if (Integer.parseInt(text.split(_ID)[2]) >= 9) {
      location.getWorld().spawnParticle(Particle.FLASH, location, 1, 0.0, 0.0, 0.0, 0.0, color, true);
    } else {
      location.getWorld().spawnParticle(Particle.FLASH, location, 1, 0.0, 0.0, 0.0, 0.0, null);
    }
  }

  public static void updateCachedState(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress) {
    updateTargetState(particle, location, value, count, index, remaining, distance, radius, angle, progress, 0.0F, 0.0F, 0.0F);
  }

  public static void updateResolvedState(
      DustOptions dustOptions,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress) {
    updateSourceState(dustOptions, location, value, count, index, remaining, distance, radius, angle, progress, 0.0F, 0.0F, 0.0F);
  }

  public static void updateStoredState(
      Location location, Sound sound, float scale, float size, int count, int index, float speed) {
    World world = resolveWorld(location);
    if (world != null && plugin != null) {
      int remaining = Math.max(1, count);
      int step = Math.max(1, index);
      new BukkitRunnable() {
        final float particleSize = scale;
        final int durationTicks = remaining;
        final Location anchorLocation = location;
        final float soundVolume = scale;
        final float soundPitch = speed;
        int maxCount = 0;
        float effectScale = this.particleSize;

        @Override
        public void run() {
          if (this.maxCount >= this.durationTicks) {
            this.cancel();
          } else {
            world.playSound(this.anchorLocation, sound, this.soundVolume, this.effectScale);
            this.effectScale = this.effectScale + this.soundPitch;
            this.maxCount++;
          }
        }
      }
          .runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(step), step);
    }
  }

  public static void applyPrimaryAbilityEffects(
      Location location,
      double value,
      double distance,
      PotionEffectType potionEffectType,
      int count,
      int index,
      boolean enabled,
      boolean active,
      boolean allowed) {
    if (location != null && location.getWorld() != null) {
      for (Player player : location.getNearbyPlayers(value, distance, value)) {
        player.addPotionEffect(new PotionEffect(potionEffectType, count, index, enabled, active, allowed));
      }
    }
  }

  public static void applyTargetAbilityEffects(Location location, double value, double distance, PotionEffectType potionEffectType) {
    if (location != null && location.getWorld() != null) {
      for (Player player : location.getNearbyPlayers(value, distance, value)) {
        player.removePotionEffect(potionEffectType);
      }
    }
  }

  public static void applyAbilityMotion(
      Location location, double value, double distance, double radius, double angle) {
    if (location != null && location.getWorld() != null) {
      Vector direction = location.toVector();

      for (Player player : location.getNearbyPlayers(value, distance, value)) {
        Vector offset = player.getLocation().toVector().subtract(direction);
        offset.setY(0);
        if (!(offset.lengthSquared() < 1.0E-4)) {
          Vector velocity = offset.normalize().multiply(radius);
          velocity.setY(angle);
          player.setVelocity(velocity);
        }
      }
    }
  }

  public static void updateTrackedState(
      Location location,
      double value,
      double distance,
      Particle particle,
      int count,
      double radius,
      double angle,
      double progress,
      double scale) {
    if (location != null && location.getWorld() != null) {
      for (Player player : location.getNearbyPlayers(value, distance, value)) {
        Location targetLocation = player.getLocation().clone().add(0.0, 1.0, 0.0);
        handleAbilityAction(targetLocation.getWorld(), particle, targetLocation, count, radius, angle, progress, scale);
      }
    }
  }

  public static void updatePreviousState(
      Location location,
      double value,
      double distance,
      DustOptions dustOptions,
      int count,
      double radius,
      double angle,
      double progress,
      double scale) {
    if (location != null && location.getWorld() != null) {
      for (Player player : location.getNearbyPlayers(value, distance, value)) {
        Location targetLocation = player.getLocation().clone().add(0.0, 1.0, 0.0);
        resetAbilityState(targetLocation.getWorld(), targetLocation, count, radius, angle, progress, scale, dustOptions);
      }
    }
  }

  public static void updateNextState(
      Collection<Player> collection,
      Particle particle,
      int count,
      double value,
      double distance,
      double radius,
      double angle,
      double progress) {
    if (collection != null) {
      for (Player player : collection) {
        if (player != null && player.isOnline()) {
          Location location = player.getLocation().clone().add(0.0, progress, 0.0);
          handleAbilityAction(player.getWorld(), particle, location, count, value, distance, radius, angle);
        }
      }
    }
  }

  public static void updateDefaultState(
      Collection<Player> collection,
      DustOptions dustOptions,
      int count,
      double value,
      double distance,
      double radius,
      double angle,
      double progress) {
    if (collection != null) {
      for (Player player : collection) {
        if (player != null && player.isOnline()) {
          Location location = player.getLocation().clone().add(0.0, progress, 0.0);
          resetAbilityState(player.getWorld(), location, count, value, distance, radius, angle, dustOptions);
        }
      }
    }
  }

  static {
    _ID = "\\.";
  }
}
