package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Brutgrotte (seit 1.15): eine weite, stockdunkle Höhle tief im Fels, ausgesponnen mit Seide. Von der Decke hängen
 * bleiche Fäden, überall kleben Netze, an den Wänden und unter der Decke hängen die weißen Eiersäcke der Brut. Zwischen
 * ihnen stehen die eingesponnenen Opfer – in einem steckt noch ein Schädel –, und neben ihnen liegt, was sie bei sich
 * trugen. In zwei Nestern am Rand schlüpfen ständig neue Höhlenspinnen (Spawner). In der Mitte, auf einem Hügel aus Seide,
 * ruht die Brutmutter. Zwei gewundene Gänge führen hinaus.
 *
 * <p>Eigene Koordinaten: Mitte der Höhle bei (0, 0, 0); y = 0 ist die Schicht des Bodens (man steht auf y = 1).
 */
final class BroodGrotto {
    static final int RX = 17;
    static final int RY = 9;
    static final int RZ = 15;
    static final int CY = 2;
    /** Die Eiersäcke: Winkel (Grad) und Abstand von der Mitte. */
    private static final int[][] SACS = {{10, 13}, {48, 11}, {85, 12}, {130, 13}, {165, 12}, {205, 13}, {245, 11}, {290, 12}, {330, 13}};
    /** Die eingesponnenen Opfer und ihre Habe. */
    private static final int[][] VICTIMS = {{-9, 7}, {11, -6}, {4, 10}};
    /** Die Nester mit den Spawnern. */
    static final int[][] NESTS = {{-12, -5}, {12, 5}};

