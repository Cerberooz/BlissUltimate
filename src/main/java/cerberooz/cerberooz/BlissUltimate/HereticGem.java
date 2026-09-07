package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
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
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class HereticGem implements Listener {
  static final String BLOOD_LINKING_ID;
  static final String BLOOD_RITUAL_ID;
  static final String BLOODSAWS_RECHARGE_ID;
  final Bliss plugin;
  TrustCommand trustCommand;
  AstraTier2Gem astraTier2Gem;
  final Random random = new Random();
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Integer> playerCounters = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastActivationTimes = new ConcurrentHashMap<>();
  final Map<UUID, Location> playerLocations = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> activeFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Integer> abilityStages = new ConcurrentHashMap<>();
  final Map<UUID, Set<UUID>> playerRelations = new ConcurrentHashMap<>();
  final Map<UUID, BukkitRunnable> secondaryTasksByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, String> valuesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Long> cooldownTimestamps = new ConcurrentHashMap<>();
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();
  final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
  final Set<ItemDisplay> activeBloodsawDisplays = ConcurrentHashMap.newKeySet();
  static final DustOptions currentDustOptions;
  static final DustOptions activeDustOptions;
  static final DustOptions dustOptions;
  static final DustOptions secondaryDustOptions;
  static final DustOptions pendingDustOptions;

  public HereticGem(Bliss bliss) {
    this.plugin = bliss;
    this.refreshAbilityState();
    this.startBackgroundTasks();
  }

  public void setTrustCommand(TrustCommand trustCommand) {
    this.trustCommand = trustCommand;
  }

  public void setAstraTier2Gem(AstraTier2Gem astraTier2Gem) {
    this.astraTier2Gem = astraTier2Gem;
  }

  public void activateAbility(Player player) {
    UUID playerId = player.getUniqueId();
    this.playerCounters.put(playerId, this.getDurationTicks());
    CooldownService.setCooldown(playerId, "bloodLinking", 0L);
    CooldownService.setCooldown(playerId, "bloodRitual", 0L);
    CooldownService.setCooldown(playerId, "bloodsaws_recharge", 0L);
    this.lastActivationTimes.remove(playerId);
    this.abilityStages.remove(playerId);
    this.activeFlagsByPlayer.remove(playerId);
    this.valuesByPlayer.remove(playerId);
    this.cooldownTimestamps.remove(playerId);
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (!this.activePlayers.contains(player.getUniqueId())) {
        if (!event.isCancelled()) {
          if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            UUID playerId = player.getUniqueId();

            for (Set<UUID> linkedPlayerIds : this.playerRelations.values()) {
              if (linkedPlayerIds.contains(playerId)) {
                double value = event.getFinalDamage();

                for (UUID targetId : linkedPlayerIds) {
                  if (!targetId.equals(playerId)) {
                    Player targetPlayer = Bukkit.getPlayer(targetId);
                    if (targetPlayer != null
                        && targetPlayer.isOnline()
                        && targetPlayer.getGameMode() != GameMode.CREATIVE
                        && targetPlayer.getGameMode() != GameMode.SPECTATOR) {
                      this.activePlayers.add(targetId);
                      targetPlayer.setHealth(Math.max(0.0, targetPlayer.getHealth() - value));
                      this.activePlayers.remove(targetId);
                    }
                  }
                }
                break;
              }
            }
          }
        }
      }
    }
  }

  boolean isAbilityActive(UUID playerId) {
    return this.astraTier2Gem != null && this.astraTier2Gem.hasRequiredState(playerId);
  }

  long getAbilityLong(UUID playerId) {
    return this.astraTier2Gem == null ? 0L : this.astraTier2Gem.getAbilityLong(playerId);
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(new BukkitRunnable() {
      @Override
      public void run() {
        for (UUID playerId : HereticGem.this.playerIds) {
          int count = HereticGem.this.playerCounters.computeIfAbsent(playerId, this::initializeCounter);
          if (count < HereticGem.this.getDurationTicks()) {
            Long storedTimestamp = HereticGem.this.lastUseTimes.get(playerId);
            if (storedTimestamp == null) {
              HereticGem.this.lastUseTimes.put(
                  playerId, System.currentTimeMillis() + (long) (HereticGem.this.getBloodsawsRechargeTimeSeconds() * 1000.0));
            } else if (System.currentTimeMillis() >= storedTimestamp) {
              HereticGem.this.playerCounters.put(playerId, ++count);
              HereticGem.this.lastUseTimes.remove(playerId);
              HereticGem.this.valuesByPlayer.remove(playerId);
              if (count < HereticGem.this.getDurationTicks()) {
                HereticGem.this.lastUseTimes.put(
                    playerId, System.currentTimeMillis() + (long) (HereticGem.this.getBloodsawsRechargeTimeSeconds() * 1000.0));
              }
            }
          }
        }
      }

      private Integer initializeCounter(UUID playerId) {
        return HereticGem.this.getDurationTicks();
      }
    }, this.plugin, 1L, 20L);
  }

  void refreshAbilityState() {
    SharedScheduler.scheduleRepeating(new BukkitRunnable() {
      @Override
      public void run() {
        HereticGem.this.playerIds.clear();
        Collection<? extends Player> collection = Bukkit.getOnlinePlayers();
        if (!collection.isEmpty()) {
          for (Player player : collection) {
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            boolean enabled = HereticGem.this.isGemItem(mainHandItem) || HereticGem.this.isGemItem(offHandItem);
            if (!enabled) {
              UUID playerId = player.getUniqueId();
              HereticGem.this.valuesByPlayer.remove(playerId);
              HereticGem.this.cooldownTimestamps.remove(playerId);
            } else if (HereticGem.this.isAbilityActive(player.getUniqueId())) {
              long lastUpdateTime = HereticGem.this.getAbilityLong(player.getUniqueId());
              long timestamp = lastUpdateTime / 1000L;
              String darkGray = ChatColor.DARK_GRAY.toString();
              String bold = ChatColor.BOLD.toString();
              player.sendActionBar(
                  "🔒 "
                  + darkGray
                  + bold
                  + "ᴅɪꜱᴀʙʟᴇᴅ: "
                  + timestamp
                  + "s");
            } else if (!HereticGem.this.plugin.isGemsDisabled()
               
                && !HereticGem.this.plugin.canUseAbility(player)) {
              HereticGem.this.playerIds.add(player.getUniqueId());
              HereticGem.this.updateAbilityState(player);
            } else {
              String displayColor = ChatColor.DARK_GRAY.toString();
              String textColor = ChatColor.BOLD.toString();
              player.sendActionBar(
                  "🔒 " + displayColor + textColor + "ᴅɪꜱᴀʙʟᴇᴅ");
            }
          }
        }
      }
    }, this.plugin, 1L, 20L);
  }

  void updateAbilityState(Player player) {
    UUID playerId = player.getUniqueId();
    long now = System.currentTimeMillis();
    long timestamp = this.cooldownTimestamps.getOrDefault(playerId, 0L);
    if (now - timestamp > 200L || !this.valuesByPlayer.containsKey(playerId)) {
      String text = "󏿿󏿿󏿿󏿿";
      String message = this.formatDisplayText(player);
      String textColor = ChatColor.WHITE.toString();
      String displayText = this.formatDisplayTextForPlayer(player);
      String labelColor = ChatColor.WHITE.toString();
      String name = this.formatDisplayTextForTarget(player);
      String suffix =
          "🤟"
              + text
              + text
              + message
              + textColor
              + " | "
              + displayText
              + labelColor
              + " 🤌 "
              + name;
      this.valuesByPlayer.put(playerId, suffix);
      this.cooldownTimestamps.put(playerId, now);
    }

    player.sendActionBar(this.valuesByPlayer.get(playerId));
  }

  String formatDisplayText(Player player) {
    UUID playerId = player.getUniqueId();
    Integer storedCount = this.playerCounters.get(playerId);
    int count = this.playerCounters.computeIfAbsent(playerId, this::initializeCounterForPlayer);
    if (count >= this.getDurationTicks()) {
      return count + " " + ChatColor.GREEN + "Ready";
    }

    Long storedTimestamp = this.lastUseTimes.get(playerId);
    long now = storedTimestamp - System.currentTimeMillis();
    if (now <= 0L) {
      return count + " " + ChatColor.GREEN + "Ready";
    }

    long timestamp = (long) Math.ceil(now / 1000.0);
    ChatColor chatColor;
    if (timestamp >= 5L) {
      chatColor = ChatColor.RED;
    } else if (timestamp == 4L) {
      chatColor = ChatColor.of("#FF8C00");
    } else if (timestamp == 3L) {
      chatColor = ChatColor.of("#FFB900");
    } else if (timestamp == 2L) {
      chatColor = ChatColor.of("#C0D900");
    } else {
      chatColor = ChatColor.GREEN;
    }

    return count + " " + chatColor + timestamp + "s";
  }

  String formatDisplayTextForPlayer(Player player) {
    UUID playerId = player.getUniqueId();
    long timestamp = CooldownService.remainingMillis(playerId, "bloodLinking");
    if (timestamp <= 0L) {
      return ChatColor.GREEN + "Ready";
    }

    long lastUpdateTime = (long) Math.ceil(timestamp / 1000.0);
    ChatColor color =
        this.resolveChatColor(
            lastUpdateTime, Math.max(1L, Math.round(this.getPendingAbilityDoubleValue())));
    return color + Long.toString(lastUpdateTime) + "s";
  }

  String formatDisplayTextForTarget(Player player) {
    UUID playerId = player.getUniqueId();
    long timestamp = CooldownService.remainingMillis(playerId, "bloodRitual");
    if (timestamp <= 0L) {
      return ChatColor.GREEN + "Ready";
    }

    long lastUpdateTime = (long) Math.ceil(timestamp / 1000.0);
    return this.resolveChatColor(lastUpdateTime, Math.max(1L, Math.round(this.getBloodRitualCooldownSeconds()))).toString() + lastUpdateTime
        + "s";
  }

  ChatColor resolveChatColor(long timestamp, long lastUpdateTime) {
    double value = timestamp / Math.max(1.0, lastUpdateTime);
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

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      if (this.isGemItem(mainHandItem)) {
        if (this.isAbilityActive(playerId)) {
          event.setCancelled(true);
        } else if (!this.plugin.isGemsDisabled()
           
            && !this.plugin.canUseAbility(player)) {
          UUID targetId = player.getUniqueId();
          long now = System.currentTimeMillis();
          int count = this.playerCounters.computeIfAbsent(targetId, this::initializeCounter);
          if (count <= 0) {
            event.setCancelled(true);
          } else {
            this.playerCounters.put(targetId, --count);
            if (!this.lastUseTimes.containsKey(targetId)) {
              this.lastUseTimes.put(
                  targetId, System.currentTimeMillis() + (long) (this.getBloodsawsRechargeTimeSeconds() * 1000.0));
            }

            Long storedTimestamp = this.lastActivationTimes.get(targetId);
            int index = this.abilityStages.getOrDefault(targetId, 0);
            if (storedTimestamp != null && now - storedTimestamp <= this.getAbilityLongValue(this.getCachedAbilityDoubleValue())) {
              this.abilityStages.put(targetId, ++index);
              if (index >= this.getRemainingTicks()) {
                if (CooldownService.remainingMillis(targetId, "bloodLinking") == 0L) {
                  this.activeFlagsByPlayer.put(targetId, true);
                  CooldownService.setCooldown(targetId, "bloodLinking", (long) this.getPendingAbilityDoubleValue());
                }

                this.lastActivationTimes.remove(targetId);
                this.abilityStages.put(targetId, 0);
              }
            } else {
              this.lastActivationTimes.put(targetId, now);
              this.abilityStages.put(targetId, 1);
            }

            this.valuesByPlayer.remove(targetId);
            this.cooldownTimestamps.remove(targetId);
            this.playAbilitySound(player);
            event.setCancelled(true);
          }
        } else {
          event.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  public void applyAbilityEffects(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (event.getAction() == Action.RIGHT_CLICK_AIR
        || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      if (this.isGemItem(mainHandItem)) {
        if (this.isAbilityActive(playerId)) {
          event.setCancelled(true);
        } else if (this.plugin.isGemsDisabled()
           
            || this.plugin.canUseAbility(player)) {
          event.setCancelled(true);
        } else if (CooldownService.isOnCooldown(playerId, "bloodRitual")) {
          event.setCancelled(true);
        } else {
          CooldownService.setCooldown(playerId, "bloodRitual", (long) this.getBloodRitualCooldownSeconds());
          this.valuesByPlayer.remove(playerId);
          this.refreshPlayerState(player);
          event.setCancelled(true);
        }
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

  @EventHandler
  public void onEntityPotionEffect(EntityPotionEffectEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (event.getCause() != Cause.PLUGIN) {
        if (event.getAction() == org.bukkit.event.entity.EntityPotionEffectEvent.Action.ADDED) {
          UUID playerId = player.getUniqueId();
          ItemStack mainHandItem = player.getInventory().getItemInMainHand();
          ItemStack offHandItem = player.getInventory().getItemInOffHand();
          if (this.isGemItem(mainHandItem) || this.isGemItem(offHandItem)) {
            if (!this.isAbilityActive(playerId)) {
              if (!this.plugin.isGemsDisabled()
                 
                  && !this.plugin.canUseAbility(player)) {
                PotionEffect effect = event.getNewEffect();
                if (effect != null) {
                  if (effect.getType() == PotionEffectType.STRENGTH) {
                    if (effect.getAmplifier() == 1) {
                      event.setCancelled(true);
                      Bukkit.getScheduler()
                          .runTaskLater(this.plugin, () -> this.applyPotionEffects(player), 2L);
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
  public void playAbilityEffects(EntityPotionEffectEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (event.getCause() != Cause.PLUGIN) {
        if (event.getAction() == org.bukkit.event.entity.EntityPotionEffectEvent.Action.ADDED) {
          UUID playerId = player.getUniqueId();
          ItemStack mainHandItem = player.getInventory().getItemInMainHand();
          ItemStack offHandItem = player.getInventory().getItemInOffHand();
          if (this.isGemItem(mainHandItem) || this.isGemItem(offHandItem)) {
            if (!this.isAbilityActive(playerId)) {
              if (!this.plugin.isGemsDisabled()
                 
                  && !this.plugin.canUseAbility(player)) {
                PotionEffect effect = event.getNewEffect();
                if (effect != null) {
                  if (effect.getType() == PotionEffectType.STRENGTH) {
                    if (effect.getAmplifier() == 0) {
                      event.setCancelled(true);
                      Bukkit.getScheduler()
                          .runTaskLater(this.plugin, () -> this.applyTierOnePotionEffects(player), 2L);
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
  public void spawnAbilityEffects(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (event.getDamager() instanceof Player targetPlayer) {
        UUID playerId = targetPlayer.getUniqueId();
        ItemStack mainHandItem = targetPlayer.getInventory().getItemInMainHand();
        ItemStack offHandItem = targetPlayer.getInventory().getItemInOffHand();
        if (this.isGemItem(mainHandItem) || this.isGemItem(offHandItem)) {
          if (!this.isAbilityActive(playerId)) {
            if (!this.plugin.isGemsDisabled()
               
                && !this.plugin.canUseAbility(targetPlayer)) {
              if (!this.isTrustedPlayer(targetPlayer, player)) {
                if (this.random.nextDouble() < this.getPassivesAutoCritChance()) {
                  event.setDamage(event.getDamage() * this.getPassivesAutoCritMultiplier());
                  event.getEntity()
                      .getWorld()
                      .playSound(
                          event.getEntity().getLocation(),
                          Sound.ENTITY_PLAYER_ATTACK_CRIT,
                          1.0F,
                          1.0F);
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
    this.cleanupAbilityState(event.getPlayer().getUniqueId());
  }

  void cleanupAbilityState(UUID playerId) {
    BukkitRunnable runnable = this.secondaryTasksByPlayer.remove(playerId);
    if (runnable != null) {
      runnable.cancel();
    }

    this.playerCounters.remove(playerId);
    this.lastActivationTimes.remove(playerId);
    this.playerLocations.remove(playerId);
    this.activeFlagsByPlayer.remove(playerId);
    this.abilityStages.remove(playerId);
    this.playerRelations.remove(playerId);
    this.valuesByPlayer.remove(playerId);
    this.cooldownTimestamps.remove(playerId);
    this.playerIds.remove(playerId);
    CooldownService.setCooldown(playerId, "bloodLinking", 0L);
    CooldownService.setCooldown(playerId, "bloodRitual", 0L);
    CooldownService.setCooldown(playerId, "bloodsaws_recharge", 0L);
  }

  void playAbilitySound(Player player) {
    Location location = player.getEyeLocation().subtract(0.0, this.getMaxDistance(), 0.0);
    Vector direction = location.getDirection().normalize();
    player.getWorld().playSound(location, Sound.ITEM_MACE_SMASH_AIR, 1.0F, 0.9F);
    player.getWorld().playSound(location, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.0F, 0.9F);
    Material material = Material.matchMaterial(this.formatDisplayTextForAbility());
    if (material == null) {
      material = Material.ECHO_SHARD;
    }

    ItemDisplay itemDisplay = this.resolveItemDisplay(location.clone(), material, 1.6);
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Player capturedPlayer = player;
      final ItemDisplay capturedItemDisplay = itemDisplay;
      final Location targetLocation = this.anchorLocation.clone();
      final Vector secondaryDirection = direction.clone();
      double distanceThreshold = 0.0;
      double effectRadius = 0.0;
      boolean active = false;
      Location effectCenter = null;
      int durationTicks = 0;
      static final String BLOODSAWS_TRAIL_BACK_OFFSET_ID = "bloodsaws.trailBackOffset";
      @Override
      public void run() {
        if (!this.capturedPlayer.isOnline()) {
          this.playAbilityEffects();
        } else if (!this.capturedItemDisplay.isValid()) {
          HereticGem.this.untrackBloodsawDisplay(this.capturedItemDisplay);
          this.cancel();
        } else if (this.distanceThreshold >= HereticGem.this.getEffectRadius()) {
          this.applyAbilityEffects();
        } else {
          this.targetLocation.add(this.secondaryDirection.clone().multiply(HereticGem.this.getSpeed()));
          this.distanceThreshold = this.distanceThreshold + HereticGem.this.getSpeed();
          this.durationTicks++;
          if (HereticGem.this.isAbilityAllowed()) {
            HereticGem.this.spawnAbilityParticles(this.targetLocation, HereticGem.this.getActiveAbilityDoubleValue(), HereticGem.this.isAbilityBlocked());
          }

          if (this.isConditionMet()) {
            this.initialize();
          }

          if (this.isAbilityAllowed()) {
            this.refreshAbilityState();
          }

          if (this.targetLocation.getBlock().getType().isSolid() && this.targetLocation.getBlock().getType() != Material.COBWEB) {
            this.applyAbilityEffects();
          } else {
            this.effectRadius = this.effectRadius + HereticGem.this.getBloodsawsRotationSpeedDegPerTick();
            HereticGem.this.teleportAbilityTarget(this.capturedItemDisplay, this.targetLocation, this.effectRadius, 1.6);
            this.updateAbilityState();
            if (this.isAbilityBlocked()) {
              this.applyAbilityEffects();
            }
          }
        }
      }

      private boolean isConditionMet() {
        Location location = this.targetLocation.clone().subtract(0.0, 0.5, 0.0);
        if (location.getBlock().getType().isSolid() && this.secondaryDirection.getY() <= 0.0) {
          if (!this.active) {
            this.active = true;
            this.effectCenter = this.targetLocation.clone();
          }

          return true;
        } else {
          return false;
        }
      }

      private void initialize() {
        double x = Math.sqrt(this.secondaryDirection.getX() * this.secondaryDirection.getX() + this.secondaryDirection.getZ() * this.secondaryDirection.getZ());
        this.secondaryDirection.setY(x * HereticGem.this.getVerticalOffset());
        this.secondaryDirection.normalize();
        this.targetLocation.add(0.0, HereticGem.this.getHorizontalOffset(), 0.0);
        this.targetLocation.getWorld().playSound(this.targetLocation, Sound.BLOCK_STONE_HIT, 0.5F, 1.2F);
      }

      private boolean isAbilityAllowed() {
        return this.targetLocation.clone().add(0.0, 0.5, 0.0).getBlock().getType().isSolid() && this.secondaryDirection.getY() > 0.0;
      }

      private void refreshAbilityState() {
        this.secondaryDirection.setY(-Math.abs(this.secondaryDirection.getY()));
        this.targetLocation.subtract(0.0, HereticGem.this.getAnimationProgress(), 0.0);
      }

      private void updateAbilityState() {
        Location location =
        this.targetLocation
        .clone()
        .subtract(this.secondaryDirection.clone().normalize().multiply(HereticGem.this.getConfiguredDouble(BLOODSAWS_TRAIL_BACK_OFFSET_ID, 0.75)));

        for (int count = 0; count < HereticGem.this.getMaxCount(); count++) {
          double angleRadians = Math.toRadians(count * (360.0 / HereticGem.this.getMaxCount()) + this.effectRadius);
          double value = HereticGem.this.getBloodsawsTrailRadius();
          Location targetLocation = location.clone().add(Math.cos(angleRadians) * value, -0.8, Math.sin(angleRadians) * value);
          if (this.durationTicks % 2 == 0) {
            this.targetLocation.getWorld().spawnParticle(Particle.DUST, targetLocation, 1, 0.0, 0.0, 0.0, 0.0, HereticGem.currentDustOptions);
          }
        }
      }

      private boolean isAbilityBlocked() {
        for (Entity entity :
            this.targetLocation
            .getWorld()
            .getNearbyEntities(
            this.targetLocation,
            HereticGem.this.getEffectScale(),
            HereticGem.this.getEffectScale(),
            HereticGem.this.getEffectScale())) {
          if (entity instanceof LivingEntity livingEntity
              && entity != this.capturedPlayer
              && !HereticGem.this.isTrustedPlayer(this.capturedPlayer, entity)) {
            this.spawnAbilityParticles(livingEntity);
            return true;
          }
        }

        return false;
      }

      private void spawnAbilityParticles(LivingEntity livingEntity) {
        livingEntity.damage(HereticGem.this.getDamage(), DamageSource.builder(DamageType.SONIC_BOOM).build());
        Vector knockback =
        livingEntity.getLocation()
        .toVector()
        .subtract(this.capturedPlayer.getLocation().toVector())
        .normalize()
        .multiply(HereticGem.this.getDamageAmount());
        knockback.setY(HereticGem.this.getKnockbackStrength());
        livingEntity.setVelocity(knockback);
        livingEntity.getWorld()
        .spawnParticle(
            Particle.BLOCK,
            livingEntity.getLocation().add(0.0, 1.0, 0.0),
            25,
            0.5,
            0.5,
            0.5,
            0.2,
            Material.REDSTONE_BLOCK.createBlockData());
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0F, 0.8F);
      }

      private void applyAbilityEffects() {
        Location location = this.effectCenter != null ? this.effectCenter : this.targetLocation.clone();
        HereticGem.this.playerLocations.put(this.capturedPlayer.getUniqueId(), location);
        this.capturedItemDisplay.remove();
        HereticGem.this.untrackBloodsawDisplay(this.capturedItemDisplay);
        HereticGem.this.scheduleAbilityUpdate(this.capturedPlayer);
        this.cancel();
      }

      private void playAbilityEffects() {
        if (this.capturedItemDisplay.isValid()) {
          this.capturedItemDisplay.remove();
        }

        HereticGem.this.untrackBloodsawDisplay(this.capturedItemDisplay);

        this.cancel();
      }


    }.runTaskTimer(this.plugin, 0L, 1L);
  }

  ItemDisplay resolveItemDisplay(Location location, Material material, double value) {
    ItemStack item = new ItemStack(material);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setCustomModelData(12347);
    item.setItemMeta(itemMeta);
    ItemDisplay display =
        location.getWorld()
            .spawn(
                location, ItemDisplay.class, spawnedDisplay -> configureBloodsawDisplay(item, spawnedDisplay));
    this.activeBloodsawDisplays.add(display);
    this.teleportAbilityTarget(display, location, 0.0, value);
    return display;
  }

  void untrackBloodsawDisplay(ItemDisplay display) {
    this.activeBloodsawDisplays.remove(display);
  }

  public void shutdown() {
    for (Map.Entry<UUID, BukkitRunnable> entry : this.secondaryTasksByPlayer.entrySet()) {
      entry.getValue().cancel();
      Player player = Bukkit.getPlayer(entry.getKey());
      if (player != null) {
        player.setGliding(false);
        player.setFallDistance(0.0F);
      }
    }
    this.secondaryTasksByPlayer.clear();
    for (ItemDisplay display : this.activeBloodsawDisplays) {
      if (display != null && display.isValid()) {
        display.remove();
      }
    }
    this.activeBloodsawDisplays.clear();
    this.lastUseTimes.clear();
    this.playerCounters.clear();
    this.lastActivationTimes.clear();
    this.playerLocations.clear();
    this.activeFlagsByPlayer.clear();
    this.abilityStages.clear();
    this.playerRelations.clear();
    this.valuesByPlayer.clear();
    this.cooldownTimestamps.clear();
    this.playerIds.clear();
    this.activePlayers.clear();
  }

  void teleportAbilityTarget(ItemDisplay itemDisplay, Location location, double value, double distance) {
    itemDisplay.teleport(location);
    itemDisplay.setInterpolationDelay(0);
    itemDisplay.setTeleportDuration(1);
    itemDisplay.setInterpolationDuration(1);
    itemDisplay.setTransformation(
        new Transformation(
            new Vector3f(0.0F, 0.0F, 0.0F),
            new Quaternionf().rotateY((float) Math.toRadians(value)),
            new Vector3f((float) distance, (float) distance, (float) distance),
            new Quaternionf()));
  }

  void spawnAbilityParticles(Location location, double value, boolean enabled) {
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

  void scheduleAbilityUpdate(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
      Location location = this.playerLocations.get(playerId);
      if (location != null) {
        this.completeAbilityAction(player, location);
        this.activeFlagsByPlayer.put(playerId, false);
      }
    }
  }

  void completeAbilityAction(Player player, Location location) {
    World world = location.getWorld();
    UUID playerId = player.getUniqueId();
    HashSet<UUID> trackedIds = new HashSet<>();

    for (Entity entity :
        world.getNearbyEntities(
            location, this.getResolvedAbilityDoubleValue(), this.getBloodLinkingRadiusY(), this.getResolvedAbilityDoubleValue())) {
      if (entity instanceof Player targetPlayer && entity != player && !this.isTrustedPlayer(player, targetPlayer)) {
        trackedIds.add(targetPlayer.getUniqueId());
      }
    }

    if (!trackedIds.isEmpty()) {
      this.playerRelations.put(playerId, trackedIds);
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
          this.playerRelations.remove(playerId);

        }, this.getAbilityIntValue(this.getBloodLinkingDurationSeconds()));
    }

    this.updateState(world, location, trackedIds);
    this.handleAbilityAction(world, location);
    this.resetAbilityState(world, location);
    this.trackAbilityState(world, location);
  }

  void updateState(World world, Location location, Set<UUID> linkedPlayerIds) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Set<UUID> playerIds = linkedPlayerIds;
      double effectRadius = 0.0;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= HereticGem.this.getEffectCount()) {
          this.cancel();
        } else {
          this.effectRadius = this.effectRadius + HereticGem.this.getBloodLinkingPhase1AngleStep();

          for (int count = 0; count < HereticGem.this.getBloodLinkingPhase1ThetaPoints(); count++) {
            double angleRadians = Math.toRadians(count * HereticGem.this.getBloodLinkingPhase1ThetaStep());

            for (int index = 0; index < HereticGem.this.getBloodLinkingPhase1PhiPoints(); index++) {
              double distance = Math.toRadians(index * HereticGem.this.getBloodLinkingPhase1PhiStep() + this.effectRadius);
              double offset = Math.sin(angleRadians) * Math.cos(distance) * HereticGem.this.getBloodLinkingPhase1CoreRadius();
              double radius = Math.sin(angleRadians) * Math.sin(distance) * HereticGem.this.getBloodLinkingPhase1CoreRadius();
              double angle = Math.cos(angleRadians) * HereticGem.this.getBloodLinkingPhase1CoreRadius();
              world.spawnParticle(
                  Particle.BLOCK,
                  this.anchorLocation.getX() + offset,
                  this.anchorLocation.getY() + 0.5 + radius,
                  this.anchorLocation.getZ() + angle,
                  1,
                  0.0,
                  0.0,
                  0.0,
                  0.0,
                  Material.REDSTONE_BLOCK.createBlockData());
            }
          }

          for (UUID playerId : this.playerIds) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
              HereticGem.this.processAbilityState(world, this.anchorLocation, player);
            }
          }

          this.durationTicks++;
        }
      }
    }.runTaskTimer(this.plugin, 0L, this.getBloodLinkingPhase1PeriodTicks());
  }

  void processAbilityState(World world, Location location, Player player) {
    if (player.getWorld().equals(world)) {
      Location targetLocation = location.clone().add(0.0, 0.5, 0.0);
      Location origin = player.getLocation().add(0.0, 1.0, 0.0);
      double distance = targetLocation.distance(origin);
      if (!(distance <= 1.0E-4)) {
        Vector direction = origin.toVector().subtract(targetLocation.toVector()).normalize();
        double value = 0.0;

        while (value < distance) {
          Location center = targetLocation.clone().add(direction.clone().multiply(value));
          world.spawnParticle(Particle.DUST, center, 1, 0.05, 0.05, 0.05, 0.0, dustOptions);
          world.spawnParticle(
              Particle.BLOCK,
              center,
              1,
              0.02,
              0.02,
              0.02,
              0.0,
              Material.REDSTONE_BLOCK.createBlockData());
          value += this.getBloodLinkingPhase1RayStep();
        }
      }
    }
  }

  void handleAbilityAction(World world, Location location) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= HereticGem.this.getActiveAbilityIntValue()) {
          this.cancel();
        } else {
          double value = Math.min(HereticGem.this.getBloodLinkingPhase2MaxHeight(), this.durationTicks * HereticGem.this.getBloodLinkingPhase2HeightPerTick());

          for (int count = 0; count < HereticGem.this.getResolvedAbilityIntValue(); count++) {
            double distance = count * 180.0;

            for (double radius = 0.0; radius <= value; radius += HereticGem.this.getBloodLinkingPhase2HeightStep()) {
              double angle;
              if (radius <= HereticGem.this.getBloodLinkingPhase2MiddleHeight()) {
                angle = HereticGem.this.getBloodLinkingPhase2MinRadius() + radius / HereticGem.this.getBloodLinkingPhase2MiddleHeight() * (HereticGem.this.getBloodLinkingPhase2MaxRadius() - HereticGem.this.getBloodLinkingPhase2MinRadius());
              } else {
                angle = HereticGem.this.getBloodLinkingPhase2MaxRadius() - (radius - HereticGem.this.getBloodLinkingPhase2MiddleHeight()) / HereticGem.this.getBloodLinkingPhase2MiddleHeight() * (HereticGem.this.getBloodLinkingPhase2MaxRadius() - HereticGem.this.getBloodLinkingPhase2EndRadius());
              }

              if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < HereticGem.this.getBloodLinkingPhase2SpawnChance()) {
                double angleRadians = Math.toRadians(distance + radius * HereticGem.this.getBloodLinkingPhase2AnglePerHeight());
                world.spawnParticle(
                    Particle.SOUL_FIRE_FLAME,
                    this.anchorLocation.getX() + Math.cos(angleRadians) * angle,
                    this.anchorLocation.getY() + radius,
                    this.anchorLocation.getZ() + Math.sin(angleRadians) * angle,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0);
              }
            }
          }

          this.durationTicks++;
        }
      }
    }
        .runTaskTimer(this.plugin, this.getPendingAbilityIntValue(), this.getCachedAbilityIntValue());
  }

  void resetAbilityState(World world, Location location) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= HereticGem.this.getBloodLinkingPhase3Ticks()) {
          this.cancel();
        } else {
          for (int count = 0; count < HereticGem.this.getBloodLinkingPhase3WaveCycles(); count++) {
            int index = this.durationTicks - count * HereticGem.this.getBloodLinkingPhase3WaveCycleSpacingTicks();
            if (index >= 0 && index < HereticGem.this.getBloodLinkingPhase3WaveCycleDurationTicks()) {
              double value = (double) index / HereticGem.this.getBloodLinkingPhase3WaveCycleDurationTicks() * HereticGem.this.getBloodLinkingPhase3WaveMaxRadius();

              for (double distance = 0.0; distance < 360.0; distance += HereticGem.this.getBloodLinkingPhase3WaveAngleStep()) {
                double angleRadians = Math.toRadians(distance);
                double radius = Math.sin(Math.toRadians(distance * HereticGem.this.getBloodLinkingPhase3HorizontalFrequency())) * HereticGem.this.getBloodLinkingPhase3HorizontalAmplitude();
                double angle = value + radius;
                double progress = Math.sin(Math.toRadians(distance * HereticGem.this.getBloodLinkingPhase3VerticalFrequency() + HereticGem.this.getBloodLinkingPhase3VerticalOffset())) * HereticGem.this.getBloodLinkingPhase3VerticalAmplitude();
                if (angle > 0.0) {
                  double offset = this.anchorLocation.getX() + Math.cos(angleRadians) * angle;
                  double scale = this.anchorLocation.getZ() + Math.sin(angleRadians) * angle;
                  double y = this.anchorLocation.getY() + HereticGem.this.getBloodLinkingPhase3BaseY() + progress;
                  world.spawnParticle(
                      Particle.DUST, offset, y, scale, 1, 0.0, 0.0, 0.0, 0.0, HereticGem.currentDustOptions);
                  if ((int) distance % 10 == 0) {
                    world.spawnParticle(
                        Particle.DUST, offset, y, scale, 1, 0.0, 0.0, 0.0, 0.0, HereticGem.pendingDustOptions);
                  }
                }
              }
            }
          }

          this.durationTicks++;
        }
      }
    }.runTaskTimer(this.plugin, 0L, this.getBloodLinkingPhase3PeriodTicks());
  }

  void trackAbilityState(World world, Location location) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= HereticGem.this.getBloodLinkingSoundsRepeats()) {
          this.cancel();
        } else {
          world.playSound(this.anchorLocation, Sound.ENTITY_GHAST_WARN, 1.0F, 1.1F);
          world.playSound(this.anchorLocation, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0F, 1.3F);
          this.durationTicks++;
        }
      }
    }.runTaskTimer(this.plugin, 0L, 20L);
  }

  void refreshPlayerState(Player player) {
    UUID playerId = player.getUniqueId();
    Location location = player.getLocation().clone();
    BukkitRunnable runnable = this.secondaryTasksByPlayer.get(playerId);
    if (runnable != null) {
      runnable.cancel();
    }

    BloodRitualFlightController motionTask = new BloodRitualFlightController(this, player, playerId, location);
    this.secondaryTasksByPlayer.put(playerId, motionTask);
    motionTask.runTaskTimer(this.plugin, 0L, 1L);
  }

  void updatePlayerState(Player player) {
    player.getWorld().spawnParticle(Particle.DUST, player.getLocation(), 4, 0.3, 0.3, 0.3, 0.0, activeDustOptions);
  }

  void finishAbilityAction(Location location, double value, Particle particle, int count) {
    double distance = 360.0 / count;

    for (int index = 0; index < count; index++) {
      double angleRadians = Math.toRadians(index * distance);
      location.getWorld()
          .spawnParticle(
              particle,
              location.clone().add(Math.cos(angleRadians) * value, 0.0, Math.sin(angleRadians) * value),
              1,
              0.0,
              0.0,
              0.0,
              0.0);
    }
  }

  void spawnPrimaryAbilityParticles(Location location, int count, double value) {
    double distance = 360.0 / count;

    for (int index = 0; index < count; index++) {
      double angleRadians = Math.toRadians(index * distance);
      Vector offset = new Vector(Math.cos(angleRadians), 0.0, Math.sin(angleRadians));
      double radius = 0.0;

      while (radius < value) {
        location.getWorld()
            .spawnParticle(
                Particle.BLOCK,
                location.clone().add(offset.clone().multiply(radius)),
                1,
                0.1,
                0.1,
                0.1,
                0.0,
                Material.REDSTONE_BLOCK.createBlockData());
        radius += this.getBloodRitualRayStep();
      }
    }
  }

  boolean isLocationBlocked(Player player, Location location) {
    boolean enabled = false;

    for (Entity entity : location.getWorld().getNearbyEntities(location, this.getBloodRitualDamageRadius(), this.getBloodRitualDamageRadius(), this.getBloodRitualDamageRadius())) {
      if (entity instanceof LivingEntity livingEntity
          && !entity.equals(player)
          && !this.isTrustedPlayer(player, entity)
          && !(livingEntity instanceof Player targetPlayer
              && (targetPlayer.getGameMode() == GameMode.SPECTATOR
                  || targetPlayer.getGameMode() == GameMode.CREATIVE))) {
        enabled = true;
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ITEM_MACE_SMASH_AIR, 1.0F, 1.0F);
        livingEntity.damage(this.getBloodRitualDamage(), DamageSource.builder(DamageType.SONIC_BOOM).build());
        livingEntity.getWorld()
            .spawnParticle(
                Particle.BLOCK,
                livingEntity.getLocation().add(0.0, 1.0, 0.0),
                20,
                0.5,
                0.5,
                0.5,
                0.3,
                Material.REDSTONE_BLOCK.createBlockData());
        livingEntity.getWorld()
            .spawnParticle(
                Particle.DUST, livingEntity.getLocation().add(0.0, 1.0, 0.0), 15, 0.5, 0.5, 0.5, 0.0, secondaryDustOptions);
      }
    }

    location.getWorld().spawnParticle(Particle.EXPLOSION, location, 1, 0.0, 0.0, 0.0, 0.0);
    return enabled;
  }

  boolean isTrustedPlayer(Player player, Entity entity) {
    if (entity instanceof Player targetPlayer) {
      if (player.getUniqueId().equals(targetPlayer.getUniqueId())) {
        return true;
      }

      if (this.trustCommand == null) {
        return false;
      }

      try {
        return this.trustCommand.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId());
      } catch (NoSuchMethodError | Exception noSuchMethodErrorException) {
        return false;
      }
    } else {
      return false;
    }
  }

  public static ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.REPEATING_COMMAND_BLOCK);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setDisplayName(
        ChatColor.of("#87161B")
            + ChatColor.BOLD.toString()
            + "ʜᴇʀᴇᴛɪᴄ "
            + ChatColor.of("#FFD773")
            + "ɢᴇᴍ");
    ArrayList<String> lore = new ArrayList<>();
    lore.add(
        ChatColor.WHITE
            + ChatColor.BOLD.toString()
            + "ꜰᴇᴀʀ ɪѕ ᴛʜᴇ ᴏɴʟʏ"
            + " ѕᴀʟᴠᴀᴛɪᴏɴ");
    lore.add(ChatColor.WHITE + "(" + ChatColor.of("#8B65DD") + "Mythic" + ChatColor.WHITE + ")");
    lore.add(" ");
    lore.add(
        ChatColor.of("#6F0B0B")
            + "🔮 "
            + ChatColor.of("#FFD773")
            + "ᴘᴀѕѕɪᴠᴇѕ");
    lore.add(ChatColor.GRAY + "- Hemorrhage");
    lore.add(ChatColor.GRAY + "- Enduring Strength");
    lore.add(ChatColor.GRAY + "- Auto Crit");
    lore.add(ChatColor.GRAY + "- Blood Hardening");
    lore.add(" ");
    lore.add(
        ChatColor.of("#6F0B0B")
            + "🔮 "
            + ChatColor.of("#B8FFFB")
            + "ᴘᴏᴡᴇʀѕ");
    lore.add(
        ChatColor.GRAY
            + "- "
            + ChatColor.of("#560E0F")
            + "🔠 ʙʟᴏᴏᴅѕᴀᴡѕ");
    lore.add(" ");
    lore.add(
        ChatColor.GRAY
            + "- "
            + ChatColor.of("#7E080D")
            + "🔡"
            + " ʙʟᴏᴏᴅʟɪɴᴋɪɴɢ");
    itemMeta.setLore(lore);
    itemMeta.setCustomModelData(100);
    itemMeta.addEnchant(Enchantment.MENDING, 1, true);
    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(itemMeta);
    return item;
  }

  boolean isGemItem(ItemStack item) {
    return AuratusGem.getPrimaryAbilityIntValue(item, Material.REPEATING_COMMAND_BLOCK) == 100;
  }

  int getConfiguredInt(String text, int count) {
    return ConfigValueCache.getInt(this.plugin, "hereticGem." + text, count);
  }

  double getConfiguredDouble(String text, double value) {
    return ConfigValueCache.getDouble(this.plugin, "hereticGem." + text, value);
  }

  boolean isConditionMet(String text, boolean enabled) {
    return ConfigValueCache.getBoolean(this.plugin, "hereticGem." + text, enabled);
  }

  String formatDisplayTextFromConfig(String text, String message) {
    return ConfigValueCache.getString(this.plugin, "hereticGem." + text, message);
  }

  long getAbilityLongValue(double value) {
    return (long) (value * 1000.0);
  }

  int getAbilityIntValue(double value) {
    return (int) Math.round(value * 20.0);
  }

  int getDurationTicks() {
    return this.getConfiguredInt("bloodsaws.maxCharges", 2);
  }

  double getBloodsawsRechargeTimeSeconds() {
    return this.getConfiguredDouble("bloodsaws.rechargeTimeSeconds", 5.0);
  }

  double getDamage() {
    return this.getConfiguredDouble("bloodsaws.damage", 5.0);
  }

  double getSpeed() {
    return this.getConfiguredDouble("bloodsaws.speed", 0.8);
  }

  double getEffectRadius() {
    return this.getConfiguredDouble("bloodsaws.maxDistance", 50.0);
  }

  double getEffectScale() {
    return this.getConfiguredDouble("bloodsaws.hitbox", 1.0);
  }

  double getDamageAmount() {
    return this.getConfiguredDouble("bloodsaws.knockbackHorizontal", 0.6);
  }

  double getKnockbackStrength() {
    return this.getConfiguredDouble("bloodsaws.knockbackY", 0.35);
  }

  double getMaxDistance() {
    return this.getConfiguredDouble("bloodsaws.eyeYOffset", 0.4);
  }

  double getVerticalOffset() {
    return this.getConfiguredDouble("bloodsaws.groundBounceYMultiplier", 0.4);
  }

  double getHorizontalOffset() {
    return this.getConfiguredDouble("bloodsaws.groundBounceOffset", 0.3);
  }

  double getAnimationProgress() {
    return this.getConfiguredDouble("bloodsaws.ceilingBounceOffset", 0.3);
  }

  double getBloodsawsRotationSpeedDegPerTick() {
    return this.getConfiguredDouble("bloodsaws.rotationSpeedDegPerTick", 30.0);
  }

  int getMaxCount() {
    return this.getConfiguredInt("bloodsaws.trailParticleCount", 6);
  }

  double getBloodsawsTrailRadius() {
    return this.getConfiguredDouble("bloodsaws.trailRadius", 0.6);
  }

  String formatDisplayTextForAbility() {
    return this.formatDisplayTextFromConfig("bloodsaws.displayMaterial", "ECHO_SHARD");
  }

  int getChargeLevel() {
    return this.getConfiguredInt("bloodsaws.displayModelData", 123457);
  }

  double getBloodsawsDisplayScale() {
    return this.getConfiguredDouble("bloodsaws.displayScale", 1.2);
  }

  boolean isAbilityAllowed() {
    return this.isConditionMet("bloodsaws.breakCobwebs", true);
  }

  boolean isAbilityBlocked() {
    return this.isConditionMet("bloodsaws.cobwebDrops", false);
  }

  double getActiveAbilityDoubleValue() {
    return this.getConfiguredDouble("bloodsaws.cobwebBreakRadius", 1.2);
  }

  double getPendingAbilityDoubleValue() {
    return this.getConfiguredDouble("bloodLinking.cooldownSeconds", 25.0);
  }

  double getCachedAbilityDoubleValue() {
    return this.getConfiguredDouble("bloodLinking.comboWindowSeconds", 1.0);
  }

  int getRemainingTicks() {
    return this.getConfiguredInt("bloodLinking.activationShots", 2);
  }

  double getResolvedAbilityDoubleValue() {
    return this.getConfiguredDouble("bloodLinking.radiusXZ", 8.0);
  }

  double getBloodLinkingRadiusY() {
    return this.getConfiguredDouble("bloodLinking.radiusY", 11.0);
  }

  double getBloodLinkingDurationSeconds() {
    return this.getConfiguredDouble("bloodLinking.durationSeconds", 10.0);
  }

  int getEffectCount() {
    return this.getConfiguredInt("bloodLinking.phase1.repeats", 20);
  }

  int getBloodLinkingPhase1PeriodTicks() {
    return this.getConfiguredInt("bloodLinking.phase1.periodTicks", 10);
  }

  double getBloodLinkingPhase1AngleStep() {
    return this.getConfiguredDouble("bloodLinking.phase1.angleStep", 15.0);
  }

  int getBloodLinkingPhase1ThetaPoints() {
    return this.getConfiguredInt("bloodLinking.phase1.thetaPoints", 15);
  }

  double getBloodLinkingPhase1ThetaStep() {
    return this.getConfiguredDouble("bloodLinking.phase1.thetaStep", 24.0);
  }

  int getBloodLinkingPhase1PhiPoints() {
    return this.getConfiguredInt("bloodLinking.phase1.phiPoints", 8);
  }

  double getBloodLinkingPhase1PhiStep() {
    return this.getConfiguredDouble("bloodLinking.phase1.phiStep", 90.0);
  }

  double getBloodLinkingPhase1CoreRadius() {
    return this.getConfiguredDouble("bloodLinking.phase1.coreRadius", 0.5);
  }

  double getBloodLinkingPhase1RayStep() {
    return this.getConfiguredDouble("bloodLinking.phase1.rayStep", 0.3);
  }

  int getActiveAbilityIntValue() {
    return this.getConfiguredInt("bloodLinking.phase2.ticks", 90);
  }

  int getPendingAbilityIntValue() {
    return this.getConfiguredInt("bloodLinking.phase2.startDelayTicks", 20);
  }

  int getCachedAbilityIntValue() {
    return this.getConfiguredInt("bloodLinking.phase2.periodTicks", 2);
  }

  double getBloodLinkingPhase2MaxHeight() {
    return this.getConfiguredDouble("bloodLinking.phase2.maxHeight", 10.0);
  }

  double getBloodLinkingPhase2HeightPerTick() {
    return this.getConfiguredDouble("bloodLinking.phase2.heightPerTick", 1.0);
  }

  int getResolvedAbilityIntValue() {
    return this.getConfiguredInt("bloodLinking.phase2.spiralCount", 2);
  }

  double getBloodLinkingPhase2HeightStep() {
    return this.getConfiguredDouble("bloodLinking.phase2.heightStep", 0.5);
  }

  double getBloodLinkingPhase2MiddleHeight() {
    return this.getConfiguredDouble("bloodLinking.phase2.middleHeight", 5.0);
  }

  double getBloodLinkingPhase2MinRadius() {
    return this.getConfiguredDouble("bloodLinking.phase2.minRadius", 0.3);
  }

  double getBloodLinkingPhase2MaxRadius() {
    return this.getConfiguredDouble("bloodLinking.phase2.maxRadius", 1.0);
  }

  double getBloodLinkingPhase2EndRadius() {
    return this.getConfiguredDouble("bloodLinking.phase2.endRadius", 0.3);
  }

  double getBloodLinkingPhase2AnglePerHeight() {
    return this.getConfiguredDouble("bloodLinking.phase2.anglePerHeight", 100.0);
  }

  double getBloodLinkingPhase2SpawnChance() {
    return this.getConfiguredDouble("bloodLinking.phase2.spawnChance", 0.4);
  }

  int getBloodLinkingPhase3Ticks() {
    return this.getConfiguredInt("bloodLinking.phase3.ticks", 90);
  }

  int getBloodLinkingPhase3PeriodTicks() {
    return this.getConfiguredInt("bloodLinking.phase3.periodTicks", 2);
  }

  int getBloodLinkingPhase3WaveCycles() {
    return this.getConfiguredInt("bloodLinking.phase3.waveCycles", 5);
  }

  int getBloodLinkingPhase3WaveCycleSpacingTicks() {
    return this.getConfiguredInt("bloodLinking.phase3.waveCycleSpacingTicks", 40);
  }

  int getBloodLinkingPhase3WaveCycleDurationTicks() {
    return this.getConfiguredInt("bloodLinking.phase3.waveCycleDurationTicks", 40);
  }

  double getBloodLinkingPhase3WaveMaxRadius() {
    return this.getConfiguredDouble("bloodLinking.phase3.waveMaxRadius", 8.0);
  }

  double getBloodLinkingPhase3WaveAngleStep() {
    return this.getConfiguredDouble("bloodLinking.phase3.waveAngleStep", 5.0);
  }

  double getBloodLinkingPhase3HorizontalFrequency() {
    return this.getConfiguredDouble("bloodLinking.phase3.horizontalFrequency", 5.0);
  }

  double getBloodLinkingPhase3HorizontalAmplitude() {
    return this.getConfiguredDouble("bloodLinking.phase3.horizontalAmplitude", 1.2);
  }

  double getBloodLinkingPhase3VerticalFrequency() {
    return this.getConfiguredDouble("bloodLinking.phase3.verticalFrequency", 5.0);
  }

  double getBloodLinkingPhase3VerticalOffset() {
    return this.getConfiguredDouble("bloodLinking.phase3.verticalOffset", 45.0);
  }

  double getBloodLinkingPhase3VerticalAmplitude() {
    return this.getConfiguredDouble("bloodLinking.phase3.verticalAmplitude", 1.0);
  }

  double getBloodLinkingPhase3BaseY() {
    return this.getConfiguredDouble("bloodLinking.phase3.baseY", 10.5);
  }

  int getBloodLinkingSoundsRepeats() {
    return this.getConfiguredInt("bloodLinking.sounds.repeats", 10);
  }

  double getBloodRitualCooldownSeconds() {
    return this.getConfiguredDouble("bloodRitual.cooldownSeconds", 45.0);
  }

  int getBloodRitualLaunchFxHeight() {
    return this.getConfiguredInt("bloodRitual.launchFxHeight", 15);
  }

  double getBloodRitualLaunchYVelocity() {
    return this.getConfiguredDouble("bloodRitual.launchYVelocity", 3.2);
  }

  double getBloodRitualWaitSeconds() {
    return this.getConfiguredDouble("bloodRitual.waitSeconds", 1.0);
  }

  double getBloodRitualGlideSpeed() {
    return this.getConfiguredDouble("bloodRitual.glideSpeed", 3.5);
  }

  int getBloodRitualTrailEveryTicks() {
    return this.getConfiguredInt("bloodRitual.trailEveryTicks", 3);
  }

  double getBloodRitualBaseGlideSpeed() {
    return this.getConfiguredDouble("bloodRitual.baseGlideSpeed", 1.78);
  }

  double getBloodRitualBoostedGlideSpeed() {
    return this.getConfiguredDouble("bloodRitual.boostedGlideSpeed", 1.75);
  }

  double getBloodRitualMaxGlideSeconds() {
    return this.getConfiguredDouble("bloodRitual.maxGlideSeconds", 3.0);
  }

  double getBloodRitualImpactTimeoutSeconds() {
    return this.getConfiguredDouble("bloodRitual.impactTimeoutSeconds", 1.0);
  }

  double getBloodRitualSpawnCircleRadius() {
    return this.getConfiguredDouble("bloodRitual.spawnCircleRadius", 5.0);
  }

  int getBloodRitualSpawnCircleCount() {
    return this.getConfiguredInt("bloodRitual.spawnCircleCount", 40);
  }

  int getBloodRitualSpawnRayCount() {
    return this.getConfiguredInt("bloodRitual.spawnRayCount", 6);
  }

  double getBloodRitualSpawnRayLength() {
    return this.getConfiguredDouble("bloodRitual.spawnRayLength", 12.0);
  }

  double getBloodRitualRayStep() {
    return this.getConfiguredDouble("bloodRitual.rayStep", 0.6);
  }

  double getBloodRitualImpactCircleRadius() {
    return this.getConfiguredDouble("bloodRitual.impactCircleRadius", 1.5);
  }

  int getBloodRitualImpactCircleCount() {
    return this.getConfiguredInt("bloodRitual.impactCircleCount", 15);
  }

  int getBloodRitualMaxLaunches() {
    return this.getConfiguredInt("bloodRitual.maxLaunches", 5);
  }

  double getBloodRitualBounceHorizontalVelocity() {
    return this.getConfiguredDouble("bloodRitual.bounceHorizontalVelocity", 4.5);
  }

  double getBloodRitualBounceYVelocity() {
    return this.getConfiguredDouble("bloodRitual.bounceYVelocity", 3.0);
  }

  double getBloodRitualSpeedBoostSeconds() {
    return this.getConfiguredDouble("bloodRitual.speedBoostSeconds", 2.0);
  }

  double getBloodRitualBounceGraceSeconds() {
    return this.getConfiguredDouble("bloodRitual.bounceGraceSeconds", 0.5);
  }

  double getBloodRitualDamageRadius() {
    return this.getConfiguredDouble("bloodRitual.damageRadius", 1.5);
  }

  double getBloodRitualDamage() {
    return this.getConfiguredDouble("bloodRitual.damage", 4.0);
  }

  double getPassivesAutoCritChance() {
    return this.getConfiguredDouble("passives.autoCritChance", 0.55);
  }

  double getPassivesAutoCritMultiplier() {
    return this.getConfiguredDouble("passives.autoCritMultiplier", 1.5);
  }

  double getPassivesEnduringStrength1Seconds() {
    return this.getConfiguredDouble("passives.enduringStrength1Seconds", 960.0);
  }

  double getPassivesEnduringStrength2Seconds() {
    return this.getConfiguredDouble("passives.enduringStrength2Seconds", 480.0);
  }

  static void configureBloodsawDisplay(ItemStack item, ItemDisplay itemDisplay) {
    itemDisplay.setItemStack(item);
    itemDisplay.setBillboard(Billboard.FIXED);
    itemDisplay.setItemDisplayTransform(ItemDisplayTransform.FIXED);
    itemDisplay.setBrightness(new Brightness(15, 15));
    itemDisplay.setInterpolationDuration(1);
    itemDisplay.setTeleportDuration(1);
    itemDisplay.setGravity(false);
    itemDisplay.setInvulnerable(true);
    itemDisplay.setPersistent(false);
  }

  void applyTierOnePotionEffects(Player player) {
    player.removePotionEffect(PotionEffectType.STRENGTH);
    player.addPotionEffect(
        new PotionEffect(
            PotionEffectType.STRENGTH, this.getAbilityIntValue(this.getPassivesEnduringStrength1Seconds()), 0, false, true, true),
        true);
  }

  void applyPotionEffects(Player player) {
    player.removePotionEffect(PotionEffectType.STRENGTH);
    player.addPotionEffect(
        new PotionEffect(
            PotionEffectType.STRENGTH, this.getAbilityIntValue(this.getPassivesEnduringStrength2Seconds()), 1, false, true, true),
        true);
  }

  Integer initializeCounter(UUID playerId) {
    return this.getDurationTicks();
  }

  Integer initializeCounterForPlayer(UUID playerId) {
    return this.getDurationTicks();
  }

  static {
    BLOODSAWS_RECHARGE_ID = "bloodsaws_recharge";
    BLOOD_LINKING_ID = "bloodLinking";
    BLOOD_RITUAL_ID = "bloodRitual";
    currentDustOptions = new DustOptions(Color.RED, 1.5F);
    activeDustOptions = new DustOptions(Color.RED, 2.0F);
    dustOptions = new DustOptions(Color.RED, 1.8F);
    secondaryDustOptions = new DustOptions(Color.fromRGB(139, 0, 0), 2.5F);
    pendingDustOptions = new DustOptions(Color.BLACK, 1.5F);
  }

  private static final class BloodRitualFlightController extends BukkitRunnable {
    int remainingTicks;
    int maxCount;
    int chargeLevel;
    int durationSeconds;
    int cooldownTicks;
    boolean active;
    int animationStep;
    int durationTicks;
    HereticFlightPhase hereticFlightPhase;
    final Player player;
    final UUID playerId;
    final Location anchorLocation;
    final HereticGem hereticGem;
    static final String BLISS_LAUNCH_ID = "bliss.launch";
    BloodRitualFlightController(HereticGem hereticGem, Player player, UUID playerId, Location location) {
      this.hereticGem = hereticGem;
      this.player = player;
      this.playerId = playerId;
      this.anchorLocation = location;
      this.remainingTicks = 0;
      this.maxCount = 0;
      this.chargeLevel = 0;
      this.durationSeconds = 0;
      this.cooldownTicks = 0;
      this.active = false;
      this.animationStep = 0;
      this.durationTicks = 0;
      this.hereticFlightPhase = HereticFlightPhase.LAUNCH;
    }

    public void run() {
      if (this.player.isOnline() && !this.hereticGem.isAbilityActive(this.playerId)) {
        this.maxCount++;
        this.player.setFallDistance(0.0F);
        if (this.active) {
          this.cooldownTicks++;
          if (this.cooldownTicks >= this.hereticGem.getAbilityIntValue(this.hereticGem.getBloodRitualImpactTimeoutSeconds())) {
            this.refreshAbilityState();
            return;
          }
        }

        if (this.animationStep > 0) {
          this.animationStep--;
        }

        if (this.durationTicks > 0) {
          this.durationTicks--;
        }

        switch (this.hereticFlightPhase) {
          case LAUNCH:
            this.player.setGliding(true);
            this.player.setVelocity(new Vector(0.0, this.hereticGem.getBloodRitualLaunchYVelocity(), 0.0));
            this.updateState(this.anchorLocation.clone().add(0.0, this.hereticGem.getBloodRitualLaunchFxHeight(), 0.0));
            this.player.playSound(this.player.getLocation(), BLISS_LAUNCH_ID, 0.5F, 1.0F);
            this.hereticFlightPhase = HereticFlightPhase.WAIT;
            this.maxCount = 0;
            break;
          case WAIT:
            if (this.maxCount >= this.hereticGem.getAbilityIntValue(this.hereticGem.getBloodRitualWaitSeconds())) {
              this.hereticFlightPhase = HereticFlightPhase.GLIDE;
              this.chargeLevel = 0;
              this.player.setGliding(true);
              this.player.setVelocity(
                  this.player.getLocation().getDirection().normalize().multiply(this.hereticGem.getBloodRitualGlideSpeed()));
            }
            break;
          case GLIDE:
            this.chargeLevel++;
            this.durationSeconds++;
            if (this.durationSeconds % this.hereticGem.getBloodRitualTrailEveryTicks() == 0) {
              this.hereticGem.updatePlayerState(this.player);
            }

            this.player.setGliding(true);
            if (this.animationStep == 0) {
              double value = this.durationTicks > 0 ? this.hereticGem.getBloodRitualBoostedGlideSpeed() : this.hereticGem.getBloodRitualBaseGlideSpeed();
              this.player.setVelocity(this.player.getLocation().getDirection().normalize().multiply(value));
            }

            if (this.animationStep == 0 && this.player.isOnGround() || this.chargeLevel >= this.hereticGem.getAbilityIntValue(this.hereticGem.getBloodRitualMaxGlideSeconds())) {
              this.hereticFlightPhase = HereticFlightPhase.IDLE;
              this.active = true;
              this.cooldownTicks = 0;
              this.initialize();
            }
          case IDLE:
        }
      } else {
        this.refreshAbilityState();
      }
    }

    void updateState(Location location) {
      this.hereticGem.finishAbilityAction(location, this.hereticGem.getBloodRitualSpawnCircleRadius(), Particle.SOUL_FIRE_FLAME, this.hereticGem.getBloodRitualSpawnCircleCount());
      this.hereticGem.spawnPrimaryAbilityParticles(location, this.hereticGem.getBloodRitualSpawnRayCount(), this.hereticGem.getBloodRitualSpawnRayLength());
    }

    void initialize() {
      Location location = this.player.getLocation();
      boolean enabled = this.hereticGem.isLocationBlocked(this.player, location);
      this.hereticGem.finishAbilityAction(location, this.hereticGem.getBloodRitualImpactCircleRadius(), Particle.SOUL_FIRE_FLAME, this.hereticGem.getBloodRitualImpactCircleCount());
      if (enabled) {
        this.active = false;
        this.cooldownTicks = 0;
        this.remainingTicks++;
        if (this.remainingTicks >= this.hereticGem.getBloodRitualMaxLaunches()) {
          this.refreshAbilityState();
          return;
        }

        new BukkitRunnable() {
          final BloodRitualFlightController motionTask = BloodRitualFlightController.this;
          static final String BLISS_LAUNCH_ID = "bliss.launch";
          @Override
          public void run() {
            if (this.motionTask.player.isOnline()) {
              this.motionTask.player.setGliding(false);
              this.motionTask.player.setFallDistance(0.0F);
              this.motionTask.player.setGliding(true);
              Vector launchVelocity = this.motionTask.player.getLocation().getDirection().normalize().multiply(this.motionTask.hereticGem.getBloodRitualBounceHorizontalVelocity());
              launchVelocity.setY(this.motionTask.hereticGem.getBloodRitualBounceYVelocity());
              this.motionTask.player.setVelocity(launchVelocity);
              this.motionTask.player.playSound(this.motionTask.player.getLocation(), BLISS_LAUNCH_ID, 1.0F, 1.2F);
              this.motionTask.durationTicks = this.motionTask.hereticGem.getAbilityIntValue(this.motionTask.hereticGem.getBloodRitualSpeedBoostSeconds());
              this.motionTask.hereticFlightPhase = HereticFlightPhase.GLIDE;
              this.motionTask.chargeLevel = 0;
              this.motionTask.maxCount = 0;
              this.motionTask.animationStep = this.motionTask.hereticGem.getAbilityIntValue(this.motionTask.hereticGem.getBloodRitualBounceGraceSeconds());
            }
          }


        }.runTaskLater(this.hereticGem.plugin, 1L);
      }
    }

    void refreshAbilityState() {
      this.player.setGliding(false);
      this.player.setFallDistance(0.0F);
      this.hereticGem.secondaryTasksByPlayer.remove(this.playerId);
      this.cancel();
    }

}
}
