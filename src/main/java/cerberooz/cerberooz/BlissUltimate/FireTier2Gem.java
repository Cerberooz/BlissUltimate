package cerberooz.cerberooz.BlissUltimate;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
import org.bukkit.NamespacedKey;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class FireTier2Gem implements Listener {
  static final String FIRE_CRISP_T2_ID;
  static final String FIRE_CAMPFIRE_T2_ID;
  static final String FIRE_FIREBALL_T2_ID;
  static final Set<Material> FIRE_AFFECTED_MATERIALS;
  final Bliss plugin;
  TrustCommand trustCommand;
  final Map<UUID, UUID> playerLinks = new ConcurrentHashMap<>();
  static final EnumMap<Material, Material> MATERIAL_CONVERSIONS;
  final Map<UUID, Location> playerLocations = new ConcurrentHashMap<>();
  final Map<UUID, Location> targetLocations = new ConcurrentHashMap<>();
  final Map<UUID, Map<Location, BlockData>> temporaryBlockSnapshots = new ConcurrentHashMap<>();
  final Map<UUID, Map<Location, BlockData>> blockSnapshots = new ConcurrentHashMap<>();
  final Map<UUID, Integer> abilityStages = new ConcurrentHashMap<>();
  final Map<UUID, Integer> playerCounters = new ConcurrentHashMap<>();
  final Map<UUID, ItemDisplay> itemDisplays = new ConcurrentHashMap<>();
  final Map<UUID, BossBar> abilityBossBars = new ConcurrentHashMap<>();
  final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
  final Map<UUID, BukkitTask> animationTasks = new ConcurrentHashMap<>();
  final Map<UUID, BukkitTask> cooldownTasks = new ConcurrentHashMap<>();
  final Map<String, Integer> namedCounters = new ConcurrentHashMap<>();
  final Map<UUID, BukkitTask> scheduledTasks = new ConcurrentHashMap<>();
  final Map<UUID, BukkitTask> cleanupTasks = new ConcurrentHashMap<>();
  final Map<UUID, BukkitTask> effectTasks = new ConcurrentHashMap<>();
  final Map<UUID, ArmorStand> armorStands = new ConcurrentHashMap<>();
  final Map<UUID, Player> players = new ConcurrentHashMap<>();
  final Map<UUID, Player> linkedPlayers = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Set<UUID>> playerRelations = new ConcurrentHashMap<>();
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();
  Location anchorLocation;
  double effectRadius;
  final Map<UUID, Long> cooldownTimestamps = new ConcurrentHashMap<>();
  final Set<Location> trackedLocations = new HashSet<>();
  AstraTier2Gem astraTier2Gem;
  final NamespacedKey itemKey;
  final Map<UUID, Set<BukkitRunnable>> activeRunnables = new ConcurrentHashMap<>();
  final Map<UUID, Set<BukkitTask>> activeTasks = new ConcurrentHashMap<>();

  public void setAstraTier2Gem(AstraTier2Gem astraTier2Gem) {
    this.astraTier2Gem = astraTier2Gem;
  }

  boolean isAbilityActive(UUID playerId) {
    return this.astraTier2Gem != null && this.astraTier2Gem.hasRequiredState(playerId);
  }

  long getAbilityLong(UUID playerId) {
    return this.astraTier2Gem == null ? 0L : this.astraTier2Gem.getAbilityLong(playerId);
  }

  public FireTier2Gem(Bliss bliss) {
    this.plugin = bliss;
    this.startBackgroundTasks();
    this.startPrimaryBackgroundTasks();
    this.itemKey = new NamespacedKey(bliss, "fireball_power");
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          final Bliss plugin = bliss;

          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if (this.plugin.isGemsDisabled()) {
                if (this.plugin.canUseAbility(player)) {
                  return;
                }

                if (FireTier2Gem.isGemItem(mainHandItem) || FireTier2Gem.isGemItem(offHandItem)) {
                  String darkGray = ChatColor.DARK_GRAY.toString();
                  String bold = ChatColor.BOLD.toString();
                  ActionBarQueue.enqueue(
                      player,
                      "🔒 " + darkGray + bold + "ᴅɪꜱᴀʙʟᴇᴅ");
                }
              }
            }
          }
        }, bliss, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  public void setTrustCommand(TrustCommand trustCommand) {
    this.trustCommand = trustCommand;
  }

  public ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(2);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
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
      lore.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      lore.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            if (!FireTier2Gem.this.itemDisplays.isEmpty()) {
              for (Entry<UUID,org.bukkit.entity.ItemDisplay> entry : FireTier2Gem.this.itemDisplays.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null
                    && player.isOnline()
                    && FireTier2Gem.this.playerCounters.containsKey(entry.getKey())
                    && FireTier2Gem.this.playerCounters.getOrDefault(entry.getKey(), 0) > 0) {
                  FireTier2Gem.this.teleportAbilityTarget(player);
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              if (!FireTier2Gem.this.plugin.isGemsDisabled()
                 
                  && !FireTier2Gem.this.plugin.canUseAbility(player)) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                boolean enabled = FireTier2Gem.isGemItem(mainHandItem) || FireTier2Gem.isGemItem(offHandItem);
                if (enabled) {
                  if ((FireTier2Gem.isGemItem(mainHandItem) || FireTier2Gem.isGemItem(offHandItem))
                      && FireTier2Gem.this.isAbilityActive(player.getUniqueId())) {
                    long lastUpdateTime = FireTier2Gem.this.getAbilityLong(player.getUniqueId());
                    long timestamp = lastUpdateTime / 1000L;
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
                    FireTier2Gem.this.activateAbility(player);
                    FireTier2Gem.this.applyAbilityEffects(player);
                  }
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (isGemItem(event.getItemDrop().getItemStack())) {
      event.setCancelled(true);
    }
  }

  void teleportAbilityTarget(Player player) {
    UUID playerId = player.getUniqueId();
    ItemDisplay itemDisplay = this.itemDisplays.get(playerId);
    if (itemDisplay != null && !itemDisplay.isDead()) {
      if (itemDisplay.getWorld().equals(player.getWorld())) {
        Location location = player.getEyeLocation();
        Location targetLocation = location.clone().add(0.0, 2.2, 0.0);
        targetLocation.setYaw(location.getYaw());
        targetLocation.setPitch(location.getPitch());
        itemDisplay.teleport(targetLocation);
        itemDisplay.setTransformation(
            new Transformation(
                new Vector3f(0.0F, 0.0F, 0.0F),
                new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F),
                new Vector3f(1.2F, 1.2F, 1.2F),
                new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F)));
      }
    }
  }

  String formatDisplayText(UUID playerId, String text) {
    return CooldownService.isOnCooldown(playerId, text)
        ? AbilityStatusFormatter.formatDisplayText(playerId, text, false)
        : ChatColor.GREEN + "Ready!";
  }

  void activateAbility(Player player) {
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        if (!CooldownService.isOnCooldown(player.getUniqueId(), "fire_auto_enchant_t2")) {
          int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.autoEnchantCooldown", 5);
          if (player.getInventory().contains(Material.DRAGON_EGG)) {
            configuredValue /= 2;
          }

          CooldownService.setCooldown(player.getUniqueId(), "fire_auto_enchant_t2", configuredValue);

          for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
              if (item.getType().name().contains("SWORD")
                  && !item.containsEnchantment(Enchantment.FIRE_ASPECT)) {
                item.addEnchantment(Enchantment.FIRE_ASPECT, 2);
              } else if (item.getType() == Material.BOW
                  && !item.containsEnchantment(Enchantment.FLAME)) {
                item.addEnchantment(Enchantment.FLAME, 1);
              }
            }
          }
        }
      }
    }
  }

  void applyAbilityEffects(Player player) {
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        player.addPotionEffect(
            new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, true, true, true));
        String primaryStatus = this.formatDisplayText(player.getUniqueId(), "fire_fireball_t2");
        String message = this.formatDisplayText(player.getUniqueId(), "fire_crisp_t2");
        String displayText = this.formatDisplayText(player.getUniqueId(), "fire_campfire_t2");
        String textColor = ChatColor.WHITE.toString();
        String labelColor = ChatColor.AQUA.toString();
        String gold = ChatColor.GOLD.toString();
        String nameColor = ChatColor.AQUA.toString();
        String configColor = ChatColor.WHITE.toString();
        String keyColor = ChatColor.AQUA.toString();
        String title =
            textColor
                + "🧨 "
                + labelColor
                + primaryStatus
                + "  "
                + gold
                + "🔮 "
                + nameColor
                + message
                + "  "
                + configColor
                + "🥾 "
                + keyColor
                + displayText;
        ActionBarQueue.enqueue(player, title);
      }
    }
  }

  public static boolean isGemItem(ItemStack item) {
    if (item == null || item.getType() != Material.PRISMARINE_SHARD) {
      return false;
    } else if (!item.hasItemMeta()) {
      return false;
    } else {
      ItemMeta itemMeta = item.getItemMeta();
      if (itemMeta != null && itemMeta.hasCustomModelData()) {
        int count = itemMeta.getCustomModelData();
        return count == 2 || count == 22 || count == 42 || count == 62 || count == 82;
      } else {
        return false;
      }
    }
  }

  @EventHandler
  public void refreshAbilityState(PlayerDropItemEvent event) {
    if (this.isLocationBlocked(event.getItemDrop().getLocation())) {
      ItemStack item = event.getItemDrop().getItemStack();
      Material material = MATERIAL_CONVERSIONS.get(item.getType());
      if (material != null) {
        ItemStack heldItem = new ItemStack(material, item.getAmount());
        event.getItemDrop().setItemStack(heldItem);
        event.getItemDrop()
            .getWorld()
            .playSound(event.getItemDrop().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5F, 1.0F);
      }
    }
  }

  boolean isLocationBlocked(Location location) {
    int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.crispRadius", 5);

    for (Location targetLocation : this.playerLocations.values()) {
      if (targetLocation != null && location.getWorld().equals(targetLocation.getWorld()) && location.distance(targetLocation) <= configuredValue) {
        return true;
      }
    }

    return false;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        if (!this.isAbilityActive(player.getUniqueId())) {
          ItemStack mainHandItem = player.getInventory().getItemInMainHand();
          ItemStack offHandItem = player.getInventory().getItemInOffHand();
          Action action = event.getAction();
          if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK
              || !player.isSneaking()
              || !isGemItem(offHandItem)
              || mainHandItem.getType() != Material.AIR
                  && !mainHandItem.getType().name().contains("SWORD")
                  && !mainHandItem.getType().name().contains("AXE")) {
            if (isGemItem(mainHandItem)) {
              if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
                if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                  this.finishAbilityAction(player);
                }
              } else if (event.getClickedBlock() != null) {
                this.scheduleAbilityUpdate(player, event.getClickedBlock().getLocation().add(0.0, 1.0, 0.0));
              } else {
                Location location = player.getLocation().add(0.0, 2.0, 0.0);
                if (location.getBlock().getType() == Material.AIR) {
                  this.scheduleAbilityUpdate(player, location);
                }
              }
            }
          } else {
            this.sendAbilityFeedback(player);
            event.setCancelled(true);
          }
        }
      }
    }
  }

  boolean isAbilityAllowed(ItemStack item) {
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

  void sendAbilityFeedback(Player player) {
    UUID playerId = player.getUniqueId();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        if (CooldownService.isOnCooldown(playerId, "fire_crisp_t2")) {
          String gold = ChatColor.GOLD.toString();
          String red = ChatColor.RED.toString();
          String messageColor = ChatColor.GOLD.toString();
          String displayColor = ChatColor.RED.toString();
          String textColor = ChatColor.GOLD.toString();
          String text = this.formatDisplayText(playerId, "fire_crisp_t2");
          player.sendMessage(
              gold
                  + "🔮 "
                  + red
                  + "Your "
                  + messageColor
                  + "Crisp "
                  + displayColor
                  + "skill is on cooldown for "
                  + textColor
                  + text);
        } else {
          this.spawnAbilityParticles(player);
        }
      }
    }
  }

  void spawnAbilityParticles(Player player) {
    UUID playerId = player.getUniqueId();
    Location location = player.getLocation();
    World world = location.getWorld();
    if (world != null) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          player.sendMessage(
              ChatColor.GOLD
                  + "🔮 "
                  + ChatColor.RED
                  + "You have activated "
                  + ChatColor.GOLD
                  + "Crisp");
          this.playerLocations.put(playerId, location.clone());
          player.addPotionEffect(
              new PotionEffect(PotionEffectType.REGENERATION, 200, 0, false, false, false));
          long timestamp = ConfigValueCache.getInt(this.plugin, "fireT2.crispCooldown", 42);
          if (player.getInventory().contains(Material.DRAGON_EGG)) {
            timestamp /= 2L;
          }

          int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.crispDuration", 10);
          int index = ConfigValueCache.getInt(this.plugin, "fireT2.crispRadius", 5);
          ActiveAbilityStore.startActive(playerId, "fire_crisp_t2", configuredValue, timestamp);
          BukkitTask task = this.scheduledTasks.remove(playerId);
          if (task != null) {
            task.cancel();
          }

          BukkitTask scheduledTask = this.cleanupTasks.remove(playerId);
          if (scheduledTask != null) {
            scheduledTask.cancel();
          }

          Map<Location, BlockData> blocksByLocation = this.temporaryBlockSnapshots.remove(playerId);
          if (blocksByLocation != null) {
            this.cleanupAbilityState(playerId, blocksByLocation, this.blockSnapshots.remove(playerId));
          }

          new BukkitRunnable() {
            final int cooldownTicks = configuredValue;
            final Location anchorLocation = location;
            final UUID capturedPlayerId = playerId;
            final int maxCount = configuredValue;
            int durationTicks = 1;
            final Map<Location, BlockData> secondaryBlockDataByLocation = new HashMap<>();
            final Map<Location, BlockData> blockDataByLocation = new HashMap<>();

            @Override
            public void run() {
              if (this.durationTicks <= this.cooldownTicks) {
                FireTier2Gem.this.updateAbilityState(this.anchorLocation, this.durationTicks, this.secondaryBlockDataByLocation, this.blockDataByLocation);
                this.durationTicks++;
              } else {
                FireTier2Gem.this.temporaryBlockSnapshots.put(this.capturedPlayerId, this.secondaryBlockDataByLocation);
                FireTier2Gem.this.blockSnapshots.put(this.capturedPlayerId, this.blockDataByLocation);
                FireTier2Gem.this.playAbilityEffects(this.capturedPlayerId, this.anchorLocation, this.cooldownTicks);
                BukkitTask scheduledTask = FireTier2Gem.this.resolveBukkitTask(this.anchorLocation, this.cooldownTicks);
                if (scheduledTask != null) {
                  FireTier2Gem.this.cleanupTasks.put(this.capturedPlayerId, scheduledTask);
                }

                new BukkitRunnable() {
                  @Override
                  public void run() {
                    BukkitTask task = FireTier2Gem.this.scheduledTasks.remove(playerId);
                    if (task != null) {
                      task.cancel();
                    }

                    BukkitTask scheduledTask = FireTier2Gem.this.cleanupTasks.remove(playerId);
                    if (scheduledTask != null) {
                      scheduledTask.cancel();
                    }

                    FireTier2Gem.this.cleanupAbilityState(playerId, secondaryBlockDataByLocation, blockDataByLocation);
                    FireTier2Gem.this.playerLocations.remove(playerId);
                    FireTier2Gem.this.temporaryBlockSnapshots.remove(playerId);
                    FireTier2Gem.this.blockSnapshots.remove(playerId);
                  }
                }.runTaskLater(FireTier2Gem.this.plugin, this.maxCount * 20L);
                this.cancel();
              }
            }
          }
              .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(5L), 1L);
          DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 119, 0), 1.0F);
          ParticleEffects.spawnPrimaryAbilityParticles(location, Color.WHITE);
          ParticleEffects.createSimpleDustTrailTask(dustOptions, location, index, 5);
          world.spawnParticle(Particle.FLAME, location, 100, 0.7, 0.7, 0.7);
          world.spawnParticle(Particle.SMOKE, location, 100, 0.7, 0.7, 0.7);
          world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0F, 1.0F);
          world.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
        }
      }
    }
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
      this.trackedLocations.add(targetLocation);
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

  BukkitTask resolveBukkitTask(Location location, int count) {
    World world = location.getWorld();
    return world == null
        ? null
        : new BukkitRunnable() {
          final Location anchorLocation = location;
          final int maxCount = count;
          final DustOptions dustOptions = new DustOptions(Color.fromRGB(58, 58, 58), 2.0F);
          int durationTicks = 0;

          @Override
          public void run() {
            if (this.durationTicks % 8 == 0) {
              world.playSound(this.anchorLocation, Sound.BLOCK_FIRE_AMBIENT, 1.0F, 1.0F);
            }

            ParticleEffects.updateState(
                Particle.FLAME,
                this.anchorLocation.clone().add(0.0, 1.0, 0.0),
                this.maxCount,
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
                this.dustOptions, this.anchorLocation, this.maxCount, 120, 1, 0.1, 0.1, 0.1, 0.0, 0.0F, 0.0F, 0.0F);
            this.durationTicks += 5;
          }
        }
            .runTaskTimer(
                this.plugin,
                SharedScheduler.staggeredInitialDelay(0L),
                SharedScheduler.staggeredInitialDelay(5L));
  }

  void playAbilityEffects(UUID playerId, Location location, int count) {
    BukkitTask scheduledTask =
        new BukkitRunnable() {
          final Location anchorLocation = location;
          final int durationTicks = count;

          @Override
          public void run() {
            if (!FireTier2Gem.this.playerLocations.containsKey(playerId)) {
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
                        world.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 0.2F, 1.2F);
                      }
                    }
                  }
                }
              }
            }
          }
        }
            .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(10L), 10L);
    this.scheduledTasks.put(playerId, scheduledTask);
  }

  Material resolveMaterialForPlayer(Material... material) {
    return material[java.util.concurrent.ThreadLocalRandom.current().nextInt(material.length)];
  }

  void spawnAbilityEffects(UUID playerId, Map<Location, BlockData> blocksByLocation) {
    this.cleanupAbilityState(playerId, blocksByLocation, this.blockSnapshots.remove(playerId));
  }

  void cleanupAbilityState(UUID playerId, Map<Location, BlockData> blocksByLocation, Map<Location, BlockData> currentBlocksByLocation) {
    if (currentBlocksByLocation == null) {
      currentBlocksByLocation = Collections.emptyMap();
    }

    for (Entry<Location,BlockData> entry : blocksByLocation.entrySet()) {
      Location location = entry.getKey();
      BlockData blockData = entry.getValue();
      Material material = blockData.getMaterial();
      BlockData currentBlockData = currentBlocksByLocation.get(location);
      boolean block = currentBlockData == null || location.getBlock().getType() == currentBlockData.getMaterial();
      if (material != Material.WATER && material != Material.LAVA && block) {
        location.getBlock().setBlockData(blockData.clone(), false);
      }

      this.trackedLocations.remove(location);
    }

    this.temporaryBlockSnapshots.remove(playerId);
    this.blockSnapshots.remove(playerId);
  }

  void scheduleAbilityUpdate(Player player, Location location) {
    UUID playerId = player.getUniqueId();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        if (CooldownService.isOnCooldown(playerId, "fire_campfire_t2")) {
          String gold = ChatColor.GOLD.toString();
          String red = ChatColor.RED.toString();
          String textColor = ChatColor.WHITE.toString();
          String messageColor = ChatColor.GOLD.toString();
          String displayColor = ChatColor.RED.toString();
          String labelColor = ChatColor.GOLD.toString();
          String text = this.formatDisplayText(playerId, "fire_campfire_t2");
          player.sendMessage(
              gold
                  + "🔮 "
                  + red
                  + "Your "
                  + textColor
                  + "🥾"
                  + messageColor
                  + "Cozy Campfire "
                  + displayColor
                  + "is on cooldown for "
                  + labelColor
                  + text);
        } else if (location.getBlock().getType() == Material.AIR) {
            location.getBlock().setType(Material.SOUL_CAMPFIRE);
            Location targetLocation = location.toCenterLocation();
            this.targetLocations.put(playerId, location);
            int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.campfireCooldown", 600);
            if (player.getInventory().contains(Material.DRAGON_EGG)) {
              configuredValue /= 2;
            }

            int index = ConfigValueCache.getInt(this.plugin, "fireT2.campfireDuration", 600);
            ActiveAbilityStore.startActive(playerId, "fire_campfire_t2", index, configuredValue);
            String loreColor = ChatColor.GOLD.toString();
            String accentColor = ChatColor.of("#befff7").toString();
            String prefixColor = ChatColor.GOLD.toString();
            String suffixColor = ChatColor.of("#befff7").toString();
            player.sendMessage(
                loreColor
                    + "🔮 "
                    + accentColor
                    + "You have used "
                    + prefixColor
                    + "🥾Cozy Campfire "
                    + suffixColor
                    + "skill!");
            player.setHealth(Math.min(20.0, player.getHealth() + 5.0));
            this.handleAbilityAction(targetLocation, playerId);
          }
        
      }
    }
  }

  void completeAbilityAction(UUID playerId, BukkitRunnable runnable) {
    if (runnable != null) {
      this.activeRunnables.computeIfAbsent(playerId, FireTier2Gem::createTrackingSetForTarget).add(runnable);
    }
  }

  void processAbilityState(UUID playerId, BukkitTask task) {
    if (task != null) {
      this.activeTasks.computeIfAbsent(playerId, FireTier2Gem::createTrackingSetForPlayer).add(task);
    }
  }

  boolean isAbilityBlocked(UUID playerId, Location location) {
    return this.targetLocations.containsKey(playerId)
        && location != null
        && location.getWorld() != null
        && location.getBlock().getType() == Material.SOUL_CAMPFIRE;
  }

  void handleAbilityAction(Location location, UUID playerId) {
    ArmorStand armorStand =
        (ArmorStand)
            location.getWorld().spawnEntity(location.clone().add(0.0, 1.0, 0.0), EntityType.ARMOR_STAND);
    armorStand.setVisible(false);
    armorStand.setGravity(false);
    armorStand.setCustomNameVisible(true);
    armorStand.setMarker(true);
    this.armorStands.put(playerId, armorStand);
    int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.campfireDuration", 600);
    int index = ConfigValueCache.getInt(this.plugin, "fireT2.campfireRadius", 4);
    BukkitTask scheduledTask =
        new BukkitRunnable() {
          final int maxCount = configuredValue;
          final UUID capturedPlayerId = playerId;
          final Location anchorLocation = location;
          final int cooldownTicks = index;
          int durationTicks = this.maxCount;
          boolean active = false;
          final DustOptions secondaryDustOptions = new DustOptions(Color.fromRGB(255, 119, 0), 1.2F);
          final DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 119, 0), 0.7F);

          @Override
          public void run() {
            if (!FireTier2Gem.this.isAbilityBlocked(this.capturedPlayerId, this.anchorLocation)) {
              FireTier2Gem.this.refreshPlayerState(this.capturedPlayerId, this.anchorLocation);
              this.cancel();
            } else {
              if (!this.active) {
                FireTier2Gem.this.completeAbilityAction(
                    this.capturedPlayerId,
                    ParticleEffects.createCleanupDustOrbitTask(
                    this.secondaryDustOptions,
                    this.anchorLocation,
                    this.cooldownTicks,
                    this.maxCount * 20,
                    true,
                    6,
                    40.0,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0F,
                    0.0F,
                    0.0F));
                FireTier2Gem.this.completeAbilityAction(
                    this.capturedPlayerId,
                    ParticleEffects.createDirectionalParticleOrbitTask(
                    Particle.WAX_ON,
                    this.anchorLocation,
                    this.cooldownTicks,
                    this.maxCount * 20,
                    true,
                    1,
                    100.0,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0F,
                    0.0F,
                    0.0F));
                BukkitTask scheduledTask =
                Bukkit.getScheduler()
                .runTaskLater(
                    FireTier2Gem.this.plugin, () -> this.spawnTrailSegment(this.capturedPlayerId, this.anchorLocation, this.cooldownTicks, this.maxCount), 60L);
                FireTier2Gem.this.processAbilityState(this.capturedPlayerId, scheduledTask);
                this.active = true;
              }

              if (this.durationTicks % 2 == 0) {
                FireTier2Gem.this.completeAbilityAction(
                    this.capturedPlayerId,
                    ParticleEffects.createSimpleDustTrailTask(
                    this.dustOptions, this.anchorLocation, this.cooldownTicks, 20));
                this.anchorLocation
                .getWorld()
                .spawnParticle(Particle.HAPPY_VILLAGER, this.anchorLocation, 10, this.cooldownTicks - 2, this.cooldownTicks - 2, this.cooldownTicks - 2);
              }

              if (this.durationTicks % 6 == 0) {
                this.anchorLocation.getWorld().playSound(this.anchorLocation, Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.3F);
                FireTier2Gem.this.resetAbilityState(this.anchorLocation, this.capturedPlayerId);
              }

              if (this.durationTicks <= 0) {
                Player player = Bukkit.getPlayer(this.capturedPlayerId);
                if (player != null && player.isOnline()) {
                  player.sendMessage(
                      ChatColor.GOLD + "🥾 " + ChatColor.RED + "Your Cozy Campfire has expired!");
                }

                FireTier2Gem.this.refreshPlayerState(this.capturedPlayerId, this.anchorLocation);
                this.cancel();
              } else {
                ArmorStand armorStand = FireTier2Gem.this.armorStands.get(this.capturedPlayerId);
                if (armorStand != null && !armorStand.isDead()) {
                  int count = this.durationTicks / 60;
                  int index = this.durationTicks % 60;
                  armorStand.setCustomName(ChatColor.GREEN + String.format("%d:%02d", count, index));
                }

                Player targetPlayer = Bukkit.getPlayer(this.capturedPlayerId);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                  FireTier2Gem.this.trackAbilityState(this.anchorLocation, targetPlayer, this.cooldownTicks);
                }

                this.durationTicks--;
              }
            }
          }

          private void spawnTrailSegment(UUID playerId, Location location, int count, int index) {
            if (FireTier2Gem.this.isAbilityBlocked(playerId, location)) {
              FireTier2Gem.this.completeAbilityAction(
                  playerId,
                  ParticleEffects.createCleanupDustOrbitTask(
                  this.secondaryDustOptions,
                  location,
                  count,
                  Math.max(1, index * 20 - 60),
                  true,
                  6,
                  40.0,
                  1,
                  0.0,
                  0.0,
                  0.0,
                  0.0,
                  0.0F,
                  0.0F,
                  0.0F));
            }
          }
        }
            .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
    this.effectTasks.put(playerId, scheduledTask);
  }

  public void resetAbilityState(Location location, UUID playerId) {
    byte step = 10;
    double value = 2.5;
    byte phase = 4;
    BukkitRunnable particleOrbitTask = new BukkitRunnable() {
      final Location anchorLocation = location;
      final int cooldownTicks = step;
      final double effectRadius = value;
      final int durationTicks = phase;
      int maxCount = 0;

      @Override
      public void run() {
        if (!FireTier2Gem.this.isAbilityBlocked(playerId, this.anchorLocation)) {
          this.cancel();
        } else if (this.maxCount >= this.cooldownTicks) {
          this.cancel();
        } else {
          double value = (double) this.maxCount / this.cooldownTicks;
          double distance = this.effectRadius * (1.0 - value);

          for (int count = 0; count < this.durationTicks; count++) {
            double radius = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 2.0 * Math.PI;
            double angle =
            Math.acos(java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0);
            double progress =
            distance * (0.8 + java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 0.4);
            double offset = progress * Math.sin(angle) * Math.cos(radius);
            double scale = progress * Math.sin(angle) * Math.sin(radius) * 0.5;
            double amount = progress * Math.cos(angle);
            Location location = this.anchorLocation.clone().add(offset, scale + 0.3, amount);
            if (location.getWorld() != null) {
              location.getWorld().spawnParticle(Particle.FLAME, location, 0, 0.0, 0.0, 0.0, 0.0);
            }
          }

          this.maxCount++;
        }
      }
    };
    this.completeAbilityAction(playerId, particleOrbitTask);
    particleOrbitTask.runTaskTimer(this.plugin, 0L, 1L);
  }

  @EventHandler
  public void onPluginDisable(PluginDisableEvent event) {
    if (event.getPlugin().equals(this.plugin)) {
      this.cleanup();
    }
  }

  void trackAbilityState(Location location, Player player, int count) {
    World world = location.getWorld();
    if (world != null) {
      double value = count * count;

      for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
        if (targetPlayer.getWorld().equals(world) && !(targetPlayer.getLocation().distanceSquared(location) > value)) {
          if (targetPlayer.equals(player)) {
            targetPlayer.addPotionEffect(
                new PotionEffect(PotionEffectType.REGENERATION, 40, 3, false, false, false));
          } else if (this.trustCommand != null
              && this.trustCommand.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId())) {
            targetPlayer.addPotionEffect(
                new PotionEffect(PotionEffectType.REGENERATION, 40, 3, false, false, false));
          }
        }
      }
    }
  }

  void refreshPlayerState(UUID playerId, Location location) {
    this.removeCampfireResources(playerId, location, true);
  }

  private void removeCampfireResources(UUID playerId, Location location, boolean applyCooldown) {
    Set<BukkitRunnable> activeTasks = this.activeRunnables.remove(playerId);
    if (activeTasks != null) {
      for (BukkitRunnable runnable : activeTasks) {
        if (runnable != null) {
          try {
            runnable.cancel();
          } catch (IllegalStateException ignored) {
            // The task was already cancelled or was never scheduled.
          }
        }
      }
    }

    Set<BukkitTask> scheduledTasks = this.activeTasks.remove(playerId);
    if (scheduledTasks != null) {
      for (BukkitTask task : scheduledTasks) {
        if (task != null) {
          task.cancel();
        }
      }
    }

    BukkitTask scheduledTask = this.effectTasks.remove(playerId);
    if (scheduledTask != null) {
      scheduledTask.cancel();
    }

    ArmorStand armorStand = this.armorStands.remove(playerId);
    if (armorStand != null && !armorStand.isDead()) {
      armorStand.remove();
    }

    this.targetLocations.remove(playerId);
    if (location != null
        && location.getWorld() != null
        && location.getBlock().getType() == Material.SOUL_CAMPFIRE) {
      location.getBlock().setType(Material.AIR);
    }

    if (applyCooldown) {
      int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.campfireCooldown", 600);
      Player player = Bukkit.getPlayer(playerId);
      if (player != null && player.getInventory().contains(Material.DRAGON_EGG)) {
        configuredValue /= 2;
      }

      ActiveAbilityStore.startActive(playerId, "fire_campfire_t2", 0L, configuredValue);
    }
  }

  void applyAbilityMotion(Player player) {
    UUID playerId = player.getUniqueId();
    int count = this.playerCounters.getOrDefault(playerId, 1);
    int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.fireballMaxPower", 10);
    int index = Math.max(1, Math.min(count, configuredValue));
    Fireball fireball = player.launchProjectile(Fireball.class);
    fireball.setShooter(player);
    fireball.setYield(0.0F);
    fireball.setIsIncendiary(false);
    fireball.setVelocity(fireball.getVelocity().multiply(55.0));
    fireball.getPersistentDataContainer().set(this.itemKey, PersistentDataType.INTEGER, index);
    this.players.put(fireball.getUniqueId(), player);
    this.abilityStages.put(fireball.getUniqueId(), index);
    String projectileId = fireball.getUniqueId().toString();
    double projectileSpeed = fireball.getVelocity().length();
    float projectileYield = fireball.getYield();
    this.updatePlayerState(
        player,
        "launch id="
            + projectileId
            + " rawPower="
            + count
            + " clampedPower="
            + index
            + " maxPower="
            + configuredValue
            + " projectileYield="
            + projectileYield
            + " velocity="
            + projectileSpeed);
    int durationTicks = ConfigValueCache.getInt(this.plugin, "fireT2.fireballCooldown", 75);
    if (player.getInventory().contains(Material.DRAGON_EGG)) {
      durationTicks /= 2;
    }

    ActiveAbilityStore.startActive(playerId, "fire_fireball_t2", 0L, durationTicks);
    this.updateTargetBossBar(player);
  }

  @EventHandler
  public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
    this.updateTargetBossBar(event.getPlayer());
  }

  @EventHandler
  public void onProjectileHit(ProjectileHitEvent event) {
    if (event.getEntity() instanceof Fireball fireball) {
      UUID playerId = fireball.getUniqueId();
      Integer storedCount = this.abilityStages.get(playerId);
      if (storedCount != null) {
        if (!(fireball.getShooter() instanceof Player player)) {
          this.players.remove(playerId);
          this.abilityStages.remove(playerId);
        } else if (storedCount != -1) {
          int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.fireballMaxPower", 10);
          int index = Math.max(1, Math.min(storedCount, configuredValue));
          double distance =
              ConfigValueCache.getDouble(this.plugin, "fireT2.fireballExplosionRadiusPerPower", 0.6);
          double value = Math.max(1.5, index * distance);
          double radius = ConfigValueCache.getDouble(this.plugin, "fireT2.fireballBaseDamage", 4.0);
          double angle =
              this.getConfiguredDouble(
                  "fireT2.fireballMaxDamage", "fireT2.fireballMaxFinalDamage", 18.0);
          double progress =
              this.getConfiguredDouble(
                  "fireT2.fireballDamagePerPowerPoint", "fireT2.fireballDamagePerPower", 1.6);
          double scale = Math.min(angle, radius + (index - 1) * progress);
          Location location = this.onProjectileHitForPlayer(event, fireball);
          World world = location.getWorld();
          if (world != null) {
            boolean enabled = false;
            if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
              enabled = this.isValidTarget(location);
            } else {
              enabled = true;
            }

            boolean active = this.isMatchingState(location);
            float size = this.getAbilityFloatValue(index, configuredValue);
            double amount = Math.max(value, size + 1.0);
            String projectileId = playerId.toString();
            String impactLocationText = this.formatDisplayTextForPlayer(location);
            this.updatePlayerState(
                player,
                "hit id="
                    + projectileId
                    + " loc="
                    + impactLocationText
                    + " rawPower="
                    + storedCount
                    + " clampedPower="
                    + index
                    + " explosionPower="
                    + size
                    + " explosionAllowed="
                    + active
                    + " pvpAllowed="
                    + enabled
                    + " radius="
                    + value
                    + " damageRadius="
                    + amount
                    + " damage="
                    + scale);
            if (active) {
              this.namedCounters.put(impactLocationText, -1);
              this.anchorLocation = location.clone();
              this.effectRadius = amount + 2.0;

              boolean success;
              try {
                success = world.createExplosion(location, size, true, true);
              } finally {
                this.anchorLocation = null;
                this.effectRadius = 0.0;
              }

              int ticks = this.namedCounters.remove(impactLocationText);
              String explosionLocationText = this.formatDisplayTextForPlayer(location);
              this.updatePlayerState(
                  player,
                  "createExplosion result="
                      + success
                      + " loc="
                      + explosionLocationText
                      + " power="
                      + size
                      + " eventBlocks="
                      + ticks);
              if (success && ticks <= 0) {
                int attempts = this.getAbilityIntValue(location, size);
                String fallbackLocationText = this.formatDisplayTextForPlayer(location);
                this.updatePlayerState(
                    player,
                    "fallbackBlockBreak blocks="
                        + attempts
                        + " loc="
                        + fallbackLocationText
                        + " power="
                        + size);
              }
            } else {
              this.updatePlayerState(
                  player,
                  "createExplosion skipped by other-explosion deny at "
                      + this.formatDisplayTextForPlayer(location));
            }

            int limit = 0;

            for (Entity entity : world.getNearbyEntities(location, amount, amount, amount)) {
              if (entity instanceof Player targetPlayer) {
                double offset = this.getAbilityDoubleValue(targetPlayer, location);
                String targetName = targetPlayer.getName();
                String distanceText = String.format(Locale.US, "%.2f", offset);
                String radiusText = String.format(Locale.US, "%.2f", amount);
                boolean silent = this.isAbilityActive(targetPlayer.getUniqueId());
                boolean protectedArea = targetPlayer.equals(player);
                boolean completed =
                    this.trustCommand != null
                        && this.trustCommand.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId());
                this.updatePlayerState(
                    player,
                    "damage candidate victim="
                        + targetName
                        + " distance="
                        + distanceText
                        + " radius="
                        + radiusText
                        + " pvpAllowed="
                        + enabled
                        + " disabled="
                        + silent
                        + " self="
                        + protectedArea
                        + " trusted="
                        + completed);
                if (enabled) {
                  UUID targetId = targetPlayer.getUniqueId();
                  if (!this.isAbilityActive(targetId)
                      && !targetPlayer.equals(player)
                      && (this.trustCommand == null || !this.trustCommand.isAbilityActive(player.getUniqueId(), targetId))
                      && !(offset > amount)) {
                    double stepSize = Math.max(0.5, scale);
                    double damage = targetPlayer.getHealth();
                    if (ConfigValueCache.getBoolean(
                        Bliss.getInstance(), "fireT2.useTrueDamageForFireball", true)) {
                      this.playerLinks.put(targetPlayer.getUniqueId(), player.getUniqueId());
                      targetPlayer.setNoDamageTicks(0);
                      this.playerIds.add(playerId);

                      try {
                        targetPlayer.damage(
                            stepSize,
                            DamageSource.builder(DamageType.SONIC_BOOM)
                                .withCausingEntity(player)
                                .withDirectEntity(fireball)
                                .build());
                      } finally {
                        this.playerIds.remove(playerId);
                      }

                      targetPlayer.setNoDamageTicks(0);
                    } else {
                      this.playerLinks.put(targetPlayer.getUniqueId(), player.getUniqueId());
                      targetPlayer.setNoDamageTicks(0);
                      targetPlayer.damage(stepSize, player);
                      targetPlayer.setNoDamageTicks(0);
                    }

                    limit++;
                    double knockback = targetPlayer.getHealth();
                    String appliedTargetName = targetPlayer.getName();
                    String damageAmountText = String.format(Locale.US, "%.2f", stepSize);
                    String healthBeforeText = String.format(Locale.US, "%.2f", damage);
                    String healthAfterText = String.format(Locale.US, "%.2f", knockback);
                    String healthDeltaText = String.format(Locale.US, "%.2f", damage - knockback);
                    this.updatePlayerState(
                        player,
                        "damage applied victim="
                            + appliedTargetName
                            + " amount="
                            + damageAmountText
                            + " healthBefore="
                            + healthBeforeText
                            + " healthAfter="
                            + healthAfterText
                            + " healthDelta="
                            + healthDeltaText);
                    Bukkit.getScheduler()
                        .runTaskLater(this.plugin, () -> this.clearFireballLink(targetPlayer, player), 100L);
                  }
                }
              }
            }

            this.updatePlayerState(
                player,
                "damage summary damagedPlayers="
                    + limit
                    + " damageRadius="
                    + String.format(Locale.US, "%.2f", amount));
            this.players.remove(playerId);
            this.abilityStages.remove(playerId);
            fireball.remove();
          }
        }
      }
    }
  }

  Location onProjectileHitForPlayer(ProjectileHitEvent event, Fireball fireball) {
    Block block = event.getHitBlock();
    if (block == null) {
      return fireball.getLocation();
    }

    BlockFace blockFace = event.getHitBlockFace();
    return blockFace != null
        ? block.getRelative(blockFace).getLocation().add(0.5, 0.5, 0.5)
        : block.getLocation().add(0.5, 0.5, 0.5);
  }

  boolean isMatchingState(Location location) {
    return this.isProtectedLocation(location, true);
  }

  boolean isProtectedLocation(Location location, boolean enabled) {
    if (location != null && location.getWorld() != null) {
      if (!Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
        return true;
      }

      try {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        com.sk89q.worldedit.util.Location targetLocation = BukkitAdapter.adapt(location);
        ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(targetLocation);
        if (applicableRegionSet.size() == 0) {
          if (enabled) {
            this.updatePlayerState(
                null,
                "wg other-explosion loc="
                    + this.formatDisplayTextForPlayer(location)
                    + " regions=0 allowed=true");
          }

          return true;
        } else {
          State state = applicableRegionSet.queryState(null, Flags.OTHER_EXPLOSION);
          boolean active = state != State.DENY;
          if (enabled) {
            String locationText = this.formatDisplayTextForPlayer(location);
            int regionCount = applicableRegionSet.size();
            String regionState = state.toString();
            this.updatePlayerState(
                null,
                "wg other-explosion loc="
                    + locationText
                    + " regions="
                    + regionCount
                    + " state="
                    + regionState
                    + " allowed="
                    + active);
          }

          return active;
        }
      } catch (Exception exception) {
        if (enabled) {
          String label = this.formatDisplayTextForPlayer(location);
          String name = exception.getClass().getSimpleName();
          String configPath = exception.getMessage();
          this.updatePlayerState(
              null,
              "wg other-explosion error loc="
                  + label
                  + " allowed=true error="
                  + name
                  + ": "
                  + configPath);
        }

        return true;
      }
    } else {
      return false;
    }
  }

  float getAbilityFloatValue(int count, int index) {
    if (index <= 1) {
      return 4.0F;
    }

    double value = Math.max(0.0, Math.min(1.0, (double) (count - 1) / (index - 1)));
    return (float) (1.0 + value * 3.0);
  }

  double getAbilityDoubleValue(Entity entity, Location location) {
    BoundingBox boundingBox = entity.getBoundingBox();
    double x = Math.max(boundingBox.getMinX(), Math.min(location.getX(), boundingBox.getMaxX()));
    double y = Math.max(boundingBox.getMinY(), Math.min(location.getY(), boundingBox.getMaxY()));
    double z = Math.max(boundingBox.getMinZ(), Math.min(location.getZ(), boundingBox.getMaxZ()));
    double distance = location.getX() - x;
    double radius = location.getY() - y;
    double angle = location.getZ() - z;
    return Math.sqrt(distance * distance + radius * radius + angle * angle);
  }

  double getConfiguredDouble(String text, String message, double value) {
    return ConfigValueCache.contains(this.plugin, text)
        ? ConfigValueCache.getDouble(this.plugin, text, value)
        : ConfigValueCache.getDouble(this.plugin, message, value);
  }

  int getAbilityIntValue(Location location, float scale) {
    World world = location.getWorld();
    if (world == null) {
      return 0;
    }

    int count = 0;
    int index = 0;
    int remaining = Math.max(1, (int) Math.ceil(scale));
    double value = scale + 0.75;

    for (int step = -remaining; step <= remaining; step++) {
      for (int ticks = -remaining; ticks <= remaining; ticks++) {
        for (int durationTicks = -remaining; durationTicks <= remaining; durationTicks++) {
          Location targetLocation = location.clone().add(step, ticks, durationTicks);
          double distance = targetLocation.distance(location);
          if (!(distance > value)) {
            Block block = targetLocation.getBlock();
            if (this.isConditionMet(block)) {
              if (!this.isProtectedLocation(block.getLocation(), false)) {
                index++;
              } else {
                world.spawnParticle(
                    Particle.BLOCK,
                    block.getLocation().add(0.5, 0.5, 0.5),
                    4,
                    0.25,
                    0.25,
                    0.25,
                    block.getBlockData());
                block.setType(Material.AIR);
                count++;
              }
            }
          }
        }
      }
    }

    String text = this.formatDisplayTextForPlayer(location);
    this.updatePlayerState(
        null,
        "fallbackBlockBreak detail radius="
            + remaining
            + " breakRadius="
            + value
            + " protectedSkipped="
            + index
            + " center="
            + text);
    return count;
  }

  boolean isConditionMet(Block block) {
    Material material = block.getType();
    return material != Material.AIR
            && material != Material.CAVE_AIR
            && material != Material.VOID_AIR
            && material != Material.BEDROCK
            && material != Material.BARRIER
            && material != Material.COMMAND_BLOCK
            && material != Material.CHAIN_COMMAND_BLOCK
            && material != Material.REPEATING_COMMAND_BLOCK
            && material != Material.STRUCTURE_BLOCK
            && material != Material.STRUCTURE_VOID
            && material != Material.END_PORTAL
            && material != Material.END_PORTAL_FRAME
            && material != Material.NETHER_PORTAL
        ? material.getBlastResistance() < 1200.0F
        : false;
  }

  void updatePlayerState(Player player, String text) {
    if (!Bukkit.getPluginManager().isPluginEnabled(Bliss.getInstance())) {
      String message = "[FireGemT2 Fireball] " + text;
      this.plugin.getLogger().info(message);
      if (player != null && player.isOnline()) {
        player.sendMessage(ChatColor.GRAY + message);
      }
    }
  }

  String formatDisplayTextForPlayer(Location location) {
    return location != null && location.getWorld() != null
        ? location.getWorld().getName()
            + " "
            + location.getBlockX()
            + ","
            + location.getBlockY()
            + ","
            + location.getBlockZ()
        : "null";
  }

  String formatDisplayTextForTarget(Location location) {
    return location != null && location.getWorld() != null
        ? location.getWorld().getUID()
            + ":"
            + location.getBlockX()
            + ":"
            + location.getBlockY()
            + ":"
            + location.getBlockZ()
        : "null";
  }

  boolean isValidTarget(Location location) {
    if (location != null && location.getWorld() != null) {
      try {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        State state =
            regionQuery.queryState(
                BukkitAdapter.adapt(location), null, Flags.OTHER_EXPLOSION);
        return state != State.DENY;
      } catch (Exception exception) {
        return true;
      }
    } else {
      return false;
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityDamage(EntityDamageEvent event) {
    Location location = this.anchorLocation;
    if (location != null && location.getWorld() != null) {
      if (event.getEntity() instanceof Player player) {
        DamageCause damageCause = event.getCause();
        if (damageCause == DamageCause.BLOCK_EXPLOSION || damageCause == DamageCause.ENTITY_EXPLOSION) {
          Location targetLocation = player.getLocation();
          if (location.getWorld().equals(targetLocation.getWorld())) {
            if (!(targetLocation.distance(location) > this.effectRadius)) {
              event.setCancelled(true);
              String playerName = player.getName();
              String damageCauseName = damageCause.toString();
              double rawDamage = event.getDamage();
              double finalDamage = event.getFinalDamage();
              String centerLocationText = this.formatDisplayTextForPlayer(location);
              this.updatePlayerState(
                  null,
                  "cancelled vanilla createExplosion player damage victim="
                      + playerName
                      + " cause="
                      + damageCauseName
                      + " damage="
                      + rawDamage
                      + " finalDamage="
                      + finalDamage
                      + " center="
                      + centerLocationText);
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Fireball fireball) {
      UUID playerId = fireball.getUniqueId();
      if (this.abilityStages.containsKey(playerId)) {
        Integer storedCount = this.abilityStages.get(playerId);
        if (storedCount != null) {
          if (storedCount != -1) {
            if (this.playerIds.contains(playerId)) {
              String projectileId = playerId.toString();
              String damageCauseName = event.getCause().toString();
              double finalDamage = event.getFinalDamage();
              double rawDamage = event.getDamage();
              this.updatePlayerState(
                  null,
                  "allowing custom tracked fireball damage id="
                      + projectileId
                      + " cause="
                      + damageCauseName
                      + " damage="
                      + rawDamage
                      + " finalDamage="
                      + finalDamage);
            } else if (event.getCause() == DamageCause.SONIC_BOOM) {
              String projectileId = playerId.toString();
              double finalDamage = event.getFinalDamage();
              double rawDamage = event.getDamage();
              this.updatePlayerState(
                  null,
                  "allowing custom sonic boom damage from tracked fireball id="
                      + projectileId
                      + " damage="
                      + rawDamage
                      + " finalDamage="
                      + finalDamage);
            } else {
              String projectileId = playerId.toString();
              String damageCauseName = event.getCause().toString();
              double rawDamage = event.getDamage();
              this.updatePlayerState(
                  null,
                  "cancelled vanilla tracked fireball damage id="
                      + projectileId
                      + " cause="
                      + damageCauseName
                      + " damage="
                      + rawDamage);
              event.setCancelled(true);
            }
          }
        }
      }
    }
  }

  void finishAbilityAction(Player player) {
    UUID playerId = player.getUniqueId();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        if (!this.isAbilityActive(playerId)) {
          if (!CooldownService.isOnCooldown(playerId, "fire_fireball_t2")) {
            if (this.playerCounters.containsKey(playerId) && this.playerCounters.getOrDefault(playerId, 0) > 0) {
              this.applyAbilityMotion(player);
            } else {
              this.sendPrimaryAbilityFeedback(player);
            }
          }
        }
      }
    }
  }

  void sendPrimaryAbilityFeedback(Player player) {
    UUID playerId = player.getUniqueId();
    this.updateTargetBossBar(player);
    this.playerCounters.put(playerId, 1);
    Location location = player.getEyeLocation().clone().add(0.0, 2.2, 0.0);
    location.setYaw(player.getLocation().getYaw());
    location.setPitch(player.getLocation().getPitch());
    ItemDisplay display = (ItemDisplay) player.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
    display.setItemStack(new ItemStack(Material.FIRE_CHARGE));
    display.setGravity(false);
    display.setPersistent(false);
    display.setInterpolationDuration(1);
    display.setInterpolationDelay(0);
    display.setTeleportDuration(1);
    display.setBillboard(Billboard.FIXED);
    display.setBrightness(new Brightness(15, 15));
    player.addPassenger(display);
    display.setTransformation(
        new Transformation(
            new Vector3f(0.0F, 0.0F, 0.0F),
            new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F),
            new Vector3f(1.2F, 1.2F, 1.2F),
            new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F)));
    this.itemDisplays.put(playerId, display);
    String gold = ChatColor.GOLD.toString();
    String accentColor = ChatColor.of("#befff7").toString();
    String textColor = ChatColor.WHITE.toString();
    String messageColor = ChatColor.GOLD.toString();
    player.sendMessage(
        gold
            + "🔮 "
            + accentColor
            + "Charging "
            + textColor
            + "🧨"
            + messageColor
            + "Fireball");
    this.updateBossBar(player);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    this.updateTargetBossBar(event.getPlayer());
  }

  Material resolveMaterialForTarget(Player player) {
    Location location = player.getLocation();
    Block block = location.getBlock();
    if (this.isProtectedTarget(block.getType())) {
      return block.getType();
    }

    Block targetBlock = location.clone().subtract(0.0, 0.1, 0.0).getBlock();
    if (this.isProtectedTarget(targetBlock.getType())) {
      return targetBlock.getType();
    }

    Block sourceBlock = location.getBlock().getRelative(BlockFace.DOWN);
    return this.isProtectedTarget(sourceBlock.getType()) ? sourceBlock.getType() : null;
  }

  boolean isProtectedTarget(Material material) {
    return FIRE_AFFECTED_MATERIALS.contains(material);
  }

  double getEffectRadius(Material material) {
    if (material == null) {
      return 1.0;
    }

    double configuredValue =
        ConfigValueCache.getDouble(this.plugin, "fireT2.fireballFireBlockChargeMultiplier", 0.5);
    return Math.max(0.1, Math.min(1.0, configuredValue));
  }

  void updateBossBar(Player player) {
    UUID playerId = player.getUniqueId();
    int configuredChargeSeconds =
        Math.max(1, ConfigValueCache.getInt(this.plugin, "fireT2.fireballChargeTime", 90));
    int maxPower = Math.max(1, ConfigValueCache.getInt(this.plugin, "fireT2.fireballMaxPower", 10));
    int secondsPerPower =
        Math.max(1, ConfigValueCache.getInt(this.plugin, "fireT2.fireballSecondsPerPower", 10));
    int progressionChargeSeconds = Math.max(1, (maxPower - 1) * secondsPerPower);
    int maxChargeSeconds = Math.max(configuredChargeSeconds, progressionChargeSeconds);
    double requiredChargeSteps = maxChargeSeconds * 10.0;
    int[] lastReportedPower = {this.playerCounters.getOrDefault(playerId, 1)};

    BukkitTask previousChargeTask = this.animationTasks.remove(playerId);
    if (previousChargeTask != null) {
      previousChargeTask.cancel();
    }

    BossBar previousBar = this.abilityBossBars.remove(playerId);
    if (previousBar != null) {
      previousBar.removeAll();
    }

    BossBar powerBar = Bukkit.createBossBar("Fireball Power", BarColor.RED, BarStyle.SOLID);
    powerBar.addPlayer(player);
    powerBar.setProgress(0.0);
    this.abilityBossBars.put(playerId, powerBar);

    BukkitTask chargeTask =
        new BukkitRunnable() {
          int elapsedRuns;
          double accumulatedCharge;

          @Override
          public void run() {
            if (!player.isOnline() || player.isDead()) {
              FireTier2Gem.this.updateTargetBossBar(player);
              this.cancel();
              return;
            }

            Integer currentPower = FireTier2Gem.this.playerCounters.get(playerId);
            if (currentPower == null || currentPower == 0) {
              this.cancel();
              return;
            }

            Material boostMaterial = FireTier2Gem.this.resolveMaterialForTarget(player);
            double chargeTimeMultiplier = FireTier2Gem.this.getEffectRadius(boostMaterial);
            this.accumulatedCharge += 1.0 / chargeTimeMultiplier;
            int elapsedSeconds = this.elapsedRuns / 10;

            if (this.accumulatedCharge >= requiredChargeSteps) {
              BossBar activeBar = FireTier2Gem.this.abilityBossBars.get(playerId);
              if (activeBar != null) {
                activeBar.setProgress(1.0);
                activeBar.setTitle("Fireball Power");
              }

              FireTier2Gem.this.playerCounters.put(playerId, maxPower);
              if (lastReportedPower[0] != maxPower) {
                String chargeSeconds = String.format(Locale.US, "%.1f", this.accumulatedCharge / 10.0);
                String boostBlock = boostMaterial == null ? "none" : boostMaterial.toString();
                String multiplier = String.format(Locale.US, "%.2f", chargeTimeMultiplier);
                FireTier2Gem.this.updatePlayerState(
                    player,
                    "charge power="
                        + maxPower
                        + "/"
                        + maxPower
                        + " elapsedSeconds="
                        + elapsedSeconds
                        + " chargeSeconds="
                        + chargeSeconds
                        + " maxChargeSeconds="
                        + maxChargeSeconds
                        + " boostBlock="
                        + boostBlock
                        + " chargeTimeMultiplier="
                        + multiplier
                        + " fullCharge=true");
                lastReportedPower[0] = maxPower;
              }

              FireTier2Gem.this.updatePrimaryBossBar(player);
              this.cancel();
              return;
            }

            double progress = this.accumulatedCharge / requiredChargeSteps;
            BossBar activeBar = FireTier2Gem.this.abilityBossBars.get(playerId);
            if (activeBar != null) {
              activeBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
            }

            int power =
                Math.max(1, Math.min(maxPower, 1 + (int) Math.floor(progress * (maxPower - 1))));
            FireTier2Gem.this.playerCounters.put(playerId, power);
            if (activeBar != null) {
              activeBar.setTitle("Fireball Power");
            }

            if (power != lastReportedPower[0]) {
              String chargeSeconds = String.format(Locale.US, "%.1f", this.accumulatedCharge / 10.0);
              String boostBlock = boostMaterial == null ? "none" : boostMaterial.toString();
              String multiplier = String.format(Locale.US, "%.2f", chargeTimeMultiplier);
              FireTier2Gem.this.updatePlayerState(
                  player,
                  "charge power="
                      + power
                      + "/"
                      + maxPower
                      + " elapsedSeconds="
                      + elapsedSeconds
                      + " chargeSeconds="
                      + chargeSeconds
                      + " maxChargeSeconds="
                      + maxChargeSeconds
                      + " secondsPerPower="
                      + secondsPerPower
                      + " boostBlock="
                      + boostBlock
                      + " chargeTimeMultiplier="
                      + multiplier);
              lastReportedPower[0] = power;
            }

            Location particleOrigin = player.getLocation().add(0.0, 1.0, 0.0);
            for (int particleIndex = 0; particleIndex < 2; particleIndex++) {
              double xOffset =
                  (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 0.8;
              double yOffset =
                  (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 0.8;
              double zOffset =
                  (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 0.8;
              player
                  .getWorld()
                  .spawnParticle(
                      Particle.DUST,
                      particleOrigin.clone().add(xOffset, yOffset, zOffset),
                      1,
                      0.0,
                      0.0,
                      0.0,
                      0.0,
                      new DustOptions(Color.fromRGB(255, 119, 0), 1.0F));
            }

            this.elapsedRuns++;
          }
        }.runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(2L), 2L);
    this.animationTasks.put(playerId, chargeTask);
  }

  void updatePrimaryBossBar(Player player) {
    UUID playerId = player.getUniqueId();
    BukkitTask task = this.animationTasks.remove(playerId);
    if (task != null) {
      task.cancel();
    }

    BukkitTask scheduledTask = this.cooldownTasks.remove(playerId);
    if (scheduledTask != null) {
      scheduledTask.cancel();
    }

    BossBar bossBar = this.abilityBossBars.remove(playerId);
    if (bossBar != null) {
      bossBar.removeAll();
    }

    BossBar abilityBossBar = this.bossBars.remove(playerId);
    if (abilityBossBar != null) {
      abilityBossBar.removeAll();
    }

    BossBar cooldownBossBar =
        Bukkit.createBossBar("Fireball Timer", BarColor.RED, BarStyle.SOLID);
    cooldownBossBar.addPlayer(player);
    cooldownBossBar.setProgress(1.0);
    this.bossBars.put(playerId, cooldownBossBar);
    int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.fireballCountdown", 10) * 20;
    BukkitTask cooldownTask =
        new BukkitRunnable() {
          final int maxCount = configuredValue;
          final UUID capturedPlayerId = playerId;
          final Player capturedPlayer = player;
          final BossBar bossBar = cooldownBossBar;
          int durationTicks = this.maxCount;
          static final String FIREBALL_TIMED_OUT_ID = "Fireball timed out!";
          @Override
          public void run() {
            if (!FireTier2Gem.this.playerCounters.containsKey(this.capturedPlayerId)) {
              this.cancel();
            } else if (this.durationTicks <= 0) {
              FireTier2Gem.this.updateTargetBossBar(this.capturedPlayer);
              this.capturedPlayer.sendMessage(ChatColor.RED + FIREBALL_TIMED_OUT_ID);
              this.cancel();
            } else {
              this.bossBar.setProgress((double) this.durationTicks / this.maxCount);
              this.durationTicks -= 5;
            }
          }


        }
            .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(5L), 5L);
    this.cooldownTasks.put(playerId, cooldownTask);
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    this.updateTargetBossBar(event.getPlayer());
  }

  void updateTargetBossBar(Player player) {
    UUID playerId = player.getUniqueId();
    ItemDisplay itemDisplay = this.itemDisplays.remove(playerId);
    if (itemDisplay != null && !itemDisplay.isDead()) {
      itemDisplay.remove();
    }

    BossBar bossBar = this.abilityBossBars.remove(playerId);
    if (bossBar != null) {
      bossBar.removeAll();
    }

    BossBar abilityBossBar = this.bossBars.remove(playerId);
    if (abilityBossBar != null) {
      abilityBossBar.removeAll();
    }

    BukkitTask task = this.animationTasks.remove(playerId);
    if (task != null) {
      task.cancel();
    }

    BukkitTask scheduledTask = this.cooldownTasks.remove(playerId);
    if (scheduledTask != null) {
      scheduledTask.cancel();
    }

    this.playerCounters.remove(playerId);
  }

  @EventHandler
  public void onPrimaryProjectileHit(ProjectileHitEvent event) {
    if (event.getEntity() instanceof Fireball fireball) {
      UUID playerId = fireball.getUniqueId();
      if (this.players.containsKey(playerId)) {
        Player player = this.players.get(playerId);
        this.lastUseTimes.put(player.getUniqueId(), System.currentTimeMillis());
      }
    }
  }

  @EventHandler
  public void onPrimaryEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (event.getEntity() instanceof Player targetPlayer) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          if (isGemItem(player.getInventory().getItemInMainHand())) {
            if (this.playerCounters.containsKey(playerId)) {
              return;
            }

            if (CooldownService.isOnCooldown(player.getUniqueId(), "fire_fireball_t2")) {
              if (!ActiveAbilityStore.isActive(player.getUniqueId(), "fire_fireball_t2")) {
                String accentColor = ChatColor.of("#FE8120").toString();
                String messageColor = ChatColor.of("#FF686F").toString();
                String textColor = ChatColor.WHITE.toString();
                String displayColor = ChatColor.of("#FE8120").toString();
                String red = ChatColor.RED.toString();
                String labelColor = ChatColor.YELLOW.toString();
                String name =
                    this.formatDisplayText(player.getUniqueId(), "fire_fireball_t2");
                player.sendMessage(
                    accentColor
                        + "🔮 "
                        + messageColor
                        + "Your "
                        + textColor
                        + "🧨"
                        + displayColor
                        + "Meteor Shower "
                        + red
                        + "is on cooldown for "
                        + labelColor
                        + name);
              }

              return;
            }

            String loreColor = ChatColor.of("#FE8120").toString();
            String prefixColor = ChatColor.of("#B8FFFB").toString();
            String suffixColor = ChatColor.of("#FE8120").toString();
            String normalizedColor = ChatColor.of("#B8FFFB").toString();
            String rawText = targetPlayer.getName();
            player.sendMessage(
                loreColor
                    + "🔮 "
                    + prefixColor
                    + "You have summoned a "
                    + suffixColor
                    + "Meteor Shower "
                    + normalizedColor
                    + "on "
                    + rawText
                    + "!");
            int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.fireballCooldown", 75);
            if (player.getInventory().contains(Material.DRAGON_EGG)) {
              configuredValue /= 2;
            }

            int index = ConfigValueCache.getInt(this.plugin, "fireT2.fireballActiveSeconds", 6);
            ActiveAbilityStore.startActive(player.getUniqueId(), "fire_fireball_t2", index, configuredValue);
            this.linkedPlayers.put(player.getUniqueId(), player);
            this.activatePrimaryAbility(player, targetPlayer);
          }

          if (isGemItem(player.getInventory().getItemInOffHand())
              || isGemItem(player.getInventory().getItemInMainHand())) {
            double distance = ConfigValueCache.getDouble(this.plugin, "fireT2.flamestrikeChance", 0.08);
            if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < distance) {
              targetPlayer.setFireTicks(100);
            }
          }
        }
      }
    }
  }

  void activatePrimaryAbility(Player player, Player targetPlayer) {
    new BukkitRunnable() {
      final Player secondaryPlayer = targetPlayer;
      final Player player = targetPlayer;
      int durationTicks = 0;

      @Override
      public void run() {
        if (this.durationTicks < 10 && this.secondaryPlayer.isOnline() && !this.secondaryPlayer.isDead()) {
          double value = (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 4.0;
          double distance = (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 4.0;
          Location location = this.secondaryPlayer.getLocation().add(value, 10.0, distance);
          Fireball fireball = this.secondaryPlayer.getWorld().spawn(location, Fireball.class);
          fireball.setYield(0.0F);
          fireball.setIsIncendiary(false);
          fireball.setDirection(new Vector(0, -1, 0));
          fireball.setVelocity(new Vector(0, -2, 0));
          fireball.setShooter(this.player);
          FireTier2Gem.this.players.put(fireball.getUniqueId(), this.player);
          FireTier2Gem.this.abilityStages.put(fireball.getUniqueId(), -1);
          this.durationTicks++;
        } else {
          FireTier2Gem.this.linkedPlayers.remove(this.player.getUniqueId());
          this.cancel();
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(10L), 10L);
  }

  @EventHandler
  public void onProjectileHitTarget(ProjectileHitEvent event) {
    if (event.getEntity() instanceof Fireball fireball) {
      UUID playerId = fireball.getUniqueId();
      if (this.players.containsKey(playerId)) {
        Integer storedCount = this.abilityStages.get(playerId);
        if (storedCount != null && storedCount == -1) {
          Player player = this.players.remove(playerId);
          this.abilityStages.remove(playerId);
          Location location = fireball.getLocation();
          double effectRadius = ConfigValueCache.getDouble(this.plugin, "fireT2.meteorShowerRadius", 4.0);
          double configuredDamage = ConfigValueCache.getDouble(this.plugin, "fireT2.meteorShowerDamage", 3.0);
          double minimumHealth = ConfigValueCache.getDouble(this.plugin, "fireT2.minRemainingHealth", 4.0);

          try {
            for (Entity entity : location.getNearbyEntities(effectRadius, effectRadius, effectRadius)) {
              if (entity instanceof LivingEntity livingEntity
                  && livingEntity instanceof Player targetPlayer
                  && !targetPlayer.equals(player)
                  && (this.trustCommand == null
                      || !this.trustCommand.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId()))) {
                double currentHealth = targetPlayer.getHealth();
                double damage = configuredDamage;
                if (currentHealth - damage < minimumHealth) {
                  damage = Math.max(0.5, currentHealth - minimumHealth);
                }

                if (damage > 0.0) {
                  DamageSource damageSource =
                      DamageSource.builder(DamageType.MAGIC)
                          .withCausingEntity(player)
                          .withDirectEntity(fireball)
                          .build();
                  targetPlayer.damage(damage, damageSource);
                }
              }
            }
          } catch (Exception exception) {
            for (Entity targetEntity : location.getNearbyEntities(effectRadius, effectRadius, effectRadius)) {
              if (targetEntity instanceof LivingEntity attacker
                  && attacker instanceof Player sourcePlayer
                  && !sourcePlayer.equals(player)
                  && (this.trustCommand == null
                      || !this.trustCommand.isAbilityActive(player.getUniqueId(), sourcePlayer.getUniqueId()))) {
                double currentHealth = sourcePlayer.getHealth();
                double damage = configuredDamage;
                if (currentHealth - damage < minimumHealth) {
                  damage = Math.max(0.5, currentHealth - minimumHealth);
                }

                if (damage > 0.0) {
                  sourcePlayer.damage(damage, player);
                }
              }
            }
          }

          location.getWorld().createExplosion(location, 0.0F, false, false, player);
          fireball.remove();
        }
      }
    }
  }

  @EventHandler
  public void onEntityShootBow(EntityShootBowEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        if (isGemItem(player.getInventory().getItemInOffHand())
            || isGemItem(player.getInventory().getItemInMainHand())) {
          double configuredValue = ConfigValueCache.getDouble(this.plugin, "fireT2.fireshotChance", 0.08);
          if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < configuredValue) {
            event.getProjectile().setFireTicks(100);
          }
        }
      }
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Location location = event.getBlock().getLocation();
    if (event.getBlock().getType() == Material.SOUL_CAMPFIRE) {
      for (Entry<UUID,Location> entry : new HashSet<>(this.targetLocations.entrySet())) {
        Location targetLocation = entry.getValue();
        if (targetLocation != null && this.isActiveForPlayer(targetLocation, location)) {
          UUID playerId = entry.getKey();
          event.setCancelled(true);
          this.refreshPlayerState(playerId, targetLocation);
          Player player = Bukkit.getPlayer(playerId);
          if (player != null) {
            player.sendMessage(ChatColor.RED + "Your campfire was destroyed!");
          }

          event.getPlayer().sendMessage(ChatColor.RED + "Campfire destroyed! All effects removed.");
          break;
        }
      }
    }
  }

  @EventHandler
  public void onEntityExplode(EntityExplodeEvent event) {
    String entityType =
        event.getEntity() == null ? "null" : event.getEntity().getType().toString();
    String locationText = this.formatDisplayTextForPlayer(event.getLocation());
    boolean cancelled = event.isCancelled();
    int affectedBlockCount = event.blockList().size();
    float yield = event.getYield();
    this.updatePlayerState(
        null,
        "EntityExplodeEvent entity="
            + entityType
            + " loc="
            + locationText
            + " cancelled="
            + cancelled
            + " yield="
            + yield
            + " blocks="
            + affectedBlockCount);
    if (event.getEntity() instanceof Fireball fireball) {
      UUID playerId = fireball.getUniqueId();
      Integer storedCount = this.abilityStages.get(playerId);
      if (storedCount != null && storedCount != -1) {
        if (!this.isMatchingState(event.getLocation())) {
          event.setCancelled(true);
          event.blockList().clear();
          this.updatePlayerState(
              null,
              "EntityExplodeEvent cancelled tracked fireball id="
                  + playerId
                  + " by other-explosion deny");
        }

        this.players.remove(playerId);
        this.abilityStages.remove(playerId);
      }
    }

    Iterator<Block> iterator = event.blockList().iterator();

    while (iterator.hasNext()) {
      Block block = iterator.next();
      if (block.getType() == Material.SOUL_CAMPFIRE) {
        for (Entry<UUID,Location> entry : new HashSet<>(this.targetLocations.entrySet())) {
          Location targetLocation = entry.getValue();
          if (targetLocation != null && this.isActiveForPlayer(targetLocation, block.getLocation())) {
            UUID targetId = entry.getKey();
            iterator.remove();
            this.refreshPlayerState(targetId, targetLocation);
            Player player = Bukkit.getPlayer(targetId);
            if (player != null) {
              player.sendMessage(
                  ChatColor.RED + "Your campfire was destroyed by explosion! All effects removed.");
            }
            break;
          }
        }
      }
    }
  }

  @EventHandler
  public void onBlockExplode(BlockExplodeEvent event) {
    String locationKey = this.formatDisplayTextForTarget(event.getBlock().getLocation());
    int removedBlockCount = 0;
    if (this.namedCounters.containsKey(locationKey)) {
      int originalBlockCount = event.blockList().size();
      event.blockList().removeIf(this::isExplosionBlockAllowed);
      removedBlockCount = originalBlockCount - event.blockList().size();
      this.namedCounters.put(locationKey, event.blockList().size());
    }

    String blockType = event.getBlock().getType().toString();
    String locationText = this.formatDisplayTextForPlayer(event.getBlock().getLocation());
    boolean cancelled = event.isCancelled();
    float yield = event.getYield();
    int remainingBlockCount = event.blockList().size();
    this.updatePlayerState(
        null,
        "BlockExplodeEvent block="
            + blockType
            + " loc="
            + locationText
            + " cancelled="
            + cancelled
            + " yield="
            + yield
            + " blocks="
            + remainingBlockCount
            + " protectedRemoved="
            + removedBlockCount);
  }

  boolean isActiveForPlayer(Location location, Location targetLocation) {
    return location.getWorld().equals(targetLocation.getWorld())
        && location.getBlockX() == targetLocation.getBlockX()
        && location.getBlockY() == targetLocation.getBlockY()
        && location.getBlockZ() == targetLocation.getBlockZ();
  }

  @EventHandler
  public void onPrimaryPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    UUID playerId = player.getUniqueId();
    UUID targetId = this.playerLinks.get(playerId);
    if (targetId != null) {
      Player targetPlayer = Bukkit.getPlayer(targetId);
      if (targetPlayer != null) {
        String textColor = ChatColor.WHITE.toString();
        String name = player.getName();
        String messageColor = ChatColor.WHITE.toString();
        String displayText = targetPlayer.getName();
        event.setDeathMessage(textColor + name + " ate a fireball from " + messageColor + displayText);
      } else {
        event.setDeathMessage(ChatColor.WHITE + player.getName() + " ate a fireball");
      }

      this.playerLinks.remove(playerId);
    } else {
      int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.crispRadius", 5);

      for (Entry<UUID,Location> entry : this.playerLocations.entrySet()) {
        Location location = entry.getValue();
        if (location != null
            && player.getWorld().equals(location.getWorld())
            && player.getLocation().distanceSquared(location) <= configuredValue * configuredValue) {
          Player sourcePlayer = Bukkit.getPlayer(entry.getKey());
          if (sourcePlayer == null) {
            break;
          }

          if (this.trustCommand == null || !this.trustCommand.isAbilityActive(sourcePlayer.getUniqueId(), player.getUniqueId())) {
            event.setDeathMessage(player.getName() + " was set ablaze by " + sourcePlayer.getName());
            break;
          }
        }
      }
    }
  }

  @EventHandler
  public void onPrimaryEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        int configuredValue = ConfigValueCache.getInt(this.plugin, "fireT2.crispRadius", 5);

        for (Entry<UUID,Location> entry : this.playerLocations.entrySet()) {
          Location location = entry.getValue();
          if (location != null
              && player.getWorld().equals(location.getWorld())
              && player.getLocation().distanceSquared(location) <= configuredValue * configuredValue) {
            Player targetPlayer = Bukkit.getPlayer(entry.getKey());
            if (targetPlayer != null
                && this.trustCommand != null
                && (player.equals(targetPlayer)
                    || this.trustCommand.isAbilityActive(targetPlayer.getUniqueId(), player.getUniqueId()))
                && (event.getCause() == DamageCause.FIRE
                    || event.getCause() == DamageCause.FIRE_TICK
                    || event.getCause() == DamageCause.LAVA
                    || event.getCause() == DamageCause.HOT_FLOOR)) {
              event.setCancelled(true);
              player.setFireTicks(0);
            }
            break;
          }
        }
      }
    }
  }

  public void updatePrimaryAbilityState(UUID playerId, UUID targetId) {
    this.playerRelations.computeIfAbsent(playerId, FireTier2Gem::createTrackingSet).add(targetId);
  }

  public void updateTargetAbilityState(UUID playerId, UUID targetId) {
    Set<UUID> trackedPlayerIds = this.playerRelations.get(playerId);
    if (trackedPlayerIds != null) {
      trackedPlayerIds.remove(targetId);
      if (trackedPlayerIds.isEmpty()) {
        this.playerRelations.remove(playerId);
      }
    }
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    Player player = event.getEntity().getKiller();
    if (player != null) {
      if (isGemItem(player.getInventory().getItemInMainHand())
          || isGemItem(player.getInventory().getItemInOffHand())) {
        if (ConfigValueCache.getBoolean(this.plugin, "fireT2.autoSmeltEnabled", true)) {
          List<ItemStack> entries = event.getDrops();

          for (int count = 0; count < entries.size(); count++) {
            ItemStack item = entries.get(count);
            switch (item.getType()) {
              case MUTTON ->
                  entries.set(count, new ItemStack(Material.COOKED_MUTTON, item.getAmount()));
              case BEEF -> entries.set(count, new ItemStack(Material.COOKED_BEEF, item.getAmount()));
              case PORKCHOP ->
                  entries.set(count, new ItemStack(Material.COOKED_PORKCHOP, item.getAmount()));
              case CHICKEN ->
                  entries.set(count, new ItemStack(Material.COOKED_CHICKEN, item.getAmount()));
              case RABBIT ->
                  entries.set(count, new ItemStack(Material.COOKED_RABBIT, item.getAmount()));
            }
          }

          event.getEntity()
              .getWorld()
              .playEffect(event.getEntity().getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 1);
        }
      }
    }
  }

  void startPrimaryBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            double configuredValue = ConfigValueCache.getDouble(FireTier2Gem.this.plugin, "fireT2.crispFireDamagePerSecond", 1.0);

            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();

              for (Entry<UUID,Location> entry : FireTier2Gem.this.playerLocations.entrySet()) {
                UUID targetId = entry.getKey();
                Location location = entry.getValue();
                if (location != null && player.getWorld().equals(location.getWorld())) {
                  double value = ConfigValueCache.getInt(FireTier2Gem.this.plugin, "fireT2.crispRadius", 5);
                  if (!(player.getLocation().distanceSquared(location) > value * value)
                      && !playerId.equals(targetId)
                      && (FireTier2Gem.this.trustCommand == null || !FireTier2Gem.this.trustCommand.isAbilityActive(targetId, playerId))) {
                    player.setFireTicks(60);
                    if (configuredValue > 0.0) {
                      player.damage(configuredValue, DamageSource.builder(DamageType.GENERIC).build());
                    }
                    break;
                  }
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  public Set<UUID> getState(UUID playerId) {
    return this.playerRelations.getOrDefault(playerId, new HashSet<>());
  }

  @EventHandler
  public void onEntityDamageByEntityTarget(EntityDamageByEntityEvent event) {
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
      target.setFireTicks(80);
    }
  }

  public void cleanup() {
    this.abilityBossBars.values().forEach(BossBar::removeAll);
    this.bossBars.values().forEach(BossBar::removeAll);
    this.animationTasks.values().forEach(BukkitTask::cancel);
    this.cooldownTasks.values().forEach(BukkitTask::cancel);
    this.scheduledTasks.values().forEach(BukkitTask::cancel);
    this.cleanupTasks.values().forEach(BukkitTask::cancel);
    this.effectTasks.values().forEach(BukkitTask::cancel);
    for (Set<BukkitRunnable> tasks : this.activeRunnables.values()) {
      for (BukkitRunnable task : tasks) {
        try {
          task.cancel();
        } catch (IllegalStateException ignored) {
          // A task that was created but never submitted has nothing to cancel.
        }
      }
    }
    this.activeTasks.values().forEach(tasks -> tasks.forEach(BukkitTask::cancel));
    this.itemDisplays.values().stream().filter(display -> !display.isDead()).forEach(Entity::remove);
    this.armorStands.values().stream().filter(FireTier2Gem::isArmorStandActive).forEach(Entity::remove);

    for (Entry<UUID, Map<Location, BlockData>> entry : this.temporaryBlockSnapshots.entrySet()) {
      this.cleanupAbilityState(entry.getKey(), entry.getValue(), this.blockSnapshots.get(entry.getKey()));
    }

    for (Entry<UUID, Location> currentEntry : new HashSet<>(this.targetLocations.entrySet())) {
      this.removeCampfireResources(currentEntry.getKey(), currentEntry.getValue(), false);
    }

    this.playerLocations.clear();
    this.targetLocations.clear();
    this.temporaryBlockSnapshots.clear();
    this.blockSnapshots.clear();
    this.abilityStages.clear();
    this.playerCounters.clear();
    this.itemDisplays.clear();
    this.abilityBossBars.clear();
    this.bossBars.clear();
    this.animationTasks.clear();
    this.cooldownTasks.clear();
    this.scheduledTasks.clear();
    this.cleanupTasks.clear();
    this.effectTasks.clear();
    this.armorStands.clear();
    this.players.clear();
    this.linkedPlayers.clear();
    this.lastUseTimes.clear();
    this.playerRelations.clear();
    this.namedCounters.clear();
    this.playerIds.clear();
    this.anchorLocation = null;
    this.effectRadius = 0.0;
    this.cooldownTimestamps.clear();
    this.trackedLocations.clear();
    this.activeRunnables.clear();
    this.activeTasks.clear();
  }

  public void activateTargetAbility(Player player) {
    UUID playerId = player.getUniqueId();
  }

  static boolean isArmorStandActive(ArmorStand armorStand) {
    return !armorStand.isDead();
  }

  static Set<UUID> createTrackingSet(UUID playerId) {
    return new HashSet<>();
  }

  boolean isExplosionBlockAllowed(Block block) {
    return !this.isProtectedLocation(block.getLocation(), false);
  }

  void clearFireballLink(Player player, Player targetPlayer) {
    UUID playerId = this.playerLinks.get(player.getUniqueId());
    if (playerId != null && playerId.equals(targetPlayer.getUniqueId())) {
      this.playerLinks.remove(player.getUniqueId());
    }
  }

  static Set<BukkitTask> createTrackingSetForPlayer(UUID playerId) {
    return ConcurrentHashMap.newKeySet();
  }

  static Set<BukkitRunnable> createTrackingSetForTarget(UUID playerId) {
    return ConcurrentHashMap.newKeySet();
  }

  static {
    FIRE_CAMPFIRE_T2_ID = "fire_campfire_t2";
    FIRE_FIREBALL_T2_ID = "fire_fireball_t2";
    FIRE_CRISP_T2_ID = "fire_crisp_t2";
    FIRE_AFFECTED_MATERIALS =
        EnumSet.of(
            Material.OBSIDIAN,
            Material.CRYING_OBSIDIAN,
            Material.MAGMA_BLOCK,
            Material.NETHERRACK,
            Material.BASALT,
            Material.SMOOTH_BASALT,
            Material.BLACKSTONE,
            Material.POLISHED_BLACKSTONE,
            Material.POLISHED_BLACKSTONE_BRICKS,
            Material.GILDED_BLACKSTONE,
            Material.LAVA,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.SOUL_SAND,
            Material.SOUL_SOIL,
            Material.ANCIENT_DEBRIS,
            Material.NETHER_BRICKS,
            Material.RED_NETHER_BRICKS,
            Material.CRACKED_NETHER_BRICKS,
            Material.CHISELED_NETHER_BRICKS);
    MATERIAL_CONVERSIONS = new EnumMap<>(Material.class);
    MATERIAL_CONVERSIONS.put(Material.POTATO, Material.BAKED_POTATO);
    MATERIAL_CONVERSIONS.put(Material.RAW_IRON, Material.IRON_INGOT);
    MATERIAL_CONVERSIONS.put(Material.RAW_COPPER, Material.COPPER_INGOT);
    MATERIAL_CONVERSIONS.put(Material.RAW_GOLD, Material.GOLD_INGOT);
    MATERIAL_CONVERSIONS.put(Material.MUTTON, Material.COOKED_MUTTON);
    MATERIAL_CONVERSIONS.put(Material.BEEF, Material.COOKED_BEEF);
    MATERIAL_CONVERSIONS.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
    MATERIAL_CONVERSIONS.put(Material.SAND, Material.GLASS);
    MATERIAL_CONVERSIONS.put(Material.COBBLESTONE, Material.STONE);
    MATERIAL_CONVERSIONS.put(Material.STONE, Material.SMOOTH_STONE);
    MATERIAL_CONVERSIONS.put(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE);
    MATERIAL_CONVERSIONS.put(Material.CHICKEN, Material.COOKED_CHICKEN);
    MATERIAL_CONVERSIONS.put(Material.PORKCHOP, Material.COOKED_PORKCHOP);
    MATERIAL_CONVERSIONS.put(Material.CLAY_BALL, Material.BRICK);
    MATERIAL_CONVERSIONS.put(Material.OAK_LOG, Material.CHARCOAL);
    MATERIAL_CONVERSIONS.put(Material.SPRUCE_LOG, Material.CHARCOAL);
    MATERIAL_CONVERSIONS.put(Material.BIRCH_LOG, Material.CHARCOAL);
    MATERIAL_CONVERSIONS.put(Material.JUNGLE_LOG, Material.CHARCOAL);
    MATERIAL_CONVERSIONS.put(Material.ACACIA_LOG, Material.CHARCOAL);
    MATERIAL_CONVERSIONS.put(Material.DARK_OAK_LOG, Material.CHARCOAL);
    MATERIAL_CONVERSIONS.put(Material.CHERRY_LOG, Material.CHARCOAL);
    MATERIAL_CONVERSIONS.put(Material.MANGROVE_LOG, Material.CHARCOAL);
  }
}
