package cerberooz.cerberooz.BlissUltimate;

import java.util.UUID;

public class AbilityStatusFormatter {
  public static String formatDisplayText(UUID playerId, String text, boolean enabled) {
    ActiveAbilityStore.settleExpired(playerId, text);
    if (ActiveAbilityStore.isActive(playerId, text)) {
      return "§cActive...";
    }

    long timestamp = CooldownService.remainingMillis(playerId, text);
    return timestamp <= 1000L ? "§aReady!" : formatDisplayTextForPlayer(timestamp, enabled);
  }

  public static String formatDisplayTextForPlayer(long timestamp, boolean enabled) {
    if (timestamp <= 1000L) {
      return "§aReady!";
    }

    long lastUpdateTime = timestamp / 1000L;
    if (lastUpdateTime <= 0L) {
      lastUpdateTime = 1L;
    }

    if (enabled) {
      return lastUpdateTime + "s";
    }

    long startTime = lastUpdateTime / 3600L;
    long expiryTime = lastUpdateTime % 3600L / 60L;
    long cooldownMillis = lastUpdateTime % 60L;
    StringBuilder stringBuilder = new StringBuilder();
    if (startTime > 0L) {
      stringBuilder.append(startTime).append("h ");
    }

    if (expiryTime > 0L || startTime > 0L) {
      stringBuilder.append(expiryTime).append("m ");
    }

    stringBuilder.append(cooldownMillis).append("s");
    return stringBuilder.toString().trim();
  }
}
