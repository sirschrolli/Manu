package io.github.sirschrolli.kingdomsatwar.worldgen.citadel;

import io.github.sirschrolli.kingdomsatwar.block.entity.ShadowThroneCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Baut die Schattenzitadelle Chunk für Chunk (alle Formen ergeben sich aus Mitte und Startwert):
 * <ol>
 *     <li>Achteckige Ringmauer (Innenseite 33,5 Blöcke von der Mitte, 4 Blöcke dick, 12 hoch) mit Wehrgang, Zinnen,
 *     leuchtenden Schießscharten aus Nachtkristall, Nischen mit Seelenlaternen und einem geböschten Sockel.</li>
 *     <li>Acht Ecktürme (22 hoch, mit Wohngeschossen, Leitern, Truhen und einem spitzen Turmhelm mit Kristallspitze).</li>
 *     <li>Torhaus im Süden mit zwei Tortürmen, halb hochgezogenem Fallgitter und einer breiten Freitreppe ins Land.</li>
 *     <li>Thronhof: Boden aus poliertem Schattenstein mit Ringen, Strahlen und einem Siegel aus Seelenkristall in der
 *     Mitte, ein Prozessionsweg mit Seelenfeuer-Schalen, Säulen (zwei davon eingestürzt) und im Norden das Podest mit
 *     Nyxars Thron zwischen zwei Obelisken. Darunter verborgen der Thronkern.</li>
 *     <li>Gelände: Fundament bis zum Boden, ringsum ein Hügel, der zur Mauer ansteigt; höheres Land wird abgetragen.</li>
 * </ol>
 */
public class CitadelPiece extends StructurePiece {
    static final double R_COURT = 33.5;
    static final double R_WALL = 37.5;
    static final double R_PLINTH = 40.5;
    static final double R_BLEND = 54.0;
    static final int WALL_H = 12;
    static final int TOWER_H = 22;
    static final double TOWER_DIST = 38.4;
    static final double TOWER_R = 5.6;
    static final double TOWER_IN = 3.6;
    static final int CLEAR = 44;
    /** Podest: Mitte (relativ), Radien der drei Stufen. */
    static final int DAIS_Z = -25;
    static final int THRONE_Z = -29;
    /** Treppe vor dem Tor. */
    static final int STAIR_Z0 = 43;
    static final int STAIR_STEPS = 28;

    private final BlockPos center;
    private final long seed;
    private final int[][] towers = new int[8][2];

    public CitadelPiece(BlockPos center, long seed) {
        super(ModStructures.SHADOW_CITADEL_PIECE.get(), 0, makeBox(center));
        this.center = center;
        this.seed = seed;
        this.layoutTowers();
    }

    public CitadelPiece(CompoundTag tag) {
        super(ModStructures.SHADOW_CITADEL_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 64), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
        this.layoutTowers();
    }

    private static BoundingBox makeBox(BlockPos c) {
        int r = (int) R_BLEND + 3;
        return new BoundingBox(c.getX() - r, c.getY() - 50, c.getZ() - r, c.getX() + r, c.getY() + CLEAR,
                c.getZ() + STAIR_Z0 + STAIR_STEPS + 2);
    }

