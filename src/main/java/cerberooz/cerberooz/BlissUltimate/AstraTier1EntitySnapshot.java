package cerberooz.cerberooz.BlissUltimate;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;

class AstraTier1EntitySnapshot {
  final String displayText;
  final EntitySnapshot entitySnapshot;
  static final String COULD_NOT_CREATE_ENTITY_SNAPSHOT_FOR_ID;

  public AstraTier1EntitySnapshot(Entity entity) {
    this.displayText = formatDisplayText(entity);
    this.entitySnapshot = entity.createSnapshot();
    if (this.entitySnapshot == null) {
      throw new IllegalStateException(COULD_NOT_CREATE_ENTITY_SNAPSHOT_FOR_ID + entity.getType());
    }
  }

  static String formatDisplayText(Entity entity) {
    return entity.getCustomName() != null
        ? entity.getCustomName()
        : entity.getType().name().toLowerCase().replace("_", " ");
  }

  public Entity resolveEntity(Location location) {
    try {
      return this.entitySnapshot.createEntity(location);
    } catch (Exception exception) {
      Bliss.getInstance()
          .getLogger()
          .log(java.util.logging.Level.WARNING, "Could not recreate an Astra entity", exception);
      return null;
    }
  }

  static {
    COULD_NOT_CREATE_ENTITY_SNAPSHOT_FOR_ID = "Could not create entity snapshot for ";
  }
}
