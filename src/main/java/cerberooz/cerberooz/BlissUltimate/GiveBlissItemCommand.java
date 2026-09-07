package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GiveBlissItemCommand implements CommandExecutor, TabCompleter {
  final GemEnergyManager gemEnergyManager;

  public GiveBlissItemCommand(GemEnergyManager gemEnergyManager) {
    this.gemEnergyManager = gemEnergyManager;
  }

  public boolean onCommand(CommandSender commandSender, Command command, String argument, String[] message) {
    if (message.length < 3) {
      commandSender.sendMessage(ChatColor.RED + "Usage: /giveblissitem <gem|item> <player> <type> [amount]");
      return true;
    }

    String normalizedText = message[0].toLowerCase();
    Player player = Bukkit.getPlayer(message[1]);
    if (player == null) {
      commandSender.sendMessage(ChatColor.RED + "Player '" + message[1] + "' is not online!");
      return true;
    }

    String displayText = message[2].toUpperCase();
    int count = 1;
    if (message.length >= 4) {
      try {
        count = Integer.parseInt(message[3]);
        if (count <= 0) {
          commandSender.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
          return true;
        }
      } catch (NumberFormatException numberFormatException) {
        commandSender.sendMessage(ChatColor.RED + "Invalid amount: " + message[3]);
        return true;
      }
    }

    if (!normalizedText.equals("gem")) {
      if (normalizedText.equals("item")) {
        if (!this.isConditionMet(displayText)) {
          commandSender.sendMessage(ChatColor.RED + "Invalid item type: " + message[2]);
          commandSender.sendMessage(
              ChatColor.GRAY
                  + "Available item types: "
                  + String.join(", ", this.getState()));
          return true;
        } else {
          this.sendAbilityFeedback(player, displayText, count);
          String green = ChatColor.GREEN.toString();
          String name = player.getName();
          commandSender.sendMessage(green + "You gave " + name + " " + count + " item: " + displayText);
          green = ChatColor.WHITE.toString();
          name = ChatColor.GREEN.toString();
          player.sendMessage(green + "You received " + name + count + " " + displayText);
          return true;
        }
      } else {
        commandSender.sendMessage(ChatColor.RED + "First argument must be 'gem' or 'item'.");
        return true;
      }
    } else {
      if (!this.gemEnergyManager.isConditionMet(displayText)) {
        commandSender.sendMessage(ChatColor.RED + "Invalid gem type: " + message[2]);
        commandSender.sendMessage(
            ChatColor.GRAY
                + "Available gem types: "
                + String.join(", ", this.gemEnergyManager.getState()));
        return true;
      }

      for (int step = 0; step < count; step++) {
        this.gemEnergyManager.finishAbilityAction(player, displayText);
      }

      int ticks = GemEnergyManager.getAbilityIntValue(player);
      String statusText = this.formatDisplayText(ticks);
      String loreColor = ChatColor.GREEN.toString();
      String prefix = player.getName();
      String suffixColor = ChatColor.GREEN.toString();
      commandSender.sendMessage(
          loreColor + "You gave " + prefix + " " + displayText + " gem (" + statusText + suffixColor + ")");
      loreColor = ChatColor.of("#E4BC74").toString();
      prefix = ChatColor.WHITE.toString();
      String description = this.formatDisplayTextForPlayer(displayText);
      String entryText;
      String feedbackText = entryText = ChatColor.WHITE.toString();
      player.sendMessage(
          loreColor
              + "🔮 "
              + prefix
              + "You received "
              + description
              + feedbackText
              + " gem ("
              + statusText
              + entryText
              + ")");
      return true;
    }
  }

  public List<String> getState() {
    return List.of("REPAIR_ITEM", "RESTORATION_ITEM", "ENERGY", "TRADER", "UPGRADER");
  }

  public boolean isConditionMet(String text) {
    return this.getState().contains(text.toUpperCase());
  }

  public void sendAbilityFeedback(Player player, String text, int count) {
    text = text.toUpperCase();
    ItemStack item;
    switch (text) {
      case "REPAIR_ITEM":
        item = BlissItems.createRecipeItem();
        break;
      case "RESTORATION_ITEM":
        item = BlissItems.createUpgradeItem();
        break;
      case "ENERGY":
        item = BlissItems.createRewardItem();
        break;
      case "TRADER":
        item = BlissItems.createTraderMenuItem();
        break;
      case "UPGRADER":
        item = BlissItems.createUpgraderMenuItem();
        break;
      default:
        player.sendMessage("§cUnknown item type.");
        return;
    }

    item.setAmount(count);
    player.getInventory().addItem(item);
  }

  @Nullable
  public List<String> onTabComplete(CommandSender commandSender, Command command, String text, String[] message) {
    ArrayList<String> entries = new ArrayList<>();
    if (message.length == 1) {
      String normalizedText = message[0].toLowerCase();
      entries.add("gem");
      entries.add("item");
      return entries.stream().filter(value -> matchesMode(normalizedText, value)).collect(Collectors.toList());
    }

    if (message.length == 2) {
      String displayText = message[1].toLowerCase();
      return Bukkit.getOnlinePlayers().stream()
          .<String>map(Player::getName)
          .filter(value -> matchesPlayer(displayText, value))
          .collect(Collectors.toList());
    }

    if (message.length == 3) {
      String formattedText = message[0].toLowerCase();
      String label = message[2].toUpperCase();
      if (formattedText.equals("gem")) {
        return this.gemEnergyManager.getState().stream()
            .filter(value -> matchesGem(label, value))
            .collect(Collectors.toList());
      }

      if (formattedText.equals("item")) {
        return this.getState().stream()
            .filter(value -> matchesItem(label, value))
            .collect(Collectors.toList());
      }
    }

    return message.length == 4 ? List.of("1", "2", "4", "8", "16", "32", "64") : entries;
  }

  String formatDisplayText(int count) {
    return switch (count) {
      case 0 -> ChatColor.DARK_GRAY + "Broken";
      case 1 -> "§cRuined";
      case 2 -> ChatColor.of("#E2C35D") + "Damaged";
      case 3 -> ChatColor.of("#7963B3") + "Cracked";
      case 4 -> ChatColor.of("#82F5AE") + "Scratched";
      case 5 -> ChatColor.of("#82EDBF") + "Pristine";
      case 6 -> ChatColor.of("#82EDBF") + "Pristine +1";
      case 7 -> ChatColor.of("#82EDBF") + "Pristine +2";
      case 8 -> ChatColor.of("#82EDBF") + "Pristine +3";
      case 9 -> ChatColor.of("#82EDBF") + "Pristine +4";
      case 10 -> ChatColor.of("#82EDBF") + "Pristine +5";
      default -> "§7Unknown";
    };
  }

  String formatDisplayTextForPlayer(String text) {
    return switch (text.toUpperCase()) {
      case "PUFF_T1", "PUFF_T2" -> "§f§lᴘᴜꜰꜰ";
      case "LIFE_T1", "LIFE_T2" -> "§d§lʟɪғᴇ";
      case "ASTRA_T1", "ASTRA_T2" -> "§5§lᴀѕᴛʀᴀ";
      case "WEALTH_T1", "WEALTH_T2" -> "§2§lᴡᴇᴀʟᴛʜ";
      case "SPEED_T1", "SPEED_T2" -> "§e§lѕᴘᴇᴇᴅ";
      case "FIRE_T1", "FIRE_T2" -> "§6§lғɪʀᴇ";
      case "STRENGTH_T1", "STRENGTH_T2" ->
          "§4§lѕᴛʀᴇɴɢᴛʜ";
      case "FLUX_T1", "FLUX_T2" -> "§3§lғʟᴜx";
      default -> "§7Unknown";
    };
  }

  static boolean matchesItem(String text, String message) {
    return message.startsWith(text);
  }

  static boolean matchesGem(String text, String message) {
    return message.startsWith(text);
  }

  static boolean matchesPlayer(String text, String message) {
    return message.toLowerCase().startsWith(text);
  }

  static boolean matchesMode(String text, String message) {
    return message.startsWith(text);
  }
}
