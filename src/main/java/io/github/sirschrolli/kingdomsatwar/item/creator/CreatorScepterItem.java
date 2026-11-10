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
 * Das Zepter des Schöpfers (Beute Schrollis): ein langer Goldstab mit einem achtstrahligen Sonnenstern, unzerbrechlich.
 * <ul>
 *     <li>Nahkampf: 16 Schaden, etwas mehr Reichweite;</li>
 *     <li>Rechtsklick: ein Lichtspeer fliegt in Blickrichtung (12 Schaden, alle 0,7 Sekunden);</li>
 *     <li>schleichend Rechtsklick: auf die Stelle, die man ansieht (bis 32 Blöcke), fährt nach einem Augenblick eine Säule
 *     aus Licht herab – 20 Schaden für alle Feinde darin (alle 8 Sekunden).</li>
 * </ul>
 */
public class CreatorScepterItem extends Item {
    public static final int SPEAR_COOLDOWN = 14;
    public static final int PILLAR_COOLDOWN = 160;
    public static final float SPEAR_DAMAGE = 12.0f;
    public static final float PILLAR_DAMAGE = 20.0f;
    public static final float PILLAR_RADIUS = 2.6f;

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
                callPillar(server, player, at);
                player.getCooldowns().addCooldown(stack, PILLAR_COOLDOWN);
            } else {
                Vec3 dir = player.getLookAngle();
                Vec3 from = player.getEyePosition().add(dir.scale(0.9)).add(0.0, -0.15, 0.0);
                DivineShotEntity.thrownSpear(server, player, from, dir, 2.6, SPEAR_DAMAGE);
                player.getCooldowns().addCooldown(stack, SPEAR_COOLDOWN);
            }
            server.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.CREATOR_SCEPTER_CAST.get(), SoundSource.PLAYERS, 1.0f,
                    player.isShiftKeyDown() ? 0.7f : 1.1f + server.getRandom().nextFloat() * 0.2f);
        }
        return InteractionResult.SUCCESS;
    }

    /** Ein Warnkreis – dann eine Säule aus Licht, die alle Feinde darin trifft und hochschleudert. */
    public static void callPillar(ServerLevel level, Player player, Vec3 at) {
        DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, at.add(0.0, 0.06, 0.0), DivineFx.GOLD, 14, PILLAR_RADIUS).onExpire(fx -> {
            DivineFx.fx(level, SpellFxEntity.Kind.PILLAR, at, DivineFx.HOLY, 16, PILLAR_RADIUS).param(20.0f).spawn();
            DivineFx.flash(level, at.add(0.0, 1.0, 0.0), DivineFx.HOLY);
            level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 1.0, at.z, 16, 1.0, 2.0, 1.0, 0.2);
            DivineFx.sound(level, at, ModSounds.SCHROLLI_PILLAR.get(), 1.8f, 1.2f);
            AABB box = new AABB(at.x - PILLAR_RADIUS, at.y - 1.0, at.z - PILLAR_RADIUS, at.x + PILLAR_RADIUS, at.y + 20.0, at.z + PILLAR_RADIUS);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, e -> foe(player, e))) {
                if (DivineFx.flat(e.position(), at) <= PILLAR_RADIUS + 0.3
                        && e.hurtServer(level, level.damageSources().indirectMagic(player, player), PILLAR_DAMAGE)) {
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
