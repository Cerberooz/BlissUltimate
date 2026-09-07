package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class RepairRitual implements Listener {
  Bliss plugin;
  final Map<Location, RepairRitualSession> sessionsByLocation = new HashMap<>();
  final Map<UUID, Long> lastUseTimes = new HashMap<>();
  final Map<UUID, Boolean> secondaryFlagsByPlayer = new HashMap<>();
  final Map<UUID, Boolean> activeFlagsByPlayer = new HashMap<>();
  final Set<UUID> playerIds = new HashSet<>();
  int durationTicks;
  final List<ItemStack> entries =
      Arrays.asList(
          BlissItems.createPuffTierOneGemStage6(),
          BlissItems.createLifeTierOneGemStage6(),
          BlissItems.createAstraTierOneGemStage6(),
          BlissItems.createWealthTierOneGemStage6(),
          BlissItems.createSpeedTierOneGemStage6(),
          BlissItems.createFireTierOneGemStage6(),
          BlissItems.createFluxTierOneGemStage6(),
          BlissItems.createStrengthTierOneGemStage6());

  public boolean isAbilityActive(UUID playerId) {
    return this.playerIds.contains(playerId);
  }

  public void initialize() {
    this.plugin.getLogger().info("Loaded " + this.playerIds.size() + " completed ritual records");
  }

  public void updateAbilityState(UUID playerId) {
    this.lastUseTimes.remove(playerId);
  }

  public RepairRitual(Bliss bliss) {
    this.plugin = bliss;
    this.durationTicks = ConfigValueCache.getInt(bliss, "RepairCooldown");
    this.initialize();
  }

  public void refreshAbilityState(UUID playerId) {
    this.playerIds.add(playerId);
  }

  public void applyAbilityEffects(UUID playerId) {
    this.playerIds.remove(playerId);
  }

  GemEnergyManager resolveGemEnergyManager() {
    return this.plugin.getEnergyManager();
  }

  boolean isConditionMet() {
    return ConfigValueCache.getBoolean(this.plugin, "DisableRepairRitual", false);
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {

      for (RepairRitualSession repairRitualSession : this.sessionsByLocation.values()) {
        if (repairRitualSession.playerId.equals(player.getUniqueId())) {
          event.setCancelled(true);
          return;
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (this.isConditionMet()) {
      Player player = event.getPlayer();
      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      if (mainHandItem != null && this.isMatchingState(mainHandItem)) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR
            || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
          event.setCancelled(true);
          GemEnergyManager gemEnergyManager = this.resolveGemEnergyManager();
          int count = GemEnergyManager.getAbilityIntValue(player);
          if (count >= 2 && count < 6) {
            if (mainHandItem.getAmount() > 1) {
              mainHandItem.setAmount(mainHandItem.getAmount() - 1);
            } else {
              mainHandItem.setAmount(0);
            }

            gemEnergyManager.scheduleAbilityUpdate(player, 6);
            gemEnergyManager.activateAbility(player);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            player.spawnParticle(
                Particle.HAPPY_VILLAGER,
                player.getLocation().add(0.0, 1.0, 0.0),
                50,
                0.5,
                0.5,
                0.5,
                0.1);
            player.sendMessage(ChatColor.GREEN + "Gem upgraded successfully!");
          }
        }
      }
    }
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    if (!this.isConditionMet()) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      Material material = block.getType();
      if (material == Material.PISTON || material == Material.STICKY_PISTON || material == Material.TNT) {
        for (Location location : this.sessionsByLocation.keySet()) {
          if (block.getLocation().distance(location) <= 15.0) {
            event.setCancelled(true);
            return;
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (!this.isConditionMet()) {
      Player player = event.getPlayer();
      Item item = event.getItemDrop();
      ItemStack heldItem = item.getItemStack();
      Location location = item.getLocation();
      Location targetLocation = this.getTargetLocation(location, 2);
      if (targetLocation != null) {
        RepairRitualSession repairRitualSession = this.sessionsByLocation.get(targetLocation);
        new ItemStack(player.getInventory().getItemInMainHand());
        new ItemStack(player.getInventory().getItemInOffHand());
        if (this.isMatchingState(heldItem) && repairRitualSession == null) {
          this.playAbilitySound(player, targetLocation, item);
        } else if (repairRitualSession != null && this.isProtectedTarget(heldItem)) {
            this.playAbilityEffects(player, repairRitualSession, targetLocation, item);
          }
        
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    this.applyAbilityEffects(playerId);
  }

  boolean canUseAbility(Player player) {
    for (ItemStack item : player.getInventory().getContents()) {
      if (this.isMatchingItem(item)) {
        return true;
      }
    }

    return false;
  }

  boolean isMatchingItem(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      return item.getType() != Material.AMETHYST_SHARD
              && item.getType() != Material.PRISMARINE_SHARD
          ? false
          : itemMeta.hasCustomModelData()
              && (itemMeta.getCustomModelData() == 95 || itemMeta.getCustomModelData() == 96);
    } else {
      return false;
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (!this.isConditionMet() && this.activeFlagsByPlayer.getOrDefault(event.getPlayer().getUniqueId(), false)) {
      event.setCancelled(true);
    }
  }

  void playAbilitySound(Player player, Location location, Item item) {
    ItemStack heldItem = item.getItemStack();
    boolean ritualEnabled =
        this.canUseAbility(player)
            || this.isMatchingItem(player.getInventory().getItemInOffHand());
    item.remove();
    Location targetLocation = location.clone().add(0.5, 500.0, 0.5);
    EnderCrystal beamCrystal = (EnderCrystal) location.getWorld().spawnEntity(targetLocation, EntityType.END_CRYSTAL);
    beamCrystal.setBeamTarget(location.clone().add(0.0, -2.0, 0.0));
    RepairRitualSession repairRitualSession = new RepairRitualSession();
    repairRitualSession.playerId = player.getUniqueId();
    repairRitualSession.enderCrystal = beamCrystal;
    repairRitualSession.durationTicks = 0;
    repairRitualSession.active = false;
    repairRitualSession.entries = new ArrayList<>();
    repairRitualSession.secondaryActive = ritualEnabled;
    repairRitualSession.maxCount = 1;
    this.sessionsByLocation.put(location, repairRitualSession);
    location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 30.0F, 1.0F);

    for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
      targetPlayer.sendMessage(ChatColor.LIGHT_PURPLE + player.getName() + " has started a repair ritual!");
    }

    player.sendMessage(
        ChatColor.GOLD + "You have started a " + ChatColor.of("#FFD773") + "§lRepair Ritual!");
    player.sendMessage(
        ChatColor.of("#96FFD9") + "Deposit a energy bottle to the pedestal to continue.");
  }

  void playAbilityEffects(Player player, RepairRitualSession repairRitualSession, Location location, Item item) {
    UUID playerId = player.getUniqueId();
    if (repairRitualSession.durationTicks >= 5) {
      player.sendMessage("§cThis pedestal already has enough energy.");
    } else if (!this.isAbilityBlocked(playerId)) {
      item.remove();
      repairRitualSession.durationTicks++;
      if (repairRitualSession.durationTicks > 5) {
        repairRitualSession.durationTicks = 5;
      }

      this.lastUseTimes.put(playerId, System.currentTimeMillis());
      this.scheduleAbilityUpdate(player, playerId);
      location.getWorld().playSound(location, Sound.BLOCK_BEACON_AMBIENT, 40.0F, 1.6F);
      location.getWorld().playSound(location, Sound.ITEM_AXE_SCRAPE, 10.0F, 0.5F);
      location.getWorld().playSound(location, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 20.0F, 1.2F);

      for (Player targetPlayer : location.getWorld().getPlayers()) {
        if (targetPlayer.getLocation().distance(location) <= 15.0) {
          targetPlayer.sendMessage(ChatColor.of("#96FFD9").toString() + repairRitualSession.durationTicks + "/5 energy deposited!");
          if (repairRitualSession.durationTicks >= 5) {
            targetPlayer.sendMessage(
                ChatColor.of("#FFD773") + "Energy deposit requirement has been fulfilled!");
            targetPlayer.sendMessage(
                ChatColor.of("#96FFD9") + "Stand in the particle circles to upgrade your gems!");
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 30.0F, 1.0F);
            if (!repairRitualSession.active) {
              this.activateAbility(location, repairRitualSession, player);
            }
          } else {
            targetPlayer.sendMessage("§cPlease wait for pedestal cooldown...");
          }
        }
      }
    }
  }

  void activateAbility(Location location, RepairRitualSession repairRitualSession, Player player) {
    repairRitualSession.active = true;
    Location targetLocation = location.clone().add(0.5, 1.0, 0.5);
    repairRitualSession.scheduledTask = new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;
      final int maxCount = 5;

      @Override
      public void run() {
        if (this.durationTicks >= 5) {
          RepairRitual.this.processAbilityState(this.anchorLocation);
          RepairRitual.this.refreshAbilityState(player.getUniqueId());
          this.cancel();
        } else {
          repairRitualSession.playerIds.clear();
          this.anchorLocation.getWorld().playSound(this.anchorLocation, Sound.BLOCK_BEACON_AMBIENT, 40.0F, 1.6F);
          RepairRitual.this.updateState(targetLocation, this.durationTicks, repairRitualSession);
          this.durationTicks++;
        }
      }
    }.runTaskTimer(this.plugin, 0L, 100L);
  }

  void updateState(Location location, int count, RepairRitualSession repairRitualSession) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;
      final double[] state = new double[] {0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0, 3.5, 4.0};

      @Override
      public void run() {
        if (this.durationTicks >= 5) {
          RepairRitual.this.spawnAbilityEffects(this.anchorLocation, repairRitualSession);
          this.cancel();
        } else {
          for (int count = 0; count <= Math.min(this.durationTicks * 2 + 1, this.state.length - 1); count++) {
            RepairRitual.this.spawnAbilityParticles(this.anchorLocation, this.state[count]);
          }

          if (this.durationTicks % 1 == 0) {
            this.anchorLocation
            .getWorld()
            .playSound(this.anchorLocation, Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.3F, 1.5F + this.durationTicks * 0.2F);
          }

          this.durationTicks++;
        }
      }
    }.runTaskTimer(this.plugin, 0L, 1L);
  }

  void spawnAbilityParticles(Location location, double value) {
    for (double distance = 0.0; distance < Math.PI * 2; distance += Math.PI / 20) {
      double offset = location.getX() + value * Math.cos(distance);
      double radius = location.getZ() + value * Math.sin(distance);
      double y = location.getY();
      Location effectLocation = new Location(location.getWorld(), offset, y, radius);
      location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, effectLocation, 1, 0.0, 0.0, 0.0, 0.0);
    }
  }

  void spawnAbilityEffects(Location location, RepairRitualSession repairRitualSession) {
    World world = location.getWorld();
    double value = 4.0;
    if (repairRitualSession != null) {
      for (Player player : world.getPlayers()) {
        if (player.getLocation().getWorld() == world && !repairRitualSession.playerIds.contains(player.getUniqueId())) {
          Location targetLocation = player.getLocation();
          double x =
              Math.sqrt(
                  Math.pow(targetLocation.getX() - location.getX(), 2.0)
                      + Math.pow(targetLocation.getZ() - location.getZ(), 2.0));
          if (x <= value && Math.abs(targetLocation.getY() - location.getY()) < 2.0) {
            GemEnergyManager gemEnergyManager = this.resolveGemEnergyManager();
            int count = GemEnergyManager.getAbilityIntValue(player.getPlayer());
            if (count >= 1 && count < 5) {
              gemEnergyManager.completeAbilityAction(player, 1);
              gemEnergyManager.activateAbility(player);
              repairRitualSession.playerIds.add(player.getUniqueId());
            }
          }
        }
      }
    }
  }

  void cleanupAbilityState(Player player, int count) {
    PlayerInventory playerInventory = player.getInventory();

    for (int index = 0; index < playerInventory.getSize(); index++) {
      ItemStack item = playerInventory.getItem(index);
      if (this.isAbilityAllowed(item)) {
        ItemStack heldItem = this.createItem(item, count);
        if (heldItem != null) {
          playerInventory.setItem(index, heldItem);
        }
      }
    }

    ItemStack offHandItem = playerInventory.getItemInOffHand();
    if (this.isAbilityAllowed(offHandItem)) {
      ItemStack targetItem = this.createItem(offHandItem, count);
      if (targetItem != null) {
        playerInventory.setItemInOffHand(targetItem);
      }
    }

    ItemStack[] candidateItem = playerInventory.getArmorContents();

    for (int remaining = 0; remaining < candidateItem.length; remaining++) {
      if (this.isAbilityAllowed(candidateItem[remaining])) {
        ItemStack secondaryItem = this.createItem(candidateItem[remaining], count);
        if (secondaryItem != null) {
          candidateItem[remaining] = secondaryItem;
        }
      }
    }

    playerInventory.setArmorContents(candidateItem);
  }

  ItemStack createItem(ItemStack item, int count) {
    if (item != null && item.hasItemMeta()) {
      int index = this.getAbilityIntValue(item);
      return index == -1 ? null : this.createAbilityItem(index, count);
    } else {
      return null;
    }
  }

  int getAbilityIntValue(ItemStack item) {
    if (!item.hasItemMeta()) {
      return -1;
    } else {
      ItemMeta itemMeta = item.getItemMeta();
      if (!itemMeta.hasDisplayName()) {
        return -1;
      } else {
        String displayName = itemMeta.getDisplayName();
        if (displayName.contains("ᴘᴜғғ") || displayName.contains("PUFF")) {
          return 0;
        } else if (displayName.contains("ʟɪғᴇ") || displayName.contains("LIFE")) {
          return 1;
        } else if (displayName.contains("ᴀѕᴛʀᴀ") || displayName.contains("ASTRA")) {
          return 2;
        } else if (displayName.contains("ᴡᴇᴀʟᴛʜ")
            || displayName.contains("WEALTH")) {
          return 3;
        } else if (displayName.contains("ѕᴘᴇᴇᴅ") || displayName.contains("SPEED")) {
          return 4;
        } else if (displayName.contains("ғɪʀᴇ") || displayName.contains("FIRE")) {
          return 5;
        } else if (displayName.contains("ꜰʟᴜx") || displayName.contains("FLUX")) {
          return 6;
        } else {
          return !displayName.contains("ѕᴛʀᴇɴɢᴛʜ")
                  && !displayName.contains("STRENGTH")
              ? -1
              : 7;
        }
      }
    }
  }

  boolean isAbilityAllowed(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      if (!itemMeta.hasDisplayName()) {
        return false;
      }

      String displayName = itemMeta.getDisplayName();
      return displayName.contains("ᴘᴜғғ")
          || displayName.contains("ʟɪғᴇ")
          || displayName.contains("ᴀѕᴛʀᴀ")
          || displayName.contains("ᴡᴇᴀʟᴛʜ")
          || displayName.contains("ѕᴘᴇᴇᴅ")
          || displayName.contains("ғɪʀᴇ")
          || displayName.contains("ꜰʟᴜx")
          || displayName.contains("ѕᴛʀᴇɴɢᴛʜ")
          || displayName.contains("PUFF")
          || displayName.contains("LIFE")
          || displayName.contains("ASTRA")
          || displayName.contains("WEALTH")
          || displayName.contains("SPEED")
          || displayName.contains("FIRE")
          || displayName.contains("FLUX")
          || displayName.contains("STRENGTH");
    } else {
      return false;
    }
  }

  ItemStack createAbilityItem(int count, int index) {
    if (index == 1) {
      switch (count) {
        case 0:
          return BlissItems.createPuffTierOneGemStage2();
        case 1:
          return BlissItems.createLifeTierOneGemStage2();
        case 2:
          return BlissItems.createAstraTierOneGemStage2();
        case 3:
          return BlissItems.createWealthTierOneGemStage2();
        case 4:
          return BlissItems.createSpeedTierOneGemStage2();
        case 5:
          return BlissItems.createFireTierOneGemStage2();
        case 6:
          return BlissItems.createFluxTierOneGemStage2();
        case 7:
          return BlissItems.createStrengthTierOneGemStage2();
      }
    } else if (index == 2) {
      switch (count) {
        case 0:
          return BlissItems.createPuffTierOneGemStage3();
        case 1:
          return BlissItems.createLifeTierOneGemStage3();
        case 2:
          return BlissItems.createAstraTierOneGemStage3();
        case 3:
          return BlissItems.createWealthTierOneGemStage3();
        case 4:
          return BlissItems.createSpeedTierOneGemStage3();
        case 5:
          return BlissItems.createFireTierOneGemStage3();
        case 6:
          return BlissItems.createFluxTierOneGemStage3();
        case 7:
          return BlissItems.createStrengthTierOneGemStage3();
      }
    } else if (index == 3) {
      switch (count) {
        case 0:
          return BlissItems.createPuffTierOneGemStage4();
        case 1:
          return BlissItems.createLifeTierOneGemStage4();
        case 2:
          return BlissItems.createAstraTierOneGemStage4();
        case 3:
          return BlissItems.createWealthTierOneGemStage4();
        case 4:
          return BlissItems.createSpeedTierOneGemStage4();
        case 5:
          return BlissItems.createFireTierOneGemStage4();
        case 6:
          return BlissItems.createFluxTierOneGemStage4();
        case 7:
          return BlissItems.createStrengthTierOneGemStage4();
      }
    } else if (index == 4) {
      switch (count) {
        case 0:
          return BlissItems.createPuffTierOneGemStage5();
        case 1:
          return BlissItems.createLifeTierOneGemStage5();
        case 2:
          return BlissItems.createAstraTierOneGemStage5();
        case 3:
          return BlissItems.createWealthTierOneGemStage5();
        case 4:
          return BlissItems.createSpeedTierOneGemStage5();
        case 5:
          return BlissItems.createFireTierOneGemStage5();
        case 6:
          return BlissItems.createFluxTierOneGemStage5();
        case 7:
          return BlissItems.createStrengthTierOneGemStage5();
      }
    } else if (index == 5) {
      switch (count) {
        case 0:
          return BlissItems.createPuffTierOneGemStage6();
        case 1:
          return BlissItems.createLifeTierOneGemStage6();
        case 2:
          return BlissItems.createAstraTierOneGemStage6();
        case 3:
          return BlissItems.createWealthTierOneGemStage6();
        case 4:
          return BlissItems.createSpeedTierOneGemStage6();
        case 5:
          return BlissItems.createFireTierOneGemStage6();
        case 6:
          return BlissItems.createFluxTierOneGemStage6();
        case 7:
          return BlissItems.createStrengthTierOneGemStage6();
      }
    }

    return BlissItems.createPuffTierOneGemStage6();
  }

  void scheduleAbilityUpdate(Player player, UUID playerId) {
    long timestamp = this.durationTicks * 1000L;
    long lastUpdateTime = timestamp / 50L;
    new BukkitRunnable() {
      final long lastUpdateTime = timestamp;
      @Override
      public void run() {
        if (RepairRitual.this.lastUseTimes.containsKey(playerId)) {
          long timestamp = RepairRitual.this.lastUseTimes.get(playerId);
          long now = System.currentTimeMillis() - timestamp;
          if (now >= this.lastUpdateTime) {
            if (player.isOnline()) {
              player.sendMessage(
                  "§6Pedestal cooldown is over. "
                  + ChatColor.of("#96FFD9")
                  + "Please deposit 1 §lᴇɴᴇʀɢʏ!");
            }

            this.cancel();
          }
        }
      }
    }.runTaskTimer(this.plugin, lastUpdateTime, lastUpdateTime);
  }

  void completeAbilityAction(Location location) {
    World world = location.getWorld();

    for (int count = 0; count < 2; count++) {
      double x =
          location.getX()
              + (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 30.0;
      double z =
          location.getZ()
              + (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 30.0;
      Location targetLocation = new Location(world, x, location.getY(), z);
      LightningStrike lightningStrike = world.spawn(targetLocation, LightningStrike.class);
      lightningStrike.setFlashCount(3);
    }
  }

  public boolean isAbilityBlocked(UUID playerId) {
    if (!this.lastUseTimes.containsKey(playerId)) {
      return false;
    } else {
      long now = System.currentTimeMillis();
      long timestamp = this.lastUseTimes.get(playerId);
      long lastUpdateTime = this.durationTicks * 1000L;
      if (now - timestamp >= lastUpdateTime) {
        this.lastUseTimes.remove(playerId);
        return false;
      } else {
        return true;
      }
    }
  }

  void applyAbilityMotion(Location location) {
    World world = location.getWorld();

    for (Entity entity : world.getNearbyEntities(location, 5.0, 5.0, 5.0)) {
      if (!(entity instanceof Player)) {
        entity.setVelocity(
            entity.getVelocity()
                .add(
                    entity.getLocation()
                        .subtract(location)
                        .toVector()
                        .normalize()
                        .multiply(2)
                        .setY(1.5)));
      }
    }
  }

  Location getTargetLocation(Location location, int count) {
    World world = location.getWorld();
    int index = location.getBlockX();
    int remaining = location.getBlockY();
    int step = location.getBlockZ();

    for (int ticks = -count; ticks <= count; ticks++) {
      for (int durationTicks = -count; durationTicks <= count; durationTicks++) {
        for (int attempts = -count; attempts <= count; attempts++) {
          Block block = world.getBlockAt(index + ticks, remaining + durationTicks, step + attempts);
          if (block.getType() == Material.BEACON) {
            return block.getLocation();
          }
        }
      }
    }

    return null;
  }

  boolean isMatchingState(ItemStack item) {
    return this.isValidTarget(item, Material.NAUTILUS_SHELL, 200);
  }

  boolean isValidTarget(ItemStack item, Material material, int count) {
    if (item != null && item.getType() == material && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      return itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == count;
    } else {
      return false;
    }
  }

  boolean isProtectedTarget(ItemStack item) {
    return this.isValidTarget(item, Material.NAUTILUS_SHELL, 300);
  }

  public void processAbilityState(Location location) {
    RepairRitualSession repairRitualSession = this.sessionsByLocation.get(location);
    if (repairRitualSession != null) {
      if (repairRitualSession.enderCrystal != null) {
        try {
          repairRitualSession.enderCrystal.remove();
        } catch (Exception exception) {
          Location targetLocation = location.clone().add(0.5, 500.0, 0.5);
          World world = location.getWorld();

          for (Entity entity : world.getNearbyEntities(targetLocation, 5.0, 5.0, 5.0)) {
            if (entity instanceof EnderCrystal) {
              entity.remove();
            }
          }
        }
      }

      if (repairRitualSession.activeRunnable != null) {
        repairRitualSession.activeRunnable.cancel();
      }

      if (repairRitualSession.secondaryRunnable != null) {
        repairRitualSession.secondaryRunnable.cancel();
      }

      if (repairRitualSession.runnable != null) {
        repairRitualSession.runnable.cancel();
      }

      if (repairRitualSession.scheduledTask != null) {
        repairRitualSession.scheduledTask.cancel();
      }

      for (ItemDisplay itemDisplay : repairRitualSession.entries) {
        if (itemDisplay != null && itemDisplay.isValid()) {
          itemDisplay.remove();
        }
      }

      Player player = Bukkit.getPlayer(repairRitualSession.playerId);
      if (player != null) {
        this.activeFlagsByPlayer.remove(repairRitualSession.playerId);
        player.removePotionEffect(PotionEffectType.GLOWING);
      }

      this.sessionsByLocation.remove(location);
      this.lastUseTimes.remove(repairRitualSession.playerId);
      location.getWorld().playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 30.0F, 1.0F);
    }
  }

  public void shutdown() {
    for (Location ritualLocation : new ArrayList<>(this.sessionsByLocation.keySet())) {
      this.processAbilityState(ritualLocation);
    }
    this.sessionsByLocation.clear();
    this.lastUseTimes.clear();
    this.secondaryFlagsByPlayer.clear();
    this.activeFlagsByPlayer.clear();
    this.playerIds.clear();
  }

  public void handleAbilityAction() {
    for (World world : Bukkit.getWorlds()) {
      for (Entity entity : world.getEntities()) {
        if (entity instanceof EnderCrystal enderCrystal && enderCrystal.getBeamTarget() != null) {
          Location location = enderCrystal.getBeamTarget();
          boolean enabled = false;

          for (Location targetLocation : this.sessionsByLocation.keySet()) {
            if (location.distance(targetLocation.clone().add(0.0, -2.0, 0.0)) < 1.0) {
              enabled = true;
              break;
            }
          }

          if (!enabled) {
            enderCrystal.remove();
          }
        }
      }
    }
  }

  ItemStack createDisplayItem(int count) {
    int index = java.util.concurrent.ThreadLocalRandom.current().nextInt(8);
    if (count == 1) {
      switch (index) {
        case 0:
          return BlissItems.createPuffTierOneGemStage2();
        case 1:
          return BlissItems.createLifeTierOneGemStage2();
        case 2:
          return BlissItems.createAstraTierOneGemStage2();
        case 3:
          return BlissItems.createWealthTierOneGemStage2();
        case 4:
          return BlissItems.createSpeedTierOneGemStage2();
        case 5:
          return BlissItems.createFireTierOneGemStage2();
        case 6:
          return BlissItems.createFluxTierOneGemStage2();
        case 7:
          return BlissItems.createStrengthTierOneGemStage2();
      }
    } else if (count == 2) {
      switch (index) {
        case 0:
          return BlissItems.createPuffTierOneGemStage3();
        case 1:
          return BlissItems.createLifeTierOneGemStage3();
        case 2:
          return BlissItems.createAstraTierOneGemStage3();
        case 3:
          return BlissItems.createWealthTierOneGemStage3();
        case 4:
          return BlissItems.createSpeedTierOneGemStage3();
        case 5:
          return BlissItems.createFireTierOneGemStage3();
        case 6:
          return BlissItems.createFluxTierOneGemStage3();
        case 7:
          return BlissItems.createStrengthTierOneGemStage3();
      }
    } else if (count == 3) {
      switch (index) {
        case 0:
          return BlissItems.createPuffTierOneGemStage4();
        case 1:
          return BlissItems.createLifeTierOneGemStage4();
        case 2:
          return BlissItems.createAstraTierOneGemStage4();
        case 3:
          return BlissItems.createWealthTierOneGemStage4();
        case 4:
          return BlissItems.createSpeedTierOneGemStage4();
        case 5:
          return BlissItems.createFireTierOneGemStage4();
        case 6:
          return BlissItems.createFluxTierOneGemStage4();
        case 7:
          return BlissItems.createStrengthTierOneGemStage4();
      }
    } else if (count == 4) {
      switch (index) {
        case 0:
          return BlissItems.createPuffTierOneGemStage5();
        case 1:
          return BlissItems.createLifeTierOneGemStage5();
        case 2:
          return BlissItems.createAstraTierOneGemStage5();
        case 3:
          return BlissItems.createWealthTierOneGemStage5();
        case 4:
          return BlissItems.createSpeedTierOneGemStage5();
        case 5:
          return BlissItems.createFireTierOneGemStage5();
        case 6:
          return BlissItems.createFluxTierOneGemStage5();
        case 7:
          return BlissItems.createStrengthTierOneGemStage5();
      }
    } else if (count == 5) {
      switch (index) {
        case 0:
          return BlissItems.createPuffTierOneGemStage6();
        case 1:
          return BlissItems.createLifeTierOneGemStage6();
        case 2:
          return BlissItems.createAstraTierOneGemStage6();
        case 3:
          return BlissItems.createWealthTierOneGemStage6();
        case 4:
          return BlissItems.createSpeedTierOneGemStage6();
        case 5:
          return BlissItems.createFireTierOneGemStage6();
        case 6:
          return BlissItems.createFluxTierOneGemStage6();
        case 7:
          return BlissItems.createStrengthTierOneGemStage6();
      }
    }

    return BlissItems.createPuffTierOneGemStage6();
  }

  ItemStack createUpgradeItem() {
    return this.entries
        .get(java.util.concurrent.ThreadLocalRandom.current().nextInt(this.entries.size()))
        .clone();
  }

  void resetAbilityState(Player player, ItemStack item) {
    for (int count = 0; count < player.getInventory().getSize(); count++) {
      ItemStack heldItem = player.getInventory().getItem(count);
      if (this.isAbilityAllowed(heldItem)) {
        player.getInventory().setItem(count, item);
        break;
      }
    }
  }

  void trackAbilityState(Player player, ItemStack item) {
    for (int count = 0; count < player.getInventory().getSize(); count++) {
      ItemStack heldItem = player.getInventory().getItem(count);
      if (this.isAbilityAllowed(heldItem)) {
        player.getInventory().setItem(count, item);
        break;
      }
    }
  }
}
