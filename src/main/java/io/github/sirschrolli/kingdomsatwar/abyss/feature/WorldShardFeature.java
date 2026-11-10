package io.github.sirschrolli.kingdomsatwar.abyss.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.block.abyss.GravityCrystalBlock;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

/**
 * Ein <b>Weltensplitter</b>: ein Stück Land, aus einer anderen Welt gerissen und schief ins Chaos geworfen – oben die Haut
 * seiner Welt (Gras auf Erde, Netherrack, Endstein, Schattengras, Leerenmoos, Schnee, Sand), darunter ihr Fels, ganz unten
 * die zerfetzte Unterseite aus Chaosgestein mit glühenden Nähten. Er liegt in beliebiger Lage: fast waagrecht, steil gekippt
 * oder ganz <b>kopfüber</b> – dann hängt sein Gras nach unten, und Laub, Ranken und Wurzeln baumeln ins Nichts. Auf der
 * Oberseite wächst, was in seiner Welt wuchs; von Netherstücken stürzt Lava, von Wiesen Wasser in die Tiefe; manchmal sitzt
 * ein Kristall der verdrehten Schwerkraft am Rand.
 *
 * @param palettes       aus welchen Welten die Splitter stammen (siehe {@link #PALETTES}), je Eintrag gleich wahrscheinlich
 * @param minRadius      kleinster Halbmesser
 * @param maxRadius      größter Halbmesser (höchstens 13, damit er in die erreichbaren Chunks passt)
 * @param invertedChance Anteil der Splitter, die kopfüber treiben
 */
