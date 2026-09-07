package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
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
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class LifeTier1Gem implements Listener {
  static final String LIFE_VITALITY_ID;
  final Map<UUID, Long> lastUseTimes = new ConcurrentHashMap<>();
  final Cache<UUID, Long> cache =
      CacheBuilder.newBuilder().expireAfterWrite(40L, TimeUnit.SECONDS).build();
  final Map<UUID, Integer> abilityStages = new ConcurrentHashMap<>();
  final Map<UUID, Integer> playerCounters = new ConcurrentHashMap<>();
  final Set<Location> trackedLocations = ConcurrentHashMap.newKeySet();
  boolean active = false;
  final Map<Location, Material> secondaryMaterialsByLocation = new ConcurrentHashMap<>();
  final Map<Location, Material> currentMaterialsByLocation = new ConcurrentHashMap<>();
  final Map<Location, Material> materialsByLocation = new ConcurrentHashMap<>();
  final Map<Location, Material> activeMaterialsByLocation = new ConcurrentHashMap<>();
  Bliss plugin;
  TrustCommand trustCommand;
  AstraTier2Gem astraTier2Gem;
  private final Runnable actionBarRenderer = this::updateActionBar;

  public Cache<UUID, Long> getCache() {
    return this.cache;
  }

  public void setTrustCommand(TrustCommand trustCommand) {
    this.trustCommand = trustCommand;
  }

  boolean isTrustedPlayer(Player player, Player targetPlayer) {
    return this.trustCommand != null && this.trustCommand.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId());
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

  public LifeTier1Gem(Bliss bliss) {
    this.plugin = bliss;
    this.initialize();
    this.startBackgroundTasks();
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          final Bliss plugin = bliss;

          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              if ((LifeTier1Gem.shouldApplyEffect(mainHandItem) || LifeTier1Gem.shouldApplyEffect(offHandItem))
                  && LifeTier1Gem.this.isAbilityActive(player.getUniqueId())) {
                long remainingMillis = LifeTier1Gem.this.getAbilityLong(player.getUniqueId());
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
                if ((this.plugin.isGemsDisabled())
                    && (LifeTier1Gem.shouldApplyEffect(mainHandItem) || LifeTier1Gem.shouldApplyEffect(offHandItem))) {
                  String displayColor = ChatColor.DARK_GRAY.toString();
                  String textColor = ChatColor.BOLD.toString();
                  ActionBarQueue.enqueue(
                      player,
                      "🔒 " + displayColor + textColor + "ᴅɪꜱᴀʙʟᴇᴅ");
                }

                if (this.plugin.canUseAbility(player)
                    && (LifeTier1Gem.shouldApplyEffect(mainHandItem) || LifeTier1Gem.shouldApplyEffect(offHandItem))) {
                  String nameColor = ChatColor.DARK_GRAY.toString();
                  String configColor = ChatColor.BOLD.toString();
                  ActionBarQueue.enqueue(
                      player,
                      "🔒 " + nameColor + configColor + "ᴅɪꜱᴀʙʟᴇᴅ");
                }
              }
            }
          }
        }, bliss, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
              ItemStack mainHandItem = player.getInventory().getItemInMainHand();
              ItemStack offHandItem = player.getInventory().getItemInOffHand();
              UUID playerId = player.getUniqueId();
              if (LifeTier1Gem.this.isAbilityActive(playerId)) {
                return;
              }

              if (LifeTier1Gem.shouldApplyEffect(mainHandItem) || LifeTier1Gem.shouldApplyEffect(offHandItem)) {
                LifeTier1Gem.this.activateAbility(player);
              }
            }
          }
        }, this.plugin, SharedScheduler.staggeredInitialDelay(20L), 20L);
  }

  void activateAbility(Player player) {
    long now = System.currentTimeMillis();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
              if ((this.isValidTarget(item) || this.isProtectedTarget(item))
                  && !item.containsEnchantment(Enchantment.UNBREAKING)
                  && !item.containsEnchantment(Enchantment.UNBREAKING)) {
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
              }

              if (this.isGemItem(item)
                  && !item.containsEnchantment(Enchantment.UNBREAKING)) {
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
              }

              if (this.isAbilityAllowed(item)
                  && !item.containsEnchantment(Enchantment.UNBREAKING)) {
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
              }

              if (this.isAbilityBlocked(item)
                  && !item.containsEnchantment(Enchantment.UNBREAKING)) {
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
              }

              if (this.isMatchingState(item)
                  && !item.containsEnchantment(Enchantment.UNBREAKING)) {
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
              }

              if (this.isActiveForPlayer(item)
                  && !item.containsEnchantment(Enchantment.UNBREAKING)) {
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
              }
            }
          }
        }
      }
    }
  }

  boolean isGemItem(ItemStack item) {
    return item.getType().name().endsWith("_HELMET");
  }

  boolean isAbilityAllowed(ItemStack item) {
    return item.getType().name().endsWith("_CHESTPLATE");
  }

  boolean isAbilityBlocked(ItemStack item) {
    return item.getType().name().endsWith("_LEGGINGS");
  }

  boolean isMatchingState(ItemStack item) {
    return item.getType().name().endsWith("_BOOTS");
  }

  boolean isValidTarget(ItemStack item) {
    return item.getType().name().endsWith("_PICKAXE");
  }

  boolean isProtectedTarget(ItemStack item) {
    return item.getType().name().endsWith("_AXE");
  }

  boolean isActiveForPlayer(ItemStack item) {
    return item.getType().name().endsWith("_SWORD");
  }

  boolean canUseAbility(Player player) {
    return shouldApplyEffect(player.getInventory().getItemInOffHand());
  }

  int getAbilityIntValue(Player player) {
    return this.playerCounters.getOrDefault(player.getUniqueId(), 0);
  }

  int getDurationTicks(Player player) {
    return this.abilityStages.getOrDefault(player.getUniqueId(), 10);
  }

  boolean hasRequiredState(Player player) {
    return this.cache.getIfPresent(player.getUniqueId()) != null;
  }

  void refreshAbilityState(Player player, long timestamp) {
    this.cache.put(player.getUniqueId(), System.currentTimeMillis() + timestamp);
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

  void initialize() {
    ActionBarQueue.registerRenderer(this.actionBarRenderer);
  }

  public void shutdown() {
    ActionBarQueue.unregisterRenderer(this.actionBarRenderer);
    this.scheduleAbilityUpdate();
    this.lastUseTimes.clear();
    this.cache.invalidateAll();
    this.abilityStages.clear();
    this.playerCounters.clear();
  }

  public ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(3);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(ChatColor.of("#befff7") + "Energy:");
      lore.add(ChatColor.of("#82EDBF") + "Pristine");
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏꜰ"
              + " ʟɪꜰᴇ");
      lore.add("");
      lore.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Green Thumb");
      lore.add(ChatColor.GRAY + "- Radiant Fist");
      lore.add(ChatColor.GRAY + "- Bonus Saturation");
      lore.add(ChatColor.GRAY + "- Bonus Absorption");
      lore.add(ChatColor.GRAY + "- Wither Immune");
      lore.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      lore.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static boolean shouldApplyEffect(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      if (item.getType() != Material.AMETHYST_SHARD) {
        return false;
      } else {
        return itemMeta.hasCustomModelData()
                && (itemMeta.getCustomModelData() == 3
                    || itemMeta.getCustomModelData() == 83
                    || itemMeta.getCustomModelData() == 63
                    || itemMeta.getCustomModelData() == 43
                    || itemMeta.getCustomModelData() == 23)
            ? itemMeta.hasDisplayName()
                && ChatColor.stripColor(itemMeta.getDisplayName())
                    .equalsIgnoreCase("ʟɪғᴇ ɢᴇᴍ")
            : false;
      }
    } else {
      return false;
    }
  }

  boolean canAffectTarget(ItemStack item) {
    if (item == null) {
      return true;
    }

    Material material = item.getType();
    return material.toString().contains("SWORD")
        || material.toString().contains("AXE")
        || material == Material.AIR;
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.isAbilityActive(playerId)) {
        if (!this.plugin.isGemsDisabled()
           ) {
          if (!this.plugin.canUseAbility(player)) {
            boolean mainHandItem =
                shouldApplyEffect(player.getInventory().getItemInMainHand())
                    || shouldApplyEffect(player.getInventory().getItemInOffHand());
            if (mainHandItem) {
              if (event.getCause() == DamageCause.WITHER) {
                event.setCancelled(true);
              }

              if (player.hasPotionEffect(PotionEffectType.WITHER)) {
                player.removePotionEffect(PotionEffectType.WITHER);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    ItemStack item = event.getItemDrop().getItemStack();
    if (shouldApplyEffect(item)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
    Player player = event.getPlayer();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        UUID playerId = player.getUniqueId();
        if (!this.isAbilityActive(playerId)) {
          boolean mainHandItem =
              shouldApplyEffect(player.getInventory().getItemInMainHand())
                  || shouldApplyEffect(player.getInventory().getItemInOffHand());
          if (event.getItem().getType() == Material.GOLDEN_APPLE && mainHandItem) {
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1, true, true, true));
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.REGENERATION, 100, 1, true, true, true));
            player.setSaturation(player.getFoodLevel());
          }

          if (event.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE && mainHandItem) {
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.REGENERATION, 400, 1, true, true, true));
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.RESISTANCE, 6000, 0, true, true, true));
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0, true, true, true));
            player.addPotionEffect(
                new PotionEffect(PotionEffectType.ABSORPTION, 6000, 4, true, true, true));
            player.setSaturation(player.getFoodLevel());
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      Player player = event.getPlayer();
      if (player.isSneaking()) {
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (shouldApplyEffect(offHandItem)) {
          if (player.isSneaking()) {
            Block block = event.getClickedBlock();
            if (block != null) {
              if (block.getBlockData() instanceof Ageable ageable
                  && ageable.getAge() < ageable.getMaximumAge()) {
                ageable.setAge(ageable.getMaximumAge());
                block.setBlockData(ageable);
                event.setCancelled(true);
                block.getWorld()
                    .spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        block.getLocation().add(0.5, 0.5, 0.5),
                        10,
                        1.0,
                        1.0,
                        1.0);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void updateAbilityState(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (!this.plugin.isGemsDisabled()) {
      if (!this.plugin.canUseAbility(player)) {
        if (!this.isAbilityActive(playerId)) {
          if (player.isSneaking()) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
              if (!this.canUseAbility(player)) {
                return;
              }

              if (!this.canAffectTarget(player.getInventory().getItemInMainHand())) {
                return;
              }

              if (CooldownService.isOnCooldown(playerId, "life_vitality")) {
                long timestamp = CooldownService.remainingMillis(playerId, "life_vitality");
                if (ActiveAbilityStore.isActive(player.getUniqueId(), "life_vitality")) {
                  return;
                }

                String accentColor = ChatColor.of("#FE04B4").toString();
                String red = ChatColor.RED.toString();
                String messageColor = ChatColor.of("#FE04B4").toString();
                String displayColor = ChatColor.RED.toString();
                String gold = ChatColor.GOLD.toString();
                String text = AbilityStatusFormatter.formatDisplayTextForPlayer(timestamp, false);
                player.sendMessage(
                    accentColor
                        + "🔮 "
                        + red
                        + "Your "
                        + messageColor
                        + "Vitality Vortex "
                        + displayColor
                        + "skill is on cooldown for "
                        + gold
                        + text);
                return;
              }

              this.applyAbilityEffects(player);
            }
          }
        }
      }
    }
  }

  void applyAbilityEffects(Player player) {
    player.sendMessage(
        ChatColor.of("#FE04B4")
            + "🔮 "
            + ChatColor.RED
            + "You have activated "
            + ChatColor.of("#FE04B4")
            + "Vitality Vortex");
    long timestamp = ConfigValueCache.getInt(this.plugin, "lifeT1.vitalityCooldown", 40);
    if (player.getInventory().contains(Material.DRAGON_EGG)) {
      timestamp /= 2L;
    }

    long lastUpdateTime = ConfigValueCache.getInt(this.plugin, "lifeT1.vitalityActiveSeconds", 9);
    ActiveAbilityStore.startActive(player.getUniqueId(), "life_vitality", lastUpdateTime, timestamp);
    this.active = true;
    Location location = player.getLocation().clone();
    player.setFoodLevel(20);
    player.setSaturation(20.0F);
    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, true, true));
    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 1, true, true, true));

    for (Player targetPlayer : player.getWorld().getPlayers()) {
      if (!targetPlayer.equals(player)
          && player.getLocation().distance(targetPlayer.getLocation()) <= 5.0
          && this.isTrustedPlayer(player, targetPlayer)) {
        targetPlayer.setFoodLevel(20);
        targetPlayer.setSaturation(20.0F);
        targetPlayer.addPotionEffect(
            new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, true, true));
        targetPlayer.addPotionEffect(
            new PotionEffect(PotionEffectType.ABSORPTION, 600, 1, true, true, true));
        String accentColor = ChatColor.of("#FE04B4").toString();
        String messageColor = ChatColor.of("#befff7").toString();
        String displayColor = ChatColor.of("#FE04B4").toString();
        String name = player.getName();
        targetPlayer.sendMessage(
            accentColor
                + "🔮 "
                + messageColor
                + "You received Vitality effects from "
                + displayColor
                + name);
      }
    }

    new BukkitRunnable() {
      final Location anchorLocation = location;
      final Player capturedPlayer = player;
      int durationTicks = 1;
      final int maxCount = 5;

      @Override
      public void run() {
        if (this.durationTicks <= 5) {
          LifeTier1Gem.this.playAbilityEffects(this.anchorLocation, this.durationTicks, this.capturedPlayer);
          LifeTier1Gem.this.spawnAbilityParticles(this.anchorLocation, this.durationTicks);
          this.durationTicks++;
        } else {
          BukkitRunnable ambientAbilityTickTask = new BukkitRunnable() {
            double effectRadius = 0.0;

            @Override
            public void run() {
              if (player.isOnline() && LifeTier1Gem.this.active) {
                LifeTier1Gem.this.spawnAbilityEffects(anchorLocation, 4.5, this.effectRadius);
                this.effectRadius += 8.0;
              } else {
                this.cancel();
              }
            }
          };
          ambientAbilityTickTask.runTaskTimer(LifeTier1Gem.this.plugin, SharedScheduler.staggeredInitialDelay(10L), 10L);
          Bukkit.getScheduler()
          .runTaskLater(
              LifeTier1Gem.this.plugin,
              () -> {
                LifeTier1Gem.this.updateState(this.anchorLocation, 5);
                Bukkit.getScheduler()
                .runTaskLater(
                LifeTier1Gem.this.plugin,
                () -> {
                  LifeTier1Gem.this.scheduleAbilityUpdate();
                  LifeTier1Gem.this.active = false;
                  ambientAbilityTickTask.cancel();
                },
                180L);
              },
              4L);
          this.cancel();
        }
      }
    }
        .runTaskTimer(this.plugin, SharedScheduler.staggeredInitialDelay(5L), 5L);
  }

  void playAbilityEffects(Location location, int count, Player player) {
    int index = Math.max(0, (count - 1) * (count - 1));
    int remaining = count * count;

    for (int step = -count; step <= count; step++) {
      for (int ticks = -count; ticks <= count; ticks++) {
        for (int durationTicks = -count; durationTicks <= count; durationTicks++) {
          int attempts = step * step + ticks * ticks + durationTicks * durationTicks;
          if (attempts >= index && attempts <= remaining + count) {
            Location targetLocation = location.clone().add(step, ticks, durationTicks);
            Block block = targetLocation.getBlock();
            Material material = block.getType();
            this.trackedLocations.add(targetLocation);
            if (this.isTrackedTarget(material)) {
              if (!"world_nether".equals(location.getWorld().getName())) {
                this.activeMaterialsByLocation.putIfAbsent(targetLocation, material);
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.REGENERATION, 20, 1, true, false, true));
                block.setType(this.resolveMaterial());
              }
            } else if (this.meetsPrimaryCondition(material)) {
              if (!"world_nether".equals(location.getWorld().getName())) {
                this.materialsByLocation.putIfAbsent(targetLocation, material);
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.ABSORPTION, 20, 1, true, false, true));
                block.setType(this.resolveMaterialForPlayer());
              }
            } else if (this.meetsTargetCondition(material)) {
              if (!"world_nether".equals(location.getWorld().getName())) {
                this.secondaryMaterialsByLocation.putIfAbsent(targetLocation, material);
                block.setType(this.resolveMaterialForPlayer());
              }
            } else if (this.meetsSourceCondition(material)) {
              this.currentMaterialsByLocation.putIfAbsent(targetLocation, material);
              player.addPotionEffect(
                  new PotionEffect(PotionEffectType.RESISTANCE, 20, 0, true, false, true));
              block.setType(Material.ACACIA_LOG);
            }
          }
        }
      }
    }
  }

  void spawnAbilityParticles(Location location, int count) {
    for (double value = 0.0; value < 360.0; value += 2.0) {
      double angleRadians = location.getX() + count * Math.cos(Math.toRadians(value));
      double distance = location.getZ() + count * Math.sin(Math.toRadians(value));
      Location effectLocation = new Location(location.getWorld(), angleRadians, location.getY() + 0.5, distance);
      location.getWorld()
          .spawnParticle(
              Particle.DUST,
              effectLocation,
              1,
              0.0,
              0.0,
              0.0,
              0.0,
              new DustOptions(Color.fromRGB(255, 0, 179), 1.0F));
    }
  }

  void spawnAbilityEffects(Location location, double value, double distance) {
    for (int count = 0; count < 60; count++) {
      double radius = distance + count * 6;
      double angleRadians = Math.toRadians(radius);
      double offset = Math.cos(angleRadians) * value;
      double angle = Math.sin(angleRadians) * value;
      Location targetLocation = location.clone().add(offset, 0.2, angle);
      Location origin = location.clone().add(offset, 0.5, angle);
      location.getWorld()
          .spawnParticle(
              Particle.DUST,
              origin,
              2,
              0.0,
              0.0,
              0.0,
              0.0,
              new DustOptions(Color.fromRGB(255, 0, 179), 1.0F));
    }
  }

  void cleanupAbilityState(Location location, int count, Player player) {
    for (int index = -count; index <= count; index++) {
      for (int remaining = -count; remaining <= count; remaining++) {
        for (int step = -count; step <= count; step++) {
          if (index * index + remaining * remaining + step * step <= count * count) {
            Location targetLocation = location.clone().add(index, remaining, step);
            Block block = targetLocation.getBlock();
            Material material = block.getType();
            this.trackedLocations.add(targetLocation);
            if (this.isTrackedTarget(material)) {
              if (!"world_nether".equals(location.getWorld().getName())) {
                this.activeMaterialsByLocation.put(targetLocation, material);
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.REGENERATION, 20, 1, true, false, true));
                block.setType(this.resolveMaterial());
              }
            } else if (this.meetsPrimaryCondition(material)) {
              if (!"world_nether".equals(location.getWorld().getName())) {
                this.materialsByLocation.put(targetLocation, material);
                player.addPotionEffect(
                    new PotionEffect(PotionEffectType.ABSORPTION, 20, 1, true, false, true));
                block.setType(this.resolveMaterialForPlayer());
              }
            } else if (this.meetsTargetCondition(material)) {
              if (!"world_nether".equals(location.getWorld().getName())) {
                this.secondaryMaterialsByLocation.put(targetLocation, material);
                block.setType(this.resolveMaterialForPlayer());
              }
            } else if (this.meetsSourceCondition(material)) {
              this.currentMaterialsByLocation.put(targetLocation, material);
              player.addPotionEffect(
                  new PotionEffect(PotionEffectType.RESISTANCE, 20, 0, true, false, true));
              block.setType(Material.ACACIA_LOG);
            }
          }
        }
      }
    }
  }

  void updateState(Location location, int count) {
    for (int index = -count; index <= count; index++) {
      for (int remaining = -count; remaining <= count; remaining++) {
        for (int step = -count; step <= count; step++) {
          if (index * index + remaining * remaining + step * step <= count * count) {
            Location targetLocation = location.clone().add(index, remaining, step);
            Block block = targetLocation.getBlock();
            if (block.getType() == Material.WATER) {
              block.setType(Material.AIR);
            }
          }
        }
      }
    }
  }

  void scheduleAbilityUpdate() {
    this.active = false;
    this.completeAbilityAction(this.secondaryMaterialsByLocation);
    this.completeAbilityAction(this.currentMaterialsByLocation);
    this.completeAbilityAction(this.materialsByLocation);
    this.completeAbilityAction(this.activeMaterialsByLocation);
    this.trackedLocations.clear();
  }

  void completeAbilityAction(Map<Location, Material> materialsByLocation) {
    for (Entry<Location,Material> entry : materialsByLocation.entrySet()) {
      Location location = entry.getKey();
      Material material = entry.getValue();
      if (material != Material.AIR && this.isConditionMet(materialsByLocation, location.getBlock().getType())) {
        location.getBlock().setType(material);
      }
    }

    materialsByLocation.clear();
  }

  boolean isConditionMet(Map<Location, Material> materialsByLocation, Material material) {
    if (materialsByLocation != this.secondaryMaterialsByLocation && materialsByLocation != this.materialsByLocation) {
      if (materialsByLocation == this.currentMaterialsByLocation) {
        return material == Material.ACACIA_LOG;
      } else {
        return materialsByLocation != this.activeMaterialsByLocation
            ? true
            : material.name().startsWith("DEAD_") && material.name().contains("CORAL");
      }
    } else {
      return material == Material.TUFF
          || material.name().startsWith("DEAD_") && material.name().endsWith("_CORAL_BLOCK");
    }
  }

  boolean isTrackedTarget(Material material) {
    return material == Material.CORNFLOWER
        || material == Material.POPPY
        || material == Material.SUNFLOWER
        || material == Material.DANDELION
        || material == Material.BLUE_ORCHID
        || material == Material.ALLIUM
        || material == Material.AZURE_BLUET
        || material == Material.RED_TULIP
        || material == Material.ORANGE_TULIP
        || material == Material.WHITE_TULIP
        || material == Material.PINK_TULIP
        || material == Material.OXEYE_DAISY
        || material == Material.LILY_OF_THE_VALLEY
        || material == Material.TORCHFLOWER
        || material == Material.PINK_PETALS
        || material == Material.LILAC
        || material == Material.ROSE_BUSH
        || material == Material.PEONY
        || material == Material.BROWN_MUSHROOM
        || material == Material.RED_MUSHROOM;
  }

  boolean meetsPrimaryCondition(Material material) {
    return material == Material.OAK_LEAVES
        || material == Material.SPRUCE_LEAVES
        || material == Material.BIRCH_LEAVES
        || material == Material.ACACIA_LEAVES
        || material == Material.CHERRY_LEAVES
        || material == Material.MANGROVE_LEAVES
        || material == Material.JUNGLE_LEAVES
        || material == Material.DARK_OAK_LEAVES;
  }

  boolean meetsTargetCondition(Material material) {
    return material == Material.GRASS_BLOCK || material == Material.STONE || material == Material.DIRT;
  }

  boolean meetsSourceCondition(Material material) {
    return material == Material.OAK_LOG
        || material == Material.SPRUCE_LOG
        || material == Material.ACACIA_LOG
        || material == Material.DARK_OAK_LOG
        || material == Material.CHERRY_LOG
        || material == Material.BIRCH_LOG
        || material == Material.JUNGLE_LOG
        || material == Material.MANGROVE_LOG;
  }

  Material resolveMaterial() {
    Material[] material =
        new Material[] {
          Material.DEAD_BUBBLE_CORAL_FAN,
          Material.DEAD_TUBE_CORAL,
          Material.DEAD_BRAIN_CORAL,
          Material.DEAD_BUBBLE_CORAL,
          Material.DEAD_FIRE_CORAL,
          Material.DEAD_HORN_CORAL,
          Material.DEAD_TUBE_CORAL_FAN,
          Material.DEAD_BRAIN_CORAL_FAN,
          Material.DEAD_FIRE_CORAL_FAN,
          Material.DEAD_HORN_CORAL_FAN
        };
    return material[java.util.concurrent.ThreadLocalRandom.current().nextInt(material.length)];
  }

  Material resolveMaterialForPlayer() {
    Material[] material =
        new Material[] {
          Material.TUFF,
          Material.DEAD_TUBE_CORAL_BLOCK,
          Material.DEAD_BUBBLE_CORAL_BLOCK,
          Material.DEAD_BRAIN_CORAL_BLOCK,
          Material.DEAD_FIRE_CORAL_BLOCK,
          Material.DEAD_HORN_CORAL_BLOCK
        };
    return material[java.util.concurrent.ThreadLocalRandom.current().nextInt(material.length)];
  }

  void updateActionBar() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (this.plugin.isGemsDisabled()) {
        return;
      }

      if (this.plugin.canUseAbility(player)) {
        return;
      }

      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      ItemStack offHandItem = player.getInventory().getItemInOffHand();
      if (shouldApplyEffect(mainHandItem) || shouldApplyEffect(offHandItem)) {
        UUID playerId = player.getUniqueId();
        if (CooldownService.isOnCooldown(playerId, "life_vitality")) {
          String text =
              AbilityStatusFormatter.formatDisplayText(playerId, "life_vitality", false);
          String textColor = ChatColor.LIGHT_PURPLE.toString();
          String messageColor = ChatColor.AQUA.toString();
          ActionBarQueue.enqueue(player, textColor + "🔺 " + messageColor + text);
        } else {
          ActionBarQueue.enqueue(
              player, ChatColor.LIGHT_PURPLE + "🔺 " + ChatColor.GREEN + "Ready!");
        }
      }
    }
  }

  static {
    LIFE_VITALITY_ID = "life_vitality";
  }
}