    private BroodGrotto() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = DeepLair.origin(context, terrain, seed, -48, 40, 24, CY + RY + 10);
        if (origin == null) {
            return Optional.empty();
        }
        Rotation rotation = DeepLair.rotation(seed);
        BoundingBox box = box(origin, rotation);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.BROOD_GROTTO, 0, origin, rotation, seed, new int[0], box))));
    }

    static BoundingBox box(BlockPos origin, Rotation rotation) {
        return SitePiece.box(origin, rotation, -RX - 10, -5, -RZ - 14, RX + 10, CY + RY + 5, RZ + 14);
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Builder(b).build();
    }

    private static final class Builder {
        private final VillageBuilder b;

        Builder(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            SiteCave.dome(this.b, RX, RY, RZ, CY, 0, 0.14, this::ground);
            DeepLair.tunnel(this.b, 0, RZ - 2, 90, 10, 1.7, this::ground);
            DeepLair.tunnel(this.b, -RX + 3, -6, 200, 9, 1.6, this::ground);
            this.nest();
            this.sacs();
            this.victims();
            this.nests();
            this.threads();
            this.webs();
            this.litter();
            DeepLair.boss(this.b, ModEntities.BROOD_MOTHER.get(), 0, 3, 0, 0.0, Direction.SOUTH);
        }

        BlockState ground(int x, int z) {
            double r = Math.sqrt(x * x + z * z);
            float n = this.b.noise(x, -3, z);
            if (r < 7.0 + n * 2.0) {
                return Blocks.PALE_MOSS_BLOCK.defaultBlockState();
            }
            return n < 0.3f ? Blocks.COARSE_DIRT.defaultBlockState()
                    : n < 0.55f ? Blocks.STONE.defaultBlockState()
                    : n < 0.72f ? Blocks.TUFF.defaultBlockState()
                    : n < 0.86f ? Blocks.GRAVEL.defaultBlockState() : Blocks.PALE_MOSS_BLOCK.defaultBlockState();
        }

        /** Der Hügel aus Seide in der Mitte, auf dem sie ruht. */
        void nest() {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    double r = Math.sqrt(x * x + z * z) + (this.b.noise(x, 3, z) - 0.5) * 0.8;
                    int h = r < 2.6 ? 2 : r < 4.6 ? 1 : 0;
                    for (int y = 1; y <= h; y++) {
                        this.b.set(x, y, z, Blocks.PALE_MOSS_BLOCK);
                    }
                    if (r < 5.8 && this.b.get(x, h + 1, z).isAir()) {
                        float n = this.b.noise(x, 4, z);
                        if (r > 4.6 && n < 0.45f) {
                            this.b.set(x, h + 1, z, Blocks.COBWEB);
                        } else if (n < 0.7f) {
                            this.b.set(x, h + 1, z, Blocks.PALE_MOSS_CARPET);
                        }
                    }
                }
            }
            // die Mitte frei für die Brutmutter (sie ist über drei Blöcke breit)
            this.b.air(-2, 3, -2, 2, 5, 2);
        }

        /** Die Eiersäcke: Klumpen am Boden und an der Decke, mit Netzen umsponnen. */
        void sacs() {
            for (int i = 0; i < SACS.length; i++) {
                int[] s = SACS[i];
                double a = Math.toRadians(s[0]);
                int x = (int) Math.round(Math.cos(a) * s[1] * RX / 17.0);
                int z = (int) Math.round(Math.sin(a) * s[1] * RZ / 17.0);
                boolean hanging = i % 2 == 1;
                if (hanging) {
                    int top = SiteCave.ceiling(this.b, x, z, 3, CY + RY + 3);
                    if (top >= CY + RY + 3) {
                        continue;
                    }
                    this.b.set(x, top - 1, z, Blocks.MUSHROOM_STEM);
                    this.b.set(x, top - 2, z, Blocks.MUSHROOM_STEM);
                    this.b.set(x + 1, top - 1, z, Blocks.MUSHROOM_STEM);
                    this.b.set(x, top - 3, z, Blocks.COBWEB);
                    this.b.set(x + 1, top - 2, z, Blocks.COBWEB);
                    this.b.set(x - 1, top - 1, z, Blocks.COBWEB);
                } else {
                    this.b.set(x, 1, z, Blocks.MUSHROOM_STEM);
                    this.b.set(x, 2, z, Blocks.MUSHROOM_STEM);
                    this.b.set(x + 1, 1, z, Blocks.MUSHROOM_STEM);
                    this.b.set(x, 1, z + 1, Blocks.MUSHROOM_STEM);
                    this.b.set(x, 3, z, Blocks.COBWEB);
                    this.b.set(x + 1, 2, z, Blocks.COBWEB);
                    this.b.set(x - 1, 1, z, Blocks.COBWEB);
                    this.b.set(x + 1, 1, z + 1, Blocks.PALE_MOSS_CARPET);
                }
            }
        }

        /** Die eingesponnenen Opfer: weiße Kokons, in einem ein Schädel; daneben ihre Habe in einer Truhe. */
        void victims() {
            for (int i = 0; i < VICTIMS.length; i++) {
                int x = VICTIMS[i][0];
                int z = VICTIMS[i][1];
                this.b.set(x, 1, z, Blocks.MUSHROOM_STEM);
                this.b.set(x, 2, z, Blocks.MUSHROOM_STEM);
                this.b.set(x, 3, z, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, (i * 5) & 15));
                this.b.set(x + 1, 2, z, Blocks.COBWEB);
                this.b.set(x - 1, 1, z, Blocks.COBWEB);
                this.b.set(x, 4, z, Blocks.COBWEB);
                if (i < 2) {
                    Direction face = x > 0 ? Direction.WEST : Direction.EAST;
                    this.b.chest(x, 1, z + 1, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, face), ModLootTables.SITE_BROOD_GROTTO);
                    this.b.set(x, 1, z - 1, Blocks.LANTERN.defaultBlockState());
                    this.b.set(x + (x > 0 ? 1 : -1), 1, z - 1, Blocks.BONE_BLOCK);
                }
            }
        }

        /** Zwei Nester am Rand, in denen ständig Höhlenspinnen schlüpfen. */
        void nests() {
            for (int[] n : NESTS) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dz != 0) {
                            this.b.set(n[0] + dx, 1, n[1] + dz, this.b.noise(n[0] + dx, 1, n[1] + dz) < 0.6f ? Blocks.COBWEB : Blocks.MUSHROOM_STEM);
                        }
                    }
                }
                DeepLair.spawner(this.b, n[0], 1, n[1], ModEntities.CAVE_SPIDER.get());
                this.b.set(n[0], 2, n[1], Blocks.COBWEB);
            }
        }

        /** Bleiche Fäden hängen von der Decke. */
        void threads() {
            for (int x = -RX + 2; x <= RX - 2; x++) {
                for (int z = -RZ + 2; z <= RZ - 2; z++) {
                    if (!SiteCave.column(this.b, x, z) || this.b.noise(x, 31, z) > 0.1f) {
                        continue;
                    }
                    int top = SiteCave.ceiling(this.b, x, z, 3, CY + RY + 3);
                    if (top >= CY + RY + 3 || !this.b.get(x, top - 1, z).isAir()) {
                        continue;
                    }
                    int len = 1 + (int) (this.b.noise(x, 32, z) * 4);
                    for (int i = 1; i <= len; i++) {
                        if (!this.b.get(x, top - i, z).isAir() || top - i <= 3) {
                            break;
                        }
                        boolean tip = i == len || !this.b.get(x, top - i - 1, z).isAir();
                        this.b.set(x, top - i, z, Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, tip));
                    }
                }
            }
        }

        /** Netze: unter der Decke, an den Wänden und am Boden nahe dem Rand. */
        void webs() {
            for (int x = -RX - 2; x <= RX + 2; x++) {
                for (int z = -RZ - 2; z <= RZ + 2; z++) {
                    if (!SiteCave.column(this.b, x, z)) {
                        continue;
                    }
                    double rim = (x * x) / (double) (RX * RX) + (z * z) / (double) (RZ * RZ);
                    for (int y = 1; y <= CY + RY + 2; y++) {
                        if (!this.b.get(x, y, z).isAir()) {
                            continue;
                        }
                        boolean roof = !this.b.get(x, y + 1, z).isAir();
                        boolean wall = !this.b.get(x + 1, y, z).isAir() || !this.b.get(x - 1, y, z).isAir() || !this.b.get(x, y, z + 1).isAir()
                                || !this.b.get(x, y, z - 1).isAir();
                        float n = this.b.noise(x, y + 40, z);
                        if ((roof && n < 0.09f) || (wall && rim > 0.55 && n < 0.12f) || (y == 1 && rim > 0.6 && n < 0.05f)) {
                            this.b.set(x, y, z, Blocks.COBWEB);
                        }
                    }
                }
            }
        }

        /** Gebeine und Schädel, Seidenfäden am Boden, ein wenig Leuchtflechte. */
        void litter() {
            for (int x = -RX + 3; x <= RX - 3; x++) {
                for (int z = -RZ + 3; z <= RZ - 3; z++) {
                    if (Math.abs(x) <= 6 && Math.abs(z) <= 6 || !this.b.get(x, 1, z).isAir()) {
                        continue;
                    }
                    BlockState below = this.b.get(x, 0, z);
                    if (!below.isSolidRender()) {
                        continue;
                    }
                    float n = this.b.noise(x, 50, z);
                    if (n < 0.012f) {
                        this.b.set(x, 1, z, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, (int) (n * 1000) & 15));
                    } else if (n < 0.03f) {
                        this.b.set(x, 1, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(BlockStateProperties.AXIS,
                                n < 0.02f ? Direction.Axis.X : Direction.Axis.Z));
                    } else if (n < 0.05f) {
                        this.b.set(x, 1, z, Blocks.GLOW_LICHEN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true));
                    } else if (n < 0.07f && Math.abs(x) < 11 && Math.abs(z) < 10) {
                        this.b.set(x, 1, z, Blocks.PALE_MOSS_CARPET);
                    }
                }
            }
        }
    }
}
