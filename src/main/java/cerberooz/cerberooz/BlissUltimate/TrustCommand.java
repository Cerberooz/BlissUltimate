package cerberooz.cerberooz.BlissUltimate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class TrustCommand implements CommandExecutor, TabCompleter, Listener {
  final Plugin plugin;
  final File file;
  FileConfiguration fileConfiguration;
  final Map<UUID, Set<UUID>> playerRelations;
  static String displayText;
  final Map<UUID, Integer> playerCounters = new HashMap<>();
  final Map<UUID, Long> lastUseTimes = new HashMap<>();

  public TrustCommand(Plugin plugin) {
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), "trusts.yml");
    this.playerRelations = new HashMap<>();
    if (!plugin.getDataFolder().exists()) {
      plugin.getDataFolder().mkdirs();
    }

    this.playAbilityEffects();
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (event.getEntity() instanceof Player targetPlayer) {
        if (this.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId())) {
          if (ConfigValueCache.getBoolean(Bliss.getInstance(), "AutoUntrust", true)) {
            int configuredValue = ConfigValueCache.getInt(Bliss.getInstance(), "HitsForUntrust", 5);
            UUID playerId = targetPlayer.getUniqueId();
            long now = System.currentTimeMillis();
            if (this.lastUseTimes.containsKey(playerId)) {
              long timestamp = this.lastUseTimes.get(playerId);
              if (now - timestamp > 10000L) {
                this.playerCounters.remove(playerId);
              }
            }

            this.lastUseTimes.put(playerId, now);
            int count = this.playerCounters.getOrDefault(playerId, 0) + 1;
            this.playerCounters.put(playerId, count);
            if (count >= configuredValue) {
              this.playerCounters.remove(playerId);
              this.lastUseTimes.remove(playerId);
              this.applyAbilityEffects(player.getUniqueId(), playerId);
              String accentColor = ChatColor.of("#E4BC74").toString();
              String messageColor = ChatColor.of("#ff8585").toString();
              String text = InvisibleKillListener.formatDisplayText(targetPlayer);
              String displayColor = ChatColor.of("#ff8585").toString();
              player.sendMessage(
                  String.format(
                      accentColor
                          + "🔮 "
                          + messageColor
                          + "Auto untrusted "
                          + text
                          + displayColor
                          + "."));
            }
          }
        }
      }
    }
  }

  public boolean onCommand(CommandSender commandSender, Command command, String argument, String[] message) {
    if (commandSender instanceof Player player) {
      if (message.length == 0) {
        return true;
      }

      switch (message[0].toLowerCase()) {
        case "add":
          if (message.length < 2) {
            player.sendMessage(
                ChatColor.of("#E4BC74")
                    + "🔮 "
                    + ChatColor.of("#ff8585")
                    + "Usage: /trust add <player>");
            return true;
          }

          String displayText = message[1];
          Player targetPlayer = Bukkit.getPlayer(displayText);
          if (targetPlayer == null) {
            player.sendMessage("§cPlayer §e" + displayText + "§c is not online!");
            return true;
          }

          if (targetPlayer.equals(player)) {
            player.sendMessage(
                ChatColor.of("#E4BC74") + "🔮 §cYou cannot trust yourself!");
            return true;
          }

          if (this.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId())) {
            String accentColor = ChatColor.of("#E4BC74").toString();
            String textColor = ChatColor.of("#befff7").toString();
            String name = targetPlayer.getName();
            String labelColor = ChatColor.of("#ff8585").toString();
            player.sendMessage(
                accentColor + "🔮 " + textColor + name + labelColor + " is already trusted!");
            return true;
          }

          this.updateAbilityState(player.getUniqueId(), targetPlayer.getUniqueId());
          String statusColor = ChatColor.of("#E4BC74").toString();
          String green = ChatColor.GREEN.toString();
          String loreColor = ChatColor.of("#befff7").toString();
          String prefix = targetPlayer.getName();
          player.sendMessage(statusColor + "🔮 " + green + "Trusted " + loreColor + prefix);
          break;
        case "remove":
          if (message.length < 2) {
            player.sendMessage(
                ChatColor.of("#E4BC74")
                    + "🔮 "
                    + ChatColor.of("#ff8585")
                    + "Usage: /trust remove <player>");
            return true;
          }

          String rawText = message[1];
          Player sourcePlayer = Bukkit.getPlayer(rawText);
          if (sourcePlayer == null) {
            String titleColor = ChatColor.of("#E4BC74").toString();
            String subtitleColor = ChatColor.of("#befff7").toString();
            String descriptionColor = ChatColor.of("#ff8585").toString();
            player.sendMessage(titleColor + "🔮 " + subtitleColor + rawText + descriptionColor + " is not online!");
            return true;
          }

          if (!this.isAbilityActive(player.getUniqueId(), sourcePlayer.getUniqueId())) {
            String prefixColor = ChatColor.of("#E4BC74").toString();
            String playerColor = ChatColor.of("#befff7").toString();
            String errorColor = ChatColor.of("#ff8585").toString();
            player.sendMessage(
                prefixColor
                    + "🔮 "
                    + playerColor
                    + sourcePlayer.getName()
                    + errorColor
                    + " is not trusted!");
            return true;
          }

          this.applyAbilityEffects(player.getUniqueId(), sourcePlayer.getUniqueId());
          String prefixColor = ChatColor.of("#E4BC74").toString();
          String actionColor = ChatColor.of("#ff8585").toString();
          String playerColor = ChatColor.of("#befff7").toString();
          player.sendMessage(
              prefixColor
                  + "🔮 "
                  + actionColor
                  + "Untrusted "
                  + playerColor
                  + sourcePlayer.getName());
          break;
        case "list":
          this.activateAbility(player);
          return true;
        case "clear":
          Iterator<UUID> iterator = this.getStateForPlayer(player.getUniqueId()).iterator();
          if (iterator.hasNext()) {
            UUID targetId = iterator.next();
            if (targetId == null) {
              player.sendMessage(
                  ChatColor.of("#E4BC74")
                      + "🔮 "
                      + ChatColor.of("#ff8585")
                      + "Trust list cleared.");
              return true;
            }

            this.applyAbilityEffects(player.getUniqueId(), targetId);
            player.sendMessage(
                ChatColor.of("#E4BC74")
                    + "🔮 "
                    + ChatColor.of("#ff8585")
                    + "Trust list cleared.");
            return true;
          }
      }

      return true;
    } else {
      commandSender.sendMessage("§cOnly players can use this command!");
      return true;
    }
  }

  void activateAbility(Player player) {
    Inventory inventory = Bukkit.createInventory(null, 45, displayText);

    for (UUID playerId : this.getStateForPlayer(player.getUniqueId())) {
      if (playerId != null && !playerId.equals(player.getUniqueId())) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        if (itemMeta != null) {
          if (Bukkit.getPlayer(playerId) == null) {
            player.openInventory(inventory);
            return;
          }

          itemMeta.setOwningPlayer(Bukkit.getPlayer(playerId));
          itemMeta.setDisplayName(ChatColor.WHITE + Bukkit.getPlayer(playerId).getName());
          ArrayList<String> entries = new ArrayList<>();
          entries.add(
              ChatColor.of("#E4BC74")
                  + "ᴄʟɪᴄᴋ ᴛᴏ"
                  + " ᴜɴᴛʀᴜѕᴛ");
          itemMeta.setLore(entries);
          item.setItemMeta(itemMeta);
        }

        inventory.addItem(item);
      }
    }

    player.openInventory(inventory);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    if (event.getView().getTitle().equals(displayText)) {
      event.setCancelled(true);
      ItemStack item = event.getCurrentItem();
      if (item.getType() != Material.AIR) {
        if (item.getType() == Material.PLAYER_HEAD) {
          SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
          Player targetPlayer = Bukkit.getPlayer(itemMeta.getOwner());
          if (targetPlayer != null) {
            this.applyAbilityEffects(player.getUniqueId(), targetPlayer.getUniqueId());
            String accentColor = ChatColor.of("#E4BC74").toString();
            String messageColor = ChatColor.of("#ff8585").toString();
            String displayColor = ChatColor.of("#befff7").toString();
            String name = targetPlayer.getName();
            player.sendMessage(accentColor + "🔮 " + messageColor + "Untrusted " + displayColor + name);
            this.refreshAbilityState(player, event.getClickedInventory());
            player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0F, 1.5F);
          }
        }
      }
    }
  }

  void refreshAbilityState(Player player, Inventory inventory) {
    inventory.clear();

    for (UUID playerId : this.getStateForPlayer(player.getUniqueId())) {
      if (playerId != null && !playerId.equals(player.getUniqueId())) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        if (itemMeta != null) {
          itemMeta.setOwningPlayer(Bukkit.getPlayer(playerId));
          itemMeta.setDisplayName(ChatColor.RED + Bukkit.getPlayer(playerId).getName());
          ArrayList<String> entries = new ArrayList<>();
          entries.add(
              ChatColor.of("#E4BC74")
                  + "ᴄʟɪᴄᴋ ᴛᴏ"
                  + " ᴜɴᴛʀᴜѕᴛ");
          itemMeta.setLore(entries);
          item.setItemMeta(itemMeta);
        }

        inventory.addItem(item);
      }
    }
  }

  @EventHandler
  public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
    Player player = event.getPlayer();
    if (event.getHand().equals(EquipmentSlot.HAND)) {
      if (event.getRightClicked() instanceof Player targetPlayer) {
        if (player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING) {
          if (!this.isAbilityActive(player.getUniqueId(), targetPlayer.getUniqueId())) {
            this.updateAbilityState(player.getUniqueId(), targetPlayer.getUniqueId());
            String accentColor = ChatColor.of("#E4BC74").toString();
            String green = ChatColor.GREEN.toString();
            String messageColor = ChatColor.of("#befff7").toString();
            String name = targetPlayer.getName();
            player.sendMessage(accentColor + "🔮 " + green + "Trusted " + messageColor + name);
          }
        }
      }
    }
  }

  @Nullable
  public List<String> onTabComplete(CommandSender commandSender, Command command, String text, String[] message) {
    if (!(commandSender instanceof Player)) {
      return List.of();
    } else if (message.length == 1) {
      return List.of("add", "remove", "list", "clear").stream()
          .filter(value -> matchesFirstArgument(message, value))
          .collect(Collectors.toList());
    } else {
      return message.length != 2
              || !message[0].equalsIgnoreCase("add") && !message[0].equalsIgnoreCase("remove")
          ? List.of()
          : Bukkit.getOnlinePlayers().stream()
              .<String>map(Player::getName)
              .filter(value -> matchesSecondArgument(message, value))
              .collect(Collectors.toList());
    }
  }

  public void updateAbilityState(UUID playerId, UUID targetId) {
    this.playerRelations.computeIfAbsent(playerId, TrustCommand::createTrackingSet).add(targetId);
    this.initialize();
  }

  public void applyAbilityEffects(UUID playerId, UUID targetId) {
    Set<UUID> trustedPlayerIds = this.playerRelations.get(playerId);
    if (trustedPlayerIds != null) {
      trustedPlayerIds.remove(targetId);
      if (trustedPlayerIds.isEmpty()) {
        this.playerRelations.remove(playerId);
      }

      this.initialize();
    }
  }

  public boolean isAbilityActive(UUID playerId, UUID targetId) {
    Set<UUID> trustedPlayerIds = this.playerRelations.get(playerId);
    return trustedPlayerIds != null && trustedPlayerIds.contains(targetId);
  }

  public List<String> getState(UUID playerId) {
    Set<UUID> trustedPlayerIds = this.playerRelations.get(playerId);
    return trustedPlayerIds != null && !trustedPlayerIds.isEmpty()
        ? trustedPlayerIds.stream()
            .map(TrustCommand::resolvePlayerName)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        : new ArrayList<>();
  }

  public Set<UUID> getStateForPlayer(UUID playerId) {
    Set<UUID> trustedPlayerIds = this.playerRelations.get(playerId);
    return trustedPlayerIds != null ? new HashSet<>(trustedPlayerIds) : new HashSet<>();
  }

  void initialize() {
    this.fileConfiguration = new YamlConfiguration();

    for (Entry<UUID, Set<UUID>> entry : this.playerRelations.entrySet()) {
      String text = entry.getKey().toString();
      List<String> entries = entry.getValue().stream().map(UUID::toString).collect(Collectors.toList());
      this.fileConfiguration.set(text, entries);
    }

    try {
      this.fileConfiguration.save(this.file);
    } catch (IOException exception) {
      this.plugin.getLogger().severe("Error saving trusts.yml: " + exception.getMessage());
    }
  }

  void playAbilityEffects() {
    if (!this.file.exists()) {
      try {
        this.file.createNewFile();
      } catch (IOException exception) {
        this.plugin.getLogger().severe("Error creating trusts.yml: " + exception.getMessage());
        return;
      }
    }

    this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);

    for (String text : this.fileConfiguration.getKeys(false)) {
      try {
        UUID playerId = UUID.fromString(text);
        List<String> entries = this.fileConfiguration.getStringList(text);
        Set<UUID> trustedPlayerIds =
            entries.stream()
                .map(TrustCommand::parsePlayerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!trustedPlayerIds.isEmpty()) {
          this.playerRelations.put(playerId, trustedPlayerIds);
        }
      } catch (IllegalArgumentException illegalArgumentException) {
        this.plugin.getLogger().warning("Invalid UUID in trusts.yml: " + text);
      }
    }

    this.plugin.getLogger().info("Loaded " + this.playerRelations.size() + " trust lists");
  }

  public void spawnAbilityEffects() {
    this.initialize();
  }

  @EventHandler
  public void cleanupAbilityState(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      if (event.getEntity() instanceof Player targetPlayer) {
        if (ConfigValueCache.getBoolean(this.plugin, "DamageTrustedPlayer")) {
          UUID playerId = player.getUniqueId();
          UUID targetId = targetPlayer.getUniqueId();
          if (this.isAbilityActive(playerId, targetId)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't damage trusted players.");
          }
        }
      }
    }
  }

  static UUID parsePlayerId(String text) {
    try {
      return UUID.fromString(text);
    } catch (IllegalArgumentException illegalArgumentException) {
      return null;
    }
  }

  static String resolvePlayerName(UUID playerId) {
    return Bukkit.getOfflinePlayer(playerId).getName();
  }

  static Set<UUID> createTrackingSet(UUID playerId) {
    return new HashSet<>();
  }

  static boolean matchesSecondArgument(String[] text, String message) {
    return message.toLowerCase().startsWith(text[1].toLowerCase());
  }

  static boolean matchesFirstArgument(String[] text, String message) {
    return message.startsWith(text[0].toLowerCase());
  }

  static {
    displayText =
        ChatColor.of("#E4BC74")
            + "          ᴛʀᴜѕᴛᴇᴅ"
            + " ᴘʟᴀʏᴇʀѕ";
  }
}
