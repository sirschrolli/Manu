package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Das Hünengrab (seit 2.0): ein grasbewachsener Grabhügel, umringt von einem Kreis stehender Steine – manche umgestürzt.
 * Vorn ein Tor aus zwei Findlingen und einem Deckstein; ein Gang aus Steinplatten führt hinab in die Grabkammer des alten
 * Fürsten. Dort liegen seine Gebeine auf einer steinernen Bahre zwischen Kerzen und Urnen, an seinen Füßen der Grabschatz,
 * in den Nischen Schädel und Krüge – und in der Wand hinter ihm schläft etwas, das die Toten weckt.
 *
 * <p>Eigene Koordinaten: Mitte des Hügels bei (0, 0, 0), y = 0 ist der erste freie Block über dem Boden; vorn (+z) das Tor.
 */
final class Barrow {
    /** Die Kammer: x -3..3, z -4..2, Fußboden auf y = -3 (man steht auf -2), Decke auf y = 3. */
    static final int FLOOR = -3;
    static final double MOUND = 10.5;
    static final double RING = 14.5;
    static final BlockPos HOARD = new BlockPos(0, -2, 2);
    static final BlockPos SPAWNER = new BlockPos(0, -2, -5);

    private Barrow() {}

    static void register() {
        Wonders.add(new Wonders.Wonder(SiteKind.BARROW, "huenengrab", "Barrow", "Hünengrab", 3, Wonders.Ground.LAND, false,
                Barrow::plan, Barrow::build, origin -> piece(origin, Rotation.NONE, 0xBA22L),
                new double[][] {Wonders.view(-16, 8, 18, 0, 2, 0), Wonders.view(0.5, 1, 17, 0.5, 0.5, 6), Wonders.view(0.5, -1.4, 4, 0, -1.5, -3),
                        Wonders.view(2.5, -1, 1.5, -1, -1.5, -3)}));
    }

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = Kit.land(context, terrain, 12, 4);
        return origin == null ? Optional.empty() : Kit.stub(piece(origin, Kit.rotation(seed), seed));
    }

    static SitePiece piece(BlockPos origin, Rotation rotation, long seed) {
        int r = (int) RING + 3;
        return new SitePiece(SiteKind.BARROW, 0, origin, rotation, seed, new int[0], SitePiece.box(origin, rotation, -r, FLOOR - 3, -r, r, 12, r));
    }

    static void build(VillageBuilder b, SitePiece piece) {
        new Grave(b).build();
    }

    private static final class Grave {
        private final VillageBuilder b;

        Grave(VillageBuilder b) {
            this.b = b;
        }

        void build() {
            this.ground();
            this.mound();
            this.chamber();
            this.passage();
            this.portal();
            this.stones();
        }

        private BlockState megalith(int x, int y, int z) {
            return DeepLair.pick(this.b, x, y, z, 17, Kit.s(Blocks.TUFF), Kit.s(Blocks.TUFF), Kit.s(Blocks.ANDESITE), Kit.s(Blocks.MOSSY_COBBLESTONE),
                    Kit.s(Blocks.POLISHED_TUFF));
        }

        private BlockState wall(int x, int y, int z) {
            return Kit.weathered(this.b, x, y, z, Kit.s(Blocks.STONE_BRICKS), Kit.s(Blocks.MOSSY_STONE_BRICKS), 0.35f, Kit.s(Blocks.CRACKED_STONE_BRICKS),
                    0.2f);
        }

        /** Unter Hügel und Steinkreis: Bäume fort, Senken aufgefüllt. */
        private void ground() {
            int r = (int) RING + 2;
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    double d = Math.sqrt(x * x + z * z);
                    if (d > RING + 1.5) {
                        continue;
                    }
                    for (int y = 0; y <= 14; y++) {
                        BlockState old = this.b.get(x, y, z);
                        if (old.is(BlockTags.LOGS) || old.is(BlockTags.LEAVES)) {
                            this.b.set(x, y, z, Kit.s(Blocks.AIR));
                        }
                    }
                    if (d <= MOUND + 1) {
                        Kit.column(this.b, x, z, Kit.s(Blocks.GRASS_BLOCK), Kit.s(Blocks.DIRT), 0);
                    }
                }
            }
        }

        /** Der Hügel: eine flache Kuppel aus Erde, oben Gras mit Farnen und Büschen. */
        private void mound() {
            int r = (int) Math.ceil(MOUND);
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    double d = Math.sqrt(x * x + z * z);
                    if (d > MOUND) {
                        continue;
                    }
                    int h = (int) Math.round(6.2 * Math.sqrt(Math.max(0.0, 1.0 - (d / MOUND) * (d / MOUND))) + (this.b.noise(x, 5, z) - 0.5) * 0.8);
                    for (int y = -1; y < h; y++) {
                        BlockState state = y == h - 1 ? Kit.s(Blocks.GRASS_BLOCK)
                                : DeepLair.pick(this.b, x, y, z, 9, Kit.s(Blocks.DIRT), Kit.s(Blocks.DIRT), Kit.s(Blocks.COARSE_DIRT),
                                        Kit.s(Blocks.ROOTED_DIRT));
                        this.b.set(x, y, z, state);
                    }
                    if (h > 0 && this.b.chance(x, h, z, 0.22f)) {
                        this.b.set(x, h, z, Kit.s(this.b.chance(x, h + 1, z, 0.4f) ? Blocks.FERN : this.b.chance(x, h + 2, z, 0.5f) ? Blocks.BUSH
                                : Blocks.SHORT_GRASS));
                    }
                }
            }
        }

        /** Die Grabkammer: gemauert, die Bahre mit den Gebeinen, Kerzen, Urnen, Nischen, der Schatz, der Spawner in der Wand. */
        private void chamber() {
            for (int x = -4; x <= 4; x++) {
                for (int z = -5; z <= 3; z++) {
                    for (int y = FLOOR; y <= 3; y++) {
                        boolean shell = x == -4 || x == 4 || z == -5 || z == 3 || y == FLOOR || y == 3;
                        if (shell) {
                            this.b.set(x, y, z, y == FLOOR ? (this.b.chance(x, y, z, 0.3f) ? Kit.s(Blocks.COBBLESTONE) : Kit.s(Blocks.STONE_BRICKS))
                                    : y == 3 ? Kit.s(Blocks.SMOOTH_STONE) : this.wall(x, y, z));
                        } else {
                            this.b.set(x, y, z, Kit.s(Blocks.AIR));
                        }
                    }
                }
            }
            // Pfeiler aus Findlingen in den Ecken, die Decke tragen
            for (int[] c : new int[][] {{-3, -4}, {3, -4}, {-3, 2}, {3, 2}}) {
                for (int y = FLOOR + 1; y <= 2; y++) {
                    this.b.set(c[0], y, c[1], this.megalith(c[0], y, c[1]));
                }
            }
            // die Bahre mit den Gebeinen
            for (int z = -3; z <= 0; z++) {
                for (int x = -1; x <= 1; x++) {
                    this.b.set(x, -2, z, Kit.s(Blocks.POLISHED_ANDESITE));
                }
                this.b.set(0, -1, z, z == -3 ? Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 0)
                        : Kit.s(Blocks.CARPET.pick(net.minecraft.world.item.DyeColor.WHITE)));
            }
            for (int[] c : new int[][] {{-1, -3}, {1, -3}, {-1, 0}, {1, 0}}) {
                Kit.candles(this.b, c[0], -1, c[1], Blocks.CANDLE, 1 + Math.floorMod(c[0] + c[1], 3), false);
            }
            Kit.chest(this.b, HOARD.getX(), HOARD.getY(), HOARD.getZ(), Direction.NORTH, Kit.loot("barrow_hoard"));
            // Urnen und Nischen an den Seiten
            for (int x : new int[] {-3, 3}) {
                for (int z = -2; z <= 0; z++) {
                    this.b.set(x, -2, z, Kit.s(z == -1 ? Blocks.DECORATED_POT : Blocks.AIR));
                    this.b.set(x + Integer.signum(x), -1, z, Kit.s(Blocks.AIR));
                    this.b.set(x + Integer.signum(x), 0, z, Kit.s(Blocks.AIR));
                }
                this.b.set(x + Integer.signum(x), -2, -1, Kit.s(Blocks.STONE_BRICK_SLAB).setValue(net.minecraft.world.level.block.SlabBlock.TYPE,
                        SlabType.TOP));
                this.b.set(x + Integer.signum(x), -1, -1, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, x < 0 ? 4 : 12));
                this.b.set(x + Integer.signum(x), -1, -2, Kit.s(Blocks.DECORATED_POT));
            }
            Kit.lootBarrel(this.b, -3, -2, -3, Kit.loot("barrow"));
            // hinter der Bahre in der Wand: der Spawner, vor ihm Spinnweben
            this.b.set(SPAWNER.getX(), SPAWNER.getY() + 1, SPAWNER.getZ(), Kit.s(Blocks.AIR));
            DeepLair.spawner(this.b, SPAWNER.getX(), SPAWNER.getY(), SPAWNER.getZ(), ModEntities.SKELETON.get());
            Kit.cobwebs(this.b, -3, 1, -4, 3, 2, 2, 0.12f);
            this.b.hangingLantern(0, 2, -1, Blocks.SOUL_LANTERN);
        }

        /** Der Gang: zwischen Findlingen, unter Decksteinen, über Stufen hinauf zum Tor. */
        private void passage() {
            for (int z = 3; z <= 11; z++) {
                int floor = z <= 4 ? FLOOR : z <= 6 ? FLOOR + 1 : FLOOR + 2;
                for (int x = -2; x <= 2; x++) {
                    for (int y = floor; y <= floor + 4; y++) {
                        boolean side = x == -2 || x == 2;
                        BlockState state;
                        if (y == floor) {
                            state = side ? this.megalith(x, y, z) : Kit.s(this.b.chance(x, y, z, 0.3f) ? Blocks.COBBLESTONE : Blocks.STONE_BRICKS);
                        } else if (y == floor + 4) {
                            state = Kit.s(Blocks.POLISHED_ANDESITE);
                        } else {
                            state = side ? this.megalith(x, y, z) : Kit.s(Blocks.AIR);
                        }
                        this.b.set(x, y, z, state);
                    }
                }
            }
            for (int x = -1; x <= 1; x++) {
                this.b.set(x, FLOOR + 1, 5, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
                this.b.set(x, FLOOR + 2, 7, VillageBuilder.stairs(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH, Half.BOTTOM));
            }
            Kit.cobwebs(this.b, -1, FLOOR + 3, 5, 1, FLOOR + 5, 9, 0.15f);
        }

        /** Das Tor: zwei aufrechte Findlinge und ein Deckstein; davor ein Vorplatz aus Steinplatten. */
        private void portal() {
            for (int x : new int[] {-2, 2}) {
                for (int y = -1; y <= 2; y++) {
                    this.b.set(x, y, 11, this.megalith(x, y, 11));
                }
            }
            for (int x = -3; x <= 3; x++) {
                this.b.set(x, 3, 11, Kit.s(Blocks.POLISHED_ANDESITE));
            }
            this.b.set(-3, 3, 11, Kit.s(Blocks.ANDESITE_SLAB));
            this.b.set(3, 3, 11, Kit.s(Blocks.ANDESITE_SLAB));
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 2; y++) {
                    this.b.set(x, y, 11, y == -1 ? Kit.s(Blocks.STONE_BRICKS) : Kit.s(Blocks.AIR));
                }
            }
            for (int x = -2; x <= 2; x++) {
                for (int z = 12; z <= 14; z++) {
                    if (this.b.chance(x, -1, z, 0.7f)) {
                        this.b.set(x, -1, z, Kit.s(this.b.chance(x, -2, z, 0.5f) ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE));
                    }
                    if (!this.b.get(x, 0, z).isAir() && !this.b.get(x, 0, z).isSolid()) {
                        this.b.set(x, 0, z, Kit.s(Blocks.AIR));
                    }
                }
            }
        }

        /** Der Steinkreis: zwölf Findlinge, zwei davon umgestürzt. */
        private void stones() {
            for (int k = 0; k < 12; k++) {
                double a = Math.toRadians(k * 30.0 + 15.0);
                int x = (int) Math.round(Math.cos(a) * RING);
                int z = (int) Math.round(Math.sin(a) * RING);
                if (z > 8 && Math.abs(x) < 4) {
                    continue;
                }
                int ground = Kit.groundAt(this.b, x, z, 8);
                if (ground == Integer.MIN_VALUE) {
                    continue;
                }
                if (k == 4 || k == 9) {
                    // umgestürzt: liegt nach außen
                    int dx = (int) Math.signum(Math.round(Math.cos(a)));
                    int dz = (int) Math.signum(Math.round(Math.sin(a)));
                    for (int i = 0; i < 3; i++) {
                        this.b.set(x + dx * i, ground, z + dz * i, this.megalith(x + dx * i, ground, z + dz * i));
                    }
                    continue;
                }
                int h = 2 + Math.floorMod(k * 7, 3);
                this.b.set(x, ground - 1, z, this.megalith(x, ground - 1, z));
                for (int y = ground; y < ground + h; y++) {
                    this.b.set(x, y, z, this.megalith(x, y, z));
                }
                if (this.b.chance(x, ground + h, z, 0.5f)) {
                    this.b.set(x, ground + h, z, Kit.s(Blocks.MOSS_CARPET));
                }
            }
        }
    }
}
