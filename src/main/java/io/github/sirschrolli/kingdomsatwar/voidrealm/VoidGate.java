package io.github.sirschrolli.kingdomsatwar.voidrealm;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.VoidRiftBlock;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Das Tor in die Leere.
 *
 * <p>Mit dem <b>Herz der Schatten</b> reißt man die Welt auf: Das Herz schwebt aus der Hand, schlägt dreimal, und vor
 * einem öffnet sich ein mannshoher, linsenförmiger Riss, gesäumt von leuchtenden Leerenkristallen. Wer hineintritt, landet
 * auf einer <b>Ankerinsel</b> in der Leere – gleich an derselben Stelle wie der Riss, auf dem nächsten treibenden
 * Bruchstück oder, wenn dort nichts treibt, auf einer kleinen Plattform aus Leerenstein. Dort steht das Gegenstück, das
 * zurückführt. Das Herz bleibt erhalten; mit ihm lässt sich ein Riss auch wieder schließen.
 *
 * <p>Wer in der Leere ins Nichts stürzt, wird von der Leere nicht ganz verschluckt: Sie spuckt ihn verwundet und geblendet
 * an der nächsten Ankerinsel wieder aus.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class VoidGate {
    public static final ResourceKey<Level> THE_VOID = ResourceKey.create(Registries.DIMENSION, KingdomsAtWar.id("the_void"));
    /** Breite (halbe Breite) der Linse je Reihe von unten nach oben – 9 Reihen hoch, bis zu 5 breit. */
    private static final int[] HALF_WIDTH = {0, 1, 1, 2, 2, 2, 1, 1, 0};
    public static final int RIFT_HEIGHT = HALF_WIDTH.length;
    /** So lange (Ticks) dauert das Öffnen. */
    private static final int OPENING_TICKS = 70;
    /** Höhe, ab der man in der Leere ins Nichts gestürzt ist. */
    private static final int FALL_LIMIT = -40;

    private static final List<Opening> OPENINGS = new ArrayList<>();

    private VoidGate() {}

    /** Ein Riss, der sich gerade öffnet. */
    private record Opening(ServerLevel level, BlockPos bottom, Direction.Axis axis, int[] age) {}

    // =====================================================================================
    // Öffnen und Schließen
    // =====================================================================================

    /**
     * Versucht, mit dem Herz der Schatten vor dem Spieler einen Riss zu öffnen ({@code ground}: angeklickter Boden). Gibt
     * {@code false} zurück (und sagt dem Spieler warum), wenn es hier nicht geht.
     */
    public static boolean tryOpen(ServerLevel level, Player player, BlockPos ground) {
        if (level.dimension() == THE_VOID) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.shadow_heart.in_void").withStyle(ChatFormatting.DARK_PURPLE));
            return false;
        }
        if (level.getServer().getLevel(THE_VOID) == null || io.github.sirschrolli.kingdomsatwar.arena.ArcaneSphere.is(level)
                || io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.isArena(level)) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.shadow_heart.cannot").withStyle(ChatFormatting.DARK_PURPLE));
            return false;
        }
        Direction facing = player.getDirection();
        Direction.Axis axis = facing.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        // der Riss steht zwei Blöcke vor dem angeklickten Boden, quer zur Blickrichtung
        BlockPos bottom = ground.above().relative(facing, 2);
        if (!fits(level, bottom, axis)) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.shadow_heart.no_space").withStyle(ChatFormatting.DARK_PURPLE));
            return false;
        }
        RiftState state = RiftState.of(level);
        if (state.near(level.dimension(), bottom.above(RIFT_HEIGHT / 2), 12) != null) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.shadow_heart.too_close").withStyle(ChatFormatting.DARK_PURPLE));
            return false;
        }
        OPENINGS.add(new Opening(level, bottom, axis, new int[1]));
        level.playSound(null, bottom.above(4), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.6f, 0.5f);
        return true;
    }

    /** Ist um den Riss genug Platz (Luft oder Pflanzen), und steht er auf festem Grund? */
    static boolean fits(ServerLevel level, BlockPos bottom, Direction.Axis axis) {
        Direction side = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        for (int y = 0; y <= RIFT_HEIGHT; y++) {
            for (int s = -4; s <= 4; s++) {
                BlockPos pos = bottom.above(y).relative(side, s);
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && !state.canBeReplaced()) {
                    return false;
                }
            }
        }
        BlockPos below = bottom.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && level.isInWorldBounds(bottom.above(RIFT_HEIGHT + 2));
    }

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || OPENINGS.isEmpty()) {
            return;
        }
        Iterator<Opening> it = OPENINGS.iterator();
        while (it.hasNext()) {
            Opening opening = it.next();
            if (opening.level() != level) {
                continue;
            }
            int age = ++opening.age()[0];
            ritual(level, opening, age);
            if (age >= OPENING_TICKS) {
                it.remove();
                build(level, opening.bottom(), opening.axis());
                RiftState state = RiftState.of(level);
                state.add(new RiftState.Rift(level.dimension(), opening.bottom(), opening.axis(), Optional.empty()));
                state.save(level);
                BlockPos center = opening.bottom().above(RIFT_HEIGHT / 2);
                level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.4f, 0.75f);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 300, 1.2, 2.2,
                        1.2, 0.4);
                level.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 120, 1.0, 2.0, 1.0,
                        0.15);
            }
        }
    }

    /** Das Öffnen: drei Herzschläge, Funken sammeln sich an der Linse, die Ränder glühen auf. */
    private static void ritual(ServerLevel level, Opening opening, int age) {
        BlockPos bottom = opening.bottom();
        Vec3 center = Vec3.atBottomCenterOf(bottom.above(RIFT_HEIGHT / 2));
        if (age == 10 || age == 28 || age == 46) {
            level.playSound(null, BlockPos.containing(center), SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 2.0f, 0.6f + age / 200.0f);
            level.sendParticles(ParticleTypes.SCULK_SOUL, center.x, center.y, center.z, 20, 0.4, 0.6, 0.4, 0.05);
        }
        if (age == 55) {
            level.playSound(null, BlockPos.containing(center), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.8f, 0.6f);
        }
        float grow = Math.min(1.0f, age / (float) OPENING_TICKS);
        Direction side = opening.axis() == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        RandomSource random = level.getRandom();
        for (int i = 0; i < 6; i++) {
            double t = random.nextDouble() * Math.PI * 2.0;
            double rx = Math.cos(t) * 2.6 * grow;
            double ry = Math.sin(t) * RIFT_HEIGHT * 0.5 * grow;
            double x = center.x + side.getStepX() * rx;
            double z = center.z + side.getStepZ() * rx;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, center.y + ry, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(new DustParticleOptions(0xB98CFF, 1.4f), center.x, center.y, center.z, 3, 0.2 * grow, 1.4 * grow, 0.2 * grow, 0.0);
    }

    /** Baut einen Riss mit seinem Kristallsaum (die Unterkante steht auf {@code bottom}). */
    public static void build(ServerLevel level, BlockPos bottom, Direction.Axis axis) {
        Direction side = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockState rift = VoidBlocks.VOID_RIFT.get().defaultBlockState().setValue(VoidRiftBlock.AXIS, axis);
        List<BlockPos> portal = new ArrayList<>();
        for (int y = 0; y < RIFT_HEIGHT; y++) {
            for (int s = -HALF_WIDTH[y]; s <= HALF_WIDTH[y]; s++) {
                portal.add(bottom.above(y).relative(side, s));
            }
        }
        RandomSource random = RandomSource.create(bottom.asLong());
        BlockState crystal = VoidBlocks.VOID_CRYSTAL.get().defaultBlockState();
        BlockState stone = VoidBlocks.POLISHED_VOID_STONE.base().get().defaultBlockState();
        // Saum: jede Lücke rings um die Linse in der Fläche des Risses
        for (BlockPos pos : portal) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int ds = -1; ds <= 1; ds++) {
                    BlockPos edge = pos.above(dy).relative(side, ds);
                    if (!portal.contains(edge) && replaceable(level, edge)) {
                        level.setBlock(edge, random.nextInt(4) == 0 ? stone : crystal, Block.UPDATE_ALL);
                    }
                }
            }
        }
        // Zacken: einzelne Kristalle stehen schräg aus dem Saum heraus wie Splitter einer zerbrochenen Scheibe
        for (int k = 0; k < 7; k++) {
            int y = random.nextInt(RIFT_HEIGHT);
            int dir = random.nextBoolean() ? 1 : -1;
            BlockPos start = bottom.above(y).relative(side, dir * (HALF_WIDTH[y] + 2));
            int len = 1 + random.nextInt(2);
            for (int l = 0; l < len; l++) {
                BlockPos spike = start.relative(side, dir * l).above(random.nextInt(3) == 0 ? (random.nextBoolean() ? 1 : -1) : 0);
                if (replaceable(level, spike)) {
                    level.setBlock(spike, crystal, Block.UPDATE_ALL);
                }
            }
        }
        for (BlockPos pos : portal) {
            level.setBlock(pos, rift, Block.UPDATE_CLIENTS);
        }
        // ein paar Drusen auf dem Boden davor und dahinter
        Direction front = side.getClockWise();
        for (int k = 0; k < 5; k++) {
            BlockPos base = bottom.relative(side, random.nextInt(7) - 3).relative(random.nextBoolean() ? front : front.getOpposite(),
                    1 + random.nextInt(2));
            if (level.getBlockState(base).canBeReplaced() && level.getBlockState(base.below()).isFaceSturdy(level, base.below(), Direction.UP)) {
                level.setBlock(base, VoidBlocks.VOID_CRYSTAL_CLUSTER.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static boolean replaceable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.is(BlockTags.REPLACEABLE);
    }

    /**
     * Schließt den Riss, zu dem {@code pos} gehört: die Linse fällt in sich zusammen, der Saum bleibt als Mahnmal stehen.
     * Ein Riss auf einer Ankerinsel der Leere lässt sich nicht schließen.
     */
    public static boolean tryClose(ServerLevel level, Player player, BlockPos pos) {
        RiftState state = RiftState.of(level);
        RiftState.Rift rift = state.near(level.dimension(), pos, 6);
        if (rift == null) {
            return false;
        }
        if (level.dimension() == THE_VOID) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.shadow_heart.anchor").withStyle(ChatFormatting.DARK_PURPLE));
            return false;
        }
        Direction side = rift.axis() == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        for (int y = 0; y < RIFT_HEIGHT; y++) {
            for (int s = -HALF_WIDTH[y]; s <= HALF_WIDTH[y]; s++) {
                BlockPos at = rift.bottom().above(y).relative(side, s);
                if (level.getBlockState(at).is(VoidBlocks.VOID_RIFT.get())) {
                    level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        state.remove(rift);
        state.save(level);
        BlockPos center = rift.center();
        level.playSound(null, center, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.6f, 0.5f);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 200, 0.8, 2.0, 0.8,
                0.2);
        return true;
    }

    // =====================================================================================
    // Reisen
    // =====================================================================================

    /** Ziel einer Reise durch einen Riss (null: der Riss führt nirgendwohin). */
    public static @Nullable TeleportTransition destination(ServerLevel level, Entity entity, BlockPos entry) {
        TeleportTransition.PostTeleportTransition after = TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET);
        RiftState state = RiftState.of(level);
        RiftState.Rift rift = state.near(level.dimension(), entry, 6);
        if (rift == null) {
            return null;
        }
        if (rift.partner().isPresent()) {
            RiftState.Partner partner = rift.partner().get();
            ServerLevel target = level.getServer().getLevel(partner.dimension());
            if (target != null) {
                RiftState.Rift other = state.at(partner.dimension(), partner.bottom());
                Direction.Axis axis = other != null ? other.axis() : rift.axis();
                return arrive(target, partner.bottom(), axis, entity, after);
            }
        }
        if (level.dimension() == THE_VOID) {
            // das Gegenstück ist verschwunden: zurück zum Weltursprung
            ServerLevel overworld = level.getServer().overworld();
            return new TeleportTransition(overworld, Vec3.atBottomCenterOf(overworld.getRespawnData().pos()), Vec3.ZERO, 0.0f, 0.0f, after);
        }
        ServerLevel voidLevel = level.getServer().getLevel(THE_VOID);
        if (voidLevel == null) {
            return null;
        }
        // zum ersten Mal: Ankerinsel in der Leere suchen und den Rückweg bauen
        BlockPos anchor = VoidLanding.ensure(voidLevel, rift.bottom(), rift.axis());
        RiftState.Rift back = new RiftState.Rift(THE_VOID, anchor, rift.axis(), Optional.of(new RiftState.Partner(level.dimension(), rift.bottom())));
        state.replace(rift, rift.withPartner(new RiftState.Partner(THE_VOID, anchor)));
        state.add(back);
        state.save(level);
        return arrive(voidLevel, anchor, rift.axis(), entity, after);
    }

    /** Ankunft vor einem Riss: zwei Blöcke davor, mit dem Blick vom Riss weg. */
    private static TeleportTransition arrive(ServerLevel level, BlockPos bottom, Direction.Axis axis, Entity entity,
            TeleportTransition.PostTeleportTransition after) {
        Direction out = axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
        BlockPos spot = bottom.relative(out, 2);
        if (!level.getBlockState(spot).isAir() || !level.getBlockState(spot.above()).isAir()) {
            spot = bottom.relative(out.getOpposite(), 2);
            out = out.getOpposite();
        }
        return new TeleportTransition(level, Vec3.atBottomCenterOf(spot), Vec3.ZERO, out.toYRot(), 0.0f, after);
    }

    // =====================================================================================
    // Sturz ins Nichts
    // =====================================================================================

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().dimension() != THE_VOID || player.getY() > FALL_LIMIT
                || player.isSpectator() || player.isCreative() || !player.isAlive()) {
            return;
        }
        ServerLevel level = player.level();
        RiftState.Rift anchor = null;
        double best = Double.MAX_VALUE;
        for (RiftState.Rift rift : RiftState.of(level).rifts()) {
            if (rift.dimension() == THE_VOID || rift.dimension().equals(THE_VOID)) {
                double d = rift.bottom().distToCenterSqr(player.getX(), rift.bottom().getY(), player.getZ());
                if (d < best) {
                    best = d;
                    anchor = rift;
                }
            }
        }
        BlockPos target = anchor != null ? anchor.bottom() : VoidLanding.ensure(level, player.blockPosition(), Direction.Axis.X);
        Direction.Axis axis = anchor != null ? anchor.axis() : Direction.Axis.X;
        TeleportTransition back = arrive(level, target, axis, player, TeleportTransition.DO_NOTHING);
        player.teleport(back);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0));
        player.hurtServer(level, level.damageSources().fellOutOfWorld(), 8.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.5f);
        player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.void.spat_out").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
