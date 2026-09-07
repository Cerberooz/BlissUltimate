package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PuffTier1Gem implements Listener {
  final Bliss plugin;
  AstraTier2Gem astraTier2Gem;
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> flagsByPlayer = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> secondaryFlagsByPlayer = new ConcurrentHashMap<>();
  final Cache<UUID, Long> cache = CacheBuilder.newBuilder().build();
  final Set<String> disabledWorldNames;

  public PuffTier1Gem(Bliss bliss) {
    this.plugin = bliss;
    this.disabledWorldNames = Set.copyOf(bliss.getConfig().getStringList("DisabledWorlds"));
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              if (!PuffTier1Gem.this.isAbilityActive(player.getUniqueId())
                  && PuffTier1Gem.this.canUseAbility(player)
                  && !PuffTier1Gem.this.cache.asMap().containsKey(player.getUniqueId())) {
                PuffTier1Gem.this.activateAbility(player);
                PuffTier1Gem.this.cache.put(player.getUniqueId(), System.currentTimeMillis() + 600000L);
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.5F, 1.3F);
                player.sendMessage("🔺 " + ChatColor.of("#82F3FF") + "Auto Enchanting");
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
              if (PuffTier1Gem.this.canUseAbility(player)) {
                if (PuffTier1Gem.this.isAbilityActive(player.getUniqueId())) {
                  long remainingMillis = PuffTier1Gem.this.getAbilityLong(player.getUniqueId());
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
                } else if (this.plugin.isGemsDisabled()
                    || PuffTier1Gem.this.isConditionMet(player.getWorld())
                    || this.plugin.canUseAbility(player)) {
                  String displayColor = ChatColor.DARK_GRAY.toString();
                  String textColor = ChatColor.BOLD.toString();
                  ActionBarQueue.enqueue(
                      player,
                      "🔒 " + displayColor + textColor + "ᴅɪꜱᴀʙʟᴇᴅ");
                }
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
              if (!this.plugin.isGemsDisabled()
                  && !PuffTier1Gem.this.isConditionMet(player.getWorld())
                  && !this.plugin.canUseAbility(player)
                  && !PuffTier1Gem.this.isAbilityActive(player.getUniqueId())
                  && PuffTier1Gem.this.canUseAbility(player)) {
                Long expiryTimestamp = PuffTier1Gem.this.cache.getIfPresent(player.getUniqueId());
                if (expiryTimestamp != null) {
                  long now = expiryTimestamp - System.currentTimeMillis();
                  if (now > 0L) {
                    String textColor = ChatColor.WHITE.toString();
                    String messageColor = ChatColor.AQUA.toString();
                    String text = PuffTier1Gem.this.formatDisplayText(now, false);
                    ActionBarQueue.enqueue(player, textColor + "🔺 " + messageColor + text);
                  } else {
                    PuffTier1Gem.this.cache.invalidate(player.getUniqueId());
                    ActionBarQueue.enqueue(
                        player, ChatColor.WHITE + "🔺 " + ChatColor.GREEN + "Ready!");
                  }
                } else {
                  ActionBarQueue.enqueue(
                      player, ChatColor.WHITE + "🔺 " + ChatColor.GREEN + "Ready!");
                }
              }
            }
          }
        }, bliss, SharedScheduler.staggeredInitialDelay(20L), 20L);
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            PuffTier1Gem.this.flagsByPlayer.entrySet().removeIf(this::isAbilityAllowed);
            PuffTier1Gem.this.secondaryFlagsByPlayer.entrySet().removeIf(this::shouldRemoveExpiredEntry);
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

  public void setAstraTier2Gem(AstraTier2Gem astraTier2Gem) {
    this.astraTier2Gem = astraTier2Gem;
  }

  boolean isAbilityActive(UUID playerId) {
    return this.astraTier2Gem != null && this.astraTier2Gem.hasRequiredState(playerId);
  }

  long getAbilityLong(UUID playerId) {
    return this.astraTier2Gem == null ? 0L : this.astraTier2Gem.getAbilityLong(playerId);
  }

  boolean isConditionMet(World world) {
    return this.disabledWorldNames.contains(world.getName());
  }

  boolean canUseAbility(Player player) {
    return isGemItem(player.getInventory().getItemInMainHand())
        || isGemItem(player.getInventory().getItemInOffHand());
  }

  boolean isAbilityAllowed(Player player) {
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
      if (player.getLocation().distanceSquared(location) <= distance && this.isAbilityAllowed(player)) {
        return true;
      }
    }

    return false;
  }

  void activateAbility(Player player) {
    ItemStack item = player.getInventory().getBoots();
    if (item != null && item.getType() != Material.AIR) {
      if (item.containsEnchantment(Enchantment.FEATHER_FALLING)
          && item.getEnchantmentLevel(Enchantment.FEATHER_FALLING) >= 3) {
        return;
      }

      item.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, 3);
    }

    for (ItemStack heldItem : player.getInventory().getContents()) {
      if (heldItem != null && heldItem.getType() == Material.BOW) {
        if (heldItem.containsEnchantment(Enchantment.POWER)
            && heldItem.getEnchantmentLevel(Enchantment.POWER) > 3) {
          return;
        }

        if (heldItem.containsEnchantment(Enchantment.PUNCH)
            && heldItem.getEnchantmentLevel(Enchantment.PUNCH) >= 1) {
          return;
        }

        heldItem.addUnsafeEnchantment(Enchantment.POWER, 3);
        heldItem.addUnsafeEnchantment(Enchantment.PUNCH, 1);
      }
    }
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
      itemMeta.setCustomModelData(5);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
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
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static boolean isGemItem(ItemStack item) {
    if (item == null) {
      return false;
    }

    if (item.getType() != Material.AMETHYST_SHARD) {
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
    return count == 5 || count == 25 || count == 45 || count == 65 || count == 85;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (player.isSneaking()) {
      if (!this.plugin.isGemsDisabled()
          && !this.isConditionMet(player.getWorld())
          && !this.plugin.canUseAbility(player)) {
        if (!this.isAbilityActive(playerId)) {
          if (this.canUseAbility(player)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                || event.getAction() == Action.RIGHT_CLICK_AIR) {
              long now = System.currentTimeMillis();
              Long storedTimestamp = this.lastUseTimes.get(playerId);
              if (storedTimestamp != null && now - storedTimestamp <= 500L) {
                this.lastUseTimes.remove(playerId);
                if (!this.cache.asMap().containsKey(player.getUniqueId())) {
                  this.activateAbility(player);
                  this.cache.put(player.getUniqueId(), System.currentTimeMillis() + 600000L);
                  player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.5F, 1.3F);
                  player.sendMessage("🔺 " + ChatColor.of("#82F3FF") + "Auto Enchanting");
                }
              } else {
                this.lastUseTimes.put(playerId, now);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
      if (this.plugin.isGemsDisabled()
          || this.isConditionMet(player.getWorld())
          || this.plugin.canUseAbility(player)) {
        player.setAllowFlight(false);
        player.setFlying(false);
      } else if (this.isAbilityActive(playerId)) {
        player.setAllowFlight(false);
        player.setFlying(false);
      } else if (this.canUseAbility(player)) {
        if (!CooldownService.isOnCooldown(playerId, "puff_jump") && !this.secondaryFlagsByPlayer.containsKey(playerId)) {
          if (player.isOnGround()) {
            player.setAllowFlight(true);
          }
        } else {
          player.setAllowFlight(false);
          player.setFlying(false);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
      if (this.canUseAbility(player)) {
        if (this.plugin.isGemsDisabled()
            || this.isConditionMet(player.getWorld())
            || this.plugin.canUseAbility(player)) {
          event.setCancelled(true);
          player.setAllowFlight(false);
          player.setFlying(false);
        } else if (this.isAbilityActive(playerId)) {
          event.setCancelled(true);
          player.setAllowFlight(false);
          player.setFlying(false);
        } else if (CooldownService.isOnCooldown(playerId, "puff_jump")) {
          event.setCancelled(true);
          player.setAllowFlight(false);
          player.setFlying(false);
        } else {
          event.setCancelled(true);
          player.setAllowFlight(false);
          player.setFlying(false);
          player.setFallDistance(0.0F);
          Location location = player.getLocation();
          player.getWorld().spawnParticle(Particle.CLOUD, location, 25, 0.5, 0.2, 0.5, 0.0);
          player.getWorld().spawnParticle(Particle.GUST, location, 1);
          this.flagsByPlayer.put(playerId, true);
          this.secondaryFlagsByPlayer.put(playerId, true);
          double configuredValue = ConfigValueCache.getDouble(this.plugin, "puffT1.doubleJumpHorizontal", 0.9);
          double distance = ConfigValueCache.getDouble(this.plugin, "puffT1.doubleJumpVertical", 0.8);
          Vector offset = player.getLocation().getDirection().multiply(configuredValue).setY(distance);
          player.setVelocity(offset);
          int index = ConfigValueCache.getInt(this.plugin, "puffT1.jumpCooldown", 5);
          String accentColor = ChatColor.of("#82F3FF").toString();
          String textColor = ChatColor.WHITE.toString();
          String messageColor = ChatColor.of("#82F3FF").toString();
          String displayColor = ChatColor.WHITE.toString();
          player.sendMessage(
              "🔮 "
                  + accentColor
                  + "You have used "
                  + textColor
                  + "Double Jump "
                  + messageColor
                  + "now on cooldown for "
                  + displayColor
                  + index
                  + "s");
          CooldownService.setCooldown(playerId, "puff_jump", index);
          new BukkitRunnable() {
            final Player capturedPlayer = player;
            final UUID capturedPlayerId = playerId;
            int durationTicks = 0;
            static final String PUFF_JUMP_ID = "puff_jump";
            @Override
            public void run() {
              if (!this.capturedPlayer.isOnline()) {
                PuffTier1Gem.this.flagsByPlayer.remove(this.capturedPlayerId);
                PuffTier1Gem.this.secondaryFlagsByPlayer.remove(this.capturedPlayerId);
                this.cancel();
              } else if (PuffTier1Gem.this.plugin.isGemsDisabled()
                  || PuffTier1Gem.this.isConditionMet(this.capturedPlayer.getWorld())
                  || PuffTier1Gem.this.plugin.canUseAbility(this.capturedPlayer)
                  || PuffTier1Gem.this.isAbilityActive(this.capturedPlayerId)) {
                PuffTier1Gem.this.flagsByPlayer.remove(this.capturedPlayerId);
                PuffTier1Gem.this.secondaryFlagsByPlayer.remove(this.capturedPlayerId);
                this.capturedPlayer.setAllowFlight(false);
                this.capturedPlayer.setFlying(false);
                this.cancel();
              } else if (this.durationTicks < 100 && !this.capturedPlayer.isOnGround()) {
                Location location = this.capturedPlayer.getLocation().clone().add(0.0, -0.5, 0.0);
                this.capturedPlayer.getWorld().spawnParticle(Particle.CLOUD, location, 3, 0.1, 0.1, 0.1, 0.0);
                this.durationTicks++;
              } else {
                PuffTier1Gem.this.flagsByPlayer.remove(this.capturedPlayerId);
                PuffTier1Gem.this.secondaryFlagsByPlayer.remove(this.capturedPlayerId);
                if (this.capturedPlayer.isOnGround()
                    && (this.capturedPlayer.getGameMode() == GameMode.SURVIVAL
                    || this.capturedPlayer.getGameMode() == GameMode.ADVENTURE)
                    && !CooldownService.isOnCooldown(this.capturedPlayerId, PUFF_JUMP_ID)
                    && PuffTier1Gem.this.canUseAbility(this.capturedPlayer)) {
                  this.capturedPlayer.setAllowFlight(true);
                }

                this.cancel();
              }
            }


          }
              .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(1L), 1L);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onBlockReceiveGame(BlockReceiveGameEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (!this.plugin.isGemsDisabled()
          && !this.isConditionMet(player.getWorld())
          && !this.plugin.canUseAbility(player)) {
        if (!this.isAbilityActive(player.getUniqueId())) {
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
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (event.getCause() == DamageCause.FALL) {
        if (!this.isAbilityActive(player.getUniqueId())) {
          if (!this.plugin.isGemsDisabled()
              && !this.isConditionMet(player.getWorld())
              && !this.plugin.canUseAbility(player)) {
            if (this.canUseAbility(player)) {
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

  @EventHandler(priority = EventPriority.LOW)
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if (event.getEntity() instanceof Player player) {
      if (!this.plugin.isGemsDisabled()
          && !this.isConditionMet(player.getWorld())
          && !this.plugin.canUseAbility(player)) {
        if (!this.isAbilityActive(player.getUniqueId())) {
          if (event.getBlock().getType() == Material.FARMLAND) {
            if (this.canUseAbility(player)) {
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
    if (isGemItem(item)) {
      event.setCancelled(true);
    }
  }

  public void shutdown() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if ((player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
          && this.canUseAbility(player)) {
        player.setFlying(false);
        player.setAllowFlight(false);
      }
    }
    this.lastUseTimes.clear();
    this.flagsByPlayer.clear();
    this.secondaryFlagsByPlayer.clear();
    this.cache.invalidateAll();
  }
}
