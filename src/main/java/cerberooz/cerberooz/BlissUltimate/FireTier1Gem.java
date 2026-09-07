package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class FireTier1Gem implements Listener {
  static final String FIRE_CRISP_ID;
  Bliss plugin;
  TrustCommand trustCommand;
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  Map<UUID, Location> originLocations = new HashMap<>();
  Map<UUID, Location> targetLocations = new HashMap<>();
  static final EnumMap<Material, Material> secondaryMATERIAL_CONVERSIONS;
  static final EnumMap<Material, Material> MATERIAL_CONVERSIONS;
  final Map<UUID, Location> playerLocations = new ConcurrentHashMap<>();
  final Map<UUID, Map<Location, BlockData>> temporaryBlockSnapshots = new ConcurrentHashMap<>();
  final Map<UUID, Map<Location, BlockData>> blockSnapshots = new ConcurrentHashMap<>();
  final Map<UUID, BukkitTask> cooldownTasks = new ConcurrentHashMap<>();
  final Set<Location> secondaryTrackedLocations = new HashSet<>();
  AstraTier2Gem astraTier2Gem;
  Map<UUID, Boolean> secondaryFlagsByPlayer = new HashMap<>();
  Map<UUID, Set<UUID>> playerRelations = new HashMap<>();
  Map<UUID, Set<Location>> pendingLocationsByPlayer = new HashMap<>();
  Map<UUID, Map<Location, Material>> primaryMaterialValuesByLocation = new HashMap<>();
  Map<UUID, Set<Location>> targetSetsByPlayer = new HashMap<>();
  Map<Location, Material> cachedMaterialsByLocation = new HashMap<>();
  Map<Location, Material> activeMaterialsByLocation = new HashMap<>();
  Map<Location, Material> materialsByLocation = new HashMap<>();
  Map<Location, Material> trackedMaterialsByLocation = new HashMap<>();
  Set<Location> activeTrackedLocations = new HashSet<>();
  Set<Location> trackedLocations = new HashSet<>();
  boolean secondaryActive = false;
  boolean active = false;
  Map<UUID, BukkitRunnable> currentTasksByPlayer = new HashMap<>();
  final Map<UUID, BukkitTask> scheduledTasks = new ConcurrentHashMap<>();

  public void setAstraTier2Gem(AstraTier2Gem astraTier2Gem) {
    this.astraTier2Gem = astraTier2Gem;
  }

  boolean isAbilityActive(UUID playerId) {
    return this.astraTier2Gem != null && this.astraTier2Gem.hasRequiredState(playerId);
  }

  long getAbilityLong(UUID playerId) {
    return this.astraTier2Gem == null ? 0L : this.astraTier2Gem.getAbilityLong(playerId);
  }

  public FireTier1Gem(Bliss bliss) {
    this.plugin = bliss;
    this.scheduleAbilityUpdate();
    this.startBackgroundTasks();
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          final Bliss plugin = bliss;

          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              boolean enabled = FireTier1Gem.isGemItem(mainHandItem) || FireTier1Gem.isGemItem(offHandItem);
              if (enabled) {
                if ((FireTier1Gem.isGemItem(mainHandItem) || FireTier1Gem.isGemItem(offHandItem))
                    && FireTier1Gem.this.isAbilityActive(player.getUniqueId())) {
                  long remainingMillis = FireTier1Gem.this.getAbilityLong(player.getUniqueId());
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
                } else if (!this.plugin.isGemsDisabled()
                   
                    && !this.plugin.canUseAbility(player)) {
                  player.addPotionEffect(
                      new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1, true, true, true));
                  if (CooldownService.isOnCooldown(player.getUniqueId(), "fire_crisp")) {
                    String displayText =
                    AbilityStatusFormatter.formatDisplayText(
                        player.getUniqueId(), "fire_crisp", false);
                    String gold = ChatColor.GOLD.toString();
                    String textColor = ChatColor.AQUA.toString();
                    ActionBarQueue.enqueue(player, gold + "🔺 " + textColor + displayText);
                  } else {
                    ActionBarQueue.enqueue(
                        player, ChatColor.GOLD + "🔺 " + ChatColor.GREEN + "Ready!");
                  }
                } else {
                  String configColor = ChatColor.DARK_GRAY.toString();
                  String keyColor = ChatColor.BOLD.toString();
                  ActionBarQueue.enqueue(
                      player,
                      "🔒 " + configColor + keyColor + "ᴅɪꜱᴀʙʟᴇᴅ");
                }
              }
            }
          }
        }, bliss, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  String formatDisplayText(long timestamp, boolean enabled) {
    long lastUpdateTime = timestamp / 1000L;
    if (enabled) {
      return String.format("%ds", lastUpdateTime);
    }

    long startTime = lastUpdateTime / 60L;
    long expiryTime = lastUpdateTime % 60L;
    return startTime > 0L ? String.format("%dm %ds", startTime, expiryTime) : String.format("%ds", expiryTime);
  }

  public ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.of("#befff7") + "Energy:");
      lore.add(ChatColor.of("#82EDBF") + "Pristine");
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      lore.add("");
      lore.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fire Resistance");
      lore.add(ChatColor.GRAY + "- Auto Smelt");
      lore.add(ChatColor.GRAY + "- Flamestrike");
      lore.add(ChatColor.GRAY + "- Fireshot");
      lore.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      lore.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static boolean isGemItem(ItemStack item) {
    if (item == null || item.getType() != Material.AMETHYST_SHARD) {
      return false;
    } else if (!item.hasItemMeta()) {
      return false;
    } else {
      ItemMeta itemMeta = item.getItemMeta();
      if (itemMeta != null && itemMeta.hasCustomModelData()) {
        int count = itemMeta.getCustomModelData();
        return count != 1 && count != 81 && count != 61 && count != 41 && count != 21
            ? false
            : itemMeta.hasDisplayName()
                && ChatColor.stripColor(itemMeta.getDisplayName())
                    .equalsIgnoreCase("ғɪʀᴇ ɢᴇᴍ");
      } else {
        return false;
      }
    }
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    Player player = event.getEntity().getKiller();
    if (player != null) {
      if (isGemItem(player.getInventory().getItemInMainHand())
          || isGemItem(player.getInventory().getItemInOffHand())) {
        List<ItemStack> entries = event.getDrops();

        for (int count = 0; count < entries.size(); count++) {
          ItemStack item = entries.get(count);
          switch (item.getType()) {
            case MUTTON:
              entries.set(count, new ItemStack(Material.COOKED_MUTTON, item.getAmount()));
              break;
            case BEEF:
              entries.set(count, new ItemStack(Material.COOKED_BEEF, item.getAmount()));
              break;
            case PORKCHOP:
              entries.set(count, new ItemStack(Material.COOKED_PORKCHOP, item.getAmount()));
              break;
            case CHICKEN:
              entries.set(count, new ItemStack(Material.COOKED_CHICKEN, item.getAmount()));
              break;
            case RABBIT:
              entries.set(count, new ItemStack(Material.COOKED_RABBIT, item.getAmount()));
          }
        }

        event.getEntity()
            .getWorld()
            .playEffect(event.getEntity().getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 1);
      }
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    Player player = event.getPlayer();
    Item item = event.getItemDrop();
    ItemStack heldItem = item.getItemStack();
    if (isGemItem(heldItem)) {
      event.setCancelled(true);
    }

    Location originLocation = this.originLocations.get(player.getUniqueId());
    if (originLocation != null) {
      Location location = player.getLocation();
      if (location.getWorld().equals(originLocation.getWorld()) && location.distance(originLocation) <= 5.0) {
        Material material = secondaryMATERIAL_CONVERSIONS.get(heldItem.getType());
        if (material != null) {
          heldItem.setType(material);
          item.setItemStack(heldItem);
          player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
          new BukkitRunnable() {
            @Override
            public void run() {
              if (item.isValid()) {
                player.getWorld().spawnParticle(Particle.FLAME, item.getLocation(), 5, 0.2, 0.2, 0.2, 0.01);
              }
            }
          }.runTask(this.plugin);
        }
      }
    }
  }

  boolean isAbilityAllowed(ItemStack item, Player player) {
    if (item != null && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      if (itemMeta != null && itemMeta.hasCustomModelData()) {
        int count = itemMeta.getCustomModelData();
        return count == 1 || count == 81 || count == 61 || count == 41 || count == 21;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  boolean isAbilityBlocked(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Material material = item.getType();
      return material.name().endsWith("_SWORD") || material.name().endsWith("_AXE");
    } else {
      return true;
    }
  }

  boolean isMatchingState(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Material material = item.getType();
      return material == Material.FIREWORK_ROCKET
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

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_AIR
        || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      Player player = event.getPlayer();
      if (player.isSneaking()) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          if (!this.plugin.isGemsDisabled()
             
              && !this.plugin.canUseAbility(player)) {
            if (this.isAbilityAllowed(player.getInventory().getItemInOffHand(), player)) {
              if (this.isAbilityBlocked(player.getInventory().getItemInMainHand())) {
                long now = System.currentTimeMillis();
                if (CooldownService.isOnCooldown(playerId, "fire_crisp")) {
                  long timestamp = CooldownService.remainingMillis(playerId, "fire_crisp");
                  String gold = ChatColor.GOLD.toString();
                  String red = ChatColor.RED.toString();
                  String messageColor = ChatColor.GOLD.toString();
                  String displayColor = ChatColor.RED.toString();
                  String textColor = ChatColor.GOLD.toString();
                  String text = AbilityStatusFormatter.formatDisplayTextForPlayer(timestamp, false);
                  player.sendMessage(
                      gold
                          + "🔺 "
                          + red
                          + "Your "
                          + messageColor
                          + "Crisp "
                          + displayColor
                          + "skill is on cooldown for "
                          + textColor
                          + text);
                } else if (this.isMatchingState(player.getInventory().getItemInMainHand())) {
                    if (player.isSneaking()) {
                      this.spawnAbilityParticles(player);
                    }
                  } else {
                    this.spawnAbilityParticles(player);
                  }
                
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void refreshAbilityState(PlayerDropItemEvent event) {
    if (this.isLocationBlocked(event.getItemDrop().getLocation())) {
      ItemStack item = event.getItemDrop().getItemStack();
      Material material = secondaryMATERIAL_CONVERSIONS.get(item.getType());
      if (material != null) {
        ItemStack heldItem = new ItemStack(material, item.getAmount());
        event.getItemDrop().setItemStack(heldItem);
        event.getItemDrop()
            .getWorld()
            .playSound(event.getItemDrop().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5F, 1.0F);
      }
    }
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            double configuredValue = ConfigValueCache.getDouble(FireTier1Gem.this.plugin, "fireT1.crispFireDamagePerSecond", 1.0);
            double distance = ConfigValueCache.getDouble(FireTier1Gem.this.plugin, "fireT1.crispRadius", 5.0);

            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();

              for (Entry<UUID,Location> entry : FireTier1Gem.this.originLocations.entrySet()) {
                UUID targetId = entry.getKey();
                Location location = entry.getValue();
                if (location != null
                    && player.getWorld().equals(location.getWorld())
                    && !(player.getLocation().distanceSquared(location) > distance * distance)
                    && !playerId.equals(targetId)
                    && !FireTier1Gem.this.isValidTarget(targetId, playerId)
                    && (FireTier1Gem.this.trustCommand == null || !FireTier1Gem.this.trustCommand.isAbilityActive(targetId, playerId))) {
                  player.setFireTicks(60);
                  if (configuredValue > 0.0) {
                    player.damage(configuredValue, DamageSource.builder(DamageType.GENERIC).build());
                  }
                  break;
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  boolean isLocationBlocked(Location location) {
    double configuredValue = ConfigValueCache.getDouble(this.plugin, "fireT1.crispRadius", 5.0);

    for (Location targetLocation : this.playerLocations.values()) {
      if (targetLocation != null
          && location.getWorld().equals(targetLocation.getWorld())
          && location.distanceSquared(targetLocation) <= configuredValue * configuredValue) {
        return true;
      }
    }

    return false;
  }

  void spawnAbilityParticles(Player player) {
    UUID playerId = player.getUniqueId();
    Location location = player.getLocation();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        player.sendMessage(
            ChatColor.GOLD
                + "🔮 "
                + ChatColor.RED
                + "You have activated "
                + ChatColor.GOLD
                + "Crisp");
        this.playerLocations.put(playerId, location.clone());
        this.originLocations.put(playerId, location.clone());
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.REGENERATION, 200, 0, false, false, false));
        long timestamp = ConfigValueCache.getInt(this.plugin, "fireT1.crispCooldown", 40);
        if (player.getInventory().contains(Material.DRAGON_EGG)) {
          timestamp /= 2L;
        }

        int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT1.crispDuration", 10);
        int index = ConfigValueCache.getInt(this.plugin, "fireT1.crispRadius", 5);
        ActiveAbilityStore.startActive(playerId, "fire_crisp", configuredValue, timestamp);
        new BukkitRunnable() {
          final int maxCount = configuredValue;
          final Location anchorLocation = location;
          final UUID capturedPlayerId = playerId;
          final int durationTicks = configuredValue;
          int cooldownTicks = 1;
          final Map<Location, BlockData> secondaryBlockDataByLocation = new HashMap<>();
          final Map<Location, BlockData> blockDataByLocation = new HashMap<>();

          @Override
          public void run() {
            if (this.cooldownTicks <= this.maxCount) {
              FireTier1Gem.this.updateAbilityState(this.anchorLocation, this.cooldownTicks, this.secondaryBlockDataByLocation, this.blockDataByLocation);
              this.cooldownTicks++;
            } else {
              FireTier1Gem.this.temporaryBlockSnapshots.put(this.capturedPlayerId, this.secondaryBlockDataByLocation);
              FireTier1Gem.this.blockSnapshots.put(this.capturedPlayerId, this.blockDataByLocation);
              FireTier1Gem.this.playAbilityEffects(this.capturedPlayerId, this.anchorLocation, this.maxCount);
              BukkitTask scheduledTask = FireTier1Gem.this.resolveBukkitTask(this.anchorLocation, this.maxCount);
              FireTier1Gem.this.scheduledTasks.put(this.capturedPlayerId, scheduledTask);
              new BukkitRunnable() {
                @Override
                public void run() {
                  BukkitTask task = FireTier1Gem.this.cooldownTasks.remove(playerId);
                  if (task != null) {
                    task.cancel();
                  }

                  BukkitTask scheduledTask = FireTier1Gem.this.scheduledTasks.remove(playerId);
                  if (scheduledTask != null) {
                    scheduledTask.cancel();
                  }

                  FireTier1Gem.this.spawnAbilityEffects(playerId, secondaryBlockDataByLocation, blockDataByLocation);
                  FireTier1Gem.this.playerLocations.remove(playerId);
                  FireTier1Gem.this.originLocations.remove(playerId);
                  FireTier1Gem.this.temporaryBlockSnapshots.remove(playerId);
                  FireTier1Gem.this.blockSnapshots.remove(playerId);
                }
              }.runTaskLater(FireTier1Gem.this.plugin, this.durationTicks * 20L);
              this.cancel();
            }
          }
        }
            .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(5L), 1L);
        DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 119, 0), 1.0F);
        ParticleEffects.spawnPrimaryAbilityParticles(location, Color.WHITE);
        ParticleEffects.createSimpleDustTrailTask(dustOptions, location, index, 5);
        location.getWorld().spawnParticle(Particle.FLAME, location, 100, 0.7, 0.7, 0.7);
        location.getWorld().spawnParticle(Particle.SMOKE, location, 100, 0.7, 0.7, 0.7);
        location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0F, 1.0F);
        location.getWorld().playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
      }
    }
  }

  BukkitTask resolveBukkitTask(Location location, int count) {
    World world = location.getWorld();
    return world == null
        ? null
        : new BukkitRunnable() {
          final Location anchorLocation = location;
          final int durationTicks = count;
          DustOptions dustOptions = new DustOptions(Color.fromRGB(58, 58, 58), 2.0F);
          int maxCount = 0;

          @Override
          public void run() {
            if (this.maxCount % 8 == 0) {
              world.playSound(this.anchorLocation, Sound.BLOCK_FIRE_AMBIENT, 1.0F, 1.0F);
            }

            ParticleEffects.updateState(
                Particle.FLAME,
                this.anchorLocation.clone().add(0.0, 1.0, 0.0),
                this.durationTicks,
                120,
                1,
                0.1,
                0.1,
                0.1,
                0.0,
                0.0F,
                0.0F,
                0.0F);
            ParticleEffects.refreshPlayerState(
                this.dustOptions, this.anchorLocation, this.durationTicks, 120, 1, 0.1, 0.1, 0.1, 0.0, 0.0F, 0.0F, 0.0F);
            this.maxCount += 5;
          }
        }
            .runTaskTimer(
                this.plugin,
                SharedScheduler.staggeredInitialDelay(0L),
                SharedScheduler.staggeredInitialDelay(5L));
  }

  void updateAbilityState(
      Location location, int count, Map<Location, BlockData> blocksByLocation, Map<Location, BlockData> currentBlocksByLocation) {
    World world = location.getWorld();
    int index = count * count;
    int remaining = location.getBlockX();
    int step = location.getBlockY();
    int ticks = location.getBlockZ();

    for (int durationTicks = -count; durationTicks <= count; durationTicks++) {
      for (int attempts = -count; attempts <= count; attempts++) {
        for (int amount = -count; amount <= count; amount++) {
          int level = durationTicks * durationTicks + attempts * attempts + amount * amount;
          if (level >= (count - 1) * (count - 1) && level <= index + count) {
            Location targetLocation = new Location(world, remaining + durationTicks, step + attempts, ticks + amount);
            Block block = targetLocation.getBlock();
            Material material = block.getType();
            if (material != Material.WATER && material != Material.LAVA) {
              Material targetMaterial = this.resolveMaterial(material);
              if (targetMaterial != null) {
                this.updateState(targetLocation, block, blocksByLocation);
                block.setType(targetMaterial, false);
                currentBlocksByLocation.put(targetLocation.clone(), block.getBlockData().clone());
              }
            } else {
              this.updateState(targetLocation, block, blocksByLocation);
              block.setType(Material.AIR);
              currentBlocksByLocation.put(targetLocation.clone(), block.getBlockData().clone());
              world.spawnParticle(Particle.CLOUD, targetLocation, 5, 0.2, 0.2, 0.2, 0.02);
              world.playSound(targetLocation, Sound.BLOCK_FIRE_EXTINGUISH, 0.3F, 1.0F);
            }
          }
        }
      }
    }
  }

  void updateState(Location location, Block block, Map<Location, BlockData> blocksByLocation) {
    if (!blocksByLocation.containsKey(location)) {
      Location targetLocation = location.clone();
      blocksByLocation.put(targetLocation, block.getBlockData().clone());
      this.secondaryTrackedLocations.add(targetLocation);
    }
  }

  Material resolveMaterial(Material material) {
    if (material != Material.GRASS_BLOCK && material != Material.STONE && material != Material.DIRT) {
      if (material.name().contains("LOG")) {
        return this.resolveMaterialForPlayer(Material.CRIMSON_STEM, Material.WARPED_STEM);
      } else if (material.name().contains("LEAVES")) {
        return this.resolveMaterialForPlayer(
            Material.NETHER_WART_BLOCK, Material.WARPED_WART_BLOCK);
      } else if (material == Material.SHORT_GRASS) {
        return this.resolveMaterialForPlayer(Material.CRIMSON_ROOTS, Material.WARPED_ROOTS);
      } else {
        return material == Material.TALL_GRASS
            ? this.resolveMaterialForPlayer(Material.TWISTING_VINES, Material.WEEPING_VINES)
            : null;
      }
    } else {
      return this.resolveMaterialForPlayer(
          Material.NETHERRACK, Material.MAGMA_BLOCK, Material.BASALT);
    }
  }

  void applyAbilityEffects(Location location, int count, int index) {
    World world = location.getWorld();
    if (world != null) {
      int remaining = index * 20;
      new BukkitRunnable() {
        final int maxCount = remaining;
        final Location anchorLocation = location;
        final int durationTicks = count;
        DustOptions dustOptions = new DustOptions(Color.fromRGB(58, 58, 58), 2.5F);
        int cooldownTicks = 0;

        @Override
        public void run() {
          if (this.cooldownTicks >= this.maxCount) {
            this.cancel();
          } else {
            if (this.cooldownTicks % 8 == 0) {
              this.anchorLocation.getWorld().playSound(this.anchorLocation, Sound.BLOCK_FIRE_AMBIENT, 1.0F, 1.0F);
            }

            ParticleEffects.updateState(
                Particle.FLAME,
                this.anchorLocation.clone().add(0.0, 1.0, 0.0),
                this.durationTicks,
                120,
                1,
                0.1,
                0.1,
                0.1,
                0.0,
                0.0F,
                0.0F,
                0.0F);
            ParticleEffects.refreshPlayerState(
                this.dustOptions, this.anchorLocation, this.durationTicks, 120, 1, 0.1, 0.1, 0.1, 0.0, 0.0F, 0.0F, 0.0F);
            this.cooldownTicks += 5;
          }
        }
      }
          .runTaskTimer(
              this.plugin,
              SharedScheduler.staggeredInitialDelay(0L),
              SharedScheduler.staggeredInitialDelay(5L));
    }
  }

  void playAbilityEffects(UUID playerId, Location location, int count) {
    BukkitTask scheduledTask =
        new BukkitRunnable() {
          final Location anchorLocation = location;
          final int durationTicks = count;

          @Override
          public void run() {
            if (!FireTier1Gem.this.playerLocations.containsKey(playerId)) {
              this.cancel();
            } else {
              World world = this.anchorLocation.getWorld();
              int count = this.anchorLocation.getBlockX();
              int index = this.anchorLocation.getBlockY();
              int remaining = this.anchorLocation.getBlockZ();

              for (int step = -this.durationTicks; step <= this.durationTicks; step++) {
                for (int ticks = -this.durationTicks; ticks <= this.durationTicks; ticks++) {
                  for (int durationTicks = -this.durationTicks; durationTicks <= this.durationTicks; durationTicks++) {
                    int attempts = step * step + ticks * ticks + durationTicks * durationTicks;
                    if (attempts <= this.durationTicks * this.durationTicks) {
                      Location location = new Location(world, count + step, index + ticks, remaining + durationTicks);
                      Block block = location.getBlock();
                      if (block.getType() == Material.WATER || block.getType() == Material.LAVA) {
                        block.setType(Material.AIR);
                        location.getWorld().playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 0.2F, 1.0F);
                      }
                    }
                  }
                }
              }
            }
          }
        }
            .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(10L), 10L);
    this.cooldownTasks.put(playerId, scheduledTask);
  }

  Material resolveMaterialForPlayer(Material... material) {
    return material[java.util.concurrent.ThreadLocalRandom.current().nextInt(material.length)];
  }

  void spawnAbilityEffects(UUID playerId, Map<Location, BlockData> blocksByLocation, Map<Location, BlockData> currentBlocksByLocation) {
    for (Entry<Location,BlockData> entry : blocksByLocation.entrySet()) {
      Location location = entry.getKey();
      BlockData blockData = entry.getValue();
      Material material = blockData.getMaterial();
      BlockData currentBlockData = currentBlocksByLocation.get(location);
      boolean block = currentBlockData == null || location.getBlock().getType() == currentBlockData.getMaterial();
      if (material != Material.WATER && material != Material.LAVA && block) {
        location.getBlock().setBlockData(blockData.clone(), false);
      }

      this.secondaryTrackedLocations.remove(location);
    }

    this.temporaryBlockSnapshots.remove(playerId);
    this.blockSnapshots.remove(playerId);
  }

  void playAbilitySound(UUID playerId) {
    Set<Location> locations = this.targetSetsByPlayer.get(playerId);
    if (locations != null && !locations.isEmpty()) {
      Iterator<Location> iterator = locations.iterator();
      int count = 0;

      while (iterator.hasNext() && count < 20) {
        Location location = iterator.next();
        Block block = location.getBlock();
        if (block.getType() == Material.WATER || block.getType() == Material.LAVA) {
          block.setType(Material.AIR);
          location.getWorld().playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 0.2F, 1.0F);
          iterator.remove();
          count++;
        }
      }
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player attacker)) {
      return;
    }

    ItemStack mainHand = attacker.getInventory().getItemInMainHand();
    ItemStack offHand = attacker.getInventory().getItemInOffHand();
    if (!isGemItem(mainHand) && !isGemItem(offHand)) {
      return;
    }
    if (Bliss.getInstance().canUseAbility(attacker)) {
      return;
    }

    Entity target = event.getEntity();
    if (target instanceof Player playerTarget
        && Bliss.getInstance().canUseAbility(playerTarget)) {
      return;
    }
    if (java.util.concurrent.ThreadLocalRandom.current().nextInt(100) < 10) {
      target.setFireTicks(40);
    }
  }

  void cleanupAbilityState(UUID playerId) {
    Set<Location> locations = this.pendingLocationsByPlayer.get(playerId);
    Map<Location, Material> materialsByLocation = this.primaryMaterialValuesByLocation.get(playerId);
    if (locations != null && materialsByLocation != null) {
      for (Location location : locations) {
        Material material = materialsByLocation.get(location);
        if (material != null) {
          location.getBlock().setType(material);
        }
      }
    }

    this.pendingLocationsByPlayer.remove(playerId);
    this.primaryMaterialValuesByLocation.remove(playerId);
  }

  void scheduleAbilityUpdate() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            if (!FireTier1Gem.this.targetLocations.isEmpty()) {
              for (Player player : Bukkit.getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();
                Location location = player.getLocation();
                Location targetLocation = FireTier1Gem.this.targetLocations.get(playerId);
                if (targetLocation != null && location.getWorld().equals(targetLocation.getWorld())) {
                  try {
                    if (location.distance(targetLocation) <= 4.0) {
                      player.setFireTicks(60);
                      player.setHealth(Math.min(player.getHealth() + 4.0, player.getMaxHealth()));
                      player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 15, 0));
                    }
                  } catch (IllegalArgumentException ignored) {
                    // Invalid or already-removed state is non-fatal here.
                  }
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(10L), 10L);
  }

  public void setTrustCommand(TrustCommand trustCommand) {
    this.trustCommand = trustCommand;
  }

  boolean isValidTarget(UUID playerId, UUID targetId) {
    Set<UUID> linkedPlayerIds = this.playerRelations.get(playerId);
    return linkedPlayerIds != null && linkedPlayerIds.contains(targetId);
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();

    for (Entry<UUID,Location> entry : this.originLocations.entrySet()) {
      UUID playerId = entry.getKey();
      Location location = entry.getValue();
      if (location != null && player.getLocation().getWorld().equals(location.getWorld())) {
        try {
          if (player.getLocation().distance(location) <= 5.0) {
            Player targetPlayer = Bukkit.getPlayer(playerId);
            if (targetPlayer != null) {
              event.setDeathMessage(player.getName() + " was set ablaze by " + targetPlayer.getName());
            }
            break;
          }
        } catch (IllegalArgumentException ignored) {
          // Invalid or already-removed state is non-fatal here.
        }
      }
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Location location = event.getBlock().getLocation();

    for (Set<Location> locations : this.pendingLocationsByPlayer.values()) {
      if (locations.contains(location) && this.secondaryActive) {
        event.setCancelled(true);
        return;
      }
    }

    if (this.activeTrackedLocations.contains(location)) {
      if (this.active) {
        event.setCancelled(true);
      } else {
        this.activeTrackedLocations.remove(location);
      }
    }

    if (this.trackedLocations.contains(location)) {
      if (this.active) {
        event.setCancelled(true);
      } else {
        this.trackedLocations.remove(location);
      }
    }
  }

  public void activateAbility(Player player) {
    UUID playerId = player.getUniqueId();
  }

  static {
    FIRE_CRISP_ID = "fire_crisp";
    secondaryMATERIAL_CONVERSIONS = new EnumMap<>(Material.class);
    MATERIAL_CONVERSIONS = new EnumMap<>(Material.class);
    secondaryMATERIAL_CONVERSIONS.put(Material.POTATO, Material.BAKED_POTATO);
    secondaryMATERIAL_CONVERSIONS.put(Material.RAW_IRON, Material.IRON_INGOT);
    secondaryMATERIAL_CONVERSIONS.put(Material.RAW_COPPER, Material.COPPER_INGOT);
    secondaryMATERIAL_CONVERSIONS.put(Material.RAW_GOLD, Material.GOLD_INGOT);
    secondaryMATERIAL_CONVERSIONS.put(Material.MUTTON, Material.COOKED_MUTTON);
    secondaryMATERIAL_CONVERSIONS.put(Material.BEEF, Material.COOKED_BEEF);
    secondaryMATERIAL_CONVERSIONS.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
    secondaryMATERIAL_CONVERSIONS.put(Material.SAND, Material.GLASS);
    secondaryMATERIAL_CONVERSIONS.put(Material.COBBLESTONE, Material.STONE);
    secondaryMATERIAL_CONVERSIONS.put(Material.STONE, Material.SMOOTH_STONE);
    secondaryMATERIAL_CONVERSIONS.put(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE);
    secondaryMATERIAL_CONVERSIONS.put(Material.CHICKEN, Material.COOKED_CHICKEN);
    secondaryMATERIAL_CONVERSIONS.put(Material.PORKCHOP, Material.COOKED_PORKCHOP);
    secondaryMATERIAL_CONVERSIONS.put(Material.CLAY_BALL, Material.BRICK);
    secondaryMATERIAL_CONVERSIONS.put(Material.OAK_LOG, Material.CHARCOAL);
    secondaryMATERIAL_CONVERSIONS.put(Material.SPRUCE_LOG, Material.CHARCOAL);
    secondaryMATERIAL_CONVERSIONS.put(Material.ACACIA_LOG, Material.CHARCOAL);
    secondaryMATERIAL_CONVERSIONS.put(Material.DARK_OAK_LOG, Material.CHARCOAL);
    secondaryMATERIAL_CONVERSIONS.put(Material.CHERRY_LOG, Material.CHARCOAL);
    secondaryMATERIAL_CONVERSIONS.put(Material.BIRCH_LOG, Material.CHARCOAL);
    secondaryMATERIAL_CONVERSIONS.put(Material.JUNGLE_LOG, Material.CHARCOAL);
    secondaryMATERIAL_CONVERSIONS.put(Material.MANGROVE_LOG, Material.CHARCOAL);
    MATERIAL_CONVERSIONS.put(Material.SHORT_GRASS, Material.CRIMSON_ROOTS);
    MATERIAL_CONVERSIONS.put(Material.TALL_GRASS, Material.TWISTING_VINES);
    MATERIAL_CONVERSIONS.put(Material.OAK_LEAVES, Material.NETHER_WART_BLOCK);
    MATERIAL_CONVERSIONS.put(Material.SPRUCE_LEAVES, Material.NETHER_WART_BLOCK);
    MATERIAL_CONVERSIONS.put(Material.BIRCH_LEAVES, Material.NETHER_WART_BLOCK);
    MATERIAL_CONVERSIONS.put(Material.ACACIA_LEAVES, Material.NETHER_WART_BLOCK);
    MATERIAL_CONVERSIONS.put(Material.CHERRY_LEAVES, Material.NETHER_WART_BLOCK);
    MATERIAL_CONVERSIONS.put(Material.MANGROVE_LEAVES, Material.NETHER_WART_BLOCK);
    MATERIAL_CONVERSIONS.put(Material.JUNGLE_LEAVES, Material.NETHER_WART_BLOCK);
    MATERIAL_CONVERSIONS.put(Material.DARK_OAK_LEAVES, Material.NETHER_WART_BLOCK);
    MATERIAL_CONVERSIONS.put(Material.GRASS_BLOCK, Material.NETHERRACK);
    MATERIAL_CONVERSIONS.put(Material.STONE, Material.NETHERRACK);
    MATERIAL_CONVERSIONS.put(Material.DIRT, Material.NETHERRACK);
    MATERIAL_CONVERSIONS.put(Material.OAK_LOG, Material.CRIMSON_STEM);
    MATERIAL_CONVERSIONS.put(Material.SPRUCE_LOG, Material.CRIMSON_STEM);
    MATERIAL_CONVERSIONS.put(Material.ACACIA_LOG, Material.CRIMSON_STEM);
    MATERIAL_CONVERSIONS.put(Material.DARK_OAK_LOG, Material.CRIMSON_STEM);
    MATERIAL_CONVERSIONS.put(Material.CHERRY_LOG, Material.CRIMSON_STEM);
    MATERIAL_CONVERSIONS.put(Material.BIRCH_LOG, Material.CRIMSON_STEM);
    MATERIAL_CONVERSIONS.put(Material.JUNGLE_LOG, Material.CRIMSON_STEM);
    MATERIAL_CONVERSIONS.put(Material.MANGROVE_LOG, Material.CRIMSON_STEM);
  }
}
