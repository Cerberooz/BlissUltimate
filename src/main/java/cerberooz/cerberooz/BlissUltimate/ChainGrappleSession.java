package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import static cerberooz.cerberooz.BlissUltimate.GrappleState.IN_FLIGHT;
import static cerberooz.cerberooz.BlissUltimate.GrappleState.PULLING_ENTITY;
import static cerberooz.cerberooz.BlissUltimate.GrappleState.PULLING_PLAYER;
import static cerberooz.cerberooz.BlissUltimate.GrappleState.RETRACTING;

class ChainGrappleSession {
  ItemDisplay itemDisplay;
  Float activeFloat;
  Float secondaryFloat;
  int durationTicks;
  Location effectCenter;
  final Player player;
  final boolean activeState;
  final boolean secondaryActive;
  boolean active;
  GrappleState grappleState;
  Location targetLocation;
  final Vector direction;
  double effectRadius;
  long lastUpdateTime;
  LivingEntity livingEntity;
  Location anchorLocation;
  Vector secondaryDirection;
  BukkitTask scheduledTask;
  final List<ItemDisplay> entries;
  ItemDisplay secondaryItemDisplay;
  int cooldownTicks;
  final ChainGrappleManager chainGrappleManager;

  ChainGrappleSession(ChainGrappleManager chainGrappleManager, Player player, boolean enabled, boolean active) {
    this.chainGrappleManager = chainGrappleManager;
    this.activeFloat = null;
    this.secondaryFloat = null;
    this.durationTicks = 0;
    this.effectCenter = null;
    this.active = false;
    this.grappleState = IN_FLIGHT;
    this.effectRadius = 0.0;
    this.lastUpdateTime = 0L;
    this.livingEntity = null;
    this.anchorLocation = null;
    this.secondaryDirection = null;
    this.entries = new ArrayList<>();
    this.cooldownTicks = 0;
    this.player = player;
    this.activeState = enabled;
    this.secondaryActive = active;
    this.targetLocation = player.getEyeLocation().clone();
    this.direction = player.getEyeLocation().getDirection().normalize();
  }

  void startBackgroundTasks() {
    this.scheduledTask = Bukkit.getScheduler().runTaskTimer(this.chainGrappleManager.plugin, this::launchGrapple, 0L, 1L);
  }

