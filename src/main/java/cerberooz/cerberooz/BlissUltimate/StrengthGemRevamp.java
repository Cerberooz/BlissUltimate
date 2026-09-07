package cerberooz.cerberooz.BlissUltimate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/** The intentionally small, standardized ability set for Strength gems. */
public final class StrengthGemRevamp implements Listener {
  private final Bliss plugin;
  private final NamespacedKey attackDamageModifierKey;
  private final Map<UUID, FrailerState> frailerStates = new HashMap<>();
  private final Map<UUID, Long> sharpEdgeUntil = new HashMap<>();
  private final Map<UUID, Integer> sharpEdgeCrits = new HashMap<>();
  private final Map<UUID, UUID> sharpEdgeTargets = new HashMap<>();
  private final Map<UUID, Long> hunterArmUntil = new HashMap<>();
  private final Map<UUID, Mark> marks = new HashMap<>();
  private final Map<UUID, Long> cooldowns = new HashMap<>();
  private final Set<UUID> attackDamageApplied = new HashSet<>();
  private final BukkitTask maintenanceTask;
  private final Runnable actionBarRenderer = this::renderActionBar;
  private int frailerParticlePhase;

  public StrengthGemRevamp(Bliss plugin) {
    this.plugin = plugin;
    this.attackDamageModifierKey = new NamespacedKey(plugin, "strength_t2_attack_damage");
    this.maintenanceTask = Bukkit.getScheduler().runTaskTimer(plugin, this::maintainPassives, 1L, 10L);
    ActionBarQueue.registerRenderer(actionBarRenderer);
  }

