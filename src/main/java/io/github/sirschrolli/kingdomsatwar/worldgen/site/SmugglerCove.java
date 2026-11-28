package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Schmugglerhöhle (seit 2.0): Unter einer Steilküste öffnet sich ein Felsentor zum Meer. Ein Wasserarm führt tief in den
 * Fels, an seinem Westufer ein Bretterkai auf Pfählen mit Laternen, am Ostufer ein Felssims; im Wasser liegt das Boot der
 * Schmuggler. Hinten die trockene Lagerhöhle: Fässer voller Rum, Kisten, ein Kartentisch mit Kerzen, Strohlager und zwei
 * Truhen. Wer die Treppe dahinter hinaufsteigt, kommt durch eine Luke zwischen Büschen oben auf der Klippe heraus. Die
 * Schmuggler sind noch da – und sie mögen keinen Besuch.
 *
 * <p>Eigene Koordinaten: y = 0 ist der Meeresspiegel (der Wasserspiegel liegt bei y = -1); das Meer liegt vorn (+z), die
 * Höhle reicht nach hinten (−z) in die Klippe. {@code data[0]}: wie viele Stufen die Treppe hinauf zur Klippe hat.
 */
final class SmugglerCove {
    /** Der Wasserarm: x -5..2 bis z = -16; die Lagerhöhle dahinter (z -24..-17). */
    static final int BACK = -17;
    static final int END = -24;
    static final int MOUTH = 4;
    static final BlockPos CHEST = new BlockPos(-5, 0, -22);
    static final BlockPos HOARD = new BlockPos(5, 0, -23);

    private SmugglerCove() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.SMUGGLER_COVE, "schmugglerhoehle", "Smugglers' Cove", "Schmugglerhöhle", 3,
                Wonders.Ground.COAST, true, SmugglerCove::plan, SmugglerCove::build, origin -> piece(origin, Rotation.NONE, 0x5A6CL, 10),
                new double[][] {Wonders.view(0.5, 3, 16, 0, 2, -4), Wonders.view(-4, 1.2, 2, 0, 1, -16), Wonders.view(3, 2, -14, -2, 1, -22),
                        Wonders.view(-3, 1.6, -18, 4, 0.5, -23), Wonders.view(0.5, 2, END - 3, 0.5, 6, END - 10)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int sea = terrain.seaLevel();
        int start = Math.floorMod((int) (seed >>> 9), 4);
        for (int k = 0; k < 4; k++) {
            Rotation rotation = Rotation.values()[(start + k) % 4];
            BlockPos origin = new BlockPos(x, sea, z);
            if (!fits(terrain, origin, rotation, sea)) {
                continue;
            }
            // die Treppe hinauf: so lang, bis sie die Oberfläche der Klippe erreicht
            for (int t = 4; t <= 40; t++) {
                BlockPos q = origin.offset(new BlockPos(0, 0, END - 1 - t).rotate(rotation));
                if (terrain.water(q.getX(), q.getZ())) {
                    break;
                }
                if (terrain.ground(q.getX(), q.getZ()) + 1 - sea <= t + 1) {
                    if (!terrain.valid(x, sea, z)) {
                        return Optional.empty();
                    }
                    return Kit.stub(piece(origin, rotation, seed, t));
                }
            }
        }
        return Optional.empty();
    }

