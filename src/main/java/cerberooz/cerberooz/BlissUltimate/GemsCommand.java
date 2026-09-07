package cerberooz.cerberooz.BlissUltimate;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GemsCommand implements CommandExecutor {
  GemsMenu gemsMenu;
  static final String ONLY_PLAYERS_CAN_EXECUTE_THIS_COMMAND_ID;

  public GemsCommand(GemsMenu gemsMenu) {
    this.gemsMenu = gemsMenu;
  }

  public boolean onCommand(CommandSender commandSender, Command command, String argument, String[] message) {
    if (commandSender instanceof Player player) {
      player.openInventory(this.gemsMenu.resolveInventory());
    } else {
      commandSender.sendMessage(ChatColor.RED + ONLY_PLAYERS_CAN_EXECUTE_THIS_COMMAND_ID);
    }

    return false;
  }

  static {
    ONLY_PLAYERS_CAN_EXECUTE_THIS_COMMAND_ID = "Only players can execute this command.";
  }
}
