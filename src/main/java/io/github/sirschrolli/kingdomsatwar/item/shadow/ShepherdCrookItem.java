package io.github.sirschrolli.kingdomsatwar.item.shadow;

import io.github.sirschrolli.kingdomsatwar.enchant.EnchantPower;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Hirtenstab (seit 0.98): die Beute des Seelenhirten – ein langer Stab aus Sumpfeiche, oben ein Haken, an dem eine
 * Laterne voll Seelenfeuer hängt. Im Nahkampf schlägt er wie ein Schwert. Rechtsklick: der <b>Hakenzug</b> – eine Kette
 * aus Seelenlicht schnellt zum anvisierten Wesen und reißt es heran ({@link #PULL_DAMAGE} Schaden, mit Schärfe mehr, dazu
 * kurz Langsamkeit); schwere Wesen bewegen sich kaum. Danach ruht er {@link #COOLDOWN} Ticks.
 */
public class ShepherdCrookItem extends Item {
    public static final int COOLDOWN = 60;
    public static final float PULL_DAMAGE = 4.0f;
    /** So weit reicht die Kette. */
    public static final double RANGE = 16.0;

    public ShepherdCrookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        LivingEntity target = aimed(player);
        if (target == null) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            pull(server, player, target, PULL_DAMAGE + 0.5f * EnchantPower.level(stack, Enchantments.SHARPNESS));
            stack.hurtAndBreak(1, player, hand);
            player.getCooldowns().addCooldown(stack, COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    /** Der Hakenzug: Kette aus Seelenlicht, das Ziel fliegt heran (je nach Standfestigkeit), Schaden und Langsamkeit. */
    public static void pull(ServerLevel level, Player player, LivingEntity target, float damage) {
        Vec3 from = player.getEyePosition().add(player.getViewVector(1.0f).scale(0.8)).add(0.0, -0.25, 0.0);
        SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, from, null, 0x6FE8D8, 10, 0.12f).end(target.getBoundingBox().getCenter()).spawn();
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY(0.6), target.getZ(), 10, 0.3, 0.4, 0.3, 0.04);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SOUL_SHEPHERD_HOOK.get(), SoundSource.PLAYERS, 1.0f, 1.25f);
        // erst der Schaden (sein Rückstoß würde den Zug sonst aufheben), dann der Zug
        target.hurtServer(level, player.damageSources().playerAttack(player), damage);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1), player);
        Vec3 pull = player.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
        double d = pull.length();
        double resist = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double k = Math.min(1.8, 0.45 + d * 0.09) * Math.max(0.0, 1.0 - resist);
        if (d > 1.5 && k > 0.01) {
            target.setDeltaMovement(pull.scale(k / d).add(0.0, 0.32 * Math.max(0.0, 1.0 - resist), 0.0));
            target.needsSync = true;
        }
    }

    /** Das Wesen, auf das der Spieler zielt (nicht er selbst, seine Tiere oder Rüstungsständer), oder null. */
    private static @Nullable LivingEntity aimed(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f);
        Vec3 end = eye.add(look.scale(RANGE));
        AABB box = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.5);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box,
                e -> e instanceof LivingEntity living && living.isAlive() && e != player && !e.isSpectator() && !(e instanceof ArmorStand)
                        && !(e instanceof OwnableEntity pet && player.equals(pet.getOwner())), RANGE * RANGE);
        Entity entity = hit != null ? hit.getEntity() : null;
        return entity instanceof LivingEntity living ? living : null;
    }
}
