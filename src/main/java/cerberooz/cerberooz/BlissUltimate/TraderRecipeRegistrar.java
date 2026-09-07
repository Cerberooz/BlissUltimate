package cerberooz.cerberooz.BlissUltimate;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class TraderRecipeRegistrar {
  final JavaPlugin javaPlugin;

  public TraderRecipeRegistrar(JavaPlugin javaPlugin) {
    this.javaPlugin = javaPlugin;
    ConfigurationSection section = javaPlugin.getConfig().getConfigurationSection("Trader");
    if (section != null && section.getBoolean("enabled")) {
      List<String> entries = section.getStringList("shape");
      ItemStack item = BlissItems.createTraderMenuItem();
      ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(javaPlugin, "trader_custom_crafting"), item);
      shapedRecipe.shape(entries.toArray(String[]::new));
      ConfigurationSection currentSection = section.getConfigurationSection("ingredients");
      if (currentSection != null) {
        for (String text : currentSection.getKeys(false)) {
          char value = text.charAt(0);
          Material material = Material.matchMaterial(currentSection.getString(text));
          if (material != null) {
            shapedRecipe.setIngredient(value, material);
          }
        }
      }

      Bukkit.addRecipe(shapedRecipe);
    }
  }
}