public record WorldShardFeature(List<Integer> palettes, int minRadius, int maxRadius, float invertedChance) implements Feature {
    public static final MapCodec<WorldShardFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(0, 6).listOf().fieldOf("palettes").forGetter(WorldShardFeature::palettes),
            Codec.intRange(3, 13).fieldOf("min_radius").forGetter(WorldShardFeature::minRadius),
            Codec.intRange(3, 13).fieldOf("max_radius").forGetter(WorldShardFeature::maxRadius),
            Codec.floatRange(0.0f, 1.0f).fieldOf("inverted_chance").forGetter(WorldShardFeature::invertedChance)
    ).apply(i, WorldShardFeature::new));

    public static final int OVERWORLD = 0;
    public static final int NETHER = 1;
    public static final int END = 2;
    public static final int SHADOW = 3;
    public static final int VOID = 4;
    public static final int FROST = 5;
    public static final int DESERT = 6;

    @Override
    public MapCodec<WorldShardFeature> codec() {
        return CODEC;
    }

    /** Die Haut einer Welt: oben, darunter, Fels. */
    private record Palette(int id, BlockState top, BlockState sub, BlockState stone) {}

    private static Palette palette(int id) {
        return switch (id) {
            case NETHER -> new Palette(id, Blocks.CRIMSON_NYLIUM.defaultBlockState(), Blocks.NETHERRACK.defaultBlockState(),
                    Blocks.BLACKSTONE.defaultBlockState());
            case END -> new Palette(id, Blocks.END_STONE.defaultBlockState(), Blocks.END_STONE.defaultBlockState(),
                    VoidBlocks.ASTRAL_MARBLE.base().get().defaultBlockState());
            case SHADOW -> new Palette(id, ShadowBlocks.SHADOW_GRASS.get().defaultBlockState(), ShadowBlocks.SHADOW_SOIL.get().defaultBlockState(),
                    ShadowBlocks.SHADOW_STONE.base().get().defaultBlockState());
            case VOID -> new Palette(id, VoidBlocks.VOID_MOSS.get().defaultBlockState(), VoidBlocks.VOID_STONE.base().get().defaultBlockState(),
                    VoidBlocks.VOID_STONE.base().get().defaultBlockState());
            case FROST -> new Palette(id, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.PACKED_ICE.defaultBlockState(),
                    Blocks.STONE.defaultBlockState());
            case DESERT -> new Palette(id, Blocks.SAND.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(),
                    Blocks.SANDSTONE.defaultBlockState());
            default -> new Palette(OVERWORLD, Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIRT.defaultBlockState(),
                    Blocks.STONE.defaultBlockState());
        };
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (this.palettes.isEmpty()) {
            return false;
        }
        Palette p = palette(this.palettes.get(random.nextInt(this.palettes.size())));
        double radius = this.minRadius + random.nextDouble() * Math.max(0, this.maxRadius - this.minRadius);
        boolean inverted = random.nextFloat() < this.invertedChance;
        // Lage: meist schief, manchmal fast waagrecht, manchmal kopfüber
        double tilt;
        if (inverted) {
            tilt = Math.toRadians(145 + random.nextDouble() * 35);
        } else if (random.nextInt(3) == 0) {
            tilt = Math.toRadians(random.nextDouble() * 18);
        } else {
            tilt = Math.toRadians(18 + random.nextDouble() * 52);
        }
        double heading = random.nextDouble() * Math.PI * 2;
        Vec3 up = new Vec3(Math.sin(tilt) * Math.cos(heading), Math.cos(tilt), Math.sin(tilt) * Math.sin(heading));
        Vec3 ref = Math.abs(up.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 a0 = ref.cross(up).normalize();
        Vec3 b0 = up.cross(a0).normalize();
        double spin = random.nextDouble() * Math.PI * 2;
        Vec3 a = a0.scale(Math.cos(spin)).add(b0.scale(Math.sin(spin)));
        Vec3 b = b0.scale(Math.cos(spin)).subtract(a0.scale(Math.sin(spin)));
        double stretch = 0.65 + random.nextDouble() * 0.35;
        double top = 1.2 + radius * 0.08;
        double depth = radius * (0.5 + random.nextDouble() * 0.4);
        long seed = random.nextLong();
        int ext = (int) Math.ceil(Math.max(radius, depth + top)) + 1;

        BlockState chaos = AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState();
        BlockState seam = AbyssBlocks.WORLD_SEAM.get().defaultBlockState();
        List<BlockPos> skin = new ArrayList<>();
        List<BlockPos> rim = new ArrayList<>();
        int placed = 0;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int dx = -ext; dx <= ext; dx++) {
            for (int dy = -ext; dy <= ext; dy++) {
                for (int dz = -ext; dz <= ext; dz++) {
                    double la = dx * a.x + dy * a.y + dz * a.z;
                    double lb = dx * b.x + dy * b.y + dz * b.z;
                    double lc = dx * up.x + dy * up.y + dz * up.z;
                    double rr = (la / radius) * (la / radius) + (lb / (radius * stretch)) * (lb / (radius * stretch));
                    double jag = 1.0 + (AbyssBuild.hash(seed, dx >> 1, dy >> 1, dz >> 1) - 0.5) * 0.3;
                    if (rr > jag * jag) {
                        continue;
                    }
                    double r = Math.sqrt(rr) / jag;
                    double surface = top * (1.0 - r * 0.35);
                    if (lc > surface) {
                        continue;
                    }
                    double low = -depth * (1.0 - Math.pow(r, 1.5)) - 0.9 - AbyssBuild.hash(seed + 7, dx, dy, dz) * 1.2;
                    if (lc < low) {
                        continue;
                    }
                    double fromTop = surface - lc;
                    double fromBottom = lc - low;
                    BlockState state;
                    if (fromTop < 1.0) {
                        state = p.top();
                    } else if (fromTop < 3.2) {
                        state = p.sub();
                    } else if (fromBottom < 1.7) {
                        state = AbyssBuild.hash(seed + 3, dx, dy, dz) < 0.1 ? seam : chaos;
                    } else {
                        state = p.stone();
                    }
                    at.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (AbyssBuild.place(level, origin, at, state)) {
                        placed++;
                        if (fromTop < 1.0) {
                            skin.add(at.immutable());
                            if (r > 0.82) {
                                rim.add(at.immutable());
                            }
                        }
                    }
                }
            }
        }
        if (placed < 12) {
            return placed > 0;
        }
        // was auf der Oberseite wächst (oder kopfüber hängt)
        Direction outward = up.y >= 0 ? Direction.UP : Direction.DOWN;
        for (BlockPos pos : skin) {
            BlockPos free = pos.relative(outward);
            if (!AbyssBuild.inReach(origin, free) || !level.isEmptyBlock(free)) {
                continue;
            }
            if (outward == Direction.UP && up.y > 0.3) {
                decorate(level, origin, p, free, random);
            } else if (outward == Direction.DOWN && up.y < -0.3) {
                hang(level, origin, p, free, random);
            }
        }
        // ein Fall ins Nichts: Lava von Netherstücken, Wasser von Wiesen und Schatten
        if (!rim.isEmpty() && up.y > 0.25 && (p.id() == NETHER || p.id() == OVERWORLD || p.id() == SHADOW) && random.nextInt(3) == 0) {
            BlockPos edge = rim.get(random.nextInt(rim.size()));
            BlockPos source = edge.above();
            if (AbyssBuild.inReach(origin, source) && level.isEmptyBlock(source)) {
                boolean lava = p.id() == NETHER;
                level.setBlock(edge, (lava ? Blocks.LAVA : Blocks.WATER).defaultBlockState(), 2);
                level.scheduleTick(edge, lava ? Fluids.LAVA : Fluids.WATER, 0);
            }
        }
        // ein Kristall der verdrehten Schwerkraft am Rand
        if (!rim.isEmpty() && up.y > 0.4 && random.nextInt(5) == 0) {
            BlockPos edge = rim.get(random.nextInt(rim.size())).above();
            if (AbyssBuild.inReach(origin, edge) && level.isEmptyBlock(edge)) {
                Block crystal = random.nextInt(3) == 0 ? AbyssBlocks.LIFT_CRYSTAL.get() : AbyssBlocks.FLOAT_CRYSTAL.get();
                level.setBlock(edge, crystal.defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP), 2);
            }
        }
        return true;
    }

    /** Was auf der Oberseite wächst. */
    private static void decorate(WorldGenLevel level, BlockPos origin, Palette p, BlockPos pos, RandomSource random) {
        int roll = random.nextInt(100);
        switch (p.id()) {
            case OVERWORLD -> {
                if (roll < 3) {
                    tree(level, origin, pos, random, false);
                } else if (roll < 12) {
                    BlockState flower = switch (random.nextInt(4)) {
                        case 0 -> Blocks.POPPY.defaultBlockState();
                        case 1 -> Blocks.DANDELION.defaultBlockState();
                        case 2 -> Blocks.CORNFLOWER.defaultBlockState();
                        default -> Blocks.AZURE_BLUET.defaultBlockState();
                    };
                    level.setBlock(pos, flower, 2);
                } else if (roll < 55) {
                    level.setBlock(pos, Blocks.SHORT_GRASS.defaultBlockState(), 2);
                }
            }
            case NETHER -> {
                if (roll < 6) {
                    level.setBlock(pos, Blocks.CRIMSON_FUNGUS.defaultBlockState(), 2);
                } else if (roll < 35) {
                    level.setBlock(pos, Blocks.CRIMSON_ROOTS.defaultBlockState(), 2);
                }
            }
            case END -> {
                if (roll < 5) {
                    ChorusFlowerBlock.generatePlant(level, pos, random, 5);
                }
            }
            case SHADOW -> {
                if (roll < 6) {
                    BlockState fern = ShadowBlocks.MOON_FERN.get().defaultBlockState();
                    if (AbyssBuild.inReach(origin, pos.above()) && level.isEmptyBlock(pos.above()) && fern.canSurvive(level, pos)) {
                        DoublePlantBlock.placeAt(level, fern, pos, 2);
                    }
                } else if (roll < 40) {
                    BlockState plant = (random.nextBoolean() ? ShadowBlocks.PALE_GRASS : ShadowBlocks.SHADOW_SPROUTS).get().defaultBlockState();
                    if (plant.canSurvive(level, pos)) {
                        level.setBlock(pos, plant, 2);
                    }
                }
            }
            case VOID -> {
                if (roll < 8) {
                    level.setBlock(pos, VoidBlocks.VOID_CRYSTAL_CLUSTER.get().defaultBlockState(), 2);
                } else if (roll < 30) {
                    level.setBlock(pos, VoidBlocks.VOID_BLOOM.get().defaultBlockState(), 2);
                }
            }
            case FROST -> {
                if (roll < 30) {
                    level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
                }
            }
            case DESERT -> {
                if (roll < 3 && AbyssBuild.inReach(origin, pos.above(2)) && level.isEmptyBlock(pos.above()) && level.isEmptyBlock(pos.above(2))) {
                    int h = 1 + random.nextInt(3);
                    for (int i = 0; i < h; i++) {
                        level.setBlock(pos.above(i), Blocks.CACTUS.defaultBlockState(), 2);
                    }
                } else if (roll < 9) {
                    level.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), 2);
                }
            }
            default -> {
            }
        }
    }

    /** Was an der nach unten gekehrten Oberseite eines kopfüber treibenden Splitters hängt. */
    private static void hang(WorldGenLevel level, BlockPos origin, Palette p, BlockPos pos, RandomSource random) {
        int roll = random.nextInt(100);
        switch (p.id()) {
            case OVERWORLD -> {
                if (roll < 4) {
                    tree(level, origin, pos, random, true);
                } else if (roll < 24) {
                    int len = 1 + random.nextInt(3);
                    BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                    for (int i = 0; i < len; i++) {
                        AbyssBuild.place(level, origin, pos.below(i), leaves);
                    }
                }
            }
            case NETHER -> {
                if (roll < 22) {
                    int len = 2 + random.nextInt(6);
                    for (int i = 0; i < len; i++) {
                        BlockPos v = pos.below(i);
                        if (!AbyssBuild.inReach(origin, v) || !level.isEmptyBlock(v)) {
                            break;
                        }
                        boolean last = i == len - 1 || !level.isEmptyBlock(v.below());
                        level.setBlock(v, (last ? Blocks.WEEPING_VINES : Blocks.WEEPING_VINES_PLANT).defaultBlockState(), 2);
                        if (last) {
                            break;
                        }
                    }
                }
            }
            case END -> {
                if (roll < 8) {
                    level.setBlock(pos, Blocks.END_ROD.defaultBlockState().setValue(net.minecraft.world.level.block.EndRodBlock.FACING, Direction.DOWN), 2);
                }
            }
            case SHADOW, VOID -> {
                if (roll < 18) {
                    int len = 2 + random.nextInt(6);
                    for (int i = 0; i < len; i++) {
                        BlockPos v = pos.below(i);
                        if (!AbyssBuild.inReach(origin, v) || !level.isEmptyBlock(v)) {
                            break;
                        }
                        boolean last = i == len - 1 || !level.isEmptyBlock(v.below());
                        level.setBlock(v, (last ? VoidBlocks.STAR_VINES : VoidBlocks.STAR_VINES_PLANT).get().defaultBlockState(), 2);
                        if (last) {
                            break;
                        }
                    }
                }
            }
            default -> {
            }
        }
    }

    /** Eine kleine Eiche – aufrecht oder kopfüber hängend (dann wächst sie nach unten). */
    private static void tree(WorldGenLevel level, BlockPos origin, BlockPos base, RandomSource random, boolean hanging) {
        Direction grow = hanging ? Direction.DOWN : Direction.UP;
        int height = 3 + random.nextInt(3);
        BlockState log = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int i = 0; i < height; i++) {
            if (!AbyssBuild.place(level, origin, base.relative(grow, i), log)) {
                return;
            }
        }
        BlockPos crown = base.relative(grow, height - 1);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    double d = dx * dx + dz * dz + (dy - 0.5) * (dy - 0.5) * 1.6;
                    if (d > 5.2 || (d > 4.0 && random.nextInt(3) == 0)) {
                        continue;
                    }
                    BlockPos l = crown.relative(grow, dy).offset(dx, 0, dz);
                    AbyssBuild.place(level, origin, l, leaves);
                }
            }
        }
    }
}