    /** Vorn tiefes Wasser, an der Mündung Strand oder Flachwasser, hinten eine Klippe, mindestens fünf Blöcke über dem Meer. */
    private static boolean fits(SiteTerrain terrain, BlockPos origin, Rotation rotation, int sea) {
        for (int dx : new int[] {-3, 0, 3}) {
            BlockPos q = origin.offset(new BlockPos(dx, 0, 13).rotate(rotation));
            if (terrain.waterDepth(q.getX(), q.getZ()) < 3) {
                return false;
            }
        }
        BlockPos mouth = origin.offset(new BlockPos(0, 0, MOUTH).rotate(rotation));
        if (terrain.ground(mouth.getX(), mouth.getZ()) > sea + 4) {
            return false;
        }
        for (int[] p : new int[][] {{0, -10}, {-5, -14}, {5, -14}, {0, -20}}) {
            BlockPos q = origin.offset(new BlockPos(p[0], 0, p[1]).rotate(rotation));
            if (terrain.water(q.getX(), q.getZ()) || terrain.ground(q.getX(), q.getZ()) < sea + 4) {
                return false;
            }
        }
        return true;
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed, int stairs) {
        return new SitePiece(SiteKind.SMUGGLER_COVE, 0, origin, rotation, seed, new int[] {stairs},
                SitePiece.box(origin, rotation, -10, -6, END - stairs - 4, 10, Math.max(12, stairs + 5), 16));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Cove(b, piece.data(0)).build();
    }

    private static final class Cove {
        private final VillageBuilder b;
        private final int stairs;
        private final Kit.Wood spruce = Kit.spruce();

        Cove(VillageBuilder b, int stairs) {
            this.b = b;
            this.stairs = stairs;
        }

        void build() {
            this.carve();
            this.shell();
            this.channel();
            this.jetty();
            this.store();
            this.decor();
            this.stairway();
            this.dwellers();
        }

        /** Halbe Breite der Höhle bei z (am Tor schmal, in der Mitte weit). */
        private double width(int z) {
            if (z > MOUTH) {
                return 3.2;
            }
            if (z >= BACK) {
                return 6.2 + Math.sin(z * 0.45) * 0.8 - (z > 0 ? 1.6 : 0);
            }
            return 6.0;
        }

        /** Gehört (x, y, z) zum Hohlraum (Luft über dem Wasser, Lagerhöhle, Treppengang)? */
        private boolean inside(int x, int y, int z) {
            if (z <= END - 1) {
                // der Treppengang: eine Stufe je Block nach hinten, drei Blöcke Kopfraum
                int t = END - 1 - z;
                return t <= this.stairs && x >= 0 && x <= 1 && y >= t && y <= t + 3;
            }
            if (z > MOUTH + 3 || y < 0) {
                return false;
            }
            double w = this.width(z);
            if (Math.abs(x) > w) {
                return false;
            }
            double k = Math.abs(x) / w;
            int h = z > MOUTH ? 4 : z < BACK ? 5 : (int) Math.round(2.5 + 4.5 * Math.sqrt(Math.max(0.0, 1.0 - k * k)));
            return y <= h;
        }

        /** Der Fels weicht: alles im Hohlraum wird Luft (außer dem Wasser unter dem Spiegel, das baut channel). */
        private void carve() {
            for (int x = -9; x <= 9; x++) {
                for (int z = END - this.stairs - 2; z <= MOUTH + 3; z++) {
                    for (int y = 0; y <= Math.max(8, this.stairs + 4); y++) {
                        if (this.inside(x, y, z) && !this.b.get(x, y, z).isAir()) {
                            this.b.set(x, y, z, Kit.s(Blocks.AIR));
                        }
                    }
                }
            }
        }

        /**
         * Die Wände: wo der Hohlraum an Luft, Erde, Sand oder Kies grenzt (eine dünne Klippe, ein Strand darüber), entsteht Fels –
         * so stürzt nichts nach, die Höhle bleibt geschlossen und ist ringsum felsig. Am Tor zum Meer bleibt das Wasser.
         */
        private void shell() {
            for (int x = -10; x <= 10; x++) {
                for (int z = END - this.stairs - 3; z <= MOUTH + 2; z++) {
                    for (int y = -1; y <= Math.max(9, this.stairs + 5); y++) {
                        if (this.inside(x, y, z)) {
                            continue;
                        }
                        boolean next = this.inside(x + 1, y, z) || this.inside(x - 1, y, z) || this.inside(x, y + 1, z) || this.inside(x, y - 1, z)
                                || this.inside(x, y, z + 1) || this.inside(x, y, z - 1);
                        if (!next) {
                            continue;
                        }
                        BlockState old = this.b.get(x, y, z);
                        boolean loose = old.getBlock() instanceof FallingBlock || old.is(BlockTags.LEAVES) || old.is(BlockTags.LOGS)
                                || old.is(BlockTags.DIRT) || old.is(BlockTags.SAND) || old.is(Blocks.CLAY) || old.is(BlockTags.TERRACOTTA);
                        boolean open = old.isAir() || !old.getFluidState().isEmpty() && y >= 0;
                        // die Treppe darf oben ins Freie münden; die Mündung zum Meer bleibt offen
                        boolean exit = z <= END - this.stairs + 1;
                        if ((loose || open) && !exit && z <= MOUTH) {
                            this.b.set(x, y, z, this.rock(x, y, z));
                        }
                    }
                }
            }
        }

