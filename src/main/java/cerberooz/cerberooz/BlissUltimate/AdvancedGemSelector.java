package cerberooz.cerberooz.BlissUltimate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AdvancedGemSelector implements Listener {
  Bliss plugin;

  public AdvancedGemSelector(Bliss bliss) {
    this.plugin = bliss;
  }

  public static void activateAbility(Player player) {
    Inventory inventory =
        Bukkit.createInventory(null, 9, ChatColor.LIGHT_PURPLE + "ɢᴇᴍѕ");
    inventory.setItem(0, BlissItems.createPuffTierTwoGemStage6());
    inventory.setItem(1, BlissItems.createLifeTierTwoGemStage6());
    inventory.setItem(2, BlissItems.createFireTierTwoGemStage6());
    inventory.setItem(3, BlissItems.createAstraTierTwoGemStage6());
    inventory.setItem(4, BlissItems.createStrengthTierTwoGemStage6());
    inventory.setItem(5, BlissItems.createWealthTierTwoGemStage11());
    inventory.setItem(6, BlissItems.createSpeedTierTwoGemStage6());
    inventory.setItem(7, BlissItems.createFluxTierTwoGemStage6());
    player.openInventory(inventory);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    if (ChatColor.translateAlternateColorCodes('&', event.getView().getTitle())
            .equals(ChatColor.LIGHT_PURPLE + "ɢᴇᴍѕ")
        && event.getCurrentItem() != null) {
      event.setCancelled(true);
      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      if (mainHandItem == null || mainHandItem.getType() != Material.NAUTILUS_SHELL) {
        return;
      }

      if (mainHandItem.getAmount() < 1) {
        return;
      }

      if (mainHandItem.getAmount() == 1) {
        player.getInventory().setItemInMainHand(null);
      } else {
        mainHandItem.setAmount(mainHandItem.getAmount() - 1);
      }

      GemEnergyManager gemEnergyManager = new GemEnergyManager(this.plugin);
      gemEnergyManager.cleanupAbilityState(player);
      switch (event.getRawSlot()) {
        case 0:
          player.getInventory().addItem(BlissItems.createPuffTierTwoGemStage6());
          break;
        case 1:
          player.getInventory().addItem(BlissItems.createLifeTierTwoGemStage6());
          break;
        case 2:
          player.getInventory().addItem(BlissItems.createFireTierTwoGemStage6());
          break;
        case 3:
          player.getInventory().addItem(BlissItems.createAstraTierTwoGemStage6());
          break;
        case 4:
          player.getInventory().addItem(BlissItems.createStrengthTierTwoGemStage6());
          break;
        case 5:
          player.getInventory().addItem(BlissItems.createWealthTierTwoGemStage11());
          break;
        case 6:
          player.getInventory().addItem(BlissItems.createSpeedTierTwoGemStage6());
          break;
        case 7:
          player.getInventory().addItem(BlissItems.createFluxTierTwoGemStage6());
          break;
        default:
          return;
      }

      player.closeInventory();
    }
  }
}
