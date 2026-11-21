package io.github.sirschrolli.kingdomsatwar.world;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock;
import io.github.sirschrolli.kingdomsatwar.block.SealPlateBlock;
import io.github.sirschrolli.kingdomsatwar.block.SealSocketBlock;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.SealBreakEntity;
import io.github.sirschrolli.kingdomsatwar.network.ScreenShakePayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.story.Story;
import io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout;
import io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLocator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Was am Siegelkreis geschieht: Splitter einsetzen (der Monolith erwacht, seine Runen und die Zeichen im Boden
 * leuchten) und – nach dem sechsten Splitter – der Siegelbruch: Lichtsäulen steigen aus den Monolithen, Strahlen laufen
 * zur Mitte, das Siegel bekommt Risse und zerspringt, ein Blitz schlägt ein und in der Mitte klafft das Schattentor.
 * Der Siegelbruch dauert {@link #BREAK_TICKS} Ticks und läuft weiter, auch wenn das Spiel zwischendurch gespeichert wird.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class SealCircle {
    /** Dauer des Siegelbruchs. */
    public static final int BREAK_TICKS = 340;
    /** Moment, in dem das Siegel zerspringt. */
    public static final int SHATTER_TICK = 220;
    private static final int CRACK_START = 130;
    private static final double AUDIENCE_RADIUS = 160.0;

    private SealCircle() {}

    // =====================================================================================
    // Splitter einsetzen
    // =====================================================================================

    /** Setzt den Splitter in den Sockel (der Gegenstand wird vom Aufrufer verbraucht). false: nicht möglich. */
    public static boolean insert(ServerLevel level, BlockPos socket, BlockState state, @Nullable ServerPlayer player) {
        DragonType element = state.getValue(SealSocketBlock.ELEMENT);
        SealState seal = SealState.of(level);
        if (seal.isInserted(element) || state.getValue(SealSocketBlock.FILLED)) {
            return false;
        }
        BlockPos center = SealCircleLayout.centerFromSocket(socket, element);
        seal.setCenter(center);
        seal.insert(element);
        level.setBlock(socket, state.setValue(SealSocketBlock.FILLED, true), Block.UPDATE_ALL);
        light(level, center, element);
        awakenEffects(level, center, socket, element);
        int count = seal.insertedCount();
        Component dragon = Component.translatable("lair.kingdomsatwar." + element.id() + ".dragon").withColor(element.color());
        for (ServerPlayer p : audience(level, center)) {
            p.sendSystemMessage(Component.translatable("message.kingdomsatwar.seal.inserted", dragon, count).withStyle(ChatFormatting.GOLD));
        }
        if (count >= DragonType.values().length && !seal.isBroken() && !seal.isBreaking()) {
            seal.setBreakTicks(0);
        }
        seal.save(level);
        if (player != null) {
            Story.onShardInserted(level, player, element);
        }
        return true;
    }

    /** Setzt einen Splitter ohne Spieler ein (Befehle, Tests): sucht den Sockel am bekannten Siegelkreis. */
    public static boolean insert(ServerLevel level, DragonType element) {
        BlockPos center = center(level);
        if (center == null) {
            return false;
        }
        BlockPos socket = SealCircleLayout.socket(center, element);
        BlockState state = level.getBlockState(socket);
        if (!(state.getBlock() instanceof SealSocketBlock) || state.getValue(SealSocketBlock.ELEMENT) != element) {
            return false;
        }
        return insert(level, socket, state, null);
    }

    /** Alles zurück auf Anfang: Splitter heraus, Runen dunkel, das Siegel wieder ganz (Befehle, Tests). */
    public static void reset(ServerLevel level) {
        SealState seal = SealState.of(level);
        BlockPos center = center(level);
        seal.reset();
        seal.save(level);
        if (center == null || !level.isLoaded(center)) {
            return;
        }
        for (DragonType element : DragonType.values()) {
            BlockPos m = SealCircleLayout.monolith(center, element);
            for (BlockPos pos : BlockPos.betweenClosed(m.offset(-1, 1, -1), m.offset(1, SealCircleLayout.MONOLITH_HEIGHT, 1))) {
                unlight(level, pos);
            }
            for (BlockPos pos : SealCircleLayout.inlays(center, element)) {
                unlight(level, pos);
            }
            BlockPos socket = SealCircleLayout.socket(center, element);
            BlockState state = level.getBlockState(socket);
            if (state.getBlock() instanceof SealSocketBlock) {
                level.setBlock(socket, state.setValue(SealSocketBlock.FILLED, false), Block.UPDATE_ALL);
            }
        }
        for (int row = -2; row <= 2; row++) {
            for (int col = -2; col <= 2; col++) {
                level.setBlock(center.offset(col, 0, row), ModBlocks.SEAL_PLATE.get().defaultBlockState()
                        .setValue(SealPlateBlock.PART, SealCircleLayout.platePart(col, row)), Block.UPDATE_ALL);
            }
        }
    }

    private static void unlight(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof SealMarkBlock && state.getValue(SealMarkBlock.LIT)) {
            level.setBlock(pos, state.setValue(SealMarkBlock.LIT, false), Block.UPDATE_ALL);
        }
    }

    /** Lässt Runen, Deckstein und Bodenzeichen eines Elements leuchten. */
    public static void light(ServerLevel level, BlockPos center, DragonType element) {
        BlockPos m = SealCircleLayout.monolith(center, element);
        for (BlockPos pos : BlockPos.betweenClosed(m.offset(-1, 1, -1), m.offset(1, SealCircleLayout.MONOLITH_HEIGHT, 1))) {
            setLit(level, pos, element);
        }
        for (BlockPos pos : SealCircleLayout.inlays(center, element)) {
            setLit(level, pos, element);
        }
        BlockPos socket = SealCircleLayout.socket(center, element);
        BlockState socketState = level.getBlockState(socket);
        if (socketState.getBlock() instanceof SealSocketBlock && !socketState.getValue(SealSocketBlock.FILLED)) {
            level.setBlock(socket, socketState.setValue(SealSocketBlock.FILLED, true), Block.UPDATE_ALL);
        }
    }

    private static void setLit(ServerLevel level, BlockPos pos, DragonType element) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof SealMarkBlock && state.getValue(SealMarkBlock.ELEMENT) == element && !state.getValue(SealMarkBlock.LIT)) {
            level.setBlock(pos, state.setValue(SealMarkBlock.LIT, true), Block.UPDATE_ALL);
        }
    }

    private static void awakenEffects(ServerLevel level, BlockPos center, BlockPos socket, DragonType element) {
        BlockPos m = SealCircleLayout.monolith(center, element);
        Vec3 top = Vec3.atCenterOf(m.above(SealCircleLayout.MONOLITH_HEIGHT));
        for (int i = 0; i < 40; i++) {
            level.sendParticles(ParticleTypes.END_ROD, top.x, top.y + i * 0.75, top.z, 2, 0.08, 0.2, 0.08, 0.0);
        }
        level.sendParticles(element.particle(), socket.getX() + 0.5, socket.getY() + 0.5, socket.getZ() + 0.5, 40, 0.6, 0.6, 0.6, 0.05);
        line(level, Vec3.atCenterOf(socket), Vec3.atCenterOf(center.above()), element.particle(), 28);
        level.playSound(null, socket, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0f, 0.6f);
        level.playSound(null, socket, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 2.0f, 0.5f);
        level.playSound(null, socket, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.5f, 0.7f);
    }

    // =====================================================================================
    // Siegelbruch
    // =====================================================================================

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        SealState seal = overworld.getData(io.github.sirschrolli.kingdomsatwar.registry.ModAttachments.SEAL);
        if (!seal.isBreaking() || !overworld.tickRateManager().runsNormally()) {
            return;   // (steht das Spiel per /tick freeze still, wartet auch der Siegelbruch)
        }
        BlockPos center = seal.center();
        if (center == null) {
            seal.setBreakTicks(-1);
            seal.setBroken(true);
            seal.save(overworld);
            return;
        }
        int t = seal.breakTicks();
        if (!overworld.isLoaded(center)) {
            // niemand in der Nähe: das Siegel bricht still
            openGate(overworld, center);
            t = BREAK_TICKS;
        } else {
            tickBreak(overworld, center, t);
        }
        if (t >= BREAK_TICKS) {
            seal.setBreakTicks(-1);
            seal.setBroken(true);
            for (SealBreakEntity effect : effects(overworld, center)) {
                effect.discard();
            }
            Story.onSealBroken(overworld);
        } else {
            seal.setBreakTicks(t + 1);
        }
        seal.save(overworld);
    }

    /** Träger der Lichteffekte über der Mitte (neu erzeugt, falls er fehlt – z. B. nach dem Laden). */
    private static void updateEffect(ServerLevel level, BlockPos center, int t) {
        List<SealBreakEntity> found = effects(level, center);
        SealBreakEntity effect;
        if (found.isEmpty()) {
            effect = ModEntities.SEAL_BREAK.get().create(level, EntitySpawnReason.TRIGGERED);
            if (effect == null) {
                return;
            }
            effect.snapTo(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 0.0f, 0.0f);
            level.addFreshEntity(effect);
        } else {
            effect = found.getFirst();
        }
        effect.setBreakTicks(t);
    }

    private static List<SealBreakEntity> effects(ServerLevel level, BlockPos center) {
        return level.getEntities(ModEntities.SEAL_BREAK.get(), new net.minecraft.world.phys.AABB(center).inflate(2.0), e -> true);
    }

    private static void tickBreak(ServerLevel level, BlockPos center, int t) {
        Vec3 heart = Vec3.atCenterOf(center).add(0, 9, 0);
        updateEffect(level, center, t);
        if (t == 0) {
            for (DragonType element : DragonType.values()) {
                light(level, center, element);
            }
            sound(level, center, SoundEvents.CONDUIT_ACTIVATE, 4.0f, 0.5f);
            sound(level, center, SoundEvents.AMBIENT_CAVE.value(), 4.0f, 0.6f);
            shake(level, center, 0.25f, 60);
            for (ServerPlayer player : audience(level, center)) {
                player.sendSystemMessage(Component.translatable("message.kingdomsatwar.seal.trembles").withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
        // 1) Lichtsäulen aus den Decksteinen (die Säulen selbst zeichnet der Effekt-Träger)
        if (t < 120 && t % 2 == 0) {
            for (DragonType element : DragonType.values()) {
                Vec3 top = Vec3.atCenterOf(SealCircleLayout.monolith(center, element).above(SealCircleLayout.MONOLITH_HEIGHT));
                level.sendParticles(element.particle(), top.x, top.y + 0.6, top.z, 3, 0.35, 0.35, 0.35, 0.03);
            }
        }
        if (t == 40 || t == 70 || t == 100) {
            sound(level, center, SoundEvents.RESPAWN_ANCHOR_CHARGE, 3.0f, 0.5f + t / 400.0f);
        }
        // 2) Strahlen von den Monolithen zur Mitte, darüber wächst eine Kugel aus Licht
        if (t >= 80 && t < SHATTER_TICK && t % 6 == 0) {
            for (DragonType element : DragonType.values()) {
                Vec3 top = Vec3.atCenterOf(SealCircleLayout.monolith(center, element).above(SealCircleLayout.MONOLITH_HEIGHT));
                line(level, top, heart, element.particle(), 10);
            }
            float size = Mth.clamp((t - 80) / 120.0f, 0.0f, 1.0f) * 2.5f;
            level.sendParticles(ParticleTypes.END_ROD, heart.x, heart.y, heart.z, 10, size * 0.5, size * 0.5, size * 0.5, 0.02);
            level.sendParticles(ParticleTypes.ENCHANT, heart.x, heart.y, heart.z, 20, size, size, size, 0.6);
        }
        if (t == 80) {
            sound(level, center, SoundEvents.BEACON_POWER_SELECT, 4.0f, 0.5f);
        }
        // 3) Das Siegel bekommt Risse
        if (t >= CRACK_START && t < SHATTER_TICK) {
            int stage = Mth.clamp((t - CRACK_START) * 10 / (SHATTER_TICK - CRACK_START), 0, 9);
            List<BlockPos> plates = SealCircleLayout.plates(center);
            for (int i = 0; i < plates.size(); i++) {
                level.destroyBlockProgress(-1_700_000_000 - i, plates.get(i), stage);
            }
            if (t % 9 == 0) {
                BlockPos p = plates.get(level.getRandom().nextInt(plates.size()));
                level.playSound(null, p, SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 2.0f, 0.5f + level.getRandom().nextFloat() * 0.3f);
                level.sendParticles(ParticleTypes.CRIT, p.getX() + 0.5, p.getY() + 1.05, p.getZ() + 0.5, 6, 0.4, 0.05, 0.4, 0.1);
            }
            if (t % 20 == 0) {
                shake(level, center, 0.2f + (t - CRACK_START) / 300.0f, 25);
            }
            // Funken sinken aus der Kugel in das Siegel
            if (t % 4 == 0) {
                line(level, heart, Vec3.atCenterOf(center).add(0, 0.6, 0), ParticleTypes.END_ROD, 8);
            }
        }
        // 4) Das Siegel zerspringt – das Tor öffnet sich
        if (t == SHATTER_TICK) {
            List<BlockPos> plates = SealCircleLayout.plates(center);
            for (int i = 0; i < plates.size(); i++) {
                level.destroyBlockProgress(-1_700_000_000 - i, plates.get(i), -1);
            }
            openGate(level, center);
            Vec3 c = Vec3.atCenterOf(center).add(0, 1, 0);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 3, 1.0, 0.5, 1.0, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, c.x, c.y, c.z, 200, 1.5, 1.5, 1.5, 0.6);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y, c.z, 300, 2.5, 0.5, 2.5, 0.4);
            LightningBolt bolt = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
            if (bolt != null) {
                bolt.snapTo(c.x, center.getY() + 1, c.z);
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            sound(level, center, SoundEvents.END_PORTAL_SPAWN, 5.0f, 0.7f);
            sound(level, center, SoundEvents.GENERIC_EXPLODE.value(), 4.0f, 0.5f);
            sound(level, center, SoundEvents.WITHER_SPAWN, 3.0f, 0.6f);
            shake(level, center, 1.0f, 50);
            level.getServer().setWeatherParameters(0, 6000, true, true);
        }
        // 5) Schatten quellen aus dem Tor
        if (t > SHATTER_TICK && t < BREAK_TICKS) {
            Vec3 c = Vec3.atCenterOf(center).add(0, 0.8, 0);
            double spread = 2.2;
            level.sendParticles(ParticleTypes.SQUID_INK, c.x, c.y, c.z, 6, spread, 0.2, spread, 0.15);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y + 1, c.z, 4, spread, 0.5, spread, 0.04);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, c.x, c.y, c.z, 10, spread, 0.3, spread, 0.25);
            if (t % 40 == 0) {
                sound(level, center, SoundEvents.ELDER_GUARDIAN_CURSE, 2.0f, 0.5f);
            }
        }
        if (t == SHATTER_TICK + 40) {
            for (ServerPlayer player : audience(level, center)) {
                player.sendSystemMessage(Component.translatable("message.kingdomsatwar.seal.broken").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    /** Die Siegelplatten werden zum Schattentor. */
    public static void openGate(ServerLevel level, BlockPos center) {
        BlockState portal = ModBlocks.SHADOW_PORTAL.get().defaultBlockState();
        for (BlockPos pos : SealCircleLayout.plates(center)) {
            if (level.getBlockState(pos).getBlock() instanceof SealPlateBlock || level.getBlockState(pos).isAir()) {
                level.setBlock(pos, portal, Block.UPDATE_ALL);
            }
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Mitte des Siegelkreises: genau (wenn bekannt oder im geladenen Gelände zu finden), sonst ungefähr. */
    public static @Nullable BlockPos center(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        SealState seal = SealState.of(overworld);
        if (seal.center() != null) {
            return seal.center();
        }
        BlockPos found = findCenter(overworld);
        if (found != null) {
            seal.setCenter(found);
            seal.save(overworld);
            return found;
        }
        return SealCircleLocator.approximateCenter(overworld);
    }

    /** Sucht die mittlere Siegelplatte in der Säule über der Kreismitte (nur, wenn der Chunk geladen ist). */
    public static @Nullable BlockPos findCenter(ServerLevel overworld) {
        ChunkPos chunk = SealCircleLocator.find(overworld);
        if (chunk == null) {
            return null;
        }
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        if (!overworld.hasChunk(chunk.x(), chunk.z())) {
            return null;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, overworld.getMaxY() - 1, z);
        for (int y = overworld.getMaxY() - 1; y > overworld.getMinY(); y--) {
            pos.setY(y);
            BlockState state = overworld.getBlockState(pos);
            if (state.getBlock() instanceof SealPlateBlock || state.is(ModBlocks.SHADOW_PORTAL.get())) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static List<ServerPlayer> audience(ServerLevel level, BlockPos center) {
        return level.getPlayers(p -> p.distanceToSqr(Vec3.atCenterOf(center)) < AUDIENCE_RADIUS * AUDIENCE_RADIUS);
    }

    private static void sound(ServerLevel level, BlockPos center, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, center, sound, SoundSource.AMBIENT, volume, pitch);
    }

    private static void shake(ServerLevel level, BlockPos center, float strength, int ticks) {
        for (ServerPlayer player : level.getPlayers(p -> p.distanceToSqr(Vec3.atCenterOf(center)) < 96 * 96)) {
            io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player, new ScreenShakePayload(strength, ticks));
        }
    }

    /** Partikel auf einer Linie (mit etwas Bewegung entlang der Linie). */
    private static void line(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle, int count) {
        Vec3 step = to.subtract(from).scale(1.0 / count);
        for (int i = 0; i <= count; i++) {
            Vec3 p = from.add(step.scale(i));
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
        }
    }
}
