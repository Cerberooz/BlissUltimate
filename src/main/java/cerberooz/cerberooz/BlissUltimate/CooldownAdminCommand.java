package cerberooz.cerberooz.BlissUltimate;

import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CooldownAdminCommand implements CommandExecutor {
  final PuffTier1Gem puffTier1Gem;
  final PuffTier2Gem puffTier2Gem;
  final StrengthGemRevamp strengthGemRevamp;
  final LifeTier1Gem lifeTier1Gem;
  final LifeTier2Gem lifeTier2Gem;
  final FireTier1Gem fireTier1Gem;
  final FireTier2Gem fireTier2Gem;
  final WealthTier1Gem wealthTier1Gem;
  final WealthTier2Gem wealthTier2Gem;
  final SpeedTier2Gem speedTier2Gem;
  final AstraTier2Gem astraTier2Gem;
  final FluxTier2Gem fluxTier2Gem;
  final AstraTier1Gem astraTier1Gem;
  final SpeedTier1Gem speedTier1Gem;
  final FluxTier1Gem fluxTier1Gem;
  final HereticGem hereticGem;
  final AuratusGem auratusGem;

  public CooldownAdminCommand(
      PuffTier1Gem puffTier1Gem,
      PuffTier2Gem puffTier2Gem,
      StrengthGemRevamp strengthGemRevamp,
      LifeTier1Gem lifeTier1Gem,
      LifeTier2Gem lifeTier2Gem,
      FireTier1Gem fireTier1Gem,
      FireTier2Gem fireTier2Gem,
      WealthTier1Gem wealthTier1Gem,
      WealthTier2Gem wealthTier2Gem,
      SpeedTier2Gem speedTier2Gem,
      RestorationRitual restorationRitual,
      AstraTier2Gem astraTier2Gem,
      FluxTier2Gem fluxTier2Gem,
      AstraTier2Gem currentAstraTier2Gem,
      AstraTier1Gem astraTier1Gem,
      SpeedTier1Gem speedTier1Gem,
      FluxTier1Gem fluxTier1Gem,
      HereticGem hereticGem,
      AuratusGem auratusGem) {
    this.puffTier1Gem = puffTier1Gem;
    this.puffTier2Gem = puffTier2Gem;
    this.strengthGemRevamp = strengthGemRevamp;
    this.lifeTier1Gem = lifeTier1Gem;
    this.lifeTier2Gem = lifeTier2Gem;
    this.fireTier1Gem = fireTier1Gem;
    this.fireTier2Gem = fireTier2Gem;
    this.wealthTier1Gem = wealthTier1Gem;
    this.wealthTier2Gem = wealthTier2Gem;
    this.speedTier2Gem = speedTier2Gem;
    this.astraTier2Gem = astraTier2Gem;
    this.fluxTier2Gem = fluxTier2Gem;
    this.astraTier1Gem = astraTier1Gem;
    this.speedTier1Gem = speedTier1Gem;
    this.fluxTier1Gem = fluxTier1Gem;
    this.hereticGem = hereticGem;
    this.auratusGem = auratusGem;
  }

  public boolean onCommand(CommandSender commandSender, Command command, String argument, String[] message) {
    Player player;
    if (message.length == 1) {
      player = Bukkit.getPlayer(message[0]);
      if (player == null) {
        commandSender.sendMessage(ChatColor.RED + "Player not found.");
        return true;
      }
    } else {
      if (!(commandSender instanceof Player targetPlayer)) {
        commandSender.sendMessage(ChatColor.RED + "Usage: /cooldown <player>");
        return true;
      }

      player = targetPlayer;
    }

    UUID playerId = player.getUniqueId();
    CooldownService.clearActiveCooldowns(playerId);
    this.lifeTier1Gem.getCache().invalidate(playerId);
    this.lifeTier2Gem.getSecondaryCache().invalidate(playerId);
    this.lifeTier2Gem.getCache().invalidate(playerId);
    this.lifeTier2Gem.getActiveCache().invalidate(playerId);
    this.fireTier2Gem.activateTargetAbility(player);
    this.astraTier2Gem.activateTrackedAbility(player);
    this.speedTier2Gem.activatePendingAbility(player);
    this.wealthTier2Gem.updatePrimaryBossBar(player);
    this.fluxTier2Gem.activateCachedAbility(player);
    this.astraTier1Gem.activatePrimaryAbility(player);
    this.speedTier1Gem.updateAbilityState(player);
    this.fireTier1Gem.activateAbility(player);
    this.fluxTier1Gem.activateAbility(player);
    this.puffTier2Gem.spawnAbilityEffects(player);
    this.strengthGemRevamp.resetCooldowns(player);
    if (Bliss.getInstance().fireGemRevamp != null) {
      Bliss.getInstance().fireGemRevamp.resetCooldowns(player);
    }
    if (Bliss.getInstance().puffGemRevamp != null) {
      Bliss.getInstance().puffGemRevamp.resetCooldowns(player);
    }
    this.wealthTier2Gem.updatePrimaryBossBar(player);
    this.hereticGem.activateAbility(player);
    AuratusGem.resetAbilityState(player);
    if (commandSender instanceof Player sourcePlayer) {
      if (player.equals(sourcePlayer)) {
        sourcePlayer.sendMessage(ChatColor.GREEN + "Your cooldowns have been reset.");
      } else {
        sourcePlayer.sendMessage(ChatColor.GREEN + "Cooldowns for " + player.getName() + " have been reset.");
        player.sendMessage(
            ChatColor.YELLOW + "All your cooldowns have been reset by " + sourcePlayer.getName() + ".");
      }
    }

    return true;
  }
}
