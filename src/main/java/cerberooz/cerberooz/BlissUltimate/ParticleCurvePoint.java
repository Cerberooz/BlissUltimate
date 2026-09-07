package cerberooz.cerberooz.BlissUltimate;

import org.bukkit.Location;
import org.bukkit.util.Vector;

class ParticleCurvePoint {
  final Location anchorLocation;
  final Vector activeDirection;
  final Vector direction;
  final Vector secondaryDirection;
  final double distanceThreshold;
  final double effectRadius;
  double effectScale = 0.0;

  ParticleCurvePoint(
      Location location, Vector direction, Vector offset, Vector velocity, double value, double distance) {
    this.anchorLocation = location;
    this.activeDirection = direction;
    this.direction = offset;
    this.secondaryDirection = velocity;
    this.distanceThreshold = value;
    this.effectRadius = distance;
  }
}
