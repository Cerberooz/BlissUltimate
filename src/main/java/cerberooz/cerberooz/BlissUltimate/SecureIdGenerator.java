package cerberooz.cerberooz.BlissUltimate;

import java.security.SecureRandom;

public final class SecureIdGenerator {
  static final String ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ID;
  static final SecureRandom secureRandom;

  SecureIdGenerator() {}

  public static String formatDisplayText(int count) {
    StringBuilder stringBuilder = new StringBuilder(count);

    for (int index = 0; index < count; index++) {
      stringBuilder.append(
          "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
              .charAt(
                  secureRandom.nextInt(
                      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length())));
    }

    return stringBuilder.toString();
  }

  static {
    ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ID = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    secureRandom = new SecureRandom();
  }
}