  /** Shift-left is an arm swing, not a block interaction. */
  @EventHandler(ignoreCancelled = true)
  public void onSwing(PlayerAnimationEvent event) {
    if (plugin.isGemsDisabled()) return;
    Player player = event.getPlayer();
    GemState gem = heldGem(player);
    if (!player.isSneaking() || gem == null || gem.energy() <= 0) return;

    Player target = findFrailerTarget(player, gem);
    if (target != null && tryActivate(player, "frailer", gem)) {
      frailerStates.put(player.getUniqueId(), new FrailerState(target.getUniqueId(), System.currentTimeMillis() + durationMillis(gem, "frailer"), value(gem, "frailer", "maxDistance", 15)));
      player.sendMessage("§4🔮 §fFrailer activated.");
    } else if (target == null) {
      player.sendMessage("§cFrailer requires a visible player target.");
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (plugin.isGemsDisabled() || !isRightClick(event.getAction())) return;
    Player player = event.getPlayer();
    GemState gem = heldGem(player);
    if (gem == null || gem.energy() <= 0) return;

    if (player.isSneaking() && isRightClick(event.getAction()) && gem.tier() == 2) {
      if (tryActivate(player, "sharpEdge", gem)) {
        sharpEdgeUntil.put(player.getUniqueId(), System.currentTimeMillis() + durationMillis(gem, "sharpEdge"));
        sharpEdgeCrits.put(player.getUniqueId(), 0);
        player.sendMessage("§4🔮 §fSharp Edge activated.");
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onSwapHands(PlayerSwapHandItemsEvent event) {
    if (plugin.isGemsDisabled() || event.getPlayer().isSneaking()) return;
    Player player = event.getPlayer();
    GemState gem = heldGem(player);
    if (gem == null || gem.tier() != 2 || gem.energy() <= 0) return;
    if (tryActivate(player, "hunterMark", gem)) {
      hunterArmUntil.put(player.getUniqueId(), System.currentTimeMillis() + durationMillis(gem, "hunterMark"));
      player.sendMessage("§4🔮 §fHunter's Mark armed. Hit an enemy to mark them.");
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDamage(EntityDamageByEntityEvent event) {
    long now = System.currentTimeMillis();
    if (!(event.getDamager() instanceof Player attacker)) return;

    UUID attackerId = attacker.getUniqueId();
    if (event.getEntity() instanceof Player victim) {
      FrailerState attackerFrailer = frailerStates.get(attackerId);
      if (attackerFrailer != null && attackerFrailer.targetId().equals(victim.getUniqueId()) && attackerFrailer.expiresAt() > now) {
        int consecutiveHits = attackerFrailer.consecutiveHits() + 1;
        double bonus = Math.min(value(heldGem(attacker), "frailer", "maxBonusPercent", 15) / 100D,
            .10D + (consecutiveHits - 1) * value(heldGem(attacker), "frailer", "bonusPerConsecutiveHitPercent", 1) / 100D);
        frailerStates.put(attackerId, attackerFrailer.withConsecutiveHits(consecutiveHits));
        event.setDamage(event.getDamage() * (1D + bonus));
      }
    }
    if (sharpEdgeUntil.getOrDefault(attackerId, 0L) > now && event.getEntity() instanceof org.bukkit.entity.LivingEntity) {
      UUID targetId = event.getEntity().getUniqueId();
      UUID previousTarget = sharpEdgeTargets.put(attackerId, targetId);
      if (previousTarget != null && !previousTarget.equals(targetId)) {
        sharpEdgeCrits.put(attackerId, 0);
      }
      int crit = sharpEdgeCrits.merge(attackerId, 1, Integer::sum);
      boolean bonusCrit = crit % bonusCritEvery(heldGem(attacker)) == 0;
      event.setDamage(event.getDamage() * 1.5D * (bonusCrit ? 1.2D : 1.0D));
      spawnCritParticles(event.getEntity(), bonusCrit);
    }
    if (hunterArmUntil.getOrDefault(attackerId, 0L) <= now || !(event.getEntity() instanceof Player target)) return;
    hunterArmUntil.remove(attackerId);
    marks.put(attackerId, new Mark(target.getUniqueId(), now + durationMillis(heldGem(attacker), "hunterMark")));
    target.setGlowing(true);
    attacker.sendMessage("§4🔮 §fMarked " + target.getName() + ".");
  }

  @EventHandler(ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    Mark mark = marks.get(player.getUniqueId());
    if (mark == null || mark.expiresAt() <= System.currentTimeMillis()) return;
    Player target = Bukkit.getPlayer(mark.targetId());
    if (target == null || !target.isOnline() || !target.getWorld().equals(player.getWorld())) return;
    Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
    Vector movement = event.getTo().toVector().subtract(event.getFrom().toVector());
    if (movement.lengthSquared() > 0.0001D && movement.normalize().dot(toTarget) > 0.1D) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 25, 0, true, false, false));
    }
  }

  private void maintainPassives() {
    long now = System.currentTimeMillis();
    for (Player player : Bukkit.getOnlinePlayers()) {
      GemState gem = heldGem(player);
      for (ItemStack item : player.getInventory().getContents()) refreshLore(item);
      refreshLore(player.getInventory().getItemInOffHand());
      boolean active = gem != null && gem.energy() > 0 && !plugin.isGemsDisabled();
      if (active) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 30, 0, true, false, false));
        if (gem.tier() == 2) applyAttackDamage(player); else removeAttackDamage(player);
      } else removeAttackDamage(player);
      updateFrailer(player, now);
      if (sharpEdgeUntil.getOrDefault(player.getUniqueId(), 0L) <= now) sharpEdgeUntil.remove(player.getUniqueId());
      if (!sharpEdgeUntil.containsKey(player.getUniqueId())) sharpEdgeTargets.remove(player.getUniqueId());
      if (sharpEdgeUntil.containsKey(player.getUniqueId())) spawnSharpEdgeAura(player);
      if (hunterArmUntil.getOrDefault(player.getUniqueId(), 0L) <= now) hunterArmUntil.remove(player.getUniqueId());
      Mark mark = marks.get(player.getUniqueId());
      if (mark != null && mark.expiresAt() <= now) {
        Player target = Bukkit.getPlayer(mark.targetId());
        if (target != null) target.setGlowing(false);
        marks.remove(player.getUniqueId());
      }
    }
  }

  private boolean tryActivate(Player player, String ability, GemState gem) {
    String key = "strength." + ability;
    long now = System.currentTimeMillis();
    UUID cooldownId = UUID.nameUUIDFromBytes((player.getUniqueId() + key).getBytes());
    if (cooldowns.getOrDefault(cooldownId, 0L) > now) return false;
    cooldowns.put(cooldownId, now + cooldownMillis(gem, ability));
    return true;
  }

