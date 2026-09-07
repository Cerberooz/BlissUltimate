package cerberooz.cerberooz.BlissUltimate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public final class LegacyColorFormatter {
  static final Pattern pattern;

  LegacyColorFormatter() {}

  public static String formatDisplayText(String text) {
    if (text != null && !text.isEmpty()) {
      Matcher matcher = pattern.matcher(text);
      StringBuffer stringBuffer = new StringBuffer();

      while (matcher.find()) {
        matcher.appendReplacement(stringBuffer, ChatColor.of("#" + matcher.group(1)).toString());
      }

      matcher.appendTail(stringBuffer);
      return ChatColor.translateAlternateColorCodes('&', stringBuffer.toString());
    } else {
      return "";
    }
  }

  public static List<String> getState(List<String> entries) {
    return entries.stream().map(LegacyColorFormatter::formatDisplayText).toList();
  }

  static {
    String hexColorPattern = "&#([A-Fa-f0-9]{6})";
    pattern = Pattern.compile(hexColorPattern);
  }
}
