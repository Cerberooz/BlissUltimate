package cerberooz.cerberooz.BlissUltimate;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
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
import java.util.concurrent.TimeUnit;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.npc.NPC.Metadata;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
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
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Horse;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerLoadEvent;
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
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class AstraTier2Gem implements Listener {
  static final String BLISS_ASTRA_T2_DRIFT_HORSE_ID;
  static final String BLISS_ASTRA_BLUR_OWNER_ID;
  final Map<UUID, UUID> secondaryPlayerLinks = new ConcurrentHashMap<>();
  final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
  static final Map<UUID, Long> lastUpdateTimes;
  final Map<UUID, Boolean> activeFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> nextFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> previousFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Zombie> trackedZombiesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, List<ItemDisplay>> activeDisplaysByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Location> playerLocations = new ConcurrentHashMap<>();
  final Map<UUID, GameMode> targetGameModesByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, ItemStack[]> resolvedInventorySnapshotsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, ItemStack[]> primaryInventorySnapshotsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, ItemStack> cachedItemsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> sourceFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Player> players = new ConcurrentHashMap<>();
  final Map<UUID, Integer> abilityStages = new ConcurrentHashMap<>();
  final Map<UUID, MovingDagger> cachedDaggersByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Long> expiryTimes = new ConcurrentHashMap<>();
  final Map<UUID, Long> startTimes = new ConcurrentHashMap<>();
  BukkitTask secondaryScheduledTask;
  BukkitTask activeScheduledTask;
  final Map<UUID, Player> targetPlayers = new HashMap<>();
  final Map<UUID, Long> cooldownTimestamps = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastActivationTimes = new ConcurrentHashMap<>();
  Map<UUID, Integer> playerCounters = new HashMap<>();
  Map<UUID, Integer> chargeLevels = new HashMap<>();
  final Map<UUID, Boolean> defaultFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> secondaryFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Horse> pendingHorsesByPlayer = new ConcurrentHashMap<>();
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();
  final Map<UUID, ItemStack> trackedItemsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, ItemStack> itemsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, ItemStack> storedItemsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, ItemStack> currentItemsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Long> sourceLastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Long> activeLastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Long> targetLastUseTimes = new ConcurrentHashMap<>();
  final Set<UUID> activePlayers = new HashSet<>();
  final Cache<UUID, Long> cache =
      CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.SECONDS).build();
  ItemStack item;
  Bliss plugin;
  TrustCommand trustCommand;
  Map<UUID, BukkitTask> cooldownTasks = new HashMap<>();
  final Map<UUID, BukkitTask> scheduledTasks = new ConcurrentHashMap<>();
  static Map<UUID, Player> linkedPlayers;
  static Map<UUID, Long> primaryLastUseTimes;
  BukkitTask scheduledTask;
  Map<UUID, UUID> playerLinks = new HashMap<>();
  Map<UUID, GameMode> pendingGameModesByPlayer = new HashMap<>();
  Map<UUID, BukkitTask> animationTasks = new HashMap<>();
  Map<UUID, UUID> activePlayerLinks = new HashMap<>();
  final Map<UUID, NPC> fallbackNpcsByPlayer = new ConcurrentHashMap<>();
  static final Map<UUID, Integer> remainingTicks;
  final Map<UUID, List<AstraTier2EntitySnapshot>> temporaryEntitySnapshotsByPlayer = new HashMap<>();
  final Map<UUID, Long> pendingLastUseTimes = new HashMap<>();

  public AstraTier2Gem(Bliss bliss) {
    this.plugin = bliss;
    this.item = createGemItem();
    this.startBackgroundTasks();
    this.playAbilityEffects();
    this.spawnAbilityEffects();
  }

  public static boolean canUseAbility(Player player) {
    return linkedPlayers.containsKey(player.getUniqueId());
  }

  public static void activateAbility(Player player) {
    UUID playerId = player.getUniqueId();
    if (linkedPlayers.containsKey(playerId)) {
      linkedPlayers.remove(playerId);
    }
  }

  public void setTrustCommand(TrustCommand trustCommand) {
    this.trustCommand = trustCommand;
  }

  boolean isTrustedPlayer(Player player, Entity entity) {
    if (this.trustCommand == null) {
      return false;
    } else {
      return (entity instanceof Player) && this.trustCommand.isAbilityActive(player.getUniqueId(), entity.getUniqueId());
    }
  }

  public static boolean isGemItem(ItemStack item) {
    if (item != null && item.getType() == Material.PRISMARINE_SHARD) {
      ItemMeta itemMeta = item.getItemMeta();
      if (itemMeta != null && itemMeta.hasCustomModelData()) {
        int count = itemMeta.getCustomModelData();
        return count == 94 || count == 74 || count == 54 || count == 34 || count == 14;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  boolean isAbilityAllowed(Player player) {
    return isGemItem(player.getInventory().getItemInMainHand())
        || isGemItem(player.getInventory().getItemInOffHand());
  }

  boolean isAbilityBlocked(Player player) {
    return this.plugin.isGemsDisabled()
       
        || this.plugin.canUseAbility(player);
  }

  boolean isMatchingState(Player player) {
    return isGemItem(player.getInventory().getItemInMainHand());
  }

  boolean isValidTarget(Player player) {
    UUID playerId = player.getUniqueId();
    return this.activeFlagsByPlayer.getOrDefault(playerId, false)
        ? isGemItem(player.getInventory().getItemInMainHand())
        : this.isAbilityAllowed(player);
  }

  boolean isProtectedTarget(Player player) {
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    return offHandItem != null && offHandItem.getType() != Material.AIR;
  }

  boolean isAbilityActive(UUID playerId) {
    Long storedTimestamp = this.cooldownTimestamps.get(playerId);
    return storedTimestamp == null || System.currentTimeMillis() >= storedTimestamp;
  }

  boolean isActiveForPlayer(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Material material = item.getType();
      return material.name().contains("SWORD") || material.name().contains("AXE");
    } else {
      return true;
    }
  }

  public static void sendAbilityFeedback(UUID playerId, long timestamp) {
    long now = System.currentTimeMillis() + timestamp;
    Long storedTimestamp = lastUpdateTimes.get(playerId);
    if (storedTimestamp == null || now > storedTimestamp) {
      lastUpdateTimes.put(playerId, now);
      Player player = Bukkit.getPlayer(playerId);
      if (player != null && player.isOnline()) {
        String accentColor = ChatColor.of("#A01FFF").toString();
        String red = ChatColor.RED.toString();
        long lastUpdateTime = timestamp / 1000L;
        player.sendMessage(
            accentColor + "🔮 " + red + "Your gem has been disabled for " + lastUpdateTime + "s!");
      }
    }
  }

  public static void updateAbilityState(UUID playerId, long timestamp) {
    long now = System.currentTimeMillis() + timestamp;
    Long storedTimestamp = lastUpdateTimes.get(playerId);
    if (storedTimestamp == null || now > storedTimestamp) {
      lastUpdateTimes.put(playerId, now);
      Player player = Bukkit.getPlayer(playerId);
    }
  }

  public static void refreshAbilityState(UUID playerId, long timestamp) {
    long now = System.currentTimeMillis() + timestamp;
    Long storedTimestamp = primaryLastUseTimes.get(playerId);
    if (storedTimestamp == null || now > storedTimestamp) {
      primaryLastUseTimes.put(playerId, now);
    }
  }

  public void updateState(Entity entity, long timestamp) {
    if (entity instanceof Player) {
      sendAbilityFeedback(entity.getUniqueId(), timestamp);
    }
  }

  public boolean hasRequiredState(UUID playerId) {
    Long storedTimestamp = lastUpdateTimes.get(playerId);
    if (storedTimestamp == null) {
      return false;
    } else if (System.currentTimeMillis() >= storedTimestamp) {
      lastUpdateTimes.remove(playerId);
      return false;
    } else {
      return true;
    }
  }

  public boolean shouldApplyEffect(UUID playerId) {
    Long storedTimestamp = primaryLastUseTimes.get(playerId);
    if (storedTimestamp == null) {
      return false;
    } else if (System.currentTimeMillis() >= storedTimestamp) {
      primaryLastUseTimes.remove(playerId);
      return false;
    } else {
      return true;
    }
  }

  public long getAbilityLong(UUID playerId) {
    Long storedTimestamp = lastUpdateTimes.get(playerId);
    if (storedTimestamp == null) {
      return 0L;
    } else {
      long now = storedTimestamp - System.currentTimeMillis();
      if (now <= 0L) {
        lastUpdateTimes.remove(playerId);
        return 0L;
      } else {
        return now;
      }
    }
  }

  public void initialize() {
    lastUpdateTimes.clear();
  }

  void applyAbilityEffects(UUID playerId, long timestamp) {
    sendAbilityFeedback(playerId, timestamp);
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    ItemStack item = event.getItemDrop().getItemStack();
    if (isGemItem(item)) {
      event.setCancelled(true);
    }
  }

  public static ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(14);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.of("#befff7") + "Energy:");
      lore.add(ChatColor.of("#82EDBF") + "Pristine");
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏꜰ ᴄᴏᴍᴏѕ");
      lore.add("");
      lore.add(
          ChatColor.of("#A01FFF")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Phasing");
      lore.add(ChatColor.GRAY + "- Soul Healing");
      lore.add(ChatColor.GRAY + "- Soul Capture");
      lore.add("");
      String accentColor = ChatColor.of("#A01FFF").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      lore.add("");
      accentColor = ChatColor.of("#A01FFF").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      lore.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      accentColor = ChatColor.GRAY.toString();
      messageColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      lore.add(
          accentColor
              + "- "
              + messageColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      accentColor = ChatColor.GRAY.toString();
      messageColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      lore.add(
          accentColor
              + "- "
              + messageColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      lore.add("");
      accentColor = ChatColor.GRAY.toString();
      messageColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      lore.add(
          accentColor
              + "- "
              + messageColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      accentColor = ChatColor.GRAY.toString();
      messageColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      lore.add(
          accentColor
              + "- "
              + messageColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  void startBackgroundTasks() {
    this.scheduledTask =
        SharedScheduler.scheduleRepeating(
            new BukkitRunnable() {
              static final String ASTRAL_PROJECTION_DARKNESS_ID = "astralProjectionDarkness";
              @Override
              public void run() {
                for (UUID playerId : AstraTier2Gem.this.activePlayers) {
                  Player player = Bukkit.getPlayer(playerId);
                  if (player != null && player.isOnline()) {
                    AstraTier2Gem.this.spawnTargetAbilityParticles(player);
                    player.setWalkSpeed(0.15F);
                    if (ConfigValueCache.getBoolean(Bliss.getInstance(), ASTRAL_PROJECTION_DARKNESS_ID, true)
                        && player.getLocation().getBlock().getType() != Material.AIR) {
                      player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 1));
                    }
                  }
                }
              }


            }, this.plugin, SharedScheduler.staggeredInitialDelay(2L), 2L);
  }

  void playAbilityEffects() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            long now = System.currentTimeMillis();
            AstraTier2Gem.this.initializePrimaryState();

            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              AstraTier2Gem.this.hasRequiredState(playerId);
              Long storedTimestamp = AstraTier2Gem.this.cooldownTimestamps.get(playerId);
              if (storedTimestamp != null && now > storedTimestamp) {
                AstraTier2Gem.this.cooldownTimestamps.remove(playerId);
              }

              Long targetLong = AstraTier2Gem.this.lastActivationTimes.get(playerId);
              if (targetLong != null && now > targetLong) {
                AstraTier2Gem.this.lastActivationTimes.remove(playerId);
              }

              if (AstraTier2Gem.this.isAbilityAllowed(player)) {
                if (AstraTier2Gem.this.isAbilityBlocked(player)) {
                  if (AstraTier2Gem.this.defaultFlagsByPlayer.getOrDefault(playerId, false)) {
                    AstraTier2Gem.this.activateCachedAbility(player);
                  }

                  if (AstraTier2Gem.this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
                    AstraTier2Gem.this.teleportPrimaryAbilityTarget(player);
                  }

                  if (AstraTier2Gem.this.players.containsKey(playerId) || AstraTier2Gem.this.activeDisplaysByPlayer.containsKey(playerId)) {
                    AstraTier2Gem.this.updatePrimaryBossBar(playerId);
                  }

                  if (AstraTier2Gem.this.playerLinks.containsKey(playerId)) {
                    AstraTier2Gem.this.handleAbilityAction(player, false, null);
                  }

                  String darkGray = ChatColor.DARK_GRAY.toString();
                  String bold = ChatColor.BOLD.toString();
                  ActionBarQueue.enqueue(
                      player,
                      "🔒 "
                      + darkGray
                      + bold
                      + "á´…ÉŞŃ•á´€Ę™Ęźá´‡á´…");
                } else {
                  AstraTier2Gem.this.sendSourceAbilityFeedback(player);
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void spawnAbilityEffects() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          static final String ASTRA_T2_ASTRAL_PROJECTION_RANGE_ID = "astraT2.astralProjectionRange";
          @Override
          public void run() {
            int configuredValue = ConfigValueCache.getInt(AstraTier2Gem.this.plugin, ASTRA_T2_ASTRAL_PROJECTION_RANGE_ID, 150);

            for (Entry<UUID,Location> entry : AstraTier2Gem.this.playerLocations.entrySet()) {
              Player player = Bukkit.getPlayer(entry.getKey());
              if (player != null && AstraTier2Gem.this.activeFlagsByPlayer.getOrDefault(entry.getKey(), false)) {
                if (AstraTier2Gem.this.isAbilityBlocked(player)) {
                  AstraTier2Gem.this.teleportPrimaryAbilityTarget(player);
                } else {
                  ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                  if (!AstraTier2Gem.isGemItem(mainHandItem)) {
                    AstraTier2Gem.this.teleportPrimaryAbilityTarget(player);
                  } else {
                    Location location = entry.getValue();
                    Location targetLocation = player.getLocation();
                    if (location.getWorld() == null
                        || targetLocation.getWorld() == null
                        || !location.getWorld().equals(targetLocation.getWorld())) {
                      AstraTier2Gem.this.teleportPrimaryAbilityTarget(player);
                    } else if (location.distanceSquared(targetLocation) > configuredValue * configuredValue) {
                      AstraTier2Gem.this.teleportPrimaryAbilityTarget(player);
                    }
                  }
                }
              }
            }
          }


        }, this.plugin, SharedScheduler.staggeredInitialDelay(5L), 5L);
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      Entity entity = event.getEntity();
      if (entity instanceof LivingEntity livingEntity) {
        UUID playerId = player.getUniqueId();
        if (this.isMatchingState(player)) {
          if (!this.isAbilityBlocked(player)) {
            if (!this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
              if (!this.playerLinks.containsKey(playerId)) {
                if (!this.players.containsKey(playerId) && !this.activeDisplaysByPlayer.containsKey(playerId)) {
                  if (!ActiveAbilityStore.isActive(playerId, "astra_daggers_t2")) {
                    if (CooldownService.isOnCooldown(playerId, "astra_daggers_t2")) {
                      String accentColor = ChatColor.of("#A01FFF").toString();
                      String messageColor = ChatColor.of("#FF686F").toString();
                      String gray = ChatColor.GRAY.toString();
                      String displayColor = ChatColor.of("#FF686F").toString();
                      String textColor = ChatColor.DARK_PURPLE.toString();
                      String text =
                          AbilityStatusFormatter.formatDisplayText(
                              playerId, "astra_daggers_t2", false);
                      player.sendMessage(
                          accentColor
                              + "🔮 "
                              + messageColor
                              + "Your "
                              + gray
                              + "Unbounded "
                              + displayColor
                              + "skill is on cooldown for "
                              + textColor
                              + text);
                    } else {
                      if (entity instanceof Player targetPlayer) {
                        if (targetPlayer.getGameMode() == GameMode.CREATIVE
                            || targetPlayer.getGameMode() == GameMode.SPECTATOR) {
                          return;
                        }

                        if (this.isTrustedPlayer(player, targetPlayer)) {
                          return;
                        }
                      }

                      GameMode gameMode = player.getGameMode();
                      this.pendingGameModesByPlayer.put(playerId, gameMode);
                      player.setGameMode(GameMode.SPECTATOR);
                      player.setSpectatorTarget(entity);
                      this.playerLinks.put(playerId, livingEntity.getUniqueId());
                      this.activePlayerLinks.put(livingEntity.getUniqueId(), playerId);
                      this.resetAbilityState(player);
                      player.sendMessage(
                          ChatColor.of("#A01FFF")
                              + "🔮 "
                              + ChatColor.of("#befff7")
                              + "You activated "
                              + ChatColor.GRAY
                              + "Unbounded");
                      new BukkitRunnable() {
                        final Player capturedPlayer = player;
                        int maxCount = 0;
                        int durationTicks = ConfigValueCache.getInt(Bliss.getInstance(), ASTRA_T2_UNBOUNDED_DURATION_ID, 15);
                        static final String ASTRA_T2_UNBOUNDED_DURATION_ID = "astraT2.unboundedDuration";
                        @Override
                        public void run() {
                          if (this.maxCount >= this.durationTicks) {
                            AstraTier2Gem.this.handleAbilityAction(this.capturedPlayer, true, null);
                            this.cancel();
                          } else {
                            this.maxCount++;
                          }
                        }


                      }
                          .runTaskTimer(Bliss.getInstance(), 0L, 20L);
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
  public void cleanupAbilityState(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof LivingEntity livingEntity) {
      if (event.getDamager() instanceof Player player) {
        UUID playerId = livingEntity.getUniqueId();
        UUID targetId = this.activePlayerLinks.get(playerId);
        if (targetId != null) {
          if (!player.getUniqueId().equals(targetId)) {
            Player targetPlayer = Bukkit.getPlayer(targetId);
            if (targetPlayer == null) {
              this.activePlayerLinks.remove(playerId);
              this.playerLinks.remove(targetId);
              this.pendingGameModesByPlayer.remove(targetId);
              BukkitTask task = this.animationTasks.remove(targetId);
              if (task != null) {
                task.cancel();
              }
            } else {
              this.handleAbilityAction(targetPlayer, true, player);
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  public void onPlayerStopSpectatingEntity(PlayerStopSpectatingEntityEvent event) {
    Player player = event.getPlayer();
    if (this.playerLinks.containsKey(player.getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (this.playerLinks.containsKey(playerId)) {
      event.setCancelled(true);
      if (event.getCause() == TeleportCause.SPECTATE) {
        player.sendMessage(
            ChatColor.of("#A01FFF")
                + "🔮 "
                + ChatColor.RED
                + "You cannot teleport during Unbounded!");
      } else {
        player.sendMessage(
            ChatColor.of("#A01FFF")
                + "🔮 "
                + ChatColor.RED
                + "You cannot teleport during Unbounded!");
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void scheduleAbilityUpdate(PlayerTeleportEvent event) {
    Location location = event.getTo();
    if (location != null && !event.getFrom().getWorld().equals(location.getWorld())) {
      UUID playerId = this.activePlayerLinks.get(event.getPlayer().getUniqueId());
      if (playerId != null) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
          this.handleAbilityAction(player, false, null);
        } else {
          this.processAbilityState(playerId, event.getPlayer().getUniqueId());
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  public void onPlayerStartSpectatingEntity(PlayerStartSpectatingEntityEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    UUID targetId = this.playerLinks.get(playerId);
    if (targetId != null) {
      Entity entity = event.getNewSpectatorTarget();
      if (!entity.getUniqueId().equals(targetId)) {
        event.setCancelled(true);
        player.sendMessage(
            ChatColor.of("#A01FFF")
                + "🔮 "
                + ChatColor.RED
                + "You cannot switch targets during Unbounded!");
      }
    }
  }

  void completeAbilityAction(Player player) {
    if (player != null && player.isOnline()) {
      UUID playerId = player.getUniqueId();
      UUID targetId = this.playerLinks.get(playerId);
      if (targetId != null) {
        Entity entity = Bukkit.getEntity(targetId);
        if (entity != null && entity.isValid() && !entity.isDead()) {
          if (player.getGameMode() != GameMode.SPECTATOR) {
            this.handleAbilityAction(player, false, null);
          } else if (!entity.getWorld().equals(player.getWorld())) {
            this.handleAbilityAction(player, false, null);
          } else {
            Entity targetEntity = player.getSpectatorTarget();
            if (targetEntity == null || !targetEntity.getUniqueId().equals(targetId)) {
              player.setSpectatorTarget(entity);
            }
          }
        } else {
          this.handleAbilityAction(player, false, null);
        }
      }
    }
  }

  void processAbilityState(UUID playerId, UUID targetId) {
    this.playerLinks.remove(playerId);
    this.activePlayerLinks.remove(targetId);
    this.pendingGameModesByPlayer.remove(playerId);
    BukkitTask task = this.animationTasks.remove(playerId);
    if (task != null) {
      task.cancel();
    }
  }

  void handleAbilityAction(Player player, boolean enabled, Player targetPlayer) {
    UUID playerId = player.getUniqueId();
    UUID targetId = this.playerLinks.remove(playerId);
    if (targetId != null) {
      this.activePlayerLinks.remove(targetId);
      BukkitTask task = this.animationTasks.remove(playerId);
      if (task != null) {
        task.cancel();
      }

      GameMode gameMode = this.pendingGameModesByPlayer.remove(playerId);
      player.setSpectatorTarget(null);
      player.setGameMode(gameMode != null ? gameMode : GameMode.SURVIVAL);
      if (enabled && Bukkit.getEntity(targetId) instanceof Player sourcePlayer) {
        this.applyAbilityEffects(sourcePlayer.getUniqueId(), 30000L);
      }

      int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.daggersCooldown", 80);
      if (player.getInventory().contains(Material.DRAGON_EGG)) {
        configuredValue /= 2;
      }

      CooldownService.setCooldown(playerId, "astra_daggers_t2", configuredValue);
      ActiveAbilityStore.removeAbility(playerId, "astra_daggers_t2");
      if (targetPlayer != null) {
        targetPlayer.sendMessage(
            ChatColor.of("#A01FFF")
                + "🔮 "
                + ChatColor.of("#befff7")
                + "You have disrupted "
                + player.getName()
                + "'s unbounded");
        String accentColor = ChatColor.of("#A01FFF").toString();
        String messageColor = ChatColor.of("#befff7").toString();
        String name = targetPlayer.getName();
        player.sendMessage(
            accentColor + "🔮 " + messageColor + name + " has disrupted your unbounded");
      }
    }
  }

  void resetAbilityState(Player player) {
    BukkitTask scheduledTask = new BukkitRunnable() {
      @Override
      public void run() {
        AstraTier2Gem.this.completeAbilityAction(player);
        DustOptions dustOptions = new DustOptions(Color.fromRGB(106, 11, 184), 1.0F);
        Location location = player.getLocation().clone().add(0.0, 0.5, 0.0);
        location.getWorld().spawnParticle(Particle.DUST, location, 10, 0.3, 1.0, 0.3, 0.0, dustOptions);
      }
    }.runTaskTimer(Bliss.getInstance(), 1L, 1L);
    this.animationTasks.put(player.getUniqueId(), scheduledTask);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();
    if (this.playerLinks.containsKey(player.getUniqueId())) {
      String normalizedText = event.getMessage().toLowerCase(Locale.ROOT).trim();
      if (normalizedText.startsWith("/tp ")
          || normalizedText.equals("/tp")
          || normalizedText.startsWith("/minecraft:tp ")
          || normalizedText.startsWith("/teleport ")
          || normalizedText.equals("/teleport")
          || normalizedText.startsWith("/minecraft:teleport ")
          || normalizedText.startsWith("/tpa ")
          || normalizedText.equals("/tpa")
          || normalizedText.startsWith("/tpaccept")
          || normalizedText.startsWith("/spectate ")
          || normalizedText.equals("/spectate")
          || normalizedText.startsWith("/minecraft:spectate ")) {
        event.setCancelled(true);
        player.sendMessage(
            ChatColor.of("#A01FFF")
                + "đź”® "
                + ChatColor.RED
                + "You cannot use teleportation during Unbounded!");
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    ItemStack item = event.getItem();
    if (event.getHand() == EquipmentSlot.HAND) {
      if (!this.isAbilityBlocked(player)) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        boolean enabled =
            player.getGameMode() != GameMode.SPECTATOR
                && !this.activeFlagsByPlayer.getOrDefault(playerId, false)
                && !this.previousFlagsByPlayer.getOrDefault(playerId, false);
        if (enabled
            && !this.cache.asMap().keySet().contains(playerId)
            && isGemItem(mainHandItem)
            && event.getAction() == Action.RIGHT_CLICK_BLOCK
            && player.isSneaking()) {
          List<AstraTier2EntitySnapshot> entries = this.temporaryEntitySnapshotsByPlayer.get(playerId);
          if (entries != null && !entries.isEmpty()) {
            Location location = event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
            this.sendCachedAbilityFeedback(player, location);
            event.setCancelled(true);
            return;
          }
        }

        if (this.isAbilityAllowed(player)) {
          if (!this.hasRequiredState(playerId)) {
            if ((event.getAction() == Action.RIGHT_CLICK_AIR
                    || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && isGemItem(player.getInventory().getItemInMainHand())
                && !player.isSneaking()
                && !event.hasBlock()) {
              if (this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
                this.teleportAbilityTarget(player);
              } else if (!CooldownService.isOnCooldown(playerId, "astra_projection_t2")) {
                this.sendPrimaryAbilityFeedback(player);
              } else {
                this.sendActiveAbilityFeedback(player, 3);
              }
            }

            if ((event.getAction() == Action.LEFT_CLICK_AIR
                    || event.getAction() == Action.LEFT_CLICK_BLOCK)
                && isGemItem(player.getInventory().getItemInMainHand())
                && !player.isSneaking()) {
              if (this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
                return;
              }

              this.updateBossBar(player);
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void trackAbilityState(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (!this.isAbilityBlocked(player)) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
          ItemStack mainHandItem = player.getInventory().getItemInMainHand();
          ItemStack offHandItem = player.getInventory().getItemInOffHand();
          if (isGemItem(mainHandItem) || isGemItem(offHandItem)) {
            double value = event.getFinalDamage();
            double distance = livingEntity.getHealth();
            if (distance - value <= 0.0
                && java.util.concurrent.ThreadLocalRandom.current().nextDouble() < 0.6) {
              double radius = Math.min(player.getHealth() + 2.5, player.getMaxHealth());
              player.setHealth(radius);
              Location location = livingEntity.getLocation().add(0.0, 1.0, 0.0);
              livingEntity.getWorld()
                  .spawnParticle(
                      Particle.DUST,
                      location,
                      30,
                      0.3,
                      0.3,
                      0.3,
                      new DustOptions(Color.fromRGB(106, 11, 184), 1.0F));
              if (livingEntity instanceof Player) {
                double angle = Math.min(player.getHealth() + 5.0, player.getMaxHealth());
                player.setHealth(angle);
                Location targetLocation = livingEntity.getLocation().add(0.0, 1.0, 0.0);
                livingEntity.getWorld()
                    .spawnParticle(
                        Particle.DUST,
                        targetLocation,
                        30,
                        0.3,
                        0.3,
                        0.3,
                        new DustOptions(Color.fromRGB(106, 11, 184), 1.0F));
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void refreshPlayerState(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (!this.isAbilityBlocked(player)) {
        UUID playerId = player.getUniqueId();
        if (event.getDamager() instanceof Player targetPlayer) {
          ItemStack mainHandItem = player.getInventory().getItemInMainHand();
          ItemStack offHandItem = player.getInventory().getItemInOffHand();
          if ((isGemItem(mainHandItem) || isGemItem(offHandItem))
              && java.util.concurrent.ThreadLocalRandom.current().nextDouble() < 0.05) {
            event.setCancelled(true);
            targetPlayer.sendMessage(
                ChatColor.of("#A01FFF")
                    + "🔮 "
                    + ChatColor.DARK_PURPLE
                    + "Your attack phased through "
                    + player.getName());
            linkedPlayers.put(playerId, player);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                AstraTier2Gem.linkedPlayers.remove(playerId, player);

              }, 20L);
          }
        }
      }
    }
  }

  @EventHandler
  public void onEntityDismount(EntityDismountEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.playerIds.contains(event.getDismounted().getUniqueId())) {
        Horse horse = this.pendingHorsesByPlayer.get(playerId);
        boolean active =
            horse != null && horse.getUniqueId().equals(event.getDismounted().getUniqueId());
        boolean enabled = this.isTrackedTarget(playerId, event.getDismounted());
        if (!this.defaultFlagsByPlayer.getOrDefault(playerId, false) || !active && !enabled) {
          if (active || enabled) {
            this.updateTargetAbilityState(playerId, event.getDismounted() instanceof Horse currentHorse ? currentHorse : horse);
          }
        } else if (CooldownService.isOnCooldown(player.getUniqueId(), "dismount")) {
          event.setCancelled(true);
        } else {
          long timestamp = ConfigValueCache.getInt(this.plugin, "astraT2.dimensionalDriftCooldown", 35);
          if (player.getInventory().contains(Material.DRAGON_EGG)) {
            timestamp = 17L;
          }

          ActiveAbilityStore.startActive(playerId, "astra_drift_t2", 0L, timestamp);
          this.applySourceAbilityEffects(player);
        }
      }
    }
  }

  @EventHandler
  public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    boolean enabled =
        this.defaultFlagsByPlayer.getOrDefault(playerId, false)
            || this.pendingHorsesByPlayer.containsKey(playerId)
            || this.isTrackedTarget(playerId, player.getVehicle());
    if (this.defaultFlagsByPlayer.getOrDefault(playerId, false)) {
      this.applySourceAbilityEffects(player);
    } else if (enabled) {
      this.updateTargetAbilityState(playerId, this.pendingHorsesByPlayer.remove(playerId));
    }

    if (enabled) {
      this.updateSourceAbilityState(playerId);
    }

    if (this.playerLinks.containsKey(playerId)) {
      this.handleAbilityAction(player, false, null);
    }

    UUID targetId = this.activePlayerLinks.get(playerId);
    if (targetId != null) {
      Player targetPlayer = Bukkit.getPlayer(targetId);
      if (targetPlayer != null) {
        this.handleAbilityAction(targetPlayer, false, null);
      } else {
        this.processAbilityState(targetId, playerId);
      }
    }
  }

  @EventHandler
  public void updatePlayerState(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (player.isSneaking()) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          if (!this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
            if (!this.hasRequiredState(playerId)) {
              if (!this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                    || event.getAction() == Action.RIGHT_CLICK_AIR) {
                  ItemStack offHandItem = player.getInventory().getItemInOffHand();
                  ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                  if (isGemItem(offHandItem)) {
                    if (this.secondaryFlagsByPlayer.getOrDefault(playerId, false)) {
                      return;
                    }

                    if (this.isActiveForPlayer(mainHandItem)) {
                      if (CooldownService.isOnCooldown(playerId, "astra_drift_t2")) {
                        this.sendActiveAbilityFeedback(player, 1);
                        return;
                      }

                      this.applyPrimaryAbilityEffects(player);
                      CooldownService.setCooldown(playerId, "dismount", 1L);
                      int configuredValue =
                          ConfigValueCache.getInt(this.plugin, "astraT2.dimDriftActiveSeconds", 10);
                      int index =
                          ConfigValueCache.getInt(this.plugin, "astraT2.dimensionalDriftCooldown", 35);
                      if (player.getInventory().contains(Material.DRAGON_EGG)) {
                        index = 17;
                      }

                      ActiveAbilityStore.startActive(playerId, "astra_drift_t2", configuredValue, index);
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
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    Entity entity = event.getRightClicked();
    if (!this.isAbilityBlocked(player)) {
      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      boolean enabled =
          player.getGameMode() != GameMode.SPECTATOR
              && !this.activeFlagsByPlayer.getOrDefault(playerId, false)
              && !this.previousFlagsByPlayer.getOrDefault(playerId, false);
      if (enabled
          && isGemItem(mainHandItem)
          && player.isSneaking()
          && event.getHand() == EquipmentSlot.HAND) {
        Entity targetEntity = this.resolveEntity(entity);
        if (this.meetsPrimaryCondition(targetEntity)) {
          if (this.meetsTargetCondition(targetEntity)) {
            event.setCancelled(true);
            return;
          }

          if (this.meetsSourceCondition(targetEntity)) {
            event.setCancelled(true);
            return;
          }

          List<AstraTier2EntitySnapshot> entries = this.temporaryEntitySnapshotsByPlayer.get(playerId);
          int count = entries != null ? entries.size() : 0;
          if (count >= 2) {
            player.sendMessage("§cYou can only store 2 entities at a time!");
            event.setCancelled(true);
            return;
          }

          this.sendPendingAbilityFeedback(player, targetEntity);
          event.setCancelled(true);
          return;
        }
      }

      if (this.isValidTarget(player)) {
        if (!this.hasRequiredState(playerId)) {
          if (this.previousFlagsByPlayer.getOrDefault(playerId, false)) {
            if (entity instanceof Player targetPlayer
                && (targetPlayer.getGameMode() == GameMode.CREATIVE
                    || targetPlayer.getGameMode() == GameMode.SPECTATOR)) {
              return;
            }

            if (this.isTrustedPlayer(player, entity)) {
              return;
            }

            this.teleportAbilityTarget(player);
            int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.spookDisableDuration", 10);
            this.applyAbilityEffects(entity.getUniqueId(), configuredValue * 1000L);
            if (entity instanceof Player sourcePlayer) {
              sourcePlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));
            }

            String accentColor = ChatColor.of("#A01FFF").toString();
            String messageColor = ChatColor.of("#befff7").toString();
            String textColor = ChatColor.YELLOW.toString();
            String name = entity.getName();
            String displayColor = ChatColor.of("#befff7").toString();
            String labelColor = ChatColor.YELLOW.toString();
            player.sendMessage(
                accentColor
                    + "🔮 "
                    + messageColor
                    + "Spooked "
                    + textColor
                    + name
                    + displayColor
                    + " for "
                    + labelColor
                    + configuredValue
                    + "s");
          } else if (!CooldownService.isOnCooldown(playerId, "astra_projection_t2")
              && !player.isSneaking()) {
            this.sendTargetAbilityFeedback(player, entity.getLocation());
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (event.isSneaking() && this.playerLinks.containsKey(playerId)) {
      this.handleAbilityAction(player, true, null);
    } else if (this.isValidTarget(player)) {
      if (event.isSneaking() && this.defaultFlagsByPlayer.getOrDefault(playerId, false)) {
        this.applySourceAbilityEffects(player);
      }

      if (this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
        for (Entity entity : player.getNearbyEntities(1.0, 1.0, 1.0)) {
          if (entity instanceof Player targetPlayer && targetPlayer != player) {
            if (targetPlayer.getGameMode() != GameMode.CREATIVE
                && targetPlayer.getGameMode() != GameMode.SPECTATOR
                && !this.isTrustedPlayer(player, entity)) {
              int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.hauntDisableDuration", 30);
              this.applyAbilityEffects(entity.getUniqueId(), configuredValue * 1000L);
              targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0));
              String accentColor = ChatColor.of("#A01FFF").toString();
              String messageColor = ChatColor.of("#befff7").toString();
              String textColor = ChatColor.YELLOW.toString();
              String name = entity.getName();
              String displayColor = ChatColor.of("#befff7").toString();
              String labelColor = ChatColor.YELLOW.toString();
              player.sendMessage(
                  accentColor
                      + "🔮 "
                      + messageColor
                      + "You haunted and disabled "
                      + textColor
                      + name
                      + displayColor
                      + " gem for "
                      + labelColor
                      + configuredValue
                      + "s");
              this.teleportAbilityTarget(player);
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      if (this.trackedZombiesByPlayer.containsValue(event.getEntity())) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) {
          UUID playerId = null;

          for (Entry<UUID,Zombie> entry : this.trackedZombiesByPlayer.entrySet()) {
            if ((entry.getValue()).equals(event.getEntity())) {
              playerId = entry.getKey();
              break;
            }
          }

          if (playerId != null) {
            Zombie zombie = this.trackedZombiesByPlayer.get(playerId);
            if (zombie != null && !zombie.isDead()) {
              Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                  if (!zombie.isDead()) {
                    zombie.setHealth(zombie.getMaxHealth());
                  }

                }, 1L);
            }
          }

          return;
        }

        UUID targetId = null;

        for (Entry<UUID,Zombie> currentEntry : this.trackedZombiesByPlayer.entrySet()) {
          if ((currentEntry.getValue()).equals(event.getEntity())) {
            targetId = currentEntry.getKey();
            break;
          }
        }

        if (targetId != null) {
          Player player = Bukkit.getPlayer(targetId);
          if (player != null && player.isOnline()) {
            Zombie currentZombie = this.trackedZombiesByPlayer.get(targetId);
            if (currentZombie != null && !currentZombie.isDead()) {
              Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                  if (!currentZombie.isDead()) {
                    currentZombie.setHealth(currentZombie.getMaxHealth());
                  }

                }, 1L);
            }

            this.teleportAbilityTarget(player);
            player.sendMessage(
                ChatColor.of("#A01FFF")
                    + "🔮 "
                    + ChatColor.RED
                    + "Your astral projection was disrupted!");
            Entity entity = damageEvent.getDamager();
            if (entity instanceof Player targetPlayer) {
              if (!targetPlayer.getUniqueId().equals(targetId)) {
                targetPlayer.sendMessage(
                    ChatColor.of("#A01FFF")
                        + "🔮 "
                        + ChatColor.of("#befff7")
                        + "You disrupted "
                        + player.getName()
                        + "'s astral projection!");
              }
            } else if (entity instanceof Projectile projectile
                && projectile.getShooter() instanceof Player sourcePlayer) {
              if (!sourcePlayer.getUniqueId().equals(targetId)) {
                sourcePlayer.sendMessage(
                    ChatColor.of("#A01FFF")
                        + "🔮 "
                        + ChatColor.of("#befff7")
                        + "You disrupted "
                        + player.getName()
                        + "'s astral projection!");
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void finishAbilityAction(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (this.isValidTarget(player) && this.previousFlagsByPlayer.getOrDefault(player.getUniqueId(), false)) {
        if (!(event.getEntity() instanceof Player targetPlayer)) {
          return;
        }
        if (targetPlayer.getGameMode() == GameMode.CREATIVE || targetPlayer.getGameMode() == GameMode.SPECTATOR) {
          event.setCancelled(true);
          return;
        }

        if (this.isTrustedPlayer(player, event.getEntity())) {
          event.setCancelled(true);
          return;
        }

        event.setCancelled(true);
        this.teleportAbilityTarget(player);
        int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.spookPunchDisableDuration", 15);
        this.applyAbilityEffects(targetPlayer.getUniqueId(), configuredValue * 1000L);
        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));
        String accentColor = ChatColor.of("#A01FFF").toString();
        String messageColor = ChatColor.of("#befff7").toString();
        String textColor = ChatColor.YELLOW.toString();
        String name = targetPlayer.getName();
        String displayColor = ChatColor.of("#befff7").toString();
        String labelColor = ChatColor.YELLOW.toString();
        player.sendMessage(
            accentColor
                + "🔮 "
                + messageColor
                + "Spooked "
                + textColor
                + name
                + displayColor
                + " for "
                + labelColor
                + configuredValue
                + "s");
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    Player player = event.getPlayer();
    this.teleportAbilityTarget(event.getPlayer());
    this.activateCachedAbility(event.getPlayer());
    this.updateCachedAbilityState(playerId);
    if (this.playerLinks.containsKey(player.getUniqueId())) {
      this.handleAbilityAction(player, false, null);
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    lastUpdateTimes.remove(playerId);
  }

  void applyPrimaryAbilityEffects(Player player) {
    if (!this.isAbilityBlocked(player)) {
      UUID playerId = player.getUniqueId();
      if (this.isAbilityAllowed(player)) {
        if (this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
          this.sendActiveAbilityFeedback(player, 1);
        } else {
          this.targetPlayers.put(playerId, player);
          if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            this.playerCounters.put(
                player.getUniqueId(),
                player.getPotionEffect(PotionEffectType.INVISIBILITY).getDuration());
            this.chargeLevels.put(
                player.getUniqueId(),
                player.getPotionEffect(PotionEffectType.INVISIBILITY).getAmplifier());
          }

          player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
          this.activatePrimaryAbility(player);

          try {
            player.sendMessage(
                ChatColor.of("#A01FFF")
                    + "🔮 "
                    + ChatColor.of("#FF686F")
                    + "You used "
                    + ChatColor.of("#C7C7C7")
                    + "Dimensional Drift");
            this.defaultFlagsByPlayer.put(playerId, true);
            Location location = player.getLocation();
            Horse mount = (Horse) player.getWorld().spawnEntity(location, EntityType.HORSE);
            this.pendingHorsesByPlayer.put(playerId, mount);
            this.updatePrimaryAbilityState(playerId, mount);
            mount.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.35);
            mount.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0.9);
            mount.getAttribute(Attribute.MAX_HEALTH).setBaseValue(53.0);
            mount.setSilent(true);
            mount.setHealth(53.0);
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1, false, false));
            mount.setOwner(player);
            mount.setInvisible(true);
            mount.setInvulnerable(true);
            mount.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                if (!mount.isDead() && player.isOnline()) {
                  mount.addPassenger(player);
                }

              }, 3L);
            this.secondaryFlagsByPlayer.put(playerId, true);
            this.activateTargetAbility(player);
            this.activateSourceAbility(player);
          } catch (Exception exception) {
            this.plugin.getLogger()
                .log(
                    java.util.logging.Level.WARNING,
                    "Could not restore astral-projection state",
                    exception);
          }
        }
      }
    }
  }

  @EventHandler
  public void onEntityMount(EntityMountEvent event) {
    if (event.getMount() instanceof Horse horse) {
      if (event.getEntity() instanceof Player player) {
        UUID playerId = null;

        for (Entry<UUID,Horse> entry : this.pendingHorsesByPlayer.entrySet()) {
          if (entry.getValue() != null
              && (entry.getValue()).getUniqueId().equals(horse.getUniqueId())) {
            playerId = entry.getKey();
            break;
          }
        }

        if (playerId != null) {
          if (!player.getUniqueId().equals(playerId)) {
            event.setCancelled(true);
          }
        }
      }
    }
  }

  void activatePrimaryAbility(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.cooldownTasks.containsKey(playerId)) {
      this.cooldownTasks.get(playerId).cancel();
    }

    BukkitTask scheduledTask =
        new BukkitRunnable() {
          @Override
          public void run() {
            if (!AstraTier2Gem.this.defaultFlagsByPlayer.getOrDefault(playerId, false)) {
              this.cancel();
              AstraTier2Gem.this.cooldownTasks.remove(playerId);
            } else if (!player.isOnline()) {
              this.cancel();
              AstraTier2Gem.this.cooldownTasks.remove(playerId);
            } else {
              Location location = player.getLocation().add(0.0, 2.0, 0.0);
              player
              .getWorld()
              .spawnParticle(
                  Particle.DUST, location, 3, new DustOptions(Color.fromRGB(106, 11, 184), 1.0F));
            }
          }
        }
            .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
    this.cooldownTasks.put(playerId, scheduledTask);
  }

  void activateTargetAbility(Player player) {
    UUID playerId = player.getUniqueId();
    ItemStack item = player.getInventory().getHelmet();
    if (item != null && item.getType() != Material.AIR) {
      this.trackedItemsByPlayer.put(playerId, item.clone());
      player.getInventory().setHelmet(null);
    }

    ItemStack heldItem = player.getInventory().getChestplate();
    if (heldItem != null && heldItem.getType() != Material.AIR) {
      this.itemsByPlayer.put(playerId, heldItem.clone());
      player.getInventory().setChestplate(null);
    }

    ItemStack targetItem = player.getInventory().getLeggings();
    if (targetItem != null && targetItem.getType() != Material.AIR) {
      this.storedItemsByPlayer.put(playerId, targetItem.clone());
      player.getInventory().setLeggings(null);
    }

    ItemStack candidateItem = player.getInventory().getBoots();
    if (candidateItem != null && candidateItem.getType() != Material.AIR) {
      this.currentItemsByPlayer.put(playerId, candidateItem.clone());
      player.getInventory().setBoots(null);
    }
  }

  void activateSourceAbility(Player player) {
    UUID playerId = player.getUniqueId();
    int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.dimensionalDriftDuration", 10) * 20;
    int count = configuredValue / 5;
    BukkitTask task = this.scheduledTasks.remove(playerId);
    if (task != null && !task.isCancelled()) {
      task.cancel();
    }

    new BukkitRunnable() {
      @Override
      public void run() {
        if (AstraTier2Gem.this.defaultFlagsByPlayer.getOrDefault(playerId, false)) {
          AstraTier2Gem.this.applyTargetAbilityEffects(player);
          AstraTier2Gem.this.defaultFlagsByPlayer.put(playerId, false);
          Bukkit.getScheduler().runTaskLater(AstraTier2Gem.this.plugin, () -> {
                AstraTier2Gem.this.secondaryFlagsByPlayer.put(playerId, false);

              }, 60L);
        }
      }
    }.runTaskLater(this.plugin, configuredValue);
  }

  void applyTargetAbilityEffects(Player player) {
    UUID playerId = player.getUniqueId();
    player.removePotionEffect(PotionEffectType.INVISIBILITY);
    if (this.playerCounters.containsKey(player.getUniqueId())) {
      player.addPotionEffect(
          new PotionEffect(
              PotionEffectType.INVISIBILITY,
              this.playerCounters.get(player.getUniqueId()),
              this.chargeLevels.get(player.getUniqueId()),
              true,
              true,
              true));
      this.playerCounters.remove(player.getUniqueId());
      this.chargeLevels.remove(player.getUniqueId());
    }

    this.activateActiveAbility(player);
    ItemStack item = this.trackedItemsByPlayer.remove(playerId);
    ItemStack heldItem = this.itemsByPlayer.remove(playerId);
    ItemStack targetItem = this.storedItemsByPlayer.remove(playerId);
    ItemStack candidateItem = this.currentItemsByPlayer.remove(playerId);
    if (item != null) {
      player.getInventory().setHelmet(item);
    }

    if (heldItem != null) {
      player.getInventory().setChestplate(heldItem);
    }

    if (targetItem != null) {
      player.getInventory().setLeggings(targetItem);
    }

    if (candidateItem != null) {
      player.getInventory().setBoots(candidateItem);
    }

    this.targetPlayers.remove(playerId, player);
    this.updateTargetAbilityState(playerId, this.pendingHorsesByPlayer.remove(playerId));
  }

  void applySourceAbilityEffects(Player player) {
    UUID playerId = player.getUniqueId();
    player.removePotionEffect(PotionEffectType.INVISIBILITY);
    if (this.playerCounters.containsKey(player.getUniqueId())) {
      player.addPotionEffect(
          new PotionEffect(
              PotionEffectType.INVISIBILITY,
              this.playerCounters.get(player.getUniqueId()),
              this.chargeLevels.get(player.getUniqueId()),
              true,
              true,
              true));
      this.playerCounters.remove(player.getUniqueId());
      this.chargeLevels.remove(player.getUniqueId());
    }

    this.activateActiveAbility(player);
    this.targetPlayers.remove(playerId, player);
    this.updateTargetAbilityState(playerId, this.pendingHorsesByPlayer.remove(playerId));
    this.activatePendingAbility(player);
    this.defaultFlagsByPlayer.put(playerId, false);
    int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.dimensionalDriftCooldown", 35);
    if (player.getInventory().contains(Material.DRAGON_EGG)) {
      configuredValue /= 2;
    }

    CooldownService.setCooldown(playerId, "astra_drift_t2", configuredValue);
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.secondaryFlagsByPlayer.put(playerId, false), 60L);
  }

  void activateActiveAbility(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.cooldownTasks.containsKey(playerId)) {
      this.cooldownTasks.get(playerId).cancel();
      this.cooldownTasks.remove(playerId);
    }
  }

  void activatePendingAbility(Player player) {
    UUID playerId = player.getUniqueId();
    ItemStack item = this.trackedItemsByPlayer.remove(playerId);
    ItemStack heldItem = this.itemsByPlayer.remove(playerId);
    ItemStack targetItem = this.storedItemsByPlayer.remove(playerId);
    ItemStack candidateItem = this.currentItemsByPlayer.remove(playerId);
    if (item != null) {
      player.getInventory().setHelmet(item);
    }

    if (heldItem != null) {
      player.getInventory().setChestplate(heldItem);
    }

    if (targetItem != null) {
      player.getInventory().setLeggings(targetItem);
    }

    if (candidateItem != null) {
      player.getInventory().setBoots(candidateItem);
    }
  }

  public boolean canAffectTarget(UUID playerId) {
    return this.secondaryFlagsByPlayer.getOrDefault(playerId, false);
  }

  public void activateCachedAbility(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.defaultFlagsByPlayer.getOrDefault(playerId, false)) {
      this.defaultFlagsByPlayer.put(playerId, false);
      this.applyTargetAbilityEffects(player);
    }
  }

  void updatePrimaryAbilityState(UUID playerId, Horse horse) {
    horse.addScoreboardTag(this.formatDisplayText(playerId));
    horse.setPersistent(false);
  }

  String formatDisplayText(UUID playerId) {
    return "bliss_astra_t2_drift_horse_" + playerId;
  }

  boolean isTrackedTarget(UUID playerId, Entity entity) {
    return entity instanceof Horse
        && entity.getScoreboardTags().contains(this.formatDisplayText(playerId));
  }

  void updateTargetAbilityState(UUID playerId, Horse horse) {
    String text = this.formatDisplayText(playerId);
    HashSet<UUID> trackedIds = new HashSet<>();
    if (horse != null) {
      trackedIds.add(horse.getUniqueId());
      this.updatePrimaryState(horse);
    }

    for (World world : Bukkit.getWorlds()) {
      for (Horse currentHorse : world.getEntitiesByClass(Horse.class)) {
        if (currentHorse.getScoreboardTags().contains(text) && trackedIds.add(currentHorse.getUniqueId())) {
          this.updatePrimaryState(currentHorse);
        }
      }
    }
  }

  void updatePrimaryState(Horse horse) {
    this.playerIds.add(horse.getUniqueId());
    horse.setInvulnerable(false);
    horse.remove();
    Bukkit.getScheduler()
        .runTaskLater(this.plugin, () -> this.forgetDriftHorse(horse), 1L);
  }

  void updateSourceAbilityState(UUID playerId) {
    Bukkit.getScheduler()
        .runTaskLater(this.plugin, () -> this.finishPrimaryDriftHorseCleanup(playerId), 1L);
    Bukkit.getScheduler()
        .runTaskLater(this.plugin, () -> this.finishDriftHorseCleanup(playerId), 5L);
  }

  NPC resolveNPC(Player player, Location location) {
    Location targetLocation = location.clone();
    if (targetLocation.getY() < 1.0) {
      targetLocation.setY(targetLocation.getWorld().getHighestBlockYAt(targetLocation) + 1);
    }

    NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, player.getName());
    npc.data().set(Metadata.SHOULD_SAVE, false);
    npc.data().set("bliss_astra_blur_owner", player.getUniqueId().toString());
    this.fallbackNpcsByPlayer.put(player.getUniqueId(), npc);
    npc.spawn(targetLocation);
    SkinTrait skinTrait = (SkinTrait) npc.getOrAddTrait(SkinTrait.class);
    skinTrait.setSkinName(player.getName());
    LookClose lookClose = (LookClose) npc.getOrAddTrait(LookClose.class);
    lookClose.lookClose(true);
    lookClose.setRange(10.0);
    ItemStack item = player.getInventory().getHelmet();
    ItemStack heldItem = player.getInventory().getChestplate();
    ItemStack targetItem = player.getInventory().getLeggings();
    ItemStack candidateItem = player.getInventory().getBoots();
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    Bukkit.getScheduler()
        .runTaskLater(
            this.plugin, () -> configureAstralNpc(npc, item, heldItem, targetItem, candidateItem, mainHandItem), 2L);
    return npc;
  }

  void sendPrimaryAbilityFeedback(Player player) {
    if (!this.isAbilityBlocked(player)) {
      UUID playerId = player.getUniqueId();
      if (this.defaultFlagsByPlayer.getOrDefault(playerId, false)) {
        this.applySourceAbilityEffects(player);
      }

      if (this.activeDisplaysByPlayer.containsKey(playerId)) {
        this.updatePrimaryBossBar(playerId);
        int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.daggersCooldown", 80);
        if (player.getInventory().contains(Material.DRAGON_EGG)) {
          configuredValue /= 2;
        }

        CooldownService.setCooldown(playerId, "astra_daggers_t2", configuredValue);
      }

      if (this.isAbilityAllowed(player)) {
        boolean enabled = this.isProtectedTarget(player);
        int index = ConfigValueCache.getInt(this.plugin, "astraT2.astralProjectionCooldown", 300);
        if (player.getInventory().contains(Material.DRAGON_EGG)) {
          index /= 2;
        }

        CooldownService.setCooldown(playerId, "astra_projection_t2", index);
        this.activeFlagsByPlayer.put(playerId, true);
        this.previousFlagsByPlayer.put(playerId, true);
        this.playerLocations.put(playerId, player.getLocation().clone());
        this.targetGameModesByPlayer.put(playerId, player.getGameMode());
        player.sendMessage(
            ChatColor.of("#A01FFF")
                + "🔮 "
                + ChatColor.of("#befff7")
                + "You have activated "
                + ChatColor.GRAY
                + "Astral Projection");
        ItemStack[] item = new ItemStack[36];
        ItemStack[] heldItem = new ItemStack[4];

        for (int count = 0; count < 36; count++) {
          ItemStack targetItem = player.getInventory().getItem(count);
          if (targetItem != null) {
            item[count] = targetItem.clone();
          }
        }

        heldItem[0] =
            player.getInventory().getHelmet() != null
                ? player.getInventory().getHelmet().clone()
                : null;
        heldItem[1] =
            player.getInventory().getChestplate() != null
                ? player.getInventory().getChestplate().clone()
                : null;
        heldItem[2] =
            player.getInventory().getLeggings() != null
                ? player.getInventory().getLeggings().clone()
                : null;
        heldItem[3] =
            player.getInventory().getBoots() != null ? player.getInventory().getBoots().clone() : null;
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (offHandItem != null && offHandItem.getType() != Material.AIR) {
          this.cachedItemsByPlayer.put(playerId, offHandItem.clone());
        }

        this.resolvedInventorySnapshotsByPlayer.put(playerId, item);
        this.primaryInventorySnapshotsByPlayer.put(playerId, heldItem);
        player.setGameMode(GameMode.SPECTATOR);
        player.setWalkSpeed(0.35F);
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack candidateItem = player.getInventory().getItemInOffHand();
        boolean active = isGemItem(mainHandItem);
        boolean allowed = isGemItem(candidateItem);

        for (int remaining = 0; remaining < 36; remaining++) {
          player.getInventory().setItem(remaining, null);
        }

        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        if (active) {
          player.getInventory().setItemInMainHand(mainHandItem.clone());
          if (candidateItem != null && candidateItem.getType() != Material.AIR && !allowed) {
            player.getInventory().setItemInOffHand(candidateItem.clone());
          }
        } else if (allowed) {
          player.getInventory().setItemInMainHand(candidateItem.clone());
        }

        if (enabled) {
          this.resolveNPC(player, player.getLocation());
        } else {
          this.resolveNPC(player, player.getLocation());
        }

        this.activePlayers.add(player.getUniqueId());
      }
    }
  }

  void applyActiveAbilityEffects(Player player) {
    UUID playerId = player.getUniqueId();
    if (!this.trackedZombiesByPlayer.containsKey(playerId)) {
      Location location = this.playerLocations.get(playerId);
      if (location != null) {
        location = location.clone();
        if (!location.getBlock().getType().isSolid()) {
          location.setY(location.getY());
        }

        Zombie zombie = null;

        try {
          zombie = (Zombie) player.getWorld().spawnEntity(location, EntityType.ZOMBIE);
          zombie.setCustomNameVisible(false);
          zombie.setSilent(true);
          zombie.setAI(false);
          zombie.setGravity(true);
          zombie.setInvulnerable(false);
          zombie.setAdult();
          zombie.setRemoveWhenFarAway(false);
          zombie.setPersistent(true);
          zombie.setCanPickupItems(false);
          zombie.setShouldBurnInDay(false);
          zombie.setTarget(null);
          zombie.setAware(false);
          zombie.addPotionEffect(
              new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false));
          zombie.addPotionEffect(
              new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 255, false, false));
          zombie.addPotionEffect(
              new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 255, false, false));
          ItemStack[] item = this.resolvedInventorySnapshotsByPlayer.get(playerId);
          ItemStack[] heldItem = this.primaryInventorySnapshotsByPlayer.get(playerId);
          ItemStack targetItem = this.cachedItemsByPlayer.get(playerId);
          if (zombie.getEquipment() != null) {
            if (heldItem != null) {
              zombie.getEquipment().setHelmet(heldItem[0]);
              zombie.getEquipment().setChestplate(heldItem[1]);
              zombie.getEquipment().setLeggings(heldItem[2]);
              zombie.getEquipment().setBoots(heldItem[3]);
            }

            if (item != null && item.length > 0) {
              ItemStack candidateItem = null;

              for (int count = 0; count < Math.min(9, item.length); count++) {
                if (item[count] != null && isGemItem(item[count])) {
                  candidateItem = item[count];
                  break;
                }
              }

              if (candidateItem != null) {
                zombie.getEquipment().setItemInMainHand(candidateItem);
              } else {
                for (ItemStack secondaryItem : item) {
                  if (secondaryItem != null && secondaryItem.getType() != Material.AIR) {
                    zombie.getEquipment().setItemInMainHand(secondaryItem);
                    break;
                  }
                }
              }
            }

            if (targetItem != null) {
              zombie.getEquipment().setItemInOffHand(targetItem);
            }

            zombie.getEquipment().setHelmetDropChance(0.0F);
            zombie.getEquipment().setChestplateDropChance(0.0F);
            zombie.getEquipment().setLeggingsDropChance(0.0F);
            zombie.getEquipment().setBootsDropChance(0.0F);
            zombie.getEquipment().setItemInMainHandDropChance(0.0F);
            zombie.getEquipment().setItemInOffHandDropChance(0.0F);
          }

          this.trackedZombiesByPlayer.put(playerId, zombie);
        } catch (Exception exception) {
          this.plugin.getLogger()
              .log(java.util.logging.Level.WARNING, "Could not create an Astra illusion", exception);
          this.teleportPrimaryAbilityTarget(player);
        }
      }
    }
  }

  void updateTargetState(NPC npc) {
    if (npc != null) {
      NPCRegistry npcRegistry = npc.getOwningRegistry();
      if (npc.isSpawned()) {
        npc.despawn();
      }

      npc.data().set(Metadata.SHOULD_SAVE, false);
      npc.destroy();
      if (npcRegistry != null) {
        npcRegistry.saveToStore();
      }
    }
  }

  void teleportAbilityTarget(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
      Location location = this.playerLocations.get(playerId);
      if (location == null) {
        location = player.getWorld().getSpawnLocation();
      } else {
        location = location.clone();
      }

      this.activeFlagsByPlayer.remove(playerId);
      this.previousFlagsByPlayer.remove(playerId);
      this.activePlayers.remove(playerId);
      GameMode gameMode = this.targetGameModesByPlayer.remove(playerId);
      player.setGameMode(gameMode != null ? gameMode : GameMode.SURVIVAL);
      player.teleport(location);
      player.setWalkSpeed(0.2F);
      this.playerLocations.remove(playerId);
      this.cooldownTimestamps.remove(playerId);
      Zombie zombie = this.trackedZombiesByPlayer.remove(playerId);
      if (zombie != null && !zombie.isDead()) {
        zombie.remove();
      }

      ItemStack[] item = this.resolvedInventorySnapshotsByPlayer.remove(playerId);
      ItemStack[] heldItem = this.primaryInventorySnapshotsByPlayer.remove(playerId);
      if (item != null) {
        player.getInventory().clear();

        for (int count = 0; count < 36; count++) {
          if (item[count] != null) {
            player.getInventory().setItem(count, item[count]);
          }
        }
      }

      if (heldItem != null) {
        player.getInventory().setHelmet(heldItem[0]);
        player.getInventory().setChestplate(heldItem[1]);
        player.getInventory().setLeggings(heldItem[2]);
        player.getInventory().setBoots(heldItem[3]);
      }

      ItemStack targetItem = this.cachedItemsByPlayer.remove(playerId);
      if (targetItem != null) {
        player.getInventory().setItemInOffHand(targetItem);
      }

      player.updateInventory();
      int configuredValue =
          ConfigValueCache.getInt(this.plugin, "astraT2.astralProjectionDeactivationCooldown", 120);
      if (player.getInventory().contains(Material.DRAGON_EGG)) {
        configuredValue /= 2;
      }

      CooldownService.setCooldown(playerId, "astra_projection_t2", configuredValue);
      new BukkitRunnable() {
        @Override
        public void run() {
          NPC npc = AstraTier2Gem.this.fallbackNpcsByPlayer.remove(playerId);
          if (npc != null) {
            AstraTier2Gem.this.updateTargetState(npc);
          }
        }
      }.runTaskLater(this.plugin, 5L);
    }
  }

  void teleportPrimaryAbilityTarget(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
      this.activePlayers.remove(playerId);
      Location location = this.playerLocations.get(playerId);
      if (location == null) {
        location = player.getWorld().getSpawnLocation();
      } else {
        location = location.clone();
      }

      this.activeFlagsByPlayer.remove(playerId);
      this.previousFlagsByPlayer.remove(playerId);
      GameMode gameMode = this.targetGameModesByPlayer.remove(playerId);
      player.setGameMode(gameMode != null ? gameMode : GameMode.SURVIVAL);
      player.teleport(location);
      player.setWalkSpeed(0.2F);
      this.playerLocations.remove(playerId);
      this.cooldownTimestamps.remove(playerId);
      Zombie zombie = this.trackedZombiesByPlayer.remove(playerId);
      if (zombie != null && !zombie.isDead()) {
        zombie.remove();
      }

      ItemStack[] item = this.resolvedInventorySnapshotsByPlayer.remove(playerId);
      ItemStack[] heldItem = this.primaryInventorySnapshotsByPlayer.remove(playerId);
      if (item != null) {
        player.getInventory().clear();

        for (int count = 0; count < 36; count++) {
          if (item[count] != null) {
            player.getInventory().setItem(count, item[count]);
          }
        }
      }

      if (heldItem != null) {
        player.getInventory().setHelmet(heldItem[0]);
        player.getInventory().setChestplate(heldItem[1]);
        player.getInventory().setLeggings(heldItem[2]);
        player.getInventory().setBoots(heldItem[3]);
      }

      ItemStack targetItem = this.cachedItemsByPlayer.remove(playerId);
      if (targetItem != null) {
        player.getInventory().setItemInOffHand(targetItem);
      }

      player.updateInventory();
      int configuredValue =
          ConfigValueCache.getInt(this.plugin, "astraT2.astralProjectionDeactivationCooldown", 120);
      if (player.getInventory().contains(Material.DRAGON_EGG)) {
        configuredValue /= 2;
      }

      CooldownService.setCooldown(playerId, "astra_projection_t2", configuredValue);
      new BukkitRunnable() {
        @Override
        public void run() {
          NPC npc = AstraTier2Gem.this.fallbackNpcsByPlayer.remove(playerId);
          if (npc != null) {
            AstraTier2Gem.this.updateTargetState(npc);
          }
        }
      }.runTaskLater(this.plugin, 5L);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (this.players.containsKey(playerId) || this.activeDisplaysByPlayer.containsKey(playerId)) {
        if (event.getClickedInventory() == player.getInventory()) {
          ItemStack item = event.getCurrentItem();
          if (item != null && isGemItem(item)) {
            if (event.getAction() == InventoryAction.PICKUP_ALL
                || event.getAction() == InventoryAction.PICKUP_HALF
                || event.getAction() == InventoryAction.PICKUP_ONE
                || event.getAction() == InventoryAction.PICKUP_SOME
                || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
              this.activateResolvedAbility(player);
              ActiveAbilityStore.startActive(playerId, "astra_daggers_t2", 0L, 0L);
              Bukkit.getScheduler()
                  .runTask(this.plugin, () -> this.finishInventoryUpdate(playerId, player));
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPrimaryEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player && this.targetPlayers.containsValue(player)) {
      event.setCancelled(true);
    }
  }

  int getAbilityIntValue(Player player) {
    int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.daggersCooldown", 80);
    if (player.getInventory().contains(Material.DRAGON_EGG)) {
      configuredValue /= 2;
    }

    return configuredValue;
  }

  void activateResolvedAbility(Player player) {
    UUID playerId = player.getUniqueId();
    int count = this.getAbilityIntValue(player);
    long now = System.currentTimeMillis();
    long timestamp = now + count * 1000L;
    Long storedTimestamp = this.startTimes.get(playerId);
    if (storedTimestamp == null || storedTimestamp <= now) {
      this.startTimes.put(playerId, timestamp);
      CooldownService.setCooldown(playerId, "astra_daggers_t2", count);
    }
  }

  void initializePrimaryState() {
    long now = System.currentTimeMillis();
    this.startTimes.entrySet().removeIf(entry -> shouldPrimaryRemoveExpiredEntry(now, entry));
  }

  void updateBossBar(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.isValidTarget(player)) {
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          if (this.activeDisplaysByPlayer.containsKey(playerId)) {
            double configuredValue = ConfigValueCache.getDouble(this.plugin, "astraT2.daggerShootCooldown", 1.0);
            long timestamp = (long) (configuredValue * 1000.0);
            Long storedTimestamp = this.lastActivationTimes.get(playerId);
            long now = System.currentTimeMillis();
            if (storedTimestamp != null && now - storedTimestamp < timestamp) {
              long lastUpdateTime = timestamp - (now - storedTimestamp);
              double value = lastUpdateTime / 1000.0;
              String accentColor = ChatColor.of("#A01FFF").toString();
              String red = ChatColor.RED.toString();
              String text = String.format("Wait %.1fs before shooting next dagger!", value);
              player.sendMessage(accentColor + "🔮 " + red + text);
            } else {
              this.playAbilitySound(player);
              this.lastActivationTimes.put(playerId, now);
            }
          } else if (CooldownService.isOnCooldown(playerId, "astra_daggers_t2")) {
            this.sendActiveAbilityFeedback(player, 2);
          } else {
            this.players.put(playerId, player);
            this.sourceFlagsByPlayer.put(playerId, false);
            player.sendMessage(
                ChatColor.of("#A01FFF")
                    + "🔮 "
                    + ChatColor.of("#befff7")
                    + "You activated "
                    + ChatColor.GRAY
                    + "Phantom Daggers");
            this.activateResolvedAbility(player);
            ActiveAbilityStore.startActive(playerId, "astra_daggers_t2", 50L, 0L);
            ArrayList<ItemDisplay> entries = new ArrayList<>();
            Location location = player.getLocation().clone().add(0.0, 1.0, 0.0);
            Vector direction = location.getDirection().normalize();
            Vector offset = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
            Location[] targetLocation =
                new Location[] {
                  location.clone().add(direction.clone().multiply(0.8)).add(offset.clone().multiply(-1)),
                  location.clone().add(direction.clone().multiply(0.8)).add(offset.clone().multiply(1)),
                  location.clone().add(offset.clone().multiply(-1.2)),
                  location.clone().add(offset.clone().multiply(1.2)),
                  location.clone().add(direction.clone().multiply(1.2))
                };

            for (int count = 0; count < 5; count++) {
              Location origin = targetLocation[count].clone().add(0.0, 0.45, 0.0);
              ItemDisplay display =
                  (ItemDisplay) player.getWorld().spawnEntity(origin, EntityType.ITEM_DISPLAY);
              display.setItemStack(this.createAbilityItem());
              display.setGravity(false);
              display.setPersistent(false);
              display.setInterpolationDuration(1);
              display.setInterpolationDelay(0);
              display.setTeleportDuration(1);
              display.setBillboard(Billboard.FIXED);
              display.setBrightness(new Brightness(15, 15));
              display.setTransformation(this.resolveTransformation());
              entries.add(display);
              player.addPassenger(display);
            }

            this.activeDisplaysByPlayer.put(playerId, entries);
            BossBar bossBar =
                Bukkit.createBossBar(
                    ChatColor.WHITE + "Daggers", BarColor.BLUE, BarStyle.SOLID);
            bossBar.addPlayer(player);
            bossBar.setProgress(1.0);
            this.bossBars.put(playerId, bossBar);
            this.abilityStages.put(playerId, 0);
            this.startTargetBackgroundTasks();
          }
        }
      }
    }
  }

  ItemStack createAbilityItem() {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setCustomModelData(230);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  void playAbilitySound(Player player) {
    UUID playerId = player.getUniqueId();
    if (!this.isValidTarget(player)) {
      this.updatePrimaryBossBar(playerId);
    } else {
      List<ItemDisplay> entries = this.activeDisplaysByPlayer.get(playerId);
      if (entries != null && !entries.isEmpty()) {
        ItemDisplay itemDisplay = null;
        int count = -1;

        for (int index = 4; index >= 0; index--) {
          if (index < entries.size()
              && entries.get(index) != null
              && !(entries.get(index)).isDead()) {
            itemDisplay = entries.get(index);
            count = index;
            break;
          }
        }

        if (itemDisplay == null) {
          this.updatePrimaryBossBar(playerId);
        } else {
          Location location = player.getEyeLocation();
          Location targetLocation = location.clone().add(location.getDirection().multiply(1.5));
          location.getWorld().playSound(location, Sound.ITEM_TRIDENT_THROW, 0.5F, 1.5F);
          targetLocation.setY(targetLocation.getY() - 0.3);
          itemDisplay.teleport(targetLocation);
          entries.set(count, null);
          this.activateStoredAbility(player, itemDisplay);
          boolean enabled = false;

          for (ItemDisplay currentItemDisplay : entries) {
            if (currentItemDisplay != null && !currentItemDisplay.isDead()) {
              enabled = true;
              break;
            }
          }

          int remaining = enabled ? 50 : 0;
          ActiveAbilityStore.startActive(playerId, "astra_daggers_t2", remaining, this.getAbilityIntValue(player));
          if (!enabled) {
            BossBar bossBar = this.bossBars.remove(playerId);
            if (bossBar != null) {
              bossBar.removeAll();
            }

            int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.daggerDuration", 50) * 20;
            Bukkit.getScheduler()
                .runTaskLater(this.plugin, () -> this.finishDaggerCooldown(playerId), configuredValue);
          }
        }
      }
    }
  }

  void activateStoredAbility(Player player, ItemDisplay itemDisplay) {
    UUID playerId = player.getUniqueId();
    Vector direction = player.getLocation().getDirection().normalize();
    this.cachedDaggersByPlayer.put(itemDisplay.getUniqueId(), new MovingDagger(playerId, itemDisplay, direction));
    this.startPrimaryBackgroundTasks();
  }

  void startPrimaryBackgroundTasks() {
    if (this.activeScheduledTask == null || this.activeScheduledTask.isCancelled()) {
      this.activeScheduledTask =
          Bukkit.getScheduler()
              .runTaskTimer(this.plugin, this::tickMovingDaggers, 1L, 1L);
    }
  }

  @EventHandler
  public void onPluginDisable(PluginDisableEvent event) {
    if (event.getPlugin().equals(Bliss.getInstance())) {
      this.shutdown();
    }
  }

  public void shutdown() {
    if (this.scheduledTask != null) {
      this.scheduledTask.cancel();
      this.scheduledTask = null;
    }
    if (this.secondaryScheduledTask != null) {
      this.secondaryScheduledTask.cancel();
      this.secondaryScheduledTask = null;
    }
    if (this.activeScheduledTask != null) {
      this.activeScheduledTask.cancel();
      this.activeScheduledTask = null;
    }
    this.cooldownTasks.values().forEach(BukkitTask::cancel);
    this.scheduledTasks.values().forEach(BukkitTask::cancel);
    this.animationTasks.values().forEach(BukkitTask::cancel);

    Set<UUID> affectedPlayers = new HashSet<>();
    affectedPlayers.addAll(this.trackedZombiesByPlayer.keySet());
    affectedPlayers.addAll(this.activeDisplaysByPlayer.keySet());
    affectedPlayers.addAll(this.pendingHorsesByPlayer.keySet());
    affectedPlayers.addAll(this.temporaryEntitySnapshotsByPlayer.keySet());
    affectedPlayers.addAll(this.bossBars.keySet());
    affectedPlayers.addAll(this.players.keySet());
    affectedPlayers.addAll(this.playerLinks.keySet());

    for (Player player : Bukkit.getOnlinePlayers()) {
      this.teleportAbilityTarget(player);
      this.activateCachedAbility(player);
      if (this.playerLinks.containsKey(player.getUniqueId())) {
        this.handleAbilityAction(player, false, null);
      }
      affectedPlayers.add(player.getUniqueId());
    }
    for (UUID playerId : affectedPlayers) {
      this.updateCachedAbilityState(playerId);
    }

    this.bossBars.values().forEach(BossBar::removeAll);
    this.bossBars.clear();
    for (MovingDagger movingDagger : this.cachedDaggersByPlayer.values()) {
      if (movingDagger.itemDisplay != null && !movingDagger.itemDisplay.isDead()) {
        movingDagger.itemDisplay.remove();
      }
    }
    this.cachedDaggersByPlayer.clear();
    for (List<ItemDisplay> displays : this.activeDisplaysByPlayer.values()) {
      for (ItemDisplay display : displays) {
        if (display != null && !display.isDead()) {
          display.remove();
        }
      }
      displays.clear();
    }
    this.activeDisplaysByPlayer.clear();
    this.trackedZombiesByPlayer.values().forEach(Entity::remove);
    this.trackedZombiesByPlayer.clear();
    this.pendingHorsesByPlayer.values().forEach(Entity::remove);
    this.pendingHorsesByPlayer.clear();

    for (NPC npc : new ArrayList<>(this.fallbackNpcsByPlayer.values())) {
      if (npc != null) {
        this.updateTargetState(npc);
      }
    }
    this.fallbackNpcsByPlayer.clear();

    this.cooldownTasks.clear();
    this.scheduledTasks.clear();
    this.animationTasks.clear();
    this.playerLinks.clear();
    this.pendingGameModesByPlayer.clear();
    this.activePlayerLinks.clear();
    this.temporaryEntitySnapshotsByPlayer.clear();
    this.pendingLastUseTimes.clear();
    this.players.clear();
    this.abilityStages.clear();
    this.expiryTimes.clear();
    this.startTimes.clear();
    this.defaultFlagsByPlayer.clear();
    this.secondaryFlagsByPlayer.clear();
    this.playerIds.clear();
    this.activePlayers.clear();
    lastUpdateTimes.clear();
    linkedPlayers.clear();
    primaryLastUseTimes.clear();
    remainingTicks.clear();
  }

  void initializeTargetState() {
    if (!this.expiryTimes.isEmpty()) {
      long now = System.currentTimeMillis();
      Iterator<Map.Entry<UUID,Long>> iterator = this.expiryTimes.entrySet().iterator();

      while (iterator.hasNext()) {
        Entry<UUID, Long> entry = iterator.next();
        if (now >= entry.getValue()) {
          remainingTicks.remove(entry.getKey());
          iterator.remove();
        }
      }
    }
  }

  Transformation resolveTransformation() {
    return new Transformation(
        new Vector3f(0.0F, 0.0F, 0.0F),
        new AxisAngle4f((float) Math.toRadians(90.0), 1.0F, 0.0F, 0.0F),
        new Vector3f(1.0F, 1.0F, 1.0F),
        new AxisAngle4f((float) Math.toRadians(90.0), 0.0F, 1.0F, 0.0F));
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    UUID playerId = player.getUniqueId();
    UUID targetId = this.secondaryPlayerLinks.get(playerId);
    if (targetId != null) {
      Player targetPlayer = Bukkit.getPlayer(targetId);
      if (targetPlayer != null) {
        String textColor = ChatColor.WHITE.toString();
        String name = player.getName();
        String messageColor = ChatColor.WHITE.toString();
        String displayColor = ChatColor.WHITE.toString();
        String label = targetPlayer.getName();
        String configColor = ChatColor.WHITE.toString();
        event.setDeathMessage(
            textColor + name + messageColor + " was pierced by " + displayColor + label + configColor + "'s daggers");
      } else {
        event.setDeathMessage(
            ChatColor.WHITE
                + player.getName()
                + ChatColor.WHITE
                + " was pierced by "
                + ChatColor.WHITE
                + " daggers");
      }

      this.secondaryPlayerLinks.remove(playerId);
    }
  }

  public static int getDurationTicks(Player player) {
    return player == null ? 0 : remainingTicks.getOrDefault(player.getUniqueId(), 0);
  }

  void startTargetBackgroundTasks() {
    if (this.secondaryScheduledTask == null || this.secondaryScheduledTask.isCancelled()) {
      this.secondaryScheduledTask =
          Bukkit.getScheduler().runTaskTimer(this.plugin, this::tickDaggerDisplays, 1L, 1L);
    }
  }

  void teleportAbilityTargetGroup(Player player, List<ItemDisplay> entries) {
    Location location = player.getLocation().clone().add(0.0, 1.0, 0.0);
    Vector direction = location.getDirection().normalize();
    Vector offset = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
    Location[] targetLocation =
        new Location[] {
          location.clone().add(direction.clone().multiply(1.8)).add(offset.clone().multiply(-2)),
          location.clone().add(direction.clone().multiply(1.8)).add(offset.clone().multiply(2)),
          location.clone().add(offset.clone().multiply(-2)),
          location.clone().add(offset.clone().multiply(2)),
          location.clone().add(direction.clone().multiply(2))
        };

    for (int count = 0; count < entries.size(); count++) {
      ItemDisplay itemDisplay = entries.get(count);
      if (itemDisplay != null && !itemDisplay.isDead()) {
        Location origin = targetLocation[count].add(0.0, 0.45, 0.0);
        itemDisplay.teleport(origin);
      }
    }
  }

  void updateActiveAbilityState(UUID playerId) {
    this.cachedDaggersByPlayer.entrySet().removeIf(entry -> shouldRemoveExpiredEntry(playerId, entry));
    this.expiryTimes.remove(playerId);
  }

  void updatePendingAbilityState(UUID playerId) {
    List<ItemDisplay> entries = this.activeDisplaysByPlayer.remove(playerId);
    this.players.remove(playerId);
    this.abilityStages.remove(playerId);
    if (entries != null) {
      for (ItemDisplay itemDisplay : entries) {
        if (itemDisplay != null && !itemDisplay.isDead()) {
          itemDisplay.remove();
        }
      }
    }

    this.updateActiveAbilityState(playerId);
  }

  void updatePrimaryBossBar(UUID playerId) {
    List<ItemDisplay> entries = this.activeDisplaysByPlayer.remove(playerId);
    this.players.remove(playerId);
    this.abilityStages.remove(playerId);
    if (entries != null) {
      for (ItemDisplay itemDisplay : entries) {
        if (itemDisplay != null && !itemDisplay.isDead()) {
          itemDisplay.remove();
        }
      }
    }

    this.updateActiveAbilityState(playerId);
    BossBar bossBar = this.bossBars.remove(playerId);
    if (bossBar != null) {
      bossBar.removeAll();
    }

    this.sourceFlagsByPlayer.remove(playerId);
  }

  @EventHandler
  public void onServerLoad(ServerLoadEvent event) {
    this.initialize();
  }

  void sendTargetAbilityFeedback(Player player, Location location) {
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityBlocked(player)) {
      if (this.isValidTarget(player)) {
        int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.astralVoidDuration", 100);
        int index =
            ConfigValueCache.getInt(this.plugin, "astraT2.astralProjectionDeactivationCooldown", 120);
        if (player.getInventory().contains(Material.DRAGON_EGG)) {
          index /= 2;
        }

        ActiveAbilityStore.startActive(playerId, "astra_projection_t2", configuredValue, index);
        CooldownService.setCooldown(playerId, "astra_projection_t2", index);
        player.sendMessage(
            ChatColor.of("#A01FFF")
                + "🔮 "
                + ChatColor.of("#befff7")
                + "You have activated "
                + ChatColor.GRAY
                + "Astral Void");
        int remaining = ConfigValueCache.getInt(this.plugin, "astraT2.astralVoidRadius", 6);
        Location targetLocation = location.clone().add(0.0, 1.4, 0.0);
        World world = targetLocation.getWorld();
        if (world != null) {
          DustOptions dustOptions = new DustOptions(Color.fromRGB(106, 11, 184), 1.15F);
          DustOptions dust = new DustOptions(Color.fromRGB(170, 45, 255), 1.35F);
          final Location effectCenter = location;
          new BukkitRunnable() {
            final int durationTicks = configuredValue * 20;
            final Player caster = player;
            final Location particleOrigin = targetLocation;
            final Location targetCenter = effectCenter;
            final int radius = remaining;
            final World effectWorld = world;
            final DustOptions innerDust = dustOptions;
            final DustOptions outerDust = dust;
            int elapsedTicks = 0;

            @Override
            public void run() {
              if (this.elapsedTicks >= this.durationTicks
                  || !this.caster.isOnline()
                  || this.particleOrigin.getWorld() == null) {
                this.cancel();
                return;
              }

              for (Player nearbyPlayer :
                  this.targetCenter.getNearbyPlayers(this.radius, this.radius, this.radius)) {
                if (!nearbyPlayer.equals(this.caster)
                    && !AstraTier2Gem.this.isTrustedPlayer(this.caster, nearbyPlayer)
                    && !AstraTier2Gem.this.isAbilityBlocked(nearbyPlayer)) {
                  AstraTier2Gem.updateAbilityState(nearbyPlayer.getUniqueId(), 1000L);
                }
              }

              double phase = this.elapsedTicks * 0.18;
              AstraTier2Gem.this.spawnAbilityParticles(
                  this.effectWorld,
                  this.particleOrigin,
                  this.radius,
                  this.innerDust,
                  this.outerDust,
                  phase);
              AstraTier2Gem.this.spawnPrimaryAbilityParticles(
                  this.effectWorld, this.particleOrigin, this.radius + 0.45, phase);
              if (this.elapsedTicks % 3 == 0) {
                this.effectWorld.spawnParticle(
                    Particle.PORTAL,
                    this.particleOrigin,
                    12,
                    this.radius * 0.25,
                    this.radius * 0.25,
                    this.radius * 0.25,
                    0.02);
              }

              this.elapsedTicks += 3;
            }
          }.runTaskTimer(Bliss.getInstance(), 0L, 3L);
        }
      }
    }
  }

  void spawnAbilityParticles(
      World world, Location location, double value, DustOptions dustOptions, DustOptions dust, double distance) {
    byte step = 11;
    byte phase = 34;
    double radius = distance * 0.7;
    double angle = distance * 0.35;

    for (int count = 0; count <= step; count++) {
      double progress = -1.0 + 2.0 * count / step;
      double scale = Math.sqrt(1.0 - progress * progress) * value;
      double amount = progress * value;

      for (int index = 0; index < phase; index++) {
        if ((index + count) % 2 == 0) {
          double speed = (Math.PI * 2) / phase * index + distance + count * 0.33;
          double offset = Math.cos(speed) * scale;
          double height = Math.sin(speed) * scale;
          Vector direction = this.getDirection(offset, amount, height, radius, angle);
          DustOptions dustStyle = count % 3 == 0 ? dust : dustOptions;
          world.spawnParticle(
              Particle.DUST,
              location.getX() + direction.getX(),
              location.getY() + direction.getY(),
              location.getZ() + direction.getZ(),
              1,
              0.0,
              0.0,
              0.0,
              0.0,
              dustStyle);
        }
      }
    }

    byte stepIndex = 60;

    for (int remaining = 0; remaining < stepIndex; remaining++) {
      double width = remaining * 0.55;
      double stepSize = width + distance * 1.35;
      double damage = Math.acos(1.0 - 2.0 * ((double) remaining / stepIndex));
      double knockback = Math.sin(damage) * Math.cos(stepSize) * value;
      double xOffset = Math.cos(damage) * value;
      double yOffset = Math.sin(damage) * Math.sin(stepSize) * value;
      Vector velocity =
          this.getDirection(knockback, xOffset, yOffset, radius * 0.75, angle * 1.2);
      world.spawnParticle(
          Particle.DUST,
          location.getX() + velocity.getX(),
          location.getY() + velocity.getY(),
          location.getZ() + velocity.getZ(),
          1,
          0.0,
          0.0,
          0.0,
          0.0,
          dust);
    }
  }

  void spawnPrimaryAbilityParticles(World world, Location location, double value, double distance) {
    byte step = 7;
    byte phase = 52;
    double[] radius = new double[] {25.0, 45.0, 65.0, 85.0, 105.0, 125.0, 145.0};
    double[] angle = new double[] {0.0, 0.7, 1.4, 2.1, 2.8, 3.5, 4.2};
    double[] progress = new double[] {0.012, -0.016, 0.014, -0.011, 0.018, -0.013, 0.015};

    for (int count = 0; count < step; count++) {
      double angleRadians = Math.toRadians(radius[count]);
      double scale = angle[count] + distance * progress[count];
      double amount = value + 0.1 + count % 3 * 0.055;
      double offset = Math.sin(angleRadians) * Math.cos(scale);
      double speed = Math.cos(angleRadians);
      double height = Math.sin(angleRadians) * Math.sin(scale);
      double width = Math.abs(speed) > 0.92 ? 1.0 : 0.0;
      double stepSize = Math.abs(speed) > 0.92 ? 0.0 : 1.0;
      double damage = 0.0;
      double projection = width * offset + stepSize * speed + damage * height;
      double basisX = width - projection * offset;
      double basisY = stepSize - projection * speed;
      double basisZ = damage - projection * height;
      double basisLength = Math.sqrt(basisX * basisX + basisY * basisY + basisZ * basisZ);
      if (!(basisLength <= 1.0E-4)) {
        basisX /= basisLength;
        basisY /= basisLength;
        basisZ /= basisLength;
        double tangentX = speed * basisZ - height * basisY;
        double tangentY = height * basisX - offset * basisZ;
        double tangentZ = offset * basisY - speed * basisX;
        double phaseOffset = angle[count] * 2.3 + distance * (0.035 + count * 0.004);

        for (int index = 0; index < phase; index++) {
          byte cycleLength = 13;
          byte visibleSteps = 6;
          int cycleStep = (index + (int) (distance * (0.25 + count * 0.03))) % cycleLength;
          if (cycleStep < visibleSteps) {
            double orbitAngle = (Math.PI * 2) / phase * index;
            double radiusWobble =
                Math.sin(orbitAngle * 3.0 + phaseOffset) * 0.075
                    + Math.sin(orbitAngle * 7.0 - phaseOffset * 0.7) * 0.035;
            double orbitRadius = amount + radiusWobble;
            double rotationOffset = distance * progress[count] * 3.5;
            double cos = Math.cos(orbitAngle + rotationOffset);
            double sin = Math.sin(orbitAngle + rotationOffset);
            double particleX = (cos * basisX + sin * tangentX) * orbitRadius;
            double particleY = (cos * basisY + sin * tangentY) * orbitRadius;
            double particleZ = (cos * basisZ + sin * tangentZ) * orbitRadius;
            double axialWobble = Math.sin(orbitAngle * 5.0 + phaseOffset) * 0.045;
            particleX += offset * axialWobble;
            particleY += speed * axialWobble;
            particleZ += height * axialWobble;
            world.spawnParticle(
                Particle.END_ROD,
                location.getX() + particleX,
                location.getY() + particleY,
                location.getZ() + particleZ,
                1,
                0.0,
                0.0,
                0.0,
                0.0);
          }
        }
      }
    }
  }

  Vector getDirection(
      double value, double distance, double radius, double angle, double progress) {
    double offset = Math.cos(angle);
    double scale = Math.sin(angle);
    double amount = value * offset - radius * scale;
    double speed = value * scale + radius * offset;
    double height = Math.cos(progress);
    double width = Math.sin(progress);
    double stepSize = distance * height - speed * width;
    double damage = distance * width + speed * height;
    return new Vector(amount, stepSize, damage);
  }

  Vector getDirectionForPlayer(
      double value, double distance, double radius, double angle, double progress, double scale) {
    double offset = Math.cos(angle);
    double amount = Math.sin(angle);
    double speed = value * offset - radius * amount;
    double height = value * amount + radius * offset;
    double width = Math.cos(progress);
    double stepSize = Math.sin(progress);
    double damage = distance * width - height * stepSize;
    double knockback = distance * stepSize + height * width;
    double xOffset = Math.cos(scale);
    double yOffset = Math.sin(scale);
    double zOffset = speed * xOffset - damage * yOffset;
    double maxDistance = speed * yOffset + damage * xOffset;
    return new Vector(zOffset, maxDistance, knockback);
  }

  void spawnTargetAbilityParticles(Player player) {
    Location location = player.getLocation();
    DustOptions dustOptions = new DustOptions(Color.fromRGB(106, 11, 184), 1.0F);
    Location[] targetLocation =
        new Location[] {
          location.clone().add(0.5, 1.2, 0.5),
          location.clone().add(-0.5, 1.2, -0.5),
          location.clone().add(-0.5, 1.2, 0.5),
          location.clone().add(0.5, 1.2, -0.5),
          location.clone().add(0.3, 0.4, 0.3),
          location.clone().add(-0.3, 0.4, -0.3),
          location.clone().add(-0.3, 0.4, 0.3),
          location.clone().add(0.3, 0.4, -0.3),
          location.clone().add(0.3, 1.6, 0.3),
          location.clone().add(-0.3, 1.6, -0.3),
          location.clone().add(-0.3, 1.6, 0.3),
          location.clone().add(0.3, 1.6, -0.3),
          location
        };

    for (Location origin : targetLocation) {
      player.getWorld().spawnParticle(Particle.DUST, origin, 3, dustOptions);
    }
  }

  void spawnSourceAbilityParticles(Location location, double value, Color color) {
    DustOptions dustOptions = new DustOptions(color, 1.0F);

    for (int count = 0; count < 32; count++) {
      double distance = (Math.PI * 2) * count / 32.0;
      double offset = location.getX() + value * Math.cos(distance);
      double radius = location.getZ() + value * Math.sin(distance);
      Location effectLocation = new Location(location.getWorld(), offset, location.getY(), radius);
      location.getWorld().spawnParticle(Particle.DUST, effectLocation, 1, dustOptions);
    }
  }

  void sendSourceAbilityFeedback(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.hasRequiredState(playerId)) {
      long now = (lastUpdateTimes.get(playerId) - System.currentTimeMillis()) / 1000L;
      String darkGray = ChatColor.DARK_GRAY.toString();
      String bold = ChatColor.BOLD.toString();
      ActionBarQueue.enqueue(
          player,
          "🔒 "
              + darkGray
              + bold
              + "ᴅɪѕᴀʙʟᴇᴅ: "
              + now);
    } else if (this.shouldApplyEffect(playerId)) {
      String displayColor = ChatColor.DARK_GRAY.toString();
      String textColor = ChatColor.BOLD.toString();
      ActionBarQueue.enqueue(
          player,
          "🔒 " + displayColor + textColor + "ᴅɪѕᴀʙʟᴇᴅ");
    } else if (this.previousFlagsByPlayer.getOrDefault(playerId, false)) {
        if (!this.isAbilityActive(playerId)) {
          Long storedTimestamp = this.cooldownTimestamps.get(playerId);
          if (storedTimestamp != null) {
            long lastUpdateTime = (storedTimestamp - System.currentTimeMillis()) / 1000L;
            String nameColor = ChatColor.AQUA.toString();
            String accentColor = ChatColor.of("#A01FFF").toString();
            String configColor = ChatColor.WHITE.toString();
            String keyColor = ChatColor.AQUA.toString();
            String red = ChatColor.RED.toString();
            long startTime = lastUpdateTime + 1L;
            ActionBarQueue.enqueue(
                player,
                "🧊 "
                    + nameColor
                    + "SPOOK "
                    + accentColor
                    + "🔮 "
                    + configColor
                    + "🌟 "
                    + keyColor
                    + "TAG "
                    + red
                    + " - Body: "
                    + startTime
                    + "s");
          } else {
            String normalizedColor = ChatColor.AQUA.toString();
            String baseColor = ChatColor.of("#A01FFF").toString();
            String titleColor = ChatColor.WHITE.toString();
            String subtitleColor = ChatColor.AQUA.toString();
            ActionBarQueue.enqueue(
                player,
                "🧊 "
                    + normalizedColor
                    + "SPOOK "
                    + baseColor
                    + "🔮 "
                    + titleColor
                    + "🌟 "
                    + subtitleColor
                    + "TAG");
          }
        } else {
          String readyColor = ChatColor.AQUA.toString();
          String accentColor = ChatColor.of("#A01FFF").toString();
          String labelColor = ChatColor.WHITE.toString();
          ActionBarQueue.enqueue(
              player,
              "🧊 "
                  + readyColor
                  + "SPOOK "
                  + accentColor
                  + "🔮 "
                  + labelColor
                  + "🌟 "
                  + readyColor
                  + "TAG");
        }
      } else {
        String driftStatus = this.formatDisplayTextForPlayer(playerId, 1);
        String daggersStatus = this.formatDisplayTextForPlayer(playerId, 2);
        String projectionStatus = this.formatDisplayTextForPlayer(playerId, 3);
        String aqua = ChatColor.AQUA.toString();
        String accentColor = ChatColor.of("#A01FFF").toString();
        String abilityColor = ChatColor.DARK_PURPLE.toString();
        String separatorColor = ChatColor.WHITE.toString();
        ActionBarQueue.enqueue(
            player,
            "🔪 "
                + aqua
                + daggersStatus
                + " "
                + accentColor
                + "🔮 "
                + abilityColor
                + driftStatus
                + " "
                + separatorColor
                + "👻 "
                + aqua
                + projectionStatus);
      }
    
  }

  String formatDisplayTextForPlayer(UUID playerId, int count) {
    String text;
    switch (count) {
      case 1:
        text = "astra_drift_t2";
        break;
      case 2:
        text = "astra_daggers_t2";
        break;
      case 3:
        text = "astra_projection_t2";
        break;
      default:
        return ChatColor.GREEN + "Ready!";
    }

    return CooldownService.isOnCooldown(playerId, text)
        ? AbilityStatusFormatter.formatDisplayText(playerId, text, false)
        : ChatColor.GREEN + "Ready!";
  }

  void sendActiveAbilityFeedback(Player player, int count) {
    String text;
    String message;
    switch (count) {
      case 1:
        text = "Dimensional Drift";
        message = this.formatDisplayTextForPlayer(player.getUniqueId(), 1);
        break;
      case 2:
        text = "Phantom Daggers";
        message = this.formatDisplayTextForPlayer(player.getUniqueId(), 2);
        break;
      case 3:
        text = "Astral Projection";
        message = this.formatDisplayTextForPlayer(player.getUniqueId(), 3);
        break;
      default:
        return;
    }

    if (!ActiveAbilityStore.isActive(player.getUniqueId(), "astra_daggers_t2")) {
      String accentColor = ChatColor.of("#A01FFF").toString();
      String displayColor = ChatColor.of("#FF686F").toString();
      String textColor = ChatColor.of("#C7C7C7").toString();
      String labelColor = ChatColor.of("#FF686F").toString();
      String nameColor = ChatColor.of("#A01FFF").toString();
      player.sendMessage(
          accentColor
              + "🔮 "
              + displayColor
              + "Your "
              + textColor
              + text
              + labelColor
              + " skill is on cooldown for "
              + nameColor
              + message);
    }
  }

  void updateCachedAbilityState(UUID playerId) {
    this.temporaryEntitySnapshotsByPlayer.remove(playerId);
    this.pendingLastUseTimes.remove(playerId);
    lastUpdateTimes.remove(playerId);
    this.activeFlagsByPlayer.remove(playerId);
    this.nextFlagsByPlayer.remove(playerId);
    this.previousFlagsByPlayer.remove(playerId);
    this.playerLocations.remove(playerId);
    this.cooldownTimestamps.remove(playerId);
    this.lastActivationTimes.remove(playerId);
    this.sourceFlagsByPlayer.remove(playerId);
    this.resolvedInventorySnapshotsByPlayer.remove(playerId);
    this.primaryInventorySnapshotsByPlayer.remove(playerId);
    this.cachedItemsByPlayer.remove(playerId);
    this.activeLastUseTimes.remove(playerId);
    this.lastUseTimes.remove(playerId);
    this.targetLastUseTimes.remove(playerId);
    this.defaultFlagsByPlayer.remove(playerId);
    this.secondaryFlagsByPlayer.remove(playerId);
    this.trackedItemsByPlayer.remove(playerId);
    this.itemsByPlayer.remove(playerId);
    this.storedItemsByPlayer.remove(playerId);
    this.currentItemsByPlayer.remove(playerId);
    this.sourceLastUseTimes.remove(playerId);
    Zombie zombie = this.trackedZombiesByPlayer.remove(playerId);
    if (zombie != null) {
      zombie.remove();
    }

    this.updateTargetAbilityState(playerId, this.pendingHorsesByPlayer.remove(playerId));
    this.updatePendingAbilityState(playerId);
    CooldownService.setCooldown(playerId, "astra_drift_t2", 0L);
    CooldownService.setCooldown(playerId, "astra_daggers_t2", 0L);
    CooldownService.setCooldown(playerId, "astra_projection_t2", 0L);
  }

  @EventHandler
  public void onPrimaryPlayerTeleport(PlayerTeleportEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
      if (!this.isValidTarget(player)) {
        this.teleportPrimaryAbilityTarget(player);
        return;
      }

      if (event.getCause() == TeleportCause.SPECTATE) {
        event.setCancelled(true);
        player.setSpectatorTarget(null);
        player.sendMessage(
            ChatColor.of("#A01FFF")
                + "🔮 "
                + ChatColor.RED
                + "You cannot teleport to players during Astral Projection!");
        return;
      }

      if (event.getCause() == TeleportCause.PLUGIN) {
        Zombie zombie = this.trackedZombiesByPlayer.get(playerId);
        if (zombie != null && event.getTo() != null) {
          Location location = zombie.getLocation();
          if (event.getTo() != null
              && location.getWorld() != null
              && event.getTo().getWorld() != null
              && location.getWorld().equals(event.getTo().getWorld())
              && location.distanceSquared(event.getTo()) < 25.0) {
            return;
          }
        }

        event.setCancelled(true);
        return;
      }

      Location targetLocation = this.playerLocations.get(playerId);
      int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.astralProjectionRange", 150);
      if (targetLocation != null && event.getTo() != null) {
        if (targetLocation.getWorld() == null
            || event.getTo().getWorld() == null
            || !targetLocation.getWorld().equals(event.getTo().getWorld())) {
          event.setCancelled(true);
          player.sendMessage(
              ChatColor.of("#A01FFF")
                  + "🔮 "
                  + ChatColor.RED
                  + "You cannot go beyond "
                  + configuredValue
                  + " blocks from your body!");
          return;
        }

        if (targetLocation.distanceSquared(event.getTo()) > configuredValue * configuredValue) {
          event.setCancelled(true);
          player.sendMessage(
              ChatColor.of("#A01FFF")
                  + "🔮 "
                  + ChatColor.RED
                  + "You cannot go beyond "
                  + configuredValue
                  + " blocks from your body!");
          return;
        }
      }

      if (event.getCause() == TeleportCause.COMMAND
          || event.getCause() == TeleportCause.ENDER_PEARL
          || event.getCause() == TeleportCause.CONSUMABLE_EFFECT) {
        event.setCancelled(true);
        player.sendMessage(
            ChatColor.of("#A01FFF")
                + "🔮 "
                + ChatColor.RED
                + "You cannot use teleportation during Astral Projection!");
        return;
      }
    }
  }

  @EventHandler
  public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    if (this.previousFlagsByPlayer.getOrDefault(event.getPlayer().getUniqueId(), false)) {
      if (this.isValidTarget(event.getPlayer())) {
        event.setCancelled(true);
      } else {
        this.teleportPrimaryAbilityTarget(event.getPlayer());
      }
    }
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    if (this.trackedZombiesByPlayer.containsValue(event.getEntity())) {
      event.setCancelled(true);

      for (Entry<UUID,Zombie> entry : this.trackedZombiesByPlayer.entrySet()) {
        if ((entry.getValue()).equals(event.getEntity())) {
          Player player = Bukkit.getPlayer(entry.getKey());
          if (player != null) {
            this.teleportAbilityTarget(player);
          }
          break;
        }
      }
    }
  }

  @EventHandler
  public void onPlayerItemHeld(PlayerItemHeldEvent event) {
    Player player = event.getPlayer();
    if (player.getGameMode() != GameMode.SPECTATOR) {
      UUID playerId = player.getUniqueId();
      if (this.activeDisplaysByPlayer.containsKey(playerId) || this.players.containsKey(playerId)) {
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (!isGemItem(item)) {
          this.activateResolvedAbility(player);
          ActiveAbilityStore.startActive(playerId, "astra_daggers_t2", 0L, 0L);
          this.updatePrimaryBossBar(playerId);
          this.lastActivationTimes.remove(playerId);
        }
      }
    }
  }

  boolean isConditionMet(Entity entity) {
    return entity instanceof Wither
        || entity instanceof Warden
        || entity instanceof FallingBlock
        || entity instanceof EnderDragon;
  }

  boolean meetsPrimaryCondition(Entity entity) {
    return entity instanceof LivingEntity
        && !(entity instanceof Player)
        && !this.isConditionMet(entity);
  }

  String formatDisplayTextForTarget(Entity entity) {
    return entity.getCustomName() != null
        ? entity.getCustomName()
        : entity.getType().name().toLowerCase().replace("_", " ");
  }

  void sendPendingAbilityFeedback(Player player, Entity entity) {
    UUID playerId = player.getUniqueId();
    Location location = entity.getLocation();
    if (!this.meetsPrimaryCondition(entity)) {
      player.sendMessage(ChatColor.of("#A01FFF") + "Â§cYou can only capture living mobs!");
    } else {
      List<AstraTier2EntitySnapshot> entries = this.temporaryEntitySnapshotsByPlayer.getOrDefault(playerId, new ArrayList<>());
      if (!this.activeFlagsByPlayer.getOrDefault(playerId, false)) {
        if (entries.size() >= 2) {
          player.sendMessage(
              ChatColor.of("#A01FFF")
                  + "🔮 §cYou can only store 2 entities at a time!");
        } else if (this.meetsTargetCondition(entity)) {
          player.sendMessage(
              ChatColor.of("#A01FFF")
                  + "🔮 §cYou cannot capture vehicles with players inside!");
        } else if (this.meetsSourceCondition(entity)) {
          player.sendMessage(
              ChatColor.of("#A01FFF")
                  + "🔮 §cYou cannot capture vehicles with boss mobs inside!");
        } else {
          AstraTier2EntitySnapshot astraTier2EntitySnapshot = new AstraTier2EntitySnapshot(entity);
          entries.add(astraTier2EntitySnapshot);
          this.temporaryEntitySnapshotsByPlayer.put(playerId, entries);
          this.cache.put(playerId, System.currentTimeMillis());
          this.spawnActiveAbilityParticles(
              location, Particle.DUST, new DustOptions(Color.fromRGB(138, 43, 226), 1.0F));
          String text = this.formatDisplayTextForTarget(entity);
          this.updateSourceState(entity);
          player.sendMessage(
              ChatColor.of("#A01FFF")
                  + "🔮 §dYou have captured "
                  + text
                  + " "
                  + entries.size()
                  + "/2");
        }
      }
    }
  }

  Entity resolveEntity(Entity entity) {
    Entity targetEntity = entity;

    while (targetEntity.isInsideVehicle()) {
      Entity sourceEntity = targetEntity.getVehicle();
      if (sourceEntity == null || sourceEntity instanceof Player) {
        break;
      }

      targetEntity = sourceEntity;
    }

    return targetEntity;
  }

  boolean meetsTargetCondition(Entity entity) {
    for (Entity targetEntity : entity.getPassengers()) {
      if (targetEntity instanceof Player) {
        return true;
      }

      if (this.meetsTargetCondition(targetEntity)) {
        return true;
      }
    }

    return false;
  }

  boolean meetsSourceCondition(Entity entity) {
    for (Entity targetEntity : entity.getPassengers()) {
      if (this.isConditionMet(targetEntity)) {
        return true;
      }

      if (this.meetsSourceCondition(targetEntity)) {
        return true;
      }
    }

    return false;
  }

  void updateSourceState(Entity entity) {
    for (Entity targetEntity : new ArrayList<>(entity.getPassengers())) {
      this.updateSourceState(targetEntity);
    }

    entity.eject();
    entity.remove();
  }

  @EventHandler
  public void onNPCDamageByEntity(NPCDamageByEntityEvent event) {
    NPC npc = event.getNPC();
    if (npc != null) {
      UUID playerId = null;

      for (Entry<UUID,NPC> entry : this.fallbackNpcsByPlayer.entrySet()) {
        NPC currentNPC = entry.getValue();
        if (currentNPC != null && currentNPC.getId() == npc.getId()) {
          playerId = entry.getKey();
          break;
        }
      }

      if (playerId != null) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
          this.teleportAbilityTarget(player);
          player.sendMessage(
              ChatColor.of("#A01FFF")
                  + "🔮 "
                  + ChatColor.RED
                  + "Your astral projection was disrupted!");
        }

        NPC targetNPC = this.fallbackNpcsByPlayer.remove(playerId);
        if (targetNPC != null) {
          this.updateTargetState(targetNPC);
        }

        if (event.getDamager() instanceof Player targetPlayer
            && player != null
            && !targetPlayer.getUniqueId().equals(playerId)) {
          targetPlayer.sendMessage(
              ChatColor.of("#A01FFF")
                  + "🔮 "
                  + ChatColor.of("#befff7")
                  + "You disrupted "
                  + player.getName()
                  + "'s astral projection!");
        }

        event.setCancelled(true);
      }
    }
  }

  void sendCachedAbilityFeedback(Player player, Location location) {
    UUID playerId = player.getUniqueId();
    List<AstraTier2EntitySnapshot> entries = this.temporaryEntitySnapshotsByPlayer.get(playerId);
    if (entries != null && !entries.isEmpty()) {
      AstraTier2EntitySnapshot astraTier2EntitySnapshot = entries.remove(entries.size() - 1);
      if (entries.isEmpty()) {
        this.temporaryEntitySnapshotsByPlayer.remove(playerId);
      } else {
        this.temporaryEntitySnapshotsByPlayer.put(playerId, entries);
      }

      Entity entity = astraTier2EntitySnapshot.resolveEntity(location);
      if (entity == null) {
        player.sendMessage(ChatColor.of("#A01FFF") + "🔮 §cError spawning entity!");
      } else {
        this.spawnActiveAbilityParticles(location, Particle.DUST, new DustOptions(Color.fromRGB(138, 43, 226), 1.0F));
        this.cache.put(playerId, System.currentTimeMillis());
        int count = entries.size();
        String accentColor = ChatColor.of("#A01FFF").toString();
        String text = astraTier2EntitySnapshot.displayText;
        player.sendMessage(
            accentColor + "🔮 §dYou released " + text + " " + count + "/2");
      }
    } else {
      player.sendMessage(
          ChatColor.of("#A01FFF") + "🔮 §cYou don't have any stored entities!");
    }
  }

  void spawnActiveAbilityParticles(Location location, Particle particle, DustOptions dustOptions) {
    byte step = 100;

    for (int count = 0; count < step; count++) {
      double value = Math.acos(1.0 - 2.0 * count / step);
      double distance = Math.PI * (1.0 + Math.sqrt(5.0)) * count;
      double offset = Math.cos(distance) * Math.sin(value) * 0.4;
      double radius = Math.sin(distance) * Math.sin(value) * 0.4;
      double angle = Math.cos(value) * 0.4;
      Location targetLocation = location.clone().add(offset, radius + 1.0, angle);
      location.getWorld().spawnParticle(particle, targetLocation, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
    }
  }

  void updateActiveState(Location location, Particle particle) {
    this.spawnActiveAbilityParticles(location, particle, new DustOptions(Color.fromRGB(138, 43, 226), 1.0F));
  }

  public void activateTrackedAbility(Player player) {
    UUID playerId = player.getUniqueId();
  }

  static boolean shouldRemoveExpiredEntry(UUID playerId, Entry<UUID, MovingDagger> entry) {
    MovingDagger movingDagger = entry.getValue();
    if (movingDagger.playerId.equals(playerId)) {
      if (movingDagger.itemDisplay != null && !movingDagger.itemDisplay.isDead()) {
        movingDagger.itemDisplay.remove();
      }

      return true;
    } else {
      return false;
    }
  }

  void tickDaggerDisplays() {
    this.initializeTargetState();
    if (this.players.isEmpty()) {
      this.secondaryScheduledTask.cancel();
      this.secondaryScheduledTask = null;
    } else {
      int configuredValue = ConfigValueCache.getInt(this.plugin, "astraT2.daggerDuration", 50) * 20;
      Iterator<Map.Entry<UUID,Player>> iterator = this.players.entrySet().iterator();

      while (iterator.hasNext()) {
        Entry<UUID, Player> entry = iterator.next();
        UUID playerId = entry.getKey();
        Player player = entry.getValue();
        int count = this.abilityStages.getOrDefault(playerId, 0) + 1;
        this.abilityStages.put(playerId, count);
        if (player != null && player.isOnline() && this.isValidTarget(player)) {
          BossBar bossBar = this.bossBars.get(playerId);
          if (bossBar != null) {
            double value = 1.0 - (double) count / configuredValue;
            bossBar.setProgress(Math.max(0.0, value));
          }

          List<ItemDisplay> entries = this.activeDisplaysByPlayer.get(playerId);
          if (entries == null) {
            this.updatePrimaryBossBar(playerId);
            iterator.remove();
          } else {
            this.teleportAbilityTargetGroup(player, entries);
            if (count >= configuredValue) {
              this.updatePrimaryBossBar(playerId);
              iterator.remove();
            }
          }
        } else {
          this.updatePrimaryBossBar(playerId);
          iterator.remove();
        }
      }
    }
  }

  void tickMovingDaggers() {
    this.initializeTargetState();
    if (this.cachedDaggersByPlayer.isEmpty()) {
      this.activeScheduledTask.cancel();
      this.activeScheduledTask = null;
    } else {
      Iterator<Map.Entry<UUID,MovingDagger>> iterator = this.cachedDaggersByPlayer.entrySet().iterator();

      while (iterator.hasNext()) {
        Entry<UUID, MovingDagger> entry = iterator.next();
        MovingDagger movingDagger = entry.getValue();
        ItemDisplay itemDisplay = movingDagger.itemDisplay;
        Player player = Bukkit.getPlayer(movingDagger.playerId);
        if (player != null && player.isOnline() && itemDisplay != null && !itemDisplay.isDead() && itemDisplay.isValid()) {
          Location location = itemDisplay.getLocation().add(movingDagger.direction.clone().multiply(2.0));
          movingDagger.durationTicks++;
          if (!location.getBlock().isPassable()) {
            itemDisplay.remove();
            iterator.remove();
          } else {
            itemDisplay.teleport(location);
            itemDisplay.getWorld()
                .spawnParticle(
                    Particle.DUST,
                    location.clone(),
                    3,
                    new DustOptions(Color.fromRGB(106, 11, 184), 1.0F));
            boolean enabled = false;

            for (Entity entity : itemDisplay.getNearbyEntities(1.0, 1.0, 1.0)) {
              if (entity instanceof LivingEntity livingEntity
                  && livingEntity != player
                  && !(livingEntity instanceof ItemDisplay)) {
                if (livingEntity instanceof Player targetPlayer) {
                  if (targetPlayer.getGameMode() == GameMode.CREATIVE
                      || targetPlayer.getGameMode() == GameMode.SPECTATOR
                      || this.plugin.canUseAbility(targetPlayer)
                      || this.isTrustedPlayer(player, targetPlayer)
                      || targetPlayer.isBlocking()) {
                    continue;
                  }

                  this.secondaryPlayerLinks.put(targetPlayer.getUniqueId(), player.getUniqueId());
                  Bukkit.getScheduler()
                      .runTaskLater(
                          this.plugin, () -> this.clearTemporaryDaggerLink(targetPlayer, player), 100L);
                }

                int count = remainingTicks.getOrDefault(movingDagger.playerId, 0) + 1;
                remainingTicks.put(movingDagger.playerId, count);
                this.expiryTimes.put(movingDagger.playerId, System.currentTimeMillis() + 25000L);
                double configuredValue = ConfigValueCache.getDouble(this.plugin, "astraT2.daggerDamage", 4.0);
                livingEntity.damage(configuredValue, DamageSource.builder(DamageType.SONIC_BOOM).build());
                World world = livingEntity.getWorld();
                Location targetLocation = livingEntity.getEyeLocation();
                DustOptions dustOptions = new DustOptions(Color.fromRGB(106, 11, 184), 1.0F);
                world.spawnParticle(Particle.SMALL_GUST, targetLocation, 2, 0.0, 0.0, 0.0, 0.0);
                world.spawnParticle(Particle.CLOUD, targetLocation, 5, 0.3, 0.3, 0.3, 0.0);
                world.spawnParticle(Particle.END_ROD, targetLocation, 10, 0.3, 0.3, 0.3, 0.0);
                world.spawnParticle(Particle.DUST, targetLocation, 10, 0.3, 0.3, 0.3, dustOptions);
                boolean active = !this.sourceFlagsByPlayer.getOrDefault(movingDagger.playerId, true);
                if (active) {
                  if (livingEntity instanceof Player) {
                    int index =
                        ConfigValueCache.getInt(this.plugin, "astraT2.daggerDisableDuration", 15);
                    this.applyAbilityEffects(livingEntity.getUniqueId(), index * 1000L);
                  }

                  this.sourceFlagsByPlayer.put(movingDagger.playerId, true);
                  BossBar bossBar = this.bossBars.get(movingDagger.playerId);
                  if (bossBar != null) {
                    bossBar.setColor(BarColor.BLUE);
                  }
                }

                enabled = true;
                break;
              }
            }

            if (enabled || movingDagger.durationTicks >= 50) {
              itemDisplay.remove();
              iterator.remove();
            }
          }
        } else {
          iterator.remove();
        }
      }
    }
  }

  void clearTemporaryDaggerLink(Player player, Player targetPlayer) {
    UUID playerId = this.secondaryPlayerLinks.get(player.getUniqueId());
    if (playerId != null && playerId.equals(targetPlayer.getUniqueId())) {
      this.secondaryPlayerLinks.remove(player.getUniqueId());
    }
  }

  void finishDaggerCooldown(UUID playerId) {
    this.updatePrimaryBossBar(playerId);
    this.lastActivationTimes.remove(playerId);
  }

  static boolean shouldPrimaryRemoveExpiredEntry(long timestamp, Entry<UUID, Long> entry) {
    return entry.getValue() <= timestamp;
  }

  void finishInventoryUpdate(UUID playerId, Player player) {
    this.updatePrimaryBossBar(playerId);
    this.lastActivationTimes.remove(playerId);
    player.updateInventory();
  }

  static void configureAstralNpc(
      NPC npc, ItemStack item, ItemStack heldItem, ItemStack targetItem, ItemStack candidateItem, ItemStack secondaryItem) {
    if (npc.getEntity() instanceof Player player) {
      if (item != null && item.getType() != Material.AIR) {
        player.getInventory().setHelmet(item.clone());
      }

      if (heldItem != null && heldItem.getType() != Material.AIR) {
        player.getInventory().setChestplate(heldItem.clone());
      }

      if (targetItem != null && targetItem.getType() != Material.AIR) {
        player.getInventory().setLeggings(targetItem.clone());
      }

      if (candidateItem != null && candidateItem.getType() != Material.AIR) {
        player.getInventory().setBoots(candidateItem.clone());
      }

      if (secondaryItem != null && secondaryItem.getType() != Material.AIR) {
        player.getInventory().setItemInMainHand(secondaryItem.clone());
      } else {
        player.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
      }

      player.updateInventory();
    }
  }

  void finishDriftHorseCleanup(UUID playerId) {
    this.updateTargetAbilityState(playerId, this.pendingHorsesByPlayer.remove(playerId));
  }

  void finishPrimaryDriftHorseCleanup(UUID playerId) {
    this.updateTargetAbilityState(playerId, this.pendingHorsesByPlayer.remove(playerId));
  }

  void forgetDriftHorse(Horse horse) {
    this.playerIds.remove(horse.getUniqueId());
  }

  static {
    BLISS_ASTRA_T2_DRIFT_HORSE_ID = "bliss_astra_t2_drift_horse_";
    BLISS_ASTRA_BLUR_OWNER_ID = "bliss_astra_blur_owner";
    lastUpdateTimes = new ConcurrentHashMap<>();
    linkedPlayers = new HashMap<>();
    primaryLastUseTimes = new HashMap<>();
    remainingTicks = new ConcurrentHashMap<>();
  }
}
