package io.github.sirschrolli.kingdomsatwar.worldgen.merfolk;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidEntity;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidProfession;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.TritonEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.SeaWoods;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Ein Meeresdorf auf dem Grund des Urmeers (seit 0.93), Chunk für Chunk gebaut:
 * <ol>
 *     <li>Der Grund wird eingeebnet (zum Rand hin sanft ins natürliche Gelände übergeleitet), Bäume, Tang und Seegras im
 *     Dorf verschwinden.</li>
 *     <li>In der Mitte der <b>Dorfplatz</b> aus Prismarin mit Speichen aus Perlmutt, darauf die <b>Dreizacksäule</b> mit
 *     leuchtenden Spitzen und einem Becken voller Meeresgurken; ringsum Laternenpfähle.</li>
 *     <li>Ringsum die <b>Muschelhäuser</b>: Kuppeln aus dem Holz der Landschaft mit einem Band aus Perlholz, Bullaugen, einem
 *     offenen Bogen zum Platz und einer leuchtenden Perle obenauf – innen je nach Beruf eingerichtet, mit einer Truhe.</li>
 *     <li>Der <b>Gesangsturm</b> der Gezeitensängerin: ein gewundenes Muschelhorn aus Perlmutt, so hoch, wie das Wasser es
 *     zulässt.</li>
 *     <li><b>Tanggärten</b> und <b>Korallenbeete</b> hinter Zäunen, zwei <b>Marktstände</b>, Wege aus Sandstein mit
 *     eingelassenen Meereslaternen.</li>
 *     <li>Die Bewohner: eine Meerjungfrau je Haus (die Berufe der Werkstätten und zufällige in den Wohnhäusern) und zwei
 *     Tritonwachen.</li>
 * </ol>
 */
public class SeaVillagePiece extends StructurePiece {
    /** Bis hierhin (Radius) wird eingeebnet und freigeräumt. */
    public static final int CORE = 30;
    /** Bis hierhin geht der Grund ins natürliche Gelände über. */
    public static final int REACH = 38;
    /** Radius des Dorfplatzes. */
    public static final int PLAZA = 9;
    public static final ResourceKey<LootTable> CHEST = ResourceKey.create(Registries.LOOT_TABLE, KingdomsAtWar.id("chests/sea_village"));

    private final BlockPos center;
    private final long seed;
    private final int sea;
    private final int woodIndex;

    public SeaVillagePiece(BlockPos center, long seed, int sea, int woodIndex) {
        super(ModStructures.SEA_VILLAGE_PIECE.get(), 0, makeBox(center, sea));
        this.center = center;
        this.seed = seed;
        this.sea = sea;
        this.woodIndex = woodIndex;
    }

