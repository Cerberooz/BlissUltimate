package cerberooz.cerberooz.BlissUltimate;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SetEnergyCommand implements CommandExecutor, TabCompleter {
  final Bliss plugin;
  final GemEnergyManager gemEnergyManager;

  public SetEnergyCommand(Bliss bliss) {
    this.plugin = bliss;
    this.gemEnergyManager = bliss.getEnergyManager();
  }

  public boolean onCommand(CommandSender commandSender, Command command, String argument, String[] message) {
    if (message.length == 0) {
      commandSender.sendMessage(
          ChatColor.RED + "Usage: /" + argument + " <amount> or /" + argument + " <player> <amount>");
      return true;
    }

    if (message.length == 2) {
      Player player = Bukkit.getPlayer(message[0]);
      if (player == null) {
        commandSender.sendMessage(ChatColor.RED + "Player " + message[0] + " isn't online.");
        return true;
      }

      int count;
      try {
        count = Integer.parseInt(message[1]);
      } catch (NumberFormatException numberFormatException) {
        commandSender.sendMessage(ChatColor.RED + "Invalid amount! Please enter a number between 0 and 10.");
        return true;
      }

      if (count >= 0 && count <= 10) {
        this.activateAbility(player, count);
        commandSender.sendMessage(ChatColor.GREEN + "Set " + player.getName() + "'s energy to " + count);
        return true;
      } else {
        commandSender.sendMessage(ChatColor.RED + "Amount must be between 0 and 10!");
        return true;
      }
    } else if (commandSender instanceof Player targetPlayer) {
      int index;
      try {
        index = Integer.parseInt(message[0]);
      } catch (NumberFormatException currentNumberFormatException) {
        targetPlayer.sendMessage(ChatColor.RED + "Invalid amount! Please enter a number between 0 a 10.");
        return true;
      }

      if (index >= 0 && index <= 10) {
        this.activateAbility(targetPlayer, index);
        targetPlayer.sendMessage(ChatColor.GREEN + "Set your energy to " + index);
        return true;
      } else {
        targetPlayer.sendMessage(ChatColor.RED + "Amount must be between 0 a 10!");
        return true;
      }
    } else {
      commandSender.sendMessage(
          ChatColor.RED + "Console must specify a player: /" + argument + " <player> <amount>");
      return true;
    }
  }

  void activateAbility(Player player, int count) {
    this.gemEnergyManager.scheduleAbilityUpdate(player, count);
    this.gemEnergyManager.activateAbility(player);
  }

  @Nullable
  public List<String> onTabComplete(CommandSender commandSender, Command command, String text, String[] message) {
    return message.length == 1
        ? Bukkit.getOnlinePlayers().stream()
            .<String>map(Player::getName)
            .filter(value -> matchesPlayerArgument(message, value))
            .toList()
        : List.of();
  }

  static boolean matchesPlayerArgument(String[] text, String message) {
    return message.toLowerCase().startsWith(text[0].toLowerCase());
  }
}
