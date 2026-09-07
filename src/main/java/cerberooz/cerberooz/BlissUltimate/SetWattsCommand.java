package cerberooz.cerberooz.BlissUltimate;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWattsCommand implements CommandExecutor {
  final FluxTier2Gem fluxTier2Gem;

  public SetWattsCommand(FluxTier2Gem fluxTier2Gem) {
    this.fluxTier2Gem = fluxTier2Gem;
  }

  public boolean onCommand(CommandSender commandSender, Command command, String argument, String[] message) {
    if (message.length == 0) {
      commandSender.sendMessage(ChatColor.RED + "Usage: /setwatts <player> <percentage>");
      return true;
    }

    double value;
    Player player;
    try {
      if (message.length == 1) {
        if (!(commandSender instanceof Player senderPlayer)) {
          commandSender.sendMessage(ChatColor.RED + "Invalid usage!");
          return true;
        }

        player = senderPlayer;
        value = Double.parseDouble(message[0]);
      } else {
        player = Bukkit.getPlayer(message[0]);
        value = Double.parseDouble(message[1]);
      }
    } catch (NumberFormatException numberFormatException) {
      commandSender.sendMessage(ChatColor.RED + "Invalid percentage.");
      return true;
    }

    if (player == null) {
      commandSender.sendMessage(ChatColor.RED + "Player not found.");
      return true;
    }

    if (value < 0.0) {
      value = 0.0;
    }

    if (value > 200.0) {
      value = 200.0;
    }

    this.fluxTier2Gem.refreshAbilityState(player, value);
    if (player.equals(commandSender)) {
      commandSender.sendMessage(ChatColor.GREEN + "You set your watts energy to " + value + "%");
    } else {
      String green = ChatColor.GREEN.toString();
      String name = player.getName();
      commandSender.sendMessage(green + "Set " + name + "'s watt energy to " + value + "%");
      player.sendMessage(ChatColor.GRAY + "Your watt energy was set to " + value + "%");
    }

    return true;
  }
}
