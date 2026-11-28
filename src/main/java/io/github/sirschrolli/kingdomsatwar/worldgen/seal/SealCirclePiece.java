package io.github.sirschrolli.kingdomsatwar.worldgen.seal;

import io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock;
import io.github.sirschrolli.kingdomsatwar.block.SealPlateBlock;
import io.github.sirschrolli.kingdomsatwar.block.SealSocketBlock;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Baut den Siegelkreis Chunk für Chunk (alle Formen ergeben sich aus Mitte und Startwert):
 * <ol>
 *     <li>Plattform (Radius 21) aus Siegelsteinziegeln mit Ringen aus poliertem und gemeißeltem Siegelstein, Stufen am
 *     Rand, Fundament bis zum Gelände; darüber wird alles freigeräumt.</li>
 *     <li>Gelände rundherum bis Radius 31 sanft an die Plattform angeglichen.</li>
 *     <li>In der Mitte das Siegel (5×5 Platten), sechs Monolithen mit Runen, Sockel und Deckstein, Zeichenreihen im
 *     Boden, Laternenpfosten und ein äußerer Ring verfallener Säulen.</li>
 * </ol>
 */
public class SealCirclePiece extends StructurePiece {
    private static final int CLEAR_HEIGHT = 40;
    private final BlockPos center;
    private final long seed;
    private final Map<Long, DragonType> inlays = new HashMap<>();

    public SealCirclePiece(BlockPos center, long seed) {
        super(ModStructures.SEAL_CIRCLE_PIECE.get(), 0, makeBox(center));
        this.center = center;
        this.seed = seed;
        this.collectInlays();
    }

    public SealCirclePiece(CompoundTag tag) {
        super(ModStructures.SEAL_CIRCLE_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 64), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
        this.collectInlays();
    }

    private static BoundingBox makeBox(BlockPos center) {
        int r = (int) Math.ceil(SealCircleLayout.BLEND_RADIUS) + 1;
        return new BoundingBox(center.getX() - r, center.getY() - 28, center.getZ() - r,
                center.getX() + r, center.getY() + CLEAR_HEIGHT, center.getZ() + r);
    }

    private void collectInlays() {
        for (DragonType element : DragonType.values()) {
            for (BlockPos pos : SealCircleLayout.inlays(BlockPos.ZERO, element)) {
                this.inlays.put(key(pos.getX(), pos.getZ()), element);
            }
        }
    }

    private static long key(int dx, int dz) {
        return ((long) dx << 32) ^ (dz & 0xFFFFFFFFL);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putLong("Seed", this.seed);
    }

    public BlockPos center() {
        return this.center;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        LairContext ctx = new LairContext(level, chunkBB, this.center, this.seed);
        int cx = this.center.getX();
        int cz = this.center.getZ();
        for (int x = chunkBB.minX(); x <= chunkBB.maxX(); x++) {
            for (int z = chunkBB.minZ(); z <= chunkBB.maxZ(); z++) {
                int dx = x - cx;
                int dz = z - cz;
                double d = Mth.length(dx, dz);
                if (d > SealCircleLayout.BLEND_RADIUS + 0.5) {
                    continue;
                }
                int gy = ctx.groundY(x, z);
                if (d <= SealCircleLayout.PLATFORM_RADIUS) {
                    this.platformColumn(ctx, x, z, dx, dz, d, gy);
                } else {
                    this.blendColumn(ctx, x, z, d, gy);
                }
            }
        }
        for (DragonType element : SealCircleLayout.RING) {
            this.monolith(ctx, element);
            this.lanternPost(ctx, SealCircleLayout.angle(element) + Math.toRadians(30.0));
        }
        for (int i = 0; i < 12; i++) {
            this.outerPillar(ctx, i);
        }
    }

    // =====================================================================================
    // Plattform und Gelände
    // =====================================================================================

    private void platformColumn(LairContext ctx, int x, int z, int dx, int dz, double d, int gy) {
        int floor = this.center.getY();
        for (int y = floor + 1; y <= floor + CLEAR_HEIGHT; y++) {
            if (!ctx.get(x, y, z).isAir()) {
                ctx.set(x, y, z, Blocks.AIR.defaultBlockState());
            }
        }
        // Fundament: Lücke zwischen Gelände und Boden füllen; am Rand sichtbare Mauer aus Ziegeln
        boolean rim = d > SealCircleLayout.PLATFORM_RADIUS - 1.6;
        int bottom = Math.min(gy, floor - 1);
        if (rim) {
            bottom -= 2;
        }
        for (int y = floor - 1; y > bottom; y--) {
            ctx.set(x, y, z, rim ? this.bricks(ctx, x, y, z, 0.25) : ModBlocks.SEALSTONE.base().get().defaultBlockState());
        }
        ctx.set(x, floor, z, this.floorBlock(ctx, x, z, dx, dz, d));
        // etwas Bewuchs auf dem äußeren Ring
        if (d > 16.6 && d < 20.6) {
            double r = ctx.rand(x, z, 31);
            BlockState floorState = ctx.get(x, floor, z);
            if (floorState.is(Blocks.GRASS_BLOCK) && r < 0.7) {
                ctx.set(x, floor + 1, z, (r < 0.12 ? Blocks.FERN : Blocks.SHORT_GRASS).defaultBlockState());
            } else if (floorState.is(ModBlocks.MOSSY_SEALSTONE_BRICKS.get()) && r < 0.35) {
                ctx.set(x, floor + 1, z, Blocks.MOSS_CARPET.defaultBlockState());
            }
        }
    }

