package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class FluxTier1Gem implements Listener {
  final Bliss plugin;
  final Cache<UUID, Boolean> cache =
      CacheBuilder.newBuilder().expireAfterWrite(500L, TimeUnit.MILLISECONDS).build();
  final Map<UUID, Integer> abilityStages = new ConcurrentHashMap<>();
  final Map<UUID, Integer> playerCounters = new ConcurrentHashMap<>();
  final Map<UUID, Double> valuesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Deque<FluxDamageSample>> secondaryDamageSamplesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, BukkitTask> scheduledTasks = new ConcurrentHashMap<>();
  final Map<UUID, BukkitTask> cooldownTasks = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Long> cooldownTimestamps = new ConcurrentHashMap<>();
  final Map<UUID, Set<String>> activeTagsByPlayer = new ConcurrentHashMap<>();
  TrustCommand trustCommand;
  AstraTier2Gem astraTier2Gem;
  static final String CROSSBOW_STUN_ARROW_ID;
  static final String STATIC_BURST_ID;
  static final int cooldownTicks;
  static final int DEFAULT_DURATION_TICKS;

  public FluxTier1Gem(Bliss bliss) {
    this.plugin = bliss;
    this.startBackgroundTasks();
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

  boolean isTrustedPlayer(Player player, Player targetPlayer) {
    return this.trustCommand != null && this.trustCommand.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId());
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              FluxTier1Gem.this.cleanupAbilityState(player.getUniqueId());
              if (!FluxTier1Gem.this.plugin.isGemsDisabled()
                 
                  && !FluxTier1Gem.this.plugin.canUseAbility(player)) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (!FluxTier1Gem.this.isAbilityActive(player.getUniqueId())
                    && (FluxTier1Gem.isGemItem(mainHandItem) || FluxTier1Gem.isGemItem(offHandItem))) {
                  player.removePotionEffect(PotionEffectType.WEAKNESS);
                  player.removePotionEffect(PotionEffectType.HUNGER);
                  player.removePotionEffect(PotionEffectType.SLOWNESS);
                }

                if (FluxTier1Gem.this.canUseAbility(player)) {
                  if (FluxTier1Gem.this.isAbilityActive(player.getUniqueId())) {
                    long remainingMillis = FluxTier1Gem.this.getAbilityLong(player.getUniqueId());
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
                    FluxTier1Gem.this.sendAbilityFeedback(player);
                  }
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            long now = System.currentTimeMillis();
            Iterator<Entry<UUID,Long>> iterator = FluxTier1Gem.this.lastUseTimes.entrySet().iterator();

            while (iterator.hasNext()) {
              Entry<UUID, Long> entry = iterator.next();
              UUID playerId = entry.getKey();
              long timestamp = entry.getValue();
              if (now - timestamp > 5000L) {
                FluxTier1Gem.this.abilityStages.put(playerId, 0);
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                  player.removePotionEffect(PotionEffectType.HASTE);
                }

                iterator.remove();
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(100L), 100L);
  }

  public ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(97);
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
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  boolean canUseAbility(Player player) {
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    return isGemItem(mainHandItem) || isGemItem(offHandItem);
  }

  public static boolean isGemItem(ItemStack item) {
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

  void sendAbilityFeedback(Player player) {
    UUID playerId = player.getUniqueId();
    ActiveAbilityStore.settleExpired(playerId, "static_burst");
    if (ActiveAbilityStore.isActive(playerId, "static_burst")) {
      String text = "&b🔺 &cActive...";
      ActionBarQueue.enqueue(player, ChatColor.translateAlternateColorCodes('&', text));
    } else if (CooldownService.isOnCooldown(playerId, "static_burst")) {
        String message = AbilityStatusFormatter.formatDisplayText(playerId, "static_burst", false);
        String displayText = "&b🔺 &b" + message;
        ActionBarQueue.enqueue(player, ChatColor.translateAlternateColorCodes('&', displayText));
      } else {
        String formattedText = "&b🔺 &aReady!";
        ActionBarQueue.enqueue(player, ChatColor.translateAlternateColorCodes('&', formattedText));
      }
    
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (!this.isAbilityActive(player.getUniqueId())) {
        if (event.getDamager() instanceof Creeper creeper) {
          ItemStack mainHandItem = player.getInventory().getItemInMainHand();
          ItemStack offHandItem = player.getInventory().getItemInOffHand();
          if (creeper.isPowered() && (isGemItem(mainHandItem) || isGemItem(offHandItem))) {
            event.setCancelled(true);
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_AIR
        || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        if (!this.plugin.isGemsDisabled()) {
          if (true) {
            if (!this.plugin.canUseAbility(player)) {
              if (this.canUseAbility(player)) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                if (isGemItem(mainHandItem) && player.isSneaking()) {
                  if (this.isAbilityAllowed(player)) {
                    event.setCancelled(true);
                  }
                } else if (player.isSneaking()) {
                  ActiveAbilityStore.settleExpired(playerId, "static_burst");
                  if (!ActiveAbilityStore.isActive(playerId, "static_burst")) {
                    if (!CooldownService.isOnCooldown(playerId, "static_burst")) {
                      this.applyAbilityMotion(player);
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

  void applyAbilityMotion(Player player) {
    UUID playerId = player.getUniqueId();
    long timestamp = ConfigValueCache.getInt(this.plugin, "fluxT1.kineticBurstCooldown", 30);
    if (player.getInventory().contains(Material.DRAGON_EGG)) {
      timestamp /= 2L;
    }

    ActiveAbilityStore.startActive(playerId, "static_burst", 3L, timestamp);
    player.sendMessage(
        ChatColor.of("#befff7")
            + "🔺 You have activated "
            + ChatColor.of("#FF686F")
            + "Kinetic Burst");
    Location location = player.getLocation();
    double value = this.valuesByPlayer.getOrDefault(playerId, 0.0);

    for (Entity entity : location.getWorld().getNearbyEntities(location, 5.0, 5.0, 5.0)) {
      if (entity instanceof Player targetPlayer
          && !targetPlayer.equals(player)
          && !this.isTrustedPlayer(player, targetPlayer)) {
        Vector offset = targetPlayer.getLocation().toVector().subtract(location.toVector());
        offset.setY(Math.max(-0.5, Math.min(0.5, offset.getY())));
        offset = offset.normalize();
        targetPlayer.setVelocity(offset.multiply(1.5));
        double distance = targetPlayer.getMaxHealth() * 0.03 * Math.max(1.0, value);
        targetPlayer.damage(distance);
      }
    }

    this.updateState(location);
    this.valuesByPlayer.put(playerId, 0.0);
  }

  void updateState(Location location) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;
      double effectRadius = 1.0;

      @Override
      public void run() {
        if (this.durationTicks >= 10) {
          FluxTier1Gem.this.refreshAbilityState(this.anchorLocation, this.effectRadius);
          this.cancel();
        } else {
          FluxTier1Gem.this.spawnAbilityParticles(this.anchorLocation, this.effectRadius);
          this.effectRadius += 0.5;
          this.durationTicks++;
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void spawnAbilityParticles(Location location, double value) {
    World world = location.getWorld();
    if (world != null) {
      double distance = 0.1;
      double radius = (Math.PI * 2) * value;
      int count = (int) Math.floor(radius / distance);
      count = Math.max(count, 8);

      for (int index = 0; index < count; index++) {
        double angle = index * 360.0 / count;
        double angleRadians = Math.toRadians(angle);
        double offset = location.getX() + value * Math.cos(angleRadians);
        double progress = location.getZ() + value * Math.sin(angleRadians);
        Location targetLocation = new Location(world, offset, location.getY(), progress);
        DustOptions dustOptions = new DustOptions(Color.fromRGB(94, 215, 255), 1.0F);
        world.spawnParticle(Particle.DUST, targetLocation, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
        world.spawnParticle(Particle.SMOKE, targetLocation, 1, 0.0, 0.0, 0.0, 0.0);
      }
    }
  }

  void refreshAbilityState(Location location, double value) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      final double effectRadius = value;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks >= 7) {
          this.cancel();
        } else {
          FluxTier1Gem.this.spawnAbilityParticles(this.anchorLocation, this.effectRadius);
          this.durationTicks++;
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(6L), 6L);
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (isGemItem(event.getItemDrop().getItemStack())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (!this.plugin.isGemsDisabled()) {
        if (true) {
          if (!this.plugin.canUseAbility(player)) {
            if (this.canUseAbility(player)) {
              this.lastUseTimes.put(playerId, System.currentTimeMillis());
              this.abilityStages.put(playerId, this.abilityStages.getOrDefault(playerId, 0) + 1);
              int count = this.abilityStages.get(playerId);
              int index = Math.min(9, count / 10 - 1);
              if (count % 10 == 0 && count <= 100) {
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, index));
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void updateAbilityState(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      this.applyAbilityEffects(player, event);
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (!(event instanceof EntityDamageByEntityEvent)) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          player.removePotionEffect(PotionEffectType.HASTE);
          this.abilityStages.put(playerId, 0);
          this.lastUseTimes.remove(playerId);
          if (this.canUseAbility(player)) {
            double value = event.getFinalDamage() / 6.0;
            this.spawnAbilityEffects(playerId, value);
          }
        }
      }
    }
  }

  void applyAbilityEffects(Player player, EntityDamageByEntityEvent event) {
    UUID playerId = player.getUniqueId();
    player.removePotionEffect(PotionEffectType.HASTE);
    this.abilityStages.put(playerId, 0);
    this.lastUseTimes.remove(playerId);
    if (this.canUseAbility(player)) {
      double value = event.getFinalDamage() / 6.0;
      this.spawnAbilityEffects(playerId, value);
    }
  }

  public void playAbilityEffects() {
    new BukkitRunnable() {
      @Override
      public void run() {
        FluxTier1Gem.this.cache.cleanUp();
        HashSet<UUID> trackedIds = new HashSet<>();
        trackedIds.addAll(FluxTier1Gem.this.abilityStages.keySet());
        trackedIds.addAll(FluxTier1Gem.this.playerCounters.keySet());
        trackedIds.addAll(FluxTier1Gem.this.valuesByPlayer.keySet());
        trackedIds.addAll(FluxTier1Gem.this.scheduledTasks.keySet());
        trackedIds.addAll(FluxTier1Gem.this.cooldownTasks.keySet());
        trackedIds.addAll(FluxTier1Gem.this.activeTagsByPlayer.keySet());
        trackedIds.addAll(FluxTier1Gem.this.cooldownTimestamps.keySet());

        for (UUID playerId : trackedIds) {
          Player player = Bukkit.getPlayer(playerId);
          if (player == null || !player.isOnline()) {
            FluxTier1Gem.this.abilityStages.remove(playerId);
            FluxTier1Gem.this.playerCounters.remove(playerId);
            FluxTier1Gem.this.valuesByPlayer.remove(playerId);
            FluxTier1Gem.this.secondaryDamageSamplesByPlayer.remove(playerId);
            FluxTier1Gem.this.lastUseTimes.remove(playerId);
            FluxTier1Gem.this.activeTagsByPlayer.remove(playerId);
            FluxTier1Gem.this.cooldownTimestamps.remove(playerId);
            ActiveAbilityStore.clearPlayer(playerId);
            BukkitTask task = FluxTier1Gem.this.scheduledTasks.get(playerId);
            if (task != null) {
              task.cancel();
              FluxTier1Gem.this.scheduledTasks.remove(playerId);
            }

            BukkitTask scheduledTask = FluxTier1Gem.this.cooldownTasks.get(playerId);
            if (scheduledTask != null) {
              scheduledTask.cancel();
              FluxTier1Gem.this.cooldownTasks.remove(playerId);
            }
          }
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1200L), 1200L);
  }

  public void activateAbility(Player player) {
    UUID playerId = player.getUniqueId();
    ActiveAbilityStore.clearPlayer(playerId);
    this.secondaryDamageSamplesByPlayer.remove(playerId);
  }

  boolean isAbilityAllowed(Player player) {
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
  public void onEntityShootBow(EntityShootBowEvent event) {
    if (event.getProjectile() instanceof AbstractArrow abstractArrow) {
      if (event.getBow() != null && event.getBow().getType() == Material.CROSSBOW) {
        if (event.getEntity() instanceof Player player) {
          if (isGemItem(player.getInventory().getItemInMainHand())
              || isGemItem(player.getInventory().getItemInOffHand())) {
            if (java.util.concurrent.ThreadLocalRandom.current().nextInt(100) <= cooldownTicks) {
              abstractArrow.setMetadata(
                  "crossbow_stun_arrow", new FixedMetadataValue(Bliss.getInstance(), true));
              Location location = abstractArrow.getLocation();
              World world = location.getWorld();
              if (world != null) {
                world.spawnParticle(
                    Particle.DUST,
                    location,
                    12,
                    0.15,
                    0.15,
                    0.15,
                    0.0,
                    new DustOptions(Color.AQUA, 1.2F));
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
                0.0,
                new DustOptions(Color.AQUA, 1.4F));
            if (livingEntity instanceof Player player) {
              UUID playerId = player.getUniqueId();
              long now = System.currentTimeMillis() + DEFAULT_DURATION_TICKS * 1000L;
              this.cooldownTimestamps.put(playerId, now);
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    Long storedTimestamp = this.cooldownTimestamps.get(playerId);
    if (storedTimestamp != null) {
      if (System.currentTimeMillis() >= storedTimestamp) {
        this.cooldownTimestamps.remove(playerId);
      } else {
        Location location = event.getFrom();
        Location targetLocation = event.getTo();
        if (targetLocation != null) {
          Location origin = location.clone();
          origin.setYaw(targetLocation.getYaw());
          origin.setPitch(targetLocation.getPitch());
          event.setTo(origin);
        }
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    this.abilityStages.remove(playerId);
    this.playerCounters.remove(playerId);
    this.valuesByPlayer.remove(playerId);
    this.secondaryDamageSamplesByPlayer.remove(playerId);
    this.lastUseTimes.remove(playerId);
    this.activeTagsByPlayer.remove(playerId);
    this.cooldownTimestamps.remove(playerId);
    BukkitTask task = this.scheduledTasks.remove(playerId);
    if (task != null) {
      task.cancel();
    }

    BukkitTask scheduledTask = this.cooldownTasks.remove(playerId);
    if (scheduledTask != null) {
      scheduledTask.cancel();
    }
  }

  void spawnAbilityEffects(UUID playerId, double value) {
    this.valuesByPlayer.merge(playerId, value, Double::sum);
    long now = System.currentTimeMillis() + 120000L;
    this.secondaryDamageSamplesByPlayer
        .computeIfAbsent(playerId, FluxTier1Gem::createDamageSampleQueue)
        .addLast(new FluxDamageSample(value, now));
  }

  void cleanupAbilityState(UUID playerId) {
    Deque<FluxDamageSample> deque = this.secondaryDamageSamplesByPlayer.get(playerId);
    if (deque != null && !deque.isEmpty()) {
      long now = System.currentTimeMillis();
      double value = 0.0;

      while (!deque.isEmpty() && (deque.peekFirst()).expiresAtMillis() <= now) {
        value += (deque.pollFirst()).amount();
      }

      if (value > 0.0) {
        double distance = this.valuesByPlayer.getOrDefault(playerId, 0.0);
        double radius = Math.max(0.0, distance - value);
        if (radius <= 0.0) {
          this.valuesByPlayer.remove(playerId);
        } else {
          this.valuesByPlayer.put(playerId, radius);
        }
      }

      if (deque.isEmpty()) {
        this.secondaryDamageSamplesByPlayer.remove(playerId);
      }

      ActiveAbilityStore.settleExpired(playerId, "static_burst");
    } else {
      ActiveAbilityStore.settleExpired(playerId, "static_burst");
    }
  }

  static Deque<FluxDamageSample> createDamageSampleQueue(UUID playerId) {
    return new ArrayDeque<>();
  }

  static {
    CROSSBOW_STUN_ARROW_ID = "crossbow_stun_arrow";
    STATIC_BURST_ID = "static_burst";
    cooldownTicks = ConfigValueCache.getInt(Bliss.getInstance(), "fluxT1.shockingChance", 10);
    DEFAULT_DURATION_TICKS = ConfigValueCache.getInt(Bliss.getInstance(), "fluxT1.shockDuration", 1);
  }
}