        private BlockState rock(int x, int y, int z) {
            return DeepLair.pick(this.b, x, y, z, 3, Kit.s(Blocks.STONE), Kit.s(Blocks.STONE), Kit.s(Blocks.ANDESITE), Kit.s(Blocks.COBBLESTONE),
                    Kit.s(Blocks.MOSSY_COBBLESTONE), Kit.s(Blocks.TUFF));
        }

        /** Der Wasserarm (drei tief, Sand und Kies am Grund), das Felssims im Osten. */
        private void channel() {
            for (int z = BACK + 1; z <= MOUTH + 3; z++) {
                for (int x = -5; x <= 5; x++) {
                    if (Math.abs(x) > this.width(Math.min(z, MOUTH)) + 0.5) {
                        continue;
                    }
                    for (int y = -4; y <= -1; y++) {
                        BlockState state;
                        if (x >= 3) {
                            state = y == -1 ? DeepLair.pick(this.b, x, y, z, 5, Kit.s(Blocks.STONE), Kit.s(Blocks.ANDESITE), Kit.s(Blocks.COBBLESTONE))
                                    : this.rock(x, y, z);
                        } else if (y == -4) {
                            state = this.b.chance(x, y, z, 0.35f) ? Kit.s(Blocks.GRAVEL) : Kit.s(Blocks.SAND);
                        } else {
                            state = Kit.s(Blocks.WATER);
                        }
                        this.b.set(x, y, z, state);
                    }
                }
            }
            // die Kaimauer am Ende des Wasserarms
            for (int x = -6; x <= 6; x++) {
                for (int y = -4; y <= -1; y++) {
                    this.b.set(x, y, BACK, y == -1 ? Kit.s(Blocks.STONE_BRICKS) : this.rock(x, y, BACK));
                }
            }
            // vorn hinaus bis zum Meer: wo Strand vor der Mündung liegt, wird ein Durchstich gegraben
            for (int z = MOUTH + 4; z <= MOUTH + 10; z++) {
                for (int x = -2; x <= 2; x++) {
                    for (int y = -3; y <= -1; y++) {
                        this.b.set(x, y, z, Kit.s(Blocks.WATER));
                    }
                    for (int y = 0; y <= 4; y++) {
                        BlockState old = this.b.get(x, y, z);
                        if (old.getBlock() instanceof FallingBlock || old.is(BlockTags.DIRT) || old.is(BlockTags.REPLACEABLE)
                                || old.is(BlockTags.FLOWERS)) {
                            this.b.set(x, y, z, Kit.s(Blocks.AIR));
                        }
                    }
                }
            }
            // das Seegras im Wasser
            for (int z = BACK + 3; z <= MOUTH; z += 2) {
                if (this.b.chance(1, 0, z, 0.5f)) {
                    this.b.set(1, -3, z, Kit.s(Blocks.SEAGRASS));
                }
            }
        }

