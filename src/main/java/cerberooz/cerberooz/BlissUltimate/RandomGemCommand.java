package cerberooz.cerberooz.BlissUltimate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class RandomGemCommand implements CommandExecutor, Listener {
  final Bliss plugin;
  final Set<UUID> playerIds = new HashSet<>();
  final List<ItemStack> entries =
      Arrays.asList(
          BlissItems.createPuffTierOneGemStage6(),
          BlissItems.createLifeTierOneGemStage6(),
          BlissItems.createAstraTierOneGemStage6(),
          BlissItems.createWealthTierOneGemStage6(),
          BlissItems.createSpeedTierOneGemStage6(),
          BlissItems.createFireTierOneGemStage6(),
          BlissItems.createFluxTierOneGemStage6(),
          BlissItems.createStrengthTierOneGemStage6());

  public RandomGemCommand(Bliss bliss) {
    this.plugin = bliss;
  }

  public boolean onCommand(CommandSender commandSender, Command command, String argument, String[] message) {
    if (!(commandSender instanceof Player player)) {
      if (message.length < 1) {
        commandSender.sendMessage(ChatColor.RED + "Usage: /" + argument + " <Player|all>");
        return true;
      }

      if (!message[0].equalsIgnoreCase("all")) {
        Player targetPlayer = Bukkit.getPlayer(message[0]);
        if (targetPlayer == null) {
          commandSender.sendMessage(ChatColor.RED + "Player " + message[0] + " isn´t online.");
          return true;
        } else {
          this.activateAbility(targetPlayer);
          commandSender.sendMessage(ChatColor.GREEN + "Started rolling gems for " + targetPlayer.getName() + ".");
          return true;
        }
      } else {
        int count = 0;

        for (Player sourcePlayer : Bukkit.getOnlinePlayers()) {
          this.activateAbility(sourcePlayer);
          count++;
        }

        commandSender.sendMessage(ChatColor.GREEN + "Started rolling gems for " + count + " online players.");
        return true;
      }
    } else {
      if (message.length < 1) {
        this.activateAbility(player);
        player.sendMessage(ChatColor.GREEN + "Started rolling gems for yourself.");
        return true;
      }

      if (!message[0].equalsIgnoreCase("all")) {
        Player otherPlayer = Bukkit.getPlayer(message[0]);
        if (otherPlayer == null) {
          player.sendMessage(ChatColor.RED + "Player " + message[0] + " isn´t online.");
          return true;
        } else {
          this.activateAbility(otherPlayer);
          player.sendMessage(ChatColor.GREEN + "Started rolling gems for " + otherPlayer.getName() + ".");
          return true;
        }
      } else {
        int index = 0;

        for (Player owner : Bukkit.getOnlinePlayers()) {
          this.activateAbility(owner);
          index++;
        }

        player.sendMessage(ChatColor.GREEN + "Started rolling gems for " + index + " online players.");
        return true;
      }
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
      if (this.playerIds.contains(player.getUniqueId())) {
        event.setCancelled(true);
      }
    }
  }

  void activateAbility(Player player) {
    int[] count = new int[] {18, 16, 14, 12, 10, 8, 6, 5, 5, 4, 4, 3, 3, 2, 1};
    int index = 0;
    this.playerIds.add(player.getUniqueId());

    for (int remaining : count) {
      index += remaining;
      Bukkit.getScheduler()
          .runTaskLater(this.plugin, () -> this.updateGemRoll(player, remaining), index);
    }
  }

  void refreshAbilityState(Player player) {
    PlayerInventory playerInventory = player.getInventory();
    int count = 0;

    for (int index = 0; index < playerInventory.getSize(); index++) {
      ItemStack item = playerInventory.getItem(index);
      if (this.isGemItem(item)) {
        playerInventory.setItem(index, null);
        count++;
      }
    }

    if (this.isGemItem(playerInventory.getItemInOffHand())) {
      playerInventory.setItemInOffHand(null);
      count++;
    }

    if (this.isGemItem(playerInventory.getItemInMainHand())) {
      playerInventory.setItemInMainHand(null);
      count++;
    }

    ItemStack[] heldItem = playerInventory.getArmorContents();

    for (int remaining = 0; remaining < heldItem.length; remaining++) {
      if (this.isGemItem(heldItem[remaining])) {
        heldItem[remaining] = null;
        count++;
      }
    }

    playerInventory.setArmorContents(heldItem);
  }

  void updateAbilityState(Player player) {
    ItemStack item =
        this.entries.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(this.entries.size()));
    player.getInventory().addItem(item);
  }

  boolean isGemItem(ItemStack item) {
    if (item == null || item.getType() == Material.AIR) {
      return false;
    }

    if (!item.hasItemMeta()) {
      return false;
    }

    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta == null) {
      return false;
    }

    String displayName = itemMeta.getDisplayName();
    if (displayName != null) {
      displayName = ChatColor.stripColor(displayName).toLowerCase();
      if (displayName.contains("gem")
          || displayName.contains("puff")
          || displayName.contains("astra")
          || displayName.contains("fire")
          || displayName.contains("life")
          || displayName.contains("speed")
          || displayName.contains("wealth")
          || displayName.contains("strength")
          || displayName.contains("flux")) {
        return true;
      }
    }

    if (itemMeta.hasLore() && itemMeta.getLore() != null) {
      for (String text : itemMeta.getLore()) {
        String normalizedText = ChatColor.stripColor(text).toLowerCase();
        if (normalizedText.contains("gem") || normalizedText.contains("tier") || normalizedText.contains("energy")) {
          return true;
        }
      }
    }

    if (itemMeta.hasCustomModelData()) {
      int count = itemMeta.getCustomModelData();
      if (count >= 1 && count <= 190) {
        return true;
      }
    }

    return BlissItems.isMatchingItem(item, BlissItems.createPuffTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createLifeTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage6());
  }

  void updateGemRoll(Player player, int count) {
    this.refreshAbilityState(player);
    if (count == 1) {
      this.playerIds.remove(player.getUniqueId());
      this.updateAbilityState(player);
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
    } else {
      this.updateAbilityState(player);
    }
  }
}
