package cerberooz.cerberooz.BlissUltimate;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisableAdminCommand implements CommandExecutor {
  final Bliss plugin;

  public DisableAdminCommand(Bliss bliss) {
    this.plugin = bliss;
  }

  public boolean onCommand(CommandSender commandSender, Command command, String argument, String[] message) {
    if (message.length == 0) {
      this.plugin.initialize();
      if (this.plugin.isGemsDisabled()) {
        commandSender.sendMessage(
            ChatColor.WHITE
                + "🔒 "
                + ChatColor.RED
                + "You have disabled all gems for everyone!");
      } else {
        commandSender.sendMessage(
            ChatColor.WHITE
                + "🔒 "
                + ChatColor.GREEN
                + "All gems are now enabled again for everyone!");
      }

      return true;
    } else {
      Player player = Bukkit.getPlayer(message[0]);
      OfflinePlayer offlinePlayer = null;
      Object targetPlayerId = null;
      String displayText = message[0];
      if (player != null) {
        targetPlayerId = player.getUniqueId();
      } else {
        offlinePlayer = Bukkit.getOfflinePlayer(message[0]);
        if (!offlinePlayer.hasPlayedBefore()) {
          commandSender.sendMessage(
              ChatColor.RED + "Player " + message[0] + " has never played on this server!");
          return true;
        }

        targetPlayerId = offlinePlayer.getUniqueId();
        displayText = offlinePlayer.getName() != null ? offlinePlayer.getName() : message[0];
      }

      long timestamp = this.getAbilityLongValue(message[1]);
      if (timestamp <= 0L) {
        commandSender.sendMessage(ChatColor.RED + "Invalid time format! Use e.g. 10s, 5m, 2h");
        return true;
      }

      commandSender.sendMessage(ChatColor.GREEN + "Gem disabled for " + displayText + " for " + message[1] + "!");
      if (player != null) {
        player.sendMessage(ChatColor.RED + "Your gem have been disabled for " + message[1] + "!");
      }

      return true;
    }
  }

  long getAbilityLongValue(String text) {
    try {
      long timestamp = Long.parseLong(text.substring(0, text.length() - 1));
      char value = text.charAt(text.length() - 1);
      switch (value) {
        case 'h':
          return timestamp * 3600L * 20L;
        case 'm':
          return timestamp * 60L * 20L;
        case 's':
          return timestamp * 20L;
        default:
          return -1L;
      }
    } catch (Exception exception) {
      return -1L;
    }
  }
}
