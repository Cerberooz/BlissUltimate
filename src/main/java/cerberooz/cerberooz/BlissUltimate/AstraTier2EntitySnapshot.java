package cerberooz.cerberooz.BlissUltimate;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;

class AstraTier2EntitySnapshot {
  final EntityType entityType;
  final String displayText;
  final EntitySnapshot entitySnapshot;
  static final String COULD_NOT_CREATE_ENTITY_SNAPSHOT_FOR_ID;

  public AstraTier2EntitySnapshot(Entity entity) {
    this.entityType = entity.getType();
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
