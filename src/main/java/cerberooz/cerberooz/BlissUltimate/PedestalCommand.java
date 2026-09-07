package cerberooz.cerberooz.BlissUltimate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;

public class PedestalCommand implements CommandExecutor, Listener {
  final Bliss plugin;
  File file;
  public static final Set<Location> trackedLocations;

  public PedestalCommand(Bliss bliss) {
    this.plugin = bliss;
    this.initialize();
  }

  void initialize() {
    this.file = new File(this.plugin.getDataFolder(), "rituals.yml");
    if (this.file.exists()) {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(this.file);
      if (config.contains("protectedBlocks")) {
        for (String text : config.getConfigurationSection("protectedBlocks").getKeys(false)) {
          World world = Bukkit.getWorld(config.getString("protectedBlocks." + text + ".world"));
          int configuredValue = config.getInt("protectedBlocks." + text + ".x");
          int index = config.getInt("protectedBlocks." + text + ".y");
          int remaining = config.getInt("protectedBlocks." + text + ".z");
          if (world != null) {
            trackedLocations.add(new Location(world, configuredValue, index, remaining));
          }
        }
      }
    }
  }

  void refreshAbilityState() {
    if (!this.plugin.getDataFolder().exists()) {
      this.plugin.getDataFolder().mkdirs();
    }

    this.file = new File(this.plugin.getDataFolder(), "rituals.yml");
    YamlConfiguration config = YamlConfiguration.loadConfiguration(this.file);
    config.set("protectedBlocks", null);
    int count = 0;

    for (Location location : trackedLocations) {
      config.set("protectedBlocks." + count + ".world", location.getWorld().getName());
      config.set("protectedBlocks." + count + ".x", location.getBlockX());
      config.set("protectedBlocks." + count + ".y", location.getBlockY());
      config.set("protectedBlocks." + count + ".z", location.getBlockZ());
      count++;
    }

    try {
      config.save(this.file);
    } catch (IOException exception) {
      this.plugin.getLogger().log(Level.SEVERE, "Could not save protected blocks!", exception);
    }
  }

