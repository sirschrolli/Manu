package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Werkzeugkasten für den Bau eines Nests Chunk für Chunk: setzt Blöcke nur im aktuellen Chunk, liefert die
 * ursprüngliche Geländehöhe und festes Rauschen (aus dem Startwert des Nests), damit alle Chunks zusammenpassen.
 */
public final class LairContext {
    public final WorldGenLevel level;
    public final BoundingBox box;
    public final BlockPos center;
    public final long seed;

    public LairContext(WorldGenLevel level, BoundingBox box, BlockPos center, long seed) {
        this.level = level;
        this.box = box;
        this.center = center;
        this.seed = seed;
    }

    public boolean inside(int x, int y, int z) {
        return this.box.isInside(x, y, z) && y > this.level.getMinY() && y < this.level.getMaxY();
    }

    public void set(int x, int y, int z, BlockState state) {
        if (this.inside(x, y, z)) {
            this.level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
        }
    }

    /** Setzt den Block nur, wenn dort gerade Luft (oder Ersetzbares wie Gras) ist. */
    public void setIfOpen(int x, int y, int z, BlockState state) {
        if (this.inside(x, y, z)) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState old = this.level.getBlockState(pos);
            if (old.isAir() || old.canBeReplaced()) {
                this.level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Block an dieser Stelle (Luft, wenn der Chunk beim Erzeugen nicht erreichbar ist). */
    public BlockState get(int x, int y, int z) {
        if (!this.reachable(x, z)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return this.level.getBlockState(new BlockPos(x, y, z));
    }

    /** Beim Erzeugen sind nur der aktuelle Chunk und seine direkten Nachbarn lesbar. */
    public boolean reachable(int x, int z) {
        return !(this.level instanceof net.minecraft.server.level.WorldGenRegion region) || region.hasChunk(x >> 4, z >> 4);
    }

    public void fill(int x, int y0, int y1, int z, BlockState state) {
        for (int y = Math.max(y0, this.box.minY()); y <= Math.min(y1, this.box.maxY()); y++) {
            this.set(x, y, z, state);
        }
    }

    /** Erste freie Höhe über dem ursprünglichen Gelände (vor dem Umbau dieser Säule abfragen!). */
    public int originalHeight(int x, int z, boolean oceanFloor) {
        if (!this.reachable(x, z)) {
            return this.center.getY();
        }
        boolean worldgen = this.level instanceof net.minecraft.server.level.WorldGenRegion;
        Heightmap.Types type = oceanFloor
                ? (worldgen ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.OCEAN_FLOOR)
                : (worldgen ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.WORLD_SURFACE);
        return this.level.getHeight(type, x, z);
    }

    /**
     * Oberster Boden-Block einer Säule: Pflanzen, Laub, Stämme, Schnee- und Ascheschichten zählen nicht, Wasser und
     * Lava schon (der Aufrufer prüft, ob es Flüssigkeit ist).
     */
    public int groundY(int x, int z) {
        int y = this.originalHeight(x, z, false) - 1;
        int min = this.level.getMinY();
        while (y > min) {
            BlockState state = this.get(x, y, z);
            if (!state.getFluidState().isEmpty() || isGround(state)) {
                return y;
            }
            y--;
        }
        return y;
    }

    /** Fester, voller Block, der zum Gelände gehört (keine Pflanze, kein Baum). */
    public static boolean isGround(BlockState state) {
        if (state.isAir() || state.is(net.minecraft.tags.BlockTags.LOGS) || state.is(net.minecraft.tags.BlockTags.LEAVES)) {
            return false;
        }
        if (state.is(net.minecraft.world.level.block.Blocks.CACTUS) || state.is(net.minecraft.world.level.block.Blocks.PUMPKIN)
                || state.is(net.minecraft.world.level.block.Blocks.MELON) || state.is(net.minecraft.world.level.block.Blocks.BEE_NEST)
                || state.is(net.minecraft.world.level.block.Blocks.BROWN_MUSHROOM_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.RED_MUSHROOM_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.MUSHROOM_STEM)) {
            return false;
        }
        return state.isSolidRender();
    }

    /** Fester Zufall 0..1 für ganze Zahlen (gleich in jedem Chunk). */
    public double rand(int a, int b, int salt) {
        long h = this.seed * 0x9E3779B97F4A7C15L + a * 0x632BE59BD9B4E019L + b * 0x85157AF5L + salt * 0xC2B2AE3D27D4EB4FL;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h >>> 11) * 0x1.0p-53;
    }

    /** Glattes Rauschen 0..1 in der Ebene (Wertrauschen, weich interpoliert). */
    public double noise(double x, double z, double scale, int salt) {
        double fx = x * scale;
        double fz = z * scale;
        int ix = Mth.floor(fx);
        int iz = Mth.floor(fz);
        double tx = smooth(fx - ix);
        double tz = smooth(fz - iz);
        double a = this.rand(ix, iz, salt);
        double b = this.rand(ix + 1, iz, salt);
        double c = this.rand(ix, iz + 1, salt);
        double d = this.rand(ix + 1, iz + 1, salt);
        return Mth.lerp(tz, Mth.lerp(tx, a, b), Mth.lerp(tx, c, d));
    }

    /** Rauschen entlang eines Winkels (rundherum stetig), 0..1. */
    public double angleNoise(double angle, double frequency, int salt) {
        double x = Math.cos(angle) * frequency;
        double z = Math.sin(angle) * frequency;
        return this.noise(x + 100, z + 100, 1.0, salt);
    }

    public static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }
}
