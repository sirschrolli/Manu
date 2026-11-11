package io.github.sirschrolli.kingdomsatwar.item.creator;

import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineFx;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineShotEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Der Speer des Schöpfers (Beute Schrollis; bis 0.77 „Zepter des Schöpfers“): ein langer Speer aus Gold und Licht mit einer
 * leuchtenden Klinge, unzerbrechlich. Seit 0.78 stärker:
 * <ul>
 *     <li>Nahkampf: {@value #MELEE_DAMAGE} Schaden, große Reichweite; jeder dritte Treffer (in kurzer Folge) entfesselt eine
 *     <b>Lichtwelle</b> – {@value #WAVE_DAMAGE} Schaden für alle Feinde ringsum, die zurückgeworfen werden;</li>
 *     <li>Rechtsklick: ein Lichtspeer fliegt in Blickrichtung ({@value #SPEAR_DAMAGE} Schaden, alle halbe Sekunde);</li>
 *     <li>schleichend Rechtsklick: auf die Stelle, die man ansieht (bis 32 Blöcke), fährt nach einem Augenblick eine Säule
 *     aus Licht herab – {@value #PILLAR_DAMAGE} Schaden für alle Feinde darin (alle 6 Sekunden).</li>
 * </ul>
 */
public class CreatorScepterItem extends Item implements io.github.sirschrolli.kingdomsatwar.rules.AbilityWeapon {
    public static final double MELEE_DAMAGE = 22.0;
    public static final int SPEAR_COOLDOWN = 10;
    public static final int PILLAR_COOLDOWN = 120;
    public static final float SPEAR_DAMAGE = 18.0f;
    public static final float PILLAR_DAMAGE = 30.0f;
    public static final float PILLAR_RADIUS = 3.2f;
    public static final float WAVE_DAMAGE = 10.0f;
    public static final double WAVE_RADIUS = 4.0;
    /** So schnell müssen die drei Treffer aufeinander folgen (Ticks zwischen zweien). */
    public static final int COMBO_WINDOW = 50;
    /** Wer zuletzt wann getroffen hat und wie oft in Folge (nur auf dem Server). */
    private static final java.util.Map<java.util.UUID, long[]> COMBO = new java.util.HashMap<>();

    public CreatorScepterItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            if (player.isShiftKeyDown()) {
                Vec3 eye = player.getEyePosition();
                Vec3 to = eye.add(player.getLookAngle().scale(32.0));
                BlockHitResult hit = server.clip(new ClipContext(eye, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                if (hit.getType() != HitResult.Type.BLOCK) {
                    return InteractionResult.FAIL;
                }
                Vec3 at = hit.getLocation();
                callPillar(server, player, at, stack);
                player.getCooldowns().addCooldown(stack, PILLAR_COOLDOWN);
            } else {
                Vec3 dir = player.getLookAngle();
                Vec3 from = player.getEyePosition().add(dir.scale(0.9)).add(0.0, -0.15, 0.0);
                DivineShotEntity.thrownSpear(server, player, from, dir, 3.0, SPEAR_DAMAGE).weapon(stack);
                player.getCooldowns().addCooldown(stack, SPEAR_COOLDOWN);
            }
            server.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CREATOR_SCEPTER_CAST.get(), SoundSource.PLAYERS, 1.0f,
                    player.isShiftKeyDown() ? 0.7f : 0.95f + server.getRandom().nextFloat() * 0.15f);
        }
        return InteractionResult.SUCCESS;
    }

    /** Jeder dritte Treffer in kurzer Folge: eine Lichtwelle um das Ziel, die alle Feinde ringsum trifft und zurückwirft. */
    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel level) || !(attacker instanceof Player player)) {
            return;
        }
        long now = level.getGameTime();
        long[] combo = COMBO.computeIfAbsent(player.getUUID(), id -> new long[] {now, 0});
        combo[1] = now - combo[0] <= COMBO_WINDOW ? combo[1] + 1 : 1;
        combo[0] = now;
        if (combo[1] < 3) {
            return;
        }
        combo[1] = 0;
        lightWave(level, player, target.position(), stack);
    }

    /** Die Lichtwelle: ein goldener Ring läuft über den Boden, alle Feinde ringsum werden getroffen und zurückgeworfen. */
    public static void lightWave(ServerLevel level, Player player, Vec3 at) {
        lightWave(level, player, at, ItemStack.EMPTY);
    }

    /** Die Lichtwelle des Speers {@code weapon}: Seine Verzauberungen verstärken sie. */
    public static void lightWave(ServerLevel level, Player player, Vec3 at, ItemStack weapon) {
        DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, at.add(0.0, 0.1, 0.0), DivineFx.GOLD, 16, 0.6f).param(0.32f).spawn();
        DivineFx.flash(level, at.add(0.0, 1.0, 0.0), DivineFx.HOLY);
        DivineFx.sparks(level, at.add(0.0, 0.8, 0.0), 30, 1.2, 0.2);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 0.6, at.z, 20, 1.6, 0.3, 1.6, 0.08);
        DivineFx.sound(level, at, ModSounds.SCHROLLI_BLAST.get(), 1.0f, 1.3f);
        AABB box = new AABB(at, at).inflate(WAVE_RADIUS, 2.5, WAVE_RADIUS);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> foe(player, e))) {
            net.minecraft.world.damagesource.DamageSource source = level.damageSources().indirectMagic(player, player);
            if (DivineFx.flat(e.position(), at) <= WAVE_RADIUS
                    && e.hurtServer(level, source, io.github.sirschrolli.kingdomsatwar.rules.WeaponPower.ability(level, weapon, e, source, WAVE_DAMAGE))) {
                Vec3 away = e.position().subtract(at).multiply(1.0, 0.0, 1.0);
                away = away.lengthSqr() < 1.0e-4 ? player.getLookAngle().multiply(1.0, 0.0, 1.0) : away.normalize();
                e.push(away.x * 0.9, 0.35, away.z * 0.9);
                e.needsSync = true;
            }
        }
    }

    /** Ein Warnkreis – dann eine Säule aus Licht, die alle Feinde darin trifft und hochschleudert. */
    public static void callPillar(ServerLevel level, Player player, Vec3 at) {
        callPillar(level, player, at, ItemStack.EMPTY);
    }

    /** Die Lichtsäule des Speers {@code weapon}: Seine Verzauberungen verstärken sie. */
    public static void callPillar(ServerLevel level, Player player, Vec3 at, ItemStack weapon) {
        ItemStack held = weapon.copy();
        DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, at.add(0.0, 0.06, 0.0), DivineFx.GOLD, 14, PILLAR_RADIUS).onExpire(fx -> {
            DivineFx.fx(level, SpellFxEntity.Kind.PILLAR, at, DivineFx.HOLY, 18, PILLAR_RADIUS).param(24.0f).spawn();
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, at.add(0.0, 0.1, 0.0), DivineFx.GOLD, 14, PILLAR_RADIUS).param(0.25f).spawn();
            DivineFx.flash(level, at.add(0.0, 1.0, 0.0), DivineFx.HOLY);
            level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 1.0, at.z, 16, 1.0, 2.0, 1.0, 0.2);
            DivineFx.sound(level, at, ModSounds.SCHROLLI_PILLAR.get(), 1.8f, 1.2f);
            AABB box = new AABB(at.x - PILLAR_RADIUS, at.y - 1.0, at.z - PILLAR_RADIUS, at.x + PILLAR_RADIUS, at.y + 20.0, at.z + PILLAR_RADIUS);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> foe(player, e))) {
                net.minecraft.world.damagesource.DamageSource source = level.damageSources().indirectMagic(player, player);
                if (DivineFx.flat(e.position(), at) <= PILLAR_RADIUS + 0.3
                        && e.hurtServer(level, source, io.github.sirschrolli.kingdomsatwar.rules.WeaponPower.ability(level, held, e, source, PILLAR_DAMAGE))) {
                    e.push(0.0, 0.8, 0.0);
                    e.needsSync = true;
                }
            }
        }).spawn();
    }

    private static boolean foe(Player player, LivingEntity e) {
        if (e == player || !e.isAlive() || e instanceof ArmorStand) {
            return false;
        }
        if (e instanceof Player other) {
            return player.canHarmPlayer(other);
        }
        if (e instanceof OwnableEntity pet && pet.getOwner() == player) {
            return false;
        }
        return !(e instanceof io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity soldier && player.getUUID().equals(soldier.getOwnerId()));
    }
}
