package io.github.sirschrolli.kingdomsatwar.item.end;

import io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Die Phasenklinge (seit 1.5): die Waffe Phasmors, des Leerwandlerkönigs – ein Zweihänder aus einem einzigen Phasitkristall,
 * die Parierstange aus Purpur, der Griff in violettes Leder gewickelt, im Knauf ein Phasit. Schwer und scharf (etwas mehr
 * Schaden als ein Netheritschwert, etwas langsamer).
 * Rechtsklick: der <b>Phasenhieb</b> – du zerfließt zu einem türkisen Nachbild und stehst bis zu {@link #DASH} Blöcke weiter
 * in Blickrichtung (nie durch Wände); wen du dabei durchquerst, den trifft die Klinge ({@link #DASH_DAMAGE} Schaden, mit
 * Schärfe mehr). Danach ruht sie {@link #COOLDOWN} Ticks.
 */
public class PhaseBladeItem extends Item {
    public static final int COOLDOWN = 100;
    public static final double DASH = 7.0;
    public static final float DASH_DAMAGE = 7.0f;

    public PhaseBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) {
            if (dash(server, sp, DASH_DAMAGE + 1.5f * EnchantPower.level(stack, Enchantments.SHARPNESS)) <= 0.5) {
                return InteractionResult.FAIL;
            }
            stack.hurtAndBreak(2, sp, hand);
            sp.getCooldowns().addCooldown(stack, COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Der Phasenhieb: Der Spieler gleitet in Blickrichtung (waagerecht, über Stufen hinweg) so weit es geht – höchstens
     * {@link #DASH} Blöcke, nie in eine Wand –, und wen er dabei durchquert, der nimmt Schaden. Gibt die Strecke zurück.
     */
    public static double dash(ServerLevel level, ServerPlayer player, float damage) {
        Vec3 start = player.position();
        Vec3 look = player.getViewVector(1.0f).multiply(1.0, 0.0, 1.0);
        if (look.lengthSqr() < 1.0e-4) {
            return 0.0;
        }
        look = look.normalize();
        AABB box = player.getBoundingBox();
        Vec3 end = start;
        for (double d = 0.5; d <= DASH + 1.0e-3; d += 0.5) {
            Vec3 next = end.add(look.scale(0.5));
            if (level.noCollision(player, box.move(next.subtract(start)))) {
                end = next;
                continue;
            }
            // eine Stufe hinauf?
            Vec3 up = next.add(0.0, 1.0, 0.0);
            if (level.noCollision(player, box.move(up.subtract(start)))) {
                end = up;
                continue;
            }
            break;
        }
        double length = end.distanceTo(start);
        if (length <= 0.5) {
            return length;
        }
        // wen er durchquert
        List<LivingEntity> hit = new ArrayList<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().expandTowards(end.subtract(start)).inflate(0.6),
                e -> canHit(player, e))) {
            if (e.getBoundingBox().inflate(0.6).clip(start.add(0.0, 1.0, 0.0), end.add(0.0, 1.0, 0.0)).isPresent()) {
                hit.add(e);
            }
        }
        player.teleportTo(level, end.x, end.y, end.z, Set.of(), player.getYRot(), player.getXRot(), false);
        player.resetFallDistance();
        VoidwalkerArmor.phaseEffects(level, player, start);
        for (double d = 0.0; d < length; d += 0.4) {
            Vec3 p = start.add(end.subtract(start).scale(d / length)).add(0.0, 1.0, 0.0);
            level.sendParticles(new DustParticleOptions(0x3FE6CC, 1.2f), p.x, p.y, p.z, 2, 0.15, 0.4, 0.15, 0.0);
        }
        level.playSound(null, end.x, end.y, end.z, ModSounds.PHASE_BLADE_DASH.get(), SoundSource.PLAYERS, 1.4f,
                0.95f + level.getRandom().nextFloat() * 0.1f);
        for (LivingEntity e : hit) {
            if (e.hurtServer(level, player.damageSources().playerAttack(player), damage)) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, e.getX(), e.getY(0.5), e.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                e.push(look.x * 0.4, 0.2, look.z * 0.4);
                e.needsSync = true;
            }
        }
        return length;
    }

    private static boolean canHit(Player player, Entity e) {
        return e != player && e.isAlive() && !(e instanceof ArmorStand) && !(e instanceof OwnableEntity pet && player.equals(pet.getOwner()))
                && !(e instanceof Player p && (p.isCreative() || p.isSpectator() || !player.canHarmPlayer(p)));
    }
}
