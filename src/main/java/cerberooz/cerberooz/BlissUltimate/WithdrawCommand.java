package cerberooz.cerberooz.BlissUltimate;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WithdrawCommand implements CommandExecutor {
  Bliss plugin;

  public WithdrawCommand(Bliss bliss) {
    this.plugin = bliss;
  }

  GemEnergyManager resolveGemEnergyManager() {
    return this.plugin.getEnergyManager();
  }

  public static boolean isMatchingItem(ItemStack item) {
    if (item == null) {
      return false;
    }

    if (item.getType() != Material.NAUTILUS_SHELL) {
      return false;
    }

    if (!item.hasItemMeta()) {
      return false;
    }

    ItemMeta itemMeta = item.getItemMeta();
    return itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == 300;
  }

  public boolean onCommand(CommandSender commandSender, Command command, String argument, String[] message) {
    if (!(commandSender instanceof Player player)) {
      commandSender.sendMessage(ChatColor.RED + "This command can only be used by players!");
      return true;
    } else {
      if (player.getInventory().firstEmpty() == -1
          && player.getInventory().getItemInOffHand().getType() != Material.AIR) {
        player.sendMessage(ChatColor.RED + "Clear your inventory before using withdraw command!");
        return true;
      }

      GemUpgraderManager gemUpgraderManager = new GemUpgraderManager();
      if (gemUpgraderManager.isActiveForPlayer(player.getInventory().getItemInOffHand())) {
        player.sendMessage(
            ChatColor.RED + "Please don´t use the withdraw command with gem in offhand.");
        return true;
      }

      Bliss.getInstance().getEnergyManager();
      if (GemEnergyManager.getAbilityIntValue(player) == 0) {
        player.sendMessage(ChatColor.RED + "Your gem is broken, you can't withdraw energy!");
        return true;
      }

      int count = 0;

      for (ItemStack item : player.getInventory().getStorageContents()) {
        if (item == null || item.getType() == Material.AIR) {
          count++;
        } else if (isMatchingItem(item)) {
          count++;
        } else if (gemUpgraderManager.isActiveForPlayer(item)) {
          count++;
        }

        if (count >= 2) {
          break;
        }
      }

      if (count < 2) {
        player.sendMessage(ChatColor.RED + "You need at least two free slots for this command!");
        return true;
      }

      if (message.length != 1) {
        player.sendMessage(ChatColor.RED + "Usage: /withdraw <amount>");
        return true;
      }

      int index;
      try {
        index = Integer.parseInt(message[0]);
      } catch (NumberFormatException numberFormatException) {
        player.sendMessage(ChatColor.RED + "Invalid amount! Please enter a valid number.");
        return true;
      }

      if (index < 1) {
        player.sendMessage(ChatColor.RED + "Amount must be at least 1!");
        return true;
      } else {
        Bliss.getInstance().getEnergyManager();
        int remaining = GemEnergyManager.getAbilityIntValue(player);
        if (remaining - index < 1) {
          player.sendMessage(
              ChatColor.RED
                  + "You must keep at least 1 energy! You can withdraw maximum "
                  + (remaining - 1)
                  + " energy.");
          return true;
        } else {
          Bliss.getInstance().getEnergyManager();
          GemEnergyManager.canUseAbility(player, index);
          ItemStack heldItem = BlissItems.createRewardItem();
          heldItem.setAmount(index);
          player.getInventory().addItem(heldItem);
          Bliss.getInstance().getEnergyManager().activateAbility(player);
          String accentColor = net.md_5.bungee.api.ChatColor.of("#E4BC74").toString();
          String gold = ChatColor.GOLD.toString();
          String textColor = ChatColor.WHITE.toString();
          String bold = ChatColor.BOLD.toString();
          String displayColor = net.md_5.bungee.api.ChatColor.of("#9DFFDF").toString();
          player.sendMessage(
              String.format(
                  accentColor
                      + "🔮 "
                      + gold
                      + "Bottled "
                      + textColor
                      + bold
                      + index
                      + displayColor
                      + " ᴇɴᴇʀɢʏ "));
          return true;
        }
      }
    }
  }
}