    private BlockState floorBlock(LairContext ctx, int x, int z, int dx, int dz, double d) {
        if (SealCircleLayout.isPlate(dx, dz)) {
            return ModBlocks.SEAL_PLATE.get().defaultBlockState().setValue(SealPlateBlock.PART, SealCircleLayout.platePart(dx, dz));
        }
        DragonType inlay = this.inlays.get(key(dx, dz));
        if (inlay != null) {
            return ModBlocks.SEAL_INLAY.get().defaultBlockState().setValue(SealMarkBlock.ELEMENT, inlay);
        }
        if (d < 3.5) {
            return ModBlocks.POLISHED_SEALSTONE.get().defaultBlockState();
        }
        if (d < 4.5) {
            return ModBlocks.CHISELED_SEALSTONE.get().defaultBlockState();
        }
        if (d < 15.5) {
            return this.bricks(ctx, x, this.center.getY(), z, 0.15 + d * 0.006);
        }
        if (d < 16.6) {
            return ModBlocks.POLISHED_SEALSTONE.get().defaultBlockState();
        }
        if (d < 20.6) {
            double r = ctx.rand(x, z, 17);
            double patch = ctx.noise(x, z, 0.18, 19);
            if (patch > 0.68 && r < 0.75) {
                return Blocks.GRASS_BLOCK.defaultBlockState();
            }
            return this.bricks(ctx, x, this.center.getY(), z, 0.35);
        }
        // Stufen am Rand: steigen zur Mitte hin an
        double a = Math.atan2(dz, dx);
        Direction in = Direction.getApproximateNearest(-Math.cos(a), 0.0, -Math.sin(a));
        return ModBlocks.SEALSTONE_BRICKS.stairs().get().defaultBlockState().setValue(StairBlock.FACING, in);
    }

    /** Siegelsteinziegel, je nach Abnutzung auch rissig oder bemoost. */
    private BlockState bricks(LairContext ctx, int x, int y, int z, double wear) {
        double r = ctx.rand(x * 31 + y, z, 5);
        double moss = ctx.noise(x, z, 0.12, 7);
        if (r < wear * 0.5) {
            return ModBlocks.CRACKED_SEALSTONE_BRICKS.get().defaultBlockState();
        }
        if (r < wear && moss > 0.45) {
            return ModBlocks.MOSSY_SEALSTONE_BRICKS.get().defaultBlockState();
        }
        return ModBlocks.SEALSTONE_BRICKS.base().get().defaultBlockState();
    }

