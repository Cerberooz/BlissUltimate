package cerberooz.cerberooz.BlissUltimate;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public class InvisibleKillListener implements Listener {
  public static boolean active;

  public InvisibleKillListener(Plugin plugin) {
    active = ConfigValueCache.getBoolean(plugin, "InvisibleKill");
  }

  @EventHandler
  void onPlayerDeath(PlayerDeathEvent event) {
    if (active) {
      Player player = event.getPlayer();
      if (player.getKiller() != null) {
        if (player.getKiller().isInvisible()
            || player.getKiller().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
          String name = player.getName();
          event.setDeathMessage(name + " was killed by " + ChatColor.MAGIC + "invisguy");
        } else if (player.getPlayer().getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
          String message = player.getName();
          event.setDeathMessage(
              ChatColor.MAGIC + "invisguy" + ChatColor.RESET + " was killed by " + message);
        } else if (player.getPlayer().getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)
            && player.getKiller().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
          event.setDeathMessage(
              ChatColor.MAGIC
                  + "invisguy "
                  + ChatColor.RESET
                  + "was killed by "
                  + ChatColor.MAGIC
                  + "invisguy");
        }
      }
    }
  }

  public static String formatDisplayText(Player player) {
    if (player == null) {
      return "Unknown";
    } else {
      return player.hasPotionEffect(PotionEffectType.INVISIBILITY)
          ? org.bukkit.ChatColor.MAGIC + "invisguy" + org.bukkit.ChatColor.RESET
          : player.getName();
    }
  }
}
