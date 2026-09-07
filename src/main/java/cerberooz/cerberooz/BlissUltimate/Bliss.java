package cerberooz.cerberooz.BlissUltimate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class Bliss extends JavaPlugin implements Listener {
  private static final List<String> CUSTOM_RECIPE_KEYS =
      List.of(
          "upgrader_custom_crafting",
          "trader_custom_crafting",
          "repair_custom_crafting",
          "restoration_custom_crafting",
          "energy_custom_crafting");

  GemEnergyManager energyManager;
  WealthPocketsManager wealthPockets;
  GemRegistry gemRegistry;
  StrengthGemRevamp strengthGemRevamp;
  FireGemRevamp fireGemRevamp;
  PuffGemRevamp puffGemRevamp;
  RitualRegistry ritualRegistry;
  Cache<UUID, Long> secondaryCache = CacheBuilder.newBuilder().build();
  Cache<UUID, Long> cache = CacheBuilder.newBuilder().build();
  boolean gemsDisabled = false;
  public static Bliss instance;
  TrustCommand trustCommand;
  final Set<UUID> playerIds = ConcurrentHashMap.newKeySet();
  final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
  int advancementCheckInterval = 40;
  boolean doubleDamageIfGemBroken = true;
  WorldGuardHook worldGuardHook;
  AstraTier2Gem astraTier2Gem;
  AdvancementManager advancementManager;
  private final Object advancementStateLock = new Object();
  private final Object advancementFileLock = new Object();
  private File advancementFile;
  private YamlConfiguration advancementData;
  private BukkitTask advancementFlushTask;
  private boolean advancementWriteInFlight;
  private String queuedAdvancementSnapshot;
  private long queuedAdvancementGeneration;
  private long advancementGeneration;
  private long highestWrittenAdvancementGeneration;

  public Cache<UUID, Long> getSecondaryCache() {
    return this.secondaryCache;
  }

  public GemEnergyManager getEnergyManager() {
    return this.energyManager;
  }

  public int getAdvancementCheckInterval() {
    return this.advancementCheckInterval;
  }

  public boolean isGemsDisabled() {
    return this.gemsDisabled;
  }

  public void initialize() {
    this.gemsDisabled = !this.gemsDisabled;
    this.getConfig().set("gemsDisabled", this.gemsDisabled);
    this.saveConfig();
    ConfigValueCache.clear(this);
  }

  @Override
  public void reloadConfig() {
    super.reloadConfig();
    this.migrateConfiguration();
    ConfigValueCache.clear(this);
    if (this.isEnabled()) {
      this.registerRecipes();
      this.refreshAbilityState();
    }
  }

  public boolean canUseAbility(Player player) {
    return this.worldGuardHook != null && this.worldGuardHook.isPvpDenied(player);
  }

  public void onEnable() {
    instance = this;
    this.saveDefaultConfig();
    this.migrateConfiguration();
    DependencyDownloader.provisionDependencies();
    this.registerRecipes();

    if (this.isConditionMet()) {
      ActionBarQueue.initialize(this);
      CooldownService.initialize(this);
      new ParticleEffects(this);
      ActiveAbilityStore activeAbilityStore = new ActiveAbilityStore();
      Bukkit.getPluginManager().registerEvents(activeAbilityStore, this);
      ActiveAbilityStore.initialize(this);
      this.registerListeners();
      this.completeAbilityAction();
      this.refreshAbilityState();
      this.initializeAdvancementStore();
      if (this.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
        this.getLogger().info("Bliss hooked into WorldGuard!");
        this.worldGuardHook = new WorldGuardHook();
      }

      if (ConfigValueCache.getBoolean(this, "TurnEnergyToUselessEnabled", true)) {
        EnergyDecayScanner.start();
      }

      this.trustCommand = new TrustCommand(this);
      this.gemRegistry = this.resolveGemRegistry();
      this.ritualRegistry = this.resolveRitualRegistry();
      this.updateState(this.gemRegistry);
      this.updateAbilityState(this.ritualRegistry);
      this.applyAbilityEffects();
      this.spawnAbilityEffects(this.gemRegistry, this.ritualRegistry);
      this.cleanupAbilityState(this.gemRegistry);
      if (Bukkit.getPluginManager().isPluginEnabled("UltimateAdvancementAPI")) {
        this.getLogger().info("UltimateAdvancementAPI plugin detected, enabling advancements!");
        this.advancementManager = new AdvancementManager(this);
        this.advancementManager.updateState(
            this.ritualRegistry.restoration, this.ritualRegistry.repair, this.gemRegistry);
      } else {
        this.getLogger().warning("UltimateAdvancementAPI not found. Advancements disabled.");
      }

      this.startBackgroundTasks();
    }
  }

  public void onDisable() {
    if (this.strengthGemRevamp != null) {
      this.strengthGemRevamp.shutdown();
    }
    if (this.fireGemRevamp != null) {
      this.fireGemRevamp.shutdown();
    }
    if (this.puffGemRevamp != null) {
      this.puffGemRevamp.shutdown();
    }
    if (this.gemRegistry != null && this.gemRegistry.fireTier2 != null) {
      this.gemRegistry.fireTier2.cleanup();
    }
    if (this.gemRegistry != null && this.gemRegistry.astraTier2 != null) {
      this.gemRegistry.astraTier2.shutdown();
    }
    if (this.gemRegistry != null && this.gemRegistry.auratus != null) {
      this.gemRegistry.auratus.shutdown();
    }
    if (this.gemRegistry != null && this.gemRegistry.astraTier1 != null) {
      this.gemRegistry.astraTier1.shutdown();
    }
    if (this.gemRegistry != null && this.gemRegistry.speedTier2 != null) {
      this.gemRegistry.speedTier2.cleanup();
    }
    if (this.gemRegistry != null && this.gemRegistry.wealthTier2 != null) {
      this.gemRegistry.wealthTier2.cleanup();
    }
    if (this.gemRegistry != null && this.gemRegistry.wealthTier1 != null) {
      this.gemRegistry.wealthTier1.shutdown();
    }
    if (this.gemRegistry != null && this.gemRegistry.puffTier1 != null) {
      this.gemRegistry.puffTier1.shutdown();
    }
    if (this.gemRegistry != null && this.gemRegistry.puffTier2 != null) {
      this.gemRegistry.puffTier2.shutdown();
    }
    if (this.gemRegistry != null && this.gemRegistry.heretic != null) {
      this.gemRegistry.heretic.shutdown();
    }
    if (this.ritualRegistry != null && this.ritualRegistry.restoration != null) {
      this.ritualRegistry.restoration.shutdown();
    }
    if (this.ritualRegistry != null && this.ritualRegistry.repair != null) {
      this.ritualRegistry.repair.shutdown();
    }
    this.shutdownAdvancementStore();
    ActiveAbilityStore.shutdown();
    GemEnergyManager.cleanup();
    if (ConfigValueCache.getBoolean(this, "TurnEnergyToUselessEnabled", true)) {
      EnergyDecayScanner.stop();
    }

    if (this.trustCommand != null) {
      this.trustCommand.spawnAbilityEffects();
    }

    if (this.wealthPockets != null) {
      this.wealthPockets.shutdown();
    }


    if (this.gemRegistry != null && this.gemRegistry.lifeTier2 != null) {
      this.gemRegistry.lifeTier2.cleanup();
    }
    if (this.gemRegistry != null && this.gemRegistry.lifeTier1 != null) {
      this.gemRegistry.lifeTier1.shutdown();
    }
    if (this.gemRegistry != null && this.gemRegistry.fluxTier2 != null) {
      this.gemRegistry.fluxTier2.shutdown();
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      AstraTier1Gem.refreshAbilityState(player);
      AstraTier2Gem.activateAbility(player);
    }

    BossBarService.clearAll();
    ActionBarQueue.shutdown();
    CooldownService.clearAll();
    SharedScheduler.shutdown();
    ConfigValueCache.clear(this);
    Bukkit.getScheduler().cancelTasks(this);
    ParticleEffects.shutdown();
    instance = null;
  }

  boolean isConditionMet() {
    boolean citizensAvailable = Bukkit.getPluginManager().isPluginEnabled("Citizens");
    boolean advancementApiAvailable = Bukkit.getPluginManager().isPluginEnabled("UltimateAdvancementAPI");
    if (citizensAvailable && advancementApiAvailable) {
      return true;
    }

    if (!citizensAvailable) {
      this.getLogger().warning("Disabling plugin! Citizens plugin is disabled.");
    }

    if (!advancementApiAvailable) {
      this.getLogger().warning("Disabling plugin! UltimateAdvancementAPI plugin is disabled.");
    }

    new BukkitRunnable() {
      @Override
      public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
          if (player.isOp()) {
            if (!citizensAvailable) {
              player.sendMessage(
                  ChatColor.RED
                  + "[WARNING]"
                  + ChatColor.YELLOW
                  + " BlissSMP is disabled! Missing Citizens.");
            }

            if (!advancementApiAvailable) {
              player.sendMessage(
                  ChatColor.RED
                  + "[WARNING]"
                  + ChatColor.YELLOW
                  + " BlissSMP is disabled! Missing UltimateAdvancementAPI.");
            }

            player.sendMessage(ChatColor.GOLD + "Restart the server to fix this.");
          }
        }
      }
    }.runTaskLater(this, 100L);
    Bukkit.getPluginManager().disablePlugin(this);
    return false;
  }

  void refreshAbilityState() {
    this.gemsDisabled = ConfigValueCache.getBoolean(this, "gemsDisabled", false);
    this.advancementCheckInterval =
        ConfigValueCache.getInt(this, "performance.advancement_check_interval", 40);
    this.doubleDamageIfGemBroken =
        ConfigValueCache.getBoolean(this, "DoubleDamageIfGemBroken", true);
  }

  public GemRegistry resolveGemRegistry() {
    if (this.gemRegistry != null) {
      return this.gemRegistry;
    }

    GemRegistry gemRegistry = new GemRegistry();
    gemRegistry.puffTier1 = new PuffTier1Gem(this);
    gemRegistry.lifeTier1 = new LifeTier1Gem(this);
    gemRegistry.fireTier1 = new FireTier1Gem(this);
    gemRegistry.wealthTier1 = new WealthTier1Gem(this);
    gemRegistry.astraTier1 = new AstraTier1Gem(this);
    gemRegistry.speedTier1 = new SpeedTier1Gem(this);
    gemRegistry.fluxTier1 = new FluxTier1Gem(this);
    gemRegistry.puffTier2 = new PuffTier2Gem(this);
    gemRegistry.lifeTier2 = new LifeTier2Gem(this);
    gemRegistry.fireTier2 = new FireTier2Gem(this);
    gemRegistry.wealthTier2 = new WealthTier2Gem(this);
    gemRegistry.astraTier2 = new AstraTier2Gem(this);
    this.astraTier2Gem = gemRegistry.astraTier2;
    gemRegistry.speedTier2 = new SpeedTier2Gem(this);
    gemRegistry.fluxTier2 = new FluxTier2Gem(this);
    gemRegistry.puffTier2.setTrustCommand(this.trustCommand);
    gemRegistry.lifeTier2.setTrustCommand(this.trustCommand);
    gemRegistry.fireTier2.setTrustCommand(this.trustCommand);
    gemRegistry.wealthTier2.setTrustCommand(this.trustCommand);
    gemRegistry.astraTier2.setTrustCommand(this.trustCommand);
    gemRegistry.speedTier2.setTrustCommand(this.trustCommand);
    gemRegistry.fluxTier2.setTrustCommand(this.trustCommand);
    gemRegistry.heretic = new HereticGem(this);
    gemRegistry.heretic.setTrustCommand(this.trustCommand);
    gemRegistry.auratus = new AuratusGem(this);
    this.gemRegistry = gemRegistry;
    return this.gemRegistry;
  }

  public RitualRegistry resolveRitualRegistry() {
    if (this.ritualRegistry != null) {
      return this.ritualRegistry;
    }

    RitualRegistry ritualRegistry = new RitualRegistry();
    ritualRegistry.restoration = new RestorationRitual(this);
    ritualRegistry.repair = new RepairRitual(this);
    this.ritualRegistry = ritualRegistry;
    return this.ritualRegistry;
  }

  void registerListeners() {
    boolean enabled = ConfigValueCache.getBoolean(this, "EnableLifeSystem", true);
    if (enabled) {
      this.energyManager = new GemEnergyManager(this);
      this.getServer().getPluginManager().registerEvents(this.energyManager, this);
      this.getLogger().info("LifeSystem was enabled");
    } else {
      this.getLogger().info("LifeSystem without energy was enabled");
      this.getServer().getPluginManager().registerEvents(new GemRespawnListener(this), this);
    }

    this.wealthPockets = WealthPocketsManager.getInstance(this);
  }

  void updateState(GemRegistry gemRegistry) {
    PluginManager pluginManager = Bukkit.getPluginManager();
    this.puffGemRevamp = new PuffGemRevamp(this);
    pluginManager.registerEvents(this.puffGemRevamp, this);
    pluginManager.registerEvents(gemRegistry.lifeTier1, this);
    gemRegistry.lifeTier1.setAstraTier2Gem(gemRegistry.astraTier2);
    pluginManager.registerEvents(gemRegistry.astraTier1, this);
    gemRegistry.astraTier1.setAstraTier2Gem(gemRegistry.astraTier2);
    pluginManager.registerEvents(gemRegistry.wealthTier1, this);
    gemRegistry.wealthTier1.setAstraTier2Gem(gemRegistry.astraTier2);
    this.strengthGemRevamp = new StrengthGemRevamp(this);
    pluginManager.registerEvents(this.strengthGemRevamp, this);
    this.fireGemRevamp = new FireGemRevamp(this);
    pluginManager.registerEvents(this.fireGemRevamp, this);
    pluginManager.registerEvents(gemRegistry.speedTier1, this);
    gemRegistry.speedTier1.setAstraTier2Gem(gemRegistry.astraTier2);
    pluginManager.registerEvents(gemRegistry.fluxTier1, this);
    gemRegistry.fluxTier1.setAstraTier2Gem(gemRegistry.astraTier2);
    pluginManager.registerEvents(gemRegistry.lifeTier2, this);
    gemRegistry.lifeTier2.setAstraTier2Gem(gemRegistry.astraTier2);
    pluginManager.registerEvents(gemRegistry.astraTier2, this);
    pluginManager.registerEvents(gemRegistry.speedTier2, this);
    gemRegistry.speedTier2.setAstraTier2Gem(gemRegistry.astraTier2);
    pluginManager.registerEvents(gemRegistry.wealthTier2, this);
    gemRegistry.wealthTier2.setAstraTier2Gem(gemRegistry.astraTier2);
    pluginManager.registerEvents(new RestrictedItemListener(gemRegistry.wealthTier2), this);
    pluginManager.registerEvents(gemRegistry.fluxTier2, this);
    gemRegistry.fluxTier2.setAstraTier2Gem(gemRegistry.astraTier2);
    pluginManager.registerEvents(gemRegistry.heretic, this);
    gemRegistry.heretic.setAstraTier2Gem(gemRegistry.astraTier2);
    pluginManager.registerEvents(gemRegistry.auratus, this);
  }

  void updateAbilityState(RitualRegistry ritualRegistry) {
    Bukkit.getPluginManager().registerEvents(ritualRegistry.restoration, this);
    Bukkit.getPluginManager().registerEvents(ritualRegistry.repair, this);
  }

  void applyAbilityEffects() {
    Bukkit.getPluginManager().registerEvents(this, this);
    GemUpgraderManager gemUpgraderManager = new GemUpgraderManager();
    Bukkit.getPluginManager().registerEvents(gemUpgraderManager, this);
    Bukkit.getPluginManager().registerEvents(new InvisibleKillListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EarlyGemSelector(this), this);
    Bukkit.getPluginManager().registerEvents(new AdvancedGemSelector(this), this);
  }

  void registerRecipes() {
    this.unregisterCustomRecipes();

    if (!ConfigValueCache.getBoolean(this, "CustomRecipesEnabled", true)) {
      this.getLogger().info("Custom Bliss recipes are disabled in the configuration.");
      return;
    }

    new UpgraderRecipeRegistrar(this);
    new TraderRecipeRegistrar(this);
    new RepairRecipeRegistrar(this);
    new RestorationRecipeRegistrar(this);
    new EnergyRecipeRegistrar(this);
  }

  private void unregisterCustomRecipes() {
    for (String key : CUSTOM_RECIPE_KEYS) {
      Bukkit.removeRecipe(new NamespacedKey(this, key));
    }
  }

  private void migrateConfiguration() {
    boolean changed = false;
    if (!this.getConfig().isSet("CustomRecipesEnabled")) {
      this.getConfig().set("CustomRecipesEnabled", true);
      changed = true;
    }
    for (String retiredKey :
        List.of(
            "EnableMaceCrafting",
            "EnableMaceEnchanting",
            "DisabledWorlds",
            "EnderPearls",
            "EnableEnd",
            "EnableNether",
            "VillagerTrading",
            "RemovePotions",
            "DragonEggEffects",
            "ItemTagEnabled",
            "Progression",
            "IntoFire",
            "EnteredEnd",
            "ProgressionCommandRun")) {
      if (this.getConfig().isSet(retiredKey)) {
        this.getConfig().set(retiredKey, null);
        changed = true;
      }
    }
    if (changed) {
      this.saveConfig();
    }
  }

  void spawnAbilityEffects(GemRegistry gemRegistry, RitualRegistry ritualRegistry) {
    GiveBlissItemCommand giveBlissItemCommand = new GiveBlissItemCommand(this.energyManager);
    this.getCommand("giveblissitem").setExecutor(giveBlissItemCommand);
    this.getCommand("giveblissitem").setTabCompleter(giveBlissItemCommand);
    this.getCommand("setwatts").setExecutor(new SetWattsCommand(gemRegistry.fluxTier2));
    PedestalCommand pedestalCommand = new PedestalCommand(this);
    Bukkit.getPluginManager().registerEvents(pedestalCommand, this);
    this.getCommand("pedestal").setExecutor(pedestalCommand);
    this.getCommand("pedestal").setTabCompleter(new PedestalTabCompleter());
    SetEnergyCommand setEnergyCommand = new SetEnergyCommand(this);
    this.getCommand("setenergy").setExecutor(setEnergyCommand);
    this.getCommand("setenergy").setTabCompleter(setEnergyCommand);
    RandomGemCommand randomGemCommand = new RandomGemCommand(this);
    this.getCommand("random").setExecutor(randomGemCommand);
    Bukkit.getPluginManager().registerEvents(randomGemCommand, this);
    this.getCommand("random").setTabCompleter(new RandomTabCompleter());
    GemsMenu gemsMenu = new GemsMenu();
    this.getCommand("gems").setExecutor(new GemsCommand(gemsMenu));
    Bukkit.getPluginManager().registerEvents(gemsMenu, this);
    this.getCommand("disable").setExecutor(new DisableAdminCommand(this));
    this.getCommand("cooldown")
        .setExecutor(
            new CooldownAdminCommand(
                gemRegistry.puffTier1,
                gemRegistry.puffTier2,
                this.strengthGemRevamp,
                gemRegistry.lifeTier1,
                gemRegistry.lifeTier2,
                gemRegistry.fireTier1,
                gemRegistry.fireTier2,
                gemRegistry.wealthTier1,
                gemRegistry.wealthTier2,
                gemRegistry.speedTier2,
                ritualRegistry.restoration,
                gemRegistry.astraTier2,
                gemRegistry.fluxTier2,
                gemRegistry.astraTier2,
                gemRegistry.astraTier1,
                gemRegistry.speedTier1,
                gemRegistry.fluxTier1,
                gemRegistry.heretic,
                gemRegistry.auratus));
    this.getCommand("trust").setExecutor(this.trustCommand);
    this.getCommand("trust").setTabCompleter(this.trustCommand);
    Bukkit.getPluginManager().registerEvents(this.trustCommand, this);
    if (ConfigValueCache.getBoolean(this, "EnableWithdrawCommand", true)) {
      this.getCommand("withdraw").setExecutor(new WithdrawCommand(this));
      this.getLogger().info("Withdraw command was enabled");
    }
  }

  void cleanupAbilityState(GemRegistry gemRegistry) {
    gemRegistry.fireTier2.setTrustCommand(this.trustCommand);
    gemRegistry.fluxTier2.setTrustCommand(this.trustCommand);
    gemRegistry.astraTier2.setTrustCommand(this.trustCommand);
    gemRegistry.puffTier2.setTrustCommand(this.trustCommand);
    gemRegistry.lifeTier2.setTrustCommand(this.trustCommand);
    gemRegistry.lifeTier1.setTrustCommand(this.trustCommand);
    gemRegistry.speedTier2.setTrustCommand(this.trustCommand);
    gemRegistry.wealthTier2.setTrustCommand(this.trustCommand);
    gemRegistry.fireTier1.setTrustCommand(this.trustCommand);
    gemRegistry.fluxTier1.setTrustCommand(this.trustCommand);
    gemRegistry.heretic.setTrustCommand(this.trustCommand);
  }

  void startBackgroundTasks() {
    SharedScheduler.scheduleRepeating(new BukkitRunnable() {
      private final Bliss plugin = Bliss.this;
      private static final String BROKEN_MESSAGE = "BROKEN";

      @Override
      public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
          UUID playerId = player.getUniqueId();
          PlayerInventory inventory = player.getInventory();
          boolean holdingBrokenGem =
          this.plugin.isMatchingItem(inventory.getItemInMainHand())
          || this.plugin.isMatchingItem(inventory.getItemInOffHand());
          if (holdingBrokenGem) {
            this.plugin.playerIds.add(playerId);
            player.sendActionBar(ChatColor.RED + BROKEN_MESSAGE);
          } else {
            this.plugin.playerIds.remove(playerId);
          }
        }
      }
    }, this, 1L, 20L);
    SharedScheduler.scheduleRepeating(new BukkitRunnable() {
      @Override
      public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
          if (!player.isFlying()) {
            continue;
          }

          GameMode gameMode = player.getGameMode();
          if (gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE) {
            player.setFlying(false);
            player.setAllowFlight(false);
          }
        }
      }
    }, this, 1L, 2L);
  }

  boolean isMatchingItem(ItemStack item) {
    if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
      ItemMeta itemMeta = item.getItemMeta();
      if (itemMeta.hasCustomModelData()) {
        int count = itemMeta.getCustomModelData();
        return count == 95 || count == 96;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (this.isMatchingItem(event.getItemDrop().getItemStack())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDeath(PlayerDeathEvent event) {
    if (!ConfigValueCache.getBoolean(this, "DropHeadOnDeath", true)) {
      return;
    }

    Player deceased = event.getEntity();
    Player killer = deceased.getKiller();
    if (killer == null) {
      return;
    }

    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta meta = (SkullMeta) head.getItemMeta();
    meta.setOwningPlayer(Bukkit.getOfflinePlayer(deceased.getUniqueId()));
    meta.setDisplayName(ChatColor.RESET + deceased.getName() + "'s Head");
    meta.setLore(List.of(ChatColor.GOLD + "Killed by " + ChatColor.YELLOW + killer.getName()));
    head.setItemMeta(meta);
    deceased.getWorld().dropItemNaturally(deceased.getLocation(), head);
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void scheduleAbilityUpdate(PlayerDeathEvent event) {
    event.getDrops().removeIf(Bliss::isManagedGemItem);
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (!this.doubleDamageIfGemBroken || !(event.getEntity() instanceof Player player)) {
      return;
    }

    for (ItemStack item : player.getInventory().getContents()) {
      if (this.isMatchingItem(item)) {
        event.setDamage(event.getDamage() * 1.5);
        return;
      }
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    ActiveAbilityStore.settlePlayer(event.getPlayer().getUniqueId());
    if (this.advancementManager != null) {
      this.advancementManager.activateAbility(player);
    }

    this.activateAbility(player);
    player.setMaxHealth(20.0);

    if (this.wealthPockets != null) {
      this.wealthPockets.preload(player.getUniqueId());
    }

    Bukkit.getScheduler().runTask(this, () -> giveStarterGemIfMissing(player));
  }

  private void giveStarterGemIfMissing(Player player) {
    if (!player.isOnline() || hasNormalGem(player)) return;
    ItemStack[] starters = {
      BlissItems.createPuffTierOneGemStage6(),
      BlissItems.createLifeTierOneGemStage6(),
      BlissItems.createFireTierOneGemStage6(),
      BlissItems.createAstraTierOneGemStage6(),
      BlissItems.createStrengthTierOneGemStage6(),
      BlissItems.createWealthTierOneGemStage6(),
      BlissItems.createSpeedTierOneGemStage6(),
      BlissItems.createFluxTierOneGemStage6()
    };
    ItemStack starter = starters[ThreadLocalRandom.current().nextInt(starters.length)];
    player.getInventory().addItem(starter).values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
    player.sendMessage("§6🔮 §fYou have been assigned a random Tier 1 Gem.");
  }

  private boolean hasNormalGem(Player player) {
    for (ItemStack item : player.getInventory().getContents()) {
      if (isNormalGem(item)) return true;
    }
    return isNormalGem(player.getInventory().getItemInOffHand());
  }

  private boolean isNormalGem(ItemStack item) {
    if (item != null
        && (item.getType() == Material.AMETHYST_SHARD || item.getType() == Material.PRISMARINE_SHARD)
        && item.hasItemMeta()
        && item.getItemMeta().hasLore()
        && item.getItemMeta().getLore().stream()
            .anyMatch(line -> ChatColor.stripColor(line).startsWith("Energy:"))) {
      return true;
    }
    return item != null && (BlissItems.isMatchingItem(item, BlissItems.createPuffTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createLifeTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createFireTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createAstraTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createWealthTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createFluxTierOneGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createPuffTierTwoGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createLifeTierTwoGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createFireTierTwoGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createAstraTierTwoGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createStrengthTierTwoGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createWealthTierTwoGemStage11())
        || BlissItems.isMatchingItem(item, BlissItems.createSpeedTierTwoGemStage6())
        || BlissItems.isMatchingItem(item, BlissItems.createFluxTierTwoGemStage6()));
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    BossBarService.removePlayer(event.getPlayer());
    ActionBarQueue.removePlayer(event.getPlayer());
    this.playerIds.remove(playerId);
    this.activePlayers.remove(playerId);
  }

  void completeAbilityAction() {
    this.getLogger().info("====================================================");
    this.getLogger().info("");
    this.getLogger().info(" _____ ____ __ _____ _______ _______ ______ ");
    this.getLogger().info(" / ___// __ \\/ / / ___// ____/ | / / ___// ____/");
    this.getLogger().info(" \\__ \\/ / / / / \\__ \\/ / | | / /\\__ \\/ __/ ");
    this.getLogger().info(" ___/ / /_/ / /___ ___/ / /___ | |/ /___/ / /___ ");
    this.getLogger().info("/____/\\____/_____//____/\\____/ |___//____/_____/ ");
    this.getLogger().info("");
    this.getLogger().info("Plugin created by SOLSTATE");
    this.getLogger().info("Version: " + this.getDescription().getVersion());
    this.getLogger().info("====================================================");
  }

  void activateAbility(Player player) {
    Attribute[] attribute =
        new Attribute[] {
          Attribute.MAX_HEALTH,
          Attribute.KNOCKBACK_RESISTANCE,
          Attribute.MOVEMENT_SPEED,
          Attribute.ATTACK_DAMAGE,
          Attribute.ATTACK_SPEED,
          Attribute.ARMOR,
          Attribute.ARMOR_TOUGHNESS,
          Attribute.LUCK,
          Attribute.ATTACK_KNOCKBACK
        };

    for (Attribute currentAttribute : attribute) {
      AttributeInstance attributeInstance = player.getAttribute(currentAttribute);
      if (attributeInstance != null) {
        for (AttributeModifier attributeModifier : attributeInstance.getModifiers()) {
          attributeInstance.removeModifier(attributeModifier);
        }
      }
    }
  }

  void initializeAdvancementStore() {
    File dataFolder = this.getDataFolder();
    if (!dataFolder.exists() && !dataFolder.mkdirs()) {
      this.getLogger().warning("Could not create the BlissSMP data directory.");
    }

    File legacyFolder = new File(dataFolder, "other");
    if (!legacyFolder.exists() && !legacyFolder.mkdirs()) {
      this.getLogger().warning("Could not create the legacy advancement data directory.");
    }

    File legacyFile = new File(legacyFolder, "advancements.yml");
    this.advancementFile = new File(dataFolder, "advancements.yml");
    File sourceFile = this.advancementFile.exists() ? this.advancementFile : legacyFile;
    synchronized (this.advancementStateLock) {
      this.advancementData = YamlConfiguration.loadConfiguration(sourceFile);
    }
  }

  public void processAbilityState(Player player, String advancement, Integer progress) {
    synchronized (this.advancementStateLock) {
      this.ensureAdvancementStoreInitialized();
      this.advancementData.set(advancement + "." + player.getUniqueId(), progress);
      this.advancementGeneration++;
      if (this.advancementFlushTask == null) {
        this.advancementFlushTask =
            Bukkit.getScheduler().runTaskLater(this, this::flushAdvancementSnapshot, 20L);
      }
    }
  }

  public int getConfiguredInt(Player player, String advancement) {
    synchronized (this.advancementStateLock) {
      this.ensureAdvancementStoreInitialized();
      return this.advancementData.getInt(advancement + "." + player.getUniqueId(), 0);
    }
  }

  private void ensureAdvancementStoreInitialized() {
    if (this.advancementData == null) {
      this.initializeAdvancementStore();
    }
  }

  private void flushAdvancementSnapshot() {
    String snapshot;
    long generation;
    synchronized (this.advancementStateLock) {
      this.advancementFlushTask = null;
      this.ensureAdvancementStoreInitialized();
      snapshot = this.advancementData.saveToString();
      generation = this.advancementGeneration;
      if (this.advancementWriteInFlight) {
        this.queuedAdvancementSnapshot = snapshot;
        this.queuedAdvancementGeneration = generation;
        return;
      }
      this.advancementWriteInFlight = true;
    }
    this.writeAdvancementSnapshotAsync(snapshot, generation);
  }

  private void writeAdvancementSnapshotAsync(String snapshot, long generation) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(this, () -> this.drainAdvancementWriteQueue(snapshot, generation));
  }

  private void drainAdvancementWriteQueue(String snapshot, long generation) {
    String pendingSnapshot = snapshot;
    long pendingGeneration = generation;
    while (pendingSnapshot != null) {
      this.writeAdvancementSnapshot(pendingSnapshot, pendingGeneration);
      synchronized (this.advancementStateLock) {
        pendingSnapshot = this.queuedAdvancementSnapshot;
        pendingGeneration = this.queuedAdvancementGeneration;
        this.queuedAdvancementSnapshot = null;
        this.queuedAdvancementGeneration = 0L;
        if (pendingSnapshot == null) {
          this.advancementWriteInFlight = false;
        }
      }
    }
  }

  private void writeAdvancementSnapshot(String snapshot, long generation) {
    synchronized (this.advancementFileLock) {
      if (generation <= this.highestWrittenAdvancementGeneration) {
        return;
      }
      try {
        Files.writeString(
            this.advancementFile.toPath(),
            snapshot,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE);
        this.highestWrittenAdvancementGeneration = generation;
      } catch (IOException failure) {
        this.getLogger().warning("Could not save advancement progress: " + failure.getMessage());
      }
    }
  }

  private void shutdownAdvancementStore() {
    String snapshot;
    long generation;
    synchronized (this.advancementStateLock) {
      if (this.advancementData == null) {
        return;
      }
      if (this.advancementFlushTask != null) {
        this.advancementFlushTask.cancel();
        this.advancementFlushTask = null;
      }
      snapshot = this.advancementData.saveToString();
      generation = this.advancementGeneration;
      this.queuedAdvancementSnapshot = null;
      this.queuedAdvancementGeneration = 0L;
    }
    this.writeAdvancementSnapshot(snapshot, generation);
  }

  public static Bliss getInstance() {
    return instance;
  }

  public boolean isAbilityAllowed(World world) {
    return false;
  }

  public boolean isAbilityActive(UUID playerId, UUID targetId) {
    return this.trustCommand != null && this.trustCommand.isAbilityActive(playerId, targetId);
  }

  public boolean isAbilityBlocked(UUID playerId) {
    return this.astraTier2Gem != null && this.astraTier2Gem.hasRequiredState(playerId);
  }

  public long getAbilityLong(UUID playerId) {
    return this.astraTier2Gem != null ? this.astraTier2Gem.getAbilityLong(playerId) : 0L;
  }

  public boolean isMatchingState(UUID playerId) {
    return CooldownService.isOnCooldown(playerId, "swing");
  }

  public void handleAbilityAction(UUID playerId, int count) {
    CooldownService.setCooldown(playerId, "swing", count);
  }

  public void spawnAbilityParticles(Location location, Color color) {
    ParticleEffects.spawnPrimaryAbilityParticles(location, color);
  }

  public void resetAbilityState(
      Particle particle,
      Location location,
      Vector direction,
      double value,
      double distance,
      int count,
      double radius,
      double angle,
      double progress,
      int index,
      double scale,
      double amount,
      double speed,
      double height,
      float size,
      float volume,
      float pitch) {
    ParticleEffects.updateAbilityState(
        particle, location, direction, value, distance, count, radius, angle, progress, index, scale, amount, speed, height,
        size, volume, pitch);
  }

  public BukkitRunnable createDirectionalParticleTask(
      Particle particle,
      Location location,
      Vector direction,
      double value,
      double distance,
      double radius,
      int count,
      int index,
      double angle,
      double progress,
      double scale,
      double amount,
      float size,
      float speed,
      float volume) {
    return ParticleEffects.createPulseParticleAnimationTask(
        particle, location, direction, value, distance, radius, count, index, angle, progress, scale, amount, size, speed,
        volume);
  }

  public BukkitRunnable createActiveParticleAnimationTask(
      Particle particle,
      Location location,
      Location targetLocation,
      int count,
      int index,
      double value,
      double distance,
      double radius,
      double angle) {
    return ParticleEffects.createActiveParticleAnimationTask(
        particle, location, targetLocation, count, index, value, distance, radius, angle);
  }

  public BukkitRunnable createCleanupParticleOrbitTask(
      Particle particle,
      Location location,
      double value,
      double distance,
      int count,
      int index,
      int remaining,
      double radius,
      double angle,
      double progress,
      double scale) {
    return ParticleEffects.createCleanupParticleOrbitTask(
        particle, location, value, distance, count, index, remaining, radius, angle, progress, scale);
  }

  public void trackAbilityState(
      Particle particle,
      Location location,
      double value,
      int count,
      int index,
      int remaining,
      double distance,
      double radius,
      double angle,
      double progress,
      float scale,
      float size,
      float speed) {
    ParticleEffects.updateTargetState(
        particle, location, value, count, index, remaining, distance, radius, angle, progress, scale, size, speed);
  }

  static boolean isManagedGemItem(ItemStack item) {
    if (item != null && item.getType() != Material.AIR) {
      Material material = item.getType();
      if ((material == Material.PRISMARINE_SHARD || material == Material.AMETHYST_SHARD)
          && item.hasItemMeta()) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null && itemMeta.hasCustomModelData()) {
          int count = itemMeta.getCustomModelData();
          return count >= 1 && count <= 196;
        }
      }

      return false;
    } else {
      return false;
    }
  }
}
