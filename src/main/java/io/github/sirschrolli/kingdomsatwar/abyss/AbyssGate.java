package io.github.sirschrolli.kingdomsatwar.abyss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.abyss.AbyssRiftBlock;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.voidrealm.RiftState;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
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
 * Das Tor in den Abgrund.
 *
 * <p>Zwei Wege führen hinein, beide aus der Leere: Fällt der Zerbrochene König, reißt <b>hinter seinem Thron</b> die Welt
 * auf – ein Abgrundriss auf einer kleinen Plattform aus Chaosgestein, über eine Brücke vom Thronplatz aus zu erreichen. Und
 * mit dem <b>Rissschlüssel</b>, den der König fallen lässt, lässt sich an jeder Stelle der Leere ein solcher Riss öffnen (und
 * wieder schließen). Wer hindurchtritt, landet auf einer <b>Ankerinsel</b> im Abgrund; dort steht das Gegenstück, das in die
 * Leere zurückführt.
 *
 * <p>Wer im Abgrund ins Nichts stürzt, wird – wie in der Leere – verwundet an der nächsten Ankerinsel wieder ausgespuckt.
 * Die Risse werden mit denen der Leere gemeinsam gespeichert ({@link RiftState}); ein Abgrundriss erkennt man an seinem Block.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class AbyssGate {
    public static final ResourceKey<Level> THE_ABYSS = ResourceKey.create(Registries.DIMENSION, KingdomsAtWar.id("the_abyss"));
    /** Breite (halbe Breite) der Linse je Reihe von unten nach oben – 11 Reihen hoch, bis zu 5 breit, oben und unten zackig. */
    private static final int[] HALF_WIDTH = {0, 1, 1, 2, 2, 2, 2, 2, 1, 1, 0};
    public static final int RIFT_HEIGHT = HALF_WIDTH.length;
    private static final int OPENING_TICKS = 80;
    /** Höhe, ab der man im Abgrund ins Nichts gestürzt ist. */
    private static final int FALL_LIMIT = -40;
    /** Der Riss hinter dem Thron: so weit hinter der Mitte des Thronplatzes (nach Norden). */
    public static final int THRONE_GATE_Z = -28;

    private static final List<Opening> OPENINGS = new ArrayList<>();

    private AbyssGate() {}

    private record Opening(ServerLevel level, BlockPos bottom, Direction.Axis axis, int[] age) {}

    // =====================================================================================
    // Öffnen und Schließen mit dem Rissschlüssel
    // =====================================================================================

    /**
     * Versucht, mit dem Rissschlüssel vor dem Spieler einen Abgrundriss zu öffnen ({@code ground}: angeklickter Boden).
     * Nur in der Leere; gibt {@code false} zurück (und sagt dem Spieler warum), wenn es hier nicht geht.
     */
    public static boolean tryOpen(ServerLevel level, Player player, BlockPos ground) {
        if (level.dimension() != VoidGate.THE_VOID) {
            player.sendOverlayMessage(Component.translatable(level.dimension() == THE_ABYSS ? "item.kingdomsatwar.rift_key.in_abyss"
                    : "item.kingdomsatwar.rift_key.not_void").withStyle(ChatFormatting.RED));
            return false;
        }
        if (level.getServer().getLevel(THE_ABYSS) == null) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.rift_key.cannot").withStyle(ChatFormatting.RED));
            return false;
        }
        Direction facing = player.getDirection();
        Direction.Axis axis = facing.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        BlockPos bottom = ground.above().relative(facing, 2);
        if (!fits(level, bottom, axis)) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.rift_key.no_space").withStyle(ChatFormatting.RED));
            return false;
        }
        RiftState state = RiftState.of(level);
        if (state.near(level.dimension(), bottom.above(RIFT_HEIGHT / 2), 14) != null) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.rift_key.too_close").withStyle(ChatFormatting.RED));
            return false;
        }
        OPENINGS.add(new Opening(level, bottom, axis, new int[1]));
        level.playSound(null, bottom.above(5), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.6f, 0.5f);
        return true;
    }

    /** Ist um den Riss genug Platz (Luft oder Pflanzen), und steht er auf festem Grund? */
    static boolean fits(ServerLevel level, BlockPos bottom, Direction.Axis axis) {
        Direction side = side(axis);
        for (int y = 0; y <= RIFT_HEIGHT; y++) {
            for (int s = -4; s <= 4; s++) {
                BlockState state = level.getBlockState(bottom.above(y).relative(side, s));
                if (!state.isAir() && !state.canBeReplaced()) {
                    return false;
                }
            }
        }
        BlockPos below = bottom.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && level.isInWorldBounds(bottom.above(RIFT_HEIGHT + 2));
    }

    /** Schließt den Abgrundriss, zu dem {@code pos} gehört (nur in der Leere; die Ankerinseln im Abgrund bleiben offen). */
    public static boolean tryClose(ServerLevel level, Player player, BlockPos pos) {
        RiftState state = RiftState.of(level);
        RiftState.Rift rift = state.near(level.dimension(), pos, 6);
        if (rift == null || !level.getBlockState(pos).is(AbyssBlocks.ABYSS_RIFT.get())) {
            return false;
        }
        if (level.dimension() != VoidGate.THE_VOID || isThroneGate(level, rift)) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.rift_key.anchor").withStyle(ChatFormatting.RED));
            return false;
        }
        Direction side = side(rift.axis());
        for (int y = 0; y < RIFT_HEIGHT; y++) {
            for (int s = -HALF_WIDTH[y]; s <= HALF_WIDTH[y]; s++) {
                BlockPos at = rift.bottom().above(y).relative(side, s);
                if (level.getBlockState(at).is(AbyssBlocks.ABYSS_RIFT.get())) {
                    level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        state.remove(rift);
        state.save(level);
        BlockPos center = rift.center();
        level.playSound(null, center, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.6f, 0.6f);
        level.sendParticles(ParticleTypes.LAVA, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 30, 0.6, 2.0, 0.6, 0.0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 60, 0.6, 2.0, 0.6, 0.05);
        return true;
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
            ritual(level, opening.bottom(), opening.axis(), age);
            if (age >= OPENING_TICKS) {
                it.remove();
                open(level, opening.bottom(), opening.axis());
            }
        }
    }

    /** Das Öffnen: Glutfunken sammeln sich, die Welt knirscht dreimal, dann reißt sie auf. */
    private static void ritual(ServerLevel level, BlockPos bottom, Direction.Axis axis, int age) {
        Vec3 center = Vec3.atBottomCenterOf(bottom.above(RIFT_HEIGHT / 2));
        if (age == 12 || age == 32 || age == 52) {
            level.playSound(null, BlockPos.containing(center), SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 2.4f, 0.5f + age / 160.0f);
            level.playSound(null, BlockPos.containing(center), SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 2.0f, 0.5f);
            level.sendParticles(ParticleTypes.LAVA, center.x, center.y, center.z, 6, 0.4, 1.2, 0.4, 0.0);
        }
        float grow = Math.min(1.0f, age / (float) OPENING_TICKS);
        Direction side = side(axis);
        RandomSource random = level.getRandom();
        for (int i = 0; i < 6; i++) {
            double t = random.nextDouble() * Math.PI * 2.0;
            double rx = Math.cos(t) * 2.6 * grow;
            double ry = Math.sin(t) * RIFT_HEIGHT * 0.5 * grow;
            level.sendParticles(ParticleTypes.FLAME, center.x + side.getStepX() * rx, center.y + ry, center.z + side.getStepZ() * rx, 1, 0.0,
                    0.0, 0.0, 0.0);
        }
        level.sendParticles(new DustParticleOptions(0xE8381A, 1.5f), center.x, center.y, center.z, 3, 0.2 * grow, 1.6 * grow, 0.2 * grow, 0.0);
    }

    /** Baut einen Riss, merkt ihn sich und lässt ihn aufbrechen. */
    static void open(ServerLevel level, BlockPos bottom, Direction.Axis axis) {
        build(level, bottom, axis);
        RiftState state = RiftState.of(level);
        if (state.at(level.dimension(), bottom) == null) {
            state.add(new RiftState.Rift(level.dimension(), bottom, axis, Optional.empty()));
            state.save(level);
        }
        BlockPos center = bottom.above(RIFT_HEIGHT / 2);
        level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.4f, 0.55f);
        level.playSound(null, center, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.2f, 0.6f);
        level.sendParticles(ParticleTypes.LAVA, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 40, 1.0, 2.5, 1.0, 0.0);
        level.sendParticles(ParticleTypes.FLAME, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 160, 1.2, 2.5, 1.2, 0.2);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 80, 1.2, 2.5, 1.2, 0.05);
    }

    static Direction side(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    /** Baut einen Abgrundriss mit seinem Saum aus Chaosgestein, glühenden Nähten und schwebenden Kristallsplittern. */
    public static void build(ServerLevel level, BlockPos bottom, Direction.Axis axis) {
        Direction side = side(axis);
        BlockState rift = AbyssBlocks.ABYSS_RIFT.get().defaultBlockState().setValue(AbyssRiftBlock.AXIS, axis);
        List<BlockPos> portal = new ArrayList<>();
        for (int y = 0; y < RIFT_HEIGHT; y++) {
            for (int s = -HALF_WIDTH[y]; s <= HALF_WIDTH[y]; s++) {
                portal.add(bottom.above(y).relative(side, s));
            }
        }
        RandomSource random = RandomSource.create(bottom.asLong() ^ 0x5EA4L);
        BlockState stone = AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState();
        BlockState polished = AbyssBlocks.POLISHED_CHAOS_STONE.base().get().defaultBlockState();
        BlockState seam = AbyssBlocks.WORLD_SEAM.get().defaultBlockState();
        for (BlockPos pos : portal) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int ds = -1; ds <= 1; ds++) {
                    BlockPos edge = pos.above(dy).relative(side, ds);
                    if (!portal.contains(edge) && replaceable(level, edge)) {
                        int roll = random.nextInt(6);
                        level.setBlock(edge, roll == 0 ? seam : roll < 3 ? polished : stone, Block.UPDATE_ALL);
                    }
                }
            }
        }
        // Zacken: Splitter des Saums ragen schräg heraus, wie die Ränder eines zerbrochenen Spiegels
        for (int k = 0; k < 8; k++) {
            int y = random.nextInt(RIFT_HEIGHT);
            int dir = random.nextBoolean() ? 1 : -1;
            BlockPos start = bottom.above(y).relative(side, dir * (HALF_WIDTH[y] + 2));
            int len = 1 + random.nextInt(3);
            for (int l = 0; l < len; l++) {
                BlockPos spike = start.relative(side, dir * l).above(random.nextInt(3) == 0 ? (random.nextBoolean() ? 1 : -1) : 0);
                if (replaceable(level, spike)) {
                    level.setBlock(spike, l == len - 1 && random.nextBoolean() ? seam : stone, Block.UPDATE_ALL);
                }
            }
        }
        for (BlockPos pos : portal) {
            level.setBlock(pos, rift, Block.UPDATE_CLIENTS);
        }
    }

    private static boolean replaceable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.is(BlockTags.REPLACEABLE);
    }

    // =====================================================================================
    // Der Riss hinter dem Thron
    // =====================================================================================

    /** Unterkante des Risses hinter dem Thron (zu {@code center}, der Mitte des Thronplatzes). */
    public static BlockPos throneGateBottom(BlockPos center) {
        return new BlockPos(center.getX(), center.getY() + 1, center.getZ() + THRONE_GATE_Z);
    }

    /** Der Riss hinter dem Thron steht auf gemeißeltem Chaosgestein in der Mitte seiner Plattform – er bleibt offen. */
    private static boolean isThroneGate(ServerLevel level, RiftState.Rift rift) {
        return level.getBlockState(rift.bottom().below()).is(AbyssBlocks.CHISELED_CHAOS_STONE.get());
    }

    /**
     * Stellt sicher, dass hinter dem Thron (Mitte des Platzes: {@code center}) der Riss in den Abgrund offen ist; baut ihn
     * sonst – mit Plattform und Brücke vom Rand des Platzes. Gibt {@code true} zurück, wenn er gerade erst entstand.
     */
    public static boolean ensureThroneGate(ServerLevel level, BlockPos center, boolean dramatic) {
        BlockPos bottom = throneGateBottom(center);
        if (!level.isLoaded(bottom) || level.getBlockState(bottom).is(AbyssBlocks.ABYSS_RIFT.get())) {
            return false;
        }
        platform(level, bottom);
        open(level, bottom, Direction.Axis.X);
        if (dramatic) {
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(Vec3.atCenterOf(center)) < 120.0 * 120.0) {
                    player.sendSystemMessage(Component.translatable("message.kingdomsatwar.abyss.throne_gate").withStyle(ChatFormatting.RED));
                }
            }
        }
        return true;
    }

    /** Die Plattform hinter dem Thron: eine Scheibe aus Chaosgestein mit glühenden Nähten, eine Brücke zum Thronplatz. */
    private static void platform(ServerLevel level, BlockPos bottom) {
        BlockPos floor = bottom.below();
        BlockState stone = AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState();
        BlockState polished = AbyssBlocks.POLISHED_CHAOS_STONE.base().get().defaultBlockState();
        BlockState bricks = AbyssBlocks.CHAOS_STONE_BRICKS.base().get().defaultBlockState();
        BlockState chiseled = AbyssBlocks.CHISELED_CHAOS_STONE.get().defaultBlockState();
        BlockState seam = AbyssBlocks.WORLD_SEAM.get().defaultBlockState();
        RandomSource random = RandomSource.create(bottom.asLong());
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double d = Math.sqrt(dx * dx * 0.8 + dz * dz * 1.1);
                if (d > 5.6) {
                    continue;
                }
                BlockPos at = floor.offset(dx, 0, dz);
                BlockState top = d < 1.2 ? chiseled : d > 4.6 ? bricks : random.nextInt(9) == 0 ? seam : polished;
                level.setBlock(at, top, Block.UPDATE_CLIENTS);
                // darunter ein zackiger Kegel, der ins Nichts hängt
                int depth = (int) Math.round((5.6 - d) * 1.6 + random.nextInt(3));
                for (int dy = 1; dy <= depth; dy++) {
                    BlockPos below = at.below(dy);
                    if (level.getBlockState(below).isAir()) {
                        level.setBlock(below, random.nextInt(14) == 0 ? seam : stone, Block.UPDATE_CLIENTS);
                    }
                }
                for (int dy = 1; dy <= RIFT_HEIGHT + 3; dy++) {
                    BlockPos air = at.above(dy);
                    if (!level.getBlockState(air).isAir() && !level.getBlockState(air).is(AbyssBlocks.ABYSS_RIFT.get())) {
                        level.setBlock(air, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        // Brücke nach Süden bis zum Rand des Thronplatzes (Sternenmarmor oder fester Boden)
        for (int dz = 6; dz <= 12; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                BlockPos at = floor.offset(dx, 0, dz);
                if (!level.getBlockState(at).isAir()) {
                    continue;
                }
                level.setBlock(at, dx == 0 ? polished : bricks, Block.UPDATE_CLIENTS);
                if (random.nextInt(3) == 0) {
                    level.setBlock(at.below(), stone, Block.UPDATE_CLIENTS);
                }
            }
        }
        // zwei Schwebekristalle an den Rändern – hier beginnt die verdrehte Schwerkraft
        level.setBlock(floor.offset(-4, 1, -2), AbyssBlocks.FLOAT_CRYSTAL.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(floor.offset(4, 1, -2), AbyssBlocks.FLOAT_CRYSTAL.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    // =====================================================================================
    // Reisen
    // =====================================================================================

    /** Ziel einer Reise durch einen Abgrundriss (null: der Riss führt nirgendwohin). */
    public static @Nullable TeleportTransition destination(ServerLevel level, Entity entity, BlockPos entry) {
        TeleportTransition.PostTeleportTransition after = TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET);
        RiftState state = RiftState.of(level);
        RiftState.Rift rift = state.near(level.dimension(), entry, 7);
        if (rift == null) {
            return null;
        }
        if (rift.partner().isPresent()) {
            RiftState.Partner partner = rift.partner().get();
            ServerLevel target = level.getServer().getLevel(partner.dimension());
            if (target != null) {
                RiftState.Rift other = state.at(partner.dimension(), partner.bottom());
                Direction.Axis axis = other != null ? other.axis() : rift.axis();
                return arrive(target, partner.bottom(), axis, after);
            }
        }
        if (level.dimension() == THE_ABYSS) {
            // das Gegenstück ist verschwunden: zurück in die Leere (oder zum Weltursprung)
            ServerLevel voidLevel = level.getServer().getLevel(VoidGate.THE_VOID);
            ServerLevel back = voidLevel != null ? voidLevel : level.getServer().overworld();
            return new TeleportTransition(back, Vec3.atBottomCenterOf(back.getRespawnData().pos()), Vec3.ZERO, 0.0f, 0.0f, after);
        }
        ServerLevel abyss = level.getServer().getLevel(THE_ABYSS);
        if (abyss == null) {
            return null;
        }
        // zum ersten Mal: Ankerinsel im Abgrund suchen und den Rückweg bauen
        BlockPos anchor = AbyssLanding.ensure(abyss, rift.bottom(), rift.axis());
        RiftState.Rift back = new RiftState.Rift(THE_ABYSS, anchor, rift.axis(), Optional.of(new RiftState.Partner(level.dimension(), rift.bottom())));
        state.replace(rift, rift.withPartner(new RiftState.Partner(THE_ABYSS, anchor)));
        state.add(back);
        state.save(level);
        return arrive(abyss, anchor, rift.axis(), after);
    }

    /** Ankunft vor einem Riss: zwei Blöcke davor, mit dem Blick vom Riss weg. */
    private static TeleportTransition arrive(ServerLevel level, BlockPos bottom, Direction.Axis axis, TeleportTransition.PostTeleportTransition after) {
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
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().dimension() != THE_ABYSS || player.getY() > FALL_LIMIT
                || player.isSpectator() || player.isCreative() || !player.isAlive()) {
            return;
        }
        ServerLevel level = player.level();
        RiftState.Rift anchor = null;
        double best = Double.MAX_VALUE;
        for (RiftState.Rift rift : RiftState.of(level).rifts()) {
            if (rift.dimension().equals(THE_ABYSS)) {
                double d = rift.bottom().distToCenterSqr(player.getX(), rift.bottom().getY(), player.getZ());
                if (d < best) {
                    best = d;
                    anchor = rift;
                }
            }
        }
        BlockPos target = anchor != null ? anchor.bottom() : AbyssLanding.ensure(level, player.blockPosition(), Direction.Axis.X);
        Direction.Axis axis = anchor != null ? anchor.axis() : Direction.Axis.X;
        player.teleport(arrive(level, target, axis, TeleportTransition.DO_NOTHING));
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 140, 0));
        player.hurtServer(level, level.damageSources().fellOutOfWorld(), 10.0f);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.4f);
        player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.abyss.spat_out").withStyle(ChatFormatting.RED));
    }
}
