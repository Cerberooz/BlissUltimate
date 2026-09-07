package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Nullable;

public class PedestalTabCompleter implements TabCompleter {
  @Nullable
  public List<String> onTabComplete(CommandSender commandSender, Command command, String text, String[] message) {
    ArrayList<String> entries = new ArrayList<>();
    if (message.length == 1) {
      entries.add("set");
      entries.add("activate");
      entries.add("deactivate");
      entries.add("remove");
    }

    return entries;
  }
}
