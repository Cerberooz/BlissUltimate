package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class AstraTier1Gem implements Listener {
  static final String DIMENSIONAL_DRIFT_ID;
  static final String BLISS_ASTRA_T1_DRIFT_HORSE_ID;
  Bliss plugin;
  final Cache<UUID, Long> cache =
      CacheBuilder.newBuilder().expireAfterWrite(35L, TimeUnit.SECONDS).build();
  final Map<UUID, Long> cooldownTimestamps = new ConcurrentHashMap<>();
  final Map<UUID, Player> players = new HashMap<>();
  final Map<UUID, Boolean> secondaryFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> pendingFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Horse> horsesByPlayer = new ConcurrentHashMap<>();
  final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
  final Map<UUID, ItemStack> targetItemsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, ItemStack> activeItemsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, ItemStack> trackedItemsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, ItemStack> currentItemsByPlayer = new ConcurrentHashMap<>();
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();
  static Map<UUID, Player> linkedPlayers;
  Map<UUID, BukkitTask> scheduledTasks = new HashMap<>();
  final Cache<UUID, Long> secondaryCache =
      CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.SECONDS).build();
  AstraTier2Gem astraTier2Gem;
  Map<UUID, Integer> abilityStages = new HashMap<>();
  Map<UUID, Integer> playerCounters = new HashMap<>();
  static final Map<ItemStack, Boolean> cachedFlagsByItem;
  final Map<UUID, AstraTier1EntitySnapshot> primaryAstraTier1EntitySnapshotValuesByPlayer = new HashMap<>();
  final Map<UUID, Long> lastUseTimes = new HashMap<>();
  public static boolean active;
  private final Runnable actionBarRenderer = this::updateActionBar;

  public void setAstraTier2Gem(AstraTier2Gem astraTier2Gem) {
    this.astraTier2Gem = astraTier2Gem;
  }

  boolean isAbilityActive(UUID playerId) {
    return this.astraTier2Gem != null && this.astraTier2Gem.hasRequiredState(playerId);
  }

  long getAbilityLong(UUID playerId) {
    return this.astraTier2Gem == null ? 0L : this.astraTier2Gem.getAbilityLong(playerId);
  }

  public AstraTier1Gem(Bliss bliss) {
    this.plugin = bliss;
    this.startBackgroundTasks();
    this.initialize();
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          final Bliss plugin = bliss;

          @Override
          public void run() {
            Iterator<UUID> iterator = AstraTier1Gem.this.playerIds.iterator();

            while (iterator.hasNext()) {
              UUID playerId = iterator.next();
              Player player = Bukkit.getPlayer(playerId);
              if (player != null && player.isOnline()) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if ((AstraTier1Gem.isAbilityBlocked(mainHandItem) || AstraTier1Gem.isAbilityBlocked(offHandItem))
                    && AstraTier1Gem.this.isAbilityActive(player.getUniqueId())) {
                  long lastUpdateTime = AstraTier1Gem.this.getAbilityLong(player.getUniqueId());
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
                } else if (!AstraTier1Gem.this.isGemItem(mainHandItem) && !AstraTier1Gem.this.isGemItem(offHandItem)) {
                  iterator.remove();
                } else if (this.plugin.isGemsDisabled()
                   
                    || this.plugin.canUseAbility(player)) {
                  if (AstraTier1Gem.this.pendingFlagsByPlayer.getOrDefault(playerId, false)) {
                    AstraTier1Gem.this.scheduleAbilityUpdate(player);
                    AstraTier1Gem.this.pendingFlagsByPlayer.put(playerId, false);
                    AstraTier1Gem.this.secondaryFlagsByPlayer.put(playerId, false);
                  }

                  String displayColor = ChatColor.DARK_GRAY.toString();
                  String textColor = ChatColor.BOLD.toString();
                  ActionBarQueue.enqueue(
                      player,
                      "🔒 " + displayColor + textColor + "ᴅɪꜱᴀʙʟᴇᴅ");
                }
              } else {
                iterator.remove();
              }
            }
          }
        }, bliss, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void activateAbility(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.scheduledTasks.containsKey(playerId)) {
      this.scheduledTasks.get(playerId).cancel();
    }

    BukkitTask scheduledTask =
        new BukkitRunnable() {
          @Override
          public void run() {
            if (AstraTier1Gem.this.pendingFlagsByPlayer.getOrDefault(playerId, false) && player.isOnline()) {
              Location location = player.getLocation().add(0.0, 2.0, 0.0);
              player
              .getWorld()
              .spawnParticle(
                  Particle.DUST, location, 2, new DustOptions(Color.fromRGB(106, 11, 184), 1.0F));
            } else {
              this.cancel();
              AstraTier1Gem.this.scheduledTasks.remove(playerId);
            }
          }
        }
            .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(2L), 2L);
    this.scheduledTasks.put(playerId, scheduledTask);
  }

  public ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(13);
      itemMeta.setMaxStackSize(1);
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
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Phasing");
      lore.add(ChatColor.GRAY + "- Soul Healing");
      lore.add(ChatColor.GRAY + "- Soul Capture");
      lore.add("");
      lore.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      lore.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    this.playerIds.remove(playerId);
    cachedFlagsByItem.clear();
    if (this.pendingFlagsByPlayer.getOrDefault(playerId, false)) {
      this.scheduleAbilityUpdate(player);
    }

    this.pendingFlagsByPlayer.remove(playerId);
    this.secondaryFlagsByPlayer.remove(playerId);
    this.players.remove(playerId);
    this.resetAbilityState(playerId, this.horsesByPlayer.remove(playerId));
    this.targetItemsByPlayer.remove(playerId);
    this.activeItemsByPlayer.remove(playerId);
    this.trackedItemsByPlayer.remove(playerId);
    this.currentItemsByPlayer.remove(playerId);
    this.activateTargetAbility(player);
    this.cache.invalidate(playerId);
    this.secondaryCache.invalidate(playerId);
    this.cooldownTimestamps.remove(playerId);
    linkedPlayers.remove(playerId);
  }

  @EventHandler
  public void onEntityMount(EntityMountEvent event) {
    if (event.getMount() instanceof Horse horse) {
      if (event.getEntity() instanceof Player player) {
        UUID playerId = null;

        for (Entry<UUID,Horse> entry : this.horsesByPlayer.entrySet()) {
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

  boolean canUseAbility(Player player) {
    if (this.plugin.isGemsDisabled()) {
      return false;
    }

    if (false) {
      return false;
    }

    if (this.plugin.canUseAbility(player)) {
      return false;
    }

    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    return isAbilityBlocked(mainHandItem) || isAbilityBlocked(offHandItem);
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              if (AstraTier1Gem.isAbilityBlocked(player.getInventory().getItemInMainHand())
                  || AstraTier1Gem.isAbilityBlocked(player.getInventory().getItemInOffHand())) {
                AstraTier1Gem.this.playerIds.add(player.getUniqueId());
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (this.canUseAbility(player)) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          if (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < 0.02) {
            event.setCancelled(true);
            if (event.getDamager() instanceof Player targetPlayer) {
              targetPlayer.sendMessage(
                  ChatColor.of("#A01FFF")
                      + "🔮 "
                      + ChatColor.DARK_PURPLE
                      + "Your attack phased through "
                      + player.getName());
              linkedPlayers.put(playerId, player);
              Bukkit.getScheduler().runTaskLater(this.plugin, () -> linkedPlayers.remove(playerId, player), 20L);
            }
          }
        }
      }
    }
  }

  public static boolean isAbilityAllowed(Player player) {
    return linkedPlayers.containsKey(player.getUniqueId());
  }

  public static void refreshAbilityState(Player player) {
    UUID playerId = player.getUniqueId();
    if (linkedPlayers.containsKey(player)) {
      linkedPlayers.remove(playerId, player);
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    ItemStack item = event.getItemDrop().getItemStack();
    if (isAbilityBlocked(item)) {
      event.setCancelled(true);
    }
  }

  boolean isGemItem(ItemStack item) {
    return item != null && item.getType() == Material.AMETHYST_SHARD;
  }

  public static boolean isAbilityBlocked(ItemStack item) {
    if (item != null && item.getType() == Material.AMETHYST_SHARD) {
      Boolean currentBoolean = cachedFlagsByItem.get(item);
      if (currentBoolean != null) {
        return currentBoolean;
      } else if (!item.hasItemMeta()) {
        cachedFlagsByItem.put(item, false);
        return false;
      } else {
        ItemMeta itemMeta = item.getItemMeta();
        if (!itemMeta.hasCustomModelData()) {
          cachedFlagsByItem.put(item, false);
          return false;
        } else {
          int count = itemMeta.getCustomModelData();
          boolean enabled =
              (count == 13 || count == 93 || count == 73 || count == 53 || count == 33)
                  && itemMeta.hasDisplayName()
                  && ChatColor.stripColor(itemMeta.getDisplayName())
                      .equalsIgnoreCase("ᴀѕᴛʀᴀ ɢᴇᴍ");
          cachedFlagsByItem.put(item, enabled);
          return enabled;
        }
      }
    } else {
      return false;
    }
  }

  @EventHandler
  public void updateAbilityState(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (this.players.containsValue(player)) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (player.isSneaking()) {
      if (!this.isAbilityActive(playerId)) {
        if (!this.plugin.isGemsDisabled()
           
            && !this.plugin.canUseAbility(player)) {
          if (event.getAction() == Action.RIGHT_CLICK_BLOCK
              || event.getAction() == Action.RIGHT_CLICK_AIR) {
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            if (this.isGemItem(offHandItem) || this.isGemItem(mainHandItem)) {
              this.playerIds.add(playerId);
            }

            if (isAbilityBlocked(offHandItem)) {
              if (this.secondaryFlagsByPlayer.getOrDefault(playerId, false)) {
                return;
              }

              if (this.isMatchingState(mainHandItem)) {
                if (CooldownService.isOnCooldown(playerId, "dimensional_drift")) {
                  long timestamp = CooldownService.remainingMillis(playerId, "dimensional_drift");
                  String accentColor = ChatColor.of("#A01FFF").toString();
                  String messageColor = ChatColor.of("#FF686F").toString();
                  String displayColor = ChatColor.of("#C7C7C7").toString();
                  String textColor = ChatColor.of("#FF686F").toString();
                  String labelColor = ChatColor.of("#A01FFF").toString();
                  String text = AbilityStatusFormatter.formatDisplayTextForPlayer(timestamp, false);
                  player.sendMessage(
                      accentColor
                          + "🔺 "
                          + messageColor
                          + "Your "
                          + displayColor
                          + "Dimensional Drift "
                          + textColor
                          + "skill is on cooldown for "
                          + labelColor
                          + text);
                  return;
                }

                this.cooldownTimestamps.remove(playerId);
                this.playAbilityEffects(player);
                long lastUpdateTime = ConfigValueCache.getInt(this.plugin, "astraT1.dimDriftActiveSeconds", 10);
                long startTime = ConfigValueCache.getInt(this.plugin, "astraT1.dimDriftCooldown", 35);
                if (player.getInventory().contains(Material.DRAGON_EGG)) {
                  startTime = 17L;
                }

                ActiveAbilityStore.startActive(playerId, "dimensional_drift", lastUpdateTime, startTime);
              }
            }
          }
        }
      }
    }
  }

  void initialize() {
    ActionBarQueue.registerRenderer(this.actionBarRenderer);
  }

  boolean isMatchingState(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Material material = item.getType();
      return material.name().contains("SWORD") || material.name().contains("AXE");
    } else {
      return true;
    }
  }

  @EventHandler
  public void applyAbilityEffects(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (event.getEntity() instanceof LivingEntity livingEntity) {
        if (this.canUseAbility(player)) {
          double value = event.getFinalDamage();
          double distance = livingEntity.getHealth();
          if (distance - value <= 0.0
              && java.util.concurrent.ThreadLocalRandom.current().nextDouble() < 0.3) {
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

  void playAbilityEffects(Player player) {
    if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
      this.abilityStages.put(
          player.getUniqueId(), player.getPotionEffect(PotionEffectType.INVISIBILITY).getDuration());
      this.playerCounters.put(
          player.getUniqueId(), player.getPotionEffect(PotionEffectType.INVISIBILITY).getAmplifier());
    }

    UUID playerId = player.getUniqueId();
    this.players.put(playerId, player);
    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
    player.sendMessage(
        ChatColor.of("#A01FFF")
            + "🔺 "
            + ChatColor.of("#FF686F")
            + "You used "
            + ChatColor.of("#C7C7C7")
            + "Dimensional Drift");
    this.pendingFlagsByPlayer.put(playerId, true);
    Horse mount = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
    this.horsesByPlayer.put(playerId, mount);
    this.handleAbilityAction(playerId, mount);
    this.activateAbility(player);
    mount.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.35);
    mount.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0.9);
    mount.getAttribute(Attribute.MAX_HEALTH).setBaseValue(53.0);
    mount.setSilent(true);
    mount.setHealth(53.0);
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
    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1, false, false));
    this.spawnAbilityEffects(player);
    this.cleanupAbilityState(player);
    CooldownService.setCooldown(playerId, "dismount_1", 1L);
  }

  void spawnAbilityEffects(Player player) {
    UUID playerId = player.getUniqueId();
    ItemStack item = player.getInventory().getHelmet();
    if (item != null && item.getType() != Material.AIR) {
      this.targetItemsByPlayer.put(playerId, item.clone());
      player.getInventory().setHelmet(null);
    }

    ItemStack heldItem = player.getInventory().getChestplate();
    if (heldItem != null && heldItem.getType() != Material.AIR) {
      this.activeItemsByPlayer.put(playerId, heldItem.clone());
      player.getInventory().setChestplate(null);
    }

    ItemStack targetItem = player.getInventory().getLeggings();
    if (targetItem != null && targetItem.getType() != Material.AIR) {
      this.trackedItemsByPlayer.put(playerId, targetItem.clone());
      player.getInventory().setLeggings(null);
    }

    ItemStack candidateItem = player.getInventory().getBoots();
    if (candidateItem != null && candidateItem.getType() != Material.AIR) {
      this.currentItemsByPlayer.put(playerId, candidateItem.clone());
      player.getInventory().setBoots(null);
    }
  }

  void cleanupAbilityState(Player player) {
    UUID playerId = player.getUniqueId();
    int configuredValue = ConfigValueCache.getInt(Bliss.getInstance(), "astraT1.dimDriftActiveSeconds", 10);
    new BukkitRunnable() {
      @Override
      public void run() {
        if (AstraTier1Gem.this.pendingFlagsByPlayer.getOrDefault(playerId, false)) {
          AstraTier1Gem.this.scheduleAbilityUpdate(player);
          AstraTier1Gem.this.pendingFlagsByPlayer.put(playerId, false);
          Bukkit.getScheduler().runTaskLater(AstraTier1Gem.this.plugin, () -> {
                AstraTier1Gem.this.secondaryFlagsByPlayer.put(playerId, false);

              }, 60L);
        }
      }
    }.runTaskLater(this.plugin, configuredValue * 20);
  }

  void scheduleAbilityUpdate(Player player) {
    UUID playerId = player.getUniqueId();
    player.removePotionEffect(PotionEffectType.INVISIBILITY);
    if (this.abilityStages.containsKey(player.getUniqueId())) {
      player.addPotionEffect(
          new PotionEffect(
              PotionEffectType.INVISIBILITY,
              this.abilityStages.get(player.getUniqueId()),
              this.playerCounters.get(player.getUniqueId()),
              true,
              true,
              true));
      this.abilityStages.remove(player.getUniqueId());
      this.playerCounters.remove(player.getUniqueId());
    }

    this.activateTargetAbility(player);
    ItemStack item = this.targetItemsByPlayer.remove(playerId);
    ItemStack heldItem = this.activeItemsByPlayer.remove(playerId);
    ItemStack targetItem = this.trackedItemsByPlayer.remove(playerId);
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

    this.pendingFlagsByPlayer.put(playerId, false);
    ActiveAbilityStore.settleExpired(playerId, "dimensional_drift");
    this.players.remove(playerId, player);
    this.resetAbilityState(playerId, this.horsesByPlayer.remove(playerId));
  }

  @EventHandler
  public void onEntityDismount(EntityDismountEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.activePlayers.contains(event.getDismounted().getUniqueId())) {
        Horse horse = this.horsesByPlayer.get(playerId);
        boolean active =
            horse != null && horse.getUniqueId().equals(event.getDismounted().getUniqueId());
        boolean enabled = this.isValidTarget(playerId, event.getDismounted());
        if (!this.pendingFlagsByPlayer.getOrDefault(playerId, false) || !active && !enabled) {
          if (active || enabled) {
            this.resetAbilityState(playerId, event.getDismounted() instanceof Horse currentHorse ? currentHorse : horse);
          }
        } else if (CooldownService.isOnCooldown(player.getUniqueId(), "dismount_1")) {
          event.setCancelled(true);
        } else {
          long timestamp = ConfigValueCache.getInt(this.plugin, "astraT1.dimDriftCooldown", 35);
          if (player.getInventory().contains(Material.DRAGON_EGG)) {
            timestamp = 17L;
          }

          ActiveAbilityStore.startActive(playerId, "dimensional_drift", 0L, timestamp);
          this.processAbilityState(player);
        }
      }
    }
  }

  @EventHandler
  public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    boolean enabled =
        this.pendingFlagsByPlayer.getOrDefault(playerId, false)
            || this.horsesByPlayer.containsKey(playerId)
            || this.isValidTarget(playerId, player.getVehicle());
    if (enabled) {
      if (this.pendingFlagsByPlayer.getOrDefault(playerId, false)) {
        this.processAbilityState(player);
      } else {
        this.resetAbilityState(playerId, this.horsesByPlayer.remove(playerId));
      }

      this.trackAbilityState(playerId);
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (this.pendingFlagsByPlayer.getOrDefault(playerId, false)) {
        event.setCancelled(true);
        this.processAbilityState(player);
      }
    }
  }

  @EventHandler
  public void completeAbilityAction(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (event.getHand() == EquipmentSlot.HAND) {
      if (!this.plugin.isGemsDisabled()
         
          && !this.plugin.canUseAbility(player)) {
        if (!this.isAbilityActive(playerId)) {
          if (player.getGameMode() != GameMode.SPECTATOR) {
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            if (!this.secondaryCache.asMap().containsKey(playerId) && isAbilityBlocked(mainHandItem) && player.isSneaking()) {
              AstraTier1EntitySnapshot astraTier1EntitySnapshot = this.primaryAstraTier1EntitySnapshotValuesByPlayer.get(playerId);
              if (astraTier1EntitySnapshot != null) {
                Location location;
                if (event.getClickedBlock() != null) {
                  location = event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
                } else {
                  location =
                      player.getLocation()
                          .add(player.getLocation().getDirection().normalize().multiply(2));
                }

                this.finishAbilityAction(player, location);
                event.setCancelled(true);
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
    if (!this.plugin.isGemsDisabled()
       
        && !this.plugin.canUseAbility(player)) {
      if (!this.isAbilityActive(playerId)) {
        if (player.getGameMode() != GameMode.SPECTATOR) {
          ItemStack mainHandItem = player.getInventory().getItemInMainHand();
          if (isAbilityBlocked(mainHandItem) && player.isSneaking() && event.getHand() == EquipmentSlot.HAND) {
            Entity targetEntity = this.resolveEntity(entity);
            if (this.isProtectedTarget(targetEntity)) {
              if (this.isActiveForPlayer(targetEntity)) {
                event.setCancelled(true);
                return;
              }

              if (this.hasRequiredState(targetEntity)) {
                event.setCancelled(true);
                return;
              }

              if (this.primaryAstraTier1EntitySnapshotValuesByPlayer.containsKey(playerId)) {
                player.sendMessage("§cYou can only store 1 entity at a time!");
                event.setCancelled(true);
                return;
              }

              this.sendAbilityFeedback(player, targetEntity);
              event.setCancelled(true);
            }
          }
        }
      }
    }
  }

  void processAbilityState(Player player) {
    UUID playerId = player.getUniqueId();
    player.removePotionEffect(PotionEffectType.INVISIBILITY);
    if (this.abilityStages.containsKey(player.getUniqueId())) {
      player.addPotionEffect(
          new PotionEffect(
              PotionEffectType.INVISIBILITY,
              this.abilityStages.get(player.getUniqueId()),
              this.playerCounters.get(player.getUniqueId()),
              true,
              true,
              true));
      this.abilityStages.remove(player.getUniqueId());
      this.playerCounters.remove(player.getUniqueId());
    }

    this.activateTargetAbility(player);
    this.players.remove(playerId, player);
    this.resetAbilityState(playerId, this.horsesByPlayer.remove(playerId));
    this.refreshPlayerState(player);
    this.pendingFlagsByPlayer.put(playerId, false);
    this.cache.put(playerId, System.currentTimeMillis() + 35000L);
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.secondaryFlagsByPlayer.put(playerId, false), 60L);
  }

  void handleAbilityAction(UUID playerId, Horse horse) {
    horse.addScoreboardTag(this.formatDisplayText(playerId));
    horse.setPersistent(false);
  }

  String formatDisplayText(UUID playerId) {
    return "bliss_astra_t1_drift_horse_" + playerId;
  }

  boolean isValidTarget(UUID playerId, Entity entity) {
    return entity instanceof Horse
        && entity.getScoreboardTags().contains(this.formatDisplayText(playerId));
  }

  void resetAbilityState(UUID playerId, Horse horse) {
    String text = this.formatDisplayText(playerId);
    HashSet<UUID> trackedIds = new HashSet<>();
    if (horse != null) {
      trackedIds.add(horse.getUniqueId());
      this.updateState(horse);
    }

    for (World world : Bukkit.getWorlds()) {
      for (Horse currentHorse : world.getEntitiesByClass(Horse.class)) {
        if (currentHorse.getScoreboardTags().contains(text) && trackedIds.add(currentHorse.getUniqueId())) {
          this.updateState(currentHorse);
        }
      }
    }
  }

  void updateState(Horse horse) {
    this.activePlayers.add(horse.getUniqueId());
    horse.setInvulnerable(false);
    horse.remove();
    Bukkit.getScheduler()
        .runTaskLater(this.plugin, () -> this.forgetDriftHorse(horse), 1L);
  }

  void trackAbilityState(UUID playerId) {
    Bukkit.getScheduler()
        .runTaskLater(this.plugin, () -> this.finishPrimaryDriftHorseCleanup(playerId), 1L);
    Bukkit.getScheduler()
        .runTaskLater(this.plugin, () -> this.finishDriftHorseCleanup(playerId), 5L);
  }

  void refreshPlayerState(Player player) {
    UUID playerId = player.getUniqueId();
    ItemStack item = this.targetItemsByPlayer.remove(playerId);
    ItemStack heldItem = this.activeItemsByPlayer.remove(playerId);
    ItemStack targetItem = this.trackedItemsByPlayer.remove(playerId);
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

  boolean isConditionMet(Entity entity) {
    return entity instanceof Wither
        || entity instanceof Warden
        || entity instanceof FallingBlock
        || entity instanceof EnderDragon;
  }

  boolean isProtectedTarget(Entity entity) {
    return entity instanceof LivingEntity
        && !(entity instanceof Player)
        && !this.isConditionMet(entity);
  }

  String formatDisplayTextForPlayer(Entity entity) {
    return entity.getCustomName() != null
        ? entity.getCustomName()
        : entity.getType().name().toLowerCase().replace("_", " ");
  }

  void sendAbilityFeedback(Player player, Entity entity) {
    UUID playerId = player.getUniqueId();
    Location location = entity.getLocation();
    if (!this.isProtectedTarget(entity)) {
      player.sendMessage(ChatColor.of("#A01FFF") + "Â§cYou can only capture living mobs!");
    } else if (this.primaryAstraTier1EntitySnapshotValuesByPlayer.containsKey(playerId)) {
      player.sendMessage(
          ChatColor.of("#A01FFF") + "🔺 §cYou can only store 1 entity at a time!");
    } else if (this.isActiveForPlayer(entity)) {
      player.sendMessage(
          ChatColor.of("#A01FFF")
              + "🔺 §cYou cannot capture vehicles with players inside!");
    } else if (this.hasRequiredState(entity)) {
      player.sendMessage(
          ChatColor.of("#A01FFF")
              + "🔺 §cYou cannot capture vehicles with boss mobs inside!");
    } else {
      AstraTier1EntitySnapshot astraTier1EntitySnapshot = new AstraTier1EntitySnapshot(entity);
      this.primaryAstraTier1EntitySnapshotValuesByPlayer.put(playerId, astraTier1EntitySnapshot);
      this.secondaryCache.put(playerId, System.currentTimeMillis());
      this.spawnAbilityParticles(location, Particle.DUST, new DustOptions(Color.fromRGB(138, 43, 226), 1.0F));
      String text = this.formatDisplayTextForPlayer(entity);
      this.updatePlayerState(entity);
      player.sendMessage(
          ChatColor.of("#A01FFF") + "🔺 §dYou have captured " + text + " 1/1");
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

  boolean isActiveForPlayer(Entity entity) {
    for (Entity targetEntity : entity.getPassengers()) {
      if (targetEntity instanceof Player) {
        return true;
      }

      if (this.isActiveForPlayer(targetEntity)) {
        return true;
      }
    }

    return false;
  }

  boolean hasRequiredState(Entity entity) {
    for (Entity targetEntity : entity.getPassengers()) {
      if (this.isConditionMet(targetEntity)) {
        return true;
      }

      if (this.hasRequiredState(targetEntity)) {
        return true;
      }
    }

    return false;
  }

  void updatePlayerState(Entity entity) {
    for (Entity targetEntity : new ArrayList<>(entity.getPassengers())) {
      this.updatePlayerState(targetEntity);
    }

    entity.eject();
    entity.remove();
  }

  void finishAbilityAction(Player player, Location location) {
    UUID playerId = player.getUniqueId();
    AstraTier1EntitySnapshot astraTier1EntitySnapshot = this.primaryAstraTier1EntitySnapshotValuesByPlayer.remove(playerId);
    if (astraTier1EntitySnapshot == null) {
      player.sendMessage(
          ChatColor.of("#A01FFF") + "🔺 §cYou don't have any stored entities!");
    } else {
      Entity entity = astraTier1EntitySnapshot.resolveEntity(location);
      if (entity == null) {
        player.sendMessage(ChatColor.of("#A01FFF") + "🔺 §cError spawning entity!");
      } else {
        this.spawnAbilityParticles(location, Particle.DUST, new DustOptions(Color.fromRGB(138, 43, 226), 1.0F));
        this.secondaryCache.put(playerId, System.currentTimeMillis());
        String accentColor = ChatColor.of("#A01FFF").toString();
        String text = astraTier1EntitySnapshot.displayText;
        player.sendMessage(accentColor + "🔺 §dYou released " + text + " 0/1");
      }
    }
  }

  void spawnAbilityParticles(Location location, Particle particle, DustOptions dustOptions) {
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

  public void activatePrimaryAbility(Player player) {
    UUID playerId = player.getUniqueId();
    this.cache.invalidate(playerId);
  }

  void activateTargetAbility(Player player) {
    UUID playerId = player.getUniqueId();
    if (this.scheduledTasks.containsKey(playerId)) {
      this.scheduledTasks.get(playerId).cancel();
      this.scheduledTasks.remove(playerId);
    }
  }

  public void shutdown() {
    ActionBarQueue.unregisterRenderer(this.actionBarRenderer);
    Set<UUID> activePlayers = new HashSet<>();
    activePlayers.addAll(this.pendingFlagsByPlayer.keySet());
    activePlayers.addAll(this.horsesByPlayer.keySet());
    activePlayers.addAll(this.targetItemsByPlayer.keySet());
    activePlayers.addAll(this.activeItemsByPlayer.keySet());
    activePlayers.addAll(this.trackedItemsByPlayer.keySet());
    activePlayers.addAll(this.currentItemsByPlayer.keySet());

    for (UUID playerId : activePlayers) {
      Player player = Bukkit.getPlayer(playerId);
      if (player != null) {
        if (this.pendingFlagsByPlayer.getOrDefault(playerId, false)) {
          this.scheduleAbilityUpdate(player);
        } else {
          this.activateTargetAbility(player);
          this.refreshPlayerState(player);
          this.resetAbilityState(playerId, this.horsesByPlayer.remove(playerId));
        }
      } else {
        this.resetAbilityState(playerId, this.horsesByPlayer.remove(playerId));
      }
    }

    for (BukkitTask task : this.scheduledTasks.values()) {
      task.cancel();
    }
    this.scheduledTasks.clear();
    for (Map.Entry<UUID, Horse> entry : new ArrayList<>(this.horsesByPlayer.entrySet())) {
      this.resetAbilityState(entry.getKey(), entry.getValue());
    }
    this.horsesByPlayer.clear();
    this.cache.invalidateAll();
    this.secondaryCache.invalidateAll();
    this.cooldownTimestamps.clear();
    this.players.clear();
    this.secondaryFlagsByPlayer.clear();
    this.pendingFlagsByPlayer.clear();
    this.activePlayers.clear();
    this.targetItemsByPlayer.clear();
    this.activeItemsByPlayer.clear();
    this.trackedItemsByPlayer.clear();
    this.currentItemsByPlayer.clear();
    this.playerIds.clear();
    this.abilityStages.clear();
    this.playerCounters.clear();
    this.primaryAstraTier1EntitySnapshotValuesByPlayer.clear();
    this.lastUseTimes.clear();
    linkedPlayers.clear();
    cachedFlagsByItem.clear();
  }

  void finishDriftHorseCleanup(UUID playerId) {
    this.resetAbilityState(playerId, this.horsesByPlayer.remove(playerId));
  }

  void finishPrimaryDriftHorseCleanup(UUID playerId) {
    this.resetAbilityState(playerId, this.horsesByPlayer.remove(playerId));
  }

  void forgetDriftHorse(Horse horse) {
    this.activePlayers.remove(horse.getUniqueId());
  }

  void updateActionBar() {
    for (UUID playerId : this.playerIds) {
      Player player = Bukkit.getPlayer(playerId);
      if (player != null && player.isOnline()) {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (this.isGemItem(mainHandItem) || this.isGemItem(offHandItem)) {
          if (this.plugin.isGemsDisabled()
             
              || this.plugin.canUseAbility(player)) {
            return;
          }

          if (isAbilityBlocked(mainHandItem) || isAbilityBlocked(offHandItem)) {
            boolean active = CooldownService.isOnCooldown(player.getUniqueId(), "dimensional_drift");
            if (active) {
              String message =
                  AbilityStatusFormatter.formatDisplayText(
                      player.getUniqueId(), "dimensional_drift", false);
              String accentColor = ChatColor.of("#A01FFF").toString();
              String textColor = ChatColor.DARK_PURPLE.toString();
              ActionBarQueue.enqueue(player, accentColor + "🔺 " + textColor + message);
            } else {
              ActionBarQueue.enqueue(
                  player, ChatColor.of("#A01FFF") + "🔺 " + ChatColor.GREEN + "Ready!");
            }
          }
        }
      }
    }
  }

  static {
    DIMENSIONAL_DRIFT_ID = "dimensional_drift";
    BLISS_ASTRA_T1_DRIFT_HORSE_ID = "bliss_astra_t1_drift_horse_";
    linkedPlayers = new HashMap<>();
    cachedFlagsByItem = new WeakHashMap<>();
    active = false;
  }
}