  private void renderActionBar() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      GemState gem = heldGem(player);
      if (gem == null || gem.energy() <= 0 || plugin.isGemsDisabled()) continue;
      StringBuilder bar = new StringBuilder("§4⚔ ꜰʀᴀɪʟᴇʀ §f").append(cooldownStatus(player, "frailer"));
      bar.append(" §8| ");
      if (gem.tier() == 2) {
        bar.append("§4✦ ѕʜᴀʀᴘ ᴇᴅɢᴇ §f").append(cooldownStatus(player, "sharpEdge"));
        bar.append(" §8| §4◉ ʜᴜɴᴛᴇʀ'ѕ ᴍᴀʀᴋ §f").append(cooldownStatus(player, "hunterMark"));
      } else {
        bar.append("§8✦ ѕʜᴀʀᴘ ᴇᴅɢᴇ §8(T2) §8| §8◉ ʜᴜɴᴛᴇʀ'ѕ ᴍᴀʀᴋ §8(T2)");
      }
      ActionBarQueue.enqueue(player, bar.toString());
    }
  }

  private String cooldownStatus(Player player, String ability) {
    UUID id = UUID.nameUUIDFromBytes((player.getUniqueId() + "strength." + ability).getBytes());
    long remaining = Math.max(0L, cooldowns.getOrDefault(id, 0L) - System.currentTimeMillis());
    return remaining == 0L ? "§aReady" : "§c" + Math.max(1L, (remaining + 999L) / 1000L) + "s";
  }

  private long cooldownMillis(GemState gem, String ability) {
    return value(gem, ability, "cooldown", defaultCooldown(ability)) * 1000L;
  }

  private long durationMillis(GemState gem, String ability) {
    return value(gem, ability, "duration", defaultDuration(ability)) * 1000L;
  }

  private int value(GemState gem, String ability, String field, int fallback) {
    String energyPath = "strength." + ability + ".energy." + gem.energy() + "." + field;
    return ConfigValueCache.getInt(plugin, energyPath, ConfigValueCache.getInt(plugin, "strength." + ability + "." + field, fallback));
  }

  private int bonusCritEvery(GemState gem) {
    if (gem == null) return ConfigValueCache.getInt(plugin, "strength.sharpEdge.bonusCritEvery", 5);
    return Math.max(1, value(gem, "sharpEdge", "bonusCritEvery", 5));
  }

  private Player findFrailerTarget(Player player, GemState gem) {
    Entity entity = player.getTargetEntity(value(gem, "frailer", "targetRange", 15));
    return entity instanceof Player target && !target.equals(player) ? target : null;
  }

  private void updateFrailer(Player caster, long now) {
    FrailerState state = frailerStates.get(caster.getUniqueId());
    if (state == null) return;
    Player target = Bukkit.getPlayer(state.targetId());
    if (target == null || !target.isOnline() || !target.getWorld().equals(caster.getWorld()) || state.expiresAt() <= now) {
      frailerStates.remove(caster.getUniqueId());
      return;
    }
    double maxDistance = state.maxDistance();
    if (caster.getLocation().distanceSquared(target.getLocation()) > maxDistance * maxDistance) {
      frailerStates.remove(caster.getUniqueId());
      caster.sendMessage("§cFrailer ended: target is too far away.");
      return;
    }
    spawnFrailerLine(target, caster);
  }

  private void spawnFrailerLine(Player target, Player caster) {
    Vector start = target.getLocation().add(0.0, 1.0, 0.0).toVector();
    Vector end = caster.getLocation().add(0.0, 1.0, 0.0).toVector();
    Vector line = end.clone().subtract(start);
    int points = Math.max(5, (int) Math.ceil(line.length() * 2.0));
    DustOptions dust = new DustOptions(Color.fromRGB(120, 0, 0), 1.15F);
    for (int point = 0; point < points; point++) {
      double progress = ((point + frailerParticlePhase) % points) / (double) points;
      target.getWorld().spawnParticle(Particle.DUST, start.clone().add(line.clone().multiply(progress)).toLocation(target.getWorld()), 1, 0, 0, 0, 0, dust);
    }
    frailerParticlePhase = (frailerParticlePhase + 2) % points;
  }

  private void spawnCritParticles(org.bukkit.entity.Entity target, boolean bonusCrit) {
    Location hit = target.getLocation().add(0.0, 1.0, 0.0);
    target.getWorld().spawnParticle(Particle.CRIT, hit, bonusCrit ? 24 : 14, .3, .45, .3, .08);
    target.getWorld().playSound(hit, Sound.ENTITY_PLAYER_ATTACK_CRIT, bonusCrit ? .9F : .65F, bonusCrit ? 1.35F : 1.0F);
  }

  private void spawnSharpEdgeAura(Player player) {
    Location center = player.getLocation().add(0.0, 0.75, 0.0);
    for (int point = 0; point < 10; point++) {
      double angle = (Math.PI * 2D * point / 10D) + (System.currentTimeMillis() % 1000L) / 1000D;
      Location particle = center.clone().add(Math.cos(angle) * .55D, point % 2 == 0 ? .15D : -.1D, Math.sin(angle) * .55D);
      player.getWorld().spawnParticle(Particle.TRIAL_OMEN, particle, 1, 0, 0, 0, 0);
    }
  }

  private static int defaultDuration(String ability) { return ability.equals("frailer") ? 7 : ability.equals("sharpEdge") ? 7 : 10; }
  private static int defaultCooldown(String ability) { return ability.equals("frailer") ? 30 : ability.equals("sharpEdge") ? 40 : 45; }
  private static boolean isLeftClick(Action action) { return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK; }
  private static boolean isRightClick(Action action) { return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK; }
  private static boolean isRightOrLeftClick(Action action) { return isLeftClick(action) || isRightClick(action); }

  private GemState heldGem(Player player) {
    GemState main = gemState(player.getInventory().getItemInMainHand());
    return main != null ? main : gemState(player.getInventory().getItemInOffHand());
  }

  static GemState gemState(ItemStack item) {
    if (item == null || !item.hasItemMeta()) return null;
    int tier = item.getType() == Material.AMETHYST_SHARD ? 1 : item.getType() == Material.PRISMARINE_SHARD ? 2 : 0;
    if (tier == 0) return null;
    ItemMeta meta = item.getItemMeta();
    if (!meta.hasCustomModelData()) return null;
    int model = meta.getCustomModelData();
    if ((tier == 1 && model != 95 && model != 89 && model != 69 && model != 49 && model != 29 && model != 9)
        || (tier == 2 && model != 96 && model != 90 && model != 70 && model != 50 && model != 30 && model != 10)) return null;
    return new GemState(tier, energyFor(item));
  }

  private static int energyFor(ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    if (meta == null || !meta.hasLore()) return 0;
    String lore = String.join(" ", meta.getLore()).replaceAll("§.", "");
    java.util.regex.Matcher energy = java.util.regex.Pattern.compile("Energy: ([0-9]+)").matcher(lore);
    if (energy.find()) return Integer.parseInt(energy.group(1));
    if (lore.contains("Useless")) return 0;
    if (lore.contains("Ruined")) return 1;
    if (lore.contains("Damaged")) return 2;
    if (lore.contains("Cracked")) return 3;
    if (lore.contains("Scratched")) return 4;
    java.util.regex.Matcher pristine = java.util.regex.Pattern.compile("Pristine(?: \\+([0-9]+))?").matcher(lore);
    return pristine.find() ? 5 + (pristine.group(1) == null ? 0 : Integer.parseInt(pristine.group(1))) : 0;
  }

  private void refreshLore(ItemStack item) {
    GemState gem = gemState(item);
    if (gem == null || item == null || !item.hasItemMeta()) return;
    ItemMeta meta = item.getItemMeta();
    boolean unlocked = gem.energy() > 0;
    boolean tierTwo = gem.tier() == 2 && unlocked;
    ArrayList<String> lore = new ArrayList<>();
    lore.add("§7[Class: §fDuelist§7]");
    lore.add("");
    lore.add("§4§lᴘᴀѕѕɪᴠᴇѕ");
    lore.add("§8=====================================");
    lore.add("§f◉ ᴘᴇʀᴍᴀɴᴇɴᴛ ѕᴛʀᴇɴɢᴛʜ ɪ");
    lore.add(tierTwo ? "§f◉ +1 ᴀᴛᴛᴀᴄᴋ ᴅᴀᴍᴀɢᴇ" : "§7◉ +1 ᴀᴛᴛᴀᴄᴋ ᴅᴀᴍᴀɢᴇ §8(Unlocks at Tier 2)");
    lore.add("");
    lore.add("§4§lᴀʙɪʟɪᴛɪᴇѕ");
    lore.add("§8=====================================");
    addAbilityLore(lore, "ꜰʀᴀɪʟᴇʀ [Shift-Left]", "Deal 10% more damage to target hit", "Consecutive attacks increase the bonus up to 15%", "Skill immediately ends if too far from target", unlocked, gem, "frailer");
    lore.add("");
    addAbilityLore(lore, "ѕʜᴀʀᴘ ᴇᴅɢᴇ [Shift-Right]", "Enables Auto-Crit", "Every " + bonusCritEvery(gem) + "th Crit deals 1.2x damage", "Changing targets resets your Critical Hit progress", tierTwo, gem, "sharpEdge");
    lore.add("");
    addAbilityLore(lore, "ʜᴜɴᴛᴇʀ'ѕ ᴍᴀʀᴋ [Offhand]", "Next target hit is marked with glowing", "Gain Speed I while moving toward the marked target", null, tierTwo, gem, "hunterMark");
    lore.add("");
    lore.add("§8Energy: " + gem.energy() + "/10");
    meta.setLore(lore);
    item.setItemMeta(meta);
  }

  /** Returns a non-mutating, fully current lore preview for menus. */
  public ItemStack createMenuPreview(ItemStack item) {
    ItemStack preview = item.clone();
    refreshLore(preview);
    return preview;
  }

  private void addAbilityLore(ArrayList<String> lore, String title, String first, String second, String third, boolean unlocked, GemState gem, String ability) {
    if (!unlocked) {
      lore.add("§8◉ " + title);
      lore.add("§8 ➥ " + first);
      lore.add("§8 ➥ " + second);
      if (third != null) lore.add("§8 ➥ " + third);
      lore.add("§8[Duration: " + defaultDuration(ability) + "s | Cooldown: " + defaultCooldown(ability) + "s]");
      lore.add("§8(Unlocks at Tier " + (ability.equals("frailer") ? "1" : "2") + ")");
      return;
    }
    lore.add("§f◉ " + title);
    lore.add("§7 ➥ " + first);
    lore.add("§7 ➥ " + second);
    if (third != null) lore.add("§7 ➥ " + third);
    lore.add("§8[§bDuration: " + (durationMillis(gem, ability) / 1000L) + "s §8| §cCooldown: " + (cooldownMillis(gem, ability) / 1000L) + "s§8]");
    lore.add("§7(Available at Tier " + (ability.equals("frailer") ? "1" : "2") + ")");
  }

  private void applyAttackDamage(Player player) {
    if (attackDamageApplied.contains(player.getUniqueId())) return;
    var attribute = player.getAttribute(Attribute.ATTACK_DAMAGE);
    if (attribute != null && attribute.getModifiers().stream().noneMatch(modifier -> modifier.getKey().equals(attackDamageModifierKey))) {
      attribute.addModifier(new AttributeModifier(attackDamageModifierKey, 1.0D, Operation.ADD_NUMBER));
    }
    attackDamageApplied.add(player.getUniqueId());
  }

  private void removeAttackDamage(Player player) {
    if (!attackDamageApplied.remove(player.getUniqueId())) return;
    var attribute = player.getAttribute(Attribute.ATTACK_DAMAGE);
    if (attribute != null) {
      attribute.getModifiers().stream()
          .filter(modifier -> modifier.getKey().equals(attackDamageModifierKey))
          .forEach(attribute::removeModifier);
    }
  }

  public void shutdown() {
    maintenanceTask.cancel();
    ActionBarQueue.unregisterRenderer(actionBarRenderer);
    for (Player player : Bukkit.getOnlinePlayers()) removeAttackDamage(player);
  }

  public void resetCooldowns(Player player) {
    UUID playerId = player.getUniqueId();
    frailerStates.remove(playerId);
    sharpEdgeUntil.remove(playerId);
    sharpEdgeCrits.remove(playerId);
    sharpEdgeTargets.remove(playerId);
    hunterArmUntil.remove(playerId);
    marks.remove(playerId);
    cooldowns.keySet().removeIf(key -> key.equals(UUID.nameUUIDFromBytes((playerId + "strength.frailer").getBytes()))
        || key.equals(UUID.nameUUIDFromBytes((playerId + "strength.sharpEdge").getBytes()))
        || key.equals(UUID.nameUUIDFromBytes((playerId + "strength.hunterMark").getBytes())));
  }

  public static boolean isTierOneGem(ItemStack item) {
    GemState gem = gemState(item);
    return gem != null && gem.tier() == 1;
  }

  public static boolean isTierTwoGem(ItemStack item) {
    GemState gem = gemState(item);
    return gem != null && gem.tier() == 2;
  }

  record GemState(int tier, int energy) {}
  private record FrailerState(UUID targetId, long expiresAt, double maxDistance, int consecutiveHits) {
    private FrailerState(UUID targetId, long expiresAt, double maxDistance) { this(targetId, expiresAt, maxDistance, 0); }
    private FrailerState withConsecutiveHits(int hits) { return new FrailerState(targetId, expiresAt, maxDistance, hits); }
  }
  private record Mark(UUID targetId, long expiresAt) {}
}
