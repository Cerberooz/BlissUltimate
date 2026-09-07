package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
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
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;

public class RestorationRitual implements Listener {
  Bliss plugin;
  final Map<Location, RestorationRitualSession> sessionsByLocation = new HashMap<>();
  final Map<UUID, Long> lastUseTimes = new HashMap<>();
  final Map<UUID, Boolean> activeFlagsByPlayer = new HashMap<>();
  final Map<UUID, Boolean> secondaryFlagsByPlayer = new HashMap<>();
  final Set<UUID> playerIds = new HashSet<>();
  final Random random = new Random();
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
  static final Set<Integer> restorationModelData;

  boolean isMatchingItem(ItemStack item) {
    return this.isAbilityAllowed(item, Material.NAUTILUS_SHELL, 210);
  }

  boolean isAbilityAllowed(ItemStack item, Material material, int count) {
    if (item != null && item.getType() == material && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      return itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == count;
    } else {
      return false;
    }
  }

  public RestorationRitual(Bliss bliss) {
    this.plugin = bliss;
    this.durationTicks = ConfigValueCache.getInt(bliss, "RestorationCooldown");
    this.initialize();
  }

  public void updateAbilityState(UUID playerId) {
    this.playerIds.add(playerId);
  }

  public void refreshAbilityState(UUID playerId) {
    this.playerIds.remove(playerId);
  }

  public boolean isAbilityActive(UUID playerId) {
    return this.playerIds.contains(playerId);
  }

  public void initialize() {
    this.plugin.getLogger().info("Loaded " + this.playerIds.size() + " completed ritual records");
  }

  public void applyAbilityEffects(UUID playerId) {
    this.lastUseTimes.remove(playerId);
  }

  GemEnergyManager resolveGemEnergyManager() {
    return this.plugin.getEnergyManager();
  }

  boolean isConditionMet() {
    return ConfigValueCache.getBoolean(this.plugin, "DisableRestorationRitual", false);
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof Player player) {

      for (RestorationRitualSession restorationRitualSession : this.sessionsByLocation.values()) {
        if (restorationRitualSession.playerId.equals(player.getUniqueId()) && !(event.getDamager() instanceof Player)) {
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
      if (mainHandItem != null && this.isMatchingItem(mainHandItem)) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR
            || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
          if (!this.canUseAbility(player)
              && !this.isAbilityBlocked(player.getInventory().getItemInOffHand())) {
            player.sendMessage(ChatColor.RED + "You need a broken gem to use this item!");
          } else {
            event.setCancelled(true);
            if (mainHandItem.getAmount() > 1) {
              mainHandItem.setAmount(mainHandItem.getAmount() - 1);
            } else {
              mainHandItem.setAmount(0);
            }

            ItemStack item = this.createItem(this.getAbilityIntValue(player));
            this.activateSourceAbility(player, item);
            this.resolveGemEnergyManager().scheduleAbilityUpdate(player, 5);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            player.spawnParticle(
                Particle.HAPPY_VILLAGER,
                player.getLocation().add(0.0, 1.0, 0.0),
                50,
                0.5,
                0.5,
                0.5,
                0.1);
            String itemName =
                item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    ? item.getItemMeta().getDisplayName()
                    : item.getType().toString();
            player.sendMessage(ChatColor.of("#befff7") + "Received: " + itemName);
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
        RestorationRitualSession restorationRitualSession = this.sessionsByLocation.get(targetLocation);
        new ItemStack(player.getInventory().getItemInMainHand());
        ItemStack offHandItem = new ItemStack(player.getInventory().getItemInOffHand());
        if (this.isMatchingItem(heldItem) && restorationRitualSession == null) {
          if (this.canUseAbility(player) || this.isAbilityBlocked(offHandItem)) {
            this.playAbilitySound(player, targetLocation, item);
            World world = player.getWorld();
            world.setStorm(true);
            world.setThundering(true);
            world.setWeatherDuration(18000);
            world.setThunderDuration(18000);
          }
        } else if (restorationRitualSession != null && this.isValidTarget(heldItem)) {
            this.playAbilityEffects(player, restorationRitualSession, targetLocation, item);
          }
        
      }
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    if (this.isAbilityActive(playerId)) {
      this.refreshAbilityState(playerId);
      this.plugin.getLogger().info("Removed ritual completion for " + player.getName() + " on disconnect");
    }

    this.lastUseTimes.remove(playerId);
    this.activeFlagsByPlayer.remove(playerId);
    this.secondaryFlagsByPlayer.remove(playerId);
  }

  boolean canUseAbility(Player player) {
    for (ItemStack item : player.getInventory().getContents()) {
      if (this.isAbilityBlocked(item)) {
        return true;
      }
    }

    return false;
  }

  boolean isAbilityBlocked(ItemStack item) {
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
    if (!this.isConditionMet()) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();
      if (this.secondaryFlagsByPlayer.getOrDefault(playerId, false)) {
        event.setCancelled(true);
      } else if (event.hasChangedPosition()) {
        Location location = this.getTargetLocation(event.getTo(), 1);
        if (location != null) {
          RestorationRitualSession restorationRitualSession = this.sessionsByLocation.get(location);
          if (restorationRitualSession != null && restorationRitualSession.durationTicks == 5 && restorationRitualSession.playerId.equals(player.getUniqueId()) && !restorationRitualSession.currentActive) {
            Location targetLocation = location.clone().add(0.5, 1.0, 0.5);
            if (player.getLocation().distance(targetLocation) <= 0.25) {
              this.startActiveRitualPhase(player, location, restorationRitualSession);
            }
          }
        }
      }
    }
  }

  void playAbilitySound(Player player, Location location, Item item) {
    ItemStack heldItem = item.getItemStack();
    boolean ritualEnabled =
        this.canUseAbility(player)
            || this.isAbilityBlocked(player.getInventory().getItemInOffHand());
    item.remove();
    Location targetLocation = location.clone().add(0.5, 500.0, 0.5);
    EnderCrystal beamCrystal = (EnderCrystal) location.getWorld().spawnEntity(targetLocation, EntityType.END_CRYSTAL);
    beamCrystal.setBeamTarget(location.clone().add(0.0, -2.0, 0.0));
    RestorationRitualSession restorationRitualSession = new RestorationRitualSession();
    restorationRitualSession.playerId = player.getUniqueId();
    restorationRitualSession.enderCrystal = beamCrystal;
    restorationRitualSession.durationTicks = 0;
    restorationRitualSession.currentActive = false;
    restorationRitualSession.entries = new ArrayList<>();
    restorationRitualSession.active = ritualEnabled;
    restorationRitualSession.maxCount = this.getAbilityIntValue(player);
    this.sessionsByLocation.put(location, restorationRitualSession);
    location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 30.0F, 1.0F);

    for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
      targetPlayer.sendMessage(
          ChatColor.LIGHT_PURPLE + player.getName() + " has started a restoration ritual!");
    }

