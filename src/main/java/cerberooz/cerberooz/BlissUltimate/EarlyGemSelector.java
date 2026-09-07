package cerberooz.cerberooz.BlissUltimate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EarlyGemSelector implements Listener {
  Bliss plugin;

  public EarlyGemSelector(Bliss bliss) {
    this.plugin = bliss;
  }

  public static void activateAbility(Player player) {
    Inventory inventory =
        Bukkit.createInventory(
            null,
            9,
            ChatColor.LIGHT_PURPLE + "ᴇᴀʀʟʏ ɢᴇᴍѕ");
    inventory.setItem(0, BlissItems.createPuffTierOneGemStage6());
    inventory.setItem(1, BlissItems.createLifeTierOneGemStage6());
    inventory.setItem(2, BlissItems.createFireTierOneGemStage6());
    inventory.setItem(3, BlissItems.createAstraTierOneGemStage6());
    inventory.setItem(4, BlissItems.createStrengthTierOneGemStage6());
    inventory.setItem(5, BlissItems.createWealthTierOneGemStage6());
    inventory.setItem(6, BlissItems.createSpeedTierOneGemStage6());
    inventory.setItem(7, BlissItems.createFluxTierOneGemStage6());
    player.openInventory(inventory);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    if (mainHandItem != null
        && BlissItems.isMatchingItem(
            mainHandItem, BlissItems.createAbilityItem())) {
      activateAbility(player);
    } else if (mainHandItem != null
        && BlissItems.isMatchingItem(
            mainHandItem, BlissItems.createDisplayItem())) {
      AdvancedGemSelector.activateAbility(player);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    if (ChatColor.translateAlternateColorCodes('&', event.getView().getTitle())
            .equals(
                ChatColor.LIGHT_PURPLE + "ᴇᴀʀʟʏ ɢᴇᴍѕ")
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
          player.getInventory()
              .addItem(BlissItems.createPuffTierOneGemStage6());
          break;
        case 1:
          player.getInventory().addItem(BlissItems.createLifeTierOneGemStage6());
          break;
        case 2:
          player.getInventory().addItem(BlissItems.createFireTierOneGemStage6());
          break;
        case 3:
          player.getInventory().addItem(BlissItems.createAstraTierOneGemStage6());
          break;
        case 4:
          player.getInventory().addItem(BlissItems.createStrengthTierOneGemStage6());
          break;
        case 5:
          player.getInventory().addItem(BlissItems.createWealthTierOneGemStage6());
          break;
        case 6:
          player.getInventory().addItem(BlissItems.createSpeedTierOneGemStage6());
          break;
        case 7:
          player.getInventory().addItem(BlissItems.createFluxTierOneGemStage6());
          break;
        default:
          return;
      }

      player.closeInventory();
    }
  }
}
