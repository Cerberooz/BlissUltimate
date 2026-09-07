package cerberooz.cerberooz.BlissUltimate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/** Provisions the runtime integrations used by the plugin. */
public final class DependencyDownloader {
  private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
  private static final int READ_TIMEOUT_MILLIS = 30_000;
  private static final String ULTIMATE_ADVANCEMENT_URL =
      "https://cdn.modrinth.com/data/Tqg6E9V7/versions/hLSJvPNb/UltimateAdvancementAPI-Plugin-2.7.2.jar";
  private static final Map<String, String> CITIZENS_DOWNLOADS =
      Map.of(
          "1.21.11",
              "https://www.dropbox.com/scl/fi/rnozftgzgrasb1jv8srs0/Citizens-2.0.41-b4132.jar?rlkey=n8ei12q6k56l5x5osm7zgemi0&st=iwckjo8v&dl=1",
          "1.21.10",
              "https://www.dropbox.com/scl/fi/rnozftgzgrasb1jv8srs0/Citizens-2.0.41-b4132.jar?rlkey=n8ei12q6k56l5x5osm7zgemi0&st=iwckjo8v&dl=1",
          "1.21.9",
              "https://ci.citizensnpcs.co/job/Citizens2/4132/artifact/dist/target/Citizens-2.0.41-b4132.jar",
          "1.21.8",
              "https://ci.citizensnpcs.co/job/Citizens2/4132/artifact/dist/target/Citizens-2.0.41-b4132.jar",
          "1.21.7",
              "https://ci.citizensnpcs.co/job/Citizens2/4132/artifact/dist/target/Citizens-2.0.41-b4132.jar",
          "1.21.6",
              "https://ci.citizensnpcs.co/job/Citizens2/4132/artifact/dist/target/Citizens-2.0.41-b4132.jar",
          "1.21.5",
              "https://ci.citizensnpcs.co/job/Citizens2/3792/artifact/dist/target/Citizens-2.0.38-b3792.jar",
          "1.21.4",
              "https://www.dropbox.com/scl/fi/5l38nyl7ypf04h4nrtgqr/citizens-2.0.37-SNAPSHOT-2.jar?rlkey=30us4et2f10qv0i5vfyecg3qt&st=uqxscmk9&dl=1");

  private DependencyDownloader() {}

  public static void provisionDependencies() {
    Bliss plugin = Bliss.getInstance();
    File pluginsFolder = plugin.getDataFolder().getParentFile();
    PluginManager pluginManager = Bukkit.getPluginManager();
    boolean citizensPresent = pluginManager.getPlugin("Citizens") != null;
    boolean advancementsPresent = pluginManager.getPlugin("UltimateAdvancementAPI") != null;

    if (!citizensPresent) {
      provisionCitizens(pluginsFolder);
    }
    if (!advancementsPresent) {
      provision(
          new File(pluginsFolder, "UltimateAdvancementAPI.jar"),
          ULTIMATE_ADVANCEMENT_URL,
          "UltimateAdvancementAPI");
    }
  }

  private static void provisionCitizens(File pluginsFolder) {
    String minecraftVersion = Bukkit.getBukkitVersion().split("-")[0];
    String downloadUrl = CITIZENS_DOWNLOADS.get(minecraftVersion);
    if (downloadUrl == null) {
      minecraftVersion = "1.21.4";
      Bukkit.getLogger()
          .warning(
              "No exact Citizens match for "
                  + Bukkit.getBukkitVersion().split("-")[0]
                  + ", using fallback "
                  + minecraftVersion);
      downloadUrl = CITIZENS_DOWNLOADS.get(minecraftVersion);
    }

    if (downloadUrl == null) {
      Bukkit.getLogger().severe("No fallback Citizens version found.");
      return;
    }
    provision(new File(pluginsFolder, "Citizens.jar"), downloadUrl, "Citizens");
  }

  private static void provision(File destination, String downloadUrl, String pluginName) {
    if (destination.exists()) {
      Bukkit.getLogger().info(destination.getName() + " already exists.");
      loadPlugin(destination, pluginName);
      return;
    }

    Bukkit.getLogger().info("Downloading " + pluginName);
    Path temporaryFile = destination.toPath().resolveSibling(destination.getName() + ".download");
    try {
      URLConnection connection = new URL(downloadUrl).openConnection();
      connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
      connection.setReadTimeout(READ_TIMEOUT_MILLIS);
      connection.setRequestProperty("User-Agent", "BlissSMP/3.1.3");
      try (InputStream input = new BufferedInputStream(connection.getInputStream())) {
        Files.copy(input, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
      }
      moveCompletedDownload(temporaryFile, destination.toPath());
      Bukkit.getLogger().info(pluginName + " downloaded successfully.");
      loadPlugin(destination, pluginName);
    } catch (Exception failure) {
      try {
        Files.deleteIfExists(temporaryFile);
      } catch (IOException cleanupFailure) {
        failure.addSuppressed(cleanupFailure);
      }
      Bukkit.getLogger().log(Level.SEVERE, "Failed to download " + pluginName, failure);
    }
  }

  private static void moveCompletedDownload(Path source, Path destination) throws IOException {
    try {
      Files.move(
          source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException ignored) {
      Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static void loadPlugin(File pluginFile, String expectedName) {
    PluginManager pluginManager = Bukkit.getPluginManager();
    Plugin existing = pluginManager.getPlugin(expectedName);
    if (existing != null) {
      Bukkit.getLogger().info("Plugin already loaded: " + existing.getName());
      return;
    }

    try {
      Plugin loaded = pluginManager.loadPlugin(pluginFile);
      if (loaded == null) {
        throw new InvalidPluginException("The plugin manager returned no plugin instance");
      }
      pluginManager.enablePlugin(loaded);
      Bukkit.getLogger().info("Loaded plugin: " + loaded.getName());
    } catch (InvalidPluginException | InvalidDescriptionException failure) {
      Bukkit.getLogger()
          .log(Level.SEVERE, "Failed to load plugin from " + pluginFile.getName(), failure);
    }
  }
}