    private void layoutTowers() {
        for (int k = 0; k < 8; k++) {
            double a = Math.toRadians(22.5 + 45.0 * k);
            this.towers[k][0] = (int) Math.round(Math.cos(a) * TOWER_DIST);
            this.towers[k][1] = (int) Math.round(Math.sin(a) * TOWER_DIST);
        }
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

    /** Mitte des Thronhofs, wo Nyxar erwacht (ein Block über dem Boden). */
    public BlockPos arenaCenter() {
        return this.center.above();
    }

    // =====================================================================================
    // Formen
    // =====================================================================================

    /** Abstand im regelmäßigen Achteck (flache Seiten nach Norden, Süden, Osten und Westen). */
    static double oct(double dx, double dz) {
        double ax = Math.abs(dx);
        double az = Math.abs(dz);
        return Math.max(Math.max(ax, az), (ax + az) * 0.7071067811865476);
    }

    /** Bogenlänge entlang der Mauer (für Zinnen, Scharten und Nischen). */
    static int along(double dx, double dz) {
        return Mth.floor((Math.atan2(dz, dx) + Math.PI) * 35.5);
    }

    private int nearestTower(int dx, int dz) {
        int best = 0;
        double bd = Double.MAX_VALUE;
        for (int k = 0; k < 8; k++) {
            double d = Mth.lengthSquared(dx - this.towers[k][0], dz - this.towers[k][1]);
            if (d < bd) {
                bd = d;
                best = k;
            }
        }
        return best;
    }

    static boolean inGatehouse(int dx, int dz) {
        return Math.abs(dx) <= 8 && dz >= 31 && dz <= 42;
    }

    static boolean inGatePassage(int dx, int dz) {
        return Math.abs(dx) <= 2 && dz >= 30 && dz <= 43;
    }

    static int gateArch(int dx) {
        return Math.abs(dx) <= 1 ? 7 : 6;
    }

    static double turretDist(int dx, int dz) {
        return Math.min(Mth.length(dx - 8, dz - 42), Mth.length(dx + 8, dz - 42));
    }

    /** Höhe der Podeststufe (0 = kein Podest). */
    static int daisLevel(int dx, int dz) {
        double r = Mth.length(dx, dz - DAIS_Z);
        if (r > 9.5 || oct(dx, dz) > R_COURT) {
            return 0;
        }
        return r <= 5.5 ? 3 : r <= 7.5 ? 2 : 1;
    }

    // =====================================================================================
    // Bauen
    // =====================================================================================

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        LairContext ctx = new LairContext(level, chunkBB, this.center, this.seed);
        int cx = this.center.getX();
        int cz = this.center.getZ();
        for (int x = chunkBB.minX(); x <= chunkBB.maxX(); x++) {
            for (int z = chunkBB.minZ(); z <= chunkBB.maxZ(); z++) {
                this.column(ctx, x, z, x - cx, z - cz);
            }
        }
        this.throne(ctx);
        this.obelisk(ctx, -7, -27);
        this.obelisk(ctx, 7, -27);
        double[] pillarAngles = {22.5, 67.5, 112.5, 157.5, 202.5, 337.5};
        for (int i = 0; i < pillarAngles.length; i++) {
            double a = Math.toRadians(pillarAngles[i]);
            this.pillar(ctx, (int) Math.round(Math.cos(a) * 23), (int) Math.round(Math.sin(a) * 23), i == 1 || i == 4);
        }
        for (int dz : new int[] {27, 18, 9, 0, -10}) {
            this.brazier(ctx, -5, dz);
            this.brazier(ctx, 5, dz);
        }
        for (int k = 0; k < 8; k++) {
            this.towerInterior(ctx, k);
        }
        this.gateDetails(ctx);
        this.throneCore(ctx);
    }

    private BlockState s(net.minecraft.world.level.block.Block block) {
        return block.defaultBlockState();
    }

    private BlockState bricks(LairContext ctx, int x, int y, int z, double wear) {
        double r = ctx.rand(x * 31 + y, z, 5);
        if (r < wear) {
            return s(ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS.get());
        }
        return s(ShadowBlocks.SHADOW_STONE_BRICKS.base().get());
    }

    private BlockState polished() {
        return s(ShadowBlocks.POLISHED_SHADOW_STONE.base().get());
    }

    private BlockState deep() {
        return ShadowBlocks.DEEP_SHADOW_STONE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
    }

