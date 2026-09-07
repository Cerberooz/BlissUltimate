# BlissUltimate

BlissUltimate is a Paper plugin for Minecraft 1.21.11, featuring collectible gems, energy and tier progression, custom abilities, and its matching resource pack.

## Requirements

- Java 21
- Paper 1.21.11
- Gradle 9.x (or use the configured Gradle wrapper once generated)

Optional soft dependencies are WorldGuard, Citizens, and UltimateAdvancementAPI.

## Build

```powershell
gradle build
```

The compiled plugin JAR is written to `build/libs/`.

## Configuration

The default plugin configuration is at `src/main/resources/config.yml`. Custom recipes have a global `CustomRecipesEnabled` toggle and individual recipe toggles. Copy the built JAR into your Paper server's `plugins/` directory, then restart the server to generate and edit its live configuration.

## Resource pack

`Bliss_Pack/` contains the resource-pack source and version overlays used by the plugin's custom-model-data items.
