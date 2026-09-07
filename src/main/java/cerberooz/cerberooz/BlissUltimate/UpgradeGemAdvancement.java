package cerberooz.cerberooz.BlissUltimate;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Range;

public class UpgradeGemAdvancement extends BaseAdvancement {
  static final String UPGRADE_NAME_MARKER;

  public UpgradeGemAdvancement(
      Bliss bliss,
      String text,
      AdvancementDisplay advancementDisplay,
      Advancement advancement,
      @Range(from = 1L, to = 2147483647L) int count) {
    super(text, advancementDisplay, advancement, count);
    this.registerEvent(PlayerInteractEvent.class, this::onPlayerInteract);
  }

  void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();

    try {
      if (this.isGranted(player)) {
        return;
      }
    } catch (Exception exception) {
      return;
    }

    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    boolean enabled = this.isGemItem(mainHandItem);
    if (enabled) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR
          || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        try {
          this.incrementProgression(player);
        } catch (Exception ignored) {
          // Failure is non-fatal; normal event and lifecycle processing continues.
        }
      }
    }
  }

  boolean isGemItem(ItemStack item) {
    if (item == null || item.getType() != Material.PLAYER_HEAD) {
      return false;
    }

    if (item.getItemMeta() == null) {
      return false;
    }

    String displayName = item.getItemMeta().getDisplayName();
    return displayName != null && displayName.contains(UPGRADE_NAME_MARKER);
  }

  static {
    UPGRADE_NAME_MARKER = "ᴜᴘɢʀᴀᴅᴇʀ";
  }
}
