package io.github.sirschrolli.kingdomsatwar.army;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Kommandostab – damit befehligt man alle eigenen Soldaten im Umkreis von 48 Blöcken:
 * <ul>
 *     <li>auf einen Gegner zeigen und klicken: <b>Angriff</b></li>
 *     <li>auf einen Block zeigen und klicken: dort <b>Stellung halten</b> (mit Schleichen: <b>Patrouille</b>)</li>
 *     <li>in den Himmel zeigen und klicken: alle <b>folgen</b></li>
 *     <li>schleichend einen eigenen Soldaten anklicken: aus dem Dienst <b>entlassen</b></li>
 * </ul>
 */
public class CommandBatonItem extends Item {
    private static final double REACH = SoldierEntity.COMMAND_RANGE;

    public CommandBatonItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            List<SoldierEntity> soldiers = SoldierEntity.soldiersOf(player, SoldierEntity.COMMAND_RANGE);
            if (soldiers.isEmpty()) {
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.command.none").withStyle(ChatFormatting.GRAY));
            } else {
                this.command(serverLevel, player, soldiers);
            }
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 10);
        }
        return InteractionResult.SUCCESS;
    }

    private void command(ServerLevel level, Player player, List<SoldierEntity> soldiers) {
        HitResult hit = pick(player);
        SoldierEntity speaker = soldiers.stream().min(Comparator.comparingDouble(s -> s.distanceToSqr(player))).orElseThrow();
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target) {
            // Angriff
            for (SoldierEntity soldier : soldiers) {
                soldier.setTarget(target);
            }
            speaker.speak("attack");
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, target.getX(), target.getY() + target.getBbHeight() + 0.4, target.getZ(),
                    3, 0.3, 0.2, 0.3, 0.0);
            this.feedback(level, player, Component.translatable("message.kingdomsatwar.command.attack", soldiers.size(), target.getDisplayName()),
                    1.4f);
        } else if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            // Stellung halten oder Patrouille rund um den Block
            BlockPos center = blockHit.getBlockPos().relative(blockHit.getDirection());
            boolean patrol = player.isSecondaryUseActive();
            List<BlockPos> posts = Formation.around(level, center, soldiers.size());
            soldiers.sort(Comparator.comparingDouble(s -> s.distanceToSqr(Vec3.atCenterOf(blockHit.getBlockPos()))));
            for (int i = 0; i < soldiers.size(); i++) {
                SoldierEntity soldier = soldiers.get(i);
                BlockPos post = posts.get(Math.min(i, posts.size() - 1));
                soldier.setTarget(null);
                soldier.setMode(patrol ? SoldierEntity.MODE_PATROL : SoldierEntity.MODE_HOLD, post);
                soldier.getNavigation().moveTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, 1.1);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, post.getX() + 0.5, post.getY() + 0.2, post.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0.0);
                soldier.salute();
            }
            speaker.speak("hold");
            this.feedback(level, player, Component.translatable(patrol ? "message.kingdomsatwar.command.patrol" : "message.kingdomsatwar.command.hold",
                    soldiers.size()), 0.8f);
        } else {
            // Alle folgen
            for (SoldierEntity soldier : soldiers) {
                soldier.setTarget(null);
                soldier.setMode(SoldierEntity.MODE_FOLLOW, null);
                soldier.salute();
            }
            speaker.speak("follow");
            this.feedback(level, player, Component.translatable("message.kingdomsatwar.command.follow", soldiers.size()), 1.0f);
        }
    }

    private void feedback(ServerLevel level, Player player, Component message, float pitch) {
        player.sendOverlayMessage(message.copy().withStyle(ChatFormatting.GOLD));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.COMMAND_BATON.get(), SoundSource.PLAYERS, 0.6f, pitch);
    }

    /** Wohin zeigt der Spieler? Auf ein Lebewesen (kein Verbündeter), einen Block – oder ins Leere. */
    private static @Nullable HitResult pick(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(REACH));
        BlockHitResult blockHit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        Vec3 limit = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eye, limit, new AABB(eye, limit).inflate(1.0),
                entity -> entity instanceof LivingEntity living && living.isAlive() && !entity.isSpectator() && isEnemyOf(player, entity),
                eye.distanceToSqr(limit));
        if (entityHit != null) {
            return entityHit;
        }
        return blockHit.getType() == HitResult.Type.MISS ? null : blockHit;
    }

    private static boolean isEnemyOf(Player player, Entity entity) {
        if (entity == player || entity.isAlliedTo(player) || player.isAlliedTo(entity)) {
            return false;
        }
        return !(entity instanceof SoldierEntity soldier && soldier.isOwnedBy(player));
    }

    /** Schleichend einen eigenen Soldaten anklicken: Er wird aus dem Dienst entlassen. Sonst: Zustand anzeigen. */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof SoldierEntity soldier) || !soldier.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }
        if (player.level() instanceof ServerLevel level) {
            if (player.isSecondaryUseActive()) {
                Component name = soldier.getDisplayName();
                level.sendParticles(ParticleTypes.POOF, soldier.getX(), soldier.getY() + 1.0, soldier.getZ(), 12, 0.3, 0.6, 0.3, 0.02);
                soldier.discard();
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.command.dismissed", name).withStyle(ChatFormatting.GRAY));
            } else {
                player.sendOverlayMessage(soldier.statusMessage());
            }
        }
        return InteractionResult.SUCCESS;
    }
}
