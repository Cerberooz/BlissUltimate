package cerberooz.cerberooz.BlissUltimate;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

/** Registers the configurable Energy crafting recipe. */
final class EnergyRecipeRegistrar {
  EnergyRecipeRegistrar(JavaPlugin plugin) {
    ConfigurationSection energyConfig = plugin.getConfig().getConfigurationSection("Energy");
    if (energyConfig == null || !energyConfig.getBoolean("enabled")) {
      return;
    }

    List<String> shape = energyConfig.getStringList("shape");
    ItemStack result = BlissItems.createRewardItem();
    ShapedRecipe recipe =
        new ShapedRecipe(new NamespacedKey(plugin, "energy_custom_crafting"), result);
    recipe.shape(shape.toArray(String[]::new));

    ConfigurationSection ingredients = energyConfig.getConfigurationSection("ingredients");
    if (ingredients != null) {
      for (String ingredientKey : ingredients.getKeys(false)) {
        Material material = Material.matchMaterial(ingredients.getString(ingredientKey));
        if (material != null) {
          recipe.setIngredient(ingredientKey.charAt(0), material);
        }
      }
    }

    Bukkit.addRecipe(recipe);
  }
}
