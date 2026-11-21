package io.github.sirschrolli.kingdomsatwar.entity.dragon;

import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.war.RealmState;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Drachen im Krieg. Mit dem Drachenhorn befiehlt der Herr allen eigenen Drachen in Hörweite (160 Blöcke), die schon
 * fliegen können und gerade niemanden tragen:
 * <ul>
 *   <li>auf einen Gegner zeigen: <b>Angriff</b> – die Drachen steigen auf und stoßen im Sturzflug mit ihrem Odem auf ihn
 *   herab, ist er gefallen, nehmen sie sich die Gegner gleich daneben vor;</li>
 *   <li>auf einen Ort zeigen (eine Burg, ein Heerlager): <b>Luftangriff</b> – die Drachen kreisen darüber und stoßen auf
 *   jeden Gegner im Umkreis von 20 Blöcken herab, bis keiner mehr da ist (höchstens zwei Minuten);</li>
 *   <li>in den Himmel zeigen oder schleichend blasen: <b>Rückkehr</b> zum Herrn.</li>
 * </ul>
 * Gegner sind Monster, Banditen und die Krieger der Reiche, mit denen der Herr im Krieg liegt – nie Spieler, nie
 * Verbündete. Kommt ein Heerhaufen gegen die Siedlung, fliegen die Drachen in der Nähe von selbst zur Verteidigung auf.
 * Schwer verwundet (ein Viertel des Lebens) bricht ein Drache den Angriff ab und kehrt zu seinem Herrn zurück. Bauten
 * nimmt der Odem nicht auseinander – dafür gibt es Belagerungswaffen.
 */
public final class DragonWar {
    /** So weit hören Drachen das Horn (und so weit zeigt es). */
    public static final double HORN_RANGE = 160.0;
    /** Umkreis eines Luftangriffs. */
    public static final double STRIKE_RADIUS = 20.0;
    /** So lange gilt ein Luftangriff höchstens. */
    public static final int STRIKE_TICKS = 2400;
    /** So lange gilt ein Angriffsbefehl auf ein Ziel höchstens. */
    public static final int ATTACK_TICKS = 2400;
    /** Ohne Gegner im Umkreis kehren die Drachen nach dieser Zeit von einem Luftangriff zurück. */
    public static final int IDLE_TICKS = 600;
    /** Unter diesem Anteil am Leben bricht ein Drache den Angriff ab. */
    public static final float RETREAT_HEALTH = 0.25f;
    /** Bei einem Heerhaufen: so weit um das Banner verteidigen die Drachen die Siedlung. */
    public static final double DEFEND_RADIUS = 30.0;

    private DragonWar() {}

    /** Kann dieser Drache in den Krieg ziehen (gehört jemandem, fliegt schon, trägt niemanden, lebt)? */
    public static boolean canFight(DragonEntity dragon) {
        return dragon.isAlive() && dragon.isTame() && dragon.getStage().canFly() && dragon.getStage().canBreathe() && !dragon.isVehicle();
    }

    /** Die eigenen kampffähigen Drachen des Spielers {@code owner} im Umkreis {@code range} um {@code center}. */
    public static List<DragonEntity> warDragons(ServerLevel level, UUID owner, Vec3 center, double range) {
        return level.getEntitiesOfClass(DragonEntity.class, new AABB(center, center).inflate(range),
                d -> canFight(d) && d.getOwnerReference() != null && owner.equals(d.getOwnerReference().getUUID())
                        && d.distanceToSqr(center) < range * range);
    }

    /**
     * Ist das ein Gegner des Drachen? Monster und Banditen (keine Creeper und keine „neutralen“ Wesen wie Endermen), die
     * Krieger der Reiche im Krieg mit seinem Herrn – nie Spieler, Drachen oder Verbündete.
     */
    public static boolean isEnemy(DragonEntity dragon, LivingEntity entity) {
        if (!entity.isAlive() || entity == dragon || entity instanceof Player || entity instanceof DragonEntity || entity.isSpectator()
                || DragonBreath.isAlly(dragon, entity)) {
            return false;
        }
        if (entity instanceof RealmWarriorEntity warrior) {
            UUID owner = dragon.getOwnerReference() == null ? null : dragon.getOwnerReference().getUUID();
            return owner != null && dragon.level() instanceof ServerLevel level && RealmState.of(level).atWar(warrior.realm(), owner);
        }
        return entity instanceof Enemy && !(entity instanceof Creeper) && !(entity instanceof NeutralMob);
    }

