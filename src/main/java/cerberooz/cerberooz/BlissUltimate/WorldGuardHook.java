package cerberooz.cerberooz.BlissUltimate;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class WorldGuardHook {
  private final RegionQuery regionQuery =
      WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

  public WorldGuardHook() {}

  public boolean isPvpDenied(Location location) {
    com.sk89q.worldedit.util.Location adaptedLocation = BukkitAdapter.adapt(location);
    State state = this.regionQuery.queryState(adaptedLocation, null, Flags.PVP);
    return state == State.DENY;
  }

  public boolean isPvpDenied(Player player) {
    com.sk89q.worldedit.util.Location adaptedLocation = BukkitAdapter.adapt(player.getLocation());
    LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
    State state = this.regionQuery.queryState(adaptedLocation, localPlayer, Flags.PVP);
    return state == State.DENY;
  }
}