    /** Gelände zwischen Plattform und Umgebung: sanft von einem Block unter dem Boden zur ursprünglichen Höhe. */
    private void blendColumn(LairContext ctx, int x, int z, double d, int gy) {
        int floor = this.center.getY();
        double inner = SealCircleLayout.PLATFORM_RADIUS + SealCircleLayout.APRON;
        double t = LairContext.smooth(Mth.clamp((d - inner) / (SealCircleLayout.BLEND_RADIUS - inner), 0.0, 1.0));
        int target = (int) Math.round(Mth.lerp(t, floor - 1, gy));
        BlockState top = ctx.get(x, gy, z);
        boolean fluid = !top.getFluidState().isEmpty();
        BlockState surface = surfaceFor(top);
        BlockState below = belowFor(surface);
        if (target < gy) {
            for (int y = target + 1; y <= gy + 24; y++) {
                if (!ctx.get(x, y, z).isAir()) {
                    ctx.set(x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
            ctx.set(x, target, z, surface);
            if (!ctx.get(x, target - 1, z).isSolidRender()) {
                ctx.set(x, target - 1, z, below);
            }
        } else if (target > gy || fluid) {
            int from = gy;
            if (fluid) {
                // Wasser auffüllen bis zum Grund
                while (from > ctx.level.getMinY() + 1 && !ctx.get(x, from, z).getFluidState().isEmpty()) {
                    from--;
                }
            }
            for (int y = from + 1; y < target; y++) {
                ctx.set(x, y, z, below);
            }
            ctx.set(x, Math.max(target, from + 1), z, surface);
            // Pflanzen, die auf dem alten Boden standen, wären jetzt begraben – darüber ist schon Luft
        }
    }

    private static BlockState surfaceFor(BlockState top) {
        if (top.is(Blocks.GRASS_BLOCK) || top.is(Blocks.PODZOL) || top.is(Blocks.MYCELIUM) || top.is(Blocks.SAND) || top.is(Blocks.RED_SAND)
                || top.is(Blocks.SNOW_BLOCK) || top.is(Blocks.MOSS_BLOCK) || top.is(Blocks.COARSE_DIRT) || top.is(Blocks.STONE)
                || top.is(Blocks.GRAVEL)) {
            return top;
        }
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private static BlockState belowFor(BlockState surface) {
        if (surface.is(Blocks.SAND) || surface.is(Blocks.RED_SAND) || surface.is(Blocks.STONE) || surface.is(Blocks.GRAVEL)) {
            return surface;
        }
        return Blocks.DIRT.defaultBlockState();
    }

    // =====================================================================================
    // Monolithen, Laternen, Säulen
    // =====================================================================================

    private void monolith(LairContext ctx, DragonType element) {
        BlockPos m = SealCircleLayout.monolith(this.center, element);
        Direction in = SealCircleLayout.inward(element);
        int mx = m.getX();
        int my = m.getY();
        int mz = m.getZ();
        BlockState stone = ModBlocks.SEAL_MONOLITH.get().defaultBlockState();
        BlockState rune = ModBlocks.SEAL_RUNE.get().defaultBlockState().setValue(SealMarkBlock.ELEMENT, element);
        // Sockel: 3×3, zwei Lagen
        for (int yy = 1; yy <= 2; yy++) {
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    ctx.set(mx + ox, my + yy, mz + oz, stone);
                }
            }
        }
        BlockPos socket = SealCircleLayout.socket(this.center, element);
        ctx.set(socket.getX(), socket.getY(), socket.getZ(), ModBlocks.SEAL_SOCKET.get().defaultBlockState()
                .setValue(SealSocketBlock.FACING, in).setValue(SealSocketBlock.ELEMENT, element));
        // Schaft: Kreuzform mit Runen (zur Mitte vier übereinander, sonst je eine)
        for (int yy = 3; yy <= 8; yy++) {
            ctx.set(mx, my + yy, mz, stone);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                boolean runeHere = side == in ? yy >= 4 && yy <= 7 : yy == 6;
                ctx.set(mx + side.getStepX(), my + yy, mz + side.getStepZ(), runeHere ? rune : stone);
            }
        }
        // Spitze und Deckstein
        ctx.set(mx, my + 9, mz, stone);
        ctx.set(mx, my + 10, mz, stone);
        ctx.set(mx, my + SealCircleLayout.MONOLITH_HEIGHT, mz, rune);
    }

    private void lanternPost(LairContext ctx, double angle) {
        int x = this.center.getX() + (int) Math.round(Math.cos(angle) * 9);
        int z = this.center.getZ() + (int) Math.round(Math.sin(angle) * 9);
        int y = this.center.getY();
        BlockState wall = ModBlocks.SEALSTONE_BRICKS.wall().get().defaultBlockState();
        ctx.set(x, y + 1, z, wall);
        ctx.set(x, y + 2, z, wall);
        ctx.set(x, y + 3, z, Blocks.SOUL_LANTERN.defaultBlockState());
    }

    /** Äußerer Ring aus zwölf verfallenen Säulen (auf dem angeglichenen Gelände). */
    private void outerPillar(LairContext ctx, int index) {
        double angle = Math.toRadians(index * 30.0 + 15.0);
        int x = this.center.getX() + (int) Math.round(Math.cos(angle) * 25);
        int z = this.center.getZ() + (int) Math.round(Math.sin(angle) * 25);
        if (!ctx.box.isInside(x, this.center.getY(), z)) {
            return;
        }
        int ground = this.center.getY() + 12;
        while (ground > this.center.getY() - 20 && !LairContext.isGround(ctx.get(x, ground, z))) {
            ground--;
        }
        double r = ctx.rand(index, 7, 41);
        int height = 2 + (int) (r * 4.0);
        for (int yy = 1; yy <= height; yy++) {
            ctx.set(x, ground + yy, z, yy == 1 ? ModBlocks.POLISHED_SEALSTONE.get().defaultBlockState() : this.bricks(ctx, x, ground + yy, z, 0.6));
        }
        if (height >= 4) {
            ctx.set(x, ground + height + 1, z, ModBlocks.SEALSTONE_BRICKS.slab().get().defaultBlockState());
        } else {
            // umgestürzter Säulenrest daneben
            double a2 = angle + Math.PI / 2;
            int fx = x + (int) Math.round(Math.cos(a2) * 2);
            int fz = z + (int) Math.round(Math.sin(a2) * 2);
            int fy = this.center.getY() + 12;
            while (fy > this.center.getY() - 20 && !LairContext.isGround(ctx.get(fx, fy, fz))) {
                fy--;
            }
            BlockState fallen = this.bricks(ctx, fx, fy, fz, 0.8);
            if (ctx.get(fx, fy + 1, fz).canBeReplaced()) {
                ctx.set(fx, fy + 1, fz, fallen);
            }
        }
        // Moos und Gras am Fuß
        if (ctx.get(x + 1, ground + 1, z).isAir() && LairContext.isGround(ctx.get(x + 1, ground, z)) && r < 0.6) {
            ctx.set(x + 1, ground + 1, z, Blocks.MOSS_CARPET.defaultBlockState());
        }
        if (ctx.get(x, ground, z).is(BlockTags.DIRT)) {
            ctx.set(x, ground, z, Blocks.COARSE_DIRT.defaultBlockState());
        }
    }
}
