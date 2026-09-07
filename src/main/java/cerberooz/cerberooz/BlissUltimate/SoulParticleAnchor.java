package cerberooz.cerberooz.BlissUltimate;

import org.bukkit.Location;
import org.bukkit.Particle;

final class SoulParticleAnchor {
  final Location anchorLocation;
  final Particle particle;

  SoulParticleAnchor(Location location, Particle particle) {
    this.anchorLocation = location;
    this.particle = particle;
  }
}
