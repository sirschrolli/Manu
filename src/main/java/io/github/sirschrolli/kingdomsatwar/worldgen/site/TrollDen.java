package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Die Trollhöhle (seit 1.15): eine hohe, bemooste Höhle, in der es nach Rauch riecht. In der Mitte glimmt eine Feuerstelle
 * mit einem Bratspieß, ringsum liegen abgenagte Knochen. An der Westwand türmt sich der Hort – Gold, rohes Erz, gestohlene
 * Fässer, ein Amboss, ein Kessel und zwei Truhen. Im Norden, auf einem Lager aus Knochen und Fellen, schläft Grumbold, der
 * Trollvater. Von der Decke hängen Wurzeln und Tropfsteine; ein breiter Trollgang führt hinaus.
 *
 * <p>Eigene Koordinaten: Mitte der Höhle bei (0, 0, 0); y = 0 ist die Schicht des Bodens (man steht auf y = 1). Das Lager
 * liegt im Norden (−z), der Gang führt nach Süden.
 */
final class TrollDen {
    static final int RX = 16;
    static final int RY = 10;
    static final int RZ = 14;
    static final int CY = 1;
    /** Das Lager des Trollvaters und die Feuerstelle. */
    static final BlockPos BED = new BlockPos(0, 0, -7);
    static final BlockPos FIRE = new BlockPos(0, 0, 4);
    /** Der Hort an der Westwand. */
    static final BlockPos HOARD = new BlockPos(-11, 0, -1);

    private TrollDen() {}