  void initialize() {
    if (this.effectRadius >= this.chainGrappleManager.getEffectScale()) {
      this.refreshAbilityState();
    } else {
      double value = 1.0;
      int count = (int) Math.ceil(this.chainGrappleManager.getChainGrappleTravelSpeed() / value);

      for (int index = 0; index < count; index++) {
        this.targetLocation.add(this.direction.clone().multiply(value));
        this.effectRadius += value;
        if (this.targetLocation.getBlock().getType().isSolid()) {
          this.anchorLocation = this.targetLocation.getBlock().getLocation().add(0.5, 0.5, 0.5);
          this.secondaryDirection = this.anchorLocation.toVector().subtract(this.player.getLocation().toVector()).normalize();
          this.grappleState = PULLING_PLAYER;
          World world = this.targetLocation.getWorld();
          if (world != null) {
            world.playSound(this.targetLocation, Sound.BLOCK_CHAIN_HIT, 1.0F, 1.0F);
            world.playSound(this.targetLocation, Sound.BLOCK_ANVIL_LAND, 0.3F, 2.0F);
            world.spawnParticle(
                Particle.BLOCK,
                this.targetLocation,
                12,
                0.15,
                0.15,
                0.15,
                0.05,
                Material.IRON_BLOCK.createBlockData());
          }

          return;
        }

        for (Entity entity : this.targetLocation.getWorld().getNearbyEntities(this.targetLocation, 0.9, 0.9, 0.9)) {
          if (this.activeState
              && entity instanceof LivingEntity livingEntity
              && !entity.equals(this.player)
              && (!(entity instanceof Player player)
                  || player.getGameMode() != GameMode.CREATIVE
                      && player.getGameMode() != GameMode.SPECTATOR)) {
            this.livingEntity = livingEntity;
            this.grappleState = PULLING_ENTITY;
            this.targetLocation = livingEntity.getLocation().clone().add(0.0, livingEntity.getHeight() * 0.5, 0.0);
            livingEntity.damage(
                this.chainGrappleManager.getChainGrappleEntityHookDamage(), DamageSource.builder(DamageType.SONIC_BOOM).build());
            World targetWorld = this.targetLocation.getWorld();
            if (targetWorld != null) {
              targetWorld.playSound(livingEntity.getLocation(), Sound.BLOCK_CHAIN_HIT, 1.0F, 1.0F);
              if (livingEntity instanceof Player targetPlayer) {
                targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.6F, 1.4F);
              } else {
                targetWorld.playSound(livingEntity.getLocation(), Sound.ENTITY_GENERIC_HURT, 0.6F, 1.4F);
              }

              targetWorld.spawnParticle(
                  Particle.DUST, this.targetLocation, 15, 0.3, 0.3, 0.3, 0.0, ChainGrappleManager.dustOptions);
            }

            return;
          }
        }

        if (this.effectRadius >= this.chainGrappleManager.getEffectScale()) {
          this.refreshAbilityState();
          return;
        }
      }

      if (this.lastUpdateTime % 1L == 0L) {
        this.player.playSound(this.player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1.0F, 1.0F);
      }
    }
  }

  void refreshAbilityState() {
    if (this.grappleState != RETRACTING) {
      this.grappleState = RETRACTING;
      this.livingEntity = null;
      this.anchorLocation = null;
      this.secondaryDirection = null;
      this.cooldownTicks = 1000;
      this.player.playSound(this.player.getLocation(), Sound.BLOCK_CHAIN_BREAK, 0.9F, 1.0F);
    }
  }

  void updateAbilityState() {
    Location location = this.player.getLocation().clone().add(0.0, 1.25, 0.0);
    Vector direction = location.toVector().subtract(this.targetLocation.toVector());
    double value = direction.length();
    if (value <= 1.2) {
      this.updateState(true);
    } else {
      double distance = Math.min(this.chainGrappleManager.getEffectRadius(), value);
      this.targetLocation.add(direction.normalize().multiply(distance));
      if (this.lastUpdateTime % 2L == 0L) {
        this.player.playSound(this.player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 0.65F, 1.0F);
      }
    }
  }

  void applyAbilityEffects() {
    if (this.anchorLocation == null) {
      this.updateState(true);
    } else {
      if (this.isAbilityAllowed()) {
        this.durationTicks++;
        if (this.durationTicks >= 5) {
          this.updateState(true);
          return;
        }
      } else {
        this.durationTicks = 0;
      }

      if (this.isConditionMet()) {
        this.updateState(true);
      } else {
        Location location = this.player.getLocation();
        Vector direction = this.anchorLocation.toVector().subtract(location.toVector());
        double value = direction.length();
        if (value <= this.chainGrappleManager.getMaxDistance()) {
          Vector offset = this.player.getVelocity().clone().multiply(0.85);
          offset.add(
              this.player.getLocation().getDirection().normalize().multiply(this.chainGrappleManager.getHorizontalOffset()));
          offset = this.getDirection(offset, this.chainGrappleManager.getAnimationProgress());
          if (offset.getY() < -0.1) {
            offset.setY(-0.1);
          }

          this.player.setVelocity(offset);
          this.player.setFallDistance(0.0F);
          this.player.playSound(this.player.getLocation(), Sound.BLOCK_CHAIN_HIT, 0.5F, 1.0F);
          this.updateState(true);
        } else {
          Vector velocity = direction.clone().normalize();
          if (!this.secondaryActive && !this.player.isGliding()) {
            Vector normalizedDirection = this.player.getVelocity();
            Vector knockback =
                this.player.getLocation().getDirection().normalize().multiply(this.chainGrappleManager.getHorizontalOffset());
            if (knockback.getY() > 0.65) {
              knockback.setY(0.65);
            } else if (knockback.getY() < -0.5) {
              knockback.setY(-0.5);
            }

            Vector axis =
                velocity.clone()
                    .multiply(this.chainGrappleManager.getDamageAmount())
                    .add(normalizedDirection.clone().multiply(0.35))
                    .add(knockback);
            double y = this.anchorLocation.getY() - location.getY();
            if (y > 1.2 && axis.getY() < 0.12) {
              axis.setY(0.12);
            }

            this.player.setVelocity(
                this.getDirection(axis, this.chainGrappleManager.getAnimationProgress()));
            this.player.setFallDistance(0.0F);
            this.targetLocation = this.anchorLocation.clone();
          } else {
            Vector relativeOffset = this.player.getVelocity();
            Vector movement = relativeOffset.clone().add(velocity.clone().multiply(this.chainGrappleManager.getChainGrappleGlideAssistStrength()));
            double distance = velocity.getY();
            if (distance > this.chainGrappleManager.getActiveAbilityDoubleValue()) {
              distance = this.chainGrappleManager.getActiveAbilityDoubleValue();
            }

            if (distance < this.chainGrappleManager.getPendingAbilityDoubleValue()) {
              distance = this.chainGrappleManager.getPendingAbilityDoubleValue();
            }

            movement.setY(relativeOffset.getY() * 0.9 + distance * 0.35);
            if (movement.length() > this.chainGrappleManager.getChainGrappleGlideMaxSpeed()) {
              movement = movement.normalize().multiply(this.chainGrappleManager.getChainGrappleGlideMaxSpeed());
            }

            this.player.setVelocity(movement);
            this.player.setFallDistance(0.0F);
            this.targetLocation = this.anchorLocation.clone();
          }
        }
      }
    }
  }

  boolean isConditionMet() {
    if (this.anchorLocation == null) {
      return false;
    }

    if (this.secondaryDirection == null) {
      return false;
    }

    Vector playerOffset = this.player.getLocation().toVector().subtract(this.anchorLocation.toVector());
    Vector direction = this.player.getVelocity();
    double value = playerOffset.dot(this.secondaryDirection);
    double distance = direction.dot(this.secondaryDirection);
    return value > this.chainGrappleManager.getCachedAbilityDoubleValue() && distance > 0.15;
  }

  Vector getDirection(Vector direction, double value) {
    return direction.length() <= value ? direction : direction.normalize().multiply(value);
  }

  boolean isAbilityAllowed() {
    Location location = this.player.getLocation();
    World world = location.getWorld();
    if (world == null) {
      return false;
    } else {
      boolean block =
          location.getBlock().getType().isSolid()
              || location.clone().add(0.0, 1.0, 0.0).getBlock().getType().isSolid();
      if (!block) {
        this.effectCenter = location.clone();
        return false;
      } else if (this.effectCenter == null) {
        this.effectCenter = location.clone();
        return false;
      } else {
        double distance = location.toVector().distance(this.effectCenter.toVector());
        this.effectCenter = location.clone();
        return distance < 0.08;
      }
    }
  }

  void playAbilityEffects() {
    if (this.livingEntity != null && !this.livingEntity.isDead() && this.livingEntity.isValid()) {
      if (!(this.livingEntity instanceof Player player && Bliss.getInstance().canUseAbility(player))) {
        Location location = this.player.getLocation();
        Location targetLocation = this.livingEntity.getLocation();
        double distance = location.distance(targetLocation);
        if (distance <= this.chainGrappleManager.getVerticalOffset()) {
          this.livingEntity.setVelocity(new Vector(0.0, 0.35, 0.0));
          this.updateState(true);
        } else {
          Vector direction =
              location.toVector()
                  .subtract(targetLocation.toVector())
                  .normalize()
                  .multiply(this.chainGrappleManager.getKnockbackStrength());
          if (this.livingEntity.isOnGround()) {
            double y = location.getY() - targetLocation.getY();
            if (y > -3.0) {
              direction.setY(0.45);
            }
          }

          this.livingEntity.setVelocity(direction);
          this.targetLocation = targetLocation.clone().add(0.0, this.livingEntity.getHeight() * 0.5, 0.0);
        }
      }
    } else {
      this.updateState(true);
    }
  }

  void spawnAbilityEffects() {
    Location location = this.player.getLocation().clone().add(0.0, 1.25, 0.0);
    Location targetLocation = this.targetLocation.clone();
    Vector direction = targetLocation.toVector().subtract(location.toVector());
    double value = direction.length();
    if (!(value < 0.2)) {
      Vector offset = direction.clone().normalize();
      Location origin = targetLocation.clone().subtract(offset.clone().multiply(0.45));
      Vector velocity = origin.toVector().subtract(location.toVector());
      double distance = velocity.length() - 3.25;
      if (!(distance < 0.2)) {
        Vector normalizedDirection = velocity.clone().normalize();
        float scale = (float) Math.toDegrees(Math.atan2(-normalizedDirection.getX(), normalizedDirection.getZ()));
        float size = (float) Math.toDegrees(-Math.asin(normalizedDirection.getY()));
        this.cooldownTicks++;
        if (this.grappleState != RETRACTING) {
          this.player.playSound(this.player.getLocation(), Sound.BLOCK_CHAIN_BREAK, 1.0F, 1.0F);
        }

        double radius = 3.0;
        double angle = Math.min(distance, this.cooldownTicks * radius);
        double progress = 0.9;
        double amount = Math.max(0.0, angle - progress);
        double speed = 2.8;
        int count = Math.max(1, (int) Math.ceil(amount / speed));

        while (this.entries.size() < count) {
          ItemDisplay display =
              (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
          ItemStack item = new ItemStack(Material.ECHO_SHARD);
          ItemMeta itemMeta = item.getItemMeta();
          itemMeta.setCustomModelData(12348);
          item.setItemMeta(itemMeta);
          display.setItemStack(item);
          display.setBillboard(Billboard.FIXED);
          display.setInterpolationDuration(1);
          display.setTeleportDuration(1);
          Transformation transformation = display.getTransformation();
          transformation.getScale().set(1.0F, 1.0F, 1.0F);
          display.setTransformation(transformation);
          this.entries.add(display);
        }

        while (this.entries.size() > count) {
          ItemDisplay itemDisplay = this.entries.remove(this.entries.size() - 1);
          if (itemDisplay != null) {
            itemDisplay.remove();
          }
        }

        for (int index = 0; index < this.entries.size(); index++) {
          ItemDisplay currentItemDisplay = this.entries.get(index);
          if (currentItemDisplay != null && currentItemDisplay.isValid()) {
            double height = Math.min(index * speed, amount);
            Location center = location.clone().add(normalizedDirection.clone().multiply(height));
            currentItemDisplay.setInterpolationDuration(1);
            currentItemDisplay.setTeleportDuration(1);
            currentItemDisplay.teleport(center);
            currentItemDisplay.setRotation(scale, size);
            this.chainGrappleManager.playAbilityEffects(currentItemDisplay.getLocation(), 3.0, false);
          }
        }

        double width = Math.max(0.0, Math.min(angle, distance));
        Location destination = location.clone().add(normalizedDirection.clone().multiply(width));
        if (this.secondaryItemDisplay == null || !this.secondaryItemDisplay.isValid()) {
          this.secondaryItemDisplay = (ItemDisplay) origin.getWorld().spawnEntity(destination, EntityType.ITEM_DISPLAY);
          ItemStack heldItem = new ItemStack(Material.ECHO_SHARD);
          ItemMeta meta = heldItem.getItemMeta();
          meta.setCustomModelData(12349);
          heldItem.setItemMeta(meta);
          this.secondaryItemDisplay.setItemStack(heldItem);
          this.secondaryItemDisplay.setBillboard(Billboard.FIXED);
          this.secondaryItemDisplay.setInterpolationDuration(1);
          this.secondaryItemDisplay.setTeleportDuration(1);
          Transformation currentTransformation = this.secondaryItemDisplay.getTransformation();
          currentTransformation.getScale().set(1.0F, 1.0F, 1.0F);
          this.secondaryItemDisplay.setTransformation(currentTransformation);
        }

        this.secondaryItemDisplay.setInterpolationDuration(1);
        this.secondaryItemDisplay.setTeleportDuration(1);
        this.secondaryItemDisplay.teleport(destination);
        this.secondaryItemDisplay.setRotation(scale, size);
      }
    }
  }

  void cleanupAbilityState() {
    for (ItemDisplay itemDisplay : this.entries) {
      if (itemDisplay != null && itemDisplay.isValid()) {
        itemDisplay.remove();
      }
    }

    this.entries.clear();
    if (this.secondaryItemDisplay != null && this.secondaryItemDisplay.isValid()) {
      this.secondaryItemDisplay.remove();
    }

    this.secondaryItemDisplay = null;
    this.cooldownTicks = 0;
  }

  void updateState(boolean enabled) {
    if (!this.active) {
      this.active = true;
      this.cleanupAbilityState();
      this.activeFloat = null;
      this.secondaryFloat = null;
      if (enabled && this.scheduledTask != null) {
        try {
          this.scheduledTask.cancel();
        } catch (Exception ignored) {
          // Failure is non-fatal; normal event and lifecycle processing continues.
        }
      }

      List<ChainGrappleSession> playerSessions = this.chainGrappleManager.sessionsByPlayer.get(this.player.getUniqueId());
      if (playerSessions != null) {
        playerSessions.remove(this);
        if (playerSessions.isEmpty()) {
          this.chainGrappleManager.sessionsByPlayer.remove(this.player.getUniqueId());
        }
      }
    }
  }

  void launchGrapple() {
    if (this.player.isOnline() && !this.player.isDead() && this.player.isValid() && !this.active) {
      this.lastUpdateTime++;
      if (this.lastUpdateTime >= this.chainGrappleManager.getDurationMillis() && this.grappleState != RETRACTING) {
        this.refreshAbilityState();
      } else if (this.lastUpdateTime >= this.chainGrappleManager.getDurationMillis() + 40L && this.grappleState == RETRACTING) {
        this.updateState(true);
      } else {
        switch (this.grappleState) {
          case IN_FLIGHT:
            this.initialize();
            break;
          case PULLING_PLAYER:
            this.applyAbilityEffects();
            break;
          case PULLING_ENTITY:
            this.playAbilityEffects();
            break;
          case RETRACTING:
            this.updateAbilityState();
        }

        if (!this.active) {
          this.spawnAbilityEffects();
        }
      }
    } else {
      this.updateState(true);
    }
  }
}
