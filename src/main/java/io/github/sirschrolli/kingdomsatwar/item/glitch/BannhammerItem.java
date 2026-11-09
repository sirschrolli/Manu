package io.github.sirschrolli.kingdomsatwar.item.glitch;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Der Bannhammer: Annis Hammer – ein Steinklotz mit Eisenbändern an einem Baumstamm-Stiel, dessen vordere Hälfte fehlende
 * Textur ist. Schwer und langsam, zerschmettert Schilde.
 * <ul>
 *     <li><b>Ruckler</b>: Jeder dritte Treffer lässt das Ziel ruckeln – es hängt einen Augenblick fest (stark verlangsamt) und
 *     wird an die Stelle zurückgerissen, an der es vor einer Sekunde stand;</li>
 *     <li><b>Bann!</b>: Jeder fünfte Treffer bannt alles ringsum – ein Knall aus Pixeln trifft alle Gegner in dreieinhalb
 *     Blöcken und wirft sie empor.</li>
 * </ul>
 */
public class BannhammerItem extends Item {
    private static final String HITS = KingdomsAtWar.MODID + ":bannhammer_hits";
    /** Wo Wesen vor einer Sekunde standen (für den Ruckler): je Wesen ein kurzer Verlauf (nur Server). */
    private static final Map<UUID, ArrayDeque<Vec3>> TRAILS = new HashMap<>();

    public BannhammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity mob, LivingEntity attacker) {
        super.postHurtEnemy(stack, mob, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag data = attacker.getPersistentData();
        int hits = data.getIntOr(HITS, 0) + 1;
        data.putInt(HITS, hits >= 15 ? 0 : hits);
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), mob.getX(), mob.getY(0.6), mob.getZ(), 10, 0.3, 0.4, 0.3, 0.08);
        if (hits % 5 == 0) {
            this.ban(level, mob, attacker);
        } else if (hits % 3 == 0) {
            this.jolt(level, mob, attacker);
        }
    }

    /** Ruckler: Das Ziel hängt fest und wird an seine Stelle von vor einer Sekunde zurückgerissen. */
    private void jolt(ServerLevel level, LivingEntity mob, LivingEntity attacker) {
        mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 4), attacker);
        ArrayDeque<Vec3> trail = TRAILS.get(mob.getUUID());
        Vec3 back = trail != null && !trail.isEmpty() ? trail.peekFirst() : null;
        if (back != null && back.distanceToSqr(mob.position()) < 8.0 * 8.0 && level.noCollision(mob, mob.getBoundingBox().move(back.subtract(mob.position())))) {
            level.sendParticles(ModParticles.GLITCH_PIXEL.get(), mob.getX(), mob.getY(0.5), mob.getZ(), 16, 0.3, 0.5, 0.3, 0.05);
            if (mob instanceof ServerPlayer player) {
                player.teleportTo(level, back.x, back.y, back.z, java.util.Set.of(), player.getYRot(), player.getXRot(), true);
            } else {
                mob.teleportTo(back.x, back.y, back.z);
            }
            mob.setDeltaMovement(Vec3.ZERO);
            mob.needsSync = true;
        }
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), mob.getX(), mob.getY(0.5), mob.getZ(), 16, 0.3, 0.5, 0.3, 0.05);
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), ModSounds.BANNHAMMER_JOLT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /** Bann! Ein Knall aus Pixeln trifft alle Gegner ringsum und wirft sie empor. */
    private void ban(ServerLevel level, LivingEntity mob, LivingEntity attacker) {
        Vec3 c = mob.position();
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), c.x, c.y + 0.6, c.z, 60, 1.4, 0.4, 1.4, 0.2);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFF800F8), c.x, c.y + 0.8, c.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, c.x, c.y, c.z, ModSounds.BANNHAMMER_BAN.get(), SoundSource.PLAYERS, 1.6f, 1.0f);
        if (attacker instanceof Player player) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.bannhammer.ban").withColor(0xF800F8));
        }
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(3.5, 2.0, 3.5),
                e -> e != attacker && e.isAlive() && !(e instanceof ArmorStand) && !(e instanceof Player p && (p.isCreative() || p.isSpectator()))
                        && !(e instanceof TamableAnimal pet && pet.isOwnedBy(attacker)) && !e.isAlliedTo(attacker))) {
            if (victim.distanceToSqr(c) > 3.5 * 3.5) {
                continue;
            }
            victim.hurtServer(level, level.damageSources().indirectMagic(attacker, attacker), 10.0f);
            victim.push(0.0, 0.7, 0.0);
            victim.needsSync = true;
        }
    }

    /** Merkt sich (auf dem Server) die Stellen der Wesen in der Nähe von Trägern des Hammers – für den Ruckler. */
    public static void track(ServerLevel level) {
        if (level.getGameTime() % 5 != 0) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (!player.getMainHandItem().is(io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.BANNHAMMER.get())) {
                continue;
            }
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(12.0), e -> e != player)) {
                ArrayDeque<Vec3> trail = TRAILS.computeIfAbsent(e.getUUID(), k -> new ArrayDeque<>());
                trail.addLast(e.position());
                while (trail.size() > 4) {
                    trail.removeFirst();
                }
            }
        }
        if (TRAILS.size() > 512) {
            TRAILS.clear();
        }
    }
}
