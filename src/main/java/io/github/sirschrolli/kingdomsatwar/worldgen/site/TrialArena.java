package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Die Arena der Prüfungen: Ein vergessenes Volk hat tief unter der Erde, in einer riesigen Höhle, ein Kolosseum aus
 * Sandstein gebaut. Außen zwei Reihen Bögen und ein Kranz von Masten, innen steigen acht Ränge über einer Sandarena an.
 * In der Brüstung öffnen sich sechs Käfige – in jedem ein Prüfungsspawner, der seine Kämpfer in die Arena schickt –, in
 * der Mitte steht auf einem Podest der Spawner der Böen. Wer siegt, bekommt Prüfungsschlüssel; mit ihnen öffnet man die
 * Tresore auf der Ehrentribüne unter dem rot-weißen Sonnensegel (darunter ein unheilvoller Tresor). Durch das Haupttor
 * im Süden geht es hinaus, links und rechts davon liegen die Kammern der Kämpfer mit ihren Vorräten.
 *
 * <p>Eigene Koordinaten: Mitte der Arena bei (0, 0, 0); y = 0 ist die Schicht des Sandes (man steht auf y = 1); das
 * Haupttor liegt im Süden (+z), die Ehrentribüne im Norden (−z).
 */
final class TrialArena {
    static final int CAVE_R = 36;
    static final int CAVE_H = 24;
    static final int CAVE_Y = 4;
    /** Winkel der Käfige (Grad, gemessen von +x nach +z). */
    static final int[] CAGES = {30, 60, 120, 150, 210, 330};
    /** Welche Art Kämpfer in welchem Käfig: Nahkampf, kleine Nahkämpfer, Fernkampf, langsamer Fernkampf. */
    private static final int[] CAGE_GROUP = {0, 1, 2, 0, 3, 1};
    private static final String[][] GROUPS = {
            {"melee/zombie", "melee/husk", "melee/spider"},
            {"small_melee/baby_zombie", "small_melee/cave_spider", "small_melee/silverfish", "small_melee/slime"},
            {"ranged/skeleton", "ranged/stray", "ranged/poison_skeleton"},
            {"slow_ranged/skeleton", "slow_ranged/stray", "slow_ranged/poison_skeleton"}};