    public SeaVillagePiece(CompoundTag tag) {
        super(ModStructures.SEA_VILLAGE_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 60), tag.getIntOr("CZ", 0));
        this.seed = tag.getLongOr("Seed", 0L);
        this.sea = tag.getIntOr("Sea", 96);
        this.woodIndex = tag.getIntOr("Wood", 0);
    }

    private static BoundingBox makeBox(BlockPos c, int sea) {
        return new BoundingBox(c.getX() - REACH - 1, c.getY() - 8, c.getZ() - REACH - 1, c.getX() + REACH + 1, Math.max(c.getY() + 4, sea),
                c.getZ() + REACH + 1);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", this.center.getX());
        tag.putInt("CY", this.center.getY());
        tag.putInt("CZ", this.center.getZ());
        tag.putLong("Seed", this.seed);
        tag.putInt("Sea", this.sea);
        tag.putInt("Wood", this.woodIndex);
    }

    public BlockPos center() {
        return this.center;
    }

    /** Wie viele Häuser (samt Turm) das Dorf hat – für die Tests. */
    public int houseCount() {
        return this.plan().houses().size();
    }

    /**
     * Für die Vorführung: je Haus die Mitte (Höhe des Bodens) und der Punkt vor dem Eingang; der Turm der Gezeitensängerin
     * zuerst.
     */
    public List<BlockPos[]> showcaseSpots() {
        List<BlockPos[]> spots = new ArrayList<>();
        int f = this.center.getY();
        for (House h : this.plan().houses()) {
            BlockPos[] spot = {new BlockPos(h.x(), f, h.z()),
                    new BlockPos(h.doorX() + h.door().getStepX() * 3, f + 2, h.doorZ() + h.door().getStepZ() * 3)};
            if (h.kind() == Kind.TOWER) {
                spots.add(0, spot);
            } else {
                spots.add(spot);
            }
        }
        return spots;
    }

    // =====================================================================================
    // Plan des Dorfs (aus dem Startwert – in jedem Chunk gleich)
    // =====================================================================================

    enum Kind {
        HOME,
        DIVER,
        GARDENER,
        BUILDER,
        TOWER;

        MermaidProfession profession(RandomSource random) {
            return switch (this) {
                case DIVER -> MermaidProfession.PEARL_DIVER;
                case GARDENER -> MermaidProfession.CORAL_GARDENER;
                case BUILDER -> MermaidProfession.SHELL_BUILDER;
                case TOWER -> MermaidProfession.TIDE_SINGER;
                case HOME -> MermaidProfession.randomTrader(random);
            };
        }
    }

    /** Ein Haus: Mitte, Radius, Höhe der Kuppel, Art, Richtung des Eingangs (zum Platz). */
    record House(int x, int z, int radius, int height, Kind kind, Direction door) {
        int doorX() {
            return this.x + this.door.getStepX() * this.radius;
        }

        int doorZ() {
            return this.z + this.door.getStepZ() * this.radius;
        }
    }

    /** Ein Garten (Rechteck um die Mitte), Tang oder Korallen. */
    record Garden(int x, int z, int halfW, int halfD, boolean kelp) {}

    /** Ein Marktstand vor dem Platz. */
    record Stall(int x, int z, Direction facing) {}

    record Plan(List<House> houses, List<Garden> gardens, List<Stall> stalls, List<double[]> lamps) {}

    private Plan plan() {
        RandomSource r = RandomSource.create(this.seed);
        int cx = this.center.getX();
        int cz = this.center.getZ();
        int count = 7 + r.nextInt(2);
        List<Kind> kinds = new ArrayList<>(List.of(Kind.TOWER, Kind.DIVER, Kind.GARDENER, Kind.BUILDER));
        while (kinds.size() < count) {
            kinds.add(Kind.HOME);
        }
        // mischen (fester Zufall)
        for (int i = kinds.size() - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            Kind k = kinds.get(i);
            kinds.set(i, kinds.get(j));
            kinds.set(j, k);
        }
        int depth = this.sea - this.center.getY();
        double a0 = r.nextDouble() * Math.PI * 2.0;
        List<House> houses = new ArrayList<>();
        List<double[]> taken = new ArrayList<>();      // x, z, Radius – für die Abstandsprüfung
        taken.add(new double[] {cx, cz, PLAZA + 1.5});
        // die Häuser: jedes in seinem Abschnitt des Rings, Abstand und Winkel zufällig (bis es frei ist)
        for (int i = 0; i < count; i++) {
            Kind kind = kinds.get(i);
            if (kind == Kind.TOWER && depth < 13) {
                kind = Kind.HOME;
            }
            int radius = kind == Kind.TOWER ? 4 : 3 + (r.nextInt(3) == 0 ? 1 : 0);
            int height = kind == Kind.TOWER ? Math.min(17, depth - 4) : radius + 3;
            for (int attempt = 0; attempt < 24; attempt++) {
                double a = a0 + (i + 0.5 + (r.nextDouble() - 0.5) * 0.7) * Math.PI * 2.0 / count;
                double dist = 15.0 + r.nextDouble() * (CORE - 1 - radius - 15.0);
                int hx = cx + (int) Math.round(Math.cos(a) * dist);
                int hz = cz + (int) Math.round(Math.sin(a) * dist);
                if (overlaps(taken, hx, hz, radius + 2.0)) {
                    continue;
                }
                Direction door = Direction.getApproximateNearest(cx - hx, 0, cz - hz);
                if (door.getAxis() == Direction.Axis.Y) {
                    door = Direction.NORTH;
                }
                taken.add(new double[] {hx, hz, radius + 2.0});
                houses.add(new House(hx, hz, radius, height, kind, door));
                break;
            }
        }
        // die Wege zu den Häusern bleiben frei
        for (House h : houses) {
            double a = Math.atan2(h.z() - cz, h.x() - cx);
            double len = Math.hypot(h.doorX() - cx, h.doorZ() - cz);
            for (double d = PLAZA + 1.0; d < len; d += 2.0) {
                taken.add(new double[] {cx + Math.cos(a) * d, cz + Math.sin(a) * d, 1.6});
            }
        }
        // Gärten und Stände in den Lücken
        List<Garden> gardens = new ArrayList<>();
        for (int g = 0; g < 3; g++) {
            for (int attempt = 0; attempt < 40; attempt++) {
                int halfW = 2 + r.nextInt(2);
                int halfD = 2 + r.nextInt(2);
                double reach = Math.sqrt(halfW * halfW + halfD * halfD) + 0.8;
                double a = r.nextDouble() * Math.PI * 2.0;
                double dist = 12.5 + r.nextDouble() * (CORE - 1 - reach - 12.5);
                int gx = cx + (int) Math.round(Math.cos(a) * dist);
                int gz = cz + (int) Math.round(Math.sin(a) * dist);
                if (!overlaps(taken, gx, gz, reach)) {
                    taken.add(new double[] {gx, gz, reach});
                    gardens.add(new Garden(gx, gz, halfW, halfD, r.nextBoolean()));
                    break;
                }
            }
        }
        List<Stall> stalls = new ArrayList<>();
        for (int n = 0; n < 2; n++) {
            for (int attempt = 0; attempt < 30; attempt++) {
                double a = r.nextDouble() * Math.PI * 2.0;
                double dist = 11.5 + r.nextDouble() * 1.5;
                int sx = cx + (int) Math.round(Math.cos(a) * dist);
                int sz = cz + (int) Math.round(Math.sin(a) * dist);
                if (!overlaps(taken, sx, sz, 2.2)) {
                    taken.add(new double[] {sx, sz, 2.2});
                    Direction facing = Direction.getApproximateNearest(cx - sx, 0, cz - sz);
                    stalls.add(new Stall(sx, sz, facing.getAxis() == Direction.Axis.Y ? Direction.NORTH : facing));
                    break;
                }
            }
        }
        // Laternenpfähle am Rand des Platzes, nicht vor den Wegen
        List<double[]> lamps = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            double a = a0 + Math.PI / 8.0 + i * Math.PI / 4.0;
            int lx = cx + (int) Math.round(Math.cos(a) * (PLAZA - 0.5));
            int lz = cz + (int) Math.round(Math.sin(a) * (PLAZA - 0.5));
            boolean onPath = false;
            for (House h : houses) {
                double ha = Math.atan2(h.z() - cz, h.x() - cx);
                if (Math.abs(Mth.wrapDegrees(Math.toDegrees(ha - a))) < 14.0) {
                    onPath = true;
                }
            }
            if (!onPath) {
                lamps.add(new double[] {lx, lz});
            }
        }
        return new Plan(houses, gardens, stalls, lamps);
    }

    private static boolean overlaps(List<double[]> taken, double x, double z, double radius) {
        for (double[] t : taken) {
            if (Math.hypot(t[0] - x, t[1] - z) < t[2] + radius) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================================
    // Bauen
    // =====================================================================================

    /** Setzt Blöcke nur im aktuellen Chunk; unter dem Meeresspiegel wird alles Wasserdurchlässige geflutet. */
    private final class Build {
        final WorldGenLevel level;
        final BoundingBox box;
        final LairContext ctx;
        final SeaWoods.SeaWood wood;
        final SeaWoods.SeaWood accent;
        final int floor;

        Build(WorldGenLevel level, BoundingBox box) {
            this.level = level;
            this.box = box;
            this.ctx = new LairContext(level, box, SeaVillagePiece.this.center, SeaVillagePiece.this.seed);
            this.wood = SeaWoods.ALL.get(Mth.clamp(SeaVillagePiece.this.woodIndex, 0, SeaWoods.ALL.size() - 1));
            this.accent = this.wood == SeaWoods.PEARL ? SeaWoods.CORAL : SeaWoods.PEARL;
            this.floor = SeaVillagePiece.this.center.getY();
        }

        boolean in(int x, int y, int z) {
            return this.box.isInside(x, y, z) && y > this.level.getMinY() && y < this.level.getMaxY();
        }

        void set(int x, int y, int z, BlockState state) {
            if (!this.in(x, y, z)) {
                return;
            }
            if (y < SeaVillagePiece.this.sea && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            this.level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
        }

        void set(int x, int y, int z, Block block) {
            this.set(x, y, z, block.defaultBlockState());
        }

        void water(int x, int y, int z) {
            if (this.in(x, y, z)) {
                this.level.setBlock(new BlockPos(x, y, z), y < SeaVillagePiece.this.sea ? Blocks.WATER.defaultBlockState()
                        : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }

        BlockState get(int x, int y, int z) {
            return this.ctx.get(x, y, z);
        }

        double rand(int a, int b, int salt) {
            return this.ctx.rand(a, b, salt);
        }

        BlockState log(Direction.Axis axis) {
            return this.wood.log.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }

        BlockState bark() {
            return this.wood.wood.get().defaultBlockState();
        }

        BlockState planks() {
            return this.wood.planks.base().get().defaultBlockState();
        }

        BlockState accentPlanks() {
            return this.accent.planks.base().get().defaultBlockState();
        }
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        Build b = new Build(level, chunkBB);
        Plan plan = this.plan();
        this.terrain(b);
        for (House house : plan.houses()) {
            this.path(b, house);
        }
        this.plaza(b, plan);
        for (House house : plan.houses()) {
            if (house.kind() == Kind.TOWER) {
                this.tower(b, house);
            } else {
                this.dome(b, house);
            }
        }
        for (Garden garden : plan.gardens()) {
            this.garden(b, garden);
        }
        for (Stall stall : plan.stalls()) {
            this.stall(b, stall);
        }
        this.kelpPatches(b);
        // Truhen und Bewohner
        RandomSource residents = RandomSource.create(this.seed ^ 0x5EAL);
        for (House house : plan.houses()) {
            this.chest(b, level, chunkBB, random, house);
            this.resident(b, house, house.kind().profession(residents));
        }
        int f = this.center.getY();
        int i = 0;
        for (Direction d : new Direction[] {Direction.NORTH, Direction.SOUTH}) {
            this.guard(b, this.center.getX() + d.getStepX() * (PLAZA + 2) + (i == 0 ? 2 : -2), f + 2,
                    this.center.getZ() + d.getStepZ() * (PLAZA + 2));
            i++;
        }
    }

    // ------------------------------------------------------------------------------------- Grund

    /** Oberster fester Block des natürlichen Grunds (Pflanzen, Tang, Stämme und Laub zählen nicht). */
    private int naturalTop(Build b, int x, int z) {
        int y = b.ctx.originalHeight(x, z, true) - 1;
        int min = b.level.getMinY() + 1;
        while (y > min && !LairContext.isGround(b.get(x, y, z))) {
            y--;
        }
        return y;
    }

    private void terrain(Build b) {
        int cx = this.center.getX();
        int cz = this.center.getZ();
        int f = this.center.getY();
        for (int x = b.box.minX(); x <= b.box.maxX(); x++) {
            for (int z = b.box.minZ(); z <= b.box.maxZ(); z++) {
                double r = Math.hypot(x - cx, z - cz);
                if (r > REACH) {
                    continue;
                }
                int natural = this.naturalTop(b, x, z);
                int target;
                if (r <= CORE) {
                    target = f;
                } else {
                    double t = LairContext.smooth((r - CORE) / (REACH - CORE));
                    target = (int) Math.round(f + (natural - f) * t);
                }
                if (natural < target) {
                    for (int y = natural + 1; y <= target; y++) {
                        b.set(x, y, z, y >= target - 1 ? Blocks.SAND.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState());
                    }
                } else if (natural > target) {
                    for (int y = target + 1; y <= natural; y++) {
                        b.water(x, y, z);
                    }
                }
                if (r <= CORE + 1.5) {
                    // freiräumen: Bäume, Tang, Seegras und Felsen über dem Dorfgrund
                    int top = Math.min(this.sea - 1, target + 26);
                    for (int y = target + 1; y <= top; y++) {
                        BlockState state = b.get(x, y, z);
                        if (!state.is(Blocks.WATER) && !state.isAir()) {
                            b.water(x, y, z);
                        }
                    }
                }
                if (r <= CORE) {
                    // Sand mit Kiesflecken und hellem Sandstein
                    double n = b.ctx.noise(x, z, 0.18, 51);
                    BlockState ground = n > 0.72 ? Blocks.GRAVEL.defaultBlockState() : n < 0.18 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState()
                            : Blocks.SAND.defaultBlockState();
                    b.set(x, target, z, ground);
                    if (b.get(x, target - 1, z).is(Blocks.WATER)) {
                        b.set(x, target - 1, z, Blocks.SANDSTONE);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------- Wege

    private void path(Build b, House house) {
        int cx = this.center.getX();
        int cz = this.center.getZ();
        int f = this.center.getY();
        double a = Math.atan2(house.z() - cz, house.x() - cx);
        double x0 = cx + Math.cos(a) * (PLAZA - 0.5);
        double z0 = cz + Math.sin(a) * (PLAZA - 0.5);
        double x1 = house.doorX() + house.door().getStepX() * 1.5;
        double z1 = house.doorZ() + house.door().getStepZ() * 1.5;
        double len = Math.hypot(x1 - x0, z1 - z0);
        int steps = (int) Math.ceil(len * 2.0);
        double px = -(z1 - z0) / Math.max(0.01, len);
        double pz = (x1 - x0) / Math.max(0.01, len);
        for (int s = 0; s <= steps; s++) {
            double t = s / (double) steps;
            double x = Mth.lerp(t, x0, x1);
            double z = Mth.lerp(t, z0, z1);
            for (double w = -1.0; w <= 1.0; w += 0.5) {
                int bx = Mth.floor(x + px * w);
                int bz = Mth.floor(z + pz * w);
                double v = b.rand(bx, bz, 52);
                BlockState state = v < 0.62 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : v < 0.86 ? Blocks.CUT_SANDSTONE.defaultBlockState()
                        : Blocks.SANDSTONE.defaultBlockState();
                b.set(bx, f, bz, state);
            }
            // alle paar Schritte eine Meereslaterne in der Mitte des Wegs
            if (s % 14 == 7) {
                b.set(Mth.floor(x), f, Mth.floor(z), Blocks.SEA_LANTERN);
            }
        }
    }

    // ------------------------------------------------------------------------------------- Platz und Dreizacksäule

    private void plaza(Build b, Plan plan) {
        int cx = this.center.getX();
        int cz = this.center.getZ();
        int f = this.center.getY();
        for (int dx = -PLAZA - 1; dx <= PLAZA + 1; dx++) {
            for (int dz = -PLAZA - 1; dz <= PLAZA + 1; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > PLAZA + 0.5) {
                    continue;
                }
                double ang = Math.atan2(dz, dx);
                double spoke = Math.abs(Math.sin(ang * 4.0));
                BlockState state;
                if (r > PLAZA - 0.5) {
                    state = SeaBlocks.TIDE_STONE_BRICKS.base().get().defaultBlockState();
                } else if (r > 5.4 && r < 6.5) {
                    state = Blocks.DARK_PRISMARINE.defaultBlockState();
                } else if (r > 3.4 && spoke < 0.16) {
                    state = ModBlocks.NACRE_BLOCK.get().defaultBlockState();
                } else if (r < 2.5) {
                    state = Blocks.DARK_PRISMARINE.defaultBlockState();
                } else {
                    state = b.rand(cx + dx, cz + dz, 53) < 0.7 ? Blocks.PRISMARINE_BRICKS.defaultBlockState() : Blocks.PRISMARINE.defaultBlockState();
                }
                b.set(cx + dx, f, cz + dz, state);
            }
        }
        // das Becken: ein flacher Rand aus Stufen, darin leuchtende Meeresgurken
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 3.5 && r < 4.5) {
                    b.set(cx + dx, f + 1, cz + dz, Blocks.PRISMARINE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                } else if (r >= 2.0 && r <= 3.5 && b.rand(cx + dx, cz + dz, 54) < 0.55) {
                    b.set(cx + dx, f + 1, cz + dz, Blocks.SEA_PICKLE.defaultBlockState()
                            .setValue(SeaPickleBlock.PICKLES, 1 + (int) (b.rand(cx + dx, cz + dz, 55) * 4)));
                }
            }
        }
        // die Dreizacksäule: Sockel, Säule, Querstück, drei Zinken mit leuchtenden Spitzen
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                b.set(cx + dx, f + 1, cz + dz, Blocks.DARK_PRISMARINE);
            }
        }
        b.set(cx, f + 2, cz, SeaBlocks.CHISELED_TIDE_STONE.get().defaultBlockState());
        for (Direction d : Direction.Plane.HORIZONTAL) {
            b.set(cx + d.getStepX(), f + 2, cz + d.getStepZ(), Blocks.PRISMARINE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, d.getOpposite()).setValue(StairBlock.HALF, Half.BOTTOM));
        }
        int top = Math.min(this.sea - 3, f + 13);
        int bar = top - 3;
        for (int y = f + 3; y < bar; y++) {
            b.set(cx, y, cz, Blocks.PRISMARINE_WALL);
        }
        b.set(cx, bar, cz, Blocks.PRISMARINE_BRICKS);
        b.set(cx - 1, bar, cz, Blocks.PRISMARINE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST)
                .setValue(StairBlock.HALF, Half.TOP));
        b.set(cx + 1, bar, cz, Blocks.PRISMARINE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST)
                .setValue(StairBlock.HALF, Half.TOP));
        b.set(cx - 2, bar, cz, Blocks.PRISMARINE_BRICKS);
        b.set(cx + 2, bar, cz, Blocks.PRISMARINE_BRICKS);
        for (int y = bar + 1; y <= top - 1; y++) {
            b.set(cx - 2, y, cz, Blocks.PRISMARINE_WALL);
            b.set(cx + 2, y, cz, Blocks.PRISMARINE_WALL);
        }
        b.set(cx - 2, top, cz, Blocks.SEA_LANTERN);
        b.set(cx + 2, top, cz, Blocks.SEA_LANTERN);
        for (int y = bar + 1; y <= top; y++) {
            b.set(cx, y, cz, Blocks.PRISMARINE_WALL);
        }
        b.set(cx, top + 1, cz, Blocks.SEA_LANTERN);
        // Laternenpfähle
        for (double[] lamp : plan.lamps()) {
            int lx = (int) lamp[0];
            int lz = (int) lamp[1];
            b.set(lx, f + 1, lz, b.log(Direction.Axis.Y));
            b.set(lx, f + 2, lz, b.log(Direction.Axis.Y));
            b.set(lx, f + 3, lz, b.wood.fence.get().defaultBlockState());
            b.set(lx, f + 4, lz, SeaBlocks.DEEP_SEA_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, false));
        }
    }

    // ------------------------------------------------------------------------------------- Muschelhäuser

    /** Radius der Kuppel in der Höhe {@code dy} über dem Boden: zwei Blöcke senkrecht, dann rund zur Spitze. */
    private static double domeRadius(House h, int dy) {
        double base = h.radius() + 0.5;
        if (dy <= 2) {
            return base;
        }
        double t = (dy - 2) / (double) (h.height() - 1);
        return t >= 1.0 ? 0.0 : base * Math.sqrt(1.0 - t * t);
    }

    private void dome(Build b, House h) {
        int f = this.center.getY();
        int r = h.radius();
        // Fußboden aus Brettern
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (Math.hypot(dx, dz) <= r + 0.3) {
                    b.set(h.x() + dx, f, h.z() + dz, b.planks());
                }
            }
        }
        boolean capped = false;
        for (int dy = 1; dy <= h.height() + 1; dy++) {
            double rad = domeRadius(h, dy);
            if (rad < 1.0) {
                if (!capped) {
                    // die leuchtende Perle obenauf
                    b.set(h.x(), f + dy, h.z(), Blocks.SEA_LANTERN);
                    capped = true;
                }
                continue;
            }
            double inner = rad - 1.05;
            for (int dx = -r - 1; dx <= r + 1; dx++) {
                for (int dz = -r - 1; dz <= r + 1; dz++) {
                    double d = Math.hypot(dx, dz);
                    int x = h.x() + dx;
                    int z = h.z() + dz;
                    if (d > rad) {
                        continue;
                    }
                    if (d < inner) {
                        b.water(x, f + dy, z);
                        continue;
                    }
                    BlockState state;
                    if (dy == 1) {
                        state = b.bark();
                    } else if (dy == 3) {
                        state = b.accentPlanks();
                    } else {
                        state = b.planks();
                    }
                    // wo die Kuppel nach innen springt, eine Treppe (zur Mitte ansteigend) – so wird sie rund statt gestuft
                    if (dy >= 3 && d > domeRadius(h, dy + 1) && !(dx == 0 && dz == 0)) {
                        Direction inward = Math.abs(dx) >= Math.abs(dz) ? (dx > 0 ? Direction.WEST : Direction.EAST)
                                : (dz > 0 ? Direction.NORTH : Direction.SOUTH);
                        var stairs = dy == 3 ? b.accent.planks.stairs().get() : b.wood.planks.stairs().get();
                        state = stairs.defaultBlockState().setValue(StairBlock.FACING, inward).setValue(StairBlock.HALF, Half.BOTTOM);
                    }
                    b.set(x, f + dy, z, state);
                }
            }
        }
        // Bullaugen in den Seiten (nicht an der Tür)
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (d == h.door()) {
                continue;
            }
            b.set(h.x() + d.getStepX() * r, f + 2, h.z() + d.getStepZ() * r, Blocks.GLASS.defaultBlockState());
        }
        // der Eingang: ein offener Bogen, darüber ein Sturz aus Rinde
        int ex = h.doorX();
        int ez = h.doorZ();
        b.water(ex, f + 1, ez);
        b.water(ex, f + 2, ez);
        b.set(ex, f + 3, ez, b.bark());
        this.furnish(b, h);
    }

    /** Einrichtung je nach Beruf: an der Rückwand, links und rechts davon. */
    private void furnish(Build b, House h) {
        int f = this.center.getY();
        Direction back = h.door().getOpposite();
        Direction side = h.door().getClockWise();
        int bx = h.x() + back.getStepX() * (h.radius() - 1);
        int bz = h.z() + back.getStepZ() * (h.radius() - 1);
        int lx = bx + side.getStepX() * 2 - back.getStepX();
        int lz = bz + side.getStepZ() * 2 - back.getStepZ();
        int rx = bx - side.getStepX() * 2 - back.getStepX();
        int rz = bz - side.getStepZ() * 2 - back.getStepZ();
        switch (h.kind()) {
            case DIVER -> {
                b.set(lx, f + 1, lz, Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
                b.set(rx, f + 1, rz, ModBlocks.NACRE_BLOCK.get().defaultBlockState());
                b.set(rx, f + 2, rz, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 3));
            }
            case GARDENER -> {
                b.set(lx, f + 1, lz, b.wood.planks.slab().get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                b.set(lx, f + 2, lz, SeaWoods.ALL.get((int) (b.rand(h.x(), h.z(), 56) * SeaWoods.ALL.size())).pottedSapling.get()
                        .defaultBlockState());
                b.set(rx, f + 1, rz, Blocks.BRAIN_CORAL_BLOCK);
                b.set(rx, f + 2, rz, Blocks.FIRE_CORAL_FAN);
            }
            case BUILDER -> {
                b.set(lx, f + 1, lz, Blocks.CRAFTING_TABLE);
                b.set(rx, f + 1, rz, Blocks.STONECUTTER);
                b.set(bx - side.getStepX(), f + 1, bz - side.getStepZ(), b.wood.log.get().defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, side.getAxis()));
            }
            default -> {
                b.set(lx, f + 1, lz, Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, side.getOpposite()));
                b.set(rx, f + 1, rz, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 2));
            }
        }
    }

    // ------------------------------------------------------------------------------------- Gesangsturm

    private void tower(Build b, House h) {
        int f = this.center.getY();
        int r = h.radius();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (Math.hypot(dx, dz) <= r + 0.3) {
                    b.set(h.x() + dx, f, h.z() + dz, ModBlocks.NACRE_BLOCK.get().defaultBlockState());
                }
            }
        }
        int height = h.height();
        for (int dy = 1; dy <= height; dy++) {
            double t = (dy - 1) / (double) height;
            double rad = (r + 0.5) * (1.0 - 0.62 * t);
            double inner = rad - 1.05;
            for (int dx = -r - 1; dx <= r + 1; dx++) {
                for (int dz = -r - 1; dz <= r + 1; dz++) {
                    double d = Math.hypot(dx, dz);
                    int x = h.x() + dx;
                    int z = h.z() + dz;
                    if (d > rad) {
                        continue;
                    }
                    if (d < inner) {
                        b.water(x, f + dy, z);
                        continue;
                    }
                    // eine Spirale aus Holz windet sich um den Turm aus Perlmutt
                    double ang = Math.atan2(dz, dx);
                    double band = Mth.positiveModulo(ang + dy * 0.55, Math.PI * 2.0);
                    BlockState state = band < 0.9 ? b.accent == SeaWoods.PEARL ? b.planks() : b.accentPlanks()
                            : ModBlocks.NACRE_BLOCK.get().defaultBlockState();
                    b.set(x, f + dy, z, state);
                }
            }
            // Fenster alle drei Ebenen, abwechselnd zu den Seiten
            if (dy % 3 == 2 && dy < height - 1) {
                Direction d = Direction.from2DDataValue(dy / 3);
                int wr = (int) Math.floor(rad - 0.5);
                if (d != h.door()) {
                    b.set(h.x() + d.getStepX() * wr, f + dy, h.z() + d.getStepZ() * wr, Blocks.GLASS.defaultBlockState());
                }
            }
        }
        // die Spitze: Laterne und ein Dorn
        b.set(h.x(), f + height + 1, h.z(), Blocks.SEA_LANTERN);
        if (f + height + 2 < this.sea - 1) {
            b.set(h.x(), f + height + 2, h.z(), Blocks.PRISMARINE_WALL);
        }
        // Eingang
        int ex = h.doorX();
        int ez = h.doorZ();
        b.water(ex, f + 1, ez);
        b.water(ex, f + 2, ez);
        b.set(ex, f + 3, ez, b.bark());
        // innen: ein Notenblock auf einem Sockel und Meeresgurken
        b.set(h.x(), f + 1, h.z(), Blocks.NOTE_BLOCK);
        b.set(h.x() + 1, f + 1, h.z(), Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 4));
    }

    // ------------------------------------------------------------------------------------- Gärten, Stände, Tang

    private void garden(Build b, Garden g) {
        int f = this.center.getY();
        for (int dx = -g.halfW(); dx <= g.halfW(); dx++) {
            for (int dz = -g.halfD(); dz <= g.halfD(); dz++) {
                int x = g.x() + dx;
                int z = g.z() + dz;
                boolean edgeX = Math.abs(dx) == g.halfW();
                boolean edgeZ = Math.abs(dz) == g.halfD();
                if (edgeX || edgeZ) {
                    if (dx == 0 && edgeZ) {
                        // ein offenes Tor in der Mitte der Schmalseite
                        b.set(x, f + 1, z, b.wood.fenceGate.get().defaultBlockState().setValue(FenceGateBlock.FACING, Direction.NORTH)
                                .setValue(FenceGateBlock.OPEN, true));
                        continue;
                    }
                    BlockState fence = b.wood.fence.get().defaultBlockState();
                    if (edgeZ && !edgeX) {
                        fence = fence.setValue(CrossCollisionBlock.EAST, true).setValue(CrossCollisionBlock.WEST, true);
                    } else if (edgeX && !edgeZ) {
                        fence = fence.setValue(CrossCollisionBlock.NORTH, true).setValue(CrossCollisionBlock.SOUTH, true);
                    } else {
                        fence = fence.setValue(dx < 0 ? CrossCollisionBlock.EAST : CrossCollisionBlock.WEST, true)
                                .setValue(dz < 0 ? CrossCollisionBlock.SOUTH : CrossCollisionBlock.NORTH, true);
                    }
                    b.set(x, f + 1, z, fence);
                    continue;
                }
                b.set(x, f, z, Blocks.SAND);
                if (g.kelp()) {
                    // Reihen aus Tang, dazwischen Seegras
                    if (dx % 2 == 0) {
                        int height = 2 + (int) (b.rand(x, z, 57) * 4);
                        for (int y = 1; y < height; y++) {
                            b.set(x, f + y, z, Blocks.KELP_PLANT);
                        }
                        b.set(x, f + height, z, Blocks.KELP.defaultBlockState().setValue(KelpBlock.AGE, 20 + (int) (b.rand(x, z, 58) * 4)));
                    } else if (b.rand(x, z, 59) < 0.6) {
                        b.set(x, f + 1, z, Blocks.SEAGRASS);
                    }
                } else {
                    // ein Beet aus Korallen
                    double v = b.rand(x, z, 60);
                    Block[] blocks = {Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL,
                            Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN};
                    if (v < 0.22) {
                        Block[] cores = {Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK,
                                Blocks.HORN_CORAL_BLOCK};
                        b.set(x, f + 1, z, cores[(int) (b.rand(x, z, 61) * cores.length)]);
                    } else if (v < 0.85) {
                        b.set(x, f + 1, z, blocks[(int) (b.rand(x, z, 62) * blocks.length)]);
                    } else {
                        b.set(x, f + 1, z, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 2));
                    }
                }
            }
        }
    }

    private void stall(Build b, Stall s) {
        int f = this.center.getY();
        Direction side = s.facing().getClockWise();
        for (int i = -1; i <= 1; i += 2) {
            for (int j = 0; j <= 1; j++) {
                int x = s.x() + side.getStepX() * i - s.facing().getStepX() * j;
                int z = s.z() + side.getStepZ() * i - s.facing().getStepZ() * j;
                for (int y = 1; y <= 3; y++) {
                    b.set(x, f + y, z, b.wood.fence.get().defaultBlockState());
                }
            }
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 2; j++) {
                int x = s.x() + side.getStepX() * i - s.facing().getStepX() * j;
                int z = s.z() + side.getStepZ() * i - s.facing().getStepZ() * j;
                b.set(x, f + 4, z, (i + j) % 2 == 0 ? b.wood.planks.slab().get().defaultBlockState()
                        : b.accent.planks.slab().get().defaultBlockState());
            }
        }
        // der Ladentisch: Fässer und eine Stufe, darauf Perlmutt
        b.set(s.x(), f + 1, s.z(), Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, s.facing()));
        b.set(s.x() + side.getStepX(), f + 1, s.z() + side.getStepZ(), b.wood.planks.slab().get().defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.TOP));
        b.set(s.x() - side.getStepX(), f + 1, s.z() - side.getStepZ(), ModBlocks.NACRE_BLOCK.get().defaultBlockState());
    }

    /** Hier und da Tang und Seegras zwischen den Häusern (nur wo der Grund frei ist). */
    private void kelpPatches(Build b) {
        int cx = this.center.getX();
        int cz = this.center.getZ();
        int f = this.center.getY();
        for (int x = b.box.minX(); x <= b.box.maxX(); x++) {
            for (int z = b.box.minZ(); z <= b.box.maxZ(); z++) {
                double r = Math.hypot(x - cx, z - cz);
                if (r <= PLAZA + 1.5 || r > CORE - 1) {
                    continue;
                }
                if (!b.get(x, f + 1, z).is(Blocks.WATER) || !b.get(x, f, z).is(Blocks.SAND) && !b.get(x, f, z).is(Blocks.GRAVEL)) {
                    continue;
                }
                double n = b.ctx.noise(x, z, 0.12, 63);
                double v = b.rand(x, z, 64);
                if (n > 0.66 && v < 0.35) {
                    int height = 3 + (int) (b.rand(x, z, 65) * 7);
                    for (int y = 1; y < height; y++) {
                        b.set(x, f + y, z, Blocks.KELP_PLANT);
                    }
                    b.set(x, f + height, z, Blocks.KELP.defaultBlockState().setValue(KelpBlock.AGE, 22));
                } else if (v < 0.08) {
                    b.set(x, f + 1, z, Blocks.SEAGRASS);
                } else if (v > 0.985) {
                    b.set(x, f + 1, z, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + (int) (n * 3)));
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------- Truhen und Bewohner

    private void chest(Build b, WorldGenLevel level, BoundingBox box, RandomSource random, House h) {
        int f = this.center.getY();
        Direction back = h.door().getOpposite();
        int x = h.x() + back.getStepX() * (h.radius() - 1);
        int z = h.z() + back.getStepZ() * (h.radius() - 1);
        BlockPos pos = new BlockPos(x, f + 1, z);
        if (h.kind() == Kind.TOWER) {
            pos = new BlockPos(h.x() - 1, f + 1, h.z());
        }
        if (box.isInside(pos)) {
            this.createChest(level, box, random, pos, CHEST, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, h.door())
                    .setValue(ChestBlock.WATERLOGGED, true));
        }
    }

    private void resident(Build b, House h, MermaidProfession profession) {
        int f = this.center.getY();
        int x = h.doorX() + h.door().getStepX() * 2;
        int z = h.doorZ() + h.door().getStepZ() * 2;
        BlockPos pos = new BlockPos(x, f + 2, z);
        if (!b.in(x, f + 2, z)) {
            return;
        }
        MermaidEntity mermaid = ModEntities.MERMAID.get().create(b.level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (mermaid == null) {
            return;
        }
        mermaid.setProfession(profession);
        mermaid.settle(this.center.above(2), CORE);
        this.place(b, mermaid, pos);
    }

    private void guard(Build b, int x, int y, int z) {
        if (!b.in(x, y, z)) {
            return;
        }
        TritonEntity triton = ModEntities.TRITON.get().create(b.level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (triton == null) {
            return;
        }
        triton.settle(this.center.above(2), CORE + 8);
        this.place(b, triton, new BlockPos(x, y, z));
    }

    private void place(Build b, Mob mob, BlockPos pos) {
        WorldGenLevel level = b.level;
        mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        mob.setPersistenceRequired();
        level.addFreshEntityWithPassengers(mob);
    }
}
