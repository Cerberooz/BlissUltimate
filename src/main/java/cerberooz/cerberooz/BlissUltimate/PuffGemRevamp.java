package cerberooz.cerberooz.BlissUltimate;

import java.util.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/** Standardized mobility and engagement kit for the Puff Initiator gem. */
public final class PuffGemRevamp implements Listener {
  private final Bliss plugin;
  private final Map<UUID, Long> cooldowns = new HashMap<>();
  private final Set<UUID> jumpedThisAirTime = new HashSet<>();
  private final Map<UUID, ControlledFloat> controlledFloats = new HashMap<>();
  private final Map<UUID, Lunge> lunges = new HashMap<>();
  private final Map<UUID, Player> pendingTrueDamageSources = new HashMap<>();
  private final BukkitTask task;
  private final Runnable actionBarRenderer = this::renderActionBar;

  public PuffGemRevamp(Bliss plugin) {
    this.plugin = plugin;
    task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    ActionBarQueue.registerRenderer(actionBarRenderer);
  }

  @EventHandler(ignoreCancelled = true)
  public void onFlightToggle(PlayerToggleFlightEvent event) {
    Player player = event.getPlayer();
    Gem gem = heldGem(player);
    if (gem == null || gem.energy < 1 || plugin.isGemsDisabled()
        || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR
        || jumpedThisAirTime.contains(player.getUniqueId())) return;
    event.setCancelled(true);
    player.setAllowFlight(false);
    player.setFlying(false);
    if (!ready(player, gem, "doubleJump", gem.tier == 2 ? 3 : 5)) return;
    jumpedThisAirTime.add(player.getUniqueId());
    Vector velocity = player.getLocation().getDirection().normalize()
        .multiply(value(gem, "doubleJump", "horizontal", 1.05D));
    velocity.setY(player.getLocation().getDirection().getY() * value(gem, "doubleJump", "verticalLook", .35D)
        + value(gem, "doubleJump", "vertical", .72D));
    player.setVelocity(velocity);
    player.setFallDistance(0F);
    cloudBurst(player.getLocation());
    windrunner(player, gem);
  }

