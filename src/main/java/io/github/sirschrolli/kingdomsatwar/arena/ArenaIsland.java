package io.github.sirschrolli.kingdomsatwar.arena;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Reißt ein Stück Welt um einen Hort heraus und setzt es als schwebende Insel in eine Kampfdimension: eine Trommel um die
 * Mitte mit zerklüftetem Rand, darunter ein rundlicher, rauer Fels, der nach unten spitz zuläuft (wie eine schwebende
 * Insel). Kopiert werden nur Blöcke – keine Wesen, keine Truhenfüllungen, keine Spawner, keine Portale –, Schilder, Banner und
 * Köpfe behalten ihr Aussehen. Die Kopie steht still: Flüssigkeiten am Rand fließen nicht aus, Sand fällt nicht.
 */
public final class ArenaIsland {
    /** Setzen ohne Folgen: nur an die Spieler melden, keine Nachbarn wecken, nichts fallen lassen, nichts auslösen. */
    static final int QUIET = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS
            | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS | Block.UPDATE_SKIP_ON_PLACE;

    /**
     * Die Form: Halbmesser (größter), Tiefe der Unterseite unter dem Boden, Höhe über dem Boden; {@code jaggedTop}: die
     * Oberkante ist zerrissen (etwa der Rand eines Kraters); {@code taper}: so viel des Halbmessers bleibt an der Oberkante
     * (1 = senkrechte Wand, weniger = ein Kegel wie ein Vulkan).
     */
    public record Shape(int radius, int below, int above, boolean jaggedTop, int salt, double taper) {
        public Shape(int radius, int below, int above, boolean jaggedTop, int salt) {
            this(radius, below, above, jaggedTop, salt, 1.0);
        }

        /** Gehört die Stelle (relativ zur Mitte auf Bodenhöhe) zur Insel? */
        public boolean inside(int dx, int dy, int dz) {
            if (dy > this.above || dy < -this.below) {
                return false;
            }
            double r2 = dx * dx + dz * dz;
            if (r2 > this.radius * this.radius) {
                return false;
            }
            double r = Math.sqrt(r2);
            double angle = Math.atan2(dz, dx);
            // der zerklüftete Rand: weiche Buchten und Zacken
            double edge = this.radius * (0.82 + 0.18 * angleNoise(angle + dy * 0.021, 5.0, this.salt));
            if (dy >= 0) {
                // nach oben enger (ein Kegel), mit Rippen und Rinnen in der Flanke
                if (this.taper < 1.0) {
                    double t = (double) dy / Math.max(1, this.above);
                    edge *= Mth.lerp(t, 1.0, this.taper) * (0.92 + 0.08 * noise(angle * 6.0, dy * 0.18, 1.0, this.salt + 13));
                }
                if (this.jaggedTop) {
                    double cut = this.above - 2 - angleNoise(angle * 1.7, 9.0, this.salt + 7) * 9.0 - rand(dx, dz, this.salt + 11) * 1.6;
                    if (dy > cut) {
                        return false;
                    }
                }
                return r <= edge;
            }
            // die Unterseite: rundlich, rau, nach unten spitz zulaufend
            double t = (double) -dy / this.below;
            double shrink = Math.sqrt(Math.max(0.0, 1.0 - Math.pow(t, 1.35)));
            double rough = 0.78 + 0.22 * noise(dx * 0.9 + dy * 0.3, dz * 0.9 - dy * 0.3, 0.19, this.salt + 3);
            return r <= edge * shrink * rough;
        }
    }

    private ArenaIsland() {}

