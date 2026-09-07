package cerberooz.cerberooz.BlissUltimate;

import java.util.UUID;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Vector;

final class MovingDagger {
  final UUID playerId;
  final ItemDisplay itemDisplay;
  final Vector direction;
  int durationTicks;

  MovingDagger(UUID playerId, ItemDisplay itemDisplay, Vector direction) {
    this.playerId = playerId;
    this.itemDisplay = itemDisplay;
    this.direction = direction;
  }
}
