package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class RandomTabCompleter implements TabCompleter {
  @Nullable
  public List<String> onTabComplete(CommandSender commandSender, Command command, String text, String[] message) {
    ArrayList<String> entries = new ArrayList<>();
    if (message.length == 1) {
      for (Player player : Bukkit.getOnlinePlayers()) {
        entries.add(player.getName());
      }
    }

    return entries;
  }
}