    /**
     * Kopiert das Stück Welt um {@code origin} (Boden der Mitte) aus {@code from} nach {@code target} in {@code to}. Was dort
     * vorher stand, wird zuerst abgeräumt. Gibt die Zahl der gesetzten Blöcke zurück.
     */
    public static int copy(ServerLevel from, BlockPos origin, ServerLevel to, BlockPos target, Shape shape) {
        clear(to, target, shape.radius() + 2, shape.below() + 2, shape.above() + 2);
        int ox = target.getX() - origin.getX();
        int oy = target.getY() - origin.getY();
        int oz = target.getZ() - origin.getZ();
        List<Copy> solid = new ArrayList<>();
        List<Copy> withData = new ArrayList<>();
        List<Copy> other = new ArrayList<>();
        int r = shape.radius();
        int y0 = Math.max(from.getMinY(), origin.getY() - shape.below());
        int y1 = Math.min(from.getMaxY(), origin.getY() + shape.above());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER);
        for (int cx = (origin.getX() - r) >> 4; cx <= (origin.getX() + r) >> 4; cx++) {
            for (int cz = (origin.getZ() - r) >> 4; cz <= (origin.getZ() + r) >> 4; cz++) {
                LevelChunk chunk = from.getChunk(cx, cz);
                for (int sy = SectionPos.blockToSectionCoord(y0); sy <= SectionPos.blockToSectionCoord(y1); sy++) {
                    LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sy));
                    if (section.hasOnlyAir()) {
                        continue;
                    }
                    for (int ly = 0; ly < 16; ly++) {
                        int y = (sy << 4) + ly;
                        if (y < y0 || y > y1) {
                            continue;
                        }
                        for (int lz = 0; lz < 16; lz++) {
                            int z = (cz << 4) + lz;
                            for (int lx = 0; lx < 16; lx++) {
                                int x = (cx << 4) + lx;
                                if (!shape.inside(x - origin.getX(), y - origin.getY(), z - origin.getZ())) {
                                    continue;
                                }
                                BlockState state = section.getBlockState(lx, ly, lz);
                                if (state.isAir() || !copyable(state)) {
                                    continue;
                                }
                                pos.set(x, y, z);
                                BlockPos dst = new BlockPos(x + ox, y + oy, z + oz);
                                if (state.hasBlockEntity()) {
                                    BlockEntity be = chunk.getBlockEntity(pos);
                                    net.minecraft.nbt.CompoundTag data = null;
                                    if (be != null && keepsData(be)) {
                                        TagValueOutput output = TagValueOutput.createWithContext(reporter.forChild(be.problemPath()),
                                                from.registryAccess());
                                        be.saveCustomOnly(output);
                                        data = output.buildResult();
                                    }
                                    withData.add(new Copy(dst, state, data));
                                } else if (!state.isSolidRender() && !state.isCollisionShapeFullBlock(from, pos)) {
                                    other.add(new Copy(dst, state, null));
                                } else {
                                    solid.add(new Copy(dst, state, null));
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Copy c : solid) {
            to.setBlock(c.pos, c.state, QUIET);
        }
        for (Copy c : withData) {
            to.setBlock(c.pos, c.state, QUIET);
            if (c.data != null && to.getBlockEntity(c.pos) instanceof BlockEntity be) {
                be.loadCustomOnly(TagValueInput.create(reporter.forChild(be.problemPath()), to.registryAccess(), c.data));
                be.setChanged();
            }
        }
        for (Copy c : other) {
            to.setBlock(c.pos, c.state, QUIET);
        }
        reporter.close();
        return solid.size() + withData.size() + other.size();
    }

    private record Copy(BlockPos pos, BlockState state, net.minecraft.nbt.@Nullable CompoundTag data) {}

    /** Kommt dieser Block mit (keine Portale, keine Prüfkammer-Spawner und -Tresore, keine Befehls- und Bauwerksblöcke)? */
    static boolean copyable(BlockState state) {
        Block block = state.getBlock();
        return !(block instanceof Portal) && !state.is(Blocks.END_PORTAL) && !state.is(Blocks.END_GATEWAY) && !state.is(Blocks.TRIAL_SPAWNER)
                && !state.is(Blocks.VAULT) && !state.is(Blocks.COMMAND_BLOCK) && !state.is(Blocks.CHAIN_COMMAND_BLOCK)
                && !state.is(Blocks.REPEATING_COMMAND_BLOCK) && !state.is(Blocks.STRUCTURE_BLOCK) && !state.is(Blocks.JIGSAW)
                && !state.is(Blocks.TEST_BLOCK) && !state.is(Blocks.TEST_INSTANCE_BLOCK) && !state.is(Blocks.SPAWNER);
    }

    /** Behält die Blockentität ihre Daten (nur, was man sieht: Schilder, Banner, Köpfe)? Truhen bleiben leer. */
    static boolean keepsData(BlockEntity be) {
        return be instanceof SignBlockEntity || be instanceof BannerBlockEntity || be instanceof SkullBlockEntity;
    }

    /** Räumt einen Zylinder (Halbmesser, Tiefe, Höhe um {@code center}) in {@code level} leer – schnell, Abschnitt für Abschnitt. */
    public static void clear(ServerLevel level, BlockPos center, int radius, int below, int above) {
        clear(level, center, radius, below, above, null);
    }

    /** Wie {@link #clear(ServerLevel, BlockPos, int, int, int)}, nur bleibt der Block bei {@code keep} stehen (etwa ein Altar). */
    public static void clear(ServerLevel level, BlockPos center, int radius, int below, int above, @Nullable BlockPos keep) {
        int y0 = Math.max(level.getMinY(), center.getY() - below);
        int y1 = Math.min(level.getMaxY(), center.getY() + above);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int cx = (center.getX() - radius) >> 4; cx <= (center.getX() + radius) >> 4; cx++) {
            for (int cz = (center.getZ() - radius) >> 4; cz <= (center.getZ() + radius) >> 4; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                for (int sy = SectionPos.blockToSectionCoord(y0); sy <= SectionPos.blockToSectionCoord(y1); sy++) {
                    LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sy));
                    if (section.hasOnlyAir()) {
                        continue;
                    }
                    for (int ly = 0; ly < 16; ly++) {
                        int y = (sy << 4) + ly;
                        if (y < y0 || y > y1) {
                            continue;
                        }
                        for (int lz = 0; lz < 16; lz++) {
                            for (int lx = 0; lx < 16; lx++) {
                                int x = (cx << 4) + lx;
                                int z = (cz << 4) + lz;
                                int dx = x - center.getX();
                                int dz = z - center.getZ();
                                if (dx * dx + dz * dz > radius * radius || section.getBlockState(lx, ly, lz).isAir()
                                        || keep != null && keep.getX() == x && keep.getY() == y && keep.getZ() == z) {
                                    continue;
                                }
                                level.setBlock(pos.set(x, y, z), air, QUIET);
                            }
                        }
                    }
                }
            }
        }
    }

