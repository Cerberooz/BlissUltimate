package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GemsMenu implements Listener {
  static String secondaryDisplayText;
  static String currentDisplayText;
  static String activeDisplayText;
  static String displayText;

  public Inventory resolveInventory() {
    Inventory inventory = Bukkit.createInventory(null, 54, activeDisplayText);
    ItemStack item = new ItemStack(Material.BARRIER);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setDisplayName(ChatColor.RED + "ᴄʟᴏѕᴇ");
    item.setItemMeta(itemMeta);
    ItemStack heldItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    ItemMeta meta = heldItem.getItemMeta();
    meta.setHideTooltip(true);
    heldItem.setItemMeta(meta);

    for (int count : new int[] {0, 1, 2, 3, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53}) {
      inventory.setItem(count, heldItem);
    }

    inventory.setItem(4, item);
    inventory.setItem(11, BlissItems.createUpgraderMenuItem());
    inventory.setItem(15, BlissItems.createTraderMenuItem());
    inventory.setItem(29, BlissItems.createRewardItem());
    inventory.setItem(42, BlissItems.createUpgradeItem());
    inventory.setItem(38, BlissItems.createRecipeItem());
    inventory.setItem(9, previewPuff(BlissItems.createPuffTierOneGemStage6()));
    inventory.setItem(10, BlissItems.createLifeTierOneGemStage6());
    inventory.setItem(18, BlissItems.createAstraTierOneGemStage6());
    inventory.setItem(19, BlissItems.createWealthTierOneGemStage6());
    inventory.setItem(27, previewStrength(BlissItems.createStrengthTierOneGemStage6()));
    inventory.setItem(28, BlissItems.createSpeedTierOneGemStage6());
    inventory.setItem(36, previewFire(BlissItems.createFireTierOneGemStage6()));
    inventory.setItem(37, BlissItems.createFluxTierOneGemStage6());
    inventory.setItem(16, previewPuff(BlissItems.createPuffTierTwoGemStage6()));
    inventory.setItem(17, BlissItems.createLifeTierTwoGemStage6());
    inventory.setItem(25, BlissItems.createAstraTierTwoGemStage6());
    inventory.setItem(26, BlissItems.createWealthTierTwoGemStage11());
    inventory.setItem(34, previewStrength(BlissItems.createStrengthTierTwoGemStage6()));
    inventory.setItem(35, BlissItems.createSpeedTierTwoGemStage6());
    inventory.setItem(43, previewFire(BlissItems.createFireTierTwoGemStage6()));
    inventory.setItem(44, BlissItems.createFluxTierTwoGemStage6());
    inventory.setItem(49, createGemItem());
    return inventory;
  }

  private ItemStack previewStrength(ItemStack item) {
    return Bliss.getInstance().strengthGemRevamp.createMenuPreview(item);
  }

  private ItemStack previewFire(ItemStack item) {
    return Bliss.getInstance().fireGemRevamp.createMenuPreview(item);
  }

  private ItemStack previewPuff(ItemStack item) {
    return Bliss.getInstance().puffGemRevamp.createMenuPreview(item);
  }

  void activateAbility(Player player) {
    Inventory inventory = Bukkit.createInventory(null, 45, displayText);
    ItemStack item = new ItemStack(Material.BARRIER);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setDisplayName(ChatColor.RED + "ᴄʟᴏѕᴇ");
    item.setItemMeta(itemMeta);
    ItemStack heldItem = new ItemStack(Material.ARROW);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(
        ChatColor.GREEN
            + "ʀᴇᴛᴜʀɴ ᴛᴏ ᴍᴀɪɴ"
            + " ᴘᴀɢᴇ");
    heldItem.setItemMeta(meta);
    ItemStack targetItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    ItemMeta updatedMeta = targetItem.getItemMeta();
    updatedMeta.setHideTooltip(true);
    targetItem.setItemMeta(updatedMeta);
    inventory.setItem(0, heldItem);
    inventory.setItem(8, item);
    inventory.setItem(9, HereticGem.createGemItem());
    inventory.setItem(10, createAbilityItem());
    inventory.setItem(11, BlissItems.createEnchantedObsidianItem());
    inventory.setItem(12, BlissItems.createEnergyToken());
    inventory.setItem(34, BlissItems.createAbilityItem());
    inventory.setItem(35, BlissItems.createDisplayItem());

    for (int count = 1; count < 8; count++) {
      inventory.setItem(count, targetItem);
    }

    for (int index = 36; index < 45; index++) {
      inventory.setItem(index, targetItem);
    }

    player.openInventory(inventory);
  }

  public static ItemStack createGemItem() {
    ItemStack item = new ItemStack(Material.REPEATING_COMMAND_BLOCK);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setDisplayName(
        net.md_5.bungee.api.ChatColor.of("#FFD773")
            + "ѕᴘᴇᴄɪᴀʟ ɪᴛᴇᴍѕ");
    ArrayList<String> lore = new ArrayList<>();
    lore.add(ChatColor.GRAY + "Click to open special items gui");
    itemMeta.setLore(lore);
    itemMeta.setCustomModelData(101);
    itemMeta.addEnchant(Enchantment.MENDING, 1, true);
    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createAbilityItem() {
    ItemStack item = new ItemStack(Material.REPEATING_COMMAND_BLOCK);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setDisplayName(
        net.md_5.bungee.api.ChatColor.of("#FAF9C6")
            + net.md_5.bungee.api.ChatColor.BOLD.toString()
            + "ᴀᴜʀᴀᴛᴜѕ "
            + net.md_5.bungee.api.ChatColor.of("#FFD773")
            + "ɢᴇᴍ");
    ArrayList<String> lore = new ArrayList<>();
    lore.add(
        net.md_5.bungee.api.ChatColor.WHITE
            + net.md_5.bungee.api.ChatColor.BOLD.toString()
            + "ᴍᴀʏ ᴛʜᴇ"
            + " ʜᴀʀᴛʏʀᴇᴅ ʙᴇ ɴᴏᴛ"
            + " ᴀꜰʀᴀɪᴅ");
    lore.add(
        net.md_5.bungee.api.ChatColor.WHITE
            + "("
            + net.md_5.bungee.api.ChatColor.of("#8B65DD")
            + "Mythic"
            + net.md_5.bungee.api.ChatColor.WHITE
            + ")");
    lore.add(" ");
    lore.add(
        net.md_5.bungee.api.ChatColor.of("#FAF9C6")
            + "🔮 "
            + net.md_5.bungee.api.ChatColor.of("#FFD773")
            + "ᴘᴀѕѕɪᴠᴇѕ");
    lore.add(net.md_5.bungee.api.ChatColor.GRAY + "- Divine Purity");
    lore.add(net.md_5.bungee.api.ChatColor.GRAY + "- Angles Grasp");
    lore.add(net.md_5.bungee.api.ChatColor.GRAY + "- Feathered Fall");
    lore.add(net.md_5.bungee.api.ChatColor.GRAY + "- Hauling Strike");
    lore.add(" ");
    lore.add(
        net.md_5.bungee.api.ChatColor.of("#FAF9C6")
            + "🔮 "
            + net.md_5.bungee.api.ChatColor.of("#B8FFFB")
            + "ᴘᴏᴡᴇʀѕ");
    lore.add(
        net.md_5.bungee.api.ChatColor.GRAY
            + "- §f⊳"
            + net.md_5.bungee.api.ChatColor.of("#C5AE30")
            + " ᴠᴇɴᴇʀᴀᴛᴇᴅ"
            + " ᴘᴇʀꜰᴏʀᴀᴛᴏʀѕ");
    lore.add(" ");
    lore.add(
        net.md_5.bungee.api.ChatColor.GRAY
            + "- §f⊲"
            + net.md_5.bungee.api.ChatColor.of("#DFE251")
            + " ᴇᴄʜᴏɪɴɢ ᴀᴇɢɪѕ");
    itemMeta.setLore(lore);
    itemMeta.setCustomModelData(101);
    itemMeta.addEnchant(Enchantment.MENDING, 1, true);
    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(itemMeta);
    return item;
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
      if (event.getView().getTitle().equals(activeDisplayText)) {
        if (event.getCurrentItem() != null) {
          event.setCancelled(true);
          player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0F, 1.5F);
          switch (event.getRawSlot()) {
            case 4:
              player.closeInventory();
            case 5:
            case 6:
            case 7:
            case 8:
            case 12:
            case 13:
            case 14:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 30:
            case 32:
            case 33:
            case 39:
            case 40:
            case 41:
            case 45:
            case 46:
            case 47:
            case 48:
            default:
              break;
            case 9:
              player.getInventory()
                  .addItem(BlissItems.createPuffTierOneGemStage6());
              break;
            case 10:
              player.getInventory().addItem(BlissItems.createLifeTierOneGemStage6());
              break;
            case 11:
              player.getInventory()
                  .addItem(BlissItems.createUpgraderMenuItem());
              break;
            case 15:
              player.getInventory()
                  .addItem(BlissItems.createTraderMenuItem());
              break;
            case 16:
              player.getInventory().addItem(BlissItems.createPuffTierTwoGemStage6());
              break;
            case 17:
              player.getInventory().addItem(BlissItems.createLifeTierTwoGemStage6());
              break;
            case 18:
              player.getInventory().addItem(BlissItems.createAstraTierOneGemStage6());
              break;
            case 19:
              player.getInventory().addItem(BlissItems.createWealthTierOneGemStage6());
              break;
            case 25:
              player.getInventory().addItem(BlissItems.createAstraTierTwoGemStage6());
              break;
            case 26:
              player.getInventory().addItem(BlissItems.createWealthTierTwoGemStage11());
              break;
            case 27:
              player.getInventory().addItem(BlissItems.createStrengthTierOneGemStage6());
              break;
            case 28:
              player.getInventory().addItem(BlissItems.createSpeedTierOneGemStage6());
              break;
            case 29:
              player.getInventory()
                  .addItem(BlissItems.createRewardItem());
              break;
            case 31:
              if (Bukkit.getPluginManager().isPluginEnabled("AuratusGem")) {
                player.getInventory()
                    .addItem(createAbilityItem());
              }
              break;
            case 34:
              player.getInventory().addItem(BlissItems.createStrengthTierTwoGemStage6());
              break;
            case 35:
              player.getInventory().addItem(BlissItems.createSpeedTierTwoGemStage6());
              break;
            case 36:
              player.getInventory().addItem(BlissItems.createFireTierOneGemStage6());
              break;
            case 37:
              player.getInventory().addItem(BlissItems.createFluxTierOneGemStage6());
              break;
            case 38:
              player.getInventory()
                  .addItem(BlissItems.createRecipeItem());
              break;
            case 42:
              player.getInventory()
                  .addItem(BlissItems.createUpgradeItem());
              break;
            case 43:
              player.getInventory().addItem(BlissItems.createFireTierTwoGemStage6());
              break;
            case 44:
              player.getInventory().addItem(BlissItems.createFluxTierTwoGemStage6());
              break;
            case 49:
              this.activateAbility(player);
          }
        }
      }
    }
  }

  @EventHandler
  public void refreshAbilityState(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
      if (event.getView().getTitle().equals(displayText)) {
        if (event.getCurrentItem() != null) {
          event.setCancelled(true);
          player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0F, 1.5F);
          switch (event.getRawSlot()) {
            case 0:
              player.openInventory(this.resolveInventory());
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 13:
            case 14:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            default:
              break;
            case 8:
              player.closeInventory();
              break;
            case 9:
              player.getInventory()
                  .addItem(HereticGem.createGemItem());
              break;
            case 10:
              player.getInventory()
                  .addItem(createAbilityItem());
              break;
            case 11:
              player.getInventory()
                  .addItem(BlissItems.createEnchantedObsidianItem());
              break;
            case 12:
              player.getInventory()
                  .addItem(BlissItems.createEnergyToken());
              break;
            case 34:
              player.getInventory()
                  .addItem(BlissItems.createAbilityItem());
              break;
            case 35:
              player.getInventory()
                  .addItem(BlissItems.createDisplayItem());
          }
        }
      }
    }
  }

  static {
    secondaryDisplayText = "           ";
    currentDisplayText = "                 ";
    activeDisplayText = currentDisplayText + ChatColor.GOLD + "ɢᴇᴍѕ";
    displayText =
        secondaryDisplayText
            + "§x§9§A§8§5§C§9ѕ§x§9§A§8§9§C§8ᴘ§x§9§B§8§E§C§7ᴇ§x§9§B§9§2§C§6ᴄ§x§9§B§9§6§C§4ɪ§x§9§C§9§A§C§3ᴀ§x§9§C§9§F§C§2ʟ"
            + " §x§9§D§A§7§C§0ɪ§x§9§D§A§B§B§Fᴛ§x§9§D§B§0§B§Dᴇ§x§9§E§B§4§B§Cᴍ§x§9§E§B§8§B§Bѕ";
  }
}
