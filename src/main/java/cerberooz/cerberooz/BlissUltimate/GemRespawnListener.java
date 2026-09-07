package cerberooz.cerberooz.BlissUltimate;

import java.util.HashMap;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class GemRespawnListener implements Listener {
  Bliss plugin;
  HashMap<Player, UUID> puffTierOneRespawns = new HashMap<>();
  HashMap<Player, UUID> puffTierTwoRespawns = new HashMap<>();
  HashMap<Player, UUID> lifeTierOneRespawns = new HashMap<>();
  HashMap<Player, UUID> lifeTierTwoRespawns = new HashMap<>();
  HashMap<Player, UUID> astraTierOneRespawns = new HashMap<>();
  HashMap<Player, UUID> astraTierTwoRespawns = new HashMap<>();
  HashMap<Player, UUID> strengthTierOneRespawns = new HashMap<>();
  HashMap<Player, UUID> strengthTierTwoRespawns = new HashMap<>();
  HashMap<Player, UUID> wealthTierOneRespawns = new HashMap<>();
  HashMap<Player, UUID> wealthTierTwoRespawns = new HashMap<>();
  HashMap<Player, UUID> fluxTierOneRespawns = new HashMap<>();
  HashMap<Player, UUID> fluxTierTwoRespawns = new HashMap<>();
  HashMap<Player, UUID> fireTierOneRespawns = new HashMap<>();
  HashMap<Player, UUID> fireTierTwoRespawns = new HashMap<>();
  HashMap<Player, UUID> speedTierOneRespawns = new HashMap<>();
  HashMap<Player, UUID> speedTierTwoRespawns = new HashMap<>();

  public GemRespawnListener(Bliss bliss) {
    this.plugin = bliss;
  }

  boolean isGemItem(Player player, ItemStack item) {
    return player.getInventory().contains(item)
        || player.getInventory().getItemInOffHand() != null
            && player.getInventory().getItemInOffHand().isSimilar(item);
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    if (this.isGemItem(player, BlissItems.createPuffTierOneGemStage6())) {
      this.puffTierOneRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createLifeTierOneGemStage6())) {
      this.lifeTierOneRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createAstraTierOneGemStage6())) {
      this.astraTierOneRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createStrengthTierOneGemStage6())) {
      this.strengthTierOneRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createFluxTierOneGemStage6())) {
      this.fluxTierOneRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createSpeedTierOneGemStage6())) {
      this.speedTierOneRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createFireTierOneGemStage6())) {
      this.fireTierOneRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createWealthTierOneGemStage6())) {
      this.wealthTierOneRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createPuffTierTwoGemStage6())) {
      this.puffTierTwoRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createLifeTierTwoGemStage6())) {
      this.lifeTierTwoRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createAstraTierTwoGemStage6())) {
      this.astraTierTwoRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createStrengthTierTwoGemStage6())) {
      this.strengthTierTwoRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createSpeedTierTwoGemStage6())) {
      this.speedTierTwoRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createFireTierTwoGemStage6())) {
      this.fireTierTwoRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createWealthTierTwoGemStage11())) {
      this.wealthTierTwoRespawns.put(player, player.getUniqueId());
    } else if (this.isGemItem(player, BlissItems.createFluxTierTwoGemStage6())) {
      this.fluxTierTwoRespawns.put(player, player.getUniqueId());
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    if (this.puffTierOneRespawns.containsKey(player)) {
      this.puffTierOneRespawns.remove(player);
      player.getInventory()
          .addItem(BlissItems.createPuffTierOneGemStage6());
    } else if (this.lifeTierOneRespawns.containsKey(player)) {
      this.lifeTierOneRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createLifeTierOneGemStage6());
    } else if (this.astraTierOneRespawns.containsKey(player)) {
      this.astraTierOneRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createAstraTierOneGemStage6());
    } else if (this.strengthTierOneRespawns.containsKey(player)) {
      this.strengthTierOneRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createStrengthTierOneGemStage6());
    } else if (this.fluxTierOneRespawns.containsKey(player)) {
      this.fluxTierOneRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createFluxTierOneGemStage6());
    } else if (this.speedTierOneRespawns.containsKey(player)) {
      this.speedTierOneRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createSpeedTierOneGemStage6());
    } else if (this.fireTierOneRespawns.containsKey(player)) {
      this.fireTierOneRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createFireTierOneGemStage6());
    } else if (this.wealthTierOneRespawns.containsKey(player)) {
      this.wealthTierOneRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createWealthTierOneGemStage6());
    } else if (this.puffTierTwoRespawns.containsKey(player)) {
      this.puffTierTwoRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createPuffTierTwoGemStage6());
    } else if (this.lifeTierTwoRespawns.containsKey(player)) {
      this.lifeTierTwoRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createLifeTierTwoGemStage6());
    } else if (this.astraTierTwoRespawns.containsKey(player)) {
      this.astraTierTwoRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createAstraTierTwoGemStage6());
    } else if (this.speedTierTwoRespawns.containsKey(player)) {
      this.speedTierTwoRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createSpeedTierTwoGemStage6());
    } else if (this.fireTierTwoRespawns.containsKey(player)) {
      this.fireTierTwoRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createFireTierTwoGemStage6());
    } else if (this.wealthTierTwoRespawns.containsKey(player)) {
      this.wealthTierTwoRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createWealthTierTwoGemStage11());
    } else if (this.fluxTierTwoRespawns.containsKey(player)) {
      this.fluxTierTwoRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createFluxTierTwoGemStage6());
    } else if (this.strengthTierTwoRespawns.containsKey(player)) {
      this.strengthTierTwoRespawns.remove(player);
      player.getInventory().addItem(BlissItems.createStrengthTierTwoGemStage6());
    }
  }
}