    private TrialArena() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int floor = -40 + Math.floorMod((int) (seed >>> 13), 21);
        if (floor - 6 < terrain.minY()) {
            return Optional.empty();
        }
        int[] span = terrain.span(x, z, 32, 16);
        if (span[0] < floor + CAVE_Y + CAVE_H + 8) {
            return Optional.empty();
        }
        // das Reich des Sculk gehört der Stillen Kathedrale
        if (terrain.biome(x, floor + 6, z).is(Biomes.DEEP_DARK)) {
            return Optional.empty();
        }
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        BlockPos origin = new BlockPos(x, floor, z);
        BoundingBox box = box(origin);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.TRIAL_ARENA, 0, origin, rotation, seed, new int[0], box))));
    }

    static BoundingBox box(BlockPos o) {
        int r = CAVE_R + 3;
        return new BoundingBox(o.getX() - r, o.getY() - 5, o.getZ() - r, o.getX() + r, o.getY() + CAVE_Y + CAVE_H + 5, o.getZ() + r);
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Arena(b, piece.seed()).build();
    }

    /** Die Konfiguration des Spawners im Käfig {@code cage} (ohne Namensraum und ohne "/normal"). */
    static String cageConfig(int cage, long seed) {
        String[] group = GROUPS[CAGE_GROUP[cage]];
        return "trial_chamber/" + group[Math.floorMod((int) (seed >>> (cage * 3 + 2)), group.length)];
    }

    private static final class Arena {
        private final VillageBuilder b;
        private final long seed;

        Arena(VillageBuilder b, long seed) {
            this.b = b;
            this.seed = seed;
        }

        void build() {
            SiteCave.dome(this.b, CAVE_R, CAVE_H, CAVE_R, CAVE_Y, 0, 0.08, this::ground);
            this.bowl();
            this.cages();
            this.gate();
            this.vomitoria();
            this.tribune();
            this.dais();
            this.lights();
            this.debris();
        }

        // =================================================================================
        // Hilfen
        // =================================================================================

        static double r(int x, int z) {
            return Math.sqrt(x * x + z * z);
        }

        static BlockState state(Block block) {
            return block.defaultBlockState();
        }

        /** Ein Punkt in Polarkoordinaten (Winkel in Grad, Abstand von der Mitte, seitlicher Versatz). */
        static int[] polar(double degrees, double along, double lateral) {
            double a = Math.toRadians(degrees);
            double x = Math.cos(a) * along - Math.sin(a) * lateral;
            double z = Math.sin(a) * along + Math.cos(a) * lateral;
            return new int[] {(int) Math.round(x), (int) Math.round(z)};
        }

        BlockState ground(int x, int z) {
            double s = SiteCave.smooth(this.b, x, 0, z, 2, 7);
            float n = this.b.noise(x, 0, z);
            if (s < 0.4) {
                return n < 0.3f ? state(Blocks.SANDSTONE) : state(Blocks.SAND);
            }
            return n < 0.25f ? state(Blocks.TUFF) : n < 0.35f ? state(Blocks.GRAVEL) : n < 0.6f ? state(Blocks.ANDESITE) : state(Blocks.STONE);
        }

        BlockState sandstone(int x, int y, int z) {
            float n = this.b.noise(x, y, z);
            return n < 0.75f ? state(Blocks.SANDSTONE) : n < 0.9f ? state(Blocks.SMOOTH_SANDSTONE) : state(Blocks.CUT_SANDSTONE);
        }

        void bulb(int x, int y, int z) {
            this.b.set(x, y, z, Blocks.COPPER_BULB.waxed().unaffected().defaultBlockState().setValue(CopperBulbBlock.LIT, true));
        }

        void lantern(int x, int y, int z) {
            this.b.set(x, y, z, Blocks.COPPER_LANTERN.waxed().unaffected().defaultBlockState().setValue(LanternBlock.HANGING, true));
        }

        // =================================================================================
        // Das Rund
        // =================================================================================

        /**
         * Arena, Brüstung, Ränge, Umgang und Außenmauer in einem Zug, Säule für Säule: Sand mit roten Ringen, die Brüstung
         * aus rotem Sandstein, acht Ränge, darunter ein Umgang; außen zwei Bogenreihen und eine Attika mit Masten.
         */
        void bowl() {
            for (int x = -25; x <= 25; x++) {
                for (int z = -25; z <= 25; z++) {
                    if (!SiteCave.column(this.b, x, z)) {
                        continue;
                    }
                    double r = r(x, z);
                    if (r >= 24.4) {
                        continue;
                    }
                    double angle = Math.toDegrees(Math.atan2(z, x));
                    this.b.set(x, -1, z, state(Blocks.SANDSTONE));
                    if (r < 12.5) {
                        boolean ring = r >= 4.5 && r < 5.3 || r >= 9.0 && r < 9.8;
                        this.b.set(x, 0, z, ring ? state(Blocks.RED_SAND) : state(Blocks.SAND));
                    } else if (r < 13.5) {
                        this.b.set(x, 0, z, state(Blocks.CUT_RED_SANDSTONE));
                        for (int y = 1; y <= 4; y++) {
                            this.b.set(x, y, z, y == 4 ? state(Blocks.SMOOTH_RED_SANDSTONE) : state(Blocks.CUT_RED_SANDSTONE));
                        }
                        this.b.set(x, 5, z, state(Blocks.RED_SANDSTONE_WALL));
                    } else if (r < 21.5) {
                        int t = (int) Math.floor(r - 13.5);
                        int seat = 5 + t;
                        boolean walk = r >= 19.5;
                        this.b.set(x, 0, z, walk ? state(Blocks.SMOOTH_SANDSTONE) : state(Blocks.SANDSTONE));
                        for (int y = 1; y < seat; y++) {
                            this.b.set(x, y, z, walk && y <= 4 ? Blocks.AIR.defaultBlockState() : this.sandstone(x, y, z));
                        }
                        // die Sitzstufen: jede vierte Reihe ein Umgang, die Treppen (alle 30°) aus Stufen
                        boolean aisle = Math.abs(Math.IEEEremainder(angle - 15, 30)) * Math.PI / 180 * r < 0.9;
                        this.b.set(x, seat, z, t == 3 ? state(Blocks.CUT_SANDSTONE)
                                : aisle ? state(Blocks.CHISELED_SANDSTONE) : state(Blocks.SMOOTH_SANDSTONE));
                    } else {
                        this.outerWall(x, z, r, angle);
                    }
                }
            }
        }

        /** Die Außenmauer: zwei Lagen; unten offene Bögen zum Umgang, darüber Blendbögen, oben eine Attika, Gesimse, Masten. */
        void outerWall(int x, int z, double r, double angle) {
            int bays = 28;
            double bay = 360.0 / bays;
            double off = Math.abs(Math.IEEEremainder(angle, bay)) * Math.PI / 180 * 23.0;
            boolean outer = r >= 22.5;
            this.b.set(x, 0, z, state(Blocks.SMOOTH_SANDSTONE));
            if (r >= 23.5) {
                // die vorspringenden Gesimse
                this.b.set(x, 6, z, VillageBuilder.slab(Blocks.CUT_SANDSTONE_SLAB, SlabType.TOP));
                this.b.set(x, 11, z, VillageBuilder.slab(Blocks.CUT_SANDSTONE_SLAB, SlabType.TOP));
                this.b.set(x, 16, z, VillageBuilder.slab(Blocks.CUT_SANDSTONE_SLAB, SlabType.BOTTOM));
                return;
            }
            for (int y = 1; y <= 15; y++) {
                BlockState s;
                boolean arch1 = y <= 4 && off <= 1.5 || y == 5 && off <= 0.6;
                boolean arch2 = y >= 7 && y <= 9 && off <= 1.5 || y == 10 && off <= 0.6;
                boolean window = y == 13 && off <= 0.5;
                if (arch1) {
                    s = Blocks.AIR.defaultBlockState();
                } else if (arch2 && outer) {
                    s = state(Blocks.SMOOTH_RED_SANDSTONE);
                } else if (window && outer) {
                    s = state(Blocks.CUT_RED_SANDSTONE);
                } else if (off >= 2.0 && outer && y != 6 && y != 11) {
                    s = state(Blocks.CUT_SANDSTONE);
                } else if ((y == 5 && off <= 1.6 || y == 10 && off <= 1.6) && outer) {
                    s = state(Blocks.CHISELED_SANDSTONE);
                } else {
                    s = this.sandstone(x, y, z);
                }
                this.b.set(x, y, z, s);
            }
            this.b.set(x, 16, z, VillageBuilder.slab(Blocks.CUT_SANDSTONE_SLAB, SlabType.BOTTOM));
            // Masten für das Sonnensegel, über jedem zweiten Pfeiler
            int index = (int) Math.round(angle / bay);
            if (outer && off < 0.5 && Math.floorMod(index, 2) == 0 && r < 23.2) {
                for (int y = 16; y <= 18; y++) {
                    this.b.set(x, y, z, state(Blocks.SPRUCE_FENCE));
                }
            }
        }

        // =================================================================================
        // Käfige, Tor, Treppen, Tribüne
        // =================================================================================

        /** Sechs Käfige in der Brüstung, in jedem ein Prüfungsspawner; das Fallgitter ist hochgezogen. */
        void cages() {
            for (int c = 0; c < CAGES.length; c++) {
                int deg = CAGES[c];
                double a = Math.toRadians(deg);
                double cos = Math.cos(a);
                double sin = Math.sin(a);
                int[] mid = polar(deg, 15, 0);
                for (int x = mid[0] - 5; x <= mid[0] + 5; x++) {
                    for (int z = mid[1] - 5; z <= mid[1] + 5; z++) {
                        double along = x * cos + z * sin;
                        double l = Math.abs(-x * sin + z * cos);
                        if (along < 12.5 || along > 17.9 || l > 3.0) {
                            continue;
                        }
                        if (along < 13.5) {
                            // die Öffnung in der Brüstung, oben das hochgezogene Gitter
                            if (l <= 1.3) {
                                for (int y = 1; y <= 3; y++) {
                                    this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                                }
                                this.b.set(x, 4, z, state(Blocks.IRON_BARS));
                            }
                        } else if (l <= 2.2 && along <= 17.0) {
                            this.b.set(x, 0, z, this.b.chance(x, 0, z, 0.5f) ? state(Blocks.COARSE_DIRT) : state(Blocks.GRAVEL));
                            for (int y = 1; y <= 4; y++) {
                                this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                            }
                        } else {
                            for (int y = 1; y <= 4; y++) {
                                this.b.set(x, y, z, state(Blocks.CUT_RED_SANDSTONE));
                            }
                        }
                    }
                }
                int[] spawner = polar(deg, 15.5, 0);
                this.spawner(spawner[0], 1, spawner[1], cageConfig(c, this.seed));
                int[] light = polar(deg, 16.2, 1.4);
                this.lantern(light[0], 4, light[1]);
                int[] bones = polar(deg, 16.2, -1.4);
                this.b.set(bones[0], 1, bones[1], Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                this.b.set(bones[0], 4, bones[1], state(Blocks.COBWEB));
            }
        }

        void spawner(int x, int y, int z, String config) {
            this.b.set(x, y, z, Blocks.TRIAL_SPAWNER.defaultBlockState());
            BlockPos pos = this.b.world(x, y, z);
            if (this.b.inside(pos) && this.b.level.getBlockEntity(pos) instanceof TrialSpawnerBlockEntity spawner) {
                CompoundTag tag = new CompoundTag();
                tag.putString("normal_config", "minecraft:" + config + "/normal");
                tag.putString("ominous_config", "minecraft:" + config + "/ominous");
                spawner.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, this.b.level.registryAccess(), tag));
            }
        }

        /** Das Haupttor im Süden: ein gewölbter Gang durch die Ränge hinaus in die Höhle; daneben die Kammern der Kämpfer. */
        void gate() {
            for (int z = 13; z <= 23; z++) {
                for (int x = -1; x <= 1; x++) {
                    this.b.set(x, 0, z, x == 0 ? state(Blocks.SMOOTH_SANDSTONE) : state(Blocks.CUT_SANDSTONE));
                    for (int y = 1; y <= 4; y++) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                    this.b.set(x, 5, z, x == 0 ? Blocks.AIR.defaultBlockState()
                            : VillageBuilder.stairs(Blocks.SMOOTH_SANDSTONE_STAIRS, x > 0 ? Direction.EAST : Direction.WEST, Half.TOP));
                }
                this.b.set(0, 6, z, state(Blocks.CUT_SANDSTONE));
                for (int x : new int[] {-2, 2}) {
                    for (int y = 1; y <= 5; y++) {
                        if (z >= 14 && z <= 21 && !(y <= 2 && z == 16)) {
                            this.b.set(x, y, z, Math.floorMod(z, 3) == 0 && y <= 4 ? state(Blocks.CUT_SANDSTONE) : this.sandstone(x, y, z));
                        }
                    }
                }
            }
            // das hochgezogene Fallgitter an der Brüstung
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, 4, 13, state(Blocks.IRON_BARS));
            }
            this.b.set(0, 5, 13, state(Blocks.IRON_BARS));
            this.lantern(0, 5, 17);
            this.lantern(0, 5, 21);
            // die Kammern der Kämpfer links und rechts des Gangs
            for (int side = -1; side <= 1; side += 2) {
                for (int x = 3; x <= 6; x++) {
                    for (int z = 14; z <= 18; z++) {
                        boolean wall = x == 6 || z == 14 || z == 18;
                        this.b.set(side * x, 0, z, state(Blocks.SMOOTH_SANDSTONE));
                        for (int y = 1; y <= 4; y++) {
                            this.b.set(side * x, y, z, wall ? state(Blocks.CUT_SANDSTONE) : Blocks.AIR.defaultBlockState());
                        }
                        this.b.set(side * x, 5, z, state(Blocks.CUT_SANDSTONE));
                    }
                }
                this.b.door(side * 2, 1, 16, Blocks.COPPER_DOOR.waxed().exposed(), side > 0 ? Direction.EAST : Direction.WEST);
                this.b.chest(side * 5, 1, 17, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), ModLootTables.SITE_TRIAL_ARENA);
                this.b.set(side * 4, 1, 17, Blocks.BARREL);
                this.b.set(side * 5, 1, 15, state(Blocks.HAY_BLOCK));
                this.b.set(side * 3, 1, 15, state(Blocks.GRINDSTONE));
                this.b.chest(side * 3, 1, 17, state(Blocks.DECORATED_POT), BuiltInLootTables.TRIAL_CHAMBERS_CORRIDOR_POT);
                this.lantern(side * 4, 4, 16);
            }
        }

        /** Im Osten und Westen führen Treppen vom Umgang unter den Rängen hinauf auf die unterste Sitzreihe. */
        void vomitoria() {
            for (int side = -1; side <= 1; side += 2) {
                for (int lx = 15; lx <= 19; lx++) {
                    int step = 20 - lx;
                    for (int z = -1; z <= 1; z++) {
                        int x = side * lx;
                        this.b.set(x, step, z, VillageBuilder.stairs(Blocks.SANDSTONE_STAIRS, side > 0 ? Direction.WEST : Direction.EAST, Half.BOTTOM));
                        for (int y = step + 1; y <= step + 3; y++) {
                            this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
                this.lantern(side * 19, 4, 0);
            }
        }

        /**
         * Die Ehrentribüne im Norden: eine Plattform in den Rängen, vorn eine Brüstung, darüber ein rot-weißes Sonnensegel
         * auf vier Pfeilern; drei Tresore (der mittlere unheilvoll), dahinter Banner.
         */
        void tribune() {
            for (int x = -5; x <= 5; x++) {
                for (int z = -20; z <= -13; z++) {
                    double r = r(x, z);
                    if (r < 13.5 || r >= 19.5 || Math.abs(x) > 4) {
                        continue;
                    }
                    for (int y = 1; y <= 6; y++) {
                        this.b.set(x, y, z, this.sandstone(x, y, z));
                    }
                    this.b.set(x, 7, z, Math.abs(x) <= 1 ? state(Blocks.CHISELED_RED_SANDSTONE) : state(Blocks.SMOOTH_RED_SANDSTONE));
                    for (int y = 8; y <= 12; y++) {
                        this.b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                    if (r < 14.5) {
                        this.b.set(x, 8, z, state(Blocks.RED_SANDSTONE_WALL));
                    }
                }
            }
            for (int x : new int[] {-4, 4}) {
                for (int z : new int[] {-14, -18}) {
                    for (int y = 8; y <= 12; y++) {
                        this.b.set(x, y, z, state(Blocks.CUT_RED_SANDSTONE));
                    }
                }
            }
            for (int x = -5; x <= 5; x++) {
                for (int z = -19; z <= -13; z++) {
                    boolean edge = Math.abs(x) == 5 || z == -19 || z == -13;
                    this.b.set(x, 13, z, edge ? VillageBuilder.slab(Blocks.CUT_RED_SANDSTONE_SLAB, SlabType.BOTTOM)
                            : Blocks.WOOL.pick(Math.floorMod(x, 2) == 0 ? DyeColor.RED : DyeColor.WHITE).defaultBlockState());
                }
            }
            this.vault(-2, 8, -17, false);
            this.vault(0, 8, -17, true);
            this.vault(2, 8, -17, false);
            this.b.chest(-3, 8, -18, state(Blocks.DECORATED_POT), BuiltInLootTables.TRIAL_CHAMBERS_CORRIDOR_POT);
            this.b.chest(3, 8, -18, state(Blocks.DECORATED_POT), BuiltInLootTables.TRIAL_CHAMBERS_CORRIDOR_POT);
            for (int x = -3; x <= 3; x += 2) {
                this.b.set(x, 10, -19, Blocks.WALL_BANNER.pick(Math.floorMod(x, 4) == 1 ? DyeColor.RED : DyeColor.YELLOW).defaultBlockState()
                        .setValue(WallBannerBlock.FACING, Direction.SOUTH));
            }
        }

        void vault(int x, int y, int z, boolean ominous) {
            this.b.set(x, y, z, Blocks.VAULT.defaultBlockState().setValue(VaultBlock.FACING, Direction.SOUTH).setValue(VaultBlock.OMINOUS, ominous));
            BlockPos pos = this.b.world(x, y, z);
            if (ominous && this.b.inside(pos) && this.b.level.getBlockEntity(pos) instanceof VaultBlockEntity vault) {
                vault.setConfig(new VaultConfig(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS, 4.0, 4.5, new ItemStack(Items.OMINOUS_TRIAL_KEY),
                        Optional.empty()));
            }
        }

        /** Das Podest in der Mitte: rund, mit Stufen, Kupferstäben und dem Spawner der Böen obenauf. */
        void dais() {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    double r = r(x, z);
                    if (r <= 2.5) {
                        this.b.set(x, 1, z, state(Blocks.CUT_SANDSTONE));
                        if (r <= 1.5) {
                            this.b.set(x, 2, z, x == 0 && z == 0 ? state(Blocks.CHISELED_SANDSTONE) : state(Blocks.SMOOTH_SANDSTONE));
                        }
                    }
                }
            }
            for (Direction d : Direction.Plane.HORIZONTAL) {
                this.b.set(d.getStepX() * 3, 1, d.getStepZ() * 3, VillageBuilder.stairs(Blocks.SANDSTONE_STAIRS, d.getOpposite(), Half.BOTTOM));
                this.b.set(d.getStepX() * 2, 2, d.getStepZ() * 2, Blocks.LIGHTNING_ROD.waxed().exposed().defaultBlockState());
            }
            this.spawner(0, 3, 0, "trial_chamber/breeze");
        }

        /** Licht: Kupferlampen in der Brüstung, Laternen im Umgang, Leuchter an langen Ketten von der Höhlendecke. */
        void lights() {
            for (int deg = 15; deg < 360; deg += 30) {
                int[] p = polar(deg, 13.0, 0);
                this.bulb(p[0], 3, p[1]);
            }
            for (int deg = 0; deg < 360; deg += 30) {
                int[] p = polar(deg + 15, 20.5, 0);
                this.lantern(p[0], 4, p[1]);
            }
            for (int deg = 45; deg < 360; deg += 90) {
                int[] p = polar(deg, 8, 0);
                if (!SiteCave.column(this.b, p[0], p[1])) {
                    continue;
                }
                int top = SiteCave.ceiling(this.b, p[0], p[1], 14, CAVE_Y + CAVE_H + 4);
                for (int y = 13; y < top; y++) {
                    this.b.set(p[0], y, p[1], Blocks.COPPER_CHAIN.waxed().weathered().defaultBlockState());
                }
                this.lantern(p[0], 12, p[1]);
            }
            // Banner an der Brüstung, links und rechts der Treppen
            for (int side = -1; side <= 1; side += 2) {
                for (int z : new int[] {-2, 2}) {
                    this.b.set(side * 12, 3, z, Blocks.WALL_BANNER.pick(z < 0 ? DyeColor.RED : DyeColor.YELLOW).defaultBlockState()
                            .setValue(WallBannerBlock.FACING, side > 0 ? Direction.WEST : Direction.EAST));
                }
            }
        }

        /** Spuren alter Kämpfe im Sand, umgestürzte Säulentrommeln in der Höhle. */
        void debris() {
            for (int i = 0; i < 7; i++) {
                double a = this.b.noise(i, 60, 0) * 360;
                double d = 4 + this.b.noise(i, 61, 0) * 7;
                int[] p = polar(a, d, 0);
                if (r(p[0], p[1]) < 3.5) {
                    continue;
                }
                this.b.set(p[0], 1, p[1], i % 3 == 0 ? Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, i * 5 % 16)
                        : Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, i % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z));
            }
            for (int i = 0; i < 4; i++) {
                double a = 30 + i * 90 + this.b.noise(i, 62, 0) * 40;
                int[] p = polar(a, 26.5, 0);
                Direction.Axis axis = i % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z;
                for (int k = 0; k < 3; k++) {
                    int x = p[0] + (axis == Direction.Axis.X ? k : 0);
                    int z = p[1] + (axis == Direction.Axis.Z ? k : 0);
                    this.b.set(x, 1, z, k == 1 ? state(Blocks.CHISELED_SANDSTONE) : state(Blocks.CUT_SANDSTONE));
                }
            }
        }
    }
}
