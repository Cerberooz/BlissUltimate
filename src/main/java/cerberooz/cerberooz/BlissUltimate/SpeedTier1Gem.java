package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.Map;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpeedTier1Gem implements Listener {
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  final Map<UUID, Boolean> flagsByPlayer = new ConcurrentHashMap<>();
  Cache<UUID, Long> cache =
      CacheBuilder.newBuilder().expireAfterWrite(60000L, TimeUnit.SECONDS).build();
  Bliss plugin;
  int durationTicks;
  AstraTier2Gem astraTier2Gem;

  public Cache<UUID, Long> getCache() {
    return this.cache;
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

  public SpeedTier1Gem(Bliss bliss) {
    this.plugin = bliss;
    this.durationTicks = ConfigValueCache.getInt(bliss, "speedT1.speedGemDolphinsGraceLvl", 0);
    this.startBackgroundTasks();
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          final Bliss plugin = bliss;

          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              if (!this.plugin.isGemsDisabled()
                 
                  && !this.plugin.canUseAbility(player)) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (SpeedTier1Gem.isProtectedTarget(mainHandItem) || SpeedTier1Gem.isProtectedTarget(offHandItem)) {
                  UUID playerId = player.getUniqueId();
                  if (SpeedTier1Gem.this.isAbilityActive(playerId)) {
                    long timestamp = SpeedTier1Gem.this.getAbilityLong(playerId);
                    long lastUpdateTime = timestamp / 1000L;
                    String darkGray = ChatColor.DARK_GRAY.toString();
                    String bold = ChatColor.BOLD.toString();
                    ActionBarQueue.enqueue(
                        player,
                        "🔒 "
                        + darkGray
                        + bold
                        + "ᴅɪꜱᴀʙʟᴇᴅ: "
                        + lastUpdateTime);
                  } else {
                    if (player.getLocation().getBlock().getType() != Material.WATER) {
                      player.addPotionEffect(
                          new PotionEffect(PotionEffectType.SPEED, 20, 0, true, true, true));
                    }

                    if (CooldownService.isOnCooldown(playerId, "speed_thunder")) {
                      String displayText =
                      AbilityStatusFormatter.formatDisplayText(playerId, "speed_thunder", false);
                      String textColor = ChatColor.YELLOW.toString();
                      String labelColor = ChatColor.AQUA.toString();
                      ActionBarQueue.enqueue(player, textColor + "🔺 " + labelColor + displayText);
                    } else {
                      ActionBarQueue.enqueue(
                          player, ChatColor.YELLOW + "🔺 " + ChatColor.GREEN + "Ready!");
                    }

                    for (ItemStack item : player.getInventory().getContents()) {
                      if (item != null) {
                        Material material = item.getType();
                        if (material == Material.IRON_PICKAXE
                            || material == Material.GOLDEN_PICKAXE
                            || material == Material.DIAMOND_PICKAXE
                            || material == Material.NETHERITE_PICKAXE) {
                          int count = item.getEnchantmentLevel(Enchantment.EFFICIENCY);
                          if (count <= 2) {
                            item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 2);
                          }
                        }
                      }
                    }

                    if (player.getLocation().getBlock().getType() == Material.WATER && SpeedTier1Gem.this.durationTicks >= 0) {
                      player.addPotionEffect(
                          new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, SpeedTier1Gem.this.durationTicks, false, true));
                    }
                  }
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

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              UUID playerId = player.getUniqueId();
              if (!SpeedTier1Gem.this.plugin.isGemsDisabled()
                 
                  && !SpeedTier1Gem.this.plugin.canUseAbility(player)) {
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (SpeedTier1Gem.isProtectedTarget(mainHandItem) || SpeedTier1Gem.isProtectedTarget(offHandItem)) {
                  SpeedTier1Gem.this.activateAbility(player, playerId);
                }
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(100L), 100L);
  }

  void activateAbility(Player player, UUID playerId) {
    long now = System.currentTimeMillis();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        for (ItemStack item : player.getInventory().getContents()) {
          if (item != null && item.getType() != Material.AIR) {
            if ((this.isGemItem(item) || this.isAbilityAllowed(item))
                && !item.containsEnchantment(Enchantment.EFFICIENCY)) {
              item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 3);
            }

            if (this.isAbilityBlocked(item)
                && item.getEnchantmentLevel(Enchantment.SOUL_SPEED) < 3) {
              item.addUnsafeEnchantment(Enchantment.SOUL_SPEED, 3);
            }
          }
        }
      }
    }
  }

  boolean isGemItem(ItemStack item) {
    return item.getType().name().endsWith("_PICKAXE");
  }

  boolean isAbilityAllowed(ItemStack item) {
    return item.getType().name().endsWith("_AXE");
  }

  boolean isAbilityBlocked(ItemStack item) {
    return item.getType().name().endsWith("_BOOTS");
  }

  boolean isMatchingState(ItemStack item) {
    return item.getType().name().endsWith("_SWORD");
  }

  boolean isValidTarget(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Material material = item.getType();
      return material == Material.FIREWORK_ROCKET
              || material == Material.WIND_CHARGE
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

  public ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(7);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.of("#befff7") + "Energy:");
      lore.add(ChatColor.of("#82EDBF") + "Pristine");
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      lore.add("");
      lore.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Speed");
      lore.add(ChatColor.GRAY + "- Dolphins Grace");
      lore.add(ChatColor.GRAY + "- Enchants Efficiency");
      lore.add(ChatColor.GRAY + "- Enchants Soul Speed");
      lore.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      lore.add(ChatColor.DARK_GRAY + "  Teleport 6 blocks forward");
      lore.add(ChatColor.DARK_GRAY + "  Cooldown: 15s");
      lore.add(ChatColor.DARK_GRAY + "  First hit: -5s cooldown");
      lore.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static boolean isProtectedTarget(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      if (item.getType() != Material.AMETHYST_SHARD) {
        return false;
      } else {
        return itemMeta.hasCustomModelData()
                && (itemMeta.getCustomModelData() == 7
                    || itemMeta.getCustomModelData() == 87
                    || itemMeta.getCustomModelData() == 67
                    || itemMeta.getCustomModelData() == 47
                    || itemMeta.getCustomModelData() == 27)
            ? itemMeta.hasDisplayName()
                && ChatColor.stripColor(itemMeta.getDisplayName())
                    .equalsIgnoreCase("ѕᴘᴇᴇᴅ ɢᴇᴍ")
            : false;
      }
    } else {
      return false;
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    ItemStack item = event.getItemDrop().getItemStack();
    if (isProtectedTarget(item)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (event.getEntity() instanceof Player) {
        UUID playerId = player.getUniqueId();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (isProtectedTarget(mainHandItem) || isProtectedTarget(offHandItem)) {
          if (CooldownService.isOnCooldown(playerId, "speed_thunder")) {
            if (!this.flagsByPlayer.getOrDefault(playerId, false)) {
              double value = CooldownService.remainingMillis(playerId, "speed_thunder");
              int count = (int) (Math.max(1000.0, value - 5000.0) / 1000.0);
              CooldownService.setCooldown(playerId, "speed_thunder", count);
              this.flagsByPlayer.put(playerId, true);
              event.getEntity()
                  .getWorld()
                  .spawnParticle(
                      Particle.CRIT,
                      event.getEntity().getLocation().add(0.0, 1.0, 0.0),
                      20,
                      0.5,
                      0.5,
                      0.5,
                      0.1);
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (player.isSneaking()) {
      UUID playerId = player.getUniqueId();
      if (!this.plugin.isGemsDisabled()
         ) {
        if (!this.plugin.canUseAbility(player)) {
          if (!this.isAbilityActive(playerId)) {
            if ((event.getAction() == Action.RIGHT_CLICK_AIR
                    || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && isProtectedTarget(player.getInventory().getItemInOffHand())) {
              if (this.isValidTarget(player.getInventory().getItemInMainHand())) {
                return;
              }

              if (!CooldownService.isOnCooldown(playerId, "speed_thunder")) {
                Location location = player.getLocation().clone();
                Location targetLocation = player.getLocation().clone().add(0.0, 1.0, 0.0);
                this.spawnAbilityParticles(targetLocation);
                Vector offset = player.getLocation().getDirection().normalize();
                Location origin = player.getLocation().add(offset.multiply(6));
                origin = this.getTargetLocation(player, location, origin, 8.0);
                player.getWorld().playSound(location, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0F, 1.0F);
                this.refreshAbilityState(player, origin);
                player.sendMessage(
                    ChatColor.YELLOW
                        + "🔺 "
                        + ChatColor.YELLOW
                        + "You have used "
                        + ChatColor.of("#ED8790")
                        + "Thunder Step");
                this.flagsByPlayer.put(playerId, false);
                int count = player.getInventory().contains(Material.DRAGON_EGG) ? 7 : 15;
                CooldownService.setCooldown(playerId, "speed_thunder", count);
              } else {
                String text =
                    AbilityStatusFormatter.formatDisplayText(playerId, "speed_thunder", false);
                player.sendMessage(
                    ChatColor.YELLOW
                        + "🔺 §cYour "
                        + ChatColor.of("#ED8790")
                        + "Thunder Step §cis on cooldown for§c "
                        + text);
              }
            }
          }
        }
      }
    }
  }

  Location getTargetLocation(Player player, Location location, Location targetLocation, double value) {
    Vector offset = player.getLocation().getDirection().clone();
    offset.setY(0);
    offset.normalize();
    Location origin = location.clone();
    boolean enabled = false;
    double distance = 0.5;

    for (double radius = distance; radius <= value; radius += distance) {
      Location center = location.clone().add(offset.clone().multiply(radius));
      Location destination = center.clone();
      destination.setY(location.getY());
      Location effectLocation = destination.clone().add(0.0, 1.0, 0.0);
      if (destination.getBlock().getType().isSolid() || effectLocation.getBlock().getType().isSolid()) {
        enabled = true;
        break;
      }

      origin = center.clone();
    }

    if (!enabled) {
      origin = location.clone().add(offset.clone().multiply(value));
    }

    origin.setY(this.getAbilityDoubleValue(origin));
    return origin;
  }

  void spawnAbilityParticles(Location location) {
    byte step = 40;
    double value = 1.5;

    for (int count = 0; count < step; count++) {
      double distance = (Math.PI * 2) * count / step;
      double offset = location.getY() + value * Math.sin(distance);
      double radius = location.getZ() + value * Math.cos(distance);
      Location effectLocation = new Location(location.getWorld(), location.getX(), offset, radius);
      location.getWorld().spawnParticle(Particle.CLOUD, effectLocation, 1, 0.0, 0.0, 0.0, 0.0);
    }
  }

  void refreshAbilityState(Player player, Location location) {
    if (player != null && player.isOnline()) {
      player.teleport(location);
      DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 255, 0), 1.5F);

      for (int count = 0; count < 25; count++) {
        double value = (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0;
        double distance = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 2.0;
        double radius = (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0;
        location.getWorld().spawnParticle(Particle.DUST, location.clone().add(value, distance, radius), 1, dustOptions);
      }
    }
  }

  double getAbilityDoubleValue(Location location) {
    Location targetLocation = location.clone();

    for (int count = 0; count < 5; count++) {
      if (targetLocation.getBlock().getType().isSolid()
          && !targetLocation.clone().add(0.0, 1.0, 0.0).getBlock().getType().isSolid()
          && !targetLocation.clone().add(0.0, 2.0, 0.0).getBlock().getType().isSolid()) {
        return targetLocation.getY() + 1.0;
      }

      targetLocation.add(0.0, -1.0, 0.0);
    }

    targetLocation = location.clone();

    for (int index = 0; index < 5; index++) {
      if (targetLocation.getBlock().getType().isSolid()
          && !targetLocation.clone().add(0.0, 1.0, 0.0).getBlock().getType().isSolid()
          && !targetLocation.clone().add(0.0, 2.0, 0.0).getBlock().getType().isSolid()) {
        return targetLocation.getY() + 1.0;
      }

      targetLocation.add(0.0, 1.0, 0.0);
    }

    return location.getY();
  }

  public void updateAbilityState(Player player) {
    UUID playerId = player.getUniqueId();
    this.cache.invalidate(playerId);
  }
}