    player.sendMessage(
        ChatColor.GOLD
            + "You have started a "
            + ChatColor.of("#FFD773")
            + "§lRestoration Ritual!");
    player.sendMessage(
        ChatColor.of("#96FFD9") + "Deposit a energy bottle to the pedestal to continue.");
  }

  void playAbilityEffects(Player player, RestorationRitualSession restorationRitualSession, Location location, Item item) {
    UUID playerId = player.getUniqueId();
    if (restorationRitualSession.durationTicks >= 5) {
      player.sendMessage("§cThis pedestal already has enough energy.");
    } else if (!this.isMatchingState(playerId)) {
      item.remove();
      restorationRitualSession.durationTicks++;
      if (restorationRitualSession.durationTicks > 5) {
        restorationRitualSession.durationTicks = 5;
      }

      this.lastUseTimes.put(playerId, System.currentTimeMillis());
      this.activateAbility(player, playerId);
      location.getWorld().playSound(location, Sound.BLOCK_BEACON_AMBIENT, 40.0F, 1.6F);
      location.getWorld().playSound(location, Sound.ITEM_AXE_SCRAPE, 10.0F, 0.5F);
      location.getWorld().playSound(location, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 20.0F, 1.2F);

      for (Player targetPlayer : location.getWorld().getPlayers()) {
        if (targetPlayer.getLocation().distance(location) <= 15.0) {
          targetPlayer.sendMessage(ChatColor.of("#96FFD9").toString() + restorationRitualSession.durationTicks + "/5 energy deposited!");
          if (restorationRitualSession.durationTicks >= 5) {
            targetPlayer.sendMessage(
                ChatColor.of("#FFD773") + "Energy deposit requirement has been fulfilled!");
            targetPlayer.sendMessage(ChatColor.of("#96FFD9") + "Please stand on the pedestal center!");
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 30.0F, 1.0F);
          } else {
            targetPlayer.sendMessage("§cPlease wait for pedestal cooldown...");
          }
        }
      }
    }
  }

  void activateAbility(Player player, UUID playerId) {
    long timestamp = this.durationTicks * 1000L;
    long lastUpdateTime = timestamp / 50L;
    new BukkitRunnable() {
      final long lastUpdateTime = timestamp;
      @Override
      public void run() {
        if (RestorationRitual.this.lastUseTimes.containsKey(playerId)) {
          long timestamp = RestorationRitual.this.lastUseTimes.get(playerId);
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

  void spawnAbilityEffects() {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    String[][] text =
        new String[][] {
          {"wealth", "GREEN"},
          {"strength", "RED"},
          {"speed", "YELLOW"},
          {"fire", "GOLD"},
          {"flux", "AQUA"},
          {"astra", "DARK_PURPLE"},
          {"life", "LIGHT_PURPLE"},
          {"puff", "WHITE"}
        };

    for (String[] message : text) {
      Team team = scoreboard.getTeam(message[0]);
      if (team == null) {
        team = scoreboard.registerNewTeam(message[0]);
      }

      team.setColor(org.bukkit.ChatColor.valueOf(message[1]));
      team.setCanSeeFriendlyInvisibles(false);
    }
  }

  void startActiveRitualPhase(Player player, Location location, RestorationRitualSession restorationRitualSession) {
    restorationRitualSession.currentActive = true;
    this.secondaryFlagsByPlayer.put(player.getUniqueId(), true);
    this.handleAbilityAction(location, restorationRitualSession);
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
        this.scheduleAbilityUpdate(player, location, restorationRitualSession);
        this.resolveGemEnergyManager().scheduleAbilityUpdate(player, 5);

      }, 100L);
  }

  void scheduleAbilityUpdate(Player player, Location location, RestorationRitualSession restorationRitualSession) {
    restorationRitualSession.activeRunnable = this.createOrbitAnimationTask(location, restorationRitualSession);
    restorationRitualSession.activeRunnable.runTaskTimer(this.plugin, 0L, 1L);
    restorationRitualSession.secondaryRunnable = this.createRitualTeamCycleTask(player);
    restorationRitualSession.secondaryRunnable.runTaskTimer(this.plugin, 0L, 20L);
    player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0, false, false));
    player.teleport(player.getLocation().setDirection(location.getDirection()));
    if (restorationRitualSession.active) {
      this.resetAbilityState(player, restorationRitualSession);
    }

    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;

      @Override
      public void run() {
        this.durationTicks++;
        if (this.durationTicks <= 260) {
          Location location = player.getLocation().add(0.0, 0.03, 0.0);
          player.teleport(location);

          for (ItemDisplay itemDisplay : restorationRitualSession.entries) {
            if (itemDisplay != null && itemDisplay.isValid()) {
              Location targetLocation = itemDisplay.getLocation().add(0.0, 0.03, 0.0);
              itemDisplay.teleport(targetLocation);
            }
          }

          if (this.durationTicks % 25 == 0) {
            RestorationRitual.this.updateState(this.anchorLocation);
          }

          if (this.durationTicks == 100
              || this.durationTicks == 118
              || this.durationTicks == 134
              || this.durationTicks == 148
              || this.durationTicks == 160
              || this.durationTicks == 170
              || this.durationTicks == 178
              || this.durationTicks == 184
              || this.durationTicks == 189
              || this.durationTicks == 194
              || this.durationTicks == 198
              || this.durationTicks == 204
              || this.durationTicks == 210
              || this.durationTicks == 216
              || this.durationTicks == 220
              || this.durationTicks == 226
              || this.durationTicks == 232) {
            if (this.durationTicks >= 216) {
              player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            }

            if (this.durationTicks >= 216) {
              if (restorationRitualSession.secondaryRunnable != null) {
                restorationRitualSession.secondaryRunnable.cancel();
              }

              restorationRitualSession.secondaryRunnable = RestorationRitual.this.createTeleportTeamCycleTask(player);
              restorationRitualSession.secondaryRunnable.runTaskTimer(RestorationRitual.this.plugin, 0L, 5L);
            }
          }

          if (this.durationTicks >= 216) {
            RestorationRitual.this.completeAbilityAction(player, restorationRitualSession, this.anchorLocation);
          }

          if (this.durationTicks == 237) {
            RestorationRitual.this.processAbilityState(player, restorationRitualSession);
          }
        }

        if (this.durationTicks >= 260) {
          RestorationRitual.this.updatePlayerState(player, this.anchorLocation, restorationRitualSession);
          this.cancel();
        }
      }
    }.runTaskTimer(this.plugin, 0L, 1L);
  }

  BukkitRunnable createRitualTeamCycleTask(Player player) {
    String[] text =
        new String[] {"wealth", "strength", "speed", "fire", "flux", "astra", "life", "puff"};
    return new BukkitRunnable() {
      final String[] teamNames = text;
      int durationTicks = 0;

      @Override
      public void run() {
        if (!player.isOnline()) {
          this.cancel();
        } else {
          Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

          for (String text : this.teamNames) {
            Team team = scoreboard.getTeam(text);
            if (team != null && team.hasEntry(player.getName())) {
              team.removeEntry(player.getName());
            }
          }

          Team currentTeam = scoreboard.getTeam(this.teamNames[this.durationTicks]);
          if (currentTeam != null) {
            currentTeam.addEntry(player.getName());
          }

          this.durationTicks = (this.durationTicks + 1) % this.teamNames.length;
        }
      }
    };
  }

  BukkitRunnable createTeleportTeamCycleTask(Player player) {
    String[] text =
        new String[] {"wealth", "strength", "speed", "fire", "flux", "astra", "life", "puff"};
    return new BukkitRunnable() {
      final String[] teamNames = text;
      int durationTicks = 0;

      @Override
      public void run() {
        if (!player.isOnline()) {
          this.cancel();
        } else {
          Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

          for (String text : this.teamNames) {
            Team team = scoreboard.getTeam(text);
            if (team != null && team.hasEntry(player.getName())) {
              team.removeEntry(player.getName());
            }
          }

          Team currentTeam = scoreboard.getTeam(this.teamNames[this.durationTicks]);
          if (currentTeam != null) {
            currentTeam.addEntry(player.getName());
          }

          this.durationTicks = (this.durationTicks + 1) % this.teamNames.length;
        }
      }
    };
  }

  BukkitRunnable createOrbitAnimationTask(
      Location location, RestorationRitualSession restorationRitualSession) {
    return new BukkitRunnable() {
      final Location anchorLocation = location;
      float effectScale = 0.0F;

      @Override
      public void run() {
        if (RestorationRitual.this.sessionsByLocation.containsKey(this.anchorLocation) && !restorationRitualSession.entries.isEmpty()) {
          this.effectScale += 2.0F;
          if (this.effectScale >= 360.0F) {
            this.effectScale = 0.0F;
          }

          RestorationRitual.this.teleportAbilityTarget(this.anchorLocation, restorationRitualSession, this.effectScale);
        } else {
          this.cancel();
        }
      }
    };
  }

  void teleportAbilityTarget(Location location, RestorationRitualSession restorationRitualSession, float scale) {
    double value = 3.0;

    for (int count = 0; count < restorationRitualSession.entries.size(); count++) {
      ItemDisplay itemDisplay = restorationRitualSession.entries.get(count);
      if (itemDisplay != null && itemDisplay.isValid()) {
        double angleRadians = Math.toRadians(scale + 45.0 * count);
        double offset = location.getX() + 0.5 + Math.cos(angleRadians) * value;
        double distance = location.getZ() + 0.5 + Math.sin(angleRadians) * value;
        double radius = itemDisplay.getLocation().getY();
        Location effectLocation = new Location(location.getWorld(), offset, radius, distance);
        itemDisplay.teleport(effectLocation);
      }
    }
  }

  void completeAbilityAction(Player player, RestorationRitualSession restorationRitualSession, Location location) {
    Location targetLocation = player.getLocation().add(0.0, 1.0, 0.0);

    for (ItemDisplay itemDisplay : restorationRitualSession.entries) {
      if (itemDisplay != null && itemDisplay.isValid()) {
        Location origin = itemDisplay.getLocation();
        Location center = targetLocation.clone();
        double x = center.getX() - origin.getX();
        double y = center.getY() - origin.getY();
        double z = center.getZ() - origin.getZ();
        double value = 0.15;
        Location destination = origin.add(x * value, y * value, z * value);
        itemDisplay.setInterpolationDelay(0);
        itemDisplay.setInterpolationDuration(5);
        itemDisplay.setTeleportDuration(1);
        itemDisplay.teleport(destination);
      }
    }
  }

  void processAbilityState(Player player, RestorationRitualSession restorationRitualSession) {
    new BukkitRunnable() {
      int durationTicks = 0;

      @Override
      public void run() {
        this.durationTicks++;
        Location location = player.getLocation().add(0.0, 1.0, 0.0);

        for (ItemDisplay itemDisplay : restorationRitualSession.entries) {
          if (itemDisplay != null && itemDisplay.isValid()) {
            Transformation transformation = itemDisplay.getTransformation();
            float scale = Math.max(0.05F, 0.5F - this.durationTicks * 0.025F);
            transformation.getScale().set(scale, scale, scale);
            itemDisplay.setTransformation(transformation);
            Location targetLocation = itemDisplay.getLocation();
            double distance = targetLocation.distance(location);
            double value = 0.08;
            double radius = Math.max(1.0, 5.0 - distance * 1.0);
            double angle = value * radius;
            double x = location.getX() - targetLocation.getX();
            double y = location.getY() - targetLocation.getY();
            double z = location.getZ() - targetLocation.getZ();
            double progress = Math.sqrt(x * x + y * y + z * z);
            if (progress > 0.1) {
              x = x / progress * angle;
              y = y / progress * angle;
              z = z / progress * angle;
              Location origin = targetLocation.add(x, y, z);
              itemDisplay.setInterpolationDelay(0);
              itemDisplay.setInterpolationDuration(5);
              itemDisplay.setTeleportDuration(5);
              itemDisplay.teleport(origin);
            }

            if (distance < 2.0) {
              double amount = this.durationTicks * 0.5;
              double speed = Math.max(0.1, distance * 0.5);
              double offset = Math.cos(amount) * speed * 0.1;
              double height = Math.sin(amount) * speed * 0.1;
              Location center = itemDisplay.getLocation().add(offset, 0.0, height);
              itemDisplay.setInterpolationDelay(0);
              itemDisplay.setInterpolationDuration(5);
              itemDisplay.setTeleportDuration(5);
              itemDisplay.teleport(center);
              player
              .getWorld()
              .spawnParticle(Particle.ENCHANT, itemDisplay.getLocation(), 2, 0.1, 0.1, 0.1, 0.1);
            }
          }
        }

        if (this.durationTicks >= 40) {
          for (ItemDisplay currentItemDisplay : restorationRitualSession.entries) {
            if (currentItemDisplay != null && currentItemDisplay.isValid()) {
              player
              .getWorld()
              .spawnParticle(Particle.EXPLOSION, currentItemDisplay.getLocation(), 1, 0.0, 0.0, 0.0, 0.0);
              currentItemDisplay.remove();
            }
          }

          restorationRitualSession.entries.clear();
          this.cancel();
        }
      }
    }.runTaskTimer(this.plugin, 0L, 1L);
  }

  void updateState(Location location) {
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

  void handleAbilityAction(Location location, RestorationRitualSession restorationRitualSession) {
    ItemStack[] ritualItems = {
      BlissItems.createWealthTierTwoGemStage11(),
      BlissItems.createStrengthTierTwoGemStage6(),
      BlissItems.createSpeedTierTwoGemStage6(),
      BlissItems.createFireTierTwoGemStage6(),
      BlissItems.createFluxTierTwoGemStage6(),
      BlissItems.createAstraTierTwoGemStage6(),
      BlissItems.createLifeTierTwoGemStage6(),
      BlissItems.createPuffTierTwoGemStage6()
    };
    String[] gemTypes = {"wealth", "strength", "speed", "fire", "flux", "astra", "life", "puff"};
    this.spawnAbilityEffects();
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    World world = location.getWorld();
    double value = 3.0;

    for (int count = 0; count < 8; count++) {
      double distance = (Math.PI / 4) * count;
      double offset = location.getX() + 0.5 + Math.cos(distance) * value;
      double radius = location.getZ() + 0.5 + Math.sin(distance) * value;
      double y = location.getY() - 1.0;
      Location targetLocation = new Location(world, offset, y, radius);
      ItemDisplay itemDisplay = (ItemDisplay) world.spawnEntity(targetLocation, EntityType.ITEM_DISPLAY);
      itemDisplay.setItemStack(ritualItems[count]);
      itemDisplay.setGravity(false);
      itemDisplay.setInvulnerable(true);
      itemDisplay.setGlowing(true);
      itemDisplay.setPersistent(false);
      Transformation transformation = itemDisplay.getTransformation();
      transformation.getScale().set(0.5F, 0.5F, 0.5F);
      itemDisplay.setTransformation(transformation);
      itemDisplay.setBillboard(Billboard.CENTER);
      itemDisplay.setBrightness(new Brightness(15, 15));
      itemDisplay.setInterpolationDelay(0);
      itemDisplay.setInterpolationDuration(5);
      itemDisplay.setTeleportDuration(5);
      Team team = scoreboard.getTeam(gemTypes[count]);
      if (team != null) {
        team.addEntry(itemDisplay.getUniqueId().toString());
      }

      restorationRitualSession.entries.add(itemDisplay);
    }

    new BukkitRunnable() {
      final Location anchorLocation = location;
      final double damageAmount = value;
      int durationTicks = 0;
      final double distanceThreshold = this.anchorLocation.getY() + 1.6;
      final double effectRadius = this.anchorLocation.getY() - 1.0;
      final double effectScale = this.distanceThreshold - this.effectRadius;

      @Override
      public void run() {
        this.durationTicks++;
        double value = Math.min(1.0, this.durationTicks / 100.0);
        double distance = 1.0 - Math.pow(1.0 - value, 3.0);
        double radius = this.effectRadius + this.effectScale * distance;

        for (int count = 0; count < restorationRitualSession.entries.size(); count++) {
          ItemDisplay itemDisplay = restorationRitualSession.entries.get(count);
          if (itemDisplay != null && itemDisplay.isValid()) {
            double angle = (Math.PI / 4) * count;
            double offset = this.anchorLocation.getX() + 0.5 + Math.cos(angle) * this.damageAmount;
            double progress = this.anchorLocation.getZ() + 0.5 + Math.sin(angle) * this.damageAmount;
            Location location = new Location(world, offset, radius, progress);
            itemDisplay.teleport(location);
            if (radius >= this.anchorLocation.getY()) {
              itemDisplay.setGlowing(true);
            }
          }
        }

        if (this.durationTicks >= 100) {
          this.cancel();
        }
      }
    }.runTaskTimer(this.plugin, 0L, 1L);
  }

  void resetAbilityState(Player player, RestorationRitualSession restorationRitualSession) {
    if (this.canUseAbility(player)
        || this.isAbilityBlocked(player.getInventory().getItemInOffHand())) {
      this.activateActiveAbility(player, restorationRitualSession);
      int count = this.getAbilityIntValue(player);
      this.trackAbilityState(player, restorationRitualSession, count, 216L);
    }
  }

  void trackAbilityState(Player player, RestorationRitualSession restorationRitualSession, int count, long timestamp) {
    Bukkit.getScheduler()
        .runTaskLater(this.plugin, () -> this.scheduleGemRollingPhase(player, restorationRitualSession, count), timestamp);
  }

  void refreshPlayerState(Player player, RestorationRitualSession restorationRitualSession, int count) {
    boolean enabled = count >= 5;
    int[] index =
        switch (count) {
          case 1 -> new int[] {14, 14, 12, 12, 10, 10, 8, 8};
          case 2 -> new int[] {12, 12, 10, 10, 8, 8, 6, 6};
          case 3 -> new int[] {10, 10, 8, 8, 6, 6, 5, 5};
          case 4 -> new int[] {8, 8, 6, 6, 5, 5, 4, 4};
          case 5 -> new int[] {6, 6, 5, 5, 4, 4, 3, 3};
          default -> new int[] {8, 8, 6, 6, 5, 5, 4, 4};
        };
    int remaining = 0;
    this.activatePendingAbility(player, restorationRitualSession, this.createItem(count));

    for (int step = 0; step < index.length; step++) {
      remaining += index[step];
      boolean active = step == index.length - 1;
      int currentStep = step;
      Bukkit.getScheduler()
          .runTaskLater(
              this.plugin,
              () -> this.startGemRolling(count, player, restorationRitualSession, active, enabled, currentStep),
              remaining);
    }
  }

  void updatePlayerState(Player player, Location location, RestorationRitualSession restorationRitualSession) {
    if (restorationRitualSession.activeRunnable != null) {
      restorationRitualSession.activeRunnable.cancel();
    }

    if (restorationRitualSession.secondaryRunnable != null) {
      restorationRitualSession.secondaryRunnable.cancel();
    }

    if (restorationRitualSession.runnable != null) {
      restorationRitualSession.runnable.cancel();
    }

    if (restorationRitualSession.active && restorationRitualSession.item != null) {
      Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.finishRitual(player, restorationRitualSession), 10L);
    }

    this.updateAbilityState(player.getUniqueId());
    this.secondaryFlagsByPlayer.remove(player.getUniqueId());
    player.removePotionEffect(PotionEffectType.GLOWING);
    this.finishAbilityAction(player.getLocation());
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
        this.activatePrimaryAbility(player);

      }, 200L);
    if (restorationRitualSession.enderCrystal != null && restorationRitualSession.enderCrystal.isValid()) {
      restorationRitualSession.enderCrystal.remove();
    }

    for (ItemDisplay itemDisplay : restorationRitualSession.entries) {
      if (itemDisplay != null && itemDisplay.isValid()) {
        itemDisplay.remove();
      }
    }

    this.sessionsByLocation.remove(location);
    this.lastUseTimes.remove(player.getUniqueId());
    this.applyAbilityMotion(location);

    for (Player targetPlayer : Bukkit.getOnlinePlayers()) {
      targetPlayer.sendMessage(
          "§f"
              + player.getName()
              + " §ahas successfully completed their Restoration ritual!");
      targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 40.0F, 1.0F);
      targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_WITHER_DEATH, 40.0F, 1.0F);
      targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 40.0F, 1.0F);
    }
  }

  void finishAbilityAction(Location location) {
    new BukkitRunnable() {
      final Location anchorLocation = location;
      int durationTicks = 0;

      @Override
      public void run() {
        this.durationTicks++;

        for (int count = 0; count < 50; count++) {
          double value = 2.0 + this.durationTicks * 0.1;
          double distance = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * 2.0 * Math.PI;
          double radius = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * Math.PI;
          double offset = this.anchorLocation.getX() + value * Math.sin(radius) * Math.cos(distance);
          double angle = this.anchorLocation.getY() + value * Math.cos(radius);
          double progress = this.anchorLocation.getZ() + value * Math.sin(radius) * Math.sin(distance);
          Location effectLocation = new Location(this.anchorLocation.getWorld(), offset, angle, progress);
          DustOptions dustOptions = new DustOptions(Color.fromRGB(128, 0, 128), 1.5F);
          this.anchorLocation.getWorld().spawnParticle(Particle.DUST, effectLocation, 2, 0.0, 0.0, 0.0, 0.0, dustOptions);
          if (count % 5 == 0) {
            DustOptions dust = new DustOptions(Color.fromRGB(255, 0, 255), 1.5F);
            this.anchorLocation.getWorld().spawnParticle(Particle.DUST, effectLocation, 2, 0.0, 0.0, 0.0, 0.0, dust);
          }
        }

        if (this.durationTicks >= 60) {
          this.cancel();
        }
      }
    }.runTaskTimer(this.plugin, 0L, 1L);
  }

  void activatePrimaryAbility(Player player) {
    try {
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
      String[] text =
          new String[] {
            "wealth",
            "strength",
            "speed",
            "fire",
            "flux",
            "astra",
            "life",
            "puff",
            "WealthGem",
            "StrengthGem",
            "SpeedGem",
            "FireGem",
            "FluxGem",
            "AstraGem",
            "LifeGem",
            "PuffGem",
            "gem_wealth",
            "gem_strength",
            "gem_speed",
            "gem_fire",
            "gem_flux",
            "gem_astra",
            "gem_life",
            "gem_puff"
          };

      for (String message : text) {
        Team team = scoreboard.getTeam(message);
        if (team != null && team.hasEntry(player.getName())) {
          team.removeEntry(player.getName());
        }
      }

      for (Team currentTeam : scoreboard.getTeams()) {
        if (currentTeam.getName().toLowerCase().contains("gem") && currentTeam.hasEntry(player.getName())) {
          currentTeam.removeEntry(player.getName());
        }
      }
    } catch (Exception exception) {
      this.plugin
          .getLogger()
          .log(
              java.util.logging.Level.WARNING,
              "Error removing player " + player.getName() + " from gem teams",
              exception);
    }
  }

  public boolean isMatchingState(UUID playerId) {
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

  boolean isValidTarget(ItemStack item) {
    return this.isAbilityAllowed(item, Material.NAUTILUS_SHELL, 300);
  }

  public void cleanupRitual(Location location) {
    RestorationRitualSession restorationRitualSession = this.sessionsByLocation.get(location);
    if (restorationRitualSession != null) {
      if (restorationRitualSession.enderCrystal != null && restorationRitualSession.enderCrystal.isValid()) {
        restorationRitualSession.enderCrystal.remove();
      }

      if (restorationRitualSession.activeRunnable != null) {
        restorationRitualSession.activeRunnable.cancel();
      }

      if (restorationRitualSession.secondaryRunnable != null) {
        restorationRitualSession.secondaryRunnable.cancel();
      }

      if (restorationRitualSession.runnable != null) {
        restorationRitualSession.runnable.cancel();
      }

      for (ItemDisplay itemDisplay : restorationRitualSession.entries) {
        if (itemDisplay != null && itemDisplay.isValid()) {
          itemDisplay.remove();
        }
      }

      Player player = Bukkit.getPlayer(restorationRitualSession.playerId);
      if (player != null) {
        this.activateCachedAbility(player, restorationRitualSession);
        this.secondaryFlagsByPlayer.remove(restorationRitualSession.playerId);
        player.removePotionEffect(PotionEffectType.GLOWING);
      }

      this.sessionsByLocation.remove(location);
      this.lastUseTimes.remove(restorationRitualSession.playerId);
      location.getWorld().playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 30.0F, 1.0F);
    }
  }

  public void shutdown() {
    for (Location ritualLocation : new ArrayList<>(this.sessionsByLocation.keySet())) {
      this.cleanupRitual(ritualLocation);
    }
    this.sessionsByLocation.clear();
    this.lastUseTimes.clear();
    this.activeFlagsByPlayer.clear();
    this.secondaryFlagsByPlayer.clear();
    this.playerIds.clear();
  }

  ItemStack createItem(int count) {
    int index = this.random.nextInt(8);
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
    } else if (count == 6) {
      switch (index) {
        case 0:
          return BlissItems.createPuffTierTwoGemStage6();
        case 1:
          return BlissItems.createLifeTierTwoGemStage6();
        case 2:
          return BlissItems.createAstraTierTwoGemStage6();
        case 3:
          return BlissItems.createWealthTierTwoGemStage11();
        case 4:
          return BlissItems.createSpeedTierTwoGemStage6();
        case 5:
          return BlissItems.createFireTierTwoGemStage6();
        case 6:
          return BlissItems.createFluxTierTwoGemStage6();
        case 7:
          return BlissItems.createStrengthTierTwoGemStage6();
      }
    }

    return BlissItems.createPuffTierOneGemStage6();
  }

  ItemStack createAbilityItem() {
    return this.entries.get(this.random.nextInt(this.entries.size())).clone();
  }

  int getAbilityIntValue(Player player) {
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    if (this.isProtectedTarget(offHandItem)) {
      return 6;
    }

    for (ItemStack item : player.getInventory().getContents()) {
      if (this.isProtectedTarget(item)) {
        return 6;
      }
    }

    return 5;
  }

  boolean isProtectedTarget(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      return itemMeta.hasCustomModelData() && restorationModelData.contains(itemMeta.getCustomModelData());
    } else {
      return false;
    }
  }

  void activateTargetAbility(Player player, ItemStack item) {
    for (int count = 0; count < player.getInventory().getSize(); count++) {
      ItemStack heldItem = player.getInventory().getItem(count);
      if (this.isActiveForPlayer(heldItem)) {
        player.getInventory().setItem(count, item.clone());
        break;
      }
    }
  }

  void activateSourceAbility(Player player, ItemStack item) {
    if (this.isAbilityBlocked(player.getInventory().getItemInOffHand())) {
      player.getInventory().setItemInOffHand(item.clone());
    } else {
      for (int count = 0; count < player.getInventory().getSize(); count++) {
        ItemStack heldItem = player.getInventory().getItem(count);
        if (this.isAbilityBlocked(heldItem)) {
          player.getInventory().setItem(count, item.clone());
          return;
        }
      }

      this.activateTargetAbility(player, item);
    }
  }

  void activateActiveAbility(Player player, RestorationRitualSession restorationRitualSession) {
    if (!restorationRitualSession.pendingActive) {
      ItemStack offHandItem = player.getInventory().getItemInOffHand();
      if (this.isAbilityBlocked(offHandItem)) {
        restorationRitualSession.pendingActive = true;
        restorationRitualSession.activeState = true;
        restorationRitualSession.secondaryItem = offHandItem.clone();
      } else {
        for (int count = 0; count < player.getInventory().getSize(); count++) {
          ItemStack item = player.getInventory().getItem(count);
          if (this.isAbilityBlocked(item)) {
            restorationRitualSession.pendingActive = true;
            restorationRitualSession.cooldownTicks = count;
            restorationRitualSession.secondaryItem = item.clone();
            return;
          }
        }
      }
    }
  }

  void activatePendingAbility(Player player, RestorationRitualSession restorationRitualSession, ItemStack item) {
    if (!restorationRitualSession.pendingActive) {
      this.activateActiveAbility(player, restorationRitualSession);
    }

    if (restorationRitualSession.activeState) {
      player.getInventory().setItemInOffHand(item.clone());
    } else if (restorationRitualSession.cooldownTicks >= 0) {
      player.getInventory().setItem(restorationRitualSession.cooldownTicks, item.clone());
    } else {
      this.activateSourceAbility(player, item);
    }
  }

  void activateCachedAbility(Player player, RestorationRitualSession restorationRitualSession) {
    if (!restorationRitualSession.secondaryActive && restorationRitualSession.pendingActive && restorationRitualSession.secondaryItem != null) {
      if (restorationRitualSession.activeState) {
        player.getInventory().setItemInOffHand(restorationRitualSession.secondaryItem.clone());
      } else if (restorationRitualSession.cooldownTicks >= 0) {
          player.getInventory().setItem(restorationRitualSession.cooldownTicks, restorationRitualSession.secondaryItem.clone());
        }
      
    }
  }

  boolean isActiveForPlayer(ItemStack item) {
    if (this.isAbilityBlocked(item)) {
      return true;
    }

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

  void finishRitual(Player player, RestorationRitualSession restorationRitualSession) {
    this.activatePendingAbility(player, restorationRitualSession, restorationRitualSession.item);
    restorationRitualSession.secondaryActive = true;
  }

  void startGemRolling(
      int count, Player player, RestorationRitualSession restorationRitualSession, boolean enabled, boolean active, int index) {
    ItemStack item = this.createItem(count);
    this.activatePendingAbility(player, restorationRitualSession, item);
    if (enabled) {
      if (active) {
        restorationRitualSession.item = item.clone();
      }

      restorationRitualSession.maxCount = count;
    }
  }

  void scheduleGemRollingPhase(Player player, RestorationRitualSession restorationRitualSession, int count) {
    this.refreshPlayerState(player, restorationRitualSession, count);
  }

  static {
    restorationModelData = new HashSet<>(Arrays.asList(96));
  }
}
