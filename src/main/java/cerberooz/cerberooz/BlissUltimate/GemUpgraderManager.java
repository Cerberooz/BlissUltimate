package cerberooz.cerberooz.BlissUltimate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class GemUpgraderManager implements Listener {
  final Set<Integer> tierOneModelData = new HashSet<>();
  final Set<Integer> tierTwoModelData = new HashSet<>();
  final Set<UUID> playerIds = new HashSet<>();
  final Map<Integer, List<ItemStack>> currentItemListsByIndex = new HashMap<>();
  final Map<Integer, List<ItemStack>> itemListsByIndex = new HashMap<>();
  final Map<Integer, Integer> secondaryCountsByIndex = new HashMap<>();
  final Random random = new Random();
  static final String UPGRADER_MANAGER_ID;
  final Map<UUID, Integer> playerCounters = new HashMap<>();
  final Map<UUID, Boolean> activeFlagsByPlayer = new HashMap<>();
  static final List<Integer> entries;
  static final Map<Integer, Integer> pendingCountsByIndex;

  public GemUpgraderManager() {
    this.processAbilityState();
    this.initialize();
    this.completeAbilityAction();
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getHand() == EquipmentSlot.HAND) {
      Player player = event.getPlayer();
      ItemStack mainHandItem = player.getInventory().getItemInMainHand();
      ItemStack offHandItem = player.getInventory().getItemInOffHand();
      boolean enabled = this.isAbilityAllowed(mainHandItem);
      boolean active = this.isAbilityAllowed(offHandItem);
      if (enabled || active) {
        if (!CooldownService.isOnCooldown(player.getUniqueId(), "trader")) {
          if (this.playerIds.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already rolling a gem.");
          } else if (!event.getAction().isRightClick()) {
            if (event.getAction().isLeftClick()) {
              if (ConfigValueCache.getBoolean(Bliss.getInstance(), "EnableUltimateTrading", true)) {
                this.sendAbilityFeedback(player);
              }
            }
          } else {
            event.setCancelled(true);
            int count = -1;
            ItemStack item = null;
            ItemStack heldItem = player.getInventory().getItemInOffHand();
            if (this.isActiveForPlayer(heldItem)) {
              count = 40;
              item = heldItem;
            } else {
              ItemStack[] targetItem = player.getInventory().getContents();

              for (int index = 0; index < targetItem.length; index++) {
                if (this.isActiveForPlayer(targetItem[index])) {
                  count = index;
                  item = targetItem[index];
                  break;
                }
              }
            }

            if (count != -1 && item != null) {
              boolean allowed = this.hasRequiredState(item);
              int remaining = GemEnergyManager.getAbilityIntValue(player);
              ItemStack candidateItem = item.clone();
              if (enabled) {
                if (mainHandItem.getAmount() > 1) {
                  mainHandItem.setAmount(mainHandItem.getAmount() - 1);
                } else {
                  player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
              } else if (offHandItem.getAmount() > 1) {
                offHandItem.setAmount(offHandItem.getAmount() - 1);
              } else {
                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
              }

              if (count == 40) {
                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
              } else {
                player.getInventory().setItem(count, new ItemStack(Material.AIR));
              }

              this.playerIds.add(player.getUniqueId());
              this.playAbilityEffects(player, candidateItem, remaining, allowed, count);
              CooldownService.setCooldown(player.getUniqueId(), "trader", 3L);
              AstraTier2Gem.sendAbilityFeedback(player.getUniqueId(), 3000L);
            } else {
              player.sendMessage(ChatColor.RED + "You need a gem to use the trader.");
            }
          }
        }
      }
    }
  }

  void sendAbilityFeedback(Player player) {
    UUID playerId = player.getUniqueId();
    ItemStack item = this.createItem(player);
    if (item == null) {
      this.updateAbilityState(playerId);
      player.sendMessage(ChatColor.RED + "You need a gem to use the trader.");
    } else {
      int count = this.getMaxCount(item);
      if (count == -1) {
        this.updateAbilityState(playerId);
        player.sendMessage(ChatColor.RED + "Could not detect your current gem.");
      } else {
        this.playerCounters.put(playerId, count);
        this.activeFlagsByPlayer.put(playerId, this.hasRequiredState(item));
        Inventory inventory = Bukkit.createInventory(null, 27, UPGRADER_MANAGER_ID);
        this.applyAbilityEffects(inventory);
        inventory.setItem(9, this.createAbilityItem(9, count == 9, 0));
        inventory.setItem(10, this.createAbilityItem(10, count == 10, 0));
        inventory.setItem(11, this.createAbilityItem(11, count == 11, 0));
        inventory.setItem(12, this.createAbilityItem(12, count == 12, 0));
        inventory.setItem(14, this.createAbilityItem(14, count == 14, 0));
        inventory.setItem(15, this.createAbilityItem(15, count == 15, 0));
        inventory.setItem(16, this.createAbilityItem(16, count == 16, 0));
        inventory.setItem(17, this.createAbilityItem(17, count == 17, 0));
        this.activateAbility(inventory, player);
        player.openInventory(inventory);
      }
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player) {
      if (event.getView().getTitle().equals(UPGRADER_MANAGER_ID)) {
        event.setCancelled(true);
        Inventory inventory = event.getInventory();
        int count = event.getRawSlot();
        if (count == 13) {
          int index = this.getAbilityIntValue(player, inventory);
          if (GemEnergyManager.getAbilityIntValue(player) - index < 1) {
            player.sendMessage(ChatColor.RED + "You need to stay atleast on ruined state!");
          } else {
            this.playAbilitySound(player, inventory, index);
          }
        } else if (entries.contains(count)) {
          int selectedSlot = this.playerCounters.getOrDefault(player.getUniqueId(), -1);
          if (count != selectedSlot) {
            ItemStack item = inventory.getItem(count);
            boolean enabled = this.isGemItem(item);
            if (enabled || this.getDurationTicks(inventory) > 1) {
              inventory.setItem(count, this.createAbilityItem(count, !enabled, 0));
              this.activateAbility(inventory, player);
              player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.5F, 1.5F);
            }
          }
        }
      }
    }
  }

  int getAbilityIntValue(Player player, Inventory inventory) {
    int selectedSlot = this.playerCounters.getOrDefault(player.getUniqueId(), -1);
    int count = 0;

    for (int index : entries) {
      if (index != selectedSlot && this.isGemItem(inventory.getItem(index))) {
        count++;
      }
    }

    return count;
  }

  int getDurationTicks(Inventory inventory) {
    int count = 0;

    for (int index : entries) {
      if (!this.isGemItem(inventory.getItem(index))) {
        count++;
      }
    }

    return count;
  }

  ItemStack createItem(Player player) {
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    if (this.isActiveForPlayer(offHandItem)) {
      return offHandItem;
    }

    for (ItemStack item : player.getInventory().getContents()) {
      if (this.isActiveForPlayer(item)) {
        return item;
      }
    }

    return null;
  }

  void activateAbility(Inventory inventory, Player player) {
    int selectedSlot = this.playerCounters.getOrDefault(player.getUniqueId(), -1);
    int count = 0;
    int index = 0;

    for (int remaining : entries) {
      if (this.isGemItem(inventory.getItem(remaining))) {
        index++;
        if (remaining != selectedSlot) {
          count++;
        }
      }
    }

    int step = entries.size() - index;
    int ticks = step <= 0 ? 0 : (int) Math.floor(100.0 / step);

    for (int durationTicks : entries) {
      boolean enabled = this.isGemItem(inventory.getItem(durationTicks));
      int attempts = 0;
      if (!enabled) {
        attempts = ticks;
      }

      inventory.setItem(durationTicks, this.createAbilityItem(durationTicks, enabled, attempts));
    }

    if (count <= 0) {
      inventory.setItem(13, EnergyDecayScanner.createUselessEnergyTradeItem());
    } else {
      inventory.setItem(13, this.createUpgradeItem(count, player));
    }
  }

  boolean isGemItem(ItemStack item) {
    if (item == null || item.getType() != Material.ECHO_SHARD) {
      return false;
    }

    if (!item.hasItemMeta()) {
      return false;
    }

    ItemMeta itemMeta = item.getItemMeta();
    return itemMeta != null && itemMeta.hasCustomModelData()
        ? pendingCountsByIndex.containsValue(itemMeta.getCustomModelData())
        : false;
  }

  ItemStack createAbilityItem(int count, boolean enabled, int index) {
    ItemStack item;
    switch (count) {
      case 9:
        item = BlissItems.createLifeTierTwoGemStage6();
        break;
      case 10:
        item = BlissItems.createStrengthTierTwoGemStage6();
        break;
      case 11:
        item = BlissItems.createFireTierTwoGemStage6();
        break;
      case 12:
        item = BlissItems.createSpeedTierTwoGemStage6();
        break;
      case 13:
      default:
        return new ItemStack(Material.AIR);
      case 14:
        item = BlissItems.createWealthTierTwoGemStage11();
        break;
      case 15:
        item = BlissItems.createFluxTierTwoGemStage6();
        break;
      case 16:
        item = BlissItems.createAstraTierTwoGemStage6();
        break;
      case 17:
        item = BlissItems.createPuffTierTwoGemStage6();
    }

    item = item.clone();
    if (enabled) {
      item.setType(Material.ECHO_SHARD);
    }

    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta == null) {
      return item;
    }

    ArrayList<String> lore = new ArrayList<>();
    lore.add(ChatColor.WHITE + "Chance: " + index + "%");
    itemMeta.setLore(lore);
    if (enabled) {
      itemMeta.setCustomModelData(pendingCountsByIndex.get(count));
    }

    item.setItemMeta(itemMeta);
    return item;
  }

  int getMaxCount(ItemStack item) {
    if (item == null) {
      return -1;
    } else if (LifeTier1Gem.shouldApplyEffect(item) || LifeTier2Gem.isGemItem(item)) {
      return 9;
    } else if (StrengthGemRevamp.isTierOneGem(item) || StrengthGemRevamp.isTierTwoGem(item)) {
      return 10;
    } else if (FireTier1Gem.isGemItem(item) || FireTier2Gem.isGemItem(item)) {
      return 11;
    } else if (SpeedTier1Gem.isProtectedTarget(item) || SpeedTier2Gem.isActiveForPlayer(item)) {
      return 12;
    } else if (WealthTier1Gem.hasRequiredState(item) || WealthTier2Gem.isStoredGemItem(item)) {
      return 14;
    } else if (FluxTier1Gem.isGemItem(item) || FluxTier2Gem.isProtectedTarget(item)) {
      return 15;
    } else if (AstraTier1Gem.isAbilityBlocked(item) || AstraTier2Gem.isGemItem(item)) {
      return 16;
    } else {
      return !PuffTier1Gem.isGemItem(item) && !PuffTier2Gem.isAbilityBlocked(item) ? -1 : 17;
    }
  }

  void playAbilitySound(Player player, Inventory inventory, int count) {
    UUID playerId = player.getUniqueId();
    Boolean currentBoolean = this.activeFlagsByPlayer.get(playerId);
    if (currentBoolean == null) {
      player.sendMessage(ChatColor.RED + "Your trade session expired. Open the trader again.");
      player.closeInventory();
      this.updateAbilityState(playerId);
    } else {
      HashMap<Integer, Integer> valuesBySlot = new HashMap<>();

      for (int index : entries) {
        ItemStack item = inventory.getItem(index);
        if (!this.isGemItem(item)) {
          int remaining = this.getRemainingTicks(item);
          if (remaining > 0) {
            valuesBySlot.put(index, remaining);
          }
        }
      }

      if (valuesBySlot.isEmpty()) {
        player.sendMessage(ChatColor.RED + "At least one gem needs to be enabled.");
      } else {
        int step = this.getChargeLevel(valuesBySlot);
        if (step == -1) {
          player.sendMessage(ChatColor.RED + "Failed to select gem.");
        } else {
          String text = this.formatDisplayText(step, currentBoolean);
          if (text == null) {
            player.sendMessage(ChatColor.RED + "Invalid selected gem.");
          } else {
            ItemStack heldItem = this.createDisplayItem(player);
            if (heldItem == null) {
              player.sendMessage(ChatColor.RED + "You need to hold the trader.");
            } else {
              this.updateState(heldItem);
              player.closeInventory();
              this.scheduleAbilityUpdate(player);
              this.cleanupAbilityState(player);
              if (count > 0) {
                GemEnergyManager.canUseAbility(player, count);
              }

              int ticks = GemEnergyManager.getAbilityIntValue(player);
              ItemStack targetItem =
                  GemEnergyManager.createItem(text, ticks);
              if (targetItem == null) {
                player.sendMessage(ChatColor.RED + "Could not create selected gem.");
                this.updateAbilityState(playerId);
              } else {
                this.refreshAbilityState(player, targetItem);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                player.sendMessage(
                    ChatColor.of("#E4BC74")
                        + "🔮 "
                        + ChatColor.of("#befff7")
                        + "You have traded your current gem to "
                        + this.formatDisplayTextFromConfig(targetItem));
                this.updateAbilityState(playerId);
              }
            }
          }
        }
      }
    }
  }

  int getChargeLevel(Map<Integer, Integer> valuesBySlot) {
    int count = 0;

    for (int index : valuesBySlot.values()) {
      count += index;
    }

    if (count <= 0) {
      return -1;
    }

    int remaining = this.random.nextInt(count) + 1;
    int step = 0;

    for (Entry<Integer, Integer> entry : valuesBySlot.entrySet()) {
      step += entry.getValue();
      if (remaining <= step) {
        return entry.getKey();
      }
    }

    return -1;
  }

  void refreshAbilityState(Player player, ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);

      for (ItemStack heldItem : leftovers.values()) {
        if (heldItem != null && heldItem.getType() != Material.AIR) {
          player.getWorld().dropItemNaturally(player.getLocation(), heldItem);
        }
      }
    }
  }

  String formatDisplayText(int count, boolean enabled) {
    if (enabled) {
      return switch (count) {
        case 9 -> "LIFE_T2";
        case 10 -> "STRENGTH_T2";
        case 11 -> "FIRE_T2";
        case 12 -> "SPEED_T2";
        default -> null;
        case 14 -> "WEALTH_T2";
        case 15 -> "FLUX_T2";
        case 16 -> "ASTRA_T2";
        case 17 -> "PUFF_T2";
      };
    } else {
      return switch (count) {
        case 9 -> "LIFE";
        case 10 -> "STRENGTH";
        case 11 -> "FIRE_T1";
        case 12 -> "SPEED";
        default -> null;
        case 14 -> "WEALTH";
        case 15 -> "FLUX_T1";
        case 16 -> "ASTRA_T1";
        case 17 -> "PUFF";
      };
    }
  }

  void updateAbilityState(UUID playerId) {
    this.playerCounters.remove(playerId);
    this.activeFlagsByPlayer.remove(playerId);
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getPlayer() instanceof Player player) {
      if (event.getView().getTitle().equals(UPGRADER_MANAGER_ID)) {
        this.updateAbilityState(player.getUniqueId());
      }
    }
  }

  ItemStack createDisplayItem(Player player) {
    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    if (this.isAbilityAllowed(mainHandItem)) {
      return mainHandItem;
    } else {
      return this.isAbilityAllowed(offHandItem) ? offHandItem : null;
    }
  }

  void updateState(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      if (item.getAmount() > 1) {
        item.setAmount(item.getAmount() - 1);
      } else {
        item.setAmount(0);
      }
    }
  }

  String formatDisplayTextForPlayer(ItemStack item) {
    return "Invalid gem!";
  }

  int getRemainingTicks(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      if (itemMeta != null && itemMeta.getLore() != null) {
        for (String text : itemMeta.getLore()) {
          String message = org.bukkit.ChatColor.stripColor(text);
          if (message != null && message.startsWith("Chance: ")) {
            message = message.replace("Chance: ", "").replace("%", "").trim();

            try {
              return Integer.parseInt(message);
            } catch (NumberFormatException numberFormatException) {
              return 0;
            }
          }
        }

        return 0;
      } else {
        return 0;
      }
    } else {
      return 0;
    }
  }

  ItemStack createUpgradeItem(int count, Player player) {
    ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
    item.setAmount(count);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setDisplayName(
        ChatColor.of("#96FFD9")
            + org.bukkit.ChatColor.BOLD.toString()
            + "ᴇɴᴇʀɢʏ "
            + ChatColor.of("#F6D7B3")
            + "ɪɴ ᴀ ʙᴏᴛᴛʟᴇ");
    ArrayList<String> lore = new ArrayList<>();
    lore.add(ChatColor.WHITE + "Left click to trade!");
    lore.add(
        ChatColor.WHITE
            + "This will leave you on the "
            + this.formatDisplayTextForTarget(player, count)
            + "§f state.");
    itemMeta.setLore(lore);
    itemMeta.setCustomModelData(300);
    item.setItemMeta(itemMeta);
    return item;
  }

  String formatDisplayTextForTarget(Player player, int count) {
    int index = GemEnergyManager.getAbilityIntValue(player) - count;
    if (index <= 0) {
      return ChatColor.DARK_RED + "Broken";
    } else if (index == 1) {
      return "§cRuined";
    } else if (index == 2) {
      return ChatColor.of("#E2C35D") + "Damaged§f";
    } else if (index == 3) {
      return ChatColor.of("#7963B3") + "Cracked§f";
    } else if (index == 4) {
      return ChatColor.of("#82F5AE") + "Scratched§f";
    } else if (index == 5) {
      return ChatColor.of("#82EDBF") + "Pristine§f";
    } else if (index == 6) {
      return ChatColor.of("#82EDBF") + "Pristine +1§f";
    } else if (index == 7) {
      return ChatColor.of("#82EDBF") + "Pristine +2§f";
    } else if (index == 8) {
      return ChatColor.of("#82EDBF") + "Pristine +3§f";
    } else if (index == 9) {
      return ChatColor.of("#82EDBF") + "Pristine +4§f";
    } else {
      return index == 10 ? ChatColor.of("#82EDBF") + "Pristine +5§f" : "Invalid state";
    }
  }

  void applyAbilityEffects(Inventory inventory) {
    for (int count = 0; count < inventory.getSize(); count++) {
      ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.setHideTooltip(true);
      item.setItemMeta(itemMeta);
      inventory.setItem(count, item);
    }
  }

  void playAbilityEffects(Player player, ItemStack item, int count, boolean enabled, int index) {
    List<ItemStack> entries = enabled ? this.getStateForPlayer(count) : this.getState(count);
    new BukkitRunnable() {
      final int maxCount = count;
      final boolean active = enabled;
      final int animationStep = count;
      int cooldownTicks = 0;
      final int durationTicks = 5;

      @Override
      public void run() {
        if (!player.isOnline()) {
          GemUpgraderManager.this.playerIds.remove(player.getUniqueId());
          this.cancel();
        } else if (this.cooldownTicks < 5) {
          ItemStack item = entries.get(GemUpgraderManager.this.random.nextInt(entries.size())).clone();
          GemUpgraderManager.this.scheduleAbilityUpdate(player);
          GemUpgraderManager.this.cleanupAbilityState(player);
          if (this.maxCount == 40) {
            player.getInventory().setItemInOffHand(item);
          } else {
            player.getInventory().setItem(this.maxCount, item);
          }

          player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0F, 1.5F);
          this.cooldownTicks++;
        } else {
          ItemStack heldItem =
          this.active
          ? GemUpgraderManager.this.createRewardItem(item, this.animationStep)
          : GemUpgraderManager.this.createRecipeItem(item, this.animationStep);
          GemUpgraderManager.this.scheduleAbilityUpdate(player);
          GemUpgraderManager.this.cleanupAbilityState(player);
          if (this.maxCount == 40) {
            player.getInventory().setItemInOffHand(heldItem);
          } else {
            player.getInventory().setItem(this.maxCount, heldItem);
          }

          player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
          GemUpgraderManager.this.playerIds.remove(player.getUniqueId());
          player.sendMessage(
              ChatColor.of("#E4BC74")
              + "🔮 "
              + ChatColor.of("#befff7")
              + "You have traded your current gem to "
              + GemUpgraderManager.this.formatDisplayTextFromConfig(heldItem));
          this.cancel();
        }
      }
    }
        .runTaskTimer(Bliss.getInstance(), SharedScheduler.staggeredInitialDelay(10L), 10L);
  }

  String formatDisplayTextFromConfig(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      String displayName = item.getItemMeta().getDisplayName();
      return displayName.replaceAll(
          " (§[0-9a-fk-orx]|§x(§[0-9a-fA-F]){6})*ɢᴇᴍ", "");
    } else {
      return null;
    }
  }

  List<ItemStack> getState(int count) {
    return new ArrayList<>(this.currentItemListsByIndex.getOrDefault(count, this.currentItemListsByIndex.get(5)));
  }

  List<ItemStack> getStateForPlayer(int count) {
    return new ArrayList<>(this.itemListsByIndex.getOrDefault(count, this.itemListsByIndex.get(5)));
  }

  boolean canUseAbility(Player player) {
    return this.playerIds.contains(player.getUniqueId());
  }

  ItemStack createRecipeItem(ItemStack heldItem, int count) {
    if (count == -1) {
      count = 5;
    }

    List<ItemStack> entries = this.getState(count);
    if (heldItem != null) {
      entries.removeIf(item -> shouldGetRandomDifferentGemT1ForReturn(heldItem, item));
    }

    return !entries.isEmpty()
        ? (entries.get(this.random.nextInt(entries.size()))).clone()
        : this.createPrimaryItem(count);
  }

  ItemStack createRewardItem(ItemStack heldItem, int count) {
    if (count == -1) {
      count = 5;
    }

    List<ItemStack> entries = this.getStateForPlayer(count);
    if (heldItem != null) {
      entries.removeIf(item -> shouldGetRandomDifferentGemT2ForReturn(heldItem, item));
    }

    return !entries.isEmpty()
        ? (entries.get(this.random.nextInt(entries.size()))).clone()
        : this.createItemTarget(count);
  }

  @EventHandler
  public void spawnAbilityEffects(InventoryClickEvent event) {
    if (event.getWhoClicked() instanceof Player player && this.canUseAbility(player)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
    if (this.canUseAbility(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (this.canUseAbility(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  boolean isAbilityAllowed(ItemStack item) {
    if (item == null || item.getType() != Material.PLAYER_HEAD) {
      return false;
    }

    if (!item.hasItemMeta()) {
      return false;
    }

    ItemMeta itemMeta = item.getItemMeta();
    if (itemMeta == null) {
      return false;
    }

    if (!itemMeta.hasCustomModelData()) {
      return false;
    }

    if (itemMeta.getCustomModelData() != 1) {
      return false;
    }

    String message = itemMeta.getDisplayName();
    // Both heads open this menu. The upgrader is the intended tier-upgrade item;
    // supporting the trader here preserves its existing random-gem behaviour.
    return message != null
        && (message.contains("ᴜᴘɢʀᴀᴅᴇʀ") || message.contains("ᴛʀᴀᴅᴇʀ"));
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    ItemStack item = event.getItemInHand();
    if (item != null
        && (BlissItems.isMatchingItem(item, BlissItems.createUpgraderMenuItem())
            || BlissItems.isMatchingItem(
                item, BlissItems.createTraderMenuItem()))) {
      event.setCancelled(true);
    }
  }

  boolean isAbilityBlocked(Player player) {
    return this.isValidTarget(player, this.tierTwoModelData);
  }

  boolean isMatchingState(Player player) {
    return this.isValidTarget(player, this.tierOneModelData);
  }

  boolean isValidTarget(Player player, Set<Integer> modelDataValues) {
    for (ItemStack item : player.getInventory().getContents()) {
      if (item != null
          && item.hasItemMeta()
          && item.getItemMeta().hasCustomModelData()
          && modelDataValues.contains(item.getItemMeta().getCustomModelData())) {
        return true;
      }
    }

    return false;
  }

  boolean isProtectedTarget(ItemStack item) {
    if (item == null || item.getType() != Material.AMETHYST_SHARD || !item.hasItemMeta()) {
      return false;
    } else {
      return item.getItemMeta().hasCustomModelData() && this.tierOneModelData.contains(item.getItemMeta().getCustomModelData());
    }
  }

  void cleanupAbilityState(Player player) {
    ItemStack[] item = player.getInventory().getContents();

    for (int count = 0; count < item.length; count++) {
      ItemStack heldItem = item[count];
      if (heldItem != null && this.isActiveForPlayer(heldItem)) {
        item[count] = null;
      }
    }

    player.getInventory().setContents(item);
  }

  void scheduleAbilityUpdate(Player player) {
    ItemStack offHandItem = player.getInventory().getItemInOffHand();
    if (offHandItem != null && this.isActiveForPlayer(offHandItem)) {
      player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
    }
  }

  public boolean isActiveForPlayer(ItemStack item) {
    if (item != null && item.hasItemMeta()) {
      Material material = item.getType();
      if (material != Material.AMETHYST_SHARD && material != Material.PRISMARINE_SHARD) {
        return false;
      }

      if (!item.getItemMeta().hasCustomModelData()) {
        return false;
      }

      int customModelData = item.getItemMeta().getCustomModelData();
      return this.tierOneModelData.contains(customModelData) || this.tierTwoModelData.contains(customModelData);
    } else {
      return false;
    }
  }

  boolean hasRequiredState(ItemStack item) {
    if (item != null && item.getType() == Material.PRISMARINE_SHARD && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      return itemMeta.hasCustomModelData() && this.tierTwoModelData.contains(itemMeta.getCustomModelData());
    } else {
      return false;
    }
  }

  ItemStack createPrimaryItem(int count) {
    ArrayList<ItemStack> lore = new ArrayList<>();
    switch (count) {
      case 0:
        lore.add(BlissItems.createPuffTierOneGemStage1());
        lore.add(BlissItems.createAstraTierOneGemStage1());
        lore.add(BlissItems.createFireTierOneGemStage1());
        lore.add(BlissItems.createLifeTierOneGemStage1());
        lore.add(BlissItems.createSpeedTierOneGemStage1());
        lore.add(BlissItems.createWealthTierOneGemStage1());
        lore.add(BlissItems.createStrengthTierOneGemStage1());
        lore.add(BlissItems.createFluxTierOneGemStage1());
        break;
      case 1:
        lore.add(BlissItems.createPuffTierOneGemStage2());
        lore.add(BlissItems.createAstraTierOneGemStage2());
        lore.add(BlissItems.createFireTierOneGemStage2());
        lore.add(BlissItems.createLifeTierOneGemStage2());
        lore.add(BlissItems.createSpeedTierOneGemStage2());
        lore.add(BlissItems.createWealthTierOneGemStage2());
        lore.add(BlissItems.createStrengthTierOneGemStage2());
        lore.add(BlissItems.createFluxTierOneGemStage2());
        break;
      case 2:
        lore.add(BlissItems.createPuffTierOneGemStage3());
        lore.add(BlissItems.createAstraTierOneGemStage3());
        lore.add(BlissItems.createFireTierOneGemStage3());
        lore.add(BlissItems.createLifeTierOneGemStage3());
        lore.add(BlissItems.createSpeedTierOneGemStage3());
        lore.add(BlissItems.createWealthTierOneGemStage3());
        lore.add(BlissItems.createStrengthTierOneGemStage3());
        lore.add(BlissItems.createFluxTierOneGemStage3());
        break;
      case 3:
        lore.add(BlissItems.createPuffTierOneGemStage4());
        lore.add(BlissItems.createAstraTierOneGemStage4());
        lore.add(BlissItems.createFireTierOneGemStage4());
        lore.add(BlissItems.createLifeTierOneGemStage4());
        lore.add(BlissItems.createSpeedTierOneGemStage4());
        lore.add(BlissItems.createWealthTierOneGemStage4());
        lore.add(BlissItems.createStrengthTierOneGemStage4());
        lore.add(BlissItems.createFluxTierOneGemStage4());
        break;
      case 4:
        lore.add(BlissItems.createPuffTierOneGemStage5());
        lore.add(BlissItems.createAstraTierOneGemStage5());
        lore.add(BlissItems.createFireTierOneGemStage5());
        lore.add(BlissItems.createLifeTierOneGemStage5());
        lore.add(BlissItems.createSpeedTierOneGemStage5());
        lore.add(BlissItems.createWealthTierOneGemStage5());
        lore.add(BlissItems.createStrengthTierOneGemStage5());
        lore.add(BlissItems.createFluxTierOneGemStage5());
        break;
      case 5:
        lore.add(BlissItems.createPuffTierOneGemStage6());
        lore.add(BlissItems.createAstraTierOneGemStage6());
        lore.add(BlissItems.createFireTierOneGemStage6());
        lore.add(BlissItems.createLifeTierOneGemStage6());
        lore.add(BlissItems.createSpeedTierOneGemStage6());
        lore.add(BlissItems.createWealthTierOneGemStage6());
        lore.add(BlissItems.createStrengthTierOneGemStage6());
        lore.add(BlissItems.createFluxTierOneGemStage6());
        break;
      case 6:
        lore.add(BlissItems.createPuffTierOneGemStage7());
        lore.add(BlissItems.createAstraTierOneGemStage7());
        lore.add(BlissItems.createFireTierOneGemStage7());
        lore.add(BlissItems.createLifeTierOneGemStage7());
        lore.add(BlissItems.createSpeedTierOneGemStage7());
        lore.add(BlissItems.createWealthTierOneGemStage7());
        lore.add(BlissItems.createStrengthTierOneGemStage7());
        lore.add(BlissItems.createFluxTierOneGemStage7());
        break;
      case 7:
        lore.add(BlissItems.createPuffTierOneGemStage8());
        lore.add(BlissItems.createAstraTierOneGemStage8());
        lore.add(BlissItems.createFireTierOneGemStage8());
        lore.add(BlissItems.createLifeTierOneGemStage8());
        lore.add(BlissItems.createSpeedTierOneGemStage8());
        lore.add(BlissItems.createWealthTierOneGemStage8());
        lore.add(BlissItems.createStrengthTierOneGemStage8());
        lore.add(BlissItems.createFluxTierOneGemStage8());
        break;
      case 8:
        lore.add(BlissItems.createPuffTierOneGemStage9());
        lore.add(BlissItems.createAstraTierOneGemStage9());
        lore.add(BlissItems.createFireTierOneGemStage9());
        lore.add(BlissItems.createLifeTierOneGemStage9());
        lore.add(BlissItems.createSpeedTierOneGemStage9());
        lore.add(BlissItems.createWealthTierOneGemStage9());
        lore.add(BlissItems.createStrengthTierOneGemStage9());
        lore.add(BlissItems.createFluxTierOneGemStage9());
        break;
      case 9:
        lore.add(BlissItems.createPuffTierOneGemStage10());
        lore.add(BlissItems.createAstraTierOneGemStage10());
        lore.add(BlissItems.createFireTierOneGemStage10());
        lore.add(BlissItems.createLifeTierOneGemStage10());
        lore.add(BlissItems.createSpeedTierOneGemStage10());
        lore.add(BlissItems.createWealthTierOneGemStage10());
        lore.add(BlissItems.createStrengthTierOneGemStage10());
        lore.add(BlissItems.createFluxTierOneGemStage10());
        break;
      case 10:
        lore.add(BlissItems.createPuffTierOneGemStage11());
        lore.add(BlissItems.createAstraTierOneGemStage11());
        lore.add(BlissItems.createFireTierOneGemStage11());
        lore.add(BlissItems.createLifeTierOneGemStage11());
        lore.add(BlissItems.createSpeedTierOneGemStage11());
        lore.add(BlissItems.createWealthTierOneGemStage11());
        lore.add(BlissItems.createStrengthTierOneGemStage11());
        lore.add(BlissItems.createFluxTierOneGemStage11());
        break;
      default:
        lore.add(BlissItems.createPuffTierOneGemStage6());
        lore.add(BlissItems.createAstraTierOneGemStage6());
        lore.add(BlissItems.createFireTierOneGemStage6());
        lore.add(BlissItems.createLifeTierOneGemStage6());
        lore.add(BlissItems.createSpeedTierOneGemStage6());
        lore.add(BlissItems.createWealthTierOneGemStage6());
        lore.add(BlissItems.createStrengthTierOneGemStage6());
        lore.add(BlissItems.createFluxTierOneGemStage6());
    }

    return (lore.get(this.random.nextInt(lore.size()))).clone();
  }

  ItemStack createItemTarget(int count) {
    ArrayList<ItemStack> lore = new ArrayList<>();
    switch (count) {
      case 0:
        lore.add(BlissItems.createPuffTierTwoGemStage1());
        lore.add(BlissItems.createAstraTierTwoGemStage1());
        lore.add(BlissItems.createFireTierTwoGemStage1());
        lore.add(BlissItems.createLifeTierTwoGemStage1());
        lore.add(BlissItems.createSpeedTierTwoGemStage1());
        lore.add(BlissItems.createWealthTierTwoGemStage1());
        lore.add(BlissItems.createStrengthTierTwoGemStage1());
        lore.add(BlissItems.createFluxTierTwoGemStage1());
        break;
      case 1:
        lore.add(BlissItems.createPuffTierTwoGemStage2());
        lore.add(BlissItems.createAstraTierTwoGemStage2());
        lore.add(BlissItems.createFireTierTwoGemStage2());
        lore.add(BlissItems.createLifeTierTwoGemStage2());
        lore.add(BlissItems.createSpeedTierTwoGemStage2());
        lore.add(BlissItems.createWealthTierTwoGemStage2());
        lore.add(BlissItems.createStrengthTierTwoGemStage2());
        lore.add(BlissItems.createFluxTierTwoGemStage2());
        break;
      case 2:
        lore.add(BlissItems.createPuffTierTwoGemStage3());
        lore.add(BlissItems.createAstraTierTwoGemStage3());
        lore.add(BlissItems.createFireTierTwoGemStage3());
        lore.add(BlissItems.createLifeTierTwoGemStage3());
        lore.add(BlissItems.createSpeedTierTwoGemStage3());
        lore.add(BlissItems.createWealthTierTwoGemStage3());
        lore.add(BlissItems.createStrengthTierTwoGemStage3());
        lore.add(BlissItems.createFluxTierTwoGemStage3());
        break;
      case 3:
        lore.add(BlissItems.createPuffTierTwoGemStage4());
        lore.add(BlissItems.createAstraTierTwoGemStage4());
        lore.add(BlissItems.createFireTierTwoGemStage4());
        lore.add(BlissItems.createLifeTierTwoGemStage4());
        lore.add(BlissItems.createSpeedTierTwoGemStage4());
        lore.add(BlissItems.createWealthTierTwoGemStage4());
        lore.add(BlissItems.createStrengthTierTwoGemStage4());
        lore.add(BlissItems.createFluxTierTwoGemStage4());
        break;
      case 4:
        lore.add(BlissItems.createPuffTierTwoGemStage5());
        lore.add(BlissItems.createAstraTierTwoGemStage5());
        lore.add(BlissItems.createFireTierTwoGemStage5());
        lore.add(BlissItems.createLifeTierTwoGemStage5());
        lore.add(BlissItems.createSpeedTierTwoGemStage5());
        lore.add(BlissItems.createWealthTierTwoGemStage5());
        lore.add(BlissItems.createStrengthTierTwoGemStage5());
        lore.add(BlissItems.createFluxTierTwoGemStage5());
        break;
      case 5:
        lore.add(BlissItems.createPuffTierTwoGemStage6());
        lore.add(BlissItems.createAstraTierTwoGemStage6());
        lore.add(BlissItems.createFireTierTwoGemStage6());
        lore.add(BlissItems.createLifeTierTwoGemStage6());
        lore.add(BlissItems.createSpeedTierTwoGemStage6());
        lore.add(BlissItems.createWealthTierTwoGemStage11());
        lore.add(BlissItems.createStrengthTierTwoGemStage6());
        lore.add(BlissItems.createFluxTierTwoGemStage6());
        break;
      case 6:
        lore.add(BlissItems.createPuffTierTwoGemStage7());
        lore.add(BlissItems.createAstraTierTwoGemStage7());
        lore.add(BlissItems.createFireTierTwoGemStage7());
        lore.add(BlissItems.createLifeTierTwoGemStage7());
        lore.add(BlissItems.createSpeedTierTwoGemStage7());
        lore.add(BlissItems.createWealthTierTwoGemStage6());
        lore.add(BlissItems.createStrengthTierTwoGemStage7());
        lore.add(BlissItems.createFluxTierTwoGemStage7());
        break;
      case 7:
        lore.add(BlissItems.createPuffTierTwoGemStage8());
        lore.add(BlissItems.createAstraTierTwoGemStage8());
        lore.add(BlissItems.createFireTierTwoGemStage8());
        lore.add(BlissItems.createLifeTierTwoGemStage8());
        lore.add(BlissItems.createSpeedTierTwoGemStage8());
        lore.add(BlissItems.createWealthTierTwoGemStage7());
        lore.add(BlissItems.createStrengthTierTwoGemStage8());
        lore.add(BlissItems.createFluxTierTwoGemStage8());
        break;
      case 8:
        lore.add(BlissItems.createPuffTierTwoGemStage9());
        lore.add(BlissItems.createAstraTierTwoGemStage9());
        lore.add(BlissItems.createFireTierTwoGemStage9());
        lore.add(BlissItems.createLifeTierTwoGemStage9());
        lore.add(BlissItems.createSpeedTierTwoGemStage9());
        lore.add(BlissItems.createWealthTierTwoGemStage8());
        lore.add(BlissItems.createStrengthTierTwoGemStage9());
        lore.add(BlissItems.createFluxTierTwoGemStage9());
        break;
      case 9:
        lore.add(BlissItems.createPuffTierTwoGemStage10());
        lore.add(BlissItems.createAstraTierTwoGemStage10());
        lore.add(BlissItems.createFireTierTwoGemStage10());
        lore.add(BlissItems.createLifeTierTwoGemStage10());
        lore.add(BlissItems.createSpeedTierTwoGemStage10());
        lore.add(BlissItems.createWealthTierTwoGemStage9());
        lore.add(BlissItems.createStrengthTierTwoGemStage10());
        lore.add(BlissItems.createFluxTierTwoGemStage10());
        break;
      case 10:
        lore.add(BlissItems.createPuffTierTwoGemStage11());
        lore.add(BlissItems.createAstraTierTwoGemStage11());
        lore.add(BlissItems.createFireTierTwoGemStage11());
        lore.add(BlissItems.createLifeTierTwoGemStage11());
        lore.add(BlissItems.createSpeedTierTwoGemStage11());
        lore.add(BlissItems.createWealthTierTwoGemStage10());
        lore.add(BlissItems.createStrengthTierTwoGemStage11());
        lore.add(BlissItems.createFluxTierTwoGemStage11());
        break;
      default:
        lore.add(BlissItems.createPuffTierTwoGemStage6());
        lore.add(BlissItems.createAstraTierTwoGemStage6());
        lore.add(BlissItems.createFireTierTwoGemStage6());
        lore.add(BlissItems.createLifeTierTwoGemStage6());
        lore.add(BlissItems.createSpeedTierTwoGemStage6());
        lore.add(BlissItems.createWealthTierTwoGemStage11());
        lore.add(BlissItems.createStrengthTierTwoGemStage6());
        lore.add(BlissItems.createFluxTierTwoGemStage6());
    }

    return (lore.get(this.random.nextInt(lore.size()))).clone();
  }

  void initialize() {
    this.currentItemListsByIndex.clear();
    this.itemListsByIndex.clear();
    this.currentItemListsByIndex.put(
        0,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage1(),
            BlissItems.createAstraTierOneGemStage1(),
            BlissItems.createFireTierOneGemStage1(),
            BlissItems.createLifeTierOneGemStage1(),
            BlissItems.createSpeedTierOneGemStage1(),
            BlissItems.createWealthTierOneGemStage1(),
            BlissItems.createStrengthTierOneGemStage1(),
            BlissItems.createFluxTierOneGemStage1()));
    this.currentItemListsByIndex.put(
        1,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage2(),
            BlissItems.createAstraTierOneGemStage2(),
            BlissItems.createFireTierOneGemStage2(),
            BlissItems.createLifeTierOneGemStage2(),
            BlissItems.createSpeedTierOneGemStage2(),
            BlissItems.createWealthTierOneGemStage2(),
            BlissItems.createStrengthTierOneGemStage2(),
            BlissItems.createFluxTierOneGemStage2()));
    this.currentItemListsByIndex.put(
        2,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage3(),
            BlissItems.createAstraTierOneGemStage3(),
            BlissItems.createFireTierOneGemStage3(),
            BlissItems.createLifeTierOneGemStage3(),
            BlissItems.createSpeedTierOneGemStage3(),
            BlissItems.createWealthTierOneGemStage3(),
            BlissItems.createStrengthTierOneGemStage3(),
            BlissItems.createFluxTierOneGemStage3()));
    this.currentItemListsByIndex.put(
        3,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage4(),
            BlissItems.createAstraTierOneGemStage4(),
            BlissItems.createFireTierOneGemStage4(),
            BlissItems.createLifeTierOneGemStage4(),
            BlissItems.createSpeedTierOneGemStage4(),
            BlissItems.createWealthTierOneGemStage4(),
            BlissItems.createStrengthTierOneGemStage4(),
            BlissItems.createFluxTierOneGemStage4()));
    this.currentItemListsByIndex.put(
        4,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage5(),
            BlissItems.createAstraTierOneGemStage5(),
            BlissItems.createFireTierOneGemStage5(),
            BlissItems.createLifeTierOneGemStage5(),
            BlissItems.createSpeedTierOneGemStage5(),
            BlissItems.createWealthTierOneGemStage5(),
            BlissItems.createStrengthTierOneGemStage5(),
            BlissItems.createFluxTierOneGemStage5()));
    this.currentItemListsByIndex.put(
        5,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage6(),
            BlissItems.createAstraTierOneGemStage6(),
            BlissItems.createFireTierOneGemStage6(),
            BlissItems.createLifeTierOneGemStage6(),
            BlissItems.createSpeedTierOneGemStage6(),
            BlissItems.createWealthTierOneGemStage6(),
            BlissItems.createStrengthTierOneGemStage6(),
            BlissItems.createFluxTierOneGemStage6()));
    this.currentItemListsByIndex.put(
        6,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage7(),
            BlissItems.createAstraTierOneGemStage7(),
            BlissItems.createFireTierOneGemStage7(),
            BlissItems.createLifeTierOneGemStage7(),
            BlissItems.createSpeedTierOneGemStage7(),
            BlissItems.createWealthTierOneGemStage7(),
            BlissItems.createStrengthTierOneGemStage7(),
            BlissItems.createFluxTierOneGemStage7()));
    this.currentItemListsByIndex.put(
        7,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage8(),
            BlissItems.createAstraTierOneGemStage8(),
            BlissItems.createFireTierOneGemStage8(),
            BlissItems.createLifeTierOneGemStage8(),
            BlissItems.createSpeedTierOneGemStage8(),
            BlissItems.createWealthTierOneGemStage8(),
            BlissItems.createStrengthTierOneGemStage8(),
            BlissItems.createFluxTierOneGemStage8()));
    this.currentItemListsByIndex.put(
        8,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage9(),
            BlissItems.createAstraTierOneGemStage9(),
            BlissItems.createFireTierOneGemStage9(),
            BlissItems.createLifeTierOneGemStage9(),
            BlissItems.createSpeedTierOneGemStage9(),
            BlissItems.createWealthTierOneGemStage9(),
            BlissItems.createStrengthTierOneGemStage9(),
            BlissItems.createFluxTierOneGemStage9()));
    this.currentItemListsByIndex.put(
        9,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage10(),
            BlissItems.createAstraTierOneGemStage10(),
            BlissItems.createFireTierOneGemStage10(),
            BlissItems.createLifeTierOneGemStage10(),
            BlissItems.createSpeedTierOneGemStage10(),
            BlissItems.createWealthTierOneGemStage10(),
            BlissItems.createStrengthTierOneGemStage10(),
            BlissItems.createFluxTierOneGemStage10()));
    this.currentItemListsByIndex.put(
        10,
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage11(),
            BlissItems.createAstraTierOneGemStage11(),
            BlissItems.createFireTierOneGemStage11(),
            BlissItems.createLifeTierOneGemStage11(),
            BlissItems.createSpeedTierOneGemStage11(),
            BlissItems.createWealthTierOneGemStage11(),
            BlissItems.createStrengthTierOneGemStage11(),
            BlissItems.createFluxTierOneGemStage11()));
    this.itemListsByIndex.put(
        0,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage1(),
            BlissItems.createAstraTierTwoGemStage1(),
            BlissItems.createFireTierTwoGemStage1(),
            BlissItems.createLifeTierTwoGemStage1(),
            BlissItems.createSpeedTierTwoGemStage1(),
            BlissItems.createWealthTierTwoGemStage1(),
            BlissItems.createStrengthTierTwoGemStage1(),
            BlissItems.createFluxTierTwoGemStage1()));
    this.itemListsByIndex.put(
        1,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage2(),
            BlissItems.createAstraTierTwoGemStage2(),
            BlissItems.createFireTierTwoGemStage2(),
            BlissItems.createLifeTierTwoGemStage2(),
            BlissItems.createSpeedTierTwoGemStage2(),
            BlissItems.createWealthTierTwoGemStage2(),
            BlissItems.createStrengthTierTwoGemStage2(),
            BlissItems.createFluxTierTwoGemStage2()));
    this.itemListsByIndex.put(
        2,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage3(),
            BlissItems.createAstraTierTwoGemStage3(),
            BlissItems.createFireTierTwoGemStage3(),
            BlissItems.createLifeTierTwoGemStage3(),
            BlissItems.createSpeedTierTwoGemStage3(),
            BlissItems.createWealthTierTwoGemStage3(),
            BlissItems.createStrengthTierTwoGemStage3(),
            BlissItems.createFluxTierTwoGemStage3()));
    this.itemListsByIndex.put(
        3,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage4(),
            BlissItems.createAstraTierTwoGemStage4(),
            BlissItems.createFireTierTwoGemStage4(),
            BlissItems.createLifeTierTwoGemStage4(),
            BlissItems.createSpeedTierTwoGemStage4(),
            BlissItems.createWealthTierTwoGemStage4(),
            BlissItems.createStrengthTierTwoGemStage4(),
            BlissItems.createFluxTierTwoGemStage4()));
    this.itemListsByIndex.put(
        4,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage5(),
            BlissItems.createAstraTierTwoGemStage5(),
            BlissItems.createFireTierTwoGemStage5(),
            BlissItems.createLifeTierTwoGemStage5(),
            BlissItems.createSpeedTierTwoGemStage5(),
            BlissItems.createWealthTierTwoGemStage5(),
            BlissItems.createStrengthTierTwoGemStage5(),
            BlissItems.createFluxTierTwoGemStage5()));
    this.itemListsByIndex.put(
        5,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage6(),
            BlissItems.createAstraTierTwoGemStage6(),
            BlissItems.createFireTierTwoGemStage6(),
            BlissItems.createLifeTierTwoGemStage6(),
            BlissItems.createSpeedTierTwoGemStage6(),
            BlissItems.createWealthTierTwoGemStage11(),
            BlissItems.createStrengthTierTwoGemStage6(),
            BlissItems.createFluxTierTwoGemStage6()));
    this.itemListsByIndex.put(
        6,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage7(),
            BlissItems.createAstraTierTwoGemStage7(),
            BlissItems.createFireTierTwoGemStage7(),
            BlissItems.createLifeTierTwoGemStage7(),
            BlissItems.createSpeedTierTwoGemStage7(),
            BlissItems.createWealthTierTwoGemStage6(),
            BlissItems.createStrengthTierTwoGemStage7(),
            BlissItems.createFluxTierTwoGemStage7()));
    this.itemListsByIndex.put(
        7,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage8(),
            BlissItems.createAstraTierTwoGemStage8(),
            BlissItems.createFireTierTwoGemStage8(),
            BlissItems.createLifeTierTwoGemStage8(),
            BlissItems.createSpeedTierTwoGemStage8(),
            BlissItems.createWealthTierTwoGemStage7(),
            BlissItems.createStrengthTierTwoGemStage8(),
            BlissItems.createFluxTierTwoGemStage8()));
    this.itemListsByIndex.put(
        8,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage9(),
            BlissItems.createAstraTierTwoGemStage9(),
            BlissItems.createFireTierTwoGemStage9(),
            BlissItems.createLifeTierTwoGemStage9(),
            BlissItems.createSpeedTierTwoGemStage9(),
            BlissItems.createWealthTierTwoGemStage8(),
            BlissItems.createStrengthTierTwoGemStage9(),
            BlissItems.createFluxTierTwoGemStage9()));
    this.itemListsByIndex.put(
        9,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage10(),
            BlissItems.createAstraTierTwoGemStage10(),
            BlissItems.createFireTierTwoGemStage10(),
            BlissItems.createLifeTierTwoGemStage10(),
            BlissItems.createSpeedTierTwoGemStage10(),
            BlissItems.createWealthTierTwoGemStage9(),
            BlissItems.createStrengthTierTwoGemStage10(),
            BlissItems.createFluxTierTwoGemStage10()));
    this.itemListsByIndex.put(
        10,
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage11(),
            BlissItems.createAstraTierTwoGemStage11(),
            BlissItems.createFireTierTwoGemStage11(),
            BlissItems.createLifeTierTwoGemStage11(),
            BlissItems.createSpeedTierTwoGemStage11(),
            BlissItems.createWealthTierTwoGemStage10(),
            BlissItems.createStrengthTierTwoGemStage11(),
            BlissItems.createFluxTierTwoGemStage11()));
  }

  void completeAbilityAction() {
    this.secondaryCountsByIndex.clear();
    this.handleAbilityAction(
        0,
        BlissItems.createPuffTierOneGemStage1(),
        BlissItems.createAstraTierOneGemStage1(),
        BlissItems.createFireTierOneGemStage1(),
        BlissItems.createLifeTierOneGemStage1(),
        BlissItems.createSpeedTierOneGemStage1(),
        BlissItems.createWealthTierOneGemStage1(),
        BlissItems.createStrengthTierOneGemStage1(),
        BlissItems.createFluxTierOneGemStage1(),
        BlissItems.createPuffTierTwoGemStage1(),
        BlissItems.createAstraTierTwoGemStage1(),
        BlissItems.createFireTierTwoGemStage1(),
        BlissItems.createLifeTierTwoGemStage1(),
        BlissItems.createSpeedTierTwoGemStage1(),
        BlissItems.createWealthTierTwoGemStage1(),
        BlissItems.createStrengthTierTwoGemStage1(),
        BlissItems.createFluxTierTwoGemStage1());
    this.handleAbilityAction(
        1,
        BlissItems.createPuffTierOneGemStage2(),
        BlissItems.createAstraTierOneGemStage2(),
        BlissItems.createFireTierOneGemStage2(),
        BlissItems.createLifeTierOneGemStage2(),
        BlissItems.createSpeedTierOneGemStage2(),
        BlissItems.createWealthTierOneGemStage2(),
        BlissItems.createStrengthTierOneGemStage2(),
        BlissItems.createFluxTierOneGemStage2(),
        BlissItems.createPuffTierTwoGemStage2(),
        BlissItems.createAstraTierTwoGemStage2(),
        BlissItems.createFireTierTwoGemStage2(),
        BlissItems.createLifeTierTwoGemStage2(),
        BlissItems.createSpeedTierTwoGemStage2(),
        BlissItems.createWealthTierTwoGemStage2(),
        BlissItems.createStrengthTierTwoGemStage2(),
        BlissItems.createFluxTierTwoGemStage2());
    this.handleAbilityAction(
        2,
        BlissItems.createPuffTierOneGemStage3(),
        BlissItems.createAstraTierOneGemStage3(),
        BlissItems.createFireTierOneGemStage3(),
        BlissItems.createLifeTierOneGemStage3(),
        BlissItems.createSpeedTierOneGemStage3(),
        BlissItems.createWealthTierOneGemStage3(),
        BlissItems.createStrengthTierOneGemStage3(),
        BlissItems.createFluxTierOneGemStage3(),
        BlissItems.createPuffTierTwoGemStage3(),
        BlissItems.createAstraTierTwoGemStage3(),
        BlissItems.createFireTierTwoGemStage3(),
        BlissItems.createLifeTierTwoGemStage3(),
        BlissItems.createSpeedTierTwoGemStage3(),
        BlissItems.createWealthTierTwoGemStage3(),
        BlissItems.createStrengthTierTwoGemStage3(),
        BlissItems.createFluxTierTwoGemStage3());
    this.handleAbilityAction(
        3,
        BlissItems.createPuffTierOneGemStage4(),
        BlissItems.createAstraTierOneGemStage4(),
        BlissItems.createFireTierOneGemStage4(),
        BlissItems.createLifeTierOneGemStage4(),
        BlissItems.createSpeedTierOneGemStage4(),
        BlissItems.createWealthTierOneGemStage4(),
        BlissItems.createStrengthTierOneGemStage4(),
        BlissItems.createFluxTierOneGemStage4(),
        BlissItems.createPuffTierTwoGemStage4(),
        BlissItems.createAstraTierTwoGemStage4(),
        BlissItems.createFireTierTwoGemStage4(),
        BlissItems.createLifeTierTwoGemStage4(),
        BlissItems.createSpeedTierTwoGemStage4(),
        BlissItems.createWealthTierTwoGemStage4(),
        BlissItems.createStrengthTierTwoGemStage4(),
        BlissItems.createFluxTierTwoGemStage4());
    this.handleAbilityAction(
        4,
        BlissItems.createPuffTierOneGemStage5(),
        BlissItems.createAstraTierOneGemStage5(),
        BlissItems.createFireTierOneGemStage5(),
        BlissItems.createLifeTierOneGemStage5(),
        BlissItems.createSpeedTierOneGemStage5(),
        BlissItems.createWealthTierOneGemStage5(),
        BlissItems.createStrengthTierOneGemStage5(),
        BlissItems.createFluxTierOneGemStage5(),
        BlissItems.createPuffTierTwoGemStage5(),
        BlissItems.createAstraTierTwoGemStage5(),
        BlissItems.createFireTierTwoGemStage5(),
        BlissItems.createLifeTierTwoGemStage5(),
        BlissItems.createSpeedTierTwoGemStage5(),
        BlissItems.createWealthTierTwoGemStage5(),
        BlissItems.createStrengthTierTwoGemStage5(),
        BlissItems.createFluxTierTwoGemStage5());
    this.handleAbilityAction(
        5,
        BlissItems.createPuffTierOneGemStage6(),
        BlissItems.createAstraTierOneGemStage6(),
        BlissItems.createFireTierOneGemStage6(),
        BlissItems.createLifeTierOneGemStage6(),
        BlissItems.createSpeedTierOneGemStage6(),
        BlissItems.createWealthTierOneGemStage6(),
        BlissItems.createStrengthTierOneGemStage6(),
        BlissItems.createFluxTierOneGemStage6(),
        BlissItems.createPuffTierTwoGemStage6(),
        BlissItems.createAstraTierTwoGemStage6(),
        BlissItems.createFireTierTwoGemStage6(),
        BlissItems.createLifeTierTwoGemStage6(),
        BlissItems.createSpeedTierTwoGemStage6(),
        BlissItems.createWealthTierTwoGemStage11(),
        BlissItems.createStrengthTierTwoGemStage6(),
        BlissItems.createFluxTierTwoGemStage6());
    this.handleAbilityAction(
        6,
        BlissItems.createPuffTierOneGemStage7(),
        BlissItems.createAstraTierOneGemStage7(),
        BlissItems.createFireTierOneGemStage7(),
        BlissItems.createLifeTierOneGemStage7(),
        BlissItems.createSpeedTierOneGemStage7(),
        BlissItems.createWealthTierOneGemStage7(),
        BlissItems.createStrengthTierOneGemStage7(),
        BlissItems.createFluxTierOneGemStage7(),
        BlissItems.createPuffTierTwoGemStage7(),
        BlissItems.createAstraTierTwoGemStage7(),
        BlissItems.createFireTierTwoGemStage7(),
        BlissItems.createLifeTierTwoGemStage7(),
        BlissItems.createSpeedTierTwoGemStage7(),
        BlissItems.createWealthTierTwoGemStage6(),
        BlissItems.createStrengthTierTwoGemStage7(),
        BlissItems.createFluxTierTwoGemStage7());
    this.handleAbilityAction(
        7,
        BlissItems.createPuffTierOneGemStage8(),
        BlissItems.createAstraTierOneGemStage8(),
        BlissItems.createFireTierOneGemStage8(),
        BlissItems.createLifeTierOneGemStage8(),
        BlissItems.createSpeedTierOneGemStage8(),
        BlissItems.createWealthTierOneGemStage8(),
        BlissItems.createStrengthTierOneGemStage8(),
        BlissItems.createFluxTierOneGemStage8(),
        BlissItems.createPuffTierTwoGemStage8(),
        BlissItems.createAstraTierTwoGemStage8(),
        BlissItems.createFireTierTwoGemStage8(),
        BlissItems.createLifeTierTwoGemStage8(),
        BlissItems.createSpeedTierTwoGemStage8(),
        BlissItems.createWealthTierTwoGemStage7(),
        BlissItems.createStrengthTierTwoGemStage8(),
        BlissItems.createFluxTierTwoGemStage8());
    this.handleAbilityAction(
        8,
        BlissItems.createPuffTierOneGemStage9(),
        BlissItems.createAstraTierOneGemStage9(),
        BlissItems.createFireTierOneGemStage9(),
        BlissItems.createLifeTierOneGemStage9(),
        BlissItems.createSpeedTierOneGemStage9(),
        BlissItems.createWealthTierOneGemStage9(),
        BlissItems.createStrengthTierOneGemStage9(),
        BlissItems.createFluxTierOneGemStage9(),
        BlissItems.createPuffTierTwoGemStage9(),
        BlissItems.createAstraTierTwoGemStage9(),
        BlissItems.createFireTierTwoGemStage9(),
        BlissItems.createLifeTierTwoGemStage9(),
        BlissItems.createSpeedTierTwoGemStage9(),
        BlissItems.createWealthTierTwoGemStage8(),
        BlissItems.createStrengthTierTwoGemStage9(),
        BlissItems.createFluxTierTwoGemStage9());
    this.handleAbilityAction(
        9,
        BlissItems.createPuffTierOneGemStage10(),
        BlissItems.createAstraTierOneGemStage10(),
        BlissItems.createFireTierOneGemStage10(),
        BlissItems.createLifeTierOneGemStage10(),
        BlissItems.createSpeedTierOneGemStage10(),
        BlissItems.createWealthTierOneGemStage10(),
        BlissItems.createStrengthTierOneGemStage10(),
        BlissItems.createFluxTierOneGemStage10(),
        BlissItems.createPuffTierTwoGemStage10(),
        BlissItems.createAstraTierTwoGemStage10(),
        BlissItems.createFireTierTwoGemStage10(),
        BlissItems.createLifeTierTwoGemStage10(),
        BlissItems.createSpeedTierTwoGemStage10(),
        BlissItems.createWealthTierTwoGemStage9(),
        BlissItems.createStrengthTierTwoGemStage10(),
        BlissItems.createFluxTierTwoGemStage10());
    this.handleAbilityAction(
        10,
        BlissItems.createPuffTierOneGemStage11(),
        BlissItems.createAstraTierOneGemStage11(),
        BlissItems.createFireTierOneGemStage11(),
        BlissItems.createLifeTierOneGemStage11(),
        BlissItems.createSpeedTierOneGemStage11(),
        BlissItems.createWealthTierOneGemStage11(),
        BlissItems.createStrengthTierOneGemStage11(),
        BlissItems.createFluxTierOneGemStage11(),
        BlissItems.createPuffTierTwoGemStage11(),
        BlissItems.createAstraTierTwoGemStage11(),
        BlissItems.createFireTierTwoGemStage11(),
        BlissItems.createLifeTierTwoGemStage11(),
        BlissItems.createSpeedTierTwoGemStage11(),
        BlissItems.createWealthTierTwoGemStage10(),
        BlissItems.createStrengthTierTwoGemStage11(),
        BlissItems.createFluxTierTwoGemStage11());
  }

  void processAbilityState() {
    for (ItemStack item :
        Arrays.asList(
            BlissItems.createPuffTierOneGemStage1(),
            BlissItems.createPuffTierOneGemStage2(),
            BlissItems.createPuffTierOneGemStage3(),
            BlissItems.createPuffTierOneGemStage4(),
            BlissItems.createPuffTierOneGemStage5(),
            BlissItems.createPuffTierOneGemStage6(),
            BlissItems.createPuffTierOneGemStage7(),
            BlissItems.createPuffTierOneGemStage8(),
            BlissItems.createPuffTierOneGemStage9(),
            BlissItems.createPuffTierOneGemStage10(),
            BlissItems.createPuffTierOneGemStage11(),
            BlissItems.createLifeTierOneGemStage1(),
            BlissItems.createLifeTierOneGemStage2(),
            BlissItems.createLifeTierOneGemStage3(),
            BlissItems.createLifeTierOneGemStage4(),
            BlissItems.createLifeTierOneGemStage5(),
            BlissItems.createLifeTierOneGemStage6(),
            BlissItems.createLifeTierOneGemStage7(),
            BlissItems.createLifeTierOneGemStage8(),
            BlissItems.createLifeTierOneGemStage9(),
            BlissItems.createLifeTierOneGemStage10(),
            BlissItems.createLifeTierOneGemStage11(),
            BlissItems.createStrengthTierOneGemStage1(),
            BlissItems.createStrengthTierOneGemStage2(),
            BlissItems.createStrengthTierOneGemStage3(),
            BlissItems.createStrengthTierOneGemStage4(),
            BlissItems.createStrengthTierOneGemStage5(),
            BlissItems.createStrengthTierOneGemStage6(),
            BlissItems.createStrengthTierOneGemStage7(),
            BlissItems.createStrengthTierOneGemStage8(),
            BlissItems.createStrengthTierOneGemStage9(),
            BlissItems.createStrengthTierOneGemStage10(),
            BlissItems.createStrengthTierOneGemStage11(),
            BlissItems.createWealthTierOneGemStage1(),
            BlissItems.createWealthTierOneGemStage2(),
            BlissItems.createWealthTierOneGemStage3(),
            BlissItems.createWealthTierOneGemStage4(),
            BlissItems.createWealthTierOneGemStage5(),
            BlissItems.createWealthTierOneGemStage6(),
            BlissItems.createWealthTierOneGemStage7(),
            BlissItems.createWealthTierOneGemStage8(),
            BlissItems.createWealthTierOneGemStage9(),
            BlissItems.createWealthTierOneGemStage10(),
            BlissItems.createWealthTierOneGemStage11(),
            BlissItems.createSpeedTierOneGemStage1(),
            BlissItems.createSpeedTierOneGemStage2(),
            BlissItems.createSpeedTierOneGemStage3(),
            BlissItems.createSpeedTierOneGemStage4(),
            BlissItems.createSpeedTierOneGemStage5(),
            BlissItems.createSpeedTierOneGemStage6(),
            BlissItems.createSpeedTierOneGemStage7(),
            BlissItems.createSpeedTierOneGemStage8(),
            BlissItems.createSpeedTierOneGemStage9(),
            BlissItems.createSpeedTierOneGemStage10(),
            BlissItems.createSpeedTierOneGemStage11(),
            BlissItems.createFireTierOneGemStage1(),
            BlissItems.createFireTierOneGemStage2(),
            BlissItems.createFireTierOneGemStage3(),
            BlissItems.createFireTierOneGemStage4(),
            BlissItems.createFireTierOneGemStage5(),
            BlissItems.createFireTierOneGemStage6(),
            BlissItems.createFireTierOneGemStage7(),
            BlissItems.createFireTierOneGemStage8(),
            BlissItems.createFireTierOneGemStage9(),
            BlissItems.createFireTierOneGemStage10(),
            BlissItems.createFireTierOneGemStage11(),
            BlissItems.createAstraTierOneGemStage1(),
            BlissItems.createAstraTierOneGemStage2(),
            BlissItems.createAstraTierOneGemStage3(),
            BlissItems.createAstraTierOneGemStage4(),
            BlissItems.createAstraTierOneGemStage5(),
            BlissItems.createAstraTierOneGemStage6(),
            BlissItems.createAstraTierOneGemStage7(),
            BlissItems.createAstraTierOneGemStage8(),
            BlissItems.createAstraTierOneGemStage9(),
            BlissItems.createAstraTierOneGemStage10(),
            BlissItems.createAstraTierOneGemStage11(),
            BlissItems.createFluxTierOneGemStage1(),
            BlissItems.createFluxTierOneGemStage2(),
            BlissItems.createFluxTierOneGemStage3(),
            BlissItems.createFluxTierOneGemStage4(),
            BlissItems.createFluxTierOneGemStage5(),
            BlissItems.createFluxTierOneGemStage6(),
            BlissItems.createFluxTierOneGemStage7(),
            BlissItems.createFluxTierOneGemStage8(),
            BlissItems.createFluxTierOneGemStage9(),
            BlissItems.createFluxTierOneGemStage10(),
            BlissItems.createFluxTierOneGemStage11())) {
      if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
        this.tierOneModelData.add(item.getItemMeta().getCustomModelData());
      }
    }

    for (ItemStack heldItem :
        Arrays.asList(
            BlissItems.createPuffTierTwoGemStage1(),
            BlissItems.createPuffTierTwoGemStage2(),
            BlissItems.createPuffTierTwoGemStage3(),
            BlissItems.createPuffTierTwoGemStage4(),
            BlissItems.createPuffTierTwoGemStage5(),
            BlissItems.createPuffTierTwoGemStage6(),
            BlissItems.createPuffTierTwoGemStage7(),
            BlissItems.createPuffTierTwoGemStage8(),
            BlissItems.createPuffTierTwoGemStage9(),
            BlissItems.createPuffTierTwoGemStage10(),
            BlissItems.createPuffTierTwoGemStage11(),
            BlissItems.createLifeTierTwoGemStage1(),
            BlissItems.createLifeTierTwoGemStage2(),
            BlissItems.createLifeTierTwoGemStage3(),
            BlissItems.createLifeTierTwoGemStage4(),
            BlissItems.createLifeTierTwoGemStage5(),
            BlissItems.createLifeTierTwoGemStage6(),
            BlissItems.createLifeTierTwoGemStage7(),
            BlissItems.createLifeTierTwoGemStage8(),
            BlissItems.createLifeTierTwoGemStage9(),
            BlissItems.createLifeTierTwoGemStage10(),
            BlissItems.createLifeTierTwoGemStage11(),
            BlissItems.createStrengthTierTwoGemStage1(),
            BlissItems.createStrengthTierTwoGemStage2(),
            BlissItems.createStrengthTierTwoGemStage3(),
            BlissItems.createStrengthTierTwoGemStage4(),
            BlissItems.createStrengthTierTwoGemStage5(),
            BlissItems.createStrengthTierTwoGemStage6(),
            BlissItems.createStrengthTierTwoGemStage7(),
            BlissItems.createStrengthTierTwoGemStage8(),
            BlissItems.createStrengthTierTwoGemStage9(),
            BlissItems.createStrengthTierTwoGemStage10(),
            BlissItems.createStrengthTierTwoGemStage11(),
            BlissItems.createWealthTierTwoGemStage1(),
            BlissItems.createWealthTierTwoGemStage2(),
            BlissItems.createWealthTierTwoGemStage3(),
            BlissItems.createWealthTierTwoGemStage4(),
            BlissItems.createWealthTierTwoGemStage5(),
            BlissItems.createWealthTierTwoGemStage11(),
            BlissItems.createWealthTierTwoGemStage6(),
            BlissItems.createWealthTierTwoGemStage7(),
            BlissItems.createWealthTierTwoGemStage8(),
            BlissItems.createWealthTierTwoGemStage9(),
            BlissItems.createWealthTierTwoGemStage10(),
            BlissItems.createSpeedTierTwoGemStage1(),
            BlissItems.createSpeedTierTwoGemStage2(),
            BlissItems.createSpeedTierTwoGemStage3(),
            BlissItems.createSpeedTierTwoGemStage4(),
            BlissItems.createSpeedTierTwoGemStage5(),
            BlissItems.createSpeedTierTwoGemStage6(),
            BlissItems.createSpeedTierTwoGemStage7(),
            BlissItems.createSpeedTierTwoGemStage8(),
            BlissItems.createSpeedTierTwoGemStage9(),
            BlissItems.createSpeedTierTwoGemStage10(),
            BlissItems.createSpeedTierTwoGemStage11(),
            BlissItems.createFireTierTwoGemStage1(),
            BlissItems.createFireTierTwoGemStage2(),
            BlissItems.createFireTierTwoGemStage3(),
            BlissItems.createFireTierTwoGemStage4(),
            BlissItems.createFireTierTwoGemStage5(),
            BlissItems.createFireTierTwoGemStage6(),
            BlissItems.createFireTierTwoGemStage7(),
            BlissItems.createFireTierTwoGemStage8(),
            BlissItems.createFireTierTwoGemStage9(),
            BlissItems.createFireTierTwoGemStage10(),
            BlissItems.createFireTierTwoGemStage11(),
            BlissItems.createAstraTierTwoGemStage1(),
            BlissItems.createAstraTierTwoGemStage2(),
            BlissItems.createAstraTierTwoGemStage3(),
            BlissItems.createAstraTierTwoGemStage4(),
            BlissItems.createAstraTierTwoGemStage5(),
            BlissItems.createAstraTierTwoGemStage6(),
            BlissItems.createAstraTierTwoGemStage7(),
            BlissItems.createAstraTierTwoGemStage8(),
            BlissItems.createAstraTierTwoGemStage9(),
            BlissItems.createAstraTierTwoGemStage10(),
            BlissItems.createAstraTierTwoGemStage11(),
            BlissItems.createFluxTierTwoGemStage1(),
            BlissItems.createFluxTierTwoGemStage2(),
            BlissItems.createFluxTierTwoGemStage3(),
            BlissItems.createFluxTierTwoGemStage4(),
            BlissItems.createFluxTierTwoGemStage5(),
            BlissItems.createFluxTierTwoGemStage6(),
            BlissItems.createFluxTierTwoGemStage7(),
            BlissItems.createFluxTierTwoGemStage8(),
            BlissItems.createFluxTierTwoGemStage9(),
            BlissItems.createFluxTierTwoGemStage10(),
            BlissItems.createFluxTierTwoGemStage11())) {
      if (heldItem.hasItemMeta() && heldItem.getItemMeta().hasCustomModelData()) {
        this.tierTwoModelData.add(heldItem.getItemMeta().getCustomModelData());
      }
    }
  }

  void handleAbilityAction(int count, ItemStack... item) {
    for (ItemStack heldItem : item) {
      if (heldItem != null && heldItem.hasItemMeta() && heldItem.getItemMeta().hasCustomModelData()) {
        this.secondaryCountsByIndex.put(heldItem.getItemMeta().getCustomModelData(), count);
      }
    }
  }

  static boolean shouldGetRandomDifferentGemT2ForReturn(ItemStack item, ItemStack heldItem) {
    return BlissItems.isMatchingItem(heldItem, item);
  }

  static boolean shouldGetRandomDifferentGemT1ForReturn(ItemStack item, ItemStack heldItem) {
    return BlissItems.isMatchingItem(heldItem, item);
  }

  static {
    UPGRADER_MANAGER_ID = ChatColor.WHITE + "                                 ";
    entries = Arrays.asList(9, 10, 11, 12, 14, 15, 16, 17);
    pendingCountsByIndex = new HashMap<>();
    pendingCountsByIndex.put(9, 10003);
    pendingCountsByIndex.put(10, 10001);
    pendingCountsByIndex.put(11, 10006);
    pendingCountsByIndex.put(12, 9999);
    pendingCountsByIndex.put(14, 10004);
    pendingCountsByIndex.put(15, 10005);
    pendingCountsByIndex.put(16, 10002);
    pendingCountsByIndex.put(17, 10000);
  }
}