    static Optional<Structure.GenerationStub> plan(Structure.GenerationContext context, SiteTerrain terrain, long seed) {
        BlockPos origin = DeepLair.origin(context, terrain, seed, -36, 34, 24, CY + RY + 10);
        if (origin == null) {
            return Optional.empty();
        }
        Rotation rotation = DeepLair.rotation(seed);
        BoundingBox box = box(origin, rotation);
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new SitePiece(SiteKind.TROLL_DEN, 0, origin, rotation, seed, new int[0], box))));
    }

    static BoundingBox box(BlockPos origin, Rotation rotation) {
        return SitePiece.box(origin, rotation, -RX - 6, -5, -RZ - 6, RX + 6, CY + RY + 5, RZ + 16);
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
            SiteCave.dome(this.b, RX, RY, RZ, CY, 0, 0.12, this::ground);
            DeepLair.tunnel(this.b, 3, RZ - 2, 80, 13, 2.2, this::ground);
            this.bed();
            this.fire();
            this.hoard();
            this.ceiling();
            this.floorLitter();
            DeepLair.boss(this.b, ModEntities.TROLL_FATHER.get(), BED.getX(), 3, BED.getZ(), 0.0, Direction.SOUTH);
        }

        /** Der Boden in weichen Flecken: Moos, grober Erde, Stein und Kies (nicht Block für Block gewürfelt); um das Feuer Asche und Kies. */
        BlockState ground(int x, int z) {
            double fire = Math.hypot(x - FIRE.getX(), z - FIRE.getZ());
            float n = this.b.noise(x, -3, z);
            if (fire < 3.2 + n) {
                return n < 0.55f ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRAVEL.defaultBlockState();
            }
            double moss = SiteCave.smooth(this.b, x, 0, z, 3, 6) + (n - 0.5) * 0.12;
            double earth = SiteCave.smooth(this.b, x, 0, z, 7, 4) + (n - 0.5) * 0.12;
            if (moss > 0.56) {
                return Blocks.MOSS_BLOCK.defaultBlockState();
            }
            if (earth > 0.6) {
                return n < 0.25f ? Blocks.ROOTED_DIRT.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
            }
            if (earth < 0.3) {
                return Blocks.GRAVEL.defaultBlockState();
            }
            return n < 0.3f ? Blocks.TUFF.defaultBlockState() : Blocks.STONE.defaultBlockState();
        }

        /** Das Lager: ein Hügel aus Knochen und Erde, oben Felle (braun, am Rand grau), ringsum Schädel. */
        void bed() {
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    double r = Math.hypot(x, z) + (this.b.noise(x, 5, z) - 0.5) * 0.7;
                    int h = r < 2.3 ? 2 : r < 3.8 ? 1 : 0;
                    int wx = BED.getX() + x;
                    int wz = BED.getZ() + z;
                    for (int y = 1; y <= h; y++) {
                        boolean bone = y == h || this.b.noise(wx, y, wz) < 0.5f;
                        this.b.set(wx, y, wz, bone ? Blocks.BONE_BLOCK.defaultBlockState().setValue(BlockStateProperties.AXIS,
                                (x + z) % 2 == 0 ? Direction.Axis.X : Direction.Axis.Z) : Blocks.COARSE_DIRT.defaultBlockState());
                    }
                    if (h == 2 && r < 2.0) {
                        this.b.set(wx, 3, wz, Blocks.CARPET.pick(DyeColor.BROWN));
                    } else if (h >= 1 && r < 3.2 && this.b.noise(wx, 9, wz) < 0.6f) {
                        this.b.set(wx, h + 1, wz, Blocks.CARPET.pick(DyeColor.GRAY));
                    } else if (h == 0 && r < 4.6 && this.b.noise(wx, 9, wz) < 0.22f) {
                        this.b.set(wx, 1, wz, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION,
                                (int) (this.b.noise(wx, 10, wz) * 16) & 15));
                    }
                }
            }
            // die Mitte frei für ihn (er ist zwei Blöcke breit und über vier hoch)
            this.b.air(BED.getX() - 1, 4, BED.getZ() - 1, BED.getX() + 1, 7, BED.getZ() + 1);
        }

        /** Die Feuerstelle: ein Steinring, ein Lagerfeuer und der Bratspieß darüber. */
        void fire() {
            int fx = FIRE.getX();
            int fz = FIRE.getZ();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dz != 0) {
                        this.b.set(fx + dx, 1, fz + dz, (dx + dz) % 2 == 0 ? Blocks.COBBLESTONE_SLAB.defaultBlockState()
                                : Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState());
                    }
                }
            }
            this.b.set(fx, 1, fz, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.FACING, Direction.SOUTH));
            for (int y = 1; y <= 3; y++) {
                this.b.set(fx - 2, y, fz, Blocks.SPRUCE_FENCE);
                this.b.set(fx + 2, y, fz, Blocks.SPRUCE_FENCE);
            }
            for (int dx = -1; dx <= 1; dx++) {
                this.b.set(fx + dx, 3, fz, Blocks.SPRUCE_FENCE);
            }
            // Sitzsteine ringsum und abgenagte Knochen
            this.b.set(fx - 3, 1, fz + 2, Blocks.MOSSY_COBBLESTONE);
            this.b.set(fx + 3, 1, fz + 2, Blocks.COBBLESTONE);
            this.b.set(fx + 2, 1, fz - 3, Blocks.BONE_BLOCK);
            this.b.set(fx - 3, 1, fz - 2, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, 3));
        }

        /** Der Hort an der Westwand. */
        void hoard() {
            int hx = HOARD.getX();
            int hz = HOARD.getZ();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    double r = Math.hypot(dx * 1.3, dz);
                    float n = this.b.noise(hx + dx, 2, hz + dz);
                    if (r < 2.2) {
                        this.b.set(hx + dx, 1, hz + dz, n < 0.3f ? Blocks.RAW_GOLD_BLOCK : n < 0.5f ? Blocks.RAW_IRON_BLOCK
                                : n < 0.62f ? Blocks.GOLD_BLOCK : Blocks.COARSE_DIRT);
                        if (r < 1.2) {
                            this.b.set(hx + dx, 2, hz + dz, n < 0.5f ? Blocks.RAW_GOLD_BLOCK : Blocks.RAW_COPPER_BLOCK);
                        }
                    }
                }
            }
            BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST);
            this.b.chest(hx + 2, 1, hz - 3, chest, ModLootTables.SITE_TROLL_DEN);
            this.b.chest(hx + 2, 1, hz + 3, chest, ModLootTables.SITE_TROLL_DEN);
            this.b.set(hx + 3, 1, hz, Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.NORTH));
            this.b.set(hx + 1, 1, hz + 4, Blocks.CAULDRON);
            this.b.set(hx + 1, 1, hz - 4, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
            this.b.set(hx, 1, hz - 4, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.EAST));
        }

        /** Wurzeln und Tropfsteine unter der Decke, Moos an den Wänden. */
        void ceiling() {
            for (int x = -RX + 1; x <= RX - 1; x++) {
                for (int z = -RZ + 1; z <= RZ - 1; z++) {
                    if (!SiteCave.column(this.b, x, z)) {
                        continue;
                    }
                    int top = SiteCave.ceiling(this.b, x, z, 4, CY + RY + 3);
                    if (top >= CY + RY + 3 || !this.b.get(x, top - 1, z).isAir()) {
                        continue;
                    }
                    float n = this.b.noise(x, 60, z);
                    if (n < 0.07f) {
                        this.b.set(x, top - 1, z, Blocks.HANGING_ROOTS);
                    } else if (n < 0.1f && this.b.get(x, top - 2, z).isAir()) {
                        // ein Tropfstein: eine oder zwei Glieder
                        boolean two = n < 0.085f && this.b.get(x, top - 3, z).isAir();
                        BlockState base = Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN);
                        if (two) {
                            this.b.set(x, top - 1, z, base.setValue(PointedDripstoneBlock.THICKNESS, SpeleothemThickness.FRUSTUM));
                            this.b.set(x, top - 2, z, base.setValue(PointedDripstoneBlock.THICKNESS, SpeleothemThickness.TIP));
                        } else {
                            this.b.set(x, top - 1, z, base.setValue(PointedDripstoneBlock.THICKNESS, SpeleothemThickness.TIP));
                        }
                    } else if (n < 0.13f) {
                        this.b.set(x, top - 1, z, Blocks.GLOW_LICHEN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.UP), true));
                    }
                }
            }
        }

        /** Am Boden: Moosteppiche, Pilze, abgenagte Knochen. */
        void floorLitter() {
            for (int x = -RX + 2; x <= RX - 2; x++) {
                for (int z = -RZ + 2; z <= RZ - 2; z++) {
                    if (Math.hypot(x - BED.getX(), z - BED.getZ()) < 5.0 || Math.hypot(x - FIRE.getX(), z - FIRE.getZ()) < 3.5
                            || Math.hypot((x - HOARD.getX()) * 1.3, z - HOARD.getZ()) < 4.5 || !this.b.get(x, 1, z).isAir()) {
                        continue;
                    }
                    BlockState below = this.b.get(x, 0, z);
                    if (!below.isSolidRender()) {
                        continue;
                    }
                    float n = this.b.noise(x, 70, z);
                    if (below.is(Blocks.MOSS_BLOCK) && n < 0.25f) {
                        this.b.set(x, 1, z, Blocks.MOSS_CARPET);
                    } else if (n < 0.03f) {
                        this.b.set(x, 1, z, n < 0.015f ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM);
                    } else if (n < 0.045f) {
                        this.b.set(x, 1, z, Blocks.BONE_BLOCK.defaultBlockState().setValue(BlockStateProperties.AXIS,
                                n < 0.038f ? Direction.Axis.X : Direction.Axis.Z));
                    } else if (n < 0.052f) {
                        this.b.set(x, 1, z, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, (int) (n * 999) & 15));
                    }
                }
            }
        }
    }
}
