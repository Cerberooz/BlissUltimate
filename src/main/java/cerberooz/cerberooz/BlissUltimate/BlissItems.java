package cerberooz.cerberooz.BlissUltimate;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class BlissItems implements Listener {
  public static boolean isMatchingItem(ItemStack item, ItemStack heldItem) {
    if (item != null && heldItem != null) {
      if (item.getType() != heldItem.getType()) {
        return false;
      } else {
        ItemMeta itemMeta = item.getItemMeta();
        ItemMeta meta = heldItem.getItemMeta();
        if (itemMeta == null && meta == null) {
          return true;
        } else if (itemMeta != null && meta != null) {
          boolean enabled =
              itemMeta.hasCustomModelData() == meta.hasCustomModelData()
                  && (!itemMeta.hasCustomModelData()
                      || itemMeta.getCustomModelData() == meta.getCustomModelData());
          String text = formatDisplayText(item);
          String message = formatDisplayText(heldItem);
          boolean active = Objects.equals(text, message);
          return enabled && active;
        } else {
          return false;
        }
      }
    } else {
      return false;
    }
  }

  static String formatDisplayText(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      String displayName = item.getItemMeta().getDisplayName();
      String text = ChatColor.stripColor(displayName);
      return text.replace(" ɢᴇᴍ", "").replace(" GEM", "").trim();
    } else {
      return "";
    }
  }

  static boolean isConditionMet(ItemMeta itemMeta, ItemMeta meta) {
    if (itemMeta == null && meta == null) {
      return true;
    } else if (itemMeta != null && meta != null) {
      ItemMeta updatedMeta = itemMeta.clone();
      ItemMeta displayMeta = meta.clone();
      updatedMeta.setLore(null);
      displayMeta.setLore(null);
      updatedMeta.getEnchants().keySet().forEach(updatedMeta::removeEnchant);
      displayMeta.getEnchants().keySet().forEach(displayMeta::removeEnchant);
      updatedMeta.setEnchantmentGlintOverride(null);
      displayMeta.setEnchantmentGlintOverride(null);
      updatedMeta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      displayMeta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      return updatedMeta.equals(displayMeta);
    } else {
      return false;
    }
  }

  public static ItemStack createItem() {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setMaxStackSize(1);
    itemMeta.setCustomModelData(230);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createAbilityItem() {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setMaxStackSize(1);
    itemMeta.setDisplayName(
        ChatColor.LIGHT_PURPLE
            + "ᴇᴀʀʟʏɢᴇᴍѕ"
            + " ѕᴇʟᴇᴄᴛᴏʀ");
    itemMeta.setCustomModelData(200);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createDisplayItem() {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setMaxStackSize(1);
    itemMeta.setDisplayName(
        ChatColor.LIGHT_PURPLE
            + "ɢᴇᴍѕ ѕᴇʟᴇᴄᴛᴏʀ");
    itemMeta.setCustomModelData(200);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createUpgradeItem() {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setMaxStackSize(1);
    itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Restoration " + ChatColor.WHITE + "Item");
    itemMeta.setCustomModelData(210);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createRecipeItem() {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setMaxStackSize(1);
    itemMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Repair " + ChatColor.WHITE + "Item");
    itemMeta.setCustomModelData(200);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createRewardItem() {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setCustomModelData(300);
    itemMeta.setDisplayName(
        ChatColor.of("#96FFD9")
            + org.bukkit.ChatColor.BOLD.toString()
            + "ᴇɴᴇʀɢʏ "
            + ChatColor.of("#F6D7B3")
            + "ɪɴ ᴀ ʙᴏᴛᴛʟᴇ");
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createEnergyToken() {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setCustomModelData(302);
    itemMeta.setDisplayName(
        ChatColor.GOLD
            + org.bukkit.ChatColor.BOLD.toString()
            + "ᴇɴᴇʀɢʏ "
            + ChatColor.GOLD
            + "ᴛᴏᴋᴇɴ");
    ArrayList<String> lore = new ArrayList<>();
    lore.add(ChatColor.DARK_GRAY + "[Left Click] energy bottle to obtain");
    lore.add(ChatColor.GRAY + "Can be traded with special villagers.");
    itemMeta.setLore(lore);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createTraderMenuItem() {
    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
    itemMeta.setDisplayName(
        ChatColor.BOLD.toString() + ChatColor.of("#f7b2f7") + "ᴛʀᴀᴅᴇʀ");
    UUID playerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    itemMeta.setCustomModelData(1);
    ArrayList<String> lore = new ArrayList<>();
    lore.add(
        ChatColor.GRAY
            + "ʟᴇғᴛ-ᴄʟɪᴄᴋ ғᴏʀ"
            + " ᴜʟᴛɪᴍᴀᴛᴇ"
            + " ᴛʀᴀᴅɪɴɢ ᴍᴇɴᴜ");
    lore.add(
        ChatColor.GRAY
            + "ʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ"
            + " ʀᴇᴄᴇɪᴠᴇ ᴀ"
            + " ʀᴀɴᴅᴏᴍ ɢᴇᴍ");
    lore.add("");
    lore.add(
        ChatColor.RED
            + "ᴡᴀʀɴɪɴɢ: ʏᴏᴜ"
            + " ᴡɪʟʟ ʟᴏѕᴇ ʏᴏᴜʀ"
            + " ᴄᴜʀʀᴇɴᴛ ɢᴇᴍ");
    itemMeta.setLore(lore);
    itemMeta.setEnchantmentGlintOverride(false);
    itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
    itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);

    try {
      PlayerProfile playerProfile = Bukkit.createPlayerProfile(playerId, "Trader");
      PlayerTextures playerTextures = playerProfile.getTextures();
      playerTextures.setSkin(
          new URL(
              "http://textures.minecraft.net/texture/bf624a44e77b95dbe1cc735536953e840ba6c19b0157c549d9b82534648c8ce4"));
      playerProfile.setTextures(playerTextures);
      itemMeta.setOwnerProfile(playerProfile);
    } catch (Exception exception) {
      Bliss.getInstance()
          .getLogger()
          .log(
              java.util.logging.Level.WARNING,
              "Could not apply the custom player-head texture",
              exception);
    }

    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createUpgraderMenuItem() {
    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
    itemMeta.setDisplayName(
        ChatColor.BOLD.toString() + ChatColor.of("#85ffa5")
            + "ᴜᴘɢʀᴀᴅᴇʀ");
    UUID playerId = UUID.fromString("124e4567-e89b-12d3-a456-426614174000");
    itemMeta.setCustomModelData(1);
    ArrayList<String> lore = new ArrayList<>();
    lore.add(
        ChatColor.WHITE
            + "ʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ"
            + " ᴜᴘɢʀᴀᴅᴇ ʏᴏᴜʀ"
            + " ɢᴇᴍ");
    lore.add("");
    lore.add(
        ChatColor.RED
            + "ᴡᴀʀɴɪɴɢ:"
            + " ᴜᴘɢʀᴀᴅᴇʀ ᴡɪʟʟ"
            + " ᴅʀᴏᴘ ᴏɴ ᴅᴇᴀᴛʜ");
    itemMeta.setLore(lore);
    itemMeta.setEnchantmentGlintOverride(false);
    itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
    itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);

    try {
      PlayerProfile playerProfile = Bukkit.createPlayerProfile(playerId, "Upgrader");
      PlayerTextures playerTextures = playerProfile.getTextures();
      playerTextures.setSkin(
          new URL(
              "http://textures.minecraft.net/texture/d992e4a8f43c77ac2e2d792d9604f61f34387c877aba3712e4047b740c578"));
      playerProfile.setTextures(playerTextures);
      itemMeta.setOwnerProfile(playerProfile);
    } catch (Exception exception) {
      Bliss.getInstance()
          .getLogger()
          .log(
              java.util.logging.Level.WARNING,
              "Could not apply the custom player-head texture",
              exception);
    }

    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createEnchantedObsidianItem() {
    ItemStack item = new ItemStack(Material.OBSIDIAN, 1);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setMaxStackSize(1);
    itemMeta.setDisplayName(
        ChatColor.LIGHT_PURPLE
            + "ᴇɴᴄʜᴀɴᴛᴇᴅ"
            + " ᴏʙꜱɪᴅɪᴀɴ");
    itemMeta.addEnchant(Enchantment.MENDING, 1, true);
    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack createPuffTierOneGemStage1() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(95);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierOneGemStage2() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(85);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("§f(§r§cRuined§r§f)");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Sculk Silence");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierOneGemStage3() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(65);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Sculk Silence");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierOneGemStage4() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(45);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Sculk Silence");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierOneGemStage5() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(25);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Sculk Silence");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierOneGemStage6() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(5);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Sculk Silence");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierOneGemStage7() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(5);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Sculk Silence");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierOneGemStage8() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(5);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Sculk Silence");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierOneGemStage9() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(5);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Sculk Silence");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierOneGemStage10() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(5);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Sculk Silence");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierOneGemStage11() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(5);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      lore.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      lore.add("");
      lore.add(
          ChatColor.WHITE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Fall Damage Immunity");
      lore.add(ChatColor.GRAY + "- Sculk Silence");
      lore.add(ChatColor.GRAY + "- Enchants Power");
      lore.add(ChatColor.GRAY + "- Enchants Punch");
      lore.add(ChatColor.GRAY + "- Enchants Feather Falling");
      lore.add(ChatColor.GRAY + "- Crop Tramp-Less");
      lore.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      lore.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage1() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(95);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage2() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(83);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      lore.add("§f(§r§cRuined§r§f)");
      lore.add("");
      lore.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Green Thumb");
      lore.add(ChatColor.GRAY + "- Radiant Fist");
      lore.add(ChatColor.GRAY + "- Bonus Saturation");
      lore.add(ChatColor.GRAY + "- Bonus Absorption");
      lore.add(ChatColor.GRAY + "- Enchants Unbreaking");
      lore.add(ChatColor.GRAY + "- Wither Immune");
      lore.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      lore.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage3() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(63);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      lore.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      lore.add("");
      lore.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Green Thumb");
      lore.add(ChatColor.GRAY + "- Radiant Fist");
      lore.add(ChatColor.GRAY + "- Bonus Saturation");
      lore.add(ChatColor.GRAY + "- Bonus Absorption");
      lore.add(ChatColor.GRAY + "- Enchants Unbreaking");
      lore.add(ChatColor.GRAY + "- Wither Immune");
      lore.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      lore.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage4() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(43);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      lore.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      lore.add("");
      lore.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Green Thumb");
      lore.add(ChatColor.GRAY + "- Radiant Fist");
      lore.add(ChatColor.GRAY + "- Bonus Saturation");
      lore.add(ChatColor.GRAY + "- Bonus Absorption");
      lore.add(ChatColor.GRAY + "- Enchants Unbreaking");
      lore.add(ChatColor.GRAY + "- Wither Immune");
      lore.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      lore.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage5() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(23);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> lore = new ArrayList<>();
      lore.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      lore.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      lore.add("");
      lore.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      lore.add(ChatColor.GRAY + "- Green Thumb");
      lore.add(ChatColor.GRAY + "- Radiant Fist");
      lore.add(ChatColor.GRAY + "- Bonus Saturation");
      lore.add(ChatColor.GRAY + "- Bonus Absorption");
      lore.add(ChatColor.GRAY + "- Enchants Unbreaking");
      lore.add(ChatColor.GRAY + "- Wither Immune");
      lore.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      lore.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      lore.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      lore.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      lore.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      lore.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(lore);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage6() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(3);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage7() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(3);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage8() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(3);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage9() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(3);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage10() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(3);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierOneGemStage11() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(3);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage1() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(95);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage2() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(93);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage3() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(73);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage4() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(53);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage5() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(33);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage6() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(13);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage7() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(13);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage8() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(13);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage9() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(13);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage10() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(13);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierOneGemStage11() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(13);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage1() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(95);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage2() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(91);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune");
      entries.add(ChatColor.GRAY + "- Enchants Looting");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage3() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(71);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune");
      entries.add(ChatColor.GRAY + "- Enchants Looting");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage4() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(51);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune");
      entries.add(ChatColor.GRAY + "- Enchants Looting");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage5() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(31);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune");
      entries.add(ChatColor.GRAY + "- Enchants Looting");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage6() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(11);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune");
      entries.add(ChatColor.GRAY + "- Enchants Looting");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage7() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(11);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune");
      entries.add(ChatColor.GRAY + "- Enchants Looting");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage8() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(11);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune");
      entries.add(ChatColor.GRAY + "- Enchants Looting");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage9() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(11);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune");
      entries.add(ChatColor.GRAY + "- Enchants Looting");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage10() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(11);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune");
      entries.add(ChatColor.GRAY + "- Enchants Looting");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierOneGemStage11() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(11);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune");
      entries.add(ChatColor.GRAY + "- Enchants Looting");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage1() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(95);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage2() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(89);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage3() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(69);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage4() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(49);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage5() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(29);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage6() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(9);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage7() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(9);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage8() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(9);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage9() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(9);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage10() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(9);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierOneGemStage11() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(9);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage1() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(95);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.of("#befff7") + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage2() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(87);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage3() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(67);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage4() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(47);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage5() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(27);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage6() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(7);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage7() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(7);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage8() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(7);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage9() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(7);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage10() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(7);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierOneGemStage11() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(7);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage1() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(95);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage2() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(81);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GOLD + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage3() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(61);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GOLD + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage4() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(41);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GOLD + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage5() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(21);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GOLD + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage6() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(1);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GOLD + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage7() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(1);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GOLD + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage8() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(1);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GOLD + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage9() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(1);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GOLD + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage10() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(1);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GOLD + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierOneGemStage11() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(1);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔺 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GOLD + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔺 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage1() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(95);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage2() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(177);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage3() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(157);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage4() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(137);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage5() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(117);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage6() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(97);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage7() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setMaxStackSize(1);
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(97);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage8() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setMaxStackSize(1);
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(97);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage9() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setMaxStackSize(1);
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(97);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage10() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setMaxStackSize(1);
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(97);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierOneGemStage11() {
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setMaxStackSize(1);
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(97);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔺 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔺 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔺 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(ChatColor.WHITE.toString() + ChatColor.BOLD + "Unknown");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage1() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(96);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage2() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(86);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fall Damage Immunity");
      entries.add(ChatColor.GRAY + "- Sculk Silence");
      entries.add(ChatColor.GRAY + "- Enchants Power");
      entries.add(ChatColor.GRAY + "- Enchants Punch");
      entries.add(ChatColor.GRAY + "- Enchants Feather Falling");
      entries.add(ChatColor.GRAY + "- Crop Tramp-Less");
      entries.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      entries.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage3() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(66);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fall Damage Immunity");
      entries.add(ChatColor.GRAY + "- Sculk Silence");
      entries.add(ChatColor.GRAY + "- Enchants Power");
      entries.add(ChatColor.GRAY + "- Enchants Punch");
      entries.add(ChatColor.GRAY + "- Enchants Feather Falling");
      entries.add(ChatColor.GRAY + "- Crop Tramp-Less");
      entries.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      entries.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage4() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(46);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fall Damage Immunity");
      entries.add(ChatColor.GRAY + "- Sculk Silence");
      entries.add(ChatColor.GRAY + "- Enchants Power");
      entries.add(ChatColor.GRAY + "- Enchants Punch");
      entries.add(ChatColor.GRAY + "- Enchants Feather Falling");
      entries.add(ChatColor.GRAY + "- Crop Tramp-Less");
      entries.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      entries.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage5() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(26);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fall Damage Immunity");
      entries.add(ChatColor.GRAY + "- Sculk Silence");
      entries.add(ChatColor.GRAY + "- Enchants Power");
      entries.add(ChatColor.GRAY + "- Enchants Punch");
      entries.add(ChatColor.GRAY + "- Enchants Feather Falling");
      entries.add(ChatColor.GRAY + "- Crop Tramp-Less");
      entries.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      entries.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage6() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(6);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fall Damage Immunity");
      entries.add(ChatColor.GRAY + "- Sculk Silence");
      entries.add(ChatColor.GRAY + "- Enchants Power");
      entries.add(ChatColor.GRAY + "- Enchants Punch");
      entries.add(ChatColor.GRAY + "- Enchants Feather Falling");
      entries.add(ChatColor.GRAY + "- Crop Tramp-Less");
      entries.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      entries.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage7() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(6);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fall Damage Immunity");
      entries.add(ChatColor.GRAY + "- Sculk Silence");
      entries.add(ChatColor.GRAY + "- Enchants Power");
      entries.add(ChatColor.GRAY + "- Enchants Punch");
      entries.add(ChatColor.GRAY + "- Enchants Feather Falling");
      entries.add(ChatColor.GRAY + "- Crop Tramp-Less");
      entries.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      entries.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage8() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(6);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fall Damage Immunity");
      entries.add(ChatColor.GRAY + "- Sculk Silence");
      entries.add(ChatColor.GRAY + "- Enchants Power");
      entries.add(ChatColor.GRAY + "- Enchants Punch");
      entries.add(ChatColor.GRAY + "- Enchants Feather Falling");
      entries.add(ChatColor.GRAY + "- Crop Tramp-Less");
      entries.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      entries.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage9() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(6);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fall Damage Immunity");
      entries.add(ChatColor.GRAY + "- Sculk Silence");
      entries.add(ChatColor.GRAY + "- Enchants Power");
      entries.add(ChatColor.GRAY + "- Enchants Punch");
      entries.add(ChatColor.GRAY + "- Enchants Feather Falling");
      entries.add(ChatColor.GRAY + "- Crop Tramp-Less");
      entries.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      entries.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage10() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(6);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fall Damage Immunity");
      entries.add(ChatColor.GRAY + "- Sculk Silence");
      entries.add(ChatColor.GRAY + "- Enchants Power");
      entries.add(ChatColor.GRAY + "- Enchants Punch");
      entries.add(ChatColor.GRAY + "- Enchants Feather Falling");
      entries.add(ChatColor.GRAY + "- Crop Tramp-Less");
      entries.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      entries.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createPuffTierTwoGemStage11() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(6);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴘᴜғғ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʙᴇ ᴛʜᴇ ʙɪɢɢᴇѕᴛ"
              + " ʙɪʀᴅ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fall Damage Immunity");
      entries.add(ChatColor.GRAY + "- Sculk Silence");
      entries.add(ChatColor.GRAY + "- Enchants Power");
      entries.add(ChatColor.GRAY + "- Enchants Punch");
      entries.add(ChatColor.GRAY + "- Enchants Feather Falling");
      entries.add(ChatColor.GRAY + "- Crop Tramp-Less");
      entries.add("");
      String textColor = ChatColor.WHITE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Double Jump");
      entries.add("");
      textColor = ChatColor.WHITE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "☁ ʙʀᴇᴇᴢʏ ʙᴀsʜ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(ChatColor.WHITE + "⏫ ᴅᴀsʜ");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage1() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(96);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage2() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(84);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage3() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(64);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage4() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(44);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage5() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(24);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage6() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(4);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage7() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(4);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage8() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(4);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage9() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(4);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage10() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(4);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createLifeTierTwoGemStage11() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(4);
      itemMeta.setMaxStackSize(1);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setDisplayName(
          ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD
              + "ʟɪғᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴄᴏɴᴛʀᴏʟѕ ᴛʜᴇ"
              + " ʙᴀʟᴀɴᴄᴇ ᴏғ"
              + " ʟɪғᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.LIGHT_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Green Thumb");
      entries.add(ChatColor.GRAY + "- Radiant Fist");
      entries.add(ChatColor.GRAY + "- Bonus Saturation");
      entries.add(ChatColor.GRAY + "- Bonus Absorption");
      entries.add(ChatColor.GRAY + "- Enchants Unbreaking");
      entries.add(ChatColor.GRAY + "- Wither Immune");
      entries.add("");
      String textColor = ChatColor.LIGHT_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.of("#FE04B4") + "Vitality Vortex");
      entries.add("");
      textColor = ChatColor.LIGHT_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#FF429A").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛ ᴅʀᴀɪɴᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#FF429A").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💘 "
              + labelColor
              + "ʜᴇᴀʀᴛʟᴏᴄᴋ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B8FFFA").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "💖 "
              + labelColor
              + "ᴄɪʀᴄʟᴇ ᴏғ ʟɪғᴇ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage1() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(96);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage2() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(90);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#B5B5B5").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ғʀᴀɪʟᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B5B5B5").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ɴᴜʟʟɪғʏ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage3() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(70);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#B5B5B5").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ғʀᴀɪʟᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B5B5B5").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ɴᴜʟʟɪғʏ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage4() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(50);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#B5B5B5").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ғʀᴀɪʟᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B5B5B5").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ɴᴜʟʟɪғʏ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage5() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(30);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#B5B5B5").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ғʀᴀɪʟᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B5B5B5").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ɴᴜʟʟɪғʏ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage6() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(10);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#B5B5B5").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ғʀᴀɪʟᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B5B5B5").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ɴᴜʟʟɪғʏ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage7() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(10);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#B5B5B5").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ғʀᴀɪʟᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B5B5B5").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ɴᴜʟʟɪғʏ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage8() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(10);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#B5B5B5").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ғʀᴀɪʟᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B5B5B5").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ɴᴜʟʟɪғʏ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage9() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(10);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#B5B5B5").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ғʀᴀɪʟᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B5B5B5").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ɴᴜʟʟɪғʏ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage10() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(10);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#B5B5B5").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ғʀᴀɪʟᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B5B5B5").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ɴᴜʟʟɪғʏ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createStrengthTierTwoGemStage11() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(10);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.DARK_RED.toString() + ChatColor.BOLD
              + "ѕᴛʀᴇɴɢᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ʜᴀᴠᴇ ᴛʜᴇ"
              + " ѕᴛʀᴇɴɢᴛʜ ᴏғ ᴀ"
              + " ᴀʀᴍʏ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_RED
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Strength");
      entries.add(ChatColor.GRAY + "- Enchant Sharpness");
      entries.add("");
      String textColor = ChatColor.DARK_RED.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.RED + "Frailer");
      entries.add("");
      textColor = ChatColor.DARK_RED.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#B5B5B5").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ғʀᴀɪʟᴇʀ "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#B5B5B5").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🤺 "
              + labelColor
              + "ɴᴜʟʟɪғʏ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#910D0D").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "⚔ "
              + labelColor
              + "ᴄʜᴀᴅ sᴛʀᴇɴɢᴛʜ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage1() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(96);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage2() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(88);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🎯 "
              + ChatColor.of("#FFE86E")
              + "ʙʟᴜʀ");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#61FFEA").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#61FFEA").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + argumentColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage3() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(68);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🎯 "
              + ChatColor.of("#FFE86E")
              + "ʙʟᴜʀ");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#61FFEA").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#61FFEA").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + argumentColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage4() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(48);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🎯 "
              + ChatColor.of("#FFE86E")
              + "ʙʟᴜʀ");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#61FFEA").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#61FFEA").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + argumentColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage5() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(28);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🎯 "
              + ChatColor.of("#FFE86E")
              + "ʙʟᴜʀ");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#61FFEA").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#61FFEA").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + argumentColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage6() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(8);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🎯 "
              + ChatColor.of("#FFE86E")
              + "ʙʟᴜʀ");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#61FFEA").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#61FFEA").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + argumentColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage7() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(8);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🎯 "
              + ChatColor.of("#FFE86E")
              + "ʙʟᴜʀ");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#61FFEA").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#61FFEA").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + argumentColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage8() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(8);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🎯 "
              + ChatColor.of("#FFE86E")
              + "ʙʟᴜʀ");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#61FFEA").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#61FFEA").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + argumentColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage9() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(8);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🎯 "
              + ChatColor.of("#FFE86E")
              + "ʙʟᴜʀ");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#61FFEA").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#61FFEA").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + argumentColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage10() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(8);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🎯 "
              + ChatColor.of("#FFE86E")
              + "ʙʟᴜʀ");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#61FFEA").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#61FFEA").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + argumentColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createSpeedTierTwoGemStage11() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(8);
      itemMeta.setMaxStackSize(1);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setDisplayName(
          ChatColor.YELLOW.toString() + ChatColor.BOLD
              + "ѕᴘᴇᴇᴅ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴡᴀᴛᴄʜ ᴛʜᴇ ᴡᴏʀʟᴅ"
              + " ᴀʀᴏᴜɴᴅ ʏᴏᴜ ᴛᴜʀɴ"
              + " ɪɴᴛᴏ ᴀ ʙʟᴜʀ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.YELLOW
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Speed");
      entries.add(ChatColor.GRAY + "- Dolphins Grace");
      entries.add(ChatColor.GRAY + "- Enchants Efficiency");
      entries.add(ChatColor.GRAY + "- Enchants Soul Speed");
      entries.add("");
      String textColor = ChatColor.YELLOW.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.YELLOW + "Thunder Step");
      entries.add("");
      textColor = ChatColor.YELLOW.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🎯 "
              + ChatColor.of("#FFE86E")
              + "ʙʟᴜʀ");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#61FFEA").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#61FFEA").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🌩 "
              + labelColor
              + "sᴘᴇᴇᴅʏ sᴛᴏʀᴍ"
              + argumentColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage1() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(96);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage2() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(92);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune III");
      entries.add(ChatColor.GRAY + "- Enchants Looting III");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Amplification");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Rich Rush");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Unfortunate");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Item Lock");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage3() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(72);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune III");
      entries.add(ChatColor.GRAY + "- Enchants Looting III");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Amplification");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Rich Rush");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Unfortunate");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Item Lock");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage4() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(52);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune III");
      entries.add(ChatColor.GRAY + "- Enchants Looting III");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Amplification");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Rich Rush");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Unfortunate");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Item Lock");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage5() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(32);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune III");
      entries.add(ChatColor.GRAY + "- Enchants Looting III");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Amplification");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Rich Rush");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Unfortunate");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Item Lock");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage6() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(12);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune III");
      entries.add(ChatColor.GRAY + "- Enchants Looting III");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Amplification");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Rich Rush");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Unfortunate");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Item Lock");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage7() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(12);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune III");
      entries.add(ChatColor.GRAY + "- Enchants Looting III");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Amplification");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Rich Rush");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Unfortunate");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Item Lock");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage8() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(12);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune III");
      entries.add(ChatColor.GRAY + "- Enchants Looting III");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Amplification");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Rich Rush");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Unfortunate");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Item Lock");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage9() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(12);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune III");
      entries.add(ChatColor.GRAY + "- Enchants Looting III");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Amplification");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Rich Rush");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Unfortunate");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Item Lock");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage10() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(12);
      itemMeta.setMaxStackSize(1);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune III");
      entries.add(ChatColor.GRAY + "- Enchants Looting III");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Amplification");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Rich Rush");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Unfortunate");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Item Lock");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createWealthTierTwoGemStage11() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(12);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_GREEN.toString() + ChatColor.BOLD
              + "ᴡᴇᴀʟᴛʜ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ғᴜᴇʟ ᴀ ᴇᴍᴘɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_GREEN
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Hero of the Village");
      entries.add(ChatColor.GRAY + "- Luck");
      entries.add(ChatColor.GRAY + "- Enchants Mending");
      entries.add(ChatColor.GRAY + "- Enchants Fortune III");
      entries.add(ChatColor.GRAY + "- Enchants Looting III");
      entries.add(ChatColor.GRAY + "- Bonus Ores");
      entries.add(ChatColor.GRAY + "- Extra EXP");
      entries.add(ChatColor.GRAY + "- Durability Chip");
      entries.add(ChatColor.GRAY + "- Double Debris");
      entries.add("");
      String textColor = ChatColor.DARK_GREEN.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor
              + "🔮 "
              + accentColor
              + bold
              + "ᴀʙɪʟɪᴛɪᴇѕ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Pockets");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Amplification");
      entries.add(ChatColor.GRAY + "- " + ChatColor.GREEN + "Rich Rush");
      entries.add("");
      textColor = ChatColor.DARK_GREEN.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Unfortunate");
      entries.add(
          ChatColor.GRAY
              + "- "
              + ChatColor.WHITE
              + "🍀 "
              + ChatColor.WHITE
              + "Item Lock");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage1() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(96);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage2() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(82);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage3() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(62);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage4() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(42);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage5() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(22);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage6() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(2);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage7() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(2);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage8() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(2);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage9() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(2);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage10() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(2);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFireTierTwoGemStage11() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(2);
      itemMeta.setMaxStackSize(1);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setDisplayName(
          ChatColor.GOLD.toString() + ChatColor.BOLD
              + "ғɪʀᴇ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴɪᴘᴜʟᴀᴛᴇ"
              + " ғɪʀᴇ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.GOLD
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Fire Resistance");
      entries.add(ChatColor.GRAY + "- Auto Smelt");
      entries.add(ChatColor.GRAY + "- Flamestrike");
      entries.add(ChatColor.GRAY + "- Fireshot");
      entries.add("");
      String gold = ChatColor.GOLD.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          gold + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.WHITE + "Crisp");
      entries.add("");
      gold = ChatColor.GOLD.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(gold + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ғɪʀᴇʙᴀʟʟ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.WHITE
              + "🧨 "
              + ChatColor.of("#FF5F33")
              + "ᴍᴇᴛᴇᴏʀ ꜱʜᴏᴡᴇʀ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.WHITE
              + "🥾 "
              + ChatColor.of("#248FD1")
              + "ᴄᴏᴢʏ Cᴀᴍᴘғɪʀᴇ");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage1() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(96);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add("ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setMaxStackSize(1);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage2() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(94);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setMaxStackSize(1);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage3() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(74);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setMaxStackSize(1);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage4() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(54);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setMaxStackSize(1);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage5() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(34);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setMaxStackSize(1);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage6() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(14);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setMaxStackSize(1);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage7() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(14);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setMaxStackSize(1);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage8() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(14);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setMaxStackSize(1);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage9() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(14);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage10() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(14);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setMaxStackSize(1);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createAstraTierTwoGemStage11() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(14);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD
              + "ᴀѕᴛʀᴀ "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴍᴀɴᴀɢᴇ ᴛʜᴇ"
              + " ᴛɪᴅᴇѕ ᴏғ ᴄᴏᴍᴏѕ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.DARK_PURPLE
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Phasing");
      entries.add(ChatColor.GRAY + "- Soul Healing");
      entries.add(ChatColor.GRAY + "- Soul Capture");
      entries.add("");
      String textColor = ChatColor.DARK_PURPLE.toString();
      String accentColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          textColor + "🔮 " + accentColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.GRAY + "- " + ChatColor.DARK_PURPLE + "Dimensional Drift");
      entries.add("");
      textColor = ChatColor.DARK_PURPLE.toString();
      accentColor = ChatColor.of("#B8FFFB").toString();
      String messageColor = ChatColor.BOLD.toString();
      entries.add(textColor + "🔮 " + accentColor + messageColor + "ᴘᴏᴡᴇʀѕ");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      String labelColor = ChatColor.of("#BFB8B8").toString();
      String nameColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴅᴀɢɢᴇʀs "
              + nameColor
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#BFB8B8").toString();
      String argumentColor = ChatColor.DARK_RED.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "🔪 "
              + labelColor
              + "ᴜɴʙᴏᴜɴᴅᴇᴅ "
              + argumentColor
              + "🤼");
      entries.add("");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String green = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ"
              + " ᴘʀᴏᴊᴇᴄᴛɪᴏɴ "
              + green
              + "🧑🏻");
      textColor = ChatColor.GRAY.toString();
      accentColor = ChatColor.WHITE.toString();
      labelColor = ChatColor.of("#AABBBF").toString();
      String normalizedColor = ChatColor.GREEN.toString();
      entries.add(
          textColor
              + "- "
              + accentColor
              + "👻 "
              + labelColor
              + "ᴀsᴛʀᴀʟ ᴠᴏɪᴅ "
              + normalizedColor
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.setMaxStackSize(1);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage1() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setMaxStackSize(1);
      itemMeta.setCustomModelData(96);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(ChatColor.WHITE + "ᴜѕᴇʟᴇѕѕ");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage2() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setMaxStackSize(1);
      itemMeta.setCustomModelData(178);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§f(§r§cRuined§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add(ChatColor.GRAY + "- Charged");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ɢʀᴏᴜɴᴅ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.GRAY
              + "- 🌀 "
              + ChatColor.of("#03EAFF")
              + "ᴋɪɴᴇᴛɪᴄ"
              + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
              + ChatColor.GREEN
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage3() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setMaxStackSize(1);
      itemMeta.setCustomModelData(158);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§f(§r" + ChatColor.of("#E2C35D") + "Damaged§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add(ChatColor.GRAY + "- Charged");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ɢʀᴏᴜɴᴅ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.GRAY
              + "- 🌀 "
              + ChatColor.of("#03EAFF")
              + "ᴋɪɴᴇᴛɪᴄ"
              + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
              + ChatColor.GREEN
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage4() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(138);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#7963B3") + "Cracked§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add(ChatColor.GRAY + "- Charged");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ɢʀᴏᴜɴᴅ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.GRAY
              + "- 🌀 "
              + ChatColor.of("#03EAFF")
              + "ᴋɪɴᴇᴛɪᴄ"
              + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
              + ChatColor.GREEN
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage5() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(118);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82F5AE") + "Scratched§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add(ChatColor.GRAY + "- Charged");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ɢʀᴏᴜɴᴅ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.GRAY
              + "- 🌀 "
              + ChatColor.of("#03EAFF")
              + "ᴋɪɴᴇᴛɪᴄ"
              + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
              + ChatColor.GREEN
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage6() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(98);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add(ChatColor.GRAY + "- Charged");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ɢʀᴏᴜɴᴅ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.GRAY
              + "- 🌀 "
              + ChatColor.of("#03EAFF")
              + "ᴋɪɴᴇᴛɪᴄ"
              + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
              + ChatColor.GREEN
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage7() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setMaxStackSize(1);
      itemMeta.setCustomModelData(98);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +1§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add(ChatColor.GRAY + "- Charged");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ɢʀᴏᴜɴᴅ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.GRAY
              + "- 🌀 "
              + ChatColor.of("#03EAFF")
              + "ᴋɪɴᴇᴛɪᴄ"
              + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
              + ChatColor.GREEN
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage8() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(98);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +2§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add(ChatColor.GRAY + "- Charged");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ɢʀᴏᴜɴᴅ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.GRAY
              + "- 🌀 "
              + ChatColor.of("#03EAFF")
              + "ᴋɪɴᴇᴛɪᴄ"
              + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
              + ChatColor.GREEN
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage9() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(98);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +3§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add(ChatColor.GRAY + "- Charged");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ɢʀᴏᴜɴᴅ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.GRAY
              + "- 🌀 "
              + ChatColor.of("#03EAFF")
              + "ᴋɪɴᴇᴛɪᴄ"
              + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
              + ChatColor.GREEN
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage10() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(98);
      itemMeta.setMaxStackSize(1);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +4§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add(ChatColor.GRAY + "- Charged");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ɢʀᴏᴜɴᴅ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.GRAY
              + "- 🌀 "
              + ChatColor.of("#03EAFF")
              + "ᴋɪɴᴇᴛɪᴄ"
              + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
              + ChatColor.GREEN
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.setEnchantmentGlintOverride(false);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }

  public static ItemStack createFluxTierTwoGemStage11() {
    ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setMaxStackSize(1);
      itemMeta.setUnbreakable(true);
      itemMeta.setCustomModelData(98);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      itemMeta.addEnchant(Enchantment.MENDING, 1, false);
      itemMeta.setDisplayName(
          ChatColor.DARK_AQUA.toString() + ChatColor.BOLD
              + "ғʟᴜx "
              + ChatColor.of("#FFD773")
              + "ɢᴇᴍ ");
      ArrayList<String> entries = new ArrayList<>();
      entries.add(
          ChatColor.WHITE.toString() + ChatColor.BOLD
              + "ᴇᴠᴇʀʏᴛʜɪɴɢ ɪѕ ᴀ"
              + " ғʟᴜᴄᴛᴜᴀᴛɪᴏɴ");
      entries.add("§r§f(" + ChatColor.of("#82EDBF") + "Pristine +5§r§f)");
      entries.add("");
      entries.add(
          ChatColor.of("#befff7")
              + "🔮 "
              + ChatColor.of("#FFE4AB")
              + "ᴘᴀѕѕɪᴠᴇѕ");
      entries.add(ChatColor.GRAY + "- Flow State");
      entries.add(ChatColor.GRAY + "- Shocking Chance");
      entries.add(ChatColor.GRAY + "- Conduction");
      entries.add(ChatColor.GRAY + "- Tireless");
      entries.add(ChatColor.GRAY + "- Charged");
      entries.add("");
      String accentColor = ChatColor.of("#befff7").toString();
      String messageColor = ChatColor.of("#82F3FF").toString();
      String bold = ChatColor.BOLD.toString();
      entries.add(
          accentColor + "🔮 " + messageColor + bold + "ᴀʙɪʟɪᴛʏ");
      entries.add(ChatColor.of("#befff7") + "- " + ChatColor.of("#FF686F") + "Static Burst");
      entries.add("");
      accentColor = ChatColor.of("#befff7").toString();
      messageColor = ChatColor.of("#B8FFFB").toString();
      String displayColor = ChatColor.BOLD.toString();
      entries.add(accentColor + "🔮 " + messageColor + displayColor + "ᴘᴏᴡᴇʀѕ");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ᴇɴᴇʀɢʏ ʙᴇᴀᴍ "
              + ChatColor.DARK_RED
              + "🧑🏻");
      entries.add(
          ChatColor.GRAY
              + "- ☄ "
              + ChatColor.of("#03EAFF")
              + "ɢʀᴏᴜɴᴅ "
              + ChatColor.DARK_RED
              + "🤼");
      entries.add("");
      entries.add(
          ChatColor.GRAY
              + "- 🌀 "
              + ChatColor.of("#03EAFF")
              + "ᴋɪɴᴇᴛɪᴄ"
              + " ᴏᴠᴇʀᴅʀɪᴠᴇ "
              + ChatColor.GREEN
              + "🤼");
      itemMeta.setLore(entries);
      itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
      item.setItemMeta(itemMeta);
    }

    return item;
  }
}
