package cerberooz.cerberooz.BlissUltimate;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class AdvancementManager {
  final Bliss plugin;
  AdvancementTab advancementTab;
  RootAdvancement rootAdvancement;
  UltimateAdvancementAPI ultimateAdvancementAPI;
  ShatteredGemAdvancement shatteredGemAdvancement;
  UpgradeGemAdvancement upgradeGemAdvancement;
  RestorationRitualAdvancement restorationRitualAdvancement;
  TraderSwapAdvancement traderSwapAdvancement;
  SavedByDiceAdvancement savedByDiceAdvancement;
  BoundaryBreakAdvancement boundaryBreakAdvancement;
  StrengthTrackerAdvancement strengthTrackerAdvancement;
  CopperTravelAdvancement copperTravelAdvancement;
  AlmostFellFromGraceAdvancement almostFellFromGraceAdvancement;
  OverflowingEnergyAdvancement overflowingEnergyAdvancement;
  RepairRitualAdvancement repairRitualAdvancement;
  PhantomDaggerAdvancement phantomDaggerAdvancement;
  OverchargedFluxAdvancement overchargedFluxAdvancement;

  public AdvancementManager(Bliss bliss) {
    this.plugin = bliss;
  }

  public void activateAbility(Player player) {
    if (this.advancementTab != null) {
      this.advancementTab.showTab(player);
      this.advancementTab.grantRootAdvancement(player);
    }
  }

  public void updateState(RestorationRitual restorationRitual, RepairRitual repairRitual, GemRegistry gemRegistry) {
    this.ultimateAdvancementAPI = UltimateAdvancementAPI.getInstance(this.plugin);
    this.advancementTab = this.ultimateAdvancementAPI.createAdvancementTab("bliss");
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setCustomModelData(95);
    item.setItemMeta(itemMeta);
    ItemStack heldItem = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta meta = heldItem.getItemMeta();
    meta.setCustomModelData(5);
    heldItem.setItemMeta(meta);
    AdvancementDisplay advancementDisplay =
        new AdvancementDisplay(
            BlissItems.createLifeTierTwoGemStage6(),
            ChatColor.LIGHT_PURPLE + "BlissSMP",
            AdvancementFrameType.TASK,
            true,
            false,
            0.0F,
            3.0F,
            "The Bliss SMP advancements!");
    this.rootAdvancement = new RootAdvancement(this.advancementTab, "root", advancementDisplay, "textures/block/deepslate_tiles.png");
    this.almostFellFromGraceAdvancement =
        new AlmostFellFromGraceAdvancement(
            this.plugin,
            "fell",
            new AdvancementDisplay(
                heldItem,
                "Almost Fell From Grace",
                AdvancementFrameType.CHALLENGE,
                true,
                true,
                1.0F,
                6.0F,
                "Prevent 30,000 points of fall damage because of a Puff Gem"),
            this.rootAdvancement,
            30000,
            gemRegistry.puffTier1,
            gemRegistry.puffTier2);
    this.copperTravelAdvancement =
        new CopperTravelAdvancement(
            this.plugin,
            "zip",
            new AdvancementDisplay(
                Material.COPPER_BLOCK,
                "Zip Away",
                AdvancementFrameType.CHALLENGE,
                true,
                true,
                1.0F,
                5.0F,
                "Travel across 1,914 copper blocks"),
            this.rootAdvancement,
            1914);
    this.shatteredGemAdvancement =
        new ShatteredGemAdvancement(
            this.plugin,
            "shattered",
            new AdvancementDisplay(
                item,
                "Shattered",
                AdvancementFrameType.CHALLENGE,
                true,
                true,
                1.0F,
                0.5F,
                "Break your gem completely"),
            this.rootAdvancement,
            1);
    this.overflowingEnergyAdvancement =
        new OverflowingEnergyAdvancement(
            this.plugin,
            "overflow",
            new AdvancementDisplay(
                BlissItems.createPuffTierTwoGemStage11(),
                "Overflowing",
                AdvancementFrameType.CHALLENGE,
                true,
                true,
                2.0F,
                0.5F,
                "Reach the Pristine +5 energy stage"),
            this.rootAdvancement,
            1);
    this.overchargedFluxAdvancement =
        new OverchargedFluxAdvancement(
            this.plugin,
            "overcharged",
            new AdvancementDisplay(
                Material.LIGHTNING_ROD,
                "Overcharged",
                AdvancementFrameType.GOAL,
                true,
                true,
                2.0F,
                5.0F,
                "Charge a Flux Gem to 100W"),
            this.rootAdvancement,
            1);
    this.restorationRitualAdvancement =
        new RestorationRitualAdvancement(
            this.plugin,
            restorationRitual,
            "restoration",
            new AdvancementDisplay(
                BlissItems.createUpgradeItem(),
                "Reaweaking",
                AdvancementFrameType.CHALLENGE,
                true,
                true,
                2.0F,
                1.5F,
                "Complete the restoration ritual"),
            this.rootAdvancement,
            1);
    this.repairRitualAdvancement =
        new RepairRitualAdvancement(
            this.plugin,
            repairRitual,
            "goodasnew",
            new AdvancementDisplay(
                BlissItems.createRecipeItem(),
                "Good As New!",
                AdvancementFrameType.CHALLENGE,
                true,
                true,
                1.0F,
                1.5F,
                "Complete the repair ritual"),
            this.rootAdvancement,
            1);
    this.strengthTrackerAdvancement =
        new StrengthTrackerAdvancement(
            this.plugin,
            "rabbit",
            new AdvancementDisplay(
                Material.RABBIT_STEW,
                "It´s Rabbit Season",
                AdvancementFrameType.GOAL,
                true,
                true,
                1.0F,
                7.0F,
                "Track a player with the Strength Ability 17 times"),
            this.rootAdvancement,
            17);
    this.boundaryBreakAdvancement =
        new BoundaryBreakAdvancement(
            this.plugin,
            "break",
            new AdvancementDisplay(
                Material.ENCHANTING_TABLE,
                "Boundary Break",
                AdvancementFrameType.TASK,
                true,
                true,
                1.0F,
                8.0F,
                "Use Wealth Gem's Amplification power to break an enchantment limit"),
            this.rootAdvancement,
            1);
    this.savedByDiceAdvancement =
        new SavedByDiceAdvancement(
            this.plugin,
            "saved",
            new AdvancementDisplay(
                Material.TOTEM_OF_UNDYING,
                "Saved By The Dice",
                AdvancementFrameType.TASK,
                true,
                true,
                1.0F,
                4.0F,
                "Avoid death with an astra phase"),
            this.rootAdvancement,
            1);
    this.phantomDaggerAdvancement =
        new PhantomDaggerAdvancement(
            this.plugin,
            "dagger",
            new AdvancementDisplay(
                BlissItems.createItem(),
                "Piercing Precision",
                AdvancementFrameType.GOAL,
                true,
                true,
                2.0F,
                4.0F,
                "Hit a player with 5 consecutive Phantom Daggers"),
            this.rootAdvancement,
            1);
    this.traderSwapAdvancement =
        new TraderSwapAdvancement(
            this.plugin,
            "time",
            new AdvancementDisplay(
                BlissItems.createTraderMenuItem(),
                "Time For A Change",
                AdvancementFrameType.TASK,
                true,
                true,
                1.0F,
                2.5F,
                "Switch your gem with a trader"),
            this.rootAdvancement,
            1);
    this.upgradeGemAdvancement =
        new UpgradeGemAdvancement(
            this.plugin,
            "level",
            new AdvancementDisplay(
                BlissItems.createUpgraderMenuItem(),
                "The Next Level",
                AdvancementFrameType.GOAL,
                true,
                true,
                2.0F,
                2.5F,
                "Upgrade your gem using the upgrader"),
            this.rootAdvancement,
            1);
    this.advancementTab.registerAdvancements(
        this.rootAdvancement,
        this.shatteredGemAdvancement,
        this.upgradeGemAdvancement,
        this.traderSwapAdvancement,
        this.restorationRitualAdvancement,
        this.savedByDiceAdvancement,
        this.boundaryBreakAdvancement,
        this.strengthTrackerAdvancement,
        this.almostFellFromGraceAdvancement,
        this.copperTravelAdvancement,
        this.overflowingEnergyAdvancement,
        this.repairRitualAdvancement,
        this.phantomDaggerAdvancement,
        this.overchargedFluxAdvancement);
  }
}
