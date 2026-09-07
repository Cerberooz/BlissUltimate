package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PuffTier2Gem implements Listener {
  static final String PUFF_BREEZY_BASH_T2_ID;
  static final String PUFF_DOUBLE_JUMP_T2_ID;
  static final String PUFF_DASH_T2_ID;
  final Bliss plugin;
  final NamespacedKey itemKey;
  final Random random = new Random();
  final Map<UUID, Boolean> flagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> secondaryFlagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, UUID> playerLinks = new ConcurrentHashMap<>();
  final Cache<UUID, Long> secondaryCache =
      CacheBuilder.newBuilder().expireAfterWrite(10L, TimeUnit.SECONDS).build();
  final Cache<UUID, Long> activeCache = CacheBuilder.newBuilder().build();
  final Cache<UUID, Long> cache =
      CacheBuilder.newBuilder().expireAfterWrite(3L, TimeUnit.MINUTES).build();
  final Cache<UUID, Long> currentCache = CacheBuilder.newBuilder().build();
  final Set<String> disabledWorldNames;
  TrustCommand trustCommand;
  AstraTier2Gem astraTier2Gem;
  final Map<UUID, UUID> secondaryPlayerLinks = new ConcurrentHashMap<>();

  long getPuffT2BreezyBashCooldown() {
    return ConfigValueCache.getInt(this.plugin, "puffT2.breezyBashCooldown", 135) * 1000L;
  }

  long getCooldownMillis() {
    return ConfigValueCache.getInt(this.plugin, "puffT2.doubleJumpCooldown", 5) * 1000L;
  }

  long getDurationMillis() {
    return ConfigValueCache.getInt(this.plugin, "puffT2.dashCooldown", 40) * 1000L;
  }

  boolean isConditionMet(World world) {
    return this.disabledWorldNames.contains(world.getName());
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

  public void setAstraTier2Gem(AstraTier2Gem astraTier2Gem) {
    this.astraTier2Gem = astraTier2Gem;
  }

  boolean isAbilityActive(UUID playerId) {
    return this.astraTier2Gem != null && this.astraTier2Gem.hasRequiredState(playerId);
  }

  long getAbilityLong(UUID playerId) {
    return this.astraTier2Gem == null ? 0L : this.astraTier2Gem.getAbilityLong(playerId);
  }

  long getLastUseTime(long timestamp, Player player) {
    return player.getInventory().contains(Material.DRAGON_EGG) ? timestamp / 2L : timestamp;
  }

  boolean isAbilityAllowed(UUID playerId, Cache<UUID, Long> cache) {
    Long storedTimestamp = cache.getIfPresent(playerId);
    if (storedTimestamp == null) {
      return false;
    } else {
      long now = storedTimestamp - System.currentTimeMillis();
      if (now <= 0L) {
        cache.invalidate(playerId);
        return false;
      } else {
        return true;
      }
    }
  }

  public PuffTier2Gem(Bliss bliss) {
    this.plugin = bliss;
    this.itemKey = new NamespacedKey(bliss, "puff_gem");
    this.disabledWorldNames = Set.copyOf(bliss.getConfig().getStringList("DisabledWorlds"));
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          final Bliss plugin = bliss;

          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if (this.plugin.isGemsDisabled() || PuffTier2Gem.this.isConditionMet(player.getWorld())) {
                if (this.plugin.canUseAbility(player)) {
                  return;
                }

                if (PuffTier2Gem.isAbilityBlocked(mainHandItem) || PuffTier2Gem.isAbilityBlocked(offHandItem)) {
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
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if ((PuffTier2Gem.isAbilityBlocked(mainHandItem) || PuffTier2Gem.isAbilityBlocked(offHandItem))
                  && PuffTier2Gem.this.isAbilityActive(player.getUniqueId())) {
                long remainingMillis = PuffTier2Gem.this.getAbilityLong(player.getUniqueId());
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
              } else if (PuffTier2Gem.isAbilityBlocked(mainHandItem) || PuffTier2Gem.isAbilityBlocked(offHandItem)) {
                PuffTier2Gem.this.activateAbility(player);
              }
            }
          }
        }, bliss, SharedScheduler.staggeredInitialDelay(20L), 20L);
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          final Bliss plugin = bliss;

          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if ((PuffTier2Gem.isAbilityBlocked(mainHandItem) || PuffTier2Gem.isAbilityBlocked(offHandItem))
                  && PuffTier2Gem.this.isAbilityActive(player.getUniqueId())) {
                long remainingMillis = PuffTier2Gem.this.getAbilityLong(player.getUniqueId());
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
                  && !PuffTier2Gem.this.isConditionMet(player.getWorld())
                  && !this.plugin.canUseAbility(player)
                  && (PuffTier2Gem.isAbilityBlocked(mainHandItem) || PuffTier2Gem.isAbilityBlocked(offHandItem))) {
                PuffTier2Gem.this.sendAbilityFeedback(player);
              }
            }
          }
        }, bliss, SharedScheduler.staggeredInitialDelay(20L), 20L);
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            PuffTier2Gem.this.flagsByPlayer.entrySet().removeIf(this::isAbilityAllowed);
            PuffTier2Gem.this.secondaryFlagsByPlayer.entrySet().removeIf(this::shouldRemoveExpiredEntry);
          }

          private boolean shouldRemoveExpiredEntry(Entry<UUID, ?> entry) {
            Player player = Bukkit.getPlayer(entry.getKey());
            return player == null || !player.isOnline();
          }

          private boolean isAbilityAllowed(Entry<UUID, ?> entry) {
            Player player = Bukkit.getPlayer(entry.getKey());
            return player == null || !player.isOnline();
          }
        }, bliss, SharedScheduler.staggeredInitialDelay(1200L), 1200L);
  }

  boolean isGemItem(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      if (item.getType() != Material.AMETHYST_SHARD) {
        return false;
      }

      ItemMeta itemMeta = item.getItemMeta();
      if (!itemMeta.hasCustomModelData()) {
        return false;
      }

      int count = itemMeta.getCustomModelData();
      return count == 5 || count == 25 || count == 45 || count == 65 || count == 85;
    } else {
      return false;
    }
  }

  void activateAbility(Player player) {
    if (!this.plugin.isGemsDisabled() && !this.isConditionMet(player.getWorld())) {
      if (!this.plugin.canUseAbility(player)) {
        ItemStack item = player.getInventory().getBoots();
        if (item != null && item.getType() != Material.AIR) {
          item.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, 4);
        }

        for (ItemStack heldItem : player.getInventory().getContents()) {
          if (heldItem != null && heldItem.getType() == Material.BOW) {
            if (!heldItem.containsEnchantment(Enchantment.POWER)) {
              heldItem.addUnsafeEnchantment(Enchantment.POWER, 5);
            }

            if (!heldItem.containsEnchantment(Enchantment.PUNCH)) {
              heldItem.addUnsafeEnchantment(Enchantment.PUNCH, 2);
            }
          }
        }
      }
    }
  }

  void sendAbilityFeedback(Player player) {
    String primaryStatus = this.formatDisplayText(player.getUniqueId(), this.cache, false);
    String message = this.formatDisplayText(player.getUniqueId(), this.secondaryCache, true);
    String displayText = this.formatDisplayText(player.getUniqueId(), this.currentCache, true);
    String textColor = ChatColor.WHITE.toString();
    String labelColor = ChatColor.AQUA.toString();
    String nameColor = ChatColor.WHITE.toString();
    String configColor = ChatColor.AQUA.toString();
    String keyColor = ChatColor.WHITE.toString();
    String argumentColor = ChatColor.AQUA.toString();
    String subtitle =
        textColor
            + "☁ "
            + labelColor
            + primaryStatus
            + " "
            + nameColor
            + "🔮 "
            + configColor
            + message
            + " "
            + keyColor
            + "⏫ "
            + argumentColor
            + displayText;
    ActionBarQueue.enqueue(player, subtitle);
  }

  String formatDisplayText(UUID playerId, Cache<UUID, Long> cache, boolean enabled) {
    String text = this.formatDisplayTextForTarget(cache);
    if (text != null && ActiveAbilityStore.isActive(playerId, text)) {
      return ChatColor.RED + "Active...";
    }

    Long storedTimestamp = cache.getIfPresent(playerId);
    if (storedTimestamp != null) {
      long now = storedTimestamp - System.currentTimeMillis();
      if (now > 0L) {
        return this.formatDisplayTextForPlayer(now, enabled);
      }
    }

    return ChatColor.GREEN + "Ready!";
  }

  String formatDisplayTextForPlayer(long timestamp, boolean enabled) {
    if (timestamp <= 1000L) {
      return ChatColor.GREEN + "Ready!";
    }

    long lastUpdateTime = timestamp / 1000L;
    if (lastUpdateTime <= 0L) {
      lastUpdateTime = 1L;
    }

    if (enabled) {
      return String.format("%ds", lastUpdateTime);
    }

    long startTime = lastUpdateTime / 60L;
    long expiryTime = lastUpdateTime % 60L;
    return startTime > 0L ? String.format("%dm %ds", startTime, expiryTime) : String.format("%ds", expiryTime);
  }

  public ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(6);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
      persistentDataContainer.set(this.itemKey, PersistentDataType.BYTE, (byte) 1);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.of("#befff7") + "Energy:");
      lore.add(ChatColor.of("#82EDBF") + "Pristine");
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Enchants Power V");
      lore.add(ChatColor.GRAY + "- Enchants Punch II");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling IV");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add(ChatColor.GRAY + "- 1% Levitation Chance");
      lore.add(ChatColor.GRAY + "- 1% Slow Falling Chance");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      lore.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      lore.add("");
      lore.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static boolean isAbilityBlocked(ItemStack item) {
    if (item == null) {
      return false;
    }

    if (item.getType() != Material.PRISMARINE_SHARD) {
      return false;
    }

    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta == null) {
      return false;
    }

    if (!itemMeta.hasCustomModelData()) {
      return false;
    }

    int count = itemMeta.getCustomModelData();
    return count == 6 || count == 26 || count == 46 || count == 66 || count == 86;
  }

  boolean canUseAbility(Player player) {
    return isAbilityBlocked(player.getInventory().getItemInMainHand())
        || isAbilityBlocked(player.getInventory().getItemInOffHand());
  }

  boolean isMatchingState(Player player) {
    return !this.plugin.isGemsDisabled()
        && !this.isConditionMet(player.getWorld())
        && !this.plugin.canUseAbility(player)
        && !this.isAbilityActive(player.getUniqueId())
        && this.canUseAbility(player);
  }

  boolean isLocationBlocked(Location location, double value) {
    if (location.getWorld() == null) {
      return false;
    }

    double distance = value * value;

    for (Player player : location.getWorld().getPlayers()) {
      if (player.getLocation().distanceSquared(location) <= distance && this.isMatchingState(player)) {
        return true;
      }
    }

    return false;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (!this.plugin.isGemsDisabled() && !this.isConditionMet(player.getWorld())) {
        if (!this.plugin.canUseAbility(player)) {
          if (isAbilityBlocked(player.getInventory().getItemInMainHand())
              || isAbilityBlocked(player.getInventory().getItemInOffHand())) {
            if (player.getGameMode() == GameMode.SURVIVAL
                || player.getGameMode() == GameMode.ADVENTURE) {
              if (this.isAbilityAllowed(playerId, this.secondaryCache)) {
                player.setAllowFlight(false);
              } else if (this.secondaryFlagsByPlayer.containsKey(playerId)) {
                player.setAllowFlight(false);
              } else if (player.isOnGround()) {
                  player.setAllowFlight(true);
                  ActiveAbilityStore.removeAbility(playerId, "puff_double_jump_t2");
                }
              
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
        if (isAbilityBlocked(player.getInventory().getItemInMainHand())
            || isAbilityBlocked(player.getInventory().getItemInOffHand())) {
          if (!this.plugin.isGemsDisabled()
              && !this.isConditionMet(player.getWorld())
              && !this.plugin.canUseAbility(player)
              && !this.isAbilityAllowed(playerId, this.secondaryCache)) {
            event.setCancelled(true);
            Location location = player.getLocation();
            player.getWorld().spawnParticle(Particle.CLOUD, location, 25, 0.5, 0.2, 0.5, 0.0);
            player.getWorld().spawnParticle(Particle.GUST, location, 1);
            this.flagsByPlayer.put(playerId, true);
            this.secondaryFlagsByPlayer.put(playerId, true);
            player.setAllowFlight(false);
            player.setFallDistance(0.0F);
            double configuredValue = ConfigValueCache.getDouble(this.plugin, "puffT2.doubleJumpHorizontal", 0.9);
            double distance = ConfigValueCache.getDouble(this.plugin, "puffT2.doubleJumpVertical", 0.8);
            Vector offset = player.getLocation().getDirection().multiply(configuredValue).setY(distance);
            player.setVelocity(offset);
            this.cleanupAbilityState(
                playerId,
                this.secondaryCache,
                this.getCooldownMillis(),
                "puff_double_jump_t2",
                ConfigValueCache.getInt(this.plugin, "puffT2.doubleJumpActiveSeconds", 2));
            String accentColor = ChatColor.of("#82F3FF").toString();
            String textColor = ChatColor.WHITE.toString();
            String messageColor = ChatColor.of("#82F3FF").toString();
            String displayColor = ChatColor.WHITE.toString();
            long timestamp = this.getCooldownMillis() / 1000L;
            player.sendMessage(
                "🔮 "
                    + accentColor
                    + "You have used "
                    + textColor
                    + "Double Jump "
                    + messageColor
                    + "now on cooldown for "
                    + displayColor
                    + timestamp
                    + "s");
            new BukkitRunnable() {
              int durationTicks = 0;

              @Override
              public void run() {
                if (!player.isOnline()
                    || PuffTier2Gem.this.plugin.isGemsDisabled()
                    || PuffTier2Gem.this.plugin.canUseAbility(player)
                    || PuffTier2Gem.this.isConditionMet(player.getWorld())) {
                  PuffTier2Gem.this.flagsByPlayer.remove(playerId);
                  PuffTier2Gem.this.secondaryFlagsByPlayer.remove(playerId);
                  ActiveAbilityStore.removeAbility(playerId, "puff_double_jump_t2");
                  this.cancel();
                } else if (player.isOnGround()) {
                  PuffTier2Gem.this.flagsByPlayer.remove(playerId);
                  PuffTier2Gem.this.secondaryFlagsByPlayer.remove(playerId);
                  ActiveAbilityStore.removeAbility(playerId, "puff_double_jump_t2");
                  if (player.isOnline()
                      && player.isOnGround()
                      && (PuffTier2Gem.isAbilityBlocked(player.getInventory().getItemInMainHand())
                      || PuffTier2Gem.isAbilityBlocked(player.getInventory().getItemInOffHand()))
                      && !PuffTier2Gem.this.plugin.isGemsDisabled()
                      && !PuffTier2Gem.this.isConditionMet(player.getWorld())
                      && !PuffTier2Gem.this.plugin.canUseAbility(player)
                      && (player.getGameMode() == GameMode.SURVIVAL
                      || player.getGameMode() == GameMode.ADVENTURE)) {
                    player.setAllowFlight(true);
                  }

                  this.cancel();
                } else if (this.durationTicks >= 120) {
                  PuffTier2Gem.this.secondaryFlagsByPlayer.remove(playerId);
                  this.cancel();
                } else {
                  Location location = player.getLocation().clone().add(0.0, -0.5, 0.0);
                  player.getWorld().spawnParticle(Particle.CLOUD, location, 3, 0.1, 0.1, 0.1, 0.0);
                  this.durationTicks++;
                }
              }
            }
                .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
          } else {
            event.setCancelled(true);
            player.setAllowFlight(false);
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onCreatureSpawn(CreatureSpawnEvent event) {
    if (event.getEntityType() == EntityType.WARDEN) {
      if (this.isLocationBlocked(event.getLocation(), 64.0)) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onBlockReceiveGame(BlockReceiveGameEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (this.isAbilityActive(playerId)) {
        return;
      }

      if (this.plugin.isGemsDisabled() || this.isConditionMet(player.getWorld())) {
        return;
      }

      if (this.plugin.canUseAbility(player)) {
        return;
      }

      if (this.canUseAbility(player)) {
        Material block = event.getBlock().getType();
        if (block == Material.SCULK_SENSOR
            || block == Material.CALIBRATED_SCULK_SENSOR
            || block == Material.SCULK_SHRIEKER) {
          event.setCancelled(true);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    UUID playerId = player.getUniqueId();
    if (!this.isAbilityActive(playerId)) {
      if (!this.plugin.isGemsDisabled() && !this.isConditionMet(player.getWorld())) {
        if (!this.plugin.canUseAbility(player)) {
          if (isAbilityBlocked(mainHandItem)) {
            if (event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) {
              if (event.getAction() == Action.RIGHT_CLICK_AIR
                  || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (this.isAbilityAllowed(player.getUniqueId(), this.currentCache)) {
                  String message = this.formatDisplayText(player.getUniqueId(), this.currentCache, false);
                  if (!ActiveAbilityStore.isActive(player.getUniqueId(), "puff_dash_t2")) {
                    player.sendMessage(
                        "§f🔮 §cYour §f⏫Dash §cskill is on"
                            + " cooldown for §f"
                            + message);
                  }

                  return;
                }

                this.updateAbilityState(player);
              }
            } else {
              if (this.isAbilityAllowed(player.getUniqueId(), this.cache)) {
                String displayText = this.formatDisplayText(player.getUniqueId(), this.cache, false);
                if (!ActiveAbilityStore.isActive(player.getUniqueId(), "puff_breezy_bash_t2")) {
                  player.sendMessage(
                      "§f🔮 §cYour §f☁Breezy Bash §cskill is on"
                          + " cooldown for §f"
                          + displayText);
                }

                return;
              }

              this.refreshAbilityState(player);
            }
          }
        }
      }
    }
  }

  void spawnAbilityParticles(Location location, double value) {
    int count = (int) (value * 32.0);

    for (int index = 0; index < count; index++) {
      double distance = (Math.PI * 2) * index / count;
      double offset = location.getX() + value * Math.cos(distance);
      double radius = location.getZ() + value * Math.sin(distance);
      Location effectLocation = new Location(location.getWorld(), offset, location.getY(), radius);
      location.getWorld().spawnParticle(Particle.SMOKE, effectLocation, 1, 0.0, 0.0, 0.0, 0.0);
      location.getWorld()
          .spawnParticle(
              Particle.DUST,
              effectLocation,
              1,
              0.0,
              0.0,
              0.0,
              0.0,
              new DustOptions(Color.fromRGB(255, 255, 255), 1.5F),
              true);
    }
  }

  void refreshAbilityState(Player player) {
    player.sendMessage("§f🔮 §bYou used §f☁Breezy Bash §bGroup");
    long timestamp = this.getLastUseTime(this.getPuffT2BreezyBashCooldown(), player);
    this.cleanupAbilityState(player.getUniqueId(), this.cache, timestamp, "puff_breezy_bash_t2", 2);
    Location location = player.getLocation();
    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;

      @Override
      public void run() {
        if (!PuffTier2Gem.this.plugin.isGemsDisabled() && !PuffTier2Gem.this.isConditionMet(player.getWorld())) {
          if (!PuffTier2Gem.this.plugin.canUseAbility(player)) {
            switch (this.durationTicks) {
            case 0:
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 0.75);
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 1.0);
              break;
            case 1:
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 1.25);
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 1.5);
              break;
            case 2:
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 1.75);
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 2.0);
              break;
            case 3:
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 2.25);
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 2.5);
              break;
            case 4:
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 2.75);
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 3.0);
              break;
            case 5:
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 3.5);
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 4.0);
              break;
            case 6:
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 4.5);
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 5.0);
              break;
            case 7:
            case 8:
            case 9:
              PuffTier2Gem.this.spawnAbilityParticles(this.anchorLocation, 5.0);
            }

            PuffTier2Gem.this.applyAbilityMotion(player, this.anchorLocation);
            this.durationTicks++;
            if (this.durationTicks > 9) {
              this.cancel();
            }
          }
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
  }

  void applyAbilityMotion(Player player, Location location) {
    double configuredValue = ConfigValueCache.getDouble(this.plugin, "puffT2.breezyBashGroupRadius", 5.0);
    double distance = ConfigValueCache.getDouble(this.plugin, "puffT2.breezyBashGroupHorizontal", 2.5);
    double radius = ConfigValueCache.getDouble(this.plugin, "puffT2.breezyBashGroupVertical", 1.25);

    for (Entity entity : location.getWorld().getNearbyEntities(location, configuredValue, configuredValue, configuredValue)) {
      if (!(entity instanceof Item)
          && !(entity instanceof ExperienceOrb)
          && !(entity instanceof Projectile)
          && !(entity instanceof Painting)
          && !(entity instanceof ItemFrame)
          && !(entity instanceof ArmorStand)
          && (!(entity instanceof Player) || !entity.equals(player))
          && !this.isTrustedPlayer(player, entity)) {
        Vector offset = entity.getLocation().toVector().subtract(location.toVector());
        if (offset.length() > 0.0) {
          offset.normalize();
          Vector direction = offset.multiply(distance).setY(radius);
          entity.setVelocity(direction);
        }
      }
    }
  }

  void updateAbilityState(Player player) {
    if (!this.plugin.isGemsDisabled() && !this.isConditionMet(player.getWorld())) {
      if (!this.plugin.canUseAbility(player)) {
        UUID playerId = player.getUniqueId();
        Location location = player.getLocation();
        player.sendMessage(
            "§f🔮 §bYou have activated §f⏫Dash §bskill");
        long timestamp = this.getLastUseTime(this.getDurationMillis(), player);
        this.cleanupAbilityState(
            playerId,
            this.currentCache,
            timestamp,
            "puff_dash_t2",
            ConfigValueCache.getInt(this.plugin, "puffT2.dashActiveSeconds", 4));
        double configuredValue = ConfigValueCache.getDouble(this.plugin, "puffT2.dashVelocity", 2.0);
        player.getWorld().spawnParticle(Particle.CLOUD, location, 25, 0.5, 0.2, 0.5, 0.0);
        player.getWorld().spawnParticle(Particle.GUST, location, 1);
        location.getWorld().playSound(location, Sound.ENTITY_BREEZE_WIND_BURST, 1.0F, 1.5F);
        Vector offset = player.getLocation().getDirection().multiply(configuredValue);
        player.setVelocity(offset);
        Set<UUID> trackedIds = ConcurrentHashMap.newKeySet();
        double distance = ConfigValueCache.getDouble(this.plugin, "puffT2.dashDamage", 9.0);
        new BukkitRunnable() {
          final Player capturedPlayer = player;
          final Set<UUID> playerIds = trackedIds;
          final UUID capturedPlayerId = playerId;
          final double effectRadius = distance;
          int durationTicks = 0;

          @Override
          public void run() {
            if (this.capturedPlayer.isOnline()
                && !this.capturedPlayer.isDead()
                && !PuffTier2Gem.this.plugin.isGemsDisabled()
                && !PuffTier2Gem.this.plugin.canUseAbility(this.capturedPlayer)
                && !PuffTier2Gem.this.isConditionMet(this.capturedPlayer.getWorld())) {
              if (this.durationTicks >= 24) {
                Bukkit.getScheduler().runTaskLater(PuffTier2Gem.this.plugin, () -> this.clearLinkedPlayers(this.playerIds, this.capturedPlayerId), 10L);
                this.cancel();
              } else {
                for (Entity entity : this.capturedPlayer.getNearbyEntities(1.5, 1.5, 1.5)) {
                  if (entity instanceof LivingEntity livingEntity && livingEntity != this.capturedPlayer) {
                    UUID playerId = livingEntity.getUniqueId();
                    if (this.playerIds.add(playerId) && !PuffTier2Gem.this.isTrustedPlayer(this.capturedPlayer, livingEntity)) {
                      if (livingEntity instanceof Player) {
                        PuffTier2Gem.this.secondaryPlayerLinks.put(playerId, this.capturedPlayerId);
                      }

                      PuffTier2Gem.this.playerLinks.put(playerId, this.capturedPlayerId);

                      try {
                        double value = livingEntity.getHealth();
                        livingEntity.damage(
                            this.effectRadius,
                            DamageSource.builder(DamageType.SONIC_BOOM)
                            .withCausingEntity(this.capturedPlayer)
                            .withDirectEntity(this.capturedPlayer)
                            .build());
                        if (livingEntity instanceof Player && !livingEntity.isDead() && livingEntity.getHealth() >= value) {
                          livingEntity.setHealth(Math.max(0.0, value - this.effectRadius));
                        }
                      } finally {
                        PuffTier2Gem.this.playerLinks.remove(playerId, this.capturedPlayerId);
                      }
                    }
                  }
                }

                this.capturedPlayer
                .getWorld()
                .spawnParticle(Particle.CLOUD, this.capturedPlayer.getLocation(), 3, 0.1, 0.1, 0.1, 0.0);
                this.durationTicks++;
              }
            } else {
              for (UUID targetId : this.playerIds) {
                PuffTier2Gem.this.secondaryPlayerLinks.remove(targetId, this.capturedPlayerId);
              }

              this.cancel();
            }
          }

          private void clearLinkedPlayers(Set<UUID> linkedPlayerIds, UUID playerId) {
            for (UUID targetId : linkedPlayerIds) {
              PuffTier2Gem.this.secondaryPlayerLinks.remove(targetId, playerId);
            }
          }
        }
            .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    UUID playerId = player.getUniqueId();
    UUID targetId = this.secondaryPlayerLinks.get(playerId);
    if (targetId != null) {
      Player targetPlayer = Bukkit.getPlayer(targetId);
      if (targetPlayer != null) {
        String textColor = ChatColor.WHITE.toString();
        String name = targetPlayer.getName();
        String messageColor = ChatColor.WHITE.toString();
        String displayColor = ChatColor.WHITE.toString();
        String label = player.getName();
        event.setDeathMessage(textColor + name + messageColor + " dashed through " + displayColor + label);
        this.secondaryPlayerLinks.remove(playerId);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        if (!this.plugin.isGemsDisabled() && !this.isConditionMet(player.getWorld())) {
          if (!this.plugin.canUseAbility(player)) {
            if (event.getCause() == DamageCause.FALL) {
              if (isAbilityBlocked(player.getInventory().getItemInMainHand())
                  || isAbilityBlocked(player.getInventory().getItemInOffHand())) {
                double value = event.getDamage();
                if (value > 0.0) {
                  int count = (int) Math.round(value);
                  int index = this.plugin.getConfiguredInt(player, "almostFellFromGrace");
                  int remaining = index + count;
                  if (remaining > 30000) {
                    remaining = 30000;
                  }

                  this.plugin.processAbilityState(player, "almostFellFromGrace", remaining);
                }

                event.setCancelled(true);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void applyAbilityEffects(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (!this.plugin.isGemsDisabled() && !this.isConditionMet(player.getWorld())) {
        if (!this.plugin.canUseAbility(player)) {
          UUID playerId = player.getUniqueId();
          if (!this.isAbilityActive(playerId)) {
            if (event.getCause() == DamageCause.CONTACT) {
              if (isAbilityBlocked(player.getInventory().getItemInMainHand())
                  || isAbilityBlocked(player.getInventory().getItemInOffHand())) {
                event.setCancelled(true);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (event.getEntity() instanceof Player targetPlayer) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          if (!this.plugin.isGemsDisabled() && !this.isConditionMet(targetPlayer.getWorld())) {
            if (!this.plugin.canUseAbility(targetPlayer)) {
              if (!this.plugin.canUseAbility(player)) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (!this.isTrustedPlayer(player, targetPlayer)) {
                  if (!player.getUniqueId().equals(this.playerLinks.get(targetPlayer.getUniqueId()))) {
                    if (isAbilityBlocked(mainHandItem)) {
                      if (this.isAbilityAllowed(player.getUniqueId(), this.cache)) {
                        String message =
                            this.formatDisplayText(player.getUniqueId(), this.cache, false);
                        if (!ActiveAbilityStore.isActive(
                            player.getUniqueId(), "puff_breezy_bash_t2")) {
                          player.sendMessage(
                              "§f🔮 §cYour §f☁Breezy Bash"
                                  + " §cskill is on cooldown for §f"
                                  + message);
                        }

                        return;
                      }

                      this.playAbilityEffects(player, targetPlayer);
                    }

                    if (isAbilityBlocked(offHandItem)) {
                      double configuredValue =
                          ConfigValueCache.getDouble(this.plugin, "puffT2.levitationChance", 0.01);
                      double distance =
                          ConfigValueCache.getDouble(this.plugin, "puffT2.slowFallingChance", 0.01);
                      if (this.random.nextDouble() < configuredValue) {
                        int index =
                            ConfigValueCache.getInt(this.plugin, "puffT2.levitationDuration", 15);
                        int remaining =
                            ConfigValueCache.getInt(this.plugin, "puffT2.levitationAmplifier", 0);
                        PotionEffect effect =
                            new PotionEffect(
                                PotionEffectType.LEVITATION, index, remaining, false, false, false);
                        targetPlayer.addPotionEffect(effect, true);
                      }

                      if (this.random.nextDouble() < distance) {
                        int step =
                            ConfigValueCache.getInt(this.plugin, "puffT2.slowFallingDuration", 30);
                        int ticks =
                            ConfigValueCache.getInt(this.plugin, "puffT2.slowFallingAmplifier", 0);
                        PotionEffect currentEffect =
                            new PotionEffect(
                                PotionEffectType.SLOW_FALLING, step, ticks, false, false, false);
                        targetPlayer.addPotionEffect(currentEffect, true);
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
  }

  void playAbilityEffects(Player player, Entity entity) {
    if (!this.plugin.isGemsDisabled() && !this.isConditionMet(player.getWorld())) {
      if (!this.plugin.isGemsDisabled() && !this.isConditionMet(entity.getWorld())) {
        if (!this.plugin.canUseAbility(player)) {
          if (this.isTrustedPlayer(player, entity)) {
            player.sendMessage(ChatColor.RED + "You cannot use Breezy Bash on trusted players!");
          } else {
            player.sendMessage(
                "§f🔮 §bYou used §f☁ Breezy Bash §bon "
                    + (entity instanceof Player
                        ? ((Player) entity).getName()
                        : entity.getType().name().toLowerCase()));
            if (entity instanceof Player targetPlayer) {
              targetPlayer.sendMessage(
                  "§f🔮 §bYou have been affected by §f☁Breezy Bash"
                      + " §bby "
                      + player.getName());
            }

            long timestamp = this.getLastUseTime(this.getPuffT2BreezyBashCooldown(), player);
            this.cleanupAbilityState(player.getUniqueId(), this.cache, timestamp, "puff_breezy_bash_t2", 3);
            double configuredValue = ConfigValueCache.getDouble(this.plugin, "puffT2.breezyBashSingleUpPush", 3.0);
            double distance =
                ConfigValueCache.getDouble(this.plugin, "puffT2.breezyBashSingleDownPush", -3.0);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                Vector offset = new Vector(0.0, configuredValue, 0.0);
                entity.setVelocity(offset);

              }, 1L);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                Vector offset = new Vector(0.0, distance, 0.0);
                entity.setVelocity(offset);

              }, 25L);
            new BukkitRunnable() {
              int durationTicks = 0;

              @Override
              public void run() {
                if (this.durationTicks <= 120 && (!entity.isOnGround() || this.durationTicks <= 20)) {
                  Location location = player.getLocation().add(0.0, 1.0, 0.0);
                  Location targetLocation = entity.getLocation().add(0.0, 0.5, 0.0);
                  if (player.getWorld().equals(entity.getWorld())) {
                    player.getWorld().spawnParticle(Particle.SMOKE, location, 3, 0.2, 0.2, 0.2, 0.05);
                    player
                    .getWorld()
                    .spawnParticle(
                        Particle.DUST, targetLocation, 15, 0.0, 0.0, 0.0, new DustOptions(Color.WHITE, 2.0F));
                    player.getWorld().spawnParticle(Particle.CLOUD, targetLocation, 8, 0.0, 0.0, 0.0, 0.1);
                    Vector direction = targetLocation.toVector().subtract(location.toVector());
                    double value = direction.length();
                    if (value > 0.0 && value < 50.0) {
                      direction.normalize();

                      for (double distance = 0.0; distance < value; distance += 0.5) {
                        Location origin = location.clone().add(direction.clone().multiply(distance));
                        player.getWorld().spawnParticle(Particle.SMOKE, origin, 1, 0.1, 0.1, 0.1, 0.02);
                        player
                        .getWorld()
                        .spawnParticle(
                            Particle.DUST, origin, 2, 0.1, 0.1, 0.1, new DustOptions(Color.SILVER, 1.5F));
                      }
                    }
                  }

                  this.durationTicks++;
                } else {
                  this.cancel();
                }
              }
            }
                .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
            new BukkitRunnable() {
              final Entity capturedEntity = entity;
              int durationTicks = 0;
              static final String PUFF_T2_BREEZY_BASH_SINGLE_DAMAGE_ID = "puffT2.breezyBashSingleDamage";
              @Override
              public void run() {
                this.durationTicks++;
                if (this.durationTicks > 100 || this.capturedEntity.isOnGround()) {
                  if (this.capturedEntity.isOnGround()) {
                    Location location = this.capturedEntity.getLocation();
                    this.capturedEntity.getWorld().spawnParticle(Particle.SMOKE, location, 10, 1.0, 0.5, 1.0, 0.1);
                    this.capturedEntity.getWorld().spawnParticle(Particle.CLOUD, location, 15, 1.5, 0.5, 1.5, 0.2);
                    this.capturedEntity
                    .getWorld()
                    .spawnParticle(
                        Particle.DUST, location, 25, 1.5, 0.5, 1.5, new DustOptions(Color.WHITE, 2.5F));
                    double configuredValue = ConfigValueCache.getDouble(PuffTier2Gem.this.plugin, PUFF_T2_BREEZY_BASH_SINGLE_DAMAGE_ID, 2.0);
                    if (this.capturedEntity instanceof Player player) {
                      player.damage(configuredValue);
                    }
                  }

                  this.cancel();
                }
              }


            }.runTaskTimer(this.plugin, 2L, 1L);
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        if (!this.plugin.isGemsDisabled() && !this.isConditionMet(event.getEntity().getWorld())) {
          if (!this.plugin.canUseAbility(player)) {
            if (event.getBlock().getType() == Material.FARMLAND
                && (isAbilityBlocked(player.getInventory().getItemInMainHand())
                    || isAbilityBlocked(player.getInventory().getItemInOffHand()))) {
              event.setCancelled(true);
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    ItemStack item = event.getItemDrop().getItemStack();
    if (isAbilityBlocked(item)) {
      event.setCancelled(true);
    }
  }

  public void spawnAbilityEffects(Player player) {
    UUID playerId = player.getUniqueId();
    this.cache.invalidate(playerId);
    this.currentCache.invalidate(playerId);
    this.secondaryCache.invalidate(playerId);
    ActiveAbilityStore.removeAbility(playerId, "puff_breezy_bash_t2");
    ActiveAbilityStore.removeAbility(playerId, "puff_double_jump_t2");
    ActiveAbilityStore.removeAbility(playerId, "puff_dash_t2");
  }

  public void shutdown() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if ((player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
          && this.canUseAbility(player)) {
        player.setFlying(false);
        player.setAllowFlight(false);
      }
    }
    this.flagsByPlayer.clear();
    this.secondaryFlagsByPlayer.clear();
    this.playerLinks.clear();
    this.secondaryPlayerLinks.clear();
    this.secondaryCache.invalidateAll();
    this.activeCache.invalidateAll();
    this.cache.invalidateAll();
    this.currentCache.invalidateAll();
  }

  String formatDisplayTextForTarget(Cache<UUID, Long> cache) {
    if (cache == this.cache) {
      return "puff_breezy_bash_t2";
    } else if (cache == this.secondaryCache) {
      return "puff_double_jump_t2";
    } else {
      return cache == this.currentCache ? "puff_dash_t2" : null;
    }
  }

  void cleanupAbilityState(UUID playerId, Cache<UUID, Long> cache, long timestamp, String text, int count) {
    long lastUpdateTime = Math.max(0L, timestamp);
    if (lastUpdateTime > 0L && count > 0) {
      ActiveAbilityStore.startActive(playerId, text, count, (long) Math.ceil(lastUpdateTime / 1000.0));
      lastUpdateTime += count * 1000L;
    }

    cache.put(playerId, System.currentTimeMillis() + lastUpdateTime);
  }

  static {
    PUFF_BREEZY_BASH_T2_ID = "puff_breezy_bash_t2";
    PUFF_DASH_T2_ID = "puff_dash_t2";
    PUFF_DOUBLE_JUMP_T2_ID = "puff_double_jump_t2";
  }
}