  public boolean onCommand(CommandSender commandSender, Command command, String argument, String[] message) {
    if (commandSender instanceof Player player) {
      if (message.length > 0 && message[0].equalsIgnoreCase("set")) {
        Location location = player.getLocation().subtract(0.0, 1.0, 0.0);
        if (!this.plugin.getDataFolder().exists()) {
          this.plugin.getDataFolder().mkdirs();
        }

        this.file = new File(this.plugin.getDataFolder(), "rituals.yml");
        if (!this.file.exists()) {
          try {
            this.file.createNewFile();
          } catch (IOException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not create rituals.yml", exception);
          }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(this.file);
        config.set("pedestal.world", location.getWorld().getName());
        config.set("pedestal.x", location.getBlockX());
        config.set("pedestal.y", location.getBlockY());
        config.set("pedestal.z", location.getBlockZ());

        try {
          config.save(this.file);
          player.sendMessage(ChatColor.GREEN + "Pedestal location saved!");
        } catch (IOException currentIOException) {
          player.sendMessage(ChatColor.RED + "Failed to save pedestal location!");
          this.plugin
              .getLogger()
              .log(java.util.logging.Level.SEVERE, "Could not save the pedestal location", currentIOException);
        }

        return true;
      } else if (message.length > 0 && message[0].equalsIgnoreCase("activate")) {
        this.file = new File(this.plugin.getDataFolder(), "rituals.yml");
        if (!this.file.exists()) {
          player.sendMessage(ChatColor.RED + "No pedestal has been set yet!");
          return true;
        }

        YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(this.file);
        String configuredText = currentConfig.getString("pedestal.world");
        int configuredValue = currentConfig.getInt("pedestal.x");
        int index = currentConfig.getInt("pedestal.y");
        int remaining = currentConfig.getInt("pedestal.z");
        World world = Bukkit.getWorld(configuredText);
        if (world == null) {
          player.sendMessage(ChatColor.RED + "World not found!");
          return true;
        }

        Location targetLocation = new Location(world, configuredValue, index, remaining);
        targetLocation
            .getWorld()
            .playSound(targetLocation, Sound.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 10.0F, 1.5F);
        Block block = targetLocation.getBlock();
        block.setType(Material.BEACON);
        String[][] displayText =
            new String[][] {
              {" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " "},
              {" ", " ", " ", " ", " ", "X", "X", "X", " ", " ", " ", " ", " "},
              {" ", " ", " ", " ", "V", "V", "V", "V", "V", " ", " ", " ", " "},
              {" ", " ", " ", "V", "R", "D", "R", "D", "R", "V", " ", " ", " "},
              {" ", " ", "V", "R", "D", "A", "Y", "A", "D", "R", "V", " ", " "},
              {" ", "X", "V", "D", "A", "T", "S", "T", "A", "D", "V", "X", " "},
              {" ", "X", "V", "R", "Y", "S", "B", "S", "Y", "R", "V", "X", " "},
              {" ", "X", "V", "D", "A", "T", "S", "T", "A", "D", "V", "X", " "},
              {" ", " ", "V", "R", "D", "A", "Y", "A", "D", "R", "V", " ", " "},
              {" ", " ", " ", "V", "R", "D", "R", "D", "R", "V", " ", " ", " "},
              {" ", " ", " ", " ", "V", "V", "V", "V", "V", " ", " ", " ", " "},
              {" ", " ", " ", " ", " ", "X", "X", "X", " ", " ", " ", " ", " "},
              {" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " "}
            };
        HashMap<String, Material> materialsByKey = new HashMap<>();
        materialsByKey.put("B", Material.BEACON);
        materialsByKey.put("R", Material.CHERRY_PLANKS);
        materialsByKey.put("Y", Material.CHERRY_LOG);
        materialsByKey.put("S", Material.GILDED_BLACKSTONE);
        materialsByKey.put("A", Material.RAW_GOLD_BLOCK);
        materialsByKey.put("T", Material.GOLD_BLOCK);
        materialsByKey.put("D", Material.STRIPPED_CHERRY_WOOD);
        materialsByKey.put("X", Material.SMOOTH_QUARTZ_STAIRS);
        materialsByKey.put("V", Material.QUARTZ_BRICKS);
        int count = targetLocation.getBlockX();
        int step = targetLocation.getBlockZ();
        int ticks = displayText.length / 2;

        for (int durationTicks = 0; durationTicks < displayText.length; durationTicks++) {
          String[] formattedText = displayText[durationTicks];
          int attempts = formattedText.length / 2;

          for (int amount = 0; amount < formattedText.length; amount++) {
            String label = formattedText[amount];
            if (!label.equals(" ")) {
              Material material = materialsByKey.get(label);
              if (material != null) {
                int level = count + (amount - attempts);
                int radius = step + (durationTicks - ticks);
                Block targetBlock = targetLocation.getWorld().getBlockAt(level, targetLocation.getBlockY(), radius);
                int size = amount - attempts;
                int limit = durationTicks - ticks;
                if (material == Material.CHERRY_LOG) {
                  targetBlock.setType(material, false);
                  Orientable orientable = (Orientable) targetBlock.getBlockData();
                  if (Math.abs(size) >= Math.abs(limit)) {
                    orientable.setAxis(Axis.X);
                  } else {
                    orientable.setAxis(Axis.Z);
                  }

                  targetBlock.setBlockData(orientable, false);
                } else if (material == Material.SMOOTH_QUARTZ_STAIRS) {
                  targetBlock.setType(material, false);
                  Stairs stairs = (Stairs) targetBlock.getBlockData();
                  if (Math.abs(size) >= Math.abs(limit)) {
                    if (size > 0) {
                      stairs.setFacing(BlockFace.WEST);
                    } else {
                      stairs.setFacing(BlockFace.EAST);
                    }
                  } else if (limit > 0) {
                    stairs.setFacing(BlockFace.NORTH);
                  } else {
                    stairs.setFacing(BlockFace.SOUTH);
                  }

                  targetBlock.setBlockData(stairs, false);
                } else {
                  targetBlock.setType(material);
                }

                trackedLocations.add(targetBlock.getLocation());
              }
            }
          }
        }

        this.refreshAbilityState();
        player.sendMessage(ChatColor.LIGHT_PURPLE + "The ritual has been activated!");
        return true;
      } else if (message.length > 0 && message[0].equalsIgnoreCase("deactivate")) {
        this.file = new File(this.plugin.getDataFolder(), "rituals.yml");
        if (!this.file.exists()) {
          player.sendMessage(ChatColor.RED + "No pedestal has been set yet!");
          return true;
        }

        YamlConfiguration targetConfig = YamlConfiguration.loadConfiguration(this.file);
        String name = targetConfig.getString("pedestal.world");
        int offset = targetConfig.getInt("pedestal.x");
        int slot = targetConfig.getInt("pedestal.y");
        int page = targetConfig.getInt("pedestal.z");
        World targetWorld = Bukkit.getWorld(name);
        if (targetWorld == null) {
          player.sendMessage(ChatColor.RED + "World not found!");
          return true;
        }

        Location origin = new Location(targetWorld, offset, slot, page);
        origin
            .getWorld()
            .playSound(origin, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 10.0F, 1.5F);
        Block sourceBlock = origin.getBlock();
        sourceBlock.setType(Material.BEDROCK);
        trackedLocations.add(sourceBlock.getLocation());
        String[][] configPath =
            new String[][] {
              {" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " "},
              {" ", " ", " ", " ", " ", "X", "X", "X", " ", " ", " ", " ", " "},
              {" ", " ", " ", " ", "V", "V", "V", "V", "V", " ", " ", " ", " "},
              {" ", " ", " ", "V", "R", "D", "R", "D", "R", "V", " ", " ", " "},
              {" ", " ", "V", "R", "D", "A", "Y", "A", "D", "R", "V", " ", " "},
              {" ", "X", "V", "D", "A", "T", "S", "T", "A", "D", "V", "X", " "},
              {" ", "X", "V", "R", "Y", "S", "B", "S", "Y", "R", "V", "X", " "},
              {" ", "X", "V", "D", "A", "T", "S", "T", "A", "D", "V", "X", " "},
              {" ", " ", "V", "R", "D", "A", "Y", "A", "D", "R", "V", " ", " "},
              {" ", " ", " ", "V", "R", "D", "R", "D", "R", "V", " ", " ", " "},
              {" ", " ", " ", " ", "V", "V", "V", "V", "V", " ", " ", " ", " "},
              {" ", " ", " ", " ", " ", "X", "X", "X", " ", " ", " ", " ", " "},
              {" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " "}
            };
        HashMap<String, Material> alternateMaterialsByKey = new HashMap<>();
        alternateMaterialsByKey.put("B", Material.BEDROCK);
        alternateMaterialsByKey.put("R", Material.POLISHED_DEEPSLATE);
        alternateMaterialsByKey.put("Y", Material.POLISHED_BASALT);
        alternateMaterialsByKey.put("S", Material.BLACKSTONE);
        alternateMaterialsByKey.put("A", Material.BASALT);
        alternateMaterialsByKey.put("T", Material.NETHERITE_BLOCK);
        alternateMaterialsByKey.put("D", Material.DEEPSLATE_TILES);
        alternateMaterialsByKey.put("X", Material.BLACKSTONE_STAIRS);
        alternateMaterialsByKey.put("V", Material.POLISHED_BLACKSTONE_BRICKS);
        int progressStep = origin.getBlockX();
        int particleCount = origin.getBlockZ();
        int hitCount = configPath.length / 2;

        for (int rowIndex = 0; rowIndex < configPath.length; rowIndex++) {
          String[] row = configPath[rowIndex];
          int rowCenter = row.length / 2;

          for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
            String materialKey = row[columnIndex];
            if (!materialKey.equals(" ")) {
              Material targetMaterial = alternateMaterialsByKey.get(materialKey);
              if (targetMaterial != null) {
                int blockX = progressStep + (columnIndex - rowCenter);
                int blockZ = particleCount + (rowIndex - hitCount);
                Block affectedBlock = origin.getWorld().getBlockAt(blockX, origin.getBlockY(), blockZ);
                int xOffset = columnIndex - rowCenter;
                int zOffset = rowIndex - hitCount;
                if (targetMaterial == Material.BASALT) {
                  affectedBlock.setType(targetMaterial, false);
                  Orientable currentOrientable = (Orientable) affectedBlock.getBlockData();
                  if (Math.abs(xOffset) >= Math.abs(zOffset)) {
                    currentOrientable.setAxis(Axis.X);
                  } else {
                    currentOrientable.setAxis(Axis.Z);
                  }

                  affectedBlock.setBlockData(currentOrientable, false);
                } else if (targetMaterial == Material.POLISHED_BASALT) {
                  affectedBlock.setType(targetMaterial, false);
                  Orientable targetOrientable = (Orientable) affectedBlock.getBlockData();
                  if (Math.abs(xOffset) >= Math.abs(zOffset)) {
                    targetOrientable.setAxis(Axis.X);
                  } else {
                    targetOrientable.setAxis(Axis.Z);
                  }

                  affectedBlock.setBlockData(targetOrientable, false);
                } else if (targetMaterial == Material.BLACKSTONE_STAIRS) {
                  affectedBlock.setType(targetMaterial, false);
                  Stairs currentStairs = (Stairs) affectedBlock.getBlockData();
                  if (Math.abs(xOffset) >= Math.abs(zOffset)) {
                    if (xOffset > 0) {
                      currentStairs.setFacing(BlockFace.WEST);
                    } else {
                      currentStairs.setFacing(BlockFace.EAST);
                    }
                  } else if (zOffset > 0) {
                    currentStairs.setFacing(BlockFace.NORTH);
                  } else {
                    currentStairs.setFacing(BlockFace.SOUTH);
                  }

                  affectedBlock.setBlockData(currentStairs, false);
                } else {
                  affectedBlock.setType(targetMaterial);
                }

                trackedLocations.add(affectedBlock.getLocation());
              }
            }
          }
        }

        this.refreshAbilityState();
        player.sendMessage(ChatColor.LIGHT_PURPLE + "The ritual has been deactivated!");
        return true;
      } else if (message.length > 0 && message[0].equalsIgnoreCase("remove")) {
        this.file = new File(this.plugin.getDataFolder(), "rituals.yml");
        if (!this.file.exists()) {
          player.sendMessage(ChatColor.RED + "No pedestal has been set yet!");
          return true;
        }

        YamlConfiguration sourceConfig = YamlConfiguration.loadConfiguration(this.file);
        String loreText = sourceConfig.getString("pedestal.world");
        int pedestalX = sourceConfig.getInt("pedestal.x");
        int pedestalY = sourceConfig.getInt("pedestal.y");
        int pedestalZ = sourceConfig.getInt("pedestal.z");
        World currentWorld = Bukkit.getWorld(loreText);
        if (currentWorld == null) {
          player.sendMessage(ChatColor.RED + "World not found!");
          return true;
        }

        Location center = new Location(currentWorld, pedestalX, pedestalY, pedestalZ);
        center
            .getWorld()
            .playSound(center, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 5.0F, 0.5F);

        for (Location destination : trackedLocations) {
          Block nearbyBlock = destination.getBlock();
          nearbyBlock.setType(Material.AIR);
        }

        trackedLocations.clear();
        sourceConfig.set("protectedBlocks", null);

        try {
          sourceConfig.save(this.file);
          player.sendMessage(ChatColor.GREEN + "Ritual has been completely removed!");
        } catch (IOException targetIOException) {
          player.sendMessage(ChatColor.RED + "Failed to save changes!");
          this.plugin.getLogger().log(Level.SEVERE, "Could not save rituals.yml after removal", targetIOException);
        }

        return true;
      } else {
        player.sendMessage(ChatColor.RED + "Usage: /" + argument + " <set|activate|deactivate|remove>");
        return true;
      }
    } else {
      commandSender.sendMessage(ChatColor.RED + "Only players can execute this command.");
      return true;
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    if (trackedLocations.contains(event.getBlock().getLocation())) {
      event.setCancelled(true);
      event.getPlayer().sendMessage(ChatColor.RED + "You cannot break any ritual blocks!");
    }
  }

  @EventHandler
  public void onEntityExplode(EntityExplodeEvent event) {
    event.blockList().removeIf(PedestalCommand::isAbilityAllowed);
  }

  @EventHandler
  public void onBlockExplode(BlockExplodeEvent event) {
    event.blockList().removeIf(PedestalCommand::isExplosionBlockAllowed);
  }

  @EventHandler
  public void onBlockBurn(BlockBurnEvent event) {
    if (trackedLocations.contains(event.getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockIgnite(BlockIgniteEvent event) {
    if (event.getBlock() != null && trackedLocations.contains(event.getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockSpread(BlockSpreadEvent event) {
    if (trackedLocations.contains(event.getBlock().getLocation()) || trackedLocations.contains(event.getSource().getLocation())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockFromTo(BlockFromToEvent event) {
    if (trackedLocations.contains(event.getToBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockPistonExtend(BlockPistonExtendEvent event) {
    for (Block block : event.getBlocks()) {
      if (trackedLocations.contains(block.getLocation())) {
        event.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler
  public void onBlockPistonRetract(BlockPistonRetractEvent event) {
    for (Block block : event.getBlocks()) {
      if (trackedLocations.contains(block.getLocation())) {
        event.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler
  public void onBlockGrow(BlockGrowEvent event) {
    if (trackedLocations.contains(event.getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onStructureGrow(StructureGrowEvent event) {
    event.getBlocks().removeIf(PedestalCommand::shouldOnStructureGrow);
  }

  @EventHandler
  public void onLightningStrike(LightningStrikeEvent event) {
    if (event.getLightning().getLocation().getBlock() != null
        && trackedLocations.contains(event.getLightning().getLocation().getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }

  static boolean shouldOnStructureGrow(BlockState blockState) {
    return trackedLocations.contains(blockState.getLocation());
  }

  static boolean isExplosionBlockAllowed(Block block) {
    return trackedLocations.contains(block.getLocation());
  }

  static boolean isAbilityAllowed(Block block) {
    return trackedLocations.contains(block.getLocation());
  }

  static {
    trackedLocations = new HashSet<>();
  }
}