    private void column(LairContext ctx, int x, int z, int dx, int dz) {
        double o = oct(dx, dz);
        int k = this.nearestTower(dx, dz);
        double td = Mth.length(dx - this.towers[k][0], dz - this.towers[k][1]);
        boolean tower = td <= TOWER_R + 0.3;
        boolean flare = !tower && td <= TOWER_R + 1.4 && o > R_WALL;
        boolean gate = inGatehouse(dx, dz) || turretDist(dx, dz) <= 3.9;
        boolean stairs = Math.abs(dx) <= 4 && dz >= STAIR_Z0 && dz < STAIR_Z0 + STAIR_STEPS;
        if (o > R_BLEND + 0.5 && !stairs && !tower && !flare && !gate) {
            return;
        }
        int floor = this.center.getY();
        int gy = ctx.groundY(x, z);
        if (o <= R_PLINTH || tower || flare || gate) {
            // innerhalb der Festung: alles freiräumen, Fundament bis zum Gelände
            for (int y = floor + 1; y <= floor + CLEAR; y++) {
                if (!ctx.get(x, y, z).isAir()) {
                    ctx.set(x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
            for (int y = floor - 1; y > Math.min(gy, floor - 2) - (o > R_COURT ? 3 : 0); y--) {
                BlockState fill = o > R_COURT || tower ? (y > gy - 1 ? this.bricks(ctx, x, y, z, 0.12) : this.deep())
                        : s(ShadowBlocks.SHADOW_STONE.base().get());
                if (o > R_WALL && !tower && !gate) {
                    fill = this.deep();
                }
                ctx.set(x, y, z, fill);
            }
        }
        if (tower) {
            this.towerColumn(ctx, x, z, dx, dz, k, td);
            return;
        }
        if (flare && !gate) {
            // ausgestellter Turmsockel
            for (int y = floor; y <= floor + 2; y++) {
                ctx.set(x, y, z, this.deep());
            }
            return;
        }
        if (gate) {
            this.gateColumn(ctx, x, z, dx, dz);
            return;
        }
        if (o <= R_COURT) {
            this.courtColumn(ctx, x, z, dx, dz, o);
        } else if (o <= R_WALL) {
            this.wallColumn(ctx, x, z, dx, dz, o);
        } else if (o <= R_PLINTH) {
            // geböschter Sockel vor der Mauer
            int top = floor + 2 - (int) Math.floor(o - R_WALL);
            for (int y = floor; y <= top; y++) {
                ctx.set(x, y, z, this.deep());
            }
        } else if (stairs) {
            this.stairColumn(ctx, x, z, dx, dz, gy);
        } else {
            this.blendColumn(ctx, x, z, o, gy);
        }
    }

    // ---------------------------------------------------------------- Hof

    private void courtColumn(LairContext ctx, int x, int z, int dx, int dz, double o) {
        int floor = this.center.getY();
        int dais = daisLevel(dx, dz);
        ctx.set(x, floor, z, this.floorBlock(ctx, x, z, dx, dz, o));
        if (dais > 0) {
            for (int lvl = 1; lvl <= dais; lvl++) {
                boolean edge = lvl == dais;
                BlockState state = edge ? this.daisTop(ctx, x, z, dx, dz, lvl) : this.bricks(ctx, x, floor + lvl, z, 0.05);
                ctx.set(x, floor + lvl, z, state);
            }
        }
    }

    private BlockState daisTop(LairContext ctx, int x, int z, int dx, int dz, int lvl) {
        double r = Mth.length(dx, dz - DAIS_Z);
        double outer = lvl == 1 ? 9.5 : lvl == 2 ? 7.5 : 5.5;
        if (r > outer - 1.0) {
            // Stufenkante: Treppe, die zur Mitte des Podests ansteigt
            double a = Math.atan2(dz - DAIS_Z, dx);
            Direction in = Direction.getApproximateNearest(-Math.cos(a), 0.0, -Math.sin(a));
            return ShadowBlocks.POLISHED_SHADOW_STONE.stairs().get().defaultBlockState().setValue(StairBlock.FACING, in);
        }
        if (lvl == 3 && Math.abs(dx) <= 1 && dz > THRONE_Z) {
            return s(ShadowBlocks.CHISELED_SHADOW_STONE.get());
        }
        return this.polished();
    }

    private BlockState floorBlock(LairContext ctx, int x, int z, int dx, int dz, double o) {
        double r = Mth.length(dx, dz);
        // Siegel in der Mitte: Auge aus Seelenkristall, Iris aus Nachtkristall, Strahlenkranz
        if (r <= 1.5) {
            return s(ShadowBlocks.SOUL_CRYSTAL_BLOCK.get());
        }
        if (r <= 3.0) {
            return s(ShadowBlocks.NIGHT_CRYSTAL.get());
        }
        if (r <= 4.0) {
            return s(ShadowBlocks.CHISELED_SHADOW_STONE.get());
        }
        double a = Math.atan2(dz, dx);
        double spoke = Math.abs(Mth.wrapDegrees(Math.toDegrees(a) * 2.0) / 2.0) % 22.5;
        boolean onSpoke = Math.min(spoke, 22.5 - spoke) * Mth.DEG_TO_RAD * r < 0.55;
        if (r <= 6.5) {
            return onSpoke ? s(ShadowBlocks.SOUL_CRYSTAL_BLOCK.get()) : this.polished();
        }
        // Prozessionsweg vom Tor zum Podest
        if (Math.abs(dx) <= 2 && dz > 6) {
            return Math.abs(dx) == 2 ? this.polished() : this.bricks(ctx, x, this.center.getY(), z, 0.06);
        }
        if (Math.abs(r - 12.0) < 0.55 || Math.abs(r - 20.0) < 0.55) {
            return this.bricks(ctx, x, this.center.getY(), z, 0.08);
        }
        if (Math.abs(r - 28.0) < 0.55) {
            return (dx + dz) % 2 == 0 ? s(ShadowBlocks.CHISELED_SHADOW_STONE.get()) : this.polished();
        }
        if (onSpoke && r > 6.5) {
            return this.bricks(ctx, x, this.center.getY(), z, 0.1);
        }
        double rr = ctx.rand(x, z, 41);
        if (o > 27.5 && ctx.noise(x, z, 0.16, 43) > 0.62 && rr < 0.8) {
            return s(ShadowBlocks.GLIMMER_MOSS.get());
        }
        if (rr < 0.05) {
            return s(ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS.get());
        }
        if (rr < 0.09) {
            return s(ShadowBlocks.SHADOW_STONE.base().get());
        }
        return this.polished();
    }

    // ---------------------------------------------------------------- Mauer

    private void wallColumn(LairContext ctx, int x, int z, int dx, int dz, double o) {
        int floor = this.center.getY();
        int s = along(dx, dz);
        boolean outerFace = o > R_WALL - 1.0;
        boolean innerFace = o <= R_COURT + 1.0;
        for (int y = floor; y <= floor + WALL_H; y++) {
            int h = y - floor;
            BlockState state;
            if (h <= 1) {
                state = this.deep();
            } else if (h == WALL_H) {
                state = o <= R_WALL - 1.0 ? this.polished() : this.bricks(ctx, x, y, z, 0.05);
            } else if (h == 6 && (outerFace || innerFace)) {
                state = this.polished();
            } else if (h == WALL_H - 1 && outerFace) {
                state = Math.floorMod(s, 4) == 0 ? s(ShadowBlocks.CHISELED_SHADOW_STONE.get()) : this.polished();
            } else {
                state = this.bricks(ctx, x, y, z, 0.1);
            }
            // leuchtende Schießscharten außen
            if (outerFace && h >= 7 && h <= 9 && Math.floorMod(s, 6) == 0) {
                state = s(ShadowBlocks.NIGHT_CRYSTAL.get());
            }
            // Nischen innen mit Seelenlaternen
            if (innerFace && h >= 1 && h <= 3 && Math.floorMod(s, 9) >= 4 && Math.floorMod(s, 9) <= 5) {
                state = h == 1 && Math.floorMod(s, 9) == 4 ? Blocks.SOUL_LANTERN.defaultBlockState() : Blocks.AIR.defaultBlockState();
            }
            ctx.set(x, y, z, state);
        }
        // Brüstung außen mit Zinnen (auf jeder vierten Zinne eine Mondlaterne)
        if (o > R_WALL - 1.0) {
            ctx.set(x, floor + WALL_H + 1, z, this.bricks(ctx, x, floor + WALL_H + 1, z, 0.06));
            if (Math.floorMod(s, 4) < 2) {
                ctx.set(x, floor + WALL_H + 2, z, this.polished());
                if (Math.floorMod(s, 16) == 0) {
                    ctx.set(x, floor + WALL_H + 3, z, ShadowBlocks.MOON_LANTERN.get().defaultBlockState());
                }
            }
        }
    }

    // ---------------------------------------------------------------- Türme

    private void towerColumn(LairContext ctx, int x, int z, int dx, int dz, int k, double td) {
        int floor = this.center.getY();
        int tx = this.towers[k][0];
        int tz = this.towers[k][1];
        double ang = Math.atan2(dz - tz, dx - tx);
        int s = Mth.floor((ang + Math.PI) * td);
        boolean shell = td > TOWER_IN;
        // Richtung zur Hofmitte (Tür) und Wehrgang (Durchgänge in Höhe der Mauerkrone)
        double toCenter = Math.atan2(-tz, -tx);
        double doorDiff = Math.abs(Mth.wrapDegrees(Math.toDegrees(ang - toCenter)));
        double o = oct(dx, dz);
        boolean walkway = o > R_COURT && o <= R_WALL - 1.0;
        for (int y = floor; y <= floor + TOWER_H + 1; y++) {
            int h = y - floor;
            BlockState state;
            if (shell) {
                if (h <= 1) {
                    state = this.deep();
                } else if (h == 11 || h == TOWER_H - 1) {
                    state = this.polished();
                } else {
                    state = this.bricks(ctx, x, y, z, 0.1);
                }
                boolean window = (h == 8 || h == 9 || h == 18 || h == 19)
                        && Math.abs(Mth.wrapDegrees(Math.toDegrees(ang) - 45.0 - 22.5 * k)) % 90.0 < 9.0;
                if (window) {
                    state = s(ShadowBlocks.NIGHT_CRYSTAL.get());
                }
                if (h >= 1 && h <= 3 && doorDiff < 13.0) {
                    state = Blocks.AIR.defaultBlockState();
                }
                if (h >= WALL_H + 1 && h <= WALL_H + 3 && walkway) {
                    state = Blocks.AIR.defaultBlockState();
                }
                if (h == TOWER_H + 1) {
                    state = this.bricks(ctx, x, y, z, 0.05);
                }
            } else {
                state = h == 0 || h == 6 || h == WALL_H || h == 17 || h == TOWER_H ? this.polished() : Blocks.AIR.defaultBlockState();
            }
            ctx.set(x, y, z, state);
        }
        // Zinnen rund um die Turmplattform
        if (td > TOWER_R - 1.0 && Math.floorMod(s, 4) < 2) {
            ctx.set(x, floor + TOWER_H + 2, z, this.polished());
        }
        // Turmhelm in der Mitte der Plattform: kurzer Rundbau, dann ein spitzes Dach mit Kristall
        if (td <= 2.6) {
            for (int y = floor + TOWER_H + 1; y <= floor + TOWER_H + 6; y++) {
                boolean wall = td > 1.5;
                boolean window = wall && y == floor + TOWER_H + 4 && Math.floorMod(s, 3) == 0;
                ctx.set(x, y, z, window ? s(ShadowBlocks.NIGHT_CRYSTAL.get()) : wall ? this.bricks(ctx, x, y, z, 0.05)
                        : (y == floor + TOWER_H + 1 ? this.polished() : Blocks.AIR.defaultBlockState()));
            }
        }
        for (int i = 0; i < 10; i++) {
            double rc = 3.4 - i * 0.38;
            if (rc < 0.3) {
                break;
            }
            if (td <= rc) {
                ctx.set(x, floor + TOWER_H + 7 + i, z, this.deep());
            }
        }
        if (dx == tx && dz == tz) {
            ctx.set(x, floor + TOWER_H + 16, z, s(ShadowBlocks.NIGHT_CRYSTAL.get()));
            ctx.set(x, floor + TOWER_H + 17, z, ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get().defaultBlockState());
        }
    }

    /** Leiter, Laternen und (in jedem zweiten Turm) eine Truhe. */
    private void towerInterior(LairContext ctx, int k) {
        int floor = this.center.getY();
        int tx = this.center.getX() + this.towers[k][0];
        int tz = this.center.getZ() + this.towers[k][1];
        // Leiter an der Außenseite (Achse, die am weitesten von der Hofmitte wegzeigt)
        Direction out = Direction.getApproximateNearest(this.towers[k][0], 0, this.towers[k][1]);
        int lx = tx + out.getStepX() * 3;
        int lz = tz + out.getStepZ() * 3;
        for (int y = floor + 1; y <= floor + TOWER_H; y++) {
            ctx.set(lx, y, lz, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, out.getOpposite()));
        }
        for (int y : new int[] {floor + 5, floor + 11, floor + 16, floor + 21}) {
            ctx.set(tx, y, tz, ShadowBlocks.MOON_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        if (k % 2 == 0) {
            Direction side = out.getClockWise();
            int chx = tx + side.getStepX() * 3;
            int chz = tz + side.getStepZ() * 3;
            if (ctx.inside(chx, floor + 1, chz)) {
                BlockPos pos = new BlockPos(chx, floor + 1, chz);
                ctx.set(chx, floor + 1, chz, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, side.getOpposite()));
                RandomizableContainer.setBlockEntityLootTable(ctx.level, RandomSource.create(pos.asLong() ^ this.seed), pos,
                        ModLootTables.SHADOW_CITADEL_CHEST);
            }
        }
    }

    // ---------------------------------------------------------------- Torhaus

    private void gateColumn(LairContext ctx, int x, int z, int dx, int dz) {
        int floor = this.center.getY();
        double turret = turretDist(dx, dz);
        if (turret <= 3.9) {
            // Tortürme: massiv, mit Zinnen und einem spitzen Dach
            int top = floor + 20;
            for (int y = floor; y <= top; y++) {
                int h = y - floor;
                ctx.set(x, y, z, h <= 1 ? this.deep() : h == 10 || h == 19 ? this.polished()
                        : (h == 14 || h == 15) && turret > 3.0 && Math.floorMod(dx + dz, 3) == 0 ? s(ShadowBlocks.NIGHT_CRYSTAL.get())
                        : this.bricks(ctx, x, y, z, 0.1));
            }
            int s = Mth.floor((Math.atan2(dz - 42, Math.abs(dx) - 8) + Math.PI) * turret);
            if (turret > 2.9 && Math.floorMod(s, 4) < 2) {
                ctx.set(x, top + 1, z, this.polished());
            }
            for (int i = 0; i < 9; i++) {
                double rc = 2.6 - i * 0.3;
                if (rc < 0.3) {
                    break;
                }
                if (turret <= rc) {
                    ctx.set(x, top + 1 + i, z, this.deep());
                }
            }
            if (Math.abs(dx) == 8 && dz == 42) {
                ctx.set(x, top + 9, z, s(ShadowBlocks.NIGHT_CRYSTAL.get()));
            }
            return;
        }
        int top = floor + 16;
        boolean passage = inGatePassage(dx, dz);
        for (int y = floor; y <= top; y++) {
            int h = y - floor;
            BlockState state;
            if (passage && h >= 1 && h <= gateArch(dx)) {
                state = Blocks.AIR.defaultBlockState();
            } else if (h == 0) {
                state = passage ? this.bricks(ctx, x, y, z, 0.05) : this.deep();
            } else if (h == 8 || h == top - floor) {
                state = this.polished();
            } else {
                state = this.bricks(ctx, x, y, z, 0.1);
            }
            ctx.set(x, y, z, state);
        }
        // Zinnen ringsum auf dem Torhaus
        boolean rim = Math.abs(dx) == 8 || dz == 31 || dz == 42;
        if (rim && Math.floorMod(dx + dz, 4) < 2) {
            ctx.set(x, top + 1, z, this.polished());
        }
    }

    private void gateDetails(LairContext ctx) {
        int floor = this.center.getY();
        int cx = this.center.getX();
        int cz = this.center.getZ();
        // halb hochgezogenes Fallgitter
        for (int dx = -2; dx <= 2; dx++) {
            for (int h = 4; h <= gateArch(dx); h++) {
                ctx.set(cx + dx, floor + h, cz + 37, Blocks.IRON_BARS.defaultBlockState());
            }
        }
        // Zeichen über dem Tor: gemeißelter Schattenstein mit Seelenkristall
        ctx.set(cx, floor + 10, cz + 43, s(ShadowBlocks.CHISELED_SHADOW_STONE.get()));
        ctx.set(cx - 1, floor + 10, cz + 43, this.polished());
        ctx.set(cx + 1, floor + 10, cz + 43, this.polished());
        ctx.set(cx, floor + 11, cz + 43, s(ShadowBlocks.SOUL_CRYSTAL_BLOCK.get()));
        ctx.set(cx, floor + 9, cz + 43, this.polished());
        // Laternenpfeiler oben an der Treppe
        for (int sx : new int[] {-5, 5}) {
            for (int h = 1; h <= 3; h++) {
                ctx.set(cx + sx, floor + h, cz + STAIR_Z0, this.deep());
            }
            ctx.set(cx + sx, floor + 4, cz + STAIR_Z0, Blocks.SOUL_LANTERN.defaultBlockState());
        }
    }

    // ---------------------------------------------------------------- Freitreppe und Gelände

    private void stairColumn(LairContext ctx, int x, int z, int dx, int dz, int gy) {
        int floor = this.center.getY();
        int step = dz - STAIR_Z0;
        int top = floor - step;
        if (top <= gy && step > 0) {
            this.blendColumn(ctx, x, z, oct(dx, dz), gy);
            return;
        }
        for (int y = top + 1; y <= floor + 14; y++) {
            if (!ctx.get(x, y, z).isAir()) {
                ctx.set(x, y, z, Blocks.AIR.defaultBlockState());
            }
        }
        boolean side = Math.abs(dx) == 4;
        for (int y = top - 1; y > gy - 2; y--) {
            ctx.set(x, y, z, side ? this.bricks(ctx, x, y, z, 0.08) : s(ShadowBlocks.SHADOW_STONE.base().get()));
        }
        if (side) {
            ctx.set(x, top, z, this.bricks(ctx, x, top, z, 0.08));
            ctx.set(x, top + 1, z, ShadowBlocks.POLISHED_SHADOW_STONE.slab().get().defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        } else if (step == 0) {
            ctx.set(x, top, z, this.polished());
        } else {
            ctx.set(x, top, z, ShadowBlocks.SHADOW_STONE_BRICKS.stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        }
    }

    /** Ringsum: ein Hügel steigt zur Festung an, höheres Land wird sanft abgetragen. */
    private void blendColumn(LairContext ctx, int x, int z, double o, int gy) {
        int floor = this.center.getY();
        double t = o - R_PLINTH;
        int mound = floor - 1 - (int) Math.floor(t * 0.9);
        int cut = floor + (int) Math.floor(t * 1.4);
        if (gy < mound) {
            for (int y = gy + 1; y <= mound; y++) {
                ctx.set(x, y, z, y == mound && ctx.rand(x, z, 61) < 0.3 ? s(ShadowBlocks.SHADOW_STONE.base().get())
                        : y == mound ? s(ShadowBlocks.SHADOW_GRASS.get()) : y > mound - 3 ? s(ShadowBlocks.SHADOW_SOIL.get())
                        : s(ShadowBlocks.SHADOW_STONE.base().get()));
            }
        } else if (gy > cut) {
            for (int y = cut + 1; y <= gy + 16; y++) {
                if (!ctx.get(x, y, z).isAir()) {
                    ctx.set(x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
            BlockState top = ctx.get(x, cut, z);
            if (!top.isAir() && !top.is(ShadowBlocks.SHADOW_GRASS.get())) {
                ctx.set(x, cut, z, s(ctx.rand(x, z, 63) < 0.7 ? ShadowBlocks.SHADOW_GRASS.get() : ShadowBlocks.SHADOW_SOIL.get()));
            }
        }
    }

    // ---------------------------------------------------------------- Thron, Obelisken, Säulen, Feuerschalen

    private void throne(LairContext ctx) {
        int floor = this.center.getY();
        int cx = this.center.getX();
        int cz = this.center.getZ();
        int base = floor + 4;
        int z = cz + THRONE_Z;
        // Sitzfläche (Lehne nach Norden) und Armlehnen
        for (int dx = -1; dx <= 1; dx++) {
            ctx.set(cx + dx, base, z + 1, ShadowBlocks.POLISHED_SHADOW_STONE.stairs().get().defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH));
        }
        for (int sx : new int[] {-2, 2}) {
            ctx.set(cx + sx, base, z + 1, s(ShadowBlocks.CHISELED_SHADOW_STONE.get()));
            ctx.set(cx + sx, base + 1, z + 1, ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get().defaultBlockState());
        }
        // hohe Rückenlehne mit Krone aus Hörnern und einem Auge aus Seelenkristall
        for (int dx = -2; dx <= 2; dx++) {
            int top = Math.abs(dx) == 2 ? 4 : Math.abs(dx) == 1 ? 6 : 8;
            for (int h = 0; h <= top; h++) {
                BlockState state = h == top ? this.polished() : this.deep();
                if (dx == 0 && h == 5) {
                    state = s(ShadowBlocks.SOUL_CRYSTAL_BLOCK.get());
                } else if (dx == 0 && h == 4) {
                    state = s(ShadowBlocks.CHISELED_SHADOW_STONE.get());
                }
                ctx.set(cx + dx, base + h, z, state);
                ctx.set(cx + dx, base + h, z - 1, h <= top - 1 ? this.bricks(ctx, cx + dx, base + h, z - 1, 0.05) : this.polished());
            }
        }
        // Hörner der Krone: schwingen von der Lehne nach außen und oben
        for (int sx : new int[] {-1, 1}) {
            for (int h = 3; h <= 6; h++) {
                ctx.set(cx + sx * 3, base + h, z, this.deep());
            }
            ctx.set(cx + sx * 4, base + 6, z, this.deep());
            ctx.set(cx + sx * 4, base + 7, z, this.deep());
            ctx.set(cx + sx * 4, base + 8, z, this.polished());
        }
        ctx.set(cx, base + 9, z, ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get().defaultBlockState());
    }

    private void obelisk(LairContext ctx, int ox, int oz) {
        int floor = this.center.getY();
        int x0 = this.center.getX() + ox;
        int z0 = this.center.getZ() + oz;
        int base = floor + 1;
        for (int y = base; y <= floor + 13; y++) {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    boolean band = y == floor + 3 || y == floor + 8 || y == floor + 13;
                    ctx.set(x0 + a, y, z0 + b, band ? this.polished() : this.deep());
                }
            }
        }
        for (int y = floor + 14; y <= floor + 17; y++) {
            ctx.set(x0, y, z0, y == floor + 16 ? s(ShadowBlocks.SOUL_CRYSTAL_BLOCK.get()) : this.deep());
        }
        ctx.set(x0, floor + 18, z0, Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
    }

    private void pillar(LairContext ctx, int px, int pz, boolean broken) {
        int floor = this.center.getY();
        int x0 = this.center.getX() + px;
        int z0 = this.center.getZ() + pz;
        int top = broken ? floor + 4 + (int) (ctx.rand(px, pz, 71) * 3) : floor + 10;
        for (int y = floor + 1; y <= top; y++) {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    boolean corner = a != 0 && b != 0;
                    if (broken && y > top - 2 && ctx.rand(x0 + a, z0 + b + y, 73) < 0.5) {
                        continue;
                    }
                    BlockState state = y <= floor + 1 ? this.deep() : y == top && !broken ? this.polished()
                            : corner ? this.deep() : this.bricks(ctx, x0 + a, y, z0 + b, broken ? 0.4 : 0.1);
                    ctx.set(x0 + a, y, z0 + b, state);
                }
            }
        }
        if (!broken) {
            ctx.set(x0, top + 1, z0, ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get().defaultBlockState());
            for (Direction d : Direction.Plane.HORIZONTAL) {
                ctx.set(x0 + d.getStepX() * 2, top - 1, z0 + d.getStepZ() * 2,
                        ShadowBlocks.MOON_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, true));
                ctx.set(x0 + d.getStepX() * 2, top, z0 + d.getStepZ() * 2, this.polished());
            }
        } else {
            // Trümmer ringsum
            for (int i = 0; i < 9; i++) {
                double a = ctx.rand(px, pz + i, 75) * Math.PI * 2;
                double r = 2.5 + ctx.rand(px + i, pz, 77) * 3.0;
                int rx = x0 + (int) Math.round(Math.cos(a) * r);
                int rz = z0 + (int) Math.round(Math.sin(a) * r);
                BlockState rubble = i % 3 == 0 ? s(ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS.get())
                        : ShadowBlocks.SHADOW_STONE_BRICKS.slab().get().defaultBlockState();
                ctx.set(rx, floor + 1, rz, rubble);
            }
        }
    }

    private void brazier(LairContext ctx, int bx, int bz) {
        int floor = this.center.getY();
        int x = this.center.getX() + bx;
        int z = this.center.getZ() + bz;
        ctx.set(x, floor + 1, z, this.polished());
        ctx.set(x, floor + 2, z, Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
    }

    private void throneCore(LairContext ctx) {
        int floor = this.center.getY();
        BlockPos core = new BlockPos(this.center.getX(), floor - 3, this.center.getZ() + DAIS_Z);
        if (ctx.inside(core.getX(), core.getY(), core.getZ())) {
            ctx.set(core.getX(), core.getY(), core.getZ(), ModBlocks.SHADOW_THRONE_CORE.get().defaultBlockState());
            if (ctx.level.getBlockEntity(core) instanceof ShadowThroneCoreBlockEntity be) {
                be.setup(this.arenaCenter());
            }
        }
    }
}
