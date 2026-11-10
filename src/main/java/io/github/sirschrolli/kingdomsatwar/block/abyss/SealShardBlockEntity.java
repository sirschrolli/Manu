package io.github.sirschrolli.kingdomsatwar.block.abyss;

import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** Merkt sich die Mitte des Schlunds, zu der die Lichtkette des Kronensplitters führt (auch auf dem Client). */
public class SealShardBlockEntity extends BlockEntity {
    private @Nullable BlockPos home;

    public SealShardBlockEntity(BlockPos pos, BlockState state) {
        super(AbyssBlocks.SEAL_SHARD_ENTITY.get(), pos, state);
    }

    /** Mitte des Rings (ein Block über dem Boden). */
    public void setup(BlockPos home) {
        this.home = home.immutable();
        this.setChanged();
    }

    public @Nullable BlockPos home() {
        return this.home;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.home != null) {
            output.putLong("Home", this.home.asLong());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.home = input.getLong("Home").map(BlockPos::of).orElse(null);
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(),
                org.slf4j.LoggerFactory.getLogger(SealShardBlockEntity.class))) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            if (this.home != null) {
                output.putLong("Home", this.home.asLong());
            }
            return output.buildResult();
        }
    }
}
