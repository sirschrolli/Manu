package io.github.sirschrolli.kingdomsatwar.architect.io;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

/**
 * Ein eingelesenes Stück Welt aus einer Schematik-Datei: ein Quader ab (0, 0, 0) mit seinen Blöcken (fehlende Stellen
 * sind Strukturleere) und den Blockobjekten, die der Dragon Architect übernimmt (Schilder, Banner). Außerhalb ist Luft.
 * So lässt es sich wie ein Stück der echten Welt vermessen.
 */
public final class Volume implements BlockGetter {
    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    private final BlockState[] states;
    private final Map<Long, BlockEntity> blockEntities = new HashMap<>();

    public Volume(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.states = new BlockState[sizeX * sizeY * sizeZ];
    }

    public boolean inside(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < this.sizeX && y < this.sizeY && z < this.sizeZ;
    }

    public void set(int x, int y, int z, BlockState state) {
        if (this.inside(x, y, z)) {
            this.states[(y * this.sizeZ + z) * this.sizeX + x] = state;
        }
    }

    public void setBlockEntity(BlockPos pos, BlockEntity blockEntity) {
        if (this.inside(pos.getX(), pos.getY(), pos.getZ())) {
            this.blockEntities.put(pos.asLong(), blockEntity);
        }
    }

    /** Wie viele Stellen einen echten Block tragen (keine Luft, keine Strukturleere). */
    public int blocks() {
        int n = 0;
        for (BlockState s : this.states) {
            if (s != null && !s.isAir()) {
                n++;
            }
        }
        return n;
    }

    /** Die unterste Lage mit einem echten Block (keine Luft, keine Strukturleere); {@code -1}: keine. */
    public int lowestLayer() {
        int layer = this.sizeX * this.sizeZ;
        for (int i = 0; i < this.states.length; i++) {
            BlockState s = this.states[i];
            if (s != null && !s.isAir() && !s.is(Blocks.STRUCTURE_VOID)) {
                return i / layer;
            }
        }
        return -1;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (!this.inside(pos.getX(), pos.getY(), pos.getZ())) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState s = this.states[(pos.getY() * this.sizeZ + pos.getZ()) * this.sizeX + pos.getX()];
        return s == null ? Blocks.STRUCTURE_VOID.defaultBlockState() : s;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return this.blockEntities.get(pos.asLong());
    }

    @Override
    public int getHeight() {
        return Math.max(1, this.sizeY);
    }

    @Override
    public int getMinY() {
        return 0;
    }
}