  /** Breeze Bash is a target ability: Shift-Right-click the intended entity. */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInteractEntity(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    Gem gem = heldGem(player);
    if (!player.isSneaking() || gem == null || gem.tier != 2 || gem.energy < 1 || plugin.isGemsDisabled()) return;
    if (!(event.getRightClicked() instanceof LivingEntity target) || !isEnemy(player, target)) return;
    if (player.getLocation().distanceSquared(target.getLocation()) > Math.pow(value(gem, "breezeBash", "range", 5D), 2D)) return;
    if (!ready(player, gem, "breezeBash", 60)) return;
    breezeBash(player, target, gem);
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onSwapHands(PlayerSwapHandItemsEvent event) {
    Player player = event.getPlayer();
    Gem gem = heldGem(player);
    if (player.isSneaking() || gem == null || gem.tier != 2 || gem.energy < 1 || plugin.isGemsDisabled()) return;
    if (!ready(player, gem, "tailwindLunge", 60)) return;
    Vector velocity = player.getLocation().getDirection().normalize().multiply(value(gem, "tailwindLunge", "velocity", 2.25D));
    player.setVelocity(velocity);
    player.setFallDistance(0F);
    lunges.put(player.getUniqueId(), new Lunge(System.currentTimeMillis() + (long) (value(gem, "tailwindLunge", "durationTicks", 9) * 50L)));
    cloudBurst(player.getLocation());
    windrunner(player, gem);
    event.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onFallDamage(EntityDamageEvent event) {
    if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity() instanceof Player player) {
      Gem gem = heldGem(player);
      if (gem != null && gem.energy > 0 && !plugin.isGemsDisabled()) event.setCancelled(true);
    }
  }

  /** Removes only armour and enchantment mitigation from marked ability damage. */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void applyTrueDamage(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof LivingEntity target) || !(event.getDamager() instanceof Player source)) return;
    Player expected = pendingTrueDamageSources.remove(target.getUniqueId());
    if (expected == null || !expected.equals(source)) return;
    try {
      event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0D);
      event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0D);
    } catch (UnsupportedOperationException ignored) {
      // Paper's legacy modifier API remains available for the configured 1.21.11 target.
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onCropTrample(org.bukkit.event.entity.EntityChangeBlockEvent event) {
    if (event.getEntity() instanceof Player player && event.getBlock().getType() == Material.FARMLAND) {
      Gem gem = heldGem(player);
      if (gem != null && gem.energy > 0 && !plugin.isGemsDisabled()) event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void constrainFloat(PlayerMoveEvent event) {
    ControlledFloat state = controlledFloats.get(event.getPlayer().getUniqueId());
    if (state == null) return;
    Location to = event.getTo();
    if (to != null) to.setY(state.y);
  }

  private void breezeBash(Player caster, LivingEntity target, Gem gem) {
    Location start = caster.getLocation();
    Location end = target.getLocation().clone().subtract(caster.getLocation().getDirection().normalize().multiply(.8));
    end.setDirection(caster.getLocation().getDirection());
    caster.teleport(end, PlayerTeleportEvent.TeleportCause.PLUGIN);
    caster.setFallDistance(0F);
    trueHurt(target, caster, value(gem, "breezeBash", "damage", 2D));
    target.setVelocity(new Vector(0, value(gem, "breezeBash", "launch", .75D), 0));
    double floatY = target.getLocation().getY() + 1D;
    controlledFloats.put(target.getUniqueId(), new ControlledFloat(System.currentTimeMillis() + durationMillis(gem, "breezeBash", 2), floatY));
    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, (int) durationMillis(gem, "breezeBash", 2) / 50 + 10, 4, true, false, true));
    target.getWorld().spawnParticle(Particle.GUST_EMITTER_LARGE, target.getLocation(), 1);
    cloudBurst(start);
    windrunner(caster, gem);
  }

  private void tick() {
    long now = System.currentTimeMillis();
    for (Player player : Bukkit.getOnlinePlayers()) {
      Gem gem = heldGem(player);
      if (player.isOnGround() || player.isInWater() || player.isInsideVehicle()) jumpedThisAirTime.remove(player.getUniqueId());
      if (gem != null && gem.energy > 0 && !plugin.isGemsDisabled()) {
        refreshLore(player, gem);
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
          // Flight must remain enabled after leaving the ground so the second jump can be
          // requested in mid-air. It is disabled immediately after that jump and reset on landing.
          player.setAllowFlight(!jumpedThisAirTime.contains(player.getUniqueId()));
        }
      } else if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
        player.setAllowFlight(false);
        player.setFlying(false);
      }
    }
    for (Iterator<Map.Entry<UUID, ControlledFloat>> it = controlledFloats.entrySet().iterator(); it.hasNext();) {
      Map.Entry<UUID, ControlledFloat> entry = it.next(); LivingEntity target = living(entry.getKey());
      if (target == null || entry.getValue().until <= now) { it.remove(); continue; }
      ControlledFloat state = entry.getValue();
      Location location = target.getLocation();
      location.setY(state.y);
      target.teleport(location);
      target.setVelocity(new Vector(target.getVelocity().getX() * .15D, 0D, target.getVelocity().getZ() * .15D));
      target.getWorld().spawnParticle(Particle.CLOUD, location.clone().add(0,.1,0), 2, .25,.02,.25,.01);
    }
    for (Iterator<Map.Entry<UUID, Lunge>> it = lunges.entrySet().iterator(); it.hasNext();) {
      Map.Entry<UUID, Lunge> entry=it.next(); Player caster=Bukkit.getPlayer(entry.getKey());
      if(caster==null || entry.getValue().until <= now) { if(caster!=null) finishLunge(caster,entry.getValue()); it.remove(); continue; }
      for(Entity entity:caster.getNearbyEntities(1.35,1.35,1.35)) if(entity instanceof LivingEntity target && isEnemy(caster,target) && entry.getValue().hit.add(target.getUniqueId())) hurt(target,caster,value(heldGem(caster),"tailwindLunge","damage",1D));
      caster.getWorld().spawnParticle(Particle.CLOUD,caster.getLocation(),3,.15,.15,.15,.01);
    }
  }

  private void renderActionBar() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      Gem gem = heldGem(player);
      if (gem == null || gem.energy < 1 || plugin.isGemsDisabled()) continue;
      StringBuilder bar = new StringBuilder("§b☁ ᴅᴏᴜʙʟᴇ ᴊᴜᴍᴘ §f").append(cooldownStatus(player, "doubleJump"));
      bar.append(" §8| ");
      if (gem.tier == 2) {
        bar.append("§b🌀 ʙʀᴇᴇᴢᴇ ʙᴀѕʜ §f").append(cooldownStatus(player, "breezeBash"));
        bar.append(" §8| §b➤ ᴛᴀɪʟᴡɪɴᴅ ʟᴜɴɢᴇ §f").append(cooldownStatus(player, "tailwindLunge"));
      } else {
        bar.append("§8🌀 ʙʀᴇᴇᴢᴇ ʙᴀѕʜ §8(T2) §8| §8➤ ᴛᴀɪʟᴡɪɴᴅ ʟᴜɴɢᴇ §8(T2)");
      }
      ActionBarQueue.enqueue(player, bar.toString());
    }
  }

  private String cooldownStatus(Player player, String ability) {
    UUID key = UUID.nameUUIDFromBytes((player.getUniqueId() + ability).getBytes());
    long remaining = Math.max(0L, cooldowns.getOrDefault(key, 0L) - System.currentTimeMillis());
    return remaining == 0L ? "§aReady" : "§c" + Math.max(1L, (remaining + 999L) / 1000L) + "s";
  }

  private void finishLunge(Player caster, Lunge lunge) {
    for(UUID id:lunge.hit) { LivingEntity target=living(id); if(target==null||!target.getWorld().equals(caster.getWorld()))continue;
      Vector pull=caster.getLocation().toVector().subtract(target.getLocation().toVector()); if(pull.lengthSquared()>0) target.setVelocity(pull.normalize().multiply(value(heldGem(caster),"tailwindLunge","pull",1.15D)).setY(.15D)); }
  }
  private void windrunner(Player player, Gem gem) { if(gem.tier==2) player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,(int)durationMillis(gem,"windrunner",2)/50,0,true,false,true)); }
  private void cloudBurst(Location location) { location.getWorld().spawnParticle(Particle.CLOUD,location,35,.45,.2,.45,.02); }
  private void hurt(LivingEntity target, Player caster, double amount) { Vector velocity=target.getVelocity().clone(); target.damage(amount,caster); Bukkit.getScheduler().runTask(plugin,()->{if(target.isValid())target.setVelocity(velocity);}); }
  private void trueHurt(LivingEntity target, Player caster, double amount) { pendingTrueDamageSources.put(target.getUniqueId(), caster); target.damage(amount, caster); pendingTrueDamageSources.remove(target.getUniqueId()); }
  private LivingEntity aimedTarget(Player caster,double range) { Vector look=caster.getEyeLocation().getDirection().normalize(); Comparator<LivingEntity> priority=Comparator.<LivingEntity>comparingDouble(e->-look.dot(e.getEyeLocation().toVector().subtract(caster.getEyeLocation().toVector()).normalize())).thenComparingDouble(e->e.getLocation().distanceSquared(caster.getLocation())); List<LivingEntity> candidates=caster.getNearbyEntities(range,range,range).stream().filter(e->e instanceof LivingEntity&&isEnemy(caster,e)).map(e->(LivingEntity)e).filter(e->{Vector to=e.getEyeLocation().toVector().subtract(caster.getEyeLocation().toVector());return to.lengthSquared()>0&&look.dot(to.normalize())>.65D;}).toList(); LivingEntity playerTarget=candidates.stream().filter(e->e instanceof Player).min(priority).orElse(null); return playerTarget!=null?playerTarget:candidates.stream().filter(e->!(e instanceof Player)).min(priority).orElse(null); }
  private static boolean isEnemy(Player caster,Entity entity){return entity!=caster&&(!(entity instanceof Player p)||!Bliss.getInstance().trustCommand.isAbilityActive(caster.getUniqueId(),p.getUniqueId()));}
  private LivingEntity living(UUID id){Entity e=Bukkit.getEntity(id);return e instanceof LivingEntity l&&l.isValid()?l:null;}
  private boolean ready(Player p,Gem gem,String ability,int fallback){UUID key=UUID.nameUUIDFromBytes((p.getUniqueId()+ability).getBytes());long now=System.currentTimeMillis();if(cooldowns.getOrDefault(key,0L)>now)return false;cooldowns.put(key,now+(long)(value(gem,ability,"cooldown",fallback)*1000D));return true;}
  private long durationMillis(Gem gem,String ability,int fallback){return (long)(value(gem,ability,"duration",fallback)*1000D);}
  private double value(Gem gem,String ability,String field,double fallback){if(gem==null)return fallback;String base="puff."+ability+".";double baseFallback=(ability.equals("doubleJump")&&field.equals("cooldown")&&gem.tier==2)?ConfigValueCache.getDouble(plugin,base+"tier2Cooldown",fallback):ConfigValueCache.getDouble(plugin,base+field,fallback);return ConfigValueCache.getDouble(plugin,base+"energy."+gem.energy+"."+field,baseFallback);}
  private Gem heldGem(Player p){Gem main=gem(p.getInventory().getItemInMainHand());return main!=null?main:gem(p.getInventory().getItemInOffHand());}
  private static Gem gem(ItemStack item){if(item==null||!item.hasItemMeta())return null;ItemMeta meta=item.getItemMeta();if(!meta.hasCustomModelData())return null;int model=meta.getCustomModelData();int tier=item.getType()==Material.AMETHYST_SHARD&&(model==5||model==25||model==45||model==65||model==85)?1:item.getType()==Material.PRISMARINE_SHARD&&(model==6||model==26||model==46||model==66||model==86)?2:0;if(tier==0)return null;String lore=meta.hasLore()?String.join(" ",meta.getLore()).replaceAll("§.",""):"";java.util.regex.Matcher match=java.util.regex.Pattern.compile("Energy: ([0-9]+)").matcher(lore);int energy=match.find()?Integer.parseInt(match.group(1)):lore.contains("Useless")?0:5;return new Gem(tier,energy);}
  private void refreshLore(Player player,Gem gem){for(ItemStack item:player.getInventory().getContents()){Gem itemGem=gem(item);if(itemGem!=null)refreshLore(item,itemGem);}}
  private void refreshLore(ItemStack item,Gem gem){ItemMeta meta=item.getItemMeta();List<String> lore=new ArrayList<>(List.of("§7[Class: §fInitiator§7]","","§b§lᴘᴀѕѕɪᴠᴇѕ","§8=====================================","§f◉ ꜰᴀʟʟ ᴅᴀᴍᴀɢᴇ ɪᴍᴍᴜɴɪᴛʏ","§f◉ ɴᴏ ᴄʀᴏᴘ ᴛʀᴀᴍᴘʟɪɴɢ",gem.tier==2?"§f◉ ᴡɪɴᴅʀᴜɴɴᴇʀ":"§8◉ ᴡɪɴᴅʀᴜɴɴᴇʀ §8(Tier 2 only)",gem.tier==2?"§7 ➥ +20% Movement Speed after using an ability":"","","§b§lᴀʙɪʟɪᴛɪᴇѕ","§8=====================================","§f◉ ᴅᴏᴜʙʟᴇ ᴊᴜᴍᴘ [Jump in air]","§7 ➥ Launch forward and upward","§8[§cCooldown: "+(gem.tier==2?3:5)+"s§8]","§7(Available at Tier 1)",""));addAbility(lore,"ʙʀᴇᴇᴢᴇ ʙᴀѕʜ [Shift-Right]",new String[]{"Dash toward the right-clicked target","Deal +2 damage and apply Controlled Levitation"},gem.tier==2,"2s","60s");lore.add("");addAbility(lore,"ᴛᴀɪʟᴡɪɴᴅ ʟᴜɴɢᴇ [Offhand]",new String[]{"Dash quickly in the direction you are facing","Damage targets passed through and pull them toward you"},gem.tier==2,null,"60s");lore.add("");lore.add("§8Energy: "+gem.energy+"/10");meta.setLore(lore);item.setItemMeta(meta);}
  public ItemStack createMenuPreview(ItemStack item){ItemStack preview=item.clone();Gem gem=gem(preview);if(gem!=null)refreshLore(preview,gem);return preview;}
  private static void addAbility(List<String> lore,String name,String[] effects,boolean unlocked,String duration,String cooldown){String color=unlocked?"§f":"§8";lore.add(color+"◉ "+name);for(String effect:effects)lore.add(color+" ➥ "+effect);lore.add(unlocked?(duration==null?"§8[§cCooldown: "+cooldown+"§8]":"§8[§bDuration: "+duration+" §8| §cCooldown: "+cooldown+"§8]"):(duration==null?"§8[Cooldown: "+cooldown+"]":"§8[Duration: "+duration+" | Cooldown: "+cooldown+"]"));lore.add(unlocked?"§7(Unlocks at Tier 2)":"§8(Unlocks at Tier 2)");}
  public void shutdown(){task.cancel();ActionBarQueue.unregisterRenderer(actionBarRenderer);jumpedThisAirTime.clear();controlledFloats.clear();lunges.clear();}
  public void resetCooldowns(Player player){jumpedThisAirTime.remove(player.getUniqueId());cooldowns.clear();}
  private record Gem(int tier,int energy){} private record ControlledFloat(long until,double y){} private static final class Lunge{final long until;final Set<UUID>hit=new HashSet<>();Lunge(long until){this.until=until;}}
}
