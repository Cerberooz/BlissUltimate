package cerberooz.cerberooz.BlissUltimate;

import java.util.*;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/** Controller-focused Fire Gem abilities.  No ability changes terrain permanently. */
public final class FireGemRevamp implements Listener {
  private final Bliss plugin;
  private final Map<UUID, Long> cooldowns = new HashMap<>();
  private final Map<UUID, ChargeState> charging = new HashMap<>();
  private final Map<UUID, Campfire> campfires = new HashMap<>();
  private final Set<UUID> fireballs = new HashSet<>();
  private final Map<UUID, Player> trueDamageSources = new HashMap<>();
  private final BukkitTask task;
  private final Runnable actionBarRenderer = this::renderActionBar;
  public FireGemRevamp(Bliss plugin) { this.plugin=plugin; task=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,1,5); ActionBarQueue.registerRenderer(actionBarRenderer); }

  /** Shift-left is detected from the player's arm swing, including when swinging into open air. */
  @EventHandler(ignoreCancelled=true) public void swing(PlayerAnimationEvent e) {
    Player p=e.getPlayer(); Gem g=held(p); if(!p.isSneaking()||g==null||g.energy<1||plugin.isGemsDisabled())return;
    if(ready(p,g,"heatwave",40))heatwave(p);
  }
  @EventHandler public void interact(PlayerInteractEvent e) {
    Player p=e.getPlayer(); Gem g=held(p); if (g==null||g.energy<1||plugin.isGemsDisabled()||!right(e.getAction())) return;
    if (p.isSneaking() && g.tier==2 && ready(p,g,"campfire",50)) { campfire(p,g); e.setCancelled(true); }
  }
  @EventHandler(priority=EventPriority.HIGHEST) public void swap(PlayerSwapHandItemsEvent e) {
    Player p=e.getPlayer(); Gem g=held(p); if (p.isSneaking()||g==null||g.tier!=2||g.energy<1||plugin.isGemsDisabled()) return;
    long now=System.currentTimeMillis(); ChargeState state=charging.remove(p.getUniqueId());
    if (state!=null) { launch(p,state,now>=state.maxChargeAt); state.remove(); e.setCancelled(true); return; }
    if (ready(p,g,"fireball",70)) { charging.put(p.getUniqueId(),ChargeState.create(p,g,now)); p.sendMessage("§6🔮 §fFireball charging. Press Offhand again to launch."); e.setCancelled(true); }
  }
  @EventHandler(ignoreCancelled=true) public void shoot(EntityShootBowEvent e) {
    if (!(e.getEntity() instanceof Player p) || held(p)==null || held(p).tier!=2 || held(p).energy<1) return;
    e.getProjectile().setFireTicks(100);
  }
  @EventHandler(ignoreCancelled=true) public void melee(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player p && held(p)!=null && held(p).tier==2 && held(p).energy>0 && e.getEntity() instanceof LivingEntity target) target.setFireTicks(Math.max(target.getFireTicks(),100));
  }
  /** Ability damage keeps its caster as the Bukkit damage source, but removes armour reduction only. */
  @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true) public void preserveTrueDamage(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof LivingEntity target) || !(e.getDamager() instanceof Player source)) return;
    Player expected=trueDamageSources.remove(target.getUniqueId()); if(expected==null || !expected.equals(source)) return;
    try { e.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0.0D); e.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0.0D); } catch (UnsupportedOperationException ignored) { }
  }
  @EventHandler(ignoreCancelled=true) public void projectileHit(ProjectileHitEvent e) {
    if (!(e.getEntity() instanceof Fireball ball) || !fireballs.remove(ball.getUniqueId())) return;
    Player caster=ball.getShooter() instanceof Player p?p:null; if(caster==null) return; boolean max=ball.getPersistentDataContainer().has(new NamespacedKey(plugin,"fire_max"));
    int energy=ball.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin,"fire_energy"),org.bukkit.persistence.PersistentDataType.INTEGER,1);
    Gem gem=new Gem(2,energy); blast(caster,gem,ball.getLocation(),max?4:2,value(gem,"fireball","range",3D),max?durationMillis(gem,"fireball",5):0L); ball.remove();
  }
  @EventHandler(ignoreCancelled=true) public void autoSmeltBlock(BlockBreakEvent e) {
    if (!(e.getPlayer() instanceof Player p) || held(p)==null || held(p).energy<1 || p.getInventory().getItemInMainHand().containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH)) return;
    Material result=smelt(e.getBlock().getType()); if(result==null) return; e.setDropItems(false); e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(),new ItemStack(result));
  }
  @EventHandler(ignoreCancelled=true) public void autoSmeltDrop(EntityDeathEvent e) {
    Player p=e.getEntity().getKiller(); if(p==null||held(p)==null||held(p).energy<1||p.getInventory().getItemInMainHand().containsEnchantment(org.bukkit.enchantments.Enchantment.SILK_TOUCH))return;
    ListIterator<ItemStack> it=e.getDrops().listIterator(); while(it.hasNext()){ ItemStack d=it.next(); Material r=smelt(d.getType()); if(r!=null) it.set(new ItemStack(r,d.getAmount())); }
  }
  private void heatwave(Player p) { for(int burst=0;burst<3;burst++){ int b=burst; Bukkit.getScheduler().runTaskLater(plugin,()->{ Location center=p.getLocation().add(0,1,0); heatwaveSphere(center,1.8D+b*1.6D); for(Entity x:p.getNearbyEntities(5,3,5)) if(enemy(p,x)&&x instanceof LivingEntity t){ normalHurt(t,p,1); Vector v=t.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(.35); v.setY(.12); t.setVelocity(v); t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,60,0,true,false,true)); } },b*10L); } }
  private void heatwaveSphere(Location center,double radius){for(int vertical=0;vertical<=8;vertical++){double phi=Math.PI*vertical/8D;double ring=radius*Math.sin(phi);double y=radius*Math.cos(phi);for(int horizontal=0;horizontal<20;horizontal++){double angle=Math.PI*2D*horizontal/20D;center.getWorld().spawnParticle(Particle.FLAME,center.clone().add(Math.cos(angle)*ring,y,Math.sin(angle)*ring),1,0,0,0,.01D);}}}
  private void campfire(Player p,Gem g) { Location l=p.getLocation().add(p.getLocation().getDirection().setY(0).normalize().multiply(2)); l.setY(Math.floor(l.getY())); campfires.put(p.getUniqueId(),new Campfire(l,System.currentTimeMillis()+durationMillis(g,"campfire",8),value(g,"campfire","range",4D),(int)value(g,"campfire","slowAmplifier",0))); if(l.getBlock().isEmpty())l.getBlock().setType(Material.CAMPFIRE); p.sendMessage("§6🔮 §fCampfire placed."); }
  private void launch(Player p,ChargeState state,boolean max){ Fireball f=p.launchProjectile(Fireball.class); f.setShooter(p);f.setYield(0);f.setIsIncendiary(false);f.setVelocity(f.getVelocity().multiply(1.5));if(max)f.getPersistentDataContainer().set(new NamespacedKey(plugin,"fire_max"),org.bukkit.persistence.PersistentDataType.BYTE,(byte)1);f.getPersistentDataContainer().set(new NamespacedKey(plugin,"fire_energy"),org.bukkit.persistence.PersistentDataType.INTEGER,state.gem.energy);fireballs.add(f.getUniqueId());p.sendMessage(max?"§6🔮 §fMax Fireball launched.":"§6🔮 §fFireball launched."); }
  private void blast(Player p,Gem gem,Location l,double damage,double radius,long zoneDuration){ l.getWorld().spawnParticle(Particle.FLAME,l,100,radius/2,1,radius/2,.04); for(Entity x:l.getWorld().getNearbyEntities(l,radius,radius,radius))if(enemy(p,x)&&x instanceof LivingEntity t)trueHurt(t,p,damage); if(zoneDuration>0){ long until=System.currentTimeMillis()+zoneDuration; int slow=(int)value(gem,"fireball","zoneSlowAmplifier",1); Bukkit.getScheduler().runTaskTimer(plugin,task1->{if(System.currentTimeMillis()>=until){task1.cancel();return;}l.getWorld().spawnParticle(Particle.SMALL_FLAME,l,35,radius/2,.1,radius/2,.01);for(Entity x:l.getWorld().getNearbyEntities(l,radius,radius,radius))if(enemy(p,x)&&x instanceof LivingEntity t){normalHurt(t,p,1);t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,25,slow,true,false,true));}},0,20);}}
  private void tick(){long now=System.currentTimeMillis(); for(Player p:Bukkit.getOnlinePlayers()){Gem g=held(p);if(g!=null&&g.energy>0&&!plugin.isGemsDisabled()){p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,30,0,true,false,false));refreshLore(p,g);} } for(Iterator<Map.Entry<UUID,ChargeState>>it=charging.entrySet().iterator();it.hasNext();){var e=it.next();ChargeState state=e.getValue();Player player=Bukkit.getPlayer(e.getKey());if(player==null||now>=state.expiresAt){state.remove();it.remove();continue;}state.update(player,now);} for(Iterator<Map.Entry<UUID,Campfire>>it=campfires.entrySet().iterator();it.hasNext();){var en=it.next();Campfire c=en.getValue();if(c.until<now){if(c.location.getBlock().getType()==Material.CAMPFIRE)c.location.getBlock().setType(Material.AIR);it.remove();continue;}Player owner=Bukkit.getPlayer(en.getKey());if(owner==null)continue;campfireSphere(c);for(Entity x:c.location.getWorld().getNearbyEntities(c.location,c.radius,3,c.radius)){if(!(x instanceof Player q))continue;if(enemy(owner,q)){q.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,15,c.slowAmplifier,true,false,true));if(c.inside.add(q.getUniqueId()))boundaryDamage(c,q,owner,now);}else{q.setFoodLevel(Math.min(20,q.getFoodLevel()+1));}} c.inside.removeIf(id->{Player q=Bukkit.getPlayer(id);if(q==null||q.getLocation().distanceSquared(c.location)>c.radius*c.radius){if(q!=null&&owner!=null)boundaryDamage(c,q,owner,now);return true;}return false;});}}
  private void boundaryDamage(Campfire campfire,Player target,Player owner,long now){long cooldown=ConfigValueCache.getInt(plugin,"fire.campfire.boundaryDamageCooldownSeconds",1)*1000L;if(campfire.lastBoundaryDamage.getOrDefault(target.getUniqueId(),0L)+cooldown>now)return;campfire.lastBoundaryDamage.put(target.getUniqueId(),now);normalHurt(target,owner,3);}
  private void campfireSphere(Campfire campfire){for(int horizontal=0;horizontal<12;horizontal++){double angle=Math.PI*2D*horizontal/12D;for(int vertical=-1;vertical<=1;vertical++){double y=vertical*.55D;double radius=Math.sqrt(Math.max(0D,campfire.radius*campfire.radius-y*y));campfire.location.getWorld().spawnParticle(Particle.FLAME,campfire.location.clone().add(Math.cos(angle)*radius,y+.55D,Math.sin(angle)*radius),1,0,0,0,0);}}}
  private void normalHurt(LivingEntity t,Player p,double amount){ Vector keep=t.getVelocity().clone(); t.damage(amount,p); Bukkit.getScheduler().runTask(plugin,()->{if(t.isValid())t.setVelocity(keep);}); }
  private void trueHurt(LivingEntity t,Player p,double amount){ trueDamageSources.put(t.getUniqueId(),p); t.damage(amount,p); trueDamageSources.remove(t.getUniqueId()); }
  private boolean ready(Player p,Gem g,String a,int seconds){String k=p.getUniqueId()+":"+a;long n=System.currentTimeMillis();if(cooldowns.getOrDefault(UUID.nameUUIDFromBytes(k.getBytes()),0L)>n)return false;cooldowns.put(UUID.nameUUIDFromBytes(k.getBytes()),n+(long)(value(g,a,"cooldown",seconds)*1000D));return true;}
  private long durationMillis(Gem g,String ability,int fallback){return (long)(value(g,ability,"duration",fallback)*1000D);}
  private double value(Gem g,String ability,String field,double fallback){return ConfigValueCache.getDouble(plugin,"fire."+ability+".energy."+g.energy+"."+field,ConfigValueCache.getDouble(plugin,"fire."+ability+"."+field,fallback));}
  private long chargeMillis(){return ConfigValueCache.getInt(plugin,"fire.fireball.chargeSeconds",10)*1000L;}
  private void renderActionBar(){for(Player p:Bukkit.getOnlinePlayers()){Gem g=held(p);if(g==null||g.energy<1||plugin.isGemsDisabled())continue;StringBuilder bar=new StringBuilder("§6☀ ʜᴇᴀᴛᴡᴀᴠᴇ §f").append(cooldownStatus(p,"heatwave"));bar.append(" §8| ");if(g.tier==2){bar.append("§6🔥 ᴄᴀᴍᴘꜰɪʀᴇ §f").append(cooldownStatus(p,"campfire"));bar.append(" §8| §6☄ ꜰɪʀᴇʙᴀʟʟ §f").append(cooldownStatus(p,"fireball"));}else bar.append("§8🔥 ᴄᴀᴍᴘꜰɪʀᴇ §8(T2) §8| §8☄ ꜰɪʀᴇʙᴀʟʟ §8(T2)");ActionBarQueue.enqueue(p,bar.toString());}}
  private String cooldownStatus(Player p,String ability){UUID id=UUID.nameUUIDFromBytes((p.getUniqueId()+":"+ability).getBytes());long remaining=Math.max(0L,cooldowns.getOrDefault(id,0L)-System.currentTimeMillis());return remaining==0L?"§aReady":"§c"+Math.max(1L,(remaining+999L)/1000L)+"s";}
  private static boolean enemy(Player p,Entity e){return e!=p && (!(e instanceof Player q)||!Bliss.getInstance().trustCommand.isAbilityActive(p.getUniqueId(),q.getUniqueId()));}
  private static boolean left(Action a){return a==Action.LEFT_CLICK_AIR||a==Action.LEFT_CLICK_BLOCK;} private static boolean right(Action a){return a==Action.RIGHT_CLICK_AIR||a==Action.RIGHT_CLICK_BLOCK;} private static boolean click(Action a){return left(a)||right(a);}
  private static Material smelt(Material m){return switch(m){case IRON_ORE,DEEPSLATE_IRON_ORE,RAW_IRON->Material.IRON_INGOT;case GOLD_ORE,DEEPSLATE_GOLD_ORE,NETHER_GOLD_ORE,RAW_GOLD->Material.GOLD_INGOT;case COPPER_ORE,DEEPSLATE_COPPER_ORE,RAW_COPPER->Material.COPPER_INGOT;case SAND,RED_SAND->Material.GLASS;case COBBLESTONE->Material.STONE;default->null;};}
  private Gem held(Player p){Gem a=gem(p.getInventory().getItemInMainHand());return a!=null?a:gem(p.getInventory().getItemInOffHand());} private static Gem gem(ItemStack i){if(i==null||!i.hasItemMeta())return null;ItemMeta m=i.getItemMeta();if(!m.hasCustomModelData())return null;int model=m.getCustomModelData();int t=i.getType()==Material.AMETHYST_SHARD&&(model==1||model==21||model==41||model==61||model==81)?1:i.getType()==Material.PRISMARINE_SHARD&&(model==2||model==22||model==42||model==62||model==82)?2:0;if(t==0)return null;String s=m.hasLore()?String.join(" ",m.getLore()).replaceAll("§.",""):"";java.util.regex.Matcher x=java.util.regex.Pattern.compile("Energy: ([0-9]+)").matcher(s);int e=x.find()?Integer.parseInt(x.group(1)):s.contains("Useless")?0:5;return new Gem(t,e);} 
  private void refreshLore(Player p,Gem g){for(ItemStack i:p.getInventory().getContents()){Gem itemGem=gem(i);if(itemGem!=null)refreshLore(i,itemGem);}}
  private void refreshLore(ItemStack i,Gem g){ItemMeta m=i.getItemMeta();List<String> l=new ArrayList<>(List.of("§7[Class: §fController§7]","","§6§lᴘᴀѕѕɪᴠᴇѕ","§8=====================================","§f◉ ꜰɪʀᴇ ʀᴇѕɪѕᴛᴀɴᴄᴇ","§f◉ ᴀᴜᴛᴏ ѕᴍᴇʟᴛ",""));l.add(g.tier==2?"§f◉ ꜰʟᴀᴍᴇѕᴛʀɪᴋᴇ":"§8◉ ꜰʟᴀᴍᴇѕᴛʀɪᴋᴇ §8(Tier 2 only)");l.addAll(List.of("","§6§lᴀʙɪʟɪᴛɪᴇѕ","§8=====================================","§f◉ ʜᴇᴀᴛᴡᴀᴠᴇ [Shift-Left]","§7 ➥ Release 3 bursts of heat that deal damage","§7 ➥ Push nearby targets back slightly","§7 ➥ -20% Movement Speed to targets hit","§8[§bDuration: 3s §8| §cCooldown: 40s§8]","§7(Available at Tier 1)",""));ability(l,"ᴄᴀᴍᴘꜰɪʀᴇ [Shift-Right]",new String[]{"Targets entering or leaving its range take 3 damage","-20% Movement Speed to nearby targets","Slowly saturates nearby allies"},g.tier==2,"20s","60s");l.add("");ability(l,"ꜰɪʀᴇʙᴀʟʟ [Offhand]",new String[]{"Charge then use again to launch","Deals 2 True damage in an area","Max charge deals 4 True damage and leaves a zone","Zone deals fire damage and -40% Movement Speed"},g.tier==2,"10s","90s");l.add("");l.add("§8Energy: "+g.energy+"/10");m.setLore(l);i.setItemMeta(m);}
  public ItemStack createMenuPreview(ItemStack item){ItemStack preview=item.clone();Gem g=gem(preview);if(g!=null)refreshLore(preview,g);return preview;}
  private static void ability(List<String>l,String n,String[]d,boolean on,String dur,String cd){if(n.contains("ᴄᴀᴍᴘ")){dur="8-10s";cd="50-40s";}if(n.contains("ꜰɪʀᴇʙᴀʟʟ")){dur="5-7s";cd="70-60s";}String c=on?"§f":"§8";l.add(c+"◉ "+n);for(String s:d)l.add(c+" ➥ "+s);l.add(on?"§8[§bDuration: "+dur+" §8| §cCooldown: "+cd+"§8]":"§8[Duration: "+dur+" | Cooldown: "+cd+"]");l.add(on?"§7(Unlocks at Tier 2)":"§8(Unlocks at Tier 2)");}
  public void shutdown(){task.cancel();ActionBarQueue.unregisterRenderer(actionBarRenderer);for(ChargeState state:charging.values())state.remove();charging.clear();for(Campfire c:campfires.values())if(c.location.getBlock().getType()==Material.CAMPFIRE)c.location.getBlock().setType(Material.AIR);campfires.clear();}
  public void resetCooldowns(Player p){ChargeState state=charging.remove(p.getUniqueId());if(state!=null)state.remove();cooldowns.clear();}
  record Gem(int tier,int energy){}
  private static final class Campfire{final Location location;final long until;final double radius;final int slowAmplifier;final Set<UUID>inside=new HashSet<>();final Map<UUID,Long>lastBoundaryDamage=new HashMap<>();Campfire(Location l,long u,double r,int slow){location=l;until=u;radius=r;slowAmplifier=slow;}}
  private static final class ChargeState {
    final Gem gem; final long startedAt; final long maxChargeAt; final long expiresAt; final BossBar bar; final ItemDisplay display;
    private ChargeState(Gem gem,long started,long max,long expires,BossBar bar,ItemDisplay display){this.gem=gem;startedAt=started;maxChargeAt=max;expiresAt=expires;this.bar=bar;this.display=display;}
    static ChargeState create(Player player,Gem gem,long now){long max=now+Bliss.getInstance().fireGemRevamp.chargeMillis();long expires=max+ConfigValueCache.getInt(Bliss.getInstance(),"fire.fireball.launchWindowSeconds",5)*1000L;BossBar bar=Bukkit.createBossBar("§cFireball: 0%",BarColor.RED,BarStyle.SOLID);bar.addPlayer(player);ItemDisplay display=player.getWorld().spawn(player.getLocation().add(0,2.15,0),ItemDisplay.class);display.setItemStack(new ItemStack(Material.FIRE_CHARGE));return new ChargeState(gem,now,max,expires,bar,display);}
    void update(Player player,long now){display.teleport(player.getLocation().add(0,2.15,0));boolean maxed=now>=maxChargeAt;double progress=maxed?Math.max(0D,(expiresAt-now)/(double)(expiresAt-maxChargeAt)):Math.min(1D,(now-startedAt)/(double)(maxChargeAt-startedAt));bar.setProgress(progress);bar.setTitle(maxed?"§cFireball: launch now":"§cFireball: "+(int)(progress*100D)+"%");int particles=maxed?30:Math.max(2,(int)(progress*22D));player.getWorld().spawnParticle(Particle.FLAME,display.getLocation(),particles,.28,.28,.28,.015);}
    void remove(){bar.removeAll();if(display.isValid())display.remove();}
  }
}