    /** Der nächste Gegner des Drachen im Umkreis {@code radius} um {@code center} ({@code null}: keiner). */
    public static @Nullable LivingEntity nearestEnemy(ServerLevel level, DragonEntity dragon, Vec3 center, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius, radius * 0.75, radius),
                        e -> isEnemy(dragon, e) && e.distanceToSqr(center) < radius * radius)
                .stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(dragon))).orElse(null);
    }

    // =====================================================================================
    // Das Drachenhorn
    // =====================================================================================

    /** Was das Horn bewirkt hat. */
    public enum Order {
        NONE, ATTACK, STRIKE, RECALL
    }

    /**
     * Der Spieler bläst das Horn: je nachdem, wohin er zeigt (und ob er schleicht), greifen seine Drachen an, fliegen einen
     * Luftangriff oder kehren zurück.
     */
    public static Order horn(ServerLevel level, ServerPlayer player) {
        level.playSound(null, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(5).value(),
                SoundSource.PLAYERS, 4.0f, 0.75f);
        List<DragonEntity> dragons = warDragons(level, player.getUUID(), player.position(), HORN_RANGE);
        if (dragons.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.dragon_horn.none").withStyle(ChatFormatting.GRAY));
            return Order.NONE;
        }
        HitResult hit = player.isSecondaryUseActive() ? null : pick(player, dragons.getFirst());
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target) {
            return attack(level, player, dragons, target);
        }
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            return strike(level, player, dragons, blockHit.getBlockPos().relative(blockHit.getDirection()));
        }
        return recall(player, dragons);
    }

    /** Angriff auf ein Ziel. */
    public static Order attack(ServerLevel level, @Nullable ServerPlayer player, List<DragonEntity> dragons, LivingEntity target) {
        for (DragonEntity dragon : dragons) {
            dragon.orderAttack(target);
        }
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, target.getX(), target.getY() + target.getBbHeight() + 0.5, target.getZ(), 4, 0.4, 0.2,
                0.4, 0.0);
        if (player != null) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.dragon_horn.attack", dragons.size(), target.getDisplayName())
                    .withStyle(ChatFormatting.GOLD));
        }
        return Order.ATTACK;
    }

    /** Luftangriff auf einen Ort. */
    public static Order strike(ServerLevel level, @Nullable ServerPlayer player, List<DragonEntity> dragons, BlockPos at) {
        for (DragonEntity dragon : dragons) {
            dragon.orderStrike(at, STRIKE_RADIUS, STRIKE_TICKS);
        }
        Vec3 c = Vec3.atBottomCenterOf(at);
        level.sendParticles(ParticleTypes.FLAME, c.x, c.y + 0.3, c.z, 30, 1.6, 0.2, 1.6, 0.02);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y + 0.6, c.z, 10, 1.2, 0.3, 1.2, 0.02);
        if (player != null) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.dragon_horn.strike", dragons.size())
                    .withStyle(ChatFormatting.GOLD));
        }
        return Order.STRIKE;
    }

    /** Alle zurück zum Herrn. */
    public static Order recall(@Nullable ServerPlayer player, List<DragonEntity> dragons) {
        for (DragonEntity dragon : dragons) {
            dragon.recall();
        }
        if (player != null) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.dragon_horn.recall", dragons.size())
                    .withStyle(ChatFormatting.GOLD));
        }
        return Order.RECALL;
    }

    /** Worauf zeigt der Spieler? Auf einen Gegner, einen Block – oder in den Himmel ({@code null}). */
    private static @Nullable HitResult pick(Player player, DragonEntity anyDragon) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(HORN_RANGE));
        BlockHitResult blockHit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        Vec3 limit = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eye, limit, new AABB(eye, limit).inflate(1.5),
                entity -> entity instanceof LivingEntity living && isEnemy(anyDragon, living), eye.distanceToSqr(limit));
        if (entityHit != null) {
            return entityHit;
        }
        return blockHit.getType() == HitResult.Type.MISS ? null : blockHit;
    }

    // =====================================================================================
    // Heerhaufen
    // =====================================================================================

    /**
     * Ein Heerhaufen zieht gegen die Siedlung des Spielers {@code owner}: Seine Drachen in der Nähe (auch die sitzenden)
     * steigen auf und verteidigen das Stadtbanner. @return wie viele Drachen aufsteigen
     */
    public static int defend(ServerLevel level, UUID owner, BlockPos banner, int ticks) {
        List<DragonEntity> dragons = warDragons(level, owner, Vec3.atCenterOf(banner), HORN_RANGE);
        for (DragonEntity dragon : dragons) {
            dragon.orderStrike(banner, DEFEND_RADIUS, ticks);
            dragon.roar();
        }
        if (!dragons.isEmpty() && level.getPlayerByUUID(owner) instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.dragon_defend", dragons.size()).withStyle(ChatFormatting.GOLD));
        }
        return dragons.size();
    }

    /** Für Krieger der Reiche: Gehört das Wesen (Spieler, Soldat, Drache) einem Spieler – welchem? */
    public static @Nullable UUID ownerOf(Entity entity) {
        if (entity instanceof Player player) {
            return player.getUUID();
        }
        if (entity instanceof SoldierEntity soldier) {
            return soldier.getOwnerId();
        }
        if (entity instanceof DragonEntity dragon && dragon.getOwnerReference() != null) {
            return dragon.getOwnerReference().getUUID();
        }
        return null;
    }
}
