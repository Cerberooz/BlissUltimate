package cerberooz.cerberooz.BlissUltimate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/** Caches scalar configuration reads until the plugin configuration is reloaded. */
public final class ConfigValueCache {
  private static final Object NULL_VALUE = new Object();
  private static final Map<JavaPlugin, Map<Key, Object>> PLUGIN_CACHES = new ConcurrentHashMap<>();

  private ConfigValueCache() {}

  public static boolean getBoolean(JavaPlugin plugin, String path) {
    return (Boolean)
        value(
            plugin,
            new Key(ValueType.BOOLEAN, path, null, false),
            () -> plugin.getConfig().getBoolean(path));
  }

  public static boolean getBoolean(Plugin plugin, String path) {
    return getBoolean(asJavaPlugin(plugin), path);
  }

  public static boolean getBoolean(JavaPlugin plugin, String path, boolean defaultValue) {
    return (Boolean)
        value(
            plugin,
            new Key(ValueType.BOOLEAN, path, defaultValue, true),
            () -> plugin.getConfig().getBoolean(path, defaultValue));
  }

  public static boolean getBoolean(Plugin plugin, String path, boolean defaultValue) {
    return getBoolean(asJavaPlugin(plugin), path, defaultValue);
  }

  public static int getInt(JavaPlugin plugin, String path) {
    return (Integer)
        value(
            plugin,
            new Key(ValueType.INTEGER, path, null, false),
            () -> plugin.getConfig().getInt(path));
  }

  public static int getInt(JavaPlugin plugin, String path, int defaultValue) {
    return (Integer)
        value(
            plugin,
            new Key(ValueType.INTEGER, path, defaultValue, true),
            () -> plugin.getConfig().getInt(path, defaultValue));
  }

  public static long getLong(JavaPlugin plugin, String path) {
    return (Long)
        value(
            plugin,
            new Key(ValueType.LONG, path, null, false),
            () -> plugin.getConfig().getLong(path));
  }

  public static long getLong(JavaPlugin plugin, String path, long defaultValue) {
    return (Long)
        value(
            plugin,
            new Key(ValueType.LONG, path, defaultValue, true),
            () -> plugin.getConfig().getLong(path, defaultValue));
  }

  public static double getDouble(JavaPlugin plugin, String path) {
    return (Double)
        value(
            plugin,
            new Key(ValueType.DOUBLE, path, null, false),
            () -> plugin.getConfig().getDouble(path));
  }

  public static double getDouble(JavaPlugin plugin, String path, double defaultValue) {
    return (Double)
        value(
            plugin,
            new Key(ValueType.DOUBLE, path, defaultValue, true),
            () -> plugin.getConfig().getDouble(path, defaultValue));
  }

  public static String getString(JavaPlugin plugin, String path) {
    Object result =
        value(
            plugin,
            new Key(ValueType.STRING, path, null, false),
            () -> plugin.getConfig().getString(path));
    return result == NULL_VALUE ? null : (String) result;
  }

  public static String getString(JavaPlugin plugin, String path, String defaultValue) {
    Object result =
        value(
            plugin,
            new Key(ValueType.STRING, path, defaultValue, true),
            () -> plugin.getConfig().getString(path, defaultValue));
    return result == NULL_VALUE ? null : (String) result;
  }

  public static boolean contains(JavaPlugin plugin, String path) {
    return (Boolean)
        value(
            plugin,
            new Key(ValueType.CONTAINS, path, null, false),
            () -> plugin.getConfig().contains(path));
  }

  public static void clear(JavaPlugin plugin) {
    PLUGIN_CACHES.remove(plugin);
  }

  public static void clearAll() {
    PLUGIN_CACHES.clear();
  }

  private static JavaPlugin asJavaPlugin(Plugin plugin) {
    if (plugin instanceof JavaPlugin javaPlugin) {
      return javaPlugin;
    }
    throw new IllegalArgumentException("Configuration caching requires a JavaPlugin");
  }

  private static Object value(JavaPlugin plugin, Key key, ValueLoader loader) {
    Map<Key, Object> values =
        PLUGIN_CACHES.computeIfAbsent(plugin, ignored -> new ConcurrentHashMap<>());
    return values.computeIfAbsent(
        key,
        ignored -> {
          Object loaded = loader.load();
          return loaded == null ? NULL_VALUE : loaded;
        });
  }

  private enum ValueType {
    BOOLEAN,
    INTEGER,
    LONG,
    DOUBLE,
    STRING,
    CONTAINS
  }

  private record Key(ValueType type, String path, Object defaultValue, boolean hasDefault) {}

  @FunctionalInterface
  private interface ValueLoader {
    Object load();
  }
}
