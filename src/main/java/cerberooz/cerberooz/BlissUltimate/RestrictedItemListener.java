package cerberooz.cerberooz.BlissUltimate;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Allay;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RestrictedItemListener implements Listener {
  static final Set<ItemStack> restrictedItems;
  final WealthTier2Gem wealthTier2Gem;

  public RestrictedItemListener(WealthTier2Gem wealthTier2Gem) {
    this.wealthTier2Gem = wealthTier2Gem;
  }

  static void initialize() {
    updateState(HereticGem.createGemItem());
    updateState(BlissItems.createEnchantedObsidianItem());
    updateState(BlissItems.createLifeTierOneGemStage1());
    updateState(BlissItems.createLifeTierOneGemStage2());
    updateState(BlissItems.createLifeTierOneGemStage3());
    updateState(BlissItems.createLifeTierOneGemStage4());
    updateState(BlissItems.createLifeTierOneGemStage5());
    updateState(BlissItems.createLifeTierOneGemStage6());
    updateState(BlissItems.createLifeTierOneGemStage7());
    updateState(BlissItems.createLifeTierOneGemStage8());
    updateState(BlissItems.createLifeTierOneGemStage9());
    updateState(BlissItems.createLifeTierOneGemStage10());
    updateState(BlissItems.createLifeTierOneGemStage11());
    updateState(BlissItems.createPuffTierOneGemStage1());
    updateState(BlissItems.createPuffTierOneGemStage2());
    updateState(BlissItems.createPuffTierOneGemStage3());
    updateState(BlissItems.createPuffTierOneGemStage4());
    updateState(BlissItems.createPuffTierOneGemStage5());
    updateState(BlissItems.createPuffTierOneGemStage6());
    updateState(BlissItems.createPuffTierOneGemStage7());
    updateState(BlissItems.createPuffTierOneGemStage8());
    updateState(BlissItems.createPuffTierOneGemStage9());
    updateState(BlissItems.createPuffTierOneGemStage10());
    updateState(BlissItems.createPuffTierOneGemStage11());
    updateState(BlissItems.createAstraTierOneGemStage1());
    updateState(BlissItems.createAstraTierOneGemStage2());
    updateState(BlissItems.createAstraTierOneGemStage3());
    updateState(BlissItems.createAstraTierOneGemStage4());
    updateState(BlissItems.createAstraTierOneGemStage5());
    updateState(BlissItems.createAstraTierOneGemStage6());
    updateState(BlissItems.createAstraTierOneGemStage7());
    updateState(BlissItems.createAstraTierOneGemStage8());
    updateState(BlissItems.createAstraTierOneGemStage9());
    updateState(BlissItems.createAstraTierOneGemStage10());
    updateState(BlissItems.createAstraTierOneGemStage11());
    updateState(BlissItems.createFireTierOneGemStage1());
    updateState(BlissItems.createFireTierOneGemStage2());
    updateState(BlissItems.createFireTierOneGemStage3());
    updateState(BlissItems.createFireTierOneGemStage4());
    updateState(BlissItems.createFireTierOneGemStage5());
    updateState(BlissItems.createFireTierOneGemStage6());
    updateState(BlissItems.createFireTierOneGemStage7());
    updateState(BlissItems.createFireTierOneGemStage8());
    updateState(BlissItems.createFireTierOneGemStage9());
    updateState(BlissItems.createFireTierOneGemStage10());
    updateState(BlissItems.createFireTierOneGemStage11());
    updateState(BlissItems.createSpeedTierOneGemStage1());
    updateState(BlissItems.createSpeedTierOneGemStage2());
    updateState(BlissItems.createSpeedTierOneGemStage3());
    updateState(BlissItems.createSpeedTierOneGemStage4());
    updateState(BlissItems.createSpeedTierOneGemStage5());
    updateState(BlissItems.createSpeedTierOneGemStage6());
    updateState(BlissItems.createSpeedTierOneGemStage7());
    updateState(BlissItems.createSpeedTierOneGemStage8());
    updateState(BlissItems.createSpeedTierOneGemStage9());
    updateState(BlissItems.createSpeedTierOneGemStage10());
    updateState(BlissItems.createSpeedTierOneGemStage11());
    updateState(BlissItems.createStrengthTierOneGemStage1());
    updateState(BlissItems.createStrengthTierOneGemStage2());
    updateState(BlissItems.createStrengthTierOneGemStage3());
    updateState(BlissItems.createStrengthTierOneGemStage4());
    updateState(BlissItems.createStrengthTierOneGemStage5());
    updateState(BlissItems.createStrengthTierOneGemStage6());
    updateState(BlissItems.createStrengthTierOneGemStage7());
    updateState(BlissItems.createStrengthTierOneGemStage8());
    updateState(BlissItems.createStrengthTierOneGemStage9());
    updateState(BlissItems.createStrengthTierOneGemStage10());
    updateState(BlissItems.createStrengthTierOneGemStage11());
    updateState(BlissItems.createWealthTierOneGemStage1());
    updateState(BlissItems.createWealthTierOneGemStage2());
    updateState(BlissItems.createWealthTierOneGemStage3());
    updateState(BlissItems.createWealthTierOneGemStage4());
    updateState(BlissItems.createWealthTierOneGemStage5());
    updateState(BlissItems.createWealthTierOneGemStage6());
    updateState(BlissItems.createWealthTierOneGemStage7());
    updateState(BlissItems.createWealthTierOneGemStage8());
    updateState(BlissItems.createWealthTierOneGemStage9());
    updateState(BlissItems.createWealthTierOneGemStage10());
    updateState(BlissItems.createWealthTierOneGemStage11());
    updateState(BlissItems.createFluxTierOneGemStage1());
    updateState(BlissItems.createFluxTierOneGemStage2());
    updateState(BlissItems.createFluxTierOneGemStage3());
    updateState(BlissItems.createFluxTierOneGemStage4());
    updateState(BlissItems.createFluxTierOneGemStage5());
    updateState(BlissItems.createFluxTierOneGemStage6());
    updateState(BlissItems.createFluxTierOneGemStage7());
    updateState(BlissItems.createFluxTierOneGemStage8());
    updateState(BlissItems.createFluxTierOneGemStage9());
    updateState(BlissItems.createFluxTierOneGemStage10());
    updateState(BlissItems.createFluxTierOneGemStage11());
    updateState(BlissItems.createLifeTierTwoGemStage1());
    updateState(BlissItems.createLifeTierTwoGemStage2());
    updateState(BlissItems.createLifeTierTwoGemStage3());
    updateState(BlissItems.createLifeTierTwoGemStage4());
    updateState(BlissItems.createLifeTierTwoGemStage5());
    updateState(BlissItems.createLifeTierTwoGemStage6());
    updateState(BlissItems.createLifeTierTwoGemStage7());
    updateState(BlissItems.createLifeTierTwoGemStage8());
    updateState(BlissItems.createLifeTierTwoGemStage9());
    updateState(BlissItems.createLifeTierTwoGemStage10());
    updateState(BlissItems.createLifeTierTwoGemStage11());
    updateState(BlissItems.createPuffTierTwoGemStage1());
    updateState(BlissItems.createPuffTierTwoGemStage2());
    updateState(BlissItems.createPuffTierTwoGemStage3());
    updateState(BlissItems.createPuffTierTwoGemStage4());
    updateState(BlissItems.createPuffTierTwoGemStage5());
    updateState(BlissItems.createPuffTierTwoGemStage6());
    updateState(BlissItems.createPuffTierTwoGemStage7());
    updateState(BlissItems.createPuffTierTwoGemStage8());
    updateState(BlissItems.createPuffTierTwoGemStage9());
    updateState(BlissItems.createPuffTierTwoGemStage10());
    updateState(BlissItems.createPuffTierTwoGemStage11());
    updateState(BlissItems.createAstraTierTwoGemStage1());
    updateState(BlissItems.createAstraTierTwoGemStage2());
    updateState(BlissItems.createAstraTierTwoGemStage3());
    updateState(BlissItems.createAstraTierTwoGemStage4());
    updateState(BlissItems.createAstraTierTwoGemStage5());
    updateState(BlissItems.createAstraTierTwoGemStage6());
    updateState(BlissItems.createAstraTierTwoGemStage7());
    updateState(BlissItems.createAstraTierTwoGemStage8());
    updateState(BlissItems.createAstraTierTwoGemStage9());
    updateState(BlissItems.createAstraTierTwoGemStage10());
    updateState(BlissItems.createAstraTierTwoGemStage11());
    updateState(BlissItems.createFireTierTwoGemStage1());
    updateState(BlissItems.createFireTierTwoGemStage2());
    updateState(BlissItems.createFireTierTwoGemStage3());
    updateState(BlissItems.createFireTierTwoGemStage4());
    updateState(BlissItems.createFireTierTwoGemStage5());
    updateState(BlissItems.createFireTierTwoGemStage6());
    updateState(BlissItems.createFireTierTwoGemStage7());
    updateState(BlissItems.createFireTierTwoGemStage8());
    updateState(BlissItems.createFireTierTwoGemStage9());
    updateState(BlissItems.createFireTierTwoGemStage10());
    updateState(BlissItems.createFireTierTwoGemStage11());
    updateState(BlissItems.createSpeedTierTwoGemStage1());
    updateState(BlissItems.createSpeedTierTwoGemStage2());
    updateState(BlissItems.createSpeedTierTwoGemStage3());
    updateState(BlissItems.createSpeedTierTwoGemStage4());
    updateState(BlissItems.createSpeedTierTwoGemStage5());
    updateState(BlissItems.createSpeedTierTwoGemStage6());
    updateState(BlissItems.createSpeedTierTwoGemStage7());
    updateState(BlissItems.createSpeedTierTwoGemStage8());
    updateState(BlissItems.createSpeedTierTwoGemStage9());
    updateState(BlissItems.createSpeedTierTwoGemStage10());
    updateState(BlissItems.createSpeedTierTwoGemStage11());
    updateState(BlissItems.createStrengthTierTwoGemStage1());
    updateState(BlissItems.createStrengthTierTwoGemStage2());
    updateState(BlissItems.createStrengthTierTwoGemStage3());
    updateState(BlissItems.createStrengthTierTwoGemStage4());
    updateState(BlissItems.createStrengthTierTwoGemStage5());
    updateState(BlissItems.createStrengthTierTwoGemStage6());
    updateState(BlissItems.createStrengthTierTwoGemStage7());
    updateState(BlissItems.createStrengthTierTwoGemStage8());
    updateState(BlissItems.createStrengthTierTwoGemStage9());
    updateState(BlissItems.createStrengthTierTwoGemStage10());
    updateState(BlissItems.createStrengthTierTwoGemStage11());
    updateState(BlissItems.createWealthTierTwoGemStage1());
    updateState(BlissItems.createWealthTierTwoGemStage2());
    updateState(BlissItems.createWealthTierTwoGemStage3());
    updateState(BlissItems.createWealthTierTwoGemStage4());
    updateState(BlissItems.createWealthTierTwoGemStage5());
    updateState(BlissItems.createWealthTierTwoGemStage11());
    updateState(BlissItems.createWealthTierTwoGemStage6());
    updateState(BlissItems.createWealthTierTwoGemStage7());
    updateState(BlissItems.createWealthTierTwoGemStage8());
    updateState(BlissItems.createWealthTierTwoGemStage9());
    updateState(BlissItems.createWealthTierTwoGemStage10());
    updateState(BlissItems.createFluxTierTwoGemStage1());
    updateState(BlissItems.createFluxTierTwoGemStage2());
    updateState(BlissItems.createFluxTierTwoGemStage3());
    updateState(BlissItems.createFluxTierTwoGemStage4());
    updateState(BlissItems.createFluxTierTwoGemStage5());
    updateState(BlissItems.createFluxTierTwoGemStage6());
    updateState(BlissItems.createFluxTierTwoGemStage7());
    updateState(BlissItems.createFluxTierTwoGemStage8());
    updateState(BlissItems.createFluxTierTwoGemStage9());
    updateState(BlissItems.createFluxTierTwoGemStage10());
    updateState(BlissItems.createFluxTierTwoGemStage11());
  }

  static void updateState(ItemStack item) {
    if (item != null) {
      restrictedItems.add(item);
    }
  }

  boolean isMatchingItem(ItemStack item) {
    return this.isAbilityAllowed(item) || this.wealthTier2Gem != null && this.wealthTier2Gem.isActiveGemItem(item);
  }

  void onInventoryInteract(InventoryInteractEvent event, Player player, String text) {
    event.setCancelled(true);
    if (this.wealthTier2Gem != null) {
      this.wealthTier2Gem.sendAbilityFeedback(player, text);
    }
  }

  void activateAbility(Cancellable cancellable, Player player, String text) {
    cancellable.setCancelled(true);
    if (this.wealthTier2Gem != null) {
      this.wealthTier2Gem.sendAbilityFeedback(player, text);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    ItemStack item = event.getCurrentItem();
    if (item != null) {
      if (this.isMatchingItem(item) && false) {
        this.onInventoryInteract(event, player, "Amplification has been cancelled.");
      }
    }
  }

  @EventHandler
  public void refreshAbilityState(InventoryClickEvent event) {
    ItemStack item = event.getCurrentItem();
    if (item != null) {
      if (item.getItemMeta() != null
          && item.getItemMeta().getDisplayName().toUpperCase().contains("BUNDLE")
          && this.isMatchingItem(event.getCursor())) {
        if (event.getAction() == InventoryAction.PICKUP_ALL_INTO_BUNDLE
            || event.getAction() == InventoryAction.PICKUP_FROM_BUNDLE
            || event.getAction() == InventoryAction.PICKUP_SOME_INTO_BUNDLE
            || event.getAction() == InventoryAction.PLACE_FROM_BUNDLE
            || event.getAction() == InventoryAction.PLACE_ALL_INTO_BUNDLE
            || event.getAction() == InventoryAction.PLACE_SOME_INTO_BUNDLE) {
          this.onInventoryInteract(
              event, (Player) event.getWhoClicked(), "Amplification has been cancelled.");
          event.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  public void updateAbilityState(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    ItemStack item = event.getCursor();
    ItemStack heldItem = event.getCurrentItem();
    ItemStack targetItem = null;
    if (event.getClick().isKeyboardClick()) {
      int count = event.getHotbarButton();
      if (count >= 0 && count <= 8) {
        targetItem = player.getInventory().getItem(count);
      }
    }

    Inventory inventory = event.getView().getTopInventory();
    boolean enabled = inventory != null && inventory.equals(player.getEnderChest());
    if (event.getClickedInventory() != null && event.getClickedInventory() != player.getInventory()) {
      if (this.isMatchingItem(item)) {
        this.onInventoryInteract(event, player, "Amplification has been cancelled.");
        return;
      }

      if (this.isMatchingItem(targetItem)) {
        this.onInventoryInteract(event, player, "Amplification has been cancelled.");
        return;
      }
    }

    if (enabled) {
      if (this.isMatchingItem(item)) {
        this.onInventoryInteract(event, player, "Amplification has been cancelled.");
        return;
      }

      if (this.isMatchingItem(targetItem)) {
        this.onInventoryInteract(event, player, "Amplification has been cancelled.");
        return;
      }
    }

    if (event.getClick() == ClickType.SWAP_OFFHAND
        && inventory != null
        && (inventory.getHolder() != player || enabled)) {
      ItemStack offHandItem = player.getInventory().getItemInOffHand();
      if (this.isMatchingItem(offHandItem)) {
        this.onInventoryInteract(event, player, "Amplification has been cancelled.");
        return;
      }
    }

    if (event.isShiftClick()
        && heldItem != null
        && this.isMatchingItem(heldItem)
        && event.getClickedInventory() == player.getInventory()
        && (event.getInventory() != player.getInventory() || enabled)) {
      this.onInventoryInteract(event, player, "Amplification has been cancelled.");
    }
  }

  @EventHandler
  public void onInventoryDrag(InventoryDragEvent event) {
    Player player = (Player) event.getWhoClicked();
    ItemStack item = event.getOldCursor();
    if (this.isMatchingItem(item)) {
      for (int count : event.getRawSlots()) {
        Inventory inventory = event.getView().getInventory(count);
        if (inventory != null && inventory != player.getInventory()) {
          this.onInventoryInteract(event, player, "Amplification has been cancelled.");
          return;
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (event.getRightClicked() instanceof Allay) {
      Player player = event.getPlayer();
      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      ItemStack offHandItem = player.getInventory().getItemInOffHand();
      if (this.isMatchingItem(mainHandItem) || this.isMatchingItem(offHandItem)) {
        this.activateAbility(event, player, "Amplification has been cancelled.");
      }
    }
  }

  @EventHandler
  public void applyAbilityEffects(PlayerInteractEntityEvent event) {
    if (event.getRightClicked() instanceof ItemFrame) {
      Player player = event.getPlayer();
      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      ItemStack offHandItem = player.getInventory().getItemInOffHand();
      if (this.isMatchingItem(mainHandItem)) {
        this.activateAbility(event, player, "Amplification has been cancelled.");
      } else if (this.isMatchingItem(offHandItem)) {
          ItemStack heldItem = player.getInventory().getItemInMainHand();
          if (heldItem == null || heldItem.getType() == Material.AIR) {
            this.activateAbility(event, player, "Amplification has been cancelled.");
          }
        }
      
    }
  }

  @EventHandler
  public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
    ArmorStand armorStand = event.getRightClicked();
    Player player = event.getPlayer();
    ItemStack item = event.getPlayerItem();
    ItemStack heldItem = event.getArmorStandItem();
    if (this.isMatchingItem(item) || this.isMatchingItem(heldItem)) {
      this.activateAbility(event, player, "Amplification has been cancelled.");
    }
  }

  @EventHandler
  public void onInventoryMoveItem(InventoryMoveItemEvent event) {
    ItemStack item = event.getItem();
    if (item != null
        && this.isMatchingItem(item)
        && event.getSource().getHolder() instanceof Player player) {
      event.setCancelled(true);
      if (this.wealthTier2Gem != null) {
        this.wealthTier2Gem.sendAbilityFeedback(player, "Amplification has been cancelled.");
      }
    }
  }

  boolean isConditionMet(Material material) {
    String text = material.name();
    return text.endsWith("SHELF") || text.endsWith("POWERED_SHELF");
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (event.getClickedBlock() != null) {
        if (event.getClickedBlock().getType() == Material.DECORATED_POT
            || this.isConditionMet(event.getClickedBlock().getType())) {
          Player player = event.getPlayer();
          ItemStack mainHandItem = player.getInventory().getItemInMainHand();

          for (int count = 0; count < 9; count++) {
            ItemStack item = player.getInventory().getItem(count);
            if (item != null && item.getType() != Material.AIR && this.isMatchingItem(item)) {
              event.setCancelled(true);
              this.activateAbility(event, player, "Amplification has been cancelled.");
            }
          }

          if (this.isMatchingItem(mainHandItem)) {
            this.activateAbility(event, player, "Amplification has been cancelled.");
          }
        }
      }
    }
  }

  boolean isAbilityAllowed(ItemStack item) {
    if (item == null) {
      return false;
    }

    for (ItemStack heldItem : restrictedItems) {
      if (BlissItems.isMatchingItem(item, heldItem)) {
        return true;
      }
    }

    return false;
  }

  static {
    restrictedItems = new HashSet<>();
    initialize();
  }
}