        /** Der Kai: Bretter auf Pfählen, Laternen an Pfosten, Taue (Ketten), Fässer und Fallen. */
        private void jetty() {
            for (int z = BACK + 1; z <= MOUTH + 1; z++) {
                for (int x = -5; x <= -3; x++) {
                    this.b.set(x, -1, z, x == -3 && z % 4 == 0 ? this.spruce.log(Direction.Axis.Y) : Kit.s(Blocks.SPRUCE_PLANKS));
                }
                if (z % 4 == 0) {
                    for (int y = -4; y <= -2; y++) {
                        this.b.set(-3, y, z, this.spruce.logY());
                    }
                }
                if (z % 6 == 0 && z < MOUTH) {
                    this.b.set(-3, 0, z, this.spruce.fence());
                    this.b.set(-3, 1, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                }
            }
            // ein paar Fässer und Seilrollen (Heuballen) am Kai
            Kit.barrel(this.b, -5, 0, -6, Direction.UP);
            Kit.barrel(this.b, -5, 0, -7, Direction.EAST);
            Kit.barrel(this.b, -5, 1, -6, Direction.UP);
            this.b.set(-5, 0, -2, Kit.s(Blocks.HAY_BLOCK));
            this.b.set(-5, 0, -12, Kit.s(Blocks.COBWEB));
            // das Boot im Wasser
            BlockPos at = this.b.world(0, -1, -7);
            if (this.b.inside(at)) {
                var boat = net.minecraft.world.entity.EntityTypes.OAK_BOAT.create(this.b.level.getLevel(), EntitySpawnReason.STRUCTURE);
                if (boat != null) {
                    boat.snapTo(at.getX() + 0.5, at.getY() + 0.55, at.getZ() + 0.5, this.b.worldDirection(Direction.SOUTH).toYRot(), 0.0f);
                    this.b.level.addFreshEntity(boat);
                }
            }
        }

        /** Die Lagerhöhle: Steinboden, Fässer voller Rum, Kisten, der Kartentisch, Strohlager, zwei Truhen, Laternen. */
        private void store() {
            for (int x = -6; x <= 6; x++) {
                for (int z = END; z <= BACK; z++) {
                    if (this.inside(x, 0, z) || this.inside(x, 1, z)) {
                        this.b.set(x, -1, z, this.b.chance(x, -1, z, 0.25f) ? Kit.s(Blocks.COBBLESTONE) : Kit.s(Blocks.STONE_BRICKS));
                    }
                }
            }
            // Fässer an den Wänden, zum Teil gestapelt
            for (int z = END + 1; z <= BACK - 1; z++) {
                for (int x : new int[] {-5, 5}) {
                    if (z == CHEST.getZ() && x == CHEST.getX() || z == HOARD.getZ() && x == HOARD.getX()) {
                        continue;
                    }
                    if (this.b.chance(x, 0, z, 0.75f)) {
                        Kit.barrel(this.b, x, 0, z, Direction.UP);
                        if (this.b.chance(x, 1, z, 0.45f)) {
                            Kit.barrel(this.b, x, 1, z, x < 0 ? Direction.EAST : Direction.WEST);
                        }
                    }
                }
            }
            Kit.chest(this.b, CHEST.getX(), CHEST.getY(), CHEST.getZ(), Direction.EAST, Kit.loot("smuggler_cove"));
            Kit.chest(this.b, HOARD.getX(), HOARD.getY(), HOARD.getZ(), Direction.WEST, Kit.loot("smuggler_cove_hoard"));
            // der Kartentisch mit Kerzen, zwei Hocker
            this.b.set(0, 0, -20, Kit.s(Blocks.CARTOGRAPHY_TABLE));
            this.b.set(1, 0, -20, this.spruce.slab(SlabType.TOP));
            Kit.candles(this.b, 1, 1, -20, Blocks.CANDLE, 3, true);
            Kit.chair(this.b, 0, 0, -19, this.spruce, Direction.NORTH);
            Kit.chair(this.b, 2, 0, -20, this.spruce, Direction.WEST);
            // Strohlager
            this.b.bed(-3, 0, -22, Blocks.STRAW_BED, Direction.NORTH);
            this.b.bed(-2, 0, -22, Blocks.STRAW_BED, Direction.NORTH);
            // Kisten mit Schmuggelgut (Kisten aus Brettern und Falltüren)
            this.b.set(3, 0, -19, Kit.s(Blocks.SPRUCE_PLANKS));
            Kit.lid(this.b, 3, 1, -19, Blocks.SPRUCE_TRAPDOOR);
            this.b.set(3, 0, -18, Kit.s(Blocks.DARK_OAK_PLANKS));
            Kit.lootBarrel(this.b, 4, 0, -18, Kit.loot("smuggler_cove"));
            // Laternen an der Decke
            this.b.hangingLantern(0, 4, -21, Blocks.LANTERN);
            this.b.hangingLantern(-1, 4, -12, Blocks.LANTERN);
            this.b.hangingLantern(1, 4, -3, Blocks.LANTERN);
        }

        /** An der Decke Tropfsteine, an den Wänden Leuchtflechten und Moos. */
        private void decor() {
            for (int x = -7; x <= 7; x++) {
                for (int z = END; z <= MOUTH; z++) {
                    for (int y = 1; y <= 7; y++) {
                        if (!this.inside(x, y, z) || !this.b.get(x, y, z).isAir()) {
                            continue;
                        }
                        boolean ceiling = !this.inside(x, y + 1, z) && this.b.get(x, y + 1, z).isSolid();
                        if (ceiling && Math.abs(x) >= 2 && this.b.chance(x, y + 60, z, 0.12f)) {
                            this.b.set(x, y, z, Blocks.POINTED_DRIPSTONE.defaultBlockState()
                                    .setValue(net.minecraft.world.level.block.PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN));
                            continue;
                        }
                        for (Direction d : Direction.Plane.HORIZONTAL) {
                            if (!this.inside(x + d.getStepX(), y, z + d.getStepZ()) && this.b.get(x + d.getStepX(), y, z + d.getStepZ()).isSolid()
                                    && this.b.chance(x, y + 90, z, 0.06f)) {
                                this.b.set(x, y, z, Blocks.GLOW_LICHEN.defaultBlockState()
                                        .setValue(net.minecraft.world.level.block.MultifaceBlock.getFaceProperty(d), true));
                                break;
                            }
                        }
                    }
                }
            }
        }

        /** Die Treppe nach hinten hinauf zur Klippe; oben eine Luke zwischen Büschen. */
        private void stairway() {
            for (int t = 0; t <= this.stairs; t++) {
                int z = END - 1 - t;
                for (int x = 0; x <= 1; x++) {
                    this.b.set(x, t - 1, z, Kit.s(Blocks.STONE_BRICKS));
                    if (t < this.stairs) {
                        this.b.set(x, t, z, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.NORTH, Half.BOTTOM));
                    }
                }
                if (t % 6 == 3) {
                    this.b.set(-1, t + 1, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
                    this.b.set(-1, t, z, this.rock(-1, t, z));
                }
            }
            // oben: der Ausgang zwischen Büschen
            int z = END - 1 - this.stairs;
            for (int[] p : new int[][] {{-1, -2}, {2, -2}, {-1, 0}, {2, 0}, {0, -3}, {1, -3}, {-1, -1}, {2, -1}}) {
                int y = this.stairs + 1;
                if (this.b.get(p[0], y, z + p[1]).isAir() && !this.b.get(p[0], y - 1, z + p[1]).isAir()) {
                    this.b.set(p[0], y, z + p[1], Kit.s(Blocks.BUSH));
                }
            }
        }

        /** Die Schmuggler: zwei mit Säbeln, einer mit Bogen. */
        private void dwellers() {
            Kit.mob(this.b, ModEntities.BANDIT.get(), -2, 0, -19, Direction.SOUTH);
            Kit.mob(this.b, ModEntities.BANDIT.get(), 2, 0, -22, Direction.SOUTH);
            Kit.mob(this.b, ModEntities.BANDIT_ARCHER.get(), -4, 0, -4, Direction.SOUTH);
        }
    }
}