    // =====================================================================================
    // Rauschen (fest, ohne Welt)
    // =====================================================================================

    static double rand(int a, int b, int salt) {
        long h = 0x9E3779B97F4A7C15L * salt + a * 0x632BE59BD9B4E019L + b * 0x85157AF5L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h >>> 11) * 0x1.0p-53;
    }

    static double noise(double x, double z, double scale, int salt) {
        double fx = x * scale;
        double fz = z * scale;
        int ix = Mth.floor(fx);
        int iz = Mth.floor(fz);
        double tx = smooth(fx - ix);
        double tz = smooth(fz - iz);
        double a = rand(ix, iz, salt);
        double b = rand(ix + 1, iz, salt);
        double c = rand(ix, iz + 1, salt);
        double d = rand(ix + 1, iz + 1, salt);
        return Mth.lerp(tz, Mth.lerp(tx, a, b), Mth.lerp(tx, c, d));
    }

    /** Glattes Rauschen um den Kreis ({@code lobes} Buchten auf dem ganzen Umfang). */
    static double angleNoise(double angle, double lobes, int salt) {
        double t = (angle / Mth.TWO_PI + 1.0) * lobes;
        int i = Mth.floor(t);
        int n = (int) Math.round(lobes);
        double f = smooth(t - i);
        return Mth.lerp(f, rand(Math.floorMod(i, n), 0, salt), rand(Math.floorMod(i + 1, n), 0, salt));
    }

    private static double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }
}
